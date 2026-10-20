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
 * GameTest：枝🌿（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class BranchesTests extends LabTestBase {
    // =====================================================================
    // [B-12] 太陽電池（IMP-25、v0.2b 第2章のゴール）
    // =====================================================================

    /** p-n 接合の板8＋銅ケーブル → 太陽電池。昼・空で 60W（1000W/m²×6%）、雨は約2割の 12W。夜・屋根の下は 0。テスト世界の昼夜・天気に合わせて確かめる */
    @GameTest
    public void solarCell(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "solar_cell", Semiconductor.PN_JUNCTION_WAFER) && recipeUses(h, "solar_cell", Power.COPPER_CABLE.block.asItem()), "solar cell = p-n slices + copper electrodes");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS - 60) < 1e-9, "1000 W/m2 x 6% x 1 m2 = 60 W");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS * 0.836 - 50) < 1.0, "matches 1954 'about 50 W per square yard'");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, true, false), com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS, "sunny day, open sky -> 60 W");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, false, false), 0.0, "night -> 0");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(false, true, false), 0.0, "under a roof -> 0");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, true, true) - 12) < 1e-9, "rain (overcast) -> diffuse light, about 20% = 12 W");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, false, true), 0.0, "rainy night -> 0");
        BlockPos fp = p(1, 1, 1), roofed = p(3, 1, 1);
        h.setBlock(fp, Branches.SOLAR_CELL.block);
        h.setBlock(roofed, Branches.SOLAR_CELL.block);
        h.setBlock(roofed.above(2), Blocks.STONE);
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(fp), ar = h.absolutePos(roofed);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.energy.SolarCellBlockEntity.class);
        var ber = h.getBlockEntity(roofed, com.taku3.semiconductorlab.energy.SolarCellBlockEntity.class);
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, ar, lv.getBlockState(ar), ber);
        double expect = com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(com.taku3.semiconductorlab.block.SaltPanBlockEntity.openSky(lv, a), lv.isBrightOutside(), lv.isRainingAt(a.above()));
        h.assertValueEqual(be.getLastWatts(), expect, "output follows the world's day/night and weather");
        h.assertValueEqual(ber.getLastWatts(), 0.0, "stone roof -> 0 W");
        LOG.info("[LAB] solar cell [B-12]: {} W (bright={}, rain={}); roofed 0 W", be.getLastWatts(), lv.isBrightOutside(), lv.isRainingAt(a.above()));
        h.succeed();
    }
}
