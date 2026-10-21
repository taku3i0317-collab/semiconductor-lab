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
     * 16×16 の NOR（RTL、1961 マイクロロジックの形、S-008 で密着露光に入るようになった）：トランジスタ2つ（左 T1・右 T2）のコレクタを
     * 1本の線（4行目）でつなぎ、真ん中の縦の n の帯（引き上げ抵抗）で電源へ。エミッタは上の 0 V へ、ベースは下の横の帯（ベースの抵抗）を通して入力へ。
     * 金属1層なので、線が交わらないように 0 V を2つ（左上・上の右寄り）に分けた。パッド：上＝0 V（0,0）・出力（6,0）・0 V（11,0）、下＝入力A（5,15）・電源（7,15）・入力B（9,15）
     */
    public static ChipDesign norGate() {
        BitGrid island = BitGrid.parse(
                "................",
                "................",
                ".#####...#####..",
                ".#####...#####..",
                ".#####...#####..",
                ".#####...#####..",
                ".#####.#.#####..",
                ".......#........",
                ".......#........",
                "..####.#.####...",
                ".......#........",
                ".......#........",
                ".......#........",
                "................",
                "................",
                "................");
        BitGrid base = BitGrid.parse(
                "................",
                "................",
                "................",
                "..###.....###...",
                "..###.....###...",
                "..###.....###...",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................");
        BitGrid emitter = BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "...#.......#....",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................");
        BitGrid contact = BitGrid.parse(
                "#...............",
                "................",
                "................",
                "................",
                "...#.#...#.#....",
                "..#.........#...",
                ".......#........",
                "................",
                "................",
                "..#..#...#..#...",
                "................",
                "................",
                ".......#........",
                "................",
                "................",
                "................");
        BitGrid metal = BitGrid.parse(
                "####..#....#....",
                "...#..#....#....",
                "...#..#....#....",
                "...#..#....#....",
                "...#.#####.#....",
                "..#....#....#...",
                "..#....#....#...",
                "..#.........#...",
                "..#.........#...",
                "..#..#...#..#...",
                ".....#...#......",
                ".....#...#......",
                ".....#.#.#......",
                ".....#.#.#......",
                ".....#.#.#......",
                ".....#.#.#......");
        return new ChipDesign(16, "nor_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, island),
                new ChipDesign.Layer(LayerKind.DIFF_P, base),
                new ChipDesign.Layer(LayerKind.DIFF_N, emitter),
                new ChipDesign.Layer(LayerKind.CONTACT, contact),
                new ChipDesign.Layer(LayerKind.METAL, metal)));
    }

    /**
     * 24×24 の SR ラッチ（RTL、S-009 の手順の判定の確かめ。投影の等倍露光 24 で焼ける）：トランジスタ2つ（左 T1・右 T2）。
     * T2 のコレクタ（Q、右のパッド）→ 下を回る線（R の線の下だけ抵抗の帯でくぐる）→ T1 のベース、T1 のコレクタ（Q でない方）→ 真ん中の横の抵抗 → T2 のベース（たがいに押さえ合う）。
     * セット S（左下）は短い抵抗で T1 のベースへ、リセット R（下の真ん中）は短い抵抗で T2 のベースへ（短いと、切れている側のベースが 0 V に近くなり、記憶が安定する）。引き上げ抵抗は上の2本の縦の帯。
     * パッド：上＝0 V（0,0）・電源（7,0）・0 V（17,0）・電源（20,0）、右＝Q（23,8）、下＝S（1,23）・R（16,23）
     */
    public static ChipDesign srLatch() {
        BitGrid island = BitGrid.parse(
                "........................",
                ".......#............#...",
                ".......#............#...",
                ".......#............#...",
                ".......#............#...",
                ".......#............#...",
                "..#####........#####....",
                "..#####........#####....",
                "..#####.######.#####....",
                "..#####........#####....",
                "..#####........#####....",
                "........................",
                ".#..............#.......",
                ".#..............#.......",
                ".#..............#.......",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "..............#####.....",
                "........................",
                "........................",
                "........................");
        BitGrid base = BitGrid.parse(
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "...###..........###.....",
                "...###..........###.....",
                "...###..........###.....",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................");
        BitGrid emitter = BitGrid.parse(
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "....#............#......",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................");
        BitGrid contact = BitGrid.parse(
                "#.......................",
                ".......#............#...",
                "........................",
                "........................",
                "........................",
                ".......#............#...",
                "........................",
                "........................",
                "....#.#.#....#...#.#....",
                "...#............#.......",
                "........................",
                "........................",
                ".#..............#.......",
                "........................",
                ".#..............#.......",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "..............#...#.....",
                "........................",
                "........................",
                "........................");
        BitGrid metal = BitGrid.parse(
                "#####..#.........#..#...",
                "....#..#.........#..#...",
                "....#............#......",
                "....#............#......",
                "....#............#......",
                "....#..#.........#..#...",
                "....#..#.........#..#...",
                "....#..#.........#..#...",
                "....#.###....#...#.#####",
                "...#.........####....#..",
                "...#............#....#..",
                "...#............#....#..",
                ".###............#....#..",
                "...#.................#..",
                ".#.#............#....#..",
                ".#.#............#....#..",
                ".#.#............#....#..",
                ".#.#............#....#..",
                ".#.#............#....#..",
                ".#.#............#....#..",
                ".#.############.#.####..",
                ".#..............#.......",
                ".#..............#.......",
                ".#..............#.......");
        return new ChipDesign(24, "sr_latch_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, island),
                new ChipDesign.Layer(LayerKind.DIFF_P, base),
                new ChipDesign.Layer(LayerKind.DIFF_N, emitter),
                new ChipDesign.Layer(LayerKind.CONTACT, contact),
                new ChipDesign.Layer(LayerKind.METAL, metal)));
    }

    /**
     * 16×16 の NMOS インバータ（[9-23]、S-011：抵抗を負荷にした n チャネル MOS、金属ゲート）。工程は MOS の4層（ソースとドレイン・ゲート酸化膜・窓・金属）。
     * ソース（上の n）・ドレイン（下の n）の間の1行がチャネル（薄いゲート酸化膜の上に Al のゲート）。引き上げ抵抗は右の縦の n の帯。
     * パッド：0 V（左上・上の線）、電源（10,0）、入力（左の 0,9）、出力（右の 15,12）
     */
    public static ChipDesign nmosInverter() {
        return new ChipDesign(16, "nmos_inverter_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, BitGrid.parse(
                "................",
                "................",
                "..........#.....",
                "..........#.....",
                "..........#.....",
                "..........#.....",
                "...###....#.....",
                "...###....#.....",
                "...###....#.....",
                "..........#.....",
                "...###....#.....",
                "...###..........",
                "...###..........",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.GATE_OX, BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "...###..........",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.CONTACT, BitGrid.parse(
                "#...............",
                "................",
                "..........#.....",
                "................",
                "................",
                "................",
                "....#...........",
                "................",
                "................",
                "................",
                "..........#.....",
                "................",
                "....#...........",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.METAL, BitGrid.parse(
                "#####.....#.....",
                "....#.....#.....",
                "....#.....#.....",
                "....#...........",
                "....#...........",
                "....#...........",
                "....#...........",
                "................",
                "................",
                "######..........",
                "..........#.....",
                "..........#.....",
                "....############",
                "................",
                "................",
                "................"))), List.of(), ChipDesign.MOS);
    }

    /**
     * NMOS インバータ＋2層目の金属（S-011 案B）：出力の線（右、1層目）から VIA で2層目に上がり、下を回って VIA で1層目の短い線（下のパッド 8,15）へ。
     * 出力のパッドが2つ（右と下）、同じ線になる
     */
    public static ChipDesign nmosInverterTwoMetal() {
        ChipDesign b = nmosInverter();
        List<ChipDesign.Layer> l = new java.util.ArrayList<>(b.layers());
        BitGrid metal = l.get(3).cells();
        for (int[] c : new int[][] {{8, 14}, {8, 15}}) metal = metal.with(c[0], c[1], true);
        l.set(3, new ChipDesign.Layer(LayerKind.METAL, metal));
        l.add(new ChipDesign.Layer(LayerKind.VIA, BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                ".............#..",
                "................",
                "........#.......",
                "................")));
        l.add(new ChipDesign.Layer(LayerKind.METAL2, BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                ".............#..",
                ".............#..",
                "........######..",
                "................")));
        return new ChipDesign(16, "nmos_two_metal_example", l, List.of(), ChipDesign.MOS);
    }

    /**
     * 16×16 の CMOS インバータ（[9-24]、1963 ワンラスとサー）。工程は CMOS の6層（n のウェル・p のソースとドレイン・n のソースとドレイン・ゲート酸化膜・窓・金属）。
     * 上半分の n のウェルに p チャネル（電源側）、下の p の板に n チャネル（0 V 側）。2つのゲートは同じ入力、2つのドレインは同じ出力。
     * ウェルは電源に、板は 0 V につなぐ。パッド：電源（上の行）、0 V（下の行）、入力（左の 0,7）、出力（右の 15,8）
     */
    public static ChipDesign cmosInverter() {
        return new ChipDesign(16, "cmos_inverter_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, BitGrid.parse(
                "................",
                "..############..",
                "..############..",
                "..############..",
                "..############..",
                "..############..",
                "..############..",
                "..############..",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.DIFF_P, BitGrid.parse(
                "................",
                "................",
                "....###.........",
                "....###.........",
                "................",
                "....###.........",
                "....###.........",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.DIFF_N, BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "....###.........",
                "....###.........",
                "................",
                "....###.........",
                "....###.........",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.GATE_OX, BitGrid.parse(
                "................",
                "................",
                "................",
                "................",
                "....###.........",
                "................",
                "................",
                "................",
                "................",
                "................",
                "................",
                "....###.........",
                "................",
                "................",
                "................",
                "................")),
                new ChipDesign.Layer(LayerKind.CONTACT, BitGrid.parse(
                "................",
                "..........#.....",
                ".....#..........",
                "................",
                "................",
                "................",
                ".....#..........",
                "................",
                "................",
                ".....#..........",
                "................",
                "................",
                "................",
                ".....#..........",
                "..........#.....",
                "................")),
                new ChipDesign.Layer(LayerKind.METAL, BitGrid.parse(
                "################",
                ".....#....#.....",
                ".....#..........",
                "................",
                ".######.........",
                ".#..............",
                ".#...#..........",
                "##...#..........",
                ".#...###########",
                ".#...#..........",
                ".#..............",
                ".######.........",
                "................",
                ".....#..........",
                ".....#....#.....",
                "################"))), List.of(), ChipDesign.CMOS);
    }

    /**
     * 24×24 の DRAM のセル（[10-12]、1970 インテル 1103 と同じ3トランジスタ、S-012 Q22-3 案A）。工程は MOS の4層。
     * M1（左上）：書きこみの線 W がゲート、データ D とためる線をつなぐ。M2（右下）：ためる線がゲート＝ここに電荷をためる。M3（右中）：読み出しの線 R がゲート。
     * 読み出しの線 OUT は抵抗で電源に引き上げ、R を入れると、覚えた1なら M2・M3 を通して 0 V へ（逆の値で読める、読んでも消えない）。
     * パッド：電源（上の行）、0 V（下の行）、D（左 0,3）、W（左 0,5）、R（右 23,14）、OUT（右 23,11）
     */
    public static ChipDesign dramCell() {
        return new ChipDesign(24, "dram_cell_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, BitGrid.parse(
                "........................",
                "........................",
                "....................#...",
                "....###.............#...",
                "....###.............#...",
                "....................#...",
                "....###.............#...",
                "....###.............#...",
                "....................#...",
                "....................#...",
                "....................#...",
                "........................",
                "..............###.......",
                "..............###.......",
                "........................",
                "..............###.......",
                "..............###.......",
                "........................",
                "..............###.......",
                "..............###.......",
                "........................",
                "........................",
                "........................",
                "........................")),
                new ChipDesign.Layer(LayerKind.GATE_OX, BitGrid.parse(
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "....###.................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "..............###.......",
                "........................",
                "........................",
                "..............###.......",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................")),
                new ChipDesign.Layer(LayerKind.CONTACT, BitGrid.parse(
                "........................",
                "........................",
                "....................#...",
                ".....#..................",
                "........................",
                "........................",
                "........................",
                ".....#..................",
                "........................",
                "........................",
                "....................#...",
                "........................",
                "...............#........",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "........................",
                "...............#........",
                "........................",
                "........................",
                "..#.....................",
                "........................")),
                new ChipDesign.Layer(LayerKind.METAL, BitGrid.parse(
                "#####################...",
                "....................#...",
                "....................#...",
                "######..................",
                "........................",
                "#######.................",
                "........................",
                ".....#..................",
                ".....########...........",
                "............#...........",
                "............#.......#...",
                "............#..#########",
                "............#..#........",
                "............#...........",
                "............#.##########",
                "............#...........",
                "............#...........",
                "............#####.......",
                "........................",
                "...............#........",
                "...............#........",
                "...............#........",
                "..#............#........",
                "########################"))), List.of(), ChipDesign.MOS);
    }

    /**
     * 48×48 の ROM のチップ（[10-13] の ROM、S-014 Q25-3 案A：1971 インテル 4001 と同じく、マスクで書きこむ）。8語×4ビット、番地は3ビット（⚠️ 4001 は 256 語）。工程は MOS の4層。
     * <pre>
     * 左の列：0 V（x=0）・電源（x=2）。上の行：電源。下のふち：番地 A0・A1・A2（x=12・16・20）と、データ D0〜D3（x=28・32・36・40）
     * 上：番地の反対を作る NOT 3つ（横の n の帯を電源から引き上げ、番地の線がゲート）→ 縦の線 !A0〜!A2（x=14・18・22）
     * 中：番地を読み解く NOR 8つ（語ごとに横の n の帯、合わない番地の線の下にゲート酸化膜＝MOS で 0 V へ）→ 右の端で金属の語の線になる
     * 右：ROM の行列。縦の n の帯＝データの線（上で電源に引き上げ）。語の線とデータの線の交わる所のとなりにゲート酸化膜を置くと MOS＝その語のそのビットは 0
 * 引き上げは負荷の MOS。0 V の拡散の帯は2語ごとに2層目の金属（x=25 の縦と、行列の上の横の行、右のふちのパッド）へつなぐ（拡散は 1 マス 1 kΩ で長いと 0 まで下がらない）
     * </pre>
     * ゲート酸化膜のマスク1枚で中身が決まる（作り直すのはその1枚だけ）
     */
    public static ChipDesign romChip(int[] rom) {
        final int n = 48, XA = 12, Y0 = 16, XM = 28;
        boolean[][] diff = new boolean[n][n], gox = new boolean[n][n], cont = new boolean[n][n], met = new boolean[n][n], via = new boolean[n][n], met2 = new boolean[n][n];
        final int XS = 25;   // 0 V のつなぎ（2層目の金属）の縦の列
        java.util.function.BiConsumer<boolean[][], int[]> hl = (g, a) -> { for (int x = a[0]; x <= a[1]; x++) g[a[2]][x] = true; };   // {x0, x1, y}
        java.util.function.BiConsumer<boolean[][], int[]> vl = (g, a) -> { for (int y = a[0]; y <= a[1]; y++) g[y][a[2]] = true; };   // {y0, y1, x}
        int lastWord = Y0 + 6 * 3 + 4, LOAD = 7;
        // 引き上げは負荷の MOS（ゲートを電源につないだ長いチャネル、W/L＝1/7。1970年代の NMOS と同じ。拡散の抵抗だけでは弱すぎて 0 まで下がらない）
        java.util.function.IntConsumer loadRow = y -> {   // 横：x=2（電源の窓）→ x=3〜9 のチャネル → x=10 から帯
            diff[y][2] = true; cont[y][2] = true;
            for (int x = 3; x < 3 + LOAD; x++) { gox[y][x] = true; met[y][x] = true; }
        };
        vl.accept(met, new int[] {0, n - 1, 0});            // 0 V
        vl.accept(met, new int[] {0, lastWord, 2});         // 電源（縦）
        hl.accept(met, new int[] {2, XM + 14, 0});          // 電源（上の行）
        for (int i = 0; i < 3; i++) {                       // 番地の反対（NOT）
            int y = 2 + 4 * i, xa = XA + 4 * i, xn = xa + 2;
            loadRow.accept(y); hl.accept(diff, new int[] {3 + LOAD, xn, y}); cont[y][xn] = true;   // 負荷の MOS → 帯、右の端が出力
            hl.accept(diff, new int[] {0, XS, y + 2}); cont[y + 2][0] = true;               // 0 V の帯（右の端も2層目で 0 V へ）
            cont[y + 2][XS] = true; met[y + 2][XS] = true; via[y + 2][XS] = true;
            gox[y + 1][xa] = true;                                                           // 番地の線の下の MOS
            vl.accept(met, new int[] {y + 1, n - 1, xa});                                     // 番地の線（下のふちから）
            vl.accept(met, new int[] {y, lastWord, xn});                                      // 反対の線
        }
        for (int w = 0; w < 8; w++) {                       // 読み解き（NOR）と ROM の行列
            int m = w / 2, ys = Y0 + 6 * m + (w % 2 == 0 ? 0 : 4), yc = w % 2 == 0 ? ys + 1 : ys - 1, yg = Y0 + 6 * m + 2;
            loadRow.accept(ys); hl.accept(diff, new int[] {3 + LOAD, XA + 12, ys}); cont[ys][XA + 12] = true;
            if (w % 2 == 0) {
                hl.accept(diff, new int[] {0, XS, yg}); cont[yg][0] = true;
                cont[yg][XS] = true; met[yg][XS] = true; via[yg][XS] = true;
                hl.accept(met2, new int[] {XS, n - 1, yg});                                   // 2層目の 0 V の行（行列の 0 V の帯へ、右のふちまで）
                for (int k = 0; k < 4; k++) { int xg = XM + 4 * k + 2; cont[yg][xg] = true; met[yg][xg] = true; via[yg][xg] = true; }
            }
            for (int i = 0; i < 3; i++) gox[yc][(w >> i & 1) != 0 ? XA + 4 * i + 2 : XA + 4 * i] = true;   // 合わない番地で 0 V へ
            hl.accept(met, new int[] {XA + 12, XM + 14, ys});                                // 語の線
            for (int k = 0; k < 4; k++) if ((rom[w] >> k & 1) == 0) gox[ys][XM + 4 * k + 1] = true;   // 0 のビットに MOS
        }
        for (int k = 0; k < 4; k++) {                       // データの線と、行列の 0 V
            int xb = XM + 4 * k;
            diff[1][xb] = true; cont[1][xb] = true; met[1][xb] = true;                        // 電源の窓 → 縦の負荷の MOS（y=2〜8）→ データの線
            for (int y = 2; y < 2 + LOAD; y++) { gox[y][xb] = true; met[y][xb] = true; }
            vl.accept(diff, new int[] {2 + LOAD, lastWord + 2, xb});
            cont[lastWord + 2][xb] = true; vl.accept(met, new int[] {lastWord + 2, n - 1, xb});
            vl.accept(diff, new int[] {Y0, lastWord + 2, xb + 2});   // 行列の 0 V の帯（2語ごとに2層目の 0 V へ＝長い拡散の抵抗を作らない）
        }
        vl.accept(met2, new int[] {2 + 4 * 0 + 2, Y0 + 6 * 3 + 2, XS});   // 2層目の縦：NOT・読み解き・行列の 0 V をつなぐ
        met[Y0 + 2][n - 1] = true; via[Y0 + 2][n - 1] = true;               // 右のふちの 0 V のパッド
        java.util.function.Function<boolean[][], BitGrid> g = a -> {
            BitGrid o = BitGrid.empty(n);
            for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) if (a[y][x]) o = o.with(x, y, true);
            return o;
        };
        return new ChipDesign(n, "rom_example", List.of(
                new ChipDesign.Layer(LayerKind.DIFF_N, g.apply(diff)), new ChipDesign.Layer(LayerKind.GATE_OX, g.apply(gox)),
                new ChipDesign.Layer(LayerKind.CONTACT, g.apply(cont)), new ChipDesign.Layer(LayerKind.METAL, g.apply(met)),
                new ChipDesign.Layer(LayerKind.VIA, g.apply(via)), new ChipDesign.Layer(LayerKind.METAL2, g.apply(met2))), List.of(), ChipDesign.MOS);
    }

    /**
     * 論理の箱（合格したハンコ）だけで組んだ 4ビット CPU の見本（[10-13]、約50個）。形ではなく線の番号で直接つなぐ（{@link LogicSim#wired}）。お題 cpu4 が解けることの証明。
     * 2ビットのカウンタの下の位＝今が1語目（命令）か2語目（数）か。レジスタ（2ビット）×2 ずつ：命令・番地・A・出力の口。番地＋1 は半加算器、A＋数 は全加算器、
     * 選ぶ所（マルチプレクサ）は NOT・AND・OR。クロックは AND で分ける（1語目のとき命令へ、2語目のとき実行へ）。roles に パッドの役目を入れる
     */
    public static LogicSim cpu4Boxes(java.util.Map<Integer, Circuit.Role> roles) { return cpu4Boxes(roles, false); }

    /** broken＝足し算のけた上がりをつながない（こわれた見本、テスト用） */
    public static LogicSim cpu4Boxes(java.util.Map<Integer, Circuit.Role> roles, boolean broken) {
        final int G = 0, V = 1, CLK = 2;
        int[] d = {3, 4, 5, 6}, port = {7, 8, 9, 10}, addr = {11, 12, 13, 14};
        roles.put(G, Circuit.Role.GROUND); roles.put(V, Circuit.Role.VCC); roles.put(CLK, Circuit.Role.CLK);
        Circuit.Role[] in = {Circuit.Role.IN_A, Circuit.Role.IN_B, Circuit.Role.IN_C, Circuit.Role.IN_D};
        for (int i = 0; i < 4; i++) { roles.put(d[i], in[i]); roles.put(port[i], Circuit.OUTS[i]); roles.put(addr[i], Circuit.OUTS[4 + i]); }
        List<LogicSim.Wire> w = new java.util.ArrayList<>();
        int[] next = {20};
        java.util.function.IntSupplier net = () -> next[0]++;
        java.util.function.BiConsumer<String, Object[]> box = (id, kv) -> {
            java.util.Map<Circuit.Role, Integer> m = new java.util.EnumMap<>(Circuit.Role.class);
            m.put(Circuit.Role.GROUND, G); m.put(Circuit.Role.VCC, V);
            for (int i = 0; i < kv.length; i += 2) m.put((Circuit.Role) kv[i], (Integer) kv[i + 1]);
            w.add(new LogicSim.Wire(id, m));
        };
        Circuit.Role A = Circuit.Role.IN_A, B = Circuit.Role.IN_B, C = Circuit.Role.IN_C, K = Circuit.Role.CLK, O1 = Circuit.Role.OUT_1, O2 = Circuit.Role.OUT_2;
        java.util.function.IntBinaryOperator and = (x, y) -> { int o = net.getAsInt(); box.accept("and", new Object[] {A, x, B, y, O1, o}); return o; };
        java.util.function.IntBinaryOperator or = (x, y) -> { int o = net.getAsInt(); box.accept("or", new Object[] {A, x, B, y, O1, o}); return o; };
        java.util.function.IntUnaryOperator not = x -> { int o = net.getAsInt(); box.accept("not", new Object[] {A, x, O1, o}); return o; };
        // 1語目／2語目（カウンタの下の位、下がるときに変わる）
        int ph = net.getAsInt(), unused = net.getAsInt();
        box.accept("counter", new Object[] {A, G, K, CLK, O1, ph, O2, unused});
        int nph = not.applyAsInt(ph);
        int clkIr = and.applyAsInt(CLK, nph), clkEx = and.applyAsInt(CLK, ph);
        // 命令のレジスタ（1語目で覚える）：ir0＝LOAD・ir1＝ADD・ir2＝OUT・ir3＝JUMP
        int[] ir = {net.getAsInt(), net.getAsInt(), net.getAsInt(), net.getAsInt()};
        box.accept("register", new Object[] {A, d[0], B, d[1], K, clkIr, O1, ir[0], O2, ir[1]});
        box.accept("register", new Object[] {A, d[2], B, d[3], K, clkIr, O1, ir[2], O2, ir[3]});
        // 選ぶ所：sel ? a : b
        java.util.Map<Integer, Integer> nots = new java.util.HashMap<>();
        java.util.function.Function<int[], Integer> mux = v -> {   // v = {sel, a, b}
            int ns = nots.computeIfAbsent(v[0], s -> not.applyAsInt(s));
            return or.applyAsInt(and.applyAsInt(v[0], v[1]), and.applyAsInt(ns, v[2]));
        };
        // 番地（プログラムカウンタ）：いつも＋1、2語目が JUMP なら数へ
        int jump = and.applyAsInt(ir[3], ph);
        int[] inc = new int[4];
        int carry = V;
        for (int i = 0; i < 4; i++) {
            int s = net.getAsInt(), c = net.getAsInt();
            box.accept("half_adder", new Object[] {A, addr[i], B, carry, O1, s, O2, c});
            inc[i] = s; carry = c;
        }
        int[] pcNext = new int[4];
        for (int i = 0; i < 4; i++) pcNext[i] = mux.apply(new int[] {jump, d[i], inc[i]});
        box.accept("register", new Object[] {A, pcNext[0], B, pcNext[1], K, CLK, O1, addr[0], O2, addr[1]});
        box.accept("register", new Object[] {A, pcNext[2], B, pcNext[3], K, CLK, O1, addr[2], O2, addr[3]});
        // A（アキュムレータ）：LOAD なら数、ADD なら A＋数（全加算器4つ）
        int[] acc = {net.getAsInt(), net.getAsInt(), net.getAsInt(), net.getAsInt()};
        int[] sum = new int[4];
        int k = G;
        for (int i = 0; i < 4; i++) {
            int s = net.getAsInt(), c = net.getAsInt();
            box.accept("full_adder", new Object[] {A, acc[i], B, d[i], C, broken ? G : k, O1, s, O2, c});
            sum[i] = s; k = c;
        }
        int[] accNext = new int[4];
        for (int i = 0; i < 4; i++) accNext[i] = mux.apply(new int[] {ir[1], sum[i], d[i]});
        int clkA = and.applyAsInt(clkEx, or.applyAsInt(ir[0], ir[1]));
        box.accept("register", new Object[] {A, accNext[0], B, accNext[1], K, clkA, O1, acc[0], O2, acc[1]});
        box.accept("register", new Object[] {A, accNext[2], B, accNext[3], K, clkA, O1, acc[2], O2, acc[3]});
        // 出力の口：OUT の2語目で A を写す
        int clkO = and.applyAsInt(clkEx, ir[2]);
        box.accept("register", new Object[] {A, acc[0], B, acc[1], K, clkO, O1, port[0], O2, port[1]});
        box.accept("register", new Object[] {A, acc[2], B, acc[3], K, clkO, O1, port[2], O2, port[3]});
        return LogicSim.wired(w);
    }

    /**
     * 酸化膜つきの板（p型）に、設計図の層を順に全部の工程で焼く（塗布 → 露光 → 現像 → 削る → はがす → 拡散／Al）。
     * 途中で失敗したら、その理由を例外で投げる（テスト用）
     */
    public static ItemStack process(ItemStack oxidizedWafer, ChipDesign d, ExposureTool tool) { return process(oxidizedWafer, d, tool, ChipState.Tone.POSITIVE); }

    /** レジストの型つき（ネガ型 KPR は有機溶剤で現像） */
    public static ItemStack process(ItemStack oxidizedWafer, ChipDesign d, ExposureTool tool, ChipState.Tone tone) { return process(oxidizedWafer, d, tool, tone, false); }

    /** dry＝窓をドライエッチング（[9-27]、方眼 36 以上でも窓が広がらない）で開ける */
    public static ItemStack process(ItemStack oxidizedWafer, ChipDesign d, ExposureTool tool, ChipState.Tone tone, boolean dry) {
        ItemStack w = oxidizedWafer;
        for (int i = 0; i < d.layers().size(); i++) {
            LayerKind k = d.layers().get(i).kind();
            MaskData mask = d.mask(i);
            if (k == LayerKind.VIA) w = step(ChipOps.depositInterlayer(w), "interlayer " + i);   // 2層目の前に層間膜（S-011）
            w = step(ChipOps.coat(w, tone), "coat " + i);
            w = step(ChipOps.expose(w, mask, tool), "expose " + i);
            w = step(ChipOps.develop(w, tone == ChipState.Tone.NEGATIVE ? ChipOps.Developer.ORGANIC : ChipOps.Developer.ALKALI), "develop " + i);
            if (k == LayerKind.METAL || k == LayerKind.METAL2) {
                w = step(ChipOps.etchMetal(w), "etchMetal " + i);
                w = step(ChipOps.strip(w), "strip " + i);
            } else if (k == LayerKind.FIN) {   // FinFET：ドライでフィンを立てる（[9-37]）
                w = step(ChipOps.formFins(w, true), "formFins " + i);
                w = step(ChipOps.strip(w), "strip " + i);
            } else if (k == LayerKind.GATE_OX) {   // MOS：開いた所の酸化膜を薄く育て直す（S-011）
                w = step(ChipOps.gateOxide(w), "gateOxide " + i);
                w = step(ChipOps.strip(w), "strip " + i);
            } else {
                w = step(ChipOps.etchOxide(w, dry), "etchOxide " + i);
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
