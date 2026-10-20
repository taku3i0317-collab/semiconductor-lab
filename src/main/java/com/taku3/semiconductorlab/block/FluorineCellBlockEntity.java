package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * フッ素の電解槽 [6-14]（IMP2 O-018、1886 モアッサン）。HF はほとんど電気を通さないので、フッ化カリウム（KF）を溶かして電解する：
 * 陽極（黒鉛）2F⁻ → F2、陰極 2H⁺ → H2。槽はフッ素に侵されにくいモネル（ニッケル＋銅）。
 * 入口：電解質の KF（減らない）・陽極の黒鉛（減らない）・フッ酸（鉛の瓶、1回1本、空き瓶は上に出る）・空のボンベ。
 * 出口：フッ素のボンベ（{@value #F2_PER_CYLINDER} 回分で1本）・水素の袋（1回1袋）。
 * <b>事故</b>（DESIGN「危ない技術の扱い」⑥）：水素の出口がいっぱいで水素が逃げられないと、陽極と陰極のガスが混ざって反応し、
 * 貯めていたフッ素を失って止まる（ブロックや地形は壊れない）。工業の材料としてだけ扱う。2kW・10秒。
 */
public class FluorineCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 10;
    public static final int F2_PER_CYLINDER = 5;
    public static final int SLOT_KF = 0, SLOT_ANODE = 1, SLOT_HF = 2, SLOT_CYL = 3, SLOT_F2 = 4, SLOT_H2 = 5, SLOT_COUNT = 6;
    public static final int DATA_F2 = DATA_COUNT, DATA_ACCIDENT = DATA_COUNT + 1, EXTRA_DATA = 2;
    /** 事故の表示を出しておく tick */
    public static final int ACCIDENT_SHOW = 20 * 10;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_KF, 8, 17, s -> s.is(Chemistry.POTASSIUM_FLUORIDE), 1)
            .in(SLOT_ANODE, 30, 17, s -> s.is(Metallurgy.GRAPHITE), 1)
            .in(SLOT_HF, 8, 39, s -> s.is(Chemistry.HYDROFLUORIC_ACID))
            .in(SLOT_CYL, 30, 39, s -> s.is(Gas.EMPTY_GAS_CYLINDER))
            .out(SLOT_F2, 62, 17)
            .out(SLOT_H2, 62, 39)
            .right("power", 17, 1, "gui.semiconductor-lab.fluorine_cell.power").samples("2.0 kW")
            .right("f2", 28, 1, "gui.semiconductor-lab.fluorine_cell.f2").samples("5", "5")
            .right("hint", 39, 3, MachineLayout.keys("gui.semiconductor-lab.fluorine_cell.", "need_kf", "need_anode", "need_hf", "need_cylinder",
                    "output_full", "need_power", "running", "accident"))
            .build();

    private int f2Runs;
    private int accidentTicks;

    public FluorineCellBlockEntity(BlockPos pos, BlockState state) {
        super(Gas.FLUORINE_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int f2Runs() { return f2Runs; }
    public boolean hadAccident() { return accidentTicks > 0; }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_F2 ? f2Runs : i == DATA_ACCIDENT ? (accidentTicks > 0 ? 1 : 0) : 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        if (accidentTicks > 0) { accidentTicks--; return false; }
        return container.getItem(SLOT_KF).is(Chemistry.POTASSIUM_FLUORIDE) && container.getItem(SLOT_ANODE).is(Metallurgy.GRAPHITE)
                && container.getItem(SLOT_HF).is(Chemistry.HYDROFLUORIC_ACID) && container.getItem(SLOT_CYL).is(Gas.EMPTY_GAS_CYLINDER)
                && container.getItem(SLOT_F2).getCount() < container.getItem(SLOT_F2).getMaxStackSize();
    }

    @Override protected void finishWork() {
        Vessel.returnEmpty(level, worldPosition, container.getItem(SLOT_HF), 1);
        container.getItem(SLOT_HF).shrink(1);
        // 事故：水素が逃げられないと、陽極のフッ素と混ざって反応する
        if (!canMerge(container.getItem(SLOT_H2), new ItemStack(Gas.HYDROGEN_GAS))) {
            f2Runs = 0;
            accidentTicks = ACCIDENT_SHOW;
            container.setChanged();
            return;
        }
        merge(SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS));
        if (++f2Runs >= F2_PER_CYLINDER) {
            f2Runs = 0;
            container.getItem(SLOT_CYL).shrink(1);
            merge(SLOT_F2, new ItemStack(Gas.FLUORINE_CYLINDER));
        }
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("F2", f2Runs); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); f2Runs = in.getIntOr("F2", 0); }
}
