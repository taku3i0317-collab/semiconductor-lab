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

    // ===== 文字（IMP-30：全部の機械で、枠からはみ出さないように自動で折り返す・縮める） =====

    /** 画面の内側の縁（px）と、1行の高さ（文字 9px ＋ すき間 1px） */
    public static final int EDGE = 4, LINE = 10;
    /** これより小さくはしない（読めなくなるので） */
    public static final float MIN_SCALE = 0.6f;
    /** 列（x）ごとの、もう文字を描いた一番下の y。同じ列の次の文は、ここより下へずらす（折り返しで重ならないように） */
    private final java.util.Map<Integer, Integer> columnBottom = new java.util.HashMap<>();

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        columnBottom.clear();
        if (recording != null) recording.clear();   // 自動チェックは最後の1枚の分だけを見る
        super.extractLabels(g, mouseX, mouseY);
    }

    /** 文字（extractLabels の中で使う。位置は画面の左上から）。長いと枠の幅で折り返す */
    protected void text(GuiGraphicsExtractor g, Component c, int x, int y) { text(g, c, x, y, TEXT_COLOR); }

    /**
     * 文字（色つき）。
     * - 反応式（lang のキーが ….recipe）は専用の行＝「持ち物」と同じ高さの右寄せに描き、入らなければ縮める
     * - それ以外は、x から枠の右の縁までの幅で折り返す。同じ列の前の文が折り返して伸びていたら、その下へずらす
     * - それでも下の縁（反応式の行、なければ持ち物の段）を越えるなら、全体を縮める（MIN_SCALE まで）
     */
    protected void text(GuiGraphicsExtractor g, Component c, int x, int y, int color) {
        if (isFormula(c)) { formula(g, c, color); return; }
        int room = Math.max(8, imageWidth - EDGE - x);
        int top = Math.max(y, columnBottom.getOrDefault(x, y));
        int limit = (hasFormula() ? inventoryLabelY : inventoryLabelY + 10) - 1;
        float scale = 1f;
        java.util.List<net.minecraft.util.FormattedCharSequence> lines;
        while (true) {
            lines = font.split(c, (int) (room / scale));
            float h = ((lines.size() - 1) * LINE + font.lineHeight) * scale;
            if (top + h <= limit + 1 || scale <= MIN_SCALE + 1e-4f) break;
            scale = Math.max(MIN_SCALE, scale - 0.1f);
        }
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, top);
        pose.scale(scale, scale);
        for (int i = 0; i < lines.size(); i++) {
            g.text(font, lines.get(i), 0, i * LINE, color, false);
            if (recording != null)
                recording.add(new TextDraw(Component.literal(plain(lines.get(i))), x, top + Math.round(i * LINE * scale),
                        Math.round(font.width(lines.get(i)) * scale), Math.round(font.lineHeight * scale), keyOf(c), scale, false));
        }
        pose.popMatrix();
        columnBottom.put(x, top + Math.round(lines.size() * LINE * scale));
    }

    /** 文字（翻訳しない文字。例：「SO2」） */
    protected void text(GuiGraphicsExtractor g, String s, int x, int y) { text(g, Component.literal(s), x, y, TEXT_COLOR); }

    /** 反応式の行：「持ち物」の右、画面の右の縁に寄せる。入らなければ縮める */
    private void formula(GuiGraphicsExtractor g, Component c, int color) {
        int right = imageWidth - 8, left = inventoryLabelX + font.width(playerInventoryTitle) + 6;
        int w = font.width(c);
        float scale = Math.min(1f, (right - left) / (float) w);
        int fx = Math.round(right - w * scale), fy = inventoryLabelY + Math.round((font.lineHeight - font.lineHeight * scale) / 2);
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(fx, fy);
        pose.scale(scale, scale);
        g.text(font, c, 0, 0, color, false);
        pose.popMatrix();
        if (recording != null) recording.add(new TextDraw(c, fx, fy, Math.round(w * scale), Math.round(font.lineHeight * scale), keyOf(c), scale, true));
    }

    /** 反応式か（lang のキーが gui.….recipe） */
    private static boolean isFormula(Component c) { String k = keyOf(c); return k != null && k.endsWith(".recipe"); }
    /** この機械の画面に反応式の行があるか（その分、ほかの文は上に収める） */
    private boolean hasFormula() { return net.minecraft.locale.Language.getInstance().has("gui.semiconductor-lab." + menu.machine().name + ".recipe"); }
    private static String keyOf(Component c) { return c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc ? tc.getKey() : null; }
    private static String plain(net.minecraft.util.FormattedCharSequence s) {
        StringBuilder b = new StringBuilder();
        s.accept((i, style, cp) -> { b.appendCodePoint(cp); return true; });
        return b.toString();
    }

    // ===== 画面の自動チェック用（IMP-30。クライアントのテスト ScreenChecks だけが使う。ふだんは null で何もしない） =====

    /** 描いた文字1行：位置・幅・高さ（画面の左上から、縮めた後の px）、元の lang のキー、縮めた倍率、反応式の行か */
    public record TextDraw(Component text, int x, int y, int width, int height, String key, float scale, boolean formula) {}

    private static java.util.List<TextDraw> recording;

    /** 文字を描くたびに list に足す（null で止める） */
    public static void recordTextInto(java.util.List<TextDraw> list) { recording = list; }

    public int panelLeft() { return leftPos; }
    public int panelTop() { return topPos; }
    public int panelWidth() { return imageWidth; }
    public int panelHeight() { return imageHeight; }
    public net.minecraft.client.gui.Font font() { return font; }
}
