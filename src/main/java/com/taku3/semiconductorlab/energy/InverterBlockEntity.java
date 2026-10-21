package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Semiconductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * インバータ [9-41]（IMP4-01、O-015）：直流を半導体のスイッチで1秒に何百〜何千回もオン・オフし、向きを入れかえて交流を作る（整流器 [4-11] の逆）。
 * 最初はサイリスタ [9-49]（1957 GE の SCR）で作られ、のちにパワー MOSFET・IGBT になった。交流の周波数を変えると、交流モーターの回る速さが変わる（電車・EV）。
 * 後ろの面＝直流の入力、前の面＝交流の出力（同じ電圧の段）。太陽電池・電池の直流を、変圧器や交流の網につなげる。
 * 効率 95%（⚠️ゲームの値。サイリスタの時代は約95%、今の IGBT は約98%）。周波数・モーターの速さは表さない（inverter.placeholder）。
 */
public class InverterBlockEntity extends PowerConverterBlockEntity {
    public InverterBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.INVERTER.type, pos, state); }
    @Override protected Current inputCurrent() { return Current.DC; }
    @Override protected Current outputCurrent() { return Current.AC; }
    @Override protected int efficiencyPercent() { return 95; }
}
