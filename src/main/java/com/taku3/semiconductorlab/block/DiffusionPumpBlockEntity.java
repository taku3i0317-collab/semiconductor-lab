package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 水銀拡散ポンプ [5-05]（IMP2-01）：ゲーデ（1915）のガラスと鉄のポンプ。下の鉄の釜で水銀を沸かし（ヒーター {@link #WATTS}）、
 * 蒸気の噴流を下向きに吹いて、気体分子をたたき落として前段ポンプへ送る。動く部品がない。
 * 動く条件：電気がある・隣に真空容器・隣に回っている回転ポンプ [5-04]（前段）・容器が {@link #START_PA} 以下（大気圧からは引けない）。
 * 1秒ごとに p' = LIMIT + (p − LIMIT)·RATIO。漏れ（0.002Pa/秒）とつり合って、ベルジャーは約 0.004Pa まで下がる。
 */
public class DiffusionPumpBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 400;
    /** これより高い圧力では噴流が乱れて引けない（前段ポンプで先に下げる） */
    public static final double START_PA = 10;
    /** 到達圧力（Pa）。水銀拡散ポンプは 10⁻⁴Pa 級（冷却トラップなしの水銀蒸気の分は省略） */
    public static final double LIMIT_PA = 1e-4;
    public static final double RATIO = 0.5;

    /** GUI へ送る追加の数値：相手の容器の圧力（SealingStationBlockEntity.encodePa）と状態（STATUS_*） */
    public static final int DATA_PRESSURE = ConsumerBlockEntity.DATA_COUNT, DATA_STATUS = ConsumerBlockEntity.DATA_COUNT + 1;
    public static final int STATUS_RUNNING = 0, STATUS_NO_CHAMBER = 1, STATUS_NO_BACKING = 2, STATUS_TOO_HIGH = 3, STATUS_NO_POWER = 4;
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 2)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.4 kW")
            .text("pressure", 100, 30, 72, 1, "gui.semiconductor-lab.pump.pressure").samples("0.0040 Pa")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.pump.running", "gui.semiconductor-lab.pump.no_chamber", "gui.semiconductor-lab.pump.no_backing", "gui.semiconductor-lab.pump.too_high", "gui.semiconductor-lab.machine.need_power")
            .build();

    @Override protected int extraDataCount() { return 2; }
    @Override protected int extraData(int i) {
        if (i == 0) { VacuumChamberBlockEntity c = chamber(); return c == null ? 0 : SealingStationBlockEntity.encodePa(c.getPressure()); }
        return status();
    }

    public DiffusionPumpBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.DIFFUSION_PUMP.type, pos, state, 0, EnergyStorage.wattsToJoulesPerTick(WATTS) * 40, 2_000);
    }

    private VacuumChamberBlockEntity chamber() { return level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition); }

    /** 隣に回っている回転ポンプ（前段）があるか */
    public boolean hasBacking() {
        if (level == null) return false;
        for (Direction d : Direction.values())
            if (level.getBlockEntity(worldPosition.relative(d)) instanceof RotaryPumpBlockEntity rp && rp.isRunning()) return true;
        return false;
    }

    /** 状態（GUI 用） */
    public int status() {
        VacuumChamberBlockEntity c = chamber();
        if (c == null) return STATUS_NO_CHAMBER;
        if (!hasBacking()) return STATUS_NO_BACKING;
        if (c.getPressure() > START_PA) return STATUS_TOO_HIGH;
        return getLastWatts() > 0 ? STATUS_RUNNING : STATUS_NO_POWER;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() {
        VacuumChamberBlockEntity c = chamber();
        return c != null && hasBacking() && c.getPressure() <= START_PA;
    }
    @Override protected void finishWork() {
        VacuumChamberBlockEntity c = chamber();
        if (c != null) c.pumpStroke(LIMIT_PA, RATIO);
    }
}
