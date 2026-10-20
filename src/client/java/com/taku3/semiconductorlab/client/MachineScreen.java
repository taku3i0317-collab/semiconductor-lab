package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.machine.TextWrap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

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

    /** 題名の倍率。画面の幅に入らない長い題名（例：Reverberatory Furnace (Roaster)）は縮めて自分で描く */
    private float titleScale = 1f;

    @Override protected void init() {
        super.init();
        int tw = font.width(title);
        titleScale = Math.min(1f, (imageWidth - 8) / (float) tw);
        titleLabelX = (imageWidth - font.width(title)) / 2;
        if (titleScale < 1f) titleLabelY = -10_000;   // バニラの題名は描かない（下で縮めて描く）
    }

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

    // ===== 文字（IMP-30）：置き場所・幅・行数はレイアウトのデータ（MachineLayout.text …）。画面のコードは「今どの文を出すか」だけを返す =====

    /** 置き場所 id に今出す文。null なら何も出さない（名札「=…」は自動で出る）。機械ごとの画面が上書きする */
    protected Component text(String id) { return null; }

    /** 警告の色（足りない電力など）。text(id) で返す文に付ける */
    protected static Component warn(Component c) { return c.copy().withStyle(s -> s.withColor(0xB02020)); }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);   // 題名と「持ち物」
        if (titleScale < 1f) {
            var pose = g.pose();
            pose.pushMatrix();
            pose.translate(4, 6 + (9 - 9 * titleScale) / 2);
            pose.scale(titleScale, titleScale);
            g.text(font, title, 0, 0, TEXT_COLOR, false);
            pose.popMatrix();
        }
        for (MachineLayout.TextDef d : menu.machine().layout.texts()) {
            Component c = text(d.id());
            if (c == null && d.keys().size() == 1 && d.keys().get(0).startsWith("=")) c = Component.literal(d.keys().get(0).substring(1));
            if (c != null) drawText(g, d, c);
        }
    }

    /** 箱の中に描く：TextWrap で幅に折り返す（自動チェックと同じ計算）。反応式の行は1行に縮める。入らない分は念のため縮めて入れる */
    private void drawText(GuiGraphicsExtractor g, MachineLayout.TextDef d, Component c) {
        var fit = TextWrap.fit(c.getString(), d.width(), d.lines(), true, font::width);   // データどおりなら縮めずに入る（自動チェックで確かめる）。念のため入らない分は縮める
        float scale = fit.scale();
        List<String> lines = fit.lines();
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(d.x(), d.y());
        pose.scale(scale, scale);
        int boxW = Math.round(d.width() / scale);
        for (int i = 0; i < lines.size(); i++) {
            Component line = Component.literal(lines.get(i)).setStyle(c.getStyle());
            int lw = font.width(line);
            int lx = switch (d.align()) { case LEFT -> 0; case CENTER -> (boxW - lw) / 2; case RIGHT -> boxW - lw; };
            g.text(font, line, lx, i * MachineLayout.LINE, TEXT_COLOR, false);
        }
        pose.popMatrix();
    }

    // ===== 画面の自動チェック（クライアント側のスクリーンショット比較 ScreenChecks）用 =====

    public int panelLeft() { return leftPos; }
    public int panelTop() { return topPos; }
    public int panelWidth() { return imageWidth; }
    public int panelHeight() { return imageHeight; }
}