package com.taku3.semiconductorlab;

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
    public static final Item GRAPHITE_BOAT         = simple("graphite_boat");
    public static final Item FUSED_QUARTZ          = simple("fused_quartz");
    public static final Item QUARTZ_TUBE           = simple("quartz_tube");
    public static final Item INDUCTION_COIL        = simple("induction_coil");
    public static final Item LEAD_SCREW            = simple("lead_screw");
    public static final Item ELECTRIC_MOTOR        = simple("electric_motor");
    public static final Item HF_POWER_SUPPLY       = simple("hf_power_supply"); // ★仮：v0.2 の電力実装で置換

    // --- トランジスタと製品 ---
    public static final Item TRANSISTOR            = simple("transistor");           // 点接触型 (1947)
    public static final Item JUNCTION_TRANSISTOR_BLANK = simple("junction_transistor_blank"); // 合金化前
    public static final Item JUNCTION_TRANSISTOR   = simple("junction_transistor");  // 合金接合型 (1952)
    public static final Item TRANSISTOR_RADIO      = register("transistor_radio", Item::new, new Item.Properties().stacksTo(1));

    public static final List<Item> ALL = List.of(
            GRAPHITE, SPHALERITE, COAL_ASH, ZINC_INGOT, GERMANIUM_CONCENTRATE, INDIUM,
            SULFUR_POWDER, SULFUR_DIOXIDE, SULFURIC_ACID, PHOSPHORUS,
            GERMANIUM_DIOXIDE, RAW_GERMANIUM, REFINED_GERMANIUM,
            N_TYPE_GERMANIUM, P_TYPE_GERMANIUM,
            GRAPHITE_BOAT_BLANK, GRAPHITE_BOAT, FUSED_QUARTZ, QUARTZ_TUBE, INDUCTION_COIL, LEAD_SCREW, ELECTRIC_MOTOR, HF_POWER_SUPPLY,
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