package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.machine.Machine;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 登録の道具（IMP-28）。幹ごとのファイル（Metallurgy・Power…）は、ここを使ってアイテム・ブロックを1行ずつ登録する。
 * 登録した順にクリエイティブタブへ並ぶ（材料タブ＝アイテム、自然タブ＝鉱石、機能タブ＝機械と道具のブロック）。
 */
public final class Reg {
    private static final List<Item> ITEMS = new ArrayList<>();
    private static final List<Block> NATURAL = new ArrayList<>();
    private static final List<Block> FUNCTIONAL = new ArrayList<>();

    private Reg() {}

    /** ふつうのアイテム（64個まで重なる） */
    public static Item item(String name) { return item(name, Item::new, new Item.Properties()); }

    /** 性質つきのアイテム（例：stacksTo(16)、データコンポーネント） */
    public static Item item(String name, Item.Properties props) { return item(name, Item::new, props); }

    /** 専用のクラスのアイテム（例：水銀真空計） */
    public static Item item(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
        Item item = factory.apply(props.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.add(item);
        return item;
    }

    /** ブロック＋ブロックのアイテム（タブには入れない。機械は Machine が機能タブに入れる） */
    public static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, SemiconductorLab.id(name));
        Block block = factory.apply(props.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        return block;
    }

    /** 鉱石・堆積物（自然ブロックのタブ） */
    public static Block ore(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block b = block(name, factory, props);
        NATURAL.add(b);
        return b;
    }

    /** 機械ではない道具のブロック（機能ブロックのタブ。例：手押しポンプ） */
    public static Block functional(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block b = block(name, factory, props);
        FUNCTIONAL.add(b);
        return b;
    }

    /** MOD のアイテム（ブロックのアイテムを除く）を登録した順に */
    public static List<Item> items() { return Collections.unmodifiableList(ITEMS); }

    /** クリエイティブタブに並べる（全部の幹を登録したあとで1回） */
    static void registerTabs() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> ITEMS.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> NATURAL.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
            Machine.all().forEach(m -> tab.accept(m.block));
            FUNCTIONAL.forEach(tab::accept);
        });
    }
}
