package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 床の振動 [8-07] 除振と恒温（IMP2）。精密な測定・露光をする場所の揺れの大きさ（nm）を、まわりの「揺らす物」から見積もる。
 * <ul>
 * <li>動いている生き物・プレイヤー（8 ブロック以内）：{@value #WALK_NM} nm ÷ 距離</li>
 * <li>動いているトロッコ：{@value #CART_NM} nm ÷ 距離</li>
 * <li>動いているピストン（6 ブロック以内）：{@value #PISTON_NM} nm ÷ 距離</li>
 * </ul>
 * 測る物の真下が除振台なら ÷ {@value #TABLE_FACTOR}（ゴムで浮かせた重い定盤）。⚠️ 数値はゲームの値（本物の足音は µm 級、除振台で数十分の1）。
 * 露光装置など、ほかの機械も {@link #nm(Level, BlockPos, Entity)} を使える。
 */
public final class Vibration {
    private Vibration() {}

    public static final double WALK_NM = 400, CART_NM = 1500, PISTON_NM = 4000;
    public static final double TABLE_FACTOR = 20;
    public static final int ENTITY_R = 8, PISTON_R = 6;

    /** pos で測るときの揺れ（nm）。except＝測っている本人（じっと立っている扱い） */
    public static double nm(Level level, BlockPos pos, Entity except) {
        Vec3 c = Vec3.atCenterOf(pos);
        double sum = 0;
        for (Entity e : level.getEntities(except, new AABB(pos).inflate(ENTITY_R), e -> e.getDeltaMovement().horizontalDistance() > 0.01)) {
            double d = Math.max(1, Math.sqrt(e.distanceToSqr(c)));
            sum += (e instanceof AbstractMinecart ? CART_NM : WALK_NM) / d;
        }
        for (BlockPos q : BlockPos.betweenClosed(pos.offset(-PISTON_R, -PISTON_R, -PISTON_R), pos.offset(PISTON_R, PISTON_R, PISTON_R))) {
            if (level.getBlockState(q).is(Blocks.MOVING_PISTON)) sum += PISTON_NM / Math.max(1, Math.sqrt(q.distSqr(pos)));
        }
        if (onTable(level, pos)) sum /= TABLE_FACTOR;
        return sum;
    }

    /** 真下が除振台か */
    public static boolean onTable(Level level, BlockPos pos) { return level.getBlockState(pos.below()).is(Precision.ISOLATION_TABLE); }

    /** 揺れ（nm）による干渉縞のゆれ（本）：光の道は往復なので 2×揺れ÷波長 */
    public static double fringeJitter(double nm, double wavelengthNm) { return 2 * nm / wavelengthNm; }
}
