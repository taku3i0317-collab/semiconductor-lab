package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * トリクロロシラン反応器＋蒸留塔 [1-16]（1950年代〜、シーメンス法の前半）。
 * 合成：金属シリコン ×1 ＋ 乾いた塩化水素 ×3 → 粗トリクロロシラン ×1（純度 99%）＋ 水素 ×1。Si + 3HCl → SiHCl3 + H2（約300℃、発熱）。
 * 蒸留：トリクロロシラン ×1 → 1段ごとに不純物（B・P・Fe・Al の塩化物）を 1/10 に＝「9」が1つ増える。上限 11N。SiHCl3 の沸点は 32℃、
 * BCl3 は 12℃、PCl3 は 76℃なので、沸点の差で分けられる。
 * 蒸留のスロットにトリクロロシランがあれば蒸留を、なければ合成をする。
 * ⚠️ 量は記号的：金属シリコン1個≈1kg、ガス1袋＝1mol で、本物の比（1kg の Si に HCl 107mol）とは合わない（DECISIONS_PENDING Q22-1）。
 * ⚠️ 電力・時間はゲームの都合（本物の合成は発熱反応で、電気は主に蒸留の加熱と冷却に使う）。
 * IMP2（S-001）：合成 {@link #SICL4_EVERY} 回ごとに副産物の四塩化ケイ素 SiCl4 が1つ出る（Si + 4HCl → SiCl4 + 2H2。本物の合成でも 1〜2割は SiCl4）。
 * 出口は SLOT_SICL4。合成石英 [7-15]・エピタキシー [9-30] の材料。それ以外の動きは前と同じ。
 */
public class TrichlorosilaneReactorBlockEntity extends ConsumerBlockEntity {
    public static final double SYNTH_WATTS = 10_000, DISTILL_WATTS = 20_000;
    /** 合成 10kW × 60秒、蒸留1段 20kW × 30秒 */
    public static final int SYNTH_TICKS = 1200, DISTILL_TICKS = 600;
    public static final int SI_PER_RUN = 1, HCL_PER_RUN = 3, H2_PER_RUN = 1;
    /** 粗トリクロロシランの純度（2N）。ゲームの段階、⚠️ */
    public static final double CRUDE = 0.99;
    public static final double MAX = Purity.ELEVEN_N;
    public static final int SLOT_SI = 0, SLOT_HCL = 1, SLOT_DISTILL = 2, SLOT_OUT = 3, SLOT_H2 = 4, SLOT_SICL4 = 5, SLOT_COUNT = 6;
    /** 何回の合成ごとに SiCl4 が1つ出るか（IMP2、S-001。⚠️ ゲームの値：本物は合成の1〜2割が SiCl4） */
    public static final int SICL4_EVERY = 4;
    public static final int DATA_MODE = DATA_COUNT;   // 0＝合成、1＝蒸留

    private boolean distilling;
    /** 合成した回数（SiCl4 の割合を決める。保存する） */
    private int synthRuns;

    /** 画面のスロットの並び（IMP-28：前の TrichlorosilaneReactorMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_SI, 8, 17, TrichlorosilaneReactorBlockEntity::isSilicon)
            .in(SLOT_HCL, 8, 35, TrichlorosilaneReactorBlockEntity::isHcl)
            .in(SLOT_DISTILL, 8, 53, TrichlorosilaneReactorBlockEntity::isTcs)
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_H2, 78, 50)
            .out(SLOT_SICL4, 56, 17)
            .right("power", 17, 1, "gui.semiconductor-lab.trichlorosilane_reactor.power").samples("20.0 kW")
            .right("need", 27, 1, "gui.semiconductor-lab.trichlorosilane_reactor.need").samples("20 kW")
            .right("hint", 38, 3, "gui.semiconductor-lab.trichlorosilane_reactor.need_input", "gui.semiconductor-lab.trichlorosilane_reactor.need_power", "gui.semiconductor-lab.trichlorosilane_reactor.distilling", "gui.semiconductor-lab.trichlorosilane_reactor.running")
            .bottom("recipe", "gui.semiconductor-lab.trichlorosilane_reactor.recipe", "gui.semiconductor-lab.trichlorosilane_reactor.recipe_distill")
            .build();

    public TrichlorosilaneReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.TRICHLOROSILANE_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(DISTILL_WATTS) * 4, DISTILL_WATTS);
    }

    public static boolean isSilicon(ItemStack s) { return s.is(Metallurgy.METALLURGICAL_SILICON); }
    public static boolean isHcl(ItemStack s) { return s.is(Metallurgy.HYDROGEN_CHLORIDE_GAS); }
    public static boolean isTcs(ItemStack s) { return s.is(Metallurgy.TRICHLOROSILANE); }

    public static ItemStack tcs(double purity) {
        ItemStack s = new ItemStack(Metallurgy.TRICHLOROSILANE);
        s.set(ModDataComponents.PURITY, purity);
        return s;
    }
    /** 蒸留1段：不純物を 1/10 に（上限 11N） */
    public static double distilled(double p) { int n = Math.min(Purity.nines(MAX), Purity.nines(p) + 1); return 1.0 - Math.pow(10, -n); }
    public static double purityOf(ItemStack s) { Double p = s.get(ModDataComponents.PURITY); return p == null ? CRUDE : p; }

    @Override protected double workWatts() { return distilling ? DISTILL_WATTS : SYNTH_WATTS; }
    @Override protected int workTicks() { return distilling ? DISTILL_TICKS : SYNTH_TICKS; }

    @Override protected boolean canWork() {
        ItemStack d = container.getItem(SLOT_DISTILL);
        if (isTcs(d)) {
            distilling = true;
            return Purity.nines(purityOf(d)) < Purity.nines(MAX) && canMerge(container.getItem(SLOT_OUT), tcs(distilled(purityOf(d))));
        }
        distilling = false;
        return isSilicon(container.getItem(SLOT_SI)) && container.getItem(SLOT_SI).getCount() >= SI_PER_RUN
                && isHcl(container.getItem(SLOT_HCL)) && container.getItem(SLOT_HCL).getCount() >= HCL_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), tcs(CRUDE))
                && canMerge(container.getItem(SLOT_H2), new ItemStack(Gas.HYDROGEN_GAS, H2_PER_RUN))
                && ((synthRuns + 1) % SICL4_EVERY != 0 || canMerge(container.getItem(SLOT_SICL4), new ItemStack(Metallurgy.SILICON_TETRACHLORIDE)));
    }

    @Override protected void finishWork() {
        if (distilling) {
            ItemStack d = container.getItem(SLOT_DISTILL);
            double p = distilled(purityOf(d));
            d.shrink(1);   // 塔の上下に B・P の塩化物が分かれて捨てられる
            merge(SLOT_OUT, tcs(p));
        } else {
            container.getItem(SLOT_SI).shrink(SI_PER_RUN);
            container.getItem(SLOT_HCL).shrink(HCL_PER_RUN);
            merge(SLOT_OUT, tcs(CRUDE));
            merge(SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, H2_PER_RUN));
            if (++synthRuns % SICL4_EVERY == 0) merge(SLOT_SICL4, new ItemStack(Metallurgy.SILICON_TETRACHLORIDE));
        }
        container.setChanged();
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out) { super.saveAdditional(out); out.putInt("SynthRuns", synthRuns); }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in) { super.loadAdditional(in); synthRuns = in.getIntOr("SynthRuns", 0); }

    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_MODE && distilling ? 1 : 0; }

}
