package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 回路のお題（v0.3b、S-005、LITHO_DESIGN §11.1）：「何を作るか（動き）は教える、どう作るか（形）は自分で見つける」。
 * お題＝入力・出力の数と、真理値表（組み合わせ回路）か手順の列（順序回路＝覚える回路、S-009 Q18-1 案A）。
 * 検査台でパッドに 0 V・5 V・入力A〜D・クロック・出力1〜4 を決め、{@link Circuit#evaluate} で計算して比べる。
 * 手順の列は、前の手の電圧から次の手の計算を始める（記憶が残る）。順に開く（前のお題を解くと次）。
 */
public final class Challenge {
    private Challenge() {}

    /**
     * @param table 組み合わせ回路：行＝入力の組み合わせ（行番号＝入力を上から順に並べた2進数。1入力なら A、2入力なら A×2＋B…）、値＝出力のビット（bit0＝出力1 …）。順序回路は空
     * @param clock 最後の入力がクロック（パッドの役目 CLK）
     * @param steps 順序回路：手ごとに {入力の行番号, 期待する出力のビット（−1＝見ない）[, そのあと待つ µs]}。null なら組み合わせ回路
     */
    public record Def(String id, int inputs, int outputs, int[] table, boolean clock, int[][] steps) {
        public Def(String id, int inputs, int outputs, int[] table) { this(id, inputs, outputs, table, false, null); }
        /** lang：litho.semiconductor-lab.challenge.&lt;id&gt;（名前）・.goal（動きの説明） */
        public String langKey() { return "litho.semiconductor-lab.challenge." + id; }
        public String goalKey() { return langKey() + ".goal"; }
        public boolean sequential() { return steps != null; }
        /** 判定する場合の数（組み合わせ＝行の数、順序＝手の数） */
        public int rows() { return sequential() ? steps.length : 1 << inputs; }
        /** i 番目の入力の役目（入力A〜D、クロックつきなら最後はクロック） */
        public Circuit.Role inputRole(int i) {
            if (clock && i == inputs - 1) return Circuit.Role.CLK;
            return new Circuit.Role[] {Circuit.Role.IN_A, Circuit.Role.IN_B, Circuit.Role.IN_C, Circuit.Role.IN_D}[i];
        }
        /** 入力の行番号 → 役目ごとの 1/0 */
        public Map<Circuit.Role, Boolean> inputsOf(int row) {
            Map<Circuit.Role, Boolean> m = new EnumMap<>(Circuit.Role.class);
            for (int i = 0; i < inputs; i++) m.put(inputRole(i), (row >> (inputs - 1 - i) & 1) != 0);
            return m;
        }
    }

    /** 順序回路で、手から手へ入力を変える細かさ（何回に分けるか） */
    static final int RAMP = 10;
    /** 順序回路の細かい1回の時間（秒）。1手＝RAMP 回＝0.1 ms。コンデンサの電荷はこの時間で持ちこされ、少しずつ漏れる（[10-12]、S-012） */
    public static final double DT = 1e-5;

    private static final List<Def> ALL = new ArrayList<>();
    static {
        add("not", 1, 1, 1, 0);                 // 入れた電気と逆の信号
        add("nor", 2, 1, 1, 0, 0, 0);           // どちらにも電気が来ないときだけ（RTL の基本）
        add("or", 2, 1, 0, 1, 1, 1);
        add("nand", 2, 1, 1, 1, 1, 0);
        add("and", 2, 1, 0, 0, 0, 1);           // 2つとも電気が来たときだけ
        add("xor", 2, 1, 0, 1, 1, 0);
        add("half_adder", 2, 2, 0b00, 0b01, 0b01, 0b10);   // 出力1＝和（XOR）、出力2＝けた上がり（AND）
        add("full_adder", 3, 2, 0b00, 0b01, 0b01, 0b10, 0b01, 0b10, 0b10, 0b11);   // [10-11] O-018：入力C＝下のけたからのけた上がり
        // ===== 覚える回路（S-009、手順の列）。[10-11] =====
        // SR ラッチ：入力A＝S（セット）、入力B＝R（リセット）、出力1＝Q。行＝S×2＋R。セット → そのまま → リセット → そのまま → セット → そのまま
        seq("sr_latch", 2, 1, false, new int[][] {{2, 1}, {0, 1}, {1, 0}, {0, 0}, {2, 1}, {0, 1}});
        // D ラッチ：入力A＝D、クロック（入っている間だけ D を写す）。行＝D×2＋CLK
        seq("d_latch", 2, 1, true, new int[][] {{3, 1}, {2, 1}, {0, 1}, {1, 0}, {0, 0}, {2, 0}, {3, 1}, {1, 0}});
        // 2ビットのカウンタ：入力A＝リセット、クロック（下がるときに1つ数える）。出力1＝下の位、出力2＝上の位。行＝R×2＋CLK
        seq("counter", 2, 2, true, new int[][] {{2, 0b00}, {0, 0b00}, {1, -1}, {0, 0b01}, {1, -1}, {0, 0b10}, {1, -1}, {0, 0b11}, {1, -1}, {0, 0b00}});
        // 2ビットのレジスタ：入力A＝D0、入力B＝D1、クロック（上がるときに覚える）。出力1＝Q0、出力2＝Q1。行＝D0×4＋D1×2＋CLK
        seq("register", 3, 2, true, new int[][] {{0b100, -1}, {0b101, 0b01}, {0b000, 0b01}, {0b010, 0b01}, {0b011, 0b10}, {0b110, 0b10}, {0b111, 0b11}, {0b000, 0b11}});
        // ===== 記憶の IC（[10-12]、S-012 Q22-3 案A：1970 インテル 1103 の3トランジスタのセル） =====
        // 入力A＝書きこみの線（W）、入力B＝データ（D）、入力C＝読み出しの線（R）、出力1＝読み出しの線（1103 と同じく、読むと逆の値：覚えた1 → 0）。行＝W×4＋D×2＋R。
        // 1を書く → 読む → 0を書く → 読む → 1を書く → 2 ms 待つ → 書き直す（リフレッシュ）→ 2 ms 待つ → 読む。書いたあとは先に W を切ってからデータを下げる（本物と同じ）
        seq("dram_cell", 3, 1, false, new int[][] {{0b110, -1}, {0b010, -1}, {0b000, -1}, {0b001, 0}, {0b000, -1}, {0b100, -1}, {0b000, -1}, {0b001, 1}, {0b000, -1},
                {0b110, -1}, {0b010, -1}, {0b000, -1, 2000}, {0b110, -1}, {0b010, -1}, {0b000, -1, 2000}, {0b001, 0}});
    }
    private static void add(String id, int in, int out, int... table) { ALL.add(new Def(id, in, out, table)); }
    private static void seq(String id, int in, int out, boolean clock, int[][] steps) { ALL.add(new Def(id, in, out, new int[0], clock, steps)); }

    public static List<Def> all() { return List.copyOf(ALL); }
    public static Def byIndex(int i) { return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }

    /** 行 r の入力 → Circuit.logic のビット（bit2＝A、bit1＝B、bit0＝C）。3入力までの組み合わせ回路用 */
    public static int bits(Def d, int r) { return r << (3 - d.inputs()); }

    /** 判定の結果：合った行（手）の数・全部の数・パッドの役目が足りないか */
    public record Result(int rowsOk, int rows, boolean missingRoles) {
        public boolean passed() { return !missingRoles && rowsOk == rows; }
    }

    /**
     * お題を判定する。roles＝線の番号 → 役目。組み合わせ回路は行ごとに、順序回路は前の手の電圧から順に計算する。
     * 電源と 0 V・要る入力・要る出力がそろっていなければ missingRoles
     */
    public static Result check(Def d, Netlist nl, Map<Integer, Circuit.Role> roles, double leakScale) {
        int ok = 0;
        double[] prev = null;
        Map<Circuit.Role, Boolean> last = null;
        for (int r = 0; r < d.rows(); r++) {
            int row = d.sequential() ? d.steps()[r][0] : r, want = d.sequential() ? d.steps()[r][1] : d.table()[r];
            Map<Circuit.Role, Boolean> now = d.inputsOf(row);
            Circuit.Eval e;
            if (d.sequential() && last != null) {   // 前の手から入力をなめらかに変える（本物の信号も一瞬では変わらない。計算が記憶の状態をたどれる）
                e = null;
                for (int k = 1; k <= RAMP; k++) {
                    Map<Circuit.Role, Double> v = new EnumMap<>(Circuit.Role.class);
                    for (var in : now.entrySet()) {
                        double from = Boolean.TRUE.equals(last.get(in.getKey())) ? Circuit.VCC : 0, to = in.getValue() ? Circuit.VCC : 0;
                        v.put(in.getKey(), from + (to - from) * k / RAMP);
                    }
                    e = Circuit.evaluateVolts(nl, roles, v, d.outputs(), leakScale, prev, DT);
                    if (e == null) break;
                    prev = e.volts();
                }
                int wait = d.steps()[r].length > 2 ? d.steps()[r][2] : 0;   // 入力をそのままにして待つ（µs）：コンデンサの電荷が漏れる
                Map<Circuit.Role, Double> hold = new EnumMap<>(Circuit.Role.class);
                for (var in : now.entrySet()) hold.put(in.getKey(), in.getValue() ? Circuit.VCC : 0);
                for (int k = 0; k < 10 && wait > 0 && e != null; k++) {
                    e = Circuit.evaluateVolts(nl, roles, hold, d.outputs(), leakScale, prev, wait * 1e-6 / 10);
                    if (e != null) prev = e.volts();
                }
            } else e = Circuit.evaluate(nl, roles, now, d.outputs(), leakScale, prev);
            if (e == null) return new Result(0, d.rows(), true);
            if (want < 0 || e.bits() == want) ok++;
            prev = d.sequential() ? e.volts() : null;
            last = now;
        }
        return new Result(ok, d.rows(), false);
    }
}
