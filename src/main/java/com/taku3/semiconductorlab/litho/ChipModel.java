package com.taku3.semiconductorlab.litho;

import java.util.List;

/**
 * 完成したチップの姿（LITHO_DESIGN §6.1）：マスごとの型（板 → 拡散を順に上書き）・どの拡散で決まったか・窓・金属。
 * of(state) は工程を終えたウェハーから、of(design) は「全部うまくいったら」の予想（製図台の予想図・断面図）。
 */
public final class ChipModel {
    public final int size;
    /** 板の型 'p' / 'n' */
    public final char substrate;
    /** マスの型 'p' / 'n' */
    private final char[] type;
    /** マスの型を決めた拡散の番号（0＝板、1＝1回目の拡散…）。あとの拡散ほど濃い */
    private final int[] level;
    private final boolean[] contact, metal;

    private ChipModel(int size, char substrate) {
        this.size = size;
        this.substrate = substrate;
        type = new char[size * size];
        level = new int[size * size];
        contact = new boolean[size * size];
        metal = new boolean[size * size];
        java.util.Arrays.fill(type, substrate);
    }

    private void apply(LayerKind k, BitGrid g, int diffusionNo) {
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            if (!g.get(x, y)) continue;
            int i = y * size + x;
            if (k.isDiffusion()) { type[i] = k.dopes; level[i] = diffusionNo; }
            else if (k == LayerKind.CONTACT) contact[i] = true;
            else if (k == LayerKind.METAL) metal[i] = true;
        }
    }

    private static ChipModel build(int size, String substrate, List<? extends Object> layers) {
        ChipModel m = new ChipModel(size, substrate.isEmpty() ? 'p' : substrate.charAt(0));
        int d = 0;
        for (Object o : layers) {
            LayerKind k; BitGrid g;
            if (o instanceof ChipState.Applied a) { k = a.kind(); g = a.cells(); }
            else { ChipDesign.Layer l = (ChipDesign.Layer) o; k = l.kind(); g = l.cells(); }
            if (k.isDiffusion()) d++;
            m.apply(k, g, d);
        }
        return m;
    }

    /** 工程を終えたウェハーの中身から（まだ露光していなければ null） */
    public static ChipModel of(ChipState s) { return s.sized() ? build(s.size(), s.substrate(), s.done()) : null; }

    /** 設計図から（板の型は substrate） */
    public static ChipModel of(ChipDesign d, String substrate) { return build(d.size(), substrate, d.layers()); }

    public boolean inside(int x, int y) { return x >= 0 && y >= 0 && x < size && y < size; }
    public char type(int x, int y) { return type[y * size + x]; }
    public int level(int x, int y) { return level[y * size + x]; }
    public boolean contact(int x, int y) { return contact[y * size + x]; }
    public boolean metal(int x, int y) { return metal[y * size + x]; }
    /** 金属がシリコンにさわっている（窓の上に金属がある）マス */
    public boolean connected(int x, int y) { return contact(x, y) && metal(x, y); }
    /** いちばん外のマス（パッドを置ける所） */
    public boolean edge(int x, int y) { return x == 0 || y == 0 || x == size - 1 || y == size - 1; }

    /**
     * 面抵抗（Ω/□、ゲームの値 ⚠️ LITHO_DESIGN §6.2）：板はうすい（高い）、あとの拡散ほど濃い（低い）
     */
    public double sheetOhms(int x, int y) {
        return switch (level(x, y)) { case 0 -> 5000; case 1 -> 1000; case 2 -> 200; default -> 10; };
    }
}
