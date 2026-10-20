package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * めっき槽 [2-12]（1838 ヤコビの電鋳）。硫酸銅の液に、銅の板（陽極）と、黒鉛を塗ったろうの型（陰極）を入れて直流を流すと、
 * 陰極に銅がくっついて型の形を写しとる（電鋳）。陽極の銅は同じだけ溶けるので、液（硫酸銅）は減らない：
 *   陰極 Cu2+ + 2e- → Cu、陽極 Cu → Cu2+ + 2e-
 * ファラデーの法則：くっつく銅の量は流した電気の量に比例する。m = Q × M / (z × F)（M＝63.5 g/mol、z＝2、F＝96485 C/mol）。
 * 電圧は {@link #VOLTS}（約1V）で一定とし、電流 I＝電力 ÷ 電圧。1個の電鋳＝銅 1 mol（63.5 g）で記号化した。
 * 硫酸銅がないと（ただの水）イオンが運べないので電流が流れない。
 */
public class ElectroplatingBathBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000, VOLTS = 1.0;
    public static final double FARADAY = 96485, COPPER_G_PER_MOL = 63.55, Z = 2;
    /** 1個の電鋳に要る電気の量（C）＝2F */
    public static final double COULOMBS_PER_COPY = Z * FARADAY;
    public static final int BAR_TOTAL = 1000;
    public static final int SLOT_ANODE = 0, SLOT_ELECTROLYTE = 1, SLOT_MOULD = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int DATA_DECI_GRAMS = DATA_COUNT, DATA_KC = DATA_COUNT + 1, DATA_AMPS = DATA_COUNT + 2, EXTRA_DATA = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_ANODE, 8, 17, s -> s.is(Items.COPPER_INGOT))
            .in(SLOT_ELECTROLYTE, 8, 35, s -> s.is(Chemistry.COPPER_SULFATE))
            .in(SLOT_MOULD, 8, 53, s -> s.is(Chemistry.GRAPHITE_WAX_MOULD))
            .out(SLOT_OUT, 78, 35)
            .right("power", 16, 1, "gui.semiconductor-lab.electroplating_bath.power").samples("2.0 kW")
            .right("amps", 25, 1, "gui.semiconductor-lab.electroplating_bath.amps").samples("2000 A")
            .right("charge", 34, 1, "gui.semiconductor-lab.electroplating_bath.charge").samples("193 kC")
            .right("mass", 43, 1, "gui.semiconductor-lab.electroplating_bath.mass").samples("63.5 g")
            .right("hint", 53, 2, "gui.semiconductor-lab.electroplating_bath.need_anode", "gui.semiconductor-lab.electroplating_bath.need_electrolyte",
                    "gui.semiconductor-lab.electroplating_bath.need_mould", "gui.semiconductor-lab.electroplating_bath.output_full",
                    "gui.semiconductor-lab.electroplating_bath.need_power", "gui.semiconductor-lab.electroplating_bath.plating")
            .build();

    /** 今の型に流した電気の量（C、保存する） */
    private double coulombs;
    private double lastAmps;

    public ElectroplatingBathBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.ELECTROPLATING_BATH.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** ファラデーの法則：電気の量 Q（C）でくっつく銅（g） */
    public static double copperGrams(double q) { return q * COPPER_G_PER_MOL / (Z * FARADAY); }
    public double getCoulombs() { return coulombs; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return BAR_TOTAL; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_ANODE).is(Items.COPPER_INGOT) && container.getItem(SLOT_ELECTROLYTE).is(Chemistry.COPPER_SULFATE)
                && container.getItem(SLOT_MOULD).is(Chemistry.GRAPHITE_WAX_MOULD) && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.COPPER_ELECTROTYPE));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_ANODE).shrink(1);      // 陽極の銅が同じだけ溶けた
        container.getItem(SLOT_MOULD).shrink(1);
        merge(SLOT_OUT, new ItemStack(Chemistry.COPPER_ELECTROTYPE));
        container.setChanged();
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean ready = canWork();
        setReady(ready);
        long drawn = ready ? storage.extractRaw(Math.min(storage.getStoredJ(), EnergyStorage.wattsToJoulesPerTick(WATTS))) : 0;
        double watts = drawn * 20.0;
        setLastWatts(watts);
        lastAmps = watts / VOLTS;
        if (!ready) { if (progress != 0 || coulombs != 0) { progress = 0; coulombs = 0; setChanged(); } return; }
        coulombs += lastAmps / 20.0;                   // 1 tick＝1/20 秒
        if (coulombs >= COULOMBS_PER_COPY) { coulombs = 0; finishWork(); }
        progress = (int) Math.min(BAR_TOTAL, coulombs / COULOMBS_PER_COPY * BAR_TOTAL);
        if (drawn > 0) setChanged();
        setLit(level, pos, state, drawn > 0);
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return switch (i) {
            case DATA_DECI_GRAMS -> (int) Math.round(copperGrams(coulombs) * 10);
            case DATA_KC -> (int) Math.round(coulombs / 1000);
            case DATA_AMPS -> (int) Math.round(lastAmps);
            default -> 0;
        };
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Coulombs", (int) Math.round(coulombs)); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); coulombs = in.getIntOr("Coulombs", 0); }
}
