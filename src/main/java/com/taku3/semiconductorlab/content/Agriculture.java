package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;

import static com.taku3.semiconductorlab.machine.Machine.metal;

/**
 * 幹11 農業・食品（[11-..] のノード。IMP2 S-003 の [11-13] 冷凍機で作成。[9-39] 露光装置 → [12-14] クリーンルーム → [12-12] 空調の前提）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/agriculture/、src/gametest/.../AgricultureTests.java）だけを書き換える。
 */
public final class Agriculture {
    private Agriculture() {}

    // [11-13] 冷凍機（IMP2 S-003、1876 リンデのアンモニア圧縮式）：水バケツ → 氷（冷媒のアンモニアは減らない）。1kW
    public static final Machine<RefrigeratorBlockEntity> REFRIGERATOR = Machine.electric("refrigerator", RefrigeratorBlockEntity::new, RefrigeratorBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
