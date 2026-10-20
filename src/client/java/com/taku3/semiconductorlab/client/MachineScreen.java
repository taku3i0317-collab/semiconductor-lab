package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 機械の画面の土台（IMP-28）：背景 textures/gui/&lt;機械の名前&gt;.png を貼り、題名を真ん中に置く。
 * 機械ごとの画面はこれを継承して、extractBackground（バー）と extractLabels（文字）だけ書く。
 * 何も書かなくてよい機械は {@link SimpleMachineScreen}（電力・進み具合・状態の3行）を使う。
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    protected static final int TEXT_COLOR = 0xFF404040;
    /** バーの空いている部分の色 */
    protected static final int BAR_EMPTY = 0xFF3A3A40;
    private final Identifier bg;

    public MachineScreen(MachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        String tex = menu.machine().layout.texture() != null ? menu.machine().layout.texture() : menu.machine().name;
        bg = Identifier.fromNamespaceAndPath("semiconductor-lab", "textures/gui/" + tex + ".png");
    }

    @Override protected void init() { super.init(); titleLabelX = (imageWidth - font.width(title)) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, bg, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    /** 横のバー（左から伸びる）。x, y は画面の左上からの位置 */
    protected void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, int value, int total, int color) {
        int ax = leftPos + x, ay = topPos + y;
        g.fill(ax, ay, ax + w, ay + h, BAR_EMPTY);
        int fw = value * w / Math.max(1, total);
        if (fw > 0) g.fill(ax, ay, ax + fw, ay + h, color);
    }

    /** 縦のバー（下から伸びる。炎・残量など） */
    protected void vbar(GuiGraphicsExtractor g, int x, int y, int w, int h, int value, int total, int color) {
        int ax = leftPos + x, ay = topPos + y;
        g.fill(ax, ay, ax + w, ay + h, BAR_EMPTY);
        int fh = value * h / Math.max(1, total);
        if (fh > 0) g.fill(ax, ay + h - fh, ax + w, ay + h, color);
    }

    /** 文字1行（extractLabels の中で使う。位置は画面の左上から） */
    protected void text(GuiGraphicsExtractor g, Component c, int x, int y) { text(g, c, x, y, TEXT_COLOR); }

    /** 文字1行（色つき） */
    protected void text(GuiGraphicsExtractor g, Component c, int x, int y, int color) {
        g.text(font, c, x, y, color, false);
        if (recording != null) recording.add(new TextDraw(c, x, y, font.width(c)));
    }

    /** 文字1行（翻訳しない文字。例：「SO2」） */
    protected void text(GuiGraphicsExtractor g, String s, int x, int y) { text(g, Component.literal(s), x, y, TEXT_COLOR); }

    // ===== 画面の自動チェック用（IMP-30。クライアントのテスト ScreenChecks だけが使う。ふだんは null で何もしない） =====

    /** 描いた文字1つ：位置（画面の左上から）と幅（px） */
    public record TextDraw(Component text, int x, int y, int width) {}

    private static java.util.List<TextDraw> recording;

    /** 文字を描くたびに list に足す（null で止める） */
    public static void recordTextInto(java.util.List<TextDraw> list) { recording = list; }

    public int panelLeft() { return leftPos; }
    public int panelTop() { return topPos; }
    public int panelWidth() { return imageWidth; }
    public int panelHeight() { return imageHeight; }
    public net.minecraft.client.gui.Font font() { return font; }
}
