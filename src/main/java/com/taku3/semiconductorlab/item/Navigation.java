package com.taku3.semiconductorlab.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 航海の計算（IMP2 O-023：[8-21] 六分儀・[14-03] クロノメーター）。マインクラフトの世界に緯度・経度を決める。
 * <ul>
 * <li>北緯＝ −z ÷ {@value #BLOCKS_PER_DEGREE}、東経＝ x ÷ {@value #BLOCKS_PER_DEGREE}（z=0 が赤道、x=0 がグリニッジ）。⚠️ 本物は 1° ≒ 111km</li>
 * <li>グリニッジ時刻：オーバーワールドの時計（0 tick＝朝6時、6000＝正午）。その場所の時刻＝グリニッジ時刻＋経度×4分</li>
 * <li>太陽の高さ：sin h ＝ cos(緯度)·cos(時角)。季節はない（太陽はいつも赤道の真上＝春分・秋分）⚠️</li>
 * </ul>
 * 空の太陽の絵はどこでも同じ時刻で動く（バニラ）。測る高さはこの計算の太陽 ⚠️。
 */
public final class Navigation {
    private Navigation() {}

    public static final double BLOCKS_PER_DEGREE = 1000;
    public static final long DAY_TICKS = 24_000;

    public static double latitudeDeg(BlockPos p) { return -p.getZ() / BLOCKS_PER_DEGREE; }
    public static double longitudeDeg(BlockPos p) { return p.getX() / BLOCKS_PER_DEGREE; }

    /** グリニッジ時刻（時、0〜24）。tick 0＝6時 */
    public static double gmtHours(long ticks) { return ((ticks % DAY_TICKS + DAY_TICKS) % DAY_TICKS / 1000.0 + 6) % 24; }

    public static double gmtHours(Level level) { return gmtHours(level.getOverworldClockTime()); }

    /** その場所の時刻（時）＝グリニッジ＋経度/15 */
    public static double localHours(double gmt, double lonDeg) { return ((gmt + lonDeg / 15) % 24 + 24) % 24; }

    /** 太陽の高さ（度）。季節なし（赤緯 0） */
    public static double sunAltitudeDeg(double latDeg, double localHours) {
        double h = Math.toRadians((localHours - 12) * 15), lat = Math.toRadians(latDeg);
        return Math.toDegrees(Math.asin(Math.cos(lat) * Math.cos(h)));
    }

    public static double sunAltitudeDeg(Level level, BlockPos p) { return sunAltitudeDeg(latitudeDeg(p), localHours(gmtHours(level), longitudeDeg(p))); }

    /** 角度を「度・分」の文字に（例 35°12'） */
    public static String dm(double deg) {
        int minutes = (int) Math.round(Math.abs(deg) * 60);
        return (deg < 0 ? "-" : "") + minutes / 60 + "°" + String.format(java.util.Locale.ROOT, "%02d", minutes % 60) + "'";
    }

    /** 時刻を HH:MM:SS に */
    public static String hms(double hours) {
        long s = Math.round(((hours % 24) + 24) % 24 * 3600);
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", s / 3600 % 24, s / 60 % 60, s % 60);
    }
}
