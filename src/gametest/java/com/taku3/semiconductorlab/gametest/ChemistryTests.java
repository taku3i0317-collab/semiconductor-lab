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
 * GameTest：幹2 化学（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class ChemistryTests extends LabTestBase {    @GameTest public void oreTrona(GameTestHelper h)      { oreFeature(h, "trona_deposit", Blocks.SAND, Blocks.STONE, Chemistry.TRONA_DEPOSIT, s -> s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE)); }

    /** [2-03] トロナ：堆積はツルハシでトロナを落とす（シルクタッチでブロック）、素手では落ちない。トロナ→かまど→炭酸ナトリウム */
    @GameTest
    public void tronaSoda(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = Chemistry.TRONA_DEPOSIT.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "trona is soft: a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(Chemistry.TRONA)), "pickaxe -> trona, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(Chemistry.TRONA_DEPOSIT.asItem()), "silk touch -> block, got " + silky);
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.TRONA).is(Chemistry.SODIUM_CARBONATE), "trona -> furnace -> sodium carbonate");
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

    /** [2-14] ソーダ石灰ガラス：砂6＋ソーダ＋生石灰 → 調合原料6 → かまど/溶鉱炉 → ガラス。実験器具（＋電解槽・真空管の組立品・鉛蓄電池：Q10-C3）はバニラのガラスでなく MOD ガラス */
    @GameTest
    public void sodaLimeGlass(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("glass_batch"));
        var r = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Items.SAND))).count(), 6L, "6 sand (SiO2 ~72%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Chemistry.SODIUM_CARBONATE))).count(), 1L, "1 soda (Na2O ~14%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Survival.QUICKLIME))).count(), 1L, "1 quicklime (CaO ~10%)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.GLASS_BATCH).is(Chemistry.SODA_LIME_GLASS), "batch -> furnace -> soda-lime glass");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Chemistry.GLASS_BATCH).is(Chemistry.SODA_LIME_GLASS), "batch -> blast furnace -> soda-lime glass");
        for (String id : new String[] {"vacuum_chamber", "mercury_vacuum_gauge", "leyden_jar", "electrolyzer", "vacuum_tube_assembly", "lead_acid_battery", "sealing_station", "sprengel_pump"}) {
            h.assertTrue(recipeUses(h, id, Chemistry.SODA_LIME_GLASS), id + " should use soda-lime glass");
            for (var v : new net.minecraft.world.item.Item[] {Items.GLASS, Items.GLASS_PANE, Items.GLASS_BOTTLE})
                h.assertFalse(recipeUses(h, id, v), id + " must not use vanilla " + v);
        }
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.SAND).is(Items.GLASS), "vanilla sand -> glass stays for windows");
        LOG.info("[LAB] soda-lime glass: 6 sand + soda + lime -> 6 batch -> glass; vacuum chamber / gauge / Leyden jar / electrolyzer / tube assembly / lead-acid battery need it");
        h.succeed();
    }

    /** [2-23] 乾留炉（IMP2-01）：原木1 → 木炭1、2本ごとに木酢液、4本ごとに木タール。燃料なし・ネザーの幹は不可。木タール →かまど→ ピッチ */
    @GameTest
    public void woodRetort(GameTestHelper h) {
        int T = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.DISTILL_TICKS;
        var W = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.class;
        int IN = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.SLOT_IN, FUEL = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.SLOT_FUEL;
        int CH = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.SLOT_CHARCOAL, VI = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.SLOT_VINEGAR, TA = com.taku3.semiconductorlab.block.WoodRetortBlockEntity.SLOT_TAR;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Chemistry.WOOD_RETORT.block);
        var be = h.getBlockEntity(pos, W);
        var c = be.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(pos);
        Runnable tick = () -> com.taku3.semiconductorlab.block.WoodRetortBlockEntity.tick(lv, a, lv.getBlockState(a), be);

        // ネザーの幹（キノコ）は木ではない
        h.assertTrue(com.taku3.semiconductorlab.block.WoodRetortBlockEntity.isInput(new ItemStack(Items.OAK_LOG)), "oak log is wood");
        h.assertTrue(com.taku3.semiconductorlab.block.WoodRetortBlockEntity.isInput(new ItemStack(Items.BIRCH_LOG)), "birch log is wood");
        h.assertFalse(com.taku3.semiconductorlab.block.WoodRetortBlockEntity.isInput(new ItemStack(Items.CRIMSON_STEM)), "crimson stem is a fungus, not wood");
        // 燃料なし：進まない
        c.setItem(IN, new ItemStack(Items.OAK_LOG, 8));
        for (int i = 0; i < T + 10; i++) tick.run();
        h.assertTrue(c.getItem(CH).isEmpty(), "no fuel -> no distillation");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 石炭1個で8本：木炭8・木酢液4・木タール2
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        for (int i = 0; i < T - 1; i++) tick.run();
        h.assertTrue(c.getItem(CH).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tick.run();
        h.assertValueEqual(c.getItem(CH).getCount(), 1, "1 log -> 1 charcoal");
        h.assertTrue(c.getItem(VI).isEmpty() && c.getItem(TA).isEmpty(), "1 log alone gives no vinegar/tar item yet");
        for (int i = 0; i < T * 7; i++) tick.run();
        h.assertTrue(c.getItem(CH).is(Items.CHARCOAL), "charcoal");
        h.assertValueEqual(c.getItem(CH).getCount(), 8, "8 charcoal");
        h.assertTrue(c.getItem(VI).is(Chemistry.WOOD_VINEGAR), "wood vinegar");
        h.assertValueEqual(c.getItem(VI).getCount(), 4, "4 wood vinegar (1 per 2 logs)");
        h.assertTrue(c.getItem(TA).is(Chemistry.WOOD_TAR), "wood tar");
        h.assertValueEqual(c.getItem(TA).getCount(), 2, "2 wood tar (1 per 4 logs)");
        h.assertTrue(c.getItem(IN).isEmpty() && c.getItem(FUEL).isEmpty(), "8 logs per coal");
        // タール → かまど → ピッチ。乾留炉のレシピは鉄の釜＋銅の冷却管＋かまど＋れんが
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.WOOD_TAR).is(Chemistry.PITCH), "tar boiled down -> pitch");
        h.assertTrue(recipeUses(h, "wood_retort", Items.IRON_INGOT) && recipeUses(h, "wood_retort", Items.COPPER_INGOT), "iron retort + copper condenser");
        LOG.info("[LAB] wood retort [2-23]: 8 logs + 1 coal -> 8 charcoal + 4 wood vinegar + 2 wood tar; tar -> furnace -> pitch");
        h.succeed();
    }

    /** [2-20] ガス工場のレトルト（IMP4-01）：石炭8＋燃料1 → コークス6・安水2・コールタール1。燃料なし・木炭は乾留しない。コークスも燃料になる */
    @GameTest
    public void gasRetort(GameTestHelper h) {
        int T = com.taku3.semiconductorlab.block.GasRetortBlockEntity.DISTILL_TICKS;
        int IN = com.taku3.semiconductorlab.block.GasRetortBlockEntity.SLOT_IN, FUEL = com.taku3.semiconductorlab.block.GasRetortBlockEntity.SLOT_FUEL, CO = com.taku3.semiconductorlab.block.GasRetortBlockEntity.SLOT_COKE, TA = com.taku3.semiconductorlab.block.GasRetortBlockEntity.SLOT_TAR, LQ = com.taku3.semiconductorlab.block.GasRetortBlockEntity.SLOT_LIQUOR;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Chemistry.GAS_RETORT.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.GasRetortBlockEntity.class);
        var c = be.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(pos);
        Runnable tick = () -> com.taku3.semiconductorlab.block.GasRetortBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        h.assertTrue(com.taku3.semiconductorlab.block.GasRetortBlockEntity.isInput(new ItemStack(Items.COAL)), "coal is baked");
        h.assertFalse(com.taku3.semiconductorlab.block.GasRetortBlockEntity.isInput(new ItemStack(Items.CHARCOAL)), "charcoal is already baked");
        h.assertFalse(com.taku3.semiconductorlab.block.GasRetortBlockEntity.isInput(new ItemStack(Items.OAK_LOG)), "wood goes to the wood retort");
        h.assertTrue(com.taku3.semiconductorlab.block.GasRetortBlockEntity.isFuel(new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.COKE)), "coke heats the retort");
        c.setItem(IN, new ItemStack(Items.COAL, 8));
        for (int i = 0; i < T + 10; i++) tick.run();
        h.assertTrue(c.getItem(CO).isEmpty(), "no fuel -> nothing");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        for (int i = 0; i < T * 8; i++) tick.run();
        h.assertTrue(c.getItem(CO).is(com.taku3.semiconductorlab.content.Metallurgy.COKE) && c.getItem(CO).getCount() == 6, "8 coal -> 6 coke, got " + c.getItem(CO));
        h.assertTrue(c.getItem(LQ).is(Chemistry.AMMONIACAL_LIQUOR) && c.getItem(LQ).getCount() == 2, "2 ammoniacal liquor, got " + c.getItem(LQ));
        h.assertTrue(c.getItem(TA).is(Chemistry.COAL_TAR) && c.getItem(TA).getCount() == 1, "1 coal tar, got " + c.getItem(TA));
        h.assertTrue(c.getItem(IN).isEmpty() && c.getItem(FUEL).isEmpty(), "1 fuel bakes 8 coal");
        // 出口のタールが満杯なら止まる
        c.setItem(TA, new ItemStack(Chemistry.COAL_TAR, 16));
        c.setItem(IN, new ItemStack(Items.COAL, 1));
        c.setItem(FUEL, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.COKE, 1));
        for (int i = 0; i < T + 10; i++) tick.run();
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "tar outlet full -> stops");
        h.assertTrue(recipeUses(h, "gas_retort", Heat.REFRACTORY_BRICK) && recipeUses(h, "gas_retort", Items.COPPER_INGOT), "fireclay retort + copper condenser");
        LOG.info("[LAB] gas retort [2-20]: 8 coal + 1 fuel -> 6 coke + 2 ammoniacal liquor + 1 coal tar; charcoal / logs rejected; coke is fuel");
        h.succeed();
    }

    /** [2-10] 鉛のレトルト（IMP4-01）：蛍石＋硫酸＋空の鉛の瓶 → フッ酸（鉛の瓶）＋石こう。水1杯で4回、瓶・水・燃料がないと止まる。フッ酸はガラス瓶に入れない */
    @GameTest
    public void leadRetortHf(GameTestHelper h) {
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.HYDROFLUORIC_ACID) == com.taku3.semiconductorlab.machine.Vessel.LEAD_BOTTLE, "HF is kept in a lead bottle");
        h.assertFalse(com.taku3.semiconductorlab.machine.Vessel.ALLOWED.get("hydrofluoric_acid").contains(com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE), "HF never in glass (SiO2 + 4HF -> SiF4)");
        h.assertTrue(new ItemStack(Chemistry.HYDROFLUORIC_ACID).getItem().getCraftingRemainder().create().is(com.taku3.semiconductorlab.content.Metallurgy.LEAD_BOTTLE), "using HF at the bench gives the lead bottle back");
        h.assertTrue(recipeUses(h, "lead_retort", com.taku3.semiconductorlab.content.Metallurgy.LEAD_INGOT), "the retort is lead");
        for (var g : new net.minecraft.world.item.Item[] {Chemistry.SODA_LIME_GLASS, Chemistry.GLASS_BOTTLE, Items.GLASS})
            h.assertFalse(recipeUses(h, "lead_retort", g), "no glass in an HF retort: " + g);
        int T = com.taku3.semiconductorlab.block.HfRetortBlockEntity.REACT_TICKS;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Chemistry.LEAD_RETORT.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.HfRetortBlockEntity.class);
        var c = be.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(pos);
        Runnable tick = () -> com.taku3.semiconductorlab.block.HfRetortBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        c.setItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_FLUORITE, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.FLUORITE, 5));
        c.setItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID, 5));
        c.setItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL, 1));
        c.setItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET, 1));
        for (int i = 0; i < T + 10; i++) tick.run();
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_HF).isEmpty(), "no lead bottle -> nothing");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_FUEL).getCount(), 1, "no fuel burnt while waiting");
        c.setItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_BOTTLE, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.LEAD_BOTTLE, 5));
        for (int i = 0; i < T * 4; i++) tick.run();
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_HF).is(Chemistry.HYDROFLUORIC_ACID) && c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_HF).getCount() == 4, "4 runs -> 4 HF, got " + c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_HF));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_GYPSUM).is(Chemistry.CALCIUM_SULFATE) && c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_GYPSUM).getCount() == 4, "4 gypsum");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_BUCKET).is(Items.BUCKET), "empty bucket returned");
        h.assertValueEqual(be.getWater(), 0, "1 bucket = 4 runs");
        for (int i = 0; i < T + 10; i++) tick.run();
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.HfRetortBlockEntity.SLOT_HF).getCount(), 4, "no water -> stops (HF gas has nowhere to go)");
        var drops = lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(a).inflate(2));
        int bottles = drops.stream().filter(e -> e.getItem().is(Chemistry.GLASS_BOTTLE)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertValueEqual(bottles, 4, "4 glass bottles from the sulfuric acid popped out");
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] lead retort [2-10]: fluorite + H2SO4 + lead bottle -> HF (lead bottle) + gypsum; 1 water bucket = {} runs; glass bottles returned; no glass in recipe", com.taku3.semiconductorlab.block.HfRetortBlockEntity.RUNS_PER_WATER);
        h.succeed();
    }

    /** [2-21] アンモニア蒸留器（IMP4-01）：安水4＋消石灰＋瓶 → アンモニア水（ガラス瓶）。水バケツ1杯で4回、石灰・瓶・水・電気がないと止まる */
    @GameTest
    public void ammoniaStill(GameTestHelper h) {
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.AMMONIA_WATER) == com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE, "ammonia water in a glass bottle");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.AMMONIA_STILL.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_LIQUOR, new ItemStack(Chemistry.AMMONIACAL_LIQUOR, 16));
        c.setItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_BOTTLE, new ItemStack(Chemistry.GLASS_BOTTLE, 8));
        c.setItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT).isEmpty(), "no lime -> the bound ammonia stays (nothing)");
        c.setItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_LIME, new ItemStack(com.taku3.semiconductorlab.content.Survival.SLAKED_LIME, 8));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        for (int i = 0; i < 4; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT).is(Chemistry.AMMONIA_WATER) && c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT).getCount() == 4, "16 liquor -> 4 ammonia water, got " + c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_BUCKET).is(Items.BUCKET) && be.getWater() == 0, "1 water bucket = 4 runs");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_LIME).getCount() == 4 && c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_BOTTLE).getCount() == 4, "1 lime and 1 bottle per run");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS + 5, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.SLOT_OUT).getCount(), 4, "no absorber water -> stops");
        LOG.info("[LAB] ammonia still [2-21]: 4 liquor + slaked lime + bottle -> ammonia water (glass); water bucket = 4 runs");
        h.succeed();
    }

    /** [2-19] 過酸化水素の電解槽（IMP4-01）：白金の陽極なしでは動かない。瓶1 → H2O2（ガラス瓶）＋H2。水4回・硫酸8回・白金32回 */
    @GameTest(maxTicks = 100)
    public void peroxideCell(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.PEROXIDE_CELL.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID, 2));
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_BOTTLE, new ItemStack(Chemistry.GLASS_BOTTLE, 8));
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ANODE, new ItemStack(Items.IRON_INGOT));
        h.assertFalse(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.LAYOUT.slots().stream().anyMatch(sl -> sl.index() == com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ANODE && sl.accepts().test(new ItemStack(com.taku3.semiconductorlab.content.Power.GRAPHITE_ROD))), "graphite is not a persulfate anode");
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ANODE, ItemStack.EMPTY);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT).isEmpty(), "no platinum anode -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ANODE, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.PLATINUM_INGOT));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        for (int i = 0; i < 4; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT).is(Chemistry.HYDROGEN_PEROXIDE) && c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT).getCount() == 4, "4 bottles of H2O2, got " + c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_H2).is(com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS) && c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_H2).getCount() == 4, "and 4 bags of hydrogen (2H2O -> H2O2 + H2)");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ACID).getCount() == 1 && be.getAcid() == com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ACID - 4, "sulfuric acid mostly recovered (1 bottle per 8 runs)");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_ANODE).isEmpty() && be.getAnode() == com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ANODE - 4, "platinum anode lasts 32 runs");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_BUCKET).is(Items.BUCKET), "1 water bucket = 4 runs");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS + 5, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.SLOT_OUT).getCount(), 4, "no water left -> stops");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] peroxide cell [2-19]: Pt anode + H2SO4 (recovered) + water + bottle -> H2O2 + H2 ({} kW x {} s)", com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.WATTS / 1000, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS / 20);
        h.succeed();
    }

    /** [2-16] 蒸留水（IMP4-01）：水バケツ＋石英の蒸留器＋瓶3 → 蒸留水3（蒸留器・バケツが戻る） */
    @GameTest
    public void distilledWater(GameTestHelper h) {
        List<ItemStack> g = new java.util.ArrayList<>(List.of(new ItemStack(Items.WATER_BUCKET), new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL),
                new ItemStack(Chemistry.GLASS_BOTTLE), new ItemStack(Chemistry.GLASS_BOTTLE), new ItemStack(Chemistry.GLASS_BOTTLE)));
        while (g.size() < 9) g.add(ItemStack.EMPTY);
        var in = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, g);
        var r = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, h.getLevel()).orElseThrow().value();
        ItemStack out = r.assemble(in);
        h.assertTrue(out.is(Chemistry.DISTILLED_WATER) && out.getCount() == 3, "bucket + still + 3 bottles -> 3 distilled water, got " + out);
        var rem = r.getRemainingItems(in);
        h.assertTrue(rem.stream().anyMatch(st -> st.is(Items.BUCKET)) && rem.stream().anyMatch(st -> st.is(com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL)), "bucket and still come back");
        LOG.info("[LAB] distilled water [2-16]: water bucket + quartz still + 3 bottles -> 3 distilled water (still, bucket back)");
        h.succeed();
    }

    /** [2-02] カリ（IMP2-01）：原木 →たき火→ 木灰、木灰4＋水バケツ → 灰汁（バケツは戻る）、灰汁 →かまど→ 炭酸カリウム */
    @GameTest
    public void potash(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Items.OAK_LOG).is(Chemistry.WOOD_ASH), "log on a campfire -> wood ash");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Items.CRIMSON_STEM).isEmpty(), "nether stems are not wood");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.OAK_LOG).is(Items.CHARCOAL), "furnace (little air) still makes charcoal");
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 2, List.of(ItemStack.EMPTY, new ItemStack(Chemistry.WOOD_ASH), new ItemStack(Chemistry.WOOD_ASH),
                new ItemStack(Chemistry.WOOD_ASH), new ItemStack(Chemistry.WOOD_ASH), new ItemStack(Items.WATER_BUCKET)));
        var r = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
        h.assertTrue(r.assemble(input).is(Chemistry.LYE), "4 wood ash + water -> lye");
        h.assertTrue(r.getRemainingItems(input).stream().anyMatch(s -> s.is(Items.BUCKET)), "the bucket comes back");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.LYE).is(Chemistry.POTASH), "lye boiled down -> K2CO3");
        LOG.info("[LAB] potash [2-02]: log -> campfire -> wood ash; 4 ash + water -> lye (bucket back); lye -> furnace -> K2CO3");
        h.succeed();
    }

    /** [2-04] 海藻灰ソーダとヨウ素（IMP2、S-002）：乾燥昆布 →たき火→ 海藻灰、灰4＋水 → 浸出液（バケツは戻る）→かまど→ 炭酸ナトリウム、浸出液＋硫酸 → ヨウ素（瓶が戻る） */
    @GameTest
    public void kelpAshAndIodine(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Items.DRIED_KELP).is(Chemistry.KELP_ASH), "dried kelp burned in open air -> kelp ash");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.KELP).is(Items.DRIED_KELP), "vanilla kelp -> dried kelp stays");
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("kelp_liquor"));
        var ings = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value().placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Chemistry.KELP_ASH))).count(), 4L, "4 kelp ash");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.KELP_LIQUOR).is(Chemistry.SODIUM_CARBONATE), "boiled down -> soda");
        h.assertTrue(recipeUses(h, "iodine", Chemistry.KELP_LIQUOR) && recipeUses(h, "iodine", Chemistry.SULFURIC_ACID), "liquor + sulfuric acid -> iodine (Courtois 1811)");
        LOG.info("[LAB] kelp [2-04]: dried kelp -> campfire -> ash -> liquor -> soda (furnace) or + H2SO4 -> iodine");
        h.succeed();
    }

    // =====================================================================
    // [2-31] アスファルト：ニエプスのヘリオグラフ（IMP3、O-005）
    // =====================================================================

    /** [2-31] 天然アスファルトの生成：砂・赤い砂・砂岩・テラコッタだけを置き換える（石は残る） */
    @GameTest public void oreNaturalAsphalt(GameTestHelper h) { oreFeature(h, "natural_asphalt", Blocks.SAND, Blocks.STONE, Chemistry.NATURAL_ASPHALT,
            st -> st.is(Blocks.SAND) || st.is(Blocks.RED_SAND) || st.is(Blocks.SANDSTONE) || st.is(Blocks.TERRACOTTA) || st.is(Blocks.RED_SANDSTONE)); }

    /**
     * 天然アスファルト → 瀝青。剥片でトウヒから松やに → かまど → テレビン油。瀝青＋テレビン油 → 感光液、スズ2＋鉛1 → ピューター板3、感光液＋板 → ヘリオグラフの板。
     * 板はブロックの影では固まらない（空の光が日光のときだけ進む）。洗う：固まりきっていれば像、足りなければ全部溶けて板が戻る
     */
    @GameTest
    public void heliograph(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 5);
        // 材料の鎖
        var drops = Block.getDrops(Chemistry.NATURAL_ASPHALT.defaultBlockState(), lv, h.absolutePos(p(1, 1, 1)), null, null, new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(drops.stream().anyMatch(st -> st.is(Chemistry.ASPHALT)), "natural asphalt -> bitumen");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.PINE_RESIN).is(Chemistry.TURPENTINE), "pine resin distilled -> turpentine");
        h.assertTrue(recipeUses(h, "bitumen_varnish", Chemistry.ASPHALT) && recipeUses(h, "bitumen_varnish", Chemistry.TURPENTINE), "bitumen + turpentine -> varnish");
        h.assertTrue(recipeUses(h, "pewter_plate", Metallurgy.TIN_INGOT) && recipeUses(h, "pewter_plate", Metallurgy.LEAD_INGOT), "pewter = tin + lead");
        h.assertTrue(recipeUses(h, "heliograph_plate", Chemistry.BITUMEN_VARNISH) && recipeUses(h, "heliograph_plate", Chemistry.PEWTER_PLATE), "varnish on pewter -> heliograph plate");
        h.assertTrue(lv.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(SemiconductorLab.id("natural_asphalt")).isPresent(), "placed feature natural_asphalt");
        // 剥片でトウヒから松やに（剥片は1傷む）
        BlockPos log = p(3, 1, 3);
        h.setBlock(log, Blocks.SPRUCE_LOG);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Survival.FLINT_FLAKE));
        BlockPos la = h.absolutePos(log);
        var ctx = new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(la), net.minecraft.core.Direction.NORTH, la, false));
        h.assertTrue(player.getMainHandItem().getItem().useOn(ctx) == net.minecraft.world.InteractionResult.SUCCESS, "flake taps the spruce");
        h.assertTrue(!h.getEntities(net.minecraft.world.entity.EntityTypes.ITEM).stream().noneMatch(e -> e.getItem().is(Chemistry.PINE_RESIN)), "pine resin oozes out");
        h.assertValueEqual(player.getMainHandItem().getDamageValue(), 1, "the flake wears by 1");
        h.assertFalse(com.taku3.semiconductorlab.item.FlakeItem.isConiferLog(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()), "no resin from stripped logs");

        // 露光：影（上に石）では進まない。空の下では、日光のときだけ進む
        var E = com.taku3.semiconductorlab.block.HeliographPlateBlock.EXPOSURE;
        BlockPos shade = p(1, 1, 1), open = p(3, 1, 1);
        h.setBlock(shade, Chemistry.HELIOGRAPH_PLATE);
        h.setBlock(shade.above(), Blocks.STONE);
        h.setBlock(open, Chemistry.HELIOGRAPH_PLATE);
        RandomSource rnd = RandomSource.create(7);
        for (int i = 0; i < 5; i++) {
            h.getBlockState(shade).randomTick(lv, h.absolutePos(shade), rnd);
            h.getBlockState(open).randomTick(lv, h.absolutePos(open), rnd);
        }
        h.assertValueEqual(h.getBlockState(shade).getValue(E), 0, "in the shade of a block the bitumen does not harden");
        h.setBlock(p(4, 2, 4), Blocks.GLASS);
        h.assertTrue(com.taku3.semiconductorlab.block.HeliographPlateBlock.openToSky(lv, h.absolutePos(p(4, 1, 4))), "light reaches the plate through glass");
        h.assertFalse(com.taku3.semiconductorlab.block.HeliographPlateBlock.openToSky(lv, h.absolutePos(shade)), "not under stone");
        boolean sun = com.taku3.semiconductorlab.block.HeliographPlateBlock.sunlit(lv, h.absolutePos(open));
        h.assertValueEqual(h.getBlockState(open).getValue(E), sun ? com.taku3.semiconductorlab.block.HeliographPlateBlock.MAX_EXPOSURE : 0, "open sky hardens only in daylight (sunlit=" + sun + ")");
        // 洗う
        h.setBlock(open, Chemistry.HELIOGRAPH_PLATE.defaultBlockState().setValue(E, com.taku3.semiconductorlab.block.HeliographPlateBlock.MAX_EXPOSURE));
        h.assertTrue(com.taku3.semiconductorlab.block.HeliographPlateBlock.wash(lv, h.absolutePos(open), h.getBlockState(open)), "fully exposed -> image");
        h.assertTrue(h.getBlockState(open).is(Chemistry.HELIOGRAPH), "heliograph remains");
        h.setBlock(shade.above(), Blocks.AIR);
        h.setBlock(shade, Chemistry.HELIOGRAPH_PLATE.defaultBlockState().setValue(E, 2));
        h.assertFalse(com.taku3.semiconductorlab.block.HeliographPlateBlock.wash(lv, h.absolutePos(shade), h.getBlockState(shade)), "under-exposed -> all dissolves");
        h.assertTrue(h.getBlockState(shade).isAir(), "plate washed away");
        h.assertTrue(h.getEntities(net.minecraft.world.entity.EntityTypes.ITEM).stream().anyMatch(e -> e.getItem().is(Chemistry.PEWTER_PLATE)), "the pewter plate comes back");
        LOG.info("[LAB] heliograph [2-31]: asphalt -> bitumen, resin -> turpentine, varnish on pewter; shade 0, open {} (sunlit={}); washed -> image / dissolved", h.getBlockState(open), sun);
        h.succeed();
    }

    // =====================================================================
    // [2-29] ゴム：ゴムの木 → ラテックス → 生ゴム → 加硫ゴム（IMP3、O-005）
    // =====================================================================

    /** 苗木 → ゴムの木。剥片で幹 → ラテックス。ラテックス3＋木酢液 → 生ゴム、＋硫黄 → 焼く → 加硫ゴム。生ゴムは暑いとべたつき寒いと硬い、加硫ゴムは変わらない */
    @GameTest
    public void rubber(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 7);
        BlockPos sap = p(3, 1, 3);
        h.setBlock(sap.below(), Blocks.GRASS_BLOCK);
        h.setBlock(sap, Chemistry.RUBBER_SAPLING);
        // 木の成長（growTree）は GameTest の世界ではバニラのジャングルの木でも起きない（false が返る）ので、育つかは人間の確認（TEST_QUEUE）。ここでは定義を確かめる
        h.assertTrue(lv.registryAccess().lookupOrThrow(Registries.FEATURE).get(Chemistry.RUBBER_TREE_FEATURE).isPresent(), "tree feature rubber_tree is loaded");
        h.assertTrue(h.getBlockState(sap).is(Chemistry.RUBBER_SAPLING) && Chemistry.RUBBER_SAPLING.defaultBlockState().canSurvive(lv, h.absolutePos(sap)), "the sapling can be planted on grass");
        h.setBlock(sap, Chemistry.RUBBER_LOG);
        h.assertTrue(lv.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(SemiconductorLab.id("rubber_trees")).isPresent(), "placed feature rubber_trees (jungle)");
        // 剥片で幹からラテックス
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Survival.OBSIDIAN_FLAKE));
        BlockPos la = h.absolutePos(sap);
        var ctx = new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(la), net.minecraft.core.Direction.NORTH, la, false));
        player.getMainHandItem().getItem().useOn(ctx);
        h.assertTrue(h.getEntities(net.minecraft.world.entity.EntityTypes.ITEM).stream().anyMatch(e -> e.getItem().is(Chemistry.LATEX)), "latex oozes from the rubber tree");
        // レシピ
        h.assertTrue(recipeUses(h, "raw_rubber", Chemistry.LATEX) && recipeUses(h, "raw_rubber", Chemistry.WOOD_VINEGAR), "latex + acid (wood vinegar) -> raw rubber");
        h.assertTrue(recipeUses(h, "rubber_compound", Chemistry.RAW_RUBBER) && recipeUses(h, "rubber_compound", Chemistry.SULFUR_POWDER), "raw rubber + sulfur");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.RUBBER_COMPOUND).is(Chemistry.VULCANIZED_RUBBER), "baked -> vulcanized rubber");
        // 暑さ・寒さ
        ItemStack raw = new ItemStack(Chemistry.RAW_RUBBER);
        com.taku3.semiconductorlab.item.RawRubberItem.applyTemperature(raw, 2.0f);
        h.assertTrue(com.taku3.semiconductorlab.item.RawRubberItem.stateOf(raw) == com.taku3.semiconductorlab.item.RawRubberItem.State.STICKY, "raw rubber gets sticky in the heat (desert 2.0)");
        com.taku3.semiconductorlab.item.RawRubberItem.applyTemperature(raw, 0.95f);
        h.assertTrue(com.taku3.semiconductorlab.item.RawRubberItem.stateOf(raw) == com.taku3.semiconductorlab.item.RawRubberItem.State.STICKY, "sticky in the jungle too (0.95)");
        com.taku3.semiconductorlab.item.RawRubberItem.applyTemperature(raw, 0.0f);
        h.assertTrue(com.taku3.semiconductorlab.item.RawRubberItem.stateOf(raw) == com.taku3.semiconductorlab.item.RawRubberItem.State.STIFF, "stiff in the cold (snowy 0.0)");
        com.taku3.semiconductorlab.item.RawRubberItem.applyTemperature(raw, 0.7f);
        h.assertTrue(com.taku3.semiconductorlab.item.RawRubberItem.stateOf(raw) == com.taku3.semiconductorlab.item.RawRubberItem.State.NORMAL, "back to normal (0.7)");
        h.assertFalse(Chemistry.VULCANIZED_RUBBER instanceof com.taku3.semiconductorlab.item.RawRubberItem, "vulcanized rubber does not change with temperature");
        // 環化ゴム：生ゴム＋硫酸＋テレビン油 → 焼く。加硫ゴムは溶けないので使えない
        h.assertTrue(recipeUses(h, "cyclized_rubber_mix", Chemistry.RAW_RUBBER) && recipeUses(h, "cyclized_rubber_mix", Chemistry.SULFURIC_ACID)
                && recipeUses(h, "cyclized_rubber_mix", Chemistry.TURPENTINE), "raw rubber + sulfuric acid + turpentine -> cyclization mix");
        h.assertFalse(recipeUses(h, "cyclized_rubber_mix", Chemistry.VULCANIZED_RUBBER), "vulcanized rubber does not dissolve -> cannot be cyclized");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.CYCLIZED_RUBBER_MIX).is(Chemistry.CYCLIZED_RUBBER), "heated -> cyclized rubber");
        LOG.info("[LAB] rubber [2-29]: sapling -> rubber tree, flake -> latex, + wood vinegar -> raw rubber (sticky hot / stiff cold), + sulfur -> baked -> vulcanized");
        h.succeed();
    }

    // =====================================================================
    // [2-26] メタノール（IMP3、O-006）
    // =====================================================================

    /** 木酢液4＋消石灰 → 石灰を入れた木酢液4 → かまど（蒸留）→ メタノール。石灰なしの木酢液はメタノールにならない */
    @GameTest
    public void methanol(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "limed_wood_liquor", Chemistry.WOOD_VINEGAR) && recipeUses(h, "limed_wood_liquor", Survival.SLAKED_LIME), "wood vinegar + slaked lime");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.LIMED_WOOD_LIQUOR).is(Chemistry.METHANOL), "distilled -> methanol");
        h.assertFalse(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.WOOD_VINEGAR).is(Chemistry.METHANOL), "without lime the acetic acid comes over too -> no methanol");
        var ja = langJson("ja_jp");
        h.assertTrue(ja.get("item.semiconductor-lab.methanol.note2").getAsString().contains("GHS"), "methanol shows the GHS hazards");
        LOG.info("[LAB] methanol [2-26]: 4 wood vinegar + slaked lime -> limed liquor -> furnace -> methanol");
        h.succeed();
    }

    // =====================================================================
    // [2-27] ホルムアルデヒド：熱した銅の網（触媒）（IMP3、O-006）
    // =====================================================================

    /** 冷たい網では反応しない。火のついたたき火の上の網ならメタノール → ホルマリン、網は減らない（何度でも） */
    @GameTest
    public void formaldehyde(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 4);
        BlockPos gauze = p(1, 2, 1);
        h.setBlock(gauze, Chemistry.COPPER_GAUZE);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack meth = new ItemStack(Chemistry.METHANOL, 3);
        h.assertFalse(com.taku3.semiconductorlab.block.CopperGauzeBlock.react(lv, h.absolutePos(gauze), player, meth), "cold gauze -> no reaction");
        h.assertValueEqual(meth.getCount(), 3, "methanol not used");
        h.setBlock(gauze.below(), Blocks.CAMPFIRE);
        h.assertTrue(com.taku3.semiconductorlab.block.CopperGauzeBlock.react(lv, h.absolutePos(gauze), player, meth), "hot gauze over a lit campfire -> formalin");
        h.assertTrue(com.taku3.semiconductorlab.block.CopperGauzeBlock.react(lv, h.absolutePos(gauze), player, meth), "again: the catalyst is not used up");
        h.assertValueEqual(player.getInventory().countItem(Chemistry.FORMALIN), 2, "2 formalin");
        h.assertTrue(h.getBlockState(gauze).is(Chemistry.COPPER_GAUZE), "the gauze is still there");
        h.setBlock(gauze.below(), Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
        h.assertFalse(com.taku3.semiconductorlab.block.CopperGauzeBlock.react(lv, h.absolutePos(gauze), player, meth), "an unlit campfire does not heat it");
        h.assertTrue(recipeUses(h, "copper_gauze", Items.COPPER_INGOT), "gauze from copper");
        LOG.info("[LAB] formaldehyde [2-27]: cold gauze no, over lit campfire methanol -> formalin (x2, gauze stays)");
        h.succeed();
    }

    // =====================================================================
    // [2-25] フェノール：コールタール → 中油 → アルカリで抽出 → 酸で戻す（IMP3、O-006）
    // =====================================================================

    /** コールタール →かまど→ 中油、＋NaOH → フェノールのナトリウム塩の液、＋硫酸 → フェノール（瓶は戻る）。中油にいきなり硫酸ではできない */
    @GameTest
    public void phenol(GameTestHelper h) {
        var lv = h.getLevel();
        var mgr = lv.getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.COAL_TAR).is(Chemistry.CARBOLIC_OIL), "coal tar distilled -> middle oil");
        h.assertTrue(recipeUses(h, "sodium_phenolate", Chemistry.CARBOLIC_OIL) && recipeUses(h, "sodium_phenolate", Gas.SODIUM_HYDROXIDE), "middle oil + NaOH -> phenoxide");
        var in = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Chemistry.SODIUM_PHENOLATE), new ItemStack(Chemistry.SULFURIC_ACID)));
        var r = mgr.getRecipeFor(CRAFT, in, lv).orElseThrow().value();
        h.assertTrue(r.assemble(in).is(Chemistry.PHENOL), "phenoxide + sulfuric acid -> phenol");
        h.assertTrue(r.getRemainingItems(in).stream().anyMatch(st -> !st.isEmpty()), "the acid bottle comes back");
        var direct = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Chemistry.CARBOLIC_OIL), new ItemStack(Chemistry.SULFURIC_ACID)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, direct, lv).isEmpty(), "acid straight into the oil does not separate phenol");
        LOG.info("[LAB] phenol [2-25]: coal tar -> middle oil -> + NaOH -> phenoxide -> + H2SO4 -> phenol");
        h.succeed();
    }

    // =====================================================================
    // [2-28] フェノール樹脂：ノボラックとベークライト（IMP3、O-006）
    // =====================================================================

    /** フェノール2＋ホルマリン＋塩酸 → 焼く → ノボラック。フェノール＋ホルマリン2＋NaOH → 焼く → ベークライト（かまどに入れても溶けない） */
    @GameTest
    public void phenolicResin(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "novolac_mix", Chemistry.PHENOL) && recipeUses(h, "novolac_mix", Chemistry.FORMALIN) && recipeUses(h, "novolac_mix", Gas.HYDROCHLORIC_ACID), "acid catalyst, extra phenol");
        h.assertTrue(cook(h, SMELT, Chemistry.NOVOLAC_MIX).is(Chemistry.NOVOLAC), "-> novolac");
        h.assertTrue(recipeUses(h, "resol_mix", Chemistry.PHENOL) && recipeUses(h, "resol_mix", Chemistry.FORMALIN) && recipeUses(h, "resol_mix", Gas.SODIUM_HYDROXIDE), "base catalyst, extra formaldehyde");
        h.assertTrue(cook(h, SMELT, Chemistry.RESOL_MIX).is(Chemistry.BAKELITE), "-> Bakelite");
        h.assertTrue(cook(h, SMELT, Chemistry.BAKELITE).isEmpty(), "Bakelite does not melt in a furnace (thermoset)");
        // 触媒の取り違え：酸で配合だけ変えたり、触媒なしでは作れない
        var lv = h.getLevel(); var mgr = lv.getServer().getRecipeManager(); var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        var noCat = net.minecraft.world.item.crafting.CraftingInput.of(3, 1, List.of(new ItemStack(Chemistry.PHENOL), new ItemStack(Chemistry.PHENOL), new ItemStack(Chemistry.FORMALIN)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, noCat, lv).isEmpty(), "no catalyst -> no resin");
        LOG.info("[LAB] phenolic resin [2-28]: acid + extra phenol -> novolac (melts); base + extra formaldehyde -> Bakelite (does not melt)");
        h.succeed();
    }

    // =====================================================================
    // [2-24] ベンゼン・キシレン：温度で分ける分留（IMP3、O-007）
    // =====================================================================

    /** コールタール：たき火 → 軽油、かまど → 中油。軽油：たき火 → ベンゼン、かまど → キシレン */
    @GameTest
    public void benzeneXylene(GameTestHelper h) {
        var CAMP = net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING;
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, CAMP, Chemistry.COAL_TAR).is(Chemistry.LIGHT_OIL), "gentle heat -> light oil");
        h.assertTrue(cook(h, SMELT, Chemistry.COAL_TAR).is(Chemistry.CARBOLIC_OIL), "strong heat -> middle oil");
        h.assertTrue(cook(h, CAMP, Chemistry.LIGHT_OIL).is(Chemistry.BENZENE), "light oil, low temperature -> benzene (80 C)");
        h.assertTrue(cook(h, SMELT, Chemistry.LIGHT_OIL).is(Chemistry.XYLENE), "light oil, higher temperature -> xylene (140 C)");
        var ja = langJson("ja_jp");
        h.assertTrue(ja.get("item.semiconductor-lab.benzene.note2").getAsString().contains("発がん性"), "benzene warns of carcinogenicity");
        LOG.info("[LAB] benzene/xylene [2-24]: tar campfire -> light oil (furnace -> middle oil); light oil campfire -> benzene, furnace -> xylene");
        h.succeed();
    }

    // =====================================================================
    // [2-57] DNQ（ジアゾナフトキノン）：ポジ型レジストの感光剤（IMP3、O-007）
    // =====================================================================

    /** 中油＋氷 → ナフタレン → スルホン化 → アルカリ融解 → 2-ナフトール → トビアス酸。硝石＋鉛 → 焼く → 亜硝酸カリウム。トビアス酸＋亜硝酸塩＋塩酸＋氷 → DNQ。DNQ は光で分解（戻らない） */
    @GameTest
    public void dnq(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "naphthalene", Chemistry.CARBOLIC_OIL) && recipeUses(h, "naphthalene", Items.ICE), "middle oil cooled -> naphthalene");
        h.assertTrue(recipeUses(h, "naphthalenesulfonic_acid", Chemistry.SULFURIC_ACID), "sulfonation");
        h.assertTrue(cook(h, SMELT, Chemistry.NAPHTHOL_MELT).is(Chemistry.NAPHTHOL), "alkali fusion -> 2-naphthol");
        h.assertTrue(recipeUses(h, "tobias_acid", Chemistry.NAPHTHOL) && recipeUses(h, "tobias_acid", Chemistry.AMMONIACAL_LIQUOR) && recipeUses(h, "tobias_acid", Chemistry.SULFUR_DIOXIDE), "Bucherer: ammonia + sulfite -> Tobias acid");
        h.assertTrue(recipeUses(h, "nitrite_mix", Gas.SALTPETER) && recipeUses(h, "nitrite_mix", Metallurgy.LEAD_INGOT), "saltpeter + lead");
        h.assertTrue(cook(h, SMELT, Chemistry.NITRITE_MIX).is(Chemistry.POTASSIUM_NITRITE), "KNO3 + Pb -> KNO2");
        h.assertTrue(recipeUses(h, "dnq", Chemistry.TOBIAS_ACID) && recipeUses(h, "dnq", Chemistry.POTASSIUM_NITRITE) && recipeUses(h, "dnq", Gas.HYDROCHLORIC_ACID) && recipeUses(h, "dnq", Items.ICE), "diazotization in the cold -> DNQ");
        ItemStack dnq = new ItemStack(Chemistry.DNQ);
        h.assertFalse(com.taku3.semiconductorlab.item.DnqItem.decomposed(dnq), "fresh DNQ");
        h.assertTrue(com.taku3.semiconductorlab.item.DnqItem.expose(dnq), "light decomposes it");
        h.assertTrue(com.taku3.semiconductorlab.item.DnqItem.decomposed(dnq) && !com.taku3.semiconductorlab.item.DnqItem.expose(dnq), "and it stays decomposed");
        LOG.info("[LAB] DNQ [2-57]: middle oil + ice -> naphthalene -> sulfonic acid -> naphthol -> Tobias acid; KNO3 + Pb -> KNO2; diazotized -> DNQ (decomposes in light)");
        h.succeed();
    }

    // =====================================================================
    // [2-17] イオン交換（IMP3、O-008）
    // =====================================================================

    /** 蒸留水 → 脱イオン水（8回）。どちらかの樹脂が尽きると止まる。硫酸で陽イオン側、NaOH で陰イオン側を再生 */
    @GameTest
    public void ionExchange(GameTestHelper h) {
        var lv = h.getLevel();
        floor(h, 3);
        BlockPos col = p(1, 1, 1);
        h.setBlock(col, Chemistry.ION_EXCHANGE_COLUMN);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int cap = com.taku3.semiconductorlab.block.IonExchangeColumnBlock.CAPACITY;
        ItemStack water = new ItemStack(Chemistry.DISTILLED_WATER, 16);
        for (int i = 0; i < cap; i++)
            h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, water), "deionized", "use " + i);
        h.assertValueEqual(player.getInventory().countItem(Chemistry.DEIONIZED_WATER), cap, "8 deionized water");
        h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, water), "spent_cation", "spent -> no more");
        h.assertValueEqual(water.getCount(), 16 - cap, "spent column does not take water");
        h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, new ItemStack(Chemistry.SULFURIC_ACID)), "regen_cation", "acid regenerates the cation side");
        h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, water), "spent_anion", "the anion side is still spent");
        h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, new ItemStack(Gas.SODIUM_HYDROXIDE)), "regen_anion", "NaOH regenerates the anion side");
        h.assertValueEqual(com.taku3.semiconductorlab.block.IonExchangeColumnBlock.use(lv, h.absolutePos(col), player, water), "deionized", "works again");
        h.assertTrue(recipeUses(h, "cation_exchange_resin", Chemistry.BAKELITE) && recipeUses(h, "cation_exchange_resin", Chemistry.SULFURIC_ACID), "sulfonated phenolic resin");
        h.assertTrue(recipeUses(h, "anion_exchange_resin", Chemistry.NOVOLAC) && recipeUses(h, "anion_exchange_resin", Chemistry.AMMONIACAL_LIQUOR), "aminated phenolic resin");
        LOG.info("[LAB] ion exchange [2-17]: distilled -> deionized x{}, spent, regenerated with H2SO4 / NaOH", cap);
        h.succeed();
    }

    // =====================================================================
    // [2-42] 酢酸（IMP3、O-008）
    // =====================================================================

    /** 石灰を入れた木酢液：かまど → メタノール、たき火（煮つめ）→ 酢酸カルシウム。＋硫酸 → 氷酢酸2。氷酢酸は寒い所で凍り、暖かい所でとける */
    @GameTest
    public void aceticAcid(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Chemistry.LIMED_WOOD_LIQUOR).is(Chemistry.CALCIUM_ACETATE), "boiled down -> calcium acetate");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.LIMED_WOOD_LIQUOR).is(Chemistry.METHANOL), "distilled -> still methanol");
        h.assertTrue(recipeUses(h, "acetic_acid", Chemistry.CALCIUM_ACETATE) && recipeUses(h, "acetic_acid", Chemistry.SULFURIC_ACID), "calcium acetate + sulfuric acid -> acetic acid");
        ItemStack aa = new ItemStack(Chemistry.ACETIC_ACID);
        h.assertTrue(com.taku3.semiconductorlab.item.GlacialAceticAcidItem.applyTemperature(aa, 0.0f) && com.taku3.semiconductorlab.item.GlacialAceticAcidItem.frozen(aa), "freezes in the cold");
        h.assertTrue(com.taku3.semiconductorlab.item.GlacialAceticAcidItem.applyTemperature(aa, 0.8f) && !com.taku3.semiconductorlab.item.GlacialAceticAcidItem.frozen(aa), "melts again where it is warm");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.CALCIUM_ACETATE).is(Chemistry.ACETONE), "[2-40] calcium acetate baked -> acetone");
        LOG.info("[LAB] acetic acid [2-42]: limed liquor campfire -> calcium acetate (furnace -> methanol); + H2SO4 -> glacial acetic acid (freezes in the cold)");
        h.succeed();
    }

    // =====================================================================
    // [2-09] 石けん（IMP3、O-008）
    // =====================================================================

    /** 牛・豚・羊が獣脂を落とす。獣脂3＋NaOH＋水 → たき火 → けん化した脂、＋塩 → 石けん3（残りの液が手元に残る）、残りの液 → かまど → グリセリン */
    @GameTest
    public void soap(GameTestHelper h) {
        var lv = h.getLevel();
        floor(h, 4);
        var cow = h.spawn(net.minecraft.world.entity.EntityTypes.COW, p(1, 1, 1));
        int tallow = 0, N = 200;
        var key = net.minecraft.world.entity.EntityTypes.COW.getDefaultLootTable().orElseThrow();
        var table = lv.getServer().reloadableRegistries().getLootTable(key);
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(lv)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, cow)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, cow.position())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, lv.damageSources().generic())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
        for (int i = 0; i < N; i++) for (ItemStack st : table.getRandomItems(params)) if (st.is(Chemistry.TALLOW)) tallow++;
        h.assertTrue(tallow > N * 0.4 && tallow < N * 0.8, "cows drop tallow ~60%: " + tallow + "/" + N);
        h.assertTrue(recipeUses(h, "soap_batch", Chemistry.TALLOW) && recipeUses(h, "soap_batch", Gas.SODIUM_HYDROXIDE), "tallow + NaOH + water");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Chemistry.SOAP_BATCH).is(Chemistry.SAPONIFIED_TALLOW), "boiled -> saponified");
        var mgr = lv.getServer().getRecipeManager(); var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        var in = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Chemistry.SAPONIFIED_TALLOW), new ItemStack(Gas.SALT)));
        var r = mgr.getRecipeFor(CRAFT, in, lv).orElseThrow().value();
        h.assertTrue(r.assemble(in).is(Chemistry.SOAP), "salting out -> soap");
        h.assertTrue(r.getRemainingItems(in).stream().anyMatch(st -> st.is(Chemistry.SPENT_LYE)), "the glycerin water stays in the grid");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.SPENT_LYE).is(Chemistry.GLYCERIN), "spent lye -> glycerin");
        LOG.info("[LAB] soap [2-09]: cow tallow {}/{}; tallow + NaOH + water -> boil -> + salt -> soap + spent lye -> glycerin", tallow, N);
        h.succeed();
    }

    // =====================================================================
    // [2-41] エポキシ樹脂（IMP3、O-008）
    // =====================================================================

    /** フェノール2＋アセトン＋HCl → BPA。グリセリン＋HCl2 → ジクロロヒドリン → ＋NaOH → エピクロロヒドリン。両方＋NaOH → 樹脂。＋ノボラック → 成形材料 → かまど → 硬化。樹脂だけでは固まらない */
    @GameTest
    public void epoxy(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "bisphenol_a", Chemistry.PHENOL) && recipeUses(h, "bisphenol_a", Chemistry.ACETONE), "phenol + acetone -> bisphenol A");
        h.assertTrue(recipeUses(h, "dichlorohydrin", Chemistry.GLYCERIN) && recipeUses(h, "epichlorohydrin", Chemistry.DICHLOROHYDRIN), "glycerin -> dichlorohydrin -> epichlorohydrin");
        h.assertTrue(recipeUses(h, "epoxy_resin", Chemistry.BISPHENOL_A) && recipeUses(h, "epoxy_resin", Chemistry.EPICHLOROHYDRIN), "BPA + epichlorohydrin -> epoxy resin");
        h.assertTrue(recipeUses(h, "epoxy_molding_compound", Chemistry.NOVOLAC), "novolac is the hardener (as in IC molding compounds)");
        h.assertTrue(cook(h, SMELT, Chemistry.EPOXY_MOLDING_COMPOUND).is(Chemistry.CURED_EPOXY), "heated -> cured epoxy");
        h.assertTrue(cook(h, SMELT, Chemistry.EPOXY_RESIN).isEmpty(), "resin without hardener does not set");
        h.assertTrue(cook(h, SMELT, Chemistry.CURED_EPOXY).isEmpty(), "cured epoxy does not melt");
        LOG.info("[LAB] epoxy [2-41]: BPA + epichlorohydrin (from glycerin) -> resin; + novolac -> molding compound -> cured; resin alone does not set");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Chemistry.PEROXIDE_CELL.block, "過酸化水素", L(io(Chemistry.GLASS_BOTTLE, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_WATER), perRuns(io(Chemistry.SULFURIC_ACID, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ACID), perRuns(io(com.taku3.semiconductorlab.content.Metallurgy.PLATINUM_INGOT, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ANODE)),
            L(io(Chemistry.HYDROGEN_PEROXIDE, 1), mol(com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS, 1)), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.WATTS, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS / 20.0, "白金の陽極で硫酸 → 過硫酸 → 水で分解して H2O2（硫酸は戻る）。全体 2H2O → H2O2 + H2");
        rec(a, Chemistry.AMMONIA_STILL.block, "アンモニア水", L(io(Chemistry.AMMONIACAL_LIQUOR, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.LIQUOR_PER_RUN), io(com.taku3.semiconductorlab.content.Survival.SLAKED_LIME, 1), io(Chemistry.GLASS_BOTTLE, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.RUNS_PER_WATER)),
            L(io(Chemistry.AMMONIA_WATER, 1)), com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.WATTS, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS / 20.0, "安水を熱し、消石灰で塩のアンモニアも追い出して水に吸わせる");
        rec(a, Chemistry.WOOD_RETORT.block, "木を乾留する", L(io(Items.OAK_LOG, 4), io(Items.COAL, 0.5)),
            L(io(Items.CHARCOAL, 4), io(Chemistry.WOOD_VINEGAR, 2), io(Chemistry.WOOD_TAR, 1)), 0, 4 * com.taku3.semiconductorlab.block.WoodRetortBlockEntity.DISTILL_TICKS / 20.0,
            "空気なしで約450℃（石炭・木炭1個で原木8本、電気なし）");
        rec(a, Chemistry.GAS_RETORT.block, "石炭を乾留する", L(io(Items.COAL, 8), io(Items.COAL, 1)),
            L(io(com.taku3.semiconductorlab.content.Metallurgy.COKE, 6), io(Chemistry.AMMONIACAL_LIQUOR, 2), io(Chemistry.COAL_TAR, 1)), 0, 8 * com.taku3.semiconductorlab.block.GasRetortBlockEntity.DISTILL_TICKS / 20.0,
            "空気なしで約1000℃（燃料は石炭・木炭・コークス1個で石炭8個、電気なし）。石炭ガスは回収しない");
        rec(a, Chemistry.LEAD_RETORT.block, "フッ酸", L(io(com.taku3.semiconductorlab.content.Metallurgy.FLUORITE, 1), io(Chemistry.SULFURIC_ACID, 1), io(com.taku3.semiconductorlab.content.Metallurgy.LEAD_BOTTLE, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.HfRetortBlockEntity.RUNS_PER_WATER), io(Items.COAL, 0.125)),
            L(io(Chemistry.HYDROFLUORIC_ACID, 1), io(Chemistry.CALCIUM_SULFATE, 1), io(Chemistry.GLASS_BOTTLE, 1)), 0, com.taku3.semiconductorlab.block.HfRetortBlockEntity.REACT_TICKS / 20.0,
            "CaF2 + H2SO4 → CaSO4 + 2HF（約200℃）。HF を受け器の水に吸わせる。釜も瓶も鉛（ガラスは溶ける）");
    }
}
