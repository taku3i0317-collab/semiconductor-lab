package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.litho.BitGrid;
import com.taku3.semiconductorlab.litho.ChipDesign;
import com.taku3.semiconductorlab.litho.LayerKind;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 製図台（O-002、docs/LITHO_DESIGN §7）：方眼紙の設計図（layout_sheet、成分 chip_design）に層ごとの模様を描き、
 * 層を1枚ずつ原図（rubylith、成分 mask）に切り出す。電気なし。
 * ⚠️ 近似（INBOX S-001）：材料なしで方眼紙・原図が出る。本物は大きな方眼紙に描き、赤いフィルム（ルビリス）を切って原図にした。
 * <p>
 * 描く・元に戻すはサーバーで（ボタン＝clickMenuButton）。設計図はスロットのアイテムなので、画面には自動で同期される。
 * ボタンの番号：{@link #BTN_NEW} など、塗る＝{@link #paintId}。
 */
public class DraftingTableBlockEntity extends BlockEntity implements MachineBE {
    public static final int SLOT_SHEET = 0, SLOT_OUT = 1, SLOT_STAMP = 2, SLOT_COUNT = 3;
    public static final int DATA_UNDO = 0, DATA_REDO = 1, DATA_COUNT = 2;
    /** ボタン：新しい方眼紙・元に戻す・やり直す・ひと筆の始まり（元に戻す単位）、方眼の大きさ（+i）、原図を切る（+層）、層を消す（+層） */
    public static final int BTN_NEW = 1, BTN_UNDO = 2, BTN_REDO = 3, BTN_STROKE = 4, BTN_PROCESS = 5, BTN_SIZE = 10, BTN_CUT = 100, BTN_CLEAR = 200;
    /** ハンコを押す（v0.3b）：300000 ＋ マス（押す所の左上） */
    public static final int BTN_STAMP = 300_000;
    /** 選べる方眼の大きさ（露光の方法ごと、ExposureTool と同じ。v0.3 の密着露光は 8、より細かい物はその方法ができてから焼ける） */
    public static final int[] SIZES = java.util.Arrays.stream(com.taku3.semiconductorlab.litho.ExposureTool.values()).mapToInt(t -> t.grid).toArray();
    private static final int MAX_HISTORY = 64;

    /** 塗る（on）・消す（off）のボタンの番号：100000×(1 か 2) ＋ 10000×層 ＋ マス */
    public static int paintId(boolean on, int layer, int cell) { return (on ? 1 : 2) * 100_000 + layer * 10_000 + cell; }

    /** バイポーラの層の役目（三重拡散＋窓＋アルミ、INBOX S-001）。lang litho.semiconductor-lab.role.&lt;役目&gt; */
    public static final String[] ROLES = {"island", "base", "emitter", "contact", "metal", "via", "metal2"};
    /** MOS の層の役目（S-011 Q21-3 案A：ソースとドレイン・ゲート酸化膜・窓・金属、2層目の金属つきなら＋VIA・2層目） */
    public static final String[] MOS_ROLES = {"source_drain", "gate_ox", "contact", "metal", "via", "metal2"};
    /** CMOS の層の役目（[9-24]）。1970年代の CMOS は金属1層（2層目なし） */
    public static final String[] CMOS_ROLES = {"n_well", "p_source_drain", "n_source_drain", "gate_ox", "contact", "metal"};
    /** 層の数のいちばん多い場合（バイポーラ＋2層目） */
    public static final int MAX_LAYERS = ROLES.length;

    /** 層 i の役目の名前（設計図の工程の種類で決まる） */
    public static String role(ChipDesign d, int i) {
        String[] r = d.cmos() ? CMOS_ROLES : d.mos() ? MOS_ROLES : ROLES;
        return i < r.length ? r[i] : "metal";
    }

    public static ChipDesign blank(int size) { return blank(size, ChipDesign.BIPOLAR, false); }

    /** 白紙（工程の種類と2層目の金属で層の並びが決まる、S-011） */
    public static ChipDesign blank(int size, String process, boolean twoMetal) {
        BitGrid e = BitGrid.empty(size);
        List<ChipDesign.Layer> l = new java.util.ArrayList<>();
        for (LayerKind k : ChipDesign.kindsFor(process, twoMetal)) l.add(new ChipDesign.Layer(k, e));
        return new ChipDesign(size, "", l, List.of(), process);
    }

    /** 画面の中の位置（画面のクラス DraftingTableScreen も同じ数字を使う） */
    public static final int H = 256, GRID_X = 8, GRID_Y = 18, GRID_W = 80, LAYER_X = 94, LAYER_Y = 18, LAYER_ROW = 9,
            BTN_Y = 82, SECTION_X = 8, SECTION_Y = 102, SECTION_H = 24, SHEET_X = 96, SHEET_Y = 102, OUT_X = 152, OUT_Y = 102,
            STAMP_X = 152, STAMP_Y = 124, SYMBOL_BTN_X = 96, SYMBOL_BTN_Y = 124;

    public static final MachineLayout LAYOUT;
    static {
        var b = MachineLayout.builder(DATA_COUNT).height(H)
                .in(SLOT_SHEET, SHEET_X, SHEET_Y, s -> s.is(Semiconductor.LAYOUT_SHEET))
                .out(SLOT_OUT, OUT_X, OUT_Y)
                .in(SLOT_STAMP, STAMP_X, STAMP_Y, s -> s.is(Semiconductor.STAMP), 1)   // ハンコ（v0.3b）：入れている間は左クリックで押す
                .area("symbol_button", SYMBOL_BTN_X, SYMBOL_BTN_Y, 16, 14)
                .area("grid", GRID_X, GRID_Y, GRID_W, GRID_W)
                .area("swatch", LAYER_X, LAYER_Y, 8, LAYER_ROW * MAX_LAYERS - 1)
                .area("buttons", LAYER_X, BTN_Y, 78, 14)
                .area("section", SECTION_X, SECTION_Y, GRID_W, SECTION_H);
        java.util.List<String> roleKeys = java.util.stream.Stream.of(ROLES, MOS_ROLES, CMOS_ROLES).flatMap(java.util.Arrays::stream).distinct().map(r -> "litho.semiconductor-lab.role." + r).toList();
        for (int i = 0; i < MAX_LAYERS; i++)
            b.lazyText("layer" + i, LAYER_X + 10, LAYER_Y + LAYER_ROW * i, 68, 1, () -> roleKeys).fit();
        LAYOUT = b.label("arrow", SHEET_X + 22, SHEET_Y + 4, 28, "→")
                .text("stamp_label", STAMP_X - 34, STAMP_Y + 4, 32, 1, "gui.semiconductor-lab.drafting_table.stamp_slot").fit()
                .text("hint", LAYER_X, 144, 78, 1, MachineLayout.keys("gui.semiconductor-lab.drafting_table.", "need_sheet", "draw", "zoom", "out_full", "stamp")).fit()
                .lazyText("symbol", SECTION_X, SECTION_Y + SECTION_H + 4, 84, 2, () -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of("gui.semiconductor-lab.drafting_table.symbol_off", "gui.semiconductor-lab.drafting_table.symbol_on", "gui.semiconductor-lab.drafting_table.inside"),
                        com.taku3.semiconductorlab.litho.Challenge.all().stream().map(com.taku3.semiconductorlab.litho.Challenge.Def::langKey)).toList()).fit()
                .build();
    }

    private final SimpleContainer container = new SimpleContainer(SLOT_COUNT) {
        @Override public void setChanged() { super.setChanged(); DraftingTableBlockEntity.this.setChanged(); }
    };
    /** 元に戻す・やり直すの履歴（ブロックを置き直すと消える） */
    private final Deque<ChipDesign> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) { case DATA_UNDO -> undo.size(); case DATA_REDO -> redo.size(); default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    public DraftingTableBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.DRAFTING_TABLE.type, pos, state); }

    @Override public SimpleContainer getContainer() { return container; }
    @Override public ContainerData getMenuData() { return data; }

    /** 置いてある設計図（なければ null） */
    public ChipDesign design() {
        ItemStack s = container.getItem(SLOT_SHEET);
        return s.is(Semiconductor.LAYOUT_SHEET) ? s.get(ModDataComponents.CHIP_DESIGN) : null;
    }

    private void setDesign(ChipDesign d) {
        ItemStack s = container.getItem(SLOT_SHEET).copy();
        s.set(ModDataComponents.CHIP_DESIGN, d);
        container.setItem(SLOT_SHEET, s);
    }

    private void remember(ChipDesign before) {
        undo.push(before);
        while (undo.size() > MAX_HISTORY) undo.removeLast();
        redo.clear();
    }

    @Override public boolean onButton(Player player, int id) {
        if (id == BTN_NEW) {
            if (!container.getItem(SLOT_SHEET).isEmpty()) return false;
            ItemStack s = new ItemStack(Semiconductor.LAYOUT_SHEET);   // ⚠️ 材料なし（S-001）
            s.set(ModDataComponents.CHIP_DESIGN, blank(SIZES[0]));
            container.setItem(SLOT_SHEET, s);
            undo.clear(); redo.clear();
            return true;
        }
        ChipDesign d = design();
        if (d == null) return false;
        if (id == BTN_UNDO || id == BTN_REDO) {
            Deque<ChipDesign> from = id == BTN_UNDO ? undo : redo, to = id == BTN_UNDO ? redo : undo;
            if (from.isEmpty()) return false;
            to.push(d);
            setDesign(from.pop());
            return true;
        }
        if (id == BTN_STROKE) { remember(d); return true; }   // ひと筆の始まり：ここまでを1回で戻せる
        if (id == BTN_PROCESS) {   // 工程の種類を順に：バイポーラ → バイポーラ＋2層目 → MOS → MOS＋2層目 → CMOS（白紙になる、元に戻せる、S-011・[9-24]）
            int cur = d.cmos() ? 4 : (d.mos() ? 2 : 0) + (d.twoMetal() ? 1 : 0), nx = (cur + 1) % 5;
            remember(d);
            setDesign(blank(d.size(), nx == 4 ? ChipDesign.CMOS : nx >= 2 ? ChipDesign.MOS : ChipDesign.BIPOLAR, nx < 4 && nx % 2 == 1));
            return true;
        }
        if (id >= BTN_SIZE && id < BTN_SIZE + SIZES.length) {
            int size = SIZES[id - BTN_SIZE];
            if (size == d.size()) return false;
            remember(d);
            setDesign(blank(size, d.process(), d.twoMetal()));
            return true;
        }
        if (id >= BTN_CUT && id < BTN_CUT + d.layers().size()) {
            if (!container.getItem(SLOT_OUT).isEmpty()) return false;
            ItemStack r = new ItemStack(Semiconductor.RUBYLITH);   // ⚠️ 材料なし（S-001）
            r.set(ModDataComponents.MASK, d.mask(id - BTN_CUT));
            container.setItem(SLOT_OUT, r);
            return true;
        }
        if (id >= BTN_CLEAR && id < BTN_CLEAR + d.layers().size()) {
            remember(d);
            setDesign(d.withLayer(id - BTN_CLEAR, BitGrid.empty(d.size())));
            return true;
        }
        if (id >= BTN_STAMP && id < BTN_STAMP + 10_000) {   // ハンコを押す（左上のマス）
            ChipDesign st = container.getItem(SLOT_STAMP).get(ModDataComponents.CHIP_DESIGN);
            int cell = id - BTN_STAMP;
            if (st == null || cell >= d.size() * d.size() || !st.process().equals(d.process())) return false;   // 工程の種類のちがうハンコは押せない
            remember(d);
            setDesign(d.stamp(st, cell % d.size(), cell / d.size()));
            return true;
        }
        if (id >= 100_000 && id < 300_000) {
            boolean on = id < 200_000;
            int layer = id % 100_000 / 10_000, cell = id % 10_000;
            if (layer >= d.layers().size() || cell >= d.size() * d.size()) return false;
            BitGrid g = d.layers().get(layer).cells();
            int x = cell % d.size(), y = cell / d.size();
            if (g.get(x, y) == on) return false;
            setDesign(d.withLayer(layer, g.with(x, y, on)));
            return true;
        }
        return false;
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); ContainerHelper.saveAllItems(out, container.getItems()); }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        container.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, container.getItems());
    }
}
