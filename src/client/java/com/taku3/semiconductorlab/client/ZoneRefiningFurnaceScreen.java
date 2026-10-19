package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** 帯域精製炉の画面。背景＋溶融帯の進行バー＋純度の表示 */
public class ZoneRefiningFurnaceScreen extends AbstractContainerScreen<ZoneRefiningFurnaceMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/zone_refining_furnace.png");
    private static final int BAR_X = 60, BAR_Y = 58, BAR_W = 56, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.refiner.";

    public ZoneRefiningFurnaceScreen(ZoneRefiningFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // 溶融帯バー（棒に沿ってコイルが進むイメージ）
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFFF8C1E);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        Component line;
        if (menu.isEmptySlot()) line = Component.translatable(K + "hint");
        else {
            int n = menu.getNines();
            String cur = n <= 0 ? Component.translatable(K + "unrefined").getString() : n + "N";
            line = n >= 11 ? Component.translatable(K + "max", cur)
                           : Component.translatable(K + "purity", cur, (n <= 0 ? 6 : n + 3) + "N");
        }
        int w = font.width(line);
        g.text(font, line, (imageWidth - w) / 2, 66, 0xFF404040, false);
    }
}