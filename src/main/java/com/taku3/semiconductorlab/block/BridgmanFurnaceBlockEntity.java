package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ブリッジマン炉 [7-15]（IMP2-02）：ブリッジマン（1925）・ストックバーガー（1936）の結晶の育て方。
 * 黒鉛のるつぼに蛍石を入れて誘導加熱で溶かし（CaF2 の融点 1418℃）、るつぼを熱い所から冷たい所へゆっくり下げると、
 * 下の先から1つの結晶が育つ。酸素が入ると濁るので、アルゴンの中で行う。
 * 蛍石 {@link #FLUORITE_PER} 個＋アルゴン1袋 → CaF2 単結晶1個（{@link #WATTS}・{@link #TICKS}）。
 */
public class BridgmanFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 90;
    public static final int FLUORITE_PER = 4;
    public static final int SLOT_IN = 0, SLOT_AR = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 26, s -> s.is(Metallurgy.FLUORITE))
            .in(SLOT_AR, 8, 50, s -> s.is(Gas.ARGON_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.machine.power").samples("3.0 kW")
            .right("stored", 27, 1, "gui.semiconductor-lab.machine.stored").samples("50 kJ")
            .right("hint", 38, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public BridgmanFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Optics.BRIDGMAN_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_IN).is(Metallurgy.FLUORITE) && container.getItem(SLOT_IN).getCount() >= FLUORITE_PER
                && container.getItem(SLOT_AR).is(Gas.ARGON_GAS)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Optics.CAF2_CRYSTAL));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(FLUORITE_PER);
        container.getItem(SLOT_AR).shrink(1);
        merge(SLOT_OUT, new ItemStack(Optics.CAF2_CRYSTAL));
        container.setChanged();
    }
}
