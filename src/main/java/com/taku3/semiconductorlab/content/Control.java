package com.taku3.semiconductorlab.content;

import net.minecraft.world.item.Item;

/**
 * 幹10 制御・計算（docs/TECH_TREE.md の「幹10」のノード）。最初のノード [10-01] 電磁石のときに作った（IMP4-01、O-011）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/control/、src/gametest/.../ControlTests.java）だけを書き換える。
 */
public final class Control {
    private Control() {}

    // ===== アイテム =====
    // [10-01] 電磁石（1825 スタージャン、1830年代 ヘンリー）：鉄の芯に絶縁した銅線を何重にも巻く。電流を流す間だけ磁石になる。イオン注入 [9-28] の質量分離・電磁レンズなどの部品
    public static final Item ELECTROMAGNET         = Reg.item("electromagnet");

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
