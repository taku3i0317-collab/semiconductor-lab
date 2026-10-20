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
 * GameTest：幹0 生存・道具（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class SurvivalTests extends LabTestBase {
    // =====================================================================
    // [0-10] 生石灰・[0-11] 消石灰
    // =====================================================================

    /** 方解石をかまど/溶鉱炉で焼くと生石灰（CaCO3 → CaO + CO2）。生石灰＋水 → 消石灰。石や凝灰岩は焼いても石灰にならない */
    @GameTest
    public void limeFromCalcite(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        h.assertTrue(cook(h, SMELT, Items.CALCITE).is(Survival.QUICKLIME), "furnace: calcite -> quicklime");
        h.assertTrue(cook(h, BLAST, Items.CALCITE).is(Survival.QUICKLIME), "blast furnace: calcite -> quicklime");
        h.assertFalse(cook(h, SMELT, Items.TUFF).is(Survival.QUICKLIME), "tuff is not CaCO3");
        h.assertFalse(cook(h, SMELT, Items.STONE).is(Survival.QUICKLIME), "stone is not CaCO3");
        h.assertTrue(recipeUses(h, "slaked_lime", Survival.QUICKLIME) && recipeUses(h, "slaked_lime", Items.WATER_BUCKET), "CaO + H2O -> Ca(OH)2");
        LOG.info("[LAB] lime: calcite -> quicklime (furnace / blast), quicklime + water -> slaked lime -> brick binder");
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

    /** [0-16] 塩田：海・浜バイオーム＋横に水＋空＋晴れで、1分に塩1、塩8ごとににがり1。右クリックで受け取る */
    @GameTest
    public void saltPan(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 4);
        BlockPos pos = p(1, 1, 1), water = p(2, 1, 1);
        h.setBlock(pos, Survival.SALT_PAN.block);
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
        h.assertValueEqual(player.getInventory().countItem(Gas.SALT), 8, "salt in inventory");
        h.assertValueEqual(player.getInventory().countItem(Survival.BITTERN), 1, "bittern in inventory");
        h.assertValueEqual(be.getSalt(), 0, "pan emptied");
        // 満杯で止まる（塩64）
        be.step(lv, a, T * 80);
        h.assertValueEqual(be.getSalt(), com.taku3.semiconductorlab.block.SaltPanBlockEntity.MAX_SALT, "stops at 64 salt");
        h.assertTrue(recipeUses(h, "salt_pan", Items.CAULDRON), "salt pan needs an iron pot (cauldron) for boiling down");
        LOG.info("[LAB] salt pan: sea + water + sky + no rain -> 1 salt / {} ticks, 1 bittern / 8 salt, stops at 64", T);
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Survival.SALT_PAN.block, "天日で塩", L(kept(cond(io("海辺の水と日光", 1), "雨の日は止まる"))), L(io(Gas.SALT, 1), perRuns(io(Survival.BITTERN, 1), SaltPanBlockEntity.SALT_PER_BITTERN)),
            0, SaltPanBlockEntity.TICKS_PER_SALT / 20.0, "電気なし");
    }
}
