package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * 高炉 [1-09]（最初のマルチブロック、DESIGN「マルチブロック」・Q28-2）。この制御ブロック＝炉床の湯口を正面の下の真ん中に置き、
 * まわりを れんが（焼いた粘土）で 3×3×4 に積む。2段目の4面は羽口（風の入口）、中は空気。羽口の外側に水車ふいご [3-04]。
 * <pre>
 *   1段目  BCB / BBB / BBB     （手前の行 → 奥の行。C＝この制御ブロック）
 *   2段目  BTB / T T / BTB     （T＝羽口）
 *   3・4段目 BBB / B B / BBB  （中は空気、上は開いている＝鉱石を入れる口）
 * </pre>
 * 炉を高くして風を強く送り続けると約1500℃になり、炭素が鉄に溶け込んで融点が約1150℃に下がり、溶けた鉄（銑鉄、C約4%）が流れ出る。
 * 石灰石（方解石＝CaCO3、バニラの方解石を石灰石の近似）は鉱石の土（SiO2）をスラグ（CaSiO3）にして分ける。
 * 判定は 20 tick に1回（MultiblockPattern）。組み上がっていないと動かない。画面を開くと足りない場所に粒子。
 * スロット：0＝鉄鉱石・砂鉄、1＝木炭、2＝石灰石、3＝銑鉄、4＝スラグ
 */
public class BlastFurnaceBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_ORE = 0, SLOT_FUEL = 1, SLOT_FLUX = 2, SLOT_OUT = 3, SLOT_SLAG = 4, SLOT_COUNT = 5;
    /** 温度（℃）：室温・木炭だけ（風なし）・送風 100%・銑鉄が溶けて流れる下限 */
    public static final int T_ROOM = 20, T_DRAFT = 900, T_BLAST = 1500, T_MELT = 1400;
    /** 銑鉄1個の時間・木炭1個が燃える時間・何個ごとに石灰石1つ（とスラグ1つ）（ゲームの値） */
    public static final int SMELT_TICKS = 400, FUEL_TICKS = 300, FLUX_EVERY = 4, CHECK_EVERY = 20;
    /** 足りない物の名前（lang multiblock.semiconductor-lab.&lt;name&gt;） */
    public static final List<String> NAMES = List.of("lining", "tuyere", "inside");
    public static final int DATA_TEMP = 0, DATA_PROGRESS = 1, DATA_TOTAL = 2, DATA_FORMED = 3, DATA_MISSING = 4, DATA_MISSING_KIND = 5,
            DATA_AIR = 6, DATA_BURN = 7, DATA_BURN_TOTAL = 8, DATA_COUNT = 9;

    private static MultiblockPattern shape;
    /** 形（れんが・羽口。羽口のブロックは Metallurgy に登録されてから作る） */
    public static MultiblockPattern shape() {
        if (shape == null) shape = MultiblockPattern.builder()
                .layer("BCB", "BBB", "BBB")
                .layer("BTB", "T T", "BTB")
                .layer("BBB", "B B", "BBB")
                .layer("BBB", "B B", "BBB")
                .where('B', Blocks.BRICKS, "lining")
                .where('T', Metallurgy.TUYERE, "tuyere")
                .controller('C').build();
        return shape;
    }

    public static final int ORE_X = 8, ORE_Y = 17, FUEL_X = 8, FUEL_Y = 53, FLUX_X = 26, FLUX_Y = 17, OUT_X = 78, OUT_Y = 26, SLAG_X = 78, SLAG_Y = 48,
            THERMO_X = 48, THERMO_Y = 17, THERMO_H = 52;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_ORE, ORE_X, ORE_Y, BloomeryBlockEntity::isOre)
            .in(SLOT_FUEL, FUEL_X, FUEL_Y, BlastFurnaceBlockEntity::isFuel)
            .in(SLOT_FLUX, FLUX_X, FLUX_Y, BlastFurnaceBlockEntity::isFlux)
            .out(SLOT_OUT, OUT_X, OUT_Y)
            .out(SLOT_SLAG, SLAG_X, SLAG_Y)
            .area("flame", FUEL_X + 3, 36, 10, 14)
            .area("thermometer", THERMO_X, THERMO_Y, 6, THERMO_H)
            .area("progress", 58, 30, 16, 6)
            .right("temp", 17, 1, "gui.semiconductor-lab.blast_furnace.temp").samples("1500")
            .right("air", 27, 1, "gui.semiconductor-lab.blast_furnace.air").samples("100")
            .right("hint", 38, 3, MachineLayout.keys("gui.semiconductor-lab.blast_furnace.", "not_formed", "need_ore", "need_fuel", "need_flux", "need_air", "smelting", "out_full"))
            .bottom("missing", "gui.semiconductor-lab.blast_furnace.missing").samples("35", "empty inside")
            .build();

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); BlastFurnaceBlockEntity.this.setChanged(); }
    };
    private int temp = T_ROOM, progress, burnTicks, air, made;
    private boolean formed;
    private int missing, missingKind;
    private MultiblockPattern.Result last;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_TEMP -> temp; case DATA_PROGRESS -> progress; case DATA_TOTAL -> SMELT_TICKS; case DATA_FORMED -> formed ? 1 : 0;
            case DATA_MISSING -> missing; case DATA_MISSING_KIND -> missingKind; case DATA_AIR -> air;
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) { super(Metallurgy.BLAST_FURNACE.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public boolean formed() { return formed; }
    public int temperature() { return temp; }
    public MultiblockPattern.Result lastCheck() { return last; }

    /** 木炭か、コークス（[1-10] コークス高炉、1709 ダービー。E の [2-20] ガス工場のレトルトで作る） */
    public static boolean isFuel(ItemStack s) { return s.is(Items.CHARCOAL) || s.is(Metallurgy.COKE); }
    /** 石灰石（バニラの方解石を石灰石 CaCO3 の近似とする。生石灰 [0-10] と同じ扱い） */
    public static boolean isFlux(ItemStack s) { return s.is(Items.CALCITE); }

    public Direction facing() {
        BlockState s = getBlockState();
        return s.hasProperty(HorizontalDirectionalBlock.FACING) ? s.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    /** 形を調べ直す（20 tick に1回と、テストから） */
    public MultiblockPattern.Result recheck() {
        if (level == null) return null;
        last = shape().check(level, worldPosition, facing());
        formed = last.formed();
        missing = last.problems().size();
        missingKind = missing == 0 ? 0 : Math.max(0, NAMES.indexOf(last.problems().get(0).expected()));
        return last;
    }

    /** 羽口の外のふいごから来る風（いちばん強いもの） */
    public double airflow() {
        if (level == null) return 0;
        MultiblockPattern p = shape();
        double best = 0;
        for (int y = 0; y < p.height(); y++) for (int z = 0; z < p.depth(); z++) for (int x = 0; x < p.width(); x++)
            if (p.layers().get(y)[z].charAt(x) == 'T') best = Math.max(best, BellowsBlockEntity.airflow(level, p.worldPos(worldPosition, facing(), x, y, z)));
        return best;
    }

    private boolean canOutput() {
        ItemStack out = container.getItem(SLOT_OUT), slag = container.getItem(SLOT_SLAG);
        return (out.isEmpty() || (out.is(Metallurgy.PIG_IRON) && out.getCount() < out.getMaxStackSize()))
                && (slag.isEmpty() || (slag.is(Metallurgy.BLAST_FURNACE_SLAG) && slag.getCount() < slag.getMaxStackSize()));
    }

    /** 石灰石が要る番か（FLUX_EVERY 個ごとに1つ） */
    private boolean needFlux() { return made % FLUX_EVERY == 0; }

    public static void tick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        if (level.getGameTime() % CHECK_EVERY == 0 || be.last == null) be.recheck();
        boolean work = be.formed && BloomeryBlockEntity.isOre(be.container.getItem(SLOT_ORE)) && be.canOutput()
                && (!be.needFlux() || isFlux(be.container.getItem(SLOT_FLUX)));
        if (be.burnTicks <= 0 && work && isFuel(be.container.getItem(SLOT_FUEL))) {
            be.container.getItem(SLOT_FUEL).shrink(1);
            be.burnTicks = FUEL_TICKS;
        }
        boolean burning = be.burnTicks > 0 && be.formed;
        if (be.burnTicks > 0) be.burnTicks--;
        double airflow = be.formed ? be.airflow() : 0;
        be.air = (int) Math.round(airflow * 100);
        // 大きな炉はゆっくり熱くなり、ゆっくり冷める
        int target = burning ? (int) Math.round(T_DRAFT + (T_BLAST - T_DRAFT) * airflow) : T_ROOM;
        be.temp += (int) Math.round((target - be.temp) * (target > be.temp ? 0.02 : 0.01));
        if (Math.abs(target - be.temp) < 3) be.temp = target;
        if (work && burning && be.temp >= T_MELT) {
            if (++be.progress >= SMELT_TICKS) {
                be.progress = 0;
                be.container.getItem(SLOT_ORE).shrink(1);
                if (be.needFlux()) {   // 石灰石が土をスラグにする：CaCO3 → CaO + CO2、CaO + SiO2 → CaSiO3
                    be.container.getItem(SLOT_FLUX).shrink(1);
                    merge(be.container, SLOT_SLAG, new ItemStack(Metallurgy.BLAST_FURNACE_SLAG));
                }
                merge(be.container, SLOT_OUT, new ItemStack(Metallurgy.PIG_IRON));
                be.made++;
                be.container.setChanged();
            }
        }
        be.setChanged();
        boolean lit = burning;
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    private static void merge(SimpleContainer c, int slot, ItemStack add) {
        ItemStack out = c.getItem(slot);
        if (out.isEmpty()) c.setItem(slot, add); else out.grow(add.getCount());
    }

    /** 画面を開いたとき、組み上がっていなければ足りない場所に粒子（DESIGN「見せる」2） */
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        if (level instanceof ServerLevel sl) {
            MultiblockPattern.Result r = recheck();
            if (r != null && !r.formed()) MultiblockPattern.showProblems(sl, r);
        }
        return MachineBE.super.createMenu(id, inv, player);
    }

    /** 足りない物の一覧（名前 → 数） */
    public List<String> missingSummary() {
        List<String> out = new ArrayList<>();
        if (last != null) last.missingCounts().forEach((k, v) -> out.add(k + "×" + v));
        return out;
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Temp", temp); out.putInt("Progress", progress); out.putInt("Burn", burnTicks); out.putInt("Made", made);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        temp = in.getIntOr("Temp", T_ROOM); progress = in.getIntOr("Progress", 0); burnTicks = in.getIntOr("Burn", 0); made = in.getIntOr("Made", 0);
    }
}
