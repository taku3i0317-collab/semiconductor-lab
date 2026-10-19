package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class ModWorldGen {
    private static ResourceKey<PlacedFeature> key(String n) { return ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id(n)); }
    public static final ResourceKey<PlacedFeature> SPHALERITE_ORE = key("sphalerite_ore"), GRAPHITE_ORE = key("graphite_ore"),
            MAGNETITE_ORE = key("magnetite_ore"), WOLFRAMITE_ORE = key("wolframite_ore"), GALENA_ORE = key("galena_ore"), IRON_SAND = key("iron_sand"),
            TRONA_DEPOSIT = key("trona_deposit"), ROCK_SALT_ORE = key("rock_salt_ore");

    public static void initialize() {
        for (var k : List.of(SPHALERITE_ORE, GRAPHITE_ORE, MAGNETITE_ORE, WOLFRAMITE_ORE, GALENA_ORE))
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, k);
        // 砂鉄：浜辺だけ（漂砂鉱床）
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_BEACH), GenerationStep.Decoration.UNDERGROUND_ORES, IRON_SAND);
        // [2-03] トロナ：砂漠だけ（干上がった塩湖の蒸発岩）。地表〜浅い地下の砂・砂岩を置き換える
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DESERT), GenerationStep.Decoration.UNDERGROUND_ORES, TRONA_DEPOSIT);
        // [6-04] 岩塩：乾燥バイオーム（砂漠・荒野）の地下の石だけ（古い海の蒸発岩層）
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.DESERT).or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS)), GenerationStep.Decoration.UNDERGROUND_ORES, ROCK_SALT_ORE);
    }
}