package com.taku3.semiconductorlab.content;

import net.minecraft.world.item.Item;

/**
 * 幹13 医療・公衆衛生（[13-..] のノード。IMP3 S-011 の [13-13] 体温計で作成）。
 * 決まり：作れる薬にしない（飲む・使うと効く薬は作らず、歴史・しくみ・道具として扱う）。危ない物は DESIGN「危ない技術の扱い」。
 * この幹のノードを足すときは、このファイル（と src/main/lang/medicine/、src/gametest/.../MedicineTests.java）だけを書き換える。
 */
public final class Medicine {
    private Medicine() {}

    // ===== [13-13] 体温計（IMP3、S-011）：ソーダ石灰ガラス2＋水銀 → 水銀の体温計。右クリックで空気の温度、しゃがんで右クリックで体温 =====
    public static final Item MERCURY_THERMOMETER   = Reg.item("mercury_thermometer", com.taku3.semiconductorlab.item.ThermometerItem::new, new Item.Properties().stacksTo(1));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
