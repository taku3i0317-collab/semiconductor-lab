package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.litho.ChipDesign;
import com.taku3.semiconductorlab.litho.ChipModel;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.DraftingTableBlockEntity.*;

/**
 * 製図台の画面（O-002、LITHO_DESIGN §7）。
 * 左上＝方眼（いまの層を左クリックで塗る・右クリックで消す。下には完成の予想の色：p＝赤・n＝青・金属＝灰・窓＝黒い点）、
 * 右＝層の一覧（クリックで選ぶ）とボタン（元に戻す・やり直す・原図を切る・層を消す・新しい方眼紙・方眼の大きさ）、
 * 左下＝断面図（マウスのある行を横から見た図：上から 金属・酸化膜・シリコン、拡散の深さ）。
 * 正解の名前は出さない（判定は検査台）。描く・戻すはサーバー（ボタン）、設計図はスロットのアイテムで同期される。
 */
public class DraftingTableScreen extends MachineScreen {
    private static final int P = 0xFFE89A9A, N = 0xFF92B4EE, P_SUB = 0xFFF2C4C4, N_SUB = 0xFFC4D6F4, METAL = 0x90707078, OXIDE = 0xFFF4F4F4,
            HILITE = 0xFFFFD030, GRIDLINE = 0x30000000;
    private static final int[] SWATCH = {N, P, N, 0xFF303030, 0xFF8A8A92, 0xFF4040A0, 0xFF506090};

    /** 層の種類の色見本 */
    private static int swatch(com.taku3.semiconductorlab.litho.LayerKind k) {
        return switch (k) {
            case DIFF_N -> N; case DIFF_P -> P; case CONTACT -> 0xFF303030; case METAL -> 0xFF8A8A92;
            case GATE_OX -> 0xFF60A060; case VIA -> 0xFF4040A0; case METAL2 -> 0xFF506090; case FIN -> 0xFFB0B0C0;   // フィン（[9-37]）はシリコンの灰色
        };
    }

    private int layer = 0, sectionRow = 0, lastCell = -1;
    private boolean painting, paintOn;
    /** 見え方（O-006）：1マスの px（0＝全部が入る大きさ）・左上に見えているマス・中ボタンで動かしている途中 */
    private int zoom = 0, ox = 0, oy = 0, lastSize = 0;
    private boolean panning;
    private double panX, panY;
    /** 拡大の上限（1マスの px） */
    private static final int MAX_ZOOM = 16;
    /** 記号で見せるか（v0.3b）・中身をのぞいている途中のハンコ（null＝自分の設計図）・マウスの下の記号 */
    private boolean symbols;
    private final java.util.Deque<ChipDesign> inside = new java.util.ArrayDeque<>();
    private com.taku3.semiconductorlab.litho.Part hoverPart;

    public DraftingTableScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    /** 見せる設計図：中身をのぞいていればそのハンコ、ふだんは置いてある設計図 */
    private ChipDesign design() { return inside.isEmpty() ? sheet() : inside.peek(); }
    private ChipDesign sheet() { return menu.machineItem(SLOT_SHEET).get(ModDataComponents.CHIP_DESIGN); }
    private boolean stamping() { return inside.isEmpty() && menu.machineItem(SLOT_STAMP).has(ModDataComponents.CHIP_DESIGN); }

    /** マス (x, y) を含む、いちばん外側の押したハンコ（なければ null） */
    private static com.taku3.semiconductorlab.litho.Part partAt(ChipDesign d, int x, int y) {
        com.taku3.semiconductorlab.litho.Part hit = null;
        for (var p : d.parts()) if (x >= p.x() && y >= p.y() && x < p.x() + p.stamp().size() && y < p.y() + p.stamp().size()) hit = p;
        return hit;
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override protected void init() {
        super.init();
        String[] labels = {"←", "→", "✂", "×", "+", "#", "P"};   // P＝工程の種類（バイポーラ／MOS・2層目、S-011）
        for (int i = 0; i < labels.length; i++) {
            final int k = i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> button(k)).bounds(leftPos + LAYER_X + 11 * i, topPos + BTN_Y, 10, 14).build());
        }
        // 記号の切りかえ（v0.3b）：押したハンコを記号（色の箱）で見せる。中身をのぞいているときは1段もどる
        addRenderableWidget(Button.builder(Component.literal("▣"), b -> { if (!inside.isEmpty()) inside.pop(); else symbols = !symbols; })
                .bounds(leftPos + SYMBOL_BTN_X, topPos + SYMBOL_BTN_Y, 16, 14).build());
    }

    private void button(int k) {
        ChipDesign d = design();
        switch (k) {
            case 0 -> press(BTN_UNDO);
            case 1 -> press(BTN_REDO);
            case 2 -> press(BTN_CUT + layer);
            case 3 -> press(BTN_CLEAR + layer);
            case 4 -> press(BTN_NEW);
            case 6 -> press(BTN_PROCESS);
            default -> {   // 方眼の大きさを順に（8 → 12 → 16 → 24 → … → 72、露光の方法ごと）
                if (d == null) return;
                int i = 0;
                while (i < SIZES.length && SIZES[i] != d.size()) i++;
                press(BTN_SIZE + (i + 1) % SIZES.length);
            }
        }
    }

    // ===== マウス：方眼に描く・層を選ぶ =====

    /** 全部が入る 1マスの px */
    private static int fit(ChipDesign d) { return Math.max(1, GRID_W / d.size()); }
    /** 今の 1マスの px */
    private int cs(ChipDesign d) { return zoom <= 0 ? fit(d) : Math.max(fit(d), zoom); }
    /** 見えている1辺のマス数 */
    private int view(ChipDesign d) { return Math.min(d.size(), GRID_W / cs(d)); }

    /** 大きさが変わったら見え方を元に戻し、はみ出さないように左上をおさえる */
    private void clampView(ChipDesign d) {
        if (d.size() != lastSize) { lastSize = d.size(); zoom = 0; ox = 0; oy = 0; }
        int max = d.size() - view(d);
        ox = Math.max(0, Math.min(max, ox));
        oy = Math.max(0, Math.min(max, oy));
    }

    private int cellAt(double mx, double my, ChipDesign d) {
        clampView(d);
        int cs = cs(d), v = view(d);
        int lx = (int) Math.floor((mx - leftPos - GRID_X) / cs), ly = (int) Math.floor((my - topPos - GRID_Y) / cs);
        if (lx < 0 || ly < 0 || lx >= v || ly >= v) return -1;
        return (oy + ly) * d.size() + (ox + lx);
    }

    /** ホイール：マウスの下のマスを中心に拡大・縮小（大きな方眼のとき） */
    @Override public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        ChipDesign d = design();
        if (d != null && sy != 0) {
            int c = cellAt(mx, my, d);
            if (c >= 0) {
                int old = cs(d), next = Math.max(fit(d), Math.min(MAX_ZOOM, old + (sy > 0 ? 2 : -2)));
                if (next != old) {
                    double localX = (mx - leftPos - GRID_X) / next, localY = (my - topPos - GRID_Y) / next;
                    zoom = next == fit(d) ? 0 : next;
                    ox = (int) Math.round(c % d.size() - localX);
                    oy = (int) Math.round(c / d.size() - localY);
                    clampView(d);
                }
                return true;
            }
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        ChipDesign d = design();
        double rx = e.x() - leftPos, ry = e.y() - topPos;
        int nLayers = d == null ? 0 : d.layers().size();
        if (rx >= LAYER_X && rx < LAYER_X + 78 && ry >= LAYER_Y && ry < LAYER_Y + LAYER_ROW * nLayers) {
            layer = (int) ((ry - LAYER_Y) / LAYER_ROW);
            return true;
        }
        if (d != null && cellAt(e.x(), e.y(), d) >= 0 && e.button() == 2) {   // 中ボタン：動かす
            panning = true; panX = 0; panY = 0;
            return true;
        }
        int cl = d == null ? -1 : cellAt(e.x(), e.y(), d);
        if (cl >= 0 && e.button() == 0 && (symbols || !inside.isEmpty())) {   // 記号をクリック → 中身をのぞく（v0.3b）
            var pt = partAt(d, cl % d.size(), cl / d.size());
            if (pt != null) { inside.push(pt.stamp()); zoom = 0; ox = 0; oy = 0; lastSize = 0; return true; }
        }
        if (!inside.isEmpty() && cl >= 0) return true;   // 中身は見るだけ（描けない）
        if (cl >= 0 && e.button() == 0 && stamping()) { press(BTN_STAMP + cl); return true; }   // ハンコを押す
        if (d != null && cellAt(e.x(), e.y(), d) >= 0 && (e.button() == 0 || e.button() == 1)) {
            painting = true;
            paintOn = e.button() == 0;
            lastCell = -1;
            press(BTN_STROKE);   // ここから離すまでが「1回」の元に戻す
            paint(e.x(), e.y(), d);
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        ChipDesign d = design();
        if (painting && d != null) { paint(e.x(), e.y(), d); return true; }
        if (panning && d != null) {
            int cs = cs(d);
            panX -= dx; panY -= dy;
            int mx = (int) (panX / cs), my = (int) (panY / cs);
            if (mx != 0 || my != 0) { ox += mx; oy += my; panX -= mx * cs; panY -= my * cs; clampView(d); }
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    @Override public boolean mouseReleased(MouseButtonEvent e) {
        if (panning) { panning = false; return true; }
        if (painting) { painting = false; lastCell = -1; return true; }
        return super.mouseReleased(e);
    }

    private void paint(double mx, double my, ChipDesign d) {
        int c = cellAt(mx, my, d);
        if (c < 0 || c == lastCell || layer >= d.layers().size()) return;
        lastCell = c;
        if (d.layers().get(layer).cells().get(c % d.size(), c / d.size()) != paintOn) press(paintId(paintOn, layer, c));
    }

    // ===== 描く =====

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        ChipDesign d = design();
        int nLayers = d == null ? ROLES.length - 2 : d.layers().size();
        if (layer >= nLayers) layer = 0;
        for (int i = 0; i < nLayers; i++) {   // 層の色見本と、いま選んでいる層（層の種類で色）
            int y = topPos + LAYER_Y + LAYER_ROW * i;
            g.fill(leftPos + LAYER_X, y + 1, leftPos + LAYER_X + 8, y + LAYER_ROW - 1, d == null ? SWATCH[i] : swatch(d.layers().get(i).kind()));
            if (i == layer) g.outline(leftPos + LAYER_X - 1, y, 80, LAYER_ROW, HILITE);
        }
        int gx = leftPos + GRID_X, gy = topPos + GRID_Y;
        g.fill(gx, gy, gx + GRID_W, gy + GRID_W, 0xFFDADADA);
        g.fill(leftPos + SECTION_X, topPos + SECTION_Y, leftPos + SECTION_X + GRID_W, topPos + SECTION_Y + SECTION_H, 0xFFDADADA);
        if (d == null) return;
        ChipModel m = ChipModel.of(d, "p");   // v0.3 の板は p型で予想（LITHO_DESIGN §2）
        clampView(d);
        int cs = cs(d), v = view(d);
        int hover = cellAt(mouseX, mouseY, d);
        if (hover >= 0) sectionRow = hover / d.size();
        for (int vy = 0; vy < v; vy++) for (int vx = 0; vx < v; vx++) {
            int x = ox + vx, y = oy + vy;
            int x0 = gx + vx * cs, y0 = gy + vy * cs;
            g.fill(x0, y0, x0 + cs, y0 + cs, color(m, x, y));
            if (m.gate(x, y)) g.fill(x0, y0, x0 + cs, y0 + cs, 0x6040A040);   // ゲート酸化膜（緑がかった色）
            if (m.metal(x, y)) g.fill(x0, y0, x0 + cs, y0 + cs, METAL);
            if (m.metal2(x, y)) g.fill(x0, y0, x0 + cs, y0 + cs, 0x80506090);   // 2層目の金属（青がかった灰）
            if (m.via(x, y)) { int d3 = Math.max(1, cs / 3); g.fill(x0 + cs / 2 - d3, y0 + cs / 2 - d3, x0 + cs / 2 + d3, y0 + cs / 2 + d3, 0xFF4040A0); }
            if (m.contact(x, y)) { int d2 = Math.max(1, cs / 4); g.fill(x0 + cs / 2 - d2, y0 + cs / 2 - d2, x0 + cs / 2 + d2, y0 + cs / 2 + d2, 0xFF202020); }
            boolean on = layer < d.layers().size() && d.layers().get(layer).cells().get(x, y);
            if (cs >= 3) g.outline(x0, y0, cs, cs, on ? HILITE : GRIDLINE);
            else if (on) g.fill(x0, y0, x0 + cs, y0 + cs, 0x80FFD030);   // 小さいマスは枠のかわりに色
        }
        hoverPart = null;
        if (symbols || !inside.isEmpty()) for (var pt : d.parts()) {   // 記号：押したハンコを色の箱で（名前は下の文字の欄）
            int px0 = gx + (pt.x() - ox) * cs, py0 = gy + (pt.y() - oy) * cs, w = pt.stamp().size() * cs;
            int x1 = Math.max(gx, px0), y1 = Math.max(gy, py0), x2 = Math.min(gx + v * cs, px0 + w), y2 = Math.min(gy + v * cs, py0 + w);
            if (x2 <= x1 || y2 <= y1) continue;
            int col = 0x60000000 | (pt.name().hashCode() * 0x9E3779B1 >>> 8 & 0xFFFFFF);
            g.fill(x1, y1, x2, y2, col);
            g.outline(x1, y1, x2 - x1, y2 - y1, 0xFF000000);
            if (hover >= 0 && pt == partAt(d, hover % d.size(), hover / d.size())) { hoverPart = pt; g.outline(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2, HILITE); }
        }
        if (sectionRow >= oy && sectionRow < oy + v) g.outline(gx - 1, gy + (sectionRow - oy) * cs - 1, v * cs + 2, cs + 2, 0xFF000000);   // 断面図の行
        if (v < d.size()) {   // 全体のどこを見ているか（右下の小さな枠）
            int mw = 14, mx0 = gx + GRID_W - mw - 1, my0 = gy + GRID_W - mw - 1;
            g.fill(mx0, my0, mx0 + mw, my0 + mw, 0x80000000);
            g.outline(mx0 + ox * mw / d.size(), my0 + oy * mw / d.size(), Math.max(2, v * mw / d.size()), Math.max(2, v * mw / d.size()), 0xFFFFFFFF);
        }
        section(g, d, m, sectionRow);
    }

    /** ハンコの名前（お題の id → lang。知らない名前はそのまま） */
    private static Component partName(String n) {
        String key = "litho.semiconductor-lab.challenge." + n;
        return net.minecraft.locale.Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(n.isEmpty() ? "?" : n);
    }

    /** マスの色（拡散の濃さで少し変える：板はうすい色） */
    private static int color(ChipModel m, int x, int y) {
        boolean sub = m.level(x, y) == 0;
        return m.type(x, y) == 'p' ? (sub ? P_SUB : P) : (sub ? N_SUB : N);
    }

    /** 断面図：上から 金属（4px）・酸化膜（2px、窓は抜ける）・シリコン（18px。拡散は順に深い→浅い） */
    private void section(GuiGraphicsExtractor g, ChipDesign d, ChipModel m, int row) {
        int sx = leftPos + SECTION_X, sy = topPos + SECTION_Y, cs = cs(d), v = view(d);
        int si = sy + 6, sh = SECTION_H - 6;
        int[] depth = {0, 16, 10, 5};   // 1回目・2回目・3回目の拡散の深さ（px、あとほど浅い）
        for (int vx = 0; vx < v; vx++) {   // 方眼と同じ所だけ（はみ出さない）
            int x = ox + vx, x0 = sx + vx * cs;
            g.fill(x0, si, x0 + cs, si + sh, m.substrate == 'p' ? P_SUB : N_SUB);
            int k = 0;
            for (ChipDesign.Layer l : d.layers()) {
                if (!l.kind().isDiffusion()) continue;
                k++;
                if (l.cells().get(x, row)) g.fill(x0, si, x0 + cs, si + Math.min(sh, depth[Math.min(k, 3)]), l.kind().dopes == 'p' ? P : N);
            }
            if (!m.contact(x, row)) g.fill(x0, si - 2, x0 + cs, si, OXIDE);
            if (m.metal(x, row)) g.fill(x0, sy, x0 + cs, si - 2, 0xFF8A8A92);
            if (m.metal(x, row) && m.contact(x, row)) g.fill(x0 + 1, si - 2, x0 + cs - 1, si, 0xFF8A8A92);   // 窓で金属がシリコンにさわる
        }
    }

    @Override
    protected Component text(String id) {
        String k0 = "gui.semiconductor-lab.drafting_table.";
        if (id.equals("symbol")) {
            if (!inside.isEmpty()) return Component.translatable(k0 + "inside", partName(inside.peek().name()));
            if (symbols && hoverPart != null) return partName(hoverPart.name());
            return Component.translatable(k0 + (symbols ? "symbol_on" : "symbol_off"));
        }
        if (id.equals("hint")) {
            String k = "gui.semiconductor-lab.drafting_table.";
            if (design() == null) return Component.translatable(k + "need_sheet");
            if (stamping()) return Component.translatable(k + "stamp");
            if (!menu.machineItem(SLOT_OUT).isEmpty()) return Component.translatable(k + "out_full");
            return Component.translatable(k + (design().size() > SIZES[0] ? "zoom" : "draw"));
        }
        if (id.equals("stamp_label")) return Component.translatable(k0 + "stamp_slot");
        if (id.startsWith("layer")) {
            ChipDesign d = design();
            int i = id.charAt(5) - '0';
            if (d == null ? i >= ROLES.length - 2 : i >= d.layers().size()) return null;
            return Component.translatable("litho.semiconductor-lab.role." + (d == null ? ROLES[i] : role(d, i)));
        }
        return null;
    }
}
