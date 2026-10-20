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
            // [1-34] ゲルマニウムの経路：GeCl4 蒸留経由なら 10N まで、硫酸経由（蒸留なし）は 9N まで
            if (stack.is(com.taku3.semiconductorlab.content.Metallurgy.RAW_GERMANIUM) || stack.is(com.taku3.semiconductorlab.content.Semiconductor.REFINED_GERMANIUM) || stack.is(com.taku3.semiconductorlab.content.Semiconductor.GERMANIUM_SINGLE_CRYSTAL))
                lines.add(Component.translatable(K + (Boolean.TRUE.equals(stack.get(ModDataComponents.CHLORIDE_ROUTE)) ? "route.chloride" : "route.sulfate"))
                        .withStyle(ChatFormatting.GRAY));
            // [9-11] ドーパント：p型（ホウ素）・n型（リン）。Shift で偏析係数
            com.taku3.semiconductorlab.Dopant dp = com.taku3.semiconductorlab.Dopant.byId(stack.getOrDefault(ModDataComponents.DOPANT, ""));
            if (dp != null) {
                lines.add(Component.translatable(K + "dopant." + dp.id).withStyle(dp.type == 'p' ? ChatFormatting.RED : ChatFormatting.BLUE));
                if (Minecraft.getInstance().hasShiftDown())
                    lines.add(Component.translatable(K + "dopant.k", String.format("%.2f", dp.k)).withStyle(ChatFormatting.GRAY));
            }
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