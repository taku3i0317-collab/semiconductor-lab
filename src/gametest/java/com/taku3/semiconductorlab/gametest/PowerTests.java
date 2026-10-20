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
 * GameTest：幹4 動力・電気（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class PowerTests extends LabTestBase {
    // =====================================================================
    // 発電
    // =====================================================================

    /** 水車：隣に流れている水があれば発電（>0）。実際に水を流して確かめる */
    @GameTest(maxTicks = 100)
    public void waterWheelFlowing(GameTestHelper h) {
        floor(h, 8);
        h.setBlock(p(3, 1, 3), Power.WATER_WHEEL.block);
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
        h.setBlock(p(3, 1, 3), Power.WATER_WHEEL.block);
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
        h.setBlock(pos, Power.STEAM_ENGINE.block);
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
        h.setBlock(pos, Power.STEAM_ENGINE.block);
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
        h.setBlock(p(3, 1, 1), Power.WATER_WHEEL.block);
        WaterWheelBlockEntity ww = h.getBlockEntity(p(3, 1, 1), WaterWheelBlockEntity.class);
        h.assertTrue(ww.installSelfExcitedDynamo() && ww.dynamoFactor() == 2, "water wheel accepts the self-excited dynamo");
        h.assertTrue(recipeUses(h, "self_excited_dynamo", Metallurgy.STEEL_INGOT) && !recipeUses(h, "self_excited_dynamo", Power.MAGNET), "steel core, no magnet");
        LOG.info("[LAB] self-excited dynamo: steam engine 100 kW, coal 15 s (1.5 MJ/coal unchanged), water wheel x2, no magnet in recipe");
        h.succeed();
    }

    /** [4-06] 蒸気タービン：500kW・石炭18秒（熱効率30%）。水はバケツ（1杯＝石炭2個）か、横の水源から取り続ける。積み替えはない */
    @GameTest
    public void steamTurbine(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Power.STEAM_TURBINE.block);
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
        h.assertTrue(recipeUses(h, "steam_turbine", Metallurgy.STEEL_INGOT), "turbine is built of steel");
        LOG.info("[LAB] steam turbine: 500 kW, 18 s/coal (30%), bucket = 2 coal, adjacent water source = unlimited feed");
        h.succeed();
    }

    /** [4-06] のテスト「アチソン炉が動く」：タービン → 銅ケーブル1本 → アチソン炉 で 2500℃に届き、人造黒鉛ができる */
    @GameTest
    public void turbinePowersAcheson(GameTestHelper h) {
        BlockPos tp = p(1, 1, 1), cp = p(2, 1, 1), fp = p(3, 1, 1);
        h.setBlock(tp, Power.STEAM_TURBINE.block);
        h.setBlock(cp, Power.COPPER_CABLE.block);
        h.setBlock(fp, Heat.ACHESON_FURNACE.block);
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
        h.assertTrue(f.getContainer().getItem(AchesonFurnaceBlockEntity.SLOT_OUT).is(Heat.SYNTHETIC_GRAPHITE), "synthetic graphite made on turbine power");
        LOG.info("[LAB] turbine -> cable -> acheson: {} W delivered, 2500 C at tick {}, graphite at tick {}, coal left {}", Math.round(maxW), reached, ticks,
                t.getContainer().getItem(SteamEngineBlockEntity.SLOT_COAL).getCount());
        h.succeed();
    }

    /** [4-07] 鉛蓄電池（タスク06 で全面充放電）：どの面からも充電（85% が貯まる、10kW まで）、1MJ で満杯、隣の機械へ直接放電（10kW まで） */
    @GameTest
    public void leadAcidBattery(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Power.LEAD_ACID_BATTERY.block);
        LeadAcidBatteryBlockEntity be = h.getBlockEntity(pos, LeadAcidBatteryBlockEntity.class);
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(pos);
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
            EnergyStorage s = EnergyApi.ENERGY.find(lv, a, d);
            h.assertTrue(s != null && s.canInsert() && s.canExtract(), "every face charges and discharges: " + d);
        }
        EnergyStorage side = EnergyApi.ENERGY.find(lv, a, DIR_E);
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
        h.setBlock(p(2, 1, 1), Power.ELECTRIC_FURNACE.block);
        ElectricFurnaceBlockEntity furnace = h.getBlockEntity(p(2, 1, 1), ElectricFurnaceBlockEntity.class);
        for (int i = 0; i < 20; i++) LeadAcidBatteryBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        h.assertValueEqual(full - be.getStoredJ(), 20 * per, "discharged 20 ticks x 500 J");
        h.assertValueEqual(furnace.getStorage().getStoredJ(), 20 * (per - per / 100), "furnace received it (1% contact loss)");
        h.assertValueEqual(be.getLastDischargeWatts(), LeadAcidBatteryBlockEntity.MAX_WATTS, "discharging at 10 kW");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        h.assertTrue(recipeUses(h, "lead_acid_battery", Metallurgy.LEAD_INGOT) && recipeUses(h, "lead_acid_battery", Chemistry.SULFURIC_ACID), "lead + sulfuric acid");
        LOG.info("[LAB] lead-acid battery: charge 10 kW at 85% from any face, full at {} J, discharge 10 kW to an adjacent machine", full);
        h.succeed();
    }

    /** タスク06：壊すと残量がアイテム（データコンポーネント battery_energy）に残り、置き直すと戻る */
    @GameTest
    public void batteryKeepsCharge(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        h.setBlock(p(1, 1, 1), Power.LEAD_ACID_BATTERY.block);
        LeadAcidBatteryBlockEntity be = h.getBlockEntity(p(1, 1, 1), LeadAcidBatteryBlockEntity.class);
        be.setStoredForTest(400_000);
        BlockPos a = h.absolutePos(p(1, 1, 1));
        List<ItemStack> drops = Block.getDrops(lv.getBlockState(a), lv, a, be, null, new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(drops.size() == 1 && drops.get(0).is(Power.LEAD_ACID_BATTERY.block.asItem()), "battery drops itself, got " + drops);
        h.assertValueEqual(drops.get(0).get(ModDataComponents.BATTERY_ENERGY), 400_000L, "charge stored on the item");
        h.setBlock(p(3, 1, 1), Power.LEAD_ACID_BATTERY.block);
        LeadAcidBatteryBlockEntity placed = h.getBlockEntity(p(3, 1, 1), LeadAcidBatteryBlockEntity.class);
        placed.applyComponentsFromItemStack(drops.get(0));
        h.assertValueEqual(placed.getStoredJ(), 400_000L, "charge restored when placed again");
        // 空の電池はコンポーネントなし（ふつうの電池とスタックできる）
        h.setBlock(p(5, 1, 1), Power.LEAD_ACID_BATTERY.block);
        BlockPos e = h.absolutePos(p(5, 1, 1));
        List<ItemStack> empty = Block.getDrops(lv.getBlockState(e), lv, e, lv.getBlockEntity(e), null, new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(empty.get(0).get(ModDataComponents.BATTERY_ENERGY) == null, "empty battery has no charge component");
        LOG.info("[LAB] battery keeps charge: 400 kJ -> item component -> placed again 400 kJ");
        h.succeed();
    }

    /** 蒸気機関→ケーブル3本→電気炉：本当に tick を進めて、電気炉に電気が届く */
    @GameTest(maxTicks = 60)
    public void cableChainDelivers(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), Power.STEAM_ENGINE.block);
        for (int x = 2; x <= 4; x++) h.setBlock(p(x, 1, 1), Power.COPPER_CABLE.block);
        h.setBlock(p(5, 1, 1), Power.ELECTRIC_FURNACE.block);
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
            h.setBlock(p(1, 1, z), Power.LEAD_ACID_BATTERY.block);
            h.getBlockEntity(p(1, 1, z), LeadAcidBatteryBlockEntity.class).setStoredForTest(100_000);
            for (int x = 2; x <= 1 + n; x++) h.setBlock(p(x, 1, z), Power.COPPER_CABLE.block);
            h.setBlock(p(2 + n, 1, z), Power.ELECTRIC_FURNACE.block);
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
        h.setBlock(p(1, 1, 1), Power.STEAM_TURBINE.block);
        h.setBlock(p(1, 1, 0), Blocks.WATER);
        h.setBlock(p(2, 1, 2), Power.STEAM_TURBINE.block);
        h.setBlock(p(2, 1, 3), Blocks.WATER);
        for (int x = 2; x <= 11; x++) h.setBlock(p(x, 1, 1), Power.COPPER_CABLE.block);
        h.setBlock(p(12, 1, 1), Heat.ACHESON_FURNACE.block);
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
            h.setBlock(p(x, 1, 1), Power.STEAM_TURBINE.block);
            h.getBlockEntity(p(x, 1, 1), SteamTurbineBlockEntity.class).getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 64));
            h.setBlock(p(x, 1, 2), Power.COPPER_CABLE.block);
            h.setBlock(p(x, 1, 3), Heat.ACHESON_FURNACE.block);
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

    // =====================================================================
    // v0.4 電気の土台（S-007）：交流／直流・電圧の段・変圧器・整流器
    // =====================================================================

    /** 東向き（テストの中の変数 net と名前がぶつからないように） */
    private static final net.minecraft.core.Direction DIR_E = net.minecraft.core.Direction.EAST;

    private static void turbine(GameTestHelper h, BlockPos t, boolean ac) {
        h.setBlock(t.north(), Blocks.WATER);
        h.setBlock(t, Power.STEAM_TURBINE.block);
        var be = h.getBlockEntity(t, SteamTurbineBlockEntity.class);
        be.getContainer().setItem(SteamEngineBlockEntity.SLOT_COAL, new ItemStack(Items.COAL, 64));
        if (ac) h.assertTrue(be.installAlternator(), "alternator on the turbine");
    }

    private static void facing(GameTestHelper h, BlockPos p, net.minecraft.world.level.block.Block b, net.minecraft.core.Direction d) {
        h.setBlock(p, b.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, d));
    }

    /**
     * ①なぜ交流か・③なぜ整流器か：交流の網では電熱の機械は動くが、電解槽（直流だけ）はもらえない。整流器を通すと直流の網になり、電解槽に届く。
     * 直流の網では変圧器は働かない（入力が交流だけ）。交流と直流の発電機が混ざった網は配らない
     */
    @GameTest(maxTicks = 300)
    public void acDcAndRectifier(GameTestHelper h) {
        turbine(h, p(1, 1, 2), true);
        for (int x = 2; x <= 4; x++) h.setBlock(p(x, 1, 2), Power.COPPER_CABLE.block);
        h.setBlock(p(3, 1, 3), Power.ELECTRIC_FURNACE.block);
        h.setBlock(p(4, 1, 3), Gas.ELECTROLYZER.block);
        BlockPos[] order = concat(new BlockPos[] {p(1, 1, 2)}, line(2, 4, 1, 2), new BlockPos[] {p(3, 1, 3), p(4, 1, 3)});
        tickAll(h, 5, order);
        CableNetwork net = CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 2)));
        h.assertValueEqual(net.current, com.taku3.semiconductorlab.energy.Current.AC, "a turbine with an alternator makes an AC network");
        h.assertValueEqual(net.mismatched, 1, "the electrolyser (DC only) cannot take AC");
        h.assertValueEqual(net.sinkCount(), 1, "the electric furnace (heating) takes AC");
        // 整流器：交流の網 → 整流器 → 直流のケーブル → 電解槽
        h.setBlock(p(4, 1, 3), Blocks.AIR);
        facing(h, p(5, 1, 2), Power.RECTIFIER.block, DIR_E);   // 後ろ（西）が交流の網、前（東）へ直流
        h.setBlock(p(6, 1, 2), Power.COPPER_CABLE.block);
        h.setBlock(p(6, 1, 3), Gas.ELECTROLYZER.block);
        BlockPos[] order2 = concat(new BlockPos[] {p(1, 1, 2)}, line(2, 4, 1, 2), new BlockPos[] {p(5, 1, 2), p(6, 1, 2), p(3, 1, 3), p(6, 1, 3)});
        tickAll(h, 20, order2);
        CableNetwork dc = CableNetwork.get(h.getLevel(), h.absolutePos(p(6, 1, 2)));
        h.assertValueEqual(dc.current, com.taku3.semiconductorlab.energy.Current.DC, "the rectifier output makes a DC network");
        h.assertTrue(dc.sinkCount() == 1 && dc.mismatched == 0, "the electrolyser is on the DC side");
        var el = h.getBlockEntity(p(6, 1, 3), com.taku3.semiconductorlab.energy.ConsumerBlockEntity.class);
        h.assertTrue(el.getStorage().getStoredJ() > 0, "DC from the rectifier reaches the electrolyser");
        h.assertTrue(h.getBlockEntity(p(5, 1, 2), com.taku3.semiconductorlab.energy.PowerConverterBlockEntity.class).getLastWatts() > 0, "power passes the rectifier");
        // 直流の網に変圧器：働かない（入力は交流だけ）
        turbine(h, p(1, 1, 6), false);
        for (int x = 2; x <= 3; x++) h.setBlock(p(x, 1, 6), Power.COPPER_CABLE.block);
        facing(h, p(4, 1, 6), Power.TRANSFORMER.block, DIR_E);
        tickAll(h, 5, concat(new BlockPos[] {p(1, 1, 6)}, line(2, 3, 1, 6), new BlockPos[] {p(4, 1, 6)}));
        CableNetwork dcNet = CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 6)));
        h.assertTrue(dcNet.current == com.taku3.semiconductorlab.energy.Current.DC && dcNet.mismatched == 1, "a transformer cannot work on DC (no changing flux)");
        // 交流と直流の発電機が混ざると流れない
        turbine(h, p(2, 2, 6), true);
        tickAll(h, 3, concat(new BlockPos[] {p(1, 1, 6), p(2, 2, 6)}, line(2, 3, 1, 6)));
        h.assertTrue(CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 6))).mixed, "AC and DC generators on one network -> nothing flows");
        LOG.info("[LAB] AC/DC: AC network runs the furnace but not the electrolyser; rectifier -> DC reaches it; transformer idle on DC; mixed AC+DC stops");
        h.succeed();
    }

    /**
     * ②なぜ高い電圧で送るか：同じ 7本でも、銅ケーブル（低圧）は 7% 失い、高圧の電線はその 1/100（0.07%）。変圧器で上げて下げると届く。
     * 高圧の電線と銅ケーブルはつながらない、機械は高圧の網に直接は入らない
     */
    @GameTest(maxTicks = 400)
    public void highVoltageTransmission(GameTestHelper h) {
        turbine(h, p(1, 1, 2), true);
        h.setBlock(p(2, 1, 2), Power.COPPER_CABLE.block);
        facing(h, p(3, 1, 2), Power.TRANSFORMER.block, DIR_E);       // 昇圧：後ろ＝低圧、前＝高圧
        for (int x = 4; x <= 10; x++) h.setBlock(p(x, 1, 2), Power.HIGH_VOLTAGE_LINE.block);
        facing(h, p(11, 1, 2), Power.TRANSFORMER.block, DIR_E);      // 降圧：後ろ＝高圧、前＝低圧
        h.setBlock(p(12, 1, 2), Power.COPPER_CABLE.block);
        h.setBlock(p(12, 1, 3), Power.ELECTRIC_FURNACE.block);
        h.setBlock(p(7, 1, 3), Power.ELECTRIC_FURNACE.block);   // 高圧の電線の横の機械：つながらない
        BlockPos[] order = concat(new BlockPos[] {p(1, 1, 2), p(2, 1, 2), p(3, 1, 2)}, line(4, 10, 1, 2), new BlockPos[] {p(11, 1, 2), p(12, 1, 2), p(12, 1, 3), p(7, 1, 3)});
        tickAll(h, 40, order);
        CableNetwork hv = CableNetwork.get(h.getLevel(), h.absolutePos(p(7, 1, 2)));
        h.assertValueEqual(hv.voltageClass, 1, "the line is a high-voltage network");
        h.assertValueEqual(hv.cables.size(), 7, "the copper cables are not part of the HV network");
        h.assertTrue(hv.sourceCount() == 1 && hv.sinkCount() == 1, "only the two transformers are on the HV network (the furnace beside it is not)");
        h.assertTrue(Math.abs(hv.lossFraction() - 0.0007) < 1e-9, "7 HV lines lose 0.07% (copper would lose 7%), got " + hv.lossFraction());
        h.assertTrue(hv.maxTransfer() == CableNetwork.MAX_TRANSFER * 10, "10x voltage carries 10x power");
        var far = h.getBlockEntity(p(12, 1, 3), com.taku3.semiconductorlab.energy.ConsumerBlockEntity.class);
        h.assertTrue(far.getStorage().getStoredJ() > 0, "power reaches the furnace 7 lines away through two transformers");
        // 比べ：同じ 20本を銅ケーブルにすると 20% 失う
        for (int x = 4; x <= 10; x++) h.setBlock(p(x, 1, 5), Power.COPPER_CABLE.block);
        CableNetwork lv = CableNetwork.get(h.getLevel(), h.absolutePos(p(4, 1, 5)));
        h.assertValueEqual(lv.lossPercent(), 7, "7 copper cables lose 7%");
        LOG.info("[LAB] HV: 7 lines lose {}% vs copper 7%; 2 transformers (98% each) carry AC to a furnace far away", String.format("%.2f", hv.lossFraction() * 100));
        h.succeed();
    }

    /** タスク06：電池はネットワークで「余れば充電・足りなければ放電」。電池どうしでは流れない */
    @GameTest
    public void batteryOnNetwork(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), Power.STEAM_ENGINE.block);
        h.setBlock(p(2, 1, 1), Power.COPPER_CABLE.block);
        h.setBlock(p(3, 1, 1), Power.COPPER_CABLE.block);
        h.setBlock(p(3, 1, 2), Power.LEAD_ACID_BATTERY.block);
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
        h.setBlock(p(3, 1, 0), Power.ELECTRIC_FURNACE.block);
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
        h.setBlock(p(2, 1, 2), Power.LEAD_ACID_BATTERY.block);
        LeadAcidBatteryBlockEntity other = h.getBlockEntity(p(2, 1, 2), LeadAcidBatteryBlockEntity.class);
        tickAll(h, 20, order);
        h.assertValueEqual(other.getStoredJ(), 0L, "no battery-to-battery flow");
        LOG.info("[LAB] battery on network: charges from spare power, discharges when the generator is gone (490 J/tick over 2 cables), no battery-to-battery flow");
        h.succeed();
    }

    // =====================================================================
    // [4-09] 高周波発振器（第1章の仮①「高周波電源」を置換）
    // =====================================================================

    /** 発振器＝真空管2＋コイル＋ライデン瓶＋ケーブル。帯域精製炉は発振器を要求し、旧・仮アイテムは消えている */
    @GameTest
    public void hfOscillatorReplacesPlaceholder(GameTestHelper h) {
        h.assertValueEqual(assemblyCount(Power.HF_OSCILLATOR, Vacuum.VACUUM_TUBE), 2, "vacuum tubes in the oscillator (assembly bench, IMP-33)");
        for (var it : new net.minecraft.world.item.Item[] {Power.LEYDEN_JAR, Power.INDUCTION_COIL, Power.COPPER_CABLE.block.asItem()})
            h.assertTrue(assemblyCount(Power.HF_OSCILLATOR, it) > 0, "oscillator should use " + it);
        h.assertTrue(recipeUses(h, "leyden_jar", Chemistry.SODA_LIME_GLASS), "Leyden jar = glass jar (soda-lime glass) + metal foil");
        h.assertTrue(recipeUses(h, "zone_refining_furnace", Power.HF_OSCILLATOR), "zone refining furnace needs the HF oscillator");
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
        h.setBlock(pos, Power.ELECTRIC_FURNACE.block);
        ElectricFurnaceBlockEntity be = h.getBlockEntity(pos, ElectricFurnaceBlockEntity.class);
        var c = be.getContainer();
        int IN = ElectricFurnaceBlockEntity.SLOT_IN, OUT = ElectricFurnaceBlockEntity.SLOT_OUT, T = ElectricFurnaceBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(ElectricFurnaceBlockEntity.WATTS);
        c.setItem(IN, new ItemStack(Heat.REFRACTORY_BRICK_BLANK, 2));
        tickConsumer(h, pos, be, T + 20, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> nothing fired");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        tickConsumer(h, pos, be, T - 1, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not done 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(OUT).is(Heat.REFRACTORY_BRICK), "blank -> refractory brick at ~1500 C");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "1 blank used");
        c.setItem(IN, new ItemStack(Semiconductor.GRAPHITE_BOAT_BLANK));
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, pos, be, T, feed);
        h.assertTrue(c.getItem(OUT).is(Semiconductor.GRAPHITE_BOAT), "clay-graphite blank -> graphite boat");
        h.assertFalse(ElectricFurnaceBlockEntity.isInput(new ItemStack(Items.RAW_IRON)), "not a general smelter");
        h.assertTrue(recipeUses(h, "electric_furnace", Power.GRAPHITE_ROD) && recipeUses(h, "electric_furnace", Heat.REFRACTORY_BRICK), "electric furnace = refractory brick + graphite rods");
        h.assertTrue(recipeUses(h, "hydrogen_furnace", Power.ELECTRIC_FURNACE.block.asItem()), "hydrogen furnace is built from the electric furnace");
        LOG.info("[LAB] electric furnace: blank -> brick / boat in {} ticks at 5 kW, nothing without power; hydrogen furnace built on it", T);
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        // recipes() は順番のない Map なので、材料の ID 順に並べて毎回同じ順で書く（IMP-27）
        for (var e : ElectricFurnaceBlockEntity.recipes().entrySet().stream().sorted(java.util.Comparator.comparing(x -> iid(x.getKey()))).toList())
            rec(a, Power.ELECTRIC_FURNACE.block, "電気で焼く", L(io(e.getKey(), 1)), L(io(e.getValue(), 1)), ElectricFurnaceBlockEntity.WATTS, ElectricFurnaceBlockEntity.TICKS / 20.0, null);
        rec(a, Power.STEAM_ENGINE.block, "蒸気で発電", L(io(Items.COAL, 1), cond(io(Items.WATER_BUCKET, 1), "バケツ1杯で石炭2個分")), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.WATTS, com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.COAL_TICKS / 20.0, "石炭1個の熱は 30 MJ（ゲームの約束）。自励式ダイナモで2倍");
        rec(a, Power.STEAM_TURBINE.block, "蒸気タービンで発電", L(io(Items.COAL, 1), cond(io(Items.WATER_BUCKET, 1), "バケツ1杯で石炭2個分")), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity.WATTS, com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity.COAL_TICKS / 20.0, "熱効率30%");
        rec(a, Power.WATER_WHEEL.block, "水車で発電", L(kept(cond(io("横に流れる水", 1), "1面ごと"))), L(io("電気", 1)),
            -com.taku3.semiconductorlab.energy.WaterWheelBlockEntity.WATTS_PER_FLOW, 0, "1面あたり。止まった水では回らない");
        rec(a, Power.LEAD_ACID_BATTERY.block, "電気をためる", L(io("電気", 1)), L(cond(io("電気", 1), (com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CAPACITY / 1_000_000) + " MJ まで、充電は "
            + com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.CHARGE_EFFICIENCY_PERCENT + "% がたまる")), com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity.MAX_WATTS, 0, "充電・放電とも最大の電力");
    }

    /** [4-13] ボルタ・[4-36] ダニエル（IMP3、S-005）：亜鉛と液がないと出ない。ボルタは使うほど下がる（分極）、ダニエルは一定。亜鉛と液を1つずつ使う */
    @GameTest(maxTicks = 100)
    public void chemicalCells(GameTestHelper h) {
        floor(h, 3);
        BlockPos vp = p(1, 1, 1), dp = p(3, 1, 1);
        h.setBlock(vp, Power.VOLTAIC_PILE.block);
        h.setBlock(dp, Power.DANIELL_CELL.block);
        var v = h.getBlockEntity(vp, com.taku3.semiconductorlab.block.ChemicalCellBlockEntity.class);
        var d = h.getBlockEntity(dp, com.taku3.semiconductorlab.block.ChemicalCellBlockEntity.class);
        var lv = h.getLevel();
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, h.absolutePos(vp), lv.getBlockState(h.absolutePos(vp)), v);
        h.assertTrue(v.getLastWatts() == 0, "no zinc -> no current");
        v.getContainer().setItem(0, new ItemStack(Metallurgy.ZINC_INGOT, 2)); v.getContainer().setItem(1, new ItemStack(Gas.SALT, 2));
        d.getContainer().setItem(0, new ItemStack(Metallurgy.ZINC_INGOT, 2)); d.getContainer().setItem(1, new ItemStack(Chemistry.COPPER_SULFATE, 2));
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, h.absolutePos(vp), lv.getBlockState(h.absolutePos(vp)), v);
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, h.absolutePos(dp), lv.getBlockState(h.absolutePos(dp)), d);
        double v0 = v.getLastWatts(), d0 = d.getLastWatts();
        h.assertTrue(v0 > 4.9 && d0 > 9.9, "both start: volta " + v0 + " W, daniell " + d0 + " W");
        h.assertValueEqual(v.getContainer().getItem(0).getCount(), 1, "one zinc in use");
        for (int i = 0; i < 1200; i++) {
            com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, h.absolutePos(vp), lv.getBlockState(h.absolutePos(vp)), v);
            com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, h.absolutePos(dp), lv.getBlockState(h.absolutePos(dp)), d);
        }
        h.assertTrue(v.getLastWatts() < v0 * 0.5, "volta polarizes: " + v0 + " -> " + v.getLastWatts() + " W");
        h.assertTrue(Math.abs(d.getLastWatts() - d0) < 0.01, "daniell stays steady: " + d.getLastWatts() + " W");
        h.assertFalse(v.supportsAlternator() || v.supportsDynamoUpgrade(), "cells do not rotate: no alternator, no dynamo");
        LOG.info("[LAB] chemical cells [4-13][4-36]: no zinc -> 0 W; volta " + v0 + " -> " + v.getLastWatts() + " W (polarization); daniell steady " + d.getLastWatts() + " W");
        h.succeed();
    }
}
