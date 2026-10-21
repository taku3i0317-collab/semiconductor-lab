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
 * ネガ型レジスト（[2-30] KPR）を入れるとネガ型で塗る（ChipOps.coat(w, NEGATIVE)。現像はキシレン）。
 * 化学増幅レジスト（[2-33] CAR）はポジ型、露光のあとホットプレートで加熱（PEB）してから現像。KrF より短い光は CAR でないと焼けない（種類を記録、A）。
 * ⚠️ 電力（500W：モーター＋プリベークの熱板）・時間（20秒）はゲームの値（spin_coater.placeholder）。
 */
public class SpinCoaterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_RESIST = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, s -> s.is(Semiconductor.OXIDIZED_WAFER) || ChipOps.isProcessWafer(s))
            .in(SLOT_RESIST, 8, 50, s -> s.is(Chemistry.POSITIVE_RESIST) || s.is(Chemistry.NEGATIVE_RESIST) || s.is(Chemistry.CHEMICALLY_AMPLIFIED_RESIST) || s.is(Chemistry.EUV_RESIST))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.spin_coater.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.spin_coater.need_input", "gui.semiconductor-lab.spin_coater.spoiled",
                    "gui.semiconductor-lab.spin_coater.cannot", "gui.semiconductor-lab.spin_coater.need_power", "gui.semiconductor-lab.spin_coater.running")
            .bottom("recipe", "gui.semiconductor-lab.spin_coater.recipe")
            .build();

    public SpinCoaterBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.SPIN_COATER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean goodResist(ItemStack s) { return (s.is(Chemistry.POSITIVE_RESIST) || s.is(Chemistry.NEGATIVE_RESIST) || s.is(Chemistry.CHEMICALLY_AMPLIFIED_RESIST) || s.is(Chemistry.EUV_RESIST)) && !PositiveResistItem.spoiled(s); }

    /** 塗ったウェハー（1〜8枚）。塗れなければ EMPTY */
    public static ItemStack product(ItemStack w) { return product(w, new ItemStack(Chemistry.POSITIVE_RESIST)); }

    /** レジストの種類で型を決めて塗る（ネガ型レジストなら NEGATIVE） */
    public static ItemStack product(ItemStack w, ItemStack resist) {
        if (w.isEmpty()) return ItemStack.EMPTY;
        ItemStack n = w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN));
        // レジストの種類も記録する（[2-33]、A）：DNQ・KPR は 250nm より短い光を吸いすぎて焼けない、化学増幅（CAR）は焼ける
        var pos = com.taku3.semiconductorlab.litho.ChipState.Tone.POSITIVE;
        ChipOps.Result r = resist.is(Chemistry.NEGATIVE_RESIST) ? ChipOps.coat(n, com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE, ChipOps.RESIST_KPR)
                : resist.is(Chemistry.CHEMICALLY_AMPLIFIED_RESIST) ? ChipOps.coat(n, pos, ChipOps.RESIST_CAR)
                : resist.is(Chemistry.EUV_RESIST) ? ChipOps.coat(n, pos, ChipOps.RESIST_EUV)   // [9-40] EUV のレジスト（D、S-008）
                : ChipOps.coat(n, pos, ChipOps.RESIST_DNQ);
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_RESIST));
        return !out.isEmpty() && goodResist(container.getItem(SLOT_RESIST)) && canMerge(container.getItem(SLOT_OUT), dust(out));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_RESIST));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        ItemStack r = container.getItem(SLOT_RESIST);
        Vessel.returnEmpty(level, worldPosition, r, 1);
        r.shrink(1);
        merge(SLOT_OUT, dust(out));
        container.setChanged();
    }

    /** クリーンルームの等級のほこりと欠陥の密度（O-017・歩留まり、C の CleanRoom.dustAt：外 +2・クラス1000 +1・クラス100 0）。失敗にはしない＝汚れは検査台の漏れ・歩留まりに効く */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.inRoom(ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition)), CleanRoom.defectDensity(level, worldPosition));   // v0.5 #4 歩留まり：部屋の欠陥の密度も残す（A）
    }
}
