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
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class ModBlocks {

    // 閃亜鉛鉱（ZnS）。Ge・In の本物の産源
    public static final Block SPHALERITE_ORE = register("sphalerite_ore", Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.STONE),
            true);

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
                                 BlockBehaviour.Properties props, boolean withItem) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, SemiconductorLab.id(name));
        Block block = factory.apply(props.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        if (withItem) {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
            BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        }
        return block;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(tab -> tab.accept(SPHALERITE_ORE));
    }
}