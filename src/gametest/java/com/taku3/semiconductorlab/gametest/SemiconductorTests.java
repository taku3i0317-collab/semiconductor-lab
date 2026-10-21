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
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(done.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "NOT example through all steps");
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
        h.assertValueEqual(found.size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "wired chip = NOT example (all discoveries)");
        LOG.info("[LAB] Al wiring [9-25]: bone ash + H2SO4 -> H3PO4; evaporator puts Al on 8 wafers (0.004 Pa, not 50 Pa); H3PO4 in the etching tank leaves the wiring; NOT example -> {} discoveries", found.size());
        h.succeed();
    }

    // =====================================================================
    // [9-13] ラッピング・鏡面研磨（やり直し）（IMP4-01）
    // =====================================================================

    /** 砂＋NaOH2 → 水ガラス、＋硫酸＋瓶 → コロイダルシリカ。研磨機：加工中のウェハー＋砥粒＋シリカ → 層を消してやり直し（2回まで） */
    @GameTest(maxTicks = 200)
    public void waferPolisher(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sodium_silicate", Items.SAND) && recipeUses(h, "sodium_silicate", com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE), "water glass = sand + NaOH");
        h.assertTrue(recipeUses(h, "colloidal_silica", Chemistry.SODIUM_SILICATE) && recipeUses(h, "colloidal_silica", Chemistry.SULFURIC_ACID), "silica sol = water glass + acid");
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(withDopant(new ItemStack(Semiconductor.OXIDIZED_WAFER, 3), "boron"), com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.WAFER_POLISHER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_WAFER, done.copy());
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_ABRASIVE, new ItemStack(Metallurgy.ALUMINA, 3));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT).isEmpty(), "no colloidal silica -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_SILICA, new ItemStack(Chemistry.COLLOIDAL_SILICA, 3));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.TICKS, feed);
        ItemStack r1 = c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT);
        h.assertTrue(r1.getCount() == 3 && com.taku3.semiconductorlab.litho.ChipOps.state(r1).done().isEmpty() && com.taku3.semiconductorlab.litho.ChipOps.state(r1).reclaims() == 1, "3 wafers reclaimed once, got " + r1);
        h.assertTrue(ItemStack.isSameItemSameComponents(r1, com.taku3.semiconductorlab.litho.ChipOps.reclaim(done).out()), "polisher = ChipOps.reclaim");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_ABRASIVE).getCount() == 2 && c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_SILICA).getCount() == 2, "1 abrasive + 1 silica per run");
        // 2回目でおしまい
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_WAFER, r1.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.TICKS, feed);
        ItemStack r2 = c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT);
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_WAFER, r2.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferPolisherBlockEntity.SLOT_OUT).isEmpty(), "third reclaim refused");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] polisher [9-13]: NOT chip -> lapping (alumina) + colloidal silica -> layers removed (reclaim), twice at most");
        h.succeed();
    }

    private static ItemStack withDopant(ItemStack s, String d) { s.set(ModDataComponents.DOPANT, d); return s; }

    // =====================================================================
    // [9-14] 洗浄 段階1（IMP4-01）
    // =====================================================================

    /** NOT の例を、層ごとに「はがす → 洗浄槽で洗う」を入れて通す（汚れは毎回 0 に戻る）→ 4つの発見。レジストが付いていると洗えない、薬品・電気がないと止まる */
    @GameTest(maxTicks = 300)
    public void cleaningBench(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.CLEANING_BENCH.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WATTS);
        ServerLevel lv = h.getLevel();
        java.util.function.UnaryOperator<ItemStack> wash = wf -> {
            c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT, ItemStack.EMPTY);
            c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WAFER, wf.copy());
            c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_NITRIC, new ItemStack(com.taku3.semiconductorlab.content.Gas.NITRIC_ACID));
            c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_HF, new ItemStack(Chemistry.HYDROFLUORIC_ACID));
            c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WATER, new ItemStack(Chemistry.DISTILLED_WATER));
            tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.TICKS, feed);
            return c.getItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT).copy();
        };
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        int maxDirt = 0;
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            w = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
            if (i == 0) h.assertTrue(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.product(w).isEmpty(), "resist on -> cannot clean");
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(w, d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.etchMetal(w).out(); else w = com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
            if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
            else if (k != com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
            maxDirt = Math.max(maxDirt, com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination());
            ItemStack cleaned = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination() <= com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.STAGE1_TARGET ? w.copy() : wash.apply(w);
            h.assertTrue(cleaned.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(cleaned).contamination() <= com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.STAGE1_TARGET, "layer " + i + ": stage-1 bench cleans 8 wafers to <= 1, got " + cleaned);
            w = cleaned;
        }
        h.assertTrue(maxDirt > 0, "dirt builds up between cleanings");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(w.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "cleaned NOT example still gives all discoveries");
        // 薬品なしでは動かない
        c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        ItemStack dirty = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out()).out();
        c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WAFER, dirty.copy());
        c.setItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_NITRIC, ItemStack.EMPTY);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT).isEmpty(), "no nitric acid -> nothing");
        lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] cleaning [9-14] stage 1: nitric + HF + distilled water clean 8 wafers to dirt 0 after every layer (max dirt {}); NOT example -> all discoveries", maxDirt);
        h.succeed();
    }

    // =====================================================================
    // [9-17] メサトランジスタ（IMP4-01）
    // =====================================================================

    /** p の板＋n の表面 → Al の点（蒸着）→ 焼いて合金 → ろう → 混酸で削る → 金線で4個。n の板は断る。β は持ち歩くと下がり、雨では速く、0 で止まる */
    @GameTest(maxTicks = 200)
    public void mesaTransistor(GameTestHelper h) {
        ItemStack pn = new ItemStack(Semiconductor.PN_JUNCTION_WAFER, 8);
        pn.set(ModDataComponents.DOPANT, "boron");
        ItemStack wrong = new ItemStack(Semiconductor.PN_JUNCTION_WAFER, 8);
        wrong.set(ModDataComponents.DOPANT, "phosphorus");
        h.assertTrue(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.chipProduct(wrong, new ItemStack(Metallurgy.ALUMINIUM_INGOT)).isEmpty(), "n base + p top: aluminium (p-type) makes no emitter -> refused");
        floor(h, 5);
        BlockPos cp = p(1, 1, 1), ep = p(2, 1, 1), tp = p(3, 1, 3);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ep, com.taku3.semiconductorlab.content.Vacuum.EVAPORATOR.block);
        h.getBlockEntity(cp, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class).setPressure(0.004);
        var ev = h.getBlockEntity(ep, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.class);
        ev.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, pn.copy());
        ev.getContainer().setItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL, new ItemStack(Metallurgy.ALUMINIUM_INGOT));
        tickConsumer(h, ep, ev, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS));
        ItemStack dotted = ev.getContainer().getItem(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT);
        h.assertTrue(dotted.is(Semiconductor.AL_DOTTED_WAFER) && dotted.getCount() == 8 && "boron".equals(dotted.get(ModDataComponents.DOPANT)), "8 Al-dotted slices keeping the dopant, got " + dotted);
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Semiconductor.AL_DOTTED_WAFER).is(Semiconductor.ALLOYED_WAFER), "furnace alloys the dots (577 C eutectic)");
        h.assertTrue(recipeUses(h, "waxed_wafer", Items.HONEYCOMB) && recipeUses(h, "silicon_etch", Chemistry.HYDROFLUORIC_ACID) && recipeUses(h, "silicon_etch", com.taku3.semiconductorlab.content.Gas.NITRIC_ACID), "wax mask, HF + HNO3");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.SILICON_ETCH) == com.taku3.semiconductorlab.machine.Vessel.LEAD_BOTTLE, "mixed acid with HF -> lead bottle");
        // 混酸のエッチング槽
        h.setBlock(tp, Semiconductor.ETCH_TANK.block);
        var tank = h.getBlockEntity(tp, com.taku3.semiconductorlab.block.EtchTankBlockEntity.class);
        var tc = tank.getContainer();
        ServerLevel lv = h.getLevel();
        BlockPos ta = h.absolutePos(tp);
        h.assertTrue(com.taku3.semiconductorlab.block.EtchTankBlockEntity.product(new ItemStack(Semiconductor.WAXED_WAFER, 8), 8, new ItemStack(Chemistry.HYDROFLUORIC_ACID)).isEmpty(), "HF alone does not cut silicon");
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER, new ItemStack(Semiconductor.WAXED_WAFER, 8));
        tc.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(Chemistry.SILICON_ETCH));
        for (int i = 0; i < com.taku3.semiconductorlab.block.EtchTankBlockEntity.METAL_TICKS; i++) com.taku3.semiconductorlab.block.EtchTankBlockEntity.tick(lv, ta, lv.getBlockState(ta), tank);
        h.assertTrue(tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).is(Semiconductor.MESA_WAFER) && tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).getCount() == 8, "8 mesa slices, got " + tc.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT));
        lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(ta).inflate(3)).forEach(net.minecraft.world.entity.Entity::discard);
        // 組み立て
        List<ItemStack> g = new ArrayList<>(List.of(new ItemStack(Semiconductor.MESA_WAFER), new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.IRON_NUGGET)));
        while (g.size() < 9) g.add(ItemStack.EMPTY);
        var in = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, g);
        ItemStack tr = lv.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, lv).orElseThrow().value().assemble(in);
        h.assertTrue(tr.is(Semiconductor.MESA_TRANSISTOR) && tr.getCount() == 4 && com.taku3.semiconductorlab.item.MesaTransistorItem.gainOf(tr) == com.taku3.semiconductorlab.item.MesaTransistorItem.FRESH_GAIN, "4 mesa transistors, beta 40, got " + tr);
        // 劣化：乾いた所で1、雨で5、0 で止まる
        ItemStack t1 = tr.copyWithCount(1);
        com.taku3.semiconductorlab.item.MesaTransistorItem.decay(t1, false);
        h.assertValueEqual(com.taku3.semiconductorlab.item.MesaTransistorItem.gainOf(t1), com.taku3.semiconductorlab.item.MesaTransistorItem.FRESH_GAIN - 1, "dry: beta -1");
        com.taku3.semiconductorlab.item.MesaTransistorItem.decay(t1, true);
        h.assertValueEqual(com.taku3.semiconductorlab.item.MesaTransistorItem.gainOf(t1), com.taku3.semiconductorlab.item.MesaTransistorItem.FRESH_GAIN - 1 - com.taku3.semiconductorlab.item.MesaTransistorItem.RAIN_DECAY, "rain: beta -5");
        for (int i = 0; i < 100; i++) com.taku3.semiconductorlab.item.MesaTransistorItem.decay(t1, true);
        h.assertValueEqual(com.taku3.semiconductorlab.item.MesaTransistorItem.gainOf(t1), 0, "leaks too much: beta 0, not negative");
        LOG.info("[LAB] mesa [9-17]: p+n slice -> Al dots -> alloy -> wax -> HF+HNO3 -> mesa -> 4 transistors (beta 40), decays 1/min dry, 5/min wet, n-base refused");
        h.succeed();
    }

    // =====================================================================
    // [9-09] FZ 法（IMP4-01）
    // =====================================================================

    /** 多結晶2＋種（減らない）＋Ar2（＋リン）→ 単結晶（酸素 0.05 ppma、るつぼを使わない）。ウェハー切断で酸素を受け継ぐ。種・Ar・電気がないと止まる */
    @GameTest(maxTicks = 100)
    public void fzPuller(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "fz_puller", com.taku3.semiconductorlab.content.Power.INDUCTION_COIL) && recipeUses(h, "fz_puller", com.taku3.semiconductorlab.content.Power.HF_OSCILLATOR), "induction heating [9-01]");
        h.assertFalse(com.taku3.semiconductorlab.block.FzPullerBlockEntity.LAYOUT.slots().stream().anyMatch(sl -> !sl.isOutput() && sl.accepts().test(new ItemStack(Semiconductor.QUARTZ_CRUCIBLE))), "no crucible slot");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.FZ_PULLER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.FzPullerBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.FzPullerBlockEntity.WATTS);
        ItemStack poly = new ItemStack(Metallurgy.POLYSILICON, 4);
        poly.set(ModDataComponents.PURITY, Purity.ELEVEN_N);
        c.setItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_POLY, poly);
        c.setItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_AR, new ItemStack(com.taku3.semiconductorlab.content.Gas.ARGON_GAS, 4));
        c.setItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_DOPANT, new ItemStack(Chemistry.PHOSPHORUS));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.FzPullerBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_OUT).isEmpty(), "no seed crystal -> nothing");
        c.setItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_SEED, new ItemStack(Semiconductor.SILICON_SEED_CRYSTAL));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.FzPullerBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.FzPullerBlockEntity.TICKS, feed);
        ItemStack x = c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_OUT);
        h.assertTrue(x.is(Semiconductor.SILICON_SINGLE_CRYSTAL) && x.getCount() == 1, "1 FZ crystal, got " + x);
        h.assertTrue(x.get(ModDataComponents.OXYGEN_PPMA) != null && x.get(ModDataComponents.OXYGEN_PPMA) < 0.1, "FZ oxygen < 0.1 ppma");
        h.assertValueEqual(x.get(ModDataComponents.DOPANT), "phosphorus", "n-type");
        h.assertValueEqual(Purity.nines(x.get(ModDataComponents.PURITY)), 11, "purity kept");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_SEED).getCount() == 1 && c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_POLY).getCount() == 2 && c.getItem(com.taku3.semiconductorlab.block.FzPullerBlockEntity.SLOT_AR).getCount() == 2, "seed kept, 2 poly + 2 Ar used");
        // ウェハー切断で酸素を受け継ぐ
        BlockPos sp = p(2, 1, 2);
        h.setBlock(sp, Semiconductor.WAFER_SAW.block);
        var saw = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.WaferSawBlockEntity.class);
        saw.getContainer().setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_CRYSTAL, x.copy());
        saw.getContainer().setItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_BLADE, new ItemStack(Semiconductor.DIAMOND_SAW_BLADE));
        tickConsumer(h, sp, saw, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.WaferSawBlockEntity.WATTS));
        ItemStack wafers = saw.getContainer().getItem(com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLOT_OUT);
        h.assertTrue(wafers.is(Semiconductor.SILICON_WAFER) && wafers.get(ModDataComponents.OXYGEN_PPMA) != null && wafers.get(ModDataComponents.OXYGEN_PPMA) < 0.1, "FZ wafers keep the low oxygen, got " + wafers);
        LOG.info("[LAB] FZ [9-09]: 2 poly + seed + 2 Ar (+P) -> 1 crystal, oxygen {} ppma (CZ about {}), no crucible, 11N kept", com.taku3.semiconductorlab.block.FzPullerBlockEntity.FZ_OXYGEN_PPMA, com.taku3.semiconductorlab.block.FzPullerBlockEntity.CZ_OXYGEN_PPMA);
        h.succeed();
    }

    // =====================================================================
    // [2-32] ポジ型レジスト・[9-19] レジスト塗布（IMP4-01、O-006）
    // =====================================================================

    /** ノボラック＋DNQ＋キシレン＋瓶 → レジスト。日なたで感光したレジストは塗れない。NOT の例の全部の「塗る」を塗布機で通す → 4つの発見 */
    @GameTest(maxTicks = 300)
    public void spinCoater(GameTestHelper h) {
        List<ItemStack> g = new ArrayList<>(List.of(new ItemStack(Chemistry.NOVOLAC), new ItemStack(Chemistry.DNQ), new ItemStack(Chemistry.XYLENE), new ItemStack(Chemistry.GLASS_BOTTLE)));
        while (g.size() < 9) g.add(ItemStack.EMPTY);
        var in = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, g);
        ItemStack resist = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, h.getLevel()).orElseThrow().value().assemble(in);
        h.assertTrue(resist.is(Chemistry.POSITIVE_RESIST), "novolac + DNQ + xylene -> positive resist");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Chemistry.POSITIVE_RESIST) == com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE, "resist in a glass bottle");
        ItemStack spoiled = resist.copy();
        h.assertTrue(com.taku3.semiconductorlab.item.PositiveResistItem.expose(spoiled) && !com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.goodResist(spoiled) && com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.goodResist(resist), "sunlight spoils the resist");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.SPIN_COATER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WATTS);
        ServerLevel lv = h.getLevel();
        java.util.function.UnaryOperator<ItemStack> coat = wf -> {
            c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT, ItemStack.EMPTY);
            c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_WAFER, wf.copy());
            c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_RESIST, new ItemStack(Chemistry.POSITIVE_RESIST));
            tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS, feed);
            return c.getItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT).copy();
        };
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        // 感光したレジストでは動かない
        c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_RESIST, spoiled.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT).isEmpty(), "spoiled resist -> nothing");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            ItemStack coated = coat.apply(w);
            h.assertTrue(coated.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(coated).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.COATED, "layer " + i + ": coater = ChipOps.coat, got " + coated);
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(coated, d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.etchMetal(w).out(); else w = com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
            if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
            else if (k != com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
        }
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(w.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "NOT example with the spin coater -> all discoveries");
        // 塗ったウェハーにもう一度は塗れない
        ItemStack again = coat.apply(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out());
        h.assertTrue(again.isEmpty(), "already coated -> cannot coat again");
        lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] resist [2-32] + spin coater [9-19]: novolac + DNQ + xylene -> resist (spoils in sunlight); coater coats 8 wafers per layer; NOT example -> all discoveries");
        h.succeed();
    }

    /** レジストはがし槽（IMP4-01）：NOT の例の全部の「はがす」を槽で通す → 4つの発見。塗っただけ（リワーク）もはがせる、レジストがないと動かない */
    @GameTest(maxTicks = 300)
    public void resistStripper(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.RESIST_STRIPPER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS);
        java.util.function.UnaryOperator<ItemStack> strip = wf -> {
            c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, ItemStack.EMPTY);
            c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, wf.copy());
            c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID));
            c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE, new ItemStack(Chemistry.HYDROGEN_PEROXIDE));
            tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS, feed);
            return c.getItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT).copy();
        };
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        // リワーク：塗っただけのウェハーをはがすと、また塗れる
        ItemStack rework = strip.apply(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out());
        h.assertTrue(rework.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.canCoat(rework), "rework: strip a coated wafer and coat again");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.etchMetal(w).out(); else w = com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out();
            ItemStack s2 = strip.apply(w);
            h.assertTrue(s2.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(s2).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.NONE, "layer " + i + ": stripper = ChipOps.strip, got " + s2);
            w = s2;
            if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
            else if (k != com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
        }
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(w.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "NOT example with the stripper -> all discoveries");
        h.assertTrue(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.product(w).isEmpty(), "no resist -> nothing to strip");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] resist stripper: H2SO4 + H2O2 strips 8 wafers per run (rework too); NOT example -> all discoveries");
        h.succeed();
    }

    // =====================================================================
    // [9-21] 現像（IMP4-01、O-006）
    // =====================================================================

    /** NOT の例を「塗布機 → 露光（ChipOps）→ 現像槽 → エッチング（ChipOps）→ はがし槽」で通す → 4つの発見。露光していないと現像しない */
    @GameTest(maxTicks = 300)
    public void developerBench(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "developer", com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE), "developer = dilute NaOH");
        floor(h, 5);
        BlockPos cp = p(1, 1, 1), dp = p(3, 1, 1), sp = p(1, 1, 3);
        h.setBlock(cp, Semiconductor.SPIN_COATER.block);
        h.setBlock(dp, Semiconductor.DEVELOPER_BENCH.block);
        h.setBlock(sp, Semiconductor.RESIST_STRIPPER.block);
        var coater = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.class);
        var dev = h.getBlockEntity(dp, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.class);
        var stripper = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.class);
        java.util.function.UnaryOperator<ItemStack> coat = wf -> {
            var c = coater.getContainer(); c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT, ItemStack.EMPTY); c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_WAFER, wf.copy()); c.setItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_RESIST, new ItemStack(Chemistry.POSITIVE_RESIST));
            tickConsumer(h, cp, coater, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WATTS)); return c.getItem(com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT).copy(); };
        java.util.function.UnaryOperator<ItemStack> develop = wf -> {
            var c = dev.getContainer(); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT, ItemStack.EMPTY); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_WAFER, wf.copy()); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_DEV, new ItemStack(Chemistry.DEVELOPER));
            tickConsumer(h, dp, dev, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WATTS)); return c.getItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT).copy(); };
        java.util.function.UnaryOperator<ItemStack> strip = wf -> {
            var c = stripper.getContainer(); c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, ItemStack.EMPTY); c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, wf.copy()); c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID)); c.setItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE, new ItemStack(Chemistry.HYDROGEN_PEROXIDE));
            tickConsumer(h, sp, stripper, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS)); return c.getItem(com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT).copy(); };
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        h.assertTrue(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.product(coat.apply(w)).isEmpty(), "coated but not exposed -> developer does nothing");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            ItemStack exposed = com.taku3.semiconductorlab.litho.ChipOps.expose(coat.apply(w), d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
            ItemStack developed = develop.apply(exposed);
            h.assertTrue(developed.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(developed).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.DEVELOPED, "layer " + i + ": bench = ChipOps.develop, got " + developed);
            w = k == com.taku3.semiconductorlab.litho.LayerKind.METAL ? com.taku3.semiconductorlab.litho.ChipOps.etchMetal(developed).out() : com.taku3.semiconductorlab.litho.ChipOps.etchOxide(developed).out();
            w = strip.apply(w);
            if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
            else if (k != com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
        }
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(w.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "coater -> develop -> strip machines: NOT example -> all discoveries");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(p(2, 1, 2))).inflate(4)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] developer [9-21]: dilute NaOH develops exposed wafers (8 per run); coater + developer + stripper machines -> NOT example all discoveries");
        h.succeed();
    }

    /** 現像槽のネガ型（O-009）：ネガ型（ChipOps.coat NEGATIVE）はキシレンで現像、アルカリでは "wrong_developer"。ポジ型にキシレンも同じ。NOT の例をネガ型で → 全部の発見 */
    @GameTest(maxTicks = 300)
    public void developerBenchNegative(GameTestHelper h) {
        floor(h, 3);
        BlockPos dp = p(1, 1, 1);
        h.setBlock(dp, Semiconductor.DEVELOPER_BENCH.block);
        var dev = h.getBlockEntity(dp, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.class);
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        ItemStack negExposed = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w, com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
        ItemStack posExposed = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
        h.assertValueEqual(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.result(negExposed, new ItemStack(Chemistry.DEVELOPER)).reason(), "wrong_developer", "negative resist + alkali -> wrong developer");
        h.assertValueEqual(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.result(posExposed, new ItemStack(Chemistry.XYLENE)).reason(), "wrong_developer", "positive resist + xylene -> wrong developer");
        h.assertTrue(!com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.product(negExposed, new ItemStack(Chemistry.XYLENE)).isEmpty(), "negative resist + xylene -> developed");
        java.util.function.UnaryOperator<ItemStack> develop = wf -> {
            var c = dev.getContainer(); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT, ItemStack.EMPTY); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_WAFER, wf.copy()); c.setItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_DEV, new ItemStack(Chemistry.XYLENE));
            tickConsumer(h, dp, dev, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.TICKS, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WATTS)); return c.getItem(com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT).copy(); };
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            ItemStack exposed = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w, com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE).out(), d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
            ItemStack developed = develop.apply(exposed);
            h.assertTrue(developed.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(developed).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.DEVELOPED, "layer " + i + ": negative resist developed in xylene, got " + developed);
            w = k == com.taku3.semiconductorlab.litho.LayerKind.METAL ? com.taku3.semiconductorlab.litho.ChipOps.etchMetal(developed).out() : com.taku3.semiconductorlab.litho.ChipOps.etchOxide(developed).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
            if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
            else if (k != com.taku3.semiconductorlab.litho.LayerKind.METAL) w = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
        }
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(w.get(ModDataComponents.CHIP_STATE)))).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "NOT example with negative resist -> all discoveries");
        LOG.info("[LAB] developer bench negative (O-009): xylene develops negative resist, alkali refused (wrong_developer), and vice versa; NOT example -> all discoveries");
        h.succeed();
    }

    /** [9-14] 段階2 RCA 洗浄台（IMP4-01）：汚れたウェハー＋NH3水＋HCl＋H2O2 2＋脱イオン水 → 汚れ0。蒸留水では動かない（RCA は脱イオン水）、レジストがあると洗えない */
    @GameTest(maxTicks = 100)
    public void rcaBench(GameTestHelper h) {
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.RCA_BENCH.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.RcaBenchBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.WATTS);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        w = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out()).out()).out();
        int dirt = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination();
        h.assertTrue(dirt > 0, "wafer is dirty after etch and strip");
        h.assertTrue(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.product(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out()).isEmpty(), "resist on -> cannot clean");
        c.setItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_AMMONIA, new ItemStack(Chemistry.AMMONIA_WATER));
        c.setItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_HCL, new ItemStack(com.taku3.semiconductorlab.content.Gas.HYDROCHLORIC_ACID));
        c.setItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_PEROXIDE, new ItemStack(Chemistry.HYDROGEN_PEROXIDE, 2));
        h.assertFalse(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.isRinse(new ItemStack(Chemistry.DISTILLED_WATER)), "RCA rinses with deionised water, not distilled");
        c.setItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_WATER, new ItemStack(Chemistry.DEIONIZED_WATER));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.RcaBenchBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.RcaBenchBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_OUT);
        h.assertTrue(out.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(out).contamination() == 0, "8 wafers cleaned to 0, got " + out);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_PEROXIDE).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_AMMONIA).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_HCL).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.RcaBenchBlockEntity.SLOT_WATER).isEmpty(), "SC-1 + SC-2 + rinse used");
        h.assertTrue(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.isRinse(new ItemStack(Chemistry.DEIONIZED_WATER)), "stage 1 bench also rinses with deionised water");
        // 段階1 の洗浄槽は 1 まで（金属が残る）、RCA は 0 まで。汚れ 1 のウェハーは段階1 では「もうきれい」、RCA なら 0 にできる
        ItemStack stage1 = com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.product(w);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(stage1).contamination(), 1, "stage 1 (nitric + HF) leaves dirt 1");
        h.assertTrue(com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.product(stage1).isEmpty() && !com.taku3.semiconductorlab.block.RcaBenchBlockEntity.product(stage1).isEmpty(), "dirt 1: stage 1 can do no more, RCA brings it to 0");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(fp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] RCA clean [9-14] stage 2: NH3 water + HCl + 2 H2O2 + DI water clean 8 wafers (dirt {} -> 0)", dirt);
        h.succeed();
    }

    // =====================================================================
    // [9-33] ワイヤーボンディング・[9-34] 封止（IMP4-01、S-001）
    // =====================================================================

    /** NOT のチップ（パッド4）4個＋金のボンディング線16＋ヘッダー4 → 線をつないだチップ4（中身を写す）。線が15本なら3個、パッドのないチップは不可 */
    @GameTest(maxTicks = 100)
    public void wireBonder(GameTestHelper h) {
        ItemStack wafer = new ItemStack(Semiconductor.OXIDIZED_WAFER, 1);
        wafer.set(ModDataComponents.DOPANT, "boron");
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(wafer, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        ItemStack chips = done.transmuteCopy(Semiconductor.CHIP, 4);
        h.assertValueEqual(com.taku3.semiconductorlab.block.WireBonderBlockEntity.padsOf(chips), 4, "the NOT chip has 4 pads");
        ItemStack bare = new ItemStack(Semiconductor.CHIP, 4);
        h.assertValueEqual(com.taku3.semiconductorlab.block.WireBonderBlockEntity.padsOf(bare), 0, "chip without contents has no pads");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.WIRE_BONDER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.WireBonderBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.WireBonderBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_CHIP, chips.copy());
        c.setItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_WIRE, new ItemStack(Metallurgy.GOLD_BONDING_WIRE, 15));
        c.setItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_HEADER, new ItemStack(Semiconductor.CHIP_HEADER, 8));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WireBonderBlockEntity.TICKS, feed / 2);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_OUT).isEmpty(), "half power -> not finished");
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.WireBonderBlockEntity.TICKS, feed);
        ItemStack b = c.getItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_OUT);
        h.assertTrue(b.is(Semiconductor.BONDED_CHIP) && b.getCount() == 3, "15 wires / 4 pads -> 3 chips bonded, got " + b);
        h.assertTrue(b.get(ModDataComponents.CHIP_STATE) != null && b.get(ModDataComponents.CHIP_STATE).equals(done.get(ModDataComponents.CHIP_STATE)), "chip contents copied");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_WIRE).getCount() == 3 && c.getItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_HEADER).getCount() == 5 && c.getItem(com.taku3.semiconductorlab.block.WireBonderBlockEntity.SLOT_CHIP).getCount() == 1, "12 wires, 3 headers, 3 chips used");
        h.assertValueEqual(com.taku3.semiconductorlab.block.WireBonderBlockEntity.batch(bare, new ItemStack(Metallurgy.GOLD_BONDING_WIRE, 64), new ItemStack(Semiconductor.CHIP_HEADER, 8)), 0, "no pads -> nothing");
        LOG.info("[LAB] wire bonder [9-33]: NOT chips have 4 pads; 15 gold wires bond 3 chips onto headers (contents kept)");
        h.succeed();
    }

    /** [9-34] 封止：線をつないだチップ3＋缶3＋窒素 → 金属缶の IC 3。窒素なしでは缶は不可。エポキシ1でチップ4個まで → プラスチックの IC。中身を写す */
    @GameTest(maxTicks = 100)
    public void packageSealer(GameTestHelper h) {
        ItemStack wafer = new ItemStack(Semiconductor.OXIDIZED_WAFER, 1);
        wafer.set(ModDataComponents.DOPANT, "boron");
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(wafer, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        ItemStack bonded = done.transmuteCopy(Semiconductor.BONDED_CHIP, 3);
        h.assertValueEqual(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.batch(bonded, new ItemStack(Semiconductor.CAN_CAP, 3), ItemStack.EMPTY), 0, "can without nitrogen -> nothing");
        h.assertValueEqual(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.batch(bonded.copyWithCount(6), new ItemStack(Chemistry.EPOXY_MOLDING_COMPOUND, 1), ItemStack.EMPTY), 4, "1 epoxy compound molds 4 chips");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Semiconductor.PACKAGE_SEALER.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_CHIP, bonded.copy());
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_SHELL, new ItemStack(Semiconductor.CAN_CAP, 3));
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_GAS, new ItemStack(com.taku3.semiconductorlab.content.Gas.NITROGEN_GAS, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.TICKS, feed);
        ItemStack canned = c.getItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_OUT);
        h.assertTrue(canned.is(Semiconductor.CAN_PACKAGED_CHIP) && canned.getCount() == 3, "3 metal-can ICs, got " + canned);
        h.assertTrue(done.get(ModDataComponents.CHIP_STATE).equals(canned.get(ModDataComponents.CHIP_STATE)), "chip contents copied");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_GAS).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_SHELL).isEmpty(), "3 caps + 1 N2 used");
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_CHIP, bonded.copyWithCount(4));
        c.setItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_SHELL, new ItemStack(Chemistry.EPOXY_MOLDING_COMPOUND, 1));
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.TICKS, feed);
        ItemStack plastic = c.getItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_OUT);
        h.assertTrue(plastic.is(Semiconductor.PLASTIC_PACKAGED_CHIP) && plastic.getCount() == 4 && c.getItem(com.taku3.semiconductorlab.block.PackageSealerBlockEntity.SLOT_SHELL).isEmpty(), "4 plastic ICs from 1 compound, got " + plastic);
        LOG.info("[LAB] package sealer [9-34]: 3 bonded + 3 caps + N2 -> 3 metal-can ICs; 4 bonded + 1 epoxy compound -> 4 plastic ICs (contents kept)");
        h.succeed();
    }

    /** [9-47] 加速器の部品（IMP4-01）：コッククロフト・ウォルトン回路＝ライデン瓶（コンデンサー）＋整流器のはしご、加速管＝ガラスと鋼の輪 */
    @GameTest
    public void acceleratorParts(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cw_multiplier", com.taku3.semiconductorlab.content.Power.LEYDEN_JAR) && recipeUses(h, "cw_multiplier", com.taku3.semiconductorlab.content.Power.RECTIFIER.block.asItem()), "CW = capacitors + rectifiers");
        h.assertTrue(recipeUses(h, "accelerator_tube", Chemistry.SODA_LIME_GLASS) && recipeUses(h, "accelerator_tube", Metallurgy.STEEL_INGOT), "tube = glass insulators + steel electrodes");
        LOG.info("[LAB] accelerator parts [9-47]: Cockcroft-Walton multiplier (Leyden jars + rectifiers), accelerator tube (glass + steel rings)");
        h.succeed();
    }

    // =====================================================================
    // [9-28] イオン注入・[9-29] アニール（IMP4-01、O-011）
    // =====================================================================

    /** 現像したウェハー＋リン → 注入（アニール前で回路に効かない）。真空が悪いと動かない。白リンの水の瓶は戻る */
    @GameTest(maxTicks = 100)
    public void ionImplanter(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "ion_implanter", Semiconductor.CW_MULTIPLIER) && recipeUses(h, "ion_implanter", com.taku3.semiconductorlab.content.Precision.MASS_SPECTROMETER), "implanter = accelerator [9-47] + mass analyser [8-33]");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
        floor(h, 5);
        BlockPos cp = p(1, 1, 1), ip = p(2, 1, 1);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ip, Semiconductor.ION_IMPLANTER.block);
        var ch = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class);
        var be = h.getBlockEntity(ip, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.SLOT_SOURCE, new ItemStack(Chemistry.PHOSPHORUS, 2));
        ch.setPressure(10);
        tickConsumer(h, ip, be, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.SLOT_OUT).isEmpty(), "10 Pa: ions hit air -> nothing");
        ch.setPressure(0.001);
        tickConsumer(h, ip, be, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.SLOT_OUT);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(out);
        h.assertTrue(out.getCount() == 8 && st.implanted().size() == 1 && st.diffusions() == 0, "8 wafers implanted, not active yet, got " + out);
        h.assertTrue(st.implanted().get(0).cells().equals(d.layers().get(0).cells()), "ions only where the resist was opened");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.IonImplanterBlockEntity.SLOT_SOURCE).getCount(), 1, "1 source per run");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(ip)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] ion implanter [9-28]: needs <= 0.01 Pa; 8 developed wafers + P -> implanted (inactive until anneal), ions only in the resist opening");
        h.succeed();
    }

    /** 注入したウェハーはアニールまで回路に効かない。レジストを付けたままは入らない。N2 はホルダーからもらう。アニールで拡散と同じ層に */
    @GameTest(maxTicks = 100)
    public void annealFurnace(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 4);
        w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
        ItemStack imp = com.taku3.semiconductorlab.block.IonImplanterBlockEntity.product(w, new ItemStack(Chemistry.PHOSPHORUS));
        h.assertTrue(!imp.isEmpty() && com.taku3.semiconductorlab.litho.ChipOps.state(imp).done().equals(com.taku3.semiconductorlab.litho.ChipOps.state(w).done()), "implanted but not annealed = same circuit as before");
        floor(h, 5);
        BlockPos gp = p(1, 1, 1), fp = p(2, 1, 1);
        h.setBlock(gp, Gas.GAS_HOLDER.block);
        h.setBlock(fp, Semiconductor.ANNEAL_FURNACE.block);
        h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GasHolderBlockEntity.class).setContentsForTest(com.taku3.semiconductorlab.block.GasType.NITROGEN, 5);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_WAFER, imp.copy());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_OUT).isEmpty(), "resist still on: it would burn -> refused");
        h.assertValueEqual(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.reason(imp), "resist_on", "reason resist_on");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_GAS).is(Gas.NITROGEN_GAS), "nitrogen pulled from the holder");
        c.setItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.litho.ChipOps.strip(imp).out());
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_OUT);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(out);
        h.assertTrue(out.getCount() == 4 && st.implanted().isEmpty() && st.diffusions() == 1 && st.has(com.taku3.semiconductorlab.litho.LayerKind.DIFF_N), "annealed: implant became an n layer, got " + out);
        c.setItem(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.SLOT_WAFER, out.copy());
        h.assertTrue(com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.product(out).isEmpty(), "nothing left to anneal");
        LOG.info("[LAB] anneal furnace [9-29]: implant inactive until anneal; resist must be stripped; N2 from holder; anneal -> DIFF_N");
        h.succeed();
    }

    /** [9-26] スパッタ（IMP4-01）：窓まで開けた NOT のウェハーに、Ar（ホルダーから）＋Al の的で Al を一面に。真空が悪いと動かない。蒸着と同じ結果 */
    @GameTest(maxTicks = 100)
    public void sputter(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        for (int i = 0; i < 4; i++) {
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out()).out()).out();
            if (d.layers().get(i).kind().isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, d.layers().get(i).kind().dopes).out();
        }
        floor(h, 5);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1), gp = p(3, 1, 1);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(sp, Semiconductor.SPUTTER.block);
        h.setBlock(gp, Gas.GAS_HOLDER.block);
        h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GasHolderBlockEntity.class).setContentsForTest(com.taku3.semiconductorlab.block.GasType.ARGON, 5);
        var ch = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class);
        var be = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.SputterBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SputterBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_TARGET, new ItemStack(Metallurgy.ALUMINIUM_INGOT, 2));
        ch.setPressure(5);
        tickConsumer(h, sp, be, com.taku3.semiconductorlab.block.SputterBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_OUT).isEmpty(), "5 Pa: air not pumped out first -> nothing");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_AR).is(Gas.ARGON_GAS), "argon pulled from the holder");
        ch.setPressure(0.003);
        tickConsumer(h, sp, be, com.taku3.semiconductorlab.block.SputterBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_OUT);
        h.assertTrue(out.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(out).metalFilm(), "8 wafers coated with Al, got " + out);
        var ev = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.chipProduct(w, new ItemStack(Metallurgy.ALUMINIUM_INGOT)));
        var sp2 = com.taku3.semiconductorlab.litho.ChipOps.state(out);
        h.assertTrue(sp2.conformalMetal() && !ev.conformalMetal(), "sputtered film is marked conformal (covers steps), evaporated is not");
        h.assertTrue(sp2.done().equals(ev.done()), "same circuit as evaporation (the difference is for yield, v0.5)");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SputterBlockEntity.SLOT_TARGET).getCount(), 1, "target wears by 1");
        h.assertTrue(recipeUses(h, "sputter", com.taku3.semiconductorlab.content.Vacuum.OIL_DIFFUSION_PUMP.block.asItem()), "sputter system is pumped by an oil diffusion pump [5-06]");
        LOG.info("[LAB] sputter [9-26]: <= 0.01 Pa + Ar from holder + Al target -> Al film on 8 process wafers (marked conformal, same circuit as evaporation)");
        h.succeed();
    }

    /** [9-27] ドライエッチング（IMP4-01）：黒鉛＋F2 → CF4、CF4 のプラズマで現像したウェハーに窓。真空が悪いと動かない */
    @GameTest(maxTicks = 100)
    public void dryEtcher(GameTestHelper h) {
        floor(h, 5);
        BlockPos fp = p(3, 1, 3);
        h.setBlock(fp, Semiconductor.FLUORINATION_REACTOR.block);
        var fr = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.class);
        var fc = fr.getContainer();
        fc.setItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_CARBON, new ItemStack(com.taku3.semiconductorlab.content.Heat.SYNTHETIC_GRAPHITE));
        fc.setItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_F2, new ItemStack(Gas.FLUORINE_CYLINDER));
        tickConsumer(h, fp, fr, com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.WATTS));
        h.assertTrue(fc.getItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_OUT).is(Semiconductor.CARBON_TETRAFLUORIDE) && fc.getItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_OUT).getCount() == com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.CF4_PER_RUN && fc.getItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_CYL).is(Gas.EMPTY_GAS_CYLINDER), "C + 2F2 -> CF4, cylinder back");
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
        BlockPos cp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(cp, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ep, Semiconductor.DRY_ETCHER.block);
        var ch = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class);
        var be = h.getBlockEntity(ep, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.SLOT_GAS, fc.getItem(com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.SLOT_OUT).copy());
        ch.setPressure(20);
        tickConsumer(h, ep, be, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.SLOT_OUT).isEmpty(), "20 Pa: no clean plasma -> nothing");
        ch.setPressure(0.05);
        tickConsumer(h, ep, be, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.TICKS, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.SLOT_OUT);
        h.assertTrue(out.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(out).hasWindows(), "8 wafers with windows, got " + out);
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(out).windows().equals(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out()).windows()), "windows exactly where the resist was opened");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.SLOT_GAS).getCount(), com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.CF4_PER_RUN - 1, "1 CF4 per run");
        h.assertTrue(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(new ItemStack(Semiconductor.OXIDIZED_WAFER)).isEmpty(), "no resist pattern -> refused");
        // 細い方眼（ステッパー g 線、36）の1マスの窓：ドライ装置は1マス、液（エッチング槽と同じ etchOxide(w)）は5マスに太る
        int n = com.taku3.semiconductorlab.litho.ChipOps.WET_LIMIT_GRID;
        var mask = new com.taku3.semiconductorlab.litho.MaskData(n, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(n).with(n / 2, n / 2, true), "", 0);
        ItemStack fine = new ItemStack(Semiconductor.OXIDIZED_WAFER); fine.set(ModDataComponents.DOPANT, "boron");
        fine = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(com.taku3.semiconductorlab.litho.ChipOps.coat(fine).out(), mask, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G, 900, 0).out()).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(fine)).windows().count(), 1, "dry etcher: fine line stays 1 cell");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(fine).out()).windows().count(), 5, "wet HF: the same line widens to 5 cells");
        LOG.info("[LAB] dry etcher [9-27]: graphite + F2 -> 8 CF4; CF4 plasma at <= 0.1 Pa opens windows where the resist is open; on a 36 grid dry keeps 1 cell, wet widens to 5");
        h.succeed();
    }

    /** [9-30] シラン（IMP4-01、S-002）：Mg2 + Si → Mg2Si、Mg2Si + 4HCl + 空ボンベ → シランのボンベ（ボンベがないと動かない） */
    @GameTest(maxTicks = 100)
    public void silaneGenerator(GameTestHelper h) {
        floor(h, 5);
        BlockPos sp = p(2, 1, 2);
        h.setBlock(sp, Semiconductor.SILANE_GENERATOR.block);
        var be = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_A, new ItemStack(Metallurgy.MAGNESIUM, 2));
        c.setItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_B, new ItemStack(Metallurgy.METALLURGICAL_SILICON));
        tickConsumer(h, sp, be, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.TICKS + 1, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_OUT).is(Semiconductor.MAGNESIUM_SILICIDE) && c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_A).isEmpty(), "2Mg + Si -> Mg2Si");
        c.setItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_A, c.removeItemNoUpdate(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_OUT));
        c.setItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_B, new ItemStack(Gas.HYDROCHLORIC_ACID, 4));
        tickConsumer(h, sp, be, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_OUT).isEmpty(), "no empty cylinder -> the pyrophoric gas has nowhere to go");
        c.setItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_CYL, new ItemStack(Gas.EMPTY_GAS_CYLINDER));
        tickConsumer(h, sp, be, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.TICKS + 1, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_OUT).is(Semiconductor.SILANE_CYLINDER) && c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_B).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.SLOT_CYL).isEmpty(), "Mg2Si + 4HCl -> SiH4 in the cylinder");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(sp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] silane generator [9-30]: 2Mg + Si -> Mg2Si; Mg2Si + 4HCl + cylinder -> silane cylinder");
        h.succeed();
    }

    /**
     * [9-30] CVD 炉と2層配線（IMP4-01、S-002 案B）：NMOS インバータ＋2層目の例を、層間膜は CVD 炉（シラン＋O2 はホルダーから）、
     * VIA の窓はドライエッチング装置、2層目の Al はスパッタ装置の product で通す → 右と下のパッドが METAL2 で同じ線
     */
    @GameTest(maxTicks = 200)
    public void v03ChainTwoMetal(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.nmosInverterTwoMetal();
        int n = d.size();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        floor(h, 5);
        BlockPos fp = p(2, 1, 2), gp = p(3, 1, 2);
        h.setBlock(fp, Semiconductor.CVD_FURNACE.block);
        h.setBlock(gp, Gas.GAS_HOLDER.block);
        h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GasHolderBlockEntity.class).setContentsForTest(com.taku3.semiconductorlab.block.GasType.OXYGEN, 4);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WATTS);
        boolean cvdTried = false;
        for (int i = 0; i < d.layers().size(); i++) {
            var k = d.layers().get(i).kind();
            if (k == com.taku3.semiconductorlab.litho.LayerKind.VIA) {
                // CVD 炉：シランのボンベ＋O2（ホルダーから）→ 層間膜。レジストが付いていると断る
                c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.litho.ChipOps.coat(w).out());
                c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_SILANE, new ItemStack(Semiconductor.SILANE_CYLINDER));
                tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.TICKS + 5, feed);
                h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_OUT).isEmpty(), "resist on: no interlayer");
                c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_WAFER, w.copy());
                tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.TICKS, feed);
                h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_OUT).getCount() == 8 && c.getItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_CYL).is(Gas.EMPTY_GAS_CYLINDER), "CVD: 8 wafers get the interlayer, cylinder back");
                w = c.removeItemNoUpdate(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_OUT);
                h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w).interlayer(), "interlayer recorded");
                cvdTried = true;
            }
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), d.mask(i), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL || k == com.taku3.semiconductorlab.litho.LayerKind.METAL2) w = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.etchMetal(w).out()).out();
            else if (k == com.taku3.semiconductorlab.litho.LayerKind.GATE_OX) w = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.gateOxide(w).out()).out();
            else {
                w = com.taku3.semiconductorlab.litho.ChipOps.strip(k == com.taku3.semiconductorlab.litho.LayerKind.VIA ? com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(w) : com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w).out()).out();
                if (k.isDiffusion()) w = com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, k.dopes).out();
                else w = k == com.taku3.semiconductorlab.litho.LayerKind.VIA ? com.taku3.semiconductorlab.block.SputterBlockEntity.product(w, new ItemStack(Metallurgy.ALUMINIUM_INGOT)) : com.taku3.semiconductorlab.litho.ChipOps.depositMetal(w).out();
            }
            h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.isProcessWafer(w), "layer " + i + " " + k + " went through");
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(w);
        h.assertTrue(cvdTried && st.has(com.taku3.semiconductorlab.litho.LayerKind.VIA) && st.has(com.taku3.semiconductorlab.litho.LayerKind.METAL2), "vias and a second metal");
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        h.assertValueEqual(nl.netOf[12 * n + 15], nl.netOf[15 * n + 8], "right pad and bottom pad are one net through metal 2");
        // 窒化膜の保護膜（O-013）：同じ CVD 炉に NH3 を入れる → passivated、2回目は断る
        c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_SILANE, new ItemStack(Semiconductor.SILANE_CYLINDER));
        c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_O2, new ItemStack(Gas.AMMONIA_GAS, 2));
        c.setItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_CYL, ItemStack.EMPTY);
        tickConsumer(h, fp, be, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.TICKS + 1, feed);
        ItemStack pas = c.getItem(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(pas.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(pas).extras().passivated(), "silane + NH3 -> nitride passivation, got " + pas);
        h.assertTrue(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.product(pas, new ItemStack(Gas.AMMONIA_GAS)).isEmpty(), "already passivated");
        h.assertTrue(com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.product(w, new ItemStack(Gas.OXYGEN_GAS)).isEmpty(), "only one interlayer");
        LOG.info("[LAB] two-metal chain [9-30]: CVD interlayer (silane + O2 from holder), dry-etched vias, sputtered metal 2 -> pads joined; NH3 -> nitride passivation");
        h.succeed();
    }

    /** [9-31] CMP（IMP4-01、v0.5 #2）：層間膜のない板は断る、層間膜のある板は段差 steps → 0、粒で汚れ +2、瓶は戻る、2回目は already_flat */
    @GameTest(maxTicks = 100)
    public void cmpPolisher(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.nmosInverterTwoMetal();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        int via = -1;
        for (int i = 0; i < d.layers().size(); i++) if (d.layers().get(i).kind() == com.taku3.semiconductorlab.litho.LayerKind.VIA) { via = i; break; }
        var head = new com.taku3.semiconductorlab.litho.ChipDesign(d.size(), "head", d.layers().subList(0, via), java.util.List.of(), com.taku3.semiconductorlab.litho.ChipDesign.MOS);
        w = com.taku3.semiconductorlab.litho.LithoExamples.process(w, head, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertTrue(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.product(w).isEmpty() && com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.reason(w).equals("no_interlayer"), "no interlayer: nothing to polish");
        w = com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.product(w);
        var before = com.taku3.semiconductorlab.litho.ChipOps.state(w);
        h.assertTrue(before.steps() > 0, "layers piled up -> steps " + before.steps());
        floor(h, 5);
        BlockPos cp = p(2, 1, 2);
        h.setBlock(cp, Semiconductor.CMP_POLISHER.block);
        var be = h.getBlockEntity(cp, com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.SLOT_WAFER, w.copy());
        c.setItem(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.SLOT_SLURRY, new ItemStack(Chemistry.COLLOIDAL_SILICA, 2));
        tickConsumer(h, cp, be, com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.WATTS));
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.SLOT_OUT);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(out);
        h.assertTrue(out.getCount() == 8 && st.steps() == 0, "CMP: steps -> 0, got " + out);
        h.assertValueEqual(st.contamination(), before.contamination() + 2, "slurry particles: dirt +2");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.SLOT_SLURRY).getCount(), 1, "1 slurry per run");
        h.assertValueEqual(com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.reason(out), "already_flat", "second pass: already flat");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(cp)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] CMP [9-31]: interlayer wafer steps " + before.steps() + " -> 0, dirt +2, bottle back");
        h.succeed();
    }

    /** [9-30] エピタキシー（IMP4-01、O-013）：p のウェハー＋SiCl4＋H2（ホルダーから）＋リン → n のエピ層＋HCl 4。2回目は断る。酸化しても受け継ぐ */
    @GameTest(maxTicks = 100)
    public void epitaxyReactor(GameTestHelper h) {
        floor(h, 5);
        BlockPos ep = p(2, 1, 2), gp = p(3, 1, 2);
        h.setBlock(ep, Semiconductor.EPITAXY_REACTOR.block);
        h.setBlock(gp, Gas.GAS_HOLDER.block);
        h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GasHolderBlockEntity.class).setContentsForTest(com.taku3.semiconductorlab.block.GasType.HYDROGEN, 4);
        var be = h.getBlockEntity(ep, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.class);
        var c = be.getContainer();
        ItemStack w = new ItemStack(Semiconductor.SILICON_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        c.setItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_WAFER, w);
        c.setItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_SICL4, new ItemStack(Metallurgy.SILICON_TETRACHLORIDE));
        c.setItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_DOPANT, new ItemStack(Chemistry.PHOSPHORUS));
        tickConsumer(h, ep, be, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.TICKS + 15, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.WATTS));
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_OUT);
        h.assertTrue(out.getCount() == 8 && "n".equals(out.get(ModDataComponents.EPI)) && "boron".equals(out.get(ModDataComponents.DOPANT)), "n epi layer on the p wafer, got " + out);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_HCL).is(Metallurgy.HYDROGEN_CHLORIDE_GAS) && c.getItem(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.SLOT_HCL).getCount() == com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.HCL_PER_RUN, "SiCl4 + 2H2 -> Si + 4HCl");
        h.assertTrue(com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.product(out, new ItemStack(Chemistry.PHOSPHORUS)).isEmpty(), "already has an epi layer");
        ItemStack ox = com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.oxidized(out, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.Mode.DRY, 8);
        h.assertValueEqual(ox.get(ModDataComponents.EPI), "n", "the epi layer survives oxidation");
        var cs = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.coat(ox).out());
        h.assertTrue(cs.substrate().equals("n") && cs.extras().epiOn().equals("p"), "coated: surface = n epi layer, base p recorded (A)");
        h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(h.absolutePos(ep)).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        LOG.info("[LAB] epitaxy [9-30]: p wafer + SiCl4 + H2 (holder) + P -> n epi layer + 4 HCl; kept through oxidation");
        h.succeed();
    }

    /** [9-36] 銅配線（IMP4-01、S-003）：CVD 炉の層間膜 → ドライで穴と溝 → スパッタ（Ta の的）でバリア → めっき装置 → CMP 装置 → METAL2＋VIA。バリアなしは汚れ +30 */
    @GameTest(maxTicks = 100)
    public void copperWiring(GameTestHelper h) {
        var base = com.taku3.semiconductorlab.litho.LithoExamples.nmosInverterTwoMetal();
        var one = new com.taku3.semiconductorlab.litho.ChipDesign(16, "one", base.layers().subList(0, 4), java.util.List.of(), com.taku3.semiconductorlab.litho.ChipDesign.MOS);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8); w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.LithoExamples.process(w, one, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        w = com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.product(w);
        for (int layer : new int[] {4, 5}) {   // 1回目＝ビアの穴、2回目＝溝（ドライエッチング装置）
            var mask = new com.taku3.semiconductorlab.litho.ChipDesign(16, "m", java.util.List.of(new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.CONTACT, base.layers().get(layer).cells()))).mask(0);
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), mask, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out()).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(w)).out();
        }
        h.assertTrue(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.product(w).getCount() == 8 && !com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.hasBarrier(w), "plating without a barrier is possible but...");
        int dirt0 = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.product(w)).contamination(), dirt0 + 30, "...copper poisons the silicon (+30)");
        ItemStack b = com.taku3.semiconductorlab.block.SputterBlockEntity.product(w, new ItemStack(Metallurgy.TANTALUM));
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(b).extras().barrier(), "sputter with a Ta target lays the barrier");
        floor(h, 5);
        BlockPos pp = p(2, 1, 2);
        h.setBlock(pp, Semiconductor.COPPER_PLATER.block);
        var be = h.getBlockEntity(pp, com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.SLOT_WAFER, b.copy());
        c.setItem(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.SLOT_SULFATE, new ItemStack(Chemistry.COPPER_SULFATE));
        c.setItem(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.SLOT_ANODE, new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT));
        tickConsumer(h, pp, be, com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.WATTS));
        ItemStack plated = c.getItem(com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.SLOT_OUT);
        h.assertTrue(plated.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(plated).metalFilm() && com.taku3.semiconductorlab.litho.ChipOps.state(plated).contamination() < dirt0 + 30, "plated with the barrier: copper film, little dirt");
        ItemStack done = com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.product(plated);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(done);
        h.assertTrue(st.has(com.taku3.semiconductorlab.litho.LayerKind.METAL2) && st.has(com.taku3.semiconductorlab.litho.LayerKind.VIA) && !st.metalFilm(), "CMP leaves copper only in trenches and holes");
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        h.assertValueEqual(nl.netOf[12 * 16 + 15], nl.netOf[15 * 16 + 8], "right pad and bottom pad joined through copper");
        LOG.info("[LAB] copper wiring [9-36]: CVD, dry-etched holes and trenches, Ta barrier by sputter, plating, CMP -> copper metal 2 + vias");
        h.succeed();
    }

    /** [9-37] FinFET のフィン（IMP4-01、S-003 案A）：現像したウェハーにドライエッチング装置＋Cl2 → FIN（レジストの残った所）。CF4 なら窓、液は不可、拡散のあとは too_late */
    @GameTest(maxTicks = 100)
    public void finEtch(GameTestHelper h) {
        int n = com.taku3.semiconductorlab.litho.Circuit.SHORT_CHANNEL_GRID;
        var bars = com.taku3.semiconductorlab.litho.BitGrid.empty(n);
        for (int y = 2; y < 8; y++) bars = bars.with(4, y, true).with(8, y, true);
        var mask = new com.taku3.semiconductorlab.litho.MaskData(n, com.taku3.semiconductorlab.litho.LayerKind.FIN, bars, "", 0);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8); w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(com.taku3.semiconductorlab.litho.ChipOps.coat(w).out(), mask, com.taku3.semiconductorlab.litho.ExposureTool.ARF_IMMERSION, 38, 0).out()).out();
        ItemStack fins = com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(w, new ItemStack(Gas.CHLORINE_GAS));
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(fins);
        h.assertTrue(fins.getCount() == 8 && st.has(com.taku3.semiconductorlab.litho.LayerKind.FIN), "Cl2 plasma stands fins up, got " + fins);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.formFins(w, false).reason(), "wet", "liquids cannot cut fins");
        h.assertTrue(!com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(w, new ItemStack(Semiconductor.CARBON_TETRAFLUORIDE))).has(com.taku3.semiconductorlab.litho.LayerKind.FIN), "CF4 opens oxide windows instead");
        h.assertTrue(com.taku3.semiconductorlab.block.DryEtcherBlockEntity.product(new ItemStack(Semiconductor.OXIDIZED_WAFER), new ItemStack(Gas.CHLORINE_GAS)).isEmpty(), "no developed resist -> refused");
        LOG.info("[LAB] fin etch [9-37]: Cl2 plasma in the dry etcher -> FIN under the resist; wet refused; CF4 gives windows");
        h.succeed();
    }

    /**
     * [9-49] サイリスタ・[9-41] インバータ（IMP4-01、O-015）：p-n 接合の板（n の板）＋白リン → サイリスタの板（拡散炉、今の分岐は変わらない）→ 作業台で素子。
     * 直流の発電機 → インバータ → 前の網は交流（変圧器が働く）。後ろの網は直流のまま
     */
    @GameTest(maxTicks = 300)
    public void thyristorInverter(GameTestHelper h) {
        ItemStack pn = new ItemStack(Semiconductor.PN_JUNCTION_WAFER, 8); pn.set(ModDataComponents.DOPANT, "phosphorus");
        h.assertTrue(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesThyristor(pn, new ItemStack(Chemistry.PHOSPHORUS)), "n-base p-n wafer + P -> p-n-p-n");
        h.assertTrue(!com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesThyristor(pn, new ItemStack(Metallurgy.BORON_TRIOXIDE)), "boron again does not make a fourth layer");
        ItemStack sw = new ItemStack(Semiconductor.SILICON_WAFER, 8); sw.set(ModDataComponents.DOPANT, "phosphorus");
        h.assertTrue(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(sw, new ItemStack(Metallurgy.BORON_TRIOXIDE)) && !com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesThyristor(sw, new ItemStack(Chemistry.PHOSPHORUS)), "the old branch is unchanged");
        floor(h, 9);
        BlockPos fp = p(7, 1, 7);
        h.setBlock(fp, Semiconductor.DIFFUSION_FURNACE.block);
        var df = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.class);
        df.getContainer().setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, pn.copy());
        df.getContainer().setItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE, new ItemStack(Chemistry.PHOSPHORUS));
        tickConsumer(h, fp, df, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS + 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS));
        ItemStack tw = df.getContainer().getItem(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT);
        h.assertTrue(tw.is(Semiconductor.THYRISTOR_WAFER) && tw.getCount() == 8, "diffusion furnace: 8 thyristor wafers, got " + tw);
        h.assertTrue(recipeUses(h, "thyristor", Semiconductor.THYRISTOR_WAFER) && recipeUses(h, "inverter", Semiconductor.THYRISTOR), "wafer -> thyristors -> inverter");
        // 直流の発電機（交流発電機なしの蒸気タービン）→ ケーブル → インバータ（東向き）→ ケーブル → 変圧器
        h.setBlock(p(1, 1, 2).north(), net.minecraft.world.level.block.Blocks.WATER);
        h.setBlock(p(1, 1, 2), com.taku3.semiconductorlab.content.Power.STEAM_TURBINE.block);
        h.getBlockEntity(p(1, 1, 2), com.taku3.semiconductorlab.energy.SteamTurbineBlockEntity.class).getContainer().setItem(com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.SLOT_COAL, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
        for (int x = 2; x <= 3; x++) h.setBlock(p(x, 1, 2), com.taku3.semiconductorlab.content.Power.COPPER_CABLE.block);
        h.setBlock(p(4, 1, 2), Semiconductor.INVERTER.block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST));
        h.setBlock(p(5, 1, 2), com.taku3.semiconductorlab.content.Power.COPPER_CABLE.block);
        h.setBlock(p(6, 1, 2), com.taku3.semiconductorlab.content.Power.TRANSFORMER.block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.EAST));
        tickAll(h, 40, p(1, 1, 2), p(2, 1, 2), p(3, 1, 2), p(4, 1, 2), p(5, 1, 2), p(6, 1, 2));
        var dcNet = com.taku3.semiconductorlab.energy.CableNetwork.get(h.getLevel(), h.absolutePos(p(2, 1, 2)));
        var acNet = com.taku3.semiconductorlab.energy.CableNetwork.get(h.getLevel(), h.absolutePos(p(5, 1, 2)));
        h.assertValueEqual(dcNet.current, com.taku3.semiconductorlab.energy.Current.DC, "the turbine side stays DC");
        h.assertValueEqual(acNet.current, com.taku3.semiconductorlab.energy.Current.AC, "the inverter output makes an AC network");
        h.assertTrue(acNet.mismatched == 0, "the transformer works on the inverter's AC");
        h.assertTrue(h.getBlockEntity(p(4, 1, 2), com.taku3.semiconductorlab.energy.PowerConverterBlockEntity.class).getLastWatts() > 0, "power passes the inverter");
        LOG.info("[LAB] thyristor [9-49] / inverter [9-41]: p-n wafer + P -> p-n-p-n; DC turbine -> inverter -> AC network, transformer works");
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
        {   // 重ね合わせのずれ（[8-17]、C と決めた形）：1層目は記録しない、2層目から最大が残る、失敗にはしない
            var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
            ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
            var e1 = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G, 3000).out();
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(e1).overlayNm(), 0, "first layer has nothing to align to");
            var e2 = com.taku3.semiconductorlab.litho.ChipOps.strip(e1).out();
            e2 = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(e2).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G, 2000).out();
            e2 = com.taku3.semiconductorlab.litho.ChipOps.strip(e2).out();
            e2 = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(e2).out(), d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G, 500).out();
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(e2).overlayNm(), 2000, "overlay keeps the worst layer");
        }
        // 方眼の大きさ：N = 8 × (10 µm ÷ CD)^(1/3) を 4 の倍数に
        for (var t : com.taku3.semiconductorlab.litho.ExposureTool.values())
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(t.cdNm), t.grid, "grid size of " + t);
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        // 予想（設計図から）
        var predicted = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(design, "p"));
        var ids = com.taku3.semiconductorlab.litho.Discoveries.match(predicted).stream().map(com.taku3.semiconductorlab.litho.Discoveries.Discovery::id).toList();
        h.assertTrue(ids.equals(java.util.List.of("diffused_resistor", "junction_diode", "planar_transistor", "monolithic_ic", "rtl_not")), "8x8 example finds all five: " + ids);
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
        {   // ネガ型 KPR（[2-30]、E と決めた形）：同じ設計図・同じ描き方で、有機溶剤で現像すると同じチップ。アルカリでは現像しない
            ItemStack nw = new ItemStack(Semiconductor.OXIDIZED_WAFER, 8); nw.set(ModDataComponents.DOPANT, "boron");
            ItemStack neg = com.taku3.semiconductorlab.litho.LithoExamples.process(nw, design, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G, com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE);
            var negNl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(neg.get(ModDataComponents.CHIP_STATE)));
            h.assertValueEqual(com.taku3.semiconductorlab.litho.Discoveries.match(negNl).size(), com.taku3.semiconductorlab.litho.Discoveries.all().size(), "negative KPR route gives the same chip");
            var c1 = com.taku3.semiconductorlab.litho.ChipOps.coat(nw, com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE).out();
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.tone(c1), com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE, "tone kept after coating");
            var x1 = com.taku3.semiconductorlab.litho.ChipOps.expose(c1, design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
            h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.develop(x1).reason(), "wrong_developer", "negative resist is not developed in alkali");
            h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.develop(x1, com.taku3.semiconductorlab.litho.ChipOps.Developer.ORGANIC).ok(), "negative resist is developed in an organic solvent");
        }
        h.assertTrue(com.taku3.semiconductorlab.litho.Discoveries.match(made).size() == com.taku3.semiconductorlab.litho.Discoveries.all().size(), "processed chip = predicted chip");
        h.assertValueEqual(wafer.getCount(), 8, "the input stack is not changed");
        // まちがった順・合わない物は断られる（ウェハーはそのまま）
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.expose(wafer, design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).reason(), "no_resist", "expose before coating");
        ItemStack undoped = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.coat(undoped).reason(), "undoped", "undoped wafer");
        ItemStack coated = com.taku3.semiconductorlab.litho.ChipOps.coat(wafer).out();
        var fine = new com.taku3.semiconductorlab.litho.MaskData(20, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(20), "", 0);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.expose(coated, fine, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).reason(), "too_fine", "20x20 mask with contact printing (16x16, S-008)");
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.expose(coated, fine, com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X).ok(), "12x12 mask with 1:1 projection");
        ItemStack exposed = com.taku3.semiconductorlab.litho.ChipOps.expose(coated, design.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
        ItemStack redo = com.taku3.semiconductorlab.litho.ChipOps.strip(exposed).out();
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.canCoat(redo), "strip resist and coat again (rework)");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.diffuse(redo, 'n').reason(), "no_window", "diffuse without windows");
        // 拡散炉 [9-16]：窓の開いた加工中のウェハーは窓からだけ（プレーナ）。窓がなければ入れても動かず、p-n 接合の板にもならない
        ItemStack opened = com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(com.taku3.semiconductorlab.litho.ChipOps.develop(exposed).out()).out()).out();
        h.assertTrue(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.planar(opened, new ItemStack(Chemistry.PHOSPHORUS)), "windows open + phosphorus -> planar diffusion");
        h.assertFalse(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.planar(redo, new ItemStack(Chemistry.PHOSPHORUS)), "no windows -> no planar diffusion");
        h.assertFalse(com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.makesJunction(opened.copyWithCount(8), new ItemStack(Metallurgy.BORON_TRIOXIDE)), "a process wafer never becomes a plain junction wafer");
        // 汚れ（[9-14]）：工程のたびに増え、洗うと落ちる。失敗にはしない
        int dirt = done.get(ModDataComponents.CHIP_STATE).contamination();
        h.assertTrue(dirt > 0, "contamination grows with the steps: " + dirt);
        ItemStack washed = com.taku3.semiconductorlab.litho.ChipOps.clean(done, 0).out();
        h.assertValueEqual(washed.get(ModDataComponents.CHIP_STATE).contamination(), 0, "cleaned to 0");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.clean(washed, 0).reason(), "no_dirt", "already clean");
        // 研磨してやり直し（2回まで）
        ItemStack r1 = com.taku3.semiconductorlab.litho.ChipOps.reclaim(done).out(), r2 = com.taku3.semiconductorlab.litho.ChipOps.reclaim(r1).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.reclaim(r2).reason(), "too_many_reclaims", "reclaim twice at most");
        LOG.info("[LAB] litho core: 8x8 NOT example -> {}; NOT in 0 -> {} V, in 1 -> {} V", ids, String.format("%.2f", hi), String.format("%.2f", lo));
        h.succeed();
    }


    /**
     * 製図台（O-002）：＋で白紙の方眼紙（8×8・5つの層）、塗る・消す、ひと筆ずつ元に戻す・やり直す、
     * ✂ で選んだ層を原図（mask 成分つき）に切り出す。8×8 の NOT の例を塗ると、設計図は例と同じになる
     */
    @GameTest
    public void draftingTable(GameTestHelper h) {
        var pos = p(1, 1, 1);
        h.setBlock(pos, Semiconductor.DRAFTING_TABLE.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_NEW), "new sheet");
        h.assertValueEqual(be.design().size(), 16, "16x16 for contact printing (S-008)");
        h.assertValueEqual(be.design().layers().size(), 5, "island, base, emitter, contacts, metal");
        // 例の設計図を、画面と同じボタンで塗る（層ごとに1筆）
        var ex = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        for (int l = 0; l < ex.layers().size(); l++) {
            be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_STROKE);
            var cells = ex.layers().get(l).cells();
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) if (cells.get(x, y)) be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.paintId(true, l, y * 16 + x));   // 16×16 の紙の左上に（S-008）
        }
        java.util.function.Function<com.taku3.semiconductorlab.litho.BitGrid, com.taku3.semiconductorlab.litho.BitGrid> on16 = g -> {   // 8×8 の例を 16×16 の左上に置いた形
            var o = com.taku3.semiconductorlab.litho.BitGrid.empty(16);
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) if (g.get(x, y)) o = o.with(x, y, true);
            return o;
        };
        for (int l = 0; l < ex.layers().size(); l++)
            h.assertTrue(be.design().layers().get(l).cells().equals(on16.apply(ex.layers().get(l).cells())), "layer " + l + " painted like the example");
        // 元に戻す（金属の1筆が消える）→ やり直す
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_UNDO);
        h.assertTrue(be.design().layers().get(4).cells().isEmpty(), "undo removes the last stroke (metal)");
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_REDO);
        h.assertTrue(be.design().layers().get(4).cells().equals(on16.apply(ex.layers().get(4).cells())), "redo brings it back");
        // 消す（1マス）
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_STROKE);
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.paintId(false, 4, 0));
        h.assertFalse(be.design().layers().get(4).cells().get(0, 0), "right click erases");
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_UNDO);
        // 原図を切る（窓の層）
        h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_CUT + 3), "cut the contact layer");
        var r = be.getContainer().getItem(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SLOT_OUT);
        h.assertTrue(r.is(Semiconductor.RUBYLITH), "rubylith comes out");
        var mask = r.get(ModDataComponents.MASK);
        h.assertTrue(mask.kind() == com.taku3.semiconductorlab.litho.LayerKind.CONTACT && mask.cells().equals(on16.apply(ex.layers().get(3).cells())) && mask.index() == 3, "mask = contact layer");
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_CUT + 4), "the outlet is full");
        // 方眼の大きさを変える（白紙になる、元に戻せる）
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_SIZE + 1);
        h.assertValueEqual(be.design().size(), com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SIZES[1], "next size");
        be.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_UNDO);
        h.assertValueEqual(be.design().size(), 16, "undo the size change");
        LOG.info("[LAB] drafting table: painted the 8x8 NOT example, undo/redo, cut the contact layer to rubylith");
        h.succeed();
    }


    /**
     * 検査台（O-002）：焼いた 8×8 の NOT を置き、パッドをボタンで 0 V・5 V にすると、画面と同じ計算で NOT として動く。
     * 「調べる」で4つの発見（ビット）。発見の進捗（discovery/…）が読み込まれている。ウェハーを替えるとパッドは元に戻る
     */
    @GameTest
    public void probeStation(GameTestHelper h) {
        var pos = p(1, 1, 1);
        h.setBlock(pos, Semiconductor.PROBE_STATION.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack wafer = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        wafer.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(wafer, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE), "nothing to probe without a wafer");
        be.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, chip);
        var nl = be.netlist();
        h.assertValueEqual(nl.pads().size(), 4, "4 pads on the 8x8 NOT");
        int[] pins = com.taku3.semiconductorlab.litho.Circuit.notPins(nl);   // 入力・出力・電源・0 V の線
        java.util.function.IntUnaryOperator padIndex = net -> { for (int i = 0; i < nl.pads().size(); i++) if (nl.pads().get(i).id() == net) return i; return -1; };
        be.onButton(player, padIndex.applyAsInt(pins[3]));                                              // 0 V
        be.onButton(player, padIndex.applyAsInt(pins[2])); be.onButton(player, padIndex.applyAsInt(pins[2]));   // 5 V
        be.onButton(player, padIndex.applyAsInt(pins[0]));                                              // 入力 0 V
        var lo = com.taku3.semiconductorlab.litho.Circuit.solve(nl, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.drive(nl, be::padState)).netVolts(nl, pins[1]);
        be.onButton(player, padIndex.applyAsInt(pins[0]));                                              // 入力 5 V
        var sol = com.taku3.semiconductorlab.litho.Circuit.solve(nl, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.drive(nl, be::padState));
        var hiIn = sol.netVolts(nl, pins[1]);
        h.assertTrue(lo > com.taku3.semiconductorlab.litho.Circuit.HIGH && hiIn < 1.5, String.format("probe: in 0 V -> out %.2f V, in 5 V -> out %.2f V (process dirt lowers the high side)", lo, hiIn));
        h.assertTrue(sol.padAmps().get(pins[2]) > 1e-4, "current flows from the 5 V pad when the transistor is on");
        // 汚れ（S-003）：工程の汚れでも NOT として動くが、出力の高い方が下がる（漏れ）。とても汚れると動かない
        var st = chip.get(ModDataComponents.CHIP_STATE);
        var cleanNl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out().get(ModDataComponents.CHIP_STATE)));
        double hiDirty = com.taku3.semiconductorlab.litho.Circuit.notOutput(nl, pins, false), hiClean = com.taku3.semiconductorlab.litho.Circuit.notOutput(cleanNl, pins, false);
        h.assertTrue(st.contamination() > 0 && hiDirty < hiClean - 0.3 && hiDirty > com.taku3.semiconductorlab.litho.Circuit.HIGH,
                String.format("dirt %d: out high %.2f V (clean %.2f V) - degraded but still a NOT", st.contamination(), hiDirty, hiClean));
        var filthy = new com.taku3.semiconductorlab.litho.ChipState(st.size(), st.substrate(), st.done(), st.windows(), st.resist(), st.resistPattern(), st.metalFilm(), st.reclaims(), 200);
        var filthyNl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(filthy));
        h.assertFalse(com.taku3.semiconductorlab.litho.Circuit.worksAsNot(filthyNl), "very dirty chip does not work as a NOT");
        h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE), "probe");
        h.assertValueEqual(be.found(), (1 << com.taku3.semiconductorlab.litho.Discoveries.all().size()) - 1, "all discoveries on this chip");
        for (var d : com.taku3.semiconductorlab.litho.Discoveries.all())
            h.assertTrue(h.getLevel().getServer().getAdvancements().get(com.taku3.semiconductorlab.SemiconductorLab.id("discovery/" + d.id())) != null, "advancement discovery/" + d.id());
        be.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, ItemStack.EMPTY);
        h.assertValueEqual(be.padState(0), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.OPEN, "pads reset when the wafer changes");
        h.assertValueEqual(be.found(), 0, "result reset when the wafer changes");
        LOG.info("[LAB] probe station: NOT out {} V / {} V, found 4", String.format("%.2f", lo), String.format("%.2f", hiIn));
        h.succeed();
    }


    // =====================================================================
    // v0.3 の通し（O-005、docs/LITHO_DESIGN.md §10）：作った機械で順に通す
    // =====================================================================

    /** 消費機械を1つ置き、入口に物を入れて回し、出口の物を返す（電気はケーブルの代わりに直接入れる） */
    private static ItemStack runConsumer(GameTestHelper h, BlockPos rel, net.minecraft.world.level.block.Block block, int[] slots, ItemStack[] items, int outSlot, int ticks, double watts) {
        h.setBlock(rel, net.minecraft.world.level.block.Blocks.AIR);   // 前の回の中身を残さない
        h.setBlock(rel, block);
        var be = (com.taku3.semiconductorlab.energy.ConsumerBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(rel));
        for (int i = 0; i < slots.length; i++) be.getContainer().setItem(slots[i], items[i]);
        tickConsumer(h, rel, be, ticks, EnergyStorage.wattsToJoulesPerTick(watts));
        return be.getContainer().getItem(outSlot).copy();
    }

    /** エッチング槽（電気なし）で1回 */
    private static ItemStack runEtch(GameTestHelper h, BlockPos rel, ItemStack wafer, net.minecraft.world.item.Item acid) {
        h.setBlock(rel, net.minecraft.world.level.block.Blocks.AIR);
        h.setBlock(rel, Semiconductor.ETCH_TANK.block);
        var be = h.getBlockEntity(rel, com.taku3.semiconductorlab.block.EtchTankBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_WAFER, wafer);
        c.setItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_HF, new ItemStack(acid));
        var lv = h.getLevel(); var a = h.absolutePos(rel);
        for (int i = 0; i < 4000 && c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).isEmpty(); i++)
            com.taku3.semiconductorlab.block.EtchTankBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        return c.getItem(com.taku3.semiconductorlab.block.EtchTankBlockEntity.SLOT_OUT).copy();
    }

    private static ItemStack ok(GameTestHelper h, com.taku3.semiconductorlab.litho.ChipOps.Result r, String what) {
        h.assertTrue(r.ok(), what + ": " + r.reason());
        return r.out();
    }

    /**
     * v0.3 の通し：シリコンの板（p型）→ 酸化炉 → [塗布・露光・現像は ChipOps で代わり] → エッチング槽（HF）→ [はがすのは ChipOps] → 拡散炉 …
     * → 真空蒸着装置（Al）→ エッチング槽（リン酸）→ 洗浄台 → 検査台で NOT を発見。
     * 実機：酸化炉 [9-15]・エッチング槽 [9-06]・拡散炉 [9-16]・真空蒸着 [9-25]・Al のエッチング [9-25]・洗浄台 [9-14]・検査台。
     * 実機：マスクアライナー [9-20]（IMP2 O-011、密着）。代わり（まだ機械がない）：レジスト塗布 [9-19]・現像 [9-21]・レジストはがし（ChipOps.coat / develop / strip）。
     * マスクは設計図から直接（MaskData。製図台・縮小カメラ [7-11] でも作れるが、ここでは通さない）
     */
    /** [9-20] マスクアライナー（IMP2 O-011）：密着はくっきりだがマスクが傷む、近接 20µm は傷まずに少しぼける、80µm はぼけて焼けない */
    @GameTest
    public void mask_aligner(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "mask_aligner", com.taku3.semiconductorlab.content.Optics.MERCURY_LAMP_TUBE) && recipeUses(h, "mask_aligner", Precision.COMPOUND_MICROSCOPE),
                "mask aligner = mercury lamp + alignment microscope + screw stage");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.blurUm(0) - 0.436) < 1e-9 && Math.abs(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.blurUm(20) - Math.sqrt(0.436 * 20)) < 1e-9, "blur: contact ~ lambda, proximity sqrt(lambda g)");
        h.assertTrue(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.sharpEnough(0) && com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.sharpEnough(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.GAP_NEAR_UM) && !com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.sharpEnough(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.GAP_FAR_UM), "80 um gap blurs beyond half the 10 um line");
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack ox = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        ox.set(ModDataComponents.DOPANT, "boron");
        ItemStack coated = ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(ox), "coat (stand-in for [9-19])");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.WATTS);
        int[] gapsSeen = new int[3];
        for (int g = 0; g < 3; g++) {
            BlockPos at = p(1 + 2 * g, 1, 1);
            h.setBlock(at, Semiconductor.MASK_ALIGNER.block);
            var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.class);
            for (int k = 0; k < g; k++) be.onButton(player, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.BTN_GAP);
            gapsSeen[g] = be.gapUm();
            ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTOMASK);
            mask.set(ModDataComponents.MASK, design.mask(0));
            be.getContainer().setItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_WAFER, coated.copy());
            be.getContainer().setItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_MASK, mask);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.TICKS + 10, full);
            ItemStack out = be.getContainer().getItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_OUT);
            int used = be.getContainer().getItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_MASK).getDamageValue();
            if (g == 0) h.assertTrue(!out.isEmpty() && used == 1, "contact: exposed, mask worn by 1");
            if (g == 1) h.assertTrue(!out.isEmpty() && used == 0, "proximity 20 um: exposed, mask not worn");
            if (g == 2) h.assertTrue(out.isEmpty(), "proximity 80 um: too blurred, nothing printed");
        }
        h.assertTrue(gapsSeen[0] == 0 && gapsSeen[1] == com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.GAP_NEAR_UM && gapsSeen[2] == com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.GAP_FAR_UM, "the button cycles contact -> 20 -> 80 um");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"power", "gap_contact", "gap", "blur", "mask_left", "need_wafer", "need_mask", "too_blurry", "mask_worn", "cannot", "output_full", "need_power", "running", "switch_gap"})
            h.assertTrue(ja.has("gui.semiconductor-lab.mask_aligner." + k), "lang mask_aligner." + k);
        LOG.info("[LAB] mask_aligner [9-20]: contact sharp but wears the mask, 20 um proximity spares it (blur 3.0 um), 80 um too blurred (5.9 um)");
        h.succeed();
    }

    /** [8-17] アライメント（IMP2 O-014）：前の層に合わせるずれ＝台の送り・顕微鏡の分解能・床の揺れの一番大きいもの。2層目の露光でチップに残る（A の ChipState.overlayNm） */
    @GameTest
    public void alignment(GameTestHelper h) {
        h.assertValueEqual(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(ItemStack.EMPTY, 0), 20_000, "hand stage: 20 um");
        h.assertValueEqual(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(new ItemStack(Precision.LEAD_SCREW), 0), 10_000, "lead screw: 10 um");
        ItemStack ball3 = com.taku3.semiconductorlab.item.BallBearingItem.of(3).transmuteCopy(Precision.BALL_SCREW);
        h.assertValueEqual(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(ball3, 0), 1_000, "ball screw grade 3: 1 um");
        h.assertValueEqual(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(ball3, 4_000), 4_000, "a moving piston nearby: the floor shake wins");
        // 1層目を焼いてから、2層目をマスクアライナー（ボールねじ段階3）で
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        w.set(ModDataComponents.DOPANT, "boron");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(w), "coat 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.expose(w, design.mask(0), com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.TOOL), "expose 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.develop(w), "develop 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w), "etch 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.strip(w), "strip 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.diffuse(w, design.layers().get(0).kind().dopes), "diffuse 0");
        w = ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(w), "coat 1");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.MASK_ALIGNER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.class);
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTOMASK);
        mask.set(ModDataComponents.MASK, design.mask(1));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_WAFER, w);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_MASK, mask);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_STAGE, ball3);
        tickConsumer(h, at, be, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.TICKS + 10, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.WATTS));
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(be.getContainer().getItem(com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.overlayNm() == 3_300, "layer 2 overlay recorded on the chip (no air conditioning: thermal 3.3 um wins over the 1 um screw): " + (st == null ? "none" : st.overlayNm()));
        h.assertTrue(langJson("ja_jp").has("gui.semiconductor-lab.mask_aligner.overlay"), "lang overlay");
        LOG.info("[LAB] alignment [8-17]: overlay hand 20 / lead 10 / ball screw 1 um (microscope 0.31 um, vibration); layer 2 records 3.3 um on the chip (thermal, no air conditioning)");
        h.succeed();
    }

    /** [9-39] ステッパー段階1（IMP2 S-004）：投影レンズで方眼16（g線）のクロムマスクを、ボタンで1チップずつ4回送って焼く。マスクは傷まない。ぼけたレンズでは焼けない */
    @GameTest(maxTicks = 100)
    public void stepper(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "stepper", Semiconductor.MASK_ALIGNER.block.asItem()), "stepper grows out of the mask aligner");
        var fine = new com.taku3.semiconductorlab.litho.MaskData(com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G.grid, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G.grid), "stepper_test", 0);   // g線ステッパーの方眼（S-008 で 36）
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, fine);
        h.assertFalse(com.taku3.semiconductorlab.litho.ChipOps.canExpose(ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"), fine, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G),
                "a g-line stepper mask is too fine for contact printing");
        ItemStack lens = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS);
        lens.set(Precision.LENS_DECENTER, java.util.List.of(5, 5, 5, 5));
        h.assertFalse(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"), fine, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G,
                com.taku3.semiconductorlab.block.StepperBlockEntity.cdOf(new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS)), 0).ok(), "an uncentred lens is too blurred for this mask (S-010: grid from the lens CD)");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.STEPPER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.StepperBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK, mask);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, lens);
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.StepperBlockEntity.WATTS);
        tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 5, full);
        h.assertValueEqual(be.stepsDone(), 0, "nothing happens until the step button is pressed (stage 1: hand stepping)");
        for (int k = 1; k <= com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "step " + k + " starts");
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 2, full);
        }
        ItemStack out = be.getContainer().getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_OUT);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(out);
        h.assertTrue(st != null && st.resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.EXPOSED && st.size() == com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G.grid, "4 hand steps -> wafer exposed at the stepper grid");
        h.assertValueEqual(be.getContainer().getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK).getDamageValue(), 0, "projection: the mask is never touched");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"power", "steps", "cd", "step", "need_wafer", "need_mask", "need_lens", "blurred", "cannot", "output_full", "press", "need_power", "running"})
            h.assertTrue(ja.has("gui.semiconductor-lab.stepper." + k), "lang stepper." + k);
        LOG.info("[LAB] stepper [9-39] stage 1: projection lens CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(lens) + " nm, 4 hand steps -> grid " + com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G.grid + " (contact max " + com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G.grid + "), mask untouched");
        h.succeed();
    }

    private static ItemStack oxidized() {
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER);
        w.set(ModDataComponents.DOPANT, "boron");
        return w;
    }

    /** O-017 ステッパーの i線（IMP2）：i線フィルター＋i線のレンズ（合成石英、NA 0.7）で方眼 48。g線のガラスのレンズは i線に使えない。i線は心出しがもっと要る */
    @GameTest(maxTicks = 100)
    public void stepper_i_line(GameTestHelper h) {
        var opt = com.taku3.semiconductorlab.content.Optics.class;
        ItemStack good = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_I), rough = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_I);
        good.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        rough.set(Precision.LENS_DECENTER, java.util.List.of(10, 10, 10, 10));
        h.assertTrue(com.taku3.semiconductorlab.item.ProjectionLensItem.meetsTool(good), "i-line lens centred to the microscope limit (1.6 um total) -> CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(good) + " nm <= 350");
        h.assertFalse(com.taku3.semiconductorlab.item.ProjectionLensItem.meetsTool(rough), "1 um per lens is enough for g-line but not i-line: CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(rough));
        ItemStack filter = new ItemStack(com.taku3.semiconductorlab.content.Optics.I_LINE_FILTER);
        ItemStack gLens = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS);
        gLens.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        h.assertFalse(com.taku3.semiconductorlab.block.StepperBlockEntity.lensMatches(gLens, filter), "a glass g-line lens absorbs i-line");
        h.assertTrue(com.taku3.semiconductorlab.block.StepperBlockEntity.toolFor(filter) == com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I && com.taku3.semiconductorlab.block.StepperBlockEntity.toolFor(ItemStack.EMPTY) == com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G, "the filter switches the light");
        int g = com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I.grid;
        var fine = new com.taku3.semiconductorlab.litho.MaskData(g, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(g), "i_test", 0);
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, fine);
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.STEPPER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.StepperBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK, mask);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, good);
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "g-line (no filter) cannot print a grid-" + g + " mask");
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_FILTER, filter);
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.StepperBlockEntity.WATTS);
        for (int k = 1; k <= com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "i-line step " + k);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 2, full);
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(be.getContainer().getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.size() == g, "i-line stepper prints grid " + g);
        h.assertTrue(langJson("ja_jp").has("gui.semiconductor-lab.stepper.lens_wrong"), "lang lens_wrong");
        LOG.info("[LAB] stepper i-line (O-017): filter + quartz lens NA 0.7 -> CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(good) + " nm, grid " + g + " (g-line 36); g-line glass lens refused");
        h.succeed();
    }

    /** [7-20] KrF エキシマレーザー（IMP2 O-018）：レーザー＋KrF のレンズ（石英、NA 0.8・k1 0.35）でステッパーが方眼 68。1チップでガスを1使う */
    @GameTest(maxTicks = 100)
    public void stepper_krf(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "krf_laser", com.taku3.semiconductorlab.content.Gas.FLUORINE_CYLINDER) && recipeUses(h, "krf_laser", com.taku3.semiconductorlab.content.Gas.KRYPTON_CYLINDER)
                && recipeUses(h, "krf_laser", com.taku3.semiconductorlab.content.Gas.NEON_CYLINDER), "KrF laser gas = Kr + F2 diluted in Ne");
        ItemStack good = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_KRF), rough = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_KRF);
        good.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        rough.set(Precision.LENS_DECENTER, java.util.List.of(10, 10, 10, 10));
        h.assertTrue(com.taku3.semiconductorlab.item.ProjectionLensItem.meetsTool(good) && !com.taku3.semiconductorlab.item.ProjectionLensItem.meetsTool(rough), "KrF lens: " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(good) + " nm ok, " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(rough) + " nm too blurred (130 target)");
        int g = com.taku3.semiconductorlab.litho.ExposureTool.KRF.grid;
        var fine = new com.taku3.semiconductorlab.litho.MaskData(g, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(g), "krf_test", 0);
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, fine);
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.STEPPER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.StepperBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK, mask);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, good);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_FILTER, new ItemStack(com.taku3.semiconductorlab.content.Optics.KRF_LASER));
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.StepperBlockEntity.WATTS);
        for (int k = 1; k <= com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "KrF step " + k);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 2, full);
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(be.getContainer().getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.size() == g, "KrF stepper prints grid " + g);
        h.assertValueEqual(be.getContainer().getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_FILTER).getDamageValue(), com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS, "one gas shot per chip");
        h.assertTrue(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(com.taku3.semiconductorlab.block.StepperBlockEntity.cdOf(good))
                > com.taku3.semiconductorlab.litho.ExposureTool.gridFor(com.taku3.semiconductorlab.block.StepperBlockEntity.cdOf(rough)), "a better-centred lens prints finer masks (S-010)");
        LOG.info("[LAB] stepper KrF [7-20]: laser + quartz lens NA 0.8 k1 0.35 -> CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(good) + " nm, grid " + g + "; gas used " + com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS + "/" + com.taku3.semiconductorlab.content.Optics.KRF_LASER_SHOTS);
        h.succeed();
    }

    /** [7-20] ArF・[7-16] 液浸（IMP2 S-005 v0.5）：ArF レーザー＋CaF2 入りのレンズ（NA 0.85）、液浸のレンズ（水で NA 1.35）はもっと細かいマスクが焼ける。液浸は超純水がないと焼けず、1チップで1本使う */
    @GameTest(maxTicks = 200)
    public void stepper_arf_immersion(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "arf_laser", com.taku3.semiconductorlab.content.Gas.ARGON_CYLINDER) && recipeUses(h, "arf_laser", com.taku3.semiconductorlab.content.Gas.FLUORINE_CYLINDER), "ArF laser = Ar + F2 in Ne");
        h.assertTrue(com.taku3.semiconductorlab.block.StepperBlockEntity.LAYOUT.slots().stream().anyMatch(sl -> sl.index() == com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS && sl.accepts().test(new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_KRF))), "the lens slot takes every projection lens");
        ItemStack dry = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_ARF), wet = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_IMMERSION);
        dry.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        wet.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        int gDry = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(dry)), gWet = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(wet));
        h.assertTrue(gWet > gDry, "water raises NA: CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(dry) + " -> " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(wet) + " nm, grid " + gDry + " -> " + gWet);
        var fine = new com.taku3.semiconductorlab.litho.MaskData(gWet, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(gWet), "immersion_test", 0);
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, fine);
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.STEPPER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.StepperBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK, mask);
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_FILTER, new ItemStack(com.taku3.semiconductorlab.content.Optics.ARF_LASER));
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, dry);
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "the dry ArF lens is too blurred for grid " + gWet);
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, wet);
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "immersion without ultrapure water cannot print");
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WATER, new ItemStack(com.taku3.semiconductorlab.content.Chemistry.ULTRAPURE_WATER, 2));
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.StepperBlockEntity.WATTS);
        for (int k = 1; k <= com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "immersion step " + k);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 2, full);
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(c.getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.size() == gWet, "ArF immersion prints grid " + gWet);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WATER).getCount(), 1, "one bottle of ultrapure water per chip");
        LOG.info("[LAB] ArF [7-20] / immersion [7-16]: dry NA 0.85 CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(dry) + " nm grid " + gDry + "; water NA 1.35 CD " + com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(wet) + " nm grid " + gWet);
        h.succeed();
    }

    /** [9-39] スキャナー（IMP2 O-021）：スリットでレンズの細い帯だけを使うので、同じレンズでステッパーより CD が細く、細かいマスクが焼ける。マスクの台のねじが要る */
    @GameTest(maxTicks = 200)
    public void scanner(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "scanner", Semiconductor.STEPPER.block.asItem()) && recipeUses(h, "scanner", Precision.BALL_SCREW), "scanner = stepper + ball screws for the mask stage");
        ItemStack lens = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_ARF);
        lens.set(Precision.LENS_DECENTER, java.util.List.of(20, 20, 20, 20));
        int stepCd = com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(lens), scanCd = com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(lens, com.taku3.semiconductorlab.block.ScannerBlockEntity.SLIT_ABERRATION);
        int gStep = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(stepCd), gScan = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(scanCd);
        h.assertTrue(scanCd < stepCd && gScan > gStep, "same lens: stepper CD " + stepCd + " nm (grid " + gStep + "), scanner slit " + scanCd + " nm (grid " + gScan + ")");
        var fine = new com.taku3.semiconductorlab.litho.MaskData(gScan, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(gScan), "scanner_test", 0);
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, fine);
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.SCANNER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.ScannerBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        c.setItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_MASK, mask);
        c.setItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_LENS, lens);
        c.setItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_FILTER, new ItemStack(com.taku3.semiconductorlab.content.Optics.ARF_LASER));
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.ScannerBlockEntity.BTN_STEP), "no mask stage -> cannot scan");
        c.setItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_MASK_STAGE, new ItemStack(Precision.LEAD_SCREW));
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ScannerBlockEntity.WATTS);
        for (int k = 1; k <= com.taku3.semiconductorlab.block.ScannerBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.ScannerBlockEntity.BTN_STEP), "scan " + k);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.ScannerBlockEntity.STEP_TICKS + 2, full);
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(c.getItem(com.taku3.semiconductorlab.block.ScannerBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.size() == gScan, "the scanner prints grid " + gScan + " that the stepper cannot with this lens");
        h.assertTrue(be.overlayNm() >= com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(new ItemStack(Precision.LEAD_SCREW), 0) / com.taku3.semiconductorlab.block.ScannerBlockEntity.REDUCTION, "mask-stage error enters at 1/4");
        LOG.info("[LAB] scanner [9-39]: slit uses a thin band of the lens, CD " + stepCd + " -> " + scanCd + " nm, grid " + gStep + " -> " + gScan);
        h.succeed();
    }

    /** v0.5 マルチパターニング（IMP2 O-022、A の判定 4e30fd22）：ArF のレンズでは焼けない細かい原図も、縮小カメラで2つに分けると1枚ずつステッパーで焼ける（LELE） */
    @GameTest(maxTicks = 200)
    public void stepper_lele(GameTestHelper h) {
        ItemStack lens = new ItemStack(com.taku3.semiconductorlab.content.Optics.PROJECTION_LENS_ARF);
        lens.set(Precision.LENS_DECENTER, java.util.List.of(4, 4, 4, 4));
        int gLens = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(lens)), g = gLens + 24;
        var cells = com.taku3.semiconductorlab.litho.BitGrid.empty(g);
        for (int x = 0; x < g; x++) for (int y = 0; y < g; y += 3) cells = cells.with(x, y, true);
        var dense = new com.taku3.semiconductorlab.litho.MaskData(g, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, cells, "lele_test", 0);
        var half1 = com.taku3.semiconductorlab.block.ReductionCameraBlockEntity.partOf(dense, 2, 0); var half2 = com.taku3.semiconductorlab.block.ReductionCameraBlockEntity.partOf(dense, 2, 1);
        int cd = com.taku3.semiconductorlab.item.ProjectionLensItem.cdNm(lens);
        h.assertFalse(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"), dense, com.taku3.semiconductorlab.litho.ExposureTool.ARF, cd, 0).ok(), "the dense original (grid " + g + ") is too fine for grid " + gLens);
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"), half1, com.taku3.semiconductorlab.litho.ExposureTool.ARF, cd, 0).ok()
                && com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"), half2, com.taku3.semiconductorlab.litho.ExposureTool.ARF, cd, 0).ok(), "each half (pitch doubled) prints");
        ItemStack mask = new ItemStack(com.taku3.semiconductorlab.content.Optics.CHROME_PHOTOMASK);
        mask.set(ModDataComponents.MASK, half1);
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Semiconductor.STEPPER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.StepperBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_WAFER, ok(h, com.taku3.semiconductorlab.litho.ChipOps.coat(oxidized()), "coat"));
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_MASK, mask);
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_LENS, lens);
        c.setItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_FILTER, new ItemStack(com.taku3.semiconductorlab.content.Optics.ARF_LASER));
        long full = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.StepperBlockEntity.WATTS);
        for (int k = 1; k <= com.taku3.semiconductorlab.block.StepperBlockEntity.STEPS; k++) {
            h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.StepperBlockEntity.BTN_STEP), "LELE step " + k);
            tickConsumer(h, at, be, com.taku3.semiconductorlab.block.StepperBlockEntity.STEP_TICKS + 2, full);
        }
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(c.getItem(com.taku3.semiconductorlab.block.StepperBlockEntity.SLOT_OUT));
        h.assertTrue(st != null && st.size() == g, "the stepper prints half 1/2 at grid " + g + " (lens alone: " + gLens + ")");
        LOG.info("[LAB] LELE [v0.5 #6]: ArF lens grid " + gLens + ", dense original grid " + g + " splits into 1/2 + 2/2 that each print");
        h.succeed();
    }

    @GameTest(maxTicks = 200)
    public void v03Chain(GameTestHelper h) {
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        var tool = com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G;
        // 酸化炉（実機）
        ItemStack w = new ItemStack(Semiconductor.SILICON_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        w = runConsumer(h, p(1, 1, 1), Semiconductor.OXIDATION_FURNACE.block,
                new int[] {com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT},
                new ItemStack[] {w, new ItemStack(Gas.OXYGEN_GAS, 1)}, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT,
                com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS);
        h.assertTrue(w.is(Semiconductor.OXIDIZED_WAFER) && w.getCount() == 8, "oxidation furnace -> 8 oxidized wafers, got " + w);
        int step = 0;
        for (int i = 0; i < design.layers().size(); i++) {
            var k = design.layers().get(i).kind();
            // レジスト塗布機 [9-19]（実機、E）
            w = runConsumer(h, p(7, 1, 4), Semiconductor.SPIN_COATER.block, new int[] {com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_RESIST},
                    new ItemStack[] {w, new ItemStack(Chemistry.POSITIVE_RESIST)}, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WATTS);
            h.assertTrue(w.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.COATED,
                    "spin coater coats layer " + i + ", got " + w);
            // マスクアライナー [9-20]（実機、密着）：設計図の層のマスクをフォトマスクにして重ねる
            ItemStack photomask = new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTOMASK);
            photomask.set(ModDataComponents.MASK, design.mask(i));
            w = runConsumer(h, p(7, 1, 1), Semiconductor.MASK_ALIGNER.block, new int[] {com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_MASK},
                    new ItemStack[] {w, photomask}, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.WATTS);
            h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.EXPOSED,
                    "mask aligner (contact) exposes layer " + i);
            // 現像槽 [9-21]（実機、E）
            w = runConsumer(h, p(7, 1, 6), Semiconductor.DEVELOPER_BENCH.block, new int[] {com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_DEV},
                    new ItemStack[] {w, new ItemStack(Chemistry.DEVELOPER)}, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WATTS);
            h.assertTrue(w.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.DEVELOPED,
                    "developer bench develops layer " + i + ", got " + w);
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL) {
                w = runEtch(h, p(3, 1, 1), w, Chemistry.PHOSPHORIC_ACID);   // Al のエッチング（実機）
                h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w).has(k), "etch tank + phosphoric acid -> metal layer");
                w = runConsumer(h, p(1, 1, 6), Semiconductor.RESIST_STRIPPER.block, new int[] {com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE},
                        new ItemStack[] {w, new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.HYDROGEN_PEROXIDE)}, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS);   // レジストはがし槽（実機、E）
            } else {
                w = runEtch(h, p(3, 1, 1), w, Chemistry.HYDROFLUORIC_ACID);   // 酸化膜の窓（実機）
                h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).hasWindows(), "etch tank + HF -> windows, layer " + i);
                w = runConsumer(h, p(1, 1, 6), Semiconductor.RESIST_STRIPPER.block, new int[] {com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE},
                        new ItemStack[] {w, new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.HYDROGEN_PEROXIDE)}, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS);   // レジストはがし槽（実機、E）
                if (k.isDiffusion()) {   // 拡散炉（実機）
                    var src = new ItemStack(k.dopes == 'n' ? Chemistry.PHOSPHORUS : Metallurgy.BORON_TRIOXIDE);
                    w = runConsumer(h, p(5, 1, 1), Semiconductor.DIFFUSION_FURNACE.block,
                            new int[] {com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE},
                            new ItemStack[] {w, src}, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT,
                            com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS);
                    h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).diffusions() == ++step, "diffusion furnace through windows, layer " + i);
                } else {   // 真空蒸着装置（実機、隣に 0.001 Pa の真空容器）
                    h.setBlock(p(2, 1, 4), Vacuum.VACUUM_CHAMBER.block);
                    h.getBlockEntity(p(2, 1, 4), com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class).setPressure(0.001);
                    w = runConsumer(h, p(1, 1, 4), Vacuum.EVAPORATOR.block,
                            new int[] {com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL},
                            new ItemStack[] {w, new ItemStack(Metallurgy.ALUMINIUM_INGOT, 1)}, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT,
                            com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS);
                    h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).metalFilm(), "evaporator -> aluminium film over the contacts");
                }
            }
        }
        int dirt = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination();
        // 洗浄台（実機）
        w = runConsumer(h, p(5, 1, 4), Semiconductor.CLEANING_BENCH.block,
                new int[] {com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_NITRIC,
                        com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_HF, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WATER},
                new ItemStack[] {w, new ItemStack(Gas.NITRIC_ACID), new ItemStack(Chemistry.HYDROFLUORIC_ACID), new ItemStack(Chemistry.DISTILLED_WATER)},
                com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WATTS);
        h.assertTrue(dirt > 0 && com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination() <= com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.STAGE1_TARGET, "stage-1 cleaning bench (nitric + HF leave a little metal): dirt " + dirt + " -> 0");
        // 検査台（実機）
        h.setBlock(p(3, 1, 6), Semiconductor.PROBE_STATION.block);
        var probe = h.getBlockEntity(p(3, 1, 6), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        probe.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, w);
        probe.onButton(h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE);
        h.assertValueEqual(probe.found(), (1 << com.taku3.semiconductorlab.litho.Discoveries.all().size()) - 1, "the probe finds resistor, diode, transistor, IC and NOT on the chip made by the machines");
        LOG.info("[LAB] v0.3 chain: oxidation, mask aligner, etch (HF / phosphoric), diffusion x3, evaporator, cleaning bench, probe = real machines; spin coater, developer bench, resist stripper = real machines too (no ChipOps stand-ins); NOT found (dirt {} -> 0)", dirt);
        h.succeed();
    }

    /** v0.3 の通しのネガ型版（IMP4-01、INBOX O-009）：A の v03Chain と同じ実機の並びで、レジストを KPR、現像液をキシレンにしても NOT が見つかる */
    @GameTest(maxTicks = 200)
    public void v03ChainNegative(GameTestHelper h) {
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        var tool = com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G;
        // 酸化炉（実機）
        ItemStack w = new ItemStack(Semiconductor.SILICON_WAFER, 8);
        w.set(ModDataComponents.DOPANT, "boron");
        w = runConsumer(h, p(1, 1, 1), Semiconductor.OXIDATION_FURNACE.block,
                new int[] {com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OXIDANT},
                new ItemStack[] {w, new ItemStack(Gas.OXYGEN_GAS, 1)}, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.SLOT_OUT,
                com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity.WATTS);
        h.assertTrue(w.is(Semiconductor.OXIDIZED_WAFER) && w.getCount() == 8, "oxidation furnace -> 8 oxidized wafers, got " + w);
        int step = 0;
        for (int i = 0; i < design.layers().size(); i++) {
            var k = design.layers().get(i).kind();
            // レジスト塗布機 [9-19]（実機、E）
            w = runConsumer(h, p(7, 1, 4), Semiconductor.SPIN_COATER.block, new int[] {com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_RESIST},
                    new ItemStack[] {w, new ItemStack(Chemistry.NEGATIVE_RESIST)}, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WATTS);
            h.assertTrue(w.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.COATED,
                    "spin coater coats layer " + i + ", got " + w);
            h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.tone(w) == com.taku3.semiconductorlab.litho.ChipState.Tone.NEGATIVE, "KPR coats negative tone, layer " + i);
            // マスクアライナー [9-20]（実機、密着）：設計図の層のマスクをフォトマスクにして重ねる
            ItemStack photomask = new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTOMASK);
            photomask.set(ModDataComponents.MASK, design.mask(i));
            w = runConsumer(h, p(7, 1, 1), Semiconductor.MASK_ALIGNER.block, new int[] {com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_MASK},
                    new ItemStack[] {w, photomask}, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.WATTS);
            h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.EXPOSED,
                    "mask aligner (contact) exposes layer " + i);
            // 現像槽 [9-21]（実機、E）
            w = runConsumer(h, p(7, 1, 6), Semiconductor.DEVELOPER_BENCH.block, new int[] {com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_DEV},
                    new ItemStack[] {w, new ItemStack(Chemistry.XYLENE)}, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WATTS);
            h.assertTrue(w.getCount() == 8 && com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).resist() == com.taku3.semiconductorlab.litho.ChipState.Resist.DEVELOPED,
                    "developer bench develops layer " + i + ", got " + w);
            if (k == com.taku3.semiconductorlab.litho.LayerKind.METAL) {
                w = runEtch(h, p(3, 1, 1), w, Chemistry.PHOSPHORIC_ACID);   // Al のエッチング（実機）
                h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w).has(k), "etch tank + phosphoric acid -> metal layer");
                w = runConsumer(h, p(1, 1, 6), Semiconductor.RESIST_STRIPPER.block, new int[] {com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE},
                        new ItemStack[] {w, new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.HYDROGEN_PEROXIDE)}, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS);   // レジストはがし槽（実機、E）
            } else {
                w = runEtch(h, p(3, 1, 1), w, Chemistry.HYDROFLUORIC_ACID);   // 酸化膜の窓（実機）
                h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).hasWindows(), "etch tank + HF -> windows, layer " + i);
                w = runConsumer(h, p(1, 1, 6), Semiconductor.RESIST_STRIPPER.block, new int[] {com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_ACID, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_PEROXIDE},
                        new ItemStack[] {w, new ItemStack(Chemistry.SULFURIC_ACID), new ItemStack(Chemistry.HYDROGEN_PEROXIDE)}, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS);   // レジストはがし槽（実機、E）
                if (k.isDiffusion()) {   // 拡散炉（実機）
                    var src = new ItemStack(k.dopes == 'n' ? Chemistry.PHOSPHORUS : Metallurgy.BORON_TRIOXIDE);
                    w = runConsumer(h, p(5, 1, 1), Semiconductor.DIFFUSION_FURNACE.block,
                            new int[] {com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_SOURCE},
                            new ItemStack[] {w, src}, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SLOT_OUT,
                            com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS);
                    h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).diffusions() == ++step, "diffusion furnace through windows, layer " + i);
                } else {   // 真空蒸着装置（実機、隣に 0.001 Pa の真空容器）
                    h.setBlock(p(2, 1, 4), Vacuum.VACUUM_CHAMBER.block);
                    h.getBlockEntity(p(2, 1, 4), com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class).setPressure(0.001);
                    w = runConsumer(h, p(1, 1, 4), Vacuum.EVAPORATOR.block,
                            new int[] {com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL},
                            new ItemStack[] {w, new ItemStack(Metallurgy.ALUMINIUM_INGOT, 1)}, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT,
                            com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS);
                    h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w) != null && com.taku3.semiconductorlab.litho.ChipOps.state(w).metalFilm(), "evaporator -> aluminium film over the contacts");
                }
            }
        }
        int dirt = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination();
        // 洗浄台（実機）
        w = runConsumer(h, p(5, 1, 4), Semiconductor.CLEANING_BENCH.block,
                new int[] {com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WAFER, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_NITRIC,
                        com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_HF, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_WATER},
                new ItemStack[] {w, new ItemStack(Gas.NITRIC_ACID), new ItemStack(Chemistry.HYDROFLUORIC_ACID), new ItemStack(Chemistry.DISTILLED_WATER)},
                com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.SLOT_OUT, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.TICKS + 10, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WATTS);
        h.assertTrue(dirt > 0 && com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination() <= com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.STAGE1_TARGET, "stage-1 cleaning bench (nitric + HF leave a little metal): dirt " + dirt + " -> 0");
        // 検査台（実機）
        h.setBlock(p(3, 1, 6), Semiconductor.PROBE_STATION.block);
        var probe = h.getBlockEntity(p(3, 1, 6), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        probe.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, w);
        probe.onButton(h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE);
        h.assertValueEqual(probe.found(), (1 << com.taku3.semiconductorlab.litho.Discoveries.all().size()) - 1, "the probe finds resistor, diode, transistor, IC and NOT on the chip made by the machines");
        LOG.info("[LAB] v0.3 chain (negative KPR, xylene developer, IMP4 O-009): oxidation, mask aligner, etch (HF / phosphoric), diffusion x3, evaporator, cleaning bench, probe = real machines; spin coater, developer bench, resist stripper = real machines too (no ChipOps stand-ins); NOT found (dirt {} -> 0)", dirt);
        h.succeed();
    }


    /**
     * 大きな方眼の回路の計算（O-006）：疎な解き方（BiCGSTAB）でも 8×8 の NOT の答えは密な解き方と同じ。
     * 24×24・72×72 に1本の長い抵抗の帯：電流は 5 V ÷（1 kΩ/□ ×（長さ−1）＋窓2つ）、どちらも速く解ける
     */
    @GameTest(maxTicks = 200)
    public void largeCircuit(GameTestHelper h) {
        var nl8 = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.LithoExamples.notGate(), "p"));
        int[] pins = com.taku3.semiconductorlab.litho.Circuit.notPins(nl8);
        double denseHi = com.taku3.semiconductorlab.litho.Circuit.notOutput(nl8, pins, false), denseLo = com.taku3.semiconductorlab.litho.Circuit.notOutput(nl8, pins, true);
        int keep = com.taku3.semiconductorlab.litho.Circuit.DENSE_MAX;
        double sparseHi, sparseLo;
        try { com.taku3.semiconductorlab.litho.Circuit.DENSE_MAX = 0; sparseHi = com.taku3.semiconductorlab.litho.Circuit.notOutput(nl8, pins, false); sparseLo = com.taku3.semiconductorlab.litho.Circuit.notOutput(nl8, pins, true); }
        finally { com.taku3.semiconductorlab.litho.Circuit.DENSE_MAX = keep; }
        h.assertTrue(Math.abs(denseHi - sparseHi) < 1e-3 && Math.abs(denseLo - sparseLo) < 1e-3,
                String.format("sparse = dense on the 8x8 NOT: %.4f/%.4f vs %.4f/%.4f", denseHi, denseLo, sparseHi, sparseLo));
        for (int n : new int[] {24, 72}) {
            var strip = com.taku3.semiconductorlab.litho.BitGrid.empty(n); var contact = com.taku3.semiconductorlab.litho.BitGrid.empty(n); var metal = com.taku3.semiconductorlab.litho.BitGrid.empty(n);
            for (int x = 0; x < n; x++) strip = strip.with(x, 1, true);
            contact = contact.with(0, 1, true).with(n - 1, 1, true);
            metal = metal.with(0, 1, true).with(n - 1, 1, true);
            var d = new com.taku3.semiconductorlab.litho.ChipDesign(n, "strip", java.util.List.of(new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, strip),
                    new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.CONTACT, contact), new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.METAL, metal)));
            var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(d, "p"));
            h.assertValueEqual(nl.pads().size(), 2, n + "x" + n + ": two pads");
            var drive = new java.util.HashMap<Integer, Double>();
            drive.put(nl.pads().get(0).id(), 5.0); drive.put(nl.pads().get(1).id(), 0.0);
            long t0 = System.nanoTime();
            var sol = com.taku3.semiconductorlab.litho.Circuit.solve(nl, drive);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            double expect = 5.0 / (1000.0 * (n - 1) + 2 * 20.0), got = Math.abs(sol.padAmps().get(nl.pads().get(0).id()));
            h.assertTrue(sol.converged() && Math.abs(got - expect) / expect < 0.02, String.format("%dx%d strip: %.4f mA (expect %.4f mA)", n, n, got * 1e3, expect * 1e3));
            h.assertTrue(ms < 5000, n + "x" + n + " solved in " + ms + " ms");
            LOG.info("[LAB] large circuit {}x{}: {} unknowns, {} ms, {} mA", n, n, n * n + nl.nets.size(), ms, String.format("%.4f", got * 1e3));
        }
        h.succeed();
    }

    /**
     * [9-32] ダイシング 段階1（O-013）：仕上がったウェハー → ダイヤモンドのけがき針で8個（まれに7個）のチップ。chip_state を引き継ぎ、検査台で同じ発見。
     * 仕上がっていない（レジストが残る・Al がない）ウェハーは割れない
     */
    @GameTest(maxTicks = 200)
    public void dicing(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER, 2); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack done = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertTrue(com.taku3.semiconductorlab.item.DiamondScribeItem.finished(done), "finished wafer can be scribed");
        h.assertFalse(com.taku3.semiconductorlab.item.DiamondScribeItem.finished(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out()), "a wafer with resist and no wiring is not finished");
        int total = 0;
        var rnd = net.minecraft.util.RandomSource.create(42);
        for (int i = 0; i < 50; i++) {
            ItemStack chips = com.taku3.semiconductorlab.item.DiamondScribeItem.scribe(done, rnd);
            h.assertTrue(chips.is(Semiconductor.CHIP) && chips.getCount() >= 7 && chips.getCount() <= 8, "8 chips, sometimes 7: " + chips);
            h.assertValueEqual(chips.get(ModDataComponents.CHIP_STATE), done.get(ModDataComponents.CHIP_STATE), "chips keep the chip state");
            total += chips.getCount();
        }
        h.assertTrue(total > 50 * 7 && total < 50 * 8, "a few chips break (about 10% of wafers lose one): " + total);
        // 検査台にチップを置く → 同じ発見
        h.setBlock(p(1, 1, 1), Semiconductor.PROBE_STATION.block);
        var probe = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        ItemStack chip = com.taku3.semiconductorlab.item.DiamondScribeItem.scribe(done, rnd);
        h.assertTrue(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.LAYOUT.slots().get(0).accepts().test(chip), "the probe station takes a chip");
        probe.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, chip);
        probe.onButton(h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE);
        h.assertValueEqual(probe.found(), (1 << com.taku3.semiconductorlab.litho.Discoveries.all().size()) - 1, "a chip gives the same discoveries as its wafer");
        h.assertTrue(recipeUses(h, "diamond_scribe", net.minecraft.world.item.Items.DIAMOND), "the scribe has a real diamond point");
        // 段階2：ダイシングソー（ボールねじの台、刃は32枚で減る）。欠けは段階1 より少ない
        int sawTotal = 0;
        for (int i = 0; i < 200; i++) sawTotal += com.taku3.semiconductorlab.block.DicingSawBlockEntity.saw(done, 3, rnd).getCount();
        h.assertTrue(sawTotal > 200 * 8 - 10, "the saw with a grade-3 stage rarely breaks a chip: " + sawTotal + " of " + 200 * 8);
        h.setBlock(p(3, 1, 1), Semiconductor.DICING_SAW.block);
        var saw = h.getBlockEntity(p(3, 1, 1), com.taku3.semiconductorlab.block.DicingSawBlockEntity.class);
        var sc = saw.getContainer();
        sc.setItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_WAFER, done.copyWithCount(2));
        sc.setItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_BLADE, new ItemStack(Semiconductor.DIAMOND_BLADE));
        long sawFeed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DicingSawBlockEntity.WATTS);
        tickConsumer(h, p(3, 1, 1), saw, com.taku3.semiconductorlab.block.DicingSawBlockEntity.TICKS + 5, sawFeed);
        h.assertTrue(sc.getItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_OUT).isEmpty(), "no ball-screw stage -> the saw does not cut");
        ItemStack screw = new ItemStack(com.taku3.semiconductorlab.content.Precision.BALL_SCREW);
        screw.set(com.taku3.semiconductorlab.content.Precision.BEARING_GRADE, 3);
        sc.setItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_STAGE, screw);
        tickConsumer(h, p(3, 1, 1), saw, com.taku3.semiconductorlab.block.DicingSawBlockEntity.TICKS + 5, sawFeed);
        ItemStack sawn = sc.getItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_OUT);
        h.assertTrue(sawn.is(Semiconductor.CHIP) && sawn.getCount() >= 7 && sawn.get(ModDataComponents.CHIP_STATE).equals(done.get(ModDataComponents.CHIP_STATE)), "saw: wafer -> chips with the chip state, got " + sawn);
        h.assertValueEqual(sc.getItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_BLADE).getDamageValue(), 1, "the blade wears by one wafer");
        h.assertTrue(!sc.getItem(com.taku3.semiconductorlab.block.DicingSawBlockEntity.SLOT_STAGE).isEmpty(), "the stage is not used up");
        LOG.info("[LAB] dicing [9-32] stage 2 (saw, grade-3 stage): {} chips of {}", sawTotal, 200 * 8);
        LOG.info("[LAB] dicing [9-32] stage 1: wafer -> {} chips over 50 wafers (8 each, ~10% lose one), chip state kept, probe finds all", total);
        h.succeed();
    }


    /**
     * v0.3b お題（S-005、LITHO_DESIGN §11.1）：真理値表を Circuit.logic で判定。NOT の例は NOT のお題に合格、役目が足りないと missingRoles、
     * 入力と出力を入れちがえると合わない行が出る。検査台でパッドの役目を決めて「調べる」→ お題の進捗 challenge/not
     */
    @GameTest(maxTicks = 200)
    public void challenges(GameTestHelper h) {
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.LithoExamples.notGate(), "p"));
        int[] pins = com.taku3.semiconductorlab.litho.Circuit.notPins(nl);   // {入力, 出力, 電源, 0V}
        var R = com.taku3.semiconductorlab.litho.Circuit.Role.class;
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        roles.put(pins[0], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A); roles.put(pins[1], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        roles.put(pins[2], com.taku3.semiconductorlab.litho.Circuit.Role.VCC); roles.put(pins[3], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        var not = com.taku3.semiconductorlab.litho.Challenge.byIndex(0);
        var r = com.taku3.semiconductorlab.litho.Challenge.check(not, nl, roles, 1.0);
        h.assertTrue(r.passed() && r.rows() == 2, "the NOT example solves the NOT challenge: " + r);
        h.assertTrue(com.taku3.semiconductorlab.litho.Challenge.check(com.taku3.semiconductorlab.litho.Challenge.byIndex(1), nl, roles, 1.0).missingRoles(), "NOR needs input B");
        var swapped = new java.util.HashMap<>(roles);
        swapped.put(pins[0], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1); swapped.put(pins[1], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        var rs = com.taku3.semiconductorlab.litho.Challenge.check(not, nl, swapped, 1.0);
        h.assertTrue(!rs.passed() && !rs.missingRoles(), "input and output swapped -> not every row matches: " + rs);
        // あと少し（§11.2）
        h.assertValueEqual(com.taku3.semiconductorlab.litho.NearMiss.diagnose(not, nl, roles, r, 1.0), "solved", "near-miss: solved");
        var noIn = new java.util.HashMap<>(roles); noIn.remove(pins[0]);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.NearMiss.diagnose(not, nl, noIn, com.taku3.semiconductorlab.litho.Challenge.check(not, nl, noIn, 1.0), 1.0), "roles", "near-miss: roles missing");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.NearMiss.diagnose(not, nl, swapped, rs, 1.0), "input_not_base", "near-miss: the input pad is on the collector, not the base");
        var noVcc = new java.util.HashMap<>(roles); noVcc.put(pins[2], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND); noVcc.put(pins[3], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        String nv = com.taku3.semiconductorlab.litho.NearMiss.diagnose(not, nl, noVcc, com.taku3.semiconductorlab.litho.Challenge.check(not, nl, noVcc, 1.0), 1.0);
        h.assertTrue(!nv.equals("solved"), "near-miss: power reversed is not solved, hint " + nv);
        var and = com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals("and")).findFirst().orElseThrow();
        h.assertTrue(and.inputs() == 2 && java.util.Arrays.equals(and.table(), new int[] {0, 0, 0, 1}), "AND: light only when both get power");
        // O-018 全加算器：3入力（入力C）・2出力。表は A+B+C の2けた
        var fa = com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals("full_adder")).findFirst().orElseThrow();
        for (int row = 0; row < 8; row++) h.assertValueEqual(fa.table()[row], Integer.bitCount(row), "full adder row " + row + " = A+B+C");
        h.assertTrue(com.taku3.semiconductorlab.litho.Challenge.check(fa, nl, roles, 1.0).missingRoles(), "the full adder needs input B, input C and output 2");
        // 検査台：パッドを決めて「調べる」→ 結果は合格
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.setBlock(p(1, 1, 1), Semiconductor.PROBE_STATION.block);
        var probe = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        probe.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, chip);
        var pnl = probe.netlist(); int[] pp = com.taku3.semiconductorlab.litho.Circuit.notPins(pnl);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int[] want = {com.taku3.semiconductorlab.block.ProbeStationBlockEntity.IN_A, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.OUT_1,
                com.taku3.semiconductorlab.block.ProbeStationBlockEntity.VCC, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.GROUND};
        for (int k = 0; k < 4; k++) {
            int padIndex = -1;
            for (int i = 0; i < pnl.pads().size(); i++) if (pnl.pads().get(i).id() == pp[k]) padIndex = i;
            for (int c = 0; c < want[k]; c++) probe.onButton(player, padIndex);
        }
        probe.onButton(player, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE);
        int res = probe.result();
        h.assertTrue(res >= 2 && (res - 2) / 16 == (res - 2) % 16, "the probe station solves the NOT challenge, result " + res);
        h.assertTrue(h.getLevel().getServer().getAdvancements().get(com.taku3.semiconductorlab.SemiconductorLab.id("challenge/not")) != null, "advancement challenge/not exists");
        // ヒント（§11.4）：? で1段ずつ、4段まで。自力の発見・ヒントを開いた記録の進捗がそろっている。歴史の文（§11.5）
        var ja = langJson("ja_jp");
        for (var cd : com.taku3.semiconductorlab.litho.Challenge.all()) {
            for (String s : new String[] {"self/", "hint/", "challenge/"})
                h.assertTrue(h.getLevel().getServer().getAdvancements().get(com.taku3.semiconductorlab.SemiconductorLab.id(s + cd.id())) != null, "advancement " + s + cd.id());
            for (int k = 1; k <= com.taku3.semiconductorlab.block.ProbeStationBlockEntity.HINTS; k++) h.assertTrue(ja.has(cd.langKey() + ".hint" + k), "hint " + k + " of " + cd.id());
            h.assertTrue(ja.has(cd.langKey() + ".history"), "history of " + cd.id());
        }
        for (int k = 0; k < 6; k++) probe.onButton(player, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_HINT);
        h.assertValueEqual(probe.hintLevel(), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.HINTS, "hints open one step at a time, up to 4");
        LOG.info("[LAB] challenges: NOT solved by the NOT example (2/2 rows), NOR needs input B, swapped pins fail some rows; {} challenges defined", com.taku3.semiconductorlab.litho.Challenge.all().size());
        h.succeed();
    }

    /**
     * v0.3b ハンコ（S-005、LITHO_DESIGN §11.3）：お題を解いたチップからハンコ（切り抜いた設計図）。製図台で押すと層が重なり、押した記録（Part）が残る。
     * 押した設計図の原図 → チップにも記録が写る（中身をのぞける）。判定は層から（記号ではない）
     */
    @GameTest(maxTicks = 200)
    public void stamps(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        ItemStack stamp = com.taku3.semiconductorlab.block.ProbeStationBlockEntity.stampOf(chip, "not");
        var sd = stamp.get(ModDataComponents.CHIP_DESIGN);
        h.assertTrue(stamp.is(Semiconductor.STAMP) && sd != null && sd.name().equals("not"), "a stamp named after the challenge");
        var sNl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(sd, "p"));
        h.assertTrue(com.taku3.semiconductorlab.litho.Circuit.worksAsNot(sNl), "the stamp still works as NOT");
        h.setBlock(p(1, 1, 1), Semiconductor.DRAFTING_TABLE.block);
        var dt = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.DraftingTableBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_NEW);
        int i16 = 0;
        while (com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SIZES[i16] != 16) i16++;
        dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_SIZE + i16);
        dt.getContainer().setItem(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SLOT_STAMP, stamp);
        h.assertTrue(dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_STAMP + 3 * 16 + 4), "press the stamp at (4, 3)");
        var d = dt.design();
        h.assertValueEqual(d.parts().size(), 1, "one part recorded");
        h.assertTrue(d.parts().get(0).x() == 4 && d.parts().get(0).y() == 3 && d.parts().get(0).name().equals("not"), "part at (4, 3) named not");
        h.assertTrue(!dt.getContainer().getItem(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SLOT_STAMP).isEmpty(), "the stamp is not used up");
        var nl16 = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(d, "p"));
        h.assertTrue(!nl16.transistors.isEmpty(), "the stamped layers make a transistor (judged from the shapes)");
        var mask = d.mask(0);
        h.assertValueEqual(mask.parts().size(), 1, "the mask carries the part record");
        ItemStack w2 = new ItemStack(Semiconductor.OXIDIZED_WAFER); w2.set(ModDataComponents.DOPANT, "boron");
        var exposed = com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(w2).out(), mask, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(exposed).parts().size(), 1, "the chip remembers the stamp it was drawn with");
        var nested = com.taku3.semiconductorlab.litho.ChipDesign.fromState(com.taku3.semiconductorlab.litho.ChipOps.state(exposed), "nested").crop();
        h.assertTrue(nested.parts().size() == 1 && nested.parts().get(0).stamp().name().equals("not"), "a stamp made from it keeps the inner stamp (hierarchy)");
        var enc = com.taku3.semiconductorlab.litho.ChipDesign.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, nested).getOrThrow();
        var dec = com.taku3.semiconductorlab.litho.ChipDesign.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, enc).getOrThrow();
        h.assertValueEqual(dec, nested, "nested stamps survive saving");
        LOG.info("[LAB] stamps: NOT chip -> stamp ({}x{}), pressed on a 16x16 sheet, part kept on the mask and chip, nested stamp round-trips", sd.size(), sd.size());
        h.succeed();
    }

    /**
     * [9-35] プローバー段階1（O-017）：きれいなウェハーは 8個とも良品、とても汚れたウェハーは悪いチップが出る。台のボールねじがないと動かない。
     * 調べたウェハーをダイシングすると良品だけが出る（印のチップは捨てる）
     */
    @GameTest(maxTicks = 200)
    public void waferProber(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER, 2); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack clean = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        clean = com.taku3.semiconductorlab.litho.ChipOps.clean(clean, 0).out();
        var rnd = net.minecraft.util.RandomSource.create(7);
        h.assertValueEqual(com.taku3.semiconductorlab.block.WaferProberBlockEntity.test(clean, rnd), 0xFF, "a clean wafer: all 8 chips good");
        ItemStack dirty = com.taku3.semiconductorlab.litho.ChipOps.addDust(clean.copy(), 35);
        int bad = 0;
        for (int i = 0; i < 20; i++) bad += 8 - Integer.bitCount(com.taku3.semiconductorlab.block.WaferProberBlockEntity.test(dirty, rnd));
        h.assertTrue(bad > 0, "a very dirty wafer has bad chips (leakage), " + bad + " of 160");
        // 機械：台がないと動かない、あれば 4秒で die_map
        h.setBlock(p(1, 1, 1), Semiconductor.WAFER_PROBER.block);
        var be = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.WaferProberBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.WaferProberBlockEntity.SLOT_WAFER, clean.copy());
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.WaferProberBlockEntity.WATTS);
        tickConsumer(h, p(1, 1, 1), be, com.taku3.semiconductorlab.block.WaferProberBlockEntity.TICKS + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.WaferProberBlockEntity.SLOT_OUT).isEmpty(), "no stage -> no probing");
        ItemStack screw = new ItemStack(com.taku3.semiconductorlab.content.Precision.BALL_SCREW);
        screw.set(com.taku3.semiconductorlab.content.Precision.BEARING_GRADE, 1);
        c.setItem(com.taku3.semiconductorlab.block.WaferProberBlockEntity.SLOT_STAGE, screw);
        tickConsumer(h, p(1, 1, 1), be, com.taku3.semiconductorlab.block.WaferProberBlockEntity.TICKS + 5, feed);
        ItemStack probed = c.getItem(com.taku3.semiconductorlab.block.WaferProberBlockEntity.SLOT_OUT);
        h.assertTrue(probed.getCount() == 1 && probed.get(ModDataComponents.DIE_MAP) != null, "probed wafer carries the die map, got " + probed);
        // ダイシング：印のチップは捨てる
        ItemStack marked = clean.copyWithCount(1);
        marked.set(ModDataComponents.DIE_MAP, 0b00111111);   // 6個良品
        h.assertValueEqual(com.taku3.semiconductorlab.item.DiamondScribeItem.good(marked), 6, "6 good chips on the map");
        int max = 0;
        for (int i = 0; i < 20; i++) max = Math.max(max, com.taku3.semiconductorlab.item.DiamondScribeItem.scribe(marked, rnd).getCount());
        h.assertValueEqual(max, 6, "dicing a probed wafer gives only the good chips");
        h.assertValueEqual(com.taku3.semiconductorlab.item.DiamondScribeItem.scribe(marked, rnd).get(ModDataComponents.DIE_MAP), -1, "chips from a probed wafer are marked good");
        h.assertTrue(com.taku3.semiconductorlab.block.DicingSawBlockEntity.saw(marked, 3, rnd).getCount() <= 6, "the saw also drops inked chips");
        LOG.info("[LAB] wafer prober [9-35]: clean wafer 8/8 good, dirty wafer {} bad of 160, die map -> only good chips diced", bad);
        h.succeed();
    }

    /**
     * S-008（Q16-1 案A）：密着露光の方眼が 16×16 になり、2入力の NOR が本物のチップにできる。16×16 の NOR の例を密着露光で全部の工程に通し、
     * 検査台と同じ役目（0V・電源・入力A/B・出力）で NOR のお題に合格。OR・AND には合わない（形がちがう）
     */
    @GameTest(maxTicks = 200)
    public void norOnContact(GameTestHelper h) {
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G.grid, 16, "contact printing draws 16x16 (S-008)");
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.norGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        chip = com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out();   // 洗浄台のあと（大きなチップは汚れの漏れが効きやすい）
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(chip.get(ModDataComponents.CHIP_STATE)));
        h.assertValueEqual(nl.transistors.size(), 2, "two transistors");
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        int n = nl.model.size;
        java.util.function.BiConsumer<int[], com.taku3.semiconductorlab.litho.Circuit.Role> at = (xy, r) -> roles.put(nl.netOf[xy[1] * n + xy[0]], r);
        at.accept(new int[] {0, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        at.accept(new int[] {11, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        at.accept(new int[] {6, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        at.accept(new int[] {5, 15}, com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        at.accept(new int[] {7, 15}, com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        at.accept(new int[] {9, 15}, com.taku3.semiconductorlab.litho.Circuit.Role.IN_B);
        h.assertValueEqual(roles.size(), 6, "six separate pads");
        java.util.function.Function<String, com.taku3.semiconductorlab.litho.Challenge.Def> def = id -> com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals(id)).findFirst().orElseThrow();
        for (int row = 0; row < 4; row++) {   // 行ごとの出力の電圧（記録）
            var dr = new java.util.HashMap<Integer, Double>();
            for (var e : roles.entrySet()) switch (e.getValue()) {
                case GROUND -> dr.put(e.getKey(), 0.0); case VCC -> dr.put(e.getKey(), 5.0);
                case IN_A -> dr.put(e.getKey(), (row & 2) != 0 ? 5.0 : 0.0); case IN_B -> dr.put(e.getKey(), (row & 1) != 0 ? 5.0 : 0.0); default -> {}
            }
            int outNet = nl.netOf[0 * n + 6];
            LOG.info("[LAB] NOR row {}: out {} V", row, String.format("%.2f", com.taku3.semiconductorlab.litho.Circuit.solve(nl, dr).netVolts(nl, outNet)));
        }
        var r = com.taku3.semiconductorlab.litho.Challenge.check(def.apply("nor"), nl, roles, 1.0);
        h.assertTrue(r.passed(), "the 16x16 NOR made by contact printing solves the NOR challenge: " + r);
        h.assertFalse(com.taku3.semiconductorlab.litho.Challenge.check(def.apply("or"), nl, roles, 1.0).passed(), "it is not an OR");
        LOG.info("[LAB] S-008: 16x16 RTL NOR through contact printing solves NOR ({} rows)", r.rows());
        h.succeed();
    }

    /**
     * S-009（Q18-1 案A）：覚える回路の判定。24×24 の SR ラッチの例を投影の等倍露光で焼き（洗浄のあと）、手順の列（前の手の電圧から計算を始める）で
     * SR ラッチのお題に合格。同じチップを「前の手を覚えない」計算（毎回 0 V から）で見ると、そのままの手で合わない＝記憶は前の電圧の中にある
     */
    @GameTest(maxTicks = 200)
    public void srLatchSequence(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.srLatch(), com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X);
        chip = com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out();
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(chip.get(ModDataComponents.CHIP_STATE)));
        h.assertValueEqual(nl.transistors.size(), 2, "two cross-coupled transistors");
        int n = nl.model.size;
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        java.util.function.BiConsumer<int[], com.taku3.semiconductorlab.litho.Circuit.Role> at = (xy, r) -> roles.put(nl.netOf[xy[1] * n + xy[0]], r);
        at.accept(new int[] {0, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        at.accept(new int[] {17, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        at.accept(new int[] {7, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        at.accept(new int[] {20, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        at.accept(new int[] {23, 8}, com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        at.accept(new int[] {1, 23}, com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        at.accept(new int[] {16, 23}, com.taku3.semiconductorlab.litho.Circuit.Role.IN_B);
        h.assertValueEqual(roles.size(), 7, "seven separate pads");
        var sr = com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals("sr_latch")).findFirst().orElseThrow();
        var r = com.taku3.semiconductorlab.litho.Challenge.check(sr, nl, roles, 1.0);
        h.assertTrue(r.passed(), "the SR latch chip solves the SR latch challenge step by step: " + r);
        h.assertFalse(com.taku3.semiconductorlab.litho.Challenge.check(com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals("nor")).findFirst().orElseThrow(), nl, roles, 1.0).passed(), "it is not a plain NOR");
        LOG.info("[LAB] S-009: 24x24 SR latch made by 1:1 projection remembers its bit ({} steps)", r.rows());
        h.succeed();
    }

    /**
     * [9-28]・[9-29] の土台（E と決めた形）：イオン注入は現像したレジストの開いた所に入り、窓は閉じない。アニールするまで回路に効かない（拡散の層にならない）、
     * アニールすると拡散と同じ層になる。レジストが付いたままアニールはできない
     */
    @GameTest(maxTicks = 100)
    public void implantAndAnneal(GameTestHelper h) {
        var d = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER); w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
        w = com.taku3.semiconductorlab.litho.ChipOps.expose(w, d.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.implant(w, 'n', 1).reason(), "not_developed", "exposed but not developed resist does not mask yet");
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(w).out();
        var r = com.taku3.semiconductorlab.litho.ChipOps.implant(w, 'n', 1);
        h.assertTrue(r.ok(), "implant through the developed resist: " + r.reason());
        w = r.out();
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(w);
        h.assertTrue(st.implanted().size() == 1 && st.diffusions() == 0, "implanted but not active yet");
        h.assertTrue(st.implanted().get(0).cells().equals(d.layers().get(0).cells()), "ions land exactly where the resist was opened (no sideways spread)");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.anneal(w).reason(), "resist_on", "strip the resist before annealing");
        w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
        var a = com.taku3.semiconductorlab.litho.ChipOps.anneal(w);
        h.assertTrue(a.ok(), "anneal");
        st = com.taku3.semiconductorlab.litho.ChipOps.state(a.out());
        h.assertTrue(st.implanted().isEmpty() && st.diffusions() == 1 && st.has(com.taku3.semiconductorlab.litho.LayerKind.DIFF_N), "annealed implant = a diffused layer");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.anneal(a.out()).reason(), "nothing_to_anneal", "nothing left to anneal");
        // [9-26] スパッタ（E と決めた形）：金属の付け方の記録だけ。蒸着とスパッタで回路は同じ
        var annealed = a.out();
        var sput = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(annealed.copy(), true);
        var evap = com.taku3.semiconductorlab.litho.ChipOps.depositMetal(annealed.copy());
        h.assertTrue(sput.ok() && com.taku3.semiconductorlab.litho.ChipOps.state(sput.out()).conformalMetal()
                && !com.taku3.semiconductorlab.litho.ChipOps.state(evap.out()).conformalMetal(), "sputtering is recorded (conformal), evaporation is not");
        LOG.info("[LAB] implant/anneal: ions through the resist opening, inactive until anneal, then a DIFF_N layer");
        h.succeed();
    }

    /**
     * S-010（Q20-1 案A）：レイリーの式で方眼が決まる。CD＝k1·λ/NA を N＝16×(10µm÷CD)^(1/3)（4刻み）へ。
     * g線 NA0.35：k1 0.6〜0.8 → 36、k1 1.0 → 32。i線 NA0.7：k1 0.6 → 52、0.7 → 48。同じ 40 のマスクは、i線の良いレンズでは焼け、g線では焼けない
     */
    @GameTest(maxTicks = 50)
    public void rayleighGrid(GameTestHelper h) {
        java.util.function.BiFunction<Double, Double, Integer> cd = (k1, lna) -> (int) Math.round(k1 * lna);
        int g06 = cd.apply(0.6, 436 / 0.35), g10 = cd.apply(1.0, 436 / 0.35), i06 = cd.apply(0.6, 365 / 0.7), i07 = cd.apply(0.7, 365 / 0.7);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(g06), 36, "g-line NA 0.35, k1 0.6");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(g10), 32, "g-line NA 0.35, k1 1.0 (worse lens)");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(i06), 52, "i-line NA 0.7, k1 0.6");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ExposureTool.gridFor(i07), 48, "i-line NA 0.7, k1 0.7");
        var mask40 = new com.taku3.semiconductorlab.litho.MaskData(40, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, com.taku3.semiconductorlab.litho.BitGrid.empty(40), "", 0);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER); w.set(ModDataComponents.DOPANT, "boron");
        ItemStack c = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(c, mask40, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G, g06, 0).reason(), "too_fine", "a 40 mask is too fine for the best g-line lens");
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(c, mask40, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I, i06, 0).ok(), "the same mask prints with a good i-line lens (shorter wavelength, larger NA)");
        LOG.info("[LAB] Rayleigh grid: g-line CD {} nm -> 36, {} nm -> 32; i-line {} nm -> 52, {} nm -> 48", g06, g10, i06, i07);
        h.succeed();
    }

    /**
     * [B-08] 電卓（S-010）：4ケタの足し算・引き算（あふれると E、C で消す）。検査台でお題に合格したチップには記録が付き、割ったチップへも続く。
     * 電卓の組立台のレシピは、全加算器・レジスタ・カウンタに合格したチップでないと入らない
     */
    @GameTest(maxTicks = 100)
    public void calculator(GameTestHelper h) {
        var R = (java.util.function.Function<String, String>) com.taku3.semiconductorlab.item.CalculatorLogic::run;
        h.assertValueEqual(R.apply("12+34="), "46", "12 + 34");
        h.assertValueEqual(R.apply("100-250="), "-150", "100 - 250");
        h.assertValueEqual(R.apply("9999+1="), "E", "overflow past 4 digits");
        h.assertValueEqual(R.apply("9999+1=C5+5="), "10", "C clears the error");
        h.assertValueEqual(R.apply("12345"), "1234", "only 4 digits can be typed");
        h.assertValueEqual(R.apply("5+3+2="), "10", "chained adding");
        // 検査台：NOT のお題に合格するとチップに記録 → 割ったチップへ
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.notGate(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.setBlock(p(1, 1, 1), Semiconductor.PROBE_STATION.block);
        var probe = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.ProbeStationBlockEntity.class);
        probe.getContainer().setItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER, chip);
        var pnl = probe.netlist(); int[] pp = com.taku3.semiconductorlab.litho.Circuit.notPins(pnl);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int[] want = {com.taku3.semiconductorlab.block.ProbeStationBlockEntity.IN_A, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.OUT_1,
                com.taku3.semiconductorlab.block.ProbeStationBlockEntity.VCC, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.GROUND};
        for (int k = 0; k < 4; k++) {
            int padIndex = -1;
            for (int i = 0; i < pnl.pads().size(); i++) if (pnl.pads().get(i).id() == pp[k]) padIndex = i;
            for (int c = 0; c < want[k]; c++) probe.onButton(player, padIndex);
        }
        probe.onButton(player, com.taku3.semiconductorlab.block.ProbeStationBlockEntity.BTN_PROBE);
        ItemStack marked = probe.getContainer().getItem(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.SLOT_WAFER);
        h.assertTrue(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(marked, "not"), "the wafer remembers it passed NOT");
        ItemStack diced = com.taku3.semiconductorlab.item.DiamondScribeItem.scribe(marked, net.minecraft.util.RandomSource.create(1));
        h.assertTrue(com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(diced, "not"), "chips cut from it keep the record");
        // 組立台のレシピ
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Branches.CALCULATOR);
        h.assertTrue(a != null && a.parts.size() == 7, "calculator assembly with 7 roles");
        ItemStack plain = new ItemStack(Semiconductor.CHIP, 2);
        ItemStack reg = new ItemStack(Semiconductor.CHIP, 2); reg.set(ModDataComponents.CHALLENGES, java.util.List.of("register"));
        h.assertFalse(a.satisfied(1, plain), "a chip that never passed the register challenge cannot be the register");
        h.assertTrue(a.satisfied(1, reg), "register chips are accepted");
        h.assertTrue(recipeUses(h, "nixie_tube", com.taku3.semiconductorlab.content.Gas.NEON_GAS), "Nixie tubes are filled with neon");
        LOG.info("[LAB] calculator [B-08]: 12+34=46, 100-250=-150, 9999+1=E; chips carry passed challenges; assembly needs adder/register/counter chips");
        h.succeed();
    }

    /**
     * [9-27] ドライエッチングの土台（E と決めた形、案α）：液（HF）は方眼 36 以上で開ける所が上下左右に1マス太る、ドライ（RIE）は太らない。
     * 密着露光の方眼（16）は液でも太らない（今の本線はこわれない）
     */
    @GameTest(maxTicks = 50)
    public void wetVsDryEtch(GameTestHelper h) {
        int n = com.taku3.semiconductorlab.litho.ChipOps.WET_LIMIT_GRID;
        var dot = com.taku3.semiconductorlab.litho.BitGrid.empty(n).with(n / 2, n / 2, true);
        var mask = new com.taku3.semiconductorlab.litho.MaskData(n, com.taku3.semiconductorlab.litho.LayerKind.DIFF_N, dot, "", 0);
        ItemStack w = new ItemStack(Semiconductor.OXIDIZED_WAFER); w.set(ModDataComponents.DOPANT, "boron");
        w = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
        w = com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(w, mask, com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G, 900, 0).out();
        w = com.taku3.semiconductorlab.litho.ChipOps.develop(w).out();
        int wet = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w.copy()).out()).windows().count();
        int dry = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w.copy(), true).out()).windows().count();
        h.assertValueEqual(dry, 1, "dry etching opens exactly the drawn cell");
        h.assertValueEqual(wet, 5, "wet HF undercuts one cell each way on a fine grid");
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.LithoExamples.notGate(), "p"));
        h.assertTrue(com.taku3.semiconductorlab.litho.Circuit.worksAsNot(nl), "coarse grids are not affected");
        LOG.info("[LAB] wet vs dry etch on grid {}: wet {} cells, dry {} cell", n, wet, dry);
        h.succeed();
    }

    /**
     * [9-23] MOSFET（S-011：GATE_OX・Al ゲート・二乗の式）：16×16 の NMOS インバータを密着露光で焼くと、MOS が1個見つかり NOT のお題に合格。
     * 2層目の金属（案B）：層間膜 → VIA → METAL2 で、離れた2つのパッドが同じ線になる。ハンコにすると工程の種類（MOS）と2層目が残る
     */
    @GameTest(maxTicks = 200)
    public void mosAndTwoMetal(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        chip = com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out();
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(chip.get(ModDataComponents.CHIP_STATE)));
        h.assertValueEqual(nl.mosfets.size(), 1, "one n-channel MOSFET");
        h.assertValueEqual(nl.transistors.size(), 0, "no bipolar transistor");
        int n = nl.model.size;
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        java.util.function.BiConsumer<int[], com.taku3.semiconductorlab.litho.Circuit.Role> at = (xy, r) -> roles.put(nl.netOf[xy[1] * n + xy[0]], r);
        at.accept(new int[] {0, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        at.accept(new int[] {10, 0}, com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        at.accept(new int[] {0, 9}, com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        at.accept(new int[] {15, 12}, com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        var not = com.taku3.semiconductorlab.litho.Challenge.byIndex(0);
        var r = com.taku3.semiconductorlab.litho.Challenge.check(not, nl, roles, 1.0);
        h.assertTrue(r.passed(), "the NMOS inverter solves the NOT challenge: " + r);
        var gateAmps = com.taku3.semiconductorlab.litho.Circuit.solve(nl, java.util.Map.of(nl.netOf[0], 0.0, nl.netOf[10], 5.0, nl.netOf[9 * n], 5.0)).padAmps().get(nl.netOf[9 * n]);
        h.assertTrue(gateAmps != null && Math.abs(gateAmps) < 1e-6, "no current flows into the gate (oxide insulates): " + gateAmps);
        // 2層目
        ItemStack ow2 = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow2.set(ModDataComponents.DOPANT, "boron");
        ItemStack two = com.taku3.semiconductorlab.litho.LithoExamples.process(ow2, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverterTwoMetal(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(two);
        h.assertTrue(st.interlayer() && st.has(com.taku3.semiconductorlab.litho.LayerKind.VIA) && st.has(com.taku3.semiconductorlab.litho.LayerKind.METAL2), "interlayer, vias and a second metal");
        var nl2 = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        h.assertValueEqual(nl2.netOf[12 * n + 15], nl2.netOf[15 * n + 8], "the right pad and the bottom pad are one net through metal 2");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.depositInterlayer(two).reason(), "too_many", "only one interlayer");
        var stamp = com.taku3.semiconductorlab.litho.ChipDesign.fromState(st, "x");
        h.assertTrue(stamp.mos() && stamp.twoMetal() && stamp.layers().size() == 6, "a stamp keeps the MOS process and the second metal");
        LOG.info("[LAB] MOS [9-23]: NMOS inverter passes NOT, gate draws no current; two-metal jumper joins pads through vias");
        h.succeed();
    }

    /** S-011：製図台の P で工程の種類を順に（バイポーラ5層 → ＋2層目7層 → MOS 4層 → MOS＋2層目6層）。MOS のハンコはバイポーラの紙には押せない */
    @GameTest(maxTicks = 50)
    public void draftingProcess(GameTestHelper h) {
        h.setBlock(p(1, 1, 1), Semiconductor.DRAFTING_TABLE.block);
        var dt = h.getBlockEntity(p(1, 1, 1), com.taku3.semiconductorlab.block.DraftingTableBlockEntity.class);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_NEW);
        int[] sizes = new int[6]; boolean[] mos = new boolean[6];
        for (int k = 0; k < 6; k++) {
            sizes[k] = dt.design().layers().size(); mos[k] = dt.design().mos();
            dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_PROCESS);
        }
        h.assertTrue(java.util.Arrays.equals(sizes, new int[] {5, 7, 4, 6, 6, 7}) && java.util.Arrays.equals(mos, new boolean[] {false, false, true, true, false, false}), "process cycle: " + java.util.Arrays.toString(sizes));
        h.assertValueEqual(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.role(com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter(), 1), "gate_ox", "MOS layer 1 is the gate oxide");
        ItemStack stamp = new ItemStack(Semiconductor.STAMP);
        stamp.set(ModDataComponents.CHIP_DESIGN, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter().crop());
        dt.getContainer().setItem(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.SLOT_STAMP, stamp);
        h.assertFalse(dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_STAMP), "a MOS stamp does not go on a bipolar sheet");
        dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_PROCESS);
        dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_PROCESS);
        h.assertTrue(dt.design().mos() && dt.onButton(player, com.taku3.semiconductorlab.block.DraftingTableBlockEntity.BTN_STAMP), "it goes on a MOS sheet");
        h.succeed();
    }

    /**
     * [9-31] CMP の受け口（E と決めた形、記録だけ）：層間膜のあとで ChipOps.planarize すると段差の段数が 0、汚れ +2。
     * 層間膜がない・レジストがある・もう平らなら断る。平らにしたあとに重ねた層はまた段差になる
     */
    @GameTest(maxTicks = 200)
    public void planarizeRecord(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack one = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.planarize(one).reason(), "no_interlayer", "no CMP before the interlayer");
        ItemStack il = com.taku3.semiconductorlab.litho.ChipOps.depositInterlayer(one).out();
        var before = com.taku3.semiconductorlab.litho.ChipOps.state(il);
        h.assertTrue(before.steps() > 0, "the surface has steps: " + before.steps());
        var r = com.taku3.semiconductorlab.litho.ChipOps.planarize(il);
        h.assertTrue(r.ok(), "CMP on the interlayer: " + r.reason());
        var after = com.taku3.semiconductorlab.litho.ChipOps.state(r.out());
        h.assertValueEqual(after.steps(), 0, "flat after CMP");
        h.assertValueEqual(after.contamination(), before.contamination() + 2, "slurry particles left");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.planarize(r.out()).reason(), "already_flat", "already flat");
        ItemStack coated = com.taku3.semiconductorlab.litho.ChipOps.coat(il).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.planarize(coated).reason(), "resist_on", "no CMP through resist");
        var codec = com.taku3.semiconductorlab.litho.ChipState.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, after).getOrThrow();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipState.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, codec).getOrThrow().steps(), 0, "planarized_at is saved");
        h.succeed();
    }

    /**
     * [9-24] CMOS：16×16 の CMOS インバータを密着露光で焼くと、p チャネルと n チャネルの MOS が1個ずつ見つかり、NOT のお題に合格。
     * 止まっている間（入力 0 V でも 5 V でも）電源から流れる電流は、抵抗を負荷にした NMOS インバータ（入力 5 V）の 1/100 より小さい。ハンコは工程 CMOS
     */
    @GameTest(maxTicks = 200)
    public void cmosInverter(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.cmosInverter(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        chip = com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out();
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(chip);
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        long pch = nl.mosfets.stream().filter(com.taku3.semiconductorlab.litho.Netlist.Mos::p).count();
        h.assertTrue(nl.mosfets.size() == 2 && pch == 1, "one p-channel and one n-channel MOSFET: " + nl.mosfets.size() + " / p " + pch);
        int n = nl.model.size;
        int gnd = nl.netOf[15 * n], vcc = nl.netOf[0], in = nl.netOf[7 * n], out = nl.netOf[8 * n + 15];
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        roles.put(gnd, com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        roles.put(vcc, com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        roles.put(in, com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        roles.put(out, com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        var r = com.taku3.semiconductorlab.litho.Challenge.check(com.taku3.semiconductorlab.litho.Challenge.byIndex(0), nl, roles, 1.0);
        h.assertTrue(r.passed(), "the CMOS inverter solves the NOT challenge: " + r);
        double worst = 0;
        for (double vin : new double[] {0.0, 5.0}) {
            Double a = com.taku3.semiconductorlab.litho.Circuit.solve(nl, java.util.Map.of(gnd, 0.0, vcc, 5.0, in, vin)).padAmps().get(vcc);
            worst = Math.max(worst, a == null ? 0 : Math.abs(a));
        }
        ItemStack ow2 = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow2.set(ModDataComponents.DOPANT, "boron");
        ItemStack nm = com.taku3.semiconductorlab.litho.ChipOps.clean(com.taku3.semiconductorlab.litho.LithoExamples.process(ow2, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G), 0).out();
        var nl2 = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.ChipOps.state(nm)));
        Double nAmps = com.taku3.semiconductorlab.litho.Circuit.solve(nl2, java.util.Map.of(nl2.netOf[0], 0.0, nl2.netOf[10], 5.0, nl2.netOf[9 * nl2.model.size], 5.0)).padAmps().get(nl2.netOf[10]);
        h.assertTrue(nAmps != null && worst < Math.abs(nAmps) / 100, "CMOS draws almost nothing while idle: CMOS " + worst + " A vs NMOS " + nAmps + " A");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipDesign.fromState(st, "x").process(), com.taku3.semiconductorlab.litho.ChipDesign.CMOS, "a stamp keeps the CMOS process");
        h.assertValueEqual(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.role(com.taku3.semiconductorlab.litho.LithoExamples.cmosInverter(), 0), "n_well", "CMOS layer 0 is the n-well");
        LOG.info("[LAB] CMOS [9-24]: inverter passes NOT, idle supply current {} A (NMOS with resistor load {} A)", worst, nAmps);
        h.succeed();
    }

    /**
     * [9-30] エピタキシーと保護膜の受け口（E と決めた形、記録だけ）：DOPANT boron の板に EPI "n" があると、塗った時の substrate は n、下の板 p が記録される。
     * 保護膜 passivate は金属のあと、レジストなしだけ、2回目は already。保存しても残る（Codec の16項目をこえる分は Extras）
     */
    @GameTest(maxTicks = 200)
    public void epiAndPassivation(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron"); ow.set(ModDataComponents.EPI, "n");
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out());
        h.assertTrue(st.substrate().equals("n") && st.extras().epiOn().equals("p"), "n epi on a p wafer: " + st.substrate() + " on " + st.extras().epiOn());
        ItemStack plain = new ItemStack(Semiconductor.OXIDIZED_WAFER); plain.set(ModDataComponents.DOPANT, "boron");
        h.assertTrue(!com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.coat(plain).out()).extras().epi(), "no epi without the component");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(plain, com.taku3.semiconductorlab.litho.LithoExamples.nmosInverter(), com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        var r = com.taku3.semiconductorlab.litho.ChipOps.passivate(chip);
        h.assertTrue(r.ok(), "passivate after the metal: " + r.reason());
        var ps = com.taku3.semiconductorlab.litho.ChipOps.state(r.out());
        h.assertTrue(ps.extras().passivated(), "passivated");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.passivate(r.out()).reason(), "already", "only once");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.passivate(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out()).reason(), "no_metal", "no passivation before the metal");
        var json = com.taku3.semiconductorlab.litho.ChipState.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, ps).getOrThrow();
        var back = com.taku3.semiconductorlab.litho.ChipState.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        h.assertTrue(back.extras().passivated() && back.steps() == ps.steps() && back.done().size() == ps.done().size() && json.getAsJsonObject().has("passivated"), "saved flat with the old fields: " + json.getAsJsonObject().keySet());
        h.succeed();
    }

    /**
     * [10-12] DRAM（S-012 全部案A）：24×24 の 1103 の3トランジスタのセルを投影露光で焼くと MOS が3個、お題 dram_cell（書く・読む・リフレッシュ）に合格。
     * 1を書いて 2 ms は覚えていて、20 ms 放っておくと電荷が漏れて忘れる（読むと 0 のときと同じ＝出力1）。とても汚れたチップは漏れが大きくて記憶にならない
     */
    @GameTest(maxTicks = 200)
    public void dramCell(GameTestHelper h) {
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack chip = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, com.taku3.semiconductorlab.litho.LithoExamples.dramCell(), com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X);
        chip = com.taku3.semiconductorlab.litho.ChipOps.clean(chip, 0).out();
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.ChipOps.state(chip)));
        h.assertValueEqual(nl.mosfets.size(), 3, "three MOSFETs (write, store, read)");
        int n = nl.model.size;
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        roles.put(nl.netOf[23 * n], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        roles.put(nl.netOf[0], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        roles.put(nl.netOf[5 * n], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        roles.put(nl.netOf[3 * n], com.taku3.semiconductorlab.litho.Circuit.Role.IN_B);
        roles.put(nl.netOf[14 * n + 23], com.taku3.semiconductorlab.litho.Circuit.Role.IN_C);
        roles.put(nl.netOf[11 * n + 23], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        var def = com.taku3.semiconductorlab.litho.Challenge.all().stream().filter(d -> d.id().equals("dram_cell")).findFirst().orElseThrow();
        var r = com.taku3.semiconductorlab.litho.Challenge.check(def, nl, roles, 1.0);
        h.assertTrue(r.passed(), "the 1103-style cell solves dram_cell: " + r);
        var forget = new com.taku3.semiconductorlab.litho.Challenge.Def("forget", 3, 1, new int[0], false, new int[][] {{0b110, -1}, {0b010, -1}, {0b000, -1}, {0b001, 0}, {0b000, -1, 20000}, {0b001, 1}});
        h.assertTrue(com.taku3.semiconductorlab.litho.Challenge.check(forget, nl, roles, 1.0).passed(), "after 20 ms without refresh the charge has leaked away");
        var hold = new com.taku3.semiconductorlab.litho.Challenge.Def("hold", 3, 1, new int[0], false, new int[][] {{0b110, -1}, {0b010, -1}, {0b000, -1, 2000}, {0b001, 0}});
        h.assertTrue(com.taku3.semiconductorlab.litho.Challenge.check(hold, nl, roles, 1.0).passed(), "it still holds after 2 ms");
        h.assertFalse(com.taku3.semiconductorlab.litho.Challenge.check(def, com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.addDust(chip, 200)))), roles, 1.0).passed(), "a very dirty chip leaks too much to work as memory");
        LOG.info("[LAB] DRAM [10-12]: 1103 three-transistor cell writes, reads, refreshes; forgets after 20 ms");
        h.succeed();
    }

    /**
     * [9-36] 銅のデュアルダマシンの受け口（E と決めた形）：層間膜 → 1回目の窓＝ビアの穴 → 2回目の窓＝溝 → バリア → 銅めっき → CMP で溝の中だけ残る。
     * NMOS インバータ＋2層目の例と同じ形を銅で作ると、右と下のパッドが同じ線（Al のエッチングなし）。バリアなしのめっきは汚れ +30
     */
    @GameTest(maxTicks = 200)
    public void copperDamascene(GameTestHelper h) {
        var base = com.taku3.semiconductorlab.litho.LithoExamples.nmosInverterTwoMetal();
        var one = new com.taku3.semiconductorlab.litho.ChipDesign(16, "one", base.layers().subList(0, 4), java.util.List.of(), com.taku3.semiconductorlab.litho.ChipDesign.MOS);
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack w = com.taku3.semiconductorlab.litho.LithoExamples.process(ow, one, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G);
        w = com.taku3.semiconductorlab.litho.ChipOps.depositInterlayer(w).out();
        var tool = com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G;
        for (int layer : new int[] {4, 5}) {   // 1回目＝VIA の穴、2回目＝2層目の溝（窓として開ける）
            var cells = base.layers().get(layer).cells();
            var mask = new com.taku3.semiconductorlab.litho.ChipDesign(16, "m", java.util.List.of(new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.CONTACT, cells))).mask(0);
            w = com.taku3.semiconductorlab.litho.ChipOps.coat(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.expose(w, mask, tool).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.develop(w).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.etchOxide(w, true).out();
            w = com.taku3.semiconductorlab.litho.ChipOps.strip(w).out();
        }
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.state(w).extras().hasViaHoles(), "the first opening is remembered as via holes");
        int dirt0 = com.taku3.semiconductorlab.litho.ChipOps.state(w).contamination();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.plateCopper(w).out()).contamination(), dirt0 + 30, "copper without a barrier poisons the silicon");
        w = com.taku3.semiconductorlab.litho.ChipOps.depositBarrier(w).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.depositBarrier(w).reason(), "already", "one barrier");
        w = com.taku3.semiconductorlab.litho.ChipOps.plateCopper(w).out();
        var r = com.taku3.semiconductorlab.litho.ChipOps.planarize(w);
        h.assertTrue(r.ok(), "CMP leaves copper only in the trenches: " + r.reason());
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(r.out());
        h.assertTrue(st.has(com.taku3.semiconductorlab.litho.LayerKind.METAL2) && st.has(com.taku3.semiconductorlab.litho.LayerKind.VIA) && !st.metalFilm() && st.steps() == 0, "metal 2 and vias, flat, no film left");
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        int n = nl.model.size;
        h.assertValueEqual(nl.netOf[12 * n + 15], nl.netOf[15 * n + 8], "the right pad and the bottom pad are one net through copper");
        LOG.info("[LAB] copper damascene [9-36]: via holes, trenches, barrier, plating, CMP -> metal 2 without etching");
        h.succeed();
    }

    /**
     * [9-37] FinFET（E と決めた形、S-013 案A）：16×16 の CMOS インバータを 100 の方眼（CD 約 40 nm 以下）に置いて ArF 液浸で焼くと、平らな MOS は漏れて NOT に合格しない。
     * チャネルとソース・ドレインの下にフィンを立てた FinFET の工程（7層）なら合格。フィンは液のエッチングでは立たない
     */
    @GameTest(maxTicks = 400)
    public void finfetShortChannel(GameTestHelper h) {
        var c = com.taku3.semiconductorlab.litho.LithoExamples.cmosInverter();
        int big = com.taku3.semiconductorlab.litho.Circuit.SHORT_CHANNEL_GRID;
        java.util.function.Function<com.taku3.semiconductorlab.litho.BitGrid, com.taku3.semiconductorlab.litho.BitGrid> embed = g -> {
            var o = com.taku3.semiconductorlab.litho.BitGrid.empty(big);
            for (int y = 0; y < g.size(); y++) for (int x = 0; x < g.size(); x++) if (g.get(x, y)) o = o.with(x, y, true);
            return o;
        };
        var flat = new java.util.ArrayList<com.taku3.semiconductorlab.litho.ChipDesign.Layer>();
        for (var l : c.layers()) flat.add(new com.taku3.semiconductorlab.litho.ChipDesign.Layer(l.kind(), embed.apply(l.cells())));
        var fin = com.taku3.semiconductorlab.litho.BitGrid.empty(big);
        for (int i : new int[] {1, 2, 3}) { var g = flat.get(i).cells(); for (int y = 0; y < big; y++) for (int x = 0; x < big; x++) if (g.get(x, y)) fin = fin.with(x, y, true); }   // ソース・ドレインの所
        var gox = flat.get(3).cells(); for (int y = 0; y < big; y++) for (int x = 0; x < big; x++) if (gox.get(x, y)) fin = fin.with(x, y, true);   // チャネル
        var finLayers = new java.util.ArrayList<com.taku3.semiconductorlab.litho.ChipDesign.Layer>();
        finLayers.add(new com.taku3.semiconductorlab.litho.ChipDesign.Layer(com.taku3.semiconductorlab.litho.LayerKind.FIN, fin));
        finLayers.addAll(flat);
        java.util.function.Function<com.taku3.semiconductorlab.litho.ChipDesign, Boolean> passes = d -> {
            ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
            ItemStack chip = com.taku3.semiconductorlab.litho.ChipOps.clean(com.taku3.semiconductorlab.litho.LithoExamples.process(ow, d, com.taku3.semiconductorlab.litho.ExposureTool.ARF_IMMERSION, com.taku3.semiconductorlab.litho.ChipState.Tone.POSITIVE, true), 0).out();
            var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(com.taku3.semiconductorlab.litho.ChipOps.state(chip)));
            int n = nl.model.size;
            var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
            roles.put(nl.netOf[15 * n], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
            roles.put(nl.netOf[0], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
            roles.put(nl.netOf[7 * n], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
            roles.put(nl.netOf[8 * n + 15], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
            return nl.mosfets.size() == 2 && com.taku3.semiconductorlab.litho.Challenge.check(com.taku3.semiconductorlab.litho.Challenge.byIndex(0), nl, roles, 1.0).passed();
        };
        h.assertFalse(passes.apply(new com.taku3.semiconductorlab.litho.ChipDesign(big, "flat", flat, java.util.List.of(), com.taku3.semiconductorlab.litho.ChipDesign.CMOS)), "a planar CMOS inverter leaks at about 40 nm");
        var finDesign = new com.taku3.semiconductorlab.litho.ChipDesign(big, "fin", finLayers, java.util.List.of(), com.taku3.semiconductorlab.litho.ChipDesign.FINFET);
        h.assertTrue(passes.apply(finDesign), "the FinFET version works");
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        ItemStack dev = com.taku3.semiconductorlab.litho.ChipOps.develop(com.taku3.semiconductorlab.litho.ChipOps.expose(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out(), finDesign.mask(0), com.taku3.semiconductorlab.litho.ExposureTool.ARF_IMMERSION).out()).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.formFins(dev, false).reason(), "wet", "wet etching cannot stand fins up");
        h.assertValueEqual(com.taku3.semiconductorlab.block.DraftingTableBlockEntity.role(finDesign, 0), "fin", "FinFET layer 0 is the fin");
        LOG.info("[LAB] FinFET [9-37]: planar CMOS fails at grid {}, FinFET passes", big);
        h.succeed();
    }

    /** v0.5 #6 マルチパターニング（C と決めた形）：方眼の2倍の大きさのマスクは、となり合う列に描いた所がなければ焼け、あれば too_fine。2倍をこえると焼けない */
    @GameTest
    public void multiPatterningSplit(GameTestHelper h) {
        int cd = 65, g = com.taku3.semiconductorlab.litho.ExposureTool.gridFor(cd), n = Math.min(com.taku3.semiconductorlab.litho.BitGrid.MAX, 2 * g);
        n -= n % 4;
        var even = com.taku3.semiconductorlab.litho.BitGrid.empty(n);
        var dense = com.taku3.semiconductorlab.litho.BitGrid.empty(n);
        for (int y = 0; y < n; y++) { even = even.with(0, y, true).with(2, y, true); dense = dense.with(0, y, true).with(1, y, true); }
        java.util.function.Function<com.taku3.semiconductorlab.litho.BitGrid, String> burn = cells -> {
            ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
            var mask = new com.taku3.semiconductorlab.litho.MaskData(cells.size(), com.taku3.semiconductorlab.litho.LayerKind.CONTACT, cells, "x [1/2]", 0);
            var r = com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out(), mask, com.taku3.semiconductorlab.litho.ExposureTool.ARF, cd, 0);
            return r.ok() ? "ok" : r.reason();
        };
        h.assertTrue(n > g, "the test grid is finer than one exposure: " + n + " > " + g);
        h.assertValueEqual(burn.apply(even), "ok", "a split mask prints at twice the grid");
        h.assertValueEqual(burn.apply(dense), "too_fine", "neighbouring columns do not");
        ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.exposeAtCd(com.taku3.semiconductorlab.litho.ChipOps.coat(ow).out(), new com.taku3.semiconductorlab.litho.MaskData(n, com.taku3.semiconductorlab.litho.LayerKind.CONTACT, even, "x", 0), com.taku3.semiconductorlab.litho.ExposureTool.ARF, cd, 0).reason(), "too_fine", "only masks split by the reduction camera");
        h.succeed();
    }

    /**
     * [10-13] 論理の箱（S-014 Q25-4 案A）：ピンつきの NOT のハンコを2つ押して線でつないだ 24×24 のチップは、論理の箱で入力と同じ出力（バッファ）。⚠️アナログでは例の RTL の NOT は次の段を引ききれない（論理の箱は電気の強さを見ない）。
     * ハンコの外に拡散を足すと論理の箱は使えない（アナログに戻る）。ハンコ1つの NOT のチップは、論理の箱で NOT のお題に合格
     */
    @GameTest(maxTicks = 200)
    public void logicBoxes(GameTestHelper h) {
        var R = com.taku3.semiconductorlab.litho.Circuit.Role.class;
        var not = com.taku3.semiconductorlab.litho.LithoExamples.notGate().named("not").withPins(java.util.List.of(
                new com.taku3.semiconductorlab.litho.ChipDesign.Pin("GROUND", 0, 0), new com.taku3.semiconductorlab.litho.ChipDesign.Pin("VCC", 7, 0),
                new com.taku3.semiconductorlab.litho.ChipDesign.Pin("OUT_1", 7, 4), new com.taku3.semiconductorlab.litho.ChipDesign.Pin("IN_A", 6, 7)));
        var blank = com.taku3.semiconductorlab.block.DraftingTableBlockEntity.blank(24, com.taku3.semiconductorlab.litho.ChipDesign.BIPOLAR, false);
        var d = blank.stamp(not, 0, 0).stamp(not, 8, 8);
        var metal = d.layers().get(4).cells();
        for (int x = 8; x <= 17; x++) metal = metal.with(x, 4, true);   // 1つ目の出力 → 右へ → 下へ → 2つ目の入力（14,15）の下
        for (int y = 4; y <= 16; y++) metal = metal.with(17, y, true);
        for (int x = 14; x <= 17; x++) metal = metal.with(x, 16, true);
        d = d.withLayer(4, metal);
        java.util.function.Function<com.taku3.semiconductorlab.litho.ChipDesign, ItemStack> burn = dd -> {
            ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
            return com.taku3.semiconductorlab.litho.ChipOps.clean(com.taku3.semiconductorlab.litho.LithoExamples.process(ow, dd, com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X), 0).out();
        };
        var st = com.taku3.semiconductorlab.litho.ChipOps.state(burn.apply(d));
        var nl = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st));
        int n = 24;
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        roles.put(nl.netOf[0], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        roles.put(nl.netOf[7], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        roles.put(nl.netOf[8 * n + 8], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND);
        roles.put(nl.netOf[8 * n + 15], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        roles.put(nl.netOf[7 * n + 6], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        roles.put(nl.netOf[12 * n + 15], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1);
        var sim = com.taku3.semiconductorlab.litho.LogicSim.of(nl, st.parts());
        h.assertTrue(sim != null && st.parts().size() == 2, "two stamps, all logic boxes");
        for (boolean in : new boolean[] {false, true}) {
            int logic = sim.step(roles, java.util.Map.of(com.taku3.semiconductorlab.litho.Circuit.Role.IN_A, in), 1);
            h.assertTrue(logic == (in ? 1 : 0), "NOT then NOT is a buffer in logic: " + logic + " for " + in);   // ⚠️アナログでは例の RTL の NOT は次の段を引ききれない（低い方が約 1 V）＝論理の箱は見ない
        }
        var stray = d.withLayer(0, d.layers().get(0).cells().with(20, 20, true));
        var st2 = com.taku3.semiconductorlab.litho.ChipOps.state(burn.apply(stray));
        h.assertTrue(com.taku3.semiconductorlab.litho.LogicSim.of(com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(st2)), st2.parts()) == null, "a diffusion outside the stamps means analog");
        var one = blank.stamp(not, 0, 0);
        var s1 = com.taku3.semiconductorlab.litho.ChipOps.state(burn.apply(one));
        var nl1 = com.taku3.semiconductorlab.litho.Netlist.extract(com.taku3.semiconductorlab.litho.ChipModel.of(s1));
        var r1 = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        r1.put(nl1.netOf[0], com.taku3.semiconductorlab.litho.Circuit.Role.GROUND); r1.put(nl1.netOf[7], com.taku3.semiconductorlab.litho.Circuit.Role.VCC);
        r1.put(nl1.netOf[4 * n + 7], com.taku3.semiconductorlab.litho.Circuit.Role.OUT_1); r1.put(nl1.netOf[7 * n + 6], com.taku3.semiconductorlab.litho.Circuit.Role.IN_A);
        h.assertTrue(com.taku3.semiconductorlab.litho.Challenge.check(com.taku3.semiconductorlab.litho.Challenge.byId("not"), nl1, r1, 1.0, s1.parts()).passed(), "a stamped NOT passes NOT as a logic box");
        var pins = com.taku3.semiconductorlab.block.ProbeStationBlockEntity.pinsOf(nl1, r1);
        h.assertValueEqual(pins.size(), 4, "four pins from the pad roles");
        LOG.info("[LAB] logic boxes [10-13]: two NOT stamps make a buffer in logic and analog");
        h.succeed();
    }

    /**
     * [2-33] 化学増幅レジストの受け口（E と決めた形）：KrF（248 nm）では、種類を決めたノボラック（DNQ）は光を通さず opaque_resist、化学増幅（CAR）は焼ける。
     * CAR は露光後の加熱（bake）をしないと現像してもどこも溶けない。加熱して現像すると窓が開く。g線ならノボラックも焼ける（前からの塗り方は見ない）
     */
    @GameTest
    public void chemicallyAmplifiedResist(GameTestHelper h) {
        var cells = com.taku3.semiconductorlab.litho.BitGrid.empty(16).with(3, 3, true).with(4, 3, true);
        var mask = new com.taku3.semiconductorlab.litho.MaskData(16, com.taku3.semiconductorlab.litho.LayerKind.CONTACT, cells, "x", 0);
        java.util.function.Function<String, ItemStack> coated = kind -> {
            ItemStack ow = new ItemStack(Semiconductor.OXIDIZED_WAFER); ow.set(ModDataComponents.DOPANT, "boron");
            return com.taku3.semiconductorlab.litho.ChipOps.coat(ow, com.taku3.semiconductorlab.litho.ChipState.Tone.POSITIVE, kind).out();
        };
        var krf = com.taku3.semiconductorlab.litho.ExposureTool.KRF;
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.expose(coated.apply(com.taku3.semiconductorlab.litho.ChipOps.RESIST_DNQ), mask, krf).reason(), "opaque_resist", "novolac is opaque at 248 nm");
        h.assertTrue(com.taku3.semiconductorlab.litho.ChipOps.expose(coated.apply(com.taku3.semiconductorlab.litho.ChipOps.RESIST_DNQ), mask, com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G).ok(), "novolac is fine with the g-line");
        ItemStack ex = com.taku3.semiconductorlab.litho.ChipOps.expose(coated.apply(com.taku3.semiconductorlab.litho.ChipOps.RESIST_CAR), mask, krf).out();
        h.assertTrue(!ex.isEmpty(), "CAR prints at 248 nm");
        var noBake = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.develop(ex).out());
        h.assertTrue(noBake.resistPattern().invert().isEmpty(), "without the post-exposure bake nothing dissolves");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.bake(coated.apply(com.taku3.semiconductorlab.litho.ChipOps.RESIST_DNQ)).reason(), "not_exposed", "bake after exposure");
        ItemStack baked = com.taku3.semiconductorlab.litho.ChipOps.bake(ex).out();
        h.assertValueEqual(com.taku3.semiconductorlab.litho.ChipOps.bake(baked).reason(), "already", "bake once");
        var dev = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.develop(baked).out());
        h.assertTrue(!dev.resistPattern().get(3, 3) && dev.resistPattern().get(0, 0), "after baking the exposed cells dissolve");
        var stripped = com.taku3.semiconductorlab.litho.ChipOps.state(com.taku3.semiconductorlab.litho.ChipOps.strip(com.taku3.semiconductorlab.litho.ChipOps.develop(baked).out()).out());
        h.assertTrue(stripped.extras().resistKind().isEmpty() && !stripped.extras().baked(), "stripping clears the resist record");
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
        rec(a, Semiconductor.CLEANING_BENCH.block, "洗浄（段階1）", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WAFERS_PER_RUN), "1〜8枚、レジストなし"), io(com.taku3.semiconductorlab.content.Gas.NITRIC_ACID, 1), io(Chemistry.HYDROFLUORIC_ACID, 1), io(Chemistry.DISTILLED_WATER, 1)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WAFERS_PER_RUN), "汚れ 0")), com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.WATTS, com.taku3.semiconductorlab.block.CleaningBenchBlockEntity.TICKS / 20.0, "熱い硝酸で煮る → フッ酸 → 蒸留水ですすぐ（1950年代）");
        rec(a, Semiconductor.SPIN_COATER.block, "レジストを塗る", L(cond(io(Semiconductor.OXIDIZED_WAFER, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WAFERS_PER_RUN), "酸化膜つきの板か加工中のウェハー 1〜8枚"), cond(io(Chemistry.POSITIVE_RESIST, 1), "感光していない物")),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WAFERS_PER_RUN), "レジストを塗った（COATED）")), com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.WATTS, com.taku3.semiconductorlab.block.SpinCoaterBlockEntity.TICKS / 20.0, "毎分数千回転で広げ、温めて溶剤を飛ばす（ChipOps.coat）");
        rec(a, Semiconductor.RESIST_STRIPPER.block, "レジストをはがす", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WAFERS_PER_RUN), "レジスト付き 1〜8枚"), io(Chemistry.SULFURIC_ACID, 1), io(Chemistry.HYDROGEN_PEROXIDE, 1)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WAFERS_PER_RUN), "レジストなし")), com.taku3.semiconductorlab.block.ResistStripperBlockEntity.WATTS, com.taku3.semiconductorlab.block.ResistStripperBlockEntity.TICKS / 20.0, "熱い硫酸＋過酸化水素でレジストを酸化して落とす（ChipOps.strip）。塗り直しにも");
        rec(a, Semiconductor.DEVELOPER_BENCH.block, "現像", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WAFERS_PER_RUN), "露光した 1〜8枚"), io(Chemistry.DEVELOPER, 1)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WAFERS_PER_RUN), "現像した（DEVELOPED）")), com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.WATTS, com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity.TICKS / 20.0, "薄い NaOH で光の当たったレジストを溶かし、熱板で固める（ChipOps.develop）");
        rec(a, Semiconductor.WIRE_BONDER.block, "ワイヤーボンディング", L(cond(io(Semiconductor.CHIP, 1), "1〜8個"), cond(io(Metallurgy.GOLD_BONDING_WIRE, 4), "パッドの数だけ（NOT の例は4）"), io(Semiconductor.CHIP_HEADER, 1)),
            L(io(Semiconductor.BONDED_CHIP, 1)), com.taku3.semiconductorlab.block.WireBonderBlockEntity.WATTS, com.taku3.semiconductorlab.block.WireBonderBlockEntity.TICKS / 20.0, "金の細い線をパッドと足に熱圧着（チップの中身を写す）");
        rec(a, Semiconductor.PACKAGE_SEALER.block, "封止（金属缶）", L(cond(io(Semiconductor.BONDED_CHIP, 1), "1〜8個"), io(Semiconductor.CAN_CAP, 1), mol(com.taku3.semiconductorlab.content.Gas.NITROGEN_GAS, 1)),
            L(io(Semiconductor.CAN_PACKAGED_CHIP, 1)), com.taku3.semiconductorlab.block.PackageSealerBlockEntity.WATTS, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.TICKS / 20.0, "乾いた窒素を満たして缶を溶接（気密）");
        rec(a, Semiconductor.PACKAGE_SEALER.block, "封止（エポキシ）", L(cond(io(Semiconductor.BONDED_CHIP, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.CHIPS_PER_COMPOUND), "1〜8個"), io(Chemistry.EPOXY_MOLDING_COMPOUND, 1)),
            L(io(Semiconductor.PLASTIC_PACKAGED_CHIP, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.CHIPS_PER_COMPOUND)), com.taku3.semiconductorlab.block.PackageSealerBlockEntity.WATTS, com.taku3.semiconductorlab.block.PackageSealerBlockEntity.TICKS / 20.0, "温めた型にエポキシを押しこむ（トランスファー成形）");
        rec(a, Semiconductor.ION_IMPLANTER.block, "イオン注入", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.WAFERS_PER_RUN), "現像したレジストか窓 1〜8枚"), cond(alt(io(Metallurgy.BORON_TRIOXIDE, 1), Chemistry.PHOSPHORUS), "B＝p型・P＝n型")),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.WAFERS_PER_RUN), "注入した（アニール前は効かない）")), com.taku3.semiconductorlab.block.IonImplanterBlockEntity.WATTS, com.taku3.semiconductorlab.block.IonImplanterBlockEntity.TICKS / 20.0, "イオン源 → 質量分析の磁石 → 加速。隣の真空容器 0.01Pa 以下");
        rec(a, Semiconductor.ANNEAL_FURNACE.block, "アニール", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.WAFERS_PER_RUN), "注入した 1〜8枚（レジストなし）"), alt(io(Gas.NITROGEN_GAS, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.GAS_PER_RUN), Gas.ARGON_GAS)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.WAFERS_PER_RUN), "注入した層が働く")), com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity.TICKS / 20.0, "N2/Ar の中で約1000℃。隣のガスホルダーから補充");
        rec(a, Semiconductor.SPUTTER.block, "Al の膜", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.SputterBlockEntity.WAFERS_PER_RUN), "窓を開けた 1〜8枚"), io(Metallurgy.ALUMINIUM_INGOT, 1), io(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.SputterBlockEntity.AR_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.SputterBlockEntity.WAFERS_PER_RUN), "Al を一面に")), com.taku3.semiconductorlab.block.SputterBlockEntity.WATTS, com.taku3.semiconductorlab.block.SputterBlockEntity.TICKS / 20.0, "Ar⁺ で的をたたく。隣の真空容器 0.01Pa 以下、Ar はホルダーから");
        rec(a, Semiconductor.FLUORINATION_REACTOR.block, "CF4 を作る", L(alt(io(Metallurgy.GRAPHITE, 1), com.taku3.semiconductorlab.content.Heat.SYNTHETIC_GRAPHITE), io(Gas.FLUORINE_CYLINDER, 1)),
            L(io(Semiconductor.CARBON_TETRAFLUORIDE, com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.CF4_PER_RUN), io(Gas.EMPTY_GAS_CYLINDER, 1)), com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.FluorinationReactorBlockEntity.TICKS / 20.0, "C + 2F2 → CF4。ニッケルの管で冷やしながら");
        rec(a, Semiconductor.DRY_ETCHER.block, "ドライエッチング", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WAFERS_PER_RUN), "現像したレジスト 1〜8枚"), io(Semiconductor.CARBON_TETRAFLUORIDE, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.CF4_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WAFERS_PER_RUN), "窓を開けた（真下だけ）")), com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WATTS, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.TICKS / 20.0, "CF4 のプラズマ（RIE）。隣の真空容器 0.1Pa 以下");
        rec(a, Semiconductor.SILANE_GENERATOR.block, "ケイ化マグネシウム", L(io(Metallurgy.MAGNESIUM, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.MG_PER_RUN), io(Metallurgy.METALLURGICAL_SILICON, 1)),
            L(io(Semiconductor.MAGNESIUM_SILICIDE, 1)), com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.TICKS / 20.0, "2Mg + Si → Mg2Si");
        rec(a, Semiconductor.SILANE_GENERATOR.block, "シラン", L(io(Semiconductor.MAGNESIUM_SILICIDE, 1), io(Gas.HYDROCHLORIC_ACID, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.HCL_PER_RUN), io(Gas.EMPTY_GAS_CYLINDER, 1)),
            L(io(Semiconductor.SILANE_CYLINDER, 1)), com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity.TICKS / 20.0, "Mg2Si + 4HCl → SiH4 + 2MgCl2（塩酸の瓶は戻る）");
        rec(a, Semiconductor.CVD_FURNACE.block, "層間膜", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WAFERS_PER_RUN), "1層目の金属を削った 1〜8枚"), io(Semiconductor.SILANE_CYLINDER, 1), io(Gas.OXYGEN_GAS, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.O2_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WAFERS_PER_RUN), "層間膜（このあと VIA・METAL2）"), io(Gas.EMPTY_GAS_CYLINDER, 1)), com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.TICKS / 20.0, "SiH4 + O2 → SiO2 を約400℃。O2 はホルダーから");
        rec(a, Semiconductor.CMP_POLISHER.block, "平らにする（CMP）", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.WAFERS_PER_RUN), "層間膜のある 1〜8枚"), io(Chemistry.COLLOIDAL_SILICA, 1)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.WAFERS_PER_RUN), "段差 0（汚れ +2）"), io(Chemistry.GLASS_BOTTLE, 1)), com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.WATTS, com.taku3.semiconductorlab.block.CmpPolisherBlockEntity.TICKS / 20.0, "スラリー＋パッドで出っぱりから削る");
        rec(a, Semiconductor.EPITAXY_REACTOR.block, "エピタキシー", L(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.WAFERS_PER_RUN), io(Metallurgy.SILICON_TETRACHLORIDE, 1), io(Gas.HYDROGEN_GAS, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.H2_PER_RUN), cond(alt(io(Metallurgy.BORON_TRIOXIDE, 1), Chemistry.PHOSPHORUS), "B＝p・P＝n の層")),
            L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.WAFERS_PER_RUN), "エピ層付き"), io(Metallurgy.HYDROGEN_CHLORIDE_GAS, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.HCL_PER_RUN)), com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.EpitaxyReactorBlockEntity.TICKS / 20.0, "SiCl4 + 2H2 → Si + 4HCl を約1200℃。H2 はホルダーから");
        rec(a, Semiconductor.CVD_FURNACE.block, "窒化膜（保護膜）", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WAFERS_PER_RUN), "金属を削り終えた 1〜8枚"), io(Semiconductor.SILANE_CYLINDER, 1), io(Gas.AMMONIA_GAS, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.O2_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WAFERS_PER_RUN), "保護膜つき"), io(Gas.EMPTY_GAS_CYLINDER, 1)), com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity.TICKS / 20.0, "3SiH4 + 4NH3 → Si3N4 + 12H2");
        rec(a, Semiconductor.COPPER_PLATER.block, "銅めっき（ダマシン）", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.WAFERS_PER_RUN), "層間膜に穴と溝 1〜8枚（先にバリア）"), io(Chemistry.COPPER_SULFATE, 1), io(net.minecraft.world.item.Items.COPPER_INGOT, 1)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.WAFERS_PER_RUN), "銅の膜（このあと CMP）")), com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.WATTS, com.taku3.semiconductorlab.block.CopperPlaterBlockEntity.TICKS / 20.0, "Cu²⁺ + 2e⁻ → Cu。バリアなしは汚れ +30");
        rec(a, Semiconductor.SPUTTER.block, "タンタルのバリア膜", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.SputterBlockEntity.WAFERS_PER_RUN), "層間膜に穴と溝 1〜8枚"), io(Metallurgy.TANTALUM, 1), io(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.SputterBlockEntity.AR_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.SputterBlockEntity.WAFERS_PER_RUN), "バリア膜つき")), com.taku3.semiconductorlab.block.SputterBlockEntity.WATTS, com.taku3.semiconductorlab.block.SputterBlockEntity.TICKS / 20.0, "銅がシリコンへしみるのを止める（[9-36]）");
        rec(a, Semiconductor.DRY_ETCHER.block, "フィンを立てる（FinFET）", L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WAFERS_PER_RUN), "フィンの形に現像した 1〜8枚"), io(Gas.CHLORINE_GAS, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.CF4_PER_RUN)),
            L(cond(io(Semiconductor.PROCESS_WAFER, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WAFERS_PER_RUN), "フィン（FIN の層）")), com.taku3.semiconductorlab.block.DryEtcherBlockEntity.WATTS, com.taku3.semiconductorlab.block.DryEtcherBlockEntity.TICKS / 20.0, "Cl2 のプラズマでシリコンを真下に削る");
        rec(a, Semiconductor.DIFFUSION_FURNACE.block, "サイリスタの板（p-n-p-n）", L(cond(io(Semiconductor.PN_JUNCTION_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN), "n の板にホウ素の p-n 接合"), io(Chemistry.PHOSPHORUS, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.SOURCE_PER_RUN)),
            L(io(Semiconductor.THYRISTOR_WAFER, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WAFERS_PER_RUN)), com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.WATTS, com.taku3.semiconductorlab.block.DiffusionFurnaceBlockEntity.TICKS / 20.0, "両面 p（p-n-p）にもう一度リン → p-n-p-n（1957 GE の SCR）");
        rec(a, Semiconductor.WAFER_SAW.block, "板に切る", L(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), perRuns(io(Semiconductor.DIAMOND_SAW_BLADE, 1), com.taku3.semiconductorlab.block.WaferSawBlockEntity.RUNS_PER_BLADE)),
            L(cond(io(Semiconductor.SILICON_WAFER, com.taku3.semiconductorlab.block.WaferSawBlockEntity.SLICES_PER_INGOT), "純度とドーパントを受け継ぐ")), com.taku3.semiconductorlab.block.WaferSawBlockEntity.WATTS, com.taku3.semiconductorlab.block.WaferSawBlockEntity.TICKS / 20.0, "ダイヤモンドの円盤刃（1954 の作り方）");
        rec(a, Semiconductor.CZ_PULLER.block, "種結晶", L(cond(io(Metallurgy.POLYSILICON, 1), "種のスロットに"), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_SEED)),
            L(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.SEED_TICKS / 20.0, "細く絞って引くと1つの粒だけが残る");
        rec(a, Semiconductor.CZ_PULLER.block, "単結晶", L(kept(io(Semiconductor.SILICON_SEED_CRYSTAL, 1)), io(Metallurgy.POLYSILICON, com.taku3.semiconductorlab.block.CzPullerBlockEntity.POLY_PER_INGOT), io(Semiconductor.QUARTZ_CRUCIBLE, 1), mol(Gas.ARGON_GAS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.AR_PER_INGOT), cond(alt(io(Metallurgy.BORON_TRIOXIDE, 1), Chemistry.PHOSPHORUS), "入れなくてもよい（真性）")),
            L(cond(io(Semiconductor.SILICON_SINGLE_CRYSTAL, 1), "B2O3 で p型・リンで n型、純度は多結晶シリコンと同じ")), com.taku3.semiconductorlab.block.CzPullerBlockEntity.WATTS, com.taku3.semiconductorlab.block.CzPullerBlockEntity.GROW_TICKS / 20.0, "約1420℃の融液から回しながら引き上げる。17.4kWh/kg");
    }
}
