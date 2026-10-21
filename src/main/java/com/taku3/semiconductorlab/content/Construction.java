package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.AirConditionerBlockEntity;
import com.taku3.semiconductorlab.item.SurveyStation;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.item.TheodoliteItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 幹12 建設・測量（[12-..] のノード。IMP2 O-007 の [12-11] 測量で作成）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/construction/、src/gametest/.../ConstructionTests.java）だけを書き換える。
 */
public final class Construction {
    private Construction() {}

    // ===== [12-11] 測量（三角測量、IMP2 O-007）=====
    /** セオドライトに記録した測点A（場所と向き） */
    public static final net.minecraft.core.component.DataComponentType<SurveyStation> SURVEY_STATION = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("survey_station"),
            net.minecraft.core.component.DataComponentType.<SurveyStation>builder().persistent(SurveyStation.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(SurveyStation.CODEC)).build());
    public static final Item THEODOLITE            = Reg.item("theodolite", TheodoliteItem::new, new Item.Properties().stacksTo(1)); // 望遠鏡＋副尺つきの目盛り円

    // ===== [12-12] 換気・空調（IMP2 S-003、1902 キャリア）：冷凍機＋送風機。動いている間 8 ブロック以内の温度のゆれ ±3℃ → ±0.1℃ =====
    public static final Machine<AirConditionerBlockEntity> AIR_CONDITIONER = Machine.electric("air_conditioner", AirConditionerBlockEntity::new, AirConditionerBlockEntity.LAYOUT, Machine.metal(3.5f));

    // ===== [12-13] HEPA フィルター（IMP2 S-003、1940年代）：ソーダ石灰ガラスを細く引いたガラス繊維のマット。空調機のフィルターの入口に入れる =====
    public static final Item PLATINUM_BUSHING      = Reg.item("platinum_bushing", com.taku3.semiconductorlab.item.ReusableToolItem::new, new Item.Properties().stacksTo(1)); // 小さな穴がたくさんある白金の板（溶けたガラスを引き出す、戻る）
    public static final Item GLASS_FIBER           = Reg.item("glass_fiber");      // ガラス繊維（溶かしたガラスを細い穴から引き出した糸）
    public static final Item HEPA_FILTER           = Reg.item("hepa_filter", new Item.Properties().stacksTo(1)); // 0.3µm の粒を 99.97% 取る

    // ===== [12-14] 層流クリーンルーム（IMP2 S-003、1962 ホイットフィールド）：天井に並べる HEPA の吹き出し口。判定は block/CleanRoom =====
    public static final Block HEPA_CEILING = Reg.block("hepa_ceiling", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(1.5f).sound(SoundType.METAL));

    // ===== [12-01] ローマンコンクリート（IMP3、S-011、決定：凝灰岩＝火山灰の近似）：消石灰＋凝灰岩2＋砂利2 → まだ固まっていない生コン4。
    //   水にふれると固まる（水中でも固まる＝水硬性。バニラのコンクリートパウダーと同じしくみ）=====
    public static final Block ROMAN_CONCRETE = Reg.block("roman_concrete", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(2.0f, 6.0f).sound(SoundType.STONE));
    public static final Block ROMAN_CONCRETE_MIX = Reg.block("roman_concrete_mix", p -> new net.minecraft.world.level.block.ConcretePowderBlock(ROMAN_CONCRETE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5f).sound(SoundType.SAND));

    // ===== [12-02] ポルトランドセメント（IMP3、S-011）：方解石（石灰石の近似）3＋粘土 → 原料の粉 →溶鉱炉（約1450℃）→ クリンカー、2つ＋石こう → セメント4。
    //   セメント＋砂4＋砂利4 → 灰色のコンクリートパウダー8（バニラのコンクリートの本物の作り方）=====
    public static final Item CEMENT_RAW_MEAL       = Reg.item("cement_raw_meal");
    public static final Item CEMENT_CLINKER        = Reg.item("cement_clinker");
    public static final Item PORTLAND_CEMENT       = Reg.item("portland_cement");

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
