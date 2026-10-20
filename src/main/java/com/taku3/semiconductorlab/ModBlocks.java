package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;

import com.taku3.semiconductorlab.block.HandVacuumPumpBlock;

import java.util.function.Function;

public class ModBlocks {

    // [1-46] 石英鉱脈（熱水が岩の割れ目に SiO2 を沈めた白い脈）。石英はモース硬度7なので石のツルハシ以上
    public static final Block QUARTZ_VEIN    = register("quartz_vein", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE), true);
    public static final Block SPHALERITE_ORE = register("sphalerite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f).sound(SoundType.STONE), true);

    // 黒鉛鉱石（変成岩中の天然黒鉛。スリランカの脈状黒鉛など）
    public static final Block GRAPHITE_ORE = register("graphite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), true);

    // 帯域精製炉（Pfann, 1952）。電気で動く（組み込みの高周波発振器 [4-09]）。点灯中は光る

    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ
    // [B-12] 太陽電池（発電機。昼・空・雨なしで 60W）

    // 電解槽 [6-01]（ガラス槽＋黒鉛電極）。電気を消費する機械の第1号

    // ガスホルダー [6-02]（水封式の低圧ガスタンク。鉄のベル＋水槽）

    // ベッセマー転炉 [1-26]（鉄の殻＋粘土の内張り＋底の羽口。送風は電動）。送風中は炉口が光る

    // 水素炉 [1-28]（耐火れんがの管状炉＋水素の流れ。WO3 の水素還元と W 粉の通電焼結）。加熱中は光る

    // 電気炉 [4-12]（耐火れんがの箱＋黒鉛棒の発熱体。抵抗加熱で約1500℃）。加熱中は光る

    // アチソン炉 [3-10]（1896。耐火れんがの箱に炭素＋砂、両端の黒鉛電極から中身に直接通電。約2500℃で人造黒鉛）。赤熱すると光る

    // 反射炉 [3-06]（焙焼炉。燃焼室と炉床を分け、炎の熱を天井で反射させる。石炭で硫化鉱を焼き、SO2 を集める）。燃焼中は光る

    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。炎が出ている間は光る

    // 塩酸合成炉 [6-06]（酸水素バーナーの派生。石英の火口で水素を塩素中で燃やし H2 + Cl2 → 2HCl、吸収塔で水に溶かす）。燃焼中は光る

    // 圧縮機 [6-03]（電動モーターのピストン圧縮機。隣のガスホルダーのガスを鋼のボンベに 50mol ずつ詰める）

    // 真空容器 [5-11]（ガラスのベルジャー＋鉄の台板）。中の圧力を持つ
    // 手押しピストンポンプ [5-01]（ゲーリケ 1650頃）。隣の真空容器を右クリックで排気
    public static final Block HAND_VACUUM_PUMP = register("hand_vacuum_pump", HandVacuumPumpBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.METAL), true);

    // 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器が十分低圧のときだけ組立品を封じる。動作中は光る

    // 銅ケーブル（v0.2a）

    // v0.2a 鉱石（産状の近似）：磁鉄鉱=深層岩／砂鉄=浜辺の砂／鉄マンガン重石=花崗岩／方鉛鉱=閃亜鉛鉱と同じ帯
    public static final Block MAGNETITE_ORE  = register("magnetite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), true);
    public static final Block IRON_SAND      = register("iron_sand", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f).sound(SoundType.SAND), true);
    public static final Block WOLFRAMITE_ORE = register("wolframite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE), true);
    public static final Block GALENA_ORE     = register("galena_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE), true);

    // [2-03] トロナ堆積（干上がった塩湖の蒸発岩 Na2CO3·NaHCO3·2H2O）。砂漠の地表〜浅い地下の砂・砂岩に生成。軟らかい（モース硬度 2.5〜3）
    public static final Block BORAX_DEPOSIT  = register("borax_deposit", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(1.0f).sound(SoundType.STONE), true);   // [1-33] ホウ砂（砂漠の塩湖の蒸発岩）
    public static final Block TRONA_DEPOSIT  = register("trona_deposit", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(1.0f).sound(SoundType.STONE), true);

    // [6-04] 岩塩鉱（古い海が干上がってできた蒸発岩の層）。砂漠・荒野の地下の石に生成。軟らかい（モース硬度 2.5）
    public static final Block ROCK_SALT_ORE  = register("rock_salt_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(1.5f, 3.0f).sound(SoundType.STONE), true);

    // [0-16] 塩田（揚げ浜式。海水を砂にまいて天日で濃くし、釜で煮詰める）。海・浜バイオームで、横に水があり、空が見えて晴れているときだけ塩がたまる

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
                                 BlockBehaviour.Properties props, boolean withItem) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, SemiconductorLab.id(name));
        Block block = factory.apply(props.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        if (withItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
            Registry.register(BuiltInRegistries.ITEM, itemKey,
                    new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        }
        return block;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> { tab.accept(QUARTZ_VEIN); tab.accept(BORAX_DEPOSIT); tab.accept(SPHALERITE_ORE); tab.accept(GRAPHITE_ORE); tab.accept(MAGNETITE_ORE); tab.accept(IRON_SAND); tab.accept(WOLFRAMITE_ORE); tab.accept(GALENA_ORE); tab.accept(TRONA_DEPOSIT); tab.accept(ROCK_SALT_ORE); });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> { com.taku3.semiconductorlab.machine.Machine.all().forEach(m -> tab.accept(m.block)); tab.accept(HAND_VACUUM_PUMP); });
    }
}