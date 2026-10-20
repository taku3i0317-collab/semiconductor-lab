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
            new Recipe(Optics.FLINT_LENS, Metallurgy.FLUORITE, 1, Optics.COATED_FLINT_LENS));

    /** 基板と付ける物から、使う組み合わせを探す（なければ null） */
    public static Recipe recipe(ItemStack substrate, ItemStack metal) {
        for (Recipe r : RECIPES) if (substrate.is(r.substrate()) && metal.is(r.metal())) return r;
        return null;
    }
    public static boolean isSubstrate(ItemStack s) { return RECIPES.stream().anyMatch(r -> s.is(r.substrate())); }
    public static boolean isMetal(ItemStack s) { return RECIPES.stream().anyMatch(r -> s.is(r.metal())); }

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
        Recipe r = recipe(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        if (r == null || container.getItem(SLOT_METAL).getCount() < r.count()) return false;
        VacuumChamberBlockEntity c = chamber();
        return c != null && c.getPressure() <= MAX_PA && canMerge(container.getItem(SLOT_OUT), new ItemStack(r.product()));
    }

    @Override protected void finishWork() {
        Recipe r = recipe(container.getItem(SLOT_SUBSTRATE), container.getItem(SLOT_METAL));
        if (r == null) return;
        container.getItem(SLOT_SUBSTRATE).shrink(1);
        container.getItem(SLOT_METAL).shrink(r.count());
        merge(SLOT_OUT, new ItemStack(r.product()));
        container.setChanged();
        VacuumChamberBlockEntity c = chamber();
        if (c != null) c.vent(VENT_FRACTION);
    }
}
