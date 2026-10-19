package com.taku3.semiconductorlab.energy;

/** 電気エネルギーの受け口。単位は J（ジュール）。1 W = 0.05 J/tick */
public interface EnergyStorage {
    long getStoredJ();
    long getCapacityJ();
    /** 入れる。実際に入った量を返す。simulate=true なら数値だけ計算 */
    long insert(long joules, boolean simulate);
    /** 取り出す。実際に出た量を返す */
    long extract(long joules, boolean simulate);
    default boolean canInsert()  { return true; }
    default boolean canExtract() { return true; }
    /** 電圧（V）。v0.2a では未使用。変圧器・高電圧は v0.4〜 */
    default int getVoltage() { return 0; }

    /** W → J/tick（20 tick = 1 秒） */
    static long wattsToJoulesPerTick(double watts) { return Math.round(watts / 20.0); }
    /** 表示用：1234567 J → "1.23 MJ" */
    static String format(long j) {
        if (j >= 1_000_000) return String.format("%.2f MJ", j / 1e6);
        if (j >= 1_000)     return String.format("%.1f kJ", j / 1e3);
        return j + " J";
    }
}