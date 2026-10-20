package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 整流器 [4-11]（1902 クーパー・ヒューイットの水銀整流器）。真空にしたガラスの球の底に水銀を入れ（陰極）、黒鉛の陽極との間にアークを飛ばす。
 * 電流は水銀 → 陽極の向きにしか流れない（電子は熱い水銀の点からしか出ない）ので、交流が一方向の直流になる。
 * 後ろの面＝交流の入力、前の面＝直流の出力（同じ電圧の段）。電解槽など直流しか使えない機械を交流の網から動かすのに要る。
 * 効率 95%（水銀のアークで約 20V 落ちる ⚠️ゲームの値）。
 */
public class RectifierBlockEntity extends PowerConverterBlockEntity {
    public RectifierBlockEntity(BlockPos pos, BlockState state) { super(Power.RECTIFIER.type, pos, state); }
    @Override protected Current inputCurrent() { return Current.AC; }
    @Override protected Current outputCurrent() { return Current.DC; }
    @Override protected int efficiencyPercent() { return 95; }
}
