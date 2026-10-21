package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 赤い LED の明かり [B-07]（1962 の GaAsP の赤い LED。IMP4-01、S-007 案A）：p-n 接合に電気を流すと、電子と穴が出会って光になる（電球のように熱くしない）。
 * 電気 {@link #WATTS} W で点灯（LIT＝明るさ7）。材料は使わない。水銀灯 [7-10] と同じ「光るだけ」の機械。
 * ⚠️ 1962 の赤い LED はとても暗かった（表示のランプ用）。明るさ・電力はゲームの値（led_lamp.placeholder）。
 */
public class LedLampBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2;
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT).text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.4 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running").build();

    public LedLampBlockEntity(BlockPos pos, BlockState state) { this(Branches.LED_LAMP.type, pos, state); }

    /** 白色 LED の明かり [B-20] も同じ（ブロックの種類だけちがう、明るさは登録の lit で決まる） */
    protected LedLampBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state, 0, 2_000, 200); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    /** 隣に人を感じるセンサー [9-46] があれば、センサーが人を感じている間だけ点く（IMP4-01、O-030） */
    @Override protected boolean canWork() { return !PirSensorBlock.holdsOff(level, worldPosition); }
    @Override protected void finishWork() {}
}
