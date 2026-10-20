package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.machine.Vessel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * アルミナの触媒管 [2-36]（鉄の管にアルミナ Al2O3 の粒を詰めた物）。火のついたたき火（か火）の上に置いて熱し、エタノールを持って右クリックすると、
 * エタノールの蒸気が熱いアルミナの上で水を失ってエチレンになる：C2H5OH → C2H4 + H2O。エチレンは袋に集める（水は捨てる）。
 * 硫化水素の袋で右クリックすると、まわりの空気の酸素と熱いアルミナの上で硫黄になる（クラウス法 [2-11]：2H2S + O2 → 2S + 2H2O）。
 * アルミナ（触媒）は減らない。冷たい管では反応しない（実験）。熱いかどうかは {@link CopperGauzeBlock#hot} と同じ決まり。
 */
public class AluminaCatalystTubeBlock extends Block {
    public AluminaCatalystTubeBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.ETHANOL) && !stack.is(Chemistry.HYDROGEN_SULFIDE_GAS)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) react(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** エタノール1つを管に通す。熱ければエチレン1袋と空のガラス瓶が手元に来て true（テストからも呼ぶ） */
    public static boolean react(Level level, BlockPos pos, Player player, ItemStack ethanol) {
        if (!CopperGauzeBlock.hot(level, pos)) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.alumina_catalyst_tube.cold"));
            return false;
        }
        boolean claus = ethanol.is(Chemistry.HYDROGEN_SULFIDE_GAS);
        Vessel.returnEmpty(player, pos, ethanol);
        ethanol.shrink(1);
        // エタノール → エチレン（脱水）。硫化水素 → 硫黄（クラウス法：2H2S + O2 → 2S + 2H2O、O2 はまわりの空気）
        ItemStack out = new ItemStack(claus ? Chemistry.SULFUR_POWDER : Chemistry.ETHYLENE_GAS);
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, out));
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.5f);
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.1, 0.05, 0.1, 0.01);
        player.sendOverlayMessage(Component.translatable(claus ? "message.semiconductor-lab.alumina_catalyst_tube.claus" : "message.semiconductor-lab.alumina_catalyst_tube.reacted"));
        return true;
    }
}
