package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.item.PongLogic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * ゲーム機 [B-09] の画面（S-017 Q27 案A ポン型）：黒いコートに白い板2つ・玉・真ん中の点線・点数（線で描く数字、文字は使わない）。
 * 左の板はマウスの高さに合わせる。1 tick に1歩（{@link PongLogic}）。サーバーとは通信しない（手元の遊び）
 */
public class PongScreen extends Screen {
    private static final int S = 2;   // コートの1マス＝画面の2ピクセル
    private static final int BODY = 0xFF3A3632, COURT = 0xFF000000, WHITE = 0xFFF0F0F0, DIM = 0xFF404040;
    private final PongLogic game = new PongLogic();
    private int left, top, mouseY;

    public PongScreen() { super(Component.translatable("item.semiconductor-lab.game_console")); }

    @Override protected void init() { left = (width - PongLogic.W * S) / 2; top = (height - PongLogic.H * S) / 2; }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void tick() { game.step((mouseY - top) / S - PongLogic.PAD / 2); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        super.extractBackground(g, mx, my, partialTick);
        mouseY = my;
        g.fill(left - 8, top - 30, left + PongLogic.W * S + 8, top + PongLogic.H * S + 8, BODY);
        g.fill(left, top, left + PongLogic.W * S, top + PongLogic.H * S, COURT);
        for (int y = 0; y < PongLogic.H; y += 6) box(g, PongLogic.W / 2 - 1, y, 2, 3, DIM);   // 真ん中の点線
        box(g, PongLogic.PAD_X_LEFT, game.left, 2, PongLogic.PAD, WHITE);
        box(g, PongLogic.PAD_X_RIGHT, game.right, 2, PongLogic.PAD, WHITE);
        box(g, game.bx, game.by, PongLogic.BALL, PongLogic.BALL, WHITE);
        digit(g, left + PongLogic.W * S / 2 - 30, top - 26, game.scoreLeft);
        digit(g, left + PongLogic.W * S / 2 + 18, top - 26, game.scoreRight);
    }

    private void box(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) { g.fill(left + x * S, top + y * S, left + (x + w) * S, top + (y + h) * S, c); }

    /** 点数（0〜9）を7本の線で */
    private static void digit(GuiGraphicsExtractor g, int x, int y, int n) {
        String on = switch (Math.min(9, n)) {
            case 0 -> "abcdef"; case 1 -> "bc"; case 2 -> "abged"; case 3 -> "abgcd"; case 4 -> "fgbc";
            case 5 -> "afgcd"; case 6 -> "afgedc"; case 7 -> "abc"; case 8 -> "abcdefg"; default -> "abcdfg";
        };
        int w = 12, h = 20, m = h / 2, t = 2;
        int[][] seg = {{x, y, w, t}, {x + w - t, y, t, m}, {x + w - t, y + m, t, h - m}, {x, y + h - t, w, t}, {x, y + m, t, h - m}, {x, y, t, m}, {x, y + m - 1, w, t}};
        for (int i = 0; i < 7; i++) if (on.indexOf((char) ('a' + i)) >= 0) g.fill(seg[i][0], seg[i][1], seg[i][0] + seg[i][2], seg[i][1] + seg[i][3], WHITE);
    }
}
