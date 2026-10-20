package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Testing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 試験用の無限電源（S-001 試験場）。クリエイティブ専用・レシピなし。燃料なしで いつも WATTS を出し、隣の機械へ押し出す。
 * 本物の発電ではない（名前にも「試験用」）。ゲームの確認を早くするためだけに使う。本物の発電の確認は試験場の蒸気機関の区画で。
 */
public class TestPowerSourceBlockEntity extends GeneratorBlockEntity {
    /** 出す電力（W）。いちばん大きい機械（アチソン炉 500 kW）も動かせる */
    public static final double WATTS = 1_000_000;

    public TestPowerSourceBlockEntity(BlockPos pos, BlockState state) {
        super(Testing.TEST_POWER_SOURCE.type, pos, state, 1_000_000, WATTS);
    }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) { return WATTS; }
}
