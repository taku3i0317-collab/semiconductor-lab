package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.WaferSawBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ウェハー切断機の画面。左＝単結晶・刃、中央バー＝進み具合、出口＝シリコンの板、右＝電力・状態 */
public class WaferSawScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.wafer_saw.";

    public WaferSawScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFD8DCE0);   // 刃の銀色
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "need", String.format("%.0f kW", WaferSawBlockEntity.WATTS / 1000)), TEXT_X, 32, TEXT_COLOR, false);
        String hint = !menu.isReady() ? "need_input" : menu.getKw() <= 0 ? "need_power" : "running";
        g.text(font, Component.translatable(K + hint), TEXT_X, 48, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "recipe"), TEXT_X, 60, TEXT_COLOR, false);
    }
}
