package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.item.CalculatorLogic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 電卓の画面 [B-08]（S-010）：上にニキシー管4本（橙色に光る数字。文字ではなく線で描く）、下にキー（0〜9・＋・−・＝・C）。
 * 計算は CalculatorLogic（サーバーとは通信しない。電卓は手元の道具）。
 */
public class CalculatorScreen extends Screen {
    private static final int W = 128, H = 150, TUBE_W = 22, TUBE_H = 34;
    private static final int GLOW = 0xFFFF8A30, DIM = 0x30FF8A30, GLASS = 0xFF20140C, BODY = 0xFF5A5650;
    private static final String[] KEYS = {"7", "8", "9", "C", "4", "5", "6", "-", "1", "2", "3", "+", "0", "="};
    private final CalculatorLogic calc = new CalculatorLogic();
    private int left, top;

    public CalculatorScreen() { super(Component.translatable("item.semiconductor-lab.calculator")); }

    @Override protected void init() {
        left = (width - W) / 2; top = (height - H) / 2;
        for (int i = 0; i < KEYS.length; i++) {
            final char k = KEYS[i].charAt(0);
            int col = i % 4, row = i / 4, w = (i == KEYS.length - 1) ? 52 : 24;   // ＝ は横に長い
            int x = left + 10 + col * 28, y = top + 52 + row * 22;
            addRenderableWidget(Button.builder(Component.literal(KEYS[i].equals("-") ? "−" : KEYS[i]), b -> calc.press(k)).bounds(x, y, w, 18).build());
        }
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.fill(left, top, left + W, top + H, BODY);
        String d = calc.display();
        for (int t = 0; t < CalculatorLogic.DIGITS; t++) {   // 右づめで4本
            int x = left + 10 + t * (TUBE_W + 5), y = top + 10;
            g.fill(x, y, x + TUBE_W, y + TUBE_H, GLASS);
            int idx = d.length() - (CalculatorLogic.DIGITS - t);
            char c = idx >= 0 ? d.charAt(idx) : ' ';
            tube(g, x + 4, y + 4, TUBE_W - 8, TUBE_H - 8, c);
        }
    }

    /** ニキシー管1本：中の数字の形（ここでは7つの線で近似 ⚠️。本物は数字の形の陰極が10枚重なっている） */
    private static void tube(GuiGraphicsExtractor g, int x, int y, int w, int h, char c) {
        String on = switch (c) {
            case '0' -> "abcdef"; case '1' -> "bc"; case '2' -> "abged"; case '3' -> "abgcd"; case '4' -> "fgbc";
            case '5' -> "afgcd"; case '6' -> "afgedc"; case '7' -> "abc"; case '8' -> "abcdefg"; case '9' -> "abcdfg";
            case '-' -> "g"; case 'E' -> "afged"; default -> "";
        };
        int m = h / 2, t = 2;
        int[][] seg = {{x, y, w, t}, {x + w - t, y, t, m}, {x + w - t, y + m, t, h - m}, {x, y + h - t, w, t}, {x, y + m, t, h - m}, {x, y, t, m}, {x, y + m - 1, w, t}};
        for (int i = 0; i < 7; i++) {
            int[] s = seg[i];
            g.fill(s[0], s[1], s[0] + s[2], s[1] + s[3], on.indexOf((char) ('a' + i)) >= 0 ? GLOW : DIM);
        }
    }
}
