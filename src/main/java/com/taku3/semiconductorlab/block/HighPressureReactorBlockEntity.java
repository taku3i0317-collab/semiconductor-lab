package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 高圧の反応釜 [2-37]（1933 ICI のフォーセットとギブソン）：エチレンを約1400気圧・約200℃に押し込めると、ほんの少しの酸素がきっかけ（開始剤）になって
 * エチレンが次々につながり、ポリエチレンになる：n C2H4 → (C2H4)n。酸素がまったくないと始まらない（1935 ペリンが酸素のはたらきに気づいた）。
 * <pre>
 *   エチレン {@link #ETHYLENE_PER_RUN} 袋 → ポリエチレン 1   （20秒、5kW）
 *   酸素：1袋で {@link #RUNS_PER_OXYGEN} 回ぶん（本物は ppm の量）
 * </pre>
 * ⚠️ 圧力・温度の数字は見せない（GUI はふつうの画面）。未反応のエチレンの回収・分子量の違い（低密度/高密度）は扱わない（high_pressure_reactor.placeholder）。
 */
public class HighPressureReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 20;
    public static final int ETHYLENE_PER_RUN = 4, RUNS_PER_OXYGEN = 16;
    public static final int SLOT_ETHYLENE = 0, SLOT_OXYGEN = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_ETHYLENE, 8, 17, s -> s.is(Chemistry.ETHYLENE_GAS))
            .in(SLOT_OXYGEN, 8, 35, s -> s.is(Gas.OXYGEN_GAS))
            .out(SLOT_OUT, 78, 26)
            .standardTexts()
            .build();

    /** 酸素（開始剤）があと何回ぶんか（保存する） */
    private int oxygen;

    public HighPressureReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.HIGH_PRESSURE_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getOxygen() { return oxygen; }
    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack e = container.getItem(SLOT_ETHYLENE);
        return e.is(Chemistry.ETHYLENE_GAS) && e.getCount() >= ETHYLENE_PER_RUN
                && (oxygen > 0 || container.getItem(SLOT_OXYGEN).is(Gas.OXYGEN_GAS))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.POLYETHYLENE));
    }

    @Override protected void finishWork() {
        if (oxygen <= 0) {
            container.getItem(SLOT_OXYGEN).shrink(1);
            oxygen = RUNS_PER_OXYGEN;
        }
        oxygen--;
        container.getItem(SLOT_ETHYLENE).shrink(ETHYLENE_PER_RUN);
        merge(SLOT_OUT, new ItemStack(Chemistry.POLYETHYLENE));
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Oxygen", oxygen); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); oxygen = in.getIntOr("Oxygen", 0); }
}
