package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.Dopant;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * エピタキシャル炉 [9-30]（1960 ベル研のエピタキシャルトランジスタ。IMP4-01、O-013）：
 * 素のシリコンウェハーを約1200℃ に熱し（誘導加熱 [3-14]）、四塩化ケイ素と水素を流すと、ウェハーと同じ向きの単結晶の層が上に育つ（SiCl4 + 2H2 → Si + 4HCl）。
 * いっしょにドーパントを入れると、その型の層になる（1960年代のバイポーラ IC は p の基板の上に n のエピ層）。シーメンス炉 [1-17] で棒を太らせる反応の、薄い膜版。
 * 素のウェハー 1〜8枚（DOPANT 付き）＋ 四塩化ケイ素 1 ＋ 水素 2 ＋ ドーパント源 1（B2O3＝p・リン＝n）→ エピ層付きのウェハー（データ部品 epi＝"n"／"p"、A の部品）＋ 乾いた塩化水素 4。
 * 水素は隣のガスホルダーから補充。エピ層は酸化炉で酸化しても受け継ぐ。
 * ⚠️ 層の厚さ・抵抗率は記録しない。回路にどう効くか（基板の型）は A の ChipOps しだい。電力・時間はゲームの値（epitaxy_reactor.placeholder）。
 */
public class EpitaxyReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 10_000;
    public static final int TICKS = 20 * 30;
    public static final int WAFERS_PER_RUN = 8, H2_PER_RUN = 2, HCL_PER_RUN = 4;
    public static final int SLOT_WAFER = 0, SLOT_SICL4 = 1, SLOT_H2 = 2, SLOT_DOPANT = 3, SLOT_OUT = 4, SLOT_HCL = 5, SLOT_COUNT = 6;
    private static final int PULL_INTERVAL = 10, H2_TARGET = 16;
    private int pullTimer;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, s -> s.is(Semiconductor.SILICON_WAFER))
            .in(SLOT_SICL4, 8, 35, s -> s.is(Metallurgy.SILICON_TETRACHLORIDE))
            .in(SLOT_H2, 8, 53, s -> s.is(Gas.HYDROGEN_GAS))
            .in(SLOT_DOPANT, 30, 53, s -> Dopant.of(s) != null)
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_HCL, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.epitaxy_reactor.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.epitaxy_reactor.need_input", "gui.semiconductor-lab.epitaxy_reactor.already",
                    "gui.semiconductor-lab.epitaxy_reactor.need_power", "gui.semiconductor-lab.epitaxy_reactor.running")
            .bottom("recipe", "gui.semiconductor-lab.epitaxy_reactor.recipe")
            .build();

    public EpitaxyReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.EPITAXY_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** エピ層付きのウェハー（1〜8枚）。もうエピ層がある・素のウェハーでない・ドーパント源がなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack dopantSource) {
        Dopant d = Dopant.of(dopantSource);
        if (d == null || !w.is(Semiconductor.SILICON_WAFER) || w.has(ModDataComponents.EPI)) return ItemStack.EMPTY;
        ItemStack out = w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN));
        out.set(ModDataComponents.EPI, String.valueOf(d.type));   // A の部品：エピ層の型 "n"／"p"
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_DOPANT)), h2 = container.getItem(SLOT_H2);
        return !out.isEmpty() && container.getItem(SLOT_SICL4).is(Metallurgy.SILICON_TETRACHLORIDE) && h2.is(Gas.HYDROGEN_GAS) && h2.getCount() >= H2_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), out) && canMerge(container.getItem(SLOT_HCL), new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, HCL_PER_RUN));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_DOPANT));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_SICL4).shrink(1);
        container.getItem(SLOT_H2).shrink(H2_PER_RUN);
        container.getItem(SLOT_DOPANT).shrink(1);
        merge(SLOT_OUT, out);
        merge(SLOT_HCL, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, HCL_PER_RUN));
        container.setChanged();
    }

    /** 隣のガスホルダーから水素を 16 袋まで補充（自分の tick で数える） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (++pullTimer < PULL_INTERVAL) return;
        pullTimer = 0;
        ItemStack h2 = container.getItem(SLOT_H2);
        if (!h2.isEmpty() && !h2.is(Gas.HYDROGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = H2_TARGET - container.getItem(SLOT_H2).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.HYDROGEN, want);
            if (n > 0) merge(SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, n));
        }
    }
}
