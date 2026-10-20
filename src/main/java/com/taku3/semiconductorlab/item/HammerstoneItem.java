package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * たたき石 [0-01]（河原の丸く硬い石。打製石器を打ち欠くハンマー）。作業台で使っても戻る（{@link ReusableToolItem}）。
 * 打ち欠きの実験：
 * ・もう片方の手に火打ち石を持って右クリック → 火打ち石1つが剥片2つになる
 * ・黒曜石ブロックを右クリック → 黒曜石の剥片（ブロックは残る）
 * ・ふつうの石（丸石・花こう岩など）を右クリック → 粒が粗くて刃にならない、と分かる（何も減らない）
 * ガラス質・微晶質の石（フリント・黒曜石）は割れ目が貝殻状に走り（貝殻状断口）、鋭い縁が残る。粒の粗い石は粒の境目で崩れる。
 */
public class HammerstoneItem extends ReusableToolItem {
    /** 黒曜石を続けて打てる間隔（tick） */
    public static final int COOLDOWN = 10;
    /** 火打ち石1つから取れる剥片の数 */
    public static final int FLAKES_PER_FLINT = 2;

    public enum Knap { GLASSY, GRAINY, NONE }

    public HammerstoneItem(Properties properties) { super(properties); }

    /** ブロックを打ったらどうなるか（テストからも呼ぶ） */
    public static Knap classify(BlockState s) {
        if (s.is(Blocks.OBSIDIAN) || s.is(Blocks.CRYING_OBSIDIAN)) return Knap.GLASSY;
        if (s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.BASE_STONE_NETHER)
                || s.is(Blocks.COBBLESTONE) || s.is(Blocks.MOSSY_COBBLESTONE) || s.is(Blocks.COBBLED_DEEPSLATE)
                || s.is(Blocks.CALCITE) || s.is(Blocks.SANDSTONE) || s.is(Blocks.RED_SANDSTONE)) return Knap.GRAINY;
        return Knap.NONE;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        Knap k = classify(level.getBlockState(ctx.getClickedPos()));
        if (k == Knap.NONE || player == null) return InteractionResult.PASS;
        if (k == Knap.GLASSY) {
            if (player.getCooldowns().isOnCooldown(ctx.getItemInHand())) return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                give(player, new ItemStack(Survival.OBSIDIAN_FLAKE));
                player.getCooldowns().addCooldown(ctx.getItemInHand(), COOLDOWN);
                player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hammerstone.glassy"));
            }
            level.playSound(null, ctx.getClickedPos(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.4f);
        } else {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hammerstone.grainy"));
            level.playSound(null, ctx.getClickedPos(), SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.8f, 0.8f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!knapFlint(player, hand)) return InteractionResult.PASS;
        level.playSound(null, player.blockPosition(), SoundEvents.STONE_HIT, SoundSource.PLAYERS, 0.8f, 1.6f);
        return InteractionResult.SUCCESS;
    }

    /** もう片方の手の火打ち石を1つ打ち欠いて剥片にする。火打ち石が無ければ false（テストからも呼ぶ） */
    public static boolean knapFlint(Player player, InteractionHand hand) {
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack flint = player.getItemInHand(other);
        if (!flint.is(Items.FLINT)) return false;
        if (player.level().isClientSide()) return true;
        flint.shrink(1);
        give(player, new ItemStack(Survival.FLINT_FLAKE, FLAKES_PER_FLINT));
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hammerstone.flint"));
        return true;
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty())
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
    }
}
