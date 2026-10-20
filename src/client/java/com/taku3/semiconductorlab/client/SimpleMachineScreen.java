package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 電気の機械の「ふつうの画面」（IMP-28）。専用の画面を書かない新しい機械はこれになる：
 * 中央バー＝進み具合（左の入口 x=8・出口 x=78 の間）、右に 電力・蓄積・状態（材料待ち／電気待ち／動作中）。
 * 文字の置き場所は LAYOUT に MachineLayout.Builder.standardTexts() を書く（IMP-30）。文は lang の gui.semiconductor-lab.machine.*（全部の機械で共通）。
 */
public class SimpleMachineScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.machine.";

    public SimpleMachineScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFFB23E);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "stored" -> Component.translatable(K + "stored", menu.getStoredKj() + " kJ");
            case "hint" -> Component.translatable(K + (!menu.isReady() ? "need_input" : menu.getKw() <= 0 ? "need_power" : "running"));
            default -> null;
        };
    }
}
