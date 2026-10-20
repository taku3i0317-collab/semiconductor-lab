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

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Chemistry.WOOD_RETORT.block, "木を乾留する", L(io(Items.OAK_LOG, 4), io(Items.COAL, 0.5)),
            L(io(Items.CHARCOAL, 4), io(Chemistry.WOOD_VINEGAR, 2), io(Chemistry.WOOD_TAR, 1)), 0, 4 * com.taku3.semiconductorlab.block.WoodRetortBlockEntity.DISTILL_TICKS / 20.0,
            "空気なしで約450℃（石炭・木炭1個で原木8本、電気なし）");
    }
}
