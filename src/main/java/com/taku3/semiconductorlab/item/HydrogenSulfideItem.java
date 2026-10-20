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
 * 硫化水素の袋 [2-11]。危ない物は DESIGN「危ない技術の扱い」6 のとおり「事故」として体験する：
 * 持ち歩いていると、1秒ごとに {@link #LEAK_CHANCE} の確率で袋から少しもれ、吐き気（数秒）とメッセージ
 * 「腐った卵のにおい。濃いと鼻がまひして、においがわからなくなる」が出る。袋は減らない。地形やほかの人には何もしない。
 */
public class HydrogenSulfideItem extends Item {
    public static final float LEAK_CHANCE = 1f / 120f;
    public static final int NAUSEA_TICKS = 20 * 6;

    public HydrogenSulfideItem(Properties properties) { super(properties); }

    /** もれる（テストからも呼ぶ） */
    public static void leak(Player p) {
        p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, NAUSEA_TICKS, 0, false, false, true));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hydrogen_sulfide_gas.leak"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % 20 != 0 || !(entity instanceof Player p)) return;
        if (level.getRandom().nextFloat() < LEAK_CHANCE) leak(p);
    }
}
