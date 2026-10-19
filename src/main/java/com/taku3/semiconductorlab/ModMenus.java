package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
    public static final MenuType<ZoneRefiningFurnaceMenu> ZONE_REFINING_FURNACE = Registry.register(
            BuiltInRegistries.MENU, SemiconductorLab.id("zone_refining_furnace"),
            new MenuType<>(ZoneRefiningFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void initialize() {}
}