package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * るつぼ炉 [3-07]（1740 ごろ ハンツマン。IMP4-01、O-030 案A 仮決定）：コークスの火の中にるつぼを入れ、中身を燃料や炎にふれさせずに溶かす。
 * ふいご [3-04] で風を送ると約 {@link #T_BLAST}℃。るつぼの仕込み → るつぼ鋼2（{@link #T_MELT}℃ 以上）、フリントガラスの調合 → フリントガラス。
 * 溶けた後のるつぼは取り出して使い回せる（{@link #CRUCIBLE_USES} 回目で割れる ⚠️）。溶鉱炉（バニラ）で焼くとるつぼは戻らない。
 * 塊鉄炉 {@link BloomeryBlockEntity} と同じ温度のしくみ。
 */
public class CrucibleFurnaceBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_CHARGE = 0, SLOT_FUEL = 1, SLOT_OUT = 2, SLOT_CRUCIBLE = 3, SLOT_COUNT = 4;
    /** 温度（℃）：室温・コークスだけ・送風 100%・溶ける下限（鋼 約1500℃） */
    public static final int T_ROOM = 20, T_DRAFT = 1100, T_BLAST = 1600, T_MELT = 1500;
    public static final int MELT_TICKS = 400, FUEL_TICKS = 800, CRUCIBLE_USES = 3;
    public static final int DATA_TEMP = 0, DATA_PROGRESS = 1, DATA_TOTAL = 2, DATA_BURN = 3, DATA_BURN_TOTAL = 4, DATA_AIR = 5, DATA_COUNT = 6;

    public static final int CHARGE_X = 20, CHARGE_Y = 17, FUEL_X = 20, FUEL_Y = 53, THERMO_X = 44, THERMO_Y = 17, THERMO_H = 52;
    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT)
            .in(SLOT_CHARGE, CHARGE_X, CHARGE_Y, CrucibleFurnaceBlockEntity::isCharge)
            .in(SLOT_FUEL, FUEL_X, FUEL_Y, CrucibleFurnaceBlockEntity::isFuel)
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_CRUCIBLE, 78, 48)
            .area("flame", FUEL_X + 3, 36, 10, 14)
            .area("thermometer", THERMO_X, THERMO_Y, 6, THERMO_H)
            .area("progress", 54, 38, 20, 6)
            .right("temp", 17, 1, "gui.semiconductor-lab.crucible_furnace.temp").samples("1600")
            .right("air", 27, 1, "gui.semiconductor-lab.crucible_furnace.air").samples("100")
            .right("hint", 38, 3, MachineLayout.keys("gui.semiconductor-lab.crucible_furnace.", "need_charge", "need_fuel", "need_air", "melting", "out_full"))
            .bottom("formula", "gui.semiconductor-lab.crucible_furnace.recipe")
            .build();

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); CrucibleFurnaceBlockEntity.this.setChanged(); }
    };
    private int temp = T_ROOM, progress, burnTicks, air, heats;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_TEMP -> temp; case DATA_PROGRESS -> progress; case DATA_TOTAL -> MELT_TICKS;
            case DATA_BURN -> burnTicks; case DATA_BURN_TOTAL -> FUEL_TICKS; case DATA_AIR -> air; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public CrucibleFurnaceBlockEntity(BlockPos pos, BlockState state) { super(Heat.CRUCIBLE_FURNACE.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }
    public int temperature() { return temp; }

    /** るつぼの仕込み（鍛鉄＋木炭入り）かフリントガラスの調合 */
    public static boolean isCharge(ItemStack s) { return s.is(Metallurgy.CRUCIBLE_CHARGE) || s.is(Optics.FLINT_GLASS_BATCH); }
    /** コークス（1700年代のハンツマンもコークスの火） */
    public static boolean isFuel(ItemStack s) { return s.is(Metallurgy.COKE); }

    /** 溶かすと何になるか */
    public static ItemStack melted(ItemStack in) {
        if (in.is(Metallurgy.CRUCIBLE_CHARGE)) return new ItemStack(Metallurgy.CRUCIBLE_STEEL, 2);
        if (in.is(Optics.FLINT_GLASS_BATCH)) return new ItemStack(Optics.FLINT_GLASS);
        return ItemStack.EMPTY;
    }

    public static int targetTemp(boolean burning, double airflow) {
        return burning ? (int) Math.round(T_DRAFT + (T_BLAST - T_DRAFT) * Math.max(0, Math.min(1, airflow))) : T_ROOM;
    }

    private boolean canOutput() {
        ItemStack want = melted(container.getItem(SLOT_CHARGE)), out = container.getItem(SLOT_OUT);
        if (want.isEmpty()) return false;
        boolean fits = out.isEmpty() || (ItemStack.isSameItemSameComponents(out, want) && out.getCount() + want.getCount() <= out.getMaxStackSize());
        ItemStack cr = container.getItem(SLOT_CRUCIBLE);
        return fits && (cr.isEmpty() || cr.getCount() < cr.getMaxStackSize());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleFurnaceBlockEntity be) {
        if (level.isClientSide()) return;
        boolean work = be.canOutput();
        if (be.burnTicks <= 0 && work && isFuel(be.container.getItem(SLOT_FUEL))) {
            be.container.getItem(SLOT_FUEL).shrink(1);
            be.burnTicks = FUEL_TICKS;
        }
        boolean burning = be.burnTicks > 0;
        if (burning) be.burnTicks--;
        double airflow = BellowsBlockEntity.airflow(level, pos);
        be.air = (int) Math.round(airflow * 100);
        int target = targetTemp(burning, airflow);
        be.temp += (int) Math.round((target - be.temp) * (target > be.temp ? 0.05 : 0.02));
        if (Math.abs(target - be.temp) < 3) be.temp = target;
        if (work && burning && be.temp >= T_MELT) {
            if (++be.progress >= MELT_TICKS) { be.progress = 0; be.finish(); }
        }
        be.setChanged();
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != burning)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, burning), 3);
    }

    /** 1回溶かし終えた（テストからも呼ぶ）：出来た物を出し、鋼ならるつぼを戻す（{@link #CRUCIBLE_USES} 回目は割れる） */
    public void finish() {
        ItemStack in = container.getItem(SLOT_CHARGE);
        ItemStack want = melted(in);
        if (want.isEmpty()) return;
        boolean steel = in.is(Metallurgy.CRUCIBLE_CHARGE);
        in.shrink(1);
        ItemStack out = container.getItem(SLOT_OUT);
        if (out.isEmpty()) container.setItem(SLOT_OUT, want); else out.grow(want.getCount());
        if (steel && ++heats % CRUCIBLE_USES != 0) {
            ItemStack cr = container.getItem(SLOT_CRUCIBLE);
            if (cr.isEmpty()) container.setItem(SLOT_CRUCIBLE, new ItemStack(Metallurgy.CLAY_CRUCIBLE)); else cr.grow(1);
        }
        container.setChanged();
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, container.getItems());
        out.putInt("Temp", temp); out.putInt("Progress", progress); out.putInt("Burn", burnTicks); out.putInt("Heats", heats);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
        temp = in.getIntOr("Temp", T_ROOM); progress = in.getIntOr("Progress", 0); burnTicks = in.getIntOr("Burn", 0); heats = in.getIntOr("Heats", 0);
    }
}
