package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** 電気炉の画面。左＝入れる物、中央バー＝焼き上がりまで、右＝電力・蓄積・状態 */
public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/electric_furnace.png");
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.electric_furnace.";

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFFFB23E);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable("gui.semiconductor-lab.hydrogen_furnace.power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable("gui.semiconductor-lab.hydrogen_furnace.stored", menu.getStoredKj() + " kJ"), TEXT_X, 32, TEXT_COLOR, false);
        String hint;
        if (!ElectricFurnaceBlockEntity.isInput(menu.getSlot(ElectricFurnaceBlockEntity.SLOT_IN).getItem())) hint = "need_input";
        else if (!menu.isReady()) hint = "output_full";
        else if (menu.getKw() <= 0) hint = "need_power";
        else hint = "heating";
        g.text(font, Component.translatable(K + hint), TEXT_X, 48, TEXT_COLOR, false);
    }
}
