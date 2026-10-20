package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 回転ポンプ [5-04] 段階①（IMP2-01）：ゲーデの回転水銀ポンプ（1905）型。旋盤で削った偏心ローターをモーターで回し、
 * すき間を水銀で封じて気体を区切って押し出す。電気 {@link #WATTS}。1秒ごとに p' = LIMIT + (p − LIMIT)·RATIO。
 * 隣の真空容器を引く。容器が隣になくても、隣の拡散ポンプ [5-05] の向こうの容器を引く（拡散ポンプの「前段ポンプ」）。
 * 段階②ひまし油・③シリコーン油は後日（CHEMISTRY #39）。
 */
public class RotaryPumpBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 250;
    /** 到達圧力（Pa）。⚠️ 段階①はゲームの値（油回転ポンプの 1〜0.1Pa の上の端） */
    public static final double LIMIT_PA = 1.0;
    /** 1秒で残る割合（1気圧 → 1Pa まで約17秒） */
    public static final double RATIO = 0.5;

    /** GUI へ送る追加の数値：相手の容器の圧力（SealingStationBlockEntity.encodePa）と状態（STATUS_*） */
    public static final int DATA_PRESSURE = ConsumerBlockEntity.DATA_COUNT, DATA_STATUS = ConsumerBlockEntity.DATA_COUNT + 1;
    public static final int STATUS_RUNNING = 0, STATUS_NO_CHAMBER = 1, STATUS_NO_BACKING = 2, STATUS_TOO_HIGH = 3, STATUS_NO_POWER = 4;
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 2)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.3 kW")
            .text("pressure", 100, 30, 72, 1, "gui.semiconductor-lab.pump.pressure").samples("0.0040 Pa")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.pump.running", "gui.semiconductor-lab.pump.no_chamber", "gui.semiconductor-lab.pump.no_backing", "gui.semiconductor-lab.pump.too_high", "gui.semiconductor-lab.machine.need_power")
            .build();

    @Override protected int extraDataCount() { return 2; }
    @Override protected int extraData(int i) {
        if (i == 0) { VacuumChamberBlockEntity c = target(); return c == null ? 0 : SealingStationBlockEntity.encodePa(c.getPressure()); }
        return status();
    }

    public RotaryPumpBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.ROTARY_PUMP.type, pos, state, 0, EnergyStorage.wattsToJoulesPerTick(WATTS) * 40, 2_000);
    }

    /** 引く相手の容器：隣の容器、なければ隣の拡散ポンプの向こうの容器 */
    public VacuumChamberBlockEntity target() {
        if (level == null) return null;
        VacuumChamberBlockEntity c = VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        if (c != null) return c;
        for (Direction d : Direction.values())
            if (level.getBlockEntity(worldPosition.relative(d)) instanceof DiffusionPumpBlockEntity dp) {
                VacuumChamberBlockEntity cc = VacuumChamberBlockEntity.findAdjacent(level, dp.getBlockPos());
                if (cc != null) return cc;
            }
        return null;
    }

    /** 状態（GUI 用） */
    public int status() { return target() == null ? STATUS_NO_CHAMBER : isRunning() ? STATUS_RUNNING : STATUS_NO_POWER; }

    /** モーターが回っているか（今の tick で電気を使った） */
    public boolean isRunning() { return getLastWatts() > 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return target() != null; }
    /** 1秒ごとに1回引く */
    @Override protected void finishWork() {
        VacuumChamberBlockEntity c = target();
        if (c != null) c.pumpStroke(LIMIT_PA, RATIO);
    }
}
