package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ハーバー・ボッシュの反応塔 [6-09]（1909 ハーバー、1913 ボッシュの工場）：窒素と水素を約200気圧・約450℃で、鉄の触媒の上に通す：N2 + 3H2 → 2NH3。
 * 触媒（磁鉄鉱をカリとアルミナで強くした物、ミタッシュ）がないと、ほとんど反応しない（実験）。触媒は減らない。
 * <pre>
 *   窒素の袋 1 ＋ 水素の袋 3 → アンモニアの袋 2   （10秒、20kW＝圧縮の電力）
 * </pre>
 * ⚠️ 1回で通る分は本物では約15%で、残りは何度も回して使う（ここでは全部反応したことにした、haber_bosch_reactor.placeholder）。
 */
public class HaberBoschReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 20_000;
    public static final int TICKS = 20 * 10;
    public static final int SLOT_N2 = 0, SLOT_H2 = 1, SLOT_CATALYST = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_N2, 8, 17, s -> s.is(Gas.NITROGEN_GAS))
            .in(SLOT_H2, 8, 35, s -> s.is(Gas.HYDROGEN_GAS))
            .in(SLOT_CATALYST, 8, 53, s -> s.is(Gas.IRON_CATALYST))
            .out(SLOT_OUT, 78, 35)
            .standardTexts()
            .build();

    public HaberBoschReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Gas.HABER_BOSCH_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_N2).is(Gas.NITROGEN_GAS) && container.getItem(SLOT_H2).is(Gas.HYDROGEN_GAS) && container.getItem(SLOT_H2).getCount() >= 3
                && container.getItem(SLOT_CATALYST).is(Gas.IRON_CATALYST)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Gas.AMMONIA_GAS, 2));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_N2).shrink(1);
        container.getItem(SLOT_H2).shrink(3);
        merge(SLOT_OUT, new ItemStack(Gas.AMMONIA_GAS, 2));
        container.setChanged();
    }
}
