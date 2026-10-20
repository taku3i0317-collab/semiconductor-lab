package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity;
import com.taku3.semiconductorlab.block.CompressorBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
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
    public static final BlockEntityType<com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity> STEAM_TURBINE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("steam_turbine"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity::new, ModBlocks.STEAM_TURBINE).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity> LEAD_ACID_BATTERY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("lead_acid_battery"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity::new, ModBlocks.LEAD_ACID_BATTERY).build());
    public static final BlockEntityType<SteamEngineBlockEntity> STEAM_ENGINE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("steam_engine"), FabricBlockEntityTypeBuilder.create(SteamEngineBlockEntity::new, ModBlocks.STEAM_ENGINE).build());
    public static final BlockEntityType<ElectrolyzerBlockEntity> ELECTROLYZER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("electrolyzer"), FabricBlockEntityTypeBuilder.create(ElectrolyzerBlockEntity::new, ModBlocks.ELECTROLYZER).build());
    public static final BlockEntityType<GasHolderBlockEntity> GAS_HOLDER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("gas_holder"), FabricBlockEntityTypeBuilder.create(GasHolderBlockEntity::new, ModBlocks.GAS_HOLDER).build());
    public static final BlockEntityType<BessemerConverterBlockEntity> BESSEMER_CONVERTER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("bessemer_converter"), FabricBlockEntityTypeBuilder.create(BessemerConverterBlockEntity::new, ModBlocks.BESSEMER_CONVERTER).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.SaltPanBlockEntity> SALT_PAN = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("salt_pan"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.SaltPanBlockEntity::new, ModBlocks.SALT_PAN).build());
    public static final BlockEntityType<HydrogenFurnaceBlockEntity> HYDROGEN_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("hydrogen_furnace"), FabricBlockEntityTypeBuilder.create(HydrogenFurnaceBlockEntity::new, ModBlocks.HYDROGEN_FURNACE).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity> TRICHLOROSILANE_REACTOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("trichlorosilane_reactor"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity::new, ModBlocks.TRICHLOROSILANE_REACTOR).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.SiemensReactorBlockEntity> SIEMENS_REACTOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("siemens_reactor"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity::new, ModBlocks.SIEMENS_REACTOR).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.CzPullerBlockEntity> CZ_PULLER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("cz_puller"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.CzPullerBlockEntity::new, ModBlocks.CZ_PULLER).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.WaferSawBlockEntity> WAFER_SAW = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("wafer_saw"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.WaferSawBlockEntity::new, ModBlocks.WAFER_SAW).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity> ARC_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("arc_furnace"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity::new, ModBlocks.ARC_FURNACE).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.CrystalPullerBlockEntity> CRYSTAL_PULLER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("crystal_puller"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.CrystalPullerBlockEntity::new, ModBlocks.CRYSTAL_PULLER).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.AirSeparationBlockEntity> AIR_SEPARATION = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("air_separation_unit"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.AirSeparationBlockEntity::new, ModBlocks.AIR_SEPARATION_UNIT).build());
    public static final BlockEntityType<CompressorBlockEntity> COMPRESSOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("compressor"), FabricBlockEntityTypeBuilder.create(CompressorBlockEntity::new, ModBlocks.COMPRESSOR).build());
    public static final BlockEntityType<VacuumChamberBlockEntity> VACUUM_CHAMBER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("vacuum_chamber"), FabricBlockEntityTypeBuilder.create(VacuumChamberBlockEntity::new, ModBlocks.VACUUM_CHAMBER).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.SprengelPumpBlockEntity> SPRENGEL_PUMP = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("sprengel_pump"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.SprengelPumpBlockEntity::new, ModBlocks.SPRENGEL_PUMP).build());
    public static final BlockEntityType<SealingStationBlockEntity> SEALING_STATION = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("sealing_station"), FabricBlockEntityTypeBuilder.create(SealingStationBlockEntity::new, ModBlocks.SEALING_STATION).build());

    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("electric_furnace"), FabricBlockEntityTypeBuilder.create(ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE).build());
    public static final BlockEntityType<AchesonFurnaceBlockEntity> ACHESON_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("acheson_furnace"), FabricBlockEntityTypeBuilder.create(AchesonFurnaceBlockEntity::new, ModBlocks.ACHESON_FURNACE).build());
    public static final BlockEntityType<com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity> REVERBERATORY_FURNACE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("reverberatory_furnace"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity::new, ModBlocks.REVERBERATORY_FURNACE).build());   // 燃料式（電気の受け口なし）
    public static final BlockEntityType<com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("oxyhydrogen_burner"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity::new, ModBlocks.OXYHYDROGEN_BURNER).build());   // ガスボンベ式（電気の受け口なし）

    public static final BlockEntityType<com.taku3.semiconductorlab.block.HclBurnerBlockEntity> HCL_BURNER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SemiconductorLab.id("hcl_burner"), FabricBlockEntityTypeBuilder.create(com.taku3.semiconductorlab.block.HclBurnerBlockEntity::new, ModBlocks.HCL_BURNER).build());   // ガス式（電気の受け口なし）

    public static void initialize() {
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), ZONE_REFINING_FURNACE);   // 帯域精製炉：発振器の電源
        // 銅ケーブルは受け口を持たない（タスク06：導体。CableNetwork がまとめて配電する）
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), WATER_WHEEL);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), STEAM_ENGINE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), STEAM_TURBINE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.storageFor(dir), LEAD_ACID_BATTERY);   // 全面で充放電（タスク06）
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), ELECTROLYZER);   // 消費側：受け取り専用
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), BESSEMER_CONVERTER);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), HYDROGEN_FURNACE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), COMPRESSOR);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), AIR_SEPARATION);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), CRYSTAL_PULLER);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), ARC_FURNACE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), WAFER_SAW);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), CZ_PULLER);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), SIEMENS_REACTOR);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), TRICHLOROSILANE_REACTOR);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), SEALING_STATION);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), ELECTRIC_FURNACE);
        EnergyApi.ENERGY.registerForBlockEntity((be, dir) -> be.getStorage(), ACHESON_FURNACE);
    }
}