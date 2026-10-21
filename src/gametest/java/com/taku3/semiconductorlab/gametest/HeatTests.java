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
 * GameTest：幹3 熱（炉と温度）（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class HeatTests extends LabTestBase {
    // =====================================================================
    // [3-12] 耐火れんが（珪石れんが・ディナス）
    // =====================================================================

    /** 成形品は溶鉱炉（≈1450℃）でだけ焼ける。ふつうのかまど（≈1000℃）では焼けない。転炉の内張りは耐火れんが */
    @GameTest
    public void refractoryBrick(GameTestHelper h) {
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        ItemStack fired = cook(h, BLAST, Heat.REFRACTORY_BRICK_BLANK);
        h.assertTrue(fired.is(Heat.REFRACTORY_BRICK), "blast furnace should fire the blank into a refractory brick, got " + fired);
        h.assertTrue(cook(h, SMELT, Heat.REFRACTORY_BRICK_BLANK).isEmpty(), "an ordinary furnace (~1000 C) must not fire silica bricks");
        h.assertTrue(recipeUses(h, "refractory_brick_blank", Items.SAND) && recipeUses(h, "refractory_brick_blank", Survival.SLAKED_LIME), "blank = silica sand + slaked lime (Dinas)");
        h.assertFalse(recipeUses(h, "refractory_brick_blank", Items.CLAY_BALL), "binder is lime now, not clay [0-10]");
        h.assertTrue(recipeUses(h, "bessemer_converter", Heat.REFRACTORY_BRICK), "converter lining should be refractory brick");
        h.assertFalse(recipeUses(h, "bessemer_converter", Items.CLAY_BALL), "converter no longer uses raw clay");
        LOG.info("[LAB] refractory brick: blank fires only in the blast furnace; Bessemer converter lined with refractory brick");
        h.succeed();
    }

    @GameTest
    public void reverberatoryRoasting(GameTestHelper h) {
        int T = ReverberatoryFurnaceBlockEntity.ROAST_TICKS;
        int IN = ReverberatoryFurnaceBlockEntity.SLOT_IN, FUEL = ReverberatoryFurnaceBlockEntity.SLOT_FUEL, OUT = ReverberatoryFurnaceBlockEntity.SLOT_OUT;
        int SO2 = ReverberatoryFurnaceBlockEntity.SLOT_SO2, GE = ReverberatoryFurnaceBlockEntity.SLOT_BY_GE, IND = ReverberatoryFurnaceBlockEntity.SLOT_BY_IN;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Heat.REVERBERATORY_FURNACE.block);
        ReverberatoryFurnaceBlockEntity be = h.getBlockEntity(pos, ReverberatoryFurnaceBlockEntity.class);
        var c = be.getContainer();

        // 燃料なし：焼けない
        c.setItem(IN, new ItemStack(Metallurgy.GALENA, 2));
        tickRoaster(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no fuel -> no roasting");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 石炭1個：方鉛鉱 → 一酸化鉛 ＋ SO2（10秒）
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        tickRoaster(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickRoaster(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(Metallurgy.LITHARGE), "galena -> litharge (2PbS + 3O2 -> 2PbO + 2SO2)");
        h.assertTrue(c.getItem(SO2).is(Chemistry.SULFUR_DIOXIDE), "SO2 is collected");
        h.assertTrue(c.getItem(FUEL).isEmpty(), "one coal was burned");
        tickRoaster(h, pos, be, T);
        h.assertValueEqual(c.getItem(OUT).getCount(), 2, "the same coal keeps burning for the 2nd ore");
        h.assertValueEqual(c.getItem(SO2).getCount(), 2, "2 SO2");
        // SO2 は硫酸の原料
        h.assertTrue(recipeUses(h, "sulfuric_acid", Chemistry.SULFUR_DIOXIDE), "SO2 -> sulfuric acid route");

        // 辰砂の粉 → 水銀 ＋ SO2（木炭でも燃える）
        c.setItem(OUT, ItemStack.EMPTY); c.setItem(SO2, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(Metallurgy.CINNABAR_RETORT_CHARGE, 1));
        c.setItem(FUEL, new ItemStack(Items.CHARCOAL, 1));
        tickRoaster(h, pos, be, T);
        h.assertTrue(c.getItem(OUT).is(Metallurgy.MERCURY), "cinnabar -> mercury (HgS + O2 -> Hg + SO2)");
        h.assertValueEqual(c.getItem(SO2).getCount(), 1, "SO2 from cinnabar");

        // 閃亜鉛鉱 8個 → 酸化亜鉛 8 ＋ SO2 8 ＋ Ge精鉱 2 ＋ インジウム 1（1回で同時に）
        c.setItem(OUT, ItemStack.EMPTY); c.setItem(SO2, ItemStack.EMPTY);
        c.setItem(IN, new ItemStack(Metallurgy.SPHALERITE, 8));
        c.setItem(FUEL, new ItemStack(Items.COAL, 2));
        tickRoaster(h, pos, be, T * 4);
        h.assertValueEqual(c.getItem(GE).getCount(), 1, "4 sphalerite -> 1 Ge concentrate");
        h.assertTrue(c.getItem(IND).isEmpty(), "no indium before the 8th");
        tickRoaster(h, pos, be, T * 4);
        h.assertTrue(c.getItem(OUT).is(Heat.ZINC_OXIDE) && c.getItem(OUT).getCount() == 8, "8 sphalerite -> 8 ZnO, got " + c.getItem(OUT));
        h.assertValueEqual(c.getItem(SO2).getCount(), 8, "8 SO2");
        h.assertValueEqual(c.getItem(GE).getCount(), 2, "8 sphalerite -> 2 Ge concentrate");
        h.assertValueEqual(c.getItem(IND).getCount(), 1, "8 sphalerite -> 1 indium");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Heat.ZINC_OXIDE).is(Metallurgy.ZINC_INGOT), "ZnO -> zinc in a furnace");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.SPHALERITE).is(Metallurgy.ZINC_INGOT), "old furnace route sphalerite -> zinc is kept");
        // 作業台での分割（旧 #16）は無くなった
        MinecraftServer server = h.getLevel().getServer();
        for (String gone : new String[] {"germanium_concentrate_from_sphalerite", "indium"})
            h.assertTrue(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, SemiconductorLab.id(gone))).isEmpty(), "crafting split recipe should be gone: " + gone);

        // SO2 の出口が満杯なら止まる（煙を捨てない）
        c.setItem(IN, new ItemStack(Metallurgy.GALENA, 1));
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(SO2, new ItemStack(Chemistry.SULFUR_DIOXIDE, 64));
        c.setItem(FUEL, new ItemStack(Items.COAL, 1));
        tickRoaster(h, pos, be, T * 2);
        h.assertTrue(c.getItem(OUT).isEmpty(), "SO2 output full -> stop");
        h.assertValueEqual(c.getItem(IN).getCount(), 1, "galena not used while SO2 is full");
        // 焼けない物は入らない
        h.assertFalse(ReverberatoryFurnaceBlockEntity.isInput(new ItemStack(Items.RAW_IRON)), "iron ore is not roasted here");
        h.assertFalse(ReverberatoryFurnaceBlockEntity.isFuel(new ItemStack(Items.LAVA_BUCKET)), "only coal / charcoal");
        LOG.info("[LAB] reverberatory furnace: galena -> PbO + SO2, cinnabar -> Hg + SO2, 8 sphalerite -> 8 ZnO + 8 SO2 + 2 Ge conc + 1 In ({} t each, coal {} t)",
                T, ReverberatoryFurnaceBlockEntity.FUEL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // [3-15] アーク炉 → [1-15] 金属シリコン（タスク15）
    // =====================================================================

    /** 珪石2＋木炭（か石炭）1＋500kW×86.4秒（12kWh）→ 金属シリコン（98%）。電力が半分なら進まない。電極は8回で1本 */
    @GameTest
    public void arcFurnaceSilicon(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Power.GRAPHITE_ROD, Heat.REFRACTORY_BRICK, Power.COPPER_CABLE.block.asItem()})
            h.assertTrue(recipeUses(h, "arc_furnace", it), "arc furnace = graphite electrodes + refractory lining + heavy cable: " + it);
        var A = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.class;
        double kwh = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.WATTS * com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS / 20.0 / 3.6e6;
        h.assertTrue(kwh >= 10 && kwh <= 14, "about 12 kWh per kg of silicon (real: over 10 kWh), got " + kwh);
        floor(h, 5);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Heat.ARC_FURNACE.block);
        var be = h.getBlockEntity(fp, A);
        var c = be.getContainer();
        int Q = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_QUARTZ, CB = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_CARBON,
            E = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_ELECTRODE, OUT = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.SLOT_OUT;
        int T = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.WATTS);
        h.assertTrue(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.isCarbon(new ItemStack(Items.COAL)), "coal also works (Q1515-2)");
        h.assertFalse(com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.isCarbon(new ItemStack(Items.STICK)), "wood sticks are not carbon");
        c.setItem(Q, new ItemStack(Metallurgy.VEIN_QUARTZ, 20));
        c.setItem(CB, new ItemStack(Items.CHARCOAL, 10));
        tickConsumer(h, fp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no electrode -> no arc");
        c.setItem(E, new ItemStack(Power.GRAPHITE_ROD, 2));
        tickConsumer(h, fp, be, T, feed / 2);
        h.assertTrue(c.getItem(OUT).isEmpty(), "half power cannot hold 2000 C -> nothing");
        c.setItem(Q, new ItemStack(Metallurgy.VEIN_QUARTZ, 20));
        tickConsumer(h, fp, be, T, feed);
        ItemStack si = c.getItem(OUT);
        h.assertTrue(si.is(Metallurgy.METALLURGICAL_SILICON) && si.getCount() == 1, "2 quartz + 1 charcoal -> 1 silicon, got " + si);
        h.assertValueEqual(si.get(com.taku3.semiconductorlab.ModDataComponents.PURITY), com.taku3.semiconductorlab.Purity.METALLURGICAL, "metallurgical grade 98%");
        h.assertValueEqual(c.getItem(Q).getCount(), 18, "SiO2 + 2C: 2 quartz per run");
        h.assertValueEqual(c.getItem(E).getCount(), 1, "first run takes a fresh electrode");
        for (int i = 0; i < 7; i++) tickConsumer(h, fp, be, T, feed);
        h.assertValueEqual(c.getItem(E).getCount(), 1, "8 runs per electrode");
        h.assertValueEqual(c.getItem(OUT).getCount(), 8, "8 silicon");
        tickConsumer(h, fp, be, T, feed);
        h.assertTrue(c.getItem(E).isEmpty(), "the 9th run takes the second electrode");
        h.assertValueEqual(c.getItem(CB).getCount(), 1, "1 charcoal per run");
        // [3-25] カーバイド：生石灰1＋炭素3（コークスも可）＋3kWh → CaC2
        double kwhC = com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.WATTS * com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS_CARBIDE / 20.0 / 3.6e6;
        h.assertTrue(kwhC >= 2.5 && kwhC <= 3.5, "about 3 kWh per kg of carbide, got " + kwhC);
        c.setItem(OUT, ItemStack.EMPTY);
        c.setItem(Q, new ItemStack(com.taku3.semiconductorlab.content.Survival.QUICKLIME, 2));
        c.setItem(CB, new ItemStack(Metallurgy.COKE, 6));
        c.setItem(E, new ItemStack(Power.GRAPHITE_ROD, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.TICKS_CARBIDE, feed);
        h.assertTrue(c.getItem(OUT).is(Heat.CALCIUM_CARBIDE) && c.getItem(OUT).getCount() == 1, "quicklime + 3 coke -> calcium carbide, got " + c.getItem(OUT));
        h.assertValueEqual(c.getItem(Q).getCount(), 1, "CaO: 1 quicklime per run");
        h.assertValueEqual(c.getItem(CB).getCount(), 3, "3C: 3 carbon per run");
        LOG.info("[LAB] arc furnace [3-15] -> metallurgical Si [1-15]: 2 quartz + charcoal + {} kWh -> Si 98%, half power stalls, 8 runs per electrode", String.format("%.1f", kwh));
        h.succeed();
    }

    // =====================================================================
    // [3-10] アチソン炉（炭素＋砂に直接通電、2500℃以上で人造黒鉛）
    // =====================================================================

    /** 電力不足では温度が届かず黒鉛にならない。炭素がないと電流が流れない。500kW を直接入れると昇温→黒鉛化 */
    @GameTest
    public void achesonFurnace(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Heat.ACHESON_FURNACE.block);
        AchesonFurnaceBlockEntity be = h.getBlockEntity(pos, AchesonFurnaceBlockEntity.class);
        var c = be.getContainer();
        int C = AchesonFurnaceBlockEntity.SLOT_CARBON, S = AchesonFurnaceBlockEntity.SLOT_SAND, OUT = AchesonFurnaceBlockEntity.SLOT_OUT;
        long full = EnergyStorage.wattsToJoulesPerTick(AchesonFurnaceBlockEntity.REQUIRED_WATTS);
        long steamEngine = EnergyStorage.wattsToJoulesPerTick(SteamEngineBlockEntity.WATTS);

        // 炭素なし：電気が来ても通り道がないので流れない（使わない）
        c.setItem(S, new ItemStack(Items.SAND, 2));
        tickConsumer(h, pos, be, 40, full);
        h.assertValueEqual(be.getLastWatts(), 0.0, "no carbon -> no current");
        h.assertTrue(be.getTemperature() < 21, "no carbon -> stays cold");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 蒸気機関1台分（50kW）：何分たっても 2500℃ に届かない（約280℃で落ち着く）
        c.setItem(C, new ItemStack(Items.COAL, 8));
        tickConsumer(h, pos, be, 20 * 120, steamEngine);
        double t50 = be.getTemperature();
        h.assertTrue(t50 > 200 && t50 < 400, "50 kW should settle near 280 C, got " + t50);
        h.assertTrue(c.getItem(OUT).isEmpty(), "underpowered -> no graphite");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);

        // 500kW を直接入れる：約47秒で 2500℃、そこから60秒で人造黒鉛2
        int heat = 0;
        while (be.getTemperature() < AchesonFurnaceBlockEntity.GRAPHITIZE_C && heat < 20 * 120) { tickConsumer(h, pos, be, 1, full); heat++; }
        h.assertTrue(be.getTemperature() >= AchesonFurnaceBlockEntity.GRAPHITIZE_C, "500 kW should reach 2500 C within 2 min");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        h.assertTrue(c.getItem(OUT).isEmpty(), "graphitizing has only just started");
        tickConsumer(h, pos, be, AchesonFurnaceBlockEntity.WORK_TICKS, full);
        h.assertTrue(c.getItem(OUT).is(Heat.SYNTHETIC_GRAPHITE), "carbon + sand at 2500 C -> synthetic graphite");
        h.assertValueEqual(c.getItem(OUT).getCount(), AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH, "graphite per batch");
        h.assertValueEqual(c.getItem(C).getCount(), 4, "4 carbon used");
        h.assertValueEqual(c.getItem(S).getCount(), 1, "1 sand used");

        // 電気を止めると冷えて、黒鉛化も止まる
        tickConsumer(h, pos, be, 20 * 60, 0);
        h.assertTrue(be.getTemperature() < 500, "cools down without power, got " + be.getTemperature());
        h.assertValueEqual(c.getItem(OUT).getCount(), AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH, "no more graphite while cold");
        h.assertTrue(recipeUses(h, "acheson_furnace", Power.GRAPHITE_ROD), "electrodes are graphite rods");
        LOG.info("[LAB] acheson: no carbon -> 0 W; 50 kW -> {} C (no graphite); 500 kW -> 2500 C in {} ticks, 4 C + sand -> 2 synthetic graphite", Math.round(t50), heat);
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */

    /**
     * [3-04] ふいご：踏む（pump）と送風 100%、約1秒で抜けて 0。横の炉は airflow で読む。
     * 横に回る水車があれば水車ふいご（ずっと 100%）
     */
    @GameTest(maxTicks = 100)
    public void bellows(GameTestHelper h) {
        floor(h, 8);
        var furnace = p(5, 1, 5);
        h.setBlock(p(5, 1, 6), Heat.BELLOWS.block);
        var be = h.getBlockEntity(p(5, 1, 6), com.taku3.semiconductorlab.block.BellowsBlockEntity.class);
        var lv = h.getLevel();
        h.assertValueEqual(com.taku3.semiconductorlab.block.BellowsBlockEntity.airflow(lv, h.absolutePos(furnace)), 0.0, "no air before pumping");
        be.pump();
        h.assertValueEqual(com.taku3.semiconductorlab.block.BellowsBlockEntity.airflow(lv, h.absolutePos(furnace)), 1.0, "one pump = full blow");
        for (int i = 0; i < com.taku3.semiconductorlab.block.BellowsBlockEntity.PER_PUMP; i++) com.taku3.semiconductorlab.block.BellowsBlockEntity.tick(lv, h.absolutePos(p(5, 1, 6)), lv.getBlockState(h.absolutePos(p(5, 1, 6))), be);
        h.assertValueEqual(com.taku3.semiconductorlab.block.BellowsBlockEntity.airflow(lv, h.absolutePos(furnace)), 0.0, "the air runs out in about a second");
        // 水車ふいご：流れる水の横の水車の隣
        h.setBlock(p(3, 1, 3), Power.WATER_WHEEL.block);
        h.setBlock(p(1, 1, 3), Blocks.WATER);
        h.setBlock(p(3, 1, 4), Heat.BELLOWS.block);
        h.succeedWhen(() -> {
            h.assertTrue(com.taku3.semiconductorlab.block.BellowsBlockEntity.waterDriven(lv, h.absolutePos(p(3, 1, 4))), "bellows beside a turning water wheel is water-driven");
            h.assertValueEqual(com.taku3.semiconductorlab.block.BellowsBlockEntity.airflow(lv, h.absolutePos(p(3, 1, 5))), 1.0, "water-powered bellows blows all the time");
            LOG.info("[LAB] bellows: one pump = about 1 s of full blow; water wheel -> continuous");
        });
    }

    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Heat.ACHESON_FURNACE.block, "人造黒鉛", L(alt(io(Items.COAL, AchesonFurnaceBlockEntity.CARBON_PER_BATCH), Items.CHARCOAL), io(Items.SAND, AchesonFurnaceBlockEntity.SAND_PER_BATCH)),
            L(io(Heat.SYNTHETIC_GRAPHITE, AchesonFurnaceBlockEntity.GRAPHITE_PER_BATCH)), AchesonFurnaceBlockEntity.REQUIRED_WATTS, AchesonFurnaceBlockEntity.WORK_TICKS / 20.0, "2500℃ 以上で進む（電力が足りないと届かない）");
        double fuelS = ReverberatoryFurnaceBlockEntity.FUEL_TICKS / (double) ReverberatoryFurnaceBlockEntity.ROAST_TICKS;
        for (var it : new net.minecraft.world.item.Item[] {Metallurgy.GALENA, Metallurgy.CINNABAR_RETORT_CHARGE, Metallurgy.SPHALERITE}) {
            var main = ReverberatoryFurnaceBlockEntity.mainProduct(new ItemStack(it)).getItem();
            var out = it == Metallurgy.SPHALERITE
                    ? L(io(main, 1), io(Chemistry.SULFUR_DIOXIDE, 1), perRuns(io(Metallurgy.GERMANIUM_CONCENTRATE, 1), ReverberatoryFurnaceBlockEntity.GE_EVERY), perRuns(io(Metallurgy.INDIUM, 1), ReverberatoryFurnaceBlockEntity.IN_EVERY))
                    : L(io(main, 1), io(Chemistry.SULFUR_DIOXIDE, 1));
            rec(a, Heat.REVERBERATORY_FURNACE.block, "焙焼", L(io(it, 1), perRuns(io(Items.COAL, 1), (int) fuelS)), out, 0, ReverberatoryFurnaceBlockEntity.ROAST_TICKS / 20.0, "燃料式（電気なし）");
        }
        rec(a, Heat.ARC_FURNACE.block, "金属シリコン", L(io(Metallurgy.VEIN_QUARTZ, ArcFurnaceBlockEntity.QUARTZ_PER_RUN), alt(io(Items.CHARCOAL, ArcFurnaceBlockEntity.CARBON_PER_RUN), Items.COAL), perRuns(io(Power.GRAPHITE_ROD, 1), ArcFurnaceBlockEntity.RUNS_PER_ELECTRODE)),
            L(io(Metallurgy.METALLURGICAL_SILICON, 1)), ArcFurnaceBlockEntity.WATTS, ArcFurnaceBlockEntity.TICKS / 20.0, "SiO2 + 2C → Si + 2CO、純度98%");
    }

    /** [3-22] 手回しの冷凍機（IMP3、O-019）：冷媒なしでは冷えない、水なしでは回らない、エーテル＋水で16回まわすと氷 */
    @GameTest
    public void perkinsIceMachine(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "diethyl_ether", Chemistry.ETHANOL) && recipeUses(h, "diethyl_ether", Chemistry.SULFURIC_ACID), "ethanol + H2SO4 -> ether");
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, Heat.PERKINS_ICE_MACHINE);
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.assertTrue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET)), "water in");
        for (int i = 0; i < 20; i++) com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.crank(level, pos, player);
        h.assertTrue(player.getInventory().countItem(net.minecraft.world.item.Items.ICE) == 0, "no refrigerant -> no ice");
        h.assertTrue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.fill(level, pos, player, new ItemStack(Chemistry.DIETHYL_ETHER)), "ether in");
        boolean made = false;
        for (int i = 0; i < com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.CRANKS; i++) made = com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.crank(level, pos, player);
        h.assertTrue(made && player.getInventory().countItem(net.minecraft.world.item.Items.ICE) == 1, "16 turns -> ice");
        h.assertFalse(level.getBlockState(pos).getValue(com.taku3.semiconductorlab.block.PerkinsIceMachineBlock.WATER), "water used");
        h.succeed();
    }

    /** S-017（E の [B-07] LED のヒ素）：反射炉で黄鉄鉱を焼くとベンガラ（Fe2O3）＋SO2、4個ごとに煙道のちりの白ヒ素（As2O3）が副産物スロットに */
    @GameTest(maxTicks = 100)
    public void pyriteWhiteArsenic(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Heat.REVERBERATORY_FURNACE.block);
        ReverberatoryFurnaceBlockEntity be = h.getBlockEntity(pos, ReverberatoryFurnaceBlockEntity.class);
        var c = be.getContainer();
        c.setItem(ReverberatoryFurnaceBlockEntity.SLOT_IN, new ItemStack(Chemistry.PYRITE, 4));
        c.setItem(ReverberatoryFurnaceBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL, 1));
        tickRoaster(h, pos, be, ReverberatoryFurnaceBlockEntity.ROAST_TICKS * 3);
        h.assertTrue(c.getItem(ReverberatoryFurnaceBlockEntity.SLOT_OUT).is(Chemistry.COLCOTHAR) && c.getItem(ReverberatoryFurnaceBlockEntity.SLOT_OUT).getCount() == 3, "pyrite -> colcothar (Fe2O3)");
        h.assertTrue(c.getItem(ReverberatoryFurnaceBlockEntity.SLOT_BY_GE).isEmpty(), "no arsenic before the 4th");
        tickRoaster(h, pos, be, ReverberatoryFurnaceBlockEntity.ROAST_TICKS);
        h.assertTrue(c.getItem(ReverberatoryFurnaceBlockEntity.SLOT_BY_GE).is(com.taku3.semiconductorlab.content.Metallurgy.WHITE_ARSENIC), "every 4th pyrite leaves white arsenic in the flue");
        h.assertValueEqual(c.getItem(ReverberatoryFurnaceBlockEntity.SLOT_SO2).getCount(), 4, "SO2 from each pyrite");
        h.succeed();
    }
}
