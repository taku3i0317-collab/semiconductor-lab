package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.litho.ChipModel;
import com.taku3.semiconductorlab.litho.Circuit;
import com.taku3.semiconductorlab.litho.Discoveries;
import com.taku3.semiconductorlab.litho.Netlist;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.taku3.semiconductorlab.block.ProbeStationBlockEntity.*;

/**
 * 検査台の画面（O-002）。左上＝チップ（型の色・金属・窓、パッドは ふちの金属：青＝0 V・橙＝5 V・灰＝つながない。クリックで切りかえ）、
 * その上に電流の明るさ（黄色。1 µA〜10 mA を対数で）。右＝さわっているパッドの電圧と電流、調べたあとに見つかった発見の名前、「調べる」。
 * 電流は Circuit.solve をこの画面で計算する（サーバーと同じ式）。
 */
public class ProbeStationScreen extends MachineScreen {
    private static final int P = 0xFFE89A9A, N = 0xFF92B4EE, P_SUB = 0xFFF2C4C4, N_SUB = 0xFFC4D6F4, METAL = 0x90707078;
    private static final int[] PAD_COLOR = {0xFFA0A0A0, 0xFF3060D0, 0xFFE08020};

    private ItemStack cachedWafer = ItemStack.EMPTY;
    private Netlist nl;
    private Circuit.Solution sol;
    private int cachedPads = -1, hoverPad = -1;

    public ProbeStationScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.semiconductor-lab.probe_station.probe"),
                b -> press(BTN_PROBE)).bounds(leftPos + RIGHT_X, topPos + PROBE_Y, 78, 14).build());
    }

    private void press(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    /** ウェハーかパッドが変わったら回路を計算し直す */
    private void refresh() {
        ItemStack w = menu.machineItem(SLOT_WAFER);
        int padsKey = 0;
        for (int i = 0; i < MAX_PADS; i++) padsKey = padsKey * 3 + menu.get(i);
        if (!ItemStack.isSameItemSameComponents(w, cachedWafer)) {
            cachedWafer = w.copy();
            nl = netlistOf(w);
            cachedPads = -1;
        }
        if (nl != null && padsKey != cachedPads) {
            cachedPads = padsKey;
            sol = Circuit.solve(nl, drive(nl, menu::get));
        }
        if (nl == null) sol = null;
    }

    private int cs() { return nl == null ? 10 : CHIP_W / nl.model.size; }

    /** マウスの下のパッドの番号（なければ −1） */
    private int padAt(double mx, double my) {
        if (nl == null) return -1;
        int c = cs(), x = (int) Math.floor((mx - leftPos - CHIP_X) / c), y = (int) Math.floor((my - topPos - CHIP_Y) / c);
        if (!nl.model.inside(x, y)) return -1;
        int net = nl.netOf[y * nl.model.size + x];
        List<Netlist.Net> pads = nl.pads();
        for (int i = 0; i < pads.size(); i++) if (pads.get(i).id() == net) return i;
        return -1;
    }

    @Override public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        refresh();
        int p = padAt(e.x(), e.y());
        if (p >= 0 && p < MAX_PADS && e.button() == 0) { press(p); return true; }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        refresh();
        int gx = leftPos + CHIP_X, gy = topPos + CHIP_Y;
        g.fill(gx, gy, gx + CHIP_W, gy + CHIP_W, 0xFFDADADA);
        hoverPad = padAt(mouseX, mouseY);
        if (nl == null) return;
        ChipModel m = nl.model;
        int c = cs(), n = m.size;
        List<Netlist.Net> pads = nl.pads();
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            int x0 = gx + x * c, y0 = gy + y * c;
            boolean sub = m.level(x, y) == 0;
            g.fill(x0, y0, x0 + c, y0 + c, m.type(x, y) == 'p' ? (sub ? P_SUB : P) : (sub ? N_SUB : N));
            if (m.metal(x, y)) g.fill(x0, y0, x0 + c, y0 + c, METAL);
            if (m.contact(x, y)) g.fill(x0 + c / 2 - 1, y0 + c / 2 - 1, x0 + c / 2 + 1, y0 + c / 2 + 1, 0xFF202020);
            if (sol != null) {   // 電流の明るさ（対数）
                double a = sol.cellAmps()[y * n + x];
                if (a > 1e-6) {
                    int alpha = (int) Math.min(200, 50 * (Math.log10(a) + 6) + 30);
                    g.fill(x0 + 1, y0 + 1, x0 + c - 1, y0 + c - 1, (alpha << 24) | 0xFFE040);
                }
            }
            int net = nl.netOf[y * n + x];
            for (int i = 0; i < pads.size() && i < MAX_PADS; i++)
                if (pads.get(i).id() == net && m.edge(x, y)) g.outline(x0, y0, c, c, PAD_COLOR[menu.get(i)]);
        }
        // 下の見本：0 V・5 V・つながない、電流
        int ly = gy + CHIP_W + 6;
        for (int i = 0; i < 3; i++) g.fill(gx + i * 12, ly, gx + i * 12 + 8, ly + 8, PAD_COLOR[i]);
        g.fill(gx + 40, ly, gx + 48, ly + 8, 0xC0FFE040);
    }

    private static String amps(double a) {
        double v = Math.abs(a);
        if (v >= 1e-3) return String.format("%.2f mA", a * 1e3);
        if (v >= 1e-6) return String.format("%.1f µA", a * 1e6);
        return "0";
    }

    @Override
    protected Component text(String id) {
        String k = "gui.semiconductor-lab.probe_station.";
        refresh();
        switch (id) {
            case "pad", "amps" -> {
                if (nl == null || hoverPad < 0) return null;
                int st = menu.get(hoverPad);
                if (id.equals("pad")) return Component.translatable(k + "pad", hoverPad + 1, st == VCC ? "5 V" : st == GROUND ? "0 V" : "—");
                Double a = sol == null ? null : sol.padAmps().get(nl.pads().get(hoverPad).id());
                return a == null ? null : Component.translatable(k + "amps", amps(a));
            }
            case "hint" -> {
                if (nl == null) return Component.translatable(k + "need_wafer");
                if (menu.get(DATA_PROBED) != 0 && menu.get(DATA_FOUND) == 0) return Component.translatable(k + "nothing");
                return Component.translatable(k + "set_pads");
            }
            default -> {
                if (!id.startsWith("found") || menu.get(DATA_PROBED) == 0) return null;
                int want = id.charAt(5) - '0', seen = 0;
                var all = Discoveries.all();
                for (int i = 0; i < all.size(); i++)
                    if ((menu.get(DATA_FOUND) >> i & 1) != 0 && seen++ == want) return Component.translatable(all.get(i).langKey());
                return null;
            }
        }
    }
}
