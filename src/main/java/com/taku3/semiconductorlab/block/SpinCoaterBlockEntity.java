package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.PositiveResistItem;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * レジスト塗布機（スピンコーター）[9-19]（IMP4-01、O-006）：ウェハーをモーターで毎分数千回転させ、真ん中にたらしたレジストを遠心力で薄く均一に広げる（約1µm）。
 * そのあと温めて溶剤を飛ばす（プリベーク）。加工中のウェハー・酸化膜つきの板 1〜8枚 ＋ ポジ型レジスト 1（空のガラス瓶は上に戻る）
 * → レジストを塗ったウェハー（ChipOps.coat、LITHO_DESIGN §5）。感光してしまったレジストは使えない。
 * ⚠️ 電力（500W：モーター＋プリベークの熱板）・時間（20秒）はゲームの値（spin_coater.placeholder）。
 */
public class SpinCoaterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_RESIST = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, s -> s.is(Semiconductor.OXIDIZED_WAFER) || ChipOps.isProcessWafer(s))
            .in(SLOT_RESIST, 8, 50, s -> s.is(Chemistry.POSITIVE_RESIST))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.spin_coater.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.spin_coater.need_input", "gui.semiconductor-lab.spin_coater.spoiled",
                    "gui.semiconductor-lab.spin_coater.cannot", "gui.semiconductor-lab.spin_coater.need_power", "gui.semiconductor-lab.spin_coater.running")
            .bottom("recipe", "gui.semiconductor-lab.spin_coater.recipe")
            .build();

    public SpinCoaterBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.SPIN_COATER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean goodResist(ItemStack s) { return s.is(Chemistry.POSITIVE_RESIST) && !PositiveResistItem.spoiled(s); }

    /** 塗ったウェハー（1〜8枚）。塗れなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (w.isEmpty()) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.coat(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && goodResist(container.getItem(SLOT_RESIST)) && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        ItemStack r = container.getItem(SLOT_RESIST);
        Vessel.returnEmpty(level, worldPosition, r, 1);
        r.shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
