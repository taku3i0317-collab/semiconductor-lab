package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 真空蒸着装置 [5-14]（IMP2-01）：いちばん古い方法＝熱したタングステン線に金属をかけて溶かし、蒸発させる。
 * 隣の真空容器（ベルジャー）を使い、圧力が {@link #MAX_PA} 以下のときだけ動く（平均自由行程 ≈ 6.6mm·Pa ÷ p。
 * 0.01Pa なら約 0.66m で、ベルジャーより長い＝蒸発した原子が途中でぶつからずに、まっすぐ基板に付く）。
 * 1回ごとにベルジャーを開けて基板を入れ替えるので、空気が少し戻る（{@link #VENT_FRACTION}）。
 * [9-25]（IMP4-01 が追記）：加工中のウェハー（process_wafer）1〜8枚＋アルミニウム1 → Al を一面に付ける（ChipOps.depositMetal、LITHO_DESIGN §5）。
 *   ウェハーのデータを書き換えるので RECIPES の表ではなく {@link #chipProduct} で扱う。
 * 何に何を付けると何になるかは {@link #RECIPES} に1行ずつ（クラウンガラス＋銀 → 半透明鏡、[7-22] レンズ＋蛍石 CaF2 → 反射防止膜付きレンズ）。
 */
public class EvaporatorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 300;
    public static final int TICKS = 20 * 20;
    public static final double MAX_PA = 0.01;
    public static final double VENT_FRACTION = 0.05;
    public static final int SLOT_SUBSTRATE = 0, SLOT_METAL = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    public static final int DATA_PRESSURE = ConsumerBlockEntity.DATA_COUNT, MENU_DATA_COUNT = ConsumerBlockEntity.DATA_COUNT + 1;

    /** 蒸着の組み合わせ：基板＋金属（count 個）→ 製品 */
    public record Recipe(Item substrate, Item metal, int count, Item product) {}

    /** 蒸着の組み合わせの表（基板・付ける物・個数・製品）。足すときはここに1行 */
    public static final java.util.List<Recipe> RECIPES = java.util.List.of(
            new Recipe(Optics.CROWN_GLASS, Metallurgy.SILVER_NUGGET, 1, Optics.HALF_SILVERED_MIRROR),
            // [7-22] 反射防止膜（ストロング 1936）：CaF2（蛍石 [1-21]）を波長の 1/4 の厚さに付ける
            new Recipe(Optics.CROWN_LENS, Metallurgy.FLUORITE, 1, Optics.COATED_CROWN_LENS),
            // [7-05] 段階3 アルミ蒸着の鏡（ストロング 1930年代、IMP2 O-020）：クラウンガラスにアルミを厚めに付ける（曇らない）
            new Recipe(Optics.CROWN_GLASS, Metallurgy.ALUMINIUM_INGOT, 1, Optics.ALUMINIZED_MIRROR),
            new Recipe(Optics.FLINT_LENS, Metallurgy.FLUORITE, 1, Optics.COATED_FLINT_LENS),
            new Recipe(Optics.SYNTHETIC_QUARTZ, Metallurgy.CHROMIUM, 1, Optics.CHROME_BLANK),   // [7-12] クロムマスクのブランク（IMP2 O-015）
            new Recipe(Optics.SYNTHETIC_QUARTZ, Metallurgy.FLUORITE, 2, Optics.I_LINE_FILTER),   // O-017 i線フィルター（薄い膜を重ねた干渉フィルター、IMP2）
            // [2-46] ITO（IMP3 O-015）：ホウケイ酸ガラスにインジウム錫の合金を付ける → かまどで焼いて酸化すると ITO
            new Recipe(com.taku3.semiconductorlab.content.Chemistry.BOROSILICATE_GLASS, com.taku3.semiconductorlab.content.Chemistry.INDIUM_TIN_ALLOY, 1, com.taku3.semiconductorlab.content.Chemistry.INDIUM_TIN_COATED_GLASS));

    /** 基板と付ける物から、使う組み合わせを探す（なければ null） */
    public static Recipe recipe(ItemStack substrate, ItemStack metal) {
        for (Recipe r : RECIPES) if (substrate.is(r.substrate()) && metal.is(r.metal())) return r;
        return null;
    }
    public static boolean isSubstrate(ItemStack s) { return RECIPES.stream().anyMatch(r -> s.is(r.substrate())) || com.taku3.semiconductorlab.litho.ChipOps.canDepositMetal(s) || s.is(com.taku3.semiconductorlab.content.Semiconductor.PN_JUNCTION_WAFER); }
    public static boolean isMetal(ItemStack s) { return RECIPES.stream().anyMatch(r -> s.is(r.metal())) || s.is(Metallurgy.ALUMINIUM_INGOT); }

    /** [9-25] 加工中のウェハーに Al を付けたもの（1回に1〜8枚、Al 1個）。当てはまらなければ EMPTY */
    public static ItemStack chipProduct(ItemStack substrate, ItemStack metal) {
        if (substrate.is(com.taku3.semiconductorlab.content.Semiconductor.PN_JUNCTION_WAFER) && metal.is(Metallurgy.ALUMINIUM_INGOT)) {
            // [9-17] メサ：p-n 接合の板の表面（n の層）にマスク越しに Al の点を付ける。n の表面でないと、焼いても接合にならないので断る
            if (!"boron".equals(substrate.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT))) return ItemStack.EMPTY;
            ItemStack out = substrate.transmuteCopy(com.taku3.semiconductorlab.content.Semiconductor.AL_DOTTED_WAFER, Math.min(substrate.getCount(), CHIP_BATCH));
            return out;
        }
        if (!com.taku3.semiconductorlab.litho.ChipOps.isProcessWafer(substrate) || !metal.is(Metallurgy.ALUMINIUM_INGOT)) return ItemStack.EMPTY;
        var r = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(substrate.copyWithCount(Math.min(substrate.getCount(), CHIP_BATCH)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }
    public static final int CHIP_BATCH = 8;

    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int i) {
            if (i == DATA_PRESSURE) { VacuumChamberBlockEntity c = chamber(); return c == null ? 0 : SealingStationBlockEntity.encodePa(c.getPressure()); }
            return data.get(i);
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return MENU_DATA_COUNT; }
    };

    public static final MachineLayout LAYOUT = MachineLayout.builder(MENU_DATA_COUNT)
            .in(SLOT_SUBSTRATE, 8, 26, EvaporatorBlockEntity::isSubstrate)
            .in(SLOT_METAL, 8, 50, EvaporatorBlockEntity::isMetal)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.evaporator.power").samples("0.3 kW")
            .right("pressure", 27, 1, "gui.semiconductor-lab.evaporator.pressure").samples("101 kPa").fit()
            .right("target", 37, 1, "gui.semiconductor-lab.evaporator.target").samples("0.01 Pa")
            .right("hint", 48, 2, "gui.semiconductor-lab.evaporator.need_input", "gui.semiconductor-lab.evaporator.need_chamber",
                    "gui.semiconductor-lab.evaporator.need_vacuum", "gui.semiconductor-lab.evaporator.output_full",
                    "gui.semiconductor-lab.evaporator.need_power", "gui.semiconductor-lab.evaporator.running")
            .build();

    public EvaporatorBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.EVAPORATOR.type, pos, state, SLOT_COUNT, 20_000, 2_000);
    }

    @Override public ContainerData getMenuData() { return menuData; }
    private VacuumChamberBlockEntity chamber() { return level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack chip = chipProduct(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        if (!chip.isEmpty()) {
            VacuumChamberBlockEntity c = chamber();
            return c != null && c.getPressure() <= MAX_PA && canMerge(container.getItem(SLOT_OUT), chip);
        }
        Recipe r = recipe(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        if (r == null || container.getItem(SLOT_METAL).getCount() < r.count()) return false;
        VacuumChamberBlockEntity c = chamber();
        return c != null && c.getPressure() <= MAX_PA && canMerge(container.getItem(SLOT_OUT), new ItemStack(r.product()));
    }

    /** [8-15] これまでに蒸着した回数（水晶振動子の膜厚計が読む、IMP2 O-016） */
    private int runs;
    public int runs() { return runs; }

    @Override protected void finishWork() {
        runs++;
        ItemStack chip = chipProduct(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        // [5-07] 隣の水銀拡散ポンプの蒸気が戻ると膜にほこり（ターボ分子ポンプならきれい、IMP2 O-017。A の ChipOps.addDust）
        VacuumChamberBlockEntity ch0 = chamber();
        if (!chip.isEmpty() && level != null && ch0 != null && TurboPumpBlockEntity.backstreams(level, ch0.getBlockPos()))
            chip = com.taku3.semiconductorlab.litho.ChipOps.addDust(chip, 1);
        if (!chip.isEmpty()) {
            container.getItem(SLOT_SUBSTRATE).shrink(chip.getCount());
            container.getItem(SLOT_METAL).shrink(1);
            merge(SLOT_OUT, chip);
            container.setChanged();
            VacuumChamberBlockEntity c = chamber();
            if (c != null) c.vent(VENT_FRACTION);
            return;
        }
        Recipe r = recipe(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        if (r == null) return;
        container.getItem(SLOT_SUBSTRATE).shrink(1);
        container.getItem(SLOT_METAL).shrink(r.count());
        merge(SLOT_OUT, new ItemStack(r.product()));
        container.setChanged();
        VacuumChamberBlockEntity c = chamber();
        if (c != null) c.vent(VENT_FRACTION);
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out) { super.saveAdditional(out); out.putInt("Runs", runs); }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in) { super.loadAdditional(in); runs = in.getIntOr("Runs", 0); }
}
