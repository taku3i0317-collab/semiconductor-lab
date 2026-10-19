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

        c.setItem(G, new ItemStack(ModItems.RAW_GERMANIUM, 4));
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
            "acheson_furnace", "bessemer_converter", "cinnabar_dust", "coal_ash", "compressor", "copper_cable", "dynamo", "electric_furnace", "electric_motor", "electrolyzer", "empty_gas_cylinder", "oxyhydrogen_burner",
            "gas_holder", "germanium_concentrate_from_ash", "germanium_dioxide",
            "graphite_boat", "graphite_boat_blank", "graphite_rod", "synthetic_graphite_boat", "hand_vacuum_pump", "hf_oscillator", "hydrogen_furnace", "induction_coil", "junction_transistor",
            "junction_transistor_blank", "lead_ingot", "lead_screw", "lead_acid_battery", "leyden_jar", "litharge", "magnet", "mercury", "mercury_vacuum_gauge", "phosphorus", "quartz_tube", "quicklime", "quicklime_from_blasting", "slaked_lime", "raw_germanium", "refractory_brick", "refractory_brick_blank", "sealing_station", "self_excited_dynamo", "steam_engine", "steam_turbine",
            "sulfuric_acid", "sulfur_dioxide", "sulfur_powder_from_block", "sulfur_powder_from_potent", "sulfur_powder_from_spike", "transistor",
            "transistor_radio", "tungsten_trioxide", "tungsten_wire", "vacuum_chamber", "vacuum_tube_assembly", "water_wheel", "zinc_ingot", "zinc_from_oxide", "reverberatory_furnace", "zone_refining_furnace"};

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
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, ModItems.WOLFRAMITE).is(ModItems.TUNGSTEN_TRIOXIDE), "wolframite -> WO3 (placeholder roast)");
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
            h.assertTrue(c.getPressure() >= floorPa + 2 - 1e-9, "air should leak back in (~1 Pa/s): " + floorPa + " -> " + c.getPressure());
            LOG.info("[LAB] hand pump: 1 stroke -> {}, {} strokes to <1.5 kPa, floor {}, leak to {}", VacuumChamberBlockEntity.formatPa(one), strokes,
                    VacuumChamberBlockEntity.formatPa(floorPa), VacuumChamberBlockEntity.formatPa(c.getPressure()));
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

    /** 容器が 1.5kPa 以下のときだけ封じる。1本封じると容器に空気がもどり、続けては封じられない。容器なしでは動かない */
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
        c.setPressure(1400);
        tickConsumer(h, sp, be, T, feed);
        h.assertTrue(box.getItem(OUT).is(ModItems.VACUUM_TUBE) && box.getItem(OUT).getCount() == 1, "at 1.4 kPa one tube should be sealed in " + T + " ticks, got " + box.getItem(OUT));
        h.assertValueEqual(box.getItem(IN).getCount(), 1, "one assembly used");
        double after = c.getPressure();
        h.assertTrue(after > SealingStationBlockEntity.SEAL_MAX_PA, "sealing lets air back into the chamber: " + after);
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertValueEqual(box.getItem(OUT).getCount(), 1, "no second tube until pumped down again");
        SealingStationBlockEntity alone = h.getBlockEntity(lonely, SealingStationBlockEntity.class);
        alone.getContainer().setItem(IN, new ItemStack(ModItems.VACUUM_TUBE_ASSEMBLY));
        tickConsumer(h, lonely, alone, T + 20, feed);
        h.assertTrue(alone.getContainer().getItem(OUT).isEmpty(), "no chamber next to it -> no sealing");
        LOG.info("[LAB] sealing station: 1 atm -> wait, 1.4 kPa -> tube in {} ticks, chamber back to {}, no chamber -> wait", T, VacuumChamberBlockEntity.formatPa(after));
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
        h.assertTrue(recipeUses(h, "leyden_jar", Items.GLASS_BOTTLE), "Leyden jar = glass jar + metal foil");
        h.assertTrue(recipeUses(h, "zone_refining_furnace", ModItems.HF_OSCILLATOR), "zone refining furnace needs the HF oscillator");
        h.assertFalse(BuiltInRegistries.ITEM.containsKey(SemiconductorLab.id("hf_power_supply")), "placeholder hf_power_supply must be gone");
        h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("hf_power_supply"))).isEmpty(), "placeholder recipe must be gone");
        LOG.info("[LAB] HF oscillator: 2 tubes + coil + Leyden jar + cable; zone refining furnace uses it; hf_power_supply removed");
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
        c.setItem(G, new ItemStack(ModItems.RAW_GERMANIUM, 2));
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
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.QUARTZ).isEmpty(), "quartz must not melt in a furnace (~1000 C < 1713 C)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Items.QUARTZ).isEmpty(), "nor in a blast furnace");

        // ボンベなし：動かない
        c.setItem(IN, new ItemStack(Items.QUARTZ, 30));
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
