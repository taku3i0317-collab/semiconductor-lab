package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.List;

/**
 * 4ビット CPU の命令と見本の動き（[10-13]、S-014 Q25-2 案A：4命令のアキュムレータの CPU）。
 * <pre>
 * 1語＝4ビット。1つの命令＝2語（命令の語・数の語）。ROM は16語（番地 0〜15）。
 * 命令の語はビットが1つだけ立つ形（⚠️ 4004 の命令の形とはちがう、読み解きを簡単にした）：
 *   1＝LOAD n（A ← n）・2＝ADD n（A ← A＋n、4ビットであふれは捨てる）・4＝OUT（出力の口 ← A）・8＝JUMP a（次の番地 ← a）・0＝何もしない
 * クロック1回で1語：上がるとき、1語目なら命令を覚え、2語目なら実行する（番地はいつも1つ進む、JUMP のときだけ a へ）
 * </pre>
 * お題 cpu4 は、組んだ CPU にこの ROM の中身を読ませて、出力の口の変わり方が見本と同じなら合格。見本は {@link #run} で計算する
 */
public final class Cpu4 {
    private Cpu4() {}

    public static final int LOAD = 1, ADD = 2, OUT = 4, JUMP = 8;

    /** プログラム1：3＋5（LOAD 3・ADD 5・OUT・JUMP 6＝そこで止まる）→ 出力 8 */
    public static final int[] ADD_3_5 = rom(LOAD, 3, ADD, 5, OUT, 0, JUMP, 6);
    /** プログラム2：数える（LOAD 1・OUT・ADD 1・JUMP 2）→ 出力 1, 2, 3, … */
    public static final int[] COUNT = rom(LOAD, 1, OUT, 0, ADD, 1, JUMP, 2);

    /** 16語の ROM（足りない所は 0） */
    public static int[] rom(int... words) {
        int[] r = new int[16];
        for (int i = 0; i < words.length && i < 16; i++) r[i] = words[i] & 255;
        return r;
    }

    /** 8ビットのプログラム1：100＋55（4ビットには入らない数）→ 155 */
    public static final int[] ADD_100_55 = rom(LOAD, 100, ADD, 55, OUT, 0, JUMP, 6);
    /** 8ビットのプログラム2：14 から数える（15 をこえる）→ 14, 15, 16, 17, 18 */
    public static final int[] COUNT_FROM_14 = rom(LOAD, 14, OUT, 0, ADD, 1, JUMP, 2);

    /**
     * 論理の箱としての 4ビット CPU（ワンチップマイコン [10-14] のハンコ）：記憶 q＝出力の口（bit0〜3）・番地（4〜7）・A（8〜11）・命令（12〜15）・2語目か（16）。
     * クロックが上がるときに語 word を1つ読む（見本の {@link #run} と同じ動き）
     */
    public static int boxStep(int q, int word) {
        int port = q & 15, pc = q >> 4 & 15, a = q >> 8 & 15, ir = q >> 12 & 15;
        boolean second = (q >> 16 & 1) != 0;
        int next = (pc + 1) & 15;
        if (!second) ir = word & 15;
        else if ((ir & LOAD) != 0) a = word & 15;
        else if ((ir & ADD) != 0) a = (a + word) & 15;
        else if ((ir & OUT) != 0) port = a;
        else if ((ir & JUMP) != 0) next = word & 15;
        return port | next << 4 | a << 8 | ir << 12 | (second ? 0 : 1) << 16;
    }

    /** 見本の CPU で cycles 回クロックを回し、出力の口が変わった値の列（最初の 0 は入れない） */
    public static List<Integer> run(int[] rom, int cycles) { return run(rom, cycles, 4); }

    /** 語の幅つき（8ビット CPU、O-027 Q26-2 案A：4命令のまま語を8ビットに） */
    public static List<Integer> run(int[] rom, int cycles, int bits) {
        int mask = (1 << bits) - 1;
        int pc = 0, a = 0, ir = 0, port = 0;
        boolean second = false;
        List<Integer> seen = new ArrayList<>();
        for (int c = 0; c < cycles; c++) {
            int word = rom[pc];
            int next = (pc + 1) & 15;
            if (!second) ir = word;
            else {
                if ((ir & LOAD) != 0) a = word;
                else if ((ir & ADD) != 0) a = (a + word) & mask;
                else if ((ir & OUT) != 0) { if (a != port) { port = a; seen.add(port); } }   // 口の値が変わったときだけ（同じ値を出しても外からは見えない）
                else if ((ir & JUMP) != 0) next = word;
            }
            pc = next;
            second = !second;
        }
        return seen;
    }

    /** プログラムを読める形に（"LOAD 3・ADD 5・OUT・JUMP 6"）。止まる JUMP（自分の番地）のあとは書かない */
    public static String listing(int[] rom) {
        StringBuilder sb = new StringBuilder();
        for (int pc = 0; pc + 1 < 16; pc += 2) {
            int op = rom[pc], n = rom[pc + 1];
            String w = switch (op) { case LOAD -> "LOAD " + n; case ADD -> "ADD " + n; case OUT -> "OUT"; case JUMP -> "JUMP " + n; default -> null; };
            if (w == null) break;
            if (sb.length() > 0) sb.append("・");
            sb.append(w);
            if (op == JUMP) break;
        }
        return sb.toString();
    }

    /** お題の見本：プログラムごとに、出力の口の変わり方の最初の何個を見るか */
    public static final int[][] PROGRAMS = {ADD_3_5, COUNT};
    public static final int CYCLES = 48;
    public static int expectCount(int program) { return program == 0 ? 1 : 5; }
}
