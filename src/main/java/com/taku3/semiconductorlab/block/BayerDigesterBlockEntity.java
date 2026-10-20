package com.taku3.semiconductorlab.block;

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
 * バイヤー法の溶解釜 [1-14]（1888 カール・ヨーゼフ・バイヤー）：ボーキサイトを熱い濃い水酸化ナトリウムの液で溶かす（約150〜250℃の圧力釜）。
 * Al(OH)3 + NaOH → NaAl(OH)4 で溶け、鉄の酸化物は溶けずに残る（赤泥）。こして冷やし、種を入れると Al(OH)3 が出てくる（逆の反応）。
 * 液（NaOH）はくり返し使う。Al(OH)3 はかまどで焼くとアルミナ Al2O3（作業台ではなく焼く：別のレシピ）。
 * <pre>
 *   ボーキサイト 2 ＋（NaOH・水は {@link #RUNS_PER_LIQUOR} 回で 1 ずつ）→ 水酸化アルミニウム 1 ＋ 赤泥 1（30秒、2kW）
 * </pre>
 * ⚠️ 加熱は電気ヒーター（本物は蒸気で熱した圧力釜。bayer_digester.placeholder）。量は記号的（本物はボーキサイト約2〜3トンでアルミナ1トン）。
 * 母液にたまるガリウム（[1-44]）は、そのノードで。
 */
public class BayerDigesterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 30;
    public static final int BAUXITE_PER_RUN = 2, RUNS_PER_LIQUOR = 4;
    public static final int DATA_LIQUOR = DATA_COUNT, EXTRA_DATA = 1;
    public static final int SLOT_BAUXITE = 0, SLOT_NAOH = 1, SLOT_WATER = 2, SLOT_OUT = 3, SLOT_MUD = 4, SLOT_BUCKET = 5, SLOT_COUNT = 6;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_BAUXITE, 8, 17, s -> s.is(Metallurgy.BAUXITE))
            .in(SLOT_NAOH, 8, 35, s -> s.is(Gas.SODIUM_HYDROXIDE))
            .in(SLOT_WATER, 8, 53, s -> s.is(Items.WATER_BUCKET))
            .out(SLOT_OUT, 56, 17)
            .out(SLOT_MUD, 56, 35)
            .out(SLOT_BUCKET, 56, 53)
            .right("power", 17, 1, "gui.semiconductor-lab.bayer_digester.power").samples("500.0 kW")
            .right("liquor", 27, 1, "gui.semiconductor-lab.bayer_digester.liquor").samples("4")
            .right("hint", 38, 3, "gui.semiconductor-lab.bayer_digester.need_input", "gui.semiconductor-lab.bayer_digester.need_liquor", "gui.semiconductor-lab.bayer_digester.need_power", "gui.semiconductor-lab.bayer_digester.running")
            .bottom("recipe", "gui.semiconductor-lab.bayer_digester.recipe")
            .build();

    /** 釜の中の液があと何回ぶん使えるか（保存する） */
    private int liquor;

    public BayerDigesterBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.BAYER_DIGESTER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getLiquor() { return liquor; }
    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_LIQUOR ? liquor : 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    /** 液が残っているか、NaOH と水を足して作れるか */
    private boolean hasLiquor() {
        return liquor > 0 || (container.getItem(SLOT_NAOH).is(Gas.SODIUM_HYDROXIDE) && container.getItem(SLOT_WATER).is(Items.WATER_BUCKET)
                && canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET)));
    }

    @Override protected boolean canWork() {
        ItemStack b = container.getItem(SLOT_BAUXITE);
        return b.is(Metallurgy.BAUXITE) && b.getCount() >= BAUXITE_PER_RUN && hasLiquor()
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Metallurgy.ALUMINIUM_HYDROXIDE))
                && canMerge(container.getItem(SLOT_MUD), new ItemStack(Metallurgy.RED_MUD));
    }

    @Override protected void finishWork() {
        if (liquor <= 0) {   // 新しい液を作る：NaOH 1 を水 1 杯に溶かす（空のバケツは出口へ）
            container.getItem(SLOT_NAOH).shrink(1);
            container.getItem(SLOT_WATER).shrink(1);
            merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
            liquor = RUNS_PER_LIQUOR;
        }
        liquor--;
        container.getItem(SLOT_BAUXITE).shrink(BAUXITE_PER_RUN);
        merge(SLOT_OUT, new ItemStack(Metallurgy.ALUMINIUM_HYDROXIDE));
        merge(SLOT_MUD, new ItemStack(Metallurgy.RED_MUD));
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Liquor", liquor); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); liquor = in.getIntOr("Liquor", 0); }
}
