package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 希ガスの精留塔 [6-08]（IMP2 O-018、1898 ラムゼーとトラヴァースが液体空気から Ne・Kr・Xe を発見）。
 * 空気分離 [6-07] の液体酸素のたまり（下）には沸点の高いクリプトン（−153℃）・キセノン（−108℃）が、窒素の上の抜けない気体にはネオン（−246℃）が集まる。
 * それをもう一度精留して取り出す。1回で酸素1袋・窒素1袋を使い、Kr {@value #KR_PER_RUN}・Xe {@value #XE_PER_RUN}・Ne {@value #NE_PER_RUN} mmol を貯め、1000 mmol で1袋。
 * ⚠️ 本物は空気の Kr 1.1ppm・Xe 0.09ppm・Ne 18ppm（ゲームは数千倍に濃くした）。2kW・5秒。
 */
public class RareGasColumnBlockEntity extends ConsumerBlockEntity implements GasSource {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 5;
    public static final int KR_PER_RUN = 31, XE_PER_RUN = 4, NE_PER_RUN = 63;
    public static final int SLOT_O2 = 0, SLOT_N2 = 1, SLOT_KR = 2, SLOT_XE = 3, SLOT_NE = 4, SLOT_COUNT = 5;

    private int kr, xe, ne;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_O2, 8, 17, s -> s.is(Gas.OXYGEN_GAS))
            .in(SLOT_N2, 8, 39, s -> s.is(Gas.NITROGEN_GAS))
            .out(SLOT_KR, 62, 17)
            .out(SLOT_XE, 62, 35)
            .out(SLOT_NE, 62, 53)
            .label("kr", 44, 21, 14, "Kr").label("xe", 44, 39, 14, "Xe").label("ne", 44, 57, 14, "Ne")
            .right("power", 17, 1, "gui.semiconductor-lab.machine.power").samples("2.0 kW")
            .right("stored", 27, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .right("hint", 38, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public RareGasColumnBlockEntity(BlockPos pos, BlockState state) {
        super(Gas.RARE_GAS_COLUMN.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override public int[] gasOutputSlots() { return new int[] {SLOT_KR, SLOT_XE, SLOT_NE}; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_O2).is(Gas.OXYGEN_GAS) && container.getItem(SLOT_N2).is(Gas.NITROGEN_GAS)
                && room(SLOT_KR, Gas.KRYPTON_GAS) && room(SLOT_XE, Gas.XENON_GAS) && room(SLOT_NE, Gas.NEON_GAS);
    }

    private boolean room(int slot, Item gas) { return canMerge(container.getItem(slot), new ItemStack(gas)); }

    @Override protected void finishWork() {
        container.getItem(SLOT_O2).shrink(1);
        container.getItem(SLOT_N2).shrink(1);
        kr += KR_PER_RUN; xe += XE_PER_RUN; ne += NE_PER_RUN;
        if (kr >= 1000) { kr -= 1000; merge(SLOT_KR, new ItemStack(Gas.KRYPTON_GAS)); }
        if (xe >= 1000) { xe -= 1000; merge(SLOT_XE, new ItemStack(Gas.XENON_GAS)); }
        if (ne >= 1000) { ne -= 1000; merge(SLOT_NE, new ItemStack(Gas.NEON_GAS)); }
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Kr", kr); out.putInt("Xe", xe); out.putInt("Ne", ne); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); kr = in.getIntOr("Kr", 0); xe = in.getIntOr("Xe", 0); ne = in.getIntOr("Ne", 0); }
}
