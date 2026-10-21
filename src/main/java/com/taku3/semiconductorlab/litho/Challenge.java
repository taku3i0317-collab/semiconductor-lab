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
    public record Def(String id, int inputs, int outputs, int[] table, boolean clock, int[][] steps, boolean program) {
        public Def(String id, int inputs, int outputs, int[] table) { this(id, inputs, outputs, table, false, null, false); }
        public Def(String id, int inputs, int outputs, int[] table, boolean clock, int[][] steps) { this(id, inputs, outputs, table, clock, steps, false); }
        /** lang：litho.semiconductor-lab.challenge.&lt;id&gt;（名前）・.goal（動きの説明） */
        public String langKey() { return "litho.semiconductor-lab.challenge." + id; }
        public String goalKey() { return langKey() + ".goal"; }
        public boolean sequential() { return steps != null; }
        /** 判定する場合の数（組み合わせ＝行の数、順序＝手の数） */
        public int rows() { return program ? Cpu4.PROGRAMS.length : sequential() ? steps.length : 1 << inputs; }
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
        // ===== 4ビット CPU（[10-13]、S-014 全部案A）：入力A〜D＝ROM の語（A が下の位）、クロック、出力1〜4＝出力の口、出力5〜8＝読む番地。判定は論理の箱だけ（Cpu4） =====
        // ROM（[10-13] の ROM、S-014 Q25-3 案A）：入力A・B・C＝番地の下の位から3ビット、出力1〜4＝その番地の語（プログラム 3＋5、Cpu4.ADD_3_5）
        ALL.add(new Def("rom_program", 3, 4, romTable(Cpu4.ADD_3_5)));
        ALL.add(new Def("cpu4", 5, 8, new int[0], true, null, true));
    }
    private static void add(String id, int in, int out, int... table) { ALL.add(new Def(id, in, out, table)); }

    /** ROM の真理値表：行（A が上の位）→ 番地（A が下の位）の語 */
    static int[] romTable(int[] rom) {
        int[] t = new int[8];
        for (int row = 0; row < 8; row++) t[row] = rom[(row >> 2 & 1) | (row >> 1 & 1) << 1 | (row & 1) << 2];
        return t;
    }
    private static void seq(String id, int in, int out, boolean clock, int[][] steps) { ALL.add(new Def(id, in, out, new int[0], clock, steps)); }

    public static List<Def> all() { return List.copyOf(ALL); }
    public static Def byIndex(int i) { return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }
    public static Def byId(String id) { for (Def d : ALL) if (d.id().equals(id)) return d; return null; }

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
    /**
     * ハンコの記録つき（[10-13]、S-014 Q25-4 案A）：チップがみな合格したハンコ（論理の箱）と線でできていれば、{@link LogicSim} で速く判定する。
     * そうでなければ今までどおりアナログ
     */
    public static Result check(Def d, Netlist nl, Map<Integer, Circuit.Role> roles, double leakScale, List<Part> parts) {
        LogicSim sim = LogicSim.of(nl, parts);
        if (d.program()) return sim == null ? new Result(0, d.rows(), false) : checkProgram(d, () -> LogicSim.of(nl, parts), roles);
        if (sim == null) return check(d, nl, roles, leakScale);
        int ok = 0;
        for (int r = 0; r < d.rows(); r++) {
            int row = d.sequential() ? d.steps()[r][0] : r, want = d.sequential() ? d.steps()[r][1] : d.table()[r];
            int bits = sim.step(roles, d.inputsOf(row), d.outputs());
            if (bits < 0) return new Result(0, d.rows(), true);
            if (want < 0 || bits == want) ok++;
            if (!d.sequential()) sim = LogicSim.of(nl, parts);   // 組み合わせ回路は行ごとに記憶を消す
        }
        return new Result(ok, d.rows(), false);
    }

    /**
     * CPU のお題（[10-13]）：プログラムごとに新しい CPU（sim）で、ROM の中身を番地（出力5〜8）に合わせて入力A〜D に入れながらクロックを {@link Cpu4#CYCLES} 回。
     * 出力の口（出力1〜4）が変わった値の列の最初の何個かが、見本 {@link Cpu4#run} と同じなら、そのプログラムは合格
     */
    public static Result checkProgram(Def d, java.util.function.Supplier<LogicSim> fresh, Map<Integer, Circuit.Role> roles) {
        Circuit.Role[] data = {Circuit.Role.IN_A, Circuit.Role.IN_B, Circuit.Role.IN_C, Circuit.Role.IN_D};
        int ok = 0;
        for (int p = 0; p < Cpu4.PROGRAMS.length; p++) {
            int[] rom = Cpu4.PROGRAMS[p];
            LogicSim sim = fresh.get();
            int addr = 0, port = 0, bits = 0;
            List<Integer> seen = new ArrayList<>();
            boolean missing = false;
            for (int c = 0; c < Cpu4.CYCLES && !missing; c++) for (boolean clk : new boolean[] {true, false}) {
                for (int k = 0; k < 6; k++) {   // 番地が変われば ROM の語も変わる：落ち着くまで
                    Map<Circuit.Role, Boolean> in = new EnumMap<>(Circuit.Role.class);
                    for (int b = 0; b < 4; b++) in.put(data[b], (rom[addr] >> b & 1) != 0);
                    in.put(Circuit.Role.CLK, clk);
                    bits = sim.step(roles, in, 8);
                    if (bits < 0) { missing = true; break; }
                    int a = bits >> 4 & 15;
                    if (a == addr) break;
                    addr = a;
                }
                if (missing) break;
                if ((bits & 15) != port) { port = bits & 15; seen.add(port); }
            }
            if (missing) return new Result(0, d.rows(), true);
            List<Integer> want = Cpu4.run(rom, Cpu4.CYCLES);
            int n = Cpu4.expectCount(p);
            if (seen.size() >= n && want.size() >= n && seen.subList(0, n).equals(want.subList(0, n))) ok++;
        }
        return new Result(ok, d.rows(), false);
    }

    public static Result check(Def d, Netlist nl, Map<Integer, Circuit.Role> roles, double leakScale) {
        if (d.program()) return new Result(0, d.rows(), false);   // CPU は論理の箱だけ（アナログでは重すぎる）
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
