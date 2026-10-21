package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * アーク灯 [4-16]（1808 デービーがアークを見せた。1870年代後半から街灯に。IMP4-01、S-009）：2本の炭素棒のあいだにアーク（電気の火花）を飛ばして光らせる。
 * 炭素は熱で空気と反応して減っていく（ハータ・エアトン 1899：シューという音は炭素と酸素の反応）。電気 {@link #WATTS} W で明るさ15、炭素棒（黒鉛）1本で {@link #TICKS} tick。
 * ⚠️ 電力・棒の減り方はゲームの値（arc_lamp.placeholder）。
 */
public class ArcLampBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 450;
    public static final int TICKS = 20 * 120;
    public static final int SLOT_ROD = 0, SLOT_COUNT = 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_ROD, 8, 35, ArcLampBlockEntity::isRod)
            .right("power", 17, 1, "gui.semiconductor-lab.arc_lamp.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.arc_lamp.need_rod", "gui.semiconductor-lab.arc_lamp.need_power", "gui.semiconductor-lab.arc_lamp.running")
            .bottom("recipe", "gui.semiconductor-lab.arc_lamp.recipe")
            .build();

    public ArcLampBlockEntity(BlockPos pos, BlockState state) {
        super(Power.ARC_LAMP.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isRod(ItemStack s) { return s.is(Metallurgy.GRAPHITE) || s.is(Heat.SYNTHETIC_GRAPHITE); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }
    @Override protected boolean canWork() { return isRod(container.getItem(SLOT_ROD)); }
    @Override protected void finishWork() {
        container.getItem(SLOT_ROD).shrink(1);   // 炭素棒が燃え尽きた
        container.setChanged();
    }
}
