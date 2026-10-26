package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.WaterSupply;
import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * 蛇口の水（[12-06]・[12-07]、S-013 の決定）。飲める（薬ではない）。
 * にごった水＝おなかをこわす（空腹の効果）、砂ろ過した水＝ときどきこわす（菌が少し残る）、塩素で消毒した水＝何も起きない（安全）。
 */
public class DrinkingWaterItem extends Item {
    public static final int MURKY_TICKS = 20 * 30, FILTERED_TICKS = 20 * 10;
    public static final float FILTERED_CHANCE = 0.25f;

    private final WaterSupply.Quality quality;

    public DrinkingWaterItem(Properties p, WaterSupply.Quality quality) {
        super(p);
        this.quality = quality;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.DRINK; }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player p) {
            drink(p, quality, level.getRandom().nextFloat());
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack bottle = new ItemStack(Chemistry.GLASS_BOTTLE);
                if (stack.isEmpty()) return bottle;
                if (!p.getInventory().add(bottle)) p.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(p.level(), p.getX(), p.getY(), p.getZ(), bottle));
            }
        }
        return stack;
    }

    /** 飲んだ効果（テストからも呼ぶ）。roll は 0〜1 の乱数。おなかをこわしたら true */
    public static boolean drink(Player p, WaterSupply.Quality q, float roll) {
        boolean sick = q == WaterSupply.Quality.MURKY || (q == WaterSupply.Quality.FILTERED && roll < FILTERED_CHANCE);
        if (sick) p.addEffect(new MobEffectInstance(MobEffects.HUNGER, q == WaterSupply.Quality.MURKY ? MURKY_TICKS : FILTERED_TICKS, 0));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.drinking_water." + (sick ? "sick" : q.name().toLowerCase())));
        return sick;
    }
}
