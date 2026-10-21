package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
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
 * スパッタ装置 [9-26]（1852 グローブが放電管の陰極が削れて壁に付くのを記録、1970年代 マグネトロンスパッタで量産へ。IMP4-01、O-011）：
 * 真空容器に少しアルゴンを入れ、的（ターゲット＝付けたい金属）に高い電圧をかけて放電させる。Ar⁺ が的をたたき、はじき出された金属の原子が
 * ウェハーに積もる。的の裏の磁石が電子を閉じこめて放電を濃くする（マグネトロン）。原子がいろいろな向きから飛んでくるので、段差の横にも付く（蒸着は影ができる）。
 * 加工中のウェハー 1〜8枚 ＋ アルミニウム 1（的が削れる分）＋ アルゴン 1 → Al を一面に付けたウェハー（ChipOps.depositMetal(w, true)＝conformalMetal の印が付く。回路は [9-25] の蒸着と同じ）。
 * 隣の真空容器が {@link #MAX_PA} 以下に引けていること（先に空気を抜いてから Ar を入れる）。アルゴンは隣のガスホルダーから補充。
 * [9-36] Ta の的にすると、銅配線（ダマシン）の溝・穴にタンタルのバリア膜（ChipOps.depositBarrier、A）。銅がシリコンへしみるのを止める。
 * EUV のマスクの板（S-008）：多層膜ミラー（40対以上）＋タンタルの的 → euv_mask_blank（吸収体 ⚠️ 本物は Ar＋N2 の反応性スパッタで TaBN）。
 * ⚠️ 段差を覆える差は記録だけ（A と決めた案②）。v0.5 の歩留まりで「蒸着は段差のふちで切れやすい」に使う。電力・時間はゲームの値（sputter.placeholder）。
 */
public class SputterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 20;
    public static final double MAX_PA = 0.01;
    /** EUV のマスクの板に要る多層膜の対の数（反射率が足りる、S-008） */
    public static final int MASK_MIN_PAIRS = 40;
    public static final int WAFERS_PER_RUN = 8, AR_PER_RUN = 1;
    public static final int SLOT_WAFER = 0, SLOT_TARGET = 1, SLOT_AR = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int DATA_VACUUM = DATA_COUNT;
    private static final int PULL_INTERVAL = 10, AR_TARGET = 16;
    private int pullTimer;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_WAFER, 8, 17, s -> ChipOps.isProcessWafer(s) || isMirrorBase(s))
            .in(SLOT_TARGET, 8, 35, s -> s.is(Metallurgy.ALUMINIUM_INGOT) || s.is(Metallurgy.TANTALUM) || s.is(com.taku3.semiconductorlab.content.Optics.MO_SI_TARGET))
            .in(SLOT_AR, 8, 53, s -> s.is(Gas.ARGON_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.sputter.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.sputter.need_input", "gui.semiconductor-lab.sputter.cannot",
                    "gui.semiconductor-lab.sputter.need_vacuum", "gui.semiconductor-lab.sputter.need_power", "gui.semiconductor-lab.sputter.running")
            .bottom("recipe", "gui.semiconductor-lab.sputter.recipe")
            .build();

    public SputterBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.SPUTTER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** Al を付けたウェハー（1〜8枚）。付けられなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack target) {
        if (w.is(com.taku3.semiconductorlab.content.Optics.MULTILAYER_MIRROR) && target.is(Metallurgy.TANTALUM))   // EUV のマスクの板（S-008）
            return com.taku3.semiconductorlab.item.MultilayerMirrorItem.pairs(w) >= MASK_MIN_PAIRS ? new ItemStack(Semiconductor.EUV_MASK_BLANK) : ItemStack.EMPTY;
        if (isMirrorBase(w)) return mirrorProduct(w, target);
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ItemStack in = w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN));
        ChipOps.Result r;
        if (target.is(Metallurgy.TANTALUM)) r = ChipOps.depositBarrier(in);   // [9-36] Ta の的：銅配線の溝・穴にバリア膜（A）
        else if (target.is(Metallurgy.ALUMINIUM_INGOT)) r = ChipOps.depositMetal(in, true);   // conformal＝段差によく付く（記録、A）
        else return ItemStack.EMPTY;
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    /** [7-17] 多層膜ミラーの土台（ULE ガラス・重ねている途中の多層膜ミラー）（IMP2 S-007） */
    public static boolean isMirrorBase(ItemStack s) {
        return s.is(com.taku3.semiconductorlab.content.Optics.ULE_GLASS) || s.is(com.taku3.semiconductorlab.content.Optics.MULTILAYER_MIRROR);
    }

    /** [7-17] Mo/Si の的を1つ当てると 10 対増える（最大 60）。それ以上・的が違うと EMPTY */
    public static ItemStack mirrorProduct(ItemStack base, ItemStack target) {
        if (!target.is(com.taku3.semiconductorlab.content.Optics.MO_SI_TARGET)) return ItemStack.EMPTY;
        int n = com.taku3.semiconductorlab.item.MultilayerMirrorItem.pairs(base) + com.taku3.semiconductorlab.item.MultilayerMirrorItem.PAIRS_PER_RUN;
        if (base.is(com.taku3.semiconductorlab.content.Optics.MULTILAYER_MIRROR) && n > com.taku3.semiconductorlab.item.MultilayerMirrorItem.MAX_PAIRS) return ItemStack.EMPTY;
        return com.taku3.semiconductorlab.item.MultilayerMirrorItem.withPairs(new ItemStack(com.taku3.semiconductorlab.content.Optics.MULTILAYER_MIRROR), base.is(com.taku3.semiconductorlab.content.Optics.ULE_GLASS) ? com.taku3.semiconductorlab.item.MultilayerMirrorItem.PAIRS_PER_RUN : n);
    }

    public boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_TARGET)), ar = container.getItem(SLOT_AR);
        return !out.isEmpty() && ar.is(Gas.ARGON_GAS) && ar.getCount() >= AR_PER_RUN && vacuumOk() && canMerge(container.getItem(SLOT_OUT), dust(out));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_TARGET));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_TARGET).shrink(1);
        container.getItem(SLOT_AR).shrink(AR_PER_RUN);
        merge(SLOT_OUT, dust(out));
        container.setChanged();
    }

    /** 隣のガスホルダーからアルゴンを 16 袋まで補充（自分の tick で数える） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (++pullTimer < PULL_INTERVAL) return;
        pullTimer = 0;
        ItemStack ar = container.getItem(SLOT_AR);
        if (!ar.isEmpty() && !ar.is(Gas.ARGON_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = AR_TARGET - container.getItem(SLOT_AR).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.ARGON, want);
            if (n > 0) merge(SLOT_AR, new ItemStack(Gas.ARGON_GAS, n));
        }
    }

    /** 画面に出す：真空がよいか（1/0） */
    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_VACUUM && vacuumOk() ? 1 : 0; }

    /** クリーンルームの等級のほこりと欠陥の密度（O-017・歩留まり、C の CleanRoom.dustAt：外 +2・クラス1000 +1・クラス100 0）。失敗にはしない＝汚れは検査台の漏れ・歩留まりに効く */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.inRoom(ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition)), CleanRoom.defectDensity(level, worldPosition));   // v0.5 #4 歩留まり：部屋の欠陥の密度も残す（A）
    }
}
