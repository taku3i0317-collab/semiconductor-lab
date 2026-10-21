package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.KilogramPrototypeItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ワット天秤（キッブル天秤）[8-25]（IMP2 S-005 案A＝古典版、1975 キッブル）。
 * ① 重さの段：コイルに電流 I を流して、磁石の中でおもり（キログラム原器）の重さ mg と釣り合わせる。
 * ② 動かす段：同じコイルを速さ v で動かし、出てくる電圧 U を測る。磁石の強さが消えて mgv = UI。
 * 電圧はウェストン標準電池（1893）、抵抗は標準抵抗、速さはクリプトン86 ランプの干渉縞で測る（He を使う光源は使わない決定）。
 * 測るたびに、原器の 1 kg からのずれ（µg）と、そのあいまいさ（±µg）が出る。あいまいさは：
 * <ul>
 * <li>標準電池の再現性 {@value #CELL_REL}、標準抵抗 {@value #RES_REL}</li>
 * <li>標準電池の温度係数 {@value #CELL_TEMP_REL}/K × 部屋の温度のゆれ（空調 [12-12] なし ±3℃・あり ±0.1℃）</li>
 * <li>空気の浮力 {@value #BUOYANCY_REL} × 圧力/大気圧（隣の真空容器の圧力。なければ大気）</li>
 * </ul>
 * 一番よくしても 10⁻⁶ ほど（数 mg）で、原器どうしの差（約 50µg）は見えない。本物は量子標準（ジョセフソン・量子ホール、液体ヘリウム）で 10⁻⁸ ⚠️説明だけ。
 */
public class KibbleBalanceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 200;
    public static final int TICKS = 20 * 30;
    /** ⚠️ 代表値：ウェストン電池の再現性・温度係数（約 −40µV/K ÷ 1.0186V）、標準抵抗、白金の円柱の空気の浮力（1.2/21450） */
    public static final double CELL_REL = 1e-6, CELL_TEMP_REL = 4e-5, RES_REL = 2e-6, BUOYANCY_REL = 5.6e-5;
    public static final int SLOT_MASS = 0, SLOT_CELL = 1, SLOT_RES = 2, SLOT_LAMP = 3, SLOT_COUNT = 4;
    public static final int DATA_RESULT = DATA_COUNT, DATA_SIGMA = DATA_COUNT + 1, DATA_HAS = DATA_COUNT + 2, EXTRA_DATA = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_MASS, 8, 17, s -> s.is(Precision.KILOGRAM_PROTOTYPE), 1)
            .in(SLOT_CELL, 30, 17, s -> s.is(Precision.WESTON_CELL), 1)
            .in(SLOT_RES, 8, 39, s -> s.is(Precision.STANDARD_RESISTOR), 1)
            .in(SLOT_LAMP, 30, 39, s -> s.is(Precision.KRYPTON_LAMP), 1)
            .right("power", 17, 1, "gui.semiconductor-lab.kibble_balance.power").samples("0.2 kW")
            .right("result", 28, 1, "gui.semiconductor-lab.kibble_balance.result").samples("+123.4 mg")
            .right("sigma", 39, 1, "gui.semiconductor-lab.kibble_balance.sigma").samples("123.4 mg")
            .right("hint", 50, 3, MachineLayout.keys("gui.semiconductor-lab.kibble_balance.", "need_mass", "need_cell", "need_resistor", "need_lamp",
                    "need_power", "running", "hidden", "seen"))
            .build();

    private long result, sigma;
    private boolean has;

    public KibbleBalanceBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.KIBBLE_BALANCE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** あいまいさ（µg）：温度のゆれ ±c ℃、圧力 pa のとき */
    public static double sigmaUg(double c, double pa) {
        double t = CELL_TEMP_REL * c, b = BUOYANCY_REL * pa / VacuumChamberBlockEntity.ATM;
        return Math.sqrt(CELL_REL * CELL_REL + RES_REL * RES_REL + t * t + b * b) * 1e9;
    }

    public long result() { return result; }
    public long sigma() { return sigma; }
    public boolean hasResult() { return has; }

    private double pressure() {
        var c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c == null ? VacuumChamberBlockEntity.ATM : c.getPressure();
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return i == DATA_RESULT ? (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, result))
                : i == DATA_SIGMA ? (int) Math.min(Integer.MAX_VALUE, sigma) : i == DATA_HAS ? (has ? 1 : 0) : 0;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_MASS).is(Precision.KILOGRAM_PROTOTYPE) && container.getItem(SLOT_CELL).is(Precision.WESTON_CELL)
                && container.getItem(SLOT_RES).is(Precision.STANDARD_RESISTOR) && container.getItem(SLOT_LAMP).is(Precision.KRYPTON_LAMP);
    }

    @Override protected void finishWork() {
        if (level == null) return;
        ItemStack mass = container.getItem(SLOT_MASS);
        KilogramPrototypeItem.ensure(mass, level);
        double s = sigmaUg(AirConditionerBlockEntity.swingC(level, worldPosition), pressure());
        long truth = KilogramPrototypeItem.massUg(mass, level.getGameTime() / KilogramPrototypeItem.DAY_TICKS);
        result = truth + Math.round(level.getRandom().nextGaussian() * s);
        sigma = Math.round(s);
        has = true;
        ItemStack lamp = container.getItem(SLOT_LAMP);
        lamp.setDamageValue(lamp.getDamageValue() + 1);
        if (lamp.getDamageValue() >= lamp.getMaxDamage()) container.setItem(SLOT_LAMP, ItemStack.EMPTY);
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putLong("Result", result); out.putLong("Sigma", sigma); out.putBoolean("Has", has); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); result = in.getLongOr("Result", 0); sigma = in.getLongOr("Sigma", 0); has = in.getBooleanOr("Has", false); }
}
