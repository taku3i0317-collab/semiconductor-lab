package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModMachines;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * アチソン炉 [3-10]（1896 特許、人造黒鉛）。耐火れんがの箱に炭素（石炭・木炭）と砂を詰め、両端の黒鉛電極から
 * 炭素そのものに大電流を流す＝中身が電熱線になる。約2500℃以上で炭素の原子が並び直して黒鉛になる。
 *
 * 温度のしくみ（ゲームの簡単なモデル）：毎 tick「入った電力 − 逃げる熱」で温度が上下する。
 *   逃げる熱 = K × (温度 − 20℃)、K は「必要電力 REQUIRED_WATTS で 2600℃ に落ち着く」ように決めてある。
 *   → 電力が足りないと、いくら待っても 2500℃ に届かない（50kW なら約280℃で止まる）。
 * 炭素が入っていないと電流の通り道がないので、電気を受け取っても流れない（使わない）。
 */
public class AchesonFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double REQUIRED_WATTS = 500_000;           // 500kW（ゲームの値。本物は数百kW〜MW級）
    public static final double AMBIENT_C = 20, GRAPHITIZE_C = 2500, STEADY_C = 2600, GLOW_C = 800;
    public static final double TAU_SECONDS = 15;                   // 温まり方・冷え方の速さ（時定数）
    public static final double K_W_PER_C = REQUIRED_WATTS / (STEADY_C - AMBIENT_C);
    public static final double HEAT_CAPACITY_J_PER_C = K_W_PER_C * TAU_SECONDS;
    public static final int WORK_TICKS = 20 * 60;
    public static final int CARBON_PER_BATCH = 4, SAND_PER_BATCH = 1, GRAPHITE_PER_BATCH = 2;
    public static final int SLOT_CARBON = 0, SLOT_SAND = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    public static final int DATA_TEMP = DATA_COUNT, EXTRA_DATA = 1;

    private double temperature = AMBIENT_C;

    /** 画面のスロットの並び（IMP-28：前の AchesonFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_CARBON, 8, 17, AchesonFurnaceBlockEntity::isCarbon)
            .in(SLOT_SAND, 8, 53, AchesonFurnaceBlockEntity::isSand)
            .out(SLOT_OUT, 78, 35)
            .build();

    public AchesonFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ACHESON_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(REQUIRED_WATTS) * 4, REQUIRED_WATTS);
    }

    public static boolean isCarbon(ItemStack s) { return s.is(Items.COAL) || s.is(Items.CHARCOAL); }
    public static boolean isSand(ItemStack s) { return s.is(Items.SAND); }
    /** その電力を流し続けたときに落ち着く温度（℃）。GUI の「到達温度」表示用 */
    public static double steadyTemperature(double watts) { return AMBIENT_C + watts / K_W_PER_C; }

    public double getTemperature() { return temperature; }

    /** 炭素が詰まっている＝電極の間に電流の通り道がある */
    private boolean hasCharge() { return isCarbon(container.getItem(SLOT_CARBON)) && container.getItem(SLOT_CARBON).getCount() >= CARBON_PER_BATCH; }

    @Override protected double workWatts() { return REQUIRED_WATTS; }
    @Override protected int workTicks() { return WORK_TICKS; }

    @Override protected boolean canWork() {
        return hasCharge() && isSand(container.getItem(SLOT_SAND)) && container.getItem(SLOT_SAND).getCount() >= SAND_PER_BATCH
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(ModItems.SYNTHETIC_GRAPHITE, GRAPHITE_PER_BATCH));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_CARBON).shrink(CARBON_PER_BATCH);   // 揮発分（石炭のガス・タール）は燃えて出ていく
        container.getItem(SLOT_SAND).shrink(SAND_PER_BATCH);       // SiO2 → SiC → Si が蒸発（アチソンの考えた触媒のはたらき）
        merge(SLOT_OUT, new ItemStack(ModItems.SYNTHETIC_GRAPHITE, GRAPHITE_PER_BATCH));
        container.setChanged();
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        // 1) 電気を流す（炭素がなければ流れない）。1 tick に使えるのは最大 REQUIRED_WATTS 分
        long drawn = hasCharge() ? storage.extractRaw(Math.min(storage.getStoredJ(), EnergyStorage.wattsToJoulesPerTick(REQUIRED_WATTS))) : 0;
        double watts = drawn * 20.0;
        // 2) 温度：入った熱 − 逃げる熱（1 tick = 1/20 秒）
        double before = temperature;
        temperature += (watts - K_W_PER_C * (temperature - AMBIENT_C)) / HEAT_CAPACITY_J_PER_C / 20.0;
        if (temperature < AMBIENT_C) temperature = AMBIENT_C;
        setLastWatts(watts);
        // 3) 黒鉛化：材料がそろい、2500℃以上の間だけ進む。温度が下がったら一時停止（リセットはしない）
        boolean ready = canWork();
        setReady(ready);
        if (!ready) { if (progress != 0) progress = 0; }
        else if (temperature >= GRAPHITIZE_C && ++progress >= WORK_TICKS) { progress = 0; finishWork(); }
        if (drawn > 0 || Math.abs(before - temperature) > 1e-6) setChanged();
        setLit(level, pos, state, temperature >= GLOW_C);
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_TEMP ? (int) Math.round(temperature) : 0; }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("TempC", (int) Math.round(temperature)); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); temperature = in.getIntOr("TempC", (int) AMBIENT_C); }

}
