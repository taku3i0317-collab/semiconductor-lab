package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity;
import com.taku3.semiconductorlab.block.OxyhydrogenBurnerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 酸水素バーナーの画面。上＝石英、左下＝水素・酸素ボンベ、中央バー＝溶ける進み、右＝溶融石英・空ボンベ。つないだガスの残り（mol）を表示 */
public class OxyhydrogenBurnerScreen extends AbstractContainerScreen<OxyhydrogenBurnerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/oxyhydrogen_burner.png");
    private static final int BAR_X = 80, BAR_Y = 41, BAR_W = 30, BAR_H = 6;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.oxyhydrogen_burner.";

    public OxyhydrogenBurnerScreen(OxyhydrogenBurnerMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF9AD8FF);   // 酸水素炎はほとんど見えない淡い青
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.literal("H2 " + menu.getH2()), OxyhydrogenBurnerMenu.H2_X - 2, OxyhydrogenBurnerMenu.GAS_Y - 10, TEXT_COLOR, false);
        g.text(font, Component.literal("O2 " + menu.getO2()), OxyhydrogenBurnerMenu.O2_X - 2, OxyhydrogenBurnerMenu.GAS_Y - 10, TEXT_COLOR, false);
        ItemStack in = menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_IN).getItem();
        String hint;
        if (in.isEmpty()) hint = "need_input";
        else if (menu.getProgress() > 0) hint = "melting";
        else if (menu.getH2() < OxyhydrogenBurnerBlockEntity.H2_PER && menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_H2).getItem().isEmpty()) hint = "need_h2";
        else if (menu.getO2() < OxyhydrogenBurnerBlockEntity.O2_PER && menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_O2).getItem().isEmpty()) hint = "need_o2";
        else hint = "out_full";
        g.text(font, Component.translatable(K + hint), 80, 22, TEXT_COLOR, false);
    }
}
