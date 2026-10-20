package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 旋盤 [8-01]（IMP2-01）。モーズリーのねじ切り旋盤（1797〜1800頃）：材料を回し、刃物台を送りねじで動かして削る。
 * 手で刃物を持たないので、同じ寸法の部品を何個でも作れる（互換部品＝精密機械の母）。動力はモーター（この MOD では電気 750W）。
 * 削れる物は {@link #product} に1行ずつ（今は鋼 → 回転ポンプのローター [5-04]）。
 */
public class LatheBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 750;
    public static final int TICKS = 20 * 30;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 35, s -> !product(s).isEmpty())
            .out(SLOT_OUT, 78, 35)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.8 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public LatheBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.LATHE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 削るとできる物（削れない物は EMPTY） */
    public static boolean isInput(ItemStack s) { return !product(s).isEmpty(); }

    public static ItemStack product(ItemStack in) {
        if (in.is(Metallurgy.STEEL_INGOT)) return new ItemStack(Precision.PUMP_ROTOR);
        return ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }
    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_IN));
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }
    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_IN));
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
