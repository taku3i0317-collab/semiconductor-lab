package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.item.AiChipItem;
import com.taku3.semiconductorlab.item.AiChipMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * AI チップの書く画面（[B-11]・[10-16]、S-020 Q32 案A、見本 docs/gui_preview/ai_chip_mock.png）：左に 8×8 の方眼（押すと塗る／消す）、
 * 右に答えの数字（ニキシー管の色、7本の線）・0〜9 の確からしさの棒（押すと「教える答え」を選ぶ）・16 個の隠れニューロンのランプ・
 * 「消す」（石）「教える」（銅）のボタン。文字は使わない。色は icons_e の22色。書くたびにチップの中のネットで読み直す（掛け算は mul4 の動き）
 */
public class AiChipScreen extends AbstractContainerScreen<AiChipMenu> {
    private static final int W = 180, H = 115, C = 11, GX = 9, GY = 9, AX = 110, AY = 10, BX = 140, BY = 9, LX = 110, LY = 60, BTN = 15;
    private static final int RESIN_D = 0xFF26262A, RESIN_L = 0xFF707078, SILICON_D = 0xFF3A3A6C, GLASS_L = 0xFFACD8E2, ELEC_D = 0xFFC49600, ELEC_L = 0xFFFFE254,
            STONE_D = 0xFF585652, STONE_L = 0xFFA09C94, COPPER_D = 0xFF8A4426, COPPER_L = 0xFFD48452, NIXIE = 0xFFFF8A30, NIXIE_OFF = 0xFF462814;
    private AiChipItem.Answer answer;

    public AiChipScreen(AiChipMenu menu, Inventory inv, Component title) { super(menu, inv, title, W, H); }

    private void reread() {
        if (minecraft == null || minecraft.player == null) return;
        boolean any = false; for (int v : menu.grid) any |= v != 0;
        answer = any ? AiChipItem.recognize(AiChipMenu.chip(minecraft.player), menu.grid) : null;
    }

    private void press(int id) {
        menu.apply(id);
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        if (id < 64 || id == AiChipMenu.CLEAR) reread();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int l = leftPos, t = topPos;
        g.fill(l, t, l + W, t + H, RESIN_D);
        g.fill(l + 3, t + 3, l + W - 3, t + H - 3, SILICON_D);
        for (int i = 0; i < 64; i++) {   // 方眼
            int x = l + GX + (i % 8) * C, y = t + GY + (i / 8) * C;
            g.fill(x, y, x + C - 1, y + C - 1, menu.grid[i] != 0 ? GLASS_L : RESIN_L);
        }
        digit(g, l + AX, t + AY, answer == null ? -1 : answer.digit());   // 答え
        for (int d = 0; d < 10; d++) {   // 確からしさの棒（選んだ教える答えは銅のふち）
            int y = t + BY + d * 5, pct = answer == null ? 0 : answer.percent()[d];
            if (menu.label == d) g.fill(l + BX - 1, y - 1, l + BX + 31, y + 4, COPPER_L);
            g.fill(l + BX, y, l + BX + 30, y + 3, RESIN_L);
            g.fill(l + BX, y, l + BX + 30 * pct / 100, y + 3, answer != null && answer.digit() == d ? ELEC_L : ELEC_D);
        }
        for (int j = 0; j < 16; j++) {   // 隠れニューロンのランプ（光り方 0〜15）
            int v = answer == null ? 0 : answer.hidden()[j], x = l + LX + (j % 8) * 8, y = t + LY + (j / 8) * 8;
            g.fill(x, y, x + 6, y + 6, mix(RESIN_L, ELEC_L, v / 15.0));
        }
        button(g, l + LX, t + 85, STONE_D, STONE_L);     // 消す
        button(g, l + LX + 20, t + 85, COPPER_D, COPPER_L);   // 教える
    }

    private static void button(GuiGraphicsExtractor g, int x, int y, int dark, int light) { g.fill(x, y, x + BTN, y + BTN, dark); g.fill(x + 2, y + 2, x + BTN - 2, y + BTN - 2, light); }

    private static int mix(int a, int b, double f) {
        int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f), gg = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f), bb = (int) ((a & 255) * (1 - f) + (b & 255) * f);
        return 0xFF000000 | r << 16 | gg << 8 | bb;
    }

    /** ニキシー管の色の数字（7本の線）。-1 は消えている */
    private static void digit(GuiGraphicsExtractor g, int x, int y, int n) {
        String on = switch (n) { case 0 -> "abcdef"; case 1 -> "bc"; case 2 -> "abged"; case 3 -> "abgcd"; case 4 -> "fgbc"; case 5 -> "afgcd"; case 6 -> "afgedc"; case 7 -> "abc"; case 8 -> "abcdefg"; case 9 -> "abcdfg"; default -> ""; };
        int w = 20, h = 34, m = h / 2, t = 3;
        int[][] seg = {{x, y, w, t}, {x + w - t, y, t, m}, {x + w - t, y + m, t, h - m}, {x, y + h - t, w, t}, {x, y + m, t, h - m}, {x, y, t, m}, {x, y + m - 1, w, t}};
        for (int i = 0; i < 7; i++) g.fill(seg[i][0], seg[i][1], seg[i][0] + seg[i][2], seg[i][1] + seg[i][3], on.indexOf((char) ('a' + i)) >= 0 ? NIXIE : NIXIE_OFF);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {}   // 文字は使わない

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        double rx = e.x() - leftPos, ry = e.y() - topPos;
        if (rx >= GX && rx < GX + 8 * C && ry >= GY && ry < GY + 8 * C) { press((int) ((ry - GY) / C) * 8 + (int) ((rx - GX) / C)); return true; }
        if (rx >= BX && rx < BX + 30 && ry >= BY - 1 && ry < BY + 50) { press(AiChipMenu.LABEL + Math.min(9, (int) ((ry - BY + 1) / 5))); return true; }
        if (ry >= 85 && ry < 85 + BTN) {
            if (rx >= LX && rx < LX + BTN) { press(AiChipMenu.CLEAR); return true; }
            if (rx >= LX + 20 && rx < LX + 20 + BTN) { press(AiChipMenu.TEACH); return true; }
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public void containerTick() {   // 教えたあと（チップの中の見本が変わったら）読み直す
        super.containerTick();
        if (minecraft != null && minecraft.player != null && minecraft.player.tickCount % 10 == 0 && answer != null) reread();
    }
}
