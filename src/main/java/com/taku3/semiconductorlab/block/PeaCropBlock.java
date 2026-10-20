package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * エンドウ [0-05]（マメ科。根の根粒にすむ根粒菌が、空気の窒素 N2 をアンモニア NH3 に変える＝窒素固定）。
 * 熟すと、下の耕地が「窒素の多い耕地」（{@link NitrogenFarmlandBlock}）になる。種＝このブロックのアイテム。
 */
public class PeaCropBlock extends CropBlock {
    public PeaCropBlock(Properties properties) { super(properties); }

    @Override
    protected ItemLike getBaseSeedId() { return this.asItem(); }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        afterGrow(level, pos);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        super.performBonemeal(level, random, pos, state, source);
        afterGrow(level, pos);
    }

    /** 熟していたら、下の耕地に窒素を残す */
    public void afterGrow(ServerLevel level, BlockPos pos) {
        BlockState now = level.getBlockState(pos);
        if (now.is(this) && isMaxAge(now)) NitrogenFarmlandBlock.enrich(level, pos.below(), Survival.NITROGEN_FARMLAND);
    }
}
