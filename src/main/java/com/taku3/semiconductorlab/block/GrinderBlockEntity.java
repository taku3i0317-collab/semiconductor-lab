package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.GaugeItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 研削盤 [8-31]（IMP2 O-013、1876 ブラウン＆シャープの万能研削盤・1883 フィッシャーの鋼球研削機）。
 * 焼入れして硬くした鋼の球は旋盤の刃では削れないので、回る砥石で少しずつ削って同じ大きさの真球にそろえる。
 * 入口：焼入れした鋼球（手で丸めた）、砥石、測る道具（減らない）。測る道具で仕上がりの段階が決まる：
 * マイクロメータ [8-04]（10µm）→ 研削した鋼球、ゲージブロック [8-05]（1µm）→ そろえた鋼球。1kW・20秒。
 */
public class GrinderBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 20;
    public static final int SLOT_IN = 0, SLOT_WHEEL = 1, SLOT_GAUGE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static boolean isGauge(ItemStack s) { return s.is(Precision.GAUGE_BLOCK_SET) || s.is(Precision.MICROMETER); }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 17, s -> s.is(Precision.STEEL_BALLS_ROUGH))
            .in(SLOT_WHEEL, 8, 39, s -> s.is(Precision.GRINDING_WHEEL), 1)
            .in(SLOT_GAUGE, 30, 39, GrinderBlockEntity::isGauge, 1)
            .out(SLOT_OUT, 78, 28)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("1.0 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.GRINDER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** できる鋼球（測る道具で決まる。ないと EMPTY） */
    public static ItemStack product(ItemStack gauge) {
        if (gauge.is(Precision.GAUGE_BLOCK_SET)) return new ItemStack(Precision.STEEL_BALLS_PRECISE, 4);
        if (gauge.getItem() instanceof GaugeItem g && g.resolutionUm <= 10) return new ItemStack(Precision.STEEL_BALLS_GROUND, 4);
        return ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_GAUGE));
        return container.getItem(SLOT_IN).is(Precision.STEEL_BALLS_ROUGH) && container.getItem(SLOT_WHEEL).is(Precision.GRINDING_WHEEL)
                && !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        merge(SLOT_OUT, product(container.getItem(SLOT_GAUGE)));
        container.getItem(SLOT_IN).shrink(1);
        container.setChanged();
    }
}
