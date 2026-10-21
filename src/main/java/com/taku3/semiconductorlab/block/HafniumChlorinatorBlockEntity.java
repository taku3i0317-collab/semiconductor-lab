package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ハフニウムの塩素化炉（IMP4-01、S-004：high-k の ALD の原料 HfCl4）：酸化ハフニウムをコークスといっしょに約900℃ に熱し、塩素を通す（炭素塩素化）。
 * HfO2 + 2C + 2Cl2 → HfCl4 + 2CO。HfCl4 は約317℃ で昇華するので、冷えた所で白い固体として集める（四塩化チタンをクロール法で作るのと同じ考え）。
 * 酸化ハフニウム 1 ＋ コークス {@link #COKE_PER_RUN} ＋ 塩素 {@link #CL2_PER_RUN} ＋ 空のガラス瓶 → 四塩化ハフニウム（瓶入り、湿気で HCl を出すので密封）＋ 一酸化炭素 2。
 * ⚠️ 電力・時間・量はゲームの値。一酸化炭素は毒の気体（袋で出す）（hafnium_chlorinator.placeholder）。
 */
public class HafniumChlorinatorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 20;
    public static final int COKE_PER_RUN = 2, CL2_PER_RUN = 2, CO_PER_RUN = 2;
    public static final int SLOT_OXIDE = 0, SLOT_COKE = 1, SLOT_CL2 = 2, SLOT_BOTTLE = 3, SLOT_OUT = 4, SLOT_CO = 5, SLOT_COUNT = 6;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_OXIDE, 8, 17, s -> s.is(Metallurgy.HAFNIUM_OXIDE))
            .in(SLOT_COKE, 8, 35, s -> s.is(Metallurgy.COKE))
            .in(SLOT_CL2, 8, 53, s -> s.is(Gas.CHLORINE_GAS))
            .in(SLOT_BOTTLE, 30, 53, s -> s.is(Chemistry.GLASS_BOTTLE))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_CO, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.hafnium_chlorinator.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.hafnium_chlorinator.need_input", "gui.semiconductor-lab.hafnium_chlorinator.need_power", "gui.semiconductor-lab.hafnium_chlorinator.running")
            .bottom("recipe", "gui.semiconductor-lab.hafnium_chlorinator.recipe")
            .build();

    public HafniumChlorinatorBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.HAFNIUM_CHLORINATOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 原料がそろっているか（画面の文にも使う） */
    public static boolean ready(ItemStack oxide, ItemStack coke, ItemStack cl2, ItemStack bottle) {
        return oxide.is(Metallurgy.HAFNIUM_OXIDE) && coke.is(Metallurgy.COKE) && coke.getCount() >= COKE_PER_RUN
                && cl2.is(Gas.CHLORINE_GAS) && cl2.getCount() >= CL2_PER_RUN && bottle.is(Chemistry.GLASS_BOTTLE);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return ready(container.getItem(SLOT_OXIDE), container.getItem(SLOT_COKE), container.getItem(SLOT_CL2), container.getItem(SLOT_BOTTLE))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Semiconductor.HAFNIUM_TETRACHLORIDE))
                && canMerge(container.getItem(SLOT_CO), new ItemStack(Metallurgy.CARBON_MONOXIDE_GAS, CO_PER_RUN));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_OXIDE).shrink(1);
        container.getItem(SLOT_COKE).shrink(COKE_PER_RUN);
        container.getItem(SLOT_CL2).shrink(CL2_PER_RUN);
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_OUT, new ItemStack(Semiconductor.HAFNIUM_TETRACHLORIDE));
        merge(SLOT_CO, new ItemStack(Metallurgy.CARBON_MONOXIDE_GAS, CO_PER_RUN));
        container.setChanged();
    }
}
