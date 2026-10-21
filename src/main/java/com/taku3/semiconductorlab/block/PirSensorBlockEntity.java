package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Semiconductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 人を感じるセンサー [9-46] の中身：{@link #CHECK} tick ごとに、{@link #RANGE} マス以内の生き物（体温がある＝赤外線を出す）の位置を前と比べ、
 * 動いた（新しく入ってきた）物がいれば LIT にして {@link #HOLD} tick（10秒 ⚠️）点けたままにする。じっとしている物は感じない（本物も同じ）。
 * 本物：焦電体の結晶は温度が変わったときだけ電気が出る。分割した鏡（フレネルレンズ）で視野を細かく分け、体が区切りを横切ると信号が出る。
 */
public class PirSensorBlockEntity extends BlockEntity {
    public static final int RANGE = 5, CHECK = 10, HOLD = 200;
    private static final double MOVE = 0.1;
    private final Map<UUID, Vec3> last = new HashMap<>();
    private int hold, timer;

    public PirSensorBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.PIR_SENSOR.type, pos, state); }

    public static void tick(Level level, BlockPos pos, BlockState state, PirSensorBlockEntity be) {
        if (level.isClientSide() || --be.timer > 0) return;
        be.timer = CHECK;
        be.sense(level);
    }

    /** 1回分見る（テストからも呼ぶ）。動いた物がいたら true */
    public boolean sense(Level level) {
        boolean moved = false;
        Map<UUID, Vec3> now = new HashMap<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(RANGE), e -> !(e instanceof ArmorStand) && e.isAlive())) {
            Vec3 p = e.position(), before = last.get(e.getUUID());
            if (before == null || before.distanceTo(p) > MOVE) moved = true;
            now.put(e.getUUID(), p);
        }
        last.clear();
        last.putAll(now);
        hold = moved ? HOLD : Math.max(0, hold - CHECK);
        boolean lit = hold > 0;
        BlockState s = level.getBlockState(worldPosition);
        if (s.getBlock() instanceof PirSensorBlock && s.getValue(PirSensorBlock.LIT) != lit) level.setBlock(worldPosition, s.setValue(PirSensorBlock.LIT, lit), 3);
        return moved;
    }
}
