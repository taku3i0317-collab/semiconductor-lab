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
 * レジストはがし槽（[9-21] の続き、IMP4-01・O-006/O-008）：熱い濃硫酸に過酸化水素をまぜた液（ペルオキソ一硫酸＝カロ酸を含む）で、
 * 有機物のレジストを酸化して溶かし去る。酸化膜・シリコン・アルミの配線はほとんど溶かさない（Al はリン酸と違い濃硫酸では不動態）。
 * 加工中のウェハー 1〜8枚 ＋ 硫酸 1 ＋ 過酸化水素 1（空の瓶 2本は上に戻る）→ レジストをはがしたウェハー（ChipOps.strip）。
 * 塗り直し（露光をまちがえたとき、はがして塗り直す＝リワーク）にも使う。
 * ⚠️ 電力（500W：液を温める）・時間（20秒）はゲームの値。濃い液を混ぜると大きく発熱する事故の危険は note に（resist_stripper.placeholder）。
 */
public class ResistStripperBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_ACID = 1, SLOT_PEROXIDE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_ACID, 26, 26, s -> s.is(Chemistry.SULFURIC_ACID))
            .in(SLOT_PEROXIDE, 26, 50, s -> s.is(Chemistry.HYDROGEN_PEROXIDE))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.resist_stripper.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.resist_stripper.need_input", "gui.semiconductor-lab.resist_stripper.no_resist",
                    "gui.semiconductor-lab.resist_stripper.need_power", "gui.semiconductor-lab.resist_stripper.running")
            .bottom("recipe", "gui.semiconductor-lab.resist_stripper.recipe")
            .build();

    public ResistStripperBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.RESIST_STRIPPER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** レジストをはがしたウェハー（1〜8枚）。レジストが付いていなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w) || ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.NONE) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.strip(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_ACID).is(Chemistry.SULFURIC_ACID) && container.getItem(SLOT_PEROXIDE).is(Chemistry.HYDROGEN_PEROXIDE)
                && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        for (int slot : new int[] {SLOT_ACID, SLOT_PEROXIDE}) {
            ItemStack used = container.getItem(slot);
            Vessel.returnEmpty(level, worldPosition, used, 1);
            used.shrink(1);
        }
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
