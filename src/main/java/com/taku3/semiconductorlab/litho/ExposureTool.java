package com.taku3.semiconductorlab.litho;

/**
 * 露光の方法と、描ける方眼の大きさ（LITHO_DESIGN §3）。N＝8×(10µm÷CD)^(1/3) を 4 の倍数に丸めた値。
 * 方眼の1マス＝その方法の最小線幅（CD）。大きすぎる（細かすぎる）マスクは焼けない。
 */
public enum ExposureTool {
    /** 密着露光（[9-20]、水銀 g線、CD 約 10 µm） */
    CONTACT_G(436, 10_000, 8),
    /** 投影の等倍露光（1973 ⚠️、CD 約 3 µm） */
    PROJECTION_1X(436, 3_000, 12),
    /** g線ステッパー（[9-39]、CD 約 1 µm） */
    STEPPER_G(436, 1_000, 16),
    /** i線ステッパー（CD 約 0.35 µm） */
    STEPPER_I(365, 350, 24),
    /** KrF エキシマ（[7-20]、CD 約 0.13 µm） */
    KRF(248, 130, 36),
    /** ArF エキシマ（CD 約 65 nm） */
    ARF(193, 65, 44),
    /** ArF 液浸（[7-16]、CD 約 38 nm） */
    ARF_IMMERSION(193, 38, 52),
    /** EUV（[9-40]、CD 約 13 nm） */
    EUV(13.5, 13, 72);

    /** 光の波長（nm） */
    public final double wavelengthNm;
    /** 本物の最小線幅のめやす（nm） */
    public final int cdNm;
    /** 方眼の1辺のマス数 */
    public final int grid;

    ExposureTool(double wavelengthNm, int cdNm, int grid) { this.wavelengthNm = wavelengthNm; this.cdNm = cdNm; this.grid = grid; }

    /** CD から方眼の大きさを出す式（表の数はこの式で確かめる。GameTest） */
    public static int gridFor(double cdNm) {
        double n = 8 * Math.cbrt(10_000.0 / cdNm);
        return (int) Math.round(n / 4.0) * 4;
    }
}
