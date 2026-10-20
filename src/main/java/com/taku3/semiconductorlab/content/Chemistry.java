package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.item.*;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.Vessel;
import com.taku3.semiconductorlab.machine.MachineBlock;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
import com.taku3.semiconductorlab.SemiconductorLab;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.taku3.semiconductorlab.machine.Machine.lit;
import static com.taku3.semiconductorlab.machine.Machine.metal;
import static com.taku3.semiconductorlab.machine.Machine.stone;

/**
 * 幹2 化学（docs/TECH_TREE.md の「幹2」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/chemistry/、src/gametest/.../ChemistryTests.java）だけを書き換える。
 */
public final class Chemistry {
    private Chemistry() {}

    // ===== アイテム =====
    // --- 試薬 ---
    // 容器（IMP-33、DESIGN「容器のしくみ」）：ガラス瓶はソーダ石灰ガラス [2-14] から。中身入りのアイテムより上に書く
    public static final Item GLASS_BOTTLE          = Reg.item("glass_bottle");      // 空のガラス瓶（硫酸・塩酸・水を張って白リン）
    public static final Item SULFUR_POWDER         = Reg.item("sulfur_powder");
    public static final Item SULFUR_DIOXIDE        = Reg.contained("sulfur_dioxide", Vessel.GAS_BAG); // 硫黄を燃やした気体
    public static final Item SULFURIC_ACID         = Reg.contained("sulfuric_acid", Vessel.GLASS_BOTTLE);   // ガラス瓶入り（使うと瓶が戻る）
    public static final Item PHOSPHORUS            = Reg.contained("phosphorus", Vessel.WATER_JAR);   // 白リン：水を張ったガラス瓶の中（空気中で自然発火するため）
    // --- ソーダ（[2-03] 天然ソーダ：トロナを焼く 2(Na2CO3·NaHCO3·2H2O) → 3Na2CO3 + CO2 + 5H2O）---
    public static final Item TRONA                 = Reg.item("trona");             // トロナ（天然ソーダの鉱物）
    public static final Item SODIUM_CARBONATE      = Reg.item("sodium_carbonate");  // 炭酸ナトリウム（ソーダ灰）
    // [2-14] ソーダ石灰ガラス（SiO2 約72%・Na2O 14%・CaO 10%）。実験器具用（窓・建材はバニラのガラス＝近似、CHEMISTRY #36）
    public static final Item GLASS_BATCH           = Reg.item("glass_batch");       // 調合原料（砂6＋ソーダ1＋生石灰1）
    public static final Item SODA_LIME_GLASS       = Reg.item("soda_lime_glass");   // 溶かして固めたガラス（器具の素材）
    // [2-02] カリ（IMP2-01）：原木 →たき火→ 木灰 →水で浸す→ 灰汁 →かまどで煮詰める→ 炭酸カリウム K2CO3（"pot ash"）
    public static final Item WOOD_ASH              = Reg.item("wood_ash");          // 木灰（K2CO3 などを含む）
    public static final Item LYE                   = Reg.item("lye", new Item.Properties().stacksTo(16)); // 灰汁（木灰の上澄み）
    public static final Item POTASH                = Reg.item("potash");            // 炭酸カリウム K2CO3
    // [2-04] 海藻灰ソーダとヨウ素（IMP2、S-002）：乾燥昆布 →たき火→ 海藻灰 ×4＋水 → 浸出液 →かまど→ 炭酸ナトリウム（不純）、浸出液＋硫酸 → ヨウ素（クールトア 1811）
    public static final Item KELP_ASH              = Reg.item("kelp_ash");          // 海藻灰（ケルプ）
    public static final Item KELP_LIQUOR           = Reg.item("kelp_liquor", new Item.Properties().stacksTo(16)); // 海藻灰の浸出液（ヨウ化物をふくむ）
    public static final Item IODINE                = Reg.item("iodine");            // ヨウ素（黒紫の結晶。熱すると紫の蒸気）
    // [2-23] 木材乾留（IMP2-01）：乾留炉の副産物。木タールをかまどで煮詰めるとピッチ（レンズ研磨 [7-03] の研磨皿）
    public static final Item WOOD_VINEGAR          = Reg.item("wood_vinegar", new Item.Properties().stacksTo(16)); // 木酢液（酢酸・メタノールを含む水）
    public static final Item WOOD_TAR              = Reg.item("wood_tar", new Item.Properties().stacksTo(16));     // 木タール
    public static final Item PITCH                 = Reg.item("pitch");             // ピッチ（タールを煮詰めた黒い固まり）
    // [2-20] コールタール（IMP4-01）：ガス工場のレトルトで石炭を乾留した副産物。安水は [2-21] アンモニア水の原料
    public static final Item COAL_TAR              = Reg.item("coal_tar", new Item.Properties().stacksTo(16));
    public static final Item AMMONIACAL_LIQUOR     = Reg.item("ammoniacal_liquor", new Item.Properties().stacksTo(16)); // 安水（NH3 約1〜2% の水）
    public static final Item AMMONIA_WATER         = Reg.contained("ammonia_water", Vessel.GLASS_BOTTLE); // [2-21] アンモニア水（NH3 約25%、IMP4-01）
    public static final Item HYDROGEN_PEROXIDE     = Reg.contained("hydrogen_peroxide", Vessel.GLASS_BOTTLE); // [2-19] 過酸化水素（約30%、IMP4-01）
    public static final Item DISTILLED_WATER       = Reg.contained("distilled_water", Vessel.GLASS_BOTTLE); // [2-16] 蒸留水（IMP4-01、洗浄のすすぎ）
    public static final Item AMMONIUM_CHLORIDE     = Reg.item("ammonium_chloride"); // 塩化アンモニウム NH4Cl（塩安、ろう付けのフラックス。[1-18] 白金の沈殿に使う、IMP4-01）
    // [1-18] 塩化白金酸の液（ガラス瓶）。Metallurgy に置くと、クラスの初期化の順でフッ酸の鉛の瓶（Metallurgy.LEAD_BOTTLE）がまだ null になるので、液は Chemistry に置く
    public static final Item CHLOROPLATINIC_ACID   = Reg.contained("chloroplatinic_acid", Vessel.GLASS_BOTTLE);
    // [2-10] フッ酸（IMP4-01）：鉛の瓶入り（ガラスを溶かすのでガラス瓶は不可＝Vessel.ALLOWED）。副産物は石こう（硫酸カルシウム）
    public static final Item HYDROFLUORIC_ACID     = Reg.contained("hydrofluoric_acid", Vessel.LEAD_BOTTLE);
    public static final Item CALCIUM_SULFATE       = Reg.item("calcium_sulfate");   // 石こう CaSO4（無水。焼いた石こう）
    public static final Item SILICON_ETCH          = Reg.contained("silicon_etch", Vessel.LEAD_BOTTLE); // [9-17] シリコンを溶かす混酸（フッ酸＋硝酸、鉛の瓶。IMP4-01）
    // [2-32] ポジ型レジスト（IMP4-01、O-006）：ノボラック＋DNQ をキシレンに溶かした液（ガラス瓶）。日なたで持つと感光して使えなくなる（item/PositiveResistItem）
    public static final Item POSITIVE_RESIST       = containedItem("positive_resist", com.taku3.semiconductorlab.item.PositiveResistItem::new, Vessel.GLASS_BOTTLE, 16);
    public static final Item DEVELOPER             = Reg.contained("developer", Vessel.GLASS_BOTTLE); // [9-21] ポジ型の現像液（薄い NaOH 水溶液、IMP4-01）
    // [9-25] リン酸（IMP4-01）：骨灰＋硫酸（シェーレの時代からの湿式法）。Al のエッチング液
    public static final Item PHOSPHORIC_ACID       = Reg.contained("phosphoric_acid", Vessel.GLASS_BOTTLE);
    // [9-13] 研磨のコロイダルシリカ（IMP4-01）：砂＋NaOH → 水ガラス（ケイ酸ナトリウム）→ 硫酸で中和 → シリカの微粒子が水に分散した液（ガラス瓶）
    public static final Item SODIUM_SILICATE       = Reg.item("sodium_silicate");
    public static final Item COLLOIDAL_SILICA      = Reg.contained("colloidal_silica", Vessel.GLASS_BOTTLE);

    // ===== ブロック =====
    public static final Block TRONA_DEPOSIT  = Reg.ore("trona_deposit", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(1.0f).sound(SoundType.STONE));

    // --- IMP3（O-005）[2-31] アスファルト：ニエプスのヘリオグラフ（1826ごろ）＝レジストの一番古い祖先。原油は使わない（地表の天然アスファルト） ---
    public static final Item ASPHALT               = Reg.item("asphalt");           // 瀝青（天然アスファルト。重い炭化水素の混ざり物）
    public static final Item PINE_RESIN            = Reg.item("pine_resin");        // 松やに（トウヒの丸太を剥片で傷つける）
    public static final Item TURPENTINE            = Reg.item("turpentine");        // テレビン油（松やにを蒸留した軽い油。瀝青を溶かす溶剤）
    public static final Item BITUMEN_VARNISH       = Reg.item("bitumen_varnish");   // 瀝青をテレビン油に溶かした感光液
    public static final Item PEWTER_PLATE          = Reg.item("pewter_plate");      // ピューター（スズ＋鉛）の板
    public static final Block NATURAL_ASPHALT = Reg.ore("natural_asphalt", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(1.2f).sound(SoundType.STONE));
    public static final Block HELIOGRAPH_PLATE = Reg.functional("heliograph_plate", HeliographPlateBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f).sound(SoundType.METAL).noOcclusion().randomTicks());
    public static final Block HELIOGRAPH      = Reg.functional("heliograph", p -> new Block(p) {
            @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s, net.minecraft.world.level.BlockGetter l, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext c) { return Block.box(1, 0, 1, 15, 1, 15); }
        }, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f).sound(SoundType.METAL).noOcclusion());

    // --- IMP3（O-005）[2-29] ゴム：ジャングルのゴムの木 → ラテックス → 酸（木酢液）で生ゴム → 硫黄と熱で加硫ゴム（1839 グッドイヤー） ---
    public static final ResourceKey<Feature> RUBBER_TREE_FEATURE = ResourceKey.create(Registries.FEATURE, SemiconductorLab.id("rubber_tree"));
    public static final ResourceKey<PlacedFeature> RUBBER_TREES_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id("rubber_trees"));
    public static final TreeGrower RUBBER_GROWER = new TreeGrower("semiconductor-lab:rubber", WeightedList.of(RUBBER_TREE_FEATURE), WeightedList.of(), WeightedList.of(), RUBBER_TREE_FEATURE);
    public static final Block RUBBER_LOG     = Reg.block("rubber_log", RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LOG));
    public static final Block RUBBER_LEAVES  = Reg.block("rubber_leaves", p -> new TintedParticleLeavesBlock(0.01f, p), BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES));
    public static final Block RUBBER_SAPLING = Reg.block("rubber_sapling", p -> new SaplingBlock(RUBBER_GROWER, p) {}, BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING));
    public static final Item LATEX                 = Reg.item("latex");             // ラテックス（ゴムの粒が水に浮いた白い樹液）
    public static final Item RAW_RUBBER            = Reg.item("raw_rubber", RawRubberItem::new, new Item.Properties());   // 生ゴム（暑いとべたつき、寒いと硬い）
    public static final Item RUBBER_COMPOUND       = Reg.item("rubber_compound");   // 生ゴム＋硫黄を練った物（焼く前）
    public static final Item VULCANIZED_RUBBER     = Reg.item("vulcanized_rubber"); // 加硫ゴム
    // 環化ゴム（ネガ型レジスト KTFR の土台）：生ゴムを溶剤の中で酸と熱し、分子の二重結合を輪にした硬い樹脂（Harries 1919 硫酸）。感光剤を混ぜてレジストにするのは [2-30]（担当 E）
    public static final Item CYCLIZED_RUBBER_MIX   = Reg.item("cyclized_rubber_mix"); // 環化前の液（生ゴム＋硫酸＋テレビン油）
    public static final Item CYCLIZED_RUBBER       = Reg.item("cyclized_rubber");     // 環化ゴム

    // --- IMP3（O-006）[2-26] メタノール：木酢液に消石灰を入れて酢酸を酢酸カルシウムにし、残りを蒸留して木精（1834 デュマとペリゴが確認） ---
    public static final Item LIMED_WOOD_LIQUOR     = Reg.item("limed_wood_liquor", new Item.Properties().stacksTo(16)); // 石灰を入れた木酢液
    public static final Item METHANOL              = Reg.item("methanol", new Item.Properties().stacksTo(16));          // メタノール（木精）。飲むと失明
    // [2-27] ホルムアルデヒド（IMP3、O-006）：熱した銅の網（触媒）の上でメタノールを空気で酸化（1867 ホフマン）
    public static final Block COPPER_GAUZE   = Reg.functional("copper_gauze", CopperGauzeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0f).sound(SoundType.COPPER).noOcclusion());
    public static final Item FORMALIN              = Reg.item("formalin", new Item.Properties().stacksTo(16));          // ホルマリン（ホルムアルデヒドの水溶液）
    // [2-25] フェノール（IMP3、O-006）：コールタールの中油を水酸化ナトリウムで洗ってフェノールだけ水に移し、酸で戻す（1834 ルンゲ）
    public static final Item CARBOLIC_OIL          = Reg.item("carbolic_oil", new Item.Properties().stacksTo(16));      // 中油（石炭酸油）
    public static final Item SODIUM_PHENOLATE      = Reg.item("sodium_phenolate", new Item.Properties().stacksTo(16));  // フェノールのナトリウム塩の水溶液
    public static final Item PHENOL                = Reg.item("phenol");                                                // フェノール（石炭酸）
    // [2-28] フェノール樹脂（IMP3、O-006）：酸触媒・フェノール多め → ノボラック（熱で溶ける。[2-32] ポジ型レジストの主成分）、塩基触媒・ホルムアルデヒド多め → レゾール → ベークライト（固まると溶けない）
    public static final Item NOVOLAC_MIX           = Reg.item("novolac_mix", new Item.Properties().stacksTo(16));
    public static final Item NOVOLAC               = Reg.item("novolac");
    public static final Item RESOL_MIX             = Reg.item("resol_mix", new Item.Properties().stacksTo(16));
    public static final Item BAKELITE              = Reg.item("bakelite");
    public static final Item FOAMY_BAKELITE        = Reg.item("foamy_bakelite");   // 泡だらけのベークライト（圧力なしで熱した失敗作、O-011）
    // [2-24] ベンゼン・キシレン（IMP3、O-007）：コールタールの分留。弱い火（たき火）では沸点の低い軽油だけ、かまどでは中油。軽油をもう一度：たき火 → ベンゼン（80℃）、かまど → キシレン（約140℃）
    public static final Item LIGHT_OIL             = Reg.item("light_oil", new Item.Properties().stacksTo(16));        // 軽油（コールタールの約170℃までの留分）
    public static final Item BENZENE               = Reg.item("benzene", new Item.Properties().stacksTo(16));          // ベンゼン（発がん性）
    public static final Item XYLENE                = Reg.item("xylene", new Item.Properties().stacksTo(16));           // キシレン（ネガ型レジストの溶剤・現像液）
    // [2-57] DNQ の原料の鎖（IMP3、O-007）：中油を冷やしてナフタレン → スルホン化 → アルカリ融解で 2-ナフトール → スルホン化＋アンモニア（ブッヘラー）でトビアス酸
    //   → 亜硝酸塩と塩酸でジアゾ化（冷やして）→ DNQ。亜硝酸塩は硝石を鉛と熱して作る（昔の方法）。レジストに混ぜるのは [2-32]（担当 E）
    public static final Item NAPHTHALENE           = Reg.item("naphthalene");
    public static final Item NAPHTHALENESULFONIC_ACID = Reg.item("naphthalenesulfonic_acid");
    public static final Item NAPHTHOL_MELT         = Reg.item("naphthol_melt");
    public static final Item NAPHTHOL              = Reg.item("naphthol");
    public static final Item TOBIAS_ACID           = Reg.item("tobias_acid");
    public static final Item NITRITE_MIX           = Reg.item("nitrite_mix");
    public static final Item POTASSIUM_NITRITE     = Reg.item("potassium_nitrite");
    public static final Item DNQ                   = Reg.item("dnq", DnqItem::new, new Item.Properties());
    // [2-17] イオン交換（IMP3、O-008）：フェノール樹脂をスルホン化 → 陽イオン交換樹脂、フェノール樹脂＋アミン（安水）＋ホルムアルデヒド → 陰イオン交換樹脂、塔に詰めて蒸留水 → 脱イオン水
    public static final Item CATION_EXCHANGE_RESIN = Reg.item("cation_exchange_resin");
    public static final Item ANION_EXCHANGE_RESIN  = Reg.item("anion_exchange_resin");
    public static final Item DEIONIZED_WATER       = Reg.contained("deionized_water", Vessel.GLASS_BOTTLE);
    public static final Block ION_EXCHANGE_COLUMN  = Reg.functional("ion_exchange_column", IonExchangeColumnBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.8f).sound(SoundType.GLASS).noOcclusion());
    // [2-42] 酢酸（IMP3、O-008）：石灰を入れた木酢液を煮詰める → 酢酸カルシウム（灰色の酢酸石灰）、＋硫酸 → 氷酢酸（1850ごろ ターンブル）。[2-40] アセトンは酢酸カルシウムを焼く
    public static final Item CALCIUM_ACETATE       = Reg.item("calcium_acetate");
    public static final Item ACETIC_ACID           = Reg.item("acetic_acid", GlacialAceticAcidItem::new, new Item.Properties().stacksTo(16));
    // [2-40] アセトン（IMP3、O-008）：酢酸カルシウムを焼く（乾留）Ca(CH3COO)2 → CaCO3 + (CH3)2CO
    public static final Item ACETONE               = Reg.item("acetone", new Item.Properties().stacksTo(16));
    // [2-09] 石けん（IMP3、O-008）：決定どおり獣脂は牛・豚・羊が落とす。獣脂＋NaOH＋水 → 煮る（けん化）→ 塩で石けんを分ける（塩析、残りの液は手元に残る）→ 液を焼くとグリセリン
    public static final Item TALLOW                = Reg.item("tallow");
    public static final Item SOAP_BATCH            = Reg.item("soap_batch");                                            // 煮る前の鍋の中身
    public static final Item SPENT_LYE             = Reg.item("spent_lye", new Item.Properties().stacksTo(16));        // 塩析の残りの液（グリセリン水）
    public static final Item SAPONIFIED_TALLOW     = Reg.item("saponified_tallow", new Item.Properties().craftRemainder(SPENT_LYE)); // 煮た物（石けん＋グリセリン）
    public static final Item SOAP                  = Reg.item("soap");
    public static final Item GLYCERIN              = Reg.item("glycerin", new Item.Properties().stacksTo(16));
    // [2-41] エポキシ樹脂（IMP3、O-008）：フェノール＋アセトン → ビスフェノールA（1891 ディアニン）、グリセリン＋塩酸 → ジクロロヒドリン → ＋NaOH → エピクロロヒドリン、
    //   両方＋NaOH → エポキシ樹脂（1936〜 カスタン、グリーンリー）。硬化剤はノボラック（IC の封止材と同じ組み合わせ）→ 成形材料 → かまどで硬化
    public static final Item BISPHENOL_A           = Reg.item("bisphenol_a");
    public static final Item DICHLOROHYDRIN        = Reg.item("dichlorohydrin", new Item.Properties().stacksTo(16));
    public static final Item EPICHLOROHYDRIN       = Reg.item("epichlorohydrin", new Item.Properties().stacksTo(16));
    public static final Item EPOXY_RESIN           = Reg.item("epoxy_resin", new Item.Properties().stacksTo(16));
    public static final Item EPOXY_MOLDING_COMPOUND = Reg.item("epoxy_molding_compound");
    public static final Item CURED_EPOXY           = Reg.item("cured_epoxy");
    // [2-39] エタノール（IMP3、O-009）：果実の皮の野生酵母 → 酵母入りの砂糖水 → 発酵のかめ（土器）→ 発酵した液 → 石英の蒸留器 → エタノール（95.6％で止まる＝共沸）
    public static final Item WILD_YEAST            = Reg.item("wild_yeast");
    public static final Item SUGAR_WASH            = Reg.item("sugar_wash", new Item.Properties().stacksTo(16));
    public static final Item FERMENTED_WASH        = Reg.item("fermented_wash", new Item.Properties().stacksTo(16));
    public static final Item ETHANOL               = Reg.contained("ethanol", Vessel.GLASS_BOTTLE);
    public static final Block FERMENTATION_CROCK   = Reg.functional("fermentation_crock", com.taku3.semiconductorlab.block.FermentationCrockBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(1.0f).sound(SoundType.DECORATED_POT).noOcclusion());
    // [2-36] エチレン（IMP3、O-009、決定どおりアルミナ触媒が本流）：熱したアルミナの管にエタノールを通す C2H5OH → C2H4 + H2O。エチレンは袋（約1mol）
    public static final Block ALUMINA_CATALYST_TUBE = Reg.functional("alumina_catalyst_tube", com.taku3.semiconductorlab.block.AluminaCatalystTubeBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).sound(SoundType.METAL));
    public static final Item ETHYLENE_GAS          = Reg.contained("ethylene_gas", Vessel.GAS_BAG);
    // [2-37] ポリエチレン（IMP3、O-009）：高圧の反応釜でエチレン＋ほんの少しの酸素 → ポリエチレン（1933 ICI）
    public static final Item POLYETHYLENE          = Reg.item("polyethylene");
    // S-006（A、D のお願い）：ポリエチレンの瓶。フッ酸を入れられる（1950年代から鉛・ろうの容器に代わった⚠️）
    public static final Item POLYETHYLENE_BOTTLE   = Reg.item("polyethylene_bottle");
    // [2-38] PTFE の前半（IMP3、O-009）：消石灰＋塩素 → さらし粉（1799 テナント）、エタノール＋さらし粉 → クロロホルム（1831）。
    //   後半（クロロホルム＋HF → CHClF2）は SbCl5 触媒＝[1-43] アンチモン待ち（IMPL3_LOG Q3-7）
    public static final Item BLEACHING_POWDER      = Reg.item("bleaching_powder");
    public static final Item CHLOROFORM            = Reg.contained("chloroform", Vessel.GLASS_BOTTLE);
    // [2-58] 無水酢酸（IMP3、O-010）：白リン＋塩素 → 三塩化リン、酢酸3＋PCl3 → 塩化アセチル3、酢酸＋NaOH → 酢酸ナトリウム、塩化アセチル＋酢酸ナトリウム → 無水酢酸（1852 ジェラール）
    public static final Item PHOSPHORUS_TRICHLORIDE = Reg.item("phosphorus_trichloride", new Item.Properties().stacksTo(16));
    public static final Item ACETYL_CHLORIDE       = Reg.item("acetyl_chloride", new Item.Properties().stacksTo(16));
    public static final Item SODIUM_ACETATE        = Reg.item("sodium_acetate");
    public static final Item ACETIC_ANHYDRIDE      = Reg.item("acetic_anhydride", new Item.Properties().stacksTo(16));
    // [2-59] ケイ皮酸（IMP3、O-010）：軽油を精留 → トルエン、＋塩素2（側鎖）→ 塩化ベンザル、＋消石灰＋水 → ベンズアルデヒド。
    //   ベンズアルデヒド＋無水酢酸＋酢酸ナトリウム → かまど → ケイ皮酸（1868 パーキン反応）、＋PCl3 → 塩化シンナモイル（KPR の材料）
    public static final Item TOLUENE               = Reg.item("toluene", new Item.Properties().stacksTo(16));
    public static final Item BENZAL_CHLORIDE       = Reg.item("benzal_chloride", new Item.Properties().stacksTo(16));
    public static final Item BENZALDEHYDE          = Reg.item("benzaldehyde", new Item.Properties().stacksTo(16));
    public static final Item PERKIN_MIX            = Reg.item("perkin_mix", new Item.Properties().stacksTo(16));
    public static final Item CINNAMIC_ACID         = Reg.item("cinnamic_acid");
    public static final Item CINNAMOYL_CHLORIDE    = Reg.item("cinnamoyl_chloride", new Item.Properties().stacksTo(16));
    /** 獣脂を落とす確率（1頭あたり） */
    public static final float TALLOW_CHANCE = 0.6f;

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 乾留炉 [2-23]（IMP2-01）：鉄のレトルト＋銅の冷却管。原木 → 木炭＋木酢液＋木タール。石炭・木炭を燃やす（電気なし）
    public static final Machine<WoodRetortBlockEntity> WOOD_RETORT = Machine.builder("wood_retort", WoodRetortBlockEntity::new)
            .ticker(WoodRetortBlockEntity::tick).layout(WoodRetortBlockEntity.LAYOUT).props(lit(metal(3.0f), 11)).build();

    // ガス工場のレトルト [2-20]（IMP4-01）：耐火れんがの釜＋銅の冷却管。石炭 → コークス＋コールタール＋安水。燃料式（電気なし）
    public static final Machine<GasRetortBlockEntity> GAS_RETORT = Machine.builder("gas_retort", GasRetortBlockEntity::new)
            .ticker(GasRetortBlockEntity::tick).layout(GasRetortBlockEntity.LAYOUT).props(lit(stone(3.5f), 11)).build();

    // 鉛のレトルト [2-10]（IMP4-01）：蛍石＋硫酸 → フッ化水素 → 水に吸わせてフッ酸（鉛の瓶）＋石こう。燃料式（電気なし）
    public static final Machine<HfRetortBlockEntity> LEAD_RETORT = Machine.builder("lead_retort", HfRetortBlockEntity::new)
            .ticker(HfRetortBlockEntity::tick).layout(HfRetortBlockEntity.LAYOUT).props(lit(metal(3.0f), 9)).build();

    // アンモニア蒸留器 [2-21]（IMP4-01）：安水＋消石灰 → アンモニアを追い出して水に吸わせる（1kW）
    public static final Machine<AmmoniaStillBlockEntity> AMMONIA_STILL = Machine.electric("ammonia_still", AmmoniaStillBlockEntity::new, AmmoniaStillBlockEntity.LAYOUT, metal(3.0f));

    // 過酸化水素の電解槽 [2-19]（IMP4-01）：白金の陽極で硫酸を電解 → 過硫酸 → 水で分解して H2O2（硫酸は戻る）。3kW
    public static final Machine<PeroxideCellBlockEntity> PEROXIDE_CELL = Machine.electric("peroxide_cell", PeroxideCellBlockEntity::new, PeroxideCellBlockEntity.LAYOUT, metal(3.0f));

    // 高圧の反応釜 [2-37]（IMP3、O-009）：エチレン4袋＋酸素（開始剤、1袋で16回）→ ポリエチレン。5kW
    public static final Machine<HighPressureReactorBlockEntity> HIGH_PRESSURE_REACTOR = Machine.electric("high_pressure_reactor", HighPressureReactorBlockEntity::new, HighPressureReactorBlockEntity.LAYOUT, metal(4.0f));

    // 加圧加熱釜（オートクレーブ）[2-28]（IMP3、O-011）：水の蒸気で圧力をかけながら熱する。組み合わせは AutoclaveBlockEntity.RECIPES の表。10kW
    public static final Machine<AutoclaveBlockEntity> AUTOCLAVE = Machine.electric("autoclave", AutoclaveBlockEntity::new, AutoclaveBlockEntity.LAYOUT, metal(4.0f));

    /** 専用のクラスを持つ中身入りのアイテム（Reg.contained と同じこと：作業台で空の容器が戻る・容器の表に登録） */
    private static Item containedItem(String name, java.util.function.Function<Item.Properties, Item> factory, Vessel v, int stack) {
        Item.Properties props = new Item.Properties().stacksTo(Math.min(stack, v.stack));
        if (v.empty() != null) props.craftRemainder(v.empty());
        Item item = Reg.item(name, factory, props);
        Vessel.register(item, v);
        return item;
    }

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {
        // [2-09] 獣脂（IMP3）：牛・豚・羊が落とす（決定）。肉といっしょにとれる脂身
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            for (var type : java.util.List.of(net.minecraft.world.entity.EntityTypes.COW, net.minecraft.world.entity.EntityTypes.PIG, net.minecraft.world.entity.EntityTypes.SHEEP)) {
                var key = type.getDefaultLootTable();
                if (key.isPresent() && table.is(key.get())) {
                    if (ctx.getRandom().nextFloat() < TALLOW_CHANCE) drops.add(new net.minecraft.world.item.ItemStack(TALLOW));
                    return;
                }
            }
        });
    }
}
