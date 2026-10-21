package com.taku3.semiconductorlab.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 犂（すき）[11-02]（IMP3、S-011）。鉄の刃（犂先）と木のわく。土・草の地面に使うと、まわり 3×3 を一度に耕地にする（くわは1マス）。
 * 本物は牛や馬に引かせて、土を切って裏返す。使うたびに少しすり減る（耐久）。
 */
public class PlowItem extends Item {
    public static final int RADIUS = 1;

    public PlowItem(Properties p) { super(p); }

    /** 耕せる地面か（土・草・土の道。上が空気） */
    public static boolean tillable(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return (s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT_PATH)) && level.getBlockState(pos.above()).isAir();
    }

    /** center を中心に (2R+1)² を耕地にする。耕した数を返す（テストからも呼ぶ） */
    public static int till(Level level, BlockPos center) {
        int n = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) for (int dz = -RADIUS; dz <= RADIUS; dz++) {
            BlockPos p = center.offset(dx, 0, dz);
            if (!tillable(level, p)) continue;
            level.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 3);
            n++;
        }
        return n;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!tillable(level, ctx.getClickedPos())) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        int n = till(level, ctx.getClickedPos());
        level.playSound(null, ctx.getClickedPos(), SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0f, 0.8f);
        if (n > 0 && ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), ctx.getHand());
        return InteractionResult.SUCCESS;
    }
}
