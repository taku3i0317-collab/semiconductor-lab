package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Survival;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * アンモニア蒸留器 [2-21]（19世紀の石炭ガス工場のやり方）：安水（[2-20]、NH3 約1〜2%）を熱して、溶けているアンモニア（遊離アンモニア）を追い出し、
 * 残った塩化アンモニウムなど（固定アンモニア）は消石灰を入れて追い出す：2NH4Cl + Ca(OH)2 → CaCl2 + 2NH3↑ + 2H2O。
 * 出てきたアンモニアの気体を冷たい水に吸わせると、濃いアンモニア水（約25%）になる。
 * <pre>
 *   安水 4 ＋ 消石灰 1 ＋ 空のガラス瓶 1 → アンモニア水（ガラス瓶）1   （20秒、1kW）
 *   吸わせる水：水バケツ 1 で {@link #RUNS_PER_WATER} 回（空のバケツが出口に戻る）
 * </pre>
 * ⚠️ 加熱は電気（本物は蒸気を吹き込む塔）。塩化カルシウムの廃液は出さない（[2-13] は保留中。ammonia_still.placeholder）。
 */
public class AmmoniaStillBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 20;
    public static final int LIQUOR_PER_RUN = 4, RUNS_PER_WATER = 4;
    public static final int SLOT_LIQUOR = 0, SLOT_LIME = 1, SLOT_BOTTLE = 2, SLOT_WATER = 3, SLOT_OUT = 4, SLOT_BUCKET = 5, SLOT_COUNT = 6;
    public static final int DATA_WATER = DATA_COUNT, EXTRA_DATA = 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_LIQUOR, 8, 17, s -> s.is(Chemistry.AMMONIACAL_LIQUOR))
            .in(SLOT_LIME, 8, 35, s -> s.is(Survival.SLAKED_LIME))
            .in(SLOT_BOTTLE, 26, 17, s -> s.is(Chemistry.GLASS_BOTTLE))
            .in(SLOT_WATER, 26, 35, s -> s.is(Items.WATER_BUCKET))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_BUCKET, 78, 48)
            .right("power", 17, 1, "gui.semiconductor-lab.ammonia_still.power").samples("500.0 kW")
            .right("water", 27, 1, "gui.semiconductor-lab.ammonia_still.water").samples("4")
            .right("hint", 38, 3, "gui.semiconductor-lab.ammonia_still.need_input", "gui.semiconductor-lab.ammonia_still.need_water",
                    "gui.semiconductor-lab.ammonia_still.need_power", "gui.semiconductor-lab.ammonia_still.running")
            .bottom("recipe", "gui.semiconductor-lab.ammonia_still.recipe")
            .build();

    /** 吸収の水があと何回ぶんか（保存する） */
    private int water;

    public AmmoniaStillBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.AMMONIA_STILL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getWater() { return water; }
    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_WATER ? water : 0; }
    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack lq = container.getItem(SLOT_LIQUOR);
        return lq.is(Chemistry.AMMONIACAL_LIQUOR) && lq.getCount() >= LIQUOR_PER_RUN
                && container.getItem(SLOT_LIME).is(Survival.SLAKED_LIME) && container.getItem(SLOT_BOTTLE).is(Chemistry.GLASS_BOTTLE)
                && (water > 0 || (container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) && canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET))))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.AMMONIA_WATER));
    }

    @Override protected void finishWork() {
        if (water <= 0) {
            container.getItem(SLOT_WATER).shrink(1);
            merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
            water = RUNS_PER_WATER;
        }
        water--;
        container.getItem(SLOT_LIQUOR).shrink(LIQUOR_PER_RUN);
        container.getItem(SLOT_LIME).shrink(1);
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_OUT, new ItemStack(Chemistry.AMMONIA_WATER));
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Water", water); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); water = in.getIntOr("Water", 0); }
}
