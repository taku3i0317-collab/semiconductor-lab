package com.taku3.semiconductorlab.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 作るときに、ときどき危ない気体がもれる物（IMP3、DESIGN「危ない技術の扱い」6：事故はその場だけ・一時的な効果）。
 * 例：フッ化水素・塩素を使う反応（[2-64] フッ素ゴムの材料）。もれると少しのあいだ効果（吐き気・弱体化など）とメッセージ。
 */
public class CraftLeakItem extends Item {
    private final Holder<MobEffect> effect;
    private final int ticks;
    private final float chance;
    private final String messageKey;

    public CraftLeakItem(Properties p, Holder<MobEffect> effect, int ticks, float chance, String messageKey) {
        super(p);
        this.effect = effect; this.ticks = ticks; this.chance = chance; this.messageKey = messageKey;
    }

    /** もれた事故（テストからも呼ぶ） */
    public void leak(Player p) {
        p.addEffect(new MobEffectInstance(effect, ticks, 0, false, true, true));
        p.sendOverlayMessage(Component.translatable(messageKey));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player p) {
        super.onCraftedBy(stack, p);
        if (!p.level().isClientSide() && p.getRandom().nextFloat() < chance) leak(p);
    }
}
