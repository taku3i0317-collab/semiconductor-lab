package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModMachines;
import com.taku3.semiconductorlab.ModItems;
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
 * 今できること：耐火れんがの焼成（≈1450℃）、粘土黒鉛ボートの焼成。水素炉 [1-28] はこの炉に水素の管を足したもの（レシピで電気炉を使う）。
 */
public class ElectricFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 10;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    /** 入れる物 → 焼き上がり（1個ずつ） */
    public static Map<Item, Item> recipes() {
        return Map.of(
                ModItems.REFRACTORY_BRICK_BLANK, ModItems.REFRACTORY_BRICK,
                ModItems.GRAPHITE_BOAT_BLANK, ModItems.GRAPHITE_BOAT);
    }

    /** 画面のスロットの並び（IMP-28：前の ElectricFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 35, ElectricFurnaceBlockEntity::isInput)
            .out(SLOT_OUT, 78, 35)
            .build();

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ELECTRIC_FURNACE.type, pos, state, SLOT_COUNT, 50_000, WATTS);
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
