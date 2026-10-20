package com.taku3.semiconductorlab.litho;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 例の設計図と、工程を順に全部通す道具（LITHO_DESIGN §6.4・§8）。GameTest と、C・E の機械のテストで使う。
 * 例は p型の板の 8×8 の NOT（RTL のインバータ。本物のマイクロロジックと同じくベースにも抵抗）：n の島（コレクタ）の中に p（ベース）、
 * その真ん中に n（エミッタ）。島の右と下に n の細い帯（コレクタの抵抗・ベースの抵抗）。ふちの4か所がパッド（上＝0 V・右上＝電源・右＝出力・下＝入力）。
 */
public final class LithoExamples {
    private LithoExamples() {}

    public static ChipDesign notGate() {
        BitGrid island = BitGrid.parse(   // n：コレクタの島（左上）と、コレクタの抵抗（右の帯）・ベースの抵抗（下の帯）
                ".#####.#",
                ".#####.#",
                ".#####.#",
                ".#####.#",
                ".#####.#",
                "........",
                "..#####.",
                "........");
        BitGrid base = BitGrid.parse(
                "........",
                "..###...",
                "..###...",
                "..###...",
                "........",
                "........",
                "........",
                "........");
        BitGrid emitter = BitGrid.parse(
                "........",
                "........",
                "...#....",
                "........",
                "........",
                "........",
                "........",
                "........");
        BitGrid contact = BitGrid.parse(
                "#......#",   // 板（0 V）・コレクタの抵抗の上の端（電源）
                "........",
                "...#....",   // エミッタ
                "..#.....",   // ベース
                ".....#.#",   // コレクタ・コレクタの抵抗の下の端（出力）
                "........",
                "..#...#.",   // ベースの抵抗の両端（ベース側・入力側）
                "........");
        BitGrid metal = BitGrid.parse(
                "####...#",   // 0 V（板・エミッタ）／ 電源
                "...#....",
                "...#....",
                "..#.....",   // ベース → ベースの抵抗
                "..#..###",   // コレクタ → 抵抗 → 出力
                "..#.....",
                "..#...#.",
                "......#.");  // 入力
        return new ChipDesign(8, "not_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, island),
                new ChipDesign.Layer(LayerKind.DIFF_P, base),
                new ChipDesign.Layer(LayerKind.DIFF_N, emitter),
                new ChipDesign.Layer(LayerKind.CONTACT, contact),
                new ChipDesign.Layer(LayerKind.METAL, metal)));
    }

    /**
     * 酸化膜つきの板（p型）に、設計図の層を順に全部の工程で焼く（塗布 → 露光 → 現像 → 削る → はがす → 拡散／Al）。
     * 途中で失敗したら、その理由を例外で投げる（テスト用）
     */
    public static ItemStack process(ItemStack oxidizedWafer, ChipDesign d, ExposureTool tool) { return process(oxidizedWafer, d, tool, ChipState.Tone.POSITIVE); }

    /** レジストの型つき（ネガ型 KPR は有機溶剤で現像） */
    public static ItemStack process(ItemStack oxidizedWafer, ChipDesign d, ExposureTool tool, ChipState.Tone tone) {
        ItemStack w = oxidizedWafer;
        for (int i = 0; i < d.layers().size(); i++) {
            LayerKind k = d.layers().get(i).kind();
            MaskData mask = d.mask(i);
            w = step(ChipOps.coat(w, tone), "coat " + i);
            w = step(ChipOps.expose(w, mask, tool), "expose " + i);
            w = step(ChipOps.develop(w, tone == ChipState.Tone.NEGATIVE ? ChipOps.Developer.ORGANIC : ChipOps.Developer.ALKALI), "develop " + i);
            if (k == LayerKind.METAL) {
                w = step(ChipOps.etchMetal(w), "etchMetal " + i);
                w = step(ChipOps.strip(w), "strip " + i);
            } else {
                w = step(ChipOps.etchOxide(w), "etchOxide " + i);
                w = step(ChipOps.strip(w), "strip " + i);
                if (k.isDiffusion()) w = step(ChipOps.diffuse(w, k.dopes), "diffuse " + i);
                else w = step(ChipOps.depositMetal(w), "depositMetal " + i);
            }
        }
        return w;
    }

    private static ItemStack step(ChipOps.Result r, String what) {
        if (!r.ok()) throw new IllegalStateException(what + ": " + r.reason());
        return r.out();
    }
}
