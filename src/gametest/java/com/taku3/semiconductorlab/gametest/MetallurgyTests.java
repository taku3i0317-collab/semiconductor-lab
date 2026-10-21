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
import net.minecraft.core.Direction;
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
    /** [1-40] クロム・[2-07] 重クロム酸カリウム（IMP2 O-015）：ソーダ焙焼 → 酸性化 → 熱で分解して Cr2O3 → アルミでテルミット還元 */
    @GameTest
    public void chromium(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "chromite_roast_charge", Metallurgy.CHROMITE) && recipeUses(h, "chromite_roast_charge", Chemistry.SODIUM_CARBONATE), "soda roast charge");
        ItemStack chromate = cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.CHROMITE_ROAST_CHARGE);
        h.assertTrue(chromate.is(Metallurgy.SODIUM_CHROMATE) && chromate.getCount() == 2, "oxidizing roast -> sodium chromate");
        h.assertTrue(recipeUses(h, "potassium_dichromate", Chemistry.SULFURIC_ACID) && recipeUses(h, "potassium_dichromate", Chemistry.POTASH), "acidify + potassium -> K2Cr2O7");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Chemistry.POTASSIUM_DICHROMATE).is(Metallurgy.CHROMIUM_OXIDE), "dichromate decomposes on heating -> Cr2O3");
        h.assertTrue(recipeUses(h, "thermite_charge", Metallurgy.ALUMINIUM_INGOT) && recipeUses(h, "thermite_charge", Metallurgy.CLAY_CRUCIBLE), "Cr2O3 + 2 Al in a crucible");
        ItemStack cr = cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.THERMITE_CHARGE);
        h.assertTrue(cr.is(Metallurgy.CHROMIUM) && cr.getCount() == 2, "Cr2O3 + 2Al -> 2Cr + Al2O3");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.CHROMITE).isEmpty(), "chromite cannot be smelted straight to chromium");
        LOG.info("[LAB] chromium [1-40][2-07]: chromite + soda + lime -> roast -> Na2CrO4 -> + H2SO4 + K2CO3 -> K2Cr2O7 -> heat -> Cr2O3 -> + 2 Al -> 2 Cr");
        h.succeed();
    }

    /** [1-11] るつぼ鋼（IMP2 O-010）：粘土＋焼粉 → るつぼ（溶鉱炉で焼く）→ るつぼ＋鍛鉄×2＋木炭 → 溶鉱炉で溶かす → るつぼ鋼×2（るつぼは割る） */
    @GameTest
    public void crucible_steel(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "raw_crucible", Heat.REFRACTORY_BRICK) && recipeUses(h, "raw_crucible", net.minecraft.world.item.Items.CLAY_BALL), "crucible = clay + grog (crushed refractory brick)");
        h.assertTrue(recipeUses(h, "clay_crucible", Metallurgy.RAW_CRUCIBLE), "raw crucible fired in the blast furnace");
        h.assertTrue(recipeUses(h, "crucible_charge", Metallurgy.CLAY_CRUCIBLE) && recipeUses(h, "crucible_charge", net.minecraft.world.item.Items.IRON_INGOT)
                && recipeUses(h, "crucible_charge", net.minecraft.world.item.Items.CHARCOAL), "charge = crucible + wrought iron + charcoal (carbon)");
        var lv = h.getLevel();
        var in = new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(Metallurgy.CRUCIBLE_CHARGE));
        ItemStack out = lv.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.BLASTING, in, lv)
                .map(r -> r.value().assemble(in)).orElse(ItemStack.EMPTY);
        h.assertTrue(out.is(Metallurgy.CRUCIBLE_STEEL) && out.getCount() == 2, "blast furnace melts the charge -> 2 crucible steel");
        h.assertTrue(lv.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING, in, lv).isEmpty(),
                "an ordinary furnace is not hot enough");
        LOG.info("[LAB] crucible_steel [1-11]: clay + grog -> crucible, + 2 wrought iron + charcoal -> blast furnace -> 2 crucible steel");
        h.succeed();
    }

    @GameTest public void oreSphalerite(GameTestHelper h) { oreFeature(h, "sphalerite_ore", Blocks.STONE, Blocks.COBBLESTONE, Metallurgy.SPHALERITE_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreGalena(GameTestHelper h)     { oreFeature(h, "galena_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.GALENA_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreStibnite(GameTestHelper h)   { oreFeature(h, "stibnite_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.STIBNITE_ORE, LabTestBase::stoneOrDeepslate); }   // [1-43] IMP3
    @GameTest public void orePyrite(GameTestHelper h)     { oreFeature(h, "pyrite_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, com.taku3.semiconductorlab.content.Chemistry.PYRITE_ORE, LabTestBase::stoneOrDeepslate); }   // [2-43] IMP3
    @GameTest public void orePyrolusite(GameTestHelper h) { oreFeature(h, "pyrolusite_ore", Blocks.STONE, Blocks.COBBLESTONE, Metallurgy.PYROLUSITE_ORE, LabTestBase::stoneOrDeepslate); }   // [4-28] IMP3 S-006
    @GameTest public void orePentlandite(GameTestHelper h) { oreFeature(h, "pentlandite_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.PENTLANDITE_ORE, LabTestBase::stoneOrDeepslate); }   // [1-37] IMP3
    @GameTest public void oreGraphite(GameTestHelper h)   { oreFeature(h, "graphite_ore", Blocks.DEEPSLATE, Blocks.STONE, Metallurgy.GRAPHITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreMagnetite(GameTestHelper h)  { oreFeature(h, "magnetite_ore", Blocks.DEEPSLATE, Blocks.STONE, Metallurgy.MAGNETITE_ORE, s -> s.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)); }
    @GameTest public void oreWolframite(GameTestHelper h) { oreFeature(h, "wolframite_ore", Blocks.GRANITE, Blocks.STONE, Metallurgy.WOLFRAMITE_ORE, s -> s.is(Blocks.GRANITE)); }
    @GameTest public void oreMalachite(GameTestHelper h)  { oreFeature(h, "malachite_ore", Blocks.STONE, Blocks.DIRT, Metallurgy.MALACHITE_ORE, LabTestBase::stoneOrDeepslate); }
    @GameTest public void oreCassiterite(GameTestHelper h) { oreFeature(h, "cassiterite_ore", Blocks.GRANITE, Blocks.STONE, Metallurgy.CASSITERITE_ORE, s -> s.is(Blocks.GRANITE)); }
    @GameTest public void oreTantalite(GameTestHelper h) { oreFeature(h, "tantalite_ore", Blocks.GRANITE, Blocks.STONE, Metallurgy.TANTALITE_ORE, x -> x.is(Blocks.GRANITE)); }   // [1-66] IMP4-01 S-003
    @GameTest public void oreSpodumene(GameTestHelper h) { oreFeature(h, "spodumene_ore", Blocks.GRANITE, Blocks.STONE, Metallurgy.SPODUMENE_ORE, x -> x.is(Blocks.GRANITE)); }   // [1-42] IMP3 S-006
    @GameTest public void oreFluorite(GameTestHelper h)   { oreFeature(h, "fluorite_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.FLUORITE_ORE, LabTestBase::stoneOrDeepslate); }   // [1-21] IMP4-01
    @GameTest public void oreChromite(GameTestHelper h)   { oreFeature(h, "chromite_ore", Blocks.DEEPSLATE, Blocks.COBBLESTONE, Metallurgy.CHROMITE_ORE, st -> st.is(Blocks.DEEPSLATE)); }   // [1-40] IMP2 O-015
    @GameTest public void oreBauxite(GameTestHelper h)    { oreFeature(h, "bauxite_ore", Blocks.DIRT, Blocks.COBBLESTONE, Metallurgy.BAUXITE_ORE, st -> st.is(Blocks.DIRT) || st.is(Blocks.COARSE_DIRT) || st.is(Blocks.STONE)); }   // [1-24] IMP4-01
    @GameTest public void oreIonAdsorptionClay(GameTestHelper h) { oreFeature(h, "ion_adsorption_clay", Blocks.DIRT, Blocks.COBBLESTONE, Metallurgy.ION_ADSORPTION_CLAY, x -> x.is(Blocks.DIRT) || x.is(Blocks.COARSE_DIRT) || x.is(Blocks.STONE)); }   // [1-19] IMP3 S-009
    @GameTest public void oreBastnaesite(GameTestHelper h) { oreFeature(h, "bastnaesite_ore", Blocks.STONE, Blocks.COBBLESTONE, Metallurgy.BASTNAESITE_ORE, LabTestBase::stoneOrDeepslate); }   // [1-19] IMP3 S-009

    /** [1-58] 伸線（IMP4-01）：引き板（戻る）で金線・銅線、ダイヤモンドのダイス（戻る）で金のボンディング線。金の粒はボンディング線にならない */
    @GameTest
    public void wireDrawing(GameTestHelper h) {
        var rm = h.getLevel().getServer().getRecipeManager();
        java.util.function.Function<List<ItemStack>, net.minecraft.world.item.crafting.CraftingInput> grid = items -> {
            List<ItemStack> g = new ArrayList<>(items); while (g.size() < 9) g.add(ItemStack.EMPTY);
            return net.minecraft.world.item.crafting.CraftingInput.of(3, 3, g);
        };
        var in1 = grid.apply(List.of(new ItemStack(Items.GOLD_INGOT), new ItemStack(Metallurgy.DRAW_PLATE)));
        var r1 = rm.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in1, h.getLevel()).orElseThrow().value();
        ItemStack gw = r1.assemble(in1);
        h.assertTrue(gw.is(Metallurgy.GOLD_WIRE) && gw.getCount() == 8, "gold ingot + draw plate -> 8 gold wire, got " + gw);
        h.assertTrue(r1.getRemainingItems(in1).stream().anyMatch(st -> st.is(Metallurgy.DRAW_PLATE)), "draw plate comes back");
        var in2 = grid.apply(List.of(new ItemStack(Metallurgy.GOLD_WIRE), new ItemStack(Metallurgy.DIAMOND_DIE)));
        var r2 = rm.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in2, h.getLevel()).orElseThrow().value();
        h.assertTrue(r2.assemble(in2).is(Metallurgy.GOLD_BONDING_WIRE) && r2.getRemainingItems(in2).stream().anyMatch(st -> st.is(Metallurgy.DIAMOND_DIE)), "gold wire + diamond die -> bonding wire, die back");
        h.assertTrue(recipeUses(h, "copper_wire", Items.COPPER_INGOT), "copper wire too");
        h.assertFalse(recipeUses(h, "gold_bonding_wire", Items.GOLD_NUGGET), "bonding wire is drawn wire, not nuggets");
        LOG.info("[LAB] wire drawing [1-58]: draw plate (kept) -> 8 gold / copper wire; diamond die (kept) -> 8 gold bonding wire");
        h.succeed();
    }

    /** [1-18] 白金の精製（IMP4-01）：NH4Cl、王水で溶かす（瓶が戻る）、沈殿、焼いて海綿、圧粉を溶鉱炉で → インゴット（c:ingots/platinum） */
    @GameTest
    public void platinumRefining(GameTestHelper h) {
        var CH = com.taku3.semiconductorlab.content.Chemistry.class;
        java.util.function.BiFunction<List<ItemStack>, Integer, net.minecraft.world.item.crafting.CraftingInput> grid = (items, wdt) -> {
            List<ItemStack> g = new ArrayList<>(items); while (g.size() < 9) g.add(ItemStack.EMPTY);
            return net.minecraft.world.item.crafting.CraftingInput.of(3, 3, g);
        };
        var rm = h.getLevel().getServer().getRecipeManager();
        var in1 = grid.apply(List.of(new ItemStack(com.taku3.semiconductorlab.content.Chemistry.AMMONIA_WATER), new ItemStack(com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID)), 3);
        var r1 = rm.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in1, h.getLevel()).orElseThrow().value();
        h.assertTrue(r1.assemble(in1).is(com.taku3.semiconductorlab.content.Chemistry.AMMONIUM_CHLORIDE), "NH3 + HCl -> NH4Cl");
        h.assertValueEqual((int) r1.getRemainingItems(in1).stream().filter(st -> st.is(com.taku3.semiconductorlab.content.Chemistry.GLASS_BOTTLE)).count(), 2, "both bottles back");
        List<ItemStack> aq = new ArrayList<>();
        aq.add(new ItemStack(Metallurgy.PLATINUM_GRAIN)); aq.add(new ItemStack(Metallurgy.PLATINUM_GRAIN));
        for (int i = 0; i < 3; i++) aq.add(new ItemStack(com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID));
        aq.add(new ItemStack(com.taku3.semiconductorlab.content.Gas.NITRIC_ACID)); aq.add(new ItemStack(com.taku3.semiconductorlab.content.Chemistry.GLASS_BOTTLE));
        var in2 = grid.apply(aq, 3);
        var r2 = rm.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in2, h.getLevel()).orElseThrow().value();
        h.assertTrue(r2.assemble(in2).is(com.taku3.semiconductorlab.content.Chemistry.CHLOROPLATINIC_ACID), "Pt + aqua regia (3 HCl : 1 HNO3) -> H2PtCl6");
        h.assertValueEqual((int) r2.getRemainingItems(in2).stream().filter(st -> st.is(com.taku3.semiconductorlab.content.Chemistry.GLASS_BOTTLE)).count(), 4, "4 acid bottles back");
        h.assertTrue(recipeUses(h, "ammonium_chloroplatinate", com.taku3.semiconductorlab.content.Chemistry.AMMONIUM_CHLORIDE), "precipitate with NH4Cl");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.AMMONIUM_CHLOROPLATINATE).is(Metallurgy.PLATINUM_SPONGE), "baked -> platinum sponge");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.PLATINUM_COMPACT).is(Metallurgy.PLATINUM_INGOT), "compact at white heat -> ingot");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.PLATINUM_GRAIN).isEmpty() && cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.PLATINUM_GRAIN).isEmpty(), "crude grains cannot be melted (1768 C)");
        h.assertTrue(new ItemStack(Metallurgy.PLATINUM_INGOT).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/platinum"))), "c:ingots/platinum");
        LOG.info("[LAB] platinum [1-18]: NH4Cl; 2 grains + aqua regia -> H2PtCl6; + 2 NH4Cl -> (NH4)2PtCl6; furnace -> sponge; 2 sponge -> compact; blast furnace -> ingot (Wollaston)");
        h.succeed();
    }

    /** [1-20] 砂白金（IMP4-01）：砂鉄を掘るとまれに（約3%）白金の粒。シルクタッチでは出ない */
    @GameTest
    public void placerPlatinum(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        ItemStack silk = new ItemStack(Items.IRON_SHOVEL);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        int pt = 0, ptSilk = 0, n = 2000;
        for (int i = 0; i < n; i++) {
            pt += Block.getDrops(Metallurgy.IRON_SAND.defaultBlockState(), lv, at, null, null, shovel).stream().filter(st -> st.is(Metallurgy.PLATINUM_GRAIN)).mapToInt(ItemStack::getCount).sum();
            ptSilk += Block.getDrops(Metallurgy.IRON_SAND.defaultBlockState(), lv, at, null, null, silk).stream().filter(st -> st.is(Metallurgy.PLATINUM_GRAIN)).mapToInt(ItemStack::getCount).sum();
        }
        h.assertTrue(pt > n * 0.01 && pt < n * 0.06, "about 3% platinum grains from iron sand, got " + pt + "/" + n);
        h.assertValueEqual(ptSilk, 0, "silk touch keeps the sand block (no grain)");
        LOG.info("[LAB] placer platinum [1-20]: {} grains in {} iron sand blocks (3%), none with silk touch", pt, n);
        h.succeed();
    }

    /** [1-29] ホール・エルー法（IMP4-01）：アルミナ2 → Al 1（15kWh）。浴（氷晶石）・陽極（黒鉛棒）・電気がないと止まる。陽極は2回、浴は16回 */
    @GameTest
    public void hallHeroult(GameTestHelper h) {
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.WATTS * com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS / 20.0 / 3.6e6 - com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.KWH_PER_INGOT) < 0.01, "100 kW x 540 s = 15 kWh per ingot");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.HALL_HEROULT_CELL.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_ALUMINA, new ItemStack(Metallurgy.ALUMINA, 10));
        c.setItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_ANODE, new ItemStack(com.taku3.semiconductorlab.content.Power.GRAPHITE_ROD, 2));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT).isEmpty(), "no cryolite bath -> alumina does not melt (2050 C)");
        c.setItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_CRYOLITE, new ItemStack(Metallurgy.CRYOLITE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        for (int i = 0; i < 4; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT).is(Metallurgy.ALUMINIUM_INGOT) && c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT).getCount() == 4, "8 alumina -> 4 aluminium, got " + c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_ANODE).isEmpty() && be.getAnode() == 0, "2 anodes burnt in 4 runs");
        h.assertValueEqual(be.getBath(), com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.RUNS_PER_CRYOLITE - 4, "1 cryolite bath lasts 16 runs");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS + 10, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.SLOT_OUT).getCount(), 4, "no anode -> stops");
        h.assertTrue(new ItemStack(Metallurgy.ALUMINIUM_INGOT).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ingots/aluminum"))), "tagged c:ingots/aluminum");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.ALUMINA).isEmpty(), "alumina cannot be smelted with carbon in a furnace");
        LOG.info("[LAB] Hall-Heroult [1-29]: 2 alumina + anode/2 + cryolite/16 -> 1 aluminium ({} kW x {} s = {} kWh)", com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.WATTS / 1000, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS / 20, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.KWH_PER_INGOT);
        h.succeed();
    }

    /** [1-38] 氷晶石の合成（IMP4-01）：フッ酸3＋Al(OH)3＋NaOH3 → 氷晶石（6HF + Al(OH)3 + 3NaOH）。フッ酸の鉛の瓶が3本戻る */
    @GameTest
    public void cryoliteSynthesis(GameTestHelper h) {
        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < 3; i++) grid.add(new ItemStack(com.taku3.semiconductorlab.content.Chemistry.HYDROFLUORIC_ACID));
        grid.add(new ItemStack(Metallurgy.ALUMINIUM_HYDROXIDE));
        for (int i = 0; i < 3; i++) grid.add(new ItemStack(com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE));
        grid.add(ItemStack.EMPTY); grid.add(ItemStack.EMPTY);
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid);
        var r = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
        h.assertTrue(r.assemble(input).is(Metallurgy.CRYOLITE), "3 HF + Al(OH)3 + 3 NaOH -> cryolite");
        long bottles = r.getRemainingItems(input).stream().filter(st -> st.is(Metallurgy.LEAD_BOTTLE)).count();
        h.assertValueEqual((int) bottles, 3, "3 lead bottles come back");
        h.assertFalse(recipeUses(h, "cryolite", Metallurgy.ALUMINA), "made from the hydroxide, not alumina");
        LOG.info("[LAB] cryolite [1-38]: 3 HF + Al(OH)3 + 3 NaOH -> Na3AlF6 (6:1:3 if 1 HF bottle = 2 HF), 3 lead bottles returned");
        h.succeed();
    }

    /** [1-14] バイヤー法（IMP4-01）：ボーキサイト2 → Al(OH)3＋赤泥。NaOH＋水の液は4回、液・電気がないと止まる。Al(OH)3 →かまど→ アルミナ */
    @GameTest
    public void bayerProcess(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.BAYER_DIGESTER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_BAUXITE, new ItemStack(Metallurgy.BAUXITE, 12));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT).isEmpty(), "no caustic liquor -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_NAOH, new ItemStack(com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE, 1));
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        for (int i = 0; i < 4; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT).is(Metallurgy.ALUMINIUM_HYDROXIDE) && c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT).getCount() == 4, "8 bauxite -> 4 Al(OH)3, got " + c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_MUD).is(Metallurgy.RED_MUD) && c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_MUD).getCount() == 4, "4 red mud");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_BUCKET).is(Items.BUCKET) && c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_NAOH).isEmpty(), "1 NaOH + 1 water = 4 runs, bucket back");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS + 10, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_OUT).getCount(), 4, "liquor used up -> stops");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.ALUMINIUM_HYDROXIDE).is(Metallurgy.ALUMINA), "Al(OH)3 calcined -> alumina");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.BAUXITE).isEmpty(), "bauxite cannot be smelted straight to aluminium");
        LOG.info("[LAB] Bayer [1-14]: 2 bauxite -> Al(OH)3 + red mud ({} s, {} kW), NaOH liquor lasts {} runs; Al(OH)3 -> furnace -> alumina", com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS / 20, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.WATTS / 1000, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.RUNS_PER_LIQUOR);
        h.succeed();
    }

    @GameTest public void oreIronSand(GameTestHelper h)   { oreFeature(h, "iron_sand", Blocks.SAND, Blocks.SANDSTONE, Metallurgy.IRON_SAND, s -> s.is(Blocks.SAND)); }

    @GameTest public void oreQuartzVein(GameTestHelper h) { oreFeature(h, "quartz_vein", Blocks.STONE, Blocks.DEEPSLATE, Metallurgy.QUARTZ_VEIN, LabTestBase::stoneOrDeepslate); }

    /** [1-46] 石英鉱脈：石のツルハシ以上（木では落ちない）で珪石、シルクタッチでブロック。オーバーワールド全体（ほかの鉱石と同じ選び方）。MOD のレシピにネザークォーツは無い */
    /** [1-23]・[1-31] モリブデン（IMP2 S-007）：輝水鉛鉱 →反射炉で焙焼 → MoO3（＋SO2）→水素炉 → Mo */
    @GameTest(maxTicks = 1000)
    public void molybdenum(GameTestHelper h) {
        h.assertTrue(com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity.mainProduct(new ItemStack(Metallurgy.MOLYBDENITE)).is(Metallurgy.MOLYBDENUM_TRIOXIDE)
                && com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity.flueGas(new ItemStack(Metallurgy.MOLYBDENITE)).is(com.taku3.semiconductorlab.content.Chemistry.SULFUR_DIOXIDE), "roasting MoS2 -> MoO3 + SO2");
        h.assertTrue(com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.modeOf(new ItemStack(Metallurgy.MOLYBDENUM_TRIOXIDE)) == com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.Mode.REDUCE_MO, "MoO3 goes to the hydrogen furnace");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Metallurgy.HYDROGEN_FURNACE.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.class);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.SLOT_IN, new ItemStack(Metallurgy.MOLYBDENUM_TRIOXIDE));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.SLOT_H2, new ItemStack(com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS, 3));
        tickConsumer(h, at, be, com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.REDUCE_TICKS + 10, com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.REDUCE_WATTS));
        h.assertTrue(be.getContainer().getItem(com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity.SLOT_OUT).is(Metallurgy.MOLYBDENUM), "MoO3 + 3H2 -> Mo");
        LOG.info("[LAB] molybdenum [1-31]: copper ore sometimes gives molybdenite; roast -> MoO3 + SO2; H2 -> Mo");
        h.succeed();
    }

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
        h.assertTrue(!normal.isEmpty() && normal.stream().anyMatch(s -> s.is(Metallurgy.VEIN_QUARTZ)) && normal.stream().allMatch(s -> s.is(Metallurgy.VEIN_QUARTZ) || s.is(Metallurgy.ROCK_CRYSTAL)), "pickaxe -> vein quartz (rarely + rock crystal, [8-15]), got " + normal);
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
            h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(raw) || (b == Metallurgy.IRON_SAND && (s.is(Metallurgy.PLATINUM_GRAIN) || s.is(Metallurgy.ILMENITE) || s.is(Metallurgy.ZIRCON) || s.is(Metallurgy.VANADIUM_MAGNETITE)))), b + " with a pickaxe should drop " + raw + ", got " + normal);   // 砂鉄はまれに砂白金 [1-20]・チタン鉄鉱 [1-41] も
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
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Metallurgy.PIG_IRON, 3));

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
        c.setItem(BessemerConverterBlockEntity.SLOT_IN, new ItemStack(Metallurgy.PIG_IRON, 1));
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

    /** [1-41] チタン（IMP3、O-018、クロール法）：チタン鉄鉱＋木炭＋塩素 → 粗 TiCl4 → 蒸留 → TiCl4 → ＋Mg2＋Ar → かまど → 塩の混ざったスポンジ → 真空蒸留 → スポンジチタン、MgCl2 が残る */
    @GameTest
    public void titaniumKroll(GameTestHelper h) {
        h.assertTrue(Metallurgy.ILMENITE_FROM_IRON_SAND > 0 && Metallurgy.ILMENITE_FROM_IRON_SAND < 0.5f, "ilmenite is a by-product of iron sand");
        h.assertTrue(recipeUses(h, "crude_titanium_tetrachloride", Metallurgy.ILMENITE) && recipeUses(h, "crude_titanium_tetrachloride", com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS), "chlorination");
        h.assertTrue(recipeUses(h, "titanium_tetrachloride", Metallurgy.QUARTZ_STILL), "distilled TiCl4");
        h.assertTrue(recipeUses(h, "kroll_charge", Metallurgy.MAGNESIUM) && recipeUses(h, "kroll_charge", com.taku3.semiconductorlab.content.Gas.ARGON_GAS), "Mg reduction under argon");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.KROLL_CHARGE).is(Metallurgy.SPONGE_WITH_SALT), "fired -> sponge with salt");
        var rem = Metallurgy.SPONGE_WITH_SALT.getCraftingRemainder(new ItemStack(Metallurgy.SPONGE_WITH_SALT));
        h.assertTrue(rem != null && rem.create().is(Metallurgy.ANHYDROUS_MAGNESIUM_CHLORIDE), "MgCl2 left over -> back to the magnesium cell");
        h.assertTrue(recipeUses(h, "titanium_sponge", Metallurgy.SPONGE_WITH_SALT), "vacuum distillation -> titanium sponge");
        LOG.info("[LAB] titanium [1-41]: ilmenite (from iron sand) + C + Cl2 -> TiCl4 -> + 2Mg under Ar -> sponge, MgCl2 recycled");
        h.succeed();
    }

    /** [1-39] マグネシウム（IMP3、O-018）：にがりをそのまま焼くと MgO（加水分解）、塩化水素の中なら無水 MgCl2。電解槽で Mg＋塩素 */
    @GameTest(maxTicks = 100)
    public void magnesiumDow(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, SMELT, Survival.BITTERN).is(Metallurgy.MAGNESIUM_OXIDE), "bittern roasted in air hydrolyses -> MgO");
        h.assertTrue(recipeUses(h, "bittern_under_hcl", Survival.BITTERN) && recipeUses(h, "bittern_under_hcl", Metallurgy.HYDROGEN_CHLORIDE_GAS), "dry under HCl");
        h.assertTrue(cook(h, SMELT, Metallurgy.BITTERN_UNDER_HCL).is(Metallurgy.ANHYDROUS_MAGNESIUM_CHLORIDE), "-> anhydrous MgCl2");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Metallurgy.MAGNESIUM_CELL.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.SLOT_IN, new ItemStack(Metallurgy.ANHYDROUS_MAGNESIUM_CHLORIDE, 2));
        for (int i = 0; i < 2; i++) tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.SLOT_OUT).is(Metallurgy.MAGNESIUM) && c.getItem(com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.SLOT_OUT).getCount() == 2, "2 MgCl2 -> 2 Mg");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.MagnesiumCellBlockEntity.SLOT_GAS).getCount(), 2, "and 2 chlorine");
        LOG.info("[LAB] magnesium [1-39]: bittern in air -> MgO; under HCl -> anhydrous MgCl2; Dow cell -> Mg + Cl2");
        h.succeed();
    }

    /** [1-37] ニッケル（IMP3、S-004）：ペントランド鉱＋砂 → 鈹 → 焙焼 → NiO → ＋H2 → 粗 Ni → ＋CO4 → Ni(CO)4 → 分解 → Ni。もれると事故 */
    @GameTest
    public void nickelMond(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(recipeUses(h, "matte_charge", Metallurgy.PENTLANDITE) && recipeUses(h, "matte_charge", Items.SAND), "ore + sand flux");
        h.assertTrue(cook(h, SMELT, Metallurgy.MATTE_CHARGE).is(Metallurgy.NICKEL_MATTE) && cook(h, SMELT, Metallurgy.NICKEL_MATTE).is(Metallurgy.NICKEL_OXIDE), "matte -> roast -> NiO");
        h.assertTrue(recipeUses(h, "crude_nickel", Metallurgy.NICKEL_OXIDE) && recipeUses(h, "crude_nickel", com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS), "NiO + H2 -> Ni");
        h.assertTrue(recipeUses(h, "carbon_monoxide_gas", Metallurgy.COKE), "coke + O2 -> CO");
        h.assertTrue(recipeUses(h, "nickel_carbonyl_gas", Metallurgy.CRUDE_NICKEL) && recipeUses(h, "nickel_carbonyl_gas", Metallurgy.CARBON_MONOXIDE_GAS), "Mond: Ni + 4CO");
        h.assertTrue(cook(h, SMELT, Metallurgy.NICKEL_CARBONYL_GAS).is(Metallurgy.NICKEL), "Ni(CO)4 -> Ni at 200 C");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        com.taku3.semiconductorlab.item.NickelCarbonylItem.leak(player);
        h.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA) && player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS), "a leak is an accident");
        LOG.info("[LAB] nickel [1-37]: pentlandite + sand -> matte -> NiO -> + H2 -> crude Ni -> + 4CO -> Ni(CO)4 -> 200 C -> Ni");
        h.succeed();
    }

    /** [1-43] アンチモン（IMP3、S-002）：輝安鉱 → 焙焼 → Sb2O3 → 還元 → アンチモン。いきなりアンチモンにはならない */
    @GameTest
    public void antimonyFromStibnite(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, SMELT, Metallurgy.STIBNITE).is(Metallurgy.ANTIMONY_TRIOXIDE), "roasting stibnite -> Sb2O3");
        h.assertTrue(cook(h, SMELT, Metallurgy.ANTIMONY_TRIOXIDE).is(Metallurgy.ANTIMONY), "reducing Sb2O3 -> antimony");
        h.assertFalse(cook(h, SMELT, Metallurgy.STIBNITE).is(Metallurgy.ANTIMONY), "stibnite does not jump straight to antimony");
        LOG.info("[LAB] antimony [1-43]: stibnite -> Sb2O3 -> antimony");
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
        for (String id : new String[] {"sphalerite_ore", "graphite_ore", "magnetite_ore", "wolframite_ore", "galena_ore", "stibnite_ore", "pentlandite_ore", "pyrite_ore", "pyrolusite_ore", "quartz_vein", "malachite_ore", "cassiterite_ore", "spodumene_ore", "tantalite_ore", "bastnaesite_ore", "fluorite_ore", "chromite_ore"})
            expect.put(id, b -> b.is(BiomeTags.IS_OVERWORLD));
        expect.put("iron_sand", b -> b.is(BiomeTags.IS_BEACH));
        expect.put("bauxite_ore", b -> b.is(BiomeTags.IS_JUNGLE) || b.is(BiomeTags.IS_SAVANNA));
        expect.put("ion_adsorption_clay", b -> b.is(BiomeTags.IS_JUNGLE) || b.is(BiomeTags.IS_SAVANNA));   // [1-19] IMP3 S-009
        expect.put("nitre_earth_block", b -> b.is(net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES));   // [6-16] IMP2-02
        expect.put("natural_asphalt", b -> b.is(BiomeTags.IS_BADLANDS) || b.is(net.minecraft.world.level.biome.Biomes.DESERT));   // [2-31] IMP3
        expect.put("rubber_trees", b -> b.is(BiomeTags.IS_JUNGLE));   // [2-29] IMP3（木。植生の段階）
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

    /** [1-30] 錫の製錬（IMP2-01）：錫石 →かまど/溶鉱炉→ スズのインゴット（SnO2 + 2C → Sn + 2CO）。c:ingots/tin */
    @GameTest
    public void tinFromCassiterite(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.CASSITERITE).is(Metallurgy.TIN_INGOT), "cassiterite + charcoal fire -> tin");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.CASSITERITE).is(Metallurgy.TIN_INGOT), "blast furnace too");
        var tag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/tin"));
        h.assertTrue(new ItemStack(Metallurgy.TIN_INGOT).is(tag), "tin ingot is c:ingots/tin");
        LOG.info("[LAB] tin [1-30]: cassiterite -> furnace -> tin ingot (c:ingots/tin)");
        h.succeed();
    }

    /** [1-05] 青銅（IMP2-01）：銅8＋スズ1 → 地金の調合9 →かまど/溶鉱炉→ 青銅（Sn 約11%）。c:ingots/bronze */
    @GameTest
    public void bronze(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("bronze_charge"));
        var ings = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value().placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Items.COPPER_INGOT))).count(), 8L, "8 copper");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Metallurgy.TIN_INGOT))).count(), 1L, "1 tin (~11% Sn)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.BRONZE_CHARGE).is(Metallurgy.BRONZE_INGOT), "melts in a furnace (~950 C)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.BRONZE_CHARGE).is(Metallurgy.BRONZE_INGOT), "blast furnace too");
        var tag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/bronze"));
        h.assertTrue(new ItemStack(Metallurgy.BRONZE_INGOT).is(tag), "bronze ingot is c:ingots/bronze");
        LOG.info("[LAB] bronze [1-05]: 8 copper + 1 tin -> 9 charge -> furnace -> bronze (Sn ~11%)");
        h.succeed();
    }

    /** [1-32] 銀（灰吹法、IMP2-01）：骨粉 → 骨灰 → 灰吹き皿、鉛8＋皿 → 調合 →溶鉱炉→ 銀の粒（かまどでは不可＝空気の吹きつけが要る）、粒9 ⇔ インゴット */
    @GameTest
    public void silverCupellation(GameTestHelper h) {
        var SM = net.minecraft.world.item.crafting.RecipeType.SMELTING; var BL = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        h.assertTrue(cook(h, SM, Items.BONE_MEAL).is(Metallurgy.BONE_ASH), "bone meal calcined -> bone ash");
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("cupel_charge"));
        var ings = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value().placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Metallurgy.LEAD_INGOT))).count(), 8L, "8 lead (argentiferous)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Metallurgy.CUPEL))).count(), 1L, "1 bone-ash cupel");
        h.assertTrue(cook(h, BL, Metallurgy.CUPEL_CHARGE).is(Metallurgy.SILVER_NUGGET), "cupellation with an air blast -> silver");
        h.assertTrue(cook(h, SM, Metallurgy.CUPEL_CHARGE).isEmpty(), "plain furnace: no air blast -> no cupellation");
        h.assertTrue(recipeUses(h, "silver_ingot", Metallurgy.SILVER_NUGGET) && recipeUses(h, "silver_nugget_from_ingot", Metallurgy.SILVER_INGOT), "9 nuggets <-> ingot");
        var tag = net.minecraft.tags.TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/silver"));
        h.assertTrue(new ItemStack(Metallurgy.SILVER_INGOT).is(tag), "c:ingots/silver");
        LOG.info("[LAB] silver [1-32]: bone ash cupel + 8 lead -> blast furnace -> silver nugget");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */

    /**
     * [1-06] 塊鉄炉：木炭の火だけでは約 800℃ で還元しない。横のふいごを踏みつづけると 1100℃ を超えてブルームができる。
     * ブルーム＋たき石 → 鉄インゴット（錬鉄）
     */
    @GameTest(maxTicks = 100)
    public void bloomery(GameTestHelper h) {
        var lv = h.getLevel();
        BlockPos pos = h.absolutePos(p(2, 1, 2)), bpos = h.absolutePos(p(3, 1, 2));
        h.setBlock(p(2, 1, 2), Metallurgy.BLOOMERY.block);
        var be = h.getBlockEntity(p(2, 1, 2), com.taku3.semiconductorlab.block.BloomeryBlockEntity.class);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BloomeryBlockEntity.SLOT_ORE, new ItemStack(Items.RAW_IRON, 2));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BloomeryBlockEntity.SLOT_FUEL, new ItemStack(Items.CHARCOAL, 16));
        for (int i = 0; i < 400; i++) com.taku3.semiconductorlab.block.BloomeryBlockEntity.tick(lv, pos, lv.getBlockState(pos), be);
        h.assertTrue(Math.abs(be.temperature() - com.taku3.semiconductorlab.block.BloomeryBlockEntity.T_DRAFT) < 10 && be.progress() == 0, "fire alone ~800 C, no reduction: " + be.temperature());
        h.setBlock(p(3, 1, 2), Heat.BELLOWS.block);
        var bel = h.getBlockEntity(p(3, 1, 2), com.taku3.semiconductorlab.block.BellowsBlockEntity.class);
        int t = 0;
        while (be.getContainer().getItem(com.taku3.semiconductorlab.block.BloomeryBlockEntity.SLOT_OUT).isEmpty() && t < 3000) {
            if (t % 15 == 0) bel.pump();   // 踏みつづける
            com.taku3.semiconductorlab.block.BellowsBlockEntity.tick(lv, bpos, lv.getBlockState(bpos), bel);
            com.taku3.semiconductorlab.block.BloomeryBlockEntity.tick(lv, pos, lv.getBlockState(pos), be);
            t++;
        }
        h.assertTrue(be.getContainer().getItem(com.taku3.semiconductorlab.block.BloomeryBlockEntity.SLOT_OUT).is(Metallurgy.IRON_BLOOM), "pumping the bellows -> bloom (after " + t + " ticks, " + be.temperature() + " C)");
        h.assertTrue(be.temperature() >= com.taku3.semiconductorlab.block.BloomeryBlockEntity.T_REDUCE, "above 1100 C while pumping");
        // [1-08] 鍛鉄：ブルームをかまどで赤く熱し直し、たき石で叩いてスラグを絞り出す → 鍛鉄（錬鉄＝鉄インゴット）。冷えたままでは叩けない
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.IRON_BLOOM).is(Metallurgy.HOT_IRON_BLOOM), "bloom -> furnace -> red-hot bloom");
        h.assertTrue(recipeUses(h, "iron_ingot_from_bloom", Metallurgy.HOT_IRON_BLOOM) && recipeUses(h, "iron_ingot_from_bloom", Survival.HAMMERSTONE), "red-hot bloom + hammerstone -> wrought iron");
        h.assertFalse(recipeUses(h, "iron_ingot_from_bloom", Metallurgy.IRON_BLOOM), "a cold bloom can't be forged");
        h.assertFalse(com.taku3.semiconductorlab.block.BloomeryBlockEntity.isFuel(new ItemStack(Items.COAL)), "coal is not used (sulfur makes iron brittle)");
        LOG.info("[LAB] bloomery: fire alone {} C; with bellows a bloom after {} ticks", com.taku3.semiconductorlab.block.BloomeryBlockEntity.T_DRAFT, t);
        h.succeed();
    }


    /**
     * [1-09] 高炉（最初のマルチブロック）：形が足りないと動かず「あと何か所」。れんが 3×3×4・羽口4つ・中は空気で組み上がる。
     * 羽口の外のふいごで風を送りつづけると 1400℃ を超え、鉄鉱石＋木炭＋石灰石 → 銑鉄＋スラグ。転炉は銑鉄だけを受け付ける（#46 を消した）
     */
    @GameTest(maxTicks = 100)
    public void blastFurnace(GameTestHelper h) {
        var lv = h.getLevel();
        BlockPos ctl = h.absolutePos(p(3, 1, 2));
        h.setBlock(p(3, 1, 2), Metallurgy.BLAST_FURNACE.block);
        var be = h.getBlockEntity(p(3, 1, 2), com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.class);
        var shape = com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.shape();
        var r = be.recheck();
        h.assertTrue(!r.formed() && r.missingCounts().get("tuyere") == 4, "nothing built -> not formed, 4 tuyeres missing: " + r.missingCounts());
        // 形どおりに積む（北向き）
        for (int y = 0; y < shape.height(); y++) for (int z = 0; z < shape.depth(); z++) for (int x = 0; x < shape.width(); x++) {
            char c = shape.layers().get(y)[z].charAt(x);
            if (c == 'B') lv.setBlockAndUpdate(shape.worldPos(ctl, Direction.NORTH, x, y, z), Blocks.BRICKS.defaultBlockState());
            if (c == 'T') lv.setBlockAndUpdate(shape.worldPos(ctl, Direction.NORTH, x, y, z), Metallurgy.TUYERE.defaultBlockState());
        }
        h.assertTrue(be.recheck().formed(), "built 3x3x4 -> formed: " + be.lastCheck().problems());
        // 正面の羽口の外にふいご
        BlockPos front = shape.worldPos(ctl, Direction.NORTH, 1, 1, 0), bpos = front.north();
        lv.setBlockAndUpdate(bpos, Heat.BELLOWS.block.defaultBlockState());
        var bel = (com.taku3.semiconductorlab.block.BellowsBlockEntity) lv.getBlockEntity(bpos);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_ORE, new ItemStack(Items.RAW_IRON, 4));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FUEL, new ItemStack(Items.CHARCOAL, 32));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FLUX, new ItemStack(Items.CALCITE, 2));
        int t = 0;
        while (be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_OUT).isEmpty() && t < 6000) {
            if (t % 15 == 0) bel.pump();
            com.taku3.semiconductorlab.block.BellowsBlockEntity.tick(lv, bpos, lv.getBlockState(bpos), bel);
            com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.tick(lv, ctl, lv.getBlockState(ctl), be);
            t++;
        }
        h.assertTrue(be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_OUT).is(Metallurgy.PIG_IRON), "blast -> pig iron after " + t + " ticks at " + be.temperature() + " C");
        h.assertTrue(be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_SLAG).is(Metallurgy.BLAST_FURNACE_SLAG) && be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FLUX).getCount() == 1, "limestone -> slag");
        h.assertTrue(be.temperature() >= com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.T_MELT, "above 1400 C");
        // 1か所こわすと止まる、中身は残る
        lv.setBlockAndUpdate(shape.worldPos(ctl, Direction.NORTH, 0, 3, 2), Blocks.AIR.defaultBlockState());
        var broken = be.recheck();
        h.assertTrue(!broken.formed() && broken.problems().size() == 1 && "lining".equals(broken.problems().get(0).expected()), "one brick missing -> not formed, 1 lining");
        h.assertTrue(!be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_ORE).isEmpty(), "contents stay");
        // [1-10] コークス高炉：コークスも燃料になる（木炭と同じように銑鉄が出る）。塊鉄炉は木炭だけ
        lv.setBlockAndUpdate(shape.worldPos(ctl, Direction.NORTH, 0, 3, 2), Blocks.BRICKS.defaultBlockState());
        be.recheck();
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FUEL, new ItemStack(Metallurgy.COKE, 16));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FLUX, new ItemStack(Items.CALCITE, 2));
        int before = be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_OUT).getCount(), t2 = 0;
        while (be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_OUT).getCount() == before && t2 < 3000) {
            if (t2 % 15 == 0) bel.pump();
            com.taku3.semiconductorlab.block.BellowsBlockEntity.tick(lv, bpos, lv.getBlockState(bpos), bel);
            com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.tick(lv, ctl, lv.getBlockState(ctl), be);
            t2++;
        }
        h.assertTrue(be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_OUT).getCount() > before
                && be.getContainer().getItem(com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SLOT_FUEL).getCount() < 16, "coke blast furnace [1-10]: coke burns and pig iron flows");
        h.assertFalse(com.taku3.semiconductorlab.block.BloomeryBlockEntity.isFuel(new ItemStack(Metallurgy.COKE)), "the bloomery stays charcoal-only");
        h.assertTrue(com.taku3.semiconductorlab.block.BessemerConverterBlockEntity.isPigIron(new ItemStack(Metallurgy.PIG_IRON))
                && !com.taku3.semiconductorlab.block.BessemerConverterBlockEntity.isPigIron(new ItemStack(Items.IRON_INGOT)), "converter takes pig iron only");
        LOG.info("[LAB] blast furnace: 3x3x4 formed; pig iron after {} ticks of blast; one brick missing -> stops", t);
        h.succeed();
    }

    /** [1-66] タンタル（IMP4-01、S-003 案A）：NaOH 4 → 金属ナトリウム、タンタル石＋HF 2＋KF → K2TaF7（容器が戻る）、K2TaF7＋Na → Ta。KF がないと動かない */
    @GameTest(maxTicks = 200)
    public void tantalum(GameTestHelper h) {
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), tp = p(4, 1, 4);
        h.setBlock(cp, Metallurgy.CASTNER_CELL.block);
        h.setBlock(tp, Metallurgy.TANTALUM_REFINERY.block);
        var cc = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.CastnerCellBlockEntity.class);
        var c = cc.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.SLOT_NAOH, new ItemStack(Gas.SODIUM_HYDROXIDE, 4));
        c.setItem(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.SLOT_FLASK, new ItemStack(Metallurgy.IRON_FLASK));
        tickConsumer(h, cp, cc, com.taku3.semiconductorlab.block.CastnerCellBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.WATTS));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.SLOT_OUT).is(Metallurgy.SODIUM) && c.getItem(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.SLOT_NAOH).isEmpty(), "4NaOH -> 4Na (in the iron flask)");
        var tr = h.getBlockEntity(tp, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.class);
        var t = tr.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.WATTS);
        t.setItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_A, new ItemStack(Metallurgy.TANTALITE));
        t.setItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_B, new ItemStack(Chemistry.HYDROFLUORIC_ACID_PE, 2));
        tickConsumer(h, tp, tr, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.TICKS + 5, feed);
        h.assertTrue(t.getItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_OUT).isEmpty(), "no KF: K2TaF7 does not crystallise");
        t.setItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_KF, new ItemStack(Chemistry.POTASSIUM_FLUORIDE));
        tickConsumer(h, tp, tr, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.TICKS + 1, feed);
        h.assertTrue(t.getItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_OUT).is(Metallurgy.POTASSIUM_FLUOROTANTALATE) && t.getItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_B).isEmpty(), "Marignac: tantalite + HF + KF -> K2TaF7");
        t.setItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_A, t.removeItemNoUpdate(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_OUT));
        t.setItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_B, c.removeItemNoUpdate(com.taku3.semiconductorlab.block.CastnerCellBlockEntity.SLOT_OUT));
        tickConsumer(h, tp, tr, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.TICKS + 1, feed);
        h.assertTrue(t.getItem(com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.SLOT_OUT).is(Metallurgy.TANTALUM), "K2TaF7 + 5Na -> Ta");
        var drops = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(tp)).inflate(2));
        h.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(Chemistry.POLYETHYLENE_BOTTLE)) && drops.stream().anyMatch(e -> e.getItem().is(Metallurgy.IRON_FLASK)), "PE bottles and the iron flask come back");
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] tantalum [1-66]: Castner 4NaOH -> Na; tantalite + 2 HF + KF -> K2TaF7 (Marignac); + Na -> Ta; containers back");
        h.succeed();
    }

    /** [1-44] ガリウム（IMP4-01、O-020）：バイヤー法の液を使い切ると使い終わった液が1つ、4つ＋PE 瓶を電解 → ガリウム＋NaOH */
    @GameTest(maxTicks = 400)
    public void gallium(GameTestHelper h) {
        floor(h, 7);
        BlockPos bp = p(1, 1, 1), gp = p(4, 1, 4);
        h.setBlock(bp, Metallurgy.BAYER_DIGESTER.block);
        var bd = h.getBlockEntity(bp, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.class);
        var c = bd.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_BAUXITE, new ItemStack(Metallurgy.BAUXITE, 2 * com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.RUNS_PER_LIQUOR));
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_NAOH, new ItemStack(Gas.SODIUM_HYDROXIDE));
        c.setItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_WATER, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.WATTS);
        tickConsumer(h, bp, bd, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS * (com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.RUNS_PER_LIQUOR - 1) + 3, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_SPENT).isEmpty(), "liquor still in use: no spent liquor yet");
        tickConsumer(h, bp, bd, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS + 1, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_SPENT).is(Metallurgy.SPENT_BAYER_LIQUOR) && c.getItem(com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.SLOT_SPENT).getCount() == 1, "liquor used up -> 1 spent liquor (gallium accumulated)");
        h.setBlock(gp, Metallurgy.GALLIUM_CELL.block);
        var gc = h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GalliumCellBlockEntity.class);
        var g = gc.getContainer();
        g.setItem(com.taku3.semiconductorlab.block.GalliumCellBlockEntity.SLOT_LIQUOR, new ItemStack(Metallurgy.SPENT_BAYER_LIQUOR, com.taku3.semiconductorlab.block.GalliumCellBlockEntity.LIQUOR_PER_RUN));
        g.setItem(com.taku3.semiconductorlab.block.GalliumCellBlockEntity.SLOT_BOTTLE, new ItemStack(Chemistry.POLYETHYLENE_BOTTLE));
        tickConsumer(h, gp, gc, com.taku3.semiconductorlab.block.GalliumCellBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.GalliumCellBlockEntity.WATTS));
        h.assertTrue(g.getItem(com.taku3.semiconductorlab.block.GalliumCellBlockEntity.SLOT_OUT).is(Metallurgy.GALLIUM) && g.getItem(com.taku3.semiconductorlab.block.GalliumCellBlockEntity.SLOT_NAOH).is(Gas.SODIUM_HYDROXIDE), "electrolysis -> gallium (PE bottle) + NaOH back");
        LOG.info("[LAB] gallium [1-44]: Bayer liquor used up -> spent liquor; 4 + PE bottle -> Ga + NaOH");
        h.succeed();
    }

    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Metallurgy.GALLIUM_CELL.block, "ガリウム", L(io(Metallurgy.SPENT_BAYER_LIQUOR, com.taku3.semiconductorlab.block.GalliumCellBlockEntity.LIQUOR_PER_RUN), io(Chemistry.POLYETHYLENE_BOTTLE, 1)),
            L(io(Metallurgy.GALLIUM, 1), io(Gas.SODIUM_HYDROXIDE, 1)), com.taku3.semiconductorlab.block.GalliumCellBlockEntity.WATTS, com.taku3.semiconductorlab.block.GalliumCellBlockEntity.TICKS / 20.0, "バイヤー法の使い終わった液を電解（液はバイヤー法の溶解釜が液を使い切るたびに1つ）");
        rec(a, Metallurgy.CASTNER_CELL.block, "金属ナトリウム", L(io(Gas.SODIUM_HYDROXIDE, com.taku3.semiconductorlab.block.CastnerCellBlockEntity.NAOH_PER_RUN), io(Metallurgy.IRON_FLASK, 1)),
            L(io(Metallurgy.SODIUM, 1)), com.taku3.semiconductorlab.block.CastnerCellBlockEntity.WATTS, com.taku3.semiconductorlab.block.CastnerCellBlockEntity.TICKS / 20.0, "4NaOH → 4Na + O2 + 2H2O（溶けた NaOH の電解、1886）");
        rec(a, Metallurgy.TANTALUM_REFINERY.block, "マリニャック法", L(io(Metallurgy.TANTALITE, 1), cond(io(Chemistry.HYDROFLUORIC_ACID_PE, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.HF_PER_RUN), "フッ酸（鉛かポリエチレンの容器）"), io(Chemistry.POTASSIUM_FLUORIDE, 1)),
            L(io(Metallurgy.POTASSIUM_FLUOROTANTALATE, 1)), com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.WATTS, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.TICKS / 20.0, "K2TaF7 は結晶に、ニオブは液に残る（1866）。容器は戻る");
        rec(a, Metallurgy.TANTALUM_REFINERY.block, "ナトリウム還元", L(io(Metallurgy.POTASSIUM_FLUOROTANTALATE, 1), io(Metallurgy.SODIUM, 1)),
            L(io(Metallurgy.TANTALUM, 1)), com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.WATTS, com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity.TICKS / 20.0, "K2TaF7 + 5Na → Ta + 2KF + 5NaF（1903）。鉄の容器は戻る");
        rec(a, Metallurgy.BLAST_FURNACE.block, "高炉で銑鉄", L(io(Items.RAW_IRON, 1), io(Items.CHARCOAL, (double) com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SMELT_TICKS / com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.FUEL_TICKS),
                perRuns(io(Items.CALCITE, 1), com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.FLUX_EVERY)),
            L(io(Metallurgy.PIG_IRON, 1), perRuns(io(Metallurgy.BLAST_FURNACE_SLAG, 1), com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.FLUX_EVERY)), 0,
            com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.SMELT_TICKS / 20.0, "マルチブロック 3×3×4。1400℃以上（羽口の外の水車ふいご）。電気なし");
        rec(a, Metallurgy.BLOOMERY.block, "鉄鉱石を還元（ブルーム）", L(io(Items.RAW_IRON, 1), io(Items.CHARCOAL, (double) com.taku3.semiconductorlab.block.BloomeryBlockEntity.REDUCE_TICKS / com.taku3.semiconductorlab.block.BloomeryBlockEntity.FUEL_TICKS)),
            L(io(Metallurgy.IRON_BLOOM, 1)), 0, com.taku3.semiconductorlab.block.BloomeryBlockEntity.REDUCE_TICKS / 20.0, "1100℃以上（ふいごで送風）。電気なし");
        rec(a, Metallurgy.HALL_HEROULT_CELL.block, "ホール・エルー法", L(io(Metallurgy.ALUMINA, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.ALUMINA_PER_RUN), perRuns(io(Metallurgy.CRYOLITE, 1), com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.RUNS_PER_CRYOLITE), perRuns(io(com.taku3.semiconductorlab.content.Power.GRAPHITE_ROD, 1), com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.RUNS_PER_ANODE)),
            L(io(Metallurgy.ALUMINIUM_INGOT, 1)), com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.WATTS, com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity.TICKS / 20.0, "約960℃の氷晶石にアルミナを溶かして電解。2Al2O3 + 3C → 4Al + 3CO2。15kWh/個");
        rec(a, Metallurgy.BAYER_DIGESTER.block, "バイヤー法", L(io(Metallurgy.BAUXITE, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.BAUXITE_PER_RUN), perRuns(io(com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE, 1), com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.RUNS_PER_LIQUOR), perRuns(io(Items.WATER_BUCKET, 1), com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.RUNS_PER_LIQUOR)),
            L(io(Metallurgy.ALUMINIUM_HYDROXIDE, 1), io(Metallurgy.RED_MUD, 1)), com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.WATTS, com.taku3.semiconductorlab.block.BayerDigesterBlockEntity.TICKS / 20.0, "Al(OH)3 + NaOH → NaAl(OH)4 → こして冷やすと Al(OH)3。鉄は赤泥に。液は使い回す");
        rec(a, Metallurgy.BESSEMER_CONVERTER.block, "ベッセマー転炉", L(io(Metallurgy.PIG_IRON, 1)), L(io(Metallurgy.STEEL_INGOT, 1)),
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

    /** [1-42] リチウム（IMP3、S-006）：リチア輝石 → 溶鉱炉 → β型 → ＋硫酸＋水 → 硫酸リチウム → ＋ソーダ灰 → 炭酸リチウム */
    @GameTest
    public void lithium(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.SPODUMENE).is(Metallurgy.BETA_SPODUMENE), "roast: alpha -> beta spodumene");
        h.assertTrue(recipeUses(h, "lithium_sulfate", Metallurgy.BETA_SPODUMENE) && recipeUses(h, "lithium_sulfate", Chemistry.SULFURIC_ACID), "beta + H2SO4 -> Li2SO4");
        h.assertTrue(recipeUses(h, "lithium_carbonate", Metallurgy.LITHIUM_SULFATE) && recipeUses(h, "lithium_carbonate", Chemistry.SODIUM_CARBONATE), "Li2SO4 + Na2CO3 -> Li2CO3");
        LOG.info("[LAB] lithium [1-42]: spodumene -> (blast) beta -> + H2SO4 -> Li2SO4 -> + Na2CO3 -> Li2CO3");
        h.succeed();
    }

    /** [1-70] ジルコニウム・ハフニウム（IMP3、S-007）：ジルコン（砂鉄の副産物）→ アルカリ融解 → フッ化物 → 分別結晶で Hf を分ける（Zr の塩が各マスに残る）→ HfO2、ナトリウム還元 → ヨウ化物法で結晶棒 */
    @GameTest
    public void zirconiumHafnium(GameTestHelper h) {
        h.assertTrue(Metallurgy.ZIRCON_FROM_IRON_SAND > 0 && Metallurgy.ZIRCON_FROM_IRON_SAND < Metallurgy.ILMENITE_FROM_IRON_SAND, "zircon: rarer by-product of iron sand");
        h.assertTrue(recipeUses(h, "crude_zirconia", Metallurgy.ZIRCON) && recipeUses(h, "crude_zirconia", com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE), "caustic fusion");
        h.assertTrue(recipeUses(h, "crude_fluorozirconate", Chemistry.HYDROFLUORIC_ACID) && recipeUses(h, "crude_fluorozirconate", Chemistry.POTASSIUM_FLUORIDE), "HF + KF -> fluoride double salt");
        h.assertTrue(recipeUses(h, "potassium_fluorohafnate", Metallurgy.CRUDE_FLUOROZIRCONATE), "fractional crystallization");
        var rem = Metallurgy.CRUDE_FLUOROZIRCONATE.getCraftingRemainder(new ItemStack(Metallurgy.CRUDE_FLUOROZIRCONATE));
        h.assertTrue(rem != null && rem.create().is(Metallurgy.POTASSIUM_FLUOROZIRCONATE), "pure K2ZrF6 stays behind in each slot");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.HAFNIUM_HYDROXIDE).is(Metallurgy.HAFNIUM_OXIDE), "Hf(OH)4 -> HfO2");
        h.assertTrue(recipeUses(h, "crude_hafnium", Metallurgy.SODIUM) && recipeUses(h, "hafnium_bar", Chemistry.IODINE) && recipeUses(h, "hafnium_bar", Metallurgy.TUNGSTEN_WIRE), "Na reduction, iodide process on W wire");
        h.assertTrue(recipeUses(h, "zirconium_bar", Metallurgy.CRUDE_ZIRCONIUM), "zirconium crystal bar");
        LOG.info("[LAB] zirconium/hafnium [1-70]: zircon -> NaOH fusion -> K2ZrF6(Hf) -> fractional crystallization -> K2HfF6 -> HfO2; Na reduction + iodide process -> crystal bars");
        h.succeed();
    }

    /** [1-19] 希土類 Y・Ce（IMP3、S-009）：バストネス石を焼いて塩酸 → CeO2。イオン吸着型の粘土 → 硫酸アンモニウムで浸出 → シュウ酸で沈殿 → 焼く → イオン交換 → Y2O3 */
    @GameTest
    public void rareEarthsYCe(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.BASTNAESITE).is(Metallurgy.ROASTED_BASTNAESITE), "roast bastnaesite");
        h.assertTrue(recipeUses(h, "cerium_oxide", Metallurgy.ROASTED_BASTNAESITE) && recipeUses(h, "cerium_oxide", com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID), "HCl leaves CeO2");
        h.assertTrue(recipeUses(h, "rare_earth_leachate", Metallurgy.ION_ADSORPTION_CLAY.asItem()) && recipeUses(h, "rare_earth_leachate", com.taku3.semiconductorlab.content.Gas.AMMONIUM_SULFATE), "ammonium sulfate leaching (ion exchange)");
        h.assertTrue(recipeUses(h, "oxalic_acid", net.minecraft.world.item.Items.SUGAR) && recipeUses(h, "rare_earth_oxalate", Metallurgy.OXALIC_ACID), "oxalic acid from sugar; oxalate precipitation");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.RARE_EARTH_OXALATE).is(Metallurgy.MIXED_RARE_EARTH_OXIDE), "calcine -> mixed oxide");
        h.assertTrue(recipeUses(h, "yttrium_oxide", Metallurgy.MIXED_RARE_EARTH_OXIDE) && recipeUses(h, "yttrium_oxide", Chemistry.CATION_EXCHANGE_RESIN), "ion exchange -> Y2O3");
        LOG.info("[LAB] rare earths [1-19]: bastnaesite -> roast -> HCl -> CeO2; ion-adsorption clay -> (NH4)2SO4 -> oxalate -> mixed oxide -> ion exchange -> Y2O3");
        h.succeed();
    }

    /** [1-72] 粉末冶金（IMP3、O-024）：磁鉄鉱＋H2 → 鉄の粉、粉4＋鋼の型（戻る）→ 圧粉体 → 電気炉で焼結 → 歯車 */
    @GameTest
    public void powderMetallurgy(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "iron_powder", Metallurgy.MAGNETITE) && recipeUses(h, "iron_powder", com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS), "sponge iron powder");
        h.assertTrue(recipeUses(h, "iron_compact", Metallurgy.IRON_POWDER) && recipeUses(h, "iron_compact", Metallurgy.STEEL_DIE), "press in the die");
        var rem = Metallurgy.STEEL_DIE.getCraftingRemainder(new ItemStack(Metallurgy.STEEL_DIE));
        h.assertTrue(rem != null && rem.create().is(Metallurgy.STEEL_DIE), "the die comes back");
        h.assertTrue(com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity.recipes().get(Metallurgy.IRON_COMPACT) == Metallurgy.SINTERED_GEAR, "electric furnace sinters the compact");
        h.succeed();
    }

    /** [1-35] ネオジム磁石（IMP3、O-024）：CeO2 の残りの液 → シュウ酸塩 → 酸化物 → イオン交換 → Nd2O3 → NdF3 → 溶融塩電解（電気炉）→ Nd。Nd＋鉄の粉＋フェロボロン → 粉 → 型と磁場で圧粉体 → 焼結 → 着磁 */
    @GameTest
    public void neodymiumMagnet(GameTestHelper h) {
        var rem = Metallurgy.ROASTED_BASTNAESITE.getCraftingRemainder(new ItemStack(Metallurgy.ROASTED_BASTNAESITE));
        h.assertTrue(rem != null && rem.create().is(Metallurgy.LIGHT_RARE_EARTH_LIQUOR), "La/Pr/Nd liquor stays after taking CeO2");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.LIGHT_RARE_EARTH_OXALATE).is(Metallurgy.LIGHT_RARE_EARTH_OXIDE), "calcine oxalate");
        h.assertTrue(recipeUses(h, "neodymium_oxide", Metallurgy.LIGHT_RARE_EARTH_OXIDE) && recipeUses(h, "neodymium_oxide", Chemistry.CATION_EXCHANGE_RESIN), "ion exchange -> Nd2O3");
        var ef = com.taku3.semiconductorlab.block.ElectricFurnaceBlockEntity.recipes();
        h.assertTrue(ef.get(Metallurgy.NEODYMIUM_ELECTROLYSIS_CHARGE) == Metallurgy.NEODYMIUM && ef.get(Metallurgy.NDFEB_COMPACT) == Metallurgy.SINTERED_NDFEB, "electric furnace: electrolysis (approx.) and sintering");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.FERROBORON_CHARGE).is(Metallurgy.FERROBORON), "ferroboron");
        h.assertTrue(recipeUses(h, "ndfeb_alloy", Metallurgy.NEODYMIUM) && recipeUses(h, "ndfeb_alloy", Metallurgy.IRON_POWDER) && recipeUses(h, "ndfeb_alloy", Metallurgy.FERROBORON), "Nd + Fe + B");
        h.assertTrue(recipeUses(h, "ndfeb_compact", Metallurgy.STEEL_DIE) && recipeUses(h, "ndfeb_compact", Metallurgy.MAGNETIZING_YOKE), "pressed in a field in the die");
        h.assertTrue(recipeUses(h, "neodymium_magnet", Metallurgy.SINTERED_NDFEB), "magnetize");
        var y = Metallurgy.MAGNETIZING_YOKE.getCraftingRemainder(new ItemStack(Metallurgy.MAGNETIZING_YOKE));
        h.assertTrue(y != null && y.create().is(Metallurgy.MAGNETIZING_YOKE), "the yoke comes back");
        LOG.info("[LAB] neodymium magnet [1-35]: bastnaesite liquor -> Nd2O3 -> NdF3 -> Nd; Nd2Fe14B -> H2 powder -> aligned compact -> sinter -> magnetize");
        h.succeed();
    }

    /** バナジウム（IMP3、O-026）：砂鉄の副産物 → ＋食塩 → 焙焼 → 水で浸出 → ＋NH4Cl → NH4VO3 → 焼く → V2O5（触媒、戻る） */
    @GameTest
    public void vanadium(GameTestHelper h) {
        h.assertTrue(Metallurgy.VANADIUM_MAGNETITE_FROM_IRON_SAND > 0 && Metallurgy.VANADIUM_MAGNETITE_FROM_IRON_SAND < 0.5f, "by-product of iron sand");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.VANADIUM_ROAST_CHARGE).is(Metallurgy.VANADIUM_CALCINE), "salt roast");
        h.assertTrue(recipeUses(h, "ammonium_metavanadate", Chemistry.AMMONIUM_CHLORIDE), "precipitate with NH4Cl");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.AMMONIUM_METAVANADATE).is(Metallurgy.VANADIUM_PENTOXIDE), "calcine -> V2O5");
        var rem = Metallurgy.VANADIUM_PENTOXIDE.getCraftingRemainder(new ItemStack(Metallurgy.VANADIUM_PENTOXIDE));
        h.assertTrue(rem != null && rem.create().is(Metallurgy.VANADIUM_PENTOXIDE), "V2O5 is a catalyst (returned)");
        h.succeed();
    }
}
