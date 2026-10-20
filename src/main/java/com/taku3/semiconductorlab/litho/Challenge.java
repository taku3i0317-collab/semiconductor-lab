package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 回路のお題（v0.3b、S-005、LITHO_DESIGN §11.1）：「何を作るか（動き）は教える、どう作るか（形）は自分で見つける」。
 * お題＝入力の数・出力の数・真理値表。検査台でパッドに 0 V・5 V・入力A・入力B・出力1・出力2 を決め、
 * 全部の入力の組み合わせで {@link Circuit#logic} を計算して真理値表と比べる。順に開く（前のお題を解くと次）。
 */
public final class Challenge {
    private Challenge() {}

    /**
     * @param table 行＝入力の組み合わせ（行番号＝入力を上から A・B・C の2進数。1入力なら A、2入力なら A×2＋B、3入力なら A×4＋B×2＋C）、値＝出力のビット（bit0＝出力1、bit1＝出力2）
     */
    public record Def(String id, int inputs, int outputs, int[] table) {
        /** lang：litho.semiconductor-lab.challenge.&lt;id&gt;（名前）・.goal（動きの説明） */
        public String langKey() { return "litho.semiconductor-lab.challenge." + id; }
        public String goalKey() { return langKey() + ".goal"; }
        public int rows() { return 1 << inputs; }
    }

    private static final List<Def> ALL = new ArrayList<>();
    static {
        add("not", 1, 1, 1, 0);                 // 入れた電気と逆の信号
        add("nor", 2, 1, 1, 0, 0, 0);           // どちらにも電気が来ないときだけ（RTL の基本）
        add("or", 2, 1, 0, 1, 1, 1);
        add("nand", 2, 1, 1, 1, 1, 0);
        add("and", 2, 1, 0, 0, 0, 1);           // 2つとも電気が来たときだけ
        add("xor", 2, 1, 0, 1, 1, 0);
        add("half_adder", 2, 2, 0b00, 0b01, 0b01, 0b10);   // 出力1＝和（XOR）、出力2＝けた上がり（AND）
        add("full_adder", 3, 2, 0b00, 0b01, 0b01, 0b10, 0b01, 0b10, 0b10, 0b11);   // [10-11] O-018：入力C＝下のけたからのけた上がり。出力1＝和、出力2＝けた上がり
    }
    private static void add(String id, int in, int out, int... table) { ALL.add(new Def(id, in, out, table)); }

    public static List<Def> all() { return List.copyOf(ALL); }

    /** 行 r の入力 → Circuit.logic のビット（bit2＝A、bit1＝B、bit0＝C） */
    public static int bits(Def d, int r) { return r << (3 - d.inputs()); }
    public static Def byIndex(int i) { return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }

    /** 判定の結果：合った行の数・全部の行の数・パッドの役目が足りないか */
    public record Result(int rowsOk, int rows, boolean missingRoles) {
        public boolean passed() { return !missingRoles && rowsOk == rows; }
    }

    /**
     * お題を判定する。roles＝線の番号 → 役目（{@link Circuit.Role}）。電源と 0 V・必要な入力・出力がそろっていなければ missingRoles
     */
    public static Result check(Def d, Netlist nl, Map<Integer, Circuit.Role> roles, double leakScale) {
        int ok = 0;
        for (int r = 0; r < d.rows(); r++) {
            int got = Circuit.logic(nl, roles, bits(d, r), d.inputs, d.outputs, leakScale);
            if (got < 0) return new Result(0, d.rows(), true);
            if (got == d.table[r]) ok++;
        }
        return new Result(ok, d.rows(), false);
    }
}
