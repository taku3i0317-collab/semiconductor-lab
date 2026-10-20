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
    public static final Item MALACHITE             = Reg.item("malachite");        // [1-03] 孔雀石 Cu2CO3(OH)2（IMP2-01。銅鉱床の上の酸化帯）
    public static final Item GALENA                = Reg.item("galena");
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
    public static final Item TUNGSTEN_POWDER       = Reg.item("tungsten_powder");   // 水素還元した W 粉
    public static final Item TUNGSTEN_BAR          = Reg.item("tungsten_bar");      // 焼結棒（水素中で通電焼結）
    public static final Item TUNGSTEN_WIRE         = Reg.item("tungsten_wire");     // 線引きした延性タングステン線
    // [1-16] 塩化水素ガス（乾いたもの、塩酸合成炉に水がないとき）→ トリクロロシラン SiHCl3（純度つき、蒸留で上がる）→ [1-17] 多結晶シリコン（純度つき）
    public static final Item HYDROGEN_CHLORIDE_GAS = Reg.item("hydrogen_chloride_gas");
    public static final Item TRICHLOROSILANE       = Reg.item("trichlorosilane");
    public static final Item POLYSILICON           = Reg.item("polysilicon");
    // [1-15] 金属シリコン（アーク炉で SiO2 + 2C → Si + 2CO、純度 98%＝冶金グレード。1個 ≈ 1kg）
    public static final Item METALLURGICAL_SILICON = Reg.item("metallurgical_silicon");
    // [1-46] 珪石（石英鉱脈の SiO2）。酸水素バーナー [7-02] で溶融石英に。ネザークォーツは使わない（化学ルール1）
    public static final Item VEIN_QUARTZ           = Reg.item("vein_quartz");
    // [1-33] ホウ素：ホウ砂 Na2B4O7·10H2O ＋ 硫酸 → ホウ酸 H3BO3（1個＝2mol）→ 焼く → B2O3（1個＝1mol）
    public static final Item BORAX                 = Reg.item("borax");
    public static final Item BORIC_ACID            = Reg.item("boric_acid");
    public static final Item BORON_TRIOXIDE        = Reg.item("boron_trioxide");
    // --- 水銀（[1-45] 辰砂 HgS の焙焼。HgS + O2 → Hg + SO2）---
    public static final Item CINNABAR_DUST         = Reg.item("cinnabar_dust");   // 辰砂（minecraft:cinnabar）を砕いた粉
    public static final Item MERCURY               = Reg.item("mercury", new Item.Properties().stacksTo(16)); // 液体の金属（容器入り）

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
    public static final Block WOLFRAMITE_ORE = Reg.ore("wolframite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE));
    // [1-03] 孔雀石（IMP2-01）：銅鉱床の上の、地表に近い酸化帯。浅い石の中（y40〜110）。モース硬度 3.5〜4
    public static final Block MALACHITE_ORE  = Reg.ore("malachite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
    // [1-22] 錫石（IMP2-01）：花崗岩の熱水鉱脈（鉄マンガン重石といっしょ）。固い（モース硬度 6〜7）
    public static final Block CASSITERITE_ORE = Reg.ore("cassiterite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE));
    public static final Block GALENA_ORE     = Reg.ore("galena_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE));
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

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
