package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 過酸化水素の電解槽 [2-19]（1908 オーストリア・ヴァイセンシュタインの工場、1910 年ごろ生産。電解法）：
 * 冷やした濃い硫酸を、白金の陽極で大きな電流密度で電気分解すると、酸素が出る代わりに過硫酸ができる（2HSO4- → S2O8^2- + 2H+ + 2e-）。
 * これを水で温めて分解すると過酸化水素（S2O8^2- + 2H2O → 2HSO4- + H2O2）。硫酸はもとに戻るので、くり返し使う。陰極では水素が出る。
 * 全体では 2H2O → H2O2 + H2（電気で水を「酸素を1つ多く持つ水」に変える）。
 * <pre>
 *   水バケツ 1（{@link #RUNS_PER_WATER} 回）＋ 空のガラス瓶 1 ＋ 硫酸（{@link #RUNS_PER_ACID} 回で 1、ほとんど戻る）＋ 白金の陽極（{@link #RUNS_PER_ANODE} 回で 1）
 *   → 過酸化水素（約30%、ガラス瓶）1 ＋ 水素 1袋   （3kW × 60秒）
 * </pre>
 * ⚠️ 電力・時間はゲームの値。冷却・真空蒸留の段は省略（peroxide_cell.placeholder）。白金は高い電流密度でも溶けにくい唯一の実用陽極（炭素・鉛では過硫酸にならない）。
 */
public class PeroxideCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 60;
    public static final int RUNS_PER_WATER = 4, RUNS_PER_ACID = 8, RUNS_PER_ANODE = 32;
    public static final int SLOT_ACID = 0, SLOT_ANODE = 1, SLOT_WATER = 2, SLOT_BOTTLE = 3, SLOT_OUT = 4, SLOT_H2 = 5, SLOT_BUCKET = 6, SLOT_COUNT = 7;
    public static final int DATA_WATER = DATA_COUNT, EXTRA_DATA = 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_ACID, 8, 17, s -> s.is(Chemistry.SULFURIC_ACID))
            .in(SLOT_ANODE, 8, 35, s -> s.is(Metallurgy.PLATINUM_INGOT))
            .in(SLOT_WATER, 8, 53, s -> s.is(Items.WATER_BUCKET))
            .in(SLOT_BOTTLE, 26, 17, s -> s.is(Chemistry.GLASS_BOTTLE))
            .out(SLOT_OUT, 70, 17)
            .out(SLOT_H2, 70, 35)
            .out(SLOT_BUCKET, 70, 53)
            .right("power", 17, 1, "gui.semiconductor-lab.peroxide_cell.power").samples("500.0 kW")
            .right("water", 27, 1, "gui.semiconductor-lab.peroxide_cell.water").samples("4")
            .right("hint", 38, 3, "gui.semiconductor-lab.peroxide_cell.need_input", "gui.semiconductor-lab.peroxide_cell.need_anode",
                    "gui.semiconductor-lab.peroxide_cell.need_power", "gui.semiconductor-lab.peroxide_cell.running")
            .bottom("recipe", "gui.semiconductor-lab.peroxide_cell.recipe")
            .build();

    /** あと何回：水・硫酸（電解液）・白金の陽極（保存する） */
    private int water, acid, anode;

    public PeroxideCellBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.PEROXIDE_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getWater() { return water; }
    public int getAcid() { return acid; }
    public int getAnode() { return anode; }
    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_WATER ? water : 0; }
    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return (acid > 0 || container.getItem(SLOT_ACID).is(Chemistry.SULFURIC_ACID))
                && (anode > 0 || container.getItem(SLOT_ANODE).is(Metallurgy.PLATINUM_INGOT))
                && (water > 0 || (container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) && canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET))))
                && container.getItem(SLOT_BOTTLE).is(Chemistry.GLASS_BOTTLE)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.HYDROGEN_PEROXIDE))
                && canMerge(container.getItem(SLOT_H2), new ItemStack(Gas.HYDROGEN_GAS));
    }

    @Override protected void finishWork() {
        if (acid <= 0) {   // 電解液の硫酸を足す（ガラス瓶は上に戻る）。硫酸はほとんど戻るので、少しずつしか減らない
            ItemStack a = container.getItem(SLOT_ACID);
            com.taku3.semiconductorlab.machine.Vessel.returnEmpty(level, worldPosition, a, 1);
            a.shrink(1);
            acid = RUNS_PER_ACID;
        }
        if (anode <= 0) { container.getItem(SLOT_ANODE).shrink(1); anode = RUNS_PER_ANODE; }
        if (water <= 0) { container.getItem(SLOT_WATER).shrink(1); merge(SLOT_BUCKET, new ItemStack(Items.BUCKET)); water = RUNS_PER_WATER; }
        acid--; anode--; water--;
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_OUT, new ItemStack(Chemistry.HYDROGEN_PEROXIDE));
        merge(SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS));
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Water", water); out.putInt("Acid", acid); out.putInt("Anode", anode); }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in); water = in.getIntOr("Water", 0); acid = in.getIntOr("Acid", 0); anode = in.getIntOr("Anode", 0);
    }
}
