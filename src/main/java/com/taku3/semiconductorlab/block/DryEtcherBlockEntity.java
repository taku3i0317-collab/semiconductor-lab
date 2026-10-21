package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ドライエッチング装置（反応性イオンエッチング RIE）[9-27]（1970年代〜。IMP4-01、O-012）：
 * 真空容器に CF4 を少し入れ、高周波 [4-09] で放電させる（プラズマ）。CF4 がこわれてできたフッ素が SiO2 を SiF4 の気体にして運び去る。
 * ウェハーを置いた電極に電圧がかかり、イオンが真上から打ちこまれる＝真下だけ削れる（異方性）。液のフッ酸 [9-06] は横にも溶けて線が太る（等方性）。
 * 現像したレジストの付いたウェハー 1〜8枚 ＋ CF4 1 → 窓を開けたウェハー（ChipOps.etchOxide(w, true)：方眼が ChipOps.WET_LIMIT_GRID 以上の細い線でも太らない。液は1マス太る）。隣の真空容器が {@link #MAX_PA} 以下に引けていること。
 * [9-37] 塩素のプラズマ（Cl2）にすると、酸化膜ではなくシリコンを真下に削る＝レジストの残った所に細い板（フィン）が立つ（ChipOps.formFins(w, true)、A）。
 *   液では横に溶けてフィンは立たない。FinFET の短チャネルの漏れは A の判定。
 * [9-43] 素のシリコンの板＋Cl2 → MEMS の板（深く削ったばねとおもり）。⚠️ 本物は 1994 ボッシュ法（SF6 で削る・C4F8 で壁を守る、を交互に）。
 * [9-38] 乾いた塩化水素の気体を入れると、フィンの中の SiGe の層だけを横から削り（選択エッチング）、Si のシートだけが残る → ゲートが4方向を囲める（ChipOps.releaseSheets、A）。
 * ⚠️ 電力・時間はゲームの値。CF4 に混ぜる O2・放電中の圧力（約1〜10Pa）は省略（dry_etcher.placeholder）。
 */
public class DryEtcherBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 15;
    public static final double MAX_PA = 0.1;
    public static final int WAFERS_PER_RUN = 8, CF4_PER_RUN = 1;
    public static final int SLOT_WAFER = 0, SLOT_GAS = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    public static final int DATA_VACUUM = DATA_COUNT;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_WAFER, 8, 26, s -> ChipOps.isProcessWafer(s) || s.is(Semiconductor.SILICON_WAFER))
            .in(SLOT_GAS, 8, 50, s -> s.is(Semiconductor.CARBON_TETRAFLUORIDE) || s.is(com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS) || s.is(com.taku3.semiconductorlab.content.Metallurgy.HYDROGEN_CHLORIDE_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.dry_etcher.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.dry_etcher.need_input", "gui.semiconductor-lab.dry_etcher.cannot",
                    "gui.semiconductor-lab.dry_etcher.need_vacuum", "gui.semiconductor-lab.dry_etcher.need_power", "gui.semiconductor-lab.dry_etcher.running", "gui.semiconductor-lab.dry_etcher.running_fin", "gui.semiconductor-lab.dry_etcher.running_gaa")
            .bottom("recipe", "gui.semiconductor-lab.dry_etcher.recipe")
            .build();

    public DryEtcherBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.DRY_ETCHER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 窓を開けたウェハー（1〜8枚）。削れなければ EMPTY */
    public static ItemStack product(ItemStack w) { return product(w, new ItemStack(Semiconductor.CARBON_TETRAFLUORIDE)); }

    /** gas が CF4 なら酸化膜に窓、Cl2 ならシリコンを削ってフィン（[9-37]）。できなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack gas) {
        if (w.is(Semiconductor.SILICON_WAFER) && gas.is(com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS))   // [9-43] MEMS：素の板を深く削ってばねとおもり（⚠️ 本物は SF6/C4F8 のボッシュ法）
            return w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)).transmuteCopy(Semiconductor.MEMS_WAFER);
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ItemStack in = w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN));
        ChipOps.Result r = gas.is(com.taku3.semiconductorlab.content.Metallurgy.HYDROGEN_CHLORIDE_GAS) ? ChipOps.releaseSheets(in)   // [9-38] HCl の気体は SiGe だけを削る → ナノシートが宙に浮く（GAA）
                : gas.is(com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS) ? ChipOps.formFins(in, true)
                : gas.is(Semiconductor.CARBON_TETRAFLUORIDE) ? ChipOps.etchOxide(in, true)   // 異方性＝細い方眼でも太らない（A）
                : null;
        return r != null && r.ok() ? r.out() : ItemStack.EMPTY;
    }

    public boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack g = container.getItem(SLOT_GAS), out = product(container.getItem(SLOT_WAFER), g);
        return !out.isEmpty() && g.getCount() >= CF4_PER_RUN && vacuumOk() && canMerge(container.getItem(SLOT_OUT), dust(out));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_GAS));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_GAS).shrink(CF4_PER_RUN);
        merge(SLOT_OUT, dust(out));
        container.setChanged();
    }

    /** 画面に出す：真空がよいか（1/0） */
    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_VACUUM && vacuumOk() ? 1 : 0; }

    /** クリーンルームの等級のほこりと欠陥の密度（O-017・歩留まり、A の inRoom）。失敗にはしない。MEMS の板（加工中のウェハーでない）はそのまま */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.inRoom(ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition)), CleanRoom.defectDensity(level, worldPosition));
    }
}
