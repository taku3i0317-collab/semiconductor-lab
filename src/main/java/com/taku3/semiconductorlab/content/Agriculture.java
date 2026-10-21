package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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

    // [11-06] 過リン酸石灰（IMP3、O-020）：骨粉3＋硫酸2 → 3（1842 ローズ）。耕地にまくとリン3回分＝熟した作物の収穫が1つ増える
    public static final Item SUPERPHOSPHATE = Reg.item("superphosphate", p -> new com.taku3.semiconductorlab.item.FertilizerItem(p, com.taku3.semiconductorlab.item.FertilizerItem.Nutrient.PHOSPHORUS), new Item.Properties());
    // [11-07] 硝酸アンモニウム（IMP3、O-020）：アンモニア＋硝酸 → 窒素肥料（硫安と同じく耕地に窒素3回分）。⚠ 酸化性の物質
    public static final Item AMMONIUM_NITRATE = Reg.item("ammonium_nitrate", com.taku3.semiconductorlab.item.FertilizerItem::new, new Item.Properties());

    // [11-11] 塩蔵・燻製（IMP3、O-020）：生の肉・魚＋塩 → 塩漬け（火なしで作れて、そのまま食べられる）→ 燻製器・たき火でいぶす → 燻製（焼いた肉より満腹）
    public static final int SALTED_NUTRITION = 5, SMOKED_NUTRITION = 9;
    public static final float SALTED_SATURATION = 6.0f, SMOKED_SATURATION = 13.0f;
    public static final Item SALTED_MEAT = Reg.item("salted_meat", new Item.Properties().food(new FoodProperties(SALTED_NUTRITION, SALTED_SATURATION, false)));
    public static final Item SMOKED_MEAT = Reg.item("smoked_meat", new Item.Properties().food(new FoodProperties(SMOKED_NUTRITION, SMOKED_SATURATION, false)));
    // [11-08] 製粉（IMP3、O-020）：石臼（石2＋棒、作業台で戻る道具）＋小麦2 → 小麦粉
    public static final Item QUERN = Reg.item("quern", com.taku3.semiconductorlab.item.CatalystItem::new, new Item.Properties().stacksTo(1));
    public static final Item FLOUR = Reg.item("flour");
    // [11-09] 発酵パン（IMP3、O-020）：小麦粉3＋野生の酵母＋水 → パン生地2 → かまど・燻製器・たき火 → ふくらんだパン（バニラのパンより満腹）
    public static final int LEAVENED_NUTRITION = 7;
    public static final float LEAVENED_SATURATION = 9.0f;
    public static final Item BREAD_DOUGH = Reg.item("bread_dough");
    public static final Item LEAVENED_BREAD = Reg.item("leavened_bread", new Item.Properties().food(new FoodProperties(LEAVENED_NUTRITION, LEAVENED_SATURATION, false)));

    // [11-03] 輪作（IMP3、S-007）：同じ作物を3回続けた耕地は収穫 −1（やせた耕地）。違う作物・豆・肥料・休閑で戻る
    public static final Block TIRED_FARMLAND = Reg.block("tired_farmland", com.taku3.semiconductorlab.block.TiredFarmlandBlock::new, net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.FARMLAND));

    // [11-02] 犂（IMP3、S-011）：鉄2＋板材3＋棒2 → 犂（耐久 250）。土・草に使うと 3×3 を一度に耕地にする
    public static final Item PLOW = Reg.item("plow", com.taku3.semiconductorlab.item.PlowItem::new, new Item.Properties().durability(250));

    // [11-01] 灌漑と塩害（IMP3、S-011）：乾いた土地で灌漑した耕地は、収穫のたびに塩がたまる → 3回で収穫 −1。石こうで戻す
    public static final Block SALTY_FARMLAND = Reg.block("salty_farmland", com.taku3.semiconductorlab.block.SaltyFarmlandBlock::new, net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.FARMLAND));

    static void init() {
        // [11-05] 堆肥（IMP3、O-021）：コンポスターの骨粉（＝堆肥の近似）を耕地に使うと、窒素とリンが1回分ずつ入る
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var stack = player.getItemInHand(hand);
            if (stack.is(com.taku3.semiconductorlab.content.Chemistry.CALCIUM_SULFATE) && level instanceof net.minecraft.server.level.ServerLevel sl
                    && com.taku3.semiconductorlab.block.SaltyFarmlandBlock.gypsum(sl, hit.getBlockPos())) {   // [11-01] 塩害を石こうで戻す
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.semiconductor-lab.salty_farmland.gypsum"));
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            if (!stack.is(net.minecraft.world.item.Items.BONE_MEAL)) return net.minecraft.world.InteractionResult.PASS;
            var pos = hit.getBlockPos();
            var st = level.getBlockState(pos);
            if (!com.taku3.semiconductorlab.block.TiredFarmlandBlock.isPlain(st) && !st.is(Survival.NITROGEN_FARMLAND)) return net.minecraft.world.InteractionResult.PASS;
            if (level.isClientSide()) return net.minecraft.world.InteractionResult.SUCCESS;
            if (!com.taku3.semiconductorlab.item.FertilizerItem.compost(level, pos)) return net.minecraft.world.InteractionResult.PASS;
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.semiconductor-lab.compost.fertilized"));
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        // [11-06] リンのある耕地の上で熟した作物を収穫すると、1つ多い
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            var crop = ctx.getOptional(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE);
            var origin = ctx.getOptional(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);
            if (crop == null || origin == null || !(crop.getBlock() instanceof net.minecraft.world.level.block.CropBlock)) return;
            var cropPos = net.minecraft.core.BlockPos.containing(origin);
            com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.harvestBonus(ctx.getLevel(), cropPos, crop, drops);
            com.taku3.semiconductorlab.block.TiredFarmlandBlock.onHarvest(ctx.getLevel(), cropPos, crop, drops);   // [11-03] 連作
            com.taku3.semiconductorlab.block.SaltyFarmlandBlock.onHarvest(ctx.getLevel(), cropPos, crop, drops, com.taku3.semiconductorlab.block.SaltyFarmlandBlock.arid(ctx.getLevel(), cropPos));   // [11-01] 塩害
        });
    }
}
