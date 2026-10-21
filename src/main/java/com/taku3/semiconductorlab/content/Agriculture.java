package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import static com.taku3.semiconductorlab.machine.Machine.metal;

/**
 * 幹11 農業・食品（[11-..] のノード。IMP2 S-003 の [11-13] 冷凍機で作成。[9-39] 露光装置 → [12-14] クリーンルーム → [12-12] 空調の前提）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/agriculture/、src/gametest/.../AgricultureTests.java）だけを書き換える。
 */
public final class Agriculture {
    /** 缶詰の満腹度：焼いた牛肉（8・12.8）より少し多い（ゲームの約束、決定 Q3-14 ①） */
    public static final int CANNED_NUTRITION = 9;
    public static final float CANNED_SATURATION = 14.4f;
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

    // [11-12] 缶詰（IMP3、S-006、決定 Q3-14 ①）：鉄＋スズ → ブリキ缶4（1810 デュランド）。缶＋焼いた肉・魚 → 詰めて封をした缶 → かまど・たき火で加熱 → 缶詰
    //   （長く持ち運べる、満腹度が少し多い食べ物）。1804〜10 アペールの瓶詰（ナポレオン軍の懸賞）
    public static final Item TIN_CAN = Reg.item("tin_can");
    public static final Item SEALED_CAN = Reg.item("sealed_can");
    public static final Item CANNED_MEAT = Reg.item("canned_meat", new Item.Properties().food(new FoodProperties(CANNED_NUTRITION, CANNED_SATURATION, false)));
    // [11-16] ボルドー液（IMP3、S-006、決定 Q3-14 ②）：硫酸銅＋消石灰＋水 → 青い液（1885 ミラルデ、ブドウのべと病）。作るだけ（作物の病気の仕組みはまだない）
    public static final Item BORDEAUX_MIXTURE = Reg.item("bordeaux_mixture", new Item.Properties().stacksTo(16));

    static void init() {}
}
