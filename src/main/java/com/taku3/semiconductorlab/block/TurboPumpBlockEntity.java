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
 * ターボ分子ポンプ [5-07]（IMP2 O-017、1958 ベッカー）。アルミの羽根車を毎分数万回転させ、羽根の先が気体分子の速さ（数百m/s）に近い速さで
 * 分子をはじき飛ばして前段ポンプ [5-04] へ送る。油や水銀の蒸気を使わないので、容器に蒸気が戻らない「きれいな真空」。
 * 動く条件：電気・隣に真空容器・隣に回っている回転ポンプ・容器が {@value #START_PA}Pa 以下・<b>精密な玉軸受（段階 {@value #MIN_BEARING} 以上）が入っている</b>
 * （手で丸めた玉では高速回転に耐えない。玉軸受 [8-20] で機械を右クリック）。到達 {@value #LIMIT_PA}Pa。
 * 拡散ポンプ [5-05] との違い：拡散ポンプの水銀の蒸気は容器に少し戻る（{@link #backstreams}）→ 真空蒸着でウェハーにほこりが付く。
 */
public class TurboPumpBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 300;
    public static final double START_PA = 10;
    public static final double LIMIT_PA = 1e-6;
    public static final double RATIO = 0.3;
    public static final int MIN_BEARING = 2;
    public static final int STATUS_NO_BEARING = 5;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 2)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.3 kW")
            .text("pressure", 100, 30, 72, 1, "gui.semiconductor-lab.pump.pressure").samples("0.0040 Pa")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.pump.running", "gui.semiconductor-lab.pump.no_chamber", "gui.semiconductor-lab.pump.no_backing",
                    "gui.semiconductor-lab.pump.too_high", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.pump.no_bearing")
            .build();

    public TurboPumpBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.TURBO_PUMP.type, pos, state, 0, EnergyStorage.wattsToJoulesPerTick(WATTS) * 40, 2_000);
    }

    private VacuumChamberBlockEntity chamber() { return level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition); }

    public boolean hasBacking() {
        if (level == null) return false;
        for (Direction d : Direction.values())
            if (level.getBlockEntity(worldPosition.relative(d)) instanceof RotaryPumpBlockEntity rp && rp.isRunning()) return true;
        return false;
    }

    public int status() {
        VacuumChamberBlockEntity c = chamber();
        if (c == null) return DiffusionPumpBlockEntity.STATUS_NO_CHAMBER;
        if (bearingGrade() < MIN_BEARING) return STATUS_NO_BEARING;
        if (!hasBacking()) return DiffusionPumpBlockEntity.STATUS_NO_BACKING;
        if (c.getPressure() > START_PA) return DiffusionPumpBlockEntity.STATUS_TOO_HIGH;
        return getLastWatts() > 0 ? DiffusionPumpBlockEntity.STATUS_RUNNING : DiffusionPumpBlockEntity.STATUS_NO_POWER;
    }

    @Override protected int extraDataCount() { return 2; }
    @Override protected int extraData(int i) {
        if (i == DiffusionPumpBlockEntity.DATA_PRESSURE) { VacuumChamberBlockEntity c = chamber(); return c == null ? 0 : SealingStationBlockEntity.encodePa(c.getPressure()); }
        return status();
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() {
        VacuumChamberBlockEntity c = chamber();
        return c != null && bearingGrade() >= MIN_BEARING && hasBacking() && c.getPressure() <= START_PA;
    }
    @Override protected void finishWork() {
        VacuumChamberBlockEntity c = chamber();
        if (c != null) c.pumpStroke(LIMIT_PA, RATIO);
    }

    /** 真空容器 chamberPos の隣で、動いている水銀拡散ポンプが蒸気を戻しているか（冷却トラップなし） */
    public static boolean backstreams(Level level, BlockPos chamberPos) {
        for (Direction d : Direction.values())
            if (level.getBlockEntity(chamberPos.relative(d)) instanceof DiffusionPumpBlockEntity dp && dp.getLastWatts() > 0) return true;
        return false;
    }
}
