package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * 蒸気タービン発電機 [4-06]（パーソンズ 1884）。蒸気で羽根車を高速で回し、直結した発電機（自励式）を回す。
 * ピストンが往復しないので速く・大きくでき、MW 級の発電ができた。
 * ゲーム：500kW。石炭1個＝18秒（500kW×18秒＝9MJ＝石炭の熱 30MJ の 30%。蒸気機関は 5%）。
 * 水：上の枠の水バケツ（1杯＝石炭2個分）、または横に水源ブロックがあればそこから取水し続ける（発電所が川や海のそばにある理由）。
 * 省略：多段タービン・復水器（蒸気を冷やして水に戻す装置）・減速歯車。羽根の削り出しは旋盤[8-01]が未実装のため省略（placeholder）。
 */
public class SteamTurbineBlockEntity extends SteamEngineBlockEntity {
    public static final double WATTS = 500_000;
    public static final int COAL_TICKS = 20 * 18;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_TURBINE, pos, state, 1_000_000, WATTS, COAL_TICKS);
    }

    /** タービンの発電機は最初から電磁石の界磁（自励式）。積み替えはない */
    @Override public boolean supportsDynamoUpgrade() { return false; }

    /** 横（東西南北）に水源ブロックがあれば給水できる */
    @Override protected boolean hasFeedWater(Level level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL)
            if (level.getFluidState(pos.relative(d)).is(Fluids.WATER)) return true;
        return false;
    }
}
