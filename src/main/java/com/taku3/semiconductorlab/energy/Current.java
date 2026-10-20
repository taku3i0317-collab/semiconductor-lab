package com.taku3.semiconductorlab.energy;

/**
 * 電流の種類（v0.4、S-007 決定 2-A：交流／直流の印だけ。周波数・同期は扱わない）。
 * DC＝直流（ダイナモ・電池・太陽電池）、AC＝交流（交流発電機・変圧器）、ANY＝どちらでもよい（電熱・モーターの機械、試験用の電源）。
 */
public enum Current {
    DC, AC, ANY;

    /** a と b はつないでよいか（どちらかが ANY なら、またはおなじ種類なら） */
    public static boolean compatible(Current a, Current b) { return a == ANY || b == ANY || a == b; }
}
