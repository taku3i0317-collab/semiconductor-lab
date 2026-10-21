package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 設計図（データコンポーネント chip_design、アイテム layout_sheet・stamp）：全部の層を工程の順に（LITHO_DESIGN §4.2）。
 * 製図台で描く。ChipModel.of(design) が「全部うまくいったら」の予想を作る。
 * parts＝押したハンコ（v0.3b、§11.3）：記号として見せ、中身をのぞくための記録。判定は層（マスクの絵）だけから。
 */
public record ChipDesign(int size, String name, List<Layer> layers, List<Part> parts, String process) {
    /** 工程の種類（S-011 Q21-3 案A）：バイポーラ（島・ベース・エミッタ・窓・金属）か MOS（ソースとドレイン・ゲート酸化膜・窓・金属） */
    public static final String BIPOLAR = "bipolar", MOS = "mos";
    /** CMOS（[9-24]、1963 ワンラスとサー）：n のウェル・p のソースとドレイン・n のソースとドレイン・ゲート酸化膜・窓・金属の6層 */
    public static final String CMOS = "cmos";
    /** 1つの層（工程の順に並ぶ） */
    public record Layer(LayerKind kind, BitGrid cells) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(i -> i.group(
                LayerKind.CODEC.fieldOf("kind").forGetter(Layer::kind),
                BitGrid.CODEC.fieldOf("cells").forGetter(Layer::cells)
        ).apply(i, Layer::new));
    }

    /** ハンコの中にハンコ（入れ子）があるので、自分を使う形 */
    public static final Codec<ChipDesign> CODEC = Codec.recursive("chip_design", self -> RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(BitGrid.MIN, BitGrid.MAX).fieldOf("size").forGetter(ChipDesign::size),
            Codec.STRING.optionalFieldOf("name", "").forGetter(ChipDesign::name),
            Layer.CODEC.listOf().fieldOf("layers").forGetter(ChipDesign::layers),
            Part.codec(self).listOf().optionalFieldOf("parts", List.of()).forGetter(ChipDesign::parts),
            Codec.STRING.optionalFieldOf("process", BIPOLAR).forGetter(ChipDesign::process)
    ).apply(i, ChipDesign::new)));

    public ChipDesign {
        layers = List.copyOf(layers);
        parts = List.copyOf(parts);
    }

    /** ハンコなしの形（前からの呼び方） */
    public ChipDesign(int size, String name, List<Layer> layers) { this(size, name, layers, List.of(), BIPOLAR); }
    public ChipDesign(int size, String name, List<Layer> layers, List<Part> parts) { this(size, name, layers, parts, BIPOLAR); }
    public boolean mos() { return MOS.equals(process); }
    public boolean cmos() { return CMOS.equals(process); }

    /** 層 i を書きかえた設計図 */
    public ChipDesign withLayer(int i, BitGrid cells) {
        List<Layer> l = new ArrayList<>(layers);
        l.set(i, new Layer(l.get(i).kind(), cells));
        return new ChipDesign(size, name, l, parts, process);
    }

    /** 名前を変えた設計図 */
    public ChipDesign named(String n) { return new ChipDesign(size, n, layers, parts, process); }

    /** 層 i のマスク（原図・フォトマスクに入れる中身）。押したハンコの記録もいっしょに運ぶ（チップがハンコになるとき中身をのぞける） */
    public MaskData mask(int i) { return new MaskData(size, layers.get(i).kind(), layers.get(i).cells(), name, i, 0, 1, parts); }

    /**
     * ハンコを (x, y) に押す（v0.3b）：ハンコの層 j を自分の層 j に重ねる（はみ出す所は捨てる）。押した記録 Part を足す。
     * 層の数・並びはハンコと同じ前提（製図台のふつうの5層）。足りない層は無視
     */
    public ChipDesign stamp(ChipDesign st, int x, int y) {
        List<Layer> l = new ArrayList<>(layers);
        for (int j = 0; j < Math.min(l.size(), st.layers().size()); j++) {
            BitGrid g = l.get(j).cells(), s = st.layers().get(j).cells();
            for (int sy = 0; sy < st.size(); sy++) for (int sx = 0; sx < st.size(); sx++)
                if (s.get(sx, sy) && g.inside(x + sx, y + sy)) g = g.with(x + sx, y + sy, true);
            l.set(j, new Layer(l.get(j).kind(), g));
        }
        List<Part> p = new ArrayList<>(parts);
        p.add(new Part(st, x, y));
        return new ChipDesign(size, name, l, p, process);
    }

    /** 何か描いてある所を囲む、いちばん小さい正方形に切り抜く（ハンコにする。最小 BitGrid.MIN） */
    public ChipDesign crop() {
        int x0 = size, y0 = size, x1 = -1, y1 = -1;
        for (Layer ly : layers) for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
            if (ly.cells().get(x, y)) { x0 = Math.min(x0, x); y0 = Math.min(y0, y); x1 = Math.max(x1, x); y1 = Math.max(y1, y); }
        if (x1 < 0) return this;
        int s = Math.max(BitGrid.MIN, Math.max(x1 - x0, y1 - y0) + 1);
        x0 = Math.min(x0, size - s); y0 = Math.min(y0, size - s);
        List<Layer> l = new ArrayList<>();
        for (Layer ly : layers) {
            BitGrid g = BitGrid.empty(s);
            for (int y = 0; y < s; y++) for (int x = 0; x < s; x++) if (ly.cells().get(x0 + x, y0 + y)) g = g.with(x, y, true);
            l.add(new Layer(ly.kind(), g));
        }
        List<Part> p = new ArrayList<>();
        for (Part pt : parts) p.add(new Part(pt.stamp(), pt.x() - x0, pt.y() - y0));
        return new ChipDesign(s, name, l, p, process);
    }

    /** 工程ごとの決まった層の並び（製図台の白紙と同じ）。twoMetal＝2層目の金属つき（S-011） */
    public static List<LayerKind> kindsFor(String process, boolean twoMetal) {
        List<LayerKind> k = new ArrayList<>(MOS.equals(process)
                ? List.of(LayerKind.DIFF_N, LayerKind.GATE_OX, LayerKind.CONTACT, LayerKind.METAL)
                : CMOS.equals(process)
                ? List.of(LayerKind.DIFF_N, LayerKind.DIFF_P, LayerKind.DIFF_N, LayerKind.GATE_OX, LayerKind.CONTACT, LayerKind.METAL)
                : List.of(LayerKind.DIFF_N, LayerKind.DIFF_P, LayerKind.DIFF_N, LayerKind.CONTACT, LayerKind.METAL));
        if (twoMetal) { k.add(LayerKind.VIA); k.add(LayerKind.METAL2); }
        return k;
    }

    /** 2層目の金属つきか */
    public boolean twoMetal() { return layers.stream().anyMatch(l -> l.kind() == LayerKind.METAL2); }

    /**
     * 焼き上がったチップから設計図を作り直す（ハンコにするとき）：工程の種類（ゲート酸化膜があれば MOS）の決まった並びに、焼いた層を順に入れる
     */
    public static ChipDesign fromState(ChipState st, String name) {
        int n = st.size();
        boolean mos = st.has(LayerKind.GATE_OX), two = st.has(LayerKind.METAL2);
        String process = !mos ? BIPOLAR : st.diffusions() >= 2 ? CMOS : MOS;   // ゲート酸化膜＋拡散2回以上＝ウェルのある CMOS
        List<LayerKind> kinds = new ArrayList<>(kindsFor(process, two));
        BitGrid[] g = new BitGrid[kinds.size()];
        java.util.Arrays.fill(g, BitGrid.empty(n));
        boolean[] used = new boolean[kinds.size()];
        for (ChipState.Applied a : st.done()) {
            for (int i = 0; i < kinds.size(); i++) {
                boolean same = kinds.get(i) == a.kind() || (kinds.get(i).isDiffusion() && a.kind().isDiffusion());
                if (!used[i] && same) { used[i] = true; g[i] = a.cells(); kinds.set(i, a.kind()); break; }
            }
        }
        List<Layer> l = new ArrayList<>();
        for (int i = 0; i < kinds.size(); i++) l.add(new Layer(kinds.get(i), g[i]));
        return new ChipDesign(n, name, l, st.parts(), process);
    }
}
