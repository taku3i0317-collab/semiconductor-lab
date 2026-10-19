package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.BessemerConverterBlock;
import com.taku3.semiconductorlab.block.CompressorBlockEntity;
import com.taku3.semiconductorlab.block.ConsumerMachineBlock;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerBlock;
import com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderBlock;
import com.taku3.semiconductorlab.block.HandVacuumPumpBlock;
import com.taku3.semiconductorlab.block.VacuumChamberBlock;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.energy.CableBlock;
import com.taku3.semiconductorlab.energy.GeneratorBlock;
import com.taku3.semiconductorlab.energy.WaterWheelBlockEntity;
import com.taku3.semiconductorlab.energy.SteamEngineBlockEntity;
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

import java.util.function.Function;

public class ModBlocks {

    public static final Block SPHALERITE_ORE = register("sphalerite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f).sound(SoundType.STONE), true);

    // 黒鉛鉱石（変成岩中の天然黒鉛。スリランカの脈状黒鉛など）
    public static final Block GRAPHITE_ORE = register("graphite_ore", Block::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops()
                    .strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), true);

    // 帯域精製炉（Pfann, 1952）。電気で動く（組み込みの高周波発振器 [4-09]）。点灯中は光る
    public static final Block ZONE_REFINING_FURNACE = register("zone_refining_furnace", p -> new ConsumerMachineBlock(p, ZoneRefiningFurnaceBlockEntity::new, () -> ModBlockEntities.ZONE_REFINING_FURNACE),
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(3.5f).sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0), true);

    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ
    public static final Block WATER_WHEEL = register("water_wheel", p -> new GeneratorBlock(p, WaterWheelBlockEntity::new, () -> ModBlockEntities.WATER_WHEEL), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD), true);
    public static final Block STEAM_ENGINE = register("steam_engine", p -> new GeneratorBlock(p, SteamEngineBlockEntity::new, () -> ModBlockEntities.STEAM_ENGINE), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.METAL), true);

    // 電解槽 [6-01]（ガラス槽＋黒鉛電極）。電気を消費する機械の第1号
    public static final Block ELECTROLYZER = register("electrolyzer", ElectrolyzerBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.GLASS), true);

    // ガスホルダー [6-02]（水封式の低圧ガスタンク。鉄のベル＋水槽）
    public static final Block GAS_HOLDER = register("gas_holder", GasHolderBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.METAL), true);

    // ベッセマー転炉 [1-26]（鉄の殻＋粘土の内張り＋底の羽口。送風は電動）。送風中は炉口が光る
    public static final Block BESSEMER_CONVERTER = register("bessemer_converter", BessemerConverterBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.METAL)
            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0), true);

    // 水素炉 [1-28]（耐火れんがの管状炉＋水素の流れ。WO3 の水素還元と W 粉の通電焼結）。加熱中は光る
    public static final Block HYDROGEN_FURNACE = register("hydrogen_furnace", p -> new ConsumerMachineBlock(p, HydrogenFurnaceBlockEntity::new, () -> ModBlockEntities.HYDROGEN_FURNACE), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.METAL)
            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0), true);

    // 電気炉 [4-12]（耐火れんがの箱＋黒鉛棒の発熱体。抵抗加熱で約1500℃）。加熱中は光る
    public static final Block ELECTRIC_FURNACE = register("electric_furnace", p -> new ConsumerMachineBlock(p, ElectricFurnaceBlockEntity::new, () -> ModBlockEntities.ELECTRIC_FURNACE), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 12 : 0), true);

    // アチソン炉 [3-10]（1896。耐火れんがの箱に炭素＋砂、両端の黒鉛電極から中身に直接通電。約2500℃で人造黒鉛）。赤熱すると光る
    public static final Block ACHESON_FURNACE = register("acheson_furnace", p -> new ConsumerMachineBlock(p, AchesonFurnaceBlockEntity::new, () -> ModBlockEntities.ACHESON_FURNACE), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0), true);

    // 圧縮機 [6-03]（電動モーターのピストン圧縮機。隣のガスホルダーのガスを鋼のボンベに 50mol ずつ詰める）
    public static final Block COMPRESSOR = register("compressor", p -> new ConsumerMachineBlock(p, CompressorBlockEntity::new, () -> ModBlockEntities.COMPRESSOR), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.METAL), true);

    // 真空容器 [5-11]（ガラスのベルジャー＋鉄の台板）。中の圧力を持つ
    public static final Block VACUUM_CHAMBER = register("vacuum_chamber", VacuumChamberBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0f).sound(SoundType.GLASS), true);
    // 手押しピストンポンプ [5-01]（ゲーリケ 1650頃）。隣の真空容器を右クリックで排気
    public static final Block HAND_VACUUM_PUMP = register("hand_vacuum_pump", HandVacuumPumpBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.METAL), true);

    // 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器が十分低圧のときだけ組立品を封じる。動作中は光る
    public static final Block SEALING_STATION = register("sealing_station", p -> new ConsumerMachineBlock(p, SealingStationBlockEntity::new, () -> ModBlockEntities.SEALING_STATION), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.METAL)
            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 7 : 0), true);

    // 銅ケーブル（v0.2a）
    public static final Block COPPER_CABLE = register("copper_cable", CableBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0f).sound(SoundType.COPPER).noOcclusion(), true);

    // v0.2a 鉱石（産状の近似）：磁鉄鉱=深層岩／砂鉄=浜辺の砂／鉄マンガン重石=花崗岩／方鉛鉱=閃亜鉛鉱と同じ帯
    public static final Block MAGNETITE_ORE  = register("magnetite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), true);
    public static final Block IRON_SAND      = register("iron_sand", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f).sound(SoundType.SAND), true);
    public static final Block WOLFRAMITE_ORE = register("wolframite_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f, 3.0f).sound(SoundType.STONE), true);
    public static final Block GALENA_ORE     = register("galena_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f).sound(SoundType.STONE), true);

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
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> { tab.accept(SPHALERITE_ORE); tab.accept(GRAPHITE_ORE); tab.accept(MAGNETITE_ORE); tab.accept(IRON_SAND); tab.accept(WOLFRAMITE_ORE); tab.accept(GALENA_ORE); });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> { tab.accept(ZONE_REFINING_FURNACE); tab.accept(COPPER_CABLE); tab.accept(WATER_WHEEL); tab.accept(STEAM_ENGINE); tab.accept(ELECTROLYZER); tab.accept(GAS_HOLDER); tab.accept(BESSEMER_CONVERTER); tab.accept(ELECTRIC_FURNACE); tab.accept(ACHESON_FURNACE); tab.accept(HYDROGEN_FURNACE); tab.accept(COMPRESSOR); tab.accept(VACUUM_CHAMBER); tab.accept(HAND_VACUUM_PUMP); tab.accept(SEALING_STATION); });
    }
}