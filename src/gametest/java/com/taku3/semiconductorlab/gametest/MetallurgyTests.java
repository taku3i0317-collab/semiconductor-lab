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
 * GameTest：幹1 冶金（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class MetallurgyTests extends LabTestBase {
    @GameTest public void oreSphalerite(GameTestHelper h) { oreFeature(h, "sphalerite_ore", Blocks.STONE, Blocks.COBBLESTONE, Metallurgy.SPHALERITE_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreGalena(GameTestHelper h)     { oreFeature(h, "galena_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.GALENA_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreGraphite(GameTestHelper h)   { oreFeature(h, "graphite_ore", Blocks.DEEPSLATE, Blocks.STONE, Metallurgy.GRAPHITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreMagnetite(GameTestHelper h)  { oreFeature(h, "magnetite_ore", Blocks.DEEPSLATE, Blocks.STONE, Metallurgy.MAGNETITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreWolframite(GameTestHelper h) { oreFeature(h, "wolframite_ore", Blocks.GRANITE, Blocks.STONE, Metallurgy.WOLFRAMITE_ORE, s -> s.is(Blocks.GRANITE)); }
    @GameTest public void oreMalachite(GameTestHelper h)  { oreFeature(h, "malachite_ore", Blocks.STONE, Blocks.DIRT, Metallurgy.MALACHITE_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreIronSand(GameTestHelper h)   { oreFeature(h, "iron_sand", Blocks.SAND, Blocks.SANDSTONE, Metallurgy.IRON_SAND, s -> s.is(Blocks.SAND)); }

    @GameTest public void oreQuartzVein(GameTestHelper h) { oreFeature(h, "quartz_vein", Blocks.STONE, Blocks.DEEPSLATE, Metallurgy.QUARTZ_VEIN, LabTestBase::stoneOrDeepslate); }

    /** [1-46] 石英鉱脈：石のツルハシ以上（木では落ちない）で珪石、シルクタッチでブロック。オーバーワールド全体（ほかの鉱石と同じ選び方）。MOD のレシピにネザークォーツは無い */
    @GameTest
    public void quartzVeinDrops(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        BlockState st = Metallurgy.QUARTZ_VEIN.defaultBlockState();
        ItemStack wood = new ItemStack(Items.WOODEN_PICKAXE), stone = new ItemStack(Items.STONE_PICKAXE), silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        h.assertFalse(wood.isCorrectToolForDrops(st), "quartz is Mohs 7: a wooden pickaxe is not enough");
        h.assertTrue(stone.isCorrectToolForDrops(st), "a stone pickaxe works");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, stone);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(Metallurgy.VEIN_QUARTZ)), "pickaxe -> vein quartz, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(Metallurgy.QUARTZ_VEIN.asItem()), "silk touch -> block, got " + silky);
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

    @GameTest public void oreBorax(GameTestHelper h)      { oreFeature(h, "borax_deposit", Blocks.SAND, Blocks.STONE, Metallurgy.BORAX_DEPOSIT, s -> s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE)); }

    /** [1-33] ホウ素：堆積→ホウ砂（木のツルハシ、シルクでブロック）→＋硫酸→ホウ酸2→かまど→B2O3。砂漠だけ */
    @GameTest
    public void boronChain(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = Metallurgy.BORAX_DEPOSIT.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "borax is soft (Mohs 2-2.5): a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(Metallurgy.BORAX)), "pickaxe -> borax, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(Metallurgy.BORAX_DEPOSIT.asItem()), "silk touch -> block, got " + silky);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("boric_acid"));
        var r = lv.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.size(), 2, "borax + sulfuric acid");
        h.assertTrue(recipeUses(h, "boric_acid", Metallurgy.BORAX) && recipeUses(h, "boric_acid", Chemistry.SULFURIC_ACID), "Na2B4O7 + H2SO4");
        ItemStack out = ((net.minecraft.world.item.crafting.CraftingRecipe) r).assemble(net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Metallurgy.BORAX), new ItemStack(Chemistry.SULFURIC_ACID))));
        h.assertTrue(out.is(Metallurgy.BORIC_ACID) && out.getCount() == 2, "4 mol H3BO3 = 2 items (2 mol each), got " + out);
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.BORIC_ACID).is(Metallurgy.BORON_TRIOXIDE), "2 H3BO3 -> B2O3 + 3 H2O in a furnace");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "borax_deposit"), "borax in deserts");
        h.assertFalse(biomeHas(h, net.minecraft.world.level.biome.Biomes.PLAINS, "borax_deposit"), "no borax in plains");
        LOG.info("[LAB] boron: deposit -> borax -> + H2SO4 -> 2 boric acid -> furnace -> B2O3, desert only");
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
                {Metallurgy.SPHALERITE_ORE, Metallurgy.SPHALERITE}, {Metallurgy.GALENA_ORE, Metallurgy.GALENA},
                {Metallurgy.GRAPHITE_ORE, Metallurgy.GRAPHITE}, {Metallurgy.MAGNETITE_ORE, Metallurgy.MAGNETITE},
                {Metallurgy.WOLFRAMITE_ORE, Metallurgy.WOLFRAMITE}, {Metallurgy.IRON_SAND, Metallurgy.MAGNETITE}};
        for (Object[] o : ores) {
            Block b = (Block) o[0];
            net.minecraft.world.item.Item raw = (net.minecraft.world.item.Item) o[1];
            List<ItemStack> normal = Block.getDrops(b.defaultBlockState(), lv, at, null, null, pick);
            List<ItemStack> silky = Block.getDrops(b.defaultBlockState(), lv, at, null, null, silk);
            h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(raw)), b + " with a pickaxe should drop " + raw + ", got " + normal);
            h.assertTrue(silky.size() == 1 && silky.get(0).is(b.asItem()), b + " with silk touch should drop itself, got " + silky);
        }
        for (Block m : new Block[] {Power.STEAM_ENGINE.block, Power.STEAM_TURBINE.block, Power.LEAD_ACID_BATTERY.block, Power.WATER_WHEEL.block, Gas.ELECTROLYZER.block, Gas.GAS_HOLDER.block,
                Metallurgy.BESSEMER_CONVERTER.block, Power.COPPER_CABLE.block, Semiconductor.ZONE_REFINING_FURNACE.block, Metallurgy.HYDROGEN_FURNACE.block, Gas.COMPRESSOR.block,
                Vacuum.VACUUM_CHAMBER.block, Vacuum.HAND_VACUUM_PUMP, Vacuum.SEALING_STATION.block, Power.ELECTRIC_FURNACE.block, Heat.ACHESON_FURNACE.block, Heat.REVERBERATORY_FURNACE.block, Optics.OXYHYDROGEN_BURNER.block}) {
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
        for (Block b : new Block[] {Metallurgy.SPHALERITE_ORE, Metallurgy.MAGNETITE_ORE, Metallurgy.WOLFRAMITE_ORE, Metallurgy.GALENA_ORE}) {
            h.assertFalse(stone.isCorrectToolForDrops(b.defaultBlockState()), b + " must need an iron pickaxe (stone is not enough)");
            h.assertTrue(iron.isCorrectToolForDrops(b.defaultBlockState()), b + " should drop with an iron pickaxe");
        }
        h.assertFalse(wood.isCorrectToolForDrops(Metallurgy.GRAPHITE_ORE.defaultBlockState()), "graphite ore needs a stone pickaxe");
        h.assertTrue(stone.isCorrectToolForDrops(Metallurgy.GRAPHITE_ORE.defaultBlockState()), "graphite ore with a stone pickaxe");
        h.assertTrue(new ItemStack(Items.IRON_SHOVEL).getDestroySpeed(Metallurgy.IRON_SAND.defaultBlockState()) > 1f, "iron sand: shovel is the right tool");
        h.assertTrue(new ItemStack(Items.IRON_AXE).getDestroySpeed(Power.WATER_WHEEL.block.defaultBlockState()) > 1f, "water wheel: axe is the right tool");
        for (Block m : new Block[] {Power.STEAM_ENGINE.block, Power.STEAM_TURBINE.block, Power.LEAD_ACID_BATTERY.block, Gas.ELECTROLYZER.block, Gas.GAS_HOLDER.block, Metallurgy.BESSEMER_CONVERTER.block, Semiconductor.ZONE_REFINING_FURNACE.block, Heat.REVERBERATORY_FURNACE.block}) {
            h.assertTrue(iron.isCorrectToolForDrops(m.defaultBlockState()), m + " should be collected with a pickaxe");
            h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(m.defaultBlockState()), m + " should not drop when broken by hand");
        }
        LOG.info("[LAB] tools: 4 ores need iron pickaxe, graphite stone+, iron sand shovel, water wheel axe, machines pickaxe");
        h.succeed();
    }

    // =====================================================================
    // ベッセマー転炉 [1-26]
    // =====================================================================

    @GameTest
    public void bessemerConverter(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Metallurgy.BESSEMER_CONVERTER.block);
        BessemerConverterBlockEntity be = h.getBlockEntity(pos, BessemerConverterBlockEntity.class);
        var c = be.getContainer();
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Items.IRON_INGOT, 3));

        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS + 100, 0);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).isEmpty(), "no power (no blast) -> no steel");

        long feed = 4000;
        tickConsumer(h, pos, be, BessemerConverterBlockEntity.TOTAL_TICKS - 1, feed);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).isEmpty(), "not finished 1 tick before 20 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(BessemerConverterBlockEntity.SLOT_OUT).is(Metallurgy.STEEL_INGOT), "steel after 20 s");
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
        c.setItem(BessemerConverterBlockEntity.SLOT_OUT, new ItemStack(Metallurgy.STEEL_INGOT, 64));
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
        h.assertTrue(new ItemStack(Metallurgy.STEEL_INGOT).is(steelTag), "steel ingot should be in #c:ingots/steel");
        LOG.info("[LAB] bessemer: iron -> steel in {} ticks with 20 kW, nothing without power", BessemerConverterBlockEntity.TOTAL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // [1-27] 鉛（方鉛鉱 → 焙焼 → PbO → 還元 → 鉛）
    // =====================================================================

    @GameTest
    public void leadFromGalena(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        ItemStack pbo = cook(h, SMELT, Metallurgy.GALENA);
        h.assertTrue(pbo.is(Metallurgy.LITHARGE), "roasting galena should give litharge (PbO), got " + pbo);
        ItemStack pb = cook(h, SMELT, Metallurgy.LITHARGE);
        h.assertTrue(pb.is(Metallurgy.LEAD_INGOT), "reducing PbO should give a lead ingot, got " + pb);
        h.assertFalse(cook(h, SMELT, Metallurgy.GALENA).is(Metallurgy.LEAD_INGOT), "galena must not jump straight to lead (roast first)");
        var leadTag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/lead"));
        var ingots = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots"));
        h.assertTrue(new ItemStack(Metallurgy.LEAD_INGOT).is(leadTag), "lead ingot should be in #c:ingots/lead");
        h.assertTrue(new ItemStack(Metallurgy.LEAD_INGOT).is(ingots), "lead ingot should be in #c:ingots");
        LOG.info("[LAB] lead: galena -> litharge (PbO) -> lead ingot, tagged c:ingots/lead");
        h.succeed();
    }

    // =====================================================================
    // [1-28] タングステン線（WO3 → 水素還元 → W粉 → 通電焼結 → 線引き）
    // =====================================================================

    @GameTest
    public void tungstenHydrogenFurnace(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.TUNGSTIC_ACID).is(Metallurgy.TUNGSTEN_TRIOXIDE), "tungstic acid -> WO3 (task09 real route)");
        h.assertTrue(recipeUses(h, "tungsten_wire", Metallurgy.TUNGSTEN_BAR), "wire is drawn from the sintered bar");
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Metallurgy.HYDROGEN_FURNACE.block);
        HydrogenFurnaceBlockEntity be = h.getBlockEntity(pos, HydrogenFurnaceBlockEntity.class);
        var c = be.getContainer();
        int IN = HydrogenFurnaceBlockEntity.SLOT_IN, H2 = HydrogenFurnaceBlockEntity.SLOT_H2, OUT = HydrogenFurnaceBlockEntity.SLOT_OUT;
        c.setItem(IN, new ItemStack(Metallurgy.TUNGSTEN_TRIOXIDE, 2));

        // 水素なし：電気があっても還元しない（炭素は使わない）
        long reduceFeed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.REDUCE_WATTS);
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, reduceFeed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no hydrogen -> no reduction");
        // 水素5袋、電気なし：止まる（上で入れた電気がバッファに残っているので、先に抜く）
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_GAS, 5));
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> no reduction");
        // 電気あり：30秒で W粉1、WO3 -1、H2 -3（WO3 + 3H2 -> W + 3H2O）
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS - 1, reduceFeed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        tickConsumer(h, pos, be, 1, reduceFeed);
        h.assertTrue(c.getItem(OUT).is(Metallurgy.TUNGSTEN_POWDER), "WO3 + 3 H2 -> W powder");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "WO3 used 1");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "H2 used 3");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        // 水素2袋では足りない（3要る）→ 止まる
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, reduceFeed);
        h.assertValueEqual(c.getItem(OUT).getCount(), 1, "2 H2 is not enough for WO3");

        // 焼結：W粉2 + H2 1 + 10kW×20秒 → 焼結棒
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(Metallurgy.TUNGSTEN_POWDER, 3));
        long sinterFeed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.SINTER_WATTS);
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.SINTER_TICKS, sinterFeed);
        h.assertTrue(c.getItem(OUT).is(Metallurgy.TUNGSTEN_BAR), "2 W powder + H2 -> sintered bar");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "powder used 2");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "H2 used 1");
        // 粉1個では焼結しない
        tickConsumer(h, pos, be, HydrogenFurnaceBlockEntity.SINTER_TICKS + 10, sinterFeed);
        h.assertValueEqual(c.getItem(OUT).getCount(), 1, "1 powder is not enough for a bar");
        // 3kW しか来ないと、電気が貯まった tick にしか進まない（約3割の速さ）→ 2倍の時間でも終わらない
        c.setItem(IN, new ItemStack(Metallurgy.TUNGSTEN_POWDER, 2));
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
        h.setBlock(fp, Metallurgy.HYDROGEN_FURNACE.block);
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        g.setContentsForTest(GasType.HYDROGEN, 20);
        g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(Gas.HYDROGEN_GAS, 64)); // 出口を埋めて量を固定
        h.runAfterDelay(25, () -> {
            var c = h.getBlockEntity(fp, HydrogenFurnaceBlockEntity.class).getContainer();
            h.assertValueEqual(c.getItem(HydrogenFurnaceBlockEntity.SLOT_H2).getCount(), 16, "furnace H2 slot filled to 16 from the holder");
            h.assertValueEqual(g.getAmount(), 4, "holder gave 16 of 20 mol");
            LOG.info("[LAB] hydrogen furnace pulled 16 H2 from an adjacent gas holder (20 -> 4 mol)");
            h.succeed();
        });
    }

    // =====================================================================
    // [1-16] トリクロロシラン合成・蒸留（タスク22）
    // =====================================================================

    /** 金属シリコン1＋乾いた HCl 3 → 粗 TCS（99%）＋H2。蒸留1段で「9」が1つ増え、9段で 11N。塩酸（水溶液）は入らない */
    @GameTest
    public void trichlorosilaneReactor(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Optics.QUARTZ_TUBE, Metallurgy.STEEL_INGOT, Power.COPPER_CABLE.block.asItem()})
            h.assertTrue(recipeUses(h, "trichlorosilane_reactor", it), "reactor + column: " + it);
        h.assertFalse(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.isHcl(new ItemStack(Gas.HYDROCHLORIC_ACID)), "hydrochloric acid (water) is not accepted: SiHCl3 reacts with water");
        // 純度：粗 2N → 蒸留9段で 11N、それ以上は上がらない
        double p = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.CRUDE;
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(p), 2, "crude TCS 99%");
        for (int i = 0; i < 9; i++) p = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.distilled(p);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(p), 11, "9 stages -> 11N");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.distilled(p)), 11, "11N is the ceiling");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.Purity.TEN_N), 10, "TEN_N reads as 10N (rounding fix)");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.TRICHLOROSILANE_REACTOR.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.class);
        var c = be.getContainer();
        int SI = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_SI, HCL = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_HCL, D = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_DISTILL, OUT = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_OUT, H2 = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SLOT_H2;
        int TS = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_TICKS, TD = com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_WATTS);
        c.setItem(SI, new ItemStack(Metallurgy.METALLURGICAL_SILICON, 2));
        c.setItem(HCL, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2));
        tickConsumer(h, fp, be, TS + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "2 HCl is not enough (Si + 3HCl)");
        c.setItem(HCL, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 6));
        tickConsumer(h, fp, be, TS, feed);
        ItemStack t = c.getItem(OUT);
        h.assertTrue(t.is(Metallurgy.TRICHLOROSILANE) && t.getCount() == 1, "Si + 3HCl -> SiHCl3, got " + t);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.purityOf(t)), 2, "crude 99%");
        h.assertTrue(c.getItem(H2).is(Gas.HYDROGEN_GAS) && c.getItem(H2).getCount() == 1, "+ H2");
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
        for (var it : new net.minecraft.world.item.Item[] {Power.GRAPHITE_ROD, Metallurgy.STEEL_INGOT, Optics.QUARTZ_TUBE, Power.COPPER_CABLE.block.asItem()})
            h.assertTrue(recipeUses(h, "siemens_reactor", it), "steel bell jar + graphite electrode chucks + sight glass + heavy cable: " + it);
        double kwh = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS * com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS / 20.0 / 3.6e6;
        h.assertTrue(kwh >= 85 && kwh <= 95, "about 90 kWh per kg (US 8,398,946), got " + kwh);
        h.assertValueEqual(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.HCL_OUT, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.HCL_PER_RUN, "HCl out = HCl for one TCS synthesis (closed loop)");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.SIEMENS_REACTOR.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.class);
        var c = be.getContainer();
        int T = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS);
        double p8 = 1 - 1e-8, p9 = com.taku3.semiconductorlab.Purity.NINE_N, p11 = com.taku3.semiconductorlab.Purity.ELEVEN_N;
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p8));
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 4));
        tickConsumer(h, fp, be, T + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).isEmpty(), "8N TCS is not pure enough (distil to 9N)");
        h.assertFalse(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.pureEnough(com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p8)), "8N rejected");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p9));
        tickConsumer(h, fp, be, T, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).isEmpty(), "half power cannot keep the rods at 1100 C");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p9));
        tickConsumer(h, fp, be, T, feed);
        ItemStack si = c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT);
        h.assertTrue(si.is(Metallurgy.POLYSILICON) && si.getCount() == 1, "SiHCl3 + H2 -> Si, got " + si);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(si.get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 9, "9N TCS -> 9N polysilicon");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_HCL).is(Metallurgy.HYDROGEN_CHLORIDE_GAS) && c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_HCL).getCount() == 3, "+ 3 HCl gas");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS).isEmpty(), "1 TCS used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_H2).getCount(), 3, "1 H2 used");
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_TCS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.tcs(p11));
        tickConsumer(h, fp, be, T, feed);
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(c.getItem(com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 11, "11N TCS -> 11N polysilicon");
        LOG.info("[LAB] siemens: TCS (>=9N) + H2 + {} kWh -> polysilicon (same purity) + 3 HCl; 8N and half power rejected", kwh);
        h.succeed();
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
        for (String id : new String[] {"sphalerite_ore", "graphite_ore", "magnetite_ore", "wolframite_ore", "galena_ore", "quartz_vein", "malachite_ore"})
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

    /** WO3 の本物のルート：鉄マンガン重石＋ソーダ →（かまど）→ Na2WO4 ＋塩酸 → H2WO4 →（かまど）→ WO3。鉱石を焼くだけの仮ルートは消えた */
    @GameTest
    public void tungstenTrioxideRoute(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, SMELT, Metallurgy.WOLFRAMITE).isEmpty(), "roasting wolframite alone no longer gives WO3 (placeholder removed)");
        h.assertTrue(recipeUses(h, "wolframite_soda_charge", Metallurgy.WOLFRAMITE) && recipeUses(h, "wolframite_soda_charge", Chemistry.SODIUM_CARBONATE), "wolframite + soda [2-03]");
        h.assertTrue(cook(h, SMELT, Metallurgy.WOLFRAMITE_SODA_CHARGE).is(Metallurgy.SODIUM_TUNGSTATE), "soda fusion -> Na2WO4");
        h.assertTrue(recipeUses(h, "tungstic_acid", Metallurgy.SODIUM_TUNGSTATE) && recipeUses(h, "tungstic_acid", Gas.HYDROCHLORIC_ACID), "Na2WO4 + 2HCl [6-06] -> H2WO4");
        h.assertTrue(cook(h, SMELT, Metallurgy.TUNGSTIC_ACID).is(Metallurgy.TUNGSTEN_TRIOXIDE), "H2WO4 -> WO3 + H2O");
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
        h.assertValueEqual(s_count(h, "crude_germanium_tetrachloride", Gas.HYDROCHLORIC_ACID), 2L, "GeO2 + 4HCl: 2 bottles of acid (2 mol each)");
        h.assertTrue(recipeUses(h, "crude_germanium_tetrachloride", Metallurgy.GERMANIUM_DIOXIDE), "starts from GeO2");
        h.assertTrue(recipeUses(h, "quartz_still", Optics.QUARTZ_TUBE) && recipeUses(h, "quartz_still", Optics.FUSED_QUARTZ), "still is quartz glassware");
        // 蒸留：作業台で使っても蒸留器は戻る
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("germanium_tetrachloride"));
        var r = (net.minecraft.world.item.crafting.CraftingRecipe) server.getRecipeManager().byKey(key).orElseThrow().value();
        var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Metallurgy.CRUDE_GERMANIUM_TETRACHLORIDE), new ItemStack(Metallurgy.QUARTZ_STILL)));
        h.assertTrue(r.matches(input, h.getLevel()), "crude GeCl4 + still matches");
        h.assertTrue(r.assemble(input).is(Metallurgy.GERMANIUM_TETRACHLORIDE), "-> distilled GeCl4");
        var rem = r.getRemainingItems(input);
        h.assertTrue(rem.stream().anyMatch(st -> st.is(Metallurgy.QUARTZ_STILL)), "the quartz still comes back, got " + rem);
        h.assertTrue(recipeUses(h, "high_purity_germanium_dioxide", Metallurgy.GERMANIUM_TETRACHLORIDE) && recipeUses(h, "high_purity_germanium_dioxide", Items.WATER_BUCKET), "hydrolysis GeCl4 + 2H2O");
        // 水素炉：高純度 GeO2 + 2H2 → 経路つきの Ge、ふつうの GeO2 → 経路なし
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.HYDROGEN_FURNACE.block);
        HydrogenFurnaceBlockEntity f = h.getBlockEntity(fp, HydrogenFurnaceBlockEntity.class);
        var fc = f.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(HydrogenFurnaceBlockEntity.REDUCE_WATTS);
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_IN, new ItemStack(Metallurgy.HIGH_PURITY_GERMANIUM_DIOXIDE, 1));
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 4));
        tickConsumer(h, fp, f, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS, feed);
        ItemStack ge = fc.getItem(HydrogenFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(ge.is(Metallurgy.RAW_GERMANIUM) && ZoneRefiningFurnaceBlockEntity.isChlorideRoute(ge), "HP GeO2 + 2H2 -> Ge with chloride route, got " + ge);
        h.assertValueEqual(fc.getItem(HydrogenFurnaceBlockEntity.SLOT_H2).getCount(), 2, "2 H2 used (GeO2 + 2H2)");
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        fc.setItem(HydrogenFurnaceBlockEntity.SLOT_IN, new ItemStack(Metallurgy.GERMANIUM_DIOXIDE, 1));
        tickConsumer(h, fp, f, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS, feed);
        ItemStack ge2 = fc.getItem(HydrogenFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(ge2.is(Metallurgy.RAW_GERMANIUM) && !ZoneRefiningFurnaceBlockEntity.isChlorideRoute(ge2), "plain GeO2 (sulfate route) -> Ge without route");
        // 帯域精製：人造黒鉛ボートでも硫酸経由は 9N で止まる、GeCl4 経由は 10N
        BlockPos zp = p(3, 1, 1);
        h.setBlock(zp, Semiconductor.ZONE_REFINING_FURNACE.block);
        ZoneRefiningFurnaceBlockEntity z = h.getBlockEntity(zp, ZoneRefiningFurnaceBlockEntity.class);
        var zc = z.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT;
        zc.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(Semiconductor.SYNTHETIC_GRAPHITE_BOAT));
        zc.setItem(G, ge2.copy());
        tickRefiner(h, zp, z, T * 4);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.NINE_N, "sulfate-route Ge stops at 9N even with a synthetic graphite boat");
        h.assertValueEqual(z.getLastWatts(), 0.0, "idle at the route cap");
        zc.setItem(G, ge.copy());
        tickRefiner(h, zp, z, T * 3);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.TEN_N, "GeCl4-route Ge reaches 10N");
        h.assertTrue(ZoneRefiningFurnaceBlockEntity.isChlorideRoute(zc.getItem(G)), "route is kept through refining");
        // 粘土ボートなら GeCl4 経由でも 9N（低いほうの上限）
        zc.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(Semiconductor.GRAPHITE_BOAT));
        zc.setItem(G, rawChloride(1));
        tickRefiner(h, zp, z, T * 4);
        h.assertValueEqual(purityOf(zc.getItem(G)), Purity.NINE_N, "clay boat caps GeCl4-route Ge at 9N");
        // 硫酸のルートも残っている
        h.assertTrue(recipeUses(h, "germanium_dioxide", Chemistry.SULFURIC_ACID), "concentrate + sulfuric acid -> GeO2 kept");
        LOG.info("[LAB] GeCl4: GeO2 + 2 HCl -> crude -> still (returned) -> GeCl4 -> + water -> HP GeO2 -> H2 furnace -> Ge(route); refining caps 10N (GeCl4) / 9N (sulfate)");
        h.succeed();
    }

    /** [1-03] 銅の製錬（孔雀石、IMP2-01）：孔雀石 →かまど/溶鉱炉→ 銅インゴット（CuO + C → Cu + CO）。シルクタッチで鉱石ブロック、ふつうは孔雀石 */
    @GameTest
    public void copperFromMalachite(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.MALACHITE).is(Items.COPPER_INGOT), "malachite + charcoal fire -> copper");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.MALACHITE).is(Items.COPPER_INGOT), "blast furnace too");
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Metallurgy.MALACHITE_ORE);
        var drops = net.minecraft.world.level.block.Block.getDrops(h.getBlockState(pos), h.getLevel(), h.absolutePos(pos), null, null, new ItemStack(Items.STONE_PICKAXE));
        h.assertTrue(drops.size() == 1 && drops.get(0).is(Metallurgy.MALACHITE), "stone pickaxe -> malachite");
        h.assertTrue(h.getBlockState(pos).is(net.minecraft.tags.BlockTags.NEEDS_STONE_TOOL), "needs a stone tool");
        LOG.info("[LAB] copper [1-03]: malachite ore (shallow stone) -> malachite -> furnace -> copper ingot");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Metallurgy.BESSEMER_CONVERTER.block, "ベッセマー転炉", L(cond(io(Items.IRON_INGOT, 1), "銑鉄の近似")), L(io(Metallurgy.STEEL_INGOT, 1)),
            BessemerConverterBlockEntity.WATTS, BessemerConverterBlockEntity.TOTAL_TICKS / 20.0, "送風の動力を電力で代表");
        rec(a, Metallurgy.HYDROGEN_FURNACE.block, "WO3 の水素還元", L(io(Metallurgy.TUNGSTEN_TRIOXIDE, 1), io(Gas.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.REDUCE_H2)),
            L(io(Metallurgy.TUNGSTEN_POWDER, 1)), HydrogenFurnaceBlockEntity.REDUCE_WATTS, HydrogenFurnaceBlockEntity.REDUCE_TICKS / 20.0, "WO3 + 3H2 → W + 3H2O");
        rec(a, Metallurgy.HYDROGEN_FURNACE.block, "タングステン粉の焼結", L(io(Metallurgy.TUNGSTEN_POWDER, HydrogenFurnaceBlockEntity.SINTER_POWDER), io(Gas.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.SINTER_H2)),
            L(io(Metallurgy.TUNGSTEN_BAR, 1)), HydrogenFurnaceBlockEntity.SINTER_WATTS, HydrogenFurnaceBlockEntity.SINTER_TICKS / 20.0, null);
        rec(a, Metallurgy.HYDROGEN_FURNACE.block, "GeO2 の水素還元", L(alt(io(Metallurgy.GERMANIUM_DIOXIDE, 1), Metallurgy.HIGH_PURITY_GERMANIUM_DIOXIDE), io(Gas.HYDROGEN_GAS, HydrogenFurnaceBlockEntity.GE_REDUCE_H2)),
            L(io(Metallurgy.RAW_GERMANIUM, 1)), HydrogenFurnaceBlockEntity.REDUCE_WATTS, HydrogenFurnaceBlockEntity.GE_REDUCE_TICKS / 20.0, "GeO2 + 2H2 → Ge + 2H2O");
        rec(a, Metallurgy.TRICHLOROSILANE_REACTOR.block, "合成", L(io(Metallurgy.METALLURGICAL_SILICON, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SI_PER_RUN), mol(Metallurgy.HYDROGEN_CHLORIDE_GAS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.HCL_PER_RUN)),
            L(io(Metallurgy.TRICHLOROSILANE, 1), mol(Gas.HYDROGEN_GAS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.H2_PER_RUN)),
            com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_WATTS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.SYNTH_TICKS / 20.0, "Si + 3HCl → SiHCl3 + H2（約300℃）。できたては 99%");
        rec(a, Metallurgy.TRICHLOROSILANE_REACTOR.block, "蒸留（1段）", L(io(Metallurgy.TRICHLOROSILANE, 1)), L(cond(io(Metallurgy.TRICHLOROSILANE, 1), "「9」が1つ増える（最大 11N）")),
            com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_WATTS, com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.DISTILL_TICKS / 20.0, "沸点の差で B・P の塩化物を分ける");
        rec(a, Metallurgy.SIEMENS_REACTOR.block, "多結晶シリコン", L(cond(io(Metallurgy.TRICHLOROSILANE, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TCS_PER_RUN), "9N 以上に蒸留したもの"), mol(Gas.HYDROGEN_GAS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.H2_PER_RUN)),
            L(cond(io(Metallurgy.POLYSILICON, 1), "純度はトリクロロシランと同じ（9N〜11N）"), mol(Metallurgy.HYDROGEN_CHLORIDE_GAS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.HCL_OUT)),
            com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.TICKS / 20.0, "SiHCl3 + H2 → Si + 3HCl（約1100℃の棒が太る）。HCl はトリクロロシラン作りに戻せる");
    }
}