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
 * 窒化膜モード（O-013）：酸素の代わりにアンモニアを入れると、窒化ケイ素の保護膜（パッシベーション）を積む（3SiH4 + 4NH3 → Si3N4 + 12H2、約700〜800℃）。
 * 金属を削り終えたウェハー → ChipOps.passivate（A、記録だけ）。水分やナトリウムからチップを守る最後の膜。
 * TFT モード（[B-19]、O-024）：ITO ガラス＋シラン＋水素 → 水素を含むアモルファスシリコンの膜のガラス（SiH4 → a-Si:H + H2、1979）。
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
            .in(SLOT_WAFER, 8, 17, s -> ChipOps.isProcessWafer(s) || s.is(com.taku3.semiconductorlab.content.Chemistry.ITO_GLASS))
            .in(SLOT_SILANE, 8, 35, s -> s.is(Semiconductor.SILANE_CYLINDER))
            .in(SLOT_O2, 8, 53, s -> s.is(Gas.OXYGEN_GAS) || s.is(Gas.AMMONIA_GAS) || s.is(Gas.HYDROGEN_GAS))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_CYL, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.cvd_furnace.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.cvd_furnace.need_input", "gui.semiconductor-lab.cvd_furnace.cannot",
                    "gui.semiconductor-lab.cvd_furnace.need_power", "gui.semiconductor-lab.cvd_furnace.running",
                    "gui.semiconductor-lab.cvd_furnace.cannot_nitride", "gui.semiconductor-lab.cvd_furnace.running_nitride", "gui.semiconductor-lab.cvd_furnace.running_tft")
            .bottom("recipe", "gui.semiconductor-lab.cvd_furnace.recipe")
            .build();

    public CvdFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.CVD_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 層間膜を付けたウェハー（1〜8枚）。付けられなければ EMPTY */
    public static ItemStack product(ItemStack w) { return product(w, new ItemStack(Gas.OXYGEN_GAS)); }

    /** gas が酸素なら層間膜（SiO2）、アンモニアなら窒化膜の保護膜（Si3N4）。できなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack gas) {
        if (w.is(com.taku3.semiconductorlab.content.Chemistry.ITO_GLASS) && gas.is(Gas.HYDROGEN_GAS))   // [B-19] TFT：ITO ガラスに水素を含むアモルファスシリコンの膜（1979 ⚠️ 本物はプラズマ CVD、画素の並びは省略）
            return w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)).transmuteCopy(com.taku3.semiconductorlab.content.Branches.TFT_ARRAY_GLASS);
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ItemStack in = w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN));
        ChipOps.Result r = gas.is(Gas.AMMONIA_GAS) ? ChipOps.passivate(in) : gas.is(Gas.OXYGEN_GAS) ? ChipOps.depositInterlayer(in) : null;
        return r != null && r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack o2 = container.getItem(SLOT_O2), out = product(container.getItem(SLOT_WAFER), o2);
        return !out.isEmpty() && container.getItem(SLOT_SILANE).is(Semiconductor.SILANE_CYLINDER) && o2.getCount() >= O2_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), dust(out)) && canMerge(container.getItem(SLOT_CYL), new ItemStack(Gas.EMPTY_GAS_CYLINDER));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_O2));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_SILANE).shrink(1);
        container.getItem(SLOT_O2).shrink(O2_PER_RUN);
        merge(SLOT_OUT, dust(out));
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

    /** クリーンルームの等級のほこりと欠陥の密度（O-017・歩留まり、C の CleanRoom.dustAt：外 +2・クラス1000 +1・クラス100 0）。失敗にはしない＝汚れは検査台の漏れ・歩留まりに効く */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.inRoom(ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition)), CleanRoom.defectDensity(level, worldPosition));   // v0.5 #4 歩留まり：部屋の欠陥の密度も残す（A）
    }
}
