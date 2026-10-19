package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlock;
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

    // 帯域精製炉（Pfann, 1952）。点灯中は光る
    public static final Block ZONE_REFINING_FURNACE = register("zone_refining_furnace", ZoneRefiningFurnaceBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(3.5f).sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0), true);

    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ
    public static final Block WATER_WHEEL = register("water_wheel", p -> new GeneratorBlock(p, WaterWheelBlockEntity::new, () -> ModBlockEntities.WATER_WHEEL), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD), true);
    public static final Block STEAM_ENGINE = register("steam_engine", p -> new GeneratorBlock(p, SteamEngineBlockEntity::new, () -> ModBlockEntities.STEAM_ENGINE), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.METAL), true);

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
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> { tab.accept(ZONE_REFINING_FURNACE); tab.accept(COPPER_CABLE); tab.accept(WATER_WHEEL); tab.accept(STEAM_ENGINE); });
    }
}