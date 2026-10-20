package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 現像槽 [9-21]（IMP4-01、O-006）：露光したウェハーを薄いアルカリの液（現像液＝水酸化ナトリウム約1%）に浸すと、ポジ型レジストの光が当たった所だけが溶けて
 * マスクの模様が浮かび上がる。すすいだあと熱板で温めてレジストを固め（ポストベーク）、エッチングの酸に耐えるようにする。
 * 露光した加工中のウェハー 1〜8枚 ＋ 現像液 1（空のガラス瓶は上に戻る）→ 現像したウェハー（ChipOps.develop）。
 * ネガ型（[2-30] KPR）はキシレン（有機溶剤）で現像する：光の当たっていない所が溶ける。型と合わない現像液では現像できない（ChipOps "wrong_developer"）。
 * ⚠️ 電力（300W：ポストベークの熱板）・時間（20秒）はゲームの値（developer_bench.placeholder）。
 */
public class DeveloperBenchBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 300;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_DEV = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_DEV, 8, 50, DeveloperBenchBlockEntity::isDeveloper)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.developer_bench.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.developer_bench.need_input", "gui.semiconductor-lab.developer_bench.not_exposed", "gui.semiconductor-lab.developer_bench.wrong_developer",
                    "gui.semiconductor-lab.developer_bench.need_power", "gui.semiconductor-lab.developer_bench.running")
            .bottom("recipe", "gui.semiconductor-lab.developer_bench.recipe")
            .build();

    public DeveloperBenchBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.DEVELOPER_BENCH.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 現像液：ポジ型のアルカリ（薄い NaOH）か、ネガ型の有機溶剤（キシレン） */
    public static boolean isDeveloper(ItemStack s) { return s.is(Chemistry.DEVELOPER) || s.is(Chemistry.XYLENE); }
    public static ChipOps.Developer kindOf(ItemStack dev) { return dev.is(Chemistry.XYLENE) ? ChipOps.Developer.ORGANIC : ChipOps.Developer.ALKALI; }

    /** 現像の結果（失敗の理由つき） */
    public static ChipOps.Result result(ItemStack w, ItemStack dev) {
        return ChipOps.develop(w.copyWithCount(Math.max(1, Math.min(w.getCount(), WAFERS_PER_RUN))), kindOf(dev));
    }

    /** 現像したウェハー（1〜8枚）。露光していない・現像液が型と合わなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack dev) {
        if (!ChipOps.isProcessWafer(w) || !isDeveloper(dev)) return ItemStack.EMPTY;
        ChipOps.Result r = result(w, dev);
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }
    /** アルカリの現像液で（前の形、テスト用） */
    public static ItemStack product(ItemStack w) { return product(w, new ItemStack(Chemistry.DEVELOPER)); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_DEV));
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_DEV));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        ItemStack d = container.getItem(SLOT_DEV);
        Vessel.returnEmpty(level, worldPosition, d, 1);
        d.shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
