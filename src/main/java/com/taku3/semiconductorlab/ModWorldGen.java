package com.taku3.semiconductorlab;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.function.Predicate;

public class ModWorldGen {
    private static ResourceKey<PlacedFeature> key(String n) { return ResourceKey.create(Registries.PLACED_FEATURE, SemiconductorLab.id(n)); }
    public static final ResourceKey<PlacedFeature> SPHALERITE_ORE = key("sphalerite_ore"), GRAPHITE_ORE = key("graphite_ore"),
            MAGNETITE_ORE = key("magnetite_ore"), WOLFRAMITE_ORE = key("wolframite_ore"), GALENA_ORE = key("galena_ore"), STIBNITE_ORE = key("stibnite_ore"), PENTLANDITE_ORE = key("pentlandite_ore"), PYRITE_ORE = key("pyrite_ore"), IRON_SAND = key("iron_sand"),
            TRONA_DEPOSIT = key("trona_deposit"), ROCK_SALT_ORE = key("rock_salt_ore"), BORAX_DEPOSIT = key("borax_deposit"), QUARTZ_VEIN = key("quartz_vein"),
            MALACHITE_ORE = key("malachite_ore"), CASSITERITE_ORE = key("cassiterite_ore"),
            FLUORITE_ORE = key("fluorite_ore"), BAUXITE_ORE = key("bauxite_ore"), CHROMITE_ORE = key("chromite_ore"),
            NITRE_EARTH = key("nitre_earth_block"),
            NATURAL_ASPHALT = key("natural_asphalt"),
            RUBBER_TREES = key("rubber_trees");

    /** 鉱石1つぶんの生成ルール：どの placed feature を、どのバイオームの選び方で入れるか（GameTest が全バイオームに対して直接調べる） */
    public record OreRule(ResourceKey<PlacedFeature> feature, Predicate<BiomeSelectionContext> biomes, GenerationStep.Decoration step) {
        /** 鉱石（地下の鉱石の段階）。木など地表の物は step を指定する（IMP3 [2-29]） */
        public OreRule(ResourceKey<PlacedFeature> feature, Predicate<BiomeSelectionContext> biomes) { this(feature, biomes, GenerationStep.Decoration.UNDERGROUND_ORES); }
    }

    public static final List<OreRule> RULES = List.of(
            // オーバーワールド全体。[1-46] 石英鉱脈もここ（地殻の約12%が石英）。石・深層岩の割れ目を埋めた脈
            new OreRule(SPHALERITE_ORE, BiomeSelectors.foundInOverworld()),
            new OreRule(GRAPHITE_ORE, BiomeSelectors.foundInOverworld()),
            new OreRule(MAGNETITE_ORE, BiomeSelectors.foundInOverworld()),
            new OreRule(WOLFRAMITE_ORE, BiomeSelectors.foundInOverworld()),
            new OreRule(GALENA_ORE, BiomeSelectors.foundInOverworld()),
            new OreRule(STIBNITE_ORE, BiomeSelectors.foundInOverworld()),   // [1-43] 方鉛鉱と同じ帯（IMP3、S-002）
            new OreRule(PENTLANDITE_ORE, BiomeSelectors.foundInOverworld()),   // [1-37] 深い所（深層岩の中、IMP3、S-004）
            new OreRule(PYRITE_ORE, BiomeSelectors.foundInOverworld()),   // [2-43] 黄鉄鉱（どこにでもある、IMP3、S-004）
            new OreRule(QUARTZ_VEIN, BiomeSelectors.foundInOverworld()),
            // [1-03] 孔雀石（IMP2-01）：銅鉱床の上部の酸化帯。浅い石の中
            new OreRule(MALACHITE_ORE, BiomeSelectors.foundInOverworld()),
            // [1-22] 錫石（IMP2-01）：花崗岩だけを置き換える（鉄マンガン重石と同じ帯。本物も W・Sn は花崗岩の同じ鉱脈に出る）
            new OreRule(CASSITERITE_ORE, BiomeSelectors.foundInOverworld()),
            // [6-16] 硝石土（IMP2-02）：鍾乳石の洞窟（石灰岩の洞窟）だけ。鍾乳石ブロックを置き換える（マンモスケーブの硝石採掘のように洞窟の堆積物）
            new OreRule(NITRE_EARTH, BiomeSelectors.includeByKey(Biomes.DRIPSTONE_CAVES)),
            // [1-21] 蛍石（IMP4-01）：方鉛鉱と同じ帯（熱水鉱脈で方鉛鉱・閃亜鉛鉱といっしょに出る）。方鉛鉱より少ない
            new OreRule(FLUORITE_ORE, BiomeSelectors.foundInOverworld()),
            // [1-40] クロム鉄鉱（IMP2 O-015）：かんらん岩・蛇紋岩（マントルから来た岩）の近似として、深い深層岩だけを置き換える
            new OreRule(CHROMITE_ORE, BiomeSelectors.foundInOverworld()),
            // [1-24] ボーキサイト（IMP4-01）：ジャングル・サバンナだけ（熱帯で岩が風化して残った土）。地表近くの土・石を置き換える
            new OreRule(BAUXITE_ORE, BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA))),
            // 砂鉄：浜辺だけ（漂砂鉱床）
            new OreRule(IRON_SAND, BiomeSelectors.tag(BiomeTags.IS_BEACH)),
            // [2-03] トロナ：砂漠だけ（干上がった塩湖の蒸発岩）。地表〜浅い地下の砂・砂岩を置き換える
            new OreRule(TRONA_DEPOSIT, BiomeSelectors.includeByKey(Biomes.DESERT)),
            // [1-33] ホウ砂：砂漠だけ（トロナと同じ塩湖の蒸発岩。シアールズ湖では両方が出る）。トロナより少ない
            new OreRule(BORAX_DEPOSIT, BiomeSelectors.includeByKey(Biomes.DESERT)),
            // [6-04] 岩塩：乾燥バイオーム（砂漠・荒野）の地下の石だけ（古い海の蒸発岩層）
            // [2-31] 天然アスファルト（IMP3）：荒野・砂漠の地表近く（死海やタール坑のように、地下の油が地表で軽い成分を失って残った瀝青）。原油は作らない
            new OreRule(NATURAL_ASPHALT, BiomeSelectors.tag(BiomeTags.IS_BADLANDS).or(BiomeSelectors.includeByKey(Biomes.DESERT))),
            // [2-29] ゴムの木（IMP3）：ジャングルだけ（パラゴムノキは南米の熱帯雨林の木）。地表の植生の段階
            new OreRule(RUBBER_TREES, BiomeSelectors.tag(BiomeTags.IS_JUNGLE), GenerationStep.Decoration.VEGETAL_DECORATION),
            new OreRule(ROCK_SALT_ORE, BiomeSelectors.includeByKey(Biomes.DESERT).or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS))));

    public static void initialize() {
        for (OreRule r : RULES) BiomeModifications.addFeature(r.biomes(), r.step(), r.feature());
    }
}
