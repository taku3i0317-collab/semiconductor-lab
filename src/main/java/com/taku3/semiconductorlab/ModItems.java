package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.item.MercuryVacuumGaugeItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Function;

public class ModItems {

    // --- 第1章：入手 ---
    public static final Item MAGNETITE             = simple("magnetite");
    public static final Item WOLFRAMITE            = simple("wolframite");
    public static final Item GALENA                = simple("galena");
    public static final Item GRAPHITE              = simple("graphite");
    public static final Item SPHALERITE            = simple("sphalerite");
    public static final Item COAL_ASH              = simple("coal_ash");
    public static final Item ZINC_INGOT            = simple("zinc_ingot");
    public static final Item GERMANIUM_CONCENTRATE = simple("germanium_concentrate");
    public static final Item INDIUM                = simple("indium");

    // --- 試薬 ---
    public static final Item SULFUR_POWDER         = simple("sulfur_powder");
    public static final Item SULFUR_DIOXIDE        = simple("sulfur_dioxide"); // 硫黄を燃やした気体
    public static final Item SULFURIC_ACID         = register("sulfuric_acid", Item::new, new Item.Properties().stacksTo(16));
    public static final Item PHOSPHORUS            = simple("phosphorus");

    // --- ゲルマニウムの精錬・精製 ---
    public static final Item GERMANIUM_DIOXIDE     = simple("germanium_dioxide");
    public static final Item RAW_GERMANIUM         = simple("raw_germanium");       // 金属Ge 98%
    public static final Item REFINED_GERMANIUM     = register("refined_germanium", Item::new, new Item.Properties().component(ModDataComponents.PURITY, Purity.SIX_N));   // 帯域精製後

    // --- ドーピング ---
    public static final Item N_TYPE_GERMANIUM      = simple("n_type_germanium");
    public static final Item P_TYPE_GERMANIUM      = simple("p_type_germanium");

    // --- 帯域精製炉の部品（Pfann 1952 の装置構成）---
    public static final Item GRAPHITE_BOAT_BLANK   = simple("graphite_boat_blank"); // 黒鉛+粘土、未焼成
    public static final Item GRAPHITE_BOAT         = simple("graphite_boat");          // 粘土黒鉛ボート（19世紀式、9N まで）
    public static final Item SYNTHETIC_GRAPHITE_BOAT = simple("synthetic_graphite_boat"); // 人造黒鉛ボート（[3-10] アチソン炉の黒鉛、10N まで）
    public static final Item FUSED_QUARTZ          = simple("fused_quartz");
    public static final Item QUARTZ_TUBE           = simple("quartz_tube");
    public static final Item INDUCTION_COIL        = simple("induction_coil");
    public static final Item LEAD_SCREW            = simple("lead_screw");
    public static final Item ELECTRIC_MOTOR        = simple("electric_motor");
    // 高周波発振器 [4-09]（真空管2本＋コイル＋ライデン瓶の発振回路。誘導加熱の電源。旧・仮アイテム hf_power_supply を置換）
    public static final Item HF_OSCILLATOR         = simple("hf_oscillator");
    public static final Item LEYDEN_JAR            = simple("leyden_jar");       // コンデンサ（ライデン瓶 1745：ガラス瓶の内外に金属箔）

    // --- 電気の部品（v0.2a）---
    public static final Item MAGNET                = simple("magnet");   // 天然磁石（磁鉄鉱のうち磁化したもの＝ロードストーン）
    public static final Item DYNAMO                = simple("dynamo");   // 磁石＋銅コイル＋鉄：ファラデーの電磁誘導
    public static final Item SELF_EXCITED_DYNAMO   = simple("self_excited_dynamo"); // [4-05] ジーメンス1866：鋼の鉄心＋コイル×4、磁石不要（残留磁気で自分を励磁）

    // --- ガス（v0.2a [6-01] 電解槽の生成物）---
    // 「袋詰め」のガス（約1mol）。貯蔵は [6-02] ガスホルダー、高圧の持ち運びは [6-03] ボンベ
    public static final Item HYDROGEN_GAS          = simple("hydrogen_gas");
    public static final Item OXYGEN_GAS            = simple("oxygen_gas");
    // 高圧ボンベ（[6-03] 圧縮機。鋼製・約150気圧・1本 50mol）
    public static final Item EMPTY_GAS_CYLINDER    = register("empty_gas_cylinder", Item::new, new Item.Properties().stacksTo(16));
    public static final Item HYDROGEN_CYLINDER     = register("hydrogen_cylinder", Item::new, new Item.Properties().stacksTo(16));
    public static final Item OXYGEN_CYLINDER       = register("oxygen_cylinder", Item::new, new Item.Properties().stacksTo(16));

    // --- 冶金（[1-26]）---
    public static final Item STEEL_INGOT           = simple("steel_ingot");   // ベッセマー転炉の鋼（C 約0.1〜0.5%）
    public static final Item LITHARGE              = simple("litharge");      // [1-27] 一酸化鉛 PbO（方鉛鉱を焙焼したもの）
    public static final Item LEAD_INGOT            = simple("lead_ingot");    // [1-27] 鉛（PbO を炭素で還元）
    public static final Item ZINC_OXIDE            = simple("zinc_oxide");    // [3-06] 閃亜鉛鉱を反射炉で焙焼した ZnO（亜鉛華）

    // --- タングステン（[1-28] 粉末冶金。クーリッジ 1909〜1910）---
    public static final Item TUNGSTEN_TRIOXIDE     = simple("tungsten_trioxide"); // WO3（黄色）。★仮：ソーダ融解・酸処理を省略
    public static final Item TUNGSTEN_POWDER       = simple("tungsten_powder");   // 水素還元した W 粉
    public static final Item TUNGSTEN_BAR          = simple("tungsten_bar");      // 焼結棒（水素中で通電焼結）
    public static final Item TUNGSTEN_WIRE         = simple("tungsten_wire");     // 線引きした延性タングステン線

    // --- 電気炉（[4-12] 抵抗加熱の発熱体）---
    public static final Item GRAPHITE_ROD          = simple("graphite_rod");      // 黒鉛棒（電気炉の発熱体・アチソン炉の電極）
    public static final Item SYNTHETIC_GRAPHITE    = simple("synthetic_graphite"); // [3-10] 人造黒鉛（アチソン炉、炭素を約2500℃以上で黒鉛化）

    // --- 熱（[3-12] 耐火れんが＝珪石れんが・ディナス 1822）---
    public static final Item REFRACTORY_BRICK_BLANK = simple("refractory_brick_blank"); // 成形しただけ（未焼成）
    public static final Item REFRACTORY_BRICK      = simple("refractory_brick");       // 高温（約1450℃）で焼いたもの

    // --- 石灰（[0-10] 方解石 CaCO3 → 焼成 → 生石灰 CaO、[0-11] CaO + H2O → 消石灰 Ca(OH)2）---
    public static final Item QUICKLIME             = simple("quicklime");
    public static final Item SLAKED_LIME           = simple("slaked_lime");

    // --- 水銀（[1-45] 辰砂 HgS の焙焼。HgS + O2 → Hg + SO2）---
    public static final Item CINNABAR_DUST         = simple("cinnabar_dust");   // 辰砂（minecraft:cinnabar）を砕いた粉
    public static final Item MERCURY               = register("mercury", Item::new, new Item.Properties().stacksTo(16)); // 液体の金属（容器入り）

    // --- 真空（[5-01] ポンプ・[5-10] 真空計）---
    public static final Item MERCURY_VACUUM_GAUGE  = register("mercury_vacuum_gauge", MercuryVacuumGaugeItem::new, new Item.Properties().stacksTo(1));

    // --- 真空管（[5-02] ド・フォレストの三極管 1906）---
    public static final Item VACUUM_TUBE_ASSEMBLY  = simple("vacuum_tube_assembly"); // ガラス球＋W フィラメント＋グリッド＋プレート（まだ空気入り）
    public static final Item VACUUM_TUBE           = simple("vacuum_tube");          // 排気して封じた三極真空管

    // --- トランジスタと製品 ---
    public static final Item TRANSISTOR            = simple("transistor");           // 点接触型 (1947)
    public static final Item JUNCTION_TRANSISTOR_BLANK = simple("junction_transistor_blank"); // 合金化前
    public static final Item JUNCTION_TRANSISTOR   = simple("junction_transistor");  // 合金接合型 (1952)
    public static final Item TRANSISTOR_RADIO      = register("transistor_radio", Item::new, new Item.Properties().stacksTo(1));

    public static final List<Item> ALL = List.of(
            MAGNETITE, WOLFRAMITE, GALENA, GRAPHITE, SPHALERITE, COAL_ASH, ZINC_INGOT, GERMANIUM_CONCENTRATE, INDIUM,
            SULFUR_POWDER, SULFUR_DIOXIDE, SULFURIC_ACID, PHOSPHORUS,
            GERMANIUM_DIOXIDE, RAW_GERMANIUM, REFINED_GERMANIUM,
            N_TYPE_GERMANIUM, P_TYPE_GERMANIUM,
            GRAPHITE_BOAT_BLANK, GRAPHITE_BOAT, SYNTHETIC_GRAPHITE_BOAT, FUSED_QUARTZ, QUARTZ_TUBE, INDUCTION_COIL, LEAD_SCREW, ELECTRIC_MOTOR, HF_OSCILLATOR, LEYDEN_JAR, MAGNET, DYNAMO, SELF_EXCITED_DYNAMO,
            HYDROGEN_GAS, OXYGEN_GAS, EMPTY_GAS_CYLINDER, HYDROGEN_CYLINDER, OXYGEN_CYLINDER,
            STEEL_INGOT, LITHARGE, LEAD_INGOT, ZINC_OXIDE,
            TUNGSTEN_TRIOXIDE, TUNGSTEN_POWDER, TUNGSTEN_BAR, TUNGSTEN_WIRE,
            REFRACTORY_BRICK_BLANK, REFRACTORY_BRICK, GRAPHITE_ROD, SYNTHETIC_GRAPHITE, QUICKLIME, SLAKED_LIME,
            CINNABAR_DUST, MERCURY, MERCURY_VACUUM_GAUGE, VACUUM_TUBE_ASSEMBLY, VACUUM_TUBE,
            TRANSISTOR, JUNCTION_TRANSISTOR_BLANK, JUNCTION_TRANSISTOR, TRANSISTOR_RADIO);

    private static Item simple(String name) {
        return register(name, Item::new, new Item.Properties());
    }

    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> ALL.forEach(tab::accept));
    }
}