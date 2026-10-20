package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * アーク炉 [3-15]（モアッサン 1892 の電気炉、エルー 1900 の製鋼アーク炉）。黒鉛の電極と原料のあいだに大電流のアーク（電気の火花）を飛ばし、
 * 約2000℃以上にする。ふつうの炉では離れない酸素とケイ素を、炭素で引きはがせる。
 * [1-15] 金属シリコン：珪石 ×2 ＋ 木炭 ×1 → 金属シリコン ×1（純度 98%＝冶金グレード）。SiO2 + 2C → Si + 2CO（1個 ≈ 1kg）。
 * 電力量 12kWh/kg（本物は 1kg あたり 10kWh 以上、現代の炉で約11〜13kWh）＝ 500kW × 86.4秒。電力が足りない tick は進まない（2000℃を保てない）。
 * 黒鉛の電極（黒鉛棒）は少しずつ燃えて減る：{@link #RUNS_PER_ELECTRODE} 回で1本。
 * ⚠️ 近似：本物（現代）は交流のサブマージドアーク炉。ゲームでは交流 [4-10] が v0.4 で出るまで、直流（モアッサンの炉と同じ、⚠️要確認）で近似。
 */
public class ArcFurnaceBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final double WATTS = 500_000;
    /** 12 kWh = 43.2 MJ ÷ 500 kW = 86.4 秒 */
    public static final int TICKS = 1728;
    public static final int QUARTZ_PER_RUN = 2, CARBON_PER_RUN = 1, RUNS_PER_ELECTRODE = 8;
    public static final int SLOT_QUARTZ = 0, SLOT_CARBON = 1, SLOT_ELECTRODE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    /** いま差している電極で、あと何回動けるか（0 なら次の電極を1本使う） */
    private int electrodeRuns;

    public ArcFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARC_FURNACE, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isQuartz(ItemStack s) { return s.is(ModItems.VEIN_QUARTZ); }
    public static boolean isCarbon(ItemStack s) { return s.is(Items.CHARCOAL); }
    public static boolean isElectrode(ItemStack s) { return s.is(ModItems.GRAPHITE_ROD); }
    public int getElectrodeRuns() { return electrodeRuns; }

    private static ItemStack silicon() {
        ItemStack s = new ItemStack(ModItems.METALLURGICAL_SILICON);
        s.set(ModDataComponents.PURITY, Purity.METALLURGICAL);
        return s;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_QUARTZ).getCount() >= QUARTZ_PER_RUN && isQuartz(container.getItem(SLOT_QUARTZ))
                && container.getItem(SLOT_CARBON).getCount() >= CARBON_PER_RUN && isCarbon(container.getItem(SLOT_CARBON))
                && (electrodeRuns > 0 || isElectrode(container.getItem(SLOT_ELECTRODE)))
                && canMerge(container.getItem(SLOT_OUT), silicon());
    }

    @Override protected void finishWork() {
        if (electrodeRuns <= 0) { container.getItem(SLOT_ELECTRODE).shrink(1); electrodeRuns = RUNS_PER_ELECTRODE; }
        electrodeRuns--;
        container.getItem(SLOT_QUARTZ).shrink(QUARTZ_PER_RUN);
        container.getItem(SLOT_CARBON).shrink(CARBON_PER_RUN);   // 炭素は CO になって炉の上で燃える
        merge(SLOT_OUT, silicon());
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("ElectrodeRuns", electrodeRuns); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); electrodeRuns = in.getIntOr("ElectrodeRuns", 0); }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.arc_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ArcFurnaceMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
