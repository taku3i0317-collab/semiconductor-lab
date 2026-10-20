package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
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
 * 洗浄槽 [9-14] 段階1（1950年代の酸洗浄、IMP4-01・O-005）：加工中のウェハーを、熱した硝酸で煮て金属・有機物の汚れを酸化して落とし、
 * フッ酸に浸して汚れた表面の薄い酸化膜ごと取り、蒸留水ですすぐ。ヒーターは 1kW（硝酸を温める）。槽はフッ酸に負けない鉛張り。
 * 加工中のウェハー 1〜8枚 ＋ 硝酸 1 ＋ フッ酸 1 ＋ 蒸留水 1（空の瓶 3本は機械の上に戻る）→ 汚れを 0 にしたウェハー（ChipOps.clean(w, 0)）。
 * レジストが付いているウェハーは洗えない（先にはがす）。段階2（RCA：SC-1・SC-2、脱イオン水）は [2-17] ができてから同じ槽に足す。
 * ⚠️ 電力・時間（40秒）はゲームの値。本物は槽を3つ並べて順に浸す（cleaning_bench.placeholder）。
 */
public class CleaningBenchBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 40;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_NITRIC = 1, SLOT_HF = 2, SLOT_WATER = 3, SLOT_OUT = 4, SLOT_COUNT = 5;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_NITRIC, 26, 17, s -> s.is(Gas.NITRIC_ACID))
            .in(SLOT_HF, 26, 35, s -> s.is(Chemistry.HF_TAG))
            .in(SLOT_WATER, 26, 53, s -> s.is(Chemistry.DISTILLED_WATER))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.cleaning_bench.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.cleaning_bench.need_input", "gui.semiconductor-lab.cleaning_bench.resist_on",
                    "gui.semiconductor-lab.cleaning_bench.no_dirt", "gui.semiconductor-lab.cleaning_bench.need_power", "gui.semiconductor-lab.cleaning_bench.running")
            .bottom("recipe", "gui.semiconductor-lab.cleaning_bench.recipe")
            .build();

    public CleaningBenchBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.CLEANING_BENCH.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 洗ったあとのウェハー（1〜8枚）。洗えなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.clean(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)), 0);
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_NITRIC).is(Gas.NITRIC_ACID) && container.getItem(SLOT_HF).is(Chemistry.HF_TAG)
                && container.getItem(SLOT_WATER).is(Chemistry.DISTILLED_WATER) && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        for (int slot : new int[] {SLOT_NITRIC, SLOT_HF, SLOT_WATER}) {   // 空の瓶（ガラス・鉛・ガラス）は上に戻る
            ItemStack used = container.getItem(slot);
            Vessel.returnEmpty(level, worldPosition, used, 1);
            used.shrink(1);
        }
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
