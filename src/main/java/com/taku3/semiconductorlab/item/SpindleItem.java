package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * 紡錘 [0-07]（つむ。回して繊維に撚りをかけ、糸にする道具）。もう片方の手に繊維（亜麻の繊維・ほぐした羊毛）を持って、右クリックを押している間まわる。
 * 紡錘車（はずみ車）が付いていると、回転の勢い（慣性モーメント）が大きいので長く回り続け、1回で多くの糸ができる。
 * 棒だけだとすぐ止まり、1回で少ししかできない。材料の比（繊維2 → 糸1）はどちらも同じ。
 */
public class SpindleItem extends Item {
    /** 糸1本ぶん撚るのにかかる回転の時間（tick） */
    public static final int TICKS_PER_THREAD = 20;
    /** 糸1本に使う繊維の数 */
    public static final int FIBRE_PER_THREAD = 2;

    /** 1回まわしたときに回り続ける時間（tick）。棒だけ＝短い、紡錘車つき＝長い */
    private final int spinTicks;

    public SpindleItem(Properties properties, int spinTicks) {
        super(properties);
        this.spinTicks = spinTicks;
    }

    public int spinTicks() { return spinTicks; }

    /** 紡げる繊維 → できる糸（紡げない物は null） */
    public static Item threadFor(ItemStack fibre) {
        if (fibre.is(Survival.FLAX_FIBER)) return Survival.LINEN_THREAD;
        if (fibre.is(Survival.CARDED_WOOL)) return Survival.WOOLEN_YARN;
        return null;
    }

    private static InteractionHand other(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack fibre = player.getItemInHand(other(hand));
        if (threadFor(fibre) == null || fibre.getCount() < FIBRE_PER_THREAD) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.spindle.no_fibre"));
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_HIT, SoundSource.PLAYERS, 0.6f, 1.2f);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return spinTicks; }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.BRUSH; }

    /** 回りきった */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player p) spin(p, p.getUsedItemHand(), spinTicks);
        return stack;
    }

    /** 途中で手を離した：回った時間のぶんだけ */
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!level.isClientSide() && entity instanceof Player p) spin(p, p.getUsedItemHand(), spinTicks - remaining);
        return true;
    }

    /** ticks のあいだ回った紡錘で、もう片方の手の繊維を糸にする。できた糸の数を返す（テストからも呼ぶ） */
    public static int spin(Player player, InteractionHand spindleHand, int ticks) {
        ItemStack fibre = player.getItemInHand(other(spindleHand));
        Item thread = threadFor(fibre);
        if (thread == null) return 0;
        int n = Math.min(ticks / TICKS_PER_THREAD, fibre.getCount() / FIBRE_PER_THREAD);
        if (n > 0) {
            fibre.shrink(n * FIBRE_PER_THREAD);
            ItemStack out = new ItemStack(thread, n);
            if (!player.getInventory().add(out) && !out.isEmpty())
                player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), out));
        }
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.spindle.spun", String.format("%.1f", ticks / 20.0), n));
        return n;
    }
}
