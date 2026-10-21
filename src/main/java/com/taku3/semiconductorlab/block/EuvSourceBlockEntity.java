package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.MultilayerMirrorItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EUV 光源（スズのプラズマ）[7-18]（IMP2 S-007、2010年代）。液滴発生器 [8-13] から落ちるスズの滴を、隣の CO2 レーザー [7-14] で撃って
 * 数十万℃のプラズマにすると、13.5nm の EUV が出る。それを多層膜ミラー [7-17]（集光鏡）で集める。
 * <ul>
 * <li>EUV の出力 ＝ 隣の CO2 レーザーの赤外線の合計 × 変換効率 {@value #CE} × 集光鏡の反射率（決定：He なしの CO2 レーザーは台数を増やして補う）</li>
 * <li>1回ごとにスズ1。水素の袋があればスズの汚れを掃除し（Sn + 4H → SnH4 の気体）、なければ集光鏡にスズが積もって反射が落ちる</li>
 * <li>EUV は空気に吸われるので、隣の真空容器が {@value #MAX_PA} Pa 以下</li>
 * </ul>
 * 本物は 20〜40kW の CO2 レーザーで 約250W の EUV。⚠️ のちの EUV 露光装置 [9-40] の光源
 */
public class EuvSourceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000, CE = 0.05, MAX_PA = 10;
    public static final int TICKS = 20 * 10;
    public static final int SLOT_TIN = 0, SLOT_DROPLET = 1, SLOT_COLLECTOR = 2, SLOT_H2 = 3, SLOT_COUNT = 4;
    public static final int DATA_EUV_MW = DATA_COUNT, DATA_LASERS = DATA_COUNT + 1, DATA_DIRTY = DATA_COUNT + 2, EXTRA_DATA = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_TIN, 8, 17, s -> s.is(Metallurgy.TIN_INGOT))
            .in(SLOT_DROPLET, 30, 17, s -> s.is(Precision.DROPLET_GENERATOR), 1)
            .in(SLOT_COLLECTOR, 8, 39, s -> s.is(Optics.MULTILAYER_MIRROR), 1)
            .in(SLOT_H2, 30, 39, s -> s.is(Gas.HYDROGEN_GAS))
            .right("power", 17, 1, "gui.semiconductor-lab.euv_source.power").samples("2.0 kW")
            .right("euv", 28, 1, "gui.semiconductor-lab.euv_source.euv").samples("12.34 W")
            .right("lasers", 39, 1, "gui.semiconductor-lab.euv_source.lasers").samples("6")
            .right("hint", 50, 3, MachineLayout.keys("gui.semiconductor-lab.euv_source.", "need_tin", "need_droplet", "need_collector", "need_vacuum",
                    "need_laser", "need_power", "running", "dirty"))
            .build();

    private int euvMilliW, lasers;
    private boolean dirty;

    public EuvSourceBlockEntity(BlockPos pos, BlockState state) {
        super(Optics.EUV_SOURCE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public double euvW() { return euvMilliW / 1000.0; }
    public int lasers() { return lasers; }

    /** 隣（6方向）の CO2 レーザー */
    private java.util.List<Co2LaserBlockEntity> adjacentLasers() {
        java.util.List<Co2LaserBlockEntity> l = new java.util.ArrayList<>();
        if (level == null) return l;
        for (Direction d : Direction.values()) if (level.getBlockEntity(worldPosition.relative(d)) instanceof Co2LaserBlockEntity c) l.add(c);
        return l;
    }

    public boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_EUV_MW ? euvMilliW : i == DATA_LASERS ? lasers : i == DATA_DIRTY ? (dirty ? 1 : 0) : 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_TIN).is(Metallurgy.TIN_INGOT) && container.getItem(SLOT_DROPLET).is(Precision.DROPLET_GENERATOR)
                && container.getItem(SLOT_COLLECTOR).is(Optics.MULTILAYER_MIRROR) && vacuumOk() && !adjacentLasers().isEmpty();
    }

    @Override protected void finishWork() {
        double ir = 0;
        int n = 0;
        for (Co2LaserBlockEntity c : adjacentLasers()) {
            double w = c.drive();
            if (w > 0) { ir += w; n++; }
        }
        lasers = n;
        ItemStack collector = container.getItem(SLOT_COLLECTOR);
        euvMilliW = (int) Math.round(ir * CE * MultilayerMirrorItem.reflectance(collector) * 1000);
        if (n == 0) { container.setChanged(); return; }
        container.getItem(SLOT_TIN).shrink(1);
        dirty = !container.getItem(SLOT_H2).is(Gas.HYDROGEN_GAS);
        if (dirty) MultilayerMirrorItem.addTin(collector, 1);   // 水素がないとスズのくずが集光鏡に積もる
        else container.getItem(SLOT_H2).shrink(1);
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("EuvMw", euvMilliW); out.putInt("Lasers", lasers); out.putBoolean("Dirty", dirty); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); euvMilliW = in.getIntOr("EuvMw", 0); lasers = in.getIntOr("Lasers", 0); dirty = in.getBooleanOr("Dirty", false); }
}
