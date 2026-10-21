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
        for (int i = 0; i < words.length && i < 16; i++) r[i] = words[i] & 15;
        return r;
    }

    /** 見本の CPU で cycles 回クロックを回し、出力の口が変わった値の列（最初の 0 は入れない） */
    public static List<Integer> run(int[] rom, int cycles) {
        int pc = 0, a = 0, ir = 0, port = 0;
        boolean second = false;
        List<Integer> seen = new ArrayList<>();
        for (int c = 0; c < cycles; c++) {
            int word = rom[pc];
            int next = (pc + 1) & 15;
            if (!second) ir = word;
            else {
                if ((ir & LOAD) != 0) a = word;
                else if ((ir & ADD) != 0) a = (a + word) & 15;
                else if ((ir & OUT) != 0) { if (a != port) { port = a; seen.add(port); } }   // 口の値が変わったときだけ（同じ値を出しても外からは見えない）
                else if ((ir & JUMP) != 0) next = word;
            }
            pc = next;
            second = !second;
        }
        return seen;
    }

    /** お題の見本：プログラムごとに、出力の口の変わり方の最初の何個を見るか */
    public static final int[][] PROGRAMS = {ADD_3_5, COUNT};
    public static final int CYCLES = 48;
    public static int expectCount(int program) { return program == 0 ? 1 : 5; }
}
