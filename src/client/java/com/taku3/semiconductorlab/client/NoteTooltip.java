package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.TooltipText;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

/**
 * item.<id>.placeholder / .note1 / .note2（旧 .note）という翻訳キーがあれば自動で表示。
 * どの行を出すか・どこで折り返すかは machine/TooltipText（自動チェック GameTest tooltip_text と同じ計算、IMP-30）
 */
public class NoteTooltip {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().replace(':', '.');
            // 鉛蓄電池の残量（データコンポーネント battery_energy）
            Long battery = stack.get(com.taku3.semiconductorlab.ModDataComponents.BATTERY_ENERGY);
            if (battery != null) lines.add(Component.translatable("tooltip.semiconductor-lab.battery_energy",
                    com.taku3.semiconductorlab.energy.EnergyStorage.format(battery), com.taku3.semiconductorlab.energy.EnergyStorage.format(com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CAPACITY)).withStyle(ChatFormatting.AQUA));
            // 加工中のウェハーの汚れ（[9-14] 洗浄、O-002）
            var chip = stack.get(com.taku3.semiconductorlab.ModDataComponents.CHIP_STATE);
            if (chip != null && chip.contamination() > 0)
                lines.add(Component.translatable("tooltip.semiconductor-lab.contamination", chip.contamination()).withStyle(ChatFormatting.GOLD));
            // ブロックのアイテムは block.<id>.… に書いてあるので、item. → block. の順で探す
            Language lang = Language.getInstance();
            String base = lang.has("item." + id) || !lang.has("block." + id) ? "item." + id : "block." + id;
            var font = Minecraft.getInstance().font;
            for (TooltipText.Line l : TooltipText.lines(base, vesselKey(stack), bom(stack), shiftDown(), k -> lang.has(k) ? lang.getOrDefault(k) : null, font::width)) {
                ChatFormatting[] style = switch (l.tone()) {
                    case VESSEL -> new ChatFormatting[] {ChatFormatting.BLUE};
                    case PLACEHOLDER -> new ChatFormatting[] {ChatFormatting.YELLOW};
                    case NOTE -> new ChatFormatting[] {ChatFormatting.GRAY};
                    case DEEP -> new ChatFormatting[] {ChatFormatting.DARK_AQUA};
                    case HINT -> new ChatFormatting[] {ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC};
                };
                lines.add(Component.literal(l.text()).withStyle(style));
            }
        });
    }

    static java.util.List<TooltipText.BomPart> bom(net.minecraft.world.item.ItemStack s) {
        var a = com.taku3.semiconductorlab.machine.Assembly.of(s.getItem());
        return a == null ? null : a.bom();
    }

    static String vesselKey(net.minecraft.world.item.ItemStack s) {
        var v = com.taku3.semiconductorlab.machine.Vessel.of(s.getItem());
        return v == null ? null : v.langKey();
    }

    /** 画面の自動チェック用：null ならキーボードの Shift、true/false ならそのふりをする */
    public static Boolean forceShift;
    static boolean shiftDown() { return forceShift != null ? forceShift : Minecraft.getInstance().hasShiftDown(); }
}
