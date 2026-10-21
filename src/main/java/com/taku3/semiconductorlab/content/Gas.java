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
 * 幹6 ガス・電解（docs/TECH_TREE.md の「幹6」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/gas/、src/gametest/.../GasTests.java）だけを書き換える。
 */
public final class Gas {
    private Gas() {}

    // ===== アイテム =====
    // [6-16] 天然硝石（IMP2-02）：鍾乳洞の硝石土 ×4＋木灰＋水 → 硝石の浸出液 →かまど（煮つめる）→ 硝石 KNO3
    public static final Item NITRE_EARTH           = Reg.item("nitre_earth");       // 硝石土（硝酸カルシウムなどを含む洞窟の土）
    public static final Item NITRE_LIQUOR          = Reg.item("nitre_liquor", new Item.Properties().stacksTo(16)); // 硝石の浸出液
    public static final Item SALTPETER             = Reg.item("saltpeter");         // 硝石 KNO3（工業の材料。火薬の配合は作らない＝DESIGN「危ない技術の扱い」）
    // [6-11] 硝酸（IMP2-02）：硝石＋硫酸を石英の蒸留器で蒸留（KNO3 + H2SO4 → HNO3 + KHSO4）→ ガラス瓶入りの硝酸。工業の材料としてだけ
    public static final Item NITRIC_ACID           = Reg.contained("nitric_acid", Vessel.GLASS_BOTTLE);
    // 硝酸銀（IMP2-02）：銀＋硝酸（3Ag + 4HNO3 → 3AgNO3 + NO + 2H2O）。写真 [B-02]・銀鏡 [7-05] 段階2 の材料
    public static final Item SILVER_NITRATE        = Reg.item("silver_nitrate");
    public static final Item HYDROCHLORIC_ACID     = Reg.contained("hydrochloric_acid", Vessel.GLASS_BOTTLE); // [6-06] 塩酸（HCl 約25%、ガラス瓶入り）
    // --- ガス（v0.2a [6-01] 電解槽の生成物）---
    // 「袋詰め」のガス（約1mol）。貯蔵は [6-02] ガスホルダー、高圧の持ち運びは [6-03] ボンベ
    public static final Item HYDROGEN_GAS          = Reg.contained("hydrogen_gas", Vessel.GAS_BAG);
    public static final Item OXYGEN_GAS            = Reg.contained("oxygen_gas", Vessel.GAS_BAG);
    // 高圧ボンベ（[6-03] 圧縮機。鋼製・約150気圧・1本 50mol）
    public static final Item EMPTY_GAS_CYLINDER    = Reg.item("empty_gas_cylinder", new Item.Properties().stacksTo(16));
    public static final Item HYDROGEN_CYLINDER     = Reg.contained("hydrogen_cylinder", Vessel.STEEL_CYLINDER);
    public static final Item OXYGEN_CYLINDER       = Reg.contained("oxygen_cylinder", Vessel.STEEL_CYLINDER);
    // [6-13] 接触法硫酸（IMP3、S-011）：白金2 → 白金の網（触媒、戻る）。SO2＋O2＋白金の網（1831）か五酸化バナジウム（1915〜）→ 三酸化硫黄。
    //   SO3＋硫酸＋鉄のびん → 発煙硫酸（オレウム、鉄のびん入り：濃い硫酸は鉄を侵さない）。発煙硫酸＋水＋ガラス瓶2 → 硫酸2（SO3 1つで硫酸が1つ増える）
    public static final Item PLATINUM_GAUZE        = Reg.item("platinum_gauze", com.taku3.semiconductorlab.item.CatalystItem::new, new Item.Properties().stacksTo(1));
    public static final Item SULFUR_TRIOXIDE       = Reg.item("sulfur_trioxide", new Item.Properties().stacksTo(16));
    public static final Item OLEUM                 = Reg.contained("oleum", com.taku3.semiconductorlab.machine.Vessel.IRON_FLASK);
    // [6-12] 鉛室法（IMP3、O-027、仮決定 Q3-19 ①：今の「SO2＋水 → 硫酸」は置きかえず、別の道）：鉛8 → 鉛の部屋（戻る）。
    //   SO2 2＋水＋硝石（窒素酸化物のもと）＋鉛の部屋＋ガラス瓶2 → 鉛室の酸2（約65%、ときどき NO2 がもれる事故）→ 1つずつ煮つめて硫酸
    public static final Item LEAD_CHAMBER          = Reg.item("lead_chamber", com.taku3.semiconductorlab.item.CatalystItem::new, new Item.Properties().stacksTo(1));
    public static final Item CHAMBER_ACID          = Reg.item("chamber_acid", com.taku3.semiconductorlab.item.ChamberAcidItem::new, new Item.Properties().stacksTo(16));
    // [6-05] クロルアルカリ電解：2NaCl + 2H2O → Cl2 + H2 + 2NaOH
    public static final Item CHLORINE_GAS          = Reg.contained("chlorine_gas", Vessel.GAS_BAG);      // 塩素（黄緑色の有毒な気体）。1袋＝約1mol
    public static final Item CHLORINE_CYLINDER     = Reg.contained("chlorine_cylinder", Vessel.STEEL_CYLINDER); // 塗色は黄（日本の規格）
    public static final Item SODIUM_HYDROXIDE      = Reg.item("sodium_hydroxide");  // 水酸化ナトリウム NaOH（苛性ソーダ）
    // [6-07] 空気分離（リンデ）：空気 → N2 78% ＋ O2 21% ＋ Ar 0.93%
    public static final Item NITROGEN_GAS          = Reg.contained("nitrogen_gas", Vessel.GAS_BAG);      // 窒素 N2。1袋＝約1mol
    public static final Item ARGON_GAS             = Reg.contained("argon_gas", Vessel.GAS_BAG);         // アルゴン Ar。1袋＝約1mol
    public static final Item NITROGEN_CYLINDER     = Reg.contained("nitrogen_cylinder", Vessel.STEEL_CYLINDER); // 塗色はねずみ色（日本の規格「その他のガス」）
    public static final Item ARGON_CYLINDER        = Reg.contained("argon_cylinder", Vessel.STEEL_CYLINDER);    // 同じくねずみ色
    // [6-08] 希ガス（IMP2 O-018）：希ガスの精留塔で液体空気の酸素・窒素のたまりから。1袋＝約1mol
    public static final Item NEON_GAS              = Reg.contained("neon_gas", Vessel.GAS_BAG);
    public static final Item KRYPTON_GAS           = Reg.contained("krypton_gas", Vessel.GAS_BAG);
    public static final Item XENON_GAS             = Reg.contained("xenon_gas", Vessel.GAS_BAG);
    // [7-14] の前提（IMP2 O-019）：二酸化炭素。石灰石（方解石）を反射炉で焼くと煙道に集まる CaCO3 → CaO + CO2（[0-10] の副産物）
    public static final Item CARBON_DIOXIDE_GAS    = Reg.contained("carbon_dioxide_gas", Vessel.GAS_BAG);
    public static final Item NEON_CYLINDER         = Reg.contained("neon_cylinder", Vessel.STEEL_CYLINDER);
    public static final Item KRYPTON_CYLINDER      = Reg.contained("krypton_cylinder", Vessel.STEEL_CYLINDER);
    // [6-14] フッ素（IMP2 O-018）：袋（ゴム）はフッ素に侵されるので、表面がフッ化物の膜で守られる鋼のボンベに直接詰める
    public static final Item FLUORINE_CYLINDER     = Reg.contained("fluorine_cylinder", Vessel.STEEL_CYLINDER);
    public static final Item XENON_CYLINDER        = Reg.contained("xenon_cylinder", Vessel.STEEL_CYLINDER);
    // --- 塩（[6-04] 岩塩・[0-16] 塩田）。岩塩でも海水でも同じ NaCl ---
    public static final Item ROCK_SALT             = Reg.item("rock_salt");         // 岩塩の原石（蒸発岩、NaCl 95〜99%）
    public static final Item SALT                  = Reg.item("salt");              // 塩 NaCl（クロルアルカリ電解 [6-05] の原料）

    // ===== ブロック =====
    // [6-04] 岩塩鉱（古い海が干上がってできた蒸発岩の層）。砂漠・荒野の地下の石に生成。軟らかい（モース硬度 2.5）
    public static final Block ROCK_SALT_ORE  = Reg.ore("rock_salt_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(1.5f, 3.0f).sound(SoundType.STONE));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 電解槽 [6-01]（ガラス槽＋黒鉛電極）。電気を消費する機械の第1号
    public static final Machine<ElectrolyzerBlockEntity> ELECTROLYZER = Machine.electric("electrolyzer", ElectrolyzerBlockEntity::new, ElectrolyzerBlockEntity.LAYOUT, metal(3.0f).sound(SoundType.GLASS));
    // ガスホルダー [6-02]（水封式の低圧ガスタンク。鉄のベル＋水槽）。電気は使わない
    public static final Machine<GasHolderBlockEntity> GAS_HOLDER = Machine.builder("gas_holder", GasHolderBlockEntity::new)
            .block(MachineEntityBlock::new).ticker(GasHolderBlockEntity::tick).layout(GasHolderBlockEntity.LAYOUT).props(metal(3.0f)).build();
    // 塩酸合成炉 [6-06]（酸水素バーナーの派生。石英の火口で水素を塩素中で燃やし H2 + Cl2 → 2HCl、吸収塔で水に溶かす）。ガス式（電気の受け口なし）。燃焼中は光る
    public static final Machine<HclBurnerBlockEntity> HCL_BURNER = Machine.builder("hcl_burner", HclBurnerBlockEntity::new)
            .ticker(HclBurnerBlockEntity::tick).layout(HclBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f).sound(SoundType.GLASS), 10)).build();
    // [6-14] フッ素の電解槽（IMP2 O-018、1886 モアッサン）：KF＋フッ酸を黒鉛の陽極で電解 → F2 のボンベ・H2 の袋（2kW）
    public static final Machine<FluorineCellBlockEntity> FLUORINE_CELL = Machine.electric("fluorine_cell", FluorineCellBlockEntity::new, FluorineCellBlockEntity.LAYOUT, metal(3.5f));
    // [6-08] 希ガスの精留塔（IMP2 O-018）：酸素1袋＋窒素1袋 → Kr・Xe・Ne を少しずつ（2kW）
    public static final Machine<RareGasColumnBlockEntity> RARE_GAS_COLUMN = Machine.electric("rare_gas_column", RareGasColumnBlockEntity::new, RareGasColumnBlockEntity.LAYOUT, metal(3.5f));
    public static final Machine<AirSeparationBlockEntity> AIR_SEPARATION_UNIT = Machine.electric("air_separation_unit", AirSeparationBlockEntity::new, AirSeparationBlockEntity.LAYOUT, metal(3.5f));
    // 圧縮機 [6-03]（電動モーターのピストン圧縮機。隣のガスホルダーのガスを鋼のボンベに 50mol ずつ詰める）
    public static final Machine<CompressorBlockEntity> COMPRESSOR = Machine.electric("compressor", CompressorBlockEntity::new, CompressorBlockEntity.LAYOUT, metal(3.5f));

    // [6-16] 硝石土（IMP2-02）：鍾乳石の洞窟（石灰岩の洞窟）の鍾乳石ブロックを置き換える、やわらかい土（シャベル）
    public static final Block NITRE_EARTH_BLOCK = Reg.ore("nitre_earth_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.8f).sound(SoundType.GRAVEL));
    // [6-10] 硝石床（IMP3、S-011）：土＋腐った肉2＋骨粉＋消石灰 → 硝石床。時間がたつと（細菌の硝化）硝石土のブロックになる
    public static final Block NITRE_BED = Reg.block("nitre_bed", com.taku3.semiconductorlab.block.NitreBedBlock::new,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.DIRT).strength(0.6f).sound(net.minecraft.world.level.block.SoundType.GRAVEL).randomTicks());

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    // [6-09] ハーバー・ボッシュ法（IMP3、S-005）：磁鉄鉱＋カリ＋アルミナ → 鉄の触媒（ミタッシュ）。反応塔：窒素1＋水素3 → アンモニア2（触媒は減らない）。
    //   アンモニア2＋硫酸 → 硫酸アンモニウム（化学肥料：耕地を窒素の多い耕地に）、アンモニア＋水＋瓶 → アンモニア水
    public static final Item IRON_CATALYST         = Reg.item("iron_catalyst", new Item.Properties().stacksTo(1));
    public static final Item AMMONIA_GAS           = Reg.contained("ammonia_gas", Vessel.GAS_BAG);
    public static final Item AMMONIUM_SULFATE      = Reg.item("ammonium_sulfate", com.taku3.semiconductorlab.item.FertilizerItem::new, new Item.Properties());
    public static final Machine<HaberBoschReactorBlockEntity> HABER_BOSCH_REACTOR = Machine.electric("haber_bosch_reactor", HaberBoschReactorBlockEntity::new, HaberBoschReactorBlockEntity.LAYOUT, metal(5.0f));

    static void init() {}
}
