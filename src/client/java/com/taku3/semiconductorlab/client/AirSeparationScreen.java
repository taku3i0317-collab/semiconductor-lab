package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 空気分離装置の画面。左＝精留塔のバー（進み具合）、中＝N2/O2/Ar の出口（ラベル付き）、右＝電力・蓄積・状態 */
public class AirSeparationScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 17, BAR_W = 6, BAR_H = 52;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.air_separation_unit.";

    public AirSeparationScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int h = menu.getProgress() * BAR_H / menu.getTotal();
        if (h > 0) g.fill(x, y + BAR_H - h, x + BAR_W, y + BAR_H, 0xFF7FC8FF);   // 下から液体空気がたまる
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        String[] gas = {"N2", "O2", "Ar"};
        for (int i = 0; i < gas.length; i++) g.text(font, gas[i], 44, 21 + i * 18, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 32, TEXT_COLOR, false);
        String hint = !menu.isReady() ? "output_full" : menu.getKw() <= 0 ? "need_power" : "running";
        g.text(font, Component.translatable(K + hint), TEXT_X, 48, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "air"), TEXT_X, 60, TEXT_COLOR, false);
    }
}
