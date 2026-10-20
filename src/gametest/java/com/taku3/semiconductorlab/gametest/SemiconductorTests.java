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
    // [9-15] 熱酸化（IMP4-01）
    // =====================================================================

    /** 板8＋酸素（ドライ）か水（ウェット）→ 酸化膜つきの板8。膜は Deal–Grove（ドライ約125nm・ウェット約660nm）、続けて焼くと伸びが鈍る。電気・材料なしでは進まない */
    @GameTest(maxTicks = 80)
    public void oxidationFurnace(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Power.ELECTRIC_FURNACE.block.asItem(), Optics.QUARTZ_TUBE, Heat.REFRACTORY_BRICK})
            h.assertTrue(recipeUses(h, "oxidation_furnace", it), "oxidation furnace = electric furnace [4-12] + quartz tube [7-02]: " + it);
        int dry = com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.grow(0, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.DRY, 1), wet = com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.grow(0, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.WET, 1);
        h.assertTrue(dry >= 115 && dry <= 135, "dry 1100C 1 h ~ 0.12 um (Deal-Grove), got " + dry);
        h.assertTrue(wet >= 620 && wet <= 700, "wet 1100C 1 h ~ 0.66 um, got " + wet);
        int dry2 = com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.grow(dry, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.DRY, 1);
        h.assertTrue(dry2 - dry < dry, "second hour grows less than the first (parabolic): " + dry + " -> " + dry2);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.grow(0, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.DRY, 2) - dry2) <= 1, "two runs = one 2 h run (nm rounding)");
        h.assertValueEqual(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.colorOf(dry), "blue", "~125 nm looks blue on the colour chart");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.OXIDATION_FURNACE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS);
        ItemStack w = new ItemStack(Semiconductor.SILICON_WAFER, 16);
        w.set(ModDataComponents.PURITY, Purity.NINE_N);
        w.set(ModDataComponents.DOPANT, "boron");
        c.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_WAFER, w);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS + 10, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT).isEmpty(), "no oxygen / water -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT, new ItemStack(Gas.OXYGEN_GAS, 2));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS, feed);
        ItemStack o = c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(o.is(Semiconductor.OXIDIZED_WAFER) && o.getCount() == com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WAFERS_PER_RUN, "8 slices + O2 -> 8 oxidized, got " + o);
        h.assertValueEqual(o.get(ModDataComponents.OXIDE_NM), dry, "dry film thickness");
        h.assertValueEqual(o.get(ModDataComponents.DOPANT), "boron", "dopant kept");
        h.assertValueEqual(Purity.nines(o.get(ModDataComponents.PURITY)), 9, "purity kept");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT).getCount(), 1, "1 bag of O2 used");
        // ウェット：酸化膜つきの板を水蒸気でもう一度 → 続きから厚くなり、空のバケツが戻る
        c.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_WAFER, c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT).copy());
        c.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT, new ItemStack(Items.WATER_BUCKET));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS + 10, feed);
        ItemStack o2 = c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT);
        h.assertValueEqual(o2.get(ModDataComponents.OXIDE_NM), com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.grow(dry, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.WET, 1), "wet continues from the dry film");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_BUCKET).is(Items.BUCKET), "empty bucket returned");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT).isEmpty(), "water used");
        // 隣のガスホルダーから酸素をもらう
        h.setBlock(p(2, 1, 1), Gas.GAS_HOLDER.block);
        h.getBlockEntity(p(2, 1, 1), GasHolderBlockEntity.class).setContentsForTest(GasType.OXYGEN, 20);
        int finalDry = dry, wetNm = o2.get(ModDataComponents.OXIDE_NM);
        h.runAfterDelay(25, () -> {   // 機械はふつうに tick する（10 tick に1回ホルダーから補充）
            ItemStack ox = c.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT);
            h.assertTrue(ox.is(Gas.OXYGEN_GAS) && ox.getCount() == 16, "pulls O2 from the gas holder up to 16, got " + ox);
            LOG.info("[LAB] oxidation [9-15]: 8 slices + O2 -> {} nm (dry 1 h), + water -> {} nm (wet, continues), 2nd dry hour -> {} nm; pulls O2 from a gas holder", finalDry, wetNm, dry2);
            h.succeed();
        });
    }

    // =====================================================================
    // [9-06] 酸化膜エッチング（IMP4-01）
    // =====================================================================

    /** 酸化膜つきの板8＋フッ酸 → シリコンの板8（純度・ドーパントを受け継ぐ）。時間は膜の厚さ÷速さ、鉛の瓶が上に戻る。フッ酸・板が足りないと動かない。槽は鉛 */
    @GameTest(maxTicks = 200)
    public void etchTank(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "etch_tank", Metallurgy.LEAD_INGOT), "HF tank is lead");
        h.assertFalse(recipeUses(h, "etch_tank", Chemistry.SODA_LIME_GLASS) || recipeUses(h, "etch_tank", Items.GLASS), "no glass in an HF tank");
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(125) > com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(60) && com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(660) > com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(125), "thicker oxide takes longer");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.ETCH_TANK.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.EtchTankBlockEntity.class);
        var c = be.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(fp);
        Runnable tick = () -> com.taku3.semiconductorlab.block.EtchTankBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 16);
        w.set(ModDataComponents.PURITY, Purity.NINE_N);
        w.set(ModDataComponents.DOPANT, "phosphorus");
        w.set(ModDataComponents.OXIDE_NM, 125);
        c.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER, w);
        int t = com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(125);
        for (int i = 0; i < t + 5; i++) tick.run();
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).isEmpty(), "no HF -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(Chemistry.HYDROFLUORIC_ACID, 2));
        for (int i = 0; i < t - 1; i++) tick.run();
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).isEmpty(), "not done 1 tick early");
        tick.run();
        ItemStack o = c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT);
        h.assertTrue(o.is(Semiconductor.SILICON_WAFER) && o.getCount() == com.taku3.semiconductorlab.block.EtchTankBlockEntity.WAFERS_PER_RUN, "8 oxidized -> 8 bare slices, got " + o);
        h.assertTrue(o.get(ModDataComponents.OXIDE_NM) == null, "oxide gone");
        h.assertValueEqual(o.get(ModDataComponents.DOPANT), "phosphorus", "dopant kept");
        h.assertValueEqual(Purity.nines(o.get(ModDataComponents.PURITY)), 9, "purity kept");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF).getCount(), 1, "1 HF per 8 slices");
        var drops = lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(a).inflate(2));
        h.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(Metallurgy.LEAD_BOTTLE)), "empty lead bottle pops out");
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        // 板が7枚では動かない
        c.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER).setCount(7);
        for (int i = 0; i < t + 5; i++) tick.run();
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).isEmpty(), "7 slices are not a batch");
        // 酸化炉 → エッチング槽 → また酸化炉に入る（板に戻る）
        h.assertTrue(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.isWafer(o), "stripped slice can be oxidized again");
        LOG.info("[LAB] etch [9-06]: 8 oxidized slices (125 nm) + HF -> 8 bare slices in {} ticks (660 nm: {} ticks); lead bottle returned; purity/dopant kept", t, com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(660));
        h.succeed();
    }

    /** [9-06]・[9-15] を露光の工程につなぐ（IMP4-01、LITHO_DESIGN §5）：現像ずみの加工中ウェハー → エッチング槽で窓が開く（ChipOps.etchOxide と同じ）→ はがす → 酸化炉で窓が閉じる（reoxidize）。8×8 の NOT の例は槽を通しても4つの発見になる */
    @GameTest(maxTicks = 200)
    public void etchAndReoxidizeWindows(GameTestHelper h) {
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack wafer = new ItemStack(Semiconductor.OXIDIZED_WAFER, 4);
        wafer.set(ModDataComponents.DOPANT, "boron");
        wafer.set(ModDataComponents.OXIDE_NM, 600);
        ItemStack dev = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(wafer).out(), design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.isInput(dev) && com.taku3.semiconductorlab.block.EtchTankBlockEntity.batch(dev) == 4, "developed wafers go into the etching tank (1-8 at a time)");
        h.assertFalse(com.taku3.semiconductorlab.block.EtchTankBlockEntity.isInput(com.taku3.semiconductorlab.litho.ChipOps.coat(wafer).out()), "coated but not developed -> refused");
        floor(h, 3);
        BlockPos tp = p(1, 1, 1);
        h.setBlock(tp, Semiconductor.ETCH_TANK.block);
        var tank = h.getBlockEntity(tp, com.taku3.semiconductorlab.block.EtchTankBlockEntity.class);
        var tc = tank.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos ta = h.absolutePos(tp);
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER, dev.copy());
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(Chemistry.HYDROFLUORIC_ACID, 1));
        for (int i = 0; i < com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(600) + 2; i++) com.taku3.semiconductorlab.block.EtchTankBlockEntity.tick(lv, ta, lv.getBlockState(ta), tank);
        ItemStack etched = tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT);
        h.assertTrue(etched.getCount() == 4 && ItemStack.isSameItemSameComponents(etched, com.taku3.semiconductorlab.litho.ChipOps.etchOxide(dev).out()), "tank = ChipOps.etchOxide, got " + etched);
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(etched).hasWindows(), "windows are open");
        h.assertTrue(tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF).isEmpty() && tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER).isEmpty(), "1 HF for the batch");
        lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(ta).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        // はがして酸化炉へ：窓が閉じ、膜が育つ
        ItemStack stripped = com.taku3.semiconductorlab.litho.ChipOps.strip(etched).out();
        h.assertTrue(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.isWafer(stripped) && com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.batch(stripped) == 4, "stripped wafer with windows goes into the oxidation furnace");
        BlockPos fp = p(1, 1, 2);
        h.setBlock(fp, Semiconductor.OXIDATION_FURNACE.block);
        var furnace = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.class);
        var fc = furnace.getContainer();
        fc.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_WAFER, stripped.copy());
        fc.setItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT, new ItemStack(com.taku3.semiconductorlab.content.Gas.OXYGEN_GAS, 1));
        tickConsumer(h, fp, furnace, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS));
        ItemStack closed = fc.getItem(com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(closed.is(Semiconductor.PROCESS_WAFER) && closed.getCount() == 4 && !com.taku3.semiconductorlab.litho.ChipOps.state(closed).hasWindows(), "reoxidize closes the windows, got " + closed);
        h.assertTrue(closed.get(ModDataComponents.OXIDE_NM) > 600, "and the oxide grows");
        // 全部の工程（エッチングはこの槽と同じ ChipOps）で NOT の例が4つの発見になる
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(wafer, design, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(done.get(ModDataComponents.CHIP_STATE)))).size(), 4, "NOT example through all steps");
        LOG.info("[LAB] litho hookup [9-06]/[9-15]: etching tank opens windows on 4 developed wafers (= ChipOps.etchOxide), oxidation furnace closes them (reoxidize) and grows the oxide");
        h.succeed();
    }

    // =====================================================================
    // [9-25] 金属配線（Al 蒸着・リン酸で削る）（IMP4-01）
    // =====================================================================

    /** NOT の例を、拡散と窓までは ChipOps で、Al を付ける工程は真空蒸着装置、Al を削る工程はエッチング槽（リン酸）で通す → 4つの発見。リン酸は骨灰＋硫酸 */
    @GameTest(maxTicks = 200)
    public void aluminiumWiring(GameTestHelper h) {
        // リン酸：骨灰1＋硫酸3＋空のガラス瓶2 → リン酸2（硫酸の瓶3本が戻る）
        List<ItemStack> grid = new ArrayList<>(List.of(new ItemStack(Metallurgy.BONE_ASH), new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.SULFURIC_ACID),
                new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.GLASS_BOTTLE), new ItemStack(Chemistry.GLASS_BOTTLE), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid);
        var rr = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel()).orElseThrow().value();
        ItemStack acid = rr.assemble(input);
        h.assertTrue(acid.is(Chemistry.PHOSPHORIC_ACID) && acid.getCount() == 2, "bone ash + 3 H2SO4 + 2 bottles -> 2 H3PO4, got " + acid);
        h.assertValueEqual((int) rr.getRemainingItems(input).stream().filter(st -> st.is(Chemistry.GLASS_BOTTLE)).count(), 3, "3 sulfuric acid bottles come back");
        // 窓（CONTACT）まで：ChipOps
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        for (int i = 0; i < 4; i++) {
            w = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.expose(w, d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
            if (d.layers().get(i).kind().isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, d.layers().get(i).kind().dopes).out();
        }
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w).hasWindows() && com.taku3.semiconductorlab.block.EvaporatorBlockEntity.isSubstrate(w), "contact windows open -> goes into the evaporator");
        // Al を一面に：真空蒸着装置（0.004Pa）
        floor(h, 5);
        BlockPos cp = p(1, 1, 1), ep = p(2, 1, 1), tp = p(3, 1, 3);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ep, com.taku3.semiconductorlab.content.Vacuum.EVAPORATOR.block);
        var ch = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class);
        var ev = h.getBlockEntity(ep, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.class);
        ev.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, w.copy());
        ev.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL, new ItemStack(Metallurgy.ALUMINIUM_INGOT, 2));
        ch.setPressure(50);
        tickConsumer(h, ep, ev, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS + 5, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS));
        h.assertTrue(ev.getContainer().getItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT).isEmpty(), "50 Pa: atoms collide, no film");
        ch.setPressure(0.004);
        tickConsumer(h, ep, ev, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS));
        ItemStack metal = ev.getContainer().getItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT);
        h.assertTrue(metal.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(metal).metalFilm(), "8 wafers covered with Al in one run, got " + metal);
        h.assertValueEqual(ev.getContainer().getItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL).getCount(), 1, "1 aluminium per run");
        h.assertTrue(ItemStack.isSameItemSameComponents(metal, com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out()), "evaporator = ChipOps.depositMetal");
        // 配線の模様：塗布・露光・現像 → エッチング槽（リン酸）
        ItemStack m = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(metal).out(), d.mask(4), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.isInput(m), "developed Al wafer goes into the etching tank");
        h.setBlock(tp, Semiconductor.ETCH_TANK.block);
        var tank = h.getBlockEntity(tp, com.taku3.semiconductorlab.block.EtchTankBlockEntity.class);
        var tc = tank.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos ta = h.absolutePos(tp);
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER, m.copy());
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(Chemistry.HYDROFLUORIC_ACID));
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.product(m, 8, new ItemStack(Chemistry.HYDROFLUORIC_ACID)).isEmpty(), "HF cannot etch the Al pattern (metal on)");
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(Chemistry.PHOSPHORIC_ACID));
        for (int i = 0; i < com.taku3.semiconductorlab.block.EtchTankBlockEntity.METAL_TICKS; i++) com.taku3.semiconductorlab.block.EtchTankBlockEntity.tick(lv, ta, lv.getBlockState(ta), tank);
        ItemStack wired = tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT);
        h.assertTrue(wired.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(wired).has(com.taku3.semiconductorlab.litho.LayerKind.METAL), "phosphoric acid leaves the Al wiring, got " + wired);
        lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(ta).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        ItemStack chip = com.taku3.semiconductorlab.litho.ChipOps.strip(wired).out();
        var found = com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(chip.get(ModDataComponents.CHIP_STATE))));
        h.assertValueEqual(found.size(), 4, "wired chip = NOT example (4 discoveries)");
        LOG.info("[LAB] Al wiring [9-25]: bone ash + H2SO4 -> H3PO4; evaporator puts Al on 8 wafers (0.004 Pa, not 50 Pa); H3PO4 in the etching tank leaves the wiring; NOT example -> {} discoveries", found.size());
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

    // =====================================================================
    // 露光とマスク（O-002、docs/LITHO_DESIGN.md）
    // =====================================================================

    /**
     * 中心の仕組み：8×8 の NOT の例を、工程の関数（ChipOps）で順に焼くと、抵抗・ダイオード・トランジスタ・NOT が見つかる。
     * 予想（設計図から）と本物の工程の結果が同じ。NOT は計算しても入力 0 → 出力 高、入力 1 → 出力 低。
     * まちがった順の工程は断られ、ウェハーは失われない。方眼の大きさの表は式どおり。
     */
    @GameTest
    public void lithoCore(GameTestHelper h) {
        // 方眼の大きさ：N = 8 × (10 µm ÷ CD)^(1/3) を 4 の倍数に
        for (var t : com.taku3.semiconductorlab.litho.ExposureTool.values())
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(t.cdNm), t.grid, "grid size of " + t);
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        // 予想（設計図から）
        var predicted = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(design, "p"));
        var ids = com.taku3.semiconductorlab.litho.Discoveries.match(predicted).stream().map(com.taku3.semiconductorlab.litho.Discoveries.Discovery::id).toList();
        h.assertTrue(ids.equals(java.util.List.of("diffused_resistor", "junction_diode", "planar_transistor", "rtl_not")), "8x8 example finds all four: " + ids);
        int[] pins = com.taku3.semiconductorlab.litho.Circuit.notPins(predicted);
        double hi = com.taku3.semiconductorlab.litho.Circuit.notOutput(predicted, pins, false), lo = com.taku3.semiconductorlab.litho.Circuit.notOutput(predicted, pins, true);
        // 出力の低い方は約 1 V：島（コレクタ）の抵抗が大きい（埋め込み層のない最初のころの IC と同じ）
        h.assertTrue(hi > 4.5 && lo < 1.5, String.format("NOT computes: in 0 -> %.2f V, in 1 -> %.2f V", hi, lo));
        // 本物の工程（p型の酸化膜つきの板から）
        ItemStack wafer = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        wafer.set(ModDataComponents.DOPANT, "boron");
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(wafer, design, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertTrue(done.is(Semiconductor.PROCESS_WAFER) && done.getCount() == 8, "8 process wafers come out");
        var made = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(done.get(ModDataComponents.CHIP_STATE)));
        h.assertTrue(com.taku3.semiconductorlab.litho.Discoveries.match(made).size() == 4, "processed chip = predicted chip");
        h.assertValueEqual(wafer.getCount(), 8, "the input stack is not changed");
        // まちがった順・合わない物は断られる（ウェハーはそのまま）
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.expose(wafer, design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).reason(), "no_resist", "expose before coating");
        ItemStack undoped = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.coat(undoped).reason(), "undoped", "undoped wafer");
        ItemStack coated = com.taku3.semiconductorlab.litho.ChipOps.coat(wafer).out();
        var fine = new com.taku3.semiconductorlab.litho.MaskData(12, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(12), "", 0);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.expose(coated, fine, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).reason(), "too_fine", "12x12 mask with contact printing (8x8)");
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.expose(coated, fine, com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X).ok(), "12x12 mask with 1:1 projection");
        ItemStack exposed = com.taku3.semiconductorlab.litho.ChipOps.expose(coated, design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
        ItemStack redo = com.taku3.semiconductorlab.litho.ChipOps.strip(exposed).out();
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.canCoat(redo), "strip resist and coat again (rework)");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.diffuse(redo, 'n').reason(), "no_window", "diffuse without windows");
        // 研磨してやり直し（2回まで）
        ItemStack r1 = com.taku3.semiconductorlab.litho.ChipOps.reclaim(done).out(), r2 = com.taku3.semiconductorlab.litho.ChipOps.reclaim(r1).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.reclaim(r2).reason(), "too_many_reclaims", "reclaim twice at most");
        LOG.info("[LAB] litho core: 8x8 NOT example -> {}; NOT in 0 -> {} V, in 1 -> {} V", ids, String.format("%.2f", hi), String.format("%.2f", lo));
        h.succeed();
    }

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
        rec(a, Semiconductor.OXIDATION_FURNACE.block, "ドライ酸化", L(cond(alt(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WAFERS_PER_RUN), Semiconductor.OXIDIZED_WAFER), "酸化膜つきなら続きから"), mol(Gas.OXYGEN_GAS, 1)),
            L(cond(io(Semiconductor.OXIDIZED_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WAFERS_PER_RUN), "膜 約125nm（1100℃・1時間）")), com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS / 20.0, "Si + O2 → SiO2。Deal–Grove の式");
        rec(a, Semiconductor.OXIDATION_FURNACE.block, "ウェット酸化", L(cond(alt(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WAFERS_PER_RUN), Semiconductor.OXIDIZED_WAFER), "酸化膜つきなら続きから"), io(Items.WATER_BUCKET, 1)),
            L(cond(io(Semiconductor.OXIDIZED_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WAFERS_PER_RUN), "膜 約660nm（1100℃・1時間）"), io(Items.BUCKET, 1)), com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS / 20.0, "Si + 2H2O → SiO2 + 2H2。水蒸気は膜を速く通る");
        rec(a, Semiconductor.ETCH_TANK.block, "酸化膜をはがす", L(cond(io(Semiconductor.OXIDIZED_WAFER, com.taku3.semiconductorlab.block.EtchTankBlockEntity.WAFERS_PER_RUN), "時間は膜の厚さ（1秒に20nm）"), io(Chemistry.HYDROFLUORIC_ACID, 1)),
            L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.EtchTankBlockEntity.WAFERS_PER_RUN), "純度とドーパントを受け継ぐ"), io(Metallurgy.LEAD_BOTTLE, 1)), 0, com.taku3.semiconductorlab.block.EtchTankBlockEntity.ticksFor(125) / 20.0, "SiO2 + 6HF → H2SiF6 + 2H2O。シリコンは溶けない。室温・電気なし（125nm の膜の時間）");
        rec(a, Semiconductor.WAFER_SAW.block, "板に切る", L(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), perRuns(io(Semiconductor.DIAMOND_SAW_BLADE, 1), com.taku3.semiconductorlab.block.WaferSawBlockEntity.RUNS_PER_BLADE)),
            L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLICES_PER_INGOT), "純度とドーパントを受け継ぐ")), com.taku3.semiconductorlab.block.WaferSawBlockEntity.WATTS, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS / 20.0, "ダイヤモンドの円盤刃（1954 の作り方）");
        rec(a, Semiconductor.CZ_PULLER.block, "種結晶", L(cond(io(Metallurgy.POLYSILICON, 1), "種のスロットに"), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_SEED)),
            L(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS / 20.0, "細く絞って引くと1つの粒だけが残る");
        rec(a, Semiconductor.CZ_PULLER.block, "単結晶", L(kept(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), io(Metallurgy.POLYSILICON, com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_INGOT), cond(alt(io(Metallurgy.BORON_TRIOXIDE, 1), Chemistry.PHOSPHORUS), "入れなくてもよい（真性）")),
            L(cond(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), "B2O3 で p型・リンで n型、純度は多結晶シリコンと同じ")), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0, "約1420℃の融液から回しながら引き上げる。17.4kWh/kg");
    }
}
