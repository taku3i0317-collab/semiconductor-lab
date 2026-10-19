package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class ModWorldGen {
    private static ResourceKey<PlacedFeature> key(String n) { return ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id(n)); }
    public static final ResourceKey<PlacedFeature> SPHALERITE_ORE = key("sphalerite_ore"), GRAPHITE_ORE = key("graphite_ore"),
            MAGNETITE_ORE = key("magnetite_ore"), WOLFRAMITE_ORE = key("wolframite_ore"), GALENA_ORE = key("galena_ore"), IRON_SAND = key("iron_sand");

    public static void initialize() {
        for (var k : List.of(SPHALERITE_ORE, GRAPHITE_ORE, MAGNETITE_ORE, WOLFRAMITE_ORE, GALENA_ORE))
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, k);
        // 砂鉄：浜辺だけ（漂砂鉱床）
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BEACH), GenerationStep.Decoration.UNDERGROUND_ORES, IRON_SAND);
    }
}