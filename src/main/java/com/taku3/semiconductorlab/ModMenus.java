package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.AirSeparationMenu;
import com.taku3.semiconductorlab.block.ArcFurnaceMenu;
import com.taku3.semiconductorlab.block.BessemerConverterMenu;
import com.taku3.semiconductorlab.block.CrystalPullerMenu;
import com.taku3.semiconductorlab.block.CompressorMenu;
import com.taku3.semiconductorlab.block.ElectrolyzerMenu;
import com.taku3.semiconductorlab.block.ElectricFurnaceMenu;
import com.taku3.semiconductorlab.block.AchesonFurnaceMenu;
import com.taku3.semiconductorlab.block.GasHolderMenu;
import com.taku3.semiconductorlab.block.HydrogenFurnaceMenu;
import com.taku3.semiconductorlab.block.SealingStationMenu;
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
    public static final MenuType<AirSeparationMenu> AIR_SEPARATION = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("air_separation_unit"),
            new MenuType<>(AirSeparationMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<com.taku3.semiconductorlab.block.TrichlorosilaneReactorMenu> TRICHLOROSILANE_REACTOR = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("trichlorosilane_reactor"),
            new MenuType<>(com.taku3.semiconductorlab.block.TrichlorosilaneReactorMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<ArcFurnaceMenu> ARC_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("arc_furnace"),
            new MenuType<>(ArcFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<CrystalPullerMenu> CRYSTAL_PULLER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("crystal_puller"),
            new MenuType<>(CrystalPullerMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<CompressorMenu> COMPRESSOR = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("compressor"),
            new MenuType<>(CompressorMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<SealingStationMenu> SEALING_STATION = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("sealing_station"),
            new MenuType<>(SealingStationMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<ElectricFurnaceMenu> ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("electric_furnace"),
            new MenuType<>(ElectricFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<AchesonFurnaceMenu> ACHESON_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("acheson_furnace"),
            new MenuType<>(AchesonFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<com.taku3.semiconductorlab.block.ReverberatoryFurnaceMenu> REVERBERATORY_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("reverberatory_furnace"),
            new MenuType<>(com.taku3.semiconductorlab.block.ReverberatoryFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<com.taku3.semiconductorlab.block.HclBurnerMenu> HCL_BURNER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("hcl_burner"),
            new MenuType<>(com.taku3.semiconductorlab.block.HclBurnerMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final MenuType<com.taku3.semiconductorlab.block.OxyhydrogenBurnerMenu> OXYHYDROGEN_BURNER = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("oxyhydrogen_burner"),
            new MenuType<>(com.taku3.semiconductorlab.block.OxyhydrogenBurnerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void initialize() {}
}