package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.content.Survival;
import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity;
import com.taku3.semiconductorlab.block.CompressorBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.GasType;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.HandVacuumPumpBlock;
import com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.item.MercuryVacuumGaugeItem;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.HclBurnerBlockEntity;
import com.taku3.semiconductorlab.block.SaltPanBlockEntity;
import com.taku3.semiconductorlab.block.SprengelPumpBlockEntity;
import com.taku3.semiconductorlab.block.AirSeparationBlockEntity;
import com.taku3.semiconductorlab.block.CrystalPullerBlockEntity;
import com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity;
import com.taku3.semiconductorlab.energy.CableBlockEntity;
import com.taku3.semiconductorlab.energy.CableNetwork;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.energy.GeneratorBlockEntity;
import com.taku3.semiconductorlab.energy.SteamEngineBlockEntity;
import com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity;
import com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyApi;
import com.taku3.semiconductorlab.energy.WaterWheelBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import net.minecraft.tags.BiomeTags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 自動テスト（GameTest）。`./gradlew.bat runGameTest` または tools/test.ps1 で実行。
 * TEST_QUEUE.md の「機械的に判定できる項目」をここに書く。見た目・GUI の操作感は人間が見る。
 *
 * 書き方の方針：
 * - 機械の中身は「BE の tick を手で N 回呼ぶ」ことで、115秒かかる電解も一瞬で確かめる（電気はテストが直接入れる）
 * - 水の流れ・ケーブル・ガスの吸い込みなど「世界の動き」が大事なものは本当に tick を進めて確かめる
 * - ログの [LAB] 行は tools/test.ps1 が拾って要約に出す
 */
public abstract class LabTestBase {
    protected static final Logger LOG = LoggerFactory.getLogger("semiconductor-lab-gametest");

    protected static BlockPos p(int x, int y, int z) { return new BlockPos(x, y, z); }

    /** 消費機械の tick を n 回まわす。feedJ > 0 なら毎回その分の電気を入れてから回す（ケーブルの代わり） */
    protected static void tickConsumer(GameTestHelper h, BlockPos rel, ConsumerBlockEntity be, int n, long feedJ) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) {
            if (feedJ > 0) be.getStorage().insert(feedJ, false);
            ConsumerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        }
    }

    protected static void tickGenerator(GameTestHelper h, BlockPos rel, GeneratorBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) GeneratorBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    /** 帯域精製炉を n tick まわす（発振器の電力 3kW をテストが直接入れる） */
    protected static void tickRefiner(GameTestHelper h, BlockPos rel, ZoneRefiningFurnaceBlockEntity be, int n) {
        tickConsumer(h, rel, be, n, EnergyStorage.wattsToJoulesPerTick(ZoneRefiningFurnaceBlockEntity.WATTS));
    }

    protected static void floor(GameTestHelper h, int size) {
        for (int x = 0; x < size; x++) for (int z = 0; z < size; z++) h.setBlock(p(x, 0, z), Blocks.STONE);
    }

    // =====================================================================
    // 鉱石6種：feature を置くと「対象ブロックだけ」が鉱石に置き換わる
    // =====================================================================

    /**
     * 1〜7 の立方体を target / other の市松模様で埋め、中心に feature を置く。
     * 置く前後で -3〜11 の範囲を比べ、「変わったブロックは全部 鉱石 になり、元は replaceable だった」ことを確かめる。
     */
    protected void oreFeature(GameTestHelper h, String id, Block target, Block other, Block ore, Predicate<BlockState> replaceable) {
        ServerLevel lv = h.getLevel();
        for (int x = 1; x <= 7; x++) for (int y = 1; y <= 7; y++) for (int z = 1; z <= 7; z++)
            h.setBlock(p(x, y, z), ((x + y + z) & 1) == 0 ? target : other);
        Map<BlockPos, BlockState> before = new HashMap<>();
        for (int x = -3; x <= 11; x++) for (int y = -3; y <= 11; y++) for (int z = -3; z <= 11; z++) {
            BlockPos a = h.absolutePos(p(x, y, z));
            before.put(a, lv.getBlockState(a));
        }
        Registry<Feature> reg = lv.registryAccess().lookupOrThrow(Registries.FEATURE);
        Feature f = reg.getValue(SemiconductorLab.id(id));
        h.assertTrue(f != null, "feature " + id + " is not registered");
        int placed = 0;
        for (long seed = 0; seed < 8 && placed == 0; seed++) {
            f.place(lv, lv.getChunkSource().getGenerator(), RandomSource.create(seed), h.absolutePos(p(4, 4, 4)));
            placed = 0;
            for (BlockPos a : before.keySet()) if (lv.getBlockState(a).is(ore)) placed++;
        }
        h.assertTrue(placed > 0, "feature " + id + " placed no ore in a block of " + target);
        for (Map.Entry<BlockPos, BlockState> e : before.entrySet()) {
            BlockState now = lv.getBlockState(e.getKey());
            if (now.equals(e.getValue())) continue;
            h.assertTrue(now.is(ore), "feature " + id + " changed " + e.getValue() + " into " + now + " at " + e.getKey());
            h.assertTrue(replaceable.test(e.getValue()), "feature " + id + " replaced a non-target block " + e.getValue() + " at " + e.getKey());
        }
        LOG.info("[LAB] ore {}: placed {} blocks, all on targets ({} kept)", id, placed, other);
        h.succeed();
    }

    protected static boolean stoneOrDeepslate(BlockState s) { return s.is(BlockTags.STONE_ORE_REPLACEABLES) || s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES); }

    // =====================================================================
    // ケーブル（タスク06：ネットワーク方式）
    // =====================================================================

    /** 並べた順に BE の tick を n 回まわす（発電機 → ケーブル → 機械 → 電池 の順に並べる） */
    protected static void tickAll(GameTestHelper h, int n, BlockPos... rels) {
        ServerLevel lv = h.getLevel();
        for (int i = 0; i < n; i++) for (BlockPos r : rels) {
            BlockPos a = h.absolutePos(r);
            BlockState s = lv.getBlockState(a);
            switch (lv.getBlockEntity(a)) {
                case GeneratorBlockEntity g -> GeneratorBlockEntity.tick(lv, a, s, g);
                case CableBlockEntity c -> CableBlockEntity.tick(lv, a, s, c);
                case ConsumerBlockEntity c -> ConsumerBlockEntity.tick(lv, a, s, c);
                case LeadAcidBatteryBlockEntity b -> LeadAcidBatteryBlockEntity.tick(lv, a, s, b);
                case com.taku3.semiconductorlab.energy.PowerConverterBlockEntity c -> com.taku3.semiconductorlab.energy.PowerConverterBlockEntity.tick(lv, a, s, c);
                case null, default -> {}
            }
        }
    }

    protected static BlockPos[] line(int x0, int x1, int y, int z) {
        BlockPos[] r = new BlockPos[x1 - x0 + 1];
        for (int x = x0; x <= x1; x++) r[x - x0] = p(x, y, z);
        return r;
    }

    protected static BlockPos[] concat(BlockPos[]... parts) {
        List<BlockPos> l = new ArrayList<>();
        for (BlockPos[] b : parts) l.addAll(List.of(b));
        return l.toArray(new BlockPos[0]);
    }

    // =====================================================================
    // 帯域精製炉
    // =====================================================================

    /** GeCl4 蒸留 [1-34] を経た還元ゲルマニウム（帯域精製で 10N まで行ける） */
    protected static ItemStack rawChloride(int n) {
        ItemStack s = new ItemStack(Metallurgy.RAW_GERMANIUM, n);
        s.set(ModDataComponents.CHLORIDE_ROUTE, true);
        return s;
    }

    protected static ItemStack refined(double purity, int n) {
        ItemStack s = new ItemStack(Semiconductor.REFINED_GERMANIUM, n);
        s.set(ModDataComponents.PURITY, purity);
        return s;
    }

    protected static double purityOf(ItemStack s) { Double d = s.get(ModDataComponents.PURITY); return d == null ? -1 : d; }

    // =====================================================================
    // データ（レシピ・進捗・関数）とバニラ要素
    // =====================================================================

    protected static final String[] RECIPES = {
            "acheson_furnace", "bessemer_converter", "cinnabar_dust", "coal_ash", "compressor", "copper_cable", "dynamo", "electric_furnace", "electric_motor", "electrolyzer", "empty_gas_cylinder", "oxyhydrogen_burner", "sodium_carbonate", "glass_batch", "soda_lime_glass", "soda_lime_glass_from_blasting",
            "gas_holder", "germanium_concentrate_from_ash", "germanium_dioxide",
            "graphite_boat", "graphite_boat_blank", "graphite_rod", "synthetic_graphite_boat", "hand_vacuum_pump", "hydrogen_furnace", "induction_coil", "junction_transistor",
            "junction_transistor_blank", "lead_ingot", "lead_screw", "lead_acid_battery", "leyden_jar", "litharge", "magnet", "mercury", "glass_bottle", "iron_flask", "lead_bottle", "cinnabar_retort_charge", "mercury_vacuum_gauge", "phosphorus", "quartz_tube", "quicklime", "quicklime_from_blasting", "slaked_lime", "raw_germanium", "refractory_brick", "refractory_brick_blank", "sealing_station", "sprengel_pump", "air_separation_unit", "crystal_puller", "arc_furnace", "boric_acid", "boron_trioxide", "self_excited_dynamo", "steam_engine", "steam_turbine",
            "sulfuric_acid", "sulfur_dioxide", "sulfur_powder_from_block", "sulfur_powder_from_potent", "sulfur_powder_from_spike", "transistor",
            "assembly_bench", "bellows", "bloomery", "blast_furnace_hearth", "tuyere", "iron_ingot_from_bloom", "drafting_table", "probe_station", "wolframite_soda_charge", "sodium_tungstate", "tungstic_acid", "tungsten_trioxide_from_acid", "tungsten_wire", "vacuum_chamber", "vacuum_tube_assembly", "water_wheel", "zinc_ingot", "zinc_from_oxide", "reverberatory_furnace", "zone_refining_furnace",
            "salt_from_rock_salt", "salt_pan", "hcl_burner",
            "crude_germanium_tetrachloride", "quartz_still", "germanium_tetrachloride", "high_purity_germanium_dioxide"};

    /** かまど系（SMELTING / BLASTING）に1個入れたときの出力。レシピがなければ EMPTY */
    protected static <T extends net.minecraft.world.item.crafting.AbstractCookingRecipe> ItemStack cook(GameTestHelper h,
            net.minecraft.world.item.crafting.RecipeType<T> type, net.minecraft.world.item.Item in) {
        var input = new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(in));
        return h.getLevel().getServer().getRecipeManager().getRecipeFor(type, input, h.getLevel())
                .map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    /** レシピ id の材料に item が含まれるか（placementInfo の Ingredient で判定） */
    /** 組立台のレシピ（IMP-33）で、製品 result の部品に item が入っているか。何個か（なければ 0） */
    protected static int assemblyCount(net.minecraft.world.item.Item result, net.minecraft.world.item.Item item) {
        var a = com.taku3.semiconductorlab.machine.Assembly.of(result);
        if (a == null) return 0;
        return a.parts.stream().filter(p -> p.ok().test(new ItemStack(item))).mapToInt(com.taku3.semiconductorlab.machine.Assembly.Part::count).sum();
    }

    protected static boolean recipeUses(GameTestHelper h, String id, net.minecraft.world.item.Item item) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
        return h.getLevel().getServer().getRecipeManager().byKey(key)
                .map(r -> r.value().placementInfo().ingredients().stream().anyMatch(i -> i.test(new ItemStack(item)))).orElse(false);
    }

    // =====================================================================
    // [3-06] 反射炉（焙焼炉）：鉱石＋燃料 → 金属酸化物/金属 ＋ SO2（＋閃亜鉛鉱の副産物）
    // =====================================================================

    protected static void tickRoaster(GameTestHelper h, BlockPos rel, ReverberatoryFurnaceBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) ReverberatoryFurnaceBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    protected static final String[] CHAPTER1 = {"root", "coal_ash", "concentrate", "dioxide", "sulfuric_acid", "raw_germanium",
            "refined", "n_type", "transistor", "junction", "radio"};
    protected static final String[] GUIDE_ADV = {"root", "welcome", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"};
    protected static final String[] GUIDE_FN = {"give_guidebook", "guide/welcome", "guide/raw_germanium", "guide/refined",
            "guide/n_type", "guide/junction", "guide/radio", "guide/sulfuric_acid"};

    // =====================================================================
    // 機械のレシピの書き出し（タスク24・25）：docs/machine_recipes.json
    // 各機械の定数（電力・時間・量）とアイテムの登録から作るので、コードを変えれば表も変わる。node.html の「ゲームでの作り方」の元データ
    // 量は数字の項目で持つ：count（1回あたり）・unit（"mol" など）・per_runs（N回で count 個へる）・consumed（false＝減らない）・
    //   max（1回に入れられる上限）・alt（代わりに使えるアイテム）・cond（条件の短い説明）。表示の文は gen_tree_views.py が作る
    // =====================================================================

    protected static String iid(net.minecraft.world.item.Item it) { return BuiltInRegistries.ITEM.getKey(it).toString(); }
    /** 材料・生成物1つ。item はアイテムか、アイテムでない物（空気・電気など）の名前 */
    protected static com.google.gson.JsonObject io(Object item, double count) {
        var o = new com.google.gson.JsonObject();
        o.addProperty("item", item instanceof net.minecraft.world.item.Item it ? iid(it) : String.valueOf(item));
        if (count == Math.rint(count)) o.addProperty("count", (long) count); else o.addProperty("count", count);
        return o;
    }
    protected static com.google.gson.JsonObject mol(Object item, double n) { var o = io(item, n); o.addProperty("unit", "mol"); return o; }
    protected static com.google.gson.JsonObject kept(com.google.gson.JsonObject o) { o.addProperty("consumed", false); return o; }
    protected static com.google.gson.JsonObject perRuns(com.google.gson.JsonObject o, int runs) { o.addProperty("per_runs", runs); return o; }
    protected static com.google.gson.JsonObject max(com.google.gson.JsonObject o, int m) { o.addProperty("max", m); return o; }
    protected static com.google.gson.JsonObject alt(com.google.gson.JsonObject o, net.minecraft.world.item.Item... its) {
        var a = new com.google.gson.JsonArray(); for (var it : its) a.add(iid(it)); o.add("alt", a); return o;
    }
    protected static com.google.gson.JsonObject cond(com.google.gson.JsonObject o, String c) { o.addProperty("cond", c); return o; }
    protected static com.google.gson.JsonArray L(com.google.gson.JsonObject... xs) { var a = new com.google.gson.JsonArray(); for (var x : xs) a.add(x); return a; }
    protected static void rec(com.google.gson.JsonArray out, net.minecraft.world.level.block.Block machine, String process,
                            com.google.gson.JsonArray in, com.google.gson.JsonArray res, double watts, double seconds, String note) {
        var o = new com.google.gson.JsonObject();
        o.addProperty("machine", BuiltInRegistries.BLOCK.getKey(machine).toString());
        o.addProperty("process", process);
        o.add("inputs", in);
        o.add("outputs", res);
        o.addProperty("watts", watts);
        o.addProperty("seconds", seconds);
        if (watts > 0 && seconds > 0) o.addProperty("energy_kj", Math.round(watts * seconds / 1000.0));
        if (note != null) o.addProperty("note", note);
        out.add(o);
    }


    // =====================================================================
    // [1-45] 水銀 ＋ [5-10] 水銀真空計
    // =====================================================================

    protected static String keyOf(net.minecraft.network.chat.Component c) {
        return c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t ? t.getKey() : "";
    }

    // =====================================================================
    // [7-02] 酸水素バーナー：ネザークォーツ ＋ H2/O2 ボンベ → 溶融石英（かまどでは溶けない）
    // =====================================================================

    protected static void tickBurner(GameTestHelper h, BlockPos rel, OxyhydrogenBurnerBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) OxyhydrogenBurnerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    // =====================================================================
    // タスク10：ガイド第2巻「電気」（guide2/）・図鑑タブ（zukan/）・へえ①〜④
    // =====================================================================

    protected static final String[] GUIDE2_ADV = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};

    protected static final String[] GUIDE3_ADV = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"};

    protected static com.google.gson.JsonObject langJson(String code) {
        try (var in = SemiconductorLab.class.getResourceAsStream("/assets/" + SemiconductorLab.MOD_ID + "/lang/" + code + ".json")) {
            return com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 生成するバイオームに placed feature id が入っているか */
    /**
     * バイオームの選び方を「ワールドに頼らず」調べるための偽の文脈。canGenerateIn は本物のワールドと同じ答え
     * （オーバーワールド＝#minecraft:is_overworld、ネザー＝#is_nether、エンド＝#is_end のバイオーム）を返す。
     * GameTest のワールドは砂漠だけなので、本物の BiomeModifications の結果では「砂漠以外のオーバーワールド」を確かめられない（FAILED.md タスク13）
     */
    protected static net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext fakeContext(net.minecraft.core.RegistryAccess ra, net.minecraft.core.Holder.Reference<net.minecraft.world.level.biome.Biome> b) {
        return new net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext() {
            @Override public ResourceKey<net.minecraft.world.level.biome.Biome> getBiomeKey() { return b.key(); }
            @Override public net.minecraft.world.level.biome.Biome getBiome() { return b.value(); }
            @Override public net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getBiomeHolder() { return b; }
            @Override public net.minecraft.core.RegistryAccess getRegistryAccess() { return ra; }
            @Override public java.util.Optional<ResourceKey<net.minecraft.world.level.levelgen.feature.Feature>> getFeatureKey(net.minecraft.world.level.levelgen.feature.Feature f) { return java.util.Optional.empty(); }
            @Override public java.util.Optional<ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature>> getPlacedFeatureKey(net.minecraft.world.level.levelgen.placement.PlacedFeature f) { return java.util.Optional.empty(); }
            @Override public boolean validForStructure(ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> s) { return false; }
            @Override public java.util.Optional<ResourceKey<net.minecraft.world.level.levelgen.structure.Structure>> getStructureKey(net.minecraft.world.level.levelgen.structure.Structure s) { return java.util.Optional.empty(); }
            @Override public boolean canGenerateIn(ResourceKey<net.minecraft.world.level.dimension.LevelStem> stem) {
                if (stem == net.minecraft.world.level.dimension.LevelStem.OVERWORLD) return b.is(BiomeTags.IS_OVERWORLD);
                if (stem == net.minecraft.world.level.dimension.LevelStem.NETHER) return b.is(BiomeTags.IS_NETHER);
                if (stem == net.minecraft.world.level.dimension.LevelStem.END) return b.is(BiomeTags.IS_END);
                return false;
            }
            @Override public boolean hasTag(net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> tag) { return b.is(tag); }
        };
    }

    protected static boolean biomeHas(GameTestHelper h, ResourceKey<net.minecraft.world.level.biome.Biome> biome, String id) {
        var b = h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome).value();
        return b.getGenerationSettings().features().stream()
                .anyMatch(set -> set.stream().anyMatch(f -> f.unwrapKey().map(k -> k.identifier().equals(SemiconductorLab.id(id))).orElse(false)));
    }

    protected static void tickHcl(GameTestHelper h, BlockPos rel, com.taku3.semiconductorlab.block.HclBurnerBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) com.taku3.semiconductorlab.block.HclBurnerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    protected static long s_count(GameTestHelper h, String id, net.minecraft.world.item.Item item) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
        return h.getLevel().getServer().getRecipeManager().byKey(key).map(r -> r.value().placementInfo().ingredients().stream().filter(i -> i.test(new ItemStack(item))).count()).orElse(0L);
    }
}
