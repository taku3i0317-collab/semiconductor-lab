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
 * GameTest：幹9 半導体プロセス（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class SemiconductorTests extends LabTestBase {
    @GameTest
    public void zoneRefiningPurity(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Semiconductor.ZONE_REFINING_FURNACE.block);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT;
        c.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(Semiconductor.SYNTHETIC_GRAPHITE_BOAT));

        c.setItem(G, rawChloride(4));   // [1-34] GeCl4 経由（硫酸経由は 9N 止まり＝geCl4Route）
        tickRefiner(h, pos, be, T - 1);
        h.assertTrue(c.getItem(G).is(Metallurgy.RAW_GERMANIUM), "not refined 1 tick before 30 s");
        tickRefiner(h, pos, be, 1);
        h.assertTrue(c.getItem(G).is(Semiconductor.REFINED_GERMANIUM), "raw -> refined");
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.SIX_N, "1st pass = 6N");
        h.assertValueEqual(c.getItem(G).getCount(), 4, "count kept");
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "2nd pass = 9N");
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "3rd pass = 10N");
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "no 11N: Ge stops at 10N");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        LOG.info("[LAB] zone refining: raw -> 6N -> 9N -> 10N, then idle (no 11N)");
        h.succeed();
    }

    @GameTest
    public void zoneRefiningDoping(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Semiconductor.ZONE_REFINING_FURNACE.block);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT, D = ZoneRefiningFurnaceBlockEntity.DOPANT_SLOT;
        c.setItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT, new ItemStack(Semiconductor.GRAPHITE_BOAT));   // ドーピングは粘土ボートでも 9N でできる

        // 9N + リン → n型
        c.setItem(G, refined(Purity.NINE_N, 8));
        c.setItem(D, new ItemStack(Chemistry.PHOSPHORUS, 2));
        tickRefiner(h, pos, be, T);
        h.assertTrue(c.getItem(G).is(Semiconductor.N_TYPE_GERMANIUM), "9N + P -> n-type");
        h.assertValueEqual(c.getItem(G).getCount(), 8, "n-type count");
        h.assertValueEqual(c.getItem(D).getCount(), 1, "1 P consumed");

        // 9N + インジウム → p型
        c.setItem(G, refined(Purity.NINE_N, 3));
        c.setItem(D, new ItemStack(Metallurgy.INDIUM, 1));
        tickRefiner(h, pos, be, T);
        h.assertTrue(c.getItem(G).is(Semiconductor.P_TYPE_GERMANIUM), "9N + In -> p-type");
        h.assertTrue(c.getItem(D).isEmpty(), "In consumed");

        // 6N + リン → ドープしない（純度不足。先に精製へ回る）
        c.setItem(G, refined(Purity.SIX_N, 2));
        c.setItem(D, new ItemStack(Chemistry.PHOSPHORUS, 1));
        tickRefiner(h, pos, be, T);
        h.assertFalse(c.getItem(G).is(Semiconductor.N_TYPE_GERMANIUM), "6N + P must not become n-type");
        h.assertValueEqual(c.getItem(D).getCount(), 1, "P not consumed with 6N");
        h.assertValueEqual(c.getItem(ZoneRefiningFurnaceBlockEntity.BOAT_SLOT).getCount(), 1, "boat is not used up");
        LOG.info("[LAB] zone doping: 9N+P -> n, 9N+In -> p, 6N+P -> no doping (refines to {} first)", Purity.label(purityOf(c.getItem(G))));
        h.succeed();
    }

    // =====================================================================
    // [9-16] 熱拡散（IMP-25）
    // =====================================================================

    /** n型の板8＋B2O3 → p-n 接合の板8（1954 と同じ向き）。p型＋リンも可。同じ型・真性・数が足りない・電力不足では動かない */
    @GameTest
    public void diffusionFurnace(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Power.ELECTRIC_FURNACE.block.asItem(), Optics.QUARTZ_TUBE, Heat.REFRACTORY_BRICK})
            h.assertTrue(recipeUses(h, "diffusion_furnace", it), "diffusion furnace = electric furnace [4-12] + quartz tube [7-02]: " + it);
        java.util.function.Function<String, ItemStack> wafer = dp -> {
            ItemStack s = new ItemStack(Semiconductor.SILICON_WAFER, 16);
            s.set(com.taku3.semiconductorlab.ModDataComponents.PURITY, com.taku3.semiconductorlab.Purity.NINE_N);
            if (dp != null) s.set(com.taku3.semiconductorlab.ModDataComponents.DOPANT, dp);
            return s;
        };
        h.assertTrue(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(wafer.apply("phosphorus"), new ItemStack(Metallurgy.BORON_TRIOXIDE)), "n + B -> junction");
        h.assertTrue(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(wafer.apply("boron"), new ItemStack(Chemistry.PHOSPHORUS)), "p + P -> junction");
        h.assertFalse(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(wafer.apply("boron"), new ItemStack(Metallurgy.BORON_TRIOXIDE)), "same type -> no junction");
        h.assertFalse(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(wafer.apply(null), new ItemStack(Metallurgy.BORON_TRIOXIDE)), "intrinsic -> no junction");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.DIFFUSION_FURNACE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS);
        ItemStack few = wafer.apply("phosphorus"); few.setCount(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN - 1);
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, few);
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE, new ItemStack(Metallurgy.BORON_TRIOXIDE, 2));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT).isEmpty(), "7 slices are not a full batch");
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, wafer.apply("phosphorus"));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT).isEmpty(), "half power cannot hold the temperature");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS, feed);
        ItemStack j = c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(j.is(Semiconductor.PN_JUNCTION_WAFER) && j.getCount() == com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN, "8 n-type + B2O3 -> 8 p-n slices, got " + j);
        h.assertValueEqual(j.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "phosphorus", "base type kept (n base, p top as in 1954)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER).getCount(), 8, "8 slices used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE).getCount(), 1, "1 B2O3 used");
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, wafer.apply("boron"));
        c.setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE, new ItemStack(Metallurgy.BORON_TRIOXIDE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT).isEmpty(), "p-type + boron -> nothing");
        LOG.info("[LAB] diffusion [9-16]: 8 n-type slices + B2O3 -> 8 p-n junction slices ({} s, {} kW); p + P also; same type / intrinsic rejected", com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS / 20, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS / 1000);
        h.succeed();
    }

    // =====================================================================
    // [9-12] ウェハー切断（IMP-25）
    // =====================================================================

    /** 単結晶1 → 板8（純度・ドーパントを受け継ぐ）。刃がないと切れない、刃は4本で1枚。電力が足りないと進まない */
    @GameTest
    public void waferSaw(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Metallurgy.STEEL_INGOT, Power.ELECTRIC_MOTOR, Precision.LEAD_SCREW})
            h.assertTrue(recipeUses(h, "wafer_saw", it), "wafer saw parts: " + it);
        h.assertTrue(recipeUses(h, "diamond_saw_blade", Items.DIAMOND) && recipeUses(h, "diamond_saw_blade", Metallurgy.STEEL_INGOT), "blade = steel disc + diamond");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.WAFER_SAW.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.WaferSawBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.WaferSawBlockEntity.WATTS);
        ItemStack ing = new ItemStack(Semiconductor.SILICON_SINGLE_CRYSTAL, 6);
        ing.set(com.taku3.semiconductorlab.ModDataComponents.PURITY, com.taku3.semiconductorlab.Purity.ELEVEN_N);
        ing.set(com.taku3.semiconductorlab.ModDataComponents.DOPANT, "phosphorus");
        c.setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_CRYSTAL, ing);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT).isEmpty(), "no blade -> no cut");
        c.setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_BLADE, new ItemStack(Semiconductor.DIAMOND_SAW_BLADE, 2));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS, feed);
        ItemStack s = c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT);
        h.assertTrue(s.is(Semiconductor.SILICON_WAFER) && s.getCount() == com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLICES_PER_INGOT, "1 ingot -> 8 slices, got " + s);
        h.assertValueEqual(s.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "phosphorus", "dopant kept (n-type)");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(s.get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 11, "purity kept");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_BLADE).getCount(), 1, "first cut takes a blade");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_CRYSTAL).getCount(), 5, "1 ingot used");
        c.setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        for (int i = 0; i < 3; i++) { tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS, feed); c.setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT, ItemStack.EMPTY); }
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_BLADE).getCount(), 1, "one blade lasts 4 ingots");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_BLADE).getCount(), 0, "5th ingot takes the next blade");
        LOG.info("[LAB] wafer saw [9-12]: 1 ingot -> {} slices (purity, dopant kept), blade lasts {} ingots", com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLICES_PER_INGOT, com.taku3.semiconductorlab.block.WaferSawBlockEntity.RUNS_PER_BLADE);
        h.succeed();
    }

    // =====================================================================
    // [9-08] CZ 法（シリコン単結晶、タスク23）
    // =====================================================================

    /** 種：多結晶Si＋るつぼ＋Ar → 種結晶。単結晶：種（残る）＋多結晶Si 2＋るつぼ＋Ar 2＋B2O3/リン → インゴット（純度とドーパントを受け継ぐ）。るつぼ・Ar・電力が欠けると動かない */
    @GameTest
    public void czPuller(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Semiconductor.CRYSTAL_PULLER.block.asItem(), Power.GRAPHITE_ROD, Metallurgy.STEEL_INGOT, Precision.LEAD_SCREW, Power.ELECTRIC_MOTOR})
            h.assertTrue(recipeUses(h, "cz_puller", it), "CZ puller = bigger Ge puller [9-07] + graphite heater + steel chamber: " + it);
        h.assertTrue(recipeUses(h, "quartz_crucible", Optics.FUSED_QUARTZ), "quartz crucible from fused quartz [7-02]");
        h.assertFalse(com.taku3.semiconductorlab.block.CzPullerBlockEntity.isCrucible(new ItemStack(Semiconductor.GRAPHITE_BOAT)), "graphite is not a crucible for Si (forms SiC)");
        double kwhPerKg = com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS * com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0 / 3.6e6 / com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT;
        h.assertTrue(Math.abs(kwhPerKg - com.taku3.semiconductorlab.block.CzPullerBlockEntity.KWH_PER_KG) < 0.05, "17.4 kWh/kg (NTU), got " + kwhPerKg);
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.CZ_PULLER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.CzPullerBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS);
        // 種結晶づくり：るつぼがないと動かない
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED, new ItemStack(Metallurgy.POLYSILICON, 1));
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR, new ItemStack(Gas.ARGON_GAS, 8));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "no quartz crucible -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(Semiconductor.QUARTZ_CRUCIBLE, 3));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).is(Semiconductor.SILICON_SEED_CRYSTAL), "polysilicon tip -> seed crystal (necking)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE).getCount(), 2, "crucible is used once (cracks on cooling)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR).getCount(), 7, "1 Ar for the seed");
        // 単結晶（ホウ素 → p型）
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED, c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).copy());
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        ItemStack poly = com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.polysilicon(com.taku3.semiconductorlab.Purity.ELEVEN_N);
        poly.setCount(4);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT, new ItemStack(Metallurgy.BORON_TRIOXIDE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "half power cannot keep 1420 C");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        ItemStack ing = c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT);
        h.assertTrue(ing.is(Semiconductor.SILICON_SINGLE_CRYSTAL) && ing.getCount() == 1, "2 polysilicon -> 1 ingot, got " + ing);
        h.assertValueEqual(ing.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "boron", "B2O3 -> p-type");
        h.assertValueEqual(com.taku3.semiconductorlab.Purity.nines(ing.get(com.taku3.semiconductorlab.ModDataComponents.PURITY)), 11, "purity from the polysilicon");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_SEED).is(Semiconductor.SILICON_SEED_CRYSTAL), "seed is reused");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT).isEmpty(), "dopant used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY).getCount(), 2, "2 polysilicon used");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR).getCount(), 5, "2 Ar for the ingot");
        // リン → n型、ドーパントなし → 真性
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_DOPANT, new ItemStack(Chemistry.PHOSPHORUS, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "phosphorus", "P -> n-type");
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_POLY, poly.copy());
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(Semiconductor.QUARTZ_CRUCIBLE, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).is(Semiconductor.SILICON_SINGLE_CRYSTAL) && c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).get(com.taku3.semiconductorlab.ModDataComponents.DOPANT) == null, "no dopant -> intrinsic");
        // アルゴンがなければ動かない
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_CRUCIBLE, new ItemStack(Semiconductor.QUARTZ_CRUCIBLE, 1));
        c.setItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_AR, ItemStack.EMPTY);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CzPullerBlockEntity.SLOT_OUT).isEmpty(), "no argon -> nothing");
        LOG.info("[LAB] CZ [9-08]: seed (necking), 2 poly + crucible + 2 Ar + B2O3/P -> p/n ingot, {} kWh/kg; no crucible / no Ar / half power rejected", kwhPerKg);
        h.succeed();
    }

    // =====================================================================
    // [9-11] ドーパント源（タスク23）
    // =====================================================================

    /** B2O3 → ホウ素（p型、k≈0.8）、白リン → リン（n型、k≈0.35）。ほかのアイテムはドーパントではない。ドーパントの印は保存と同期ができる */
    @GameTest
    public void dopantSources(GameTestHelper h) {
        var D = com.taku3.semiconductorlab.Dopant.class;
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.of(new ItemStack(Metallurgy.BORON_TRIOXIDE)), com.taku3.semiconductorlab.Dopant.BORON, "B2O3 is the boron source");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.of(new ItemStack(Chemistry.PHOSPHORUS)), com.taku3.semiconductorlab.Dopant.PHOSPHORUS, "white phosphorus is the P source");
        h.assertTrue(com.taku3.semiconductorlab.Dopant.of(new ItemStack(Metallurgy.BORAX)) == null, "borax itself is not a dopant (make B2O3 first)");
        h.assertTrue(com.taku3.semiconductorlab.Dopant.of(new ItemStack(Items.REDSTONE)) == null, "no vanilla stand-in");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.BORON.type, 'p', "B (group III) -> p-type");
        h.assertValueEqual(com.taku3.semiconductorlab.Dopant.PHOSPHORUS.type, 'n', "P (group V) -> n-type");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.Dopant.BORON.k - 0.8) < 1e-9 && Math.abs(com.taku3.semiconductorlab.Dopant.PHOSPHORUS.k - 0.35) < 1e-9, "segregation coefficients B 0.8, P 0.35");
        for (var d : com.taku3.semiconductorlab.Dopant.values()) h.assertValueEqual(com.taku3.semiconductorlab.Dopant.byId(d.id), d, "id round trip " + d);
        ItemStack s = new ItemStack(Metallurgy.POLYSILICON);
        s.set(com.taku3.semiconductorlab.ModDataComponents.DOPANT, "boron");
        h.assertValueEqual(s.get(com.taku3.semiconductorlab.ModDataComponents.DOPANT), "boron", "dopant component stored");
        LOG.info("[LAB] dopants [9-11]: B2O3 -> B (p, k={}), P4 -> P (n, k={}); As/Sb wait for [1-04]/[1-43]", com.taku3.semiconductorlab.Dopant.BORON.k, com.taku3.semiconductorlab.Dopant.PHOSPHORUS.k);
        h.succeed();
    }

    // =====================================================================
    // [9-07] Ge 単結晶の引き上げ（タスク14）
    // =====================================================================

    /** 種がない・水素がない・未精製では動かない。W線＋Ge1→種結晶（線は消える）、種結晶＋Ge4→単結晶（種は残る・純度と経路を受け継ぐ） */
    @GameTest
    public void crystalPuller(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Power.HF_OSCILLATOR, Power.INDUCTION_COIL, Metallurgy.GRAPHITE, Optics.QUARTZ_TUBE, Precision.LEAD_SCREW})
            h.assertTrue(recipeUses(h, "crystal_puller", it), "puller = induction heating + graphite crucible + pulling screw: " + it);
        floor(h, 5);
        BlockPos pp = p(1, 1, 1);
        h.setBlock(pp, Semiconductor.CRYSTAL_PULLER.block);
        var be = h.getBlockEntity(pp, com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.class);
        var c = be.getContainer();
        int GE = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_GE, SEED = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_SEED,
            H2 = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_H2, OUT = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SLOT_OUT;
        int TS = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.SEED_TICKS, TG = com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.GROW_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.WATTS);
        ItemStack ge = new ItemStack(Semiconductor.REFINED_GERMANIUM, 9);
        ge.set(com.taku3.semiconductorlab.ModDataComponents.PURITY, com.taku3.semiconductorlab.Purity.TEN_N);
        ge.set(com.taku3.semiconductorlab.ModDataComponents.CHLORIDE_ROUTE, true);
        h.assertFalse(com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.isGermanium(new ItemStack(Metallurgy.RAW_GERMANIUM)), "unrefined germanium is not accepted");
        c.setItem(GE, ge);
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_GAS, 2));
        tickConsumer(h, pp, be, TG + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no seed -> nothing grows");
        // ① 最初の種：タングステン線から（粒の選択）
        c.setItem(SEED, new ItemStack(Metallurgy.TUNGSTEN_WIRE, 1));
        tickConsumer(h, pp, be, TS, feed);
        h.assertTrue(c.getItem(OUT).is(Semiconductor.GERMANIUM_SEED_CRYSTAL), "W wire + 1 Ge -> seed crystal, got " + c.getItem(OUT));
        h.assertTrue(c.getItem(SEED).isEmpty(), "the wire is grown into the crystal");
        h.assertValueEqual(c.getItem(GE).getCount(), 8, "1 Ge used for the seed");
        h.assertValueEqual(c.getItem(H2).getCount(), 1, "1 bag of hydrogen per run");
        // ② 単結晶：種は戻る
        c.setItem(SEED, c.getItem(OUT).copy());
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, pp, be, TG, feed);
        ItemStack ingot = c.getItem(OUT);
        h.assertTrue(ingot.is(Semiconductor.GERMANIUM_SINGLE_CRYSTAL), "seed + 4 Ge -> single crystal, got " + ingot);
        h.assertValueEqual(ingot.get(com.taku3.semiconductorlab.ModDataComponents.PURITY), com.taku3.semiconductorlab.Purity.TEN_N, "purity carried over");
        h.assertTrue(Boolean.TRUE.equals(ingot.get(com.taku3.semiconductorlab.ModDataComponents.CHLORIDE_ROUTE)), "GeCl4 route carried over");
        h.assertTrue(c.getItem(SEED).is(Semiconductor.GERMANIUM_SEED_CRYSTAL), "the seed comes back");
        h.assertValueEqual(c.getItem(GE).getCount(), 4, "4 Ge per ingot");
        h.assertTrue(c.getItem(H2).isEmpty(), "hydrogen used up");
        // 水素がなければ止まる（酸化を防ぐ雰囲気）
        tickConsumer(h, pp, be, TG + 10, feed);
        h.assertValueEqual(ingot.getCount(), 1, "no hydrogen -> no second ingot");
        LOG.info("[LAB] crystal puller: W wire + Ge -> seed ({} ticks), seed + 4 Ge + H2 -> single crystal ({} ticks, 10N + route kept, seed returned), no H2 -> stop", TS, TG);
        h.succeed();
    }
    /** [3-14] 誘導加熱炉＝帯域精製炉＋高周波発振器（タスク12で ✅）：誘導コイル＋発振器＋電気を通さない石英管、燃料スロットなし・電力 3kW だけ */
    @GameTest
    public void inductionHeatingIsZoneRefiner(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Power.INDUCTION_COIL, Power.HF_OSCILLATOR, Optics.QUARTZ_TUBE})
            h.assertTrue(recipeUses(h, "zone_refining_furnace", it), "induction heating needs " + it);
        h.assertTrue(recipeUses(h, "induction_coil", Items.COPPER_INGOT), "the coil is copper [4-03]");
        h.assertValueEqual(ZoneRefiningFurnaceBlockEntity.WATTS, 3000.0, "the HF oscillator draws 3 kW");
        h.assertValueEqual(ZoneRefiningFurnaceBlockEntity.SLOT_COUNT, 3, "germanium, dopant, boat - no fuel slot (heat comes from eddy currents)");
        LOG.info("[LAB] induction heating [3-14]: zone refiner = copper coil + HF oscillator + quartz tube, 3 kW, no fuel");
        h.succeed();
    }

    // =====================================================================
    // 帯域精製炉の電力駆動（ROADMAP #9）とボート（粘土黒鉛＝9N、人造黒鉛＝10N）
    // =====================================================================

    /** 電気がないと止まる。ボートがないと動かない。粘土黒鉛ボートは 9N 止まり、人造黒鉛ボートに替えると 10N */
    @GameTest
    public void zoneRefinerPowerAndBoat(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Semiconductor.ZONE_REFINING_FURNACE.block);
        ZoneRefiningFurnaceBlockEntity be = h.getBlockEntity(pos, ZoneRefiningFurnaceBlockEntity.class);
        var c = be.getContainer();
        int T = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS, G = ZoneRefiningFurnaceBlockEntity.GE_SLOT, B = ZoneRefiningFurnaceBlockEntity.BOAT_SLOT;

        // ボートなし：電気があっても動かない（電気も使わない）
        c.setItem(G, rawChloride(2));   // 10N まで確かめるので GeCl4 経由の Ge
        tickRefiner(h, pos, be, T + 10);
        h.assertTrue(c.getItem(G).is(Metallurgy.RAW_GERMANIUM), "no boat -> no refining");
        h.assertValueEqual(be.getLastWatts(), 0.0, "no boat -> no power drawn");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 粘土黒鉛ボート＋電気なし：止まる
        c.setItem(B, new ItemStack(Semiconductor.GRAPHITE_BOAT));
        tickConsumer(h, pos, be, T + 10, 0);
        h.assertTrue(c.getItem(G).is(Metallurgy.RAW_GERMANIUM), "no power -> furnace stops");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 電気あり：6N → 9N、そこで止まる（粘土の不純物）
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "clay-graphite boat: raw -> 6N -> 9N");
        tickRefiner(h, pos, be, T * 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.NINE_N, "clay-graphite boat stops at 9N");
        h.assertValueEqual(be.getLastWatts(), 0.0, "idle at the cap -> no power drawn");
        // 人造黒鉛ボートに替える → 10N
        c.setItem(B, new ItemStack(Semiconductor.SYNTHETIC_GRAPHITE_BOAT));
        tickRefiner(h, pos, be, T);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.TEN_N, "synthetic graphite boat -> 10N");
        h.assertTrue(c.getItem(B).is(Semiconductor.SYNTHETIC_GRAPHITE_BOAT), "boat stays");
        // 途中で電気が切れると一時停止（進捗は残る）、戻ると続きから
        c.setItem(G, new ItemStack(Metallurgy.RAW_GERMANIUM, 1));
        tickRefiner(h, pos, be, T / 2);
        tickConsumer(h, pos, be, T, 0);
        h.assertTrue(c.getItem(G).is(Metallurgy.RAW_GERMANIUM), "paused while power is off");
        tickRefiner(h, pos, be, T - T / 2);
        h.assertValueEqual(purityOf(c.getItem(G)), Purity.SIX_N, "resumes when power returns");
        h.assertTrue(recipeUses(h, "synthetic_graphite_boat", Heat.SYNTHETIC_GRAPHITE), "synthetic boat is made from Acheson graphite");
        h.assertFalse(recipeUses(h, "zone_refining_furnace", Semiconductor.GRAPHITE_BOAT), "the boat now goes in its own slot, not in the furnace recipe");
        LOG.info("[LAB] zone refiner: needs boat + {} W; clay-graphite boat caps at 9N, synthetic graphite boat reaches 10N; pauses without power", ZoneRefiningFurnaceBlockEntity.WATTS);
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        double zt = ZoneRefiningFurnaceBlockEntity.TOTAL_TICKS / 20.0, zw = ZoneRefiningFurnaceBlockEntity.WATTS;
        rec(a, Semiconductor.ZONE_REFINING_FURNACE.block, "帯域精製（1パスで純度が1段上がる：6N→9N→10N）",
            L(max(io(Metallurgy.RAW_GERMANIUM, 1), 8), kept(cond(io(Semiconductor.GRAPHITE_BOAT, 1), "粘土黒鉛＝9Nまで、人造黒鉛＝10Nまで"))),
            L(io(Semiconductor.REFINED_GERMANIUM, 1)), zw, zt, "入れた数だけ出る。GeCl4 経由でない Ge は 9N まで");
        rec(a, Semiconductor.ZONE_REFINING_FURNACE.block, "ゾーンレベリング（n型）", L(max(cond(io(Semiconductor.REFINED_GERMANIUM, 1), "9N 以上"), 8), io(Chemistry.PHOSPHORUS, 1)),
            L(io(Semiconductor.N_TYPE_GERMANIUM, 1)), zw, zt, "入れた数だけ出る");
        rec(a, Semiconductor.ZONE_REFINING_FURNACE.block, "ゾーンレベリング（p型）", L(max(cond(io(Semiconductor.REFINED_GERMANIUM, 1), "9N 以上"), 8), io(Metallurgy.INDIUM, 1)),
            L(io(Semiconductor.P_TYPE_GERMANIUM, 1)), zw, zt, "入れた数だけ出る");
        rec(a, Semiconductor.CRYSTAL_PULLER.block, "種結晶づくり", L(io(Metallurgy.TUNGSTEN_WIRE, 1), io(Semiconductor.REFINED_GERMANIUM, CrystalPullerBlockEntity.GE_PER_SEED), io(Gas.HYDROGEN_GAS, CrystalPullerBlockEntity.H2_PER_RUN)),
            L(io(Semiconductor.GERMANIUM_SEED_CRYSTAL, 1)), CrystalPullerBlockEntity.WATTS, CrystalPullerBlockEntity.SEED_TICKS / 20.0, "粒の選択。線は結晶に埋まる");
        rec(a, Semiconductor.CRYSTAL_PULLER.block, "単結晶の引き上げ", L(kept(io(Semiconductor.GERMANIUM_SEED_CRYSTAL, 1)), io(Semiconductor.REFINED_GERMANIUM, CrystalPullerBlockEntity.GE_PER_INGOT), io(Gas.HYDROGEN_GAS, CrystalPullerBlockEntity.H2_PER_RUN)),
            L(io(Semiconductor.GERMANIUM_SINGLE_CRYSTAL, 1)), CrystalPullerBlockEntity.WATTS, CrystalPullerBlockEntity.GROW_TICKS / 20.0, "純度と経路を受け継ぐ");
        rec(a, Semiconductor.DIFFUSION_FURNACE.block, "p-n 接合", L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN), "n型 か p型"), cond(alt(io(Metallurgy.BORON_TRIOXIDE, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SOURCE_PER_RUN), Chemistry.PHOSPHORUS), "板と反対の型")),
            L(io(Semiconductor.PN_JUNCTION_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN)), com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS / 20.0, "ドーパントが表面からしみ込む（1954：n型＋ホウ素）");
        rec(a, Semiconductor.WAFER_SAW.block, "板に切る", L(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), perRuns(io(Semiconductor.DIAMOND_SAW_BLADE, 1), com.taku3.semiconductorlab.block.WaferSawBlockEntity.RUNS_PER_BLADE)),
            L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLICES_PER_INGOT), "純度とドーパントを受け継ぐ")), com.taku3.semiconductorlab.block.WaferSawBlockEntity.WATTS, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS / 20.0, "ダイヤモンドの円盤刃（1954 の作り方）");
        rec(a, Semiconductor.CZ_PULLER.block, "種結晶", L(cond(io(Metallurgy.POLYSILICON, 1), "種のスロットに"), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_SEED)),
            L(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS / 20.0, "細く絞って引くと1つの粒だけが残る");
        rec(a, Semiconductor.CZ_PULLER.block, "単結晶", L(kept(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), io(Metallurgy.POLYSILICON, com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_INGOT), cond(alt(io(Metallurgy.BORON_TRIOXIDE, 1), Chemistry.PHOSPHORUS), "入れなくてもよい（真性）")),
            L(cond(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), "B2O3 で p型・リンで n型、純度は多結晶シリコンと同じ")), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0, "約1420℃の融液から回しながら引き上げる。17.4kWh/kg");
    }
}
