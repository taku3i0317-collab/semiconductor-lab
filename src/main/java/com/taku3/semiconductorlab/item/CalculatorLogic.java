package com.taku3.semiconductorlab.item;

/**
 * 電卓の中の計算（[B-08]、S-010）：4ケタの足し算・引き算（1960年代の電卓の近似 ⚠️）。
 * 本物の中身は、数を覚えるレジスタ2つ（表示中の数・足される数）と、加算器（引き算は2の補数＝10の補数で足し算にする）、
 * キーを順に読むカウンタ。ここではその動きを Java で行う（電卓を作れるのは、それらのお題に合格したチップを組み立てたときだけ）。
 * キー：'0'〜'9'・'+'・'-'・'='・'C'。表示：最大4ケタ、負の数は前に「-」、4ケタを超えたら「E」（あふれ）。
 */
public final class CalculatorLogic {
    public static final int DIGITS = 4, MAX = 9999;

    private int entry, acc;
    private char op;            // 待っている計算（'+'・'-'・0＝なし）
    private boolean typing;     // 今、数を打っている途中か
    private boolean error;

    /** キーを1つ押す */
    public void press(char k) {
        if (k == 'C') { entry = 0; acc = 0; op = 0; typing = false; error = false; return; }
        if (error) return;   // あふれたら C まで止まる（本物と同じ）
        if (k >= '0' && k <= '9') {
            if (!typing) { entry = 0; typing = true; }
            if (Math.abs(entry) * 10 + (k - '0') <= MAX) entry = entry * 10 + (k - '0');   // 4ケタより多くは入らない
            return;
        }
        if (k == '+' || k == '-' || k == '=') {
            if (typing || op == 0) acc = apply(op, acc, entry, op == 0);
            if (error) return;
            entry = acc;
            op = k == '=' ? 0 : k;
            typing = false;
        }
    }

    private int apply(char o, int a, int b, boolean first) {
        if (first) return b;
        int r = o == '+' ? a + b : o == '-' ? a - b : b;
        if (Math.abs(r) > MAX) { error = true; return a; }
        return r;
    }

    /** 表示の文字（"1234"・"-12"・"E"） */
    public String display() { return error ? "E" : Integer.toString(entry); }
    public boolean error() { return error; }

    /** テスト用：キーを順に押して表示を返す */
    public static String run(String keys) {
        CalculatorLogic c = new CalculatorLogic();
        for (char k : keys.toCharArray()) c.press(k);
        return c.display();
    }
}
