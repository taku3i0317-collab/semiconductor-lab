package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderMenu;
import com.taku3.semiconductorlab.block.GasType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** ガスホルダーの画面。左＝袋詰めガスの入口、中央＝貯蔵量バーとガスの種類・量・体積、右＝出口 */
public class GasHolderScreen extends AbstractContainerScreen<GasHolderMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/gas_holder.png");
    private static final int BAR_X = 48, BAR_Y = 18, BAR_W = 80, BAR_H = 8;
    private static final int TEXT_X = 48, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.gas_holder.";
    /** 25℃・1気圧の 1mol の体積（L） */
    private static final double LITERS_PER_MOL = 24.5;

    public GasHolderScreen(GasHolderMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    private static int color(GasType t) {
        return switch (t) { case HYDROGEN -> 0xFFE8E070; case OXYGEN -> 0xFF6FB7FF; default -> 0xFF808080; };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getAmount() * BAR_W / GasHolderBlockEntity.CAPACITY;
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, color(menu.getGasType()));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        GasType t = menu.getGasType();
        int amount = menu.getAmount();
        g.text(font, Component.translatable(K + "gas." + t.key), TEXT_X, 30, TEXT_COLOR, false);
        g.text(font, Component.literal(amount + "/" + GasHolderBlockEntity.CAPACITY + " mol"), TEXT_X, 41, TEXT_COLOR, false);
        g.text(font, Component.literal(String.format("≈%.1f m³", amount * LITERS_PER_MOL / 1000)), TEXT_X, 52, TEXT_COLOR, false);
        if (menu.justBanged()) g.text(font, Component.translatable(K + "bang"), TEXT_X, 63, 0xFFC03030, false);
        g.text(font, Component.translatable(K + "in"), 22, 54, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "out"), 130, 54, TEXT_COLOR, false);
    }
}
