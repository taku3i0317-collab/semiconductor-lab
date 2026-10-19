package com.taku3.semiconductorlab;

public final class Purity {
    private Purity() {}

    // 段階（DESIGN.md：98% / 6N / 9N / 10N）
    public static final double METALLURGICAL = 0.98;
    public static final double SIX_N    = 0.999999;
    public static final double NINE_N   = 0.999999999;
    public static final double TEN_N = 0.9999999999;

    /** 「9」の個数。0.999999 → 6 */
    public static int nines(double p) {
        double r = 1.0 - p;
        if (r <= 1e-12) return 12;
        return (int) Math.floor(-Math.log10(r) + 1e-9);
    }

    /** 表示用ラベル：3N以上は「6N」、それ未満は「98.0%」 */
    public static String label(double p) {
        int n = nines(p);
        return n >= 3 ? n + "N" : String.format("%.1f%%", p * 100);
    }

    /** パーセント表記（9の数に合わせて桁を出す）：99.9999% */
    public static String percent(double p) {
        int digits = Math.max(1, nines(p) - 2);
        return String.format("%." + digits + "f%%", p * 100);
    }

    /** 不純物の割合：「1,000,000個に1個」の分母 */
    public static String impurityDenominator(double p) {
        double r = 1.0 - p;
        if (r <= 1e-12) return "∞";
        return String.format("%,d", Math.round(1.0 / r));
    }
}