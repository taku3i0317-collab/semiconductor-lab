package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 氷酢酸 [2-42]（水をほとんど含まない酢酸）。約17℃で凍る（だから「氷」酢酸）。寒い所（バイオームの気温 COLD 以下）に持っていくと
 * 名前が「凍った氷酢酸」に変わり、暖かい所でまた液体に戻る（持ち物の中で CHECK_TICKS ごとに調べる）。
 */
public class GlacialAceticAcidItem extends Item {
    public static final float COLD = 0.3f;
    public static final int CHECK_TICKS = 100;
    public static final String FROZEN = "item.semiconductor-lab.acetic_acid.frozen";

    public GlacialAceticAcidItem(Properties properties) { super(properties); }

    public static boolean frozen(ItemStack stack) {
        Component name = stack.get(DataComponents.ITEM_NAME);
        return name != null && name.getContents() instanceof TranslatableContents t && t.getKey().equals(FROZEN);
    }

    /** 気温で凍る・とける。変わったら true（テストからも呼ぶ） */
    public static boolean applyTemperature(ItemStack stack, float temperature) {
        boolean want = temperature <= COLD;
        if (frozen(stack) == want) return false;
        if (want) stack.set(DataComponents.ITEM_NAME, Component.translatable(FROZEN));
        else stack.remove(DataComponents.ITEM_NAME);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % CHECK_TICKS != 0) return;
        if (applyTemperature(stack, level.getBiome(entity.blockPosition()).value().getBaseTemperature()) && entity instanceof Player p)
            p.sendOverlayMessage(Component.translatable(frozen(stack) ? "message.semiconductor-lab.acetic_acid.frozen" : "message.semiconductor-lab.acetic_acid.melted"));
    }
}
