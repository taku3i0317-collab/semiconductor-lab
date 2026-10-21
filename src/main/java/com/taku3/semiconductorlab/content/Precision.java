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
 * 幹8 精密機械・計測（docs/TECH_TREE.md の「幹8」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/precision/、src/gametest/.../PrecisionTests.java）だけを書き換える。
 */
public final class Precision {
    private Precision() {}

    // ===== アイテム =====
    public static final Item LEAD_SCREW            = Reg.item("lead_screw");
    // [8-01] 旋盤で削った部品（IMP2-01）
    // [8-10] 光学顕微鏡（IMP2-04）：単レンズ（レーウェンフック 1670年代、NA 0.2⚠️）と複式（色消し対物、NA 0.9）。見分けられる線 d ＝ λ ÷ 2NA
    public static final Item SIMPLE_MICROSCOPE     = Reg.item("simple_microscope", p -> new MicroscopeItem(p, 0.2), new Item.Properties().stacksTo(1));
    public static final Item COMPOUND_MICROSCOPE   = Reg.item("compound_microscope", p -> new MicroscopeItem(p, 0.9), new Item.Properties().stacksTo(1));
    // [8-02] ねじ切りと互換性部品（IMP2-05）：旋盤の親ねじの入口に送りねじを入れて鋼を削る → 同じねじ山の規格ねじ（ホイットワース 1841：山の角度 55°）
    public static final Item STANDARD_SCREW        = Reg.item("standard_screw");
    /** 削った部品の寸法の誤差（µm、+ は大きすぎ）。旋盤のくせで決まる。ノギス・マイクロメータ [8-04] で測る（IMP2-05） */
    public static final net.minecraft.core.component.DataComponentType<Integer> PART_ERROR_UM = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("part_error_um"),
            net.minecraft.core.component.DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    // [8-04] ノギスとマイクロメータ（IMP2-05）：段階1 ノギス（副尺 1631、青銅、読み 0.05mm）→ 段階2 マイクロメータ（パルマー 1848、規格ねじ、読み 0.01mm）
    public static final Item VERNIER_CALIPER       = Reg.item("vernier_caliper", p -> new GaugeItem(p, 50), new Item.Properties().stacksTo(1));
    public static final Item MICROMETER            = Reg.item("micrometer", p -> new GaugeItem(p, 10), new Item.Properties().stacksTo(1));
    public static final Item PUMP_ROTOR            = Reg.item("pump_rotor");        // 回転ポンプ [5-04] の偏心ローター（鋼）
    // [8-05] ゲージブロック（IMP2 O-010、ヨハンソン 1896）：焼入れしたるつぼ鋼の長さの基準。重ねて好きな長さ（リンギング）、マイクロメータと旋盤を合わせる
    public static final Item GAUGE_BLOCK_SET       = Reg.item("gauge_block_set", GaugeBlockSetItem::new, new Item.Properties().stacksTo(1));
    // [8-22] メートル原器（IMP2 O-007）：子午線の三角測量（[12-11]、1792〜98）で決めた長さの白金の棒（1799 アルシーヴのメートル）。2つのブロックの間を m で測る
    /** メートル原器で付けた1つ目の印（ブロックの場所） */
    public static final net.minecraft.core.component.DataComponentType<net.minecraft.core.BlockPos> METRE_MARK = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("metre_mark"),
            net.minecraft.core.component.DataComponentType.<net.minecraft.core.BlockPos>builder().persistent(net.minecraft.core.BlockPos.CODEC)
                    .networkSynchronized(net.minecraft.core.BlockPos.STREAM_CODEC).build());
    public static final Item METRE_BAR             = Reg.item("metre_bar", MetreBarItem::new, new Item.Properties().stacksTo(1));
    // [8-23] キログラム原器（IMP2 O-020）：白金インゴットをベンガラで磨き上げた円柱（1799 アルシーヴのキログラム。1889 の国際原器は Pt90・Ir10%⚠️）。2つ持って比べると差が µg で出る
    // [8-24] 国際単位系 1960（IMP2 S-005 案A）：クリプトン86 の放電ランプ（石英管＋Kr＋W電極）。干渉計のもう片方の手に持つと、2点の間をクリプトンの波長で測れる（1m＝1,650,763.73 波長）。耐久＝ランプの寿命
    public static final Item KRYPTON_LAMP          = Reg.item("krypton_lamp", new Item.Properties().durability(64));
    public static final Item KILOGRAM_PROTOTYPE    = Reg.item("kilogram_prototype", KilogramPrototypeItem::new, new Item.Properties().stacksTo(1));

    // [8-03] 平面と定盤（IMP2 O-009）：銑鉄の鋳物の板を3枚、2枚ずつ順にすり合わせる（ホイットワース 1830〜40年代）。そり 1µm 以下で定盤
    /** 板のそり（0.1µm 単位、＋凸・−凹）。すり合わせ（item/CastPlateItem）で変わる */
    public static final net.minecraft.core.component.DataComponentType<Integer> PLATE_CURVE = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("plate_curve"),
            net.minecraft.core.component.DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final Item CAST_IRON_PLATE       = Reg.item("cast_iron_plate", CastPlateItem::new, new Item.Properties().stacksTo(1));
    /** 定盤（置けるブロック。精密な機械の「隣に定盤」の条件に使う予定） */
    public static final Block SURFACE_PLATE = Reg.block("surface_plate", Block::new,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL)
                    .strength(5.0f, 6.0f).sound(net.minecraft.world.level.block.SoundType.METAL).requiresCorrectToolForDrops());

    // [8-07] 除振（IMP2）：定盤をゴムで浮かせた台。上に置いた物の揺れが 1/20 に（block/Vibration）。恒温（20℃の部屋）は空調 [12-12] のあと
    public static final Block ISOLATION_TABLE = Reg.block("isolation_table", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 6.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

    // [8-14] 鏡筒の精密組立（心出し、IMP2 O-010）：レンズ4枚の心のずれを顕微鏡で見て寄せる（item/LensBarrelItem）。全部 1µm 以下で心出し済み → [7-19] 投影レンズ
    /** 鏡筒の中のレンズそれぞれの心のずれ（0.1µm 単位） */
    public static final net.minecraft.core.component.DataComponentType<java.util.List<Integer>> LENS_DECENTER = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("lens_decenter"),
            net.minecraft.core.component.DataComponentType.<java.util.List<Integer>>builder().persistent(com.mojang.serialization.Codec.INT.listOf())
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(com.mojang.serialization.Codec.INT.listOf())).build());
    public static final Item LENS_BARREL           = Reg.item("lens_barrel", LensBarrelItem::new, new Item.Properties().stacksTo(1));          // 組んだままの鏡筒
    public static final Item CENTERED_LENS_BARREL  = Reg.item("centered_lens_barrel", LensBarrelItem::new, new Item.Properties().stacksTo(1)); // 心出し済みの鏡筒

    // [8-31] 研削盤・[8-20] 玉軸受（IMP2 O-013）：焼入れした鋼球を砥石で研削し、測る道具でそろえる → 玉軸受の段階（1＝手で丸めた、2＝マイクロメータ、3＝ゲージブロック）
    public static final Item STEEL_BALLS_ROUGH     = Reg.item("steel_balls_rough");     // 鋼を丸めて焼入れした球（大きさがばらばら）
    public static final Item RAW_GRINDING_WHEEL    = Reg.item("raw_grinding_wheel");    // アルミナ＋粘土を固めただけ
    public static final Item GRINDING_WHEEL        = Reg.item("grinding_wheel", new Item.Properties().stacksTo(1)); // 焼いた砥石（減らない）
    public static final Item STEEL_BALLS_GROUND    = Reg.item("steel_balls_ground");    // 研削してマイクロメータでそろえた球（±10µm）
    public static final Item STEEL_BALLS_PRECISE   = Reg.item("steel_balls_precise");   // 研削してゲージブロックでそろえた球（±1µm）
    /** 玉軸受の段階（1〜3） */
    public static final net.minecraft.core.component.DataComponentType<Integer> BEARING_GRADE = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("bearing_grade"),
            net.minecraft.core.component.DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final Item BALL_BEARING          = Reg.item("ball_bearing", BallBearingItem::new, new Item.Properties());
    // [8-08] ボールねじ（IMP2 O-014）：送りねじ＋鋼球。段階（bearing_grade）で送りの細かさ 5・2・1µm（送りねじは 10µm）。旋盤の親ねじに入れると部品の誤差・電力が下がる
    // [8-12] 刻線機（IMP2 O-016、1882 ローランド）：スペキュラムの鏡にダイヤモンドで 1mm に 600 本の溝 → 刻線した回折格子（揺れ・温度があるとゴースト）
    public static final net.minecraft.core.component.DataComponentType<Boolean> GRATING_GHOSTS = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("grating_ghosts"),
            net.minecraft.core.component.DataComponentType.<Boolean>builder().persistent(com.mojang.serialization.Codec.BOOL)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());
    /** 刻線した格子：1mm に 600 本（溝の間隔 1.667µm）、幅 50mm で 30000 本 */
    public static final double RULED_GRATING_MICRONS = 1000.0 / 600, RULED_GRATING_LINES = 30_000;
    public static final Item RULED_GRATING         = Reg.item("ruled_grating", new Item.Properties().stacksTo(1));
    public static final Item RULED_SPECTROSCOPE    = Reg.item("ruled_spectroscope", p -> new SpectroscopeItem(p, RULED_GRATING_MICRONS, (int) RULED_GRATING_LINES), new Item.Properties().stacksTo(1));
    // [8-15] 水晶振動子（IMP2 O-016）：水晶をダイヤモンドののこぎりで AT カットの水晶片に → ルージュで磨いて振動数を合わせる → 銀の電極で振動子（膜厚計・時計）
    /** 水晶片の厚さ（µm）。振動数＝1661kHz·mm ÷ 厚さ */
    public static final net.minecraft.core.component.DataComponentType<Integer> BLANK_THICKNESS_UM = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("blank_thickness_um"),
            net.minecraft.core.component.DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final Item DIAMOND_SAW           = Reg.item("diamond_saw", ReusableToolItem::new, new Item.Properties().stacksTo(1)); // ダイヤモンドの刃ののこぎり（戻る）
    public static final Item QUARTZ_BLANK          = Reg.item("quartz_blank", QuartzBlankItem::new, new Item.Properties().stacksTo(1));
    public static final Item CRYSTAL_RESONATOR     = Reg.item("crystal_resonator", CrystalResonatorItem::new, new Item.Properties().stacksTo(1));
    // [8-18] 人工水晶（IMP2 O-016、1940年代末〜）：鋼の釜（D のオートクレーブ）に石英くず＋NaOH の水と種結晶を入れて育てる。表に1行（下の static）
    public static final Item HYDROTHERMAL_CHARGE   = Reg.item("hydrothermal_charge");   // 珪石×4＋水酸化ナトリウム＋種の水晶
    public static final Item CULTURED_QUARTZ       = Reg.item("cultured_quartz");       // 人工水晶（種から育った単結晶）
    public static final Item BALL_SCREW            = Reg.item("ball_screw", BallScrewItem::new, new Item.Properties().stacksTo(1));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 旋盤 [8-01]（IMP2-01、モーズリー 1797〜1800頃）：モーターで材料を回し、送りねじの刃物台で削る。750W
    // 刻線機 [8-12]（IMP2 O-016）：鏡＋ダイヤモンド（減らない）＋送りねじ → 刻線した格子（300W・60秒）
    public static final Machine<RulingEngineBlockEntity> RULING_ENGINE = Machine.electric("ruling_engine", RulingEngineBlockEntity::new, RulingEngineBlockEntity.LAYOUT, metal(3.5f));
    // 電子顕微鏡 [8-11]（IMP2 O-017、1931 ルスカとクノール）：隣の真空容器 0.01Pa 以下で、マスク・ウェハーの細かい線を光学顕微鏡と見比べる（2kW）
    public static final Machine<ElectronMicroscopeBlockEntity> ELECTRON_MICROSCOPE = Machine.electric("electron_microscope", ElectronMicroscopeBlockEntity::new, ElectronMicroscopeBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<LatheBlockEntity> LATHE = Machine.electric("lathe", LatheBlockEntity::new, LatheBlockEntity.LAYOUT, metal(3.5f));
    // 研削盤 [8-31]（IMP2 O-013）：焼入れした鋼球＋砥石＋測る道具 → そろえた鋼球（1kW）
    public static final Machine<GrinderBlockEntity> GRINDER = Machine.electric("grinder", GrinderBlockEntity::new, GrinderBlockEntity.LAYOUT, metal(3.5f));

    static {
        // [8-18] D の加圧加熱釜の表に1行（IMP3_LOG の使い方どおり）。⚠️ 本物は約350℃・1000気圧以上、ゲームの釜（300℃・20気圧まで）に合わせた
        AutoclaveBlockEntity.RECIPES.add(new AutoclaveBlockEntity.Recipe("cultured_quartz", s -> s.is(HYDROTHERMAL_CHARGE), 1,
                () -> new net.minecraft.world.item.ItemStack(CULTURED_QUARTZ, 3), () -> new net.minecraft.world.item.ItemStack(HYDROTHERMAL_CHARGE), 280, 15, 20 * 40));
    }

    // [8-33] 質量分析器（IMP4-01、O-011。C の了承で幹8 に E が追記）：電磁石でイオンを曲げて重さでふるい分ける。右クリックでもう片方の手の試料の同位体
    public static final Item MASS_SPECTROMETER     = Reg.item("mass_spectrometer", com.taku3.semiconductorlab.item.MassSpectrometerItem::new, new Item.Properties().stacksTo(1));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
