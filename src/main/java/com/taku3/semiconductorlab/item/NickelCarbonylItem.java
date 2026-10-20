package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ニッケルカルボニルの袋 [1-37]（モンド法の途中の気体、猛毒）。DESIGN「危ない技術の扱い」6 のとおり「事故」として：
 * 持ち歩いていると、1秒ごとに {@link #LEAK_CHANCE} の確率で少しもれ、吐き気と弱体化（数秒）とメッセージ。袋は減らない。
 */
public class NickelCarbonylItem extends Item {
    public static final float LEAK_CHANCE = 1f / 60f;
    public static final int EFFECT_TICKS = 20 * 8;

    public NickelCarbonylItem(Properties properties) { super(properties); }

    public static void leak(Player p) {
        p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, EFFECT_TICKS, 0, false, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_TICKS, 0, false, false, true));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.nickel_carbonyl_gas.leak"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % 20 != 0 || !(entity instanceof Player p)) return;
        if (level.getRandom().nextFloat() < LEAK_CHANCE) leak(p);
    }
}
