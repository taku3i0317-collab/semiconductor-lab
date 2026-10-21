package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.item.*;
import com.taku3.semiconductorlab.machine.Machine;
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
 * 幹9 半導体プロセス（docs/TECH_TREE.md の「幹9」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/semiconductor/、src/gametest/.../SemiconductorTests.java）だけを書き換える。
 */
public final class Semiconductor {
    private Semiconductor() {}

    // ===== アイテム =====
    public static final Item REFINED_GERMANIUM     = Reg.item("refined_germanium", new Item.Properties().component(ModDataComponents.PURITY, Purity.SIX_N));   // 帯域精製後
    // --- ドーピング ---
    public static final Item N_TYPE_GERMANIUM      = Reg.item("n_type_germanium");
    public static final Item P_TYPE_GERMANIUM      = Reg.item("p_type_germanium");
    // --- 帯域精製炉の部品（Pfann 1952 の装置構成）---
    public static final Item GRAPHITE_BOAT_BLANK   = Reg.item("graphite_boat_blank"); // 黒鉛+粘土、未焼成
    public static final Item GRAPHITE_BOAT         = Reg.item("graphite_boat");          // 粘土黒鉛ボート（19世紀式、9N まで）
    public static final Item SYNTHETIC_GRAPHITE_BOAT = Reg.item("synthetic_graphite_boat"); // 人造黒鉛ボート（[3-10] アチソン炉の黒鉛、10N まで）
    // [9-07] Ge 単結晶の引き上げ：種結晶（W線から粒の選択で作る、引き上げでは減らない）と単結晶インゴット（純度・経路を受け継ぐ）
    public static final Item GERMANIUM_SEED_CRYSTAL   = Reg.item("germanium_seed_crystal");
    public static final Item GERMANIUM_SINGLE_CRYSTAL = Reg.item("germanium_single_crystal", new Item.Properties().stacksTo(16));
    // [9-08] CZ 法：石英るつぼ（使い捨て）→ シリコン種結晶 → シリコン単結晶インゴット（純度・ドーパントつき）
    public static final Item QUARTZ_CRUCIBLE       = Reg.item("quartz_crucible");
    public static final Item SILICON_SEED_CRYSTAL  = Reg.item("silicon_seed_crystal");
    public static final Item SILICON_SINGLE_CRYSTAL = Reg.item("silicon_single_crystal", new Item.Properties().stacksTo(16));
    // [9-12] ウェハー切断：ダイヤモンドの刃（減る）→ シリコンの板（純度・ドーパントつき）
    public static final Item DIAMOND_SAW_BLADE     = Reg.item("diamond_saw_blade");
    public static final Item SILICON_WAFER         = Reg.item("silicon_wafer");
    // [9-16] 熱拡散：p-n 接合の板（純度と土台のドーパントつき）
    public static final Item PN_JUNCTION_WAFER     = Reg.item("pn_junction_wafer");
    // [9-49] サイリスタ（IMP4-01、O-015）：p-n 接合の板（実は両面 p＝p-n-p）にもう一度リン → p-n-p-n の板 → 素子
    public static final Item THYRISTOR_WAFER       = Reg.item("thyristor_wafer");
    public static final Item THYRISTOR             = Reg.item("thyristor");
    // high-k（IMP4-01、S-004）：ALD の原料 HfCl4（D の HfO2 を炭素塩素化）。湿気で HCl を出すので瓶に密封
    // [9-42] 画像センサー・[9-43] MEMS（IMP4-01、O-019 仮決定の案A）：手に持って光の電子の数・加速度を見る素子。組立台で
    public static final Item IMAGE_SENSOR          = Reg.item("image_sensor", com.taku3.semiconductorlab.item.ImageSensorItem::new, new Item.Properties().stacksTo(1));
    public static final Item MEMS_WAFER            = Reg.item("mems_wafer");          // ばねとおもりを深く削り出した板
    public static final Item MEMS_ACCELEROMETER    = Reg.item("mems_accelerometer", com.taku3.semiconductorlab.item.MemsAccelerometerItem::new, new Item.Properties().stacksTo(1));
    // EUV のマスク（IMP4-01、S-008）：反射型。C の多層膜ミラー（40対以上）にスパッタでタンタルの吸収体 → マスクの板。模様は C の縮小カメラ（A の MaskData）→ euv_mask
    public static final Item EUV_MASK_BLANK        = Reg.item("euv_mask_blank", new Item.Properties().stacksTo(1));
    public static final Item EUV_MASK              = Reg.item("euv_mask", new Item.Properties().stacksTo(1));
    public static final Item HAFNIUM_TETRACHLORIDE = Reg.contained("hafnium_tetrachloride", com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE);
    // [9-15] 熱酸化：酸化膜つきの板（純度・ドーパント・膜の厚さ oxide_nm つき）
    public static final Item OXIDIZED_WAFER        = Reg.item("oxidized_wafer");
    // [9-17] メサトランジスタ（IMP4-01）：p-n 接合の板（p の板＋n の表面）→ Al の点を蒸着 → 焼いて合金（エミッタ）→ ろうで守る → 混酸で周りを削る（メサ）→ 金線でつなぐ
    public static final Item AL_DOTTED_WAFER       = Reg.item("al_dotted_wafer");
    public static final Item ALLOYED_WAFER         = Reg.item("alloyed_wafer");
    public static final Item WAXED_WAFER           = Reg.item("waxed_wafer");
    public static final Item MESA_WAFER            = Reg.item("mesa_wafer");
    public static final Item MESA_TRANSISTOR       = Reg.item("mesa_transistor", MesaTransistorItem::new, new Item.Properties().stacksTo(16));
    // 露光とマスク（O-002、docs/LITHO_DESIGN.md）：加工中のウェハー（chip_state つき。レジストを塗ると酸化膜つきの板がこれになる）
    public static final Item PROCESS_WAFER         = Reg.item("process_wafer");
    // [9-27] 四フッ化炭素（IMP4-01）：ドライエッチングのガス。フッ素化の反応器で 黒鉛＋F2
    public static final Item CARBON_TETRAFLUORIDE  = Reg.contained("carbon_tetrafluoride", com.taku3.semiconductorlab.machine.Vessel.GAS_BAG);
    // [9-30] シラン（IMP4-01、S-002：層間膜の CVD の原料）。Mg2Si＋塩酸。空気中で自然に燃えるのでボンベ
    public static final Item MAGNESIUM_SILICIDE    = Reg.item("magnesium_silicide");
    public static final Item SILANE_CYLINDER       = Reg.contained("silane_cylinder", com.taku3.semiconductorlab.machine.Vessel.STEEL_CYLINDER);
    // [9-32] ダイシング 段階1（A、O-013）：ダイヤモンドのけがき針で傷を付けて割る（1960ごろ）→ チップ（chip_state を引き継ぐ）
    public static final Item DIAMOND_SCRIBE        = Reg.item("diamond_scribe", com.taku3.semiconductorlab.item.DiamondScribeItem::new, new Item.Properties().durability(64));
    public static final Item CHIP                  = Reg.item("chip");
    // [9-45] ベイヤー配列のカラーフィルター（IMP2 O-031、1976）：顔料を混ぜたレジスト（赤＝ベンガラ・緑＝酸化クロム・青＝⚠️硫酸銅で近似、本物は有機顔料）を
    //   2×2 の「緑・赤／青・緑」に並べた膜。E の画像センサー [9-42] の素子に載せて色の分かる素子に（組み立ては E の ID が決まってから）
    public static final Item RED_COLOR_RESIST      = Reg.item("red_color_resist", new Item.Properties().stacksTo(16));
    public static final Item GREEN_COLOR_RESIST    = Reg.item("green_color_resist", new Item.Properties().stacksTo(16));
    public static final Item BLUE_COLOR_RESIST     = Reg.item("blue_color_resist", new Item.Properties().stacksTo(16));
    public static final Item BAYER_FILTER          = Reg.item("bayer_filter");
    // 色の分かる画像センサー（[9-45]、IMP2 O-031）：E の画像センサーの素子＋ベイヤーのフィルター。表示は E の ImageSensorItem と同じ
    public static final Item COLOR_IMAGE_SENSOR    = Reg.item("color_image_sensor", com.taku3.semiconductorlab.item.ImageSensorItem::new, new Item.Properties().stacksTo(1));
    // [9-33] ワイヤーボンディング（IMP4-01）：ヘッダー（鋼の台にガラスで封じた足）、線をつないだチップ（CHIP_STATE を写す）
    public static final Item CHIP_HEADER           = Reg.item("chip_header");
    public static final Item BONDED_CHIP           = Reg.item("bonded_chip");
    // [9-34] 封止（IMP4-01）：金属缶（鋼）、金属缶の IC・プラスチック（エポキシ）の IC（CHIP_STATE を写す）
    public static final Item CAN_CAP               = Reg.item("can_cap");
    public static final Item CAN_PACKAGED_CHIP     = Reg.item("can_packaged_chip");
    public static final Item PLASTIC_PACKAGED_CHIP = Reg.item("plastic_packaged_chip");
    // [9-47] 加速器の部品（IMP4-01、O-011）：コッククロフト・ウォルトン回路（整流器とコンデンサーのはしご、電圧を何倍にも）と加速管（ガラスと金属の輪を重ねた真空の管）
    public static final Item CW_MULTIPLIER         = Reg.item("cw_multiplier");
    public static final Item ACCELERATOR_TUBE      = Reg.item("accelerator_tube");
    // v0.3b（A、O-016 ③）：ハンコ。お題を解いたチップの形（成分 chip_design、切り抜き）。製図台で何度でも押せる（減らない）
    public static final Item STAMP                 = Reg.item("stamp", new Item.Properties().stacksTo(1));
    // [9-32] 段階2（A、O-014）：ダイシングソーの刃（ダイヤモンドの粒を青銅で固めた薄い円盤、32枚で減る）
    public static final Item DIAMOND_BLADE         = Reg.item("diamond_blade", new Item.Properties().durability(com.taku3.semiconductorlab.block.DicingSawBlockEntity.BLADE_USES));
    // 製図台の設計図（方眼紙、成分 chip_design）と原図（ルビリス、成分 mask）。⚠️ 材料なしで製図台から出る（INBOX S-001）
    public static final Item LAYOUT_SHEET          = Reg.item("layout_sheet", new Item.Properties().stacksTo(1));
    public static final Item RUBYLITH              = Reg.item("rubylith", new Item.Properties().stacksTo(1));
    // --- トランジスタと製品 ---
    public static final Item TRANSISTOR            = Reg.item("transistor");           // 点接触型 (1947)
    public static final Item JUNCTION_TRANSISTOR_BLANK = Reg.item("junction_transistor_blank"); // 合金化前
    public static final Item JUNCTION_TRANSISTOR   = Reg.item("junction_transistor");  // 合金接合型 (1952)

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 帯域精製炉（Pfann, 1952）。電気で動く（組み込みの高周波発振器 [4-09]）。点灯中は光る
    public static final Machine<ZoneRefiningFurnaceBlockEntity> ZONE_REFINING_FURNACE = Machine.electric("zone_refining_furnace", ZoneRefiningFurnaceBlockEntity::new, ZoneRefiningFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 13));
    public static final Machine<CzPullerBlockEntity> CZ_PULLER = Machine.electric("cz_puller", CzPullerBlockEntity::new, CzPullerBlockEntity.LAYOUT, metal(4.0f));
    // 製図台（O-002、docs/LITHO_DESIGN §7）：方眼に層ごとの模様を描き、原図に切り出す。電気なし
    public static final Machine<DraftingTableBlockEntity> DRAFTING_TABLE = Machine.builder("drafting_table", DraftingTableBlockEntity::new)
            .layout(DraftingTableBlockEntity.LAYOUT)
            .props(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD).strength(2.5f).sound(net.minecraft.world.level.block.SoundType.WOOD)).build();
    // 検査台（プローブ、O-002）：パッドに電圧をかけて電流を見る、つながり方を判定して「発見」を知らせる
    public static final Machine<ProbeStationBlockEntity> PROBE_STATION = Machine.builder("probe_station", ProbeStationBlockEntity::new)
            .layout(ProbeStationBlockEntity.LAYOUT).props(metal(3.0f)).build();
    public static final Machine<WaferSawBlockEntity> WAFER_SAW = Machine.electric("wafer_saw", WaferSawBlockEntity::new, WaferSawBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<DiffusionFurnaceBlockEntity> DIFFUSION_FURNACE = Machine.electric("diffusion_furnace", DiffusionFurnaceBlockEntity::new, DiffusionFurnaceBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<OxidationFurnaceBlockEntity> OXIDATION_FURNACE = Machine.electric("oxidation_furnace", OxidationFurnaceBlockEntity::new, OxidationFurnaceBlockEntity.LAYOUT, metal(4.0f));
    // [9-06] エッチング槽（IMP4-01）：鉛張りの槽にフッ酸。酸化膜だけを溶かす（室温、電気なし）
    public static final Machine<EtchTankBlockEntity> ETCH_TANK = Machine.builder("etch_tank", EtchTankBlockEntity::new)
            .ticker(EtchTankBlockEntity::tick).layout(EtchTankBlockEntity.LAYOUT).props(metal(3.0f)).build();
    // [9-13] 研磨機（IMP4-01）：砥粒でラッピング → コロイダルシリカで鏡面。加工中のウェハーを最初からやり直す（ChipOps.reclaim）。1kW
    public static final Machine<WaferPolisherBlockEntity> WAFER_POLISHER = Machine.electric("wafer_polisher", WaferPolisherBlockEntity::new, WaferPolisherBlockEntity.LAYOUT, metal(3.5f));
    // [9-14] 洗浄槽 段階1（IMP4-01）：硝酸で煮る → フッ酸 → 蒸留水ですすぐ（ChipOps.clean）。1kW
    public static final Machine<CleaningBenchBlockEntity> CLEANING_BENCH = Machine.electric("cleaning_bench", CleaningBenchBlockEntity::new, CleaningBenchBlockEntity.LAYOUT, metal(3.0f));
    // [9-20] マスクアライナー（密着・近接露光、IMP2 O-011）：レジストを塗ったウェハー＋フォトマスク → g線で露光（ChipOps.expose、CONTACT_G）。画面のボタンですき間（密着／20µm／80µm）
    public static final Machine<MaskAlignerBlockEntity> MASK_ALIGNER = Machine.electric("mask_aligner", MaskAlignerBlockEntity::new, MaskAlignerBlockEntity.LAYOUT, metal(3.5f));
    // [9-39] 縮小投影露光（ステッパー、IMP2 S-003・S-004）：投影レンズで 1/5 に縮めて1チップずつ送って焼く（段階1＝手で送る、段階2＝[10-15] の自動送り）。g線、方眼16
    public static final Machine<StepperBlockEntity> STEPPER = Machine.electric("stepper", StepperBlockEntity::new, StepperBlockEntity.LAYOUT, metal(4.0f));
    // スキャナー [9-39] の続き（IMP2 O-021、ステップ・アンド・スキャン 1990年代）：ステッパー＋ボールねじ×2（マスクの台）。細いスリットでマスクと板を一緒に動かすので、同じレンズで CD が細くなる
    public static final Machine<ScannerBlockEntity> SCANNER = Machine.electric("scanner", ScannerBlockEntity::new, ScannerBlockEntity.LAYOUT, metal(4.0f));
    // [9-09] FZ 炉（IMP4-01）：るつぼなしで帯を宙に保つ。酸素が少ない単結晶（20kW、誘導加熱、Ar）
    public static final Machine<FzPullerBlockEntity> FZ_PULLER = Machine.electric("fz_puller", FzPullerBlockEntity::new, FzPullerBlockEntity.LAYOUT, metal(4.0f));
    // [9-19] レジスト塗布機（スピンコーター、IMP4-01）：回して薄く広げ、温めて溶剤を飛ばす（ChipOps.coat）。500W
    public static final Machine<SpinCoaterBlockEntity> SPIN_COATER = Machine.electric("spin_coater", SpinCoaterBlockEntity::new, SpinCoaterBlockEntity.LAYOUT, metal(3.0f));
    // レジストはがし槽（[9-21] の続き、IMP4-01）：熱い硫酸＋過酸化水素でレジストを酸化して落とす（ChipOps.strip）。500W
    // [9-35] テスト（ウェハープローバー）段階1（A、O-017）
    public static final Machine<WaferProberBlockEntity> WAFER_PROBER = Machine.electric("wafer_prober", WaferProberBlockEntity::new, WaferProberBlockEntity.LAYOUT, metal(3.0f));
    public static final Machine<DicingSawBlockEntity> DICING_SAW = Machine.electric("dicing_saw", DicingSawBlockEntity::new, DicingSawBlockEntity.LAYOUT, metal(3.5f));
    public static final Machine<ResistStripperBlockEntity> RESIST_STRIPPER = Machine.electric("resist_stripper", ResistStripperBlockEntity::new, ResistStripperBlockEntity.LAYOUT, metal(3.0f));
    // [9-21] 現像槽（IMP4-01）：薄いアルカリで光の当たったレジストを溶かし、熱板で固める（ChipOps.develop）。300W
    public static final Machine<DeveloperBenchBlockEntity> DEVELOPER_BENCH = Machine.electric("developer_bench", DeveloperBenchBlockEntity::new, DeveloperBenchBlockEntity.LAYOUT, metal(3.0f));
    // [9-14] RCA 洗浄台 段階2（IMP4-01）：SC-1（アンモニア水＋過酸化水素）・SC-2（塩酸＋過酸化水素）、脱イオン水ですすぐ（ChipOps.clean）。1.5kW
    public static final Machine<RcaBenchBlockEntity> RCA_BENCH = Machine.electric("rca_bench", RcaBenchBlockEntity::new, RcaBenchBlockEntity.LAYOUT, metal(3.0f));
    // [9-33] ワイヤーボンダー（IMP4-01）：金の細い線を熱と圧力でパッドと足にくっつける。パッド1つに1本。200W
    public static final Machine<WireBonderBlockEntity> WIRE_BONDER = Machine.electric("wire_bonder", WireBonderBlockEntity::new, WireBonderBlockEntity.LAYOUT, metal(3.0f));
    // [9-34] 封止機（IMP4-01）：金属缶＋乾いた窒素で溶接して気密封止、またはエポキシでトランスファー成形。500W
    public static final Machine<PackageSealerBlockEntity> PACKAGE_SEALER = Machine.electric("package_sealer", PackageSealerBlockEntity::new, PackageSealerBlockEntity.LAYOUT, metal(3.5f));
    // [9-28] イオン注入装置（IMP4-01）：イオン源＋質量分析の磁石＋CW 回路の加速器。隣の真空容器 0.01Pa 以下。5kW
    public static final Machine<IonImplanterBlockEntity> ION_IMPLANTER = Machine.electric("ion_implanter", IonImplanterBlockEntity::new, IonImplanterBlockEntity.LAYOUT, metal(4.0f));
    // [9-29] アニール炉（IMP4-01）：注入したウェハーを N2/Ar の中で熱して働かせる。3kW
    public static final Machine<AnnealFurnaceBlockEntity> ANNEAL_FURNACE = Machine.electric("anneal_furnace", AnnealFurnaceBlockEntity::new, AnnealFurnaceBlockEntity.LAYOUT, metal(4.0f));
    // [9-26] スパッタ装置（IMP4-01）：Ar⁺ で Al の的をたたいて膜にする。隣の真空容器 0.01Pa 以下＋アルゴン。5kW
    public static final Machine<SputterBlockEntity> SPUTTER = Machine.electric("sputter", SputterBlockEntity::new, SputterBlockEntity.LAYOUT, lit(metal(4.0f), 9));
    // [9-27] フッ素化の反応器（IMP4-01）：黒鉛＋フッ素のボンベ → CF4 8袋。500W
    public static final Machine<FluorinationReactorBlockEntity> FLUORINATION_REACTOR = Machine.electric("fluorination_reactor", FluorinationReactorBlockEntity::new, FluorinationReactorBlockEntity.LAYOUT, metal(3.5f));
    // [9-27] ドライエッチング装置 RIE（IMP4-01）：CF4 のプラズマで真下だけ削る。隣の真空容器 0.1Pa 以下。3kW
    public static final Machine<DryEtcherBlockEntity> DRY_ETCHER = Machine.electric("dry_etcher", DryEtcherBlockEntity::new, DryEtcherBlockEntity.LAYOUT, lit(metal(4.0f), 9));
    // [9-30] シランの発生器（IMP4-01、S-002）：Mg + Si → Mg2Si、Mg2Si + 4HCl → SiH4。1kW
    public static final Machine<SilaneGeneratorBlockEntity> SILANE_GENERATOR = Machine.electric("silane_generator", SilaneGeneratorBlockEntity::new, SilaneGeneratorBlockEntity.LAYOUT, metal(3.5f));
    // [9-30] CVD 炉（層間膜、IMP4-01 S-002）：SiH4 + O2 → SiO2 を約400℃。2層目の金属の前。2kW
    public static final Machine<CvdFurnaceBlockEntity> CVD_FURNACE = Machine.electric("cvd_furnace", CvdFurnaceBlockEntity::new, CvdFurnaceBlockEntity.LAYOUT, lit(metal(4.0f), 9));
    // [9-31] CMP 装置（IMP4-01、v0.5 #2）：層間膜をスラリーで平らに（段差 steps → 0）。2kW
    public static final Machine<CmpPolisherBlockEntity> CMP_POLISHER = Machine.electric("cmp_polisher", CmpPolisherBlockEntity::new, CmpPolisherBlockEntity.LAYOUT, metal(4.0f));
    // [9-30] エピタキシャル炉（IMP4-01、O-013）：SiCl4 + 2H2 → Si + 4HCl を約1200℃、素のウェハーに単結晶の層。10kW
    public static final Machine<EpitaxyReactorBlockEntity> EPITAXY_REACTOR = Machine.electric("epitaxy_reactor", EpitaxyReactorBlockEntity::new, EpitaxyReactorBlockEntity.LAYOUT, lit(metal(4.0f), 9));
    // [9-36] ウェハーの銅めっき装置（IMP4-01、S-003）：硫酸銅＋銅の陽極で溝と穴を銅で埋める（ダマシン）。1kW
    public static final Machine<CopperPlaterBlockEntity> COPPER_PLATER = Machine.electric("copper_plater", CopperPlaterBlockEntity::new, CopperPlaterBlockEntity.LAYOUT, metal(3.5f));
    // [9-41] インバータ（IMP4-01、O-015）：直流 → 交流（整流器の逆、サイリスタのスイッチ）。後ろ＝直流、前＝交流、95%
    public static final Machine<InverterBlockEntity> INVERTER = Machine.builder("inverter", InverterBlockEntity::new)
            .ticker(com.taku3.semiconductorlab.energy.PowerConverterBlockEntity::tick).energy((be, dir) -> be.storageFor(dir)).props(metal(2.5f)).build();
    // high-k（IMP4-01、S-004）：ハフニウムの塩素化炉（HfO2 + 2C + 2Cl2 → HfCl4 + 2CO、3kW）と ALD 装置（HfCl4 と水を交互に、2kW）
    public static final Machine<HafniumChlorinatorBlockEntity> HAFNIUM_CHLORINATOR = Machine.electric("hafnium_chlorinator", HafniumChlorinatorBlockEntity::new, HafniumChlorinatorBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<AldReactorBlockEntity> ALD_REACTOR = Machine.electric("ald_reactor", AldReactorBlockEntity::new, AldReactorBlockEntity.LAYOUT, metal(4.0f));
    // [2-33] ホットプレート（露光後の加熱 PEB、IMP4-01）：化学増幅レジストの酸を働かせる。300W
    public static final Machine<BakePlateBlockEntity> BAKE_PLATE = Machine.electric("bake_plate", BakePlateBlockEntity::new, BakePlateBlockEntity.LAYOUT, metal(3.0f));
    public static final Machine<CrystalPullerBlockEntity> CRYSTAL_PULLER = Machine.electric("crystal_puller", CrystalPullerBlockEntity::new, CrystalPullerBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    /** 組立台のレシピ（[9-42]・[9-43]、IMP4-01）。読み出しのチップは検査台で NOT（増幅のもと）に合格した物 */
    static net.minecraft.world.item.ItemStack passedNot() {
        var st = new net.minecraft.world.item.ItemStack(CHIP);
        st.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("not"));
        return st;
    }

    static void assemblies() {
        java.util.function.Predicate<net.minecraft.world.item.ItemStack> readout = s -> s.is(CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "not");
        com.taku3.semiconductorlab.machine.Assembly.recipe("image_sensor", IMAGE_SENSOR)
                .part("photodiode", PN_JUNCTION_WAFER, 1)
                .part("readout", readout, 1, CHIP, Semiconductor::passedNot)
                .part("wiring", Metallurgy.ALUMINIUM_INGOT, 1)
                .part("window", Chemistry.SODA_LIME_GLASS, 1)
                .seconds(5).build();
        com.taku3.semiconductorlab.machine.Assembly.recipe("mems_accelerometer", MEMS_ACCELEROMETER)
                .part("proof_mass", MEMS_WAFER, 1)
                .part("readout", readout, 1, CHIP, Semiconductor::passedNot)
                .part("wiring", Metallurgy.ALUMINIUM_INGOT, 1)
                .part("seal", CAN_CAP, 1)
                .seconds(5).build();
    }

    static void init() {}
}
