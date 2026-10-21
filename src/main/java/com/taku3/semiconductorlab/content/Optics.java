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
 * 幹7 光学（docs/TECH_TREE.md の「幹7」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/optics/、src/gametest/.../OpticsTests.java）だけを書き換える。
 */
public final class Optics {
    private Optics() {}

    // ===== アイテム =====
    public static final Item FUSED_QUARTZ          = Reg.item("fused_quartz");
    public static final Item QUARTZ_TUBE           = Reg.item("quartz_tube");
    // [7-15] 合成石英（IMP2、S-001）：SiCl4 を酸水素炎で加水分解（ハイド 1934）。天然の珪石より不純物がずっと少なく、紫外をよく通す
    public static final Item SYNTHETIC_QUARTZ      = Reg.item("synthetic_quartz");
    // [7-01] 光学ガラス（IMP2-01）。クラウン＝ソーダ石灰ガラスを溶鉱炉で長く溶かしてかき混ぜたもの（分散が小さい）、
    // フリント＝一酸化鉛 PbO を約4割入れた鉛ガラス（レイヴンズクロフト 1674。屈折率が高く、分散が大きい）
    public static final Item FLINT_GLASS_BATCH     = Reg.item("flint_glass_batch"); // 調合原料（砂5＋一酸化鉛1＋カリ1。レイヴンズクロフトと同じカリ [2-02]）
    public static final Item FLINT_GLASS           = Reg.item("flint_glass");
    public static final Item CROWN_GLASS           = Reg.item("crown_glass");
    // [7-03] レンズ研磨（IMP2-01）：① 鋳鉄の研削皿＋砂（水といっしょにこする）→ すりガラスのレンズ素材、
    // ② ピッチの研磨皿＋ベンガラ Fe2O3 → 透明なレンズ。研削皿と研磨皿は作業台で使っても戻る
    public static final Item ROUGE                 = Reg.item("rouge");             // ベンガラ（酸化鉄(III) Fe2O3 の細かい粉）
    public static final Item GRINDING_TOOL         = Reg.item("grinding_tool", ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item PITCH_LAP             = Reg.item("pitch_lap", ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item CROWN_LENS_BLANK      = Reg.item("crown_lens_blank");  // 削っただけ（すりガラス）
    public static final Item FLINT_LENS_BLANK      = Reg.item("flint_lens_blank");
    public static final Item CROWN_LENS            = Reg.item("crown_lens");        // 磨いた凸レンズ
    public static final Item FLINT_LENS            = Reg.item("flint_lens");        // 磨いた凹レンズ（色消しの相方）
    // [7-04] 色消しレンズと屈折望遠鏡（IMP2-01）：クラウン凸＋フリント凹 → 色消しレンズ（ドロンド 1758）。対物＝色消し、接眼＝凸のケプラー式
    public static final Item ACHROMATIC_LENS       = Reg.item("achromatic_lens");
    public static final Item REFRACTING_TELESCOPE  = Reg.item("refracting_telescope", TelescopeItem::new, new Item.Properties().stacksTo(1));
    // [7-05] 反射鏡 段階1 スペキュラム（IMP2-01）：銅2＋スズ1 の白い合金を削って磨いた鏡（反射率 約6割、すぐ曇る）→ ニュートン式反射望遠鏡
    public static final Item SPECULUM_CHARGE       = Reg.item("speculum_charge");   // 地金の調合（銅2＋スズ1）
    public static final Item SPECULUM_INGOT        = Reg.item("speculum_ingot");    // スペキュラム合金（Cu 約67%・Sn 約33%）
    public static final Item SPECULUM_MIRROR       = Reg.item("speculum_mirror", p -> new MirrorItem(p, "speculum", 63, 50, 35), new Item.Properties());   // 磨いた凹面鏡
    public static final Item REFLECTING_TELESCOPE  = Reg.item("reflecting_telescope", TelescopeItem::new, new Item.Properties().stacksTo(1));
    // [7-10] 水銀ランプ（IMP2-01）：石英管＋水銀＋W電極2 → 組立品 →封止台（0.1Pa 以下、シュプレンゲル [5-15]）→ 水銀灯の管 → 直流で光る水銀灯（385W、クーパー・ヒューイット 1901）
    public static final Item MERCURY_LAMP_ASSEMBLY = Reg.item("mercury_lamp_assembly");
    public static final Item MERCURY_LAMP_TUBE     = Reg.item("mercury_lamp_tube");
    // [5-14] 真空蒸着の製品（IMP2-01）：クラウンガラスに銀を薄く蒸着した半透明鏡（半分反射・半分透過）。干渉計 [7-09] の光を2つに分ける板
    public static final Item HALF_SILVERED_MIRROR  = Reg.item("half_silvered_mirror");
    // [7-05] 段階2 銀鏡反応（IMP2 O-012、リービッヒ 1835・フーコー 1857）：ガラス＋硝酸銀＋アンモニア水＋砂糖（還元剤）→ 薄い銀＝半透明鏡、もう一度 → 銀鏡（反射率 約9割）
    public static final Item SILVERED_MIRROR       = Reg.item("silvered_mirror", p -> new MirrorItem(p, "silver", 96, 80, 25), new Item.Properties());
    // [7-05] 段階3 アルミ蒸着の鏡（IMP2 O-020、1930年代 ストロング）：クラウンガラスに真空蒸着装置 [5-14] でアルミ [1-29] を付ける。曇らない・紫外も反射
    public static final Item ALUMINIZED_MIRROR     = Reg.item("aluminized_mirror", p -> new MirrorItem(p, "aluminium", 91, 92, 90), new Item.Properties());
    // [7-09] マイケルソン干渉計（IMP2-01）：半透明鏡＋スペキュラム鏡×2＋送りねじ（動く鏡）＋鉄の台。水銀の緑線 546.1nm で長さを測る
    public static final Item INTERFEROMETER        = Reg.item("interferometer", InterferometerItem::new, new Item.Properties().stacksTo(1));
    // [7-22] 反射防止膜（IMP2-01）：真空蒸着装置でレンズに CaF2 を 1/4 波長の厚さ付けたもの（表面の反射 約4% → 約2%）。投影レンズ [7-19] の材料
    public static final Item COATED_CROWN_LENS     = Reg.item("coated_crown_lens");
    public static final Item COATED_FLINT_LENS     = Reg.item("coated_flint_lens");
    // [7-15] 紫外光学材料・CaF2（IMP2-02）：ブリッジマン炉で蛍石を溶かして単結晶に → 削って磨いて CaF2 レンズ（紫外を通す。色消しのアポクロマートにも）
    public static final Item CAF2_CRYSTAL          = Reg.item("caf2_crystal");
    public static final Item CAF2_LENS             = Reg.item("caf2_lens");
    // [7-11] 銀塩フォトマスクの材料（IMP2-03。乾板の化学：1871 マドックスのゼラチン乾板の流れ）
    public static final Item HIDE_BROTH            = Reg.item("hide_broth", new Item.Properties().stacksTo(16)); // 皮の煮汁（革×2＋水）
    public static final Item GELATIN               = Reg.item("gelatin");           // ゼラチン（煮汁を煮つめて乾かす）
    public static final Item PHOTO_EMULSION        = Reg.item("photo_emulsion", new Item.Properties().stacksTo(16)); // 塩化銀ゼラチン乳剤
    public static final Item PHOTO_PLATE           = Reg.item("photo_plate");       // 感光乾板（ガラス板＋乳剤）
    public static final Item FERROUS_SULFATE       = Reg.item("ferrous_sulfate");   // 硫酸鉄(II)＝緑ばん（現像液）
    public static final Item HYPO_LIQUOR           = Reg.item("hypo_liquor", new Item.Properties().stacksTo(16)); // チオ硫酸の煮汁
    public static final Item SODIUM_THIOSULFATE    = Reg.item("sodium_thiosulfate"); // チオ硫酸ナトリウム＝ハイポ（定着液）
    // [7-11] フォトマスク（IMP2-03、LITHO_DESIGN §4.3）：縮小カメラで原図の mask 成分を写した乾板。密着露光で傷むので耐久 PHOTOMASK_USES 回
    public static final int PHOTOMASK_USES = 10;
    public static final Item PHOTOMASK             = Reg.item("photomask", new Item.Properties().durability(PHOTOMASK_USES));
    // [7-12] クロムマスク（IMP2 O-015）：合成石英にクロムを蒸着したブランク → 縮小カメラでレジストに写して塩酸で Cr を削る。傷に強く（耐久 CHROME_MASK_USES）、乳剤の粒より細かく描ける
    public static final int CHROME_MASK_USES = 100;
    public static final Item CHROME_BLANK          = Reg.item("chrome_blank");     // クロムを蒸着した合成石英の板（マスクブランクス）
    public static final Item CHROME_PHOTOMASK      = Reg.item("chrome_photomask", new Item.Properties().durability(CHROME_MASK_USES));
    // [7-19] 投影レンズ（IMP2 O-010）：心出し済みの鏡筒 [8-14]＋反射防止膜のレンズ [7-22] → g線の 1/5 縮小レンズ（鏡筒のずれを引き継いで CD が決まる）
    public static final Item PROJECTION_LENS       = Reg.item("projection_lens", ProjectionLensItem::new, new Item.Properties().stacksTo(1));
    // O-017 i線の投影レンズ（IMP2）：ふつうのガラスは 365nm を吸うので合成石英のレンズで組む。NA 0.7⚠️（本物の i線ステッパーは 0.5〜0.65）
    public static final Item PROJECTION_LENS_I     = Reg.item("projection_lens_i", p -> new ProjectionLensItem(p, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I, 0.7), new Item.Properties().stacksTo(1));
    // O-017 i線フィルター（IMP2）：水銀灯の光から 365nm だけを通す（合成石英に CaF2 などの薄い膜を重ねた干渉フィルター）
    public static final Item I_LINE_FILTER         = Reg.item("i_line_filter", new Item.Properties().stacksTo(1));
    // [7-20] KrF エキシマレーザー（IMP2 O-018）：Kr＋F2（Ne で薄める）を放電で励起 → 248nm の紫外パルス。ステッパーの光源の入口に入れる。耐久＝ガスの残り（1チップで1）
    public static final int KRF_LASER_SHOTS = 64;
    public static final Item KRF_LASER             = Reg.item("krf_laser", new Item.Properties().durability(KRF_LASER_SHOTS));
    // [7-20] ArF エキシマレーザー（IMP2 S-005 v0.5）：Ar＋F2（Ne で薄める）→ 193nm。2000年代の主役
    public static final Item ARF_LASER             = Reg.item("arf_laser", new Item.Properties().durability(KRF_LASER_SHOTS));
    // ArF の投影レンズ：193nm では石英だけだと色がにじむので CaF2 のレンズを混ぜて色消し。NA 0.85・k1 0.35⚠️
    public static final Item PROJECTION_LENS_ARF   = Reg.item("projection_lens_arf", p -> new ProjectionLensItem(p, com.taku3.semiconductorlab.litho.ExposureTool.ARF, 0.85, 0.35), new Item.Properties().stacksTo(1));
    // [7-16] 液浸の投影レンズ（IMP2 S-005 v0.5、2006〜）：いちばん下のレンズとウェハーの間を超純水（193nm で屈折率 1.44）で満たし、NA 1.35。k1 0.3⚠️
    public static final Item PROJECTION_LENS_IMMERSION = Reg.item("projection_lens_immersion", p -> new ProjectionLensItem(p, com.taku3.semiconductorlab.litho.ExposureTool.ARF_IMMERSION, 1.35, 0.3), new Item.Properties().stacksTo(1));
    // KrF の投影レンズ：248nm を通すのは石英と CaF2 だけ。NA 0.8・解像度を上げる工夫（位相シフトマスクなど）で k1 0.35⚠️
    public static final Item PROJECTION_LENS_KRF   = Reg.item("projection_lens_krf", p -> new ProjectionLensItem(p, com.taku3.semiconductorlab.litho.ExposureTool.KRF, 0.8, 0.35), new Item.Properties().stacksTo(1));
    // [7-13] ルビーレーザー（IMP2 O-019、1960 メイマン）：アルミナ＋少しの酸化クロム → 酸水素炎で溶かして落とす（ベルヌーイ法 1902）→ ルビーの単結晶
    // → 両端に銀の鏡 → ルビーの棒。キセノンの閃光ランプ（石英管＋Xe＋W電極）で照らして 694.3nm の赤いパルス。耐久＝閃光ランプの残り
    public static final Item RUBY_FEED             = Reg.item("ruby_feed");        // アルミナ＋酸化クロムの粉（Al2O3:Cr）
    public static final Item RUBY_BOULE            = Reg.item("ruby_boule");       // 炎で育てたルビーの単結晶（ブール）
    public static final Item RUBY_ROD              = Reg.item("ruby_rod", new Item.Properties().stacksTo(1));
    public static final Item XENON_FLASHLAMP       = Reg.item("xenon_flashlamp", new Item.Properties().stacksTo(16));
    public static final int RUBY_LAMP_SHOTS = 100;
    public static final Item RUBY_LASER            = Reg.item("ruby_laser", RubyLaserItem::new, new Item.Properties().durability(RUBY_LAMP_SHOTS));
    // [7-14] CO2 レーザー（IMP2 O-019）：10.6µm の赤外線はガラスを通らないので、窓は岩塩 NaCl の結晶（湿気で曇る＝耐久）
    public static final Item SALT_WINDOW           = Reg.item("salt_window", new Item.Properties().durability(64));
    // [7-21] 超低膨張ガラス ULE（IMP2 O-019、1960年代 コーニング）：SiCl4 と TiCl4 を混ぜて酸水素炎で加水分解 → SiO2 に TiO2 約7%。室温で熱膨張ほぼ 0
    public static final Item ULE_FEED              = Reg.item("ule_feed", new Item.Properties().stacksTo(16)); // SiCl4＋TiCl4 の混ぜた液
    public static final Item ULE_GLASS             = Reg.item("ule_glass");
    // [7-06] プリズム分光（IMP2-01）：フリントガラスを削って磨いた三角柱（分散が大きい）→ 分光器（キルヒホッフとブンゼン 1859：隙間＋レンズ＋プリズム＋望遠鏡）
    public static final Item PRISM                 = Reg.item("prism");
    public static final Item SPECTROSCOPE          = Reg.item("spectroscope", SpectroscopeItem::new, new Item.Properties().stacksTo(1));
    // [7-07] 回折格子（IMP2-01）：フラウンホーファー 1821 の針金格子（2本の細いねじに銅線を巻く。間隔 0.004 インチ＝101.6μm）→ 波長を測れる分光器
    public static final double WIRE_GRATING_MICRONS = 101.6;
    public static final Item WIRE_GRATING          = Reg.item("wire_grating");
    /** 針金の格子の溝の数（幅 約25mm ÷ 0.1016mm ≒ 246 本）。ナトリウムの D 線（R≈982）は分けられない */
    public static final int WIRE_GRATING_LINES = 246;
    public static final Item GRATING_SPECTROSCOPE  = Reg.item("grating_spectroscope", p -> new SpectroscopeItem(p, WIRE_GRATING_MICRONS, WIRE_GRATING_LINES), new Item.Properties().stacksTo(1));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 縮小カメラ [7-11]（IMP2-03）：原図を照明して色消しレンズで縮め、感光乾板に写して現像・定着 → フォトマスク（原図は減らない）。1kW
    public static final Machine<ReductionCameraBlockEntity> REDUCTION_CAMERA = Machine.electric("reduction_camera", ReductionCameraBlockEntity::new, ReductionCameraBlockEntity.LAYOUT, lit(metal(3.0f), 8));
    // ブリッジマン炉 [7-15]（IMP2-02）：誘導加熱 3kW、アルゴンの中で蛍石を溶かし、ゆっくり下げて CaF2 単結晶に
    public static final Machine<BridgmanFurnaceBlockEntity> BRIDGMAN_FURNACE = Machine.electric("bridgman_furnace", BridgmanFurnaceBlockEntity::new, BridgmanFurnaceBlockEntity.LAYOUT, lit(metal(3.5f), 10));
    // 水銀灯 [7-10]（IMP2-01）：直流 385W（110V・3.5A）でつく。画面なし。点灯中は明るさ15、分光器で水銀の線（g線・h線・i線）が見える
    public static final Machine<MercuryLampBlockEntity> MERCURY_LAMP = Machine.electric("mercury_lamp", MercuryLampBlockEntity::new, MercuryLampBlockEntity.LAYOUT,
            lit(metal(1.5f).sound(SoundType.GLASS), 15));
    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。ガスボンベ式（電気の受け口なし）。炎が出ている間は光る
    public static final Machine<OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Machine.builder("oxyhydrogen_burner", OxyhydrogenBurnerBlockEntity::new)
            .ticker(OxyhydrogenBurnerBlockEntity::tick).layout(OxyhydrogenBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f), 12)).build();

    // CO2 レーザー [7-14]（IMP2 O-019、1964 パテル）：CO2＋N2 に放電、10.6µm の赤外線。1kW、He なしの低出力版（決定）。珪石を溶かして溶融石英
    public static final Machine<Co2LaserBlockEntity> CO2_LASER = Machine.electric("co2_laser", Co2LaserBlockEntity::new, Co2LaserBlockEntity.LAYOUT, lit(metal(3.0f), 6));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
