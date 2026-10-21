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
        // ポリエチレンの瓶（S-006）：空の PE の瓶なら PE の瓶入りのフッ酸。どちらも HF のタグ
        h.assertTrue(com.taku3.semiconductorlab.block.HfRetortBlockEntity.hfFor(new ItemStack(Chemistry.POLYETHYLENE_BOTTLE)).is(Chemistry.HYDROFLUORIC_ACID_PE) && com.taku3.semiconductorlab.block.HfRetortBlockEntity.hfFor(new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.LEAD_BOTTLE)).is(Chemistry.HYDROFLUORIC_ACID), "bottle decides the HF item");
        h.assertTrue(new ItemStack(Chemistry.HYDROFLUORIC_ACID_PE).is(Chemistry.HF_TAG) && new ItemStack(Chemistry.HYDROFLUORIC_ACID).is(Chemistry.HF_TAG), "both in the HF tag");
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.isAcid(new ItemStack(Chemistry.HYDROFLUORIC_ACID_PE)), "etching tank takes HF in PE");
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
        h.assertTrue(cook(h, SMELT, Chemistry.RESOL_MIX).is(Chemistry.FOAMY_BAKELITE), "furnace (no pressure) -> foamy Bakelite (O-011: real Bakelite needs the autoclave)");
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
        h.assertTrue(recipeUses(h, "dnq", Chemistry.TOBIAS_ACID) && recipeUses(h, "dnq", Chemistry.BLEACHING_POWDER) && recipeUses(h, "dnq", Chemistry.POTASSIUM_NITRITE) && recipeUses(h, "dnq", Gas.HYDROCHLORIC_ACID) && recipeUses(h, "dnq", Items.ICE), "diazotization in the cold -> DNQ");
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

    // =====================================================================
    // [2-39] エタノール（IMP3、O-009）
    // =====================================================================

    /** 果実＋砂糖＋水 → 酵母。砂糖3＋酵母＋水 → 砂糖水。かめに仕込む → 3段発酵 → 発酵した液2＋酵母1。液3＋蒸留器＋瓶 → エタノール。発酵前は取り出せない */
    @GameTest
    public void ethanol(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "wild_yeast_from_berries", net.minecraft.world.item.Items.SWEET_BERRIES) && recipeUses(h, "wild_yeast_from_apple", net.minecraft.world.item.Items.APPLE), "yeast from fruit skins");
        h.assertTrue(recipeUses(h, "sugar_wash", Chemistry.WILD_YEAST), "sugar wash needs yeast");
        h.assertTrue(recipeUses(h, "ethanol", Chemistry.FERMENTED_WASH) && recipeUses(h, "ethanol", com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL), "distil fermented wash");
        h.assertTrue(recipeUses(h, "fermentation_crock", com.taku3.semiconductorlab.content.Survival.EARTHENWARE_POT), "crock from earthenware");
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, Chemistry.FERMENTATION_CROCK);
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.assertFalse(com.taku3.semiconductorlab.block.FermentationCrockBlock.collect(level, pos, player), "empty crock gives nothing");
        ItemStack wash = new ItemStack(Chemistry.SUGAR_WASH, 2);
        h.assertTrue(com.taku3.semiconductorlab.block.FermentationCrockBlock.fill(level, pos, player, wash) && wash.getCount() == 1, "filled");
        h.assertFalse(com.taku3.semiconductorlab.block.FermentationCrockBlock.fill(level, pos, player, wash), "cannot fill twice");
        h.assertFalse(com.taku3.semiconductorlab.block.FermentationCrockBlock.collect(level, pos, player), "not done yet");
        for (int i = 0; i < 3; i++) com.taku3.semiconductorlab.block.FermentationCrockBlock.ferment(level, pos);
        h.assertTrue(com.taku3.semiconductorlab.block.FermentationCrockBlock.collect(level, pos, player), "done");
        h.assertTrue(player.getInventory().countItem(Chemistry.FERMENTED_WASH) == 2 && player.getInventory().countItem(Chemistry.WILD_YEAST) == 1, "2 wash + yeast back");
        h.assertTrue(level.getBlockState(pos).getValue(com.taku3.semiconductorlab.block.FermentationCrockBlock.STAGE) == 0, "crock empty again");
        LOG.info("[LAB] ethanol [2-39]: fruit -> wild yeast -> sugar wash -> crock (3 stages) -> 2 fermented wash + yeast -> still -> ethanol 95.6%");
        h.succeed();
    }

    // =====================================================================
    // [2-36] エチレン（IMP3、O-009）
    // =====================================================================

    /** 冷たい管では何も起きない。火のついたたき火の上ならエタノール → エチレン（袋）＋空のガラス瓶、管は残る */
    @GameTest
    public void ethylene(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "alumina_catalyst_tube", com.taku3.semiconductorlab.content.Metallurgy.ALUMINA), "tube is packed with alumina");
        floor(h, 3);
        BlockPos fire = p(1, 1, 1);
        h.setBlock(fire.above(), Chemistry.ALUMINA_CATALYST_TUBE);
        BlockPos tube = h.absolutePos(fire.above());
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack eth = new ItemStack(Chemistry.ETHANOL, 2);
        h.assertFalse(com.taku3.semiconductorlab.block.AluminaCatalystTubeBlock.react(level, tube, player, eth), "cold tube does not react");
        h.setBlock(fire, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        h.assertTrue(com.taku3.semiconductorlab.block.AluminaCatalystTubeBlock.react(level, tube, player, eth), "hot tube reacts");
        h.assertTrue(eth.getCount() == 1 && player.getInventory().countItem(Chemistry.ETHYLENE_GAS) == 1 && player.getInventory().countItem(Chemistry.GLASS_BOTTLE) == 1, "ethylene bag + empty bottle");
        h.assertTrue(level.getBlockState(tube).is(Chemistry.ALUMINA_CATALYST_TUBE), "catalyst stays");
        LOG.info("[LAB] ethylene [2-36]: cold tube no; over lit campfire ethanol -> ethylene bag + bottle, alumina stays");
        h.succeed();
    }

    // =====================================================================
    // [2-37] ポリエチレン（IMP3、O-009）
    // =====================================================================

    /** 酸素なしでは始まらない。エチレン4＋酸素 → ポリエチレン、酸素1袋で16回。電気が半分だと終わらない */
    @GameTest(maxTicks = 100)
    public void polyethylene(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.HIGH_PRESSURE_REACTOR.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.WATTS);
        int T = com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.TICKS;
        c.setItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_ETHYLENE, new ItemStack(Chemistry.ETHYLENE_GAS, 12));
        tickConsumer(h, fp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_OUT).isEmpty(), "no oxygen -> no polymerization");
        c.setItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_OXYGEN, new ItemStack(com.taku3.semiconductorlab.content.Gas.OXYGEN_GAS, 1));
        tickConsumer(h, fp, be, T, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        for (int i = 0; i < 3; i++) tickConsumer(h, fp, be, T, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Chemistry.POLYETHYLENE) && out.getCount() == 3, "12 ethylene -> 3 polyethylene, got " + out);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.SLOT_OXYGEN).isEmpty() && be.getOxygen() == com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.RUNS_PER_OXYGEN - 3, "1 oxygen bag = 16 runs");
        LOG.info("[LAB] polyethylene [2-37]: no O2 -> nothing; 4 ethylene + trace O2 (1 bag / 16 runs) -> polyethylene at 5 kW");
        h.succeed();
    }

    // =====================================================================
    // [2-38] PTFE の前半：さらし粉・クロロホルム（IMP3、O-009）
    // =====================================================================

    /** 消石灰＋塩素 → さらし粉。エタノール＋さらし粉2＋蒸留器＋瓶 → クロロホルム（ガラス瓶） */
    @GameTest
    public void chloroform(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "bleaching_powder", com.taku3.semiconductorlab.content.Survival.SLAKED_LIME) && recipeUses(h, "bleaching_powder", com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS), "lime + chlorine -> bleaching powder");
        h.assertTrue(recipeUses(h, "chloroform", Chemistry.ETHANOL) && recipeUses(h, "chloroform", Chemistry.BLEACHING_POWDER), "ethanol + bleaching powder -> chloroform");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.CHLOROFORM) == com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE, "chloroform in a glass bottle");
        LOG.info("[LAB] chloroform [2-38 first half]: slaked lime + Cl2 -> bleaching powder; ethanol + 2 bleaching powder -> chloroform (Swarts step waits for [1-43] antimony)");
        h.succeed();
    }

    // =====================================================================
    // [2-58] 無水酢酸・[2-59] ケイ皮酸（IMP3、O-010、KPR の材料）
    // =====================================================================

    /** P＋Cl2 → PCl3、酢酸＋PCl3 → 塩化アセチル、＋酢酸ナトリウム → 無水酢酸。軽油 → トルエン → 塩化ベンザル → ベンズアルデヒド。パーキン → かまど → ケイ皮酸 → 塩化シンナモイル */
    @GameTest
    public void cinnamicAcid(GameTestHelper h) {
        var CL = com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS;
        h.assertTrue(recipeUses(h, "phosphorus_trichloride", Chemistry.PHOSPHORUS) && recipeUses(h, "phosphorus_trichloride", CL), "P + Cl2 -> PCl3");
        h.assertTrue(recipeUses(h, "acetyl_chloride", Chemistry.ACETIC_ACID) && recipeUses(h, "acetyl_chloride", Chemistry.PHOSPHORUS_TRICHLORIDE), "acetic acid + PCl3 -> acetyl chloride");
        h.assertTrue(recipeUses(h, "acetic_anhydride", Chemistry.ACETYL_CHLORIDE) && recipeUses(h, "acetic_anhydride", Chemistry.SODIUM_ACETATE), "Gerhardt: acetyl chloride + sodium acetate");
        h.assertTrue(recipeUses(h, "toluene", Chemistry.LIGHT_OIL) && recipeUses(h, "benzal_chloride", Chemistry.TOLUENE) && recipeUses(h, "benzaldehyde", Chemistry.BENZAL_CHLORIDE), "toluene -> benzal chloride -> benzaldehyde");
        h.assertTrue(recipeUses(h, "perkin_mix", Chemistry.BENZALDEHYDE) && recipeUses(h, "perkin_mix", Chemistry.ACETIC_ANHYDRIDE) && recipeUses(h, "perkin_mix", Chemistry.SODIUM_ACETATE), "Perkin: benzaldehyde + anhydride + acetate");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.PERKIN_MIX).is(Chemistry.CINNAMIC_ACID), "heated -> cinnamic acid");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.BENZALDEHYDE).isEmpty(), "benzaldehyde alone does not become cinnamic acid");
        h.assertTrue(recipeUses(h, "cinnamoyl_chloride", Chemistry.CINNAMIC_ACID), "cinnamic acid -> cinnamoyl chloride");
        LOG.info("[LAB] cinnamic acid [2-58][2-59]: PCl3 -> acetyl chloride -> acetic anhydride; toluene -> benzal chloride -> benzaldehyde; Perkin -> cinnamic acid -> cinnamoyl chloride");
        h.succeed();
    }

    // =====================================================================
    // [2-28] 加圧加熱釜（オートクレーブ、IMP3、O-011）
    // =====================================================================

    /** 水なし → 1気圧のまま → 泡だらけ。水あり・200℃ → 蒸気で約12気圧 → ベークライト2。温度が足りないと進まない。飽和水蒸気圧の目安 */
    @GameTest(maxTicks = 2000)
    public void autoclave(GameTestHelper h) {
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.steamAtm(100) - 1.0) < 0.05 && Math.abs(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.steamAtm(150) - 4.7) < 0.4, "saturated steam: 1 atm at 100 C, about 4.7 atm at 150 C");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.AUTOCLAVE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.AutoclaveBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS);
        int T = 20 * 30;
        // 1) 温度が足りないと進まない（電気なし＝冷えたまま）
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, new ItemStack(Chemistry.RESOL_MIX, 2));
        tickConsumer(h, fp, be, T + 5, 0);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).isEmpty(), "cold -> nothing");
        // 2) 水なし：熱くても 1気圧 → 泡だらけ
        be.setTemperatureForTest(200);
        tickConsumer(h, fp, be, T + 5, feed);
        h.assertTrue(be.getPressureAtm() < 1.01, "no water -> 1 atm");
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Chemistry.FOAMY_BAKELITE) && out.getCount() == 2, "no pressure -> foamy Bakelite, got " + out);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        // 3) 水あり：蒸気の圧力 → ベークライト、空のバケツ
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_WATER, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        be.setTemperatureForTest(200);
        tickConsumer(h, fp, be, T + 5, feed);
        h.assertTrue(be.getPressureAtm() >= 5, "steam pressure >= 5 atm, got " + be.getPressureAtm());
        out = c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Chemistry.BAKELITE) && out.getCount() == 2, "under pressure -> Bakelite, got " + out);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_BUCKET).is(net.minecraft.world.item.Items.BUCKET) && be.getWater() == com.taku3.semiconductorlab.block.AutoclaveBlockEntity.RUNS_PER_WATER - 1, "1 bucket = 4 runs");
        // 4) 表で探せる（ほかの担当が1行足す形）
        h.assertTrue(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.find(new ItemStack(Chemistry.RESOL_MIX)) != null && com.taku3.semiconductorlab.block.AutoclaveBlockEntity.find(new ItemStack(Chemistry.NOVOLAC_MIX)) == null, "recipe table lookup");
        LOG.info("[LAB] autoclave [2-28]: cold no; dry 1 atm -> foamy Bakelite; water -> steam ~12 atm -> Bakelite x2; 1 bucket = 4 runs");
        h.succeed();
    }

    // =====================================================================
    // [2-60] ポリビニルアルコール・[2-61] ポリケイ皮酸ビニル（IMP3、O-010、KPR の材料）
    // =====================================================================

    /** カーバイド＋水 → アセチレン、＋酢酸＋硫酸水銀（戻る）→ 酢酸ビニル → ＋H2O2 → PVAc → ＋メタノール＋NaOH → PVA → ＋塩化シンナモイル → ポリケイ皮酸ビニル */
    @GameTest
    public void polyvinylCinnamate(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "acetylene_gas", com.taku3.semiconductorlab.content.Heat.CALCIUM_CARBIDE), "carbide + water -> acetylene");
        h.assertTrue(recipeUses(h, "vinyl_acetate", Chemistry.ACETYLENE_GAS) && recipeUses(h, "vinyl_acetate", Chemistry.ACETIC_ACID) && recipeUses(h, "vinyl_acetate", Chemistry.MERCURIC_SULFATE), "Klatte: acetylene + acetic acid, Hg catalyst");
        var rem = Chemistry.MERCURIC_SULFATE.getCraftingRemainder(new ItemStack(Chemistry.MERCURIC_SULFATE));
        h.assertTrue(rem != null && rem.create().is(Chemistry.MERCURIC_SULFATE), "catalyst stays on the crafting table");
        h.assertTrue(recipeUses(h, "polyvinyl_acetate", Chemistry.VINYL_ACETATE) && recipeUses(h, "polyvinyl_acetate", Chemistry.HYDROGEN_PEROXIDE), "radical polymerization");
        h.assertTrue(recipeUses(h, "polyvinyl_alcohol", Chemistry.POLYVINYL_ACETATE) && recipeUses(h, "polyvinyl_alcohol", Chemistry.METHANOL), "saponification -> PVA");
        h.assertTrue(recipeUses(h, "polyvinyl_cinnamate", Chemistry.POLYVINYL_ALCOHOL) && recipeUses(h, "polyvinyl_cinnamate", Chemistry.CINNAMOYL_CHLORIDE), "PVA + cinnamoyl chloride -> KPR material");
        LOG.info("[LAB] polyvinyl cinnamate [2-60][2-61]: carbide -> acetylene -> vinyl acetate (Hg cat.) -> PVAc -> PVA -> + cinnamoyl chloride -> polyvinyl cinnamate");
        h.succeed();
    }

    // =====================================================================
    // [2-38] PTFE の後半（IMP3、S-002）
    // =====================================================================

    /** Sb＋Cl2 → SbCl5（触媒、戻る）。CHCl3＋2HF＋SbCl5 → R-22。熱分解炉：R-22 2 → TFE＋HCl2。加圧加熱釜：TFE 4 → PTFE */
    @GameTest(maxTicks = 2000)
    public void ptfe(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "antimony_pentachloride", com.taku3.semiconductorlab.content.Metallurgy.ANTIMONY), "Sb + Cl2 -> SbCl5");
        h.assertTrue(recipeUses(h, "chlorodifluoromethane_gas", Chemistry.CHLOROFORM) && recipeUses(h, "chlorodifluoromethane_gas", Chemistry.HYDROFLUORIC_ACID) && recipeUses(h, "chlorodifluoromethane_gas", Chemistry.ANTIMONY_PENTACHLORIDE), "Swarts: CHCl3 + 2HF with SbCl5");
        var rem = Chemistry.ANTIMONY_PENTACHLORIDE.getCraftingRemainder(new ItemStack(Chemistry.ANTIMONY_PENTACHLORIDE));
        h.assertTrue(rem != null && rem.create().is(Chemistry.ANTIMONY_PENTACHLORIDE), "catalyst stays");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.PYROLYSIS_FURNACE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.SLOT_IN, new ItemStack(Chemistry.CHLORODIFLUOROMETHANE_GAS, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.SLOT_OUT).isEmpty(), "needs 2 R-22 per TFE");
        c.setItem(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.SLOT_IN, new ItemStack(Chemistry.CHLORODIFLUOROMETHANE_GAS, 8));
        for (int i = 0; i < 4; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.TICKS, feed);
        ItemStack tfe = c.getItem(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(tfe.is(Chemistry.TETRAFLUOROETHYLENE_GAS) && tfe.getCount() == 4, "8 R-22 -> 4 TFE, got " + tfe);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.SLOT_HCL).getCount(), 8, "2 HCl per TFE");
        BlockPos ap = p(3, 1, 1);
        h.setBlock(ap, Chemistry.AUTOCLAVE.block);
        var ac = h.getBlockEntity(ap, com.taku3.semiconductorlab.block.AutoclaveBlockEntity.class);
        long afeed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS);
        ac.getContainer().setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, tfe.copy());
        ac.setTemperatureForTest(100);
        tickConsumer(h, ap, ac, 20 * 30 + 5, afeed);
        ItemStack ptfe = ac.getContainer().getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT);
        h.assertTrue(ptfe.is(Chemistry.PTFE) && ptfe.getCount() == 1, "4 TFE -> PTFE in the autoclave, got " + ptfe);
        LOG.info("[LAB] PTFE [2-38]: Sb -> SbCl5; CHCl3 + 2HF (SbCl5) -> R-22; pyrolysis 2 R-22 -> TFE + 2 HCl; autoclave 4 TFE -> PTFE");
        h.succeed();
    }

    // =====================================================================
    // [2-18] 超純水（IMP3、S-002）
    // =====================================================================

    /** パルプ＋無水酢酸 → 酢酸セルロース → RO 膜。装置：蒸留水は受けない、脱イオン水＋PE の瓶 → 超純水（PE の瓶）＋ガラス瓶 */
    @GameTest(maxTicks = 200)
    public void ultrapureWater(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cellulose_acetate", com.taku3.semiconductorlab.content.Communication.WOOD_PULP) && recipeUses(h, "cellulose_acetate", Chemistry.ACETIC_ANHYDRIDE), "pulp + acetic anhydride -> cellulose acetate");
        h.assertTrue(recipeUses(h, "ro_membrane", Chemistry.CELLULOSE_ACETATE) && recipeUses(h, "ro_membrane", Chemistry.ACETONE), "Loeb-Sourirajan casting");
        h.assertTrue(recipeUses(h, "ultrapure_water_system", Chemistry.RO_MEMBRANE) && recipeUses(h, "ultrapure_water_system", com.taku3.semiconductorlab.content.Optics.MERCURY_LAMP_TUBE), "system has RO + UV");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.ULTRAPURE_WATER) == com.taku3.semiconductorlab.machine.Vessel.POLYETHYLENE_BOTTLE, "ultrapure water is kept in polyethylene, not glass");
        h.assertTrue(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.RESISTIVITY_MOHM_CM == 18.2, "theoretical limit 18.2 MOhm cm");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.ULTRAPURE_WATER_SYSTEM.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.class);
        var c = be.getContainer();
        h.assertFalse(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.LAYOUT.slots().get(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.SLOT_IN).accepts().test(new ItemStack(Chemistry.DISTILLED_WATER)), "distilled water must go through ion exchange first");
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.SLOT_IN, new ItemStack(Chemistry.DEIONIZED_WATER, 2));
        c.setItem(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.SLOT_BOTTLE, new ItemStack(Chemistry.POLYETHYLENE_BOTTLE, 2));
        for (int i = 0; i < 2; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Chemistry.ULTRAPURE_WATER) && out.getCount() == 2, "2 DI water -> 2 ultrapure water, got " + out);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.SLOT_GLASS).getCount(), 2, "glass bottles come back");
        LOG.info("[LAB] ultrapure water [2-18]: pulp + acetic anhydride -> cellulose acetate -> RO membrane; system: DI water + PE bottle -> ultrapure water (18.2 MOhm cm)");
        h.succeed();
    }

    // =====================================================================
    // [2-12] 硫酸塩と銅めっき（IMP3、O-013）
    // =====================================================================

    /** 孔雀石＋硫酸 → 硫酸銅、石灰＋硫酸 → 石こう。めっき槽：硫酸銅なし → 何も起きない。ファラデー：半分の電気で半分の銅。2F で電鋳1、陽極の銅1が減り、硫酸銅は減らない */
    @GameTest(maxTicks = 200)
    public void electroplating(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "copper_sulfate", com.taku3.semiconductorlab.content.Metallurgy.MALACHITE) && recipeUses(h, "copper_sulfate", Chemistry.SULFURIC_ACID), "malachite + H2SO4 -> CuSO4");
        h.assertTrue(recipeUses(h, "calcium_sulfate_from_lime", com.taku3.semiconductorlab.content.Survival.SLAKED_LIME), "lime + H2SO4 -> gypsum");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.copperGrams(2 * 96485) - 63.55) < 0.01, "Faraday: 2F -> 63.55 g Cu");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.ELECTROPLATING_BATH.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.WATTS);
        int full = (int) Math.ceil(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.COULOMBS_PER_COPY / (com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.WATTS / com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.VOLTS) * 20);
        c.setItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_ANODE, new ItemStack(Items.COPPER_INGOT, 2));
        c.setItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_MOULD, new ItemStack(Chemistry.GRAPHITE_WAX_MOULD, 2));
        tickConsumer(h, fp, be, 40, feed);
        h.assertTrue(be.getCoulombs() == 0, "no copper sulfate -> no ions -> no current");
        c.setItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_ELECTROLYTE, new ItemStack(Chemistry.COPPER_SULFATE, 1));
        tickConsumer(h, fp, be, full / 2, feed);
        double g = com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.copperGrams(be.getCoulombs());
        h.assertTrue(Math.abs(g - 31.8) < 1.0, "half the charge -> half the copper (~31.8 g), got " + g);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_OUT).isEmpty(), "not done yet");
        tickConsumer(h, fp, be, full / 2 + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_OUT).is(Chemistry.COPPER_ELECTROTYPE), "2F -> one electrotype");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_ANODE).getCount(), 1, "anode copper dissolved by the same amount");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.SLOT_ELECTROLYTE).getCount(), 1, "copper sulfate is not used up");
        LOG.info("[LAB] electroplating [2-12]: no CuSO4 -> no current; m = Q M / 2F (half charge -> half copper); 2F -> electrotype, anode -1, CuSO4 stays");
        h.succeed();
    }

    // =====================================================================
    // [2-06] ソルベー法・[2-08] 苛性ソーダ（IMP3、O-013）
    // =====================================================================

    /** アンモニアなしでは動かない。アンモニア水1本（4単位）で 16回まで減らずに回り、17回目の手前で1単位補給。塩2＋方解石1 → ソーダ1。ソーダ＋消石灰 → NaOH */
    @GameTest(maxTicks = 200)
    public void solvay(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sodium_hydroxide_from_soda", Chemistry.SODIUM_CARBONATE) && recipeUses(h, "sodium_hydroxide_from_soda", com.taku3.semiconductorlab.content.Survival.SLAKED_LIME), "[2-08] lime-soda process");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.SOLVAY_TOWER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.WATTS);
        int T = com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.TICKS;
        c.setItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_SALT, new ItemStack(com.taku3.semiconductorlab.content.Gas.SALT, 64));
        c.setItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_LIMESTONE, new ItemStack(Items.CALCITE, 32));
        tickConsumer(h, fp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_OUT).isEmpty(), "no ammonia -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_AMMONIA, new ItemStack(Chemistry.AMMONIA_WATER, 1));
        for (int i = 0; i < 16; i++) tickConsumer(h, fp, be, T, feed);
        ItemStack soda = c.getItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_OUT);
        h.assertTrue(soda.is(Chemistry.SODIUM_CARBONATE) && soda.getCount() == 16, "16 runs -> 16 soda, got " + soda);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_SALT).getCount(), 32, "2 salt per run");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_LIMESTONE).getCount(), 16, "1 limestone per run");
        h.assertValueEqual(be.getAmmonia(), 3, "ammonia circulates: one bottle (4) lost only 1 unit in 16 runs");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SLOT_BOTTLE).is(Chemistry.GLASS_BOTTLE), "empty bottle comes out");
        LOG.info("[LAB] solvay [2-06][2-08]: no NH3 -> nothing; 1 bottle of ammonia water runs 16 times losing 1 unit; 2 NaCl + CaCO3 -> Na2CO3; soda + lime -> NaOH");
        h.succeed();
    }

    // =====================================================================
    // [2-22] 王水（IMP3、O-013）
    // =====================================================================

    /** 塩酸3＋硝酸1 → 王水。金を溶かせるのは王水だけ（塩酸だけ・硝酸だけのレシピはない）。力を失うと金は溶かせない。SO2 で金の粉 → かまど → 金 */
    @GameTest
    public void aquaRegia(GameTestHelper h) {
        var HCL = com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID;
        var HNO3 = com.taku3.semiconductorlab.content.Gas.NITRIC_ACID;
        h.assertTrue(recipeUses(h, "aqua_regia", HCL) && recipeUses(h, "aqua_regia", HNO3), "3 HCl + 1 HNO3");
        h.assertTrue(recipeUses(h, "chloroauric_acid", Chemistry.AQUA_REGIA) && recipeUses(h, "chloroauric_acid", Items.GOLD_INGOT), "aqua regia dissolves gold");
        h.assertFalse(recipeUses(h, "chloroauric_acid", HCL) || recipeUses(h, "chloroauric_acid", HNO3) || recipeUses(h, "chloroauric_acid", Chemistry.SPENT_AQUA_REGIA), "neither acid alone, nor spent aqua regia");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack ar = new ItemStack(Chemistry.AQUA_REGIA, 3);
        player.getInventory().add(ar);
        ItemStack inInv = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (player.getInventory().getItem(i).is(Chemistry.AQUA_REGIA)) inInv = player.getInventory().getItem(i);
        h.assertFalse(inInv.isEmpty(), "aqua regia in inventory");
        com.taku3.semiconductorlab.item.AquaRegiaItem.decay(inInv, player);
        h.assertTrue(player.getInventory().countItem(Chemistry.AQUA_REGIA) == 0 && player.getInventory().countItem(Chemistry.SPENT_AQUA_REGIA) == 3, "aqua regia loses its power over time");
        h.assertTrue(recipeUses(h, "gold_powder", Chemistry.CHLOROAURIC_ACID) && recipeUses(h, "gold_powder", Chemistry.SULFUR_DIOXIDE), "SO2 precipitates gold");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.GOLD_POWDER).is(Items.GOLD_INGOT), "gold powder -> gold ingot");
        LOG.info("[LAB] aqua regia [2-22]: 3 HCl + HNO3; only aqua regia dissolves gold; decays when kept; SO2 -> gold powder -> gold ingot");
        h.succeed();
    }

    // =====================================================================
    // [2-15] ホウケイ酸ガラス（IMP3、O-014）
    // =====================================================================

    /** 砂＋B2O3＋ソーダ → 調合原料 → 溶鉱炉だけで溶ける。たき火で：ソーダ石灰ガラスは割れる、ホウケイ酸ガラスは割れない。カレットはかまどで戻る */
    @GameTest
    public void borosilicateGlass(GameTestHelper h) {
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        var FIRE = net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING;
        h.assertTrue(recipeUses(h, "borosilicate_batch", com.taku3.semiconductorlab.content.Metallurgy.BORON_TRIOXIDE), "batch has B2O3");
        h.assertTrue(cook(h, BLAST, Chemistry.BOROSILICATE_BATCH).is(Chemistry.BOROSILICATE_GLASS), "blast furnace melts it");
        h.assertTrue(cook(h, SMELT, Chemistry.BOROSILICATE_BATCH).isEmpty(), "a plain furnace is not hot enough");
        h.assertTrue(cook(h, FIRE, Chemistry.SODA_LIME_GLASS).is(Chemistry.CRACKED_GLASS), "soda-lime glass cracks on an open flame");
        h.assertTrue(cook(h, FIRE, Chemistry.BOROSILICATE_GLASS).isEmpty(), "borosilicate glass does not crack");
        h.assertTrue(cook(h, SMELT, Chemistry.CRACKED_GLASS).is(Chemistry.SODA_LIME_GLASS), "cullet remelts");
        LOG.info("[LAB] borosilicate [2-15]: blast furnace only; campfire cracks soda-lime glass but not borosilicate; cullet remelts");
        h.succeed();
    }

    // =====================================================================
    // [2-54] クロマトグラフィー（IMP3、O-014）
    // =====================================================================

    /** 空の柱にベンゼン → 何もない。色素の液 → しみこむ（瓶が戻る）、二度は入らない。ベンゼンで流す → カロテン＋クロロフィル、柱は空に戻る */
    @GameTest
    public void chromatography(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "leaf_pigment_extract", Items.OAK_LEAVES) && recipeUses(h, "leaf_pigment_extract", Chemistry.ETHANOL), "leaves + ethanol -> extract");
        h.assertTrue(recipeUses(h, "chromatography_column", Items.CALCITE), "column packed with calcium carbonate (Tsvet)");
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, Chemistry.CHROMATOGRAPHY_COLUMN);
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack benz = new ItemStack(Chemistry.BENZENE, 2), ext = new ItemStack(Chemistry.LEAF_PIGMENT_EXTRACT, 2);
        h.assertValueEqual(com.taku3.semiconductorlab.block.ChromatographyColumnBlock.use(level, pos, player, benz), "empty", "nothing to separate yet");
        h.assertValueEqual(com.taku3.semiconductorlab.block.ChromatographyColumnBlock.use(level, pos, player, ext), "loaded", "extract soaks in");
        h.assertValueEqual(com.taku3.semiconductorlab.block.ChromatographyColumnBlock.use(level, pos, player, ext), "already_loaded", "only one load at a time");
        h.assertTrue(player.getInventory().countItem(Chemistry.GLASS_BOTTLE) == 1 && ext.getCount() == 1, "bottle returned");
        h.assertValueEqual(com.taku3.semiconductorlab.block.ChromatographyColumnBlock.use(level, pos, player, benz), "separated", "benzene runs the bands apart");
        h.assertTrue(player.getInventory().countItem(Chemistry.CAROTENE) == 1 && player.getInventory().countItem(Chemistry.CHLOROPHYLL) == 1, "carotene + chlorophyll");
        h.assertFalse(level.getBlockState(pos).getValue(com.taku3.semiconductorlab.block.ChromatographyColumnBlock.LOADED), "column empty again");
        LOG.info("[LAB] chromatography [2-54]: leaves + ethanol -> extract; CaCO3 column + benzene -> carotene (first) + chlorophyll (band)");
        h.succeed();
    }

    // =====================================================================
    // [2-05] ルブラン法（IMP3、O-014）
    // =====================================================================

    /** 塩2＋硫酸 → 仕込み →かまど → 芒硝（HCl は空へ）、または吸収塔で塩酸＋芒硝が残る。芒硝＋方解石＋木炭 →かまど → 黒灰 → ＋水 → ソーダ */
    @GameTest
    public void leblanc(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "salt_cake_charge", com.taku3.semiconductorlab.content.Gas.SALT) && recipeUses(h, "salt_cake_charge", Chemistry.SULFURIC_ACID), "2 NaCl + H2SO4");
        h.assertTrue(cook(h, SMELT, Chemistry.SALT_CAKE_CHARGE).is(Chemistry.SALT_CAKE), "roasted -> salt cake (HCl vented)");
        h.assertTrue(recipeUses(h, "hydrochloric_acid_from_salt_cake_charge", Chemistry.SALT_CAKE_CHARGE), "absorption tower captures HCl");
        var rem = Chemistry.SALT_CAKE_CHARGE.getCraftingRemainder(new ItemStack(Chemistry.SALT_CAKE_CHARGE));
        h.assertTrue(rem != null && rem.create().is(Chemistry.SALT_CAKE), "salt cake stays in the grid");
        h.assertTrue(recipeUses(h, "black_ash_charge", Items.CALCITE) && recipeUses(h, "black_ash_charge", Items.CHARCOAL), "salt cake + limestone + charcoal");
        h.assertTrue(cook(h, SMELT, Chemistry.BLACK_ASH_CHARGE).is(Chemistry.BLACK_ASH), "-> black ash");
        h.assertTrue(recipeUses(h, "sodium_carbonate_from_black_ash", Chemistry.BLACK_ASH), "leach -> soda");
        LOG.info("[LAB] leblanc [2-05]: NaCl + H2SO4 -> salt cake (+HCl vented or absorbed); + CaCO3 + C -> black ash -> leach -> Na2CO3");
        h.succeed();
    }

    // =====================================================================
    // [2-47] 化学強化ガラス（IMP3、O-015）
    // =====================================================================

    /** 砂＋アルミナ＋ソーダ → 溶鉱炉 → アルミノケイ酸ガラス、＋硝石 → 浴 → かまど → 強化ガラス。たたき石でふつうのガラスは割れ、強化ガラスは割れない */
    @GameTest
    public void strengthenedGlass(GameTestHelper h) {
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "aluminosilicate_batch", com.taku3.semiconductorlab.content.Metallurgy.ALUMINA), "batch has alumina");
        h.assertTrue(cook(h, BLAST, Chemistry.ALUMINOSILICATE_BATCH).is(Chemistry.ALUMINOSILICATE_GLASS), "blast furnace melts it");
        h.assertTrue(recipeUses(h, "glass_in_salt_bath", com.taku3.semiconductorlab.content.Gas.SALTPETER), "potassium nitrate bath");
        h.assertTrue(cook(h, SMELT, Chemistry.GLASS_IN_SALT_BATH).is(Chemistry.STRENGTHENED_GLASS), "ion exchange in molten KNO3");
        for (var g : new net.minecraft.world.item.Item[] {Chemistry.SODA_LIME_GLASS, Chemistry.BOROSILICATE_GLASS, Chemistry.ALUMINOSILICATE_GLASS})
            h.assertTrue(craft2(h, new ItemStack(g), new ItemStack(com.taku3.semiconductorlab.content.Survival.HAMMERSTONE)).is(Chemistry.CRACKED_GLASS), "hammerstone cracks " + g);
        h.assertTrue(craft2(h, new ItemStack(Chemistry.STRENGTHENED_GLASS), new ItemStack(com.taku3.semiconductorlab.content.Survival.HAMMERSTONE)).isEmpty(), "strengthened glass does not crack");
        LOG.info("[LAB] strengthened glass [2-47]: aluminosilicate glass + KNO3 bath -> strengthened; hammerstone cracks ordinary glasses but not strengthened");
        h.succeed();
    }

    /** 作業台に2つ並べたときの出力 */
    private static ItemStack craft2(GameTestHelper h, ItemStack a, ItemStack b) {
        var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, java.util.List.of(a, b));
        return h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel())
                .map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    // =====================================================================
    // [2-46] 透明な電極 ITO（IMP3、O-015）
    // =====================================================================

    /** In8＋Sn → 合金9。蒸着装置の表：ホウケイ酸ガラス＋合金 → 金属を付けたガラス。かまど（空気で酸化）→ ITO ガラス */
    @GameTest
    public void itoGlass(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "indium_tin_alloy", com.taku3.semiconductorlab.content.Metallurgy.INDIUM) && recipeUses(h, "indium_tin_alloy", com.taku3.semiconductorlab.content.Metallurgy.TIN_INGOT), "In + Sn alloy");
        var r = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.recipe(new ItemStack(Chemistry.BOROSILICATE_GLASS), new ItemStack(Chemistry.INDIUM_TIN_ALLOY));
        h.assertTrue(r != null && r.product() == Chemistry.INDIUM_TIN_COATED_GLASS, "vacuum evaporation of In-Sn onto glass");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.INDIUM_TIN_COATED_GLASS).is(Chemistry.ITO_GLASS), "oxidized in air -> ITO");
        LOG.info("[LAB] ITO [2-46]: In8 + Sn -> alloy; evaporator: borosilicate glass + alloy -> coated; furnace (air) -> ITO glass");
        h.succeed();
    }

    // =====================================================================
    // [2-44] 赤リン（IMP3、O-016）
    // =====================================================================

    /** 白リン＋窒素 → かまど → 赤リン。空気の中で熱すると燃えて五酸化二リン。白リンは水の瓶が要るが、赤リンは容器なし */
    @GameTest
    public void redPhosphorus(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "phosphorus_under_nitrogen", Chemistry.PHOSPHORUS) && recipeUses(h, "phosphorus_under_nitrogen", com.taku3.semiconductorlab.content.Gas.NITROGEN_GAS), "shut out the air with nitrogen");
        h.assertTrue(cook(h, SMELT, Chemistry.PHOSPHORUS_UNDER_NITROGEN).is(Chemistry.RED_PHOSPHORUS), "no air -> red phosphorus");
        h.assertTrue(cook(h, SMELT, Chemistry.PHOSPHORUS).is(Chemistry.PHOSPHORUS_PENTOXIDE), "in air it burns -> P4O10");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.PHOSPHORUS) == com.taku3.semiconductorlab.machine.Vessel.WATER_JAR && com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.RED_PHOSPHORUS) == null, "white needs water, red does not");
        LOG.info("[LAB] red phosphorus [2-44]: P4 + N2 -> furnace -> red P; in air -> P4O10; red needs no water jar");
        h.succeed();
    }

    // =====================================================================
    // [2-11] 硫化水素と硫黄回収（IMP3、O-016）
    // =====================================================================

    /** 黒灰を洗うとかすが残る。かす＋塩酸 → H2S。熱したアルミナの管で H2S → 硫黄（冷たいと反応しない）。もれると吐き気 */
    @GameTest
    public void sulfurRecovery(GameTestHelper h) {
        var rem = Chemistry.BLACK_ASH.getCraftingRemainder(new ItemStack(Chemistry.BLACK_ASH));
        h.assertTrue(rem != null && rem.create().is(Chemistry.ALKALI_WASTE), "washing black ash leaves the CaS waste");
        h.assertTrue(recipeUses(h, "hydrogen_sulfide_gas", Chemistry.ALKALI_WASTE) && recipeUses(h, "hydrogen_sulfide_gas", com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID), "CaS + 2HCl -> H2S");
        floor(h, 3);
        BlockPos fire = p(1, 1, 1);
        h.setBlock(fire.above(), Chemistry.ALUMINA_CATALYST_TUBE);
        BlockPos tube = h.absolutePos(fire.above());
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack gas = new ItemStack(Chemistry.HYDROGEN_SULFIDE_GAS, 2);
        h.assertFalse(com.taku3.semiconductorlab.block.AluminaCatalystTubeBlock.react(level, tube, player, gas), "cold tube does not react");
        h.setBlock(fire, net.minecraft.world.level.block.Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        h.assertTrue(com.taku3.semiconductorlab.block.AluminaCatalystTubeBlock.react(level, tube, player, gas), "hot alumina: Claus");
        h.assertTrue(gas.getCount() == 1 && player.getInventory().countItem(Chemistry.SULFUR_POWDER) == 1, "H2S -> sulfur");
        com.taku3.semiconductorlab.item.HydrogenSulfideItem.leak(player);
        h.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA), "a leak makes you feel sick (accident)");
        LOG.info("[LAB] sulfur recovery [2-11]: black ash -> alkali waste (CaS); + HCl -> H2S; hot alumina (Claus) -> sulfur; leak -> nausea");
        h.succeed();
    }

    // =====================================================================
    // [2-56] 自動製びん機（IMP3、O-016）
    // =====================================================================

    /** ガラス10を入れて電気を流し続けると、10秒で瓶10（1分に60本の速さ）。ガラスが尽きると止まる */
    @GameTest(maxTicks = 100)
    public void bottleMachine(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Chemistry.BOTTLE_MACHINE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.BottleMachineBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.BottleMachineBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.BottleMachineBlockEntity.SLOT_IN, new ItemStack(Chemistry.SODA_LIME_GLASS, 10));
        tickConsumer(h, fp, be, 20 * 12, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.BottleMachineBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Chemistry.GLASS_BOTTLE) && out.getCount() == 10, "10 glass -> 10 bottles in about 10 s, got " + out);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BottleMachineBlockEntity.SLOT_IN).isEmpty(), "stops when the glass runs out");
        LOG.info("[LAB] bottle machine [2-56]: 1 bottle per second (60 a minute) from soda-lime glass");
        h.succeed();
    }

    // =====================================================================
    // [2-51] ナイロン 66（IMP3、S-004）
    // =====================================================================

    /** ラネー Ni（戻る）で フェノール → シクロヘキサノール、アジポニトリル → HMDA。加圧加熱釜：水なしでは塩のまま、水あり約270℃でナイロン 66 → かまど → 糸4 */
    @GameTest(maxTicks = 2000)
    public void nylon(GameTestHelper h) {
        var rem = Chemistry.RANEY_NICKEL.getCraftingRemainder(new ItemStack(Chemistry.RANEY_NICKEL));
        h.assertTrue(rem != null && rem.create().is(Chemistry.RANEY_NICKEL), "Raney nickel stays (catalyst)");
        h.assertTrue(recipeUses(h, "raney_alloy_charge", com.taku3.semiconductorlab.content.Metallurgy.NICKEL), "Raney alloy from nickel");
        h.assertTrue(recipeUses(h, "cyclohexanol", Chemistry.PHENOL) && recipeUses(h, "cyclohexanol", Chemistry.RANEY_NICKEL), "phenol hydrogenated");
        h.assertTrue(recipeUses(h, "adipic_acid", com.taku3.semiconductorlab.content.Gas.NITRIC_ACID), "nitric acid oxidation");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.AMMONIUM_ADIPATE).is(Chemistry.ADIPONITRILE), "dehydration -> adiponitrile");
        h.assertTrue(recipeUses(h, "hexamethylenediamine", Chemistry.ADIPONITRILE) && recipeUses(h, "nylon_salt", Chemistry.HEXAMETHYLENEDIAMINE), "HMDA + adipic acid -> nylon salt");
        floor(h, 3);
        BlockPos ap = p(1, 1, 1);
        h.setBlock(ap, Chemistry.AUTOCLAVE.block);
        var ac = h.getBlockEntity(ap, com.taku3.semiconductorlab.block.AutoclaveBlockEntity.class);
        var c = ac.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, new ItemStack(Chemistry.NYLON_SALT, 1));
        ac.setTemperatureForTest(290);
        tickConsumer(h, ap, ac, 20 * 30 + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).is(Chemistry.NYLON_SALT), "no water -> no pressure -> salt comes back");
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).copy());
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        ac.setTemperatureForTest(290);
        tickConsumer(h, ap, ac, 20 * 30 + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).is(Chemistry.NYLON_66), "under pressure at ~270 C -> nylon 66, got " + c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT));
        ItemStack fib = cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.NYLON_66);
        h.assertTrue(fib.is(Chemistry.NYLON_FIBER) && fib.getCount() == 4, "melt-spun -> 4 fibre");
        LOG.info("[LAB] nylon [2-51]: Raney Ni; phenol -> cyclohexanol -> adipic acid; -> adiponitrile -> HMDA; nylon salt -> autoclave 270 C with water -> nylon 66 -> fibre");
        h.succeed();
    }

    // =====================================================================
    // [2-45] 光で固まる樹脂（IMP3、S-004）
    // =====================================================================

    /** レッペ法でアクリル酸 → トリアクリレート。AlCl3・CCl4 → ベンゾフェノン。樹脂は光で固まる（cure） */
    @GameTest
    public void photopolymer(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "acrylic_acid", Chemistry.ACETYLENE_GAS) && recipeUses(h, "acrylic_acid", com.taku3.semiconductorlab.content.Metallurgy.CARBON_MONOXIDE_GAS) && recipeUses(h, "acrylic_acid", com.taku3.semiconductorlab.content.Metallurgy.NICKEL_CARBONYL_GAS), "Reppe: acetylene + CO + water (Ni carbonyl)");
        h.assertTrue(recipeUses(h, "glycerol_triacrylate", Chemistry.GLYCERIN) && recipeUses(h, "glycerol_triacrylate", Chemistry.ACRYLIC_ACID), "three-armed monomer");
        h.assertTrue(recipeUses(h, "benzophenone", Chemistry.BENZENE) && recipeUses(h, "benzophenone", Chemistry.ALUMINIUM_CHLORIDE) && recipeUses(h, "benzophenone", Chemistry.CARBON_TETRACHLORIDE), "Friedel-Crafts -> benzophenone");
        h.assertTrue(recipeUses(h, "photopolymer_resin", Chemistry.BENZOPHENONE) && recipeUses(h, "photopolymer_resin", Chemistry.HEXAMETHYLENEDIAMINE), "photoinitiator + amine");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(Chemistry.PHOTOPOLYMER_RESIN, 2));
        ItemStack inInv = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (player.getInventory().getItem(i).is(Chemistry.PHOTOPOLYMER_RESIN)) inInv = player.getInventory().getItem(i);
        com.taku3.semiconductorlab.item.PhotopolymerResinItem.cure(inInv, player);
        h.assertTrue(player.getInventory().countItem(Chemistry.PHOTOPOLYMER_RESIN) == 0 && player.getInventory().countItem(Chemistry.CURED_PHOTOPOLYMER) == 2, "light cures the resin");
        LOG.info("[LAB] photopolymer [2-45]: Reppe acrylic acid -> glycerol triacrylate; benzophenone (Friedel-Crafts) + amine; light cures it");
        h.succeed();
    }

    // =====================================================================
    // [2-43] ばん油（IMP3、S-004）
    // =====================================================================

    /** 黄鉄鉱は水の中で緑礬に（陸では変わらない）。緑礬＋蒸留器＋瓶 → ばん油（硫酸）、ベンガラが残る */
    @GameTest(maxTicks = 200)
    public void oilOfVitriol(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "oil_of_vitriol", Chemistry.GREEN_VITRIOL) && recipeUses(h, "oil_of_vitriol", com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL), "green vitriol in a glass retort");
        var rem = Chemistry.GREEN_VITRIOL.getCraftingRemainder(new ItemStack(Chemistry.GREEN_VITRIOL));
        h.assertTrue(rem != null && rem.create().is(Chemistry.COLCOTHAR), "colcothar is left");
        floor(h, 4);
        BlockPos water = p(1, 1, 1);
        for (int y = 1; y <= 3; y++)
            for (BlockPos wall : new BlockPos[] {p(0, y, 1), p(2, y, 1), p(1, y, 0), p(1, y, 2)}) h.setBlock(wall, net.minecraft.world.level.block.Blocks.STONE);
        h.setBlock(water, net.minecraft.world.level.block.Blocks.WATER);
        var wet = h.spawnItem(Chemistry.PYRITE, new net.minecraft.world.phys.Vec3(1.5, 1.3, 1.5));
        var onLand = h.spawnItem(Chemistry.PYRITE, new net.minecraft.world.phys.Vec3(3.5, 1.1, 3.5));
        wet.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        onLand.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        com.taku3.semiconductorlab.item.Retting.ticksForWeathering = 20;
        h.runAfterDelay(80, () -> {
            com.taku3.semiconductorlab.item.Retting.ticksForWeathering = com.taku3.semiconductorlab.item.Retting.WEATHERING_TICKS;
            h.assertTrue(wet.getItem().is(Chemistry.GREEN_VITRIOL), "pyrite in water -> green vitriol (got " + wet.getItem() + ")");
            h.assertTrue(onLand.getItem().is(Chemistry.PYRITE), "pyrite on land stays");
            LOG.info("[LAB] oil of vitriol [2-43]: pyrite weathers in water -> green vitriol; + retort -> sulfuric acid, colcothar left");
            h.succeed();
        });
    }

    // =====================================================================
    // [2-50] 配位重合触媒（IMP3、O-018）
    // =====================================================================

    /** Al＋H2＋エチレン3 → AlEt3、＋TiCl4 → 触媒（戻る）。エチレン4＋触媒 → HDPE。触媒なしでは作業台でポリエチレンにならない（高圧の反応釜が要る） */
    @GameTest
    public void zieglerNatta(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "triethylaluminium", com.taku3.semiconductorlab.content.Metallurgy.ALUMINIUM_INGOT) && recipeUses(h, "triethylaluminium", Chemistry.ETHYLENE_GAS), "Ziegler direct process");
        h.assertTrue(recipeUses(h, "ziegler_natta_catalyst", com.taku3.semiconductorlab.content.Metallurgy.TITANIUM_TETRACHLORIDE) && recipeUses(h, "ziegler_natta_catalyst", Chemistry.TRIETHYLALUMINIUM), "TiCl4 + AlEt3");
        var rem = Chemistry.ZIEGLER_NATTA_CATALYST.getCraftingRemainder(new ItemStack(Chemistry.ZIEGLER_NATTA_CATALYST));
        h.assertTrue(rem != null && rem.create().is(Chemistry.ZIEGLER_NATTA_CATALYST), "catalyst stays");
        h.assertTrue(recipeUses(h, "hdpe", Chemistry.ETHYLENE_GAS) && recipeUses(h, "hdpe", Chemistry.ZIEGLER_NATTA_CATALYST), "ethylene on the catalyst -> HDPE");
        var g = net.minecraft.core.NonNullList.withSize(4, ItemStack.EMPTY);
        for (int i = 0; i < 4; i++) g.set(i, new ItemStack(Chemistry.ETHYLENE_GAS));
        var in = net.minecraft.world.item.crafting.CraftingInput.of(2, 2, g);
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, h.getLevel()).isEmpty(), "without the catalyst nothing happens at ordinary pressure");
        LOG.info("[LAB] Ziegler-Natta [2-50]: Al + H2 + C2H4 -> AlEt3; + TiCl4 -> catalyst (stays); 4 ethylene -> HDPE; no catalyst -> nothing");
        h.succeed();
    }

    // =====================================================================
    // [2-55] 導電性高分子（IMP3、O-018）
    // =====================================================================

    /** アセチレン4＋ZN 触媒 → ポリアセチレン、＋ヨウ素 → ドープ。テスター：ドープ前はつかない、後はつく。銅・黒鉛はつく、ポリエチレン・ガラスはつかない */
    @GameTest
    public void conductivePolymer(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "polyacetylene_film", Chemistry.ACETYLENE_GAS) && recipeUses(h, "polyacetylene_film", Chemistry.ZIEGLER_NATTA_CATALYST), "Shirakawa: acetylene on Ziegler-Natta catalyst");
        h.assertTrue(recipeUses(h, "doped_polyacetylene", Chemistry.IODINE), "iodine doping");
        var T = (java.util.function.Predicate<net.minecraft.world.item.Item>) it -> com.taku3.semiconductorlab.item.ContinuityTesterItem.lampOn(new ItemStack(it));
        h.assertFalse(T.test(Chemistry.POLYACETYLENE_FILM), "undoped polyacetylene: bulb stays dark");
        h.assertTrue(T.test(Chemistry.DOPED_POLYACETYLENE), "doped: bulb lights");
        h.assertTrue(T.test(Items.COPPER_INGOT) && T.test(com.taku3.semiconductorlab.content.Metallurgy.GRAPHITE), "metals and graphite conduct");
        h.assertFalse(T.test(Chemistry.POLYETHYLENE) || T.test(Chemistry.SODA_LIME_GLASS) || T.test(Chemistry.ULTRAPURE_WATER), "plastic, glass, ultrapure water do not");
        h.assertTrue(com.taku3.semiconductorlab.item.ContinuityTesterItem.conductivity(new ItemStack(Chemistry.DOPED_POLYACETYLENE)) / com.taku3.semiconductorlab.item.ContinuityTesterItem.conductivity(new ItemStack(Chemistry.POLYACETYLENE_FILM)) > 1e6, "doping raises conductivity by over a million");
        LOG.info("[LAB] conductive polymer [2-55]: acetylene + ZN catalyst -> polyacetylene (dark); + iodine -> doped (lamp lights)");
        h.succeed();
    }

    // =====================================================================
    // [2-48] 冷媒フロン とオゾン層（IMP3、O-019）
    // =====================================================================

    /** CCl4＋HF2＋SbCl5 → CFC-12。冷凍機の冷媒になり（減らない）、氷1つごとにオゾンが減る。もれなければ時間で戻る */
    @GameTest
    public void cfcOzone(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cfc12_gas", Chemistry.CARBON_TETRACHLORIDE) && recipeUses(h, "cfc12_gas", Chemistry.HYDROFLUORIC_ACID) && recipeUses(h, "cfc12_gas", Chemistry.ANTIMONY_PENTACHLORIDE), "Swarts: CCl4 + 2HF");
        var level = h.getLevel();
        var ozone = com.taku3.semiconductorlab.OzoneLayer.get(level);
        ozone.resetForTest(level);
        double before = ozone.dobson(level);
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, com.taku3.semiconductorlab.content.Heat.PERKINS_ICE_MACHINE);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.assertTrue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(Chemistry.CFC12_GAS)), "CFC as refrigerant");
        for (int n = 0; n < 2; n++) {
            com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(Items.WATER_BUCKET));
            for (int i = 0; i < com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.CRANKS; i++) com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.crank(level, pos, player);
        }
        h.assertTrue(player.getInventory().countItem(Items.ICE) == 2, "2 ice with CFC");
        h.assertValueEqual(level.getBlockState(pos).getValue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.REFRIGERANT), com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.CFC, "CFC does not leak away from the machine");
        double after = ozone.dobson(level);
        h.assertTrue(after < before - 5, "but the ozone layer shrank: " + before + " -> " + after);
        var fresh = new com.taku3.semiconductorlab.OzoneLayer(250, 0);
        h.assertTrue(Math.abs(fresh.dobson(level) - Math.min(300, 250 + level.getGameTime() / 24000.0)) < 0.01, "it recovers 1 DU per day without leaks");
        ozone.resetForTest(level);
        LOG.info("[LAB] CFC [2-48]: CCl4 + 2HF (SbCl5) -> CFC-12; as refrigerant it stays but each ice leaks -> ozone " + before + " -> " + after + " DU");
        h.succeed();
    }

    /** Q3-11 ②（IMP3、S-006）：オゾンホール（220 DU 未満）で日なたなら日焼け、日かげ・ホールなしは無い。HFC は冷媒になり、オゾンは減らない */
    @GameTest
    public void ozoneHoleHfc(GameTestHelper h) {
        h.assertTrue(com.taku3.semiconductorlab.OzoneLayer.sunburnRisk(200, true), "hole + sun -> risk");
        h.assertFalse(com.taku3.semiconductorlab.OzoneLayer.sunburnRisk(200, false) || com.taku3.semiconductorlab.OzoneLayer.sunburnRisk(300, true), "no risk in shade or without a hole");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        com.taku3.semiconductorlab.OzoneLayer.sunburn(player);
        h.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "sunburn = brief weakness");
        h.assertTrue(recipeUses(h, "hfc23_gas", Chemistry.CHLOROFORM) && recipeUses(h, "hfc23_gas", Chemistry.HYDROFLUORIC_ACID), "chloroform + 3HF -> HFC-23");
        var level = h.getLevel();
        var ozone = com.taku3.semiconductorlab.OzoneLayer.get(level);
        ozone.resetForTest(level);
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, com.taku3.semiconductorlab.content.Heat.PERKINS_ICE_MACHINE);
        h.assertTrue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(Chemistry.HFC23_GAS)), "HFC as refrigerant");
        com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(Items.WATER_BUCKET));
        for (int i = 0; i < com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.CRANKS; i++) com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.crank(level, pos, player);
        h.assertTrue(player.getInventory().countItem(Items.ICE) == 1, "ice with HFC");
        h.assertTrue(ozone.dobson(level) >= 299.9, "HFC does not lower the ozone");
        h.succeed();
    }

    /** EUV 用の化学増幅レジスト（IMP3、S-010）：トリブチルアミン（クエンチャー）＝ブタノール3＋NH3（Ni）。EUV レジスト＝保護 PHS＋光酸発生剤2＋クエンチャー＋キシレン＋瓶（ガラス瓶入り） */
    @GameTest
    public void euvResist(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "tributylamine", Chemistry.BUTANOL) && recipeUses(h, "tributylamine", com.taku3.semiconductorlab.content.Gas.AMMONIA_GAS) && recipeUses(h, "tributylamine", Chemistry.RANEY_NICKEL), "quencher from butanol + NH3");
        h.assertTrue(recipeUses(h, "euv_resist", Chemistry.PROTECTED_PHS) && recipeUses(h, "euv_resist", Chemistry.PHOTOACID_GENERATOR) && recipeUses(h, "euv_resist", Chemistry.TRIBUTYLAMINE), "EUV CAR = resin + PAG + quencher");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.EUV_RESIST) == com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE, "in a glass bottle");
        double euvEv = 1239.84 / 13.5, arfEv = 1239.84 / 193;
        h.assertTrue(euvEv / arfEv > 13 && euvEv / arfEv < 15, "an EUV photon has ~14x the energy of ArF -> ~1/14 the photons per dose");
        h.succeed();
    }

    /** 銅フタロシアニン（IMP3、O-025）：ナフタレン＋V2O5 → 無水フタル酸、NH3＋CO2 → 尿素、Cu＋HCl → CuCl、MoO3＋NH3 水 → モリブデン酸アンモニウム（触媒）→ 青い顔料 */
    @GameTest
    public void copperPhthalocyanine(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "phthalic_anhydride", Chemistry.NAPHTHALENE) && recipeUses(h, "phthalic_anhydride", com.taku3.semiconductorlab.content.Metallurgy.VANADIUM_PENTOXIDE), "V2O5 air oxidation (no mercury)");
        h.assertTrue(recipeUses(h, "urea", com.taku3.semiconductorlab.content.Gas.AMMONIA_GAS) && recipeUses(h, "urea", com.taku3.semiconductorlab.content.Gas.CARBON_DIOXIDE_GAS), "urea from NH3 + CO2");
        h.assertTrue(recipeUses(h, "ammonium_molybdate", com.taku3.semiconductorlab.content.Metallurgy.MOLYBDENUM_TRIOXIDE), "molybdate catalyst from C's MoO3");
        h.assertTrue(recipeUses(h, "copper_phthalocyanine", Chemistry.PHTHALIC_ANHYDRIDE) && recipeUses(h, "copper_phthalocyanine", Chemistry.UREA)
                && recipeUses(h, "copper_phthalocyanine", Chemistry.CUPROUS_CHLORIDE) && recipeUses(h, "copper_phthalocyanine", Chemistry.AMMONIUM_MOLYBDATE), "Wyler process");
        var rem = Chemistry.AMMONIUM_MOLYBDATE.getCraftingRemainder(new ItemStack(Chemistry.AMMONIUM_MOLYBDATE));
        h.assertTrue(rem != null && rem.create().is(Chemistry.AMMONIUM_MOLYBDATE), "molybdate catalyst returned");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        // IMP3、O-028 で書き出しに足した：オートクレーブの亜硫酸パルプ、真空蒸着の ITO（D が足した行）
        rec(a, Chemistry.AUTOCLAVE.block, "亜硫酸パルプ", L(io(com.taku3.semiconductorlab.content.Communication.PULP_CHARGE, 1)), L(io(com.taku3.semiconductorlab.content.Communication.WOOD_PULP, 2)),
            com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS, 30.0, "木のチップを亜硫酸塩の液で約130℃・3気圧で煮てリグニンをとかす（[15-01]）");
        rec(a, com.taku3.semiconductorlab.content.Vacuum.EVAPORATOR.block, "インジウム錫を付ける", L(io(Chemistry.BOROSILICATE_GLASS, 1), io(Chemistry.INDIUM_TIN_ALLOY, 1)), L(io(Chemistry.INDIUM_TIN_COATED_GLASS, 1)),
            com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS / 20.0, "真空で合金を蒸発させてガラスに付ける → かまどで酸化して ITO（[2-46]）");
        for (var r : com.taku3.semiconductorlab.block.ResistReactorBlockEntity.RECIPES) {
            var ins = new com.google.gson.JsonArray();
            for (var i : r.in()) ins.add(io(i.shown(), i.count()));
            if (r.catalyst() != null) ins.add(r.key().equals("pag") ? io(r.catalyst(), 1) : kept(io(r.catalyst(), 1)));
            var outs = new com.google.gson.JsonArray();
            outs.add(io(r.out(), r.outCount()));
            if (r.out2() != null) outs.add(io(r.out2(), r.out2Count()));
            rec(a, Chemistry.RESIST_REACTOR.block, "化学増幅レジスト：" + r.key(), ins, outs, com.taku3.semiconductorlab.block.ResistReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.ResistReactorBlockEntity.TICKS / 20.0, "[2-33] 1982 IBM の化学増幅レジストの材料");
        }
        rec(a, Chemistry.PEROXIDE_CELL.block, "過酸化水素", L(io(Chemistry.GLASS_BOTTLE, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_WATER), perRuns(io(Chemistry.SULFURIC_ACID, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ACID), perRuns(io(com.taku3.semiconductorlab.content.Metallurgy.PLATINUM_INGOT, 1), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.RUNS_PER_ANODE)),
            L(io(Chemistry.HYDROGEN_PEROXIDE, 1), mol(com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS, 1)), com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.WATTS, com.taku3.semiconductorlab.block.PeroxideCellBlockEntity.TICKS / 20.0, "白金の陽極で硫酸 → 過硫酸 → 水で分解して H2O2（硫酸は戻る）。全体 2H2O → H2O2 + H2");
        rec(a, Chemistry.AMMONIA_STILL.block, "アンモニア水", L(io(Chemistry.AMMONIACAL_LIQUOR, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.LIQUOR_PER_RUN), io(com.taku3.semiconductorlab.content.Survival.SLAKED_LIME, 1), io(Chemistry.GLASS_BOTTLE, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.RUNS_PER_WATER)),
            L(io(Chemistry.AMMONIA_WATER, 1)), com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.WATTS, com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity.TICKS / 20.0, "安水を熱し、消石灰で塩のアンモニアも追い出して水に吸わせる");
        rec(a, Chemistry.HIGH_PRESSURE_REACTOR.block, "ポリエチレン", L(mol(Chemistry.ETHYLENE_GAS, com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.ETHYLENE_PER_RUN), perRuns(mol(com.taku3.semiconductorlab.content.Gas.OXYGEN_GAS, 1), com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.RUNS_PER_OXYGEN)),
            L(io(Chemistry.POLYETHYLENE, 1)), com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.HighPressureReactorBlockEntity.TICKS / 20.0, "約1400気圧・約200℃、ほんの少しの酸素が開始剤：n C2H4 → (C2H4)n（1933 ICI）");
        rec(a, Chemistry.AUTOCLAVE.block, "ベークライト", L(io(Chemistry.RESOL_MIX, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.AutoclaveBlockEntity.RUNS_PER_WATER)),
            L(io(Chemistry.BAKELITE, 2)), com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS, 30.0, "150℃以上・5気圧以上（水の蒸気の圧力）で固める。圧力が足りないと泡だらけのベークライト（ベークランドの「熱と圧力」）");
        rec(a, Chemistry.PYROLYSIS_FURNACE.block, "TFE", L(mol(Chemistry.CHLORODIFLUOROMETHANE_GAS, com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.FEED_PER_RUN)),
            L(mol(Chemistry.TETRAFLUOROETHYLENE_GAS, 1), mol(com.taku3.semiconductorlab.content.Metallurgy.HYDROGEN_CHLORIDE_GAS, 2)), com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.PyrolysisFurnaceBlockEntity.TICKS / 20.0, "白金の管で約700℃：2CHClF2 → C2F4 + 2HCl");
        rec(a, Chemistry.AUTOCLAVE.block, "PTFE", L(mol(Chemistry.TETRAFLUOROETHYLENE_GAS, 4)), L(io(Chemistry.PTFE, 1)), com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS, 30.0, "TFE を水の中で温めて重合：n C2F4 → (C2F4)n（1938 プランケット）");
        rec(a, Chemistry.ULTRAPURE_WATER_SYSTEM.block, "超純水", L(io(Chemistry.DEIONIZED_WATER, 1), io(Chemistry.POLYETHYLENE_BOTTLE, 1)),
            L(io(Chemistry.ULTRAPURE_WATER, 1), io(Chemistry.GLASS_BOTTLE, 1)), com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.WATTS, com.taku3.semiconductorlab.block.UltrapureWaterSystemBlockEntity.TICKS / 20.0, "イオン交換した水を逆浸透膜でこし、紫外線で殺菌。18.2 MΩ·cm（25℃）");
        rec(a, Chemistry.ELECTROPLATING_BATH.block, "銅の電鋳", L(io(Items.COPPER_INGOT, 1), io(Chemistry.GRAPHITE_WAX_MOULD, 1)), L(io(Chemistry.COPPER_ELECTROTYPE, 1)),
            com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.WATTS, com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.COULOMBS_PER_COPY / (com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.WATTS / com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.VOLTS),
            "硫酸銅の液（減らない）で、陽極の銅を型へ運ぶ。2F（193 kC）で銅 63.5 g（ファラデーの法則）");
        rec(a, Chemistry.SOLVAY_TOWER.block, "炭酸ナトリウム", L(io(com.taku3.semiconductorlab.content.Gas.SALT, com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.SALT_PER_RUN), io(Items.CALCITE, 1),
                perRuns(io(Chemistry.AMMONIA_WATER, 1), com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.LOSS_EVERY * com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.NH3_PER_BOTTLE)),
            L(io(Chemistry.SODIUM_CARBONATE, 1)), com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.WATTS, com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.TICKS / 20.0, "2NaCl + CaCO3 → Na2CO3 + CaCl2（捨てる）。アンモニアは回って戻る（補給は少し）");
        rec(a, Chemistry.BOTTLE_MACHINE.block, "ガラス瓶", L(io(Chemistry.SODA_LIME_GLASS, 1)), L(io(Chemistry.GLASS_BOTTLE, 1)),
            com.taku3.semiconductorlab.block.BottleMachineBlockEntity.WATTS, com.taku3.semiconductorlab.block.BottleMachineBlockEntity.TICKS / 20.0, "溶けたガラスを型に吸い上げて吹く（1903 オーエンス）。止めずに作り続ける");
        rec(a, Chemistry.AUTOCLAVE.block, "ナイロン 66", L(io(Chemistry.NYLON_SALT, 1), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.AutoclaveBlockEntity.RUNS_PER_WATER)),
            L(io(Chemistry.NYLON_66, 1)), com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS, 30.0, "ナイロン塩の水溶液を約270℃・15気圧以上（1935 カロザース）。圧力が足りないと塩のまま");
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

    /** [2-64] フッ素ゴム（IMP3、O-029）：アセチレン＋HF → ＋Cl2 → 熱分解 → VDF、TFE → 熱分解 → HFP、乳化重合 → MgO で加硫 → O リング。HF・塩素の事故 */
    @GameTest
    public void fluororubber(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "difluoroethane_gas", Chemistry.ACETYLENE_GAS) && recipeUses(h, "difluoroethane_gas", Chemistry.HYDROFLUORIC_ACID), "C2H2 + 2HF");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.CHLORODIFLUOROETHANE_GAS).is(Chemistry.VINYLIDENE_FLUORIDE_GAS), "crack -> VDF");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.TETRAFLUOROETHYLENE_GAS).is(Chemistry.HEXAFLUOROPROPYLENE_GAS), "TFE -> HFP");
        h.assertTrue(recipeUses(h, "fkm_raw_rubber", Chemistry.VINYLIDENE_FLUORIDE_GAS) && recipeUses(h, "fkm_raw_rubber", Chemistry.HEXAFLUOROPROPYLENE_GAS), "copolymer");
        h.assertTrue(recipeUses(h, "fkm_compound", com.taku3.semiconductorlab.content.Metallurgy.MAGNESIUM_OXIDE), "MgO cure");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.FKM_COMPOUND).is(Chemistry.FKM_O_RING), "bake -> O-ring");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ((com.taku3.semiconductorlab.item.CraftLeakItem) Chemistry.DIFLUOROETHANE_GAS).leak(player);
        h.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "HF leak -> brief weakness");
        h.succeed();
    }
}
