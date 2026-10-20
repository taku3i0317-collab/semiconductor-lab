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
 * GameTest：幹7 光学（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class OpticsTests extends LabTestBase {
    @GameTest
    public void oxyhydrogenFusedQuartz(GameTestHelper h) {
        int T = OxyhydrogenBurnerBlockEntity.MELT_TICKS;
        int IN = OxyhydrogenBurnerBlockEntity.SLOT_IN, H2 = OxyhydrogenBurnerBlockEntity.SLOT_H2, O2 = OxyhydrogenBurnerBlockEntity.SLOT_O2;
        int OUT = OxyhydrogenBurnerBlockEntity.SLOT_OUT, EMPTY = OxyhydrogenBurnerBlockEntity.SLOT_EMPTY;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Optics.OXYHYDROGEN_BURNER.block);
        OxyhydrogenBurnerBlockEntity be = h.getBlockEntity(pos, OxyhydrogenBurnerBlockEntity.class);
        var c = be.getContainer();

        // かまどではもう溶けない（仮レシピ #19 は削除）
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.VEIN_QUARTZ).isEmpty(), "quartz must not melt in a furnace (~1000 C < 1713 C)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Metallurgy.VEIN_QUARTZ).isEmpty(), "nor in a blast furnace");
        // タスク13：入力は珪石 [1-46]。ネザークォーツ（現実に無い素材）は受け付けない
        h.assertTrue(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(Metallurgy.VEIN_QUARTZ)), "the burner melts vein quartz");
        h.assertFalse(OxyhydrogenBurnerBlockEntity.isInput(new ItemStack(Items.QUARTZ)), "nether quartz is not accepted (chemistry rule 1)");

        // ボンベなし：動かない
        c.setItem(IN, new ItemStack(Metallurgy.VEIN_QUARTZ, 30));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no cylinders -> no melting");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 水素だけ：動かない（酸素がないと燃えない）。水素ボンベはつながない（無駄に開けない）
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "hydrogen only -> no flame");
        // 酸素だけ：動かない
        c.setItem(H2, ItemStack.EMPTY);
        c.setItem(O2, new ItemStack(Gas.OXYGEN_CYLINDER, 1));
        tickBurner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "oxygen only -> no flame");

        // 両方：10秒で溶融石英1個、空ボンベ2本が出る
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_CYLINDER, 2));
        tickBurner(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickBurner(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(Optics.FUSED_QUARTZ), "quartz -> fused quartz");
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
        h.assertTrue(recipeUses(h, "quartz_tube", Optics.FUSED_QUARTZ), "fused quartz -> quartz tube");
        LOG.info("[LAB] oxyhydrogen burner: no gas -> 0, H2+O2 -> fused quartz in {} ticks, 12.5 per H2 cylinder", T);
        h.succeed();
    }

    /** [7-01] 光学ガラス（IMP2-01）：フリント＝砂5＋PbO1＋ソーダ1 →調合原料5 →溶鉱炉→ フリント。クラウン＝ソーダ石灰ガラス →溶鉱炉（かき混ぜ）→ クラウン */
    @GameTest
    public void opticalGlass(GameTestHelper h) {
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("flint_glass_batch"));
        var r = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ings = r.placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Items.SAND))).count(), 5L, "5 sand (SiO2 ~51%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Metallurgy.LITHARGE))).count(), 1L, "1 litharge (PbO ~38%)");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Chemistry.SODIUM_CARBONATE))).count(), 1L, "1 soda (Na2O ~11%)");
        h.assertValueEqual(ings.size(), 7, "nothing else in the batch");
        // 溶鉱炉（長く溶かしてかき混ぜる）でだけ光学ガラスになる。かまどでは泡と脈理が残る＝作れない
        var BL = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SM = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        h.assertTrue(cook(h, BL, Optics.FLINT_GLASS_BATCH).is(Optics.FLINT_GLASS), "flint batch -> blast furnace -> flint glass");
        h.assertTrue(cook(h, BL, Chemistry.SODA_LIME_GLASS).is(Optics.CROWN_GLASS), "soda-lime glass -> blast furnace (stirred) -> crown glass");
        h.assertTrue(cook(h, SM, Optics.FLINT_GLASS_BATCH).isEmpty(), "no optical flint from a plain furnace");
        h.assertTrue(cook(h, SM, Chemistry.SODA_LIME_GLASS).isEmpty(), "no optical crown from a plain furnace");
        // バニラのガラスは光学ガラスにならない（化学ルール1：近似のガラスから光学ガラスを作らない）
        h.assertTrue(cook(h, BL, Items.GLASS).isEmpty(), "vanilla glass is not optical glass");
        for (String id : new String[] {"flint_glass", "crown_glass"}) {
            var rk = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(id));
            h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(rk).isPresent(), "recipe " + id);
        }
        LOG.info("[LAB] optical glass: 5 sand + PbO + soda -> 5 flint batch -> blast -> flint; soda-lime glass -> blast -> crown");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Optics.OXYHYDROGEN_BURNER.block, "石英を溶かす", L(io(Metallurgy.VEIN_QUARTZ, 1), mol(Gas.HYDROGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.H2_PER), mol(Gas.OXYGEN_CYLINDER, OxyhydrogenBurnerBlockEntity.O2_PER)),
            L(io(Optics.FUSED_QUARTZ, 1)), 0, OxyhydrogenBurnerBlockEntity.MELT_TICKS / 20.0, "2H2 + O2 → 2H2O（約2800℃）。電気なし");
    }
}
