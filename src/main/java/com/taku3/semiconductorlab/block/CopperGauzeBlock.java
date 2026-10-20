package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 銅の網 [2-27]（触媒）。火のついたたき火（か火）の上に置いて熱し、メタノールを持って右クリックすると、
 * メタノールの蒸気が空気の酸素と網の上で反応してホルムアルデヒドになり、水に溶かしてホルマリンがとれる：2CH3OH + O2 → 2HCHO + 2H2O。
 * 網（触媒）は減らない。冷たい網では反応しない（実験）。
 */
public class CopperGauzeBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 2, 15);

    public CopperGauzeBlock(Properties properties) { super(properties); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    /** 網が熱いか（真下に火のついたたき火か火） */
    public static boolean hot(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return CampfireBlock.isLitCampfire(below) || below.is(Blocks.FIRE);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.METHANOL)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) react(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** メタノール1つを網に通す。熱ければホルマリンができて true（テストからも呼ぶ） */
    public static boolean react(Level level, BlockPos pos, Player player, ItemStack methanol) {
        if (!hot(level, pos)) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.copper_gauze.cold"));
            return false;
        }
        methanol.shrink(1);
        ItemStack out = new ItemStack(Chemistry.FORMALIN);
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, out));
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.8f);
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 6, 0.2, 0.05, 0.2, 0.01);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.copper_gauze.reacted"));
        return true;
    }
}
