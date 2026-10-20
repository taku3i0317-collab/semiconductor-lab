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
 * RCA 洗浄台 [9-14] 段階2（1965 カーン（RCA）、1970 発表。IMP4-01）：
 * SC-1＝アンモニア水＋過酸化水素＋水（1:1:5、約75℃）で有機物と微粒子を、SC-2＝塩酸＋過酸化水素＋水（1:1:6）で金属を落とし、
 * 間ごとに脱イオン水（[2-17]）か超純水（[2-18]）ですすぐ。
 * 加工中のウェハー 1〜8枚 ＋ アンモニア水 1 ＋ 塩酸 1 ＋ 過酸化水素 2 ＋ 脱イオン水か超純水 1（空の瓶は機械の上に戻る）→ 汚れ 0（ChipOps.clean(w, 0)）。
 * 段階1（硝酸・フッ酸、{@link CleaningBenchBlockEntity}）との差（金属・微粒子がより少ない）は、v0.5 の歩留まりで付ける。
 * ⚠️ 電力（1.5kW：液を約75℃に温める）・時間（40秒）はゲームの値（rca_bench.placeholder）。
 */
public class RcaBenchBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_500;
    public static final int TICKS = 20 * 40;
    public static final int WAFERS_PER_RUN = 8, PEROXIDE_PER_RUN = 2;
    public static final int SLOT_WAFER = 0, SLOT_AMMONIA = 1, SLOT_HCL = 2, SLOT_PEROXIDE = 3, SLOT_WATER = 4, SLOT_OUT = 5, SLOT_COUNT = 6;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_AMMONIA, 26, 17, s -> s.is(Chemistry.AMMONIA_WATER))
            .in(SLOT_HCL, 26, 35, s -> s.is(Gas.HYDROCHLORIC_ACID))
            .in(SLOT_PEROXIDE, 26, 53, s -> s.is(Chemistry.HYDROGEN_PEROXIDE))
            .in(SLOT_WATER, 44, 53, RcaBenchBlockEntity::isRinse)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.rca_bench.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.rca_bench.need_input", "gui.semiconductor-lab.rca_bench.resist_on",
                    "gui.semiconductor-lab.rca_bench.no_dirt", "gui.semiconductor-lab.rca_bench.need_power", "gui.semiconductor-lab.rca_bench.running")
            .bottom("recipe", "gui.semiconductor-lab.rca_bench.recipe")
            .build();

    public RcaBenchBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.RCA_BENCH.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** すすぎの水：脱イオン水か超純水（蒸留水は段階1 の洗浄槽で） */
    public static boolean isRinse(ItemStack s) { return s.is(Chemistry.DEIONIZED_WATER) || s.is(Chemistry.ULTRAPURE_WATER); }

    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.clean(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)), 0);
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_AMMONIA).is(Chemistry.AMMONIA_WATER) && container.getItem(SLOT_HCL).is(Gas.HYDROCHLORIC_ACID)
                && container.getItem(SLOT_PEROXIDE).is(Chemistry.HYDROGEN_PEROXIDE) && container.getItem(SLOT_PEROXIDE).getCount() >= PEROXIDE_PER_RUN
                && isRinse(container.getItem(SLOT_WATER)) && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        int[][] use = {{SLOT_AMMONIA, 1}, {SLOT_HCL, 1}, {SLOT_PEROXIDE, PEROXIDE_PER_RUN}, {SLOT_WATER, 1}};
        for (int[] u : use) {
            ItemStack used = container.getItem(u[0]);
            Vessel.returnEmpty(level, worldPosition, used, u[1]);
            used.shrink(u[1]);
        }
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
