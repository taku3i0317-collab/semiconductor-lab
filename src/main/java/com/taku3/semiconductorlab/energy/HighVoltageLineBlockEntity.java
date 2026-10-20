package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 高圧の電線（[4-10] の続き、1886 スタンレーのグレート・バリントンの交流の配電 ⚠️）。電圧の段 1＝低圧の約10倍（約2000V ⚠️ゲームの値）。
 * 同じ電力を送るなら電流は 1/10、電線の損失 I²R は 1/100（1本 0.01%）。同じ太さで送れる電力も10倍（網あたり 10MW）。
 * 銅ケーブル（低圧）とはつながらない。機械・発電機は低圧なので、高圧の網には変圧器だけがつながる。
 */
public class HighVoltageLineBlockEntity extends CableBlockEntity {
    public HighVoltageLineBlockEntity(BlockPos pos, BlockState state) { super(Power.HIGH_VOLTAGE_LINE.type, pos, state); }
    @Override public int voltageClass() { return 1; }
}
