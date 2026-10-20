package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Agriculture;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 冷凍機 [11-13]（IMP2 S-003、1876 リンデのアンモニア圧縮式）。アンモニアを圧縮して放熱し、膨張させて蒸発させると、まわりの熱をうばう。
 * 水バケツ → 氷（バケツは戻る）。冷媒のアンモニアは中を回るだけで減らない（入口に1つ入れておく）。1kW・20秒。
 * のちの空調 [12-12] の心臓部。⚠️ 冷媒は本物は無水アンモニア（ここではアンモニア水 [2-21] で近似）。
 */
public class RefrigeratorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 20;
    public static final int SLOT_WATER = 0, SLOT_REFRIGERANT = 1, SLOT_ICE = 2, SLOT_BUCKET = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WATER, 8, 17, s -> s.is(Items.WATER_BUCKET))
            .in(SLOT_REFRIGERANT, 8, 39, s -> s.is(Chemistry.AMMONIA_WATER), 1)
            .out(SLOT_ICE, 78, 17)
            .out(SLOT_BUCKET, 78, 39)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("1.0 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public RefrigeratorBlockEntity(BlockPos pos, BlockState state) {
        super(Agriculture.REFRIGERATOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) && container.getItem(SLOT_REFRIGERANT).is(Chemistry.AMMONIA_WATER)
                && canMerge(container.getItem(SLOT_ICE), new ItemStack(Items.ICE)) && canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_WATER).shrink(1);
        merge(SLOT_ICE, new ItemStack(Items.ICE));
        merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
        container.setChanged();
    }
}
