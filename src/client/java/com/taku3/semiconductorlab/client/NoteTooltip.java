package com.taku3.semiconductorlab.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

/** item.<id>.note / item.<id>.placeholder という翻訳キーがあれば自動で表示 */
public class NoteTooltip {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().replace(':', '.');
            String ph = "item." + id + ".placeholder";
            if (Language.getInstance().has(ph)) lines.add(Component.translatable(ph).withStyle(ChatFormatting.YELLOW));
            String note = "item." + id + ".note";
            if (Language.getInstance().has(note)) {
                if (Minecraft.getInstance().hasShiftDown()) lines.add(Component.translatable(note).withStyle(ChatFormatting.GRAY));
                else lines.add(Component.translatable("tooltip.semiconductor-lab.shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }
}