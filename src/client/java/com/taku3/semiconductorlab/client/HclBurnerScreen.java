package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.HclBurnerBlockEntity;
import com.taku3.semiconductorlab.block.HclBurnerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** 塩酸合成炉の画面。左上＝水バケツ、左下＝水素・塩素、中央バー＝燃焼の進み、右＝塩酸、右下＝空ボンベ・空バケツ。中のガス（mol）と水を表示 */
public class HclBurnerScreen extends AbstractContainerScreen<HclBurnerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/hcl_burner.png");
    private static final int BAR_X = 80, BAR_Y = 41, BAR_W = 30, BAR_H = 6;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.hcl_burner.";

    public HclBurnerScreen(HclBurnerMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFE0E8A0);   // 塩素中の水素の炎は白っぽい黄緑
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.literal("H2 " + menu.getH2()), HclBurnerMenu.H2_X - 2, HclBurnerMenu.GAS_Y - 10, TEXT_COLOR, false);
        g.text(font, Component.literal("Cl2 " + menu.getCl2()), HclBurnerMenu.CL2_X - 2, HclBurnerMenu.GAS_Y - 10, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "water", menu.getWater()), HclBurnerMenu.WATER_X + 20, HclBurnerMenu.WATER_Y + 4, TEXT_COLOR, false);
        String hint;
        if (menu.getProgress() > 0) hint = "burning";
        else if (menu.getWater() <= 0 && menu.getSlot(HclBurnerBlockEntity.SLOT_WATER).getItem().isEmpty()) hint = "need_water";
        else if (menu.getH2() < HclBurnerBlockEntity.H2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_H2).getItem().isEmpty()) hint = "need_h2";
        else if (menu.getCl2() < HclBurnerBlockEntity.CL2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_CL2).getItem().isEmpty()) hint = "need_cl2";
        else hint = "out_full";
        g.text(font, Component.translatable(K + hint), 80, 26, TEXT_COLOR, false);
    }
}
