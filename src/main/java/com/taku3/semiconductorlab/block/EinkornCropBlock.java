package com.taku3.semiconductorlab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ヒトツブコムギ [0-04]（約1万1500年前、肥沃な三日月地帯で栽培化された最初の麦の1つ）。
 * 野生種（wild=true）：熟すと穂の軸がもろく、穂がばらばらに落ちる（脱粒）。熟したまま放っておくと、
 *   ランダムtick で穂がこぼれて芽からやり直しになる（こぼれた種が自然に生える＝収穫はなし）。
 *   熟したうちに刈ると、まれに「穂が落ちない」変異の種（栽培種）が混ざる（ルートテーブル）。
 * 栽培種（wild=false）：穂が落ちないので、熟したまま待てる。刈ると麦（バニラの小麦）と種。
 * 種のアイテムはこのブロックのアイテム（Reg.block が作る）。
 */
public class EinkornCropBlock extends CropBlock {
    /** 熟した野生種が、1回のランダムtick で穂を落とす確率 */
    public static final float SHATTER_CHANCE = 0.25f;

    private final boolean wild;

    public EinkornCropBlock(Properties properties, boolean wild) {
        super(properties);
        this.wild = wild;
    }

    public boolean isWild() { return wild; }

    @Override
    protected ItemLike getBaseSeedId() { return this.asItem(); }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return wild || super.isRandomlyTicking(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (wild && isMaxAge(state)) {
            if (random.nextFloat() < SHATTER_CHANCE) shatter(level, pos);
            return;
        }
        super.randomTick(state, level, pos, random);
    }

    /** 野生種の穂が落ちる：収穫はなし、こぼれた種から芽（age 0）に戻る */
    public void shatter(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, getStateForAge(0), 2);
        level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.6f, 1.3f);
    }
}
