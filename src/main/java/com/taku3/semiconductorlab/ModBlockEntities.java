package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.energy.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<ZoneRefiningFurnaceBlockEntity> ZONE_REFINING_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("zone_refining_furnace"), FabricBlockEntityTypeBuilder.create(ZoneRefiningFurnaceBlockEntity::new, ModBlocks.ZONE_REFINING_FURNACE).build());
    public static final BlockEntityType<CableBlockEntity> COPPER_CABLE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("copper_cable"), FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, ModBlocks.COPPER_CABLE).build());
    public static final BlockEntityType<WaterWheelBlockEntity> WATER_WHEEL = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("water_wheel"), FabricBlockEntityTypeBuilder.create(WaterWheelBlockEntity::new, ModBlocks.WATER_WHEEL).build());
    public static final BlockEntityType<SteamEngineBlockEntity> STEAM_ENGINE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("steam_engine"), FabricBlockEntityTypeBuilder.create(SteamEngineBlockEntity::new, ModBlocks.STEAM_ENGINE).build());

    public static void initialize() {
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), COPPER_CABLE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), WATER_WHEEL);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), STEAM_ENGINE);
    }
}