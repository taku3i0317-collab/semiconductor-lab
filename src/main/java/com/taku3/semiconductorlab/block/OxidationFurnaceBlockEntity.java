package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 酸化炉 [9-15]（1955 フロッシュとデリック、ベル研）。電気炉 [4-12] の石英管 [7-02] にシリコンの板を入れ、約1100℃で酸素か水蒸気を流すと、
 * 表面が Si + O2 → SiO2（ドライ）／ Si + 2H2O → SiO2 + 2H2（ウェット）でガラスの膜になる。この膜はドーパントを通さない「マスク」になる。
 * シリコンの板 ×{@link #WAFERS_PER_RUN}（または酸化膜つきの板）＋ 酸素1袋（ドライ）か 水バケツ1（ウェット、空のバケツが戻る）
 * → 酸化膜つきの板 ×{@link #WAFERS_PER_RUN}。膜の厚さ（nm）は Deal–Grove の式（1965）で、1回＝1100℃で1時間ぶん。
 * もう一度入れると、今の厚さから続けて育つ（厚いほど酸素が膜を通りにくく、伸びが遅くなる）。
 * 加工中のウェハー（process_wafer、LITHO_DESIGN）も入る：開いている窓を閉じ直す（ChipOps.reoxidize、窓の開けまちがいのやり直し）。1回に1〜8枚。
 * ⚠️ 電力（5kW）・時間（120秒＝本物の1時間）はゲームの値（拡散炉と同じ）。
 */
public class OxidationFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 120;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_OXIDANT = 1, SLOT_OUT = 2, SLOT_BUCKET = 3, SLOT_COUNT = 4;
    private static final int PULL_INTERVAL = 10, O2_TARGET = 16;

    /**
     * Deal–Grove の定数（1100℃、&lt;111&gt; シリコン。B＝放物線の速さ µm²/h、B/A＝直線の速さ µm/h）。
     * 出典：Deal & Grove, J. Appl. Phys. 36, 3770 (1965) の表（教科書 Jaeger『Introduction to Microelectronic Fabrication』の表 3.1 に同じ値）
     */
    public static final double DRY_B = 0.027, DRY_B_OVER_A = 0.30, WET_B = 0.51, WET_B_OVER_A = 4.64;
    public static final double HOURS_PER_RUN = 1.0;

    public enum Mode { NONE, DRY, WET }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, OxidationFurnaceBlockEntity::isWafer)
            .in(SLOT_OXIDANT, 8, 50, s -> modeOf(s) != Mode.NONE)
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_BUCKET, 78, 50)
            .right("power", 17, 1, "gui.semiconductor-lab.oxidation_furnace.power").samples("500.0 kW")
            .right("need", 27, 1, "gui.semiconductor-lab.oxidation_furnace.need").samples("50 kW")
            .right("hint", 38, 3, "gui.semiconductor-lab.oxidation_furnace.need_input", "gui.semiconductor-lab.oxidation_furnace.need_power",
                    "gui.semiconductor-lab.oxidation_furnace.running_dry", "gui.semiconductor-lab.oxidation_furnace.running_wet")
            .bottom("recipe", "gui.semiconductor-lab.oxidation_furnace.recipe_dry", "gui.semiconductor-lab.oxidation_furnace.recipe_wet", "gui.semiconductor-lab.oxidation_furnace.recipe")
            .build();

    public OxidationFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.OXIDATION_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isWafer(ItemStack s) { return s.is(Semiconductor.SILICON_WAFER) || s.is(Semiconductor.OXIDIZED_WAFER) || ChipOps.canReoxidize(s); }

    /** 1回に焼く枚数：板は8枚ちょうど、加工中のウェハーは1〜8枚（あれば全部） */
    public static int batch(ItemStack w) {
        if (ChipOps.isProcessWafer(w)) return Math.min(w.getCount(), WAFERS_PER_RUN);
        return w.getCount() >= WAFERS_PER_RUN ? WAFERS_PER_RUN : 0;
    }

    public static Mode modeOf(ItemStack oxidant) {
        if (oxidant.is(Gas.OXYGEN_GAS)) return Mode.DRY;
        if (oxidant.is(Items.WATER_BUCKET)) return Mode.WET;
        return Mode.NONE;
    }
    public Mode mode() { return modeOf(container.getItem(SLOT_OXIDANT)); }

    /** 今の厚さ x0（nm）から、hours 時間でどこまで育つか（Deal–Grove：x² + A·x = B·(t + τ)、τ は x0 までにかかった時間） */
    public static int grow(int x0nm, Mode m, double hours) {
        double b = m == Mode.WET ? WET_B : DRY_B, a = b / (m == Mode.WET ? WET_B_OVER_A : DRY_B_OVER_A);
        double x0 = x0nm / 1000.0;
        double tau = (x0 * x0 + a * x0) / b;
        double x = a / 2 * (Math.sqrt(1 + 4 * b * (hours + tau) / (a * a)) - 1);
        return (int) Math.round(x * 1000);
    }

    public static int oxideOf(ItemStack s) { return s.getOrDefault(ModDataComponents.OXIDE_NM, 0); }

    /**
     * 膜の色（光の干渉：膜の表と裏で反射した光が強め合う色が、厚さで変わる）。シリコンの上の熱酸化膜の色見本
     * （Pliskin & Conrad, IBM J. Res. Dev. 8, 43 (1964) の表を大まかにまとめた。境目の厚さ nm → lang の色の名前）
     */
    private static final int[] COLOR_UPTO = {40, 60, 80, 110, 140, 160, 190, 230, 260, 285, 330, 355, 395, 430, 470, 500, 530, 590, 630, 680};
    private static final String[] COLOR_NAME = {"silver", "tan", "brown", "violet", "blue", "light_blue", "yellow", "gold", "orange", "red_violet",
            "blue", "green", "yellow", "pink", "red_violet", "blue", "green", "yellow", "pink", "violet"};
    public static String colorOf(int nm) {
        for (int i = 0; i < COLOR_UPTO.length; i++) if (nm < COLOR_UPTO[i]) return COLOR_NAME[i];
        return "blue_green";   // これより厚い所は色がくり返す（見本の続き。大まかに）
    }

    /** 酸化膜つきの板：元の板の純度・ドーパントを受け継ぎ、膜を育てる */
    public static ItemStack oxidized(ItemStack wafer, Mode m, int count) {
        if (ChipOps.isProcessWafer(wafer)) {   // 加工中のウェハー：窓を閉じ、膜をその分育てる
            ChipOps.Result r = ChipOps.reoxidize(wafer.copyWithCount(count));
            if (!r.ok()) return ItemStack.EMPTY;
            ItemStack out = r.out();
            out.set(ModDataComponents.OXIDE_NM, grow(oxideOf(wafer), m, HOURS_PER_RUN));
            return out;
        }
        ItemStack s = new ItemStack(Semiconductor.OXIDIZED_WAFER, count);
        Double p = wafer.get(ModDataComponents.PURITY);
        if (p != null) s.set(ModDataComponents.PURITY, p);
        String d = wafer.get(ModDataComponents.DOPANT);
        if (d != null) s.set(ModDataComponents.DOPANT, d);
        String epi = wafer.get(ModDataComponents.EPI);   // [9-30] エピタキシャル層も受け継ぐ
        if (epi != null) s.set(ModDataComponents.EPI, epi);
        s.set(ModDataComponents.OXIDE_NM, grow(oxideOf(wafer), m, HOURS_PER_RUN));
        return s;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        Mode m = mode();
        ItemStack w = container.getItem(SLOT_WAFER);
        int n = batch(w);
        if (m == Mode.NONE || !isWafer(w) || n == 0) return false;
        if (m == Mode.WET && !canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET))) return false;
        ItemStack out = oxidized(w, m, n);
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        Mode m = mode();
        int n = batch(container.getItem(SLOT_WAFER));
        ItemStack out = oxidized(container.getItem(SLOT_WAFER), m, n);
        container.getItem(SLOT_WAFER).shrink(n);
        container.getItem(SLOT_OXIDANT).shrink(1);
        if (m == Mode.WET) merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダー（酸素）から、スロットが 16 袋になるまで補充する（水バケツが入っているときはしない） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack o = container.getItem(SLOT_OXIDANT);
        if (!o.isEmpty() && !o.is(Gas.OXYGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = O2_TARGET - container.getItem(SLOT_OXIDANT).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.OXYGEN, want);
            if (n > 0) merge(SLOT_OXIDANT, new ItemStack(Gas.OXYGEN_GAS, n));
        }
    }
}
