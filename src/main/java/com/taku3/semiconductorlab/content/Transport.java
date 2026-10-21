package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.item.ChronometerItem;
import net.minecraft.world.item.Item;

/**
 * 幹14 交通・運輸（[14-..] のノード。IMP2 O-023 の [14-03] 経度とクロノメーターで作成。[8-21] 六分儀の続き）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/transport/、src/gametest/.../TransportTests.java）だけを書き換える。
 */
public final class Transport {
    private Transport() {}

    // [14-03] マリン・クロノメーター（IMP2 O-023、1761 ハリソン H4）：るつぼ鋼のぜんまい＋ルビーの軸受け＋規格ねじ＋青銅。グリニッジの時刻を船に持ち歩く。
    //   1日の狂い ±0.5秒⚠️（H4 は81日で約5秒）。六分儀で南中を測るとき、もう片方の手に持つと経度が出る
    public static final Item MARINE_CHRONOMETER    = Reg.item("marine_chronometer", p -> new ChronometerItem(p, 0.5, "marine_chronometer"), new Item.Properties().stacksTo(1));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
