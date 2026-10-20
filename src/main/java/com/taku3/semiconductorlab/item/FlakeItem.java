package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * 打製石器の剥片 [0-01]（小さな刃物）。[2-31] で使い道を足した：トウヒ（針葉樹）の丸太の皮に傷をつけると、松やにがにじみ出る。
 * 1回ごとに剥片が1傷み、少し待たないと次は採れない（COOLDOWN）。
 */
public class FlakeItem extends Item {
    public static final int COOLDOWN = 40;

    public FlakeItem(Properties properties) { super(properties); }

    /** 針葉樹（トウヒ）の丸太か。皮のある物だけ（皮をはいだ丸太からは採れない） */
    public static boolean isConiferLog(BlockState s) { return s.is(Blocks.SPRUCE_LOG) || s.is(Blocks.SPRUCE_WOOD); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null || !isConiferLog(level.getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        ItemStack flake = ctx.getItemInHand();
        if (player.getCooldowns().isOnCooldown(flake)) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            Block.popResourceFromFace(level, ctx.getClickedPos(), ctx.getClickedFace(), new ItemStack(Chemistry.PINE_RESIN));
            player.getCooldowns().addCooldown(flake, COOLDOWN);
            flake.hurtAndBreak(1, player, ctx.getHand());
        }
        level.playSound(null, ctx.getClickedPos(), SoundEvents.AXE_STRIP.value(), SoundSource.PLAYERS, 0.6f, 1.5f);
        return InteractionResult.SUCCESS;
    }
}
