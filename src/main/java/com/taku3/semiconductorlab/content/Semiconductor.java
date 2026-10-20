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
    // [9-32] ダイシング 段階1（A、O-013）：ダイヤモンドのけがき針で傷を付けて割る（1960ごろ）→ チップ（chip_state を引き継ぐ）
    public static final Item DIAMOND_SCRIBE        = Reg.item("diamond_scribe", com.taku3.semiconductorlab.item.DiamondScribeItem::new, new Item.Properties().durability(64));
    public static final Item CHIP                  = Reg.item("chip");
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
    public static final Machine<CrystalPullerBlockEntity> CRYSTAL_PULLER = Machine.electric("crystal_puller", CrystalPullerBlockEntity::new, CrystalPullerBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
