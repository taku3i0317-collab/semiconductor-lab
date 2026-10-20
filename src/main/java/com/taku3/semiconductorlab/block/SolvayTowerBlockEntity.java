package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
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
 * ソルベー塔 [2-06]（1861 ソルベーの特許、アンモニアソーダ法）。工場を1つのブロックにまとめた：
 * <pre>
 *   石灰がま：CaCO3 → CaO + CO2
 *   炭酸化塔：NaCl + NH3 + CO2 + H2O → NaHCO3↓ + NH4Cl        （重曹は水に溶けにくいので沈む）
 *   焼く    ：2NaHCO3 → Na2CO3 + CO2 + H2O                     （この CO2 も塔へ戻る）
 *   アンモニア回収：2NH4Cl + Ca(OH)2 → 2NH3 + CaCl2 + 2H2O      （NH3 は塔へ戻る＝循環）
 *   全体    ：2NaCl + CaCO3 → Na2CO3 + CaCl2
 * </pre>
 * アンモニアは使い捨てでなく回る。最初にアンモニア水を入れると、塔の中に {@link #NH3_PER_BOTTLE} 単位たまり、
 * 1回ごとに {@link #NH3_PER_RUN} 単位が塩化アンモニウムになっては石灰で戻ってくる。もれて失うのは {@link #LOSS_EVERY} 回に1単位だけ（補給）。
 * ⚠️ 副産物の塩化カルシウム（[2-13]、DECISIONS_PENDING Q1717-8 で保留）は作らず、名前だけ（solvay_tower.placeholder）。
 */
public class SolvayTowerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 15;
    public static final int SALT_PER_RUN = 2, LIMESTONE_PER_RUN = 1, NH3_PER_BOTTLE = 4, NH3_PER_RUN = 2, LOSS_EVERY = 16;
    public static final int SLOT_SALT = 0, SLOT_LIMESTONE = 1, SLOT_AMMONIA = 2, SLOT_OUT = 3, SLOT_BOTTLE = 4, SLOT_COUNT = 5;
    public static final int DATA_NH3 = DATA_COUNT, DATA_UNTIL_LOSS = DATA_COUNT + 1, EXTRA_DATA = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_SALT, 8, 17, s -> s.is(Gas.SALT))
            .in(SLOT_LIMESTONE, 8, 35, s -> s.is(Items.CALCITE))
            .in(SLOT_AMMONIA, 8, 53, s -> s.is(Chemistry.AMMONIA_WATER))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_BOTTLE, 78, 53)
            .right("power", 16, 1, "gui.semiconductor-lab.solvay_tower.power").samples("5.0 kW")
            .right("nh3", 25, 1, "gui.semiconductor-lab.solvay_tower.nh3").samples("4")
            .right("loss", 34, 1, "gui.semiconductor-lab.solvay_tower.loss").samples("16")
            .right("hint", 44, 3, "gui.semiconductor-lab.solvay_tower.need_input", "gui.semiconductor-lab.solvay_tower.need_ammonia",
                    "gui.semiconductor-lab.solvay_tower.output_full", "gui.semiconductor-lab.solvay_tower.need_power", "gui.semiconductor-lab.solvay_tower.running")
            .build();

    /** 塔の中を回っているアンモニア（単位、保存する） */
    private int ammonia;
    /** 次に1単位失うまでの回数（保存する） */
    private int untilLoss = LOSS_EVERY;

    public SolvayTowerBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.SOLVAY_TOWER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getAmmonia() { return ammonia; }
    public int getUntilLoss() { return untilLoss; }
    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_SALT).is(Gas.SALT) && container.getItem(SLOT_SALT).getCount() >= SALT_PER_RUN
                && container.getItem(SLOT_LIMESTONE).is(Items.CALCITE)
                && (ammonia >= NH3_PER_RUN || (container.getItem(SLOT_AMMONIA).is(Chemistry.AMMONIA_WATER) && canMerge(container.getItem(SLOT_BOTTLE), new ItemStack(Chemistry.GLASS_BOTTLE))))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.SODIUM_CARBONATE));
    }

    @Override protected void finishWork() {
        while (ammonia < NH3_PER_RUN && container.getItem(SLOT_AMMONIA).is(Chemistry.AMMONIA_WATER)) {   // 補給：アンモニア水を塔に入れる
            container.getItem(SLOT_AMMONIA).shrink(1);
            merge(SLOT_BOTTLE, new ItemStack(Chemistry.GLASS_BOTTLE));
            ammonia += NH3_PER_BOTTLE;
        }
        if (ammonia < NH3_PER_RUN) return;
        container.getItem(SLOT_SALT).shrink(SALT_PER_RUN);
        container.getItem(SLOT_LIMESTONE).shrink(LIMESTONE_PER_RUN);
        merge(SLOT_OUT, new ItemStack(Chemistry.SODIUM_CARBONATE));
        // アンモニアは NH4Cl になって、石灰で NH3 に戻る（循環）。少しだけもれて失う
        if (--untilLoss <= 0) { ammonia--; untilLoss = LOSS_EVERY; }
        container.setChanged();
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_NH3 ? ammonia : i == DATA_UNTIL_LOSS ? untilLoss : 0; }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Ammonia", ammonia); out.putInt("UntilLoss", untilLoss); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); ammonia = in.getIntOr("Ammonia", 0); untilLoss = in.getIntOr("UntilLoss", LOSS_EVERY); }
}
