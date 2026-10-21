package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.BallScrewItem;
import com.taku3.semiconductorlab.item.DiamondScribeItem;
import com.taku3.semiconductorlab.litho.Discoveries;
import com.taku3.semiconductorlab.litho.Netlist;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * [9-35] テスト（ウェハープローバー）段階1（1960年代、O-017、A）：ウェハーのまま、チップ1個ずつに針（プローブカード＝タングステンの細い針）を
 * 自動で当てて電気を通し、良いか悪いかを決め、悪いチップにインクの点を付ける。台はボールねじ [8-08]（C）で1チップずつ送る。
 * 検査台（手で1個ずつ、電流が見える）とちがい、速く全部を調べるが、見るのは「良いか悪いか」だけ。
 * 判定：チップごとに漏れ（汚れ、S-003）の強さを 0.5〜1.5 倍で変えて回路を解き、汚れのない時と同じ発見が出れば良品。
 * 結果はウェハーのデータ成分 die_map（良いチップのビット）。ダイシングでは印の付いたチップは捨てる（良品だけ出る）。
 * 段階2（コンピュータで判定・記録する自動テスター）は [10-14] の後。⚠️ 1チップ 0.5秒・500W はゲームの値、インクは省略。
 */
public class WaferProberBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    /** 1枚（8チップ）を調べる時間：1チップ 0.5秒 × 8（手の検査台は1個ずつボタン） */
    public static final int TICKS = 10 * DiamondScribeItem.CHIPS_PER_WAFER;
    public static final int SLOT_WAFER = 0, SLOT_STAGE = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, s -> DiamondScribeItem.finished(s) && !s.has(ModDataComponents.DIE_MAP))
            .in(SLOT_STAGE, 8, 50, s -> s.is(Precision.BALL_SCREW), 1)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.machine.power").samples("0.5 kW")
            .right("hint", 28, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running").fit()
            .build();

    public WaferProberBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.WAFER_PROBER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /**
     * チップごとの良否（ビット i＝チップ i が良品）。汚れたウェハーほど悪いチップが出る（漏れ）。
     * さらに、工程の部屋の欠陥（v0.5 #4）：チップごとに Y＝e^(−D·A)（ChipOps.expectedYield）の確率で欠陥がない＝良品になれる。失敗にはしない（良品の数に効く）
     */
    public static int test(ItemStack wafer, RandomSource random) {
        Netlist nl = ProbeStationBlockEntity.netlistOf(wafer);
        if (nl == null) return 0;
        var cleaned = com.taku3.semiconductorlab.litho.ChipOps.clean(wafer.copyWithCount(1), 0);   // 汚れのないときの動き（くらべる相手）
        Netlist ref = cleaned.ok() ? ProbeStationBlockEntity.netlistOf(cleaned.out()) : nl;
        int nominal = Discoveries.match(ref, 1.0).size(), map = 0;
        for (int i = 0; i < DiamondScribeItem.CHIPS_PER_WAFER; i++) {
            double leak = 0.5 + random.nextDouble();   // 同じ1枚の中でもチップごとに少しずつちがう
            boolean noDefect = random.nextDouble() < com.taku3.semiconductorlab.litho.ChipOps.expectedYield(com.taku3.semiconductorlab.litho.ChipOps.state(wafer));
            if (noDefect && Discoveries.match(nl, leak).size() == nominal) map |= 1 << i;
        }
        return map;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack w = container.getItem(SLOT_WAFER);
        if (!DiamondScribeItem.finished(w) || w.has(ModDataComponents.DIE_MAP) || BallScrewItem.grade(container.getItem(SLOT_STAGE)) <= 0) return false;
        return container.getItem(SLOT_OUT).isEmpty();   // 1枚ずつ取り出す（ウェハーごとに結果がちがう）
    }

    @Override protected void finishWork() {
        ItemStack w = container.getItem(SLOT_WAFER);
        ItemStack out = w.copyWithCount(1);
        out.set(ModDataComponents.DIE_MAP, test(w, level == null ? RandomSource.create() : level.getRandom()));
        w.shrink(1);
        container.setItem(SLOT_OUT, out);   // 1枚ずつ（ウェハーごとに結果がちがうので重ねない）
        container.setChanged();
    }
}
