package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.block.CrystalPullerBlockEntity;
import com.taku3.semiconductorlab.block.CrystalPullerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 引き上げ機の画面。左＝Ge・種・水素、中央バー＝引き上げの進み具合、出口、右＝電力・状態・モード */
public class CrystalPullerScreen extends AbstractContainerScreen<CrystalPullerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/crystal_puller.png");
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.crystal_puller.";

    public CrystalPullerScreen(CrystalPullerMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFC0C8D0);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 32, TEXT_COLOR, false);
        ItemStack seed = menu.getSlot(CrystalPullerBlockEntity.SLOT_SEED).getItem();
        var mode = CrystalPullerBlockEntity.modeOf(seed);
        String hint;
        if (mode == CrystalPullerBlockEntity.Mode.NONE) hint = "need_seed";
        else if (!menu.getSlot(CrystalPullerBlockEntity.SLOT_H2).getItem().is(ModItems.HYDROGEN_GAS)) hint = "need_h2";
        else if (!menu.isReady()) hint = "need_ge";
        else if (menu.getKw() <= 0) hint = "need_power";
        else hint = "running";
        g.text(font, Component.translatable(K + hint), TEXT_X, 48, TEXT_COLOR, false);
        if (mode != CrystalPullerBlockEntity.Mode.NONE) g.text(font, Component.translatable(K + "mode." + mode.name().toLowerCase()), TEXT_X, 60, TEXT_COLOR, false);
    }
}
