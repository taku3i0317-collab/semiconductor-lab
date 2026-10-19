package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.AchesonFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity.*;

/**
 * アチソン炉の画面。左上＝炭素、左下＝砂、中央バー＝黒鉛化の進み具合、その下に温度計（2500℃に印）、
 * 右＝電力（いま／必要）・温度・状態。電力不足なら「この電力で届く温度」を出して、なぜ動かないかを見せる。
 */
public class AchesonFurnaceScreen extends AbstractContainerScreen<AchesonFurnaceMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/acheson_furnace.png");
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final int TEMP_Y = 48, TEMP_H = 4;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040, WARN_COLOR = 0xFFB02020;
    private static final String K = "gui.semiconductor-lab.acheson_furnace.";

    public AchesonFurnaceScreen(AchesonFurnaceMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF9FA4AA);
        // 温度計（0〜STEADY_C）。2500℃ の位置に白い印
        int ty = topPos + TEMP_Y;
        g.fill(x, ty, x + BAR_W, ty + TEMP_H, 0xFF3A3A40);
        int tw = (int) Math.min(BAR_W, menu.getTemperature() * BAR_W / STEADY_C);
        if (tw > 0) g.fill(x, ty, x + tw, ty + TEMP_H, menu.getTemperature() >= GRAPHITIZE_C ? 0xFFFFF4D6 : 0xFFFF5A1E);
        int mark = x + (int) (GRAPHITIZE_C * BAR_W / STEADY_C);
        g.fill(mark, ty - 1, mark + 1, ty + TEMP_H + 1, 0xFFFFFFFF);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        double kw = menu.getKw(), needKw = REQUIRED_WATTS / 1000;
        int t = menu.getTemperature();
        boolean under = kw < needKw * 0.97;
        g.text(font, Component.translatable("gui.semiconductor-lab.hydrogen_furnace.power", String.format("%.0f kW", kw)), TEXT_X, 16, under ? WARN_COLOR : TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "need", String.format("%.0f kW", needKw)), TEXT_X, 26, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "temp", t + "℃"), TEXT_X, 38, TEXT_COLOR, false);
        ItemStack carbon = menu.getSlot(SLOT_CARBON).getItem(), sand = menu.getSlot(SLOT_SAND).getItem();
        String hint;
        Object arg = null;
        if (carbon.getCount() < CARBON_PER_BATCH) hint = "need_carbon";
        else if (sand.isEmpty()) hint = "need_sand";
        else if (!menu.isReady()) hint = "output_full";
        else if (kw <= 0) hint = "need_power";
        else if (under && t < GRAPHITIZE_C) { hint = "underpowered"; arg = (int) steadyTemperature(kw * 1000) + "℃"; }
        else if (t < GRAPHITIZE_C) hint = "heating";
        else hint = "graphitizing";
        Component line = Component.translatable(K + hint);
        g.text(font, line, TEXT_X, 50, hint.equals("underpowered") ? WARN_COLOR : TEXT_COLOR, false);
        if (hint.equals("underpowered")) g.text(font, Component.translatable(K + "underpowered2", arg), TEXT_X, 60, WARN_COLOR, false);
        g.text(font, Component.literal("C"), 28, 21, TEXT_COLOR, false);
        g.text(font, Component.literal("SiO2"), 28, 57, TEXT_COLOR, false);
    }
}
