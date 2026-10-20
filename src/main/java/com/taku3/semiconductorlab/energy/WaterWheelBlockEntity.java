package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/** 水力発電機（水車＋ダイナモ）。隣の「流れている水」1面につき 500 W（自励式ダイナモ [4-05] なら 1000 W）。止まった水（水源）では回らない */
public class WaterWheelBlockEntity extends GeneratorBlockEntity {
    public static final double WATTS_PER_FLOW = 500;
    public WaterWheelBlockEntity(BlockPos pos, BlockState state) { super(Power.WATER_WHEEL.type, pos, state, 10_000, WATTS_PER_FLOW * 4 * 2); }
    @Override public boolean supportsDynamoUpgrade() { return true; }

    @Override protected double produceWatts(Level level, BlockPos pos, BlockState state) {
        int flows = 0;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            FluidState f = level.getFluidState(pos.relative(d));
            if (!f.isEmpty() && f.is(Fluids.FLOWING_WATER) && f.getFlow(level, pos.relative(d)).lengthSqr() > 0) flows++;
        }
        // 流れの力には余裕がある。永久磁石のダイナモは磁場が弱く、その一部しか電気にできなかった → 自励式で2倍
        return flows * WATTS_PER_FLOW * dynamoFactor();
    }
}