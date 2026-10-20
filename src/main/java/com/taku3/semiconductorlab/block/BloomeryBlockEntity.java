package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 塊鉄炉（ブルーマリー）[1-06]。木炭を燃やし、鉄鉱石（赤鉄鉱の近似＝バニラの鉄の原石、CHEMISTRY #35）か砂鉄を、溶かさずに還元する。
 * <pre>
 *   Fe2O3 + 3CO → 2Fe + 3CO2（約1100〜1200℃。鉄の融点 1538℃ には届かないので、スラグの混じったスポンジ状の鉄＝ブルームになる）
 * </pre>
 * 温度は木炭の火だけでは約 {@link #T_DRAFT}℃。横のふいご [3-04] で風を送ると上がり、送風 100% で約 {@link #T_BLAST}℃
 * （TECH_TREE の決定：ふいご＋木炭 ≈1300℃）。{@link #T_REDUCE}℃ 以上の間だけ還元が進む＝踏みつづける。
 * ブルームはたき石で叩いてスラグを出すと錬鉄（バニラの鉄インゴット）になる（作業台、たき石は戻る）。
 * スロット：0＝鉱石、1＝木炭、2＝ブルーム
 */
public class BloomeryBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_ORE = 0, SLOT_FUEL = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    /** 温度（℃）：室温・木炭だけ・送風 100%・還元が進む下限 */
    public static final int T_ROOM = 20, T_DRAFT = 800, T_BLAST = 1300, T_REDUCE = 1100;
    /** 1個の鉱石を還元する時間（ゲームの値）・木炭1個が燃える時間 */
    public static final int REDUCE_TICKS = 600, FUEL_TICKS = 400;
    public static final int DATA_TEMP = 0, DATA_PROGRESS = 1, DATA_TOTAL = 2, DATA_BURN = 3, DATA_BURN_TOTAL = 4, DATA_AIR = 5, DATA_COUNT = 6;

    public static final int ORE_X = 20, ORE_Y = 17, FUEL_X = 20, FUEL_Y = 53, OUT_X = 78, OUT_Y = 35, THERMO_X = 44, THERMO_Y = 17, THERMO_H = 52;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_ORE, ORE_X, ORE_Y, BloomeryBlockEntity::isOre)
            .in(SLOT_FUEL, FUEL_X, FUEL_Y, BloomeryBlockEntity::isFuel)
            .out(SLOT_OUT, OUT_X, OUT_Y)
            .area("flame", FUEL_X + 3, 36, 10, 14)
            .area("thermometer", THERMO_X, THERMO_Y, 6, THERMO_H)
            .area("progress", 54, 38, 20, 6)
            .right("temp", 17, 1, "gui.semiconductor-lab.bloomery.temp").samples("1300")
            .right("air", 27, 1, "gui.semiconductor-lab.bloomery.air").samples("100")
            .right("hint", 38, 3, MachineLayout.keys("gui.semiconductor-lab.bloomery.", "need_ore", "need_fuel", "need_air", "reducing", "out_full"))
            .bottom("formula", "=Fe2O3 + 3CO → 2Fe + 3CO2")
            .build();

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); BloomeryBlockEntity.this.setChanged(); }
    };
    private int temp = T_ROOM, progress, burnTicks, air;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_TEMP -> temp; case DATA_PROGRESS -> progress; case DATA_TOTAL -> REDUCE_TICKS;
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS; case DATA_AIR -> air; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public BloomeryBlockEntity(BlockPos pos, BlockState state) { super(Metallurgy.BLOOMERY.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int temperature() { return temp; }
    public int progress() { return progress; }

    /** 鉄鉱石（バニラの鉄の原石＝赤鉄鉱 Fe2O3 の近似）か砂鉄（磁鉄鉱 Fe3O4 の粒） */
    public static boolean isOre(ItemStack s) { return s.is(Items.RAW_IRON) || s.is(Metallurgy.IRON_SAND.asItem()); }
    /** 木炭だけ（石炭は硫黄が鉄に入ってもろくなるので、昔の製鉄は木炭） */
    public static boolean isFuel(ItemStack s) { return s.is(Items.CHARCOAL); }

    /** 火と風で決まる温度の行き先 */
    public static int targetTemp(boolean burning, double airflow) {
        return burning ? (int) Math.round(T_DRAFT + (T_BLAST - T_DRAFT) * Math.max(0, Math.min(1, airflow))) : T_ROOM;
    }

    private boolean canOutput() {
        ItemStack out = container.getItem(SLOT_OUT);
        return out.isEmpty() || (out.is(Metallurgy.IRON_BLOOM) && out.getCount() < out.getMaxStackSize());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BloomeryBlockEntity be) {
        if (level.isClientSide()) return;
        boolean work = isOre(be.container.getItem(SLOT_ORE)) && be.canOutput();
        // 焼く物があれば木炭をくべる（空焚きはしない）
        if (be.burnTicks <= 0 && work && isFuel(be.container.getItem(SLOT_FUEL))) {
            be.container.getItem(SLOT_FUEL).shrink(1);
            be.burnTicks = FUEL_TICKS;
        }
        boolean burning = be.burnTicks > 0;
        if (burning) be.burnTicks--;
        double airflow = BellowsBlockEntity.airflow(level, pos);
        be.air = (int) Math.round(airflow * 100);
        // 温度は行き先へ少しずつ（火は速く上がり、ゆっくり冷める）
        int target = targetTemp(burning, airflow);
        be.temp += (int) Math.round((target - be.temp) * (target > be.temp ? 0.05 : 0.02));
        if (Math.abs(target - be.temp) < 3) be.temp = target;
        if (work && burning && be.temp >= T_REDUCE) {
            if (++be.progress >= REDUCE_TICKS) {
                be.progress = 0;
                be.container.getItem(SLOT_ORE).shrink(1);
                ItemStack out = be.container.getItem(SLOT_OUT);
                if (out.isEmpty()) be.container.setItem(SLOT_OUT, new ItemStack(Metallurgy.IRON_BLOOM)); else out.grow(1);
                be.container.setChanged();
            }
        }
        be.setChanged();
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != burning)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, burning), 3);
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Temp", temp); out.putInt("Progress", progress); out.putInt("Burn", burnTicks);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        temp = in.getIntOr("Temp", T_ROOM); progress = in.getIntOr("Progress", 0); burnTicks = in.getIntOr("Burn", 0);
    }
}
