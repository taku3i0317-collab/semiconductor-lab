package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 熱分解炉 [2-38]（白金の管を電気で約700℃に熱する）：クロロジフルオロメタン（R-22）を通すと、2つが塩化水素を2つ失ってつながり、
 * テトラフルオロエチレン（TFE）になる：2CHClF2 → C2F4 + 2HCl。
 * <pre>
 *   R-22 {@link #FEED_PER_RUN} 袋 → TFE 1袋 ＋ 塩化水素 2袋   （10秒、8kW）
 * </pre>
 * ⚠️ 温度は数字で扱わない（ふつうの画面）。副産物（ヘキサフルオロプロピレンなど）は出さない（pyrolysis_furnace.placeholder）。
 */
public class PyrolysisFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 8_000;
    public static final int TICKS = 20 * 10;
    public static final int FEED_PER_RUN = 2;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_HCL = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 26, s -> s.is(Chemistry.CHLORODIFLUOROMETHANE_GAS))
            .out(SLOT_OUT, 78, 17)
            .out(SLOT_HCL, 78, 35)
            .standardTexts()
            .build();

    public PyrolysisFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.PYROLYSIS_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack in = container.getItem(SLOT_IN);
        return in.is(Chemistry.CHLORODIFLUOROMETHANE_GAS) && in.getCount() >= FEED_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.TETRAFLUOROETHYLENE_GAS))
                && canMerge(container.getItem(SLOT_HCL), new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(FEED_PER_RUN);
        merge(SLOT_OUT, new ItemStack(Chemistry.TETRAFLUOROETHYLENE_GAS));
        merge(SLOT_HCL, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2));
        container.setChanged();
    }
}
