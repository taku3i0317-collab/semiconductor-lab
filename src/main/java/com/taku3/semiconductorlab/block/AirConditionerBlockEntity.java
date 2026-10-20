package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Construction;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 空調機 [12-12] 換気・空調（IMP2 S-003、1902 キャリア）。冷凍機 [11-13] の冷やす仕組みと送風機で、まわりの温度と湿度をそろえる。
 * 電気が来て動いている間、{@value #RANGE} ブロック以内の温度のゆれが ±{@value #SWING_OFF_C}℃ → ±{@value #SWING_ON_C}℃ になる（{@link #swingC}）。
 * 精密な機械はこの温度のゆれで伸び縮みする：マスクアライナー [9-20] の重ね合わせ [8-17] に「温度のずれ」として足される（[8-07] 恒温）。
 * ⚠️ 数値はゲームの値（鋼は 1℃・1m で約 11µm 伸びる。台の長さ 100mm として ±3℃ で約 3µm）。
 */
public class AirConditionerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int RANGE = 8;
    public static final double SWING_OFF_C = 3.0, SWING_ON_C = 0.1;
    /** 精密な台の長さ（m）と鋼の線膨張（1/℃）：温度のずれ（nm）＝ 長さ × 膨張 × ゆれ */
    public static final double STAGE_M = 0.1, STEEL_PER_C = 11e-6;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .text("power", 8, 18, 160, 1, "gui.semiconductor-lab.machine.power").samples("2.0 kW")
            .text("stored", 8, 30, 160, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 8, 42, 160, 2, "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public AirConditionerBlockEntity(BlockPos pos, BlockState state) {
        super(Construction.AIR_CONDITIONER.type, pos, state, 0, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return true; }   // 材料なし：電気が来ている間ずっと動く
    @Override protected void finishWork() {}

    /** pos のまわりで、動いている空調機があるか */
    public static boolean conditioned(Level level, BlockPos pos) {
        for (BlockPos q : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE)))
            if (level.getBlockEntity(q) instanceof AirConditionerBlockEntity ac && ac.getLastWatts() > 0) return true;
        return false;
    }

    /** pos の温度のゆれ（±℃） */
    public static double swingC(Level level, BlockPos pos) { return conditioned(level, pos) ? SWING_ON_C : SWING_OFF_C; }

    /** 温度のゆれで精密な台が伸び縮みする量（nm） */
    public static double thermalNm(double swingC) { return STAGE_M * STEEL_PER_C * swingC * 1e9; }
}
