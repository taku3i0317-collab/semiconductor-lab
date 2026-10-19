package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.energy.SteamEngineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** 蒸気機関の画面。上＝水バケツ、下＝石炭、間の炎＝燃焼の残り時間。右に出力・蓄積・水の残り */
public class SteamEngineScreen extends AbstractContainerScreen<SteamEngineMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/steam_engine.png");
    private static final int FLAME_X = 57, FLAME_Y = 37, FLAME_W = 14, FLAME_H = 14;
    private static final int TEXT_X = 82, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.steam_engine.";

    public SteamEngineScreen(SteamEngineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // 炎：燃えている間、残り時間に応じて下から縮む（かまどと同じ見せ方）
        int x = leftPos + FLAME_X, y = topPos + FLAME_Y;
        g.fill(x, y, x + FLAME_W, y + FLAME_H, 0xFF3A3A40);
        int h = menu.getBurn() * FLAME_H / menu.getBurnTotal();
        if (h > 0) g.fill(x, y + FLAME_H - h, x + FLAME_W, y + FLAME_H, 0xFFFF8C1E);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable(K + "output", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 32, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "water", menu.getWater()), TEXT_X, 44, TEXT_COLOR, false);
        Component hint = null;
        if (menu.getBurn() > 0) hint = Component.translatable(K + "running");
        else if (menu.getWater() <= 0 && !menu.getSlot(1).getItem().is(Items.WATER_BUCKET)) hint = Component.translatable(K + "need_water");
        else if (menu.getSlot(0).getItem().isEmpty()) hint = Component.translatable(K + "need_coal");
        if (hint != null) g.text(font, hint, TEXT_X, 58, TEXT_COLOR, false);
    }
}
