package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.item.PhoneMenu;
import com.taku3.semiconductorlab.item.SmartphoneItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * スマホのホーム画面（[B-10]、S-017 の3・S-019 の5）：MOD のドット絵に合わせた自作のアイコン（B の icons_e と同じ決まり：16×16、11素材×濃い・薄い の22色、1つのアイコンは2素材まで）が並ぶ。
 * 実在の会社の画面はまねしない。アイコン：電卓（樹脂＋電気）・写真（樹脂＋ガラス）・傾き（シリコン＋電気）。下に電池の残り。文字は使わない
 */
public class SmartphoneScreen extends AbstractContainerScreen<PhoneMenu> {
    private static final int W = 96, H = 160, ICON = 16, SCALE = 2, GAP = 8;
    /** icons_e の色（PALETTE の値と同じ）：素材 → {濃い, 薄い} */
    private static final int[] RESIN = {0xFF26262A, 0xFF707078}, ELEC = {0xFFC49600, 0xFFFFE254}, GLASS = {0xFF38768A, 0xFFACD8E2}, SILICON = {0xFF3A3A6C, 0xFF7A7AB6};
    /** アイコンの点：'.'＝透明、a/A＝1つ目の素材の濃い/薄い、b/B＝2つ目 */
    private static final String[][] ICONS = {
        {   // 電卓：樹脂の体に、電気の色の表示と押しボタン
            "..aaaaaaaaaaaa..", ".aAAAAAAAAAAAAa.", ".aAbbbbbbbbbbAa.", ".aAbBBBBBBBBbAa.", ".aAbBBBBBBBBbAa.", ".aAbbbbbbbbbbAa.", ".aAAAAAAAAAAAAa.",
            ".aAbbAbbAbbAbbAa".substring(0, 16), ".aAbbAbbAbbAAAa.", ".aAAAAAAAAAAAAa.", ".aAbbAbbAbbAbbAa".substring(0, 16), ".aAbbAbbAbbAAAa.", ".aAAAAAAAAAAAAa.",
            ".aAbbAbbAbbAbbAa".substring(0, 16), ".aAAAAAAAAAAAAa.", "..aaaaaaaaaaaa.."},
        {   // 写真：樹脂の体に、ガラスのレンズ
            "................", "....aaaa........", "...aAAAAa.......", ".aaaaaaaaaaaaaa.", "aAAAAAAAAAAAAAAa", "aAAAAbbbbbbAAAAa", "aAAAbBBBBBBbAAAa",
            "aAAAbBbbbbBbAAAa", "aAAAbBbBBbBbAAAa", "aAAAbBbBBbBbAAAa", "aAAAbBbbbbBbAAAa", "aAAAbBBBBBBbAAAa", "aAAAAbbbbbbAAAAa", "aAAAAAAAAAAAAAAa",
            ".aaaaaaaaaaaaaa.", "................"},
        {   // 傾き：シリコンのチップ（MEMS の加速度計）と、電気の色の矢印（重力の向き）
            "................", "...a.a.a.a.a....", "..aaaaaaaaaaa...", ".aaAAAAAAAAAaa..", "..aAAAAbAAAAa...", ".aaAAAAbAAAAaa..", "..aAAAAbAAAAa...",
            ".aaAAAAbAAAAaa..", "..aAAbbbbbAAa...", ".aaAAAbbbAAAaa..", "..aAAAAbAAAAa...", ".aaAAAAAAAAAaa..", "..aaaaaaaaaaa...", "...a.a.a.a.a....",
            "................", "................"},
    };
    private static final int[][][] COLORS = {{RESIN, ELEC}, {RESIN, GLASS}, {SILICON, ELEC}};

    public SmartphoneScreen(PhoneMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, W, H);
    }

    private int iconX(int i) { return leftPos + 12 + i % 2 * (ICON * SCALE + GAP); }
    private int iconY(int i) { return topPos + 20 + i / 2 * (ICON * SCALE + GAP); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.fill(leftPos, topPos, leftPos + W, topPos + H, RESIN[0]);                          // 体（樹脂）
        g.fill(leftPos + 6, topPos + 12, leftPos + W - 6, topPos + H - 18, SILICON[0]);      // 画面
        for (int i = 0; i < ICONS.length; i++) icon(g, i, iconX(i), iconY(i));
        long e = minecraft == null || minecraft.player == null ? 0 : SmartphoneItem.energy(PhoneMenu.phone(minecraft.player));
        int bw = W - 24, filled = (int) (bw * Math.min(1.0, e / (double) SmartphoneItem.CAPACITY_J));
        g.fill(leftPos + 12, topPos + H - 12, leftPos + 12 + bw, topPos + H - 7, RESIN[1]);  // 電池
        g.fill(leftPos + 12, topPos + H - 12, leftPos + 12 + filled, topPos + H - 7, ELEC[1]);
    }

    private static void icon(GuiGraphicsExtractor g, int k, int x, int y) {
        for (int r = 0; r < ICON; r++) for (int c = 0; c < ICON; c++) {
            char ch = ICONS[k][r].charAt(c);
            if (ch == '.') continue;
            int[] m = COLORS[k][Character.toLowerCase(ch) == 'a' ? 0 : 1];
            int col = Character.isUpperCase(ch) ? m[1] : m[0];
            g.fill(x + c * SCALE, y + r * SCALE, x + (c + 1) * SCALE, y + (r + 1) * SCALE, col);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {}   // 文字は使わない（アイコンだけ）

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        for (int i = 0; i < ICONS.length; i++) {
            if (e.x() < iconX(i) || e.x() >= iconX(i) + ICON * SCALE || e.y() < iconY(i) || e.y() >= iconY(i) + ICON * SCALE) continue;
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
            if (i == 0 && minecraft != null && minecraft.player != null && SmartphoneItem.energy(PhoneMenu.phone(minecraft.player)) >= SmartphoneItem.COST_J[0])
                minecraft.gui.setScreen(new CalculatorScreen());   // 電卓のアプリ＝今の電卓の画面
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }
}
