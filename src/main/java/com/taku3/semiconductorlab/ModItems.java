package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {

    // 第1章：ゲルマニウムの入手と精製
    public static final Item COAL_ASH          = register("coal_ash", Item::new, new Item.Properties());
    public static final Item RAW_GERMANIUM     = register("raw_germanium", Item::new, new Item.Properties());
    public static final Item REFINED_GERMANIUM = register("refined_germanium", Item::new, new Item.Properties());
    public static final Item TRANSISTOR        = register("transistor", Item::new, new Item.Properties());
    public static final Item TRANSISTOR_RADIO  = register("transistor_radio", Item::new, new Item.Properties().stacksTo(1));

    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SemiconductorLab.id(name));
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
            tab.accept(COAL_ASH);
            tab.accept(RAW_GERMANIUM);
            tab.accept(REFINED_GERMANIUM);
            tab.accept(TRANSISTOR);
            tab.accept(TRANSISTOR_RADIO);
        });
    }
}