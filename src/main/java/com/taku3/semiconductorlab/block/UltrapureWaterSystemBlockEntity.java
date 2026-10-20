package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 超純水装置 [2-18]：イオン交換した水（脱イオン水）を、逆浸透膜（RO、酢酸セルロース）でこして残りの有機物・粒子・菌を除き、
 * 紫外線（低圧水銀灯 254nm）で菌を殺して、ポリエチレンの瓶に詰める。抵抗率は水そのものの電離で決まる上限 18.2 MΩ·cm（25℃）。
 * <pre>
 *   脱イオン水（ガラス瓶）1 ＋ 空のポリエチレンの瓶 1 → 超純水（ポリエチレンの瓶）1 ＋ 空のガラス瓶 1   （5秒、1kW）
 * </pre>
 * 蒸留水そのままは入らない（先にイオン交換）。⚠️ 膜の目づまり・水の循環（本物は止めずに回し続ける）は扱わない（ultrapure_water_system.placeholder）。
 */
public class UltrapureWaterSystemBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 5;
    public static final double RESISTIVITY_MOHM_CM = 18.2;
    public static final int SLOT_IN = 0, SLOT_BOTTLE = 1, SLOT_OUT = 2, SLOT_GLASS = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 17, s -> s.is(Chemistry.DEIONIZED_WATER))
            .in(SLOT_BOTTLE, 8, 35, s -> s.is(Chemistry.POLYETHYLENE_BOTTLE))
            .out(SLOT_OUT, 78, 17)
            .out(SLOT_GLASS, 78, 35)
            .standardTexts()
            .build();

    public UltrapureWaterSystemBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.ULTRAPURE_WATER_SYSTEM.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_IN).is(Chemistry.DEIONIZED_WATER) && container.getItem(SLOT_BOTTLE).is(Chemistry.POLYETHYLENE_BOTTLE)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.ULTRAPURE_WATER))
                && canMerge(container.getItem(SLOT_GLASS), new ItemStack(Chemistry.GLASS_BOTTLE));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(1);
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_OUT, new ItemStack(Chemistry.ULTRAPURE_WATER));
        merge(SLOT_GLASS, new ItemStack(Chemistry.GLASS_BOTTLE));
        container.setChanged();
    }
}
