package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 鉛室の酸 [6-12]（鉛室法 1746、IMP3 S-011）。作るとき、ときどき窒素酸化物（NO2、茶色のけむり）がもれて、作った人が少しのあいだ気分が悪くなる（事故、DESIGN 6：その場だけ・一時的）。
 */
public class ChamberAcidItem extends Item {
    public static final float LEAK_CHANCE = 0.2f;
    public static final int LEAK_TICKS = 20 * 6;

    public ChamberAcidItem(Properties p) { super(p); }

    /** NO2 がもれた事故（テストからも呼ぶ） */
    public static void leak(Player p) {
        p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, LEAK_TICKS, 0, false, true, true));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.chamber_acid.leak"));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player p) {
        super.onCraftedBy(stack, p);
        if (!p.level().isClientSide() && p.getRandom().nextFloat() < LEAK_CHANCE) leak(p);
    }
}
