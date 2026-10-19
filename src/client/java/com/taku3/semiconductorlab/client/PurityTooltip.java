package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class PurityTooltip {
    private static final String K = "tooltip.semiconductor-lab.";

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            Double p = stack.get(ModDataComponents.PURITY);
            if (p == null) return;
            lines.add(Component.translatable(K + "purity", Purity.label(p)).withStyle(ChatFormatting.AQUA));
            if (Minecraft.getInstance().hasShiftDown()) {
                lines.add(Component.translatable(K + "purity.percent", Purity.percent(p)).withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(K + "purity.ratio", Purity.impurityDenominator(p)).withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(K + "purity.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                lines.add(Component.translatable(K + "shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }
}