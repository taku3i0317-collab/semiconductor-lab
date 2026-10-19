package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity;
import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.GasType;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity;
import com.taku3.semiconductorlab.energy.CableBlockEntity;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.energy.GeneratorBlockEntity;
import com.taku3.semiconductorlab.energy.SteamEngineBlockEntity;
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

    private static void tickRefiner(GameTestHelper h, BlockPos rel, ZoneRefiningFurnaceBlockEntity be, int n) {
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(rel);
        for (int i = 0; i < n; i++) ZoneRefiningFurnaceBlockEntity.tick(lv, a, lv.getBlockState(a), be);
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
        for (Block m : new Block[] {ModBlocks.STEAM_ENGINE, ModBlocks.WATER_WHEEL, ModBlocks.ELECTROLYZER, ModBlocks.GAS_HOLDER,
                ModBlocks.BESSEMER_CONVERTER, ModBlocks.COPPER_CABLE, ModBlocks.ZONE_REFINING_FURNACE, ModBlocks.HYDROGEN_FURNACE}) {
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
        for (Block m : new Block[] {ModBlocks.STEAM_ENGINE, ModBlocks.ELECTROLYZER, ModBlocks.GAS_HOLDER, ModBlocks.BESSEMER_CONVERTER, ModBlocks.ZONE_REFINING_FURNACE}) {
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

    // =====================================================================
    // ケーブル
    // =====================================================================

    /** 蒸気機関→ケーブル3本：末端のケーブルに電気が届く（本当に tick を進める） */
    @GameTest(maxTicks = 60)
    public void cableChainFills(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), ModBlocks.STEAM_ENGINE);
        for (int x = 2; x <= 4; x++) h.setBlock(p(x, 1, 1), ModBlocks.COPPER_CABLE);
        SteamEngineBlockEntity eng = h.getBlockEntity(p(1, 1, 1), SteamEngineBlockEntity.class);
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL));
        eng.getContainer().setItem(SteamEngineBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        long[] first = {-1};
        h.runAfterDelay(3, () -> first[0] = h.getBlockEntity(p(4, 1, 1), CableBlockEntity.class).getStorage().getStoredJ());
        h.succeedWhen(() -> {
            long now = h.getBlockEntity(p(4, 1, 1), CableBlockEntity.class).getStorage().getStoredJ();
            h.assertTrue(first[0] >= 0 && now > first[0], "3rd cable should be filling up (was " + first[0] + ", now " + now + ")");
            LOG.info("[LAB] cable x3: last cable {} J -> {} J", first[0], now);
        });
    }

    /** ケーブル1ホップで 1% 失う：10000 J のケーブルの隣の空ケーブルへ半分(5000)送ると 4950 届く */
    @GameTest
    public void cableLossOnePercent(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), ModBlocks.COPPER_CABLE);
        h.setBlock(p(2, 1, 1), ModBlocks.COPPER_CABLE);
        CableBlockEntity a = h.getBlockEntity(p(1, 1, 1), CableBlockEntity.class);
        CableBlockEntity b = h.getBlockEntity(p(2, 1, 1), CableBlockEntity.class);
        a.getStorage().insert(10_000, false);
        BlockPos abs = h.absolutePos(p(1, 1, 1));
        CableBlockEntity.tick(h.getLevel(), abs, h.getLevel().getBlockState(abs), a);
        h.assertValueEqual(a.getStorage().getStoredJ(), 5_000L, "sender keeps half");
        h.assertValueEqual(b.getStorage().getStoredJ(), 4_950L, "receiver gets half minus 1%");
        LOG.info("[LAB] cable loss: sent 5000 J, received {} J", b.getStorage().getStoredJ());
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
        LOG.info("[LAB] zone doping: 9N+P -> n, 9N+In -> p, 6N+P -> no doping (refines to {} first)", Purity.label(purityOf(c.getItem(G))));
        h.succeed();
    }

    // =====================================================================
    // データ（レシピ・進捗・関数）とバニラ要素
    // =====================================================================

    private static final String[] RECIPES = {
            "bessemer_converter", "coal_ash", "copper_cable", "dynamo", "electric_motor", "electrolyzer", "fused_quartz",
            "gas_holder", "germanium_concentrate_from_ash", "germanium_concentrate_from_sphalerite", "germanium_dioxide",
            "graphite_boat", "graphite_boat_blank", "hf_power_supply", "hydrogen_furnace", "indium", "induction_coil", "junction_transistor",
            "junction_transistor_blank", "lead_ingot", "lead_screw", "litharge", "magnet", "phosphorus", "quartz_tube", "raw_germanium", "refractory_brick", "refractory_brick_blank", "steam_engine",
            "sulfuric_acid", "sulfur_dioxide", "sulfur_powder_from_block", "sulfur_powder_from_potent", "sulfur_powder_from_spike", "transistor",
            "transistor_radio", "tungsten_trioxide", "tungsten_wire", "water_wheel", "zinc_ingot", "zone_refining_furnace"};

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
        h.assertTrue(recipeUses(h, "refractory_brick_blank", Items.SAND) && recipeUses(h, "refractory_brick_blank", Items.CLAY_BALL), "blank = silica sand + clay");
        h.assertTrue(recipeUses(h, "bessemer_converter", ModItems.REFRACTORY_BRICK), "converter lining should be refractory brick");
        h.assertFalse(recipeUses(h, "bessemer_converter", Items.CLAY_BALL), "converter no longer uses raw clay");
        LOG.info("[LAB] refractory brick: blank fires only in the blast furnace; Bessemer converter lined with refractory brick");
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

    private static final String[] CHAPTER1 = {"root", "coal_ash", "concentrate", "dioxide", "sulfuric_acid", "raw_germanium",
            "refined", "n_type", "transistor", "junction", "radio"};
    private static final String[] GUIDE_ADV = {"root", "welcome", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"};
    private static final String[] GUIDE_FN = {"give_guidebook", "guide/welcome", "guide/raw_germanium", "guide/refined",
            "guide/n_type", "guide/junction", "guide/radio", "guide/sulfuric_acid"};

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
