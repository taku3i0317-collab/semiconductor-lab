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
import net.minecraft.world.item.Item;
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

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
