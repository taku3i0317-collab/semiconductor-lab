package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.world.item.Item;

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
    // [11-10] 酢（IMP3、S-005）：発酵した液（酒）を発酵のかめで空気にさらす → 酢酸菌が酢に（1864 パスツール）。酢＋水酸化ナトリウム → 酢酸ナトリウム
    public static final Item VINEGAR = Reg.item("vinegar", new Item.Properties().stacksTo(16));
    // [11-15] 低温殺菌（IMP3、S-005）：発酵した液をたき火で少し温める（約60℃）→ 低温殺菌した酒（菌がいないので酢にならない）
    public static final Item PASTEURIZED_WINE = Reg.item("pasteurized_wine", new Item.Properties().stacksTo(16));

    // [11-14] 砂糖（IMP3、S-005）：サトウキビ3（またはテンサイ＝ビートルート4）＋消石灰＋水 → 石灰を入れたしぼり汁 → たき火で煮つめる → 黒糖（茶色）。
    //   黒糖2＋骨炭＋水 → 白い砂糖2（骨炭が色を吸う）。骨 →かまど（空気を断って焼く）→ 骨炭
    public static final Item CANE_JUICE = Reg.item("cane_juice", new Item.Properties().stacksTo(16));
    public static final Item BEET_JUICE = Reg.item("beet_juice", new Item.Properties().stacksTo(16));
    public static final Item RAW_SUGAR = Reg.item("raw_sugar");
    public static final Item BONE_CHAR = Reg.item("bone_char");

    static void init() {}
}
