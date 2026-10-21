package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.energy.GeneratorBlockEntity;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Predicate;

/**
 * 化学電池（一次電池）の発電ブロック（IMP3、S-005。A と決めた形：GeneratorBlockEntity を継承、直流、回らないので交流発電機・ダイナモは積めない）。
 * 亜鉛（負極）が溶けるときに出す電子で電流が流れる：Zn → Zn2+ + 2e-。亜鉛1つと電解液1つで {@link #TICKS_PER_FUEL} tick 動き、尽きたら止まる。
 * <ul>
 * <li>{@link Kind#VOLTA} ボルタの電堆 [4-13]（1800）：銅と亜鉛を塩水の布ではさんで積む。正極（銅）に水素の泡がたまって電圧が下がっていく（分極）＝出力がだんだん落ちる</li>
 * <li>{@link Kind#DANIELL} ダニエル電池 [4-36]（1836）：亜鉛は硫酸亜鉛、銅は硫酸銅の液に入れ、素焼きの筒で分ける。正極で銅が出る（Cu2+ + 2e- → Cu）ので水素がたまらず、出力が落ちない</li>
 * <li>{@link Kind#DRY} 乾電池 [4-28]（1886〜1896）：亜鉛の缶（負極）＋二酸化マンガン（正極側、水素を水に変えて分極を防ぐ）＋塩化アンモニウムの糊＋炭素棒。
 *     亜鉛と液が缶の中にあるので、乾電池1本だけを電池入れに入れる（決定 Q3-12 ①、IMP3 S-006）</li>
 * </ul>
 * ⚠️ 電力はゲームの値（本物の1組は約1V・数十mA）。電圧は扱わない。
 */
public class ChemicalCellBlockEntity extends GeneratorBlockEntity implements MachineBE {
    public enum Kind {
        VOLTA(5, true, s -> s.is(Power.ZINC_SOURCE), s -> s.is(Gas.SALT)),
        DANIELL(10, false, s -> s.is(Power.ZINC_SOURCE), s -> s.is(Chemistry.COPPER_SULFATE)),
        /** 乾電池：亜鉛も糊も中にあるので、液のスロットは使わない（null） */
        DRY(8, false, s -> s.is(Power.DRY_CELL), null);
        public final double watts;
        public final boolean polarizes;
        /** 負極（亜鉛、乾電池なら乾電池そのもの）と電解液 */
        public final Predicate<ItemStack> fuel, electrolyte;
        Kind(double watts, boolean polarizes, Predicate<ItemStack> fuel, Predicate<ItemStack> electrolyte) { this.watts = watts; this.polarizes = polarizes; this.fuel = fuel; this.electrolyte = electrolyte; }
    }

    public static final int TICKS_PER_FUEL = 20 * 120;
    /** 分極の速さ（tick）と、下がりきったときの割合（ボルタ） */
    public static final double POLARIZE_TICKS = 600, POLARIZED_FLOOR = 0.3;
    public static final int SLOT_ZINC = 0, SLOT_ELECTROLYTE = 1, SLOT_COUNT = 2;
    public static final int DATA_DECI_W = 0, DATA_LEFT = 1, DATA_COUNT = 2;

    private static MachineLayout layout(Kind k) {
        if (k == Kind.DRY) return MachineLayout.builder(DATA_COUNT)
                .in(SLOT_ZINC, 56, 35, k.fuel)
                .texture("dry_cell_holder")
                .text("output", 82, 17, 90, 1, "gui.semiconductor-lab.chemical_cell.output").samples("10.0 W")
                .text("left", 82, 27, 90, 1, "gui.semiconductor-lab.chemical_cell.left").samples("100%")
                .text("hint", 82, 40, 90, 3, "gui.semiconductor-lab.chemical_cell.need_dry_cell", "gui.semiconductor-lab.chemical_cell.running")
                .build();
        return MachineLayout.builder(DATA_COUNT)
                .in(SLOT_ZINC, 56, 17, k.fuel)
                .in(SLOT_ELECTROLYTE, 56, 53, k.electrolyte)
                .texture(k == Kind.VOLTA ? "voltaic_pile" : "daniell_cell")
                .text("output", 82, 17, 90, 1, "gui.semiconductor-lab.chemical_cell.output").samples("10.0 W")
                .text("left", 82, 27, 90, 1, "gui.semiconductor-lab.chemical_cell.left").samples("100%")
                .text("hint", 82, 40, 90, 3, "gui.semiconductor-lab.chemical_cell.need_zinc", "gui.semiconductor-lab.chemical_cell.running", "gui.semiconductor-lab.chemical_cell.polarized")
                .build();
    }
    public static final MachineLayout VOLTA_LAYOUT = layout(Kind.VOLTA);
    public static final MachineLayout DANIELL_LAYOUT = layout(Kind.DANIELL);
    public static final MachineLayout DRY_LAYOUT = layout(Kind.DRY);

    private final Kind kind;
    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) { @Override public void setChanged() { super.setChanged(); ChemicalCellBlockEntity.this.setChanged(); } };
    /** 今の亜鉛があと何 tick もつか／今の亜鉛で何 tick 動いたか（分極） */
    private int left, used;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_DECI_W -> (int) Math.round(getLastWatts() * 10);
            case DATA_LEFT -> left <= 0 ? 0 : (int) Math.round(100.0 * left / TICKS_PER_FUEL);
            default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public ChemicalCellBlockEntity(Kind kind, BlockPos pos, BlockState state) {
        super(switch (kind) { case VOLTA -> Power.VOLTAIC_PILE.type; case DANIELL -> Power.DANIELL_CELL.type; case DRY -> Power.DRY_CELL_HOLDER.type; }, pos, state, 2_000, kind.watts);
        this.kind = kind;
    }

    public Kind kind() { return kind; }
    @Override public boolean supportsAlternator() { return false; }
    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }

    /** 分極したあとの出力の割合（ボルタだけ、使った時間で下がる） */
    public static double polarization(Kind k, int usedTicks) {
        if (!k.polarizes) return 1.0;
        return POLARIZED_FLOOR + (1 - POLARIZED_FLOOR) * Math.exp(-usedTicks / POLARIZE_TICKS);
    }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) {
        if (left <= 0) {
            ItemStack zinc = container.getItem(SLOT_ZINC), liquid = container.getItem(SLOT_ELECTROLYTE);
            if (!kind.fuel.test(zinc) || (kind.electrolyte != null && !kind.electrolyte.test(liquid))) return 0;
            zinc.shrink(1);
            if (kind.electrolyte != null) liquid.shrink(1);
            container.setChanged();
            left = TICKS_PER_FUEL;
            used = 0;   // 新しい亜鉛と液：分極も元にもどる
        }
        left--; used++;
        setChanged();
        return kind.watts * polarization(kind, used);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Left", left); out.putInt("Used", used); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); left = in.getIntOr("Left", 0); used = in.getIntOr("Used", 0); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) net.minecraft.world.Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
}
