package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CVD 炉（層間膜）[9-30] の一部（IMP4-01、S-002）：気体の反応で膜を積む（化学気相成長）。
 * シランと酸素を約400℃ のウェハーの上で反応させ、二酸化ケイ素の膜を一面に積む（SiH4 + O2 → SiO2 + 2H2O、常圧 CVD）。
 * 熱酸化（約1000℃、[9-15]）はシリコンを食べて育つうえ、アルミ（660℃ で溶ける）の上ではできない＝金属の上の絶縁膜は CVD で積む。
 * 1層目の金属を削った加工中のウェハー 1〜8枚 ＋ シランのボンベ 1 ＋ 酸素 1 → 層間膜を付けたウェハー（ChipOps.depositInterlayer、A）＋ 空のボンベ。
 * そのあと VIA の窓（塗布→露光→現像→エッチング→はがす）→ 2層目の Al → 削る（METAL2）。酸素は隣のガスホルダーから補充。
 * ⚠️ 電力・時間はゲームの値。膜の厚さ・りん（PSG）を混ぜて流れやすくする工夫は省略（cvd_furnace.placeholder）。
 */
public class CvdFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8, O2_PER_RUN = 1;
    public static final int SLOT_WAFER = 0, SLOT_SILANE = 1, SLOT_O2 = 2, SLOT_OUT = 3, SLOT_CYL = 4, SLOT_COUNT = 5;
    private static final int PULL_INTERVAL = 10, O2_TARGET = 16;
    private int pullTimer;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, ChipOps::isProcessWafer)
            .in(SLOT_SILANE, 8, 35, s -> s.is(Semiconductor.SILANE_CYLINDER))
            .in(SLOT_O2, 8, 53, s -> s.is(Gas.OXYGEN_GAS))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_CYL, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.cvd_furnace.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.cvd_furnace.need_input", "gui.semiconductor-lab.cvd_furnace.cannot",
                    "gui.semiconductor-lab.cvd_furnace.need_power", "gui.semiconductor-lab.cvd_furnace.running")
            .bottom("recipe", "gui.semiconductor-lab.cvd_furnace.recipe")
            .build();

    public CvdFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.CVD_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 層間膜を付けたウェハー（1〜8枚）。付けられなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.depositInterlayer(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER)), o2 = container.getItem(SLOT_O2);
        return !out.isEmpty() && container.getItem(SLOT_SILANE).is(Semiconductor.SILANE_CYLINDER) && o2.is(Gas.OXYGEN_GAS) && o2.getCount() >= O2_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), out) && canMerge(container.getItem(SLOT_CYL), new ItemStack(Gas.EMPTY_GAS_CYLINDER));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_SILANE).shrink(1);
        container.getItem(SLOT_O2).shrink(O2_PER_RUN);
        merge(SLOT_OUT, out);
        merge(SLOT_CYL, new ItemStack(Gas.EMPTY_GAS_CYLINDER));
        container.setChanged();
    }

    /** 隣のガスホルダーから酸素を 16 袋まで補充（自分の tick で数える） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (++pullTimer < PULL_INTERVAL) return;
        pullTimer = 0;
        ItemStack o2 = container.getItem(SLOT_O2);
        if (!o2.isEmpty() && !o2.is(Gas.OXYGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = O2_TARGET - container.getItem(SLOT_O2).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.OXYGEN, want);
            if (n > 0) merge(SLOT_O2, new ItemStack(Gas.OXYGEN_GAS, n));
        }
    }
}
