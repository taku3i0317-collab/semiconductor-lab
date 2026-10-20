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
            // 鉛蓄電池の残量（データコンポーネント battery_energy）
            Long battery = stack.get(com.taku3.semiconductorlab.ModDataComponents.BATTERY_ENERGY);
            if (battery != null) lines.add(Component.translatable("tooltip.semiconductor-lab.battery_energy",
                    com.taku3.semiconductorlab.energy.EnergyStorage.format(battery), com.taku3.semiconductorlab.energy.EnergyStorage.format(com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CAPACITY)).withStyle(ChatFormatting.AQUA));
            String ph = key(id, "placeholder");
            // 「省略：」（en は Omitted / Simplified）は Shift のときだけ。「近似：」「未実装」はいつも出す（化学ルール1：本物と違うところは隠さない）
            boolean omittedHidden = false;
            if (ph != null) {
                Component phc = Component.translatable(ph).withStyle(ChatFormatting.YELLOW);
                if (!isOmitted(phc.getString()) || shiftDown()) lines.add(phc); else omittedHidden = true;
            }
            // へえ①（子ども）は常に表示、へえ②（学生）は Shift。旧 .note は .note2 扱い。.note3/.note4 はガイドブック用
            String note1 = key(id, "note1");
            if (note1 != null) lines.add(Component.translatable(note1).withStyle(ChatFormatting.GRAY));
            String note = key(id, "note2");
            if (note == null) note = key(id, "note");
            if (note != null) {
                if (shiftDown()) {
                    lines.add(Component.translatable(note).withStyle(ChatFormatting.GRAY));
                    // へえ③（大人）・④（ガチ）は冒頭だけ。全文はガイドタブ「図鑑」（advancement zukan/<id>）
                    for (String s : new String[] {"note3", "note4"}) {
                        String k = key(id, s);
                        if (k != null) lines.add(Component.translatable("tooltip.semiconductor-lab." + s + "_locked",
                                preview(Language.getInstance().getOrDefault(k))).withStyle(ChatFormatting.DARK_AQUA));
                    }
                } else lines.add(Component.translatable("tooltip.semiconductor-lab.shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else if (omittedHidden) lines.add(Component.translatable("tooltip.semiconductor-lab.shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            wrap(lines);
        });
    }

    /** ツールチップの1行の幅（px）。これより長い行は折り返す（バニラの長い説明と同じくらい、IMP-30） */
    public static final int WIDTH = 200;

    /** 「省略：」の行か（ja・en） */
    static boolean isOmitted(String s) { return s.startsWith("省略") || s.startsWith("Omitted") || s.startsWith("Simplified"); }

    /** 1行目（アイテムの名前）より後ろの長い行を WIDTH で折り返す。色・書式は行の中でもそのまま */
    static void wrap(java.util.List<Component> lines) {
        var font = Minecraft.getInstance().font;
        java.util.List<Component> out = new java.util.ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Component c = lines.get(i);
            if (i == 0 || font.width(c) <= WIDTH) { out.add(c); continue; }
            for (var ft : font.getSplitter().splitLines(c, WIDTH, net.minecraft.network.chat.Style.EMPTY)) {
                var m = Component.empty();
                ft.visit((style, text) -> { m.append(Component.literal(text).setStyle(style)); return java.util.Optional.empty(); }, net.minecraft.network.chat.Style.EMPTY);
                out.add(m);
            }
        }
        lines.clear();
        lines.addAll(out);
    }

    /** 画面の自動チェック（IMP-30 ScreenChecks）用：null ならキーボードの Shift、true/false ならそのふりをする */
    public static Boolean forceShift;
    static boolean shiftDown() { return forceShift != null ? forceShift : Minecraft.getInstance().hasShiftDown(); }

    /** 冒頭 PREVIEW 文字（コードポイント単位）＋「…」 */
    static final int PREVIEW = 18;
    static String preview(String s) {
        if (s.codePointCount(0, s.length()) <= PREVIEW) return s;
        return s.substring(0, s.offsetByCodePoints(0, PREVIEW)) + "…";
    }

    private static String key(String id, String suffix) {
        for (String prefix : new String[] {"item.", "block."}) {
            String k = prefix + id + "." + suffix;
            if (Language.getInstance().has(k)) return k;
        }
        return null;
    }
}