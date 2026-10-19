package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.BessemerConverterMenu;
import com.taku3.semiconductorlab.block.CompressorMenu;
import com.taku3.semiconductorlab.block.ElectrolyzerMenu;
import com.taku3.semiconductorlab.block.GasHolderMenu;
import com.taku3.semiconductorlab.block.HydrogenFurnaceMenu;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceMenu;
import com.taku3.semiconductorlab.energy.SteamEngineMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
    public static final MenuType<ZoneRefiningFurnaceMenu> ZONE_REFINING_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("zone_refining_furnace"),
            new MenuType<>(ZoneRefiningFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<SteamEngineMenu> STEAM_ENGINE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("steam_engine"),
            new MenuType<>(SteamEngineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<ElectrolyzerMenu> ELECTROLYZER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("electrolyzer"),
            new MenuType<>(ElectrolyzerMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<GasHolderMenu> GAS_HOLDER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("gas_holder"),
            new MenuType<>(GasHolderMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<BessemerConverterMenu> BESSEMER_CONVERTER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("bessemer_converter"),
            new MenuType<>(BessemerConverterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<HydrogenFurnaceMenu> HYDROGEN_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("hydrogen_furnace"),
            new MenuType<>(HydrogenFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<CompressorMenu> COMPRESSOR = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("compressor"),
            new MenuType<>(CompressorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void initialize() {}
}