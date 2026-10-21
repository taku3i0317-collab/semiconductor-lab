package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * 電気炉 [4-12]（抵抗加熱）。耐火れんがの箱の中で黒鉛棒に電流を流し、ジュール熱（電気抵抗で出る熱、P = I²R）で約1500℃まで熱する。
 * 燃料を燃やさないので、煙・灰・硫黄が中身に入らない。
 * 今できること：耐火れんがの焼成（≈1450℃）、粘土黒鉛ボートの焼成、鉄の圧粉体の焼結（[1-72]、約1100℃、溶かさずに粉どうしをくっつける）。水素炉 [1-28] はこの炉に水素の管を足したもの（レシピで電気炉を使う）。
 */
public class ElectricFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 10;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    /** 入れる物 → 焼き上がり（1個ずつ） */
    public static Map<Item, Item> recipes() {
        return Map.of(
                Heat.REFRACTORY_BRICK_BLANK, Heat.REFRACTORY_BRICK,
                Semiconductor.GRAPHITE_BOAT_BLANK, Semiconductor.GRAPHITE_BOAT,
                com.taku3.semiconductorlab.content.Metallurgy.IRON_COMPACT, com.taku3.semiconductorlab.content.Metallurgy.SINTERED_GEAR,   // [1-72] 焼結（IMP3、O-024）
                com.taku3.semiconductorlab.content.Metallurgy.NDFEB_COMPACT, com.taku3.semiconductorlab.content.Metallurgy.SINTERED_NDFEB,   // [1-35] ネオジム磁石の焼結
                com.taku3.semiconductorlab.content.Metallurgy.NEODYMIUM_ELECTROLYSIS_CHARGE, com.taku3.semiconductorlab.content.Metallurgy.NEODYMIUM,   // [1-35] 溶融塩電解の近似 ⚠️
                com.taku3.semiconductorlab.content.Metallurgy.STAINLESS_CHARGE, com.taku3.semiconductorlab.content.Metallurgy.STAINLESS_STEEL);   // [1-62] ステンレス鋼（IMP3、O-029）
    }

    /** 画面のスロットの並び（IMP-28：前の ElectricFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 35, ElectricFurnaceBlockEntity::isInput)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.hydrogen_furnace.power").samples("5.0 kW")
            .right("stored", 27, 1, "gui.semiconductor-lab.hydrogen_furnace.stored").samples("50 kJ")
            .right("hint", 38, 3, "gui.semiconductor-lab.electric_furnace.need_input", "gui.semiconductor-lab.electric_furnace.output_full", "gui.semiconductor-lab.electric_furnace.need_power", "gui.semiconductor-lab.electric_furnace.heating")
            .build();

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Power.ELECTRIC_FURNACE.type, pos, state, SLOT_COUNT, 50_000, WATTS);
    }

    public static boolean isInput(ItemStack s) { return recipes().containsKey(s.getItem()); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack in = container.getItem(SLOT_IN);
        return isInput(in) && canMerge(container.getItem(SLOT_OUT), new ItemStack(recipes().get(in.getItem())));
    }

    @Override protected void finishWork() {
        ItemStack in = container.getItem(SLOT_IN);
        ItemStack out = new ItemStack(recipes().get(in.getItem()));
        in.shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

}
