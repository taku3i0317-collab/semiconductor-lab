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
    private static final int[] SWATCH = {N, P, N, 0xFF303030, 0xFF8A8A92};

    private int layer = 0, sectionRow = 0, lastCell = -1;
    private boolean painting, paintOn;

    public DraftingTableScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    private ChipDesign design() { return menu.machineItem(SLOT_SHEET).get(ModDataComponents.CHIP_DESIGN); }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override protected void init() {
        super.init();
        String[] labels = {"←", "→", "✂", "×", "+", "#"};
        for (int i = 0; i < labels.length; i++) {
            final int k = i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> button(k)).bounds(leftPos + LAYER_X + 13 * i, topPos + BTN_Y, 12, 14).build());
        }
    }

    private void button(int k) {
        ChipDesign d = design();
        switch (k) {
            case 0 -> press(BTN_UNDO);
            case 1 -> press(BTN_REDO);
            case 2 -> press(BTN_CUT + layer);
            case 3 -> press(BTN_CLEAR + layer);
            case 4 -> press(BTN_NEW);
            default -> {   // 方眼の大きさを順に（8 → 12 → 16）
                if (d == null) return;
                int i = 0;
                while (i < SIZES.length && SIZES[i] != d.size()) i++;
                press(BTN_SIZE + (i + 1) % SIZES.length);
            }
        }
    }

    // ===== マウス：方眼に描く・層を選ぶ =====

    private int cellAt(double mx, double my, ChipDesign d) {
        int cs = GRID_W / d.size();
        int x = (int) Math.floor((mx - leftPos - GRID_X) / cs), y = (int) Math.floor((my - topPos - GRID_Y) / cs);
        return x >= 0 && y >= 0 && x < d.size() && y < d.size() ? y * d.size() + x : -1;
    }

    @Override public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        ChipDesign d = design();
        double rx = e.x() - leftPos, ry = e.y() - topPos;
        if (rx >= LAYER_X && rx < LAYER_X + 78 && ry >= LAYER_Y && ry < LAYER_Y + LAYER_ROW * ROLES.length) {
            layer = (int) ((ry - LAYER_Y) / LAYER_ROW);
            return true;
        }
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
        return super.mouseDragged(e, dx, dy);
    }

    @Override public boolean mouseReleased(MouseButtonEvent e) {
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
        for (int i = 0; i < ROLES.length; i++) {   // 層の色見本と、いま選んでいる層
            int y = topPos + LAYER_Y + LAYER_ROW * i;
            g.fill(leftPos + LAYER_X, y + 1, leftPos + LAYER_X + 8, y + 9, SWATCH[i]);
            if (i == layer) g.outline(leftPos + LAYER_X - 1, y, 80, LAYER_ROW - 1, HILITE);
        }
        ChipDesign d = design();
        int gx = leftPos + GRID_X, gy = topPos + GRID_Y;
        g.fill(gx, gy, gx + GRID_W, gy + GRID_W, 0xFFDADADA);
        g.fill(leftPos + SECTION_X, topPos + SECTION_Y, leftPos + SECTION_X + GRID_W, topPos + SECTION_Y + SECTION_H, 0xFFDADADA);
        if (d == null) return;
        ChipModel m = ChipModel.of(d, "p");   // v0.3 の板は p型で予想（LITHO_DESIGN §2）
        int cs = GRID_W / d.size();
        int hover = cellAt(mouseX, mouseY, d);
        if (hover >= 0) sectionRow = hover / d.size();
        for (int y = 0; y < d.size(); y++) for (int x = 0; x < d.size(); x++) {
            int x0 = gx + x * cs, y0 = gy + y * cs;
            g.fill(x0, y0, x0 + cs, y0 + cs, color(m, x, y));
            if (m.metal(x, y)) g.fill(x0, y0, x0 + cs, y0 + cs, METAL);
            if (m.contact(x, y)) g.fill(x0 + cs / 2 - 1, y0 + cs / 2 - 1, x0 + cs / 2 + 1, y0 + cs / 2 + 1, 0xFF202020);
            if (layer < d.layers().size() && d.layers().get(layer).cells().get(x, y)) g.outline(x0, y0, cs, cs, HILITE);
            else g.outline(x0, y0, cs, cs, GRIDLINE);
        }
        g.outline(gx - 1, gy + sectionRow * cs - 1, GRID_W + 2, cs + 2, 0xFF000000);   // 断面図の行
        section(g, d, m, sectionRow);
    }

    /** マスの色（拡散の濃さで少し変える：板はうすい色） */
    private static int color(ChipModel m, int x, int y) {
        boolean sub = m.level(x, y) == 0;
        return m.type(x, y) == 'p' ? (sub ? P_SUB : P) : (sub ? N_SUB : N);
    }

    /** 断面図：上から 金属（4px）・酸化膜（2px、窓は抜ける）・シリコン（18px。拡散は順に深い→浅い） */
    private void section(GuiGraphicsExtractor g, ChipDesign d, ChipModel m, int row) {
        int sx = leftPos + SECTION_X, sy = topPos + SECTION_Y, cs = GRID_W / d.size();
        int si = sy + 6, sh = SECTION_H - 6;
        int[] depth = {0, 16, 10, 5};   // 1回目・2回目・3回目の拡散の深さ（px、あとほど浅い）
        for (int x = 0; x < d.size(); x++) {
            int x0 = sx + x * cs;
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
        if (id.equals("hint")) {
            String k = "gui.semiconductor-lab.drafting_table.";
            if (design() == null) return Component.translatable(k + "need_sheet");
            if (!menu.machineItem(SLOT_OUT).isEmpty()) return Component.translatable(k + "out_full");
            return Component.translatable(k + "draw");
        }
        if (id.startsWith("layer")) return Component.translatable("litho.semiconductor-lab.role." + ROLES[id.charAt(5) - '0']);
        return null;
    }
}
