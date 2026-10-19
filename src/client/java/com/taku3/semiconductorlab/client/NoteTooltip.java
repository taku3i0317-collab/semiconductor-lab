package com.taku3.semiconductorlab.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

/** item.<id>.placeholder / .note1 / .note2（旧 .note）という翻訳キーがあれば自動で表示 */
public class NoteTooltip {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().replace(':', '.');
            // ブロックのアイテムは block.<id>.note に書いてあるので、item. → block. の順で探す
            String ph = key(id, "placeholder");
            if (ph != null) lines.add(Component.translatable(ph).withStyle(ChatFormatting.YELLOW));
            // へえ①（子ども）は常に表示、へえ②（学生）は Shift。旧 .note は .note2 扱い。.note3/.note4 はガイドブック用
            String note1 = key(id, "note1");
            if (note1 != null) lines.add(Component.translatable(note1).withStyle(ChatFormatting.GRAY));
            String note = key(id, "note2");
            if (note == null) note = key(id, "note");
            if (note != null) {
                if (Minecraft.getInstance().hasShiftDown()) lines.add(Component.translatable(note).withStyle(ChatFormatting.GRAY));
                else lines.add(Component.translatable("tooltip.semiconductor-lab.shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }

    private static String key(String id, String suffix) {
        for (String prefix : new String[] {"item.", "block."}) {
            String k = prefix + id + "." + suffix;
            if (Language.getInstance().has(k)) return k;
        }
        return null;
    }
}