package com.taku3.semiconductorlab.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntBinaryOperator;

/**
 * 自作チップの上で動く小さなニューラルネット（[10-16]、S-019 Q30 全部案A）：8×8 の方眼（64）→ 16 → 10（0〜9）。
 * <ul>
 * <li>学習：同梱の自作データ（tools/gen_digits.py、CC0）から、決まった種で誤差逆伝播（{@link #train}）。いつ学習しても同じ重みになる（同梱の重み digit_weights.json と同じ＝テストで確かめる）</li>
 * <li>推論：重みと途中の数を 4ビット（0〜15）にして、掛け算はみな 4ビット×4ビット（{@link #predict} の mul＝お題 mul4 に合格したチップの箱でもよい）。符号は足すか引くかで</li>
 * <li>学び直し：プレイヤーが描いた数字で、少しだけ重みを動かす（{@link #learn}）</li>
 * </ul>
 * ⚠️ 本物の手書き数字（MNIST 28×28）よりずっと小さい。正答率はゲームの見本のデータでのめやす
 */
public final class DigitNet {
    public static final int IN = 64, HID = 16, OUT = 10, Q = 15;
    public static final long SEED = 20261021L;
    /** 重み（実数、学習用）：w1[h][i]・b1[h]・w2[o][h]・b2[o] */
    public final double[][] w1 = new double[HID][IN], w2 = new double[OUT][HID];
    public final double[] b1 = new double[HID], b2 = new double[OUT];

    /** 1つの見本：64個の 0/1 と、答え */
    public record Sample(int[] x, int label) {}

    /** 同梱の自作データ（data/semiconductor-lab/ai/digits.json）を読む。which＝"train" か "test" */
    public static List<Sample> bundled(String which) {
        try (InputStream in = DigitNet.class.getResourceAsStream("/data/semiconductor-lab/ai/digits.json")) {
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            List<Sample> out = new ArrayList<>();
            for (var e : o.getAsJsonArray(which)) {
                JsonArray a = e.getAsJsonArray(), px = a.get(0).getAsJsonArray();
                int[] x = new int[IN];
                for (int i = 0; i < IN; i++) x[i] = px.get(i).getAsInt();
                out.add(new Sample(x, a.get(1).getAsInt()));
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("digits.json", e);
        }
    }

    /** 決まった種で重みを小さな乱数に */
    public static DigitNet fresh() {
        DigitNet n = new DigitNet();
        Random r = new Random(SEED);
        for (double[] row : n.w1) for (int i = 0; i < IN; i++) row[i] = (r.nextDouble() - 0.5) * 0.3;
        for (double[] row : n.w2) for (int h = 0; h < HID; h++) row[h] = (r.nextDouble() - 0.5) * 0.6;
        return n;
    }

    /** 同梱のデータで学習した重み（いつ作っても同じ） */
    public static DigitNet trained() {
        DigitNet n = fresh();
        n.train(bundled("train"), 30, 0.05);
        return n;
    }

    /** 実数のまま前へ：隠れ層（ReLU）と、出力（そのまま） */
    private double[] hidden(int[] x) {
        double[] h = new double[HID];
        for (int j = 0; j < HID; j++) {
            double s = b1[j];
            for (int i = 0; i < IN; i++) if (x[i] != 0) s += w1[j][i];
            h[j] = Math.max(0, s);
        }
        return h;
    }

    private double[] logits(double[] h) {
        double[] z = new double[OUT];
        for (int o = 0; o < OUT; o++) { double s = b2[o]; for (int j = 0; j < HID; j++) s += w2[o][j] * h[j]; z[o] = s; }
        return z;
    }

    /** 1つの見本で1歩（誤差逆伝播、ソフトマックスと交差エントロピー） */
    private void step(Sample s, double lr) {
        double[] h = hidden(s.x()), z = logits(h);
        double max = Double.NEGATIVE_INFINITY;
        for (double v : z) max = Math.max(max, v);
        double sum = 0;
        double[] p = new double[OUT];
        for (int o = 0; o < OUT; o++) { p[o] = StrictMath.exp(z[o] - max); sum += p[o]; }
        double[] dz = new double[OUT];
        for (int o = 0; o < OUT; o++) dz[o] = p[o] / sum - (o == s.label() ? 1 : 0);
        double[] dh = new double[HID];
        for (int o = 0; o < OUT; o++) {
            for (int j = 0; j < HID; j++) { dh[j] += dz[o] * w2[o][j]; w2[o][j] -= lr * dz[o] * h[j]; }
            b2[o] -= lr * dz[o];
        }
        for (int j = 0; j < HID; j++) {
            if (h[j] <= 0) continue;
            for (int i = 0; i < IN; i++) if (s.x()[i] != 0) w1[j][i] -= lr * dh[j];
            b1[j] -= lr * dh[j];
        }
    }

    /** 学習（順番は決まった種でまぜる） */
    public void train(List<Sample> data, int epochs, double lr) {
        List<Sample> d = new ArrayList<>(data);
        Random r = new Random(SEED + 1);
        for (int e = 0; e < epochs; e++) {
            java.util.Collections.shuffle(d, r);
            for (Sample s : d) step(s, lr);
        }
    }

    /** 学び直し（プレイヤーの描いた数字）：その見本で steps 歩だけ */
    public void learn(int[] x, int label, int steps) { for (int k = 0; k < steps; k++) step(new Sample(x, label), 0.05); }

    /** 実数で答える（くらべる用） */
    public int predictFloat(int[] x) { return argmax(logits(hidden(x))); }

    // ===== 4ビットの推論（チップの上で） =====

    /** 4ビットにした重み：大きさ 0〜15 と符号。scale＝実数 1 が何段か */
    public record Quantized(int[][] q1, int[] b1, int[][] q2, int[] b2, double s1, double s2, double hScale) {}

    /** 重みを 4ビットに（層ごとに一番大きい重みを 15 に）。隠れ層の数は学習のデータで一番大きい値を 15 に */
    public Quantized quantize(List<Sample> calib) {
        double m1 = 1e-9, m2 = 1e-9, hmax = 1e-9;
        for (double[] r : w1) for (double v : r) m1 = Math.max(m1, Math.abs(v));
        for (double[] r : w2) for (double v : r) m2 = Math.max(m2, Math.abs(v));
        for (Sample s : calib) for (double v : hidden(s.x())) hmax = Math.max(hmax, v);
        double s1 = Q / m1, s2 = Q / m2;
        int[][] q1 = new int[HID][IN], q2 = new int[OUT][HID];
        int[] qb1 = new int[HID], qb2 = new int[OUT];
        for (int j = 0; j < HID; j++) { for (int i = 0; i < IN; i++) q1[j][i] = (int) Math.round(w1[j][i] * s1); qb1[j] = (int) Math.round(b1[j] * s1 * Q); }
        double hScale = Q / hmax;
        for (int o = 0; o < OUT; o++) { for (int j = 0; j < HID; j++) q2[o][j] = (int) Math.round(w2[o][j] * s2); qb2[o] = (int) Math.round(b2[o] * s2 * hScale); }   // 出力の目盛り＝s2×hScale
        return new Quantized(q1, qb1, q2, qb2, s1, s2, hScale);
    }

    /** 掛け算の回数を数える入れ物（チップの速さの計算用） */
    public static final class Counter { public long multiplies; }

    /**
     * 4ビットで答える。mul＝4ビット×4ビットの掛け算（0〜15 × 0〜15）。符号つきの重みは、大きさを掛けてから足すか引くか。
     * 入力は 0/1 を 0/15 に、隠れ層は 0〜15 に丸める
     */
    public static int predict(Quantized q, int[] x, IntBinaryOperator mul, Counter count) {
        int[] h = new int[HID];
        for (int j = 0; j < HID; j++) {
            long s = q.b1()[j];
            for (int i = 0; i < IN; i++) {
                if (x[i] == 0 || q.q1()[j][i] == 0) continue;
                int w = q.q1()[j][i];
                int p = mul.applyAsInt(Q, Math.abs(w));
                if (count != null) count.multiplies++;
                s += w < 0 ? -p : p;
            }
            double real = Math.max(0, s / (q.s1() * Q));
            h[j] = (int) Math.min(Q, Math.round(real * q.hScale()));
        }
        long best = Long.MIN_VALUE;
        int arg = 0;
        for (int o = 0; o < OUT; o++) {
            long s = q.b2()[o];
            for (int j = 0; j < HID; j++) {
                int w = q.q2()[o][j];
                if (w == 0 || h[j] == 0) continue;
                int p = mul.applyAsInt(h[j], Math.abs(w));
                if (count != null) count.multiplies++;
                s += w < 0 ? -p : p;
            }
            if (s > best) { best = s; arg = o; }   // 足し算の目盛りはみな s2×hScale でそろっている
        }
        return arg;
    }

    private static int argmax(double[] z) { int a = 0; for (int i = 1; i < z.length; i++) if (z[i] > z[a]) a = i; return a; }

    /** 重みを文字にする（同梱の digit_weights.json と比べる・書き出す。小数6けた） */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"license\":\"CC0-1.0\",\"note\":\"trained by DigitNet.trained() from digits.json\"");
        appendMatrix(sb, "w1", w1); appendVector(sb, "b1", b1); appendMatrix(sb, "w2", w2); appendVector(sb, "b2", b2);
        return sb.append("}\n").toString();
    }

    private static void appendMatrix(StringBuilder sb, String k, double[][] m) {
        sb.append(",\n\"").append(k).append("\":[");
        for (int r = 0; r < m.length; r++) { if (r > 0) sb.append(','); appendRow(sb, m[r]); }
        sb.append(']');
    }
    private static void appendVector(StringBuilder sb, String k, double[] v) { sb.append(",\n\"").append(k).append("\":"); appendRow(sb, v); }
    private static void appendRow(StringBuilder sb, double[] v) {
        sb.append('[');
        for (int i = 0; i < v.length; i++) { if (i > 0) sb.append(','); sb.append(String.format(java.util.Locale.ROOT, "%.6f", v[i])); }
        sb.append(']');
    }

    /** 同梱の学習済みの重み（data/semiconductor-lab/ai/digit_weights.json） */
    public static DigitNet bundledWeights() {
        try (InputStream in = DigitNet.class.getResourceAsStream("/data/semiconductor-lab/ai/digit_weights.json")) {
            if (in == null) return null;
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            DigitNet n = new DigitNet();
            read(o.getAsJsonArray("w1"), n.w1); read(o.getAsJsonArray("w2"), n.w2);
            for (int j = 0; j < HID; j++) n.b1[j] = o.getAsJsonArray("b1").get(j).getAsDouble();
            for (int k = 0; k < OUT; k++) n.b2[k] = o.getAsJsonArray("b2").get(k).getAsDouble();
            return n;
        } catch (Exception e) {
            return null;
        }
    }
    private static void read(JsonArray a, double[][] m) { for (int r = 0; r < m.length; r++) for (int c = 0; c < m[r].length; c++) m[r][c] = a.get(r).getAsJsonArray().get(c).getAsDouble(); }
}
