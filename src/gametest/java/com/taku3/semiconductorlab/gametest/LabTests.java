package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
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
public class LabTests {
    private static final Logger LOG = LoggerFactory.getLogger("semiconductor-lab-gametest");

    private static BlockPos p(int x, int y, int z) { return new BlockPos(x, y, z); }

    /** 消費機械の tick を n 回まわす。feedJ > 0 なら毎回その分の電気を入れてから回す（ケーブルの代わり） */
    private static void tickConsumer(GameTestHelper h, BlockPos rel, ConsumerBlockEntity be, int n, long feedJ) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) {
            if (feedJ > 0) be.getStorage().insert(feedJ, false);
            ConsumerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        }
    }

    private static void tickGenerator(GameTestHelper h, BlockPos rel, GeneratorBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) GeneratorBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    /** 帯域精製炉を n tick まわす（発振器の電力 3kW をテストが直接入れる） */
    private static void tickRefiner(GameTestHelper h, BlockPos rel, ZoneRefiningFurnaceBlockEntity be, int n) {
        tickConsumer(h, rel, be, n, EnergyStorage.wattsToJoulesPerTick(ZoneRefiningFurnaceBlockEntity.WATTS));
    }

    private static void floor(GameTestHelper h, int size) {
        for (int x = 0; x < size; x++) for (int z = 0; z < size; z++) h.setBlock(p(x, 0, z), Blocks.STONE);
    }

    // =====================================================================
    // 鉱石6種：feature を置くと「対象ブロックだけ」が鉱石に置き換わる
    // =====================================================================

    /**
     * 1〜7 の立方体を target / other の市松模様で埋め、中心に feature を置く。
     * 置く前後で -3〜11 の範囲を比べ、「変わったブロックは全部 鉱石 になり、元は replaceable だった」ことを確かめる。
     */
    private void oreFeature(GameTestHelper h, String id, Block target, Block other, Block ore, Predicate<BlockState> replaceable) {
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

    private static boolean stoneOrDeepslate(BlockState s) { return s.is(BlockTags.STONE_ORE_REPLACEABLES) || s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES); }

    @GameTest public void oreSphalerite(GameTestHelper h) { oreFeature(h, "sphalerite_ore", Blocks.STONE, Blocks.COBBLESTONE, ModBlocks.SPHALERITE_ORE, LabTests::stoneOrDeepslate); }
    @GameTest public void oreGalena(GameTestHelper h)     { oreFeature(h, "galena_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, ModBlocks.GALENA_ORE, LabTests::stoneOrDeepslate); }
    @GameTest public void oreGraphite(GameTestHelper h)   { oreFeature(h, "graphite_ore", Blocks.DEEPSLATE, Blocks.STONE, ModBlocks.GRAPHITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreMagnetite(GameTestHelper h)  { oreFeature(h, "magnetite_ore", Blocks.DEEPSLATE, Blocks.STONE, ModBlocks.MAGNETITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreWolframite(GameTestHelper h) { oreFeature(h, "wolframite_ore", Blocks.GRANITE, Blocks.STONE, ModBlocks.WOLFRAMITE_ORE, s -> s.is(Blocks.GRANITE)); }
    @GameTest public void oreIronSand(GameTestHelper h)   { oreFeature(h, "iron_sand", Blocks.SAND, Blocks.SANDSTONE, ModBlocks.IRON_SAND, s -> s.is(Blocks.SAND)); }
    @GameTest public void oreTrona(GameTestHelper h)      { oreFeature(h, "trona_deposit", Blocks.SAND, Blocks.STONE, ModBlocks.TRONA_DEPOSIT, s -> s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE)); }

    @GameTest public void oreQuartzVein(GameTestHelper h) { oreFeature(h, "quartz_vein", Blocks.STONE, Blocks.DEEPSLATE, ModBlocks.QUARTZ_VEIN, LabTests::stoneOrDeepslate); }

    /** [1-46] 石英鉱脈：石のツルハシ以上（木では落ちない）で珪石、シルクタッチでブロック。オーバーワールド全体（ほかの鉱石と同じ選び方）。MOD のレシピにネザークォーツは無い */
    @GameTest
    public void quartzVeinDrops(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        BlockState st = ModBlocks.QUARTZ_VEIN.defaultBlockState();
        ItemStack wood = new ItemStack(Items.WOODEN_PICKAXE), stone = new ItemStack(Items.STONE_PICKAXE), silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        h.assertFalse(wood.isCorrectToolForDrops(st), "quartz is Mohs 7: a wooden pickaxe is not enough");
        h.assertTrue(stone.isCorrectToolForDrops(st), "a stone pickaxe works");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, stone);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(ModItems.VEIN_QUARTZ)), "pickaxe -> vein quartz, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(ModBlocks.QUARTZ_VEIN.asItem()), "silk touch -> block, got " + silky);
        // foundInOverworld は「そのワールドのオーバーワールドに出るバイオーム」に入る。GameTest のワールドは砂漠だけなので砂漠で確かめる（平原などは本物のワールドで TEST_QUEUE）
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "quartz_vein") && biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "sphalerite_ore"), "quartz veins go wherever the other overworld ores go");
        List<String> uses = new ArrayList<>();
        for (var r : lv.getServer().getRecipeManager().getRecipes())
            if (r.id().identifier().getNamespace().equals(SemiconductorLab.MOD_ID) && r.value().placementInfo().ingredients().stream().anyMatch(i -> i.test(new ItemStack(Items.QUARTZ))))
                uses.add(r.id().identifier().toString());
        h.assertTrue(uses.isEmpty(), "no MOD recipe may use nether quartz: " + uses);
        LOG.info("[LAB] quartz vein: stone pickaxe -> vein quartz, silk -> block, overworld-wide; no MOD recipe uses nether quartz");
        h.succeed();
    }

    @GameTest public void oreBorax(GameTestHelper h)      { oreFeature(h, "borax_deposit", Blocks.SAND, Blocks.STONE, ModBlocks.BORAX_DEPOSIT, s -> s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE)); }

    /** [1-33] ホウ素：堆積→ホウ砂（木のツルハシ、シルクでブロック）→＋硫酸→ホウ酸2→かまど→B2O3。砂漠だけ */
    @GameTest
    public void boronChain(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = ModBlocks.BORAX_DEPOSIT.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "borax is soft (Mohs 2-2.5): a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(ModItems.BORAX)), "pickaxe -> borax, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(ModBlocks.BORAX_DEPOSIT.asItem()), "silk touch -> block, got " + silky);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("boric_acid"));
        var r = lv.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.size(), 2, "borax + sulfuric acid");
        h.assertTrue(recipeUses(h, "boric_acid", ModItems.BORAX) && recipeUses(h, "boric_acid", ModItems.SULFURIC_ACID), "Na2B4O7 + H2SO4");
        ItemStack out = ((net.minecraft.world.item.crafting.CraftingRecipe) r).assemble(net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(ModItems.BORAX), new ItemStack(ModItems.SULFURIC_ACID))));
        h.assertTrue(out.is(ModItems.BORIC_ACID) && out.getCount() == 2, "4 mol H3BO3 = 2 items (2 mol each), got " + out);
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.BORIC_ACID).is(ModItems.BORON_TRIOXIDE), "2 H3BO3 -> B2O3 + 3 H2O in a furnace");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "borax_deposit"), "borax in deserts");
        h.assertFalse(biomeHas(h, net.minecraft.world.level.biome.Biomes.PLAINS, "borax_deposit"), "no borax in plains");
        LOG.info("[LAB] boron: deposit -> borax -> + H2SO4 -> 2 boric acid -> furnace -> B2O3, desert only");
        h.succeed();
    }

    /** [2-03] トロナ：堆積はツルハシでトロナを落とす（シルクタッチでブロック）、素手では落ちない。トロナ→かまど→炭酸ナトリウム */
    @GameTest
    public void tronaSoda(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = ModBlocks.TRONA_DEPOSIT.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "trona is soft: a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(ModItems.TRONA)), "pickaxe -> trona, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(ModBlocks.TRONA_DEPOSIT.asItem()), "silk touch -> block, got " + silky);
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.TRONA).is(ModItems.SODIUM_CARBONATE), "trona -> furnace -> sodium carbonate");
        // 砂漠だけに生成（placed feature があり、砂漠に追加されている）
        var placed = lv.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        h.assertTrue(placed.getValue(SemiconductorLab.id("trona_deposit")) != null, "placed feature trona_deposit registered");
        var desert = lv.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.DESERT).value();
        var plains = lv.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS).value();
        java.util.function.Predicate<net.minecraft.world.level.biome.Biome> has = b -> b.getGenerationSettings().features().stream()
                .anyMatch(set -> set.stream().anyMatch(f -> f.unwrapKey().map(k -> k.identifier().equals(SemiconductorLab.id("trona_deposit"))).orElse(false)));
        h.assertTrue(has.test(desert), "trona generates in the desert");
        h.assertFalse(has.test(plains), "trona does not generate in plains");
        LOG.info("[LAB] trona: pickaxe -> trona, silk -> block, furnace -> Na2CO3, desert only");
        h.succeed();
    }

    /**
     * 鉱石のドロップ：ふつうのツルハシ（シャベル）では原石、シルクタッチでは鉱石ブロックそのもの。
     * 26.3 のルートテーブル書式（condition / modifier）で書けているかの確認。機械はツールなしで壊しても本体が落ちる。
     */
    @GameTest
    public void blockDrops(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        Object[][] ores = {
                {ModBlocks.SPHALERITE_ORE, ModItems.SPHALERITE}, {ModBlocks.GALENA_ORE, ModItems.GALENA},
                {ModBlocks.GRAPHITE_ORE, ModItems.GRAPHITE}, {ModBlocks.MAGNETITE_ORE, ModItems.MAGNETITE},
                {ModBlocks.WOLFRAMITE_ORE, ModItems.WOLFRAMITE}, {ModBlocks.IRON_SAND, ModItems.MAGNETITE}};
        for (Object[] o : ores) {
            Block b = (Block) o[0];
            net.minecraft.world.item.Item raw = (net.minecraft.world.item.Item) o[1];
            List<ItemStack> normal = Block.getDrops(b.defaultBlockState(), lv, at, null, null, pick);
            List<ItemStack> silky = Block.getDrops(b.defaultBlockState(), lv, at, null, null, silk);
            h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(raw)), b + " with a pickaxe should drop " + raw + ", got " + normal);
            h.assertTrue(silky.size() == 1 && silky.get(0).is(b.asItem()), b + " with silk touch should drop itself, got " + silky);
        }
        for (Block m : new Block[] {ModBlocks.STEAM_ENGINE, ModBlocks.STEAM_TURBINE, ModBlocks.LEAD_ACID_BATTERY, ModBlocks.WATER_WHEEL, ModBlocks.ELECTROLYZER, ModBlocks.GAS_HOLDER,
                ModBlocks.BESSEMER_CONVERTER, ModBlocks.COPPER_CABLE, ModBlocks.ZONE_REFINING_FURNACE, ModBlocks.HYDROGEN_FURNACE, ModBlocks.COMPRESSOR,
                ModBlocks.VACUUM_CHAMBER, ModBlocks.HAND_VACUUM_PUMP, ModBlocks.SEALING_STATION, ModBlocks.ELECTRIC_FURNACE, ModBlocks.ACHESON_FURNACE, ModBlocks.REVERBERATORY_FURNACE, ModBlocks.OXYHYDROGEN_BURNER}) {
            List<ItemStack> d = Block.getDrops(m.defaultBlockState(), lv, at, null, null, pick);
            h.assertTrue(d.size() == 1 && d.get(0).is(m.asItem()), m + " should drop itself, got " + d);
        }
        LOG.info("[LAB] block drops: 6 ores drop raw items (silk touch -> ore block), machines drop themselves");
        h.succeed();
    }

    /** 道具：鉱石4種は鉄ツルハシ必須、黒鉛は石ツルハシ以上、砂鉄はシャベル、水車は斧、機械はツルハシ */
    @GameTest
    public void toolRequirements(GameTestHelper h) {
        ItemStack wood = new ItemStack(Items.WOODEN_PICKAXE), stone = new ItemStack(Items.STONE_PICKAXE), iron = new ItemStack(Items.IRON_PICKAXE);
        for (Block b : new Block[] {ModBlocks.SPHALERITE_ORE, ModBlocks.MAGNETITE_ORE, ModBlocks.WOLFRAMITE_ORE, ModBlocks.GALENA_ORE}) {
            h.assertFalse(stone.isCorrectToolForDrops(b.defaultBlockState()), b + " must need an iron pickaxe (stone is not enough)");
            h.assertTrue(iron.isCorrectToolForDrops(b.defaultBlockState()), b + " should drop with an iron pickaxe");
        }
        h.assertFalse(wood.isCorrectToolForDrops(ModBlocks.GRAPHITE_ORE.defaultBlockState()), "graphite ore needs a stone pickaxe");
        h.assertTrue(stone.isCorrectToolForDrops(ModBlocks.GRAPHITE_ORE.defaultBlockState()), "graphite ore with a stone pickaxe");
        h.assertTrue(new ItemStack(Items.IRON_SHOVEL).getDestroySpeed(ModBlocks.IRON_SAND.defaultBlockState()) > 1f, "iron sand: shovel is the right tool");
        h.assertTrue(new ItemStack(Items.IRON_AXE).getDestroySpeed(ModBlocks.WATER_WHEEL.defaultBlockState()) > 1f, "water wheel: axe is the right tool");
        for (Block m : new Block[] {ModBlocks.STEAM_ENGINE, ModBlocks.STEAM_TURBINE, ModBlocks.LEAD_ACID_BATTERY, ModBlocks.ELECTROLYZER, ModBlocks.GAS_HOLDER, ModBlocks.BESSEMER_CONVERTER, ModBlocks.ZONE_REFINING_FURNACE, ModBlocks.REVERBERATORY_FURNACE}) {
            h.assertTrue(iron.isCorrectToolForDrops(m.defaultBlockState()), m + " should be collected with a pickaxe");
            h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(m.defaultBlockState()), m + " should not drop when broken by hand");
        }
        LOG.info("[LAB] tools: 4 ores need iron pickaxe, graphite stone+, iron sand shovel, water wheel axe, machines pickaxe");
        h.succeed();
    }

    // =====================================================================
    // 発電
    // =====================================================================

    /** 水車：隣に流れている水があれば発電（>0）。実際に水を流して確かめる */
    @GameTest(maxTicks = 100)
    public void waterWheelFlowing(GameTestHelper h) {
        floor(h, 8);
        h.setBlock(p(3, 1, 3), ModBlocks.WATER_WHEEL);
        h.setBlock(p(1, 1, 3), Blocks.WATER);   // 水源。ここから (2,1,3) へ流れが出る
        h.succeedWhen(() -> {
            WaterWheelBlockEntity be = h.getBlockEntity(p(3, 1, 3), WaterWheelBlockEntity.class);
            h.assertTrue(be.getLastWatts() > 0, "water wheel next to flowing water should produce power, got " + be.getLastWatts());
            LOG.info("[LAB] water wheel with flowing water: {} W", be.getLastWatts());
        });
    }

    /** 水車：四方が水源（止まった水）だけなら 0 */
    @GameTest(maxTicks = 60)
    public void waterWheelStillWater(GameTestHelper h) {
        floor(h, 8);
        h.setBlock(p(3, 1, 3), ModBlocks.WATER_WHEEL);
        h.setBlock(p(2, 1, 3), Blocks.WATER);
        h.setBlock(p(4, 1, 3), Blocks.WATER);
        h.setBlock(p(3, 1, 2), Blocks.WATER);
        h.setBlock(p(3, 1, 4), Blocks.WATER);
        h.runAfterDelay(40, () -> {
            WaterWheelBlockEntity be = h.getBlockEntity(p(3, 1, 3), WaterWheelBlockEntity.class);
            h.assertValueEqual(be.getLastWatts(), 0.0, "water wheel surrounded by still water (sources) must give 0 W");
            h.succeed();
        });
    }

    /** 蒸気機関：石炭+水で 50kW、欠けると 0、水バケツは空バケツになって戻る。1杯＝石炭2個分 */
    @GameTest
    public void steamEngine(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.STEAM_ENGINE);
        SteamEngineBlockEntity be = h.getBlockEntity(pos, SteamEngineBlockEntity.class);
        var c = be.getContainer();

        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(be.getLastWatts(), 0.0, "empty steam engine power");

        c.setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 3));
        tickGenerator(h, pos, be, 5);
        h.assertValueEqual(be.getLastWatts(), 0.0, "coal without water power");
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 3, "coal must not burn without water");

        c.setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(be.getLastWatts(), SteamEngineBlockEntity.WATTS, "coal + water power");
        h.assertTrue(c.getItem(SteamEngineBlockEntity.SLOT_WATER).is(Items.BUCKET), "water bucket should turn into an empty bucket");
        h.assertValueEqual(be.getWaterUnits(), 1, "one bucket = 2 coal; 1 left after the first coal");

        tickGenerator(h, pos, be, 1199);   // 石炭2個分（30秒×2）が燃え尽きる
        h.assertValueEqual(be.getLastWatts(), SteamEngineBlockEntity.WATTS, "still burning the 2nd coal at tick 1200");
        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(be.getLastWatts(), 0.0, "water used up -> 0 W");
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 1, "3 coal - 2 burned");
        h.assertTrue(be.getStorage().getStoredJ() > 0, "energy should be stored");
        LOG.info("[LAB] steam engine: 50 kW with coal+water, 0 without, bucket returned, stored {} J", be.getStorage().getStoredJ());
        h.succeed();
    }

    /** [4-05] 自励式ダイナモ：蒸気機関に積むと 100kW、ただし石炭は15秒で燃え尽きる（石炭1個の電気は同じ 1.5MJ）。2個目は付かない */
    @GameTest
    public void selfExcitedDynamo(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.STEAM_ENGINE);
        SteamEngineBlockEntity be = h.getBlockEntity(pos, SteamEngineBlockEntity.class);
        h.assertTrue(be.supportsDynamoUpgrade() && !be.isSelfExcited(), "a new steam engine has a permanent-magnet dynamo");
        h.assertTrue(be.installSelfExcitedDynamo(), "install the self-excited dynamo");
        h.assertFalse(be.installSelfExcitedDynamo(), "cannot install twice");
        var c = be.getContainer();
        c.setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 2));
        c.setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(be.getLastWatts(), SteamEngineBlockEntity.WATTS * 2, "self-excited: output x2");
        tickGenerator(h, pos, be, SteamEngineBlockEntity.COAL_TICKS / 2 - 1);
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 1, "still on the 1st coal at 15 s");
        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 0, "2nd coal lit after 15 s (coal burns twice as fast)");
        // 石炭1個の電気 = 出力 × 燃焼時間 は永久磁石と同じ（エネルギー保存）
        double perCoalMagneto = SteamEngineBlockEntity.WATTS * SteamEngineBlockEntity.COAL_TICKS / 20.0;
        double perCoalSelf = be.ratedWatts() * (SteamEngineBlockEntity.COAL_TICKS / 2) / 20.0;
        h.assertValueEqual(perCoalSelf, perCoalMagneto, "same electricity per coal");
        h.assertTrue(Math.abs(be.thermalEfficiency() - 0.05) < 1e-9, "steam engine thermal efficiency 5%");
        // 水車も積み替えられる
        h.setBlock(p(3, 1, 1), ModBlocks.WATER_WHEEL);
        WaterWheelBlockEntity ww = h.getBlockEntity(p(3, 1, 1), WaterWheelBlockEntity.class);
        h.assertTrue(ww.installSelfExcitedDynamo() && ww.dynamoFactor() == 2, "water wheel accepts the self-excited dynamo");
        h.assertTrue(recipeUses(h, "self_excited_dynamo", ModItems.STEEL_INGOT) && !recipeUses(h, "self_excited_dynamo", ModItems.MAGNET), "steel core, no magnet");
        LOG.info("[LAB] self-excited dynamo: steam engine 100 kW, coal 15 s (1.5 MJ/coal unchanged), water wheel x2, no magnet in recipe");
        h.succeed();
    }

    /** [4-06] 蒸気タービン：500kW・石炭18秒（熱効率30%）。水はバケツ（1杯＝石炭2個）か、横の水源から取り続ける。積み替えはない */
    @GameTest
    public void steamTurbine(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.STEAM_TURBINE);
        SteamTurbineBlockEntity be = h.getBlockEntity(pos, SteamTurbineBlockEntity.class);
        var c = be.getContainer();
        c.setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 5));
        tickGenerator(h, pos, be, 3);
        h.assertValueEqual(be.getLastWatts(), 0.0, "coal without water -> 0 W");
        c.setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickGenerator(h, pos, be, 1);
        h.assertValueEqual(be.getLastWatts(), SteamTurbineBlockEntity.WATTS, "turbine 500 kW");
        tickGenerator(h, pos, be, SteamTurbineBlockEntity.COAL_TICKS * 2);
        h.assertValueEqual(be.getLastWatts(), 0.0, "1 bucket = 2 coal, then stops");
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 3, "2 coal burned");
        h.assertTrue(Math.abs(be.thermalEfficiency() - 0.30) < 1e-9, "turbine thermal efficiency 30%, got " + be.thermalEfficiency());
        // 横に水源：バケツなしで燃え続ける
        h.setBlock(p(1, 1, 2), Blocks.WATER);
        tickGenerator(h, pos, be, SteamTurbineBlockEntity.COAL_TICKS * 2 + 1);
        h.assertValueEqual(be.getLastWatts(), SteamTurbineBlockEntity.WATTS, "running on river water");
        h.assertValueEqual(c.getItem(SteamEngineBlockEntity.SLOT_COAL).getCount(), 0, "3 more coal lit with no bucket");
        h.assertFalse(be.supportsDynamoUpgrade(), "turbine generator is already self-excited");
        h.assertTrue(recipeUses(h, "steam_turbine", ModItems.STEEL_INGOT), "turbine is built of steel");
        LOG.info("[LAB] steam turbine: 500 kW, 18 s/coal (30%), bucket = 2 coal, adjacent water source = unlimited feed");
        h.succeed();
    }

    /** [4-06] のテスト「アチソン炉が動く」：タービン → 銅ケーブル1本 → アチソン炉 で 2500℃に届き、人造黒鉛ができる */
    @GameTest
    public void turbinePowersAcheson(GameTestHelper h) {
        BlockPos tp = p(1, 1, 1), cp = p(2, 1, 1), fp = p(3, 1, 1);
        h.setBlock(tp, ModBlocks.STEAM_TURBINE);
        h.setBlock(cp, ModBlocks.COPPER_CABLE);
        h.setBlock(fp, ModBlocks.ACHESON_FURNACE);
        h.setBlock(p(1, 1, 0), Blocks.WATER);
        SteamTurbineBlockEntity t = h.getBlockEntity(tp, SteamTurbineBlockEntity.class);
        CableBlockEntity cable = h.getBlockEntity(cp, CableBlockEntity.class);
        AchesonFurnaceBlockEntity f = h.getBlockEntity(fp, AchesonFurnaceBlockEntity.class);
        t.getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 32));
        f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_CARBON, new ItemStack(Items.COAL, 4));
        f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_SAND, new ItemStack(Items.SAND, 1));
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(tp), ac = h.absolutePos(cp), af = h.absolutePos(fp);
        int ticks = 0, reached = -1;
        double maxW = 0;
        while (ticks < 20 * 200 && f.getContainer().getItem(AchesonFurnaceBlockEntity.SLOT_OUT).isEmpty()) {
            GeneratorBlockEntity.tick(lv, at, lv.getBlockState(at), t);
            CableBlockEntity.tick(lv, ac, lv.getBlockState(ac), cable);
            ConsumerBlockEntity.tick(lv, af, lv.getBlockState(af), f);
            maxW = Math.max(maxW, f.getLastWatts());
            if (reached < 0 && f.getTemperature() >= AchesonFurnaceBlockEntity.GRAPHITIZE_C) reached = ticks;
            ticks++;
        }
        h.assertTrue(reached >= 0, "turbine via 1 cable should heat the Acheson furnace past 2500 C (max power seen " + maxW + " W, temp " + f.getTemperature() + ")");
        h.assertTrue(f.getContainer().getItem(AchesonFurnaceBlockEntity.SLOT_OUT).is(ModItems.SYNTHETIC_GRAPHITE), "synthetic graphite made on turbine power");
        LOG.info("[LAB] turbine -> cable -> acheson: {} W delivered, 2500 C at tick {}, graphite at tick {}, coal left {}", Math.round(maxW), reached, ticks,
                t.getContainer().getItem(SteamEngineBlockEntity.SLOT_COAL).getCount());
        h.succeed();
    }

    /** [4-07] 鉛蓄電池（タスク06 で全面充放電）：どの面からも充電（85% が貯まる、10kW まで）、1MJ で満杯、隣の機械へ直接放電（10kW まで） */
    @GameTest
    public void leadAcidBattery(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.LEAD_ACID_BATTERY);
        LeadAcidBatteryBlockEntity be = h.getBlockEntity(pos, LeadAcidBatteryBlockEntity.class);
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(pos);
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
            EnergyStorage s = EnergyApi.ENERGY.find(lv, a, d);
            h.assertTrue(s != null && s.canInsert() && s.canExtract(), "every face charges and discharges: " + d);
        }
        EnergyStorage side = EnergyApi.ENERGY.find(lv, a, net.minecraft.core.Direction.EAST);
        long per = LeadAcidBatteryBlockEntity.MAX_PER_TICK;

        // 充電：毎 tick たっぷり入れても 10kW（500 J/tick）まで、貯まるのはその 85%
        for (int i = 0; i < 100; i++) { side.insert(1_000_000, false); LeadAcidBatteryBlockEntity.tick(lv, a, lv.getBlockState(a), be); }
        h.assertValueEqual(be.getStoredJ(), 100 * per * 85 / 100, "100 ticks x 500 J x 85%");
        h.assertValueEqual(be.getLastChargeWatts(), LeadAcidBatteryBlockEntity.MAX_WATTS, "charging at 10 kW");
        // 満杯まで：1MJ を超えない
        int t = 0;
        while (side.insert(1_000_000, false) > 0 && t++ < 5000) LeadAcidBatteryBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        h.assertTrue(be.getStoredJ() <= LeadAcidBatteryBlockEntity.CAPACITY && be.getStoredJ() > LeadAcidBatteryBlockEntity.CAPACITY - per, "full at 1 MJ, got " + be.getStoredJ());
        LeadAcidBatteryBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        long full = be.getStoredJ();

        // 放電：ケーブルなしで隣（正面でなくてよい）の電気炉へ 10kW ずつ押し出す
        h.setBlock(p(2, 1, 1), ModBlocks.ELECTRIC_FURNACE);
        ElectricFurnaceBlockEntity furnace = h.getBlockEntity(p(2, 1, 1), ElectricFurnaceBlockEntity.class);
        for (int i = 0; i < 20; i++) LeadAcidBatteryBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        h.assertValueEqual(full - be.getStoredJ(), 20 * per, "discharged 20 ticks x 500 J");
        h.assertValueEqual(furnace.getStorage().getStoredJ(), 20 * (per - per / 100), "furnace received it (1% contact loss)");
        h.assertValueEqual(be.getLastDischargeWatts(), LeadAcidBatteryBlockEntity.MAX_WATTS, "discharging at 10 kW");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        h.assertTrue(recipeUses(h, "lead_acid_battery", ModItems.LEAD_INGOT) && recipeUses(h, "lead_acid_battery", ModItems.SULFURIC_ACID), "lead + sulfuric acid");
        LOG.info("[LAB] lead-acid battery: charge 10 kW at 85% from any face, full at {} J, discharge 10 kW to an adjacent machine", full);
        h.succeed();
    }

    /** タスク06：壊すと残量がアイテム（データコンポーネント battery_energy）に残り、置き直すと戻る */
    @GameTest
    public void batteryKeepsCharge(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        h.setBlock(p(1, 1, 1), ModBlocks.LEAD_ACID_BATTERY);
        LeadAcidBatteryBlockEntity be = h.getBlockEntity(p(1, 1, 1), LeadAcidBatteryBlockEntity.class);
        be.setStoredForTest(400_000);
        BlockPos a = h.absolutePos(p(1, 1, 1));
        List<ItemStack> drops = Block.getDrops(lv.getBlockState(a), lv, a, be, null, new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(drops.size() == 1 && drops.get(0).is(ModBlocks.LEAD_ACID_BATTERY.asItem()), "battery drops itself, got " + drops);
        h.assertValueEqual(drops.get(0).get(ModDataComponents.BATTERY_ENERGY), 400_000L, "charge stored on the item");
        h.setBlock(p(3, 1, 1), ModBlocks.LEAD_ACID_BATTERY);
        LeadAcidBatteryBlockEntity placed = h.getBlockEntity(p(3, 1, 1), LeadAcidBatteryBlockEntity.class);
        placed.applyComponentsFromItemStack(drops.get(0));
        h.assertValueEqual(placed.getStoredJ(), 400_000L, "charge restored when placed again");
        // 空の電池はコンポーネントなし（ふつうの電池とスタックできる）
        h.setBlock(p(5, 1, 1), ModBlocks.LEAD_ACID_BATTERY);
        BlockPos e = h.absolutePos(p(5, 1, 1));
        List<ItemStack> empty = Block.getDrops(lv.getBlockState(e), lv, e, lv.getBlockEntity(e), null, new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(empty.get(0).get(ModDataComponents.BATTERY_ENERGY) == null, "empty battery has no charge component");
        LOG.info("[LAB] battery keeps charge: 400 kJ -> item component -> placed again 400 kJ");
        h.succeed();
    }

    // =====================================================================
    // ケーブル（タスク06：ネットワーク方式）
    // =====================================================================

    /** 並べた順に BE の tick を n 回まわす（発電機 → ケーブル → 機械 → 電池 の順に並べる） */
    private static void tickAll(GameTestHelper h, int n, BlockPos... rels) {
        ServerLevel lv = h.getLevel();
        for (int i = 0; i < n; i++) for (BlockPos r : rels) {
            BlockPos a = h.absolutePos(r);
            BlockState s = lv.getBlockState(a);
            switch (lv.getBlockEntity(a)) {
                case GeneratorBlockEntity g -> GeneratorBlockEntity.tick(lv, a, s, g);
                case CableBlockEntity c -> CableBlockEntity.tick(lv, a, s, c);
                case ConsumerBlockEntity c -> ConsumerBlockEntity.tick(lv, a, s, c);
                case LeadAcidBatteryBlockEntity b -> LeadAcidBatteryBlockEntity.tick(lv, a, s, b);
                case null, default -> {}
            }
        }
    }

    private static BlockPos[] line(int x0, int x1, int y, int z) {
        BlockPos[] r = new BlockPos[x1 - x0 + 1];
        for (int x = x0; x <= x1; x++) r[x - x0] = p(x, y, z);
        return r;
    }

    private static BlockPos[] concat(BlockPos[]... parts) {
        List<BlockPos> l = new ArrayList<>();
        for (BlockPos[] b : parts) l.addAll(List.of(b));
        return l.toArray(new BlockPos[0]);
    }

    /** 蒸気機関→ケーブル3本→電気炉：本当に tick を進めて、電気炉に電気が届く */
    @GameTest(maxTicks = 60)
    public void cableChainDelivers(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), ModBlocks.STEAM_ENGINE);
        for (int x = 2; x <= 4; x++) h.setBlock(p(x, 1, 1), ModBlocks.COPPER_CABLE);
        h.setBlock(p(5, 1, 1), ModBlocks.ELECTRIC_FURNACE);
        SteamEngineBlockEntity eng = h.getBlockEntity(p(1, 1, 1), SteamEngineBlockEntity.class);
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL));
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        h.succeedWhen(() -> {
            long j = h.getBlockEntity(p(5, 1, 1), ElectricFurnaceBlockEntity.class).getStorage().getStoredJ();
            h.assertTrue(j > 0, "electric furnace 3 cables away should receive power");
            LOG.info("[LAB] cable x3 network: furnace has {} J", j);
        });
    }

    /** 損失は最長経路のケーブル1本につき 1%：電池（10kW＝500 J/tick）→ ケーブル1本 → 495 J、5本 → 475 J。ケーブルは貯めない */
    @GameTest
    public void cableLossPerHop(GameTestHelper h) {
        int[] lengths = {1, 5};
        for (int k = 0; k < lengths.length; k++) {
            int n = lengths[k], z = 1 + 2 * k;
            h.setBlock(p(1, 1, z), ModBlocks.LEAD_ACID_BATTERY);
            h.getBlockEntity(p(1, 1, z), LeadAcidBatteryBlockEntity.class).setStoredForTest(100_000);
            for (int x = 2; x <= 1 + n; x++) h.setBlock(p(x, 1, z), ModBlocks.COPPER_CABLE);
            h.setBlock(p(2 + n, 1, z), ModBlocks.ELECTRIC_FURNACE);
            tickAll(h, 1, concat(line(2, 1 + n, 1, z), new BlockPos[] {p(1, 1, z)}));
            long got = h.getBlockEntity(p(2 + n, 1, z), ElectricFurnaceBlockEntity.class).getStorage().getStoredJ();
            h.assertValueEqual(got, 500L * (100 - n) / 100, n + " cables: 500 J minus " + n + "%");
            CableNetwork net = CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, z)));
            h.assertValueEqual(net.longestPath, n, "longest path");
            LOG.info("[LAB] cable loss: {} cables -> {} J of 500 J", n, got);
        }
        h.succeed();
    }

    /** タスク06：ケーブル10本先のアチソン炉に 500kW が届く（タービン2台＝1MW、損失10%で 900kW まで）。1台だと損失で 450kW しか届かない */
    @GameTest
    public void cableTenHops500kW(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), ModBlocks.STEAM_TURBINE);
        h.setBlock(p(1, 1, 0), Blocks.WATER);
        h.setBlock(p(2, 1, 2), ModBlocks.STEAM_TURBINE);
        h.setBlock(p(2, 1, 3), Blocks.WATER);
        for (int x = 2; x <= 11; x++) h.setBlock(p(x, 1, 1), ModBlocks.COPPER_CABLE);
        h.setBlock(p(12, 1, 1), ModBlocks.ACHESON_FURNACE);
        for (BlockPos t : new BlockPos[] {p(1, 1, 1), p(2, 1, 2)})
            h.getBlockEntity(t, SteamTurbineBlockEntity.class).getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 64));
        AchesonFurnaceBlockEntity f = h.getBlockEntity(p(12, 1, 1), AchesonFurnaceBlockEntity.class);
        f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_CARBON, new ItemStack(Items.COAL, 4));
        f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_SAND, new ItemStack(Items.SAND, 1));
        BlockPos[] order = concat(new BlockPos[] {p(1, 1, 1), p(2, 1, 2)}, line(2, 11, 1, 1), new BlockPos[] {p(12, 1, 1)});
        tickAll(h, 200, order);
        CableNetwork net = CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 1)));
        h.assertValueEqual(net.longestPath, 10, "10 cables in a row");
        h.assertValueEqual(net.lossPercent(), 10, "10% loss");
        h.assertValueEqual(f.getLastWatts(), AchesonFurnaceBlockEntity.REQUIRED_WATTS, "500 kW reaches the furnace 10 cables away");
        // タービンを1台にすると 500kW × 0.9 = 450kW しか届かない
        h.setBlock(p(2, 1, 2), Blocks.AIR);
        tickAll(h, 1000, order);   // タービンの内部バッファ（1MJ）と炉のバッファが尽きるまで待つ
        double one = f.getLastWatts();
        h.assertTrue(Math.abs(one - 450_000) < 1_000, "one turbine through 10 cables -> about 450 kW, got " + one);
        LOG.info("[LAB] 10 cables: 2 turbines -> {} W at the furnace, 1 turbine -> {} W (10% loss)", AchesonFurnaceBlockEntity.REQUIRED_WATTS, one);
        h.succeed();
    }

    /** タスク06：通過上限 1MW を超える要求は按分。タービン3台（1.5MW）→ ケーブル3本 → アチソン炉3台：取り出しは 1MW で頭打ち、3台に等分 */
    @GameTest
    public void cableShareOverLimit(GameTestHelper h) {
        List<BlockPos> order = new ArrayList<>();
        for (int x = 2; x <= 4; x++) {
            h.setBlock(p(x, 1, 0), Blocks.WATER);
            h.setBlock(p(x, 1, 1), ModBlocks.STEAM_TURBINE);
            h.getBlockEntity(p(x, 1, 1), SteamTurbineBlockEntity.class).getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 64));
            h.setBlock(p(x, 1, 2), ModBlocks.COPPER_CABLE);
            h.setBlock(p(x, 1, 3), ModBlocks.ACHESON_FURNACE);
            AchesonFurnaceBlockEntity f = h.getBlockEntity(p(x, 1, 3), AchesonFurnaceBlockEntity.class);
            f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_CARBON, new ItemStack(Items.COAL, 4));
            f.getContainer().setItem(AchesonFurnaceBlockEntity.SLOT_SAND, new ItemStack(Items.SAND, 1));
        }
        for (int x = 2; x <= 4; x++) order.add(p(x, 1, 1));
        for (int x = 2; x <= 4; x++) order.add(p(x, 1, 2));
        for (int x = 2; x <= 4; x++) order.add(p(x, 1, 3));
        tickAll(h, 100, order.toArray(new BlockPos[0]));
        CableNetwork net = CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 2)));
        h.assertTrue(net.lastTakenJ <= CableNetwork.MAX_TRANSFER && net.lastTakenJ >= CableNetwork.MAX_TRANSFER - 10, "taken is capped at 1 MW (50 kJ/tick), got " + net.lastTakenJ);
        double expectEach = CableNetwork.MAX_TRANSFER * 0.97 * 20 / 3;   // 3本＝3% 損失、3等分
        double sum = 0;
        for (int x = 2; x <= 4; x++) {
            double w = h.getBlockEntity(p(x, 1, 3), AchesonFurnaceBlockEntity.class).getLastWatts();
            sum += w;
            h.assertTrue(Math.abs(w - expectEach) < expectEach * 0.01, "furnace " + x + " gets an equal share ~" + Math.round(expectEach) + " W, got " + w);
        }
        LOG.info("[LAB] cable limit: 1.5 MW supply, 1 MW taken, {} W delivered to 3 furnaces (shared equally)", Math.round(sum));
        h.succeed();
    }

    /** タスク06：電池はネットワークで「余れば充電・足りなければ放電」。電池どうしでは流れない */
    @GameTest
    public void batteryOnNetwork(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), ModBlocks.STEAM_ENGINE);
        h.setBlock(p(2, 1, 1), ModBlocks.COPPER_CABLE);
        h.setBlock(p(3, 1, 1), ModBlocks.COPPER_CABLE);
        h.setBlock(p(3, 1, 2), ModBlocks.LEAD_ACID_BATTERY);
        SteamEngineBlockEntity eng = h.getBlockEntity(p(1, 1, 1), SteamEngineBlockEntity.class);
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 4));
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        LeadAcidBatteryBlockEntity bat = h.getBlockEntity(p(3, 1, 2), LeadAcidBatteryBlockEntity.class);
        BlockPos[] order = {p(1, 1, 1), p(2, 1, 1), p(3, 1, 1), p(3, 1, 0), p(3, 1, 2), p(2, 1, 2)};
        // ① 発電機だけ → 電池が 10kW で充電
        tickAll(h, 100, order);
        h.assertValueEqual(bat.getLastChargeWatts(), LeadAcidBatteryBlockEntity.MAX_WATTS, "battery charges at 10 kW from the network");
        h.assertTrue(bat.getStoredJ() > 40_000, "battery charged, got " + bat.getStoredJ());
        // ② 電気炉もつなぐ（50kW の発電 > 炉の要求）→ 電池は放電しない
        h.setBlock(p(3, 1, 0), ModBlocks.ELECTRIC_FURNACE);
        tickAll(h, 5, order);
        h.assertValueEqual(bat.getLastDischargeWatts(), 0.0, "spare power: battery does not discharge");
        // ③ 発電機を外す → 電池から炉へ放電（最長経路2本＝2% 損失）
        h.setBlock(p(1, 1, 1), Blocks.AIR);
        ElectricFurnaceBlockEntity furnace = h.getBlockEntity(p(3, 1, 0), ElectricFurnaceBlockEntity.class);
        long before = furnace.getStorage().getStoredJ(), batBefore = bat.getStoredJ();
        tickAll(h, 1, order);
        h.assertValueEqual(furnace.getStorage().getStoredJ() - before, 490L, "battery -> 2 cables -> furnace: 500 J - 2%");
        h.assertValueEqual(batBefore - bat.getStoredJ(), 500L, "battery gave 500 J");
        h.assertValueEqual(bat.getLastDischargeWatts(), LeadAcidBatteryBlockEntity.MAX_WATTS, "discharging at 10 kW");
        // ④ 炉を外して空の電池をつなぐ → 電池どうしでは流れない
        h.setBlock(p(3, 1, 0), Blocks.AIR);
        h.setBlock(p(2, 1, 2), ModBlocks.LEAD_ACID_BATTERY);
        LeadAcidBatteryBlockEntity other = h.getBlockEntity(p(2, 1, 2), LeadAcidBatteryBlockEntity.class);
        tickAll(h, 20, order);
        h.assertValueEqual(other.getStoredJ(), 0L, "no battery-to-battery flow");
        LOG.info("[LAB] battery on network: charges from spare power, discharges when the generator is gone (490 J/tick over 2 cables), no battery-to-battery flow");
        h.succeed();
    }

    // =====================================================================
    // 電解槽 [6-01]
    // =====================================================================

    @GameTest
    public void electrolyzer(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ELECTROLYZER);
        ElectrolyzerBlockEntity be = h.getBlockEntity(pos, ElectrolyzerBlockEntity.class);
        var c = be.getContainer();
        c.setItem(ElectrolyzerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, new ItemStack(ModItems.SULFURIC_ACID, 3));

        // 電気なし：止まる
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 100, 0);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "no power -> no hydrogen");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.WATER_BUCKET), "no power -> water stays");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);

        // 電気あり。1tick にちょうど使う分（5kW＝250 J/tick）だけ入れる＝バッファに余りを貯めない
        long feed = 250;
        tickConsumer(h, pos, be, 1, feed);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickConsumer(h, pos, be, 999, feed);
        // 途中で電気を切る → 止まる（進捗は保たれる）
        tickConsumer(h, pos, be, 500, 0);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "paused: nothing made");
        // 再接続 → 続きから（電気のあった tick の合計が 2300 で完成）
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS - 1001, feed);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "not finished 1 powered tick before 115 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 2, "H2 count");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).is(ModItems.HYDROGEN_GAS), "H2 item");
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_O2).getCount(), 1, "O2 count");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_O2).is(ModItems.OXYGEN_GAS), "O2 item");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.BUCKET), "water bucket -> empty bucket");
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_ACID).getCount(), 3, "sulfuric acid is not consumed");

        // 硫酸を抜くと止まる（純水はほぼ電気を通さない）
        c.setItem(ElectrolyzerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, ItemStack.EMPTY);
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 2, "without acid nothing more is made");

        // 出口が満杯（水素 63 個＋2 は入らない）→ 止まる
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, new ItemStack(ModItems.SULFURIC_ACID, 1));
        c.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, 63));
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 63, "outlet full -> stops");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.WATER_BUCKET), "outlet full -> water not used");
        LOG.info("[LAB] electrolyzer: H2 x2 + O2 x1 after {} ticks, acid kept, stops without power/acid", ElectrolyzerBlockEntity.TOTAL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // ガスホルダー [6-02]
    // =====================================================================

    @GameTest(maxTicks = 80)
    public void gasHolderPullAndBang(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        h.setBlock(ep, ModBlocks.ELECTROLYZER);
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, 2));
        ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, new ItemStack(ModItems.OXYGEN_GAS, 1));

        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            var gc = g.getContainer();
            h.assertValueEqual(g.getGasType(), GasType.HYDROGEN, "holder gas type after pulling");
            int total = g.getAmount() + gc.getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertValueEqual(total, 2, "2 mol of H2 pulled (stored + 1 bag in the out slot)");
            h.assertTrue(ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "electrolyzer H2 outlet emptied");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_O2).getCount(), 1, "O2 is not pulled into an H2 holder");
            h.assertTrue(g.getAmount() > 0, "some H2 must stay inside for the bang test");
            // 電解槽に残った O2 を片付ける（爆発で空になったホルダーが、次の吸い込みで O2 を吸ってしまうため）
            ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, ItemStack.EMPTY);
            // 爆鳴気：水素の入ったホルダーに酸素を入れる
            gc.setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(ModItems.OXYGEN_GAS, 1));
        });
        h.runAfterDelay(30, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            h.assertValueEqual(g.getAmount(), 0, "oxyhydrogen bang empties the holder");
            h.assertValueEqual(g.getGasType(), GasType.NONE, "gas type after bang");
            h.assertBlockPresent(ModBlocks.GAS_HOLDER, hp);
            LOG.info("[LAB] gas holder: pulled H2 from electrolyzer, O2 left alone, bang -> empty, block intact");
            h.succeed();
        });
    }

    /** 満杯（1000mol）のホルダーは、電解槽の出口からも「入」の袋からも吸い込まない */
    @GameTest(maxTicks = 80)
    public void gasHolderFullStops(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        h.setBlock(ep, ModBlocks.ELECTROLYZER);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        var gc = g.getContainer();
        g.setContentsForTest(GasType.HYDROGEN, GasHolderBlockEntity.CAPACITY);
        // 「出」を埋めておく（空だと1袋出して 999 になり、また吸えてしまうため）
        gc.setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(ModItems.HYDROGEN_GAS, 64));
        gc.setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(ModItems.HYDROGEN_GAS, 3));
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, 4));
        h.runAfterDelay(30, () -> {
            h.assertValueEqual(g.getAmount(), GasHolderBlockEntity.CAPACITY, "full holder stays at capacity");
            h.assertValueEqual(gc.getItem(GasHolderBlockEntity.SLOT_IN).getCount(), 3, "full holder must not take bags from the in slot");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 4, "full holder must not pull from the electrolyzer");
            // 1袋取り出す → 999 になり、また吸い込める
            gc.setItem(GasHolderBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        });
        h.runAfterDelay(60, () -> {
            h.assertTrue(gc.getItem(GasHolderBlockEntity.SLOT_IN).getCount() < 3 || ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount() < 4,
                    "after taking bags out, the holder should accept gas again");
            LOG.info("[LAB] gas holder full ({} mol): in-slot and electrolyzer untouched; resumes after emptying the out slot", GasHolderBlockEntity.CAPACITY);
            h.succeed();
        });
    }

    // =====================================================================
    // ベッセマー転炉 [1-26]
    // =====================================================================

    @GameTest
    public void bessemerConverter(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.BESSEMER_CONVERTER);
        BessemerConverterBlockEntity be = h.getBlockEntity(pos, BessemerConverterBlockEntity.class);
        var c = be.getContainer();
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Items.IRON_INGOT, 3));

        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS + 100, 0);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).isEmpty(), "no power (no blast) -> no steel");

        long feed = 4000;
        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS - 1, feed);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).isEmpty(), "not finished 1 tick before 20 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).is(ModItems.STEEL_INGOT), "steel after 20 s");
        h.assertValueEqual(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).getCount(), 1, "steel count");
        h.assertValueEqual(c.getItem(BessemerConverterBlockEntity.SLOT_IN).getCount(), 2, "iron consumed 1");

        // 鉄以外（金インゴット）は受け付けない
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Items.GOLD_INGOT, 1));
        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).getCount(), 1, "gold must not become steel");

        // 送風中は炉口が光る（LIT）
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Items.IRON_INGOT, 1));
        tickConsumer(h, pos, be, 1, feed);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        // 出口が満杯（鋼 64）→ 止まる
        c.setItem(BessemerConverterBlockEntity.SLOT_OUT, new ItemStack(ModItems.STEEL_INGOT, 64));
        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(BessemerConverterBlockEntity.SLOT_IN).getCount(), 1, "outlet full -> iron not used");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);

        // 炎の段階：①ケイ素（最初の2割）→②炭素（〜9割）→③炎が落ちる
        int T = BessemerConverterBlockEntity.TOTAL_TICKS;
        h.assertValueEqual(BessemerConverterBlockEntity.stageOf(0, T), BessemerConverterBlockEntity.Stage.SILICON, "stage at 0%");
        h.assertValueEqual(BessemerConverterBlockEntity.stageOf(T / 2, T), BessemerConverterBlockEntity.Stage.CARBON, "stage at 50%");
        h.assertValueEqual(BessemerConverterBlockEntity.stageOf(T * 95 / 100, T), BessemerConverterBlockEntity.Stage.DROP, "stage at 95%");

        // 鋼インゴットは c:ingots/steel タグに入っている（他MODのレシピで使える）
        var steelTag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/steel"));
        h.assertTrue(new ItemStack(ModItems.STEEL_INGOT).is(steelTag), "steel ingot should be in #c:ingots/steel");
        LOG.info("[LAB] bessemer: iron -> steel in {} ticks with 20 kW, nothing without power", BessemerConverterBlockEntity.TOTAL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // 帯域精製炉
    // =====================================================================

    /** GeCl4 蒸留 [1-34] を経た還元ゲルマニウム（帯域精製で 10N まで行ける） */
    private static ItemStack rawChloride(int n) {
        ItemStack s = new ItemStack(ModItems.RAW_GERMANIUM, n);
        s.set(ModDataComponents.CHLORIDE_ROUTE, true);
        return s;
    }

    private static ItemStack refined(double purity, int n) {
        ItemStack s = new ItemStack(ModItems.REFINED_GERMANIUM, n);
        s.set(ModDataComponents.PURITY, purity);
        return s;
    }

    private static double purityOf(ItemStack s) { Double d = s.get(ModDataComponents.PURITY); return d == null ? -1 : d; }

    @GameTest
    public void zoneRefiningPurity(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ZONE_REFINING_FURNACE);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT;
        c.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(ModItems.SYNTHETIC_GRAPHITE_BOAT));

        c.setItem(G, rawChloride(4));   // [1-34] GeCl4 経由（硫酸経由は 9N 止まり＝geCl4Route）
        tickRefiner(h, pos, be, T - 1);
        h.assertTrue(c.getItem(G).is(ModItems.RAW_GERMANIUM), "not refined 1 tick before 30 s");
        tickRefiner(h, pos, be, 1);
        h.assertTrue(c.getItem(G).is(ModItems.REFINED_GERMANIUM), "raw -> refined");
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.SIX_N, "1st pass = 6N");
        h.assertValueEqual(c.getItem(G).getCount(), 4, "count kept");
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "2nd pass = 9N");
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "3rd pass = 10N");
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "no 11N: Ge stops at 10N");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        LOG.info("[LAB] zone refining: raw -> 6N -> 9N -> 10N, then idle (no 11N)");
        h.succeed();
    }

    @GameTest
    public void zoneRefiningDoping(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ZONE_REFINING_FURNACE);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT, D = ZoneRefiningFurnaceBlockEntity.DOPANT_SLOT;
        c.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(ModItems.GRAPHITE_BOAT));   // ドーピングは粘土ボートでも 9N でできる

        // 9N + リン → n型
        c.setItem(G, refined(Purity.NINE_N, 8));
        c.setItem(D, new ItemStack(ModItems.PHOSPHORUS, 2));
        tickRefiner(h, pos, be, T);
        h.assertTrue(c.getItem(G).is(ModItems.N_TYPE_GERMANIUM), "9N + P -> n-type");
        h.assertValueEqual(c.getItem(G).getCount(), 8, "n-type count");
        h.assertValueEqual(c.getItem(D).getCount(), 1, "1 P consumed");

        // 9N + インジウム → p型
        c.setItem(G, refined(Purity.NINE_N, 3));
        c.setItem(D, new ItemStack(ModItems.INDIUM, 1));
        tickRefiner(h, pos, be, T);
        h.assertTrue(c.getItem(G).is(ModItems.P_TYPE_GERMANIUM), "9N + In -> p-type");
        h.assertTrue(c.getItem(D).isEmpty(), "In consumed");

        // 6N + リン → ドープしない（純度不足。先に精製へ回る）
        c.setItem(G, refined(Purity.SIX_N, 2));
        c.setItem(D, new ItemStack(ModItems.PHOSPHORUS, 1));
        tickRefiner(h, pos, be, T);
        h.assertFalse(c.getItem(G).is(ModItems.N_TYPE_GERMANIUM), "6N + P must not become n-type");
        h.assertValueEqual(c.getItem(D).getCount(), 1, "P not consumed with 6N");
        h.assertValueEqual(c.getItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT).getCount(), 1, "boat is not used up");
        LOG.info("[LAB] zone doping: 9N+P -> n, 9N+In -> p, 6N+P -> no doping (refines to {} first)", Purity.label(purityOf(c.getItem(G))));
        h.succeed();
    }

    // =====================================================================
    // データ（レシピ・進捗・関数）とバニラ要素
    // =====================================================================

    private static final String[] RECIPES = {
            "acheson_furnace", "bessemer_converter", "cinnabar_dust", "coal_ash", "compressor", "copper_cable", "dynamo", "electric_furnace", "electric_motor", "electrolyzer", "empty_gas_cylinder", "oxyhydrogen_burner", "sodium_carbonate", "glass_batch", "soda_lime_glass", "soda_lime_glass_from_blasting",
            "gas_holder", "germanium_concentrate_from_ash", "germanium_dioxide",
            "graphite_boat", "graphite_boat_blank", "graphite_rod", "synthetic_graphite_boat", "hand_vacuum_pump", "hf_oscillator", "hydrogen_furnace", "induction_coil", "junction_transistor",
            "junction_transistor_blank", "lead_ingot", "lead_screw", "lead_acid_battery", "leyden_jar", "litharge", "magnet", "mercury", "mercury_vacuum_gauge", "phosphorus", "quartz_tube", "quicklime", "quicklime_from_blasting", "slaked_lime", "raw_germanium", "refractory_brick", "refractory_brick_blank", "sealing_station", "sprengel_pump", "air_separation_unit", "crystal_puller", "arc_furnace", "boric_acid", "boron_trioxide", "self_excited_dynamo", "steam_engine", "steam_turbine",
            "sulfuric_acid", "sulfur_dioxide", "sulfur_powder_from_block", "sulfur_powder_from_potent", "sulfur_powder_from_spike", "transistor",
            "transistor_radio", "wolframite_soda_charge", "sodium_tungstate", "tungstic_acid", "tungsten_trioxide_from_acid", "tungsten_wire", "vacuum_chamber", "vacuum_tube_assembly", "water_wheel", "zinc_ingot", "zinc_from_oxide", "reverberatory_furnace", "zone_refining_furnace",
            "salt_from_rock_salt", "salt_pan", "hcl_burner",
            "crude_germanium_tetrachloride", "quartz_still", "germanium_tetrachloride", "high_purity_germanium_dioxide"};

    @GameTest
    public void recipesResolve(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String r : RECIPES) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(r));
            if (server.getRecipeManager().byKey(key).isEmpty()) missing.add(r);
        }
        h.assertTrue(missing.isEmpty(), "recipes not loaded (JSON error or missing item?): " + missing);
        LOG.info("[LAB] recipes: all {} resolved in RecipeManager", RECIPES.length);
        h.succeed();
    }

    /** かまど系（SMELTING / BLASTING）に1個入れたときの出力。レシピがなければ EMPTY */
    private static <T extends net.minecraft.world.item.crafting.AbstractCookingRecipe> ItemStack cook(GameTestHelper h,
            net.minecraft.world.item.crafting.RecipeType<T> type, net.minecraft.world.item.Item in) {
        var input = new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(in));
        return h.getLevel().getServer().getRecipeManager().getRecipeFor(type, input, h.getLevel())
                .map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    /** レシピ id の材料に item が含まれるか（placementInfo の Ingredient で判定） */
    private static boolean recipeUses(GameTestHelper h, String id, net.minecraft.world.item.Item item) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
        return h.getLevel().getServer().getRecipeManager().byKey(key)
                .map(r -> r.value().placementInfo().ingredients().stream().anyMatch(i -> i.test(new ItemStack(item)))).orElse(false);
    }

    // =====================================================================
    // [3-12] 耐火れんが（珪石れんが・ディナス）
    // =====================================================================

    /** 成形品は溶鉱炉（≈1450℃）でだけ焼ける。ふつうのかまど（≈1000℃）では焼けない。転炉の内張りは耐火れんが */
    @GameTest
    public void refractoryBrick(GameTestHelper h) {
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        ItemStack fired = cook(h, BLAST, ModItems.REFRACTORY_BRICK_BLANK);
        h.assertTrue(fired.is(ModItems.REFRACTORY_BRICK), "blast furnace should fire the blank into a refractory brick, got " + fired);
        h.assertTrue(cook(h, SMELT, ModItems.REFRACTORY_BRICK_BLANK).isEmpty(), "an ordinary furnace (~1000 C) must not fire silica bricks");
        h.assertTrue(recipeUses(h, "refractory_brick_blank", Items.SAND) && recipeUses(h, "refractory_brick_blank", ModItems.SLAKED_LIME), "blank = silica sand + slaked lime (Dinas)");
        h.assertFalse(recipeUses(h, "refractory_brick_blank", Items.CLAY_BALL), "binder is lime now, not clay [0-10]");
        h.assertTrue(recipeUses(h, "bessemer_converter", ModItems.REFRACTORY_BRICK), "converter lining should be refractory brick");
        h.assertFalse(recipeUses(h, "bessemer_converter", Items.CLAY_BALL), "converter no longer uses raw clay");
        LOG.info("[LAB] refractory brick: blank fires only in the blast furnace; Bessemer converter lined with refractory brick");
        h.succeed();
    }

    // =====================================================================
    // [0-10] 生石灰・[0-11] 消石灰
    // =====================================================================

    /** 方解石をかまど/溶鉱炉で焼くと生石灰（CaCO3 → CaO + CO2）。生石灰＋水 → 消石灰。石や凝灰岩は焼いても石灰にならない */
    @GameTest
    public void limeFromCalcite(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        h.assertTrue(cook(h, SMELT, Items.CALCITE).is(ModItems.QUICKLIME), "furnace: calcite -> quicklime");
        h.assertTrue(cook(h, BLAST, Items.CALCITE).is(ModItems.QUICKLIME), "blast furnace: calcite -> quicklime");
        h.assertFalse(cook(h, SMELT, Items.TUFF).is(ModItems.QUICKLIME), "tuff is not CaCO3");
        h.assertFalse(cook(h, SMELT, Items.STONE).is(ModItems.QUICKLIME), "stone is not CaCO3");
        h.assertTrue(recipeUses(h, "slaked_lime", ModItems.QUICKLIME) && recipeUses(h, "slaked_lime", Items.WATER_BUCKET), "CaO + H2O -> Ca(OH)2");
        LOG.info("[LAB] lime: calcite -> quicklime (furnace / blast), quicklime + water -> slaked lime -> brick binder");
        h.succeed();
    }

    // =====================================================================
    // [1-27] 鉛（方鉛鉱 → 焙焼 → PbO → 還元 → 鉛）
    // =====================================================================

    @GameTest
    public void leadFromGalena(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        ItemStack pbo = cook(h, SMELT, ModItems.GALENA);
        h.assertTrue(pbo.is(ModItems.LITHARGE), "roasting galena should give litharge (PbO), got " + pbo);
        ItemStack pb = cook(h, SMELT, ModItems.LITHARGE);
        h.assertTrue(pb.is(ModItems.LEAD_INGOT), "reducing PbO should give a lead ingot, got " + pb);
        h.assertFalse(cook(h, SMELT, ModItems.GALENA).is(ModItems.LEAD_INGOT), "galena must not jump straight to lead (roast first)");
        var leadTag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/lead"));
        var ingots = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots"));
        h.assertTrue(new ItemStack(ModItems.LEAD_INGOT).is(leadTag), "lead ingot should be in #c:ingots/lead");
        h.assertTrue(new ItemStack(ModItems.LEAD_INGOT).is(ingots), "lead ingot should be in #c:ingots");
        LOG.info("[LAB] lead: galena -> litharge (PbO) -> lead ingot, tagged c:ingots/lead");
        h.succeed();
    }

    // =====================================================================
    // [3-06] 反射炉（焙焼炉）：鉱石＋燃料 → 金属酸化物/金属 ＋ SO2（＋閃亜鉛鉱の副産物）
    // =====================================================================

    private static void tickRoaster(GameTestHelper h, BlockPos rel, ReverberatoryFurnaceBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) ReverberatoryFurnaceBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    @GameTest
    public void reverberatoryRoasting(GameTestHelper h) {
        int T = ReverberatoryFurnaceBlockEntity.ROAST_TICKS;
        int IN = ReverberatoryFurnaceBlockEntity.SLOT_IN, FUEL = ReverberatoryFurnaceBlockEntity.SLOT_FUEL, OUT = ReverberatoryFurnaceBlockEntity.SLOT_OUT;
        int SO2 = ReverberatoryFurnaceBlockEntity.SLOT_SO2, GE = ReverberatoryFurnaceBlockEntity.SLOT_BY_GE, IND = ReverberatoryFurnaceBlockEntity.SLOT_BY_IN;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.REVERBERATORY_FURNACE);
        ReverberatoryFurnaceBlockEntity be = h.getBlockEntity(pos, ReverberatoryFurnaceBlockEntity.class);
        var c = be.getContainer();

        // 燃料なし：焼けない
        c.setItem(IN, new ItemStack(ModItems.GALENA, 2));
        tickRoaster(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no fuel -> no roasting");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 石炭1個：方鉛鉱 → 一酸化鉛 ＋ SO2（10秒）
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        tickRoaster(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickRoaster(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(ModItems.LITHARGE), "galena -> litharge (2PbS + 3O2 -> 2PbO + 2SO2)");
        h.assertTrue(c.getItem(SO2).is(ModItems.SULFUR_DIOXIDE), "SO2 is collected");
        h.assertTrue(c.getItem(FUEL).isEmpty(), "one coal was burned");
        tickRoaster(h, pos, be, T);
        h.assertValueEqual(c.getItem(OUT).getCount(), 2, "the same coal keeps burning for the 2nd ore");
        h.assertValueEqual(c.getItem(SO2).getCount(), 2, "2 SO2");
        // SO2 は硫酸の原料
        h.assertTrue(recipeUses(h, "sulfuric_acid", ModItems.SULFUR_DIOXIDE), "SO2 -> sulfuric acid route");

        // 辰砂の粉 → 水銀 ＋ SO2（木炭でも燃える）
        c.setItem(OUT, ItemStack.EMPTY); c.setItem(SO2, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(ModItems.CINNABAR_DUST, 1));
        c.setItem(FUEL, new ItemStack(Items.CHARCOAL, 1));
        tickRoaster(h, pos, be, T);
        h.assertTrue(c.getItem(OUT).is(ModItems.MERCURY), "cinnabar -> mercury (HgS + O2 -> Hg + SO2)");
        h.assertValueEqual(c.getItem(SO2).getCount(), 1, "SO2 from cinnabar");

        // 閃亜鉛鉱 8個 → 酸化亜鉛 8 ＋ SO2 8 ＋ Ge精鉱 2 ＋ インジウム 1（1回で同時に）
        c.setItem(OUT, ItemStack.EMPTY); c.setItem(SO2, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(ModItems.SPHALERITE, 8));
        c.setItem(FUEL, new ItemStack(Items.COAL, 2));
        tickRoaster(h, pos, be, T * 4);
        h.assertValueEqual(c.getItem(GE).getCount(), 1, "4 sphalerite -> 1 Ge concentrate");
        h.assertTrue(c.getItem(IND).isEmpty(), "no indium before the 8th");
        tickRoaster(h, pos, be, T * 4);
        h.assertTrue(c.getItem(OUT).is(ModItems.ZINC_OXIDE) && c.getItem(OUT).getCount() == 8, "8 sphalerite -> 8 ZnO, got " + c.getItem(OUT));
        h.assertValueEqual(c.getItem(SO2).getCount(), 8, "8 SO2");
        h.assertValueEqual(c.getItem(GE).getCount(), 2, "8 sphalerite -> 2 Ge concentrate");
        h.assertValueEqual(c.getItem(IND).getCount(), 1, "8 sphalerite -> 1 indium");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.ZINC_OXIDE).is(ModItems.ZINC_INGOT), "ZnO -> zinc in a furnace");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.SPHALERITE).is(ModItems.ZINC_INGOT), "old furnace route sphalerite -> zinc is kept");
        // 作業台での分割（旧 #16）は無くなった
        MinecraftServer server = h.getLevel().getServer();
        for (String gone : new String[] {"germanium_concentrate_from_sphalerite", "indium"})
            h.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(gone))).isEmpty(), "crafting split recipe should be gone: " + gone);

        // SO2 の出口が満杯なら止まる（煙を捨てない）
        c.setItem(IN, new ItemStack(ModItems.GALENA, 1));
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(SO2, new ItemStack(ModItems.SULFUR_DIOXIDE, 64));
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        tickRoaster(h, pos, be, T * 2);
        h.assertTrue(c.getItem(OUT).isEmpty(), "SO2 output full -> stop");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "galena not used while SO2 is full");
        // 焼けない物は入らない
        h.assertFalse(ReverberatoryFurnaceBlockEntity.isInput(new ItemStack(Items.RAW_IRON)), "iron ore is not roasted here");
        h.assertFalse(ReverberatoryFurnaceBlockEntity.isFuel(new ItemStack(Items.LAVA_BUCKET)), "only coal / charcoal");
        LOG.info("[LAB] reverberatory furnace: galena -> PbO + SO2, cinnabar -> Hg + SO2, 8 sphalerite -> 8 ZnO + 8 SO2 + 2 Ge conc + 1 In ({} t each, coal {} t)",
                T, ReverberatoryFurnaceBlockEntity.FUEL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // [1-28] タングステン線（WO3 → 水素還元 → W粉 → 通電焼結 → 線引き）
    // =====================================================================

    @GameTest
    public void tungstenHydrogenFurnace(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.TUNGSTIC_ACID).is(ModItems.TUNGSTEN_TRIOXIDE), "tungstic acid -> WO3 (task09 real route)");
        h.assertTrue(recipeUses(h, "tungsten_wire", ModItems.TUNGSTEN_BAR), "wire is drawn from the sintered bar");
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.HYDROGEN_FURNACE);
        HydrogenFurnaceBlockEntity be = h.getBlockEntity(pos, HydrogenFurnaceBlockEntity.class);
        var c = be.getContainer();
        int IN = HydrogenFurnaceBlockEntity.SLOT_IN, H2 = HydrogenFurnaceBlockEntity.SLOT_H2, OUT = HydrogenFurnaceBlockEntity.SLOT_OUT;
        c.setItem(IN, new ItemStack(ModItems.TUNGSTEN_TRIOXIDE, 2));

        // 水素なし：電気があっても還元しない（炭素は使わない）
        long reduceFeed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.REDUCE_WATTS);
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, reduceFeed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no hydrogen -> no reduction");
        // 水素5袋、電気なし：止まる（上で入れた電気がバッファに残っているので、先に抜く）
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_GAS, 5));
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> no reduction");
        // 電気あり：30秒で W粉1、WO3 -1、H2 -3（WO3 + 3H2 -> W + 3H2O）
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS - 1, reduceFeed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        tickConsumer(h, pos, be, 1, reduceFeed);
        h.assertTrue(c.getItem(OUT).is(ModItems.TUNGSTEN_POWDER), "WO3 + 3 H2 -> W powder");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "WO3 used 1");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "H2 used 3");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        // 水素2袋では足りない（3要る）→ 止まる
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, reduceFeed);
        h.assertValueEqual(c.getItem(OUT).getCount(), 1, "2 H2 is not enough for WO3");

        // 焼結：W粉2 + H2 1 + 10kW×20秒 → 焼結棒
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(ModItems.TUNGSTEN_POWDER, 3));
        long sinterFeed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.SINTER_WATTS);
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.SINTER_TICKS, sinterFeed);
        h.assertTrue(c.getItem(OUT).is(ModItems.TUNGSTEN_BAR), "2 W powder + H2 -> sintered bar");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "powder used 2");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "H2 used 1");
        // 粉1個では焼結しない
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.SINTER_TICKS + 10, sinterFeed);
        h.assertValueEqual(c.getItem(OUT).getCount(), 1, "1 powder is not enough for a bar");
        // 3kW しか来ないと、電気が貯まった tick にしか進まない（約3割の速さ）→ 2倍の時間でも終わらない
        c.setItem(IN, new ItemStack(ModItems.TUNGSTEN_POWDER, 2));
        c.setItem(OUT, ItemStack.EMPTY);
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.SINTER_TICKS * 2, reduceFeed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "with only 3 kW supplied, sintering (10 kW) must not finish in 2x the time");
        LOG.info("[LAB] hydrogen furnace: WO3 + 3 H2 -> W powder ({} t @3 kW), 2 W + H2 -> bar ({} t @10 kW), stops without H2/power",
                HydrogenFurnaceBlockEntity.REDUCE_TICKS, HydrogenFurnaceBlockEntity.SINTER_TICKS);
        h.succeed();
    }

    /** 隣の水素ガスホルダーから、水素炉のスロットへ 16 袋まで自動で補充（本当に tick を進める） */
    @GameTest(maxTicks = 60)
    public void hydrogenFurnacePullsFromHolder(GameTestHelper h) {
        BlockPos fp = p(1, 1, 1), hp = p(2, 1, 1);
        h.setBlock(fp, ModBlocks.HYDROGEN_FURNACE);
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        g.setContentsForTest(GasType.HYDROGEN, 20);
        g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(ModItems.HYDROGEN_GAS, 64)); // 出口を埋めて量を固定
        h.runAfterDelay(25, () -> {
            var c = h.getBlockEntity(fp, HydrogenFurnaceBlockEntity.class).getContainer();
            h.assertValueEqual(c.getItem(HydrogenFurnaceBlockEntity.SLOT_H2).getCount(), 16, "furnace H2 slot filled to 16 from the holder");
            h.assertValueEqual(g.getAmount(), 4, "holder gave 16 of 20 mol");
            LOG.info("[LAB] hydrogen furnace pulled 16 H2 from an adjacent gas holder (20 -> 4 mol)");
            h.succeed();
        });
    }

    // =====================================================================
    // [6-03] 圧縮機（ガスホルダーのガス → 鋼のボンベに 50mol）
    // =====================================================================

    @GameTest
    public void compressorFillsCylinders(GameTestHelper h) {
        BlockPos cp = p(1, 1, 1), hp = p(2, 1, 1);
        h.setBlock(cp, ModBlocks.COMPRESSOR);
        CompressorBlockEntity be = h.getBlockEntity(cp, CompressorBlockEntity.class);
        var c = be.getContainer();
        int E = CompressorBlockEntity.SLOT_EMPTY, OUT = CompressorBlockEntity.SLOT_OUT, T = CompressorBlockEntity.TOTAL_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS);
        c.setItem(E, new ItemStack(ModItems.EMPTY_GAS_CYLINDER, 3));

        // ホルダーなし：電気があっても動かない
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no gas holder -> nothing filled");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 水素 40mol（50 に足りない）→ 動かない
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(ModItems.HYDROGEN_GAS, 64)); // ホルダーの出口を埋めて量を固定
        g.setContentsForTest(GasType.HYDROGEN, 40);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "40 mol is not enough for a 50 mol cylinder");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 水素 120mol、電気なし → 動かない
        g.setContentsForTest(GasType.HYDROGEN, 120);
        tickConsumer(h, cp, be, T + 10, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> nothing filled");

        // 電気あり：124秒で水素ボンベ1本、ホルダー -50、空ボンベ -1
        tickConsumer(h, cp, be, T - 1, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(cp, BlockStateProperties.LIT, true);
        tickConsumer(h, cp, be, 1, feed);
        h.assertTrue(c.getItem(OUT).is(ModItems.HYDROGEN_CYLINDER), "H2 holder -> hydrogen cylinder");
        h.assertValueEqual(g.getAmount(), 70, "holder lost 50 mol");
        h.assertValueEqual(c.getItem(E).getCount(), 2, "one empty cylinder used");

        // ホルダーの中身を酸素に替える → 出口に水素ボンベがあるので酸素ボンベは置けない＝止まる
        g.setContentsForTest(GasType.OXYGEN, 100);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertValueEqual(g.getAmount(), 100, "out slot holds H2 cylinders -> O2 must not be compressed");
        // 出口を空ける → 酸素ボンベ
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, cp, be, T, feed);
        h.assertTrue(c.getItem(OUT).is(ModItems.OXYGEN_CYLINDER), "O2 holder -> oxygen cylinder");
        h.assertValueEqual(g.getAmount(), 50, "O2 holder lost 50 mol");
        h.assertValueEqual(GasType.ofCylinder(c.getItem(OUT)), GasType.OXYGEN, "GasType.ofCylinder");

        // 空ボンベがなくなったら止まる
        c.setItem(E, ItemStack.EMPTY);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertValueEqual(g.getAmount(), 50, "no empty cylinder -> holder untouched");
        h.assertBlockProperty(cp, BlockStateProperties.LIT, false);
        LOG.info("[LAB] compressor: 50 mol from an adjacent holder -> 1 cylinder in {} ticks @5 kW (620 kJ); H2 and O2; stops without holder/gas/power/cylinder", T);
        h.succeed();
    }

    private static final String[] CHAPTER1 = {"root", "coal_ash", "concentrate", "dioxide", "sulfuric_acid", "raw_germanium",
            "refined", "n_type", "transistor", "junction", "radio"};
    private static final String[] GUIDE_ADV = {"root", "welcome", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"};
    private static final String[] GUIDE_FN = {"give_guidebook", "guide/welcome", "guide/raw_germanium", "guide/refined",
            "guide/n_type", "guide/junction", "guide/radio", "guide/sulfuric_acid"};

    // =====================================================================
    // [5-01] 手押しピストンポンプ ＋ [5-11] 真空容器
    // =====================================================================

    /** 1回で p' = 1000 + (p-1000)*0.8。何回引いても約 1kPa より下がらない。容器がなければ何もしない。放っておくと漏れて上がる */
    @GameTest(maxTicks = 80)
    public void handPumpEvacuates(GameTestHelper h) {
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), pp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, ModBlocks.VACUUM_CHAMBER);
        h.setBlock(pp, ModBlocks.HAND_VACUUM_PUMP);
        h.setBlock(lonely, ModBlocks.HAND_VACUUM_PUMP);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        h.assertTrue(Math.abs(c.getPressure() - VacuumChamberBlockEntity.ATM) < 1e-6, "a new chamber starts at 1 atm");
        HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp));
        double one = 1000 + (VacuumChamberBlockEntity.ATM - 1000) * 0.8;
        h.assertTrue(Math.abs(c.getPressure() - one) < 1, "one stroke should give " + one + " Pa, got " + c.getPressure());
        int n = 1;
        while (c.getPressure() > 1500 && n < 200) { HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp)); n++; }
        h.assertTrue(n >= 20 && n <= 30, "about 24 strokes to get below 1.5 kPa, took " + n);
        for (int i = 0; i < 100; i++) HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp));
        double floorPa = c.getPressure();
        h.assertTrue(floorPa >= HandVacuumPumpBlock.LIMIT_PA && floorPa < HandVacuumPumpBlock.LIMIT_PA + 1, "hand pump bottoms out at ~1 kPa, got " + floorPa);
        h.assertTrue(HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(lonely)) == null, "a pump with no chamber next to it does nothing");
        final int strokes = n;
        h.runAfterDelay(45, () -> {
            h.assertTrue(c.getPressure() > floorPa, "air should leak back in (~0.002 Pa/s): " + floorPa + " -> " + c.getPressure());
            LOG.info("[LAB] hand pump: 1 stroke -> {}, {} strokes to <1.5 kPa, floor {}, leak to {}", VacuumChamberBlockEntity.formatPa(one), strokes,
                    VacuumChamberBlockEntity.formatPa(floorPa), VacuumChamberBlockEntity.formatPa(c.getPressure()));
            h.succeed();
        });
    }

    // =====================================================================
    // [5-15] シュプレンゲル水銀ポンプ（タスク12）
    // =====================================================================

    /** 水銀4個で動く。1秒ごとに p' = 0.05 + (p-0.05)*0.9。1気圧から約2分で 0.1Pa 以下、漏れがあっても 0.1Pa 未満を保つ。水銀なし・容器なしでは何もしない */
    @GameTest
    public void sprengelPumpReachesSoftVacuum(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sprengel_pump", ModItems.SODA_LIME_GLASS), "the pump is glass tubing (soda-lime glass)");
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, ModBlocks.VACUUM_CHAMBER);
        h.setBlock(sp, ModBlocks.SPRENGEL_PUMP);
        h.setBlock(lonely, ModBlocks.SPRENGEL_PUMP);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var pump = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.class);
        h.assertTrue(pump.pumpOnce() == null && c.getPressure() == VacuumChamberBlockEntity.ATM, "no mercury -> no pumping");
        for (int i = 0; i < com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.MERCURY_NEEDED; i++) h.assertTrue(pump.addMercury(), "mercury " + i + " should go in");
        h.assertTrue(!pump.addMercury(), "the reservoir holds only " + com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.MERCURY_NEEDED);
        int s = 0;
        while (c.getPressure() > SealingStationBlockEntity.SEAL_MAX_PA && s < 600) { pump.pumpOnce(); c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND); s++; }
        h.assertTrue(s >= 90 && s <= 180, "about 2 minutes from 1 atm to 0.1 Pa, took " + s + " s");
        for (int i = 0; i < 300; i++) { pump.pumpOnce(); c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND); }
        double eq = c.getPressure();
        h.assertTrue(eq >= com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.LIMIT_PA && eq < SealingStationBlockEntity.SEAL_MAX_PA, "steady state with the leak stays below 0.1 Pa, got " + eq);
        c.setPressure(HandVacuumPumpBlock.LIMIT_PA);
        int r = 0;
        while (c.getPressure() > SealingStationBlockEntity.SEAL_MAX_PA && r < 600) { pump.pumpOnce(); r++; }
        h.assertTrue(r < s, "roughing to 1 kPa with the hand pump first is faster: " + r + " < " + s);
        var alone = h.getBlockEntity(lonely, com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.class);
        for (int i = 0; i < 4; i++) alone.addMercury();
        h.assertTrue(alone.pumpOnce() == null, "no chamber next to it -> nothing");
        h.destroyBlock(sp);
        final int fromAtm = s, fromRough = r;
        h.runAfterDelay(2, () -> {
            h.assertItemEntityPresent(ModItems.MERCURY, sp, 2.0);
            LOG.info("[LAB] Sprengel pump: 4 Hg, 1 atm -> 0.1 Pa in {} s, from 1 kPa in {} s, steady {} with leak, breaking drops the mercury", fromAtm, fromRough, VacuumChamberBlockEntity.formatPa(eq));
            h.succeed();
        });
    }

    // =====================================================================
    // 機械のレシピの書き出し（タスク24・25）：docs/machine_recipes.json
    // 各機械の定数（電力・時間・量）とアイテムの登録から作るので、コードを変えれば表も変わる。node.html の「ゲームでの作り方」の元データ
    // 量は数字の項目で持つ：count（1回あたり）・unit（"mol" など）・per_runs（N回で count 個へる）・consumed（false＝減らない）・
    //   max（1回に入れられる上限）・alt（代わりに使えるアイテム）・cond（条件の短い説明）。表示の文は gen_tree_views.py が作る
    // =====================================================================

    private static String iid(net.minecraft.world.item.Item it) { return BuiltInRegistries.ITEM.getKey(it).toString(); }
    /** 材料・生成物1つ。item はアイテムか、アイテムでない物（空気・電気など）の名前 */
    private static com.google.gson.JsonObject io(Object item, double count) {
        var o = new com.google.gson.JsonObject();
        o.addProperty("item", item instanceof net.minecraft.world.item.Item it ? iid(it) : String.valueOf(item));
        if (count == Math.rint(count)) o.addProperty("count", (long) count); else o.addProperty("count", count);
        return o;
    }
    private static com.google.gson.JsonObject mol(Object item, double n) { var o = io(item, n); o.addProperty("unit", "mol"); return o; }
    private static com.google.gson.JsonObject kept(com.google.gson.JsonObject o) { o.addProperty("consumed", false); return o; }
    private static com.google.gson.JsonObject perRuns(com.google.gson.JsonObject o, int runs) { o.addProperty("per_runs", runs); return o; }
    private static com.google.gson.JsonObject max(com.google.gson.JsonObject o, int m) { o.addProperty("max", m); return o; }
    private static com.google.gson.JsonObject alt(com.google.gson.JsonObject o, net.minecraft.world.item.Item... its) {
        var a = new com.google.gson.JsonArray(); for (var it : its) a.add(iid(it)); o.add("alt", a); return o;
    }
    private static com.google.gson.JsonObject cond(com.google.gson.JsonObject o, String c) { o.addProperty("cond", c); return o; }
    private static com.google.gson.JsonArray L(com.google.gson.JsonObject... xs) { var a = new com.google.gson.JsonArray(); for (var x : xs) a.add(x); return a; }
    private static void rec(com.google.gson.JsonArray out, net.minecraft.world.level.block.Block machine, String process,
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

    @GameTest
    public void exportMachineRecipes(GameTestHelper h) {
        var a = new com.google.gson.JsonArray();
        double zt = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS / 20.0, zw = ZoneRefiningFurnaceBlockEntity.WATTS;
        rec(a, ModBlocks.ZONE_REFINING_FURNACE, "帯域精製（1パスで純度が1段上がる：6N→9N→10N）",
            L(max(io(ModItems.RAW_GERMANIUM, 1), 8), kept(cond(io(ModItems.GRAPHITE_BOAT, 1), "粘土黒鉛＝9Nまで、人造黒鉛＝10Nまで"))),
            L(io(ModItems.REFINED_GERMANIUM, 1)), zw, zt, "入れた数だけ出る。GeCl4 経由でない Ge は 9N まで");
        rec(a, ModBlocks.ZONE_REFINING_FURNACE, "ゾーンレベリング（n型）", L(max(cond(io(ModItems.REFINED_GERMANIUM, 1), "9N 以上"), 8), io(ModItems.PHOSPHORUS, 1)),
            L(io(ModItems.N_TYPE_GERMANIUM, 1)), zw, zt, "入れた数だけ出る");
        rec(a, ModBlocks.ZONE_REFINING_FURNACE, "ゾーンレベリング（p型）", L(max(cond(io(ModItems.REFINED_GERMANIUM, 1), "9N 以上"), 8), io(ModItems.INDIUM, 1)),
            L(io(ModItems.P_TYPE_GERMANIUM, 1)), zw, zt, "入れた数だけ出る");
        rec(a, ModBlocks.ELECTROLYZER, "水の電気分解", L(io(Items.WATER_BUCKET, 1), kept(io(ModItems.SULFURIC_ACID, 1))),
            L(io(ModItems.HYDROGEN_GAS, 2), io(ModItems.OXYGEN_GAS, 1), io(Items.BUCKET, 1)), ElectrolyzerBlockEntity.WATTS, ElectrolyzerBlockEntity.TOTAL_TICKS / 20.0, "2H2O → 2H2 + O2。硫酸は電解質");
        rec(a, ModBlocks.ELECTROLYZER, "塩水の電気分解（クロルアルカリ）", L(io(Items.WATER_BUCKET, 1), io(ModItems.SALT, ElectrolyzerBlockEntity.SALT_PER_RUN)),
            L(io(ModItems.HYDROGEN_GAS, 1), io(ModItems.CHLORINE_GAS, 1), io(ModItems.SODIUM_HYDROXIDE, 2), io(Items.BUCKET, 1)), ElectrolyzerBlockEntity.WATTS, ElectrolyzerBlockEntity.BRINE_TICKS / 20.0, "2NaCl + 2H2O → Cl2 + H2 + 2NaOH");
        rec(a, ModBlocks.BESSEMER_CONVERTER, "ベッセマー転炉", L(cond(io(Items.IRON_INGOT, 1), "銑鉄の近似")), L(io(ModItems.STEEL_INGOT, 1)),
            BessemerConverterBlockEntity.WATTS, BessemerConverterBlockEntity.TOTAL_TICKS / 20.0, "送風の動力を電力で代表");
        rec(a, ModBlocks.HYDROGEN_FURNACE, "WO3 の水素還元", L(io(ModItems.TUNGSTEN_TRIOXIDE, 1), io(ModItems.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.REDUCE_H2)),
            L(io(ModItems.TUNGSTEN_POWDER, 1)), HydrogenFurnaceBlockEntity.REDUCE_WATTS, HydrogenFurnaceBlockEntity.REDUCE_TICKS / 20.0, "WO3 + 3H2 → W + 3H2O");
        rec(a, ModBlocks.HYDROGEN_FURNACE, "タングステン粉の焼結", L(io(ModItems.TUNGSTEN_POWDER, HydrogenFurnaceBlockEntity.SINTER_POWDER), io(ModItems.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.SINTER_H2)),
            L(io(ModItems.TUNGSTEN_BAR, 1)), HydrogenFurnaceBlockEntity.SINTER_WATTS, HydrogenFurnaceBlockEntity.SINTER_TICKS / 20.0, null);
        rec(a, ModBlocks.HYDROGEN_FURNACE, "GeO2 の水素還元", L(alt(io(ModItems.GERMANIUM_DIOXIDE, 1), ModItems.HIGH_PURITY_GERMANIUM_DIOXIDE), io(ModItems.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.GE_REDUCE_H2)),
            L(io(ModItems.RAW_GERMANIUM, 1)), HydrogenFurnaceBlockEntity.REDUCE_WATTS, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS / 20.0, "GeO2 + 2H2 → Ge + 2H2O");
        rec(a, ModBlocks.COMPRESSOR, "ボンベに詰める", L(io(ModItems.EMPTY_GAS_CYLINDER, 1), mol("隣のガスホルダーのガス", CompressorBlockEntity.MOL_PER_CYLINDER)),
            L(io("そのガスのボンベ", 1)), CompressorBlockEntity.WATTS, CompressorBlockEntity.TOTAL_TICKS / 20.0, "約150気圧。H2・O2・Cl2・N2・Ar");
        rec(a, ModBlocks.SEALING_STATION, "真空管の封止", L(io(ModItems.VACUUM_TUBE_ASSEMBLY, 1), kept(cond(io("隣の真空容器", 1), SealingStationBlockEntity.SEAL_MAX_PA + " Pa 以下"))),
            L(io(ModItems.VACUUM_TUBE, 1)), SealingStationBlockEntity.WATTS, SealingStationBlockEntity.TICKS / 20.0, "封じるたびに容器へ空気がもどる");
        for (var e : ElectricFurnaceBlockEntity.recipes().entrySet())
            rec(a, ModBlocks.ELECTRIC_FURNACE, "電気で焼く", L(io(e.getKey(), 1)), L(io(e.getValue(), 1)), ElectricFurnaceBlockEntity.WATTS, ElectricFurnaceBlockEntity.TICKS / 20.0, null);
        rec(a, ModBlocks.ACHESON_FURNACE, "人造黒鉛", L(alt(io(Items.COAL, AchesonFurnaceBlockEntity.CARBON_PER_BATCH), Items.CHARCOAL), io(Items.SAND, AchesonFurnaceBlockEntity.SAND_PER_BATCH)),
            L(io(ModItems.SYNTHETIC_GRAPHITE, AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH)), AchesonFurnaceBlockEntity.REQUIRED_WATTS, AchesonFurnaceBlockEntity.WORK_TICKS / 20.0, "2500℃ 以上で進む（電力が足りないと届かない）");
        double fuelS = ReverberatoryFurnaceBlockEntity.FUEL_TICKS / (double) ReverberatoryFurnaceBlockEntity.ROAST_TICKS;
        for (var it : new net.minecraft.world.item.Item[] {ModItems.GALENA, ModItems.CINNABAR_DUST, ModItems.SPHALERITE}) {
            var main = ReverberatoryFurnaceBlockEntity.mainProduct(new ItemStack(it)).getItem();
            var out = it == ModItems.SPHALERITE
                    ? L(io(main, 1), io(ModItems.SULFUR_DIOXIDE, 1), perRuns(io(ModItems.GERMANIUM_CONCENTRATE, 1), ReverberatoryFurnaceBlockEntity.GE_EVERY), perRuns(io(ModItems.INDIUM, 1), ReverberatoryFurnaceBlockEntity.IN_EVERY))
                    : L(io(main, 1), io(ModItems.SULFUR_DIOXIDE, 1));
            rec(a, ModBlocks.REVERBERATORY_FURNACE, "焙焼", L(io(it, 1), perRuns(io(Items.COAL, 1), (int) fuelS)), out, 0, ReverberatoryFurnaceBlockEntity.ROAST_TICKS / 20.0, "燃料式（電気なし）");
        }
        rec(a, ModBlocks.OXYHYDROGEN_BURNER, "石英を溶かす", L(io(ModItems.VEIN_QUARTZ, 1), mol(ModItems.HYDROGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.H2_PER), mol(ModItems.OXYGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.O2_PER)),
            L(io(ModItems.FUSED_QUARTZ, 1)), 0, OxyhydrogenBurnerBlockEntity.MELT_TICKS / 20.0, "2H2 + O2 → 2H2O（約2800℃）。電気なし");
        rec(a, ModBlocks.HCL_BURNER, "塩酸を作る", L(mol(ModItems.HYDROGEN_GAS, HclBurnerBlockEntity.H2_PER), mol(ModItems.CHLORINE_GAS, HclBurnerBlockEntity.CL2_PER), io(Items.WATER_BUCKET, 1.0 / HclBurnerBlockEntity.ACID_PER_BUCKET)),
            L(io(ModItems.HYDROCHLORIC_ACID, 1)), 0, HclBurnerBlockEntity.BURN_TICKS / 20.0, "H2 + Cl2 → 2HCl を水に溶かす。電気なし");
        rec(a, ModBlocks.SALT_PAN, "天日で塩", L(kept(cond(io("海辺の水と日光", 1), "雨の日は止まる"))), L(io(ModItems.SALT, 1), perRuns(io(ModItems.BITTERN, 1), SaltPanBlockEntity.SALT_PER_BITTERN)),
            0, SaltPanBlockEntity.TICKS_PER_SALT / 20.0, "電気なし");
        rec(a, ModBlocks.SPRENGEL_PUMP, "真空にする", L(kept(io(ModItems.MERCURY, SprengelPumpBlockEntity.MERCURY_NEEDED)), kept(io("隣の真空容器", 1))),
            L(cond(io("真空容器の圧力", 1), "1秒ごとに残り " + (int) (SprengelPumpBlockEntity.RATIO * 100) + "%、" + SprengelPumpBlockEntity.LIMIT_PA + " Pa まで")), 0, 1, "電気なし");
        rec(a, ModBlocks.AIR_SEPARATION_UNIT, "空気分離", L(mol("空気", AirSeparationBlockEntity.AIR_MOL)),
            L(mol(ModItems.NITROGEN_GAS, AirSeparationBlockEntity.N2_PER_RUN / 1000.0), mol(ModItems.OXYGEN_GAS, AirSeparationBlockEntity.O2_PER_RUN / 1000.0), mol(ModItems.ARGON_GAS, AirSeparationBlockEntity.AR_PER_RUN / 1000.0)),
            AirSeparationBlockEntity.WATTS, AirSeparationBlockEntity.TICKS / 20.0, "端数は持ち越して袋（1 mol）にする");
        rec(a, ModBlocks.CRYSTAL_PULLER, "種結晶づくり", L(io(ModItems.TUNGSTEN_WIRE, 1), io(ModItems.REFINED_GERMANIUM, CrystalPullerBlockEntity.GE_PER_SEED), io(ModItems.HYDROGEN_GAS, CrystalPullerBlockEntity.H2_PER_RUN)),
            L(io(ModItems.GERMANIUM_SEED_CRYSTAL, 1)), CrystalPullerBlockEntity.WATTS, CrystalPullerBlockEntity.SEED_TICKS / 20.0, "粒の選択。線は結晶に埋まる");
        rec(a, ModBlocks.CRYSTAL_PULLER, "単結晶の引き上げ", L(kept(io(ModItems.GERMANIUM_SEED_CRYSTAL, 1)), io(ModItems.REFINED_GERMANIUM, CrystalPullerBlockEntity.GE_PER_INGOT), io(ModItems.HYDROGEN_GAS, CrystalPullerBlockEntity.H2_PER_RUN)),
            L(io(ModItems.GERMANIUM_SINGLE_CRYSTAL, 1)), CrystalPullerBlockEntity.WATTS, CrystalPullerBlockEntity.GROW_TICKS / 20.0, "純度と経路を受け継ぐ");
        rec(a, ModBlocks.ARC_FURNACE, "金属シリコン", L(io(ModItems.VEIN_QUARTZ, ArcFurnaceBlockEntity.QUARTZ_PER_RUN), alt(io(Items.CHARCOAL, ArcFurnaceBlockEntity.CARBON_PER_RUN), Items.COAL), perRuns(io(ModItems.GRAPHITE_ROD, 1), ArcFurnaceBlockEntity.RUNS_PER_ELECTRODE)),
            L(io(ModItems.METALLURGICAL_SILICON, 1)), ArcFurnaceBlockEntity.WATTS, ArcFurnaceBlockEntity.TICKS / 20.0, "SiO2 + 2C → Si + 2CO、純度98%");
        rec(a, ModBlocks.STEAM_ENGINE, "蒸気で発電", L(io(Items.COAL, 1), cond(io(Items.WATER_BUCKET, 1), "バケツ1杯で石炭2個分")), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.WATTS, com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.COAL_TICKS / 20.0, "石炭1個の熱は 30 MJ（ゲームの約束）。自励式ダイナモで2倍");
        rec(a, ModBlocks.STEAM_TURBINE, "蒸気タービンで発電", L(io(Items.COAL, 1), cond(io(Items.WATER_BUCKET, 1), "バケツ1杯で石炭2個分")), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity.WATTS, com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity.COAL_TICKS / 20.0, "熱効率30%");
        rec(a, ModBlocks.WATER_WHEEL, "水車で発電", L(kept(cond(io("横に流れる水", 1), "1面ごと"))), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.WaterWheelBlockEntity.WATTS_PER_FLOW, 0, "1面あたり。止まった水では回らない");
        rec(a, ModBlocks.LEAD_ACID_BATTERY, "電気をためる", L(io("電気", 1)), L(cond(io("電気", 1), (com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CAPACITY / 1_000_000) + " MJ まで、充電は "
            + com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CHARGE_EFFICIENCY_PERCENT + "% がたまる")), com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.MAX_WATTS, 0, "充電・放電とも最大の電力");
        exportExtraMachines(a);

        var root = new com.google.gson.JsonObject();
        root.addProperty("_note", "自動生成：GameTest exportMachineRecipes（tools/test.ps1 で更新）。各機械の定数とアイテムの登録から作る。watts が負＝発電、0＝電気なし。"
            + "量：count（1回あたり）・unit・per_runs（N回で count 個へる）・consumed=false（減らない）・max（1回の上限）・alt（代わり）・cond（条件）");
        root.add("recipes", a);
        java.nio.file.Path dir = java.nio.file.Paths.get("").toAbsolutePath();
        while (dir != null && !java.nio.file.Files.exists(dir.resolve("docs").resolve("TECH_TREE.md"))) dir = dir.getParent();
        h.assertTrue(dir != null, "project root (docs/TECH_TREE.md) not found from " + java.nio.file.Paths.get("").toAbsolutePath());
        try {
            java.nio.file.Files.writeString(dir.resolve("docs").resolve("machine_recipes.json"),
                    new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n", java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        // 量は全部数字の項目になっている（説明文の量がない）
        for (var r : a) for (String side : new String[] {"inputs", "outputs"})
            for (var x : r.getAsJsonObject().getAsJsonArray(side)) h.assertTrue(x.getAsJsonObject().get("count").isJsonPrimitive(), "every amount is a number");
        h.assertTrue(a.size() >= 25, "machine recipes: " + a.size());
        LOG.info("[LAB] machine recipes: {} entries written to docs/machine_recipes.json", a.size());
        h.succeed();
    }

    /** 後から足した機械（タスク22 [1-16][1-17] など）。exportMachineRecipes から呼ぶ */
    private static void exportExtraMachines(com.google.gson.JsonArray a) {
        rec(a, ModBlocks.HCL_BURNER, "乾いた塩化水素", L(mol(ModItems.HYDROGEN_GAS, HclBurnerBlockEntity.H2_PER), mol(ModItems.CHLORINE_GAS, HclBurnerBlockEntity.CL2_PER)),
            L(mol(ModItems.HYDROGEN_CHLORIDE_GAS, 2)), 0, HclBurnerBlockEntity.BURN_TICKS / 20.0, "水のスロットが空のとき。H2 + Cl2 → 2HCl を水に溶かさず袋に。電気なし");
        rec(a, ModBlocks.TRICHLOROSILANE_REACTOR, "合成", L(io(ModItems.METALLURGICAL_SILICON, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SI_PER_RUN), mol(ModItems.HYDROGEN_CHLORIDE_GAS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.HCL_PER_RUN)),
            L(io(ModItems.TRICHLOROSILANE, 1), mol(ModItems.HYDROGEN_GAS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.H2_PER_RUN)),
            com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_WATTS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_TICKS / 20.0, "Si + 3HCl → SiHCl3 + H2（約300℃）。できたては 99%");
        rec(a, ModBlocks.TRICHLOROSILANE_REACTOR, "蒸留（1段）", L(io(ModItems.TRICHLOROSILANE, 1)), L(cond(io(ModItems.TRICHLOROSILANE, 1), "「9」が1つ増える（最大 11N）")),
            com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_WATTS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_TICKS / 20.0, "沸点の差で B・P の塩化物を分ける");
        rec(a, ModBlocks.SIEMENS_REACTOR, "多結晶シリコン", L(cond(io(ModItems.TRICHLOROSILANE, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TCS_PER_RUN), "9N 以上に蒸留したもの"), mol(ModItems.HYDROGEN_GAS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.H2_PER_RUN)),
            L(cond(io(ModItems.POLYSILICON, 1), "純度はトリクロロシランと同じ（9N〜11N）"), mol(ModItems.HYDROGEN_CHLORIDE_GAS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.HCL_OUT)),
            com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS / 20.0, "SiHCl3 + H2 → Si + 3HCl（約1100℃の棒が太る）。HCl はトリクロロシラン作りに戻せる");
        rec(a, ModBlocks.CZ_PULLER, "種結晶", L(cond(io(ModItems.POLYSILICON, 1), "種のスロットに"), io(ModItems.QUARTZ_CRUCIBLE, 1), mol(ModItems.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_SEED)),
            L(io(ModItems.SILICON_SEED_CRYSTAL, 1)), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS / 20.0, "細く絞って引くと1つの粒だけが残る");
        rec(a, ModBlocks.CZ_PULLER, "単結晶", L(kept(io(ModItems.SILICON_SEED_CRYSTAL, 1)), io(ModItems.POLYSILICON, com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT), io(ModItems.QUARTZ_CRUCIBLE, 1), mol(ModItems.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_INGOT), cond(alt(io(ModItems.BORON_TRIOXIDE, 1), ModItems.PHOSPHORUS), "入れなくてもよい（真性）")),
            L(cond(io(ModItems.SILICON_SINGLE_CRYSTAL, 1), "B2O3 で p型・リンで n型、純度は多結晶シリコンと同じ")), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0, "約1420℃の融液から回しながら引き上げる。17.4kWh/kg");
    }

    // =====================================================================
    // [9-08] CZ 法（シリコン単結晶、タスク23）
    // =====================================================================

    /** 種：多結晶Si＋るつぼ＋Ar → 種結晶。単結晶：種（残る）＋多結晶Si 2＋るつぼ＋Ar 2＋B2O3/リン → インゴット（純度とドーパントを受け継ぐ）。るつぼ・Ar・電力が欠けると動かない */
    @GameTest
    public void czPuller(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModBlocks.CRYSTAL_PULLER.asItem(), ModItems.GRAPHITE_ROD, ModItems.STEEL_INGOT, ModItems.LEAD_SCREW, ModItems.ELECTRIC_MOTOR})
            h.assertTrue(recipeUses(h, "cz_puller", it), "CZ puller = bigger Ge puller [9-07] + graphite heater + steel chamber: " + it);
        h.assertTrue(recipeUses(h, "quartz_crucible", ModItems.FUSED_QUARTZ), "quartz crucible from fused quartz [7-02]");
        h.assertFalse(com.taku3.semiconductorlab.block.CzPullerBlockEntity.isCrucible(new ItemStack(ModItems.GRAPHITE_BOAT)), "graphite is not a crucible for Si (forms SiC)");
        double kwhPerKg = com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS * com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0 / 3.6e6 / com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT;
        h.assertTrue(Math.abs(kwhPerKg - com.taku3.semiconductorlab.block.CzPullerBlockEntity.KWH_PER_KG) < 0.05, "17.4 kWh/kg (NTU), got " + kwhPerKg);
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, ModBlocks.CZ_PULLER);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.CzPullerBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS);
        // 種結晶づくり：るつぼがないと動かない
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED, new ItemStack(ModItems.POLYSILICON, 1));
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR, new ItemStack(ModItems.ARGON_GAS, 8));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "no quartz crucible -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(ModItems.QUARTZ_CRUCIBLE, 3));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).is(ModItems.SILICON_SEED_CRYSTAL), "polysilicon tip -> seed crystal (necking)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE).getCount(), 2, "crucible is used once (cracks on cooling)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR).getCount(), 7, "1 Ar for the seed");
        // 単結晶（ホウ素 → p型）
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED, c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).copy());
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        ItemStack poly = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.polysilicon(com.taku3.semiconductorlab.Purity.ELEVEN_N);
        poly.setCount(4);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT, new ItemStack(ModItems.BORON_TRIOXIDE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "half power cannot keep 1420 C");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        ItemStack ing = c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT);
        h.assertTrue(ing.is(ModItems.SILICON_SINGLE_CRYSTAL) && ing.getCount() == 1, "2 polysilicon -> 1 ingot, got " + ing);
        h.assertValueEqual(ing.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "boron", "B2O3 -> p-type");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(ing.get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 11, "purity from the polysilicon");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED).is(ModItems.SILICON_SEED_CRYSTAL), "seed is reused");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT).isEmpty(), "dopant used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY).getCount(), 2, "2 polysilicon used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR).getCount(), 5, "2 Ar for the ingot");
        // リン → n型、ドーパントなし → 真性
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT, new ItemStack(ModItems.PHOSPHORUS, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "phosphorus", "P -> n-type");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly.copy());
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(ModItems.QUARTZ_CRUCIBLE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).is(ModItems.SILICON_SINGLE_CRYSTAL) && c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.DOPANT) == null, "no dopant -> intrinsic");
        // アルゴンがなければ動かない
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(ModItems.QUARTZ_CRUCIBLE, 1));
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR, ItemStack.EMPTY);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "no argon -> nothing");
        LOG.info("[LAB] CZ [9-08]: seed (necking), 2 poly + crucible + 2 Ar + B2O3/P -> p/n ingot, {} kWh/kg; no crucible / no Ar / half power rejected", kwhPerKg);
        h.succeed();
    }

    // =====================================================================
    // [9-11] ドーパント源（タスク23）
    // =====================================================================

    /** B2O3 → ホウ素（p型、k≈0.8）、白リン → リン（n型、k≈0.35）。ほかのアイテムはドーパントではない。ドーパントの印は保存と同期ができる */
    @GameTest
    public void dopantSources(GameTestHelper h) {
        var D = com.taku3.semiconductorlab.Dopant.class;
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.of(new ItemStack(ModItems.BORON_TRIOXIDE)), com.taku3.semiconductorlab.Dopant.BORON, "B2O3 is the boron source");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.of(new ItemStack(ModItems.PHOSPHORUS)), com.taku3.semiconductorlab.Dopant.PHOSPHORUS, "white phosphorus is the P source");
        h.assertTrue(com.taku3.semiconductorlab.Dopant.of(new ItemStack(ModItems.BORAX)) == null, "borax itself is not a dopant (make B2O3 first)");
        h.assertTrue(com.taku3.semiconductorlab.Dopant.of(new ItemStack(Items.REDSTONE)) == null, "no vanilla stand-in");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.BORON.type, 'p', "B (group III) -> p-type");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.PHOSPHORUS.type, 'n', "P (group V) -> n-type");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.Dopant.BORON.k - 0.8) < 1e-9 && Math.abs(com.taku3.semiconductorlab.Dopant.PHOSPHORUS.k - 0.35) < 1e-9, "segregation coefficients B 0.8, P 0.35");
        for (var d : com.taku3.semiconductorlab.Dopant.values()) h.assertValueEqual(com.taku3.semiconductorlab.Dopant.byId(d.id), d, "id round trip " + d);
        ItemStack s = new ItemStack(ModItems.POLYSILICON);
        s.set(com.taku3.semiconductorlab.ModDataComponents.DOPANT, "boron");
        h.assertValueEqual(s.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "boron", "dopant component stored");
        LOG.info("[LAB] dopants [9-11]: B2O3 -> B (p, k={}), P4 -> P (n, k={}); As/Sb wait for [1-04]/[1-43]", com.taku3.semiconductorlab.Dopant.BORON.k, com.taku3.semiconductorlab.Dopant.PHOSPHORUS.k);
        h.succeed();
    }

    // =====================================================================
    // [1-16] トリクロロシラン合成・蒸留（タスク22）
    // =====================================================================

    /** 金属シリコン1＋乾いた HCl 3 → 粗 TCS（99%）＋H2。蒸留1段で「9」が1つ増え、9段で 11N。塩酸（水溶液）は入らない */
    @GameTest
    public void trichlorosilaneReactor(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModItems.QUARTZ_TUBE, ModItems.STEEL_INGOT, ModBlocks.COPPER_CABLE.asItem()})
            h.assertTrue(recipeUses(h, "trichlorosilane_reactor", it), "reactor + column: " + it);
        h.assertFalse(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.isHcl(new ItemStack(ModItems.HYDROCHLORIC_ACID)), "hydrochloric acid (water) is not accepted: SiHCl3 reacts with water");
        // 純度：粗 2N → 蒸留9段で 11N、それ以上は上がらない
        double p = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.CRUDE;
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(p), 2, "crude TCS 99%");
        for (int i = 0; i < 9; i++) p = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.distilled(p);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(p), 11, "9 stages -> 11N");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.distilled(p)), 11, "11N is the ceiling");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.Purity.TEN_N), 10, "TEN_N reads as 10N (rounding fix)");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, ModBlocks.TRICHLOROSILANE_REACTOR);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.class);
        var c = be.getContainer();
        int SI = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_SI, HCL = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_HCL, D = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_DISTILL, OUT = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_OUT, H2 = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_H2;
        int TS = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_TICKS, TD = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_WATTS);
        c.setItem(SI, new ItemStack(ModItems.METALLURGICAL_SILICON, 2));
        c.setItem(HCL, new ItemStack(ModItems.HYDROGEN_CHLORIDE_GAS, 2));
        tickConsumer(h, fp, be, TS + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "2 HCl is not enough (Si + 3HCl)");
        c.setItem(HCL, new ItemStack(ModItems.HYDROGEN_CHLORIDE_GAS, 6));
        tickConsumer(h, fp, be, TS, feed);
        ItemStack t = c.getItem(OUT);
        h.assertTrue(t.is(ModItems.TRICHLOROSILANE) && t.getCount() == 1, "Si + 3HCl -> SiHCl3, got " + t);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.purityOf(t)), 2, "crude 99%");
        h.assertTrue(c.getItem(H2).is(ModItems.HYDROGEN_GAS) && c.getItem(H2).getCount() == 1, "+ H2");
        h.assertValueEqual(c.getItem(HCL).getCount(), 3, "3 HCl used");
        h.assertValueEqual(c.getItem(SI).getCount(), 1, "1 Si used");
        // 蒸留：左下に入れると、合成より先に蒸留する
        c.setItem(D, c.getItem(OUT).copy());
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, fp, be, TD, feed);
        h.assertTrue(c.getItem(D).isEmpty(), "distilled 1");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.purityOf(c.getItem(OUT))), 3, "1 stage -> 3N");
        h.assertValueEqual(c.getItem(SI).getCount(), 1, "synthesis waits while distilling");
        // 11N のものはもう蒸留しない
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(SI, ItemStack.EMPTY);
        c.setItem(D, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p));
        tickConsumer(h, fp, be, TD + 10, feed);
        h.assertValueEqual(c.getItem(D).getCount(), 1, "11N stays (ceiling)");
        LOG.info("[LAB] trichlorosilane: Si + 3HCl -> TCS 2N + H2 ({} s), each distillation stage +1N ({} s), 11N after 9 stages", TS / 20, TD / 20);
        h.succeed();
    }

    // =====================================================================
    // [1-17] シーメンス法（タスク22）
    // =====================================================================

    /** 9N 以上の TCS 1＋H2 1＋500kW×648秒（90kWh）→ 多結晶シリコン（純度は TCS と同じ）＋HCl 3（[1-16] にちょうど1回分戻る）。電力半分・8N では動かない */
    @GameTest
    public void siemensReactor(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModItems.GRAPHITE_ROD, ModItems.STEEL_INGOT, ModItems.QUARTZ_TUBE, ModBlocks.COPPER_CABLE.asItem()})
            h.assertTrue(recipeUses(h, "siemens_reactor", it), "steel bell jar + graphite electrode chucks + sight glass + heavy cable: " + it);
        double kwh = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS * com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS / 20.0 / 3.6e6;
        h.assertTrue(kwh >= 85 && kwh <= 95, "about 90 kWh per kg (US 8,398,946), got " + kwh);
        h.assertValueEqual(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.HCL_OUT, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.HCL_PER_RUN, "HCl out = HCl for one TCS synthesis (closed loop)");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, ModBlocks.SIEMENS_REACTOR);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.class);
        var c = be.getContainer();
        int T = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS);
        double p8 = 1 - 1e-8, p9 = com.taku3.semiconductorlab.Purity.NINE_N, p11 = com.taku3.semiconductorlab.Purity.ELEVEN_N;
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p8));
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, 4));
        tickConsumer(h, fp, be, T + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).isEmpty(), "8N TCS is not pure enough (distil to 9N)");
        h.assertFalse(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.pureEnough(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p8)), "8N rejected");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p9));
        tickConsumer(h, fp, be, T, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).isEmpty(), "half power cannot keep the rods at 1100 C");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p9));
        tickConsumer(h, fp, be, T, feed);
        ItemStack si = c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT);
        h.assertTrue(si.is(ModItems.POLYSILICON) && si.getCount() == 1, "SiHCl3 + H2 -> Si, got " + si);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(si.get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 9, "9N TCS -> 9N polysilicon");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_HCL).is(ModItems.HYDROGEN_CHLORIDE_GAS) && c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_HCL).getCount() == 3, "+ 3 HCl gas");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS).isEmpty(), "1 TCS used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_H2).getCount(), 3, "1 H2 used");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p11));
        tickConsumer(h, fp, be, T, feed);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 11, "11N TCS -> 11N polysilicon");
        LOG.info("[LAB] siemens: TCS (>=9N) + H2 + {} kWh -> polysilicon (same purity) + 3 HCl; 8N and half power rejected", kwh);
        h.succeed();
    }

    // =====================================================================
    // [3-15] アーク炉 → [1-15] 金属シリコン（タスク15）
    // =====================================================================

    /** 珪石2＋木炭（か石炭）1＋500kW×86.4秒（12kWh）→ 金属シリコン（98%）。電力が半分なら進まない。電極は8回で1本 */
    @GameTest
    public void arcFurnaceSilicon(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModItems.GRAPHITE_ROD, ModItems.REFRACTORY_BRICK, ModBlocks.COPPER_CABLE.asItem()})
            h.assertTrue(recipeUses(h, "arc_furnace", it), "arc furnace = graphite electrodes + refractory lining + heavy cable: " + it);
        var A = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.class;
        double kwh = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.WATTS * com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS / 20.0 / 3.6e6;
        h.assertTrue(kwh >= 10 && kwh <= 14, "about 12 kWh per kg of silicon (real: over 10 kWh), got " + kwh);
        floor(h, 5);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, ModBlocks.ARC_FURNACE);
        var be = h.getBlockEntity(fp, A);
        var c = be.getContainer();
        int Q = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_QUARTZ, CB = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_CARBON,
            E = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_ELECTRODE, OUT = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_OUT;
        int T = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.WATTS);
        h.assertTrue(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.isCarbon(new ItemStack(Items.COAL)), "coal also works (Q1515-2)");
        h.assertFalse(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.isCarbon(new ItemStack(Items.STICK)), "wood sticks are not carbon");
        c.setItem(Q, new ItemStack(ModItems.VEIN_QUARTZ, 20));
        c.setItem(CB, new ItemStack(Items.CHARCOAL, 10));
        tickConsumer(h, fp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no electrode -> no arc");
        c.setItem(E, new ItemStack(ModItems.GRAPHITE_ROD, 2));
        tickConsumer(h, fp, be, T, feed / 2);
        h.assertTrue(c.getItem(OUT).isEmpty(), "half power cannot hold 2000 C -> nothing");
        c.setItem(Q, new ItemStack(ModItems.VEIN_QUARTZ, 20));
        tickConsumer(h, fp, be, T, feed);
        ItemStack si = c.getItem(OUT);
        h.assertTrue(si.is(ModItems.METALLURGICAL_SILICON) && si.getCount() == 1, "2 quartz + 1 charcoal -> 1 silicon, got " + si);
        h.assertValueEqual(si.get(com.taku3.semiconductorlab.ModDataComponents.PURITY), com.taku3.semiconductorlab.Purity.METALLURGICAL, "metallurgical grade 98%");
        h.assertValueEqual(c.getItem(Q).getCount(), 18, "SiO2 + 2C: 2 quartz per run");
        h.assertValueEqual(c.getItem(E).getCount(), 1, "first run takes a fresh electrode");
        for (int i = 0; i < 7; i++) tickConsumer(h, fp, be, T, feed);
        h.assertValueEqual(c.getItem(E).getCount(), 1, "8 runs per electrode");
        h.assertValueEqual(c.getItem(OUT).getCount(), 8, "8 silicon");
        tickConsumer(h, fp, be, T, feed);
        h.assertTrue(c.getItem(E).isEmpty(), "the 9th run takes the second electrode");
        h.assertValueEqual(c.getItem(CB).getCount(), 1, "1 charcoal per run");
        LOG.info("[LAB] arc furnace [3-15] -> metallurgical Si [1-15]: 2 quartz + charcoal + {} kWh -> Si 98%, half power stalls, 8 runs per electrode", String.format("%.1f", kwh));
        h.succeed();
    }

    // =====================================================================
    // [0-12] 木炭（タスク15）：バニラの木炭＝乾留した木の炭
    // =====================================================================

    /** 原木→かまど→木炭（バニラ）。MOD の還元（GeO2）とリンづくりが木炭を炭素として使う。木炭には へえ と省略の注記がある */
    @GameTest
    public void charcoal(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.OAK_LOG).is(Items.CHARCOAL), "log -> furnace -> charcoal (dry distillation)");
        h.assertTrue(recipeUses(h, "raw_germanium", Items.CHARCOAL), "GeO2 + C -> Ge uses charcoal as the reductant");
        h.assertTrue(recipeUses(h, "phosphorus", Items.CHARCOAL), "Wohler's phosphorus uses charcoal");
        var ja = langJson("ja_jp");
        for (String s : new String[] {"note1", "note2", "note3", "note4", "placeholder"})
            h.assertTrue(ja.has("item.minecraft.charcoal." + s), "charcoal should have " + s);
        LOG.info("[LAB] charcoal [0-12]: log -> furnace -> charcoal; used by GeO2 reduction and phosphorus; notes present");
        h.succeed();
    }

    // =====================================================================
    // [9-07] Ge 単結晶の引き上げ（タスク14）
    // =====================================================================

    /** 種がない・水素がない・未精製では動かない。W線＋Ge1→種結晶（線は消える）、種結晶＋Ge4→単結晶（種は残る・純度と経路を受け継ぐ） */
    @GameTest
    public void crystalPuller(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModItems.HF_OSCILLATOR, ModItems.INDUCTION_COIL, ModItems.GRAPHITE, ModItems.QUARTZ_TUBE, ModItems.LEAD_SCREW})
            h.assertTrue(recipeUses(h, "crystal_puller", it), "puller = induction heating + graphite crucible + pulling screw: " + it);
        floor(h, 5);
        BlockPos pp = p(1, 1, 1);
        h.setBlock(pp, ModBlocks.CRYSTAL_PULLER);
        var be = h.getBlockEntity(pp, com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.class);
        var c = be.getContainer();
        int GE = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_GE, SEED = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_SEED,
            H2 = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_H2, OUT = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_OUT;
        int TS = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SEED_TICKS, TG = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.GROW_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.WATTS);
        ItemStack ge = new ItemStack(ModItems.REFINED_GERMANIUM, 9);
        ge.set(com.taku3.semiconductorlab.ModDataComponents.PURITY, com.taku3.semiconductorlab.Purity.TEN_N);
        ge.set(com.taku3.semiconductorlab.ModDataComponents.CHLORIDE_ROUTE, true);
        h.assertFalse(com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.isGermanium(new ItemStack(ModItems.RAW_GERMANIUM)), "unrefined germanium is not accepted");
        c.setItem(GE, ge);
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_GAS, 2));
        tickConsumer(h, pp, be, TG + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no seed -> nothing grows");
        // ① 最初の種：タングステン線から（粒の選択）
        c.setItem(SEED, new ItemStack(ModItems.TUNGSTEN_WIRE, 1));
        tickConsumer(h, pp, be, TS, feed);
        h.assertTrue(c.getItem(OUT).is(ModItems.GERMANIUM_SEED_CRYSTAL), "W wire + 1 Ge -> seed crystal, got " + c.getItem(OUT));
        h.assertTrue(c.getItem(SEED).isEmpty(), "the wire is grown into the crystal");
        h.assertValueEqual(c.getItem(GE).getCount(), 8, "1 Ge used for the seed");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "1 bag of hydrogen per run");
        // ② 単結晶：種は戻る
        c.setItem(SEED, c.getItem(OUT).copy());
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, pp, be, TG, feed);
        ItemStack ingot = c.getItem(OUT);
        h.assertTrue(ingot.is(ModItems.GERMANIUM_SINGLE_CRYSTAL), "seed + 4 Ge -> single crystal, got " + ingot);
        h.assertValueEqual(ingot.get(com.taku3.semiconductorlab.ModDataComponents.PURITY), com.taku3.semiconductorlab.Purity.TEN_N, "purity carried over");
        h.assertTrue(Boolean.TRUE.equals(ingot.get(com.taku3.semiconductorlab.ModDataComponents.CHLORIDE_ROUTE)), "GeCl4 route carried over");
        h.assertTrue(c.getItem(SEED).is(ModItems.GERMANIUM_SEED_CRYSTAL), "the seed comes back");
        h.assertValueEqual(c.getItem(GE).getCount(), 4, "4 Ge per ingot");
        h.assertTrue(c.getItem(H2).isEmpty(), "hydrogen used up");
        // 水素がなければ止まる（酸化を防ぐ雰囲気）
        tickConsumer(h, pp, be, TG + 10, feed);
        h.assertValueEqual(ingot.getCount(), 1, "no hydrogen -> no second ingot");
        LOG.info("[LAB] crystal puller: W wire + Ge -> seed ({} ticks), seed + 4 Ge + H2 -> single crystal ({} ticks, 10N + route kept, seed returned), no H2 -> stop", TS, TG);
        h.succeed();
    }

    // =====================================================================
    // [6-07] 空気分離（タスク12）
    // =====================================================================

    /** 空気10mol/回：N2 7.808・O2 2.095・Ar 0.093 mol を mmol で持ち越して袋にする。電気なしでは動かない。隣のホルダーが N2 を吸い、圧縮機で窒素ボンベ */
    @GameTest(maxTicks = 100)
    public void airSeparation(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "air_separation_unit", ModBlocks.COMPRESSOR.asItem()), "Linde's machine starts with a compressor [6-03]");
        floor(h, 7);
        BlockPos ap = p(1, 1, 1), hp = p(2, 1, 1), cp = p(3, 1, 1);
        h.setBlock(ap, ModBlocks.AIR_SEPARATION_UNIT);
        var asu = h.getBlockEntity(ap, com.taku3.semiconductorlab.block.AirSeparationBlockEntity.class);
        var box = asu.getContainer();
        int T = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.TICKS;
        int N2 = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_N2, O2 = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_O2, AR = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_AR;
        tickConsumer(h, ap, asu, T + 10, 0);
        h.assertTrue(box.getItem(N2).isEmpty(), "no power -> no separation");
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirSeparationBlockEntity.WATTS);
        tickConsumer(h, ap, asu, T, feed);
        h.assertValueEqual(box.getItem(N2).getCount(), 7, "run 1: 7.808 mol N2 -> 7 bags");
        h.assertValueEqual(box.getItem(O2).getCount(), 2, "run 1: 2.095 mol O2 -> 2 bags");
        h.assertTrue(box.getItem(AR).isEmpty(), "run 1: 0.093 mol Ar -> no bag yet");
        int n2 = 0, o2 = 0;
        for (int run = 1; run <= 11; run++) {
            if (run > 1) tickConsumer(h, ap, asu, T, feed);
            n2 += box.getItem(N2).getCount();
            o2 += box.getItem(O2).getCount();
            box.setItem(N2, ItemStack.EMPTY); box.setItem(O2, ItemStack.EMPTY);
            if (run == 10) h.assertTrue(box.getItem(AR).isEmpty(), "10 runs = 100 mol air = 0.93 mol Ar -> still no bag");
        }
        h.assertValueEqual(n2, 85, "11 runs: 85.888 mol N2 -> 85 bags");
        h.assertValueEqual(o2, 23, "11 runs: 23.045 mol O2 -> 23 bags");
        h.assertTrue(box.getItem(AR).is(ModItems.ARGON_GAS) && box.getItem(AR).getCount() == 1, "11 runs = 110 mol air -> 1 bag of argon, got " + box.getItem(AR));
        h.assertValueEqual(asu.argonRemainderMmol(), 23, "argon carry-over 1023 - 1000 mmol");
        box.setItem(N2, new ItemStack(ModItems.NITROGEN_GAS, 5));
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        h.setBlock(cp, ModBlocks.COMPRESSOR);
        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            int got = g.getAmount() + g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertTrue(got >= 5, "the holder pulls nitrogen from the separator, got " + got);
            g.setContentsForTest(GasType.NITROGEN, 60);
            CompressorBlockEntity comp = h.getBlockEntity(cp, CompressorBlockEntity.class);
            comp.getContainer().setItem(CompressorBlockEntity.SLOT_EMPTY, new ItemStack(ModItems.EMPTY_GAS_CYLINDER, 1));
            tickConsumer(h, cp, comp, CompressorBlockEntity.TOTAL_TICKS, EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS));
            h.assertTrue(comp.getContainer().getItem(CompressorBlockEntity.SLOT_OUT).is(ModItems.NITROGEN_CYLINDER), "compressor -> nitrogen cylinder");
            h.assertTrue(!GasType.explosive(GasType.NITROGEN, GasType.HYDROGEN) && !GasType.explosive(GasType.ARGON, GasType.OXYGEN), "N2 and Ar react with nothing");
            h.assertTrue(GasType.ARGON.cylinderItem() == ModItems.ARGON_CYLINDER, "argon has a cylinder");
            LOG.info("[LAB] air separation: 11 runs (110 mol air) -> N2 {} / O2 {} / Ar 1 bags, holder pulls N2, compressor -> N2 cylinder", 85, 23);
            h.succeed();
        });
    }

    // =====================================================================
    // [1-45] 水銀 ＋ [5-10] 水銀真空計
    // =====================================================================

    private static String keyOf(net.minecraft.network.chat.Component c) {
        return c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t ? t.getKey() : "";
    }

    /** 辰砂→粉→（かまど）→水銀。真空計は 1気圧で 760mm、1mm 未満は「読めない」。容器に当てると SUCCESS、石には PASS */
    @GameTest
    public void mercuryGauge(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cinnabar_dust", Items.CINNABAR), "cinnabar dust is crushed from the vanilla cinnabar block");
        ItemStack hg = cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.CINNABAR_DUST);
        h.assertTrue(hg.is(ModItems.MERCURY), "roasting cinnabar dust should give mercury, got " + hg);
        h.assertTrue(recipeUses(h, "mercury_vacuum_gauge", ModItems.MERCURY), "the gauge needs mercury");
        double atm = MercuryVacuumGaugeItem.mmHg(VacuumChamberBlockEntity.ATM);
        h.assertTrue(Math.abs(atm - 760) < 0.5, "1 atm should read 760 mmHg, got " + atm);
        h.assertTrue(keyOf(MercuryVacuumGaugeItem.reading(1000)).endsWith(".reading"), "1 kPa (7.5 mm) is readable");
        h.assertTrue(keyOf(MercuryVacuumGaugeItem.reading(100)).endsWith(".below"), "100 Pa (0.75 mm) is below what the eye can read");
        floor(h, 5);
        BlockPos cp = p(1, 1, 1);
        h.setBlock(cp, ModBlocks.VACUUM_CHAMBER);
        ItemStack gauge = new ItemStack(ModItems.MERCURY_VACUUM_GAUGE);
        java.util.function.Function<BlockPos, net.minecraft.world.InteractionResult> use = rel -> {
            BlockPos a = h.absolutePos(rel);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false);
            return gauge.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(h.getLevel(), null, net.minecraft.world.InteractionHand.MAIN_HAND, gauge, hit));
        };
        h.assertTrue(use.apply(cp) instanceof net.minecraft.world.InteractionResult.Success, "gauge on a chamber should succeed");
        h.assertTrue(use.apply(p(3, 0, 3)) == net.minecraft.world.InteractionResult.PASS, "gauge on stone should pass");
        LOG.info("[LAB] mercury: cinnabar -> dust -> furnace -> mercury; gauge 1 atm = {} mmHg, 1 kPa readable, 100 Pa below 1 mm", String.format("%.1f", atm));
        h.succeed();
    }

    // =====================================================================
    // [5-02] 真空管（封止台）
    // =====================================================================

    /** 容器が 0.1Pa 以下のときだけ封じる（手押しポンプの 1kPa では不可）。1本封じると容器に空気がもどり、続けては封じられない。容器なしでは動かない */
    @GameTest
    public void sealingStationNeedsVacuum(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "vacuum_tube_assembly", ModItems.TUNGSTEN_WIRE), "the filament is tungsten wire [1-28]");
        double back = SealingStationBlockEntity.decodePa(SealingStationBlockEntity.encodePa(1234));
        h.assertTrue(Math.abs(back - 1234) < 5, "pressure sync encoding round-trip, got " + back);
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, ModBlocks.VACUUM_CHAMBER);
        h.setBlock(sp, ModBlocks.SEALING_STATION);
        h.setBlock(lonely, ModBlocks.SEALING_STATION);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        SealingStationBlockEntity be = h.getBlockEntity(sp, SealingStationBlockEntity.class);
        var box = be.getContainer();
        int IN = SealingStationBlockEntity.SLOT_IN, OUT = SealingStationBlockEntity.SLOT_OUT, T = SealingStationBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(SealingStationBlockEntity.WATTS);
        box.setItem(IN, new ItemStack(ModItems.VACUUM_TUBE_ASSEMBLY, 2));
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertTrue(box.getItem(OUT).isEmpty(), "at 1 atm nothing may be sealed");
        c.setPressure(HandVacuumPumpBlock.LIMIT_PA);
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertTrue(box.getItem(OUT).isEmpty(), "the hand pump's 1 kPa is not enough to seal a tube");
        c.setPressure(0.08);
        tickConsumer(h, sp, be, T, feed);
        h.assertTrue(box.getItem(OUT).is(ModItems.VACUUM_TUBE) && box.getItem(OUT).getCount() == 1, "at 0.08 Pa one tube should be sealed in " + T + " ticks, got " + box.getItem(OUT));
        h.assertValueEqual(box.getItem(IN).getCount(), 1, "one assembly used");
        double after = c.getPressure();
        h.assertTrue(after > SealingStationBlockEntity.SEAL_MAX_PA, "sealing lets air back into the chamber: " + after);
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertValueEqual(box.getItem(OUT).getCount(), 1, "no second tube until pumped down again");
        SealingStationBlockEntity alone = h.getBlockEntity(lonely, SealingStationBlockEntity.class);
        alone.getContainer().setItem(IN, new ItemStack(ModItems.VACUUM_TUBE_ASSEMBLY));
        tickConsumer(h, lonely, alone, T + 20, feed);
        h.assertTrue(alone.getContainer().getItem(OUT).isEmpty(), "no chamber next to it -> no sealing");
        LOG.info("[LAB] sealing station: 1 atm / 1 kPa -> wait, 0.08 Pa -> tube in {} ticks, chamber back to {}, no chamber -> wait", T, VacuumChamberBlockEntity.formatPa(after));
        h.succeed();
    }

    // =====================================================================
    // [4-09] 高周波発振器（第1章の仮①「高周波電源」を置換）
    // =====================================================================

    /** 発振器＝真空管2＋コイル＋ライデン瓶＋ケーブル。帯域精製炉は発振器を要求し、旧・仮アイテムは消えている */
    @GameTest
    public void hfOscillatorReplacesPlaceholder(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("hf_oscillator"));
        var ings = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value().placementInfo().ingredients();
        long tubes = ings.stream().filter(i -> i.test(new ItemStack(ModItems.VACUUM_TUBE))).count();
        h.assertValueEqual(tubes, 2L, "vacuum tubes in the oscillator");
        for (var it : new net.minecraft.world.item.Item[] {ModItems.LEYDEN_JAR, ModItems.INDUCTION_COIL, ModBlocks.COPPER_CABLE.asItem()})
            h.assertTrue(recipeUses(h, "hf_oscillator", it), "oscillator should use " + it);
        h.assertTrue(recipeUses(h, "leyden_jar", ModItems.SODA_LIME_GLASS), "Leyden jar = glass jar (soda-lime glass) + metal foil");
        h.assertTrue(recipeUses(h, "zone_refining_furnace", ModItems.HF_OSCILLATOR), "zone refining furnace needs the HF oscillator");
        h.assertFalse(BuiltInRegistries.ITEM.containsKey(SemiconductorLab.id("hf_power_supply")), "placeholder hf_power_supply must be gone");
        h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("hf_power_supply"))).isEmpty(), "placeholder recipe must be gone");
        LOG.info("[LAB] HF oscillator: 2 tubes + coil + Leyden jar + cable; zone refining furnace uses it; hf_power_supply removed");
        h.succeed();
    }
    /** [3-14] 誘導加熱炉＝帯域精製炉＋高周波発振器（タスク12で ✅）：誘導コイル＋発振器＋電気を通さない石英管、燃料スロットなし・電力 3kW だけ */
    @GameTest
    public void inductionHeatingIsZoneRefiner(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {ModItems.INDUCTION_COIL, ModItems.HF_OSCILLATOR, ModItems.QUARTZ_TUBE})
            h.assertTrue(recipeUses(h, "zone_refining_furnace", it), "induction heating needs " + it);
        h.assertTrue(recipeUses(h, "induction_coil", Items.COPPER_INGOT), "the coil is copper [4-03]");
        h.assertValueEqual(ZoneRefiningFurnaceBlockEntity.WATTS, 3000.0, "the HF oscillator draws 3 kW");
        h.assertValueEqual(ZoneRefiningFurnaceBlockEntity.SLOT_COUNT, 3, "germanium, dopant, boat - no fuel slot (heat comes from eddy currents)");
        LOG.info("[LAB] induction heating [3-14]: zone refiner = copper coil + HF oscillator + quartz tube, 3 kW, no fuel");
        h.succeed();
    }


    // =====================================================================
    // [4-12] 電気炉（黒鉛棒の抵抗加熱、約1500℃）
    // =====================================================================

    /** れんが・ボートの成形品を電気で焼く。電気なしでは焼けない。水素炉は電気炉から作る */
    @GameTest
    public void electricFurnace(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ELECTRIC_FURNACE);
        ElectricFurnaceBlockEntity be = h.getBlockEntity(pos, ElectricFurnaceBlockEntity.class);
        var c = be.getContainer();
        int IN = ElectricFurnaceBlockEntity.SLOT_IN, OUT = ElectricFurnaceBlockEntity.SLOT_OUT, T = ElectricFurnaceBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(ElectricFurnaceBlockEntity.WATTS);
        c.setItem(IN, new ItemStack(ModItems.REFRACTORY_BRICK_BLANK, 2));
        tickConsumer(h, pos, be, T + 20, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> nothing fired");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        tickConsumer(h, pos, be, T - 1, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not done 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(OUT).is(ModItems.REFRACTORY_BRICK), "blank -> refractory brick at ~1500 C");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "1 blank used");
        c.setItem(IN, new ItemStack(ModItems.GRAPHITE_BOAT_BLANK));
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, pos, be, T, feed);
        h.assertTrue(c.getItem(OUT).is(ModItems.GRAPHITE_BOAT), "clay-graphite blank -> graphite boat");
        h.assertFalse(ElectricFurnaceBlockEntity.isInput(new ItemStack(Items.RAW_IRON)), "not a general smelter");
        h.assertTrue(recipeUses(h, "electric_furnace", ModItems.GRAPHITE_ROD) && recipeUses(h, "electric_furnace", ModItems.REFRACTORY_BRICK), "electric furnace = refractory brick + graphite rods");
        h.assertTrue(recipeUses(h, "hydrogen_furnace", ModBlocks.ELECTRIC_FURNACE.asItem()), "hydrogen furnace is built from the electric furnace");
        LOG.info("[LAB] electric furnace: blank -> brick / boat in {} ticks at 5 kW, nothing without power; hydrogen furnace built on it", T);
        h.succeed();
    }

    // =====================================================================
    // [3-10] アチソン炉（炭素＋砂に直接通電、2500℃以上で人造黒鉛）
    // =====================================================================

    /** 電力不足では温度が届かず黒鉛にならない。炭素がないと電流が流れない。500kW を直接入れると昇温→黒鉛化 */
    @GameTest
    public void achesonFurnace(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ACHESON_FURNACE);
        AchesonFurnaceBlockEntity be = h.getBlockEntity(pos, AchesonFurnaceBlockEntity.class);
        var c = be.getContainer();
        int C = AchesonFurnaceBlockEntity.SLOT_CARBON, S = AchesonFurnaceBlockEntity.SLOT_SAND, OUT = AchesonFurnaceBlockEntity.SLOT_OUT;
        long full = EnergyStorage.wattsToJoulesPerTick(AchesonFurnaceBlockEntity.REQUIRED_WATTS);
        long steamEngine = EnergyStorage.wattsToJoulesPerTick(SteamEngineBlockEntity.WATTS);

        // 炭素なし：電気が来ても通り道がないので流れない（使わない）
        c.setItem(S, new ItemStack(Items.SAND, 2));
        tickConsumer(h, pos, be, 40, full);
        h.assertValueEqual(be.getLastWatts(), 0.0, "no carbon -> no current");
        h.assertTrue(be.getTemperature() < 21, "no carbon -> stays cold");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 蒸気機関1台分（50kW）：何分たっても 2500℃ に届かない（約280℃で落ち着く）
        c.setItem(C, new ItemStack(Items.COAL, 8));
        tickConsumer(h, pos, be, 20 * 120, steamEngine);
        double t50 = be.getTemperature();
        h.assertTrue(t50 > 200 && t50 < 400, "50 kW should settle near 280 C, got " + t50);
        h.assertTrue(c.getItem(OUT).isEmpty(), "underpowered -> no graphite");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);

        // 500kW を直接入れる：約47秒で 2500℃、そこから60秒で人造黒鉛2
        int heat = 0;
        while (be.getTemperature() < AchesonFurnaceBlockEntity.GRAPHITIZE_C && heat < 20 * 120) { tickConsumer(h, pos, be, 1, full); heat++; }
        h.assertTrue(be.getTemperature() >= AchesonFurnaceBlockEntity.GRAPHITIZE_C, "500 kW should reach 2500 C within 2 min");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        h.assertTrue(c.getItem(OUT).isEmpty(), "graphitizing has only just started");
        tickConsumer(h, pos, be, AchesonFurnaceBlockEntity.WORK_TICKS, full);
        h.assertTrue(c.getItem(OUT).is(ModItems.SYNTHETIC_GRAPHITE), "carbon + sand at 2500 C -> synthetic graphite");
        h.assertValueEqual(c.getItem(OUT).getCount(), AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH, "graphite per batch");
        h.assertValueEqual(c.getItem(C).getCount(), 4, "4 carbon used");
        h.assertValueEqual(c.getItem(S).getCount(), 1, "1 sand used");

        // 電気を止めると冷えて、黒鉛化も止まる
        tickConsumer(h, pos, be, 20 * 60, 0);
        h.assertTrue(be.getTemperature() < 500, "cools down without power, got " + be.getTemperature());
        h.assertValueEqual(c.getItem(OUT).getCount(), AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH, "no more graphite while cold");
        h.assertTrue(recipeUses(h, "acheson_furnace", ModItems.GRAPHITE_ROD), "electrodes are graphite rods");
        LOG.info("[LAB] acheson: no carbon -> 0 W; 50 kW -> {} C (no graphite); 500 kW -> 2500 C in {} ticks, 4 C + sand -> 2 synthetic graphite", Math.round(t50), heat);
        h.succeed();
    }

    // =====================================================================
    // 帯域精製炉の電力駆動（ROADMAP #9）とボート（粘土黒鉛＝9N、人造黒鉛＝10N）
    // =====================================================================

    /** 電気がないと止まる。ボートがないと動かない。粘土黒鉛ボートは 9N 止まり、人造黒鉛ボートに替えると 10N */
    @GameTest
    public void zoneRefinerPowerAndBoat(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ZONE_REFINING_FURNACE);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT, B = ZoneRefiningFurnaceBlockEntity.BOAT_SLOT;

        // ボートなし：電気があっても動かない（電気も使わない）
        c.setItem(G, rawChloride(2));   // 10N まで確かめるので GeCl4 経由の Ge
        tickRefiner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(G).is(ModItems.RAW_GERMANIUM), "no boat -> no refining");
        h.assertValueEqual(be.getLastWatts(), 0.0, "no boat -> no power drawn");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 粘土黒鉛ボート＋電気なし：止まる
        c.setItem(B, new ItemStack(ModItems.GRAPHITE_BOAT));
        tickConsumer(h, pos, be, T + 10, 0);
        h.assertTrue(c.getItem(G).is(ModItems.RAW_GERMANIUM), "no power -> furnace stops");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 電気あり：6N → 9N、そこで止まる（粘土の不純物）
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "clay-graphite boat: raw -> 6N -> 9N");
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "clay-graphite boat stops at 9N");
        h.assertValueEqual(be.getLastWatts(), 0.0, "idle at the cap -> no power drawn");
        // 人造黒鉛ボートに替える → 10N
        c.setItem(B, new ItemStack(ModItems.SYNTHETIC_GRAPHITE_BOAT));
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "synthetic graphite boat -> 10N");
        h.assertTrue(c.getItem(B).is(ModItems.SYNTHETIC_GRAPHITE_BOAT), "boat stays");
        // 途中で電気が切れると一時停止（進捗は残る）、戻ると続きから
        c.setItem(G, new ItemStack(ModItems.RAW_GERMANIUM, 1));
        tickRefiner(h, pos, be, T / 2);
        tickConsumer(h, pos, be, T, 0);
        h.assertTrue(c.getItem(G).is(ModItems.RAW_GERMANIUM), "paused while power is off");
        tickRefiner(h, pos, be, T - T / 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.SIX_N, "resumes when power returns");
        h.assertTrue(recipeUses(h, "synthetic_graphite_boat", ModItems.SYNTHETIC_GRAPHITE), "synthetic boat is made from Acheson graphite");
        h.assertFalse(recipeUses(h, "zone_refining_furnace", ModItems.GRAPHITE_BOAT), "the boat now goes in its own slot, not in the furnace recipe");
        LOG.info("[LAB] zone refiner: needs boat + {} W; clay-graphite boat caps at 9N, synthetic graphite boat reaches 10N; pauses without power", ZoneRefiningFurnaceBlockEntity.WATTS);
        h.succeed();
    }

    // =====================================================================
    // [7-02] 酸水素バーナー：ネザークォーツ ＋ H2/O2 ボンベ → 溶融石英（かまどでは溶けない）
    // =====================================================================

    private static void tickBurner(GameTestHelper h, BlockPos rel, OxyhydrogenBurnerBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) OxyhydrogenBurnerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    @GameTest
    public void oxyhydrogenFusedQuartz(GameTestHelper h) {
        int T = OxyhydrogenBurnerBlockEntity.MELT_TICKS;
        int IN = OxyhydrogenBurnerBlockEntity.SLOT_IN, H2 = OxyhydrogenBurnerBlockEntity.SLOT_H2, O2 = OxyhydrogenBurnerBlockEntity.SLOT_O2;
        int OUT = OxyhydrogenBurnerBlockEntity.SLOT_OUT, EMPTY = OxyhydrogenBurnerBlockEntity.SLOT_EMPTY;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.OXYHYDROGEN_BURNER);
        OxyhydrogenBurnerBlockEntity be = h.getBlockEntity(pos, OxyhydrogenBurnerBlockEntity.class);
        var c = be.getContainer();

        // かまどではもう溶けない（仮レシピ #19 は削除）
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.VEIN_QUARTZ).isEmpty(), "quartz must not melt in a furnace (~1000 C < 1713 C)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, ModItems.VEIN_QUARTZ).isEmpty(), "nor in a blast furnace");
        // タスク13：入力は珪石 [1-46]。ネザークォーツ（現実に無い素材）は受け付けない
        h.assertTrue(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(ModItems.VEIN_QUARTZ)), "the burner melts vein quartz");
        h.assertFalse(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(Items.QUARTZ)), "nether quartz is not accepted (chemistry rule 1)");

        // ボンベなし：動かない
        c.setItem(IN, new ItemStack(ModItems.VEIN_QUARTZ, 30));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no cylinders -> no melting");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 水素だけ：動かない（酸素がないと燃えない）。水素ボンベはつながない（無駄に開けない）
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "hydrogen only -> no flame");
        // 酸素だけ：動かない
        c.setItem(H2, ItemStack.EMPTY);
        c.setItem(O2, new ItemStack(ModItems.OXYGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "oxygen only -> no flame");

        // 両方：10秒で溶融石英1個、空ボンベ2本が出る
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_CYLINDER, 2));
        tickBurner(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickBurner(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(ModItems.FUSED_QUARTZ), "quartz -> fused quartz");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 2, "one H2 and one O2 cylinder connected -> 2 empties");
        h.assertValueEqual(be.getH2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - OxyhydrogenBurnerBlockEntity.H2_PER, "H2 46 mol left");
        h.assertValueEqual(be.getO2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - OxyhydrogenBurnerBlockEntity.O2_PER, "O2 48 mol left");
        // 水素ボンベ1本＝12個まで（50/4）、13個目で2本目をつなぐ
        tickBurner(h, pos, be, T * 11);
        h.assertValueEqual(c.getItem(OUT).getCount(), 12, "12 fused quartz from the first H2 cylinder");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "2nd H2 cylinder still unopened");
        tickBurner(h, pos, be, T);
        h.assertValueEqual(c.getItem(OUT).getCount(), 13, "13th quartz");
        h.assertTrue(c.getItem(H2).isEmpty(), "2nd H2 cylinder connected");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 3, "3 empty cylinders");
        h.assertValueEqual(be.getO2(), OxyhydrogenBurnerBlockEntity.MOL_PER_CYLINDER - 13 * OxyhydrogenBurnerBlockEntity.O2_PER, "O2 used at 2 mol each (2H2 + O2)");
        // 溶融石英 → 石英管のルートは残る
        h.assertTrue(recipeUses(h, "quartz_tube", ModItems.FUSED_QUARTZ), "fused quartz -> quartz tube");
        LOG.info("[LAB] oxyhydrogen burner: no gas -> 0, H2+O2 -> fused quartz in {} ticks, 12.5 per H2 cylinder", T);
        h.succeed();
    }

    /** [2-14] ソーダ石灰ガラス：砂6＋ソーダ＋生石灰 → 調合原料6 → かまど/溶鉱炉 → ガラス。実験器具（＋電解槽・真空管の組立品・鉛蓄電池：Q10-C3）はバニラのガラスでなく MOD ガラス */
    @GameTest
    public void sodaLimeGlass(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("glass_batch"));
        var r = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Items.SAND))).count(), 6L, "6 sand (SiO2 ~72%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(ModItems.SODIUM_CARBONATE))).count(), 1L, "1 soda (Na2O ~14%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(ModItems.QUICKLIME))).count(), 1L, "1 quicklime (CaO ~10%)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.GLASS_BATCH).is(ModItems.SODA_LIME_GLASS), "batch -> furnace -> soda-lime glass");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, ModItems.GLASS_BATCH).is(ModItems.SODA_LIME_GLASS), "batch -> blast furnace -> soda-lime glass");
        for (String id : new String[] {"vacuum_chamber", "mercury_vacuum_gauge", "leyden_jar", "electrolyzer", "vacuum_tube_assembly", "lead_acid_battery", "sealing_station", "sprengel_pump"}) {
            h.assertTrue(recipeUses(h, id, ModItems.SODA_LIME_GLASS), id + " should use soda-lime glass");
            for (var v : new net.minecraft.world.item.Item[] {Items.GLASS, Items.GLASS_PANE, Items.GLASS_BOTTLE})
                h.assertFalse(recipeUses(h, id, v), id + " must not use vanilla " + v);
        }
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.SAND).is(Items.GLASS), "vanilla sand -> glass stays for windows");
        LOG.info("[LAB] soda-lime glass: 6 sand + soda + lime -> 6 batch -> glass; vacuum chamber / gauge / Leyden jar / electrolyzer / tube assembly / lead-acid battery need it");
        h.succeed();
    }

    @GameTest
    public void advancementsAndFunctions(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String a : CHAPTER1) if (server.getAdvancements().get(SemiconductorLab.id("chapter1/" + a)) == null) missing.add("advancement chapter1/" + a);
        for (String a : GUIDE_ADV) if (server.getAdvancements().get(SemiconductorLab.id("guide/" + a)) == null) missing.add("advancement guide/" + a);
        for (String f : GUIDE_FN) if (server.getFunctions().get(SemiconductorLab.id(f)).isEmpty()) missing.add("function " + f);
        long ch1 = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("chapter1/")).count();
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        h.assertValueEqual(ch1, 11L, "number of chapter1 advancements");
        LOG.info("[LAB] advancements: chapter1 = {}, guide = {}, functions = {} all registered", ch1, GUIDE_ADV.length, GUIDE_FN.length);
        h.succeed();
    }

    // =====================================================================
    // タスク10：ガイド第2巻「電気」（guide2/）・図鑑タブ（zukan/）・へえ①〜④
    // =====================================================================

    private static final String[] GUIDE2_ADV = {"root", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10"};

    /** guide2/ の11個と give_guidebook2 が登録され、ルートの報酬が本を渡す関数になっている */
    @GameTest
    public void guide2Electricity(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        List<String> missing = new ArrayList<>();
        for (String a : GUIDE2_ADV) if (server.getAdvancements().get(SemiconductorLab.id("guide2/" + a)) == null) missing.add("guide2/" + a);
        if (server.getFunctions().get(SemiconductorLab.id("give_guidebook2")).isEmpty()) missing.add("function give_guidebook2");
        h.assertTrue(missing.isEmpty(), "not registered: " + missing);
        long n = server.getAdvancements().getAllAdvancements().stream()
                .filter(x -> x.id().getNamespace().equals(SemiconductorLab.MOD_ID) && x.id().getPath().startsWith("guide2/")).count();
        h.assertValueEqual(n, (long) GUIDE2_ADV.length, "number of guide2 advancements");
        LOG.info("[LAB] guide2 (electricity): {} advancements + give_guidebook2 registered", n);
        h.succeed();
    }

    private static com.google.gson.JsonObject langJson(String code) {
        try (var in = SemiconductorLab.class.getResourceAsStream("/assets/" + SemiconductorLab.MOD_ID + "/lang/" + code + ".json")) {
            return com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** MOD の全アイテムに へえ①〜④（en/ja）があり、図鑑タブ zukan/<id> が全部ある */
    @GameTest
    public void notesAndZukanComplete(GameTestHelper h) {
        MinecraftServer server = h.getLevel().getServer();
        var en = langJson("en_us");
        var ja = langJson("ja_jp");
        List<String> missing = new ArrayList<>();
        int items = 0;
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (!key.getNamespace().equals(SemiconductorLab.MOD_ID)) continue;
            items++;
            String base = (en.has("item." + key.getNamespace() + "." + key.getPath()) ? "item." : "block.") + key.getNamespace() + "." + key.getPath();
            for (var lang : List.of(en, ja)) {
                for (String s : new String[] {"note1", "note3", "note4"}) if (!lang.has(base + "." + s)) missing.add(base + "." + s);
                if (!lang.has(base + ".note2") && !lang.has(base + ".note")) missing.add(base + ".note2");
            }
            if (server.getAdvancements().get(SemiconductorLab.id("zukan/" + key.getPath())) == null) missing.add("zukan/" + key.getPath());
        }
        h.assertTrue(missing.isEmpty(), "missing (" + missing.size() + "): " + missing);
        LOG.info("[LAB] notes: all {} items have note1-4 in en/ja and a zukan entry", items);
        h.succeed();
    }

    // =====================================================================
    // タスク09：塩素の鎖 [6-04][0-16] → [6-05] → [6-06] → WO3 本物化・[1-34] GeCl4
    // =====================================================================

    @GameTest public void oreRockSalt(GameTestHelper h) { oreFeature(h, "rock_salt_ore", Blocks.STONE, Blocks.DEEPSLATE, ModBlocks.ROCK_SALT_ORE, s -> s.is(Blocks.STONE)); }

    /** 生成するバイオームに placed feature id が入っているか */
    /**
     * バイオームの選び方を「ワールドに頼らず」調べるための偽の文脈。canGenerateIn は本物のワールドと同じ答え
     * （オーバーワールド＝#minecraft:is_overworld、ネザー＝#is_nether、エンド＝#is_end のバイオーム）を返す。
     * GameTest のワールドは砂漠だけなので、本物の BiomeModifications の結果では「砂漠以外のオーバーワールド」を確かめられない（FAILED.md タスク13）
     */
    private static net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext fakeContext(net.minecraft.core.RegistryAccess ra, net.minecraft.core.Holder.Reference<net.minecraft.world.level.biome.Biome> b) {
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

    /**
     * タスク14：全部の鉱石の生成条件（ModWorldGen.RULES）を、レジストリの全バイオームに対して直接調べる。
     * 期待値はここに別に書く（仕様）：オーバーワールド全体／浜辺だけ／砂漠だけ／砂漠＋荒野。MOD の placed feature は全部ルールを持つ
     */
    @GameTest
    public void oreBiomeRules(GameTestHelper h) {
        var ra = h.getLevel().registryAccess();
        var biomes = ra.lookupOrThrow(Registries.BIOME);
        java.util.Map<String, java.util.function.Predicate<net.minecraft.core.Holder.Reference<net.minecraft.world.level.biome.Biome>>> expect = new java.util.HashMap<>();
        for (String id : new String[] {"sphalerite_ore", "graphite_ore", "magnetite_ore", "wolframite_ore", "galena_ore", "quartz_vein"})
            expect.put(id, b -> b.is(BiomeTags.IS_OVERWORLD));
        expect.put("iron_sand", b -> b.is(BiomeTags.IS_BEACH));
        expect.put("trona_deposit", b -> b.is(net.minecraft.world.level.biome.Biomes.DESERT));
        expect.put("borax_deposit", b -> b.is(net.minecraft.world.level.biome.Biomes.DESERT));
        expect.put("rock_salt_ore", b -> b.is(net.minecraft.world.level.biome.Biomes.DESERT) || b.is(BiomeTags.IS_BADLANDS));
        List<String> wrong = new ArrayList<>();
        java.util.Map<String, Integer> count = new java.util.TreeMap<>();
        java.util.Set<String> ruled = new java.util.HashSet<>();
        for (var rule : com.taku3.semiconductorlab.ModWorldGen.RULES) {
            String id = rule.feature().identifier().getPath();
            ruled.add(id);
            var want = expect.get(id);
            if (want == null) { wrong.add(id + ": no expectation written in the test"); continue; }
            int n = 0;
            for (var b : biomes.listElements().toList()) {
                boolean got = rule.biomes().test(fakeContext(ra, b)), exp = want.test(b);
                if (got) n++;
                if (got != exp) wrong.add(id + " @ " + b.key().identifier() + ": got " + got + ", expected " + exp);
            }
            count.put(id, n);
        }
        for (String id : expect.keySet()) if (!ruled.contains(id)) wrong.add(id + ": no rule in ModWorldGen.RULES");
        for (var pf : ra.lookupOrThrow(Registries.PLACED_FEATURE).listElementIds().toList())
            if (pf.identifier().getNamespace().equals(SemiconductorLab.MOD_ID) && !ruled.contains(pf.identifier().getPath())) wrong.add(pf.identifier() + ": placed feature without a biome rule");
        h.assertTrue(wrong.isEmpty(), "biome rules wrong (" + wrong.size() + "): " + wrong.subList(0, Math.min(10, wrong.size())));
        // 目で見て分かる数の確認：砂漠専用は1つ、オーバーワールドは数十、ネザー・エンドのバイオームには入らない
        h.assertValueEqual(count.get("trona_deposit"), 1, "trona: desert only");
        h.assertValueEqual(count.get("borax_deposit"), 1, "borax: desert only");
        h.assertTrue(count.get("rock_salt_ore") >= 2, "rock salt: desert + badlands (" + count.get("rock_salt_ore") + ")");
        h.assertTrue(count.get("quartz_vein") >= 40, "quartz veins in every overworld biome (" + count.get("quartz_vein") + ")");
        h.assertFalse(com.taku3.semiconductorlab.ModWorldGen.RULES.get(0).biomes().test(fakeContext(ra, biomes.getOrThrow(net.minecraft.world.level.biome.Biomes.NETHER_WASTES))), "no overworld ore in the nether");
        LOG.info("[LAB] ore biome rules vs all {} biomes: {}", biomes.listElements().count(), count);
        h.succeed();
    }

    private static boolean biomeHas(GameTestHelper h, ResourceKey<net.minecraft.world.level.biome.Biome> biome, String id) {
        var b = h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome).value();
        return b.getGenerationSettings().features().stream()
                .anyMatch(set -> set.stream().anyMatch(f -> f.unwrapKey().map(k -> k.identifier().equals(SemiconductorLab.id(id))).orElse(false)));
    }

    /** [6-04] 岩塩鉱：ツルハシで岩塩、シルクタッチでブロック。砂漠・荒野だけ。岩塩→作業台→塩 */
    @GameTest
    public void rockSalt(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = ModBlocks.ROCK_SALT_ORE.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "rock salt is soft: a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(ModItems.ROCK_SALT)), "pickaxe -> rock salt, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(ModBlocks.ROCK_SALT_ORE.asItem()), "silk touch -> block, got " + silky);
        h.assertTrue(recipeUses(h, "salt_from_rock_salt", ModItems.ROCK_SALT), "rock salt -> salt");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "rock_salt_ore"), "rock salt under deserts");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.BADLANDS, "rock_salt_ore"), "rock salt under badlands");
        h.assertFalse(biomeHas(h, net.minecraft.world.level.biome.Biomes.PLAINS, "rock_salt_ore"), "no rock salt under plains");
        LOG.info("[LAB] rock salt: pickaxe -> rock salt, silk -> block, crafting -> salt, desert/badlands only");
        h.succeed();
    }

    /** [0-16] 塩田：海・浜バイオーム＋横に水＋空＋晴れで、1分に塩1、塩8ごとににがり1。右クリックで受け取る */
    @GameTest
    public void saltPan(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 4);
        BlockPos pos = p(1, 1, 1), water = p(2, 1, 1);
        h.setBlock(pos, ModBlocks.SALT_PAN);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.SaltPanBlockEntity.class);
        BlockPos a = h.absolutePos(pos);
        int T = com.taku3.semiconductorlab.block.SaltPanBlockEntity.TICKS_PER_SALT;
        boolean sea = com.taku3.semiconductorlab.block.SaltPanBlockEntity.isSeaBiome(lv, a);
        LOG.info("[LAB] salt pan: test biome = {} (sea={})", lv.getBiome(a).unwrapKey().map(k -> k.identifier().toString()).orElse("?"), sea);
        h.setBlock(water, Blocks.WATER);
        if (!sea) {
            // 海でなければ（平原など）水があっても動かない
            be.step(lv, a, T * 2);
            h.assertValueEqual(be.getSalt(), 0, "not by the sea -> no salt");
            h.assertTrue(be.status(lv, a) == com.taku3.semiconductorlab.block.SaltPanBlockEntity.Status.NOT_SEA, "status NOT_SEA");
        }
        be.setForceSeaForTest(true);
        h.assertTrue(be.status(lv, a) != com.taku3.semiconductorlab.block.SaltPanBlockEntity.Status.NOT_SEA, "forced sea");
        if (lv.isRainingAt(a.above())) { LOG.info("[LAB] salt pan: raining in the test world, skip the sunny part"); h.succeed(); return; }
        // 水なし：止まる
        h.setBlock(water, Blocks.STONE);
        be.step(lv, a, T * 2);
        h.assertValueEqual(be.getSalt(), 0, "no seawater beside -> no salt");
        // 屋根：止まる
        h.setBlock(water, Blocks.WATER);
        h.setBlock(p(1, 2, 1), Blocks.STONE);
        be.step(lv, a, T * 2);
        h.assertValueEqual(be.getSalt(), 0, "no sky -> no salt");
        h.setBlock(p(1, 2, 1), Blocks.AIR);
        // 晴れ・空・水：1分で1個
        be.step(lv, a, T - 20);
        h.assertValueEqual(be.getSalt(), 0, "not yet 1 s before a minute");
        be.step(lv, a, 20);
        h.assertValueEqual(be.getSalt(), 1, "1 salt per minute");
        h.assertValueEqual(be.getBittern(), 0, "no bittern yet");
        be.step(lv, a, T * 7);
        h.assertValueEqual(be.getSalt(), 8, "8 salt");
        h.assertValueEqual(be.getBittern(), 1, "1 bittern per 8 salt (NaCl crystallises first, MgCl2 stays)");
        // 右クリック相当：プレイヤーに渡る
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int n = be.collect(player);
        h.assertValueEqual(n, 9, "collected 8 salt + 1 bittern");
        h.assertValueEqual(player.getInventory().countItem(ModItems.SALT), 8, "salt in inventory");
        h.assertValueEqual(player.getInventory().countItem(ModItems.BITTERN), 1, "bittern in inventory");
        h.assertValueEqual(be.getSalt(), 0, "pan emptied");
        // 満杯で止まる（塩64）
        be.step(lv, a, T * 80);
        h.assertValueEqual(be.getSalt(), com.taku3.semiconductorlab.block.SaltPanBlockEntity.MAX_SALT, "stops at 64 salt");
        h.assertTrue(recipeUses(h, "salt_pan", Items.CAULDRON), "salt pan needs an iron pot (cauldron) for boiling down");
        LOG.info("[LAB] salt pan: sea + water + sky + no rain -> 1 salt / {} ticks, 1 bittern / 8 salt, stops at 64", T);
        h.succeed();
    }

    /** [6-05] クロルアルカリ電解：水＋塩×2 → H2×1（陰極）＋Cl2×1（陽極）＋NaOH×2。硫酸を入れれば従来どおり水の電気分解 */
    @GameTest
    public void chlorAlkali(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.ELECTROLYZER);
        ElectrolyzerBlockEntity be = h.getBlockEntity(pos, ElectrolyzerBlockEntity.class);
        var c = be.getContainer();
        int W = ElectrolyzerBlockEntity.SLOT_WATER, S = ElectrolyzerBlockEntity.SLOT_ACID, H2 = ElectrolyzerBlockEntity.SLOT_H2,
                AN = ElectrolyzerBlockEntity.SLOT_O2, NA = ElectrolyzerBlockEntity.SLOT_NAOH, T = ElectrolyzerBlockEntity.BRINE_TICKS;
        long feed = 250;
        // 塩1個では足りない（2NaCl）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET));
        c.setItem(S, new ItemStack(ModItems.SALT, 1));
        tickConsumer(h, pos, be, T + 10, feed);
        h.assertTrue(c.getItem(H2).isEmpty(), "1 salt is not enough for 2NaCl");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());
        // 塩3個：128秒で1回分
        c.setItem(S, new ItemStack(ModItems.SALT, 3));
        h.assertValueEqual(be.mode(), ElectrolyzerBlockEntity.MODE_BRINE, "salt -> brine mode");
        tickConsumer(h, pos, be, T - 1, feed);
        h.assertTrue(c.getItem(H2).isEmpty(), "not finished 1 tick before 128 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(H2).is(ModItems.HYDROGEN_GAS) && c.getItem(H2).getCount() == 1, "cathode: 1 H2, got " + c.getItem(H2));
        h.assertTrue(c.getItem(AN).is(ModItems.CHLORINE_GAS) && c.getItem(AN).getCount() == 1, "anode: 1 Cl2, got " + c.getItem(AN));
        h.assertTrue(c.getItem(NA).is(ModItems.SODIUM_HYDROXIDE) && c.getItem(NA).getCount() == 2, "2 NaOH, got " + c.getItem(NA));
        h.assertValueEqual(c.getItem(S).getCount(), 1, "2 salt consumed");
        h.assertTrue(c.getItem(W).is(Items.BUCKET), "water used");
        // 陽極の出口に酸素が残っていると塩素は出せない（混ぜない）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET));
        c.setItem(S, new ItemStack(ModItems.SALT, 2));
        c.setItem(AN, new ItemStack(ModItems.OXYGEN_GAS, 1));
        tickConsumer(h, pos, be, T + 10, feed);
        h.assertTrue(c.getItem(W).is(Items.WATER_BUCKET), "anode outlet holds O2 -> brine run blocked");
        // 硫酸に戻すと水の電気分解（NaOH は出ない、塩素も出ない）
        c.setItem(AN, ItemStack.EMPTY);
        c.setItem(H2, ItemStack.EMPTY);
        c.setItem(NA, ItemStack.EMPTY);
        c.setItem(S, new ItemStack(ModItems.SULFURIC_ACID, 1));
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS, feed);
        h.assertTrue(c.getItem(AN).is(ModItems.OXYGEN_GAS), "acid -> water electrolysis, O2 at the anode");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "2 H2 in water mode");
        h.assertTrue(c.getItem(NA).isEmpty(), "no NaOH in water mode");
        // 塩素はボンベとガスホルダーに対応、水素と混ぜると爆発（塩素爆鳴気）、酸素とは爆発しない
        h.assertTrue(GasType.CHLORINE.cylinderItem() == ModItems.CHLORINE_CYLINDER && GasType.of(new ItemStack(ModItems.CHLORINE_GAS)) == GasType.CHLORINE, "chlorine gas type");
        h.assertTrue(GasType.explosive(GasType.HYDROGEN, GasType.CHLORINE), "H2 + Cl2 explodes");
        h.assertFalse(GasType.explosive(GasType.OXYGEN, GasType.CHLORINE), "O2 + Cl2 does not explode");
        LOG.info("[LAB] chlor-alkali: water + 2 salt -> H2 + Cl2 + 2 NaOH in {} ticks; acid -> H2 x2 + O2", T);
        h.succeed();
    }

    /** 塩素：電解槽の陽極からガスホルダーへ吸われ、圧縮機で塩素ボンベになる。水素のホルダーに塩素を入れると爆発 */
    @GameTest(maxTicks = 80)
    public void chlorineHolderAndCylinder(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1), cp = p(0, 1, 1), h2p = p(1, 1, 3);
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        h.setBlock(ep, ModBlocks.ELECTROLYZER);
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, new ItemStack(ModItems.CHLORINE_GAS, 3));
        ec.setItem(ElectrolyzerBlockEntity.SLOT_NAOH, new ItemStack(ModItems.SODIUM_HYDROXIDE, 2));
        h.setBlock(h2p, ModBlocks.GAS_HOLDER);
        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            int total = g.getAmount() + g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertValueEqual(g.getGasType() == GasType.NONE ? GasType.of(g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT)) : g.getGasType(), GasType.CHLORINE, "holder took chlorine");
            h.assertValueEqual(total, 3, "3 mol Cl2 pulled");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_NAOH).getCount(), 2, "NaOH is not a gas: stays");
            // 圧縮機：塩素 60mol → 塩素ボンベ
            h.setBlock(cp, ModBlocks.COMPRESSOR);
            CompressorBlockEntity comp = h.getBlockEntity(cp, CompressorBlockEntity.class);
            g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(ModItems.CHLORINE_GAS, 64));
            g.setContentsForTest(GasType.CHLORINE, 60);
            comp.getContainer().setItem(CompressorBlockEntity.SLOT_EMPTY, new ItemStack(ModItems.EMPTY_GAS_CYLINDER, 1));
            tickConsumer(h, cp, comp, CompressorBlockEntity.TOTAL_TICKS, EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS));
            h.assertTrue(comp.getContainer().getItem(CompressorBlockEntity.SLOT_OUT).is(ModItems.CHLORINE_CYLINDER), "compressor -> chlorine cylinder");
            h.assertValueEqual(g.getAmount(), 10, "60 - 50 mol");
            // 塩素爆鳴気：水素の入ったホルダーに塩素の袋
            GasHolderBlockEntity g2 = h.getBlockEntity(h2p, GasHolderBlockEntity.class);
            g2.setContentsForTest(GasType.HYDROGEN, 30);
            g2.getContainer().setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(ModItems.CHLORINE_GAS, 1));
        });
        h.runAfterDelay(30, () -> {
            GasHolderBlockEntity g2 = h.getBlockEntity(h2p, GasHolderBlockEntity.class);
            h.assertValueEqual(g2.getAmount(), 0, "H2 + Cl2 bang empties the holder");
            LOG.info("[LAB] chlorine: electrolyzer anode -> holder, compressor -> chlorine cylinder, H2 holder + Cl2 -> bang");
            h.succeed();
        });
    }

    private static void tickHcl(GameTestHelper h, BlockPos rel, com.taku3.semiconductorlab.block.HclBurnerBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) com.taku3.semiconductorlab.block.HclBurnerBlockEntity.tick(lv, a, lv.getBlockState(a), be);
    }

    /** [6-06] 塩酸合成炉：H2 1mol ＋ Cl2 1mol ＋ 水 1/4 バケツ（100 tick）→ 塩酸1。袋・ボンベ・隣のホルダーから。片方だけでは燃えない */
    @GameTest(maxTicks = 80)
    public void hclBurner(GameTestHelper h) {
        int T = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.BURN_TICKS;
        int H2 = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_H2, CL = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_CL2,
                W = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_WATER, OUT = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_OUT,
                EMPTY = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_EMPTY, BUCKET = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_BUCKET;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, ModBlocks.HCL_BURNER);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.HclBurnerBlockEntity.class);
        var c = be.getContainer();
        // 水だけ・水素だけ：燃えない（水素の袋は開けない）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET, 1));
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_GAS, 3));
        tickHcl(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "hydrogen without chlorine -> no flame");
        h.assertValueEqual(c.getItem(H2).getCount(), 3, "H2 bags not opened without chlorine");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 塩素の袋を足す：100 tick で塩酸1、バケツは空になって出る（水は残り3本分）
        c.setItem(CL, new ItemStack(ModItems.CHLORINE_GAS, 3));
        tickHcl(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickHcl(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(ModItems.HYDROCHLORIC_ACID), "H2 + Cl2 -> 2HCl -> hydrochloric acid");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "1 mol H2 used");
        h.assertValueEqual(c.getItem(CL).getCount(), 2, "1 mol Cl2 used (1:1)");
        h.assertTrue(c.getItem(BUCKET).is(Items.BUCKET), "empty bucket comes out");
        h.assertValueEqual(be.getWater(), 3, "1 bucket = 4 bottles of acid");
        // ボンベ：塩素ボンベ1本＝50mol、空ボンベが出る
        c.setItem(CL, new ItemStack(ModItems.CHLORINE_CYLINDER, 1));
        c.setItem(H2, new ItemStack(ModItems.HYDROGEN_CYLINDER, 1));
        tickHcl(h, pos, be, T * 3);
        h.assertValueEqual(c.getItem(OUT).getCount(), 4, "4 acids from 1 bucket");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 2, "H2 and Cl2 cylinders opened -> 2 empties");
        h.assertValueEqual(be.getCl2(), 47, "Cl2 50 - 3 mol");
        // 水がなくなると止まる
        tickHcl(h, pos, be, T * 2);
        h.assertValueEqual(c.getItem(OUT).getCount(), 4, "no water -> stops");
        // 出口を空けると、水なし＝乾いた塩化水素ガス 2mol（[1-16] 用、タスク22）
        c.setItem(OUT, ItemStack.EMPTY);
        tickHcl(h, pos, be, T);
        h.assertTrue(c.getItem(OUT).is(ModItems.HYDROGEN_CHLORIDE_GAS) && c.getItem(OUT).getCount() == 2, "no water -> dry HCl gas x2, got " + c.getItem(OUT));
        h.assertValueEqual(be.getWater(), 0, "dry mode uses no water");
        // 隣のガスホルダー（塩素）からももらえる
        BlockPos pos2 = p(3, 1, 1), hp = p(4, 1, 1);
        h.setBlock(pos2, ModBlocks.HCL_BURNER);
        h.setBlock(hp, ModBlocks.GAS_HOLDER);
        var be2 = h.getBlockEntity(pos2, com.taku3.semiconductorlab.block.HclBurnerBlockEntity.class);
        GasHolderBlockEntity holder = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        holder.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(ModItems.CHLORINE_GAS, 64));
        holder.setContentsForTest(GasType.CHLORINE, 20);
        be2.getContainer().setItem(W, new ItemStack(Items.WATER_BUCKET, 1));
        be2.getContainer().setItem(H2, new ItemStack(ModItems.HYDROGEN_GAS, 4));
        ServerLevel lv = h.getLevel();
        // 引き込みは 10 tick ごと（ゲーム時刻）。tick を手で回すとゲーム時刻は進まないので、引き込みを直接確かめる
        long t0 = lv.getGameTime();
        h.assertTrue(recipeUses(h, "hcl_burner", ModBlocks.OXYHYDROGEN_BURNER.asItem()), "derived from the oxyhydrogen burner [7-02]");
        h.runAfterDelay(25, () -> {
            h.assertTrue(be2.getCl2() > 0 || be2.getContainer().getItem(OUT).getCount() > 0, "pulled chlorine from the adjacent holder");
            h.assertTrue(holder.getAmount() < 20, "holder chlorine decreased");
            LOG.info("[LAB] HCl burner: H2 + Cl2 + 1/4 bucket -> 1 hydrochloric acid in {} ticks; bags, cylinders and adjacent holder (from game time {})", T, t0);
            h.succeed();
        });
    }

    /** WO3 の本物のルート：鉄マンガン重石＋ソーダ →（かまど）→ Na2WO4 ＋塩酸 → H2WO4 →（かまど）→ WO3。鉱石を焼くだけの仮ルートは消えた */
    @GameTest
    public void tungstenTrioxideRoute(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, SMELT, ModItems.WOLFRAMITE).isEmpty(), "roasting wolframite alone no longer gives WO3 (placeholder removed)");
        h.assertTrue(recipeUses(h, "wolframite_soda_charge", ModItems.WOLFRAMITE) && recipeUses(h, "wolframite_soda_charge", ModItems.SODIUM_CARBONATE), "wolframite + soda [2-03]");
        h.assertTrue(cook(h, SMELT, ModItems.WOLFRAMITE_SODA_CHARGE).is(ModItems.SODIUM_TUNGSTATE), "soda fusion -> Na2WO4");
        h.assertTrue(recipeUses(h, "tungstic_acid", ModItems.SODIUM_TUNGSTATE) && recipeUses(h, "tungstic_acid", ModItems.HYDROCHLORIC_ACID), "Na2WO4 + 2HCl [6-06] -> H2WO4");
        h.assertTrue(cook(h, SMELT, ModItems.TUNGSTIC_ACID).is(ModItems.TUNGSTEN_TRIOXIDE), "H2WO4 -> WO3 + H2O");
        ResourceKey<Recipe<?>> old = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("tungsten_trioxide"));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(old).isEmpty(), "old recipe tungsten_trioxide removed");
        LOG.info("[LAB] WO3: wolframite + soda -> fusion -> Na2WO4 + HCl -> H2WO4 -> roast -> WO3 (no shortcut)");
        h.succeed();
    }

    /**
     * [1-34] GeCl4 蒸留精製：GeO2 ＋ 塩酸×2 → 粗 GeCl4 ＋ 石英の蒸留器（戻る）→ GeCl4 ＋ 水 → 高純度 GeO2 → 水素炉 → Ge（経路つき）。
     * 帯域精製の上限：GeCl4 経由なら 10N、硫酸経由（精鉱＋硫酸 → GeO2 → 木炭）なら人造黒鉛ボートでも 9N
     */
    @GameTest
    public void geCl4Route(GameTestHelper h) {
        var server = h.getLevel().getServer();
        h.assertValueEqual(s_count(h, "crude_germanium_tetrachloride", ModItems.HYDROCHLORIC_ACID), 2L, "GeO2 + 4HCl: 2 bottles of acid (2 mol each)");
        h.assertTrue(recipeUses(h, "crude_germanium_tetrachloride", ModItems.GERMANIUM_DIOXIDE), "starts from GeO2");
        h.assertTrue(recipeUses(h, "quartz_still", ModItems.QUARTZ_TUBE) && recipeUses(h, "quartz_still", ModItems.FUSED_QUARTZ), "still is quartz glassware");
        // 蒸留：作業台で使っても蒸留器は戻る
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("germanium_tetrachloride"));
        var r = (net.minecraft.world.item.crafting.CraftingRecipe) server.getRecipeManager().byKey(key).orElseThrow().value();
        var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(ModItems.CRUDE_GERMANIUM_TETRACHLORIDE), new ItemStack(ModItems.QUARTZ_STILL)));
        h.assertTrue(r.matches(input, h.getLevel()), "crude GeCl4 + still matches");
        h.assertTrue(r.assemble(input).is(ModItems.GERMANIUM_TETRACHLORIDE), "-> distilled GeCl4");
        var rem = r.getRemainingItems(input);
        h.assertTrue(rem.stream().anyMatch(st -> st.is(ModItems.QUARTZ_STILL)), "the quartz still comes back, got " + rem);
        h.assertTrue(recipeUses(h, "high_purity_germanium_dioxide", ModItems.GERMANIUM_TETRACHLORIDE) && recipeUses(h, "high_purity_germanium_dioxide", Items.WATER_BUCKET), "hydrolysis GeCl4 + 2H2O");
        // 水素炉：高純度 GeO2 + 2H2 → 経路つきの Ge、ふつうの GeO2 → 経路なし
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, ModBlocks.HYDROGEN_FURNACE);
        HydrogenFurnaceBlockEntity f = h.getBlockEntity(fp, HydrogenFurnaceBlockEntity.class);
        var fc = f.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.REDUCE_WATTS);
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_IN, new ItemStack(ModItems.HIGH_PURITY_GERMANIUM_DIOXIDE, 1));
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, 4));
        tickConsumer(h, fp, f, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS, feed);
        ItemStack ge = fc.getItem(HydrogenFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(ge.is(ModItems.RAW_GERMANIUM) && ZoneRefiningFurnaceBlockEntity.isChlorideRoute(ge), "HP GeO2 + 2H2 -> Ge with chloride route, got " + ge);
        h.assertValueEqual(fc.getItem(HydrogenFurnaceBlockEntity.SLOT_H2).getCount(), 2, "2 H2 used (GeO2 + 2H2)");
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_IN, new ItemStack(ModItems.GERMANIUM_DIOXIDE, 1));
        tickConsumer(h, fp, f, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS, feed);
        ItemStack ge2 = fc.getItem(HydrogenFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(ge2.is(ModItems.RAW_GERMANIUM) && !ZoneRefiningFurnaceBlockEntity.isChlorideRoute(ge2), "plain GeO2 (sulfate route) -> Ge without route");
        // 帯域精製：人造黒鉛ボートでも硫酸経由は 9N で止まる、GeCl4 経由は 10N
        BlockPos zp = p(3, 1, 1);
        h.setBlock(zp, ModBlocks.ZONE_REFINING_FURNACE);
        ZoneRefiningFurnaceBlockEntity z = h.getBlockEntity(zp, ZoneRefiningFurnaceBlockEntity.class);
        var zc = z.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT;
        zc.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(ModItems.SYNTHETIC_GRAPHITE_BOAT));
        zc.setItem(G, ge2.copy());
        tickRefiner(h, zp, z, T * 4);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.NINE_N, "sulfate-route Ge stops at 9N even with a synthetic graphite boat");
        h.assertValueEqual(z.getLastWatts(), 0.0, "idle at the route cap");
        zc.setItem(G, ge.copy());
        tickRefiner(h, zp, z, T * 3);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.TEN_N, "GeCl4-route Ge reaches 10N");
        h.assertTrue(ZoneRefiningFurnaceBlockEntity.isChlorideRoute(zc.getItem(G)), "route is kept through refining");
        // 粘土ボートなら GeCl4 経由でも 9N（低いほうの上限）
        zc.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(ModItems.GRAPHITE_BOAT));
        zc.setItem(G, rawChloride(1));
        tickRefiner(h, zp, z, T * 4);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.NINE_N, "clay boat caps GeCl4-route Ge at 9N");
        // 硫酸のルートも残っている
        h.assertTrue(recipeUses(h, "germanium_dioxide", ModItems.SULFURIC_ACID), "concentrate + sulfuric acid -> GeO2 kept");
        LOG.info("[LAB] GeCl4: GeO2 + 2 HCl -> crude -> still (returned) -> GeCl4 -> + water -> HP GeO2 -> H2 furnace -> Ge(route); refining caps 10N (GeCl4) / 9N (sulfate)");
        h.succeed();
    }

    private static long s_count(GameTestHelper h, String id, net.minecraft.world.item.Item item) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
        return h.getLevel().getServer().getRecipeManager().byKey(key).map(r -> r.value().placementInfo().ingredients().stream().filter(i -> i.test(new ItemStack(item))).count()).orElse(0L);
    }

    /** 26.2 以降のバニラ要素が実在するかをログに出すだけ（失敗にしない） */
    @GameTest(required = false)
    public void vanillaElementsExist(GameTestHelper h) {
        for (String n : new String[] {"cinnabar", "sulfur", "sulfur_spike", "potent_sulfur"}) {
            Identifier id = Identifier.withDefaultNamespace(n);
            LOG.info("[LAB] [VANILLA] minecraft:{} block={} item={}", n, BuiltInRegistries.BLOCK.containsKey(id), BuiltInRegistries.ITEM.containsKey(id));
        }
        // 辰砂・硫黄の仲間（建材の派生など）を、レジストリにある実名で全部出す（[1-45] の「ゲーム」行の根拠）
        BuiltInRegistries.ITEM.keySet().stream().filter(i -> i.getNamespace().equals("minecraft"))
                .filter(i -> i.getPath().contains("cinnabar") || i.getPath().contains("sulfur")).sorted()
                .forEach(i -> LOG.info("[LAB] [VANILLA] registry item {} (block={})", i, BuiltInRegistries.BLOCK.containsKey(i)));
        h.succeed();
    }
}
