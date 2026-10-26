package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 塩素の注入器 [12-07]：水道の管の途中に置き、塩素のボンベ（黄）を持って右クリックすると、ごく少しずつ塩素を水に溶かす（{@link #DOSES} 回分）。
 * 入れかえのとき、ときどき塩素がもれる（事故：吐き気。DESIGN「危ない技術の扱い」＝量や条件は書かない）。
 */
public class ChlorinatorBlock extends Block {
    public static final int MAX_DOSES = 16;
    public static final IntegerProperty DOSES = IntegerProperty.create("doses", 0, MAX_DOSES);
    public static final float LEAK_CHANCE = 0.1f;

    public ChlorinatorBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(DOSES, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(DOSES); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Gas.CHLORINE_CYLINDER)) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.chlorinator.doses", state.getValue(DOSES), MAX_DOSES));
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) load(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** 塩素のボンベを1本入れる（テストからも呼ぶ） */
    public static boolean load(Level level, BlockPos pos, Player player, ItemStack cylinder) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(DOSES) >= MAX_DOSES) return false;
        cylinder.shrink(1);
        ItemStack empty = new ItemStack(Gas.EMPTY_GAS_CYLINDER);
        if (!player.getInventory().add(empty)) player.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), empty));
        level.setBlock(pos, s.setValue(DOSES, MAX_DOSES), 3);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.6f);
        if (level.getRandom().nextFloat() < LEAK_CHANCE) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 8, 0, false, true, true));
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.chlorinator.leak"));
        } else {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.chlorinator.doses", MAX_DOSES, MAX_DOSES));
        }
        return true;
    }

    /** 瓶1本ぶん消毒した：1回分へらす */
    public static void use(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.hasProperty(DOSES) && s.getValue(DOSES) > 0) level.setBlock(pos, s.setValue(DOSES, s.getValue(DOSES) - 1), 3);
    }
}
