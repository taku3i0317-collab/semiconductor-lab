package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 刻線機 [8-12]（IMP2 O-016、1882 ローランド）。精密なねじで台を少しずつ送りながら、ダイヤモンドの刃でスペキュラムの鏡 [7-05] に
 * 1mm に 600 本の溝を刻む → 刻線した回折格子（針金の格子 [7-07] より何百倍も細かく、溝の数が多いので分解能が高い）。
 * 何日もかかる間に床が揺れたり温度が変わったりすると、溝の間隔が周期的にずれて「ゴースト（にせの線）」が出る：
 * 刻み終わったときに、床の揺れ [8-07] が {@value #MAX_VIBRATION_NM}nm を超えるか、空調 [12-12] がなく温度のゆれが {@value #MAX_SWING_C}℃ を超えるとゴースト。
 * 入口：鏡（スペキュラム鏡か銀鏡）、ダイヤモンド（減らない）、送りねじ（親ねじ、減らない）。300W・60秒。⚠️ 時間・電力・ゴーストの条件はゲームの値。
 */
public class RulingEngineBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 300;
    public static final int TICKS = 20 * 60;
    public static final double MAX_VIBRATION_NM = 50, MAX_SWING_C = 1.0;
    public static final int SLOT_BLANK = 0, SLOT_DIAMOND = 1, SLOT_SCREW = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static boolean isBlank(ItemStack s) { return s.is(Optics.SPECULUM_MIRROR) || s.is(Optics.SILVERED_MIRROR); }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_BLANK, 8, 17, RulingEngineBlockEntity::isBlank, 1)
            .in(SLOT_DIAMOND, 8, 39, s -> s.is(Items.DIAMOND), 1)
            .in(SLOT_SCREW, 30, 39, s -> s.is(Precision.LEAD_SCREW) || s.is(Precision.BALL_SCREW), 1)
            .out(SLOT_OUT, 78, 28)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.3 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public RulingEngineBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.RULING_ENGINE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** その場所で刻むとゴーストが出るか（揺れと温度） */
    public static boolean ghosts(Level level, BlockPos pos) {
        return Vibration.nm(level, pos, null) > MAX_VIBRATION_NM || AirConditionerBlockEntity.swingC(level, pos) > MAX_SWING_C;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack s = container.getItem(SLOT_SCREW);
        return isBlank(container.getItem(SLOT_BLANK)) && container.getItem(SLOT_DIAMOND).is(Items.DIAMOND)
                && (s.is(Precision.LEAD_SCREW) || s.is(Precision.BALL_SCREW)) && container.getItem(SLOT_OUT).isEmpty();
    }

    @Override protected void finishWork() {
        ItemStack g = new ItemStack(Precision.RULED_GRATING);
        g.set(Precision.GRATING_GHOSTS, level != null && ghosts(level, worldPosition));
        container.setItem(SLOT_OUT, g);
        container.getItem(SLOT_BLANK).shrink(1);
        container.setChanged();
    }
}
