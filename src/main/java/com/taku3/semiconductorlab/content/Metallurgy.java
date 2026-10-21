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
 * 幹1 冶金（docs/TECH_TREE.md の「幹1」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/metallurgy/、src/gametest/.../MetallurgyTests.java）だけを書き換える。
 */
public final class Metallurgy {
    private Metallurgy() {}

    // ===== アイテム =====
    // --- 第1章：入手 ---
    public static final Item MAGNETITE             = Reg.item("magnetite");
    public static final Item WOLFRAMITE            = Reg.item("wolframite");
    public static final Item CASSITERITE           = Reg.item("cassiterite");      // [1-22] 錫石 SnO2（IMP2-01。花崗岩の鉱脈、鉄マンガン重石と同じ帯）
    public static final Item TIN_INGOT             = Reg.item("tin_ingot");        // [1-30] スズ（IMP2-01。SnO2 + 2C → Sn + 2CO、融点 232℃）
    public static final Item BRONZE_CHARGE         = Reg.item("bronze_charge");    // [1-05] 青銅の地金の調合（銅8＋スズ1。るつぼに入れて溶かす）
    public static final Item BRONZE_INGOT          = Reg.item("bronze_ingot");     // [1-05] 青銅（Cu-Sn 約11%、IMP2-01）
    // [1-32] 銀（灰吹法、IMP2-01）：骨粉 →かまど→ 骨灰 ×4 → 灰吹き皿。鉛8＋皿 → 灰吹きの調合 →溶鉱炉（空気を吹く）→ 銀の粒（PbO は皿に吸われる）
    public static final Item BONE_ASH              = Reg.item("bone_ash");          // 骨灰（リン酸カルシウム）
    public static final Item CUPEL                 = Reg.item("cupel");             // 灰吹き皿
    public static final Item CUPEL_CHARGE          = Reg.item("cupel_charge");      // 鉛（銀入り）を皿にのせたもの
    public static final Item SILVER_NUGGET         = Reg.item("silver_nugget");
    public static final Item SILVER_INGOT          = Reg.item("silver_ingot");
    public static final Item MALACHITE             = Reg.item("malachite");        // [1-03] 孔雀石 Cu2CO3(OH)2（IMP2-01。銅鉱床の上の酸化帯）
    public static final Item GALENA                = Reg.item("galena");
    // [1-43] アンチモン（IMP3、S-002）：輝安鉱 Sb2S3 →かまど（焙焼）→ 三酸化アンチモン →かまど（還元）→ アンチモン。＋塩素 → 五塩化アンチモン（スワーツ反応の触媒、[2-38]）
    public static final Item STIBNITE              = Reg.item("stibnite");
    public static final Item ANTIMONY_TRIOXIDE     = Reg.item("antimony_trioxide");
    public static final Item ANTIMONY              = Reg.item("antimony");
    // [1-37] ニッケル（IMP3、S-004）：ペントランド鉱 (Fe,Ni)9S8 ＋砂（鉄をスラグに）→ かまど → ニッケルのかわ（硫化物）→ かまど（焙焼）→ 酸化ニッケル
    //   → ＋水素 → 粗ニッケル → ＋一酸化炭素4（約50℃）→ ニッケルカルボニル（猛毒の気体）→ かまど（約200℃で分解）→ ニッケル（1890 モンド法）
    public static final Item PENTLANDITE           = Reg.item("pentlandite");
    // [1-42] リチウム（IMP3、S-006）：リチア輝石 LiAlSi2O6（花崗岩ペグマタイト）→ 溶鉱炉で焼く（α→β）→ ＋硫酸＋水 → 硫酸リチウム → ＋ソーダ灰 → 炭酸リチウム
    public static final Item SPODUMENE             = Reg.item("spodumene");
    public static final Item BETA_SPODUMENE        = Reg.item("beta_spodumene");
    public static final Item LITHIUM_SULFATE       = Reg.item("lithium_sulfate");
    public static final Item LITHIUM_CARBONATE     = Reg.item("lithium_carbonate");
    public static final Item PYROLUSITE            = Reg.item("pyrolusite");   // 軟マンガン鉱 MnO2（[4-28] 乾電池の正極、決定 Q3-12 ①、IMP3 S-006）
    public static final Item MATTE_CHARGE          = Reg.item("matte_charge");
    public static final Item NICKEL_MATTE          = Reg.item("nickel_matte");
    public static final Item NICKEL_OXIDE          = Reg.item("nickel_oxide");
    public static final Item CRUDE_NICKEL          = Reg.item("crude_nickel");
    public static final Item CARBON_MONOXIDE_GAS   = Reg.contained("carbon_monoxide_gas", Vessel.GAS_BAG);
    public static final Item NICKEL_CARBONYL_GAS   = Reg.item("nickel_carbonyl_gas", com.taku3.semiconductorlab.item.NickelCarbonylItem::new, new Item.Properties());
    public static final Item NICKEL                = Reg.item("nickel");
    // [1-39] マグネシウム（IMP3、O-018、決定(b) ダウ法）：にがり（[0-16] 塩田）2＋塩化水素の袋 → かまど（塩化水素の中で乾かす）→ 無水の塩化マグネシウム
    //   → マグネシウムの電解槽 → マグネシウム＋塩素。実験：にがりをそのまま焼くと、水と反応して酸化マグネシウムになる（加水分解）
    public static final Item BITTERN_UNDER_HCL     = Reg.item("bittern_under_hcl");
    public static final Item ANHYDROUS_MAGNESIUM_CHLORIDE = Reg.item("anhydrous_magnesium_chloride");
    public static final Item MAGNESIUM_OXIDE       = Reg.item("magnesium_oxide");
    public static final Item MAGNESIUM             = Reg.item("magnesium");
    // [1-41] チタン（IMP3、O-018、クロール法 1940）：チタン鉄鉱は砂鉄の副産物（決定、ILMENITE_FROM_IRON_SAND の確率）。チタン鉄鉱＋木炭＋塩素3 → 粗い四塩化チタン →（蒸留）→ 四塩化チタン、
    //   ＋マグネシウム2＋アルゴンの袋 → クロールの仕込み →かまど → 塩の混ざったスポンジ →（真空で蒸留）→ スポンジチタン（塩化マグネシウムが作業台に残る＝[1-39] の電解へ戻る）
    public static final float ILMENITE_FROM_IRON_SAND = 0.1f;
    public static final Item ILMENITE              = Reg.item("ilmenite");
    // [1-19] 希土類（イットリウム・セリウム、IMP3、S-009。E の白色 LED の蛍光体 YAG:Ce のため）。放射性のトリウムが少ない鉱石を選んだ（モナズ石は説明だけ）
    //   Ce：バストネス石 →かまど（焙焼、Ce を CeO2 に）→ ＋塩酸2（ほかの希土類がとける）→ 酸化セリウム
    //   Y ：イオン吸着型の粘土4＋硫酸アンモニウム＋水 → 希土類の浸出液 → ＋シュウ酸 → シュウ酸塩 →かまど→ 希土類の酸化物（Y が多い）
    //       → 4つ＋塩酸＋陽イオン交換樹脂 → 酸化イットリウム2（1947 スペディングのイオン交換）。シュウ酸＝砂糖＋硝酸2（1776 シェーレ）
    public static final Item BASTNAESITE           = Reg.item("bastnaesite");
    public static final Item ROASTED_BASTNAESITE   = Reg.item("roasted_bastnaesite");
    public static final Item CERIUM_OXIDE          = Reg.item("cerium_oxide");
    public static final Item OXALIC_ACID           = Reg.item("oxalic_acid");
    public static final Item RARE_EARTH_LEACHATE   = Reg.item("rare_earth_leachate", new Item.Properties().stacksTo(16));
    public static final Item RARE_EARTH_OXALATE    = Reg.item("rare_earth_oxalate");
    public static final Item MIXED_RARE_EARTH_OXIDE = Reg.item("mixed_rare_earth_oxide");
    public static final Item YTTRIUM_OXIDE         = Reg.item("yttrium_oxide");
    // [1-70] ジルコニウム・ハフニウム（IMP3、S-007）：ジルコン ZrSiO4（Hf を約2%ふくむ）は砂鉄の浜の重い砂に、チタン鉄鉱といっしょにまじる（ZIRCON_FROM_IRON_SAND）。
    //   ジルコン＋水酸化ナトリウム2 →溶鉱炉→ 粗い酸化ジルコニウム（Hf 入り）→ ＋フッ酸2＋フッ化カリウム → 粗いフッ化ジルコニウムカリウム（Hf 入り）
    //   → 8つ＋水で分別結晶（1924 ヘヴェシー）→ フッ化ハフニウムカリウム1（各マスに純粋なフッ化ジルコニウムカリウムが残る）
    //   K2HfF6 ＋アンモニア水 → 水酸化ハフニウム →かまど→ 酸化ハフニウム HfO2（high-k）。K2ZrF6/K2HfF6 ＋ナトリウム → 粗い金属 → ＋ヨウ素＋W線 → 結晶棒（1925 ヨウ化物法）
    public static final float ZIRCON_FROM_IRON_SAND = 0.05f;
    public static final Item ZIRCON                = Reg.item("zircon");
    public static final Item CRUDE_ZIRCONIA        = Reg.item("crude_zirconia");
    public static final Item POTASSIUM_FLUOROZIRCONATE = Reg.item("potassium_fluorozirconate");
    public static final Item CRUDE_FLUOROZIRCONATE = Reg.item("crude_fluorozirconate", new Item.Properties().craftRemainder(POTASSIUM_FLUOROZIRCONATE));
    public static final Item POTASSIUM_FLUOROHAFNATE = Reg.item("potassium_fluorohafnate");
    public static final Item HAFNIUM_HYDROXIDE     = Reg.item("hafnium_hydroxide");
    public static final Item HAFNIUM_OXIDE         = Reg.item("hafnium_oxide");
    public static final Item CRUDE_ZIRCONIUM       = Reg.item("crude_zirconium");
    public static final Item CRUDE_HAFNIUM         = Reg.item("crude_hafnium");
    public static final Item ZIRCONIUM_BAR         = Reg.item("zirconium_bar");
    public static final Item HAFNIUM_BAR           = Reg.item("hafnium_bar");
    public static final Item CRUDE_TITANIUM_TETRACHLORIDE = Reg.item("crude_titanium_tetrachloride", new Item.Properties().stacksTo(16));
    public static final Item TITANIUM_TETRACHLORIDE = Reg.item("titanium_tetrachloride", new Item.Properties().stacksTo(16));
    public static final Item KROLL_CHARGE          = Reg.item("kroll_charge");
    public static final Item SPONGE_WITH_SALT      = Reg.item("sponge_with_salt", new Item.Properties().craftRemainder(ANHYDROUS_MAGNESIUM_CHLORIDE));
    public static final Item TITANIUM_SPONGE       = Reg.item("titanium_sponge");
    public static final Item FLUORITE              = Reg.item("fluorite");         // [1-21] 蛍石 CaF2（IMP4-01、[2-10] フッ酸の原料。方鉛鉱・閃亜鉛鉱と同じ熱水鉱脈）
    public static final Item COKE                  = Reg.item("coke");             // [1-10] コークス（IMP4-01：[2-20] ガス工場のレトルトで石炭を乾留した残り。高炉での使い道は [1-10] で）
    public static final Item BAUXITE               = Reg.item("bauxite");          // [1-24] ボーキサイト（IMP4-01、アルミの原料。熱帯の風化残留土＝ジャングル・サバンナの地表近く）
    // [1-40] クロム（IMP2 O-015）：クロム鉄鉱 FeCr2O4 → ソーダ焙焼 → クロム酸ナトリウム → [2-07] 重クロム酸カリウム → 熱で分解 → Cr2O3 → アルミでテルミット還元
    public static final Item CHROMITE              = Reg.item("chromite");          // クロム鉄鉱（かんらん岩・蛇紋岩の近似＝深層岩の中）
    public static final Item CHROMITE_ROAST_CHARGE = Reg.item("chromite_roast_charge"); // クロム鉄鉱＋炭酸ナトリウム＋生石灰（焙焼の前）
    public static final Item SODIUM_CHROMATE       = Reg.item("sodium_chromate");   // クロム酸ナトリウム Na2CrO4（6価クロム）
    public static final Item CHROMIUM_OXIDE        = Reg.item("chromium_oxide");    // 酸化クロム(III) Cr2O3（緑）
    public static final Item THERMITE_CHARGE       = Reg.item("thermite_charge");   // Cr2O3＋アルミ粉をるつぼに入れた物（工業の還元用）
    public static final Item CHROMIUM              = Reg.item("chromium");          // 金属クロム
    // [1-14] バイヤー法（IMP4-01）：水酸化アルミニウム →かまど→ アルミナ。赤泥は残りかす（強いアルカリ）
    public static final Item ALUMINIUM_HYDROXIDE   = Reg.item("aluminium_hydroxide");
    public static final Item ALUMINA               = Reg.item("alumina");
    public static final Item RED_MUD               = Reg.item("red_mud");
    public static final Item CRYOLITE              = Reg.item("cryolite");         // [1-38] 氷晶石 Na3AlF6（IMP4-01、合成。ホール・エルー法の電解浴）
    public static final Item ALUMINIUM_INGOT       = Reg.item("aluminium_ingot");  // [1-29] アルミニウム（IMP4-01、ホール・エルー法。c:ingots/aluminum）
    public static final Item PLATINUM_GRAIN        = Reg.item("platinum_grain");   // [1-20] 砂白金（IMP4-01：砂鉄を掘るとまれに。自然白金の粒、Pd・Ir などを含む）
    // [1-18] 白金の精製（IMP4-01、ウォラストン 1800 年ごろ）：王水で溶かす → 塩化アンモニウムで黄色い沈殿 → 焼いて白金海綿 → 押し固めて白熱で鍛える
    public static final Item AMMONIUM_CHLOROPLATINATE = Reg.item("ammonium_chloroplatinate");
    public static final Item PLATINUM_SPONGE       = Reg.item("platinum_sponge");
    public static final Item PLATINUM_COMPACT      = Reg.item("platinum_compact");
    public static final Item PLATINUM_INGOT        = Reg.item("platinum_ingot");
    // カドミウム（IMP2 S-005、[8-25] ウェストン標準電池の前提）：亜鉛鉱の中に 0.1〜0.5%（シュトロマイヤー 1817）。酸化亜鉛を炭で還元して蒸留すると、亜鉛（沸点 907℃）より先にカドミウム（767℃）が出る
    public static final Item CADMIUM               = Reg.item("cadmium");   // c:ingots/platinum
    // [1-58] 伸線（IMP4-01、S-001 の答え①）：引き板（穴が段々に小さい鉄の板、戻る）で金・銅の線、ダイヤモンドのダイス（戻る）でボンディング用の細い金線
    public static final Item DRAW_PLATE            = Reg.item("draw_plate", com.taku3.semiconductorlab.item.ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item DIAMOND_DIE           = Reg.item("diamond_die", com.taku3.semiconductorlab.item.ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item GOLD_WIRE             = Reg.item("gold_wire");
    public static final Item GOLD_BONDING_WIRE     = Reg.item("gold_bonding_wire");
    public static final Item COPPER_WIRE           = Reg.item("copper_wire");
    public static final Item GRAPHITE              = Reg.item("graphite");
    public static final Item SPHALERITE            = Reg.item("sphalerite");
    public static final Item COAL_ASH              = Reg.item("coal_ash");
    public static final Item ZINC_INGOT            = Reg.item("zinc_ingot");
    public static final Item GERMANIUM_CONCENTRATE = Reg.item("germanium_concentrate");
    public static final Item INDIUM                = Reg.item("indium");
    // --- ゲルマニウムの精錬・精製 ---
    public static final Item GERMANIUM_DIOXIDE     = Reg.item("germanium_dioxide");
    public static final Item RAW_GERMANIUM         = Reg.item("raw_germanium");       // 金属Ge 98%
    // [1-34] 四塩化ゲルマニウム蒸留精製：GeO2 + 4HCl → GeCl4 + 2H2O → 蒸留（沸点83℃）→ GeCl4 + 2H2O → 高純度 GeO2 + 4HCl
    public static final Item CRUDE_GERMANIUM_TETRACHLORIDE = Reg.item("crude_germanium_tetrachloride"); // 粗 GeCl4（他の塩化物が混じった液）
    public static final Item GERMANIUM_TETRACHLORIDE = Reg.item("germanium_tetrachloride");              // 蒸留した GeCl4（無色の液）
    public static final Item HIGH_PURITY_GERMANIUM_DIOXIDE = Reg.item("high_purity_germanium_dioxide");  // 加水分解した高純度 GeO2（水素炉で還元 → 10N まで精製できる Ge）
    public static final Item QUARTZ_STILL          = Reg.item("quartz_still", com.taku3.semiconductorlab.item.QuartzStillItem::new, new Item.Properties().stacksTo(1)); // 石英の蒸留器（作業台で使っても戻る）
    // --- 冶金（[1-26]）---
    public static final Item STEEL_INGOT           = Reg.item("steel_ingot");   // ベッセマー転炉の鋼（C 約0.1〜0.5%）
    public static final Item LITHARGE              = Reg.item("litharge");      // [1-27] 一酸化鉛 PbO（方鉛鉱を焙焼したもの）
    public static final Item LEAD_INGOT            = Reg.item("lead_ingot");    // [1-27] 鉛（PbO を炭素で還元）
    // --- タングステン（[1-28] 粉末冶金。クーリッジ 1909〜1910）---
    // WO3 の本物のルート（タスク09）：鉄マンガン重石＋ソーダ → 融解 → Na2WO4 → 塩酸 → H2WO4 → 焼成 → WO3
    public static final Item WOLFRAMITE_SODA_CHARGE = Reg.item("wolframite_soda_charge"); // 調合物（鉄マンガン重石＋炭酸ナトリウム）
    public static final Item SODIUM_TUNGSTATE      = Reg.item("sodium_tungstate");  // タングステン酸ナトリウム Na2WO4（水に溶ける）
    public static final Item TUNGSTIC_ACID         = Reg.item("tungstic_acid");     // タングステン酸 H2WO4（黄色の沈殿）
    public static final Item TUNGSTEN_TRIOXIDE     = Reg.item("tungsten_trioxide"); // WO3（黄色）
    // [1-23] 輝水鉛鉱・[1-31] モリブデン（IMP2 S-007、[7-17] 多層膜ミラーの前提）：銅の鉱石を掘るとまれに輝水鉛鉱 MoS2（斑岩銅鉱床の副産物）。
    //   反射炉で焙焼 2MoS2 + 7O2 → 2MoO3 + 4SO2 → 水素炉で還元 MoO3 + 3H2 → Mo + 3H2O（イェルム 1781 は炭素で還元）
    public static final float MOLYBDENITE_FROM_COPPER = 0.05f;
    public static final Item MOLYBDENITE           = Reg.item("molybdenite");
    public static final Item MOLYBDENUM_TRIOXIDE   = Reg.item("molybdenum_trioxide");
    public static final Item MOLYBDENUM            = Reg.item("molybdenum");
    public static final Item TUNGSTEN_POWDER       = Reg.item("tungsten_powder");   // 水素還元した W 粉
    public static final Item TUNGSTEN_BAR          = Reg.item("tungsten_bar");      // 焼結棒（水素中で通電焼結）
    public static final Item TUNGSTEN_WIRE         = Reg.item("tungsten_wire");     // 線引きした延性タングステン線
    // [1-16] 塩化水素ガス（乾いたもの、塩酸合成炉に水がないとき）→ トリクロロシラン SiHCl3（純度つき、蒸留で上がる）→ [1-17] 多結晶シリコン（純度つき）
    public static final Item HYDROGEN_CHLORIDE_GAS = Reg.contained("hydrogen_chloride_gas", Vessel.GAS_BAG);
    public static final Item TRICHLOROSILANE       = Reg.item("trichlorosilane");
    // 四塩化ケイ素 SiCl4（IMP2、S-001）：トリクロロシラン反応器の副産物。合成石英 [7-15]・エピタキシー [9-30] の共通の材料
    public static final Item SILICON_TETRACHLORIDE = Reg.item("silicon_tetrachloride");
    public static final Item POLYSILICON           = Reg.item("polysilicon");
    // [1-15] 金属シリコン（アーク炉で SiO2 + 2C → Si + 2CO、純度 98%＝冶金グレード。1個 ≈ 1kg）
    public static final Item METALLURGICAL_SILICON = Reg.item("metallurgical_silicon");
    // [1-46] 珪石（石英鉱脈の SiO2）。酸水素バーナー [7-02] で溶融石英に。ネザークォーツは使わない（化学ルール1）
    public static final Item VEIN_QUARTZ           = Reg.item("vein_quartz");
    public static final Item ROCK_CRYSTAL          = Reg.item("rock_crystal");      // [8-15] 水晶（石英鉱脈からまれに出る透明な単結晶、仮決定 Q1346-2。IMP2 O-016）
    // [1-33] ホウ素：ホウ砂 Na2B4O7·10H2O ＋ 硫酸 → ホウ酸 H3BO3（1個＝2mol）→ 焼く → B2O3（1個＝1mol）
    public static final Item BORAX                 = Reg.item("borax");
    public static final Item BORIC_ACID            = Reg.item("boric_acid");
    public static final Item BORON_TRIOXIDE        = Reg.item("boron_trioxide");
    // --- 水銀（[1-45] 辰砂 HgS の焙焼。HgS + O2 → Hg + SO2）---
    // 容器（IMP-33）：鉄のフラスコ（水銀）・鉛の瓶（フッ酸 [2-10]、ガラスを溶かすので）。中身入りより上に書く
    // [1-06] 塊鉄炉の海綿鉄（ブルーム：溶けずにできたスポンジ状の鉄、スラグ入り）。たき石で叩くと錬鉄（鉄インゴット）
    public static final Item IRON_BLOOM            = Reg.item("iron_bloom");
    // [1-08] 赤熱したブルーム（かまどで熱し直す）。たき石で叩いてスラグを絞り出すと鍛鉄（錬鉄＝鉄インゴット）
    public static final Item HOT_IRON_BLOOM        = Reg.item("hot_iron_bloom");
    // [1-09] 高炉の銑鉄（炭素約4%、溶けて流れ出た鉄。硬いがもろい → 転炉 [1-26] で鋼に）とスラグ（ケイ酸カルシウム）
    public static final Item PIG_IRON              = Reg.item("pig_iron");
    public static final Item BLAST_FURNACE_SLAG    = Reg.item("blast_furnace_slag");
    // [1-11] るつぼ鋼（IMP2 O-010、ハンツマン 1740頃）：耐火粘土のるつぼに鍛鉄＋木炭を入れ、ふたをして完全に溶かす → 炭素のむらがない鋼（C 約1%）
    public static final Item RAW_CRUCIBLE          = Reg.item("raw_crucible");          // 粘土＋焼粉（耐火れんがを砕いた物）で形を作っただけ
    public static final Item CLAY_CRUCIBLE         = Reg.item("clay_crucible", new Item.Properties().stacksTo(16)); // 焼いた耐火粘土のるつぼ
    public static final Item CRUCIBLE_CHARGE       = Reg.item("crucible_charge");       // るつぼ＋鍛鉄×2＋木炭（ふたをした物）
    public static final Item CRUCIBLE_STEEL        = Reg.item("crucible_steel");        // るつぼ鋼のインゴット（C 約1%、焼入れで硬くなる）
    public static final Item IRON_FLASK            = Reg.item("iron_flask");
    public static final Item LEAD_BOTTLE           = Reg.item("lead_bottle");
    public static final Item CINNABAR_DUST         = Reg.item("cinnabar_dust");   // 辰砂（minecraft:cinnabar）を砕いた粉
    public static final Item MERCURY               = Reg.contained("mercury", Vessel.IRON_FLASK);
    // [1-66] タンタル（IMP4-01、S-003 案A）：タンタル石 → マリニャック法の K2TaF7 → ナトリウム還元。還元剤の金属ナトリウムはカストナー法（1886）
    public static final Item SODIUM                = Reg.contained("sodium", Vessel.IRON_FLASK);   // 金属ナトリウム（水で燃えるので鉄の容器に密封）
    public static final Item TANTALITE             = Reg.item("tantalite");                          // タンタル石（コルンブ石）(Fe,Mn)(Ta,Nb)2O6
    public static final Item POTASSIUM_FLUOROTANTALATE = Reg.item("potassium_fluorotantalate");      // K2TaF7（マリニャック法の結晶）
    public static final Item TANTALUM              = Reg.item("tantalum");
    // [1-44] ガリウム（IMP4-01、O-020）：バイヤー法の液を使い回すとたまる → 使い終わった液を電解。融点約30℃、アルミを壊すのでポリエチレン瓶
    public static final Item SPENT_BAYER_LIQUOR    = Reg.item("spent_bayer_liquor", new Item.Properties().stacksTo(16));
    public static final Item GALLIUM               = Reg.contained("gallium", Vessel.POLYETHYLENE_BOTTLE);
    // [B-07] の前提 ヒ素（IMP4-01、S-007）：金属の鉱石（黄鉄鉱・錫石）を焼いた煙道のちり＝白ヒ素 As2O3（反射炉の副産物、A）。毒、工業の材料としてだけ
    public static final Item WHITE_ARSENIC         = Reg.item("white_arsenic");                           // タンタル（還元した粉を固めた金属）
    // レトルトの仕込み：辰砂の粉＋空の鉄のフラスコ（受け器）。焼くと水銀がフラスコに集まる（かまど・反射炉）
    public static final Item CINNABAR_RETORT_CHARGE = Reg.item("cinnabar_retort_charge"); // 液体の金属（容器入り）

    // ===== ブロック =====
    // [1-46] 石英鉱脈（熱水が岩の割れ目に SiO2 を沈めた白い脈）。石英はモース硬度7なので石のツルハシ以上
    public static final Block QUARTZ_VEIN    = Reg.ore("quartz_vein", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
    public static final Block SPHALERITE_ORE = Reg.ore("sphalerite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f).sound(SoundType.STONE));
    // 黒鉛鉱石（変成岩中の天然黒鉛。スリランカの脈状黒鉛など）
    public static final Block GRAPHITE_ORE = Reg.ore("graphite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE));
    // v0.2a 鉱石（産状の近似）：磁鉄鉱=深層岩／砂鉄=浜辺の砂／鉄マンガン重石=花崗岩／方鉛鉱=閃亜鉛鉱と同じ帯
    public static final Block MAGNETITE_ORE  = Reg.ore("magnetite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE));
    public static final Block IRON_SAND      = Reg.ore("iron_sand", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f).sound(SoundType.SAND));
    // [1-09] 高炉の羽口（焼いた粘土の管。ここからふいごの風を炉の中へ吹き込む。高炉の形の一部）
    public static final Block TUYERE         = Reg.block("tuyere", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0f, 6.0f).sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final Block WOLFRAMITE_ORE = Reg.ore("wolframite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE));
    // [1-03] 孔雀石（IMP2-01）：銅鉱床の上の、地表に近い酸化帯。浅い石の中（y40〜110）。モース硬度 3.5〜4
    public static final Block MALACHITE_ORE  = Reg.ore("malachite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
    // [1-22] 錫石（IMP2-01）：花崗岩の熱水鉱脈（鉄マンガン重石といっしょ）。固い（モース硬度 6〜7）
    public static final Block CASSITERITE_ORE = Reg.ore("cassiterite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE));
    public static final Block SPODUMENE_ORE = Reg.ore("spodumene_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE));   // [1-42] リチア輝石（花崗岩の中のまれな鉱脈、IMP3 S-006）
    public static final Block GALENA_ORE     = Reg.ore("galena_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
    public static final Block STIBNITE_ORE   = Reg.ore("stibnite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(2.5f, 3.0f).sound(SoundType.STONE));   // [1-43] 輝安鉱（方鉛鉱と同じ熱水鉱脈の帯）
    public static final Block PENTLANDITE_ORE = Reg.ore("pentlandite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops().strength(4.0f, 3.0f).sound(SoundType.DEEPSLATE));   // [1-37] ペントランド鉱（深い所、IMP3）
    public static final Block TANTALITE_ORE = Reg.ore("tantalite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(4.0f, 3.0f).sound(SoundType.STONE));   // [1-66] タンタル石（花崗岩ペグマタイト、リチア輝石よりまれ、IMP4-01）
    public static final Block PYROLUSITE_ORE = Reg.ore("pyrolusite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));   // 軟マンガン鉱（浅い石の中、IMP3 S-006）
    public static final Block FLUORITE_ORE   = Reg.ore("fluorite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
    public static final Block BAUXITE_ORE    = Reg.ore("bauxite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).requiresCorrectToolForDrops().strength(1.5f, 3.0f).sound(SoundType.GRAVEL));
    public static final Block BASTNAESITE_ORE = Reg.ore("bastnaesite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));   // [1-19] バストネス石（IMP3 S-009）
    public static final Block ION_ADSORPTION_CLAY = Reg.ore("ion_adsorption_clay", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PINK).strength(0.8f, 1.0f).sound(SoundType.GRAVEL));   // [1-19] イオン吸着型の粘土（ジャングル・サバンナ、IMP3 S-009）
    public static final Block CHROMITE_ORE   = Reg.ore("chromite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(4.0f, 3.0f).sound(SoundType.DEEPSLATE));
    // [2-03] トロナ堆積（干上がった塩湖の蒸発岩 Na2CO3·NaHCO3·2H2O）。砂漠の地表〜浅い地下の砂・砂岩に生成。軟らかい（モース硬度 2.5〜3）
    public static final Block BORAX_DEPOSIT  = Reg.ore("borax_deposit", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(1.0f).sound(SoundType.STONE));   // [1-33] ホウ砂（砂漠の塩湖の蒸発岩）

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // ベッセマー転炉 [1-26]（鉄の殻＋粘土の内張り＋底の羽口。送風は電動）。送風中は炉口が光る
    public static final Machine<BessemerConverterBlockEntity> BESSEMER_CONVERTER = Machine.electric("bessemer_converter", BessemerConverterBlockEntity::new, BessemerConverterBlockEntity.LAYOUT, lit(metal(3.5f), 13));
    // 水素炉 [1-28]（耐火れんがの管状炉＋水素の流れ。WO3 の水素還元と W 粉の通電焼結）。加熱中は光る
    public static final Machine<HydrogenFurnaceBlockEntity> HYDROGEN_FURNACE = Machine.electric("hydrogen_furnace", HydrogenFurnaceBlockEntity::new, HydrogenFurnaceBlockEntity.LAYOUT, lit(metal(3.5f), 10));
    // 第2章 シリコン（[1-16] [1-17] [9-08] [9-12] [9-16] [3-15]）
    public static final Machine<TrichlorosilaneReactorBlockEntity> TRICHLOROSILANE_REACTOR = Machine.electric("trichlorosilane_reactor", TrichlorosilaneReactorBlockEntity::new, TrichlorosilaneReactorBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<SiemensReactorBlockEntity> SIEMENS_REACTOR = Machine.electric("siemens_reactor", SiemensReactorBlockEntity::new, SiemensReactorBlockEntity.LAYOUT, metal(4.0f));

    // バイヤー法の溶解釜 [1-14]（IMP4-01）：ボーキサイト＋NaOH の液 → 水酸化アルミニウム＋赤泥（2kW）
    public static final Machine<BayerDigesterBlockEntity> BAYER_DIGESTER = Machine.electric("bayer_digester", BayerDigesterBlockEntity::new, BayerDigesterBlockEntity.LAYOUT, metal(3.5f));
    // [1-06] 塊鉄炉（ブルーマリー）：木炭＋鉄鉱石・砂鉄 → ブルーム。横のふいご [3-04] で風を送らないと還元の温度に届かない
    public static final Machine<BloomeryBlockEntity> BLOOMERY = Machine.builder("bloomery", BloomeryBlockEntity::new)
            .ticker(BloomeryBlockEntity::tick).layout(BloomeryBlockEntity.LAYOUT).props(lit(stone(3.0f), 12)).build();
    // [1-09] 高炉（最初のマルチブロック）：この制御ブロック（炉床の湯口）のまわりに れんが 3×3×4・2段目に羽口4つ。羽口の外に水車ふいご
    public static final Machine<BlastFurnaceBlockEntity> BLAST_FURNACE = Machine.builder("blast_furnace_hearth", BlastFurnaceBlockEntity::new)
            .ticker(BlastFurnaceBlockEntity::tick).layout(BlastFurnaceBlockEntity.LAYOUT).props(lit(stone(3.5f), 14)).build();
    // ホール・エルー電解槽 [1-29]（IMP4-01）：アルミナ＋氷晶石の浴＋炭素の陽極 → アルミニウム（100kW、15kWh/個）
    public static final Machine<HallHeroultCellBlockEntity> HALL_HEROULT_CELL = Machine.electric("hall_heroult_cell", HallHeroultCellBlockEntity::new, HallHeroultCellBlockEntity.LAYOUT, lit(metal(4.0f), 7));

    // マグネシウムの電解槽 [1-39]（IMP3、O-018、ダウ法）：無水 MgCl2 → Mg＋塩素。10kW
    // [1-66] カストナーの電解槽（IMP4-01）：溶けた NaOH の電解 → 金属ナトリウム。2kW
    public static final Machine<CastnerCellBlockEntity> CASTNER_CELL = Machine.electric("castner_cell", CastnerCellBlockEntity::new, CastnerCellBlockEntity.LAYOUT, metal(4.0f));
    // [1-66] タンタルの精製槽（IMP4-01）：マリニャック法（フッ酸＋KF → K2TaF7）とナトリウム還元。3kW
    public static final Machine<TantalumRefineryBlockEntity> TANTALUM_REFINERY = Machine.electric("tantalum_refinery", TantalumRefineryBlockEntity::new, TantalumRefineryBlockEntity.LAYOUT, metal(4.0f));
    // [1-44] ガリウムの電解槽（IMP4-01）：使い終わったバイヤー法の液 4 → ガリウム＋NaOH。1kW
    public static final Machine<GalliumCellBlockEntity> GALLIUM_CELL = Machine.electric("gallium_cell", GalliumCellBlockEntity::new, GalliumCellBlockEntity.LAYOUT, metal(3.5f));
    public static final Machine<MagnesiumCellBlockEntity> MAGNESIUM_CELL = Machine.electric("magnesium_cell", MagnesiumCellBlockEntity::new, MagnesiumCellBlockEntity.LAYOUT, metal(4.0f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {
        // [1-23] 銅の鉱石（ふつう・深層）を掘ると、まれに輝水鉛鉱（IMP2 S-007。決定どおり「銅の鉱山のついで」）
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            if (!table.is(net.minecraft.world.level.block.Blocks.COPPER_ORE.getLootTable().orElseThrow())
                    && !table.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE.getLootTable().orElseThrow())) return;
            if (drops.stream().anyMatch(st -> st.is(net.minecraft.world.item.Items.RAW_COPPER)) && ctx.getRandom().nextFloat() < MOLYBDENITE_FROM_COPPER)
                drops.add(new net.minecraft.world.item.ItemStack(MOLYBDENITE));
        });
        // [1-41] 浜の砂鉄には、黒い粒のチタン鉄鉱がまじっている（決定：砂鉄の副産物、IMP3 O-018）
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            if (!table.is(IRON_SAND.getLootTable().orElseThrow())) return;
            if (drops.stream().anyMatch(st -> st.is(MAGNETITE)) && ctx.getRandom().nextFloat() < ILMENITE_FROM_IRON_SAND)
                drops.add(new net.minecraft.world.item.ItemStack(ILMENITE));
        });
        // [1-70] 浜の重い砂には、ジルコンもまじっている（チタン鉄鉱の前例どおり、IMP3 S-007）
        net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            if (!table.is(IRON_SAND.getLootTable().orElseThrow())) return;
            if (drops.stream().anyMatch(st -> st.is(MAGNETITE)) && ctx.getRandom().nextFloat() < ZIRCON_FROM_IRON_SAND)
                drops.add(new net.minecraft.world.item.ItemStack(ZIRCON));
        });
    }
}
