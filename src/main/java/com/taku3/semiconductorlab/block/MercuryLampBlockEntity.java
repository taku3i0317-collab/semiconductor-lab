package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 水銀灯 [7-10]（IMP2-01）。石英か ガラスの管に水銀を入れて空気を抜き、両端の電極のあいだに直流を流すと、
 * 水銀の蒸気の中でアーク放電が起き、水銀原子が決まった色の光（g線 436nm・h線 405nm・i線 365nm・緑 546nm・黄 577/579nm）を出す。
 * 1901 クーパー・ヒューイットの水銀灯：直流 110V・3.5A＝385W。電気が足りていれば点灯（LIT＝明るさ15）、材料は使わない。
 * スロットはない（画面は SimpleMachineScreen：電力・状態）。分光器で点灯中のこのブロックを見ると水銀の線が出る（SpectroscopeItem）。
 */
public class MercuryLampBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 385;
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT).text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.4 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running").build();

    public MercuryLampBlockEntity(BlockPos pos, BlockState state) { super(Optics.MERCURY_LAMP.type, pos, state, 0, 20_000, 2_000); }

    @Override protected double workWatts() { return WATTS; }
    /** 光り続けるだけなので「仕事の区切り」は1秒ごと（進み具合のバーが回る） */
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return true; }
    @Override protected void finishWork() {}

    /** 点灯しているか（電気が足りて LIT） */
    public static boolean isLit(BlockState s) {
        return s.is(Optics.MERCURY_LAMP.block) && s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
    }
}
