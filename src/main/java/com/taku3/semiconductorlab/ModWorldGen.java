package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModWorldGen {

    public static final ResourceKey<PlacedFeature> SPHALERITE_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id("sphalerite_ore"));

    public static final ResourceKey<PlacedFeature> GRAPHITE_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id("graphite_ore"));

    public static void initialize() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, GRAPHITE_ORE);
        // 閃亜鉛鉱：本物は熱水鉱脈に多い。ゲームではオーバーワールド全域の中層に生成
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, SPHALERITE_ORE);
    }
}