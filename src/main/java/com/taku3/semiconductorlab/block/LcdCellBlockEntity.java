package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.item.PolarizerItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 液晶セル（TN 方式）[B-18]（1970〜71 ロシュ社の研究者たち。IMP3、O-023）。こすった ITO ガラス2枚のあいだに液晶（MBBA）、外側に偏光フィルム2枚を直角に貼る。
 * 電気なし：液晶の分子がガラスのこすった向きにそって 90 度ねじれて並び、光の向きを 90 度回す → 直角の2枚目を通る（明るい）。
 * 電気あり（LIT）：分子が電場の向きに立ってねじれがほどけ、光の向きが回らない → 2枚目で止まる（暗い）。だから数字の形の電極だけを暗くできる。
 * ⚠️ 電力はゲームの値（本物の液晶は数マイクロワットで、電卓や腕時計が電池で何年も動く）。交流で動かす（直流だと液晶が電気分解でこわれる）のは省略。
 */
public class LcdCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1;
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT).text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.4 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running").build();

    public LcdCellBlockEntity(BlockPos pos, BlockState state) { super(Branches.LCD_CELL.type, pos, state, 0, 200, 20); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return true; }
    @Override protected void finishWork() {}

    /** ねじれた液晶が光の向きを回す角度（度）：電気なし 90、電気あり 0 */
    public static int twistDeg(boolean voltageOn) { return voltageOn ? 0 : 90; }

    /** 直角の偏光フィルム2枚のあいだにセルを入れたときの明るさ（％、ふつうの光を 100）。電気なしで明るく、電気ありで暗い（ノーマリーホワイト） */
    public static double brightness(boolean voltageOn) {
        int crossed = 90;
        return PolarizerItem.brightness(2, Math.floorMod(crossed - twistDeg(voltageOn), 180));
    }
}
