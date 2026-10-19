package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** 電解槽の画面。左＝水バケツ/硫酸、中央バー＝分解の進み具合、出口＝水素/酸素、右＝電力・蓄積・状態 */
public class ElectrolyzerScreen extends AbstractContainerScreen<ElectrolyzerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/electrolyzer.png");
    private static final int BAR_X = 28, BAR_Y = 38, BAR_W = 24, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.electrolyzer.";

    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF4FA3FF); // 青＝電気
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 32, TEXT_COLOR, false);
        // 状態：足りないものを1つだけ表示（上から順に確認）
        String hint;
        var second = menu.getSlot(ElectrolyzerBlockEntity.SLOT_ACID).getItem();
        if (!menu.getSlot(ElectrolyzerBlockEntity.SLOT_WATER).getItem().is(Items.WATER_BUCKET)) hint = "need_water";
        else if (!ElectrolyzerBlockEntity.isSecondInput(second)) hint = "need_acid";
        else if (ElectrolyzerBlockEntity.isSalt(second) && second.getCount() < ElectrolyzerBlockEntity.SALT_PER_RUN) hint = "need_salt";
        else if (!menu.isReady()) hint = "output_full";
        else if (menu.getKw() <= 0) hint = "need_power";
        else hint = "running";
        g.text(font, Component.translatable(K + hint), TEXT_X, 48, TEXT_COLOR, false);
        // モード（水の電気分解／クロルアルカリ）と出口ラベル。陽極は酸素か塩素
        boolean brine = menu.getMode() == ElectrolyzerBlockEntity.MODE_BRINE;
        g.text(font, Component.translatable(K + (brine ? "mode_brine" : "mode_water")), TEXT_X, 62, TEXT_COLOR, false);
        g.text(font, Component.literal("H2"), 62, 21, TEXT_COLOR, false);
        g.text(font, Component.literal(brine ? "Cl2" : "O2"), 58, 39, TEXT_COLOR, false);
        g.text(font, Component.literal("NaOH"), 52, 57, TEXT_COLOR, false);
    }
}
