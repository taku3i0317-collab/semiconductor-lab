package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** 帯域精製炉の画面。左＝Ge、右＝ドーパント。バーは溶融帯の進み具合 */
public class ZoneRefiningFurnaceScreen extends AbstractContainerScreen<ZoneRefiningFurnaceMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/zone_refining_furnace.png");
    private static final int BAR_X = 60, BAR_Y = 58, BAR_W = 56, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.refiner.";

    public ZoneRefiningFurnaceScreen(ZoneRefiningFurnaceMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        int color = menu.getMode() == ZoneRefiningFurnaceBlockEntity.MODE_DOPE ? 0xFF4FA3FF : 0xFFFF8C1E; // 青＝ドーピング、橙＝精製
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, color);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        Component line;
        int n = menu.getNines();
        if (menu.isGeEmpty()) line = Component.translatable(K + "hint");
        else if (menu.getMode() == ZoneRefiningFurnaceBlockEntity.MODE_DOPE) line = Component.translatable(K + "doping", n + "N");
        else if (n >= 9 && menu.hasDopant()) line = Component.translatable(K + "purity", n + "N", (n >= 10 ? "-" : "10N"));
        else if (n >= 9 && n < 10) line = Component.translatable(K + "ready", n + "N");
        else if (n >= 10) line = Component.translatable(K + "max", n + "N");
        // 決定（2026-10）：6N 以下＋ドーパントは「先に精製 → 9N になったら自動でドープ」。待っている理由を見せる
        else if (menu.hasDopant()) line = Component.translatable(K + "dopant_wait");
        else {
            String cur = n <= 0 ? Component.translatable(K + "unrefined").getString() : n + "N";
            line = Component.translatable(K + "purity", cur, (n <= 0 ? 6 : 9) + "N");
        }
        g.text(font, line, (imageWidth - font.width(line)) / 2, 66, 0xFF404040, false);
    }
}