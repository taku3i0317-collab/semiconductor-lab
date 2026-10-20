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
 * GameTest：幹6 ガス・電解（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class GasTests extends LabTestBase {
    /** [6-14] フッ素の電解槽（IMP2 O-018）：KF（減らない）＋黒鉛の陽極＋フッ酸5本 → フッ素のボンベ1本＋水素5袋。水素の出口がいっぱいだと事故（フッ素を失って止まる） */
    @GameTest(maxTicks = 400)
    public void fluorine_cell(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "potassium_fluoride", Chemistry.POTASH) && recipeUses(h, "potassium_fluoride", Chemistry.HYDROFLUORIC_ACID), "KF = potash + HF");
        h.assertTrue(recipeUses(h, "fluorine_cell", Metallurgy.NICKEL), "monel-like cell (nickel + copper)");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Gas.FLUORINE_CELL.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.FluorineCellBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_KF, new ItemStack(Chemistry.POTASSIUM_FLUORIDE));
        c.setItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_ANODE, new ItemStack(Metallurgy.GRAPHITE));
        c.setItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_HF, new ItemStack(Chemistry.HYDROFLUORIC_ACID, 6));
        c.setItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_CYL, new ItemStack(Gas.EMPTY_GAS_CYLINDER, 2));
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.WATTS);
        tickConsumer(h, at, be, (com.taku3.semiconductorlab.block.FluorineCellBlockEntity.TICKS + 1) * com.taku3.semiconductorlab.block.FluorineCellBlockEntity.F2_PER_CYLINDER, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_F2).is(Gas.FLUORINE_CYLINDER) && c.getItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_H2).getCount() == com.taku3.semiconductorlab.block.FluorineCellBlockEntity.F2_PER_CYLINDER, "5 HF -> 1 F2 cylinder + 5 H2 bags");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_KF).is(Chemistry.POTASSIUM_FLUORIDE) && c.getItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_ANODE).is(Metallurgy.GRAPHITE), "KF and the anode stay");
        // 事故：水素の出口をいっぱいに
        c.setItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 64));
        tickConsumer(h, at, be, com.taku3.semiconductorlab.block.FluorineCellBlockEntity.TICKS + 1, feed);
        h.assertTrue(be.hadAccident() && be.f2Runs() == 0, "hydrogen cannot escape -> the gases mix: accident, fluorine lost");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.FluorineCellBlockEntity.SLOT_F2).getCount() == 1, "the finished cylinder is unharmed");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"power", "f2", "need_kf", "need_anode", "need_hf", "need_cylinder", "output_full", "need_power", "running", "accident"})
            h.assertTrue(ja.has("gui.semiconductor-lab.fluorine_cell." + k), "lang " + k);
        LOG.info("[LAB] fluorine_cell [6-14]: KF + graphite anode, 5 HF -> F2 cylinder + 5 H2; H2 outlet full -> accident (F2 lost, no block damage)");
        h.succeed();
    }

    /** [6-08] 希ガスの精留塔（IMP2 O-018）：酸素・窒素の袋を1つずつ → 32回で Kr 1袋、16回で Ne 1袋（Xe はもっと少ない）。新しいガスはボンベにも詰められる */
    @GameTest(maxTicks = 300)
    public void rare_gases(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "rare_gas_column", Gas.AIR_SEPARATION_UNIT.block.asItem()), "rare gas column after the air separation unit");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Gas.RARE_GAS_COLUMN.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.class);
        var c = be.getContainer();
        c.setItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_O2, new ItemStack(Gas.OXYGEN_GAS, 64));
        c.setItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_N2, new ItemStack(Gas.NITROGEN_GAS, 64));
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.WATTS);
        tickConsumer(h, at, be, (com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.TICKS + 1) * 33, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_KR).is(Gas.KRYPTON_GAS) && c.getItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_NE).getCount() >= 2, "33 runs -> 1 Kr, 2 Ne: " + c.getItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_KR) + " " + c.getItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_NE));
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.RareGasColumnBlockEntity.SLOT_XE).isEmpty(), "xenon is much rarer");
        h.assertTrue(GasType.of(new ItemStack(Gas.KRYPTON_GAS)) == GasType.KRYPTON && GasType.NEON.cylinderItem() == Gas.NEON_CYLINDER && GasType.XENON.bagItem() == Gas.XENON_GAS, "new gas types with bags and cylinders");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"neon", "krypton", "xenon"}) h.assertTrue(ja.has("gui.semiconductor-lab.gas_holder.gas." + k), "gas holder name " + k);
        LOG.info("[LAB] rare_gases [6-08]: 33 runs (33 O2 + 33 N2) -> Kr 1, Ne 2, Xe 0 (game-concentrated; air has Kr 1.1 ppm, Ne 18 ppm, Xe 0.09 ppm)");
        h.succeed();
    }

    // =====================================================================
    // 電解槽 [6-01]
    // =====================================================================

    @GameTest
    public void electrolyzer(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Gas.ELECTROLYZER.block);
        ElectrolyzerBlockEntity be = h.getBlockEntity(pos, ElectrolyzerBlockEntity.class);
        var c = be.getContainer();
        c.setItem(ElectrolyzerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID, 3));

        // 電気なし：止まる
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 100, 0);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "no power -> no hydrogen");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.WATER_BUCKET), "no power -> water stays");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);

        // 電気あり。1tick にちょうど使う分（5kW＝250 J/tick）だけ入れる＝バッファに余りを貯めない
        long feed = 250;
        tickConsumer(h, pos, be, 1, feed);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickConsumer(h, pos, be, 999, feed);
        // 途中で電気を切る → 止まる（進捗は保たれる）
        tickConsumer(h, pos, be, 500, 0);
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "paused: nothing made");
        // 再接続 → 続きから（電気のあった tick の合計が 2300 で完成）
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS - 1001, feed);
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "not finished 1 powered tick before 115 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 2, "H2 count");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).is(Gas.HYDROGEN_GAS), "H2 item");
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_O2).getCount(), 1, "O2 count");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_O2).is(Gas.OXYGEN_GAS), "O2 item");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.BUCKET), "water bucket -> empty bucket");
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_ACID).getCount(), 3, "sulfuric acid is not consumed");

        // 硫酸を抜くと止まる（純水はほぼ電気を通さない）
        c.setItem(ElectrolyzerBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, ItemStack.EMPTY);
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 2, "without acid nothing more is made");

        // 出口が満杯（水素 63 個＋2 は入らない）→ 止まる
        c.setItem(ElectrolyzerBlockEntity.SLOT_ACID, new ItemStack(Chemistry.SULFURIC_ACID, 1));
        c.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 63));
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS + 10, feed);
        h.assertValueEqual(c.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 63, "outlet full -> stops");
        h.assertTrue(c.getItem(ElectrolyzerBlockEntity.SLOT_WATER).is(Items.WATER_BUCKET), "outlet full -> water not used");
        LOG.info("[LAB] electrolyzer: H2 x2 + O2 x1 after {} ticks, acid kept, stops without power/acid", ElectrolyzerBlockEntity.TOTAL_TICKS);
        h.succeed();
    }

    // =====================================================================
    // ガスホルダー [6-02]
    // =====================================================================

    @GameTest(maxTicks = 80)
    public void gasHolderPullAndBang(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        h.setBlock(ep, Gas.ELECTROLYZER.block);
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 2));
        ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, new ItemStack(Gas.OXYGEN_GAS, 1));

        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            var gc = g.getContainer();
            h.assertValueEqual(g.getGasType(), GasType.HYDROGEN, "holder gas type after pulling");
            int total = g.getAmount() + gc.getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertValueEqual(total, 2, "2 mol of H2 pulled (stored + 1 bag in the out slot)");
            h.assertTrue(ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).isEmpty(), "electrolyzer H2 outlet emptied");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_O2).getCount(), 1, "O2 is not pulled into an H2 holder");
            h.assertTrue(g.getAmount() > 0, "some H2 must stay inside for the bang test");
            // 電解槽に残った O2 を片付ける（爆発で空になったホルダーが、次の吸い込みで O2 を吸ってしまうため）
            ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, ItemStack.EMPTY);
            // 爆鳴気：水素の入ったホルダーに酸素を入れる
            gc.setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(Gas.OXYGEN_GAS, 1));
        });
        h.runAfterDelay(30, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            h.assertValueEqual(g.getAmount(), 0, "oxyhydrogen bang empties the holder");
            h.assertValueEqual(g.getGasType(), GasType.NONE, "gas type after bang");
            h.assertBlockPresent(Gas.GAS_HOLDER.block, hp);
            LOG.info("[LAB] gas holder: pulled H2 from electrolyzer, O2 left alone, bang -> empty, block intact");
            h.succeed();
        });
    }

    /** 満杯（1000mol）のホルダーは、電解槽の出口からも「入」の袋からも吸い込まない */
    @GameTest(maxTicks = 80)
    public void gasHolderFullStops(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        h.setBlock(ep, Gas.ELECTROLYZER.block);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        var gc = g.getContainer();
        g.setContentsForTest(GasType.HYDROGEN, GasHolderBlockEntity.CAPACITY);
        // 「出」を埋めておく（空だと1袋出して 999 になり、また吸えてしまうため）
        gc.setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(Gas.HYDROGEN_GAS, 64));
        gc.setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(Gas.HYDROGEN_GAS, 3));
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 4));
        h.runAfterDelay(30, () -> {
            h.assertValueEqual(g.getAmount(), GasHolderBlockEntity.CAPACITY, "full holder stays at capacity");
            h.assertValueEqual(gc.getItem(GasHolderBlockEntity.SLOT_IN).getCount(), 3, "full holder must not take bags from the in slot");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount(), 4, "full holder must not pull from the electrolyzer");
            // 1袋取り出す → 999 になり、また吸い込める
            gc.setItem(GasHolderBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        });
        h.runAfterDelay(60, () -> {
            h.assertTrue(gc.getItem(GasHolderBlockEntity.SLOT_IN).getCount() < 3 || ec.getItem(ElectrolyzerBlockEntity.SLOT_H2).getCount() < 4,
                    "after taking bags out, the holder should accept gas again");
            LOG.info("[LAB] gas holder full ({} mol): in-slot and electrolyzer untouched; resumes after emptying the out slot", GasHolderBlockEntity.CAPACITY);
            h.succeed();
        });
    }

    // =====================================================================
    // [6-03] 圧縮機（ガスホルダーのガス → 鋼のボンベに 50mol）
    // =====================================================================

    @GameTest
    public void compressorFillsCylinders(GameTestHelper h) {
        BlockPos cp = p(1, 1, 1), hp = p(2, 1, 1);
        h.setBlock(cp, Gas.COMPRESSOR.block);
        CompressorBlockEntity be = h.getBlockEntity(cp, CompressorBlockEntity.class);
        var c = be.getContainer();
        int E = CompressorBlockEntity.SLOT_EMPTY, OUT = CompressorBlockEntity.SLOT_OUT, T = CompressorBlockEntity.TOTAL_TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS);
        c.setItem(E, new ItemStack(Gas.EMPTY_GAS_CYLINDER, 3));

        // ホルダーなし：電気があっても動かない
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no gas holder -> nothing filled");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 水素 40mol（50 に足りない）→ 動かない
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(Gas.HYDROGEN_GAS, 64)); // ホルダーの出口を埋めて量を固定
        g.setContentsForTest(GasType.HYDROGEN, 40);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "40 mol is not enough for a 50 mol cylinder");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());

        // 水素 120mol、電気なし → 動かない
        g.setContentsForTest(GasType.HYDROGEN, 120);
        tickConsumer(h, cp, be, T + 10, 0);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no power -> nothing filled");

        // 電気あり：124秒で水素ボンベ1本、ホルダー -50、空ボンベ -1
        tickConsumer(h, cp, be, T - 1, feed);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(cp, BlockStateProperties.LIT, true);
        tickConsumer(h, cp, be, 1, feed);
        h.assertTrue(c.getItem(OUT).is(Gas.HYDROGEN_CYLINDER), "H2 holder -> hydrogen cylinder");
        h.assertValueEqual(g.getAmount(), 70, "holder lost 50 mol");
        h.assertValueEqual(c.getItem(E).getCount(), 2, "one empty cylinder used");

        // ホルダーの中身を酸素に替える → 出口に水素ボンベがあるので酸素ボンベは置けない＝止まる
        g.setContentsForTest(GasType.OXYGEN, 100);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertValueEqual(g.getAmount(), 100, "out slot holds H2 cylinders -> O2 must not be compressed");
        // 出口を空ける → 酸素ボンベ
        c.setItem(OUT, ItemStack.EMPTY);
        tickConsumer(h, cp, be, T, feed);
        h.assertTrue(c.getItem(OUT).is(Gas.OXYGEN_CYLINDER), "O2 holder -> oxygen cylinder");
        h.assertValueEqual(g.getAmount(), 50, "O2 holder lost 50 mol");
        h.assertValueEqual(GasType.ofCylinder(c.getItem(OUT)), GasType.OXYGEN, "GasType.ofCylinder");

        // 空ボンベがなくなったら止まる
        c.setItem(E, ItemStack.EMPTY);
        tickConsumer(h, cp, be, T + 10, feed);
        h.assertValueEqual(g.getAmount(), 50, "no empty cylinder -> holder untouched");
        h.assertBlockProperty(cp, BlockStateProperties.LIT, false);
        LOG.info("[LAB] compressor: 50 mol from an adjacent holder -> 1 cylinder in {} ticks @5 kW (620 kJ); H2 and O2; stops without holder/gas/power/cylinder", T);
        h.succeed();
    }

    // =====================================================================
    // [6-07] 空気分離（タスク12）
    // =====================================================================

    /** 空気10mol/回：N2 7.808・O2 2.095・Ar 0.093 mol を mmol で持ち越して袋にする。電気なしでは動かない。隣のホルダーが N2 を吸い、圧縮機で窒素ボンベ */
    @GameTest(maxTicks = 100)
    public void airSeparation(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "air_separation_unit", Gas.COMPRESSOR.block.asItem()), "Linde's machine starts with a compressor [6-03]");
        floor(h, 7);
        BlockPos ap = p(1, 1, 1), hp = p(2, 1, 1), cp = p(3, 1, 1);
        h.setBlock(ap, Gas.AIR_SEPARATION_UNIT.block);
        var asu = h.getBlockEntity(ap, com.taku3.semiconductorlab.block.AirSeparationBlockEntity.class);
        var box = asu.getContainer();
        int T = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.TICKS;
        int N2 = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_N2, O2 = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_O2, AR = com.taku3.semiconductorlab.block.AirSeparationBlockEntity.SLOT_AR;
        tickConsumer(h, ap, asu, T + 10, 0);
        h.assertTrue(box.getItem(N2).isEmpty(), "no power -> no separation");
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirSeparationBlockEntity.WATTS);
        tickConsumer(h, ap, asu, T, feed);
        h.assertValueEqual(box.getItem(N2).getCount(), 7, "run 1: 7.808 mol N2 -> 7 bags");
        h.assertValueEqual(box.getItem(O2).getCount(), 2, "run 1: 2.095 mol O2 -> 2 bags");
        h.assertTrue(box.getItem(AR).isEmpty(), "run 1: 0.093 mol Ar -> no bag yet");
        int n2 = 0, o2 = 0;
        for (int run = 1; run <= 11; run++) {
            if (run > 1) tickConsumer(h, ap, asu, T, feed);
            n2 += box.getItem(N2).getCount();
            o2 += box.getItem(O2).getCount();
            box.setItem(N2, ItemStack.EMPTY); box.setItem(O2, ItemStack.EMPTY);
            if (run == 10) h.assertTrue(box.getItem(AR).isEmpty(), "10 runs = 100 mol air = 0.93 mol Ar -> still no bag");
        }
        h.assertValueEqual(n2, 85, "11 runs: 85.888 mol N2 -> 85 bags");
        h.assertValueEqual(o2, 23, "11 runs: 23.045 mol O2 -> 23 bags");
        h.assertTrue(box.getItem(AR).is(Gas.ARGON_GAS) && box.getItem(AR).getCount() == 1, "11 runs = 110 mol air -> 1 bag of argon, got " + box.getItem(AR));
        h.assertValueEqual(asu.argonRemainderMmol(), 23, "argon carry-over 1023 - 1000 mmol");
        box.setItem(N2, new ItemStack(Gas.NITROGEN_GAS, 5));
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        h.setBlock(cp, Gas.COMPRESSOR.block);
        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            int got = g.getAmount() + g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertTrue(got >= 5, "the holder pulls nitrogen from the separator, got " + got);
            g.setContentsForTest(GasType.NITROGEN, 60);
            CompressorBlockEntity comp = h.getBlockEntity(cp, CompressorBlockEntity.class);
            comp.getContainer().setItem(CompressorBlockEntity.SLOT_EMPTY, new ItemStack(Gas.EMPTY_GAS_CYLINDER, 1));
            tickConsumer(h, cp, comp, CompressorBlockEntity.TOTAL_TICKS, EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS));
            h.assertTrue(comp.getContainer().getItem(CompressorBlockEntity.SLOT_OUT).is(Gas.NITROGEN_CYLINDER), "compressor -> nitrogen cylinder");
            h.assertTrue(!GasType.explosive(GasType.NITROGEN, GasType.HYDROGEN) && !GasType.explosive(GasType.ARGON, GasType.OXYGEN), "N2 and Ar react with nothing");
            h.assertTrue(GasType.ARGON.cylinderItem() == Gas.ARGON_CYLINDER, "argon has a cylinder");
            LOG.info("[LAB] air separation: 11 runs (110 mol air) -> N2 {} / O2 {} / Ar 1 bags, holder pulls N2, compressor -> N2 cylinder", 85, 23);
            h.succeed();
        });
    }

    // =====================================================================
    // タスク09：塩素の鎖 [6-04][0-16] → [6-05] → [6-06] → WO3 本物化・[1-34] GeCl4
    // =====================================================================

    @GameTest public void oreRockSalt(GameTestHelper h) { oreFeature(h, "rock_salt_ore", Blocks.STONE, Blocks.DEEPSLATE, Gas.ROCK_SALT_ORE, s -> s.is(Blocks.STONE)); }

    /** [6-04] 岩塩鉱：ツルハシで岩塩、シルクタッチでブロック。砂漠・荒野だけ。岩塩→作業台→塩 */
    @GameTest
    public void rockSalt(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        BlockPos at = h.absolutePos(p(1, 1, 1));
        ItemStack pick = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        BlockState st = Gas.ROCK_SALT_ORE.defaultBlockState();
        h.assertTrue(pick.isCorrectToolForDrops(st), "rock salt is soft: a wooden pickaxe is enough");
        h.assertFalse(ItemStack.EMPTY.isCorrectToolForDrops(st), "not by hand");
        List<ItemStack> normal = Block.getDrops(st, lv, at, null, null, pick);
        List<ItemStack> silky = Block.getDrops(st, lv, at, null, null, silk);
        h.assertTrue(!normal.isEmpty() && normal.stream().allMatch(s -> s.is(Gas.ROCK_SALT)), "pickaxe -> rock salt, got " + normal);
        h.assertTrue(silky.size() == 1 && silky.get(0).is(Gas.ROCK_SALT_ORE.asItem()), "silk touch -> block, got " + silky);
        h.assertTrue(recipeUses(h, "salt_from_rock_salt", Gas.ROCK_SALT), "rock salt -> salt");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.DESERT, "rock_salt_ore"), "rock salt under deserts");
        h.assertTrue(biomeHas(h, net.minecraft.world.level.biome.Biomes.BADLANDS, "rock_salt_ore"), "rock salt under badlands");
        h.assertFalse(biomeHas(h, net.minecraft.world.level.biome.Biomes.PLAINS, "rock_salt_ore"), "no rock salt under plains");
        LOG.info("[LAB] rock salt: pickaxe -> rock salt, silk -> block, crafting -> salt, desert/badlands only");
        h.succeed();
    }

    /** [6-05] クロルアルカリ電解：水＋塩×2 → H2×1（陰極）＋Cl2×1（陽極）＋NaOH×2。硫酸を入れれば従来どおり水の電気分解 */
    @GameTest
    public void chlorAlkali(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Gas.ELECTROLYZER.block);
        ElectrolyzerBlockEntity be = h.getBlockEntity(pos, ElectrolyzerBlockEntity.class);
        var c = be.getContainer();
        int W = ElectrolyzerBlockEntity.SLOT_WATER, S = ElectrolyzerBlockEntity.SLOT_ACID, H2 = ElectrolyzerBlockEntity.SLOT_H2,
                AN = ElectrolyzerBlockEntity.SLOT_O2, NA = ElectrolyzerBlockEntity.SLOT_NAOH, T = ElectrolyzerBlockEntity.BRINE_TICKS;
        long feed = 250;
        // 塩1個では足りない（2NaCl）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET));
        c.setItem(S, new ItemStack(Gas.SALT, 1));
        tickConsumer(h, pos, be, T + 10, feed);
        h.assertTrue(c.getItem(H2).isEmpty(), "1 salt is not enough for 2NaCl");
        ((com.taku3.semiconductorlab.energy.SimpleEnergyStorage) be.getStorage()).extractRaw(be.getStorage().getStoredJ());
        // 塩3個：128秒で1回分
        c.setItem(S, new ItemStack(Gas.SALT, 3));
        h.assertValueEqual(be.mode(), ElectrolyzerBlockEntity.MODE_BRINE, "salt -> brine mode");
        tickConsumer(h, pos, be, T - 1, feed);
        h.assertTrue(c.getItem(H2).isEmpty(), "not finished 1 tick before 128 s");
        tickConsumer(h, pos, be, 1, feed);
        h.assertTrue(c.getItem(H2).is(Gas.HYDROGEN_GAS) && c.getItem(H2).getCount() == 1, "cathode: 1 H2, got " + c.getItem(H2));
        h.assertTrue(c.getItem(AN).is(Gas.CHLORINE_GAS) && c.getItem(AN).getCount() == 1, "anode: 1 Cl2, got " + c.getItem(AN));
        h.assertTrue(c.getItem(NA).is(Gas.SODIUM_HYDROXIDE) && c.getItem(NA).getCount() == 2, "2 NaOH, got " + c.getItem(NA));
        h.assertValueEqual(c.getItem(S).getCount(), 1, "2 salt consumed");
        h.assertTrue(c.getItem(W).is(Items.BUCKET), "water used");
        // 陽極の出口に酸素が残っていると塩素は出せない（混ぜない）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET));
        c.setItem(S, new ItemStack(Gas.SALT, 2));
        c.setItem(AN, new ItemStack(Gas.OXYGEN_GAS, 1));
        tickConsumer(h, pos, be, T + 10, feed);
        h.assertTrue(c.getItem(W).is(Items.WATER_BUCKET), "anode outlet holds O2 -> brine run blocked");
        // 硫酸に戻すと水の電気分解（NaOH は出ない、塩素も出ない）
        c.setItem(AN, ItemStack.EMPTY);
        c.setItem(H2, ItemStack.EMPTY);
        c.setItem(NA, ItemStack.EMPTY);
        c.setItem(S, new ItemStack(Chemistry.SULFURIC_ACID, 1));
        tickConsumer(h, pos, be, ElectrolyzerBlockEntity.TOTAL_TICKS, feed);
        h.assertTrue(c.getItem(AN).is(Gas.OXYGEN_GAS), "acid -> water electrolysis, O2 at the anode");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "2 H2 in water mode");
        h.assertTrue(c.getItem(NA).isEmpty(), "no NaOH in water mode");
        // 塩素はボンベとガスホルダーに対応、水素と混ぜると爆発（塩素爆鳴気）、酸素とは爆発しない
        h.assertTrue(GasType.CHLORINE.cylinderItem() == Gas.CHLORINE_CYLINDER && GasType.of(new ItemStack(Gas.CHLORINE_GAS)) == GasType.CHLORINE, "chlorine gas type");
        h.assertTrue(GasType.explosive(GasType.HYDROGEN, GasType.CHLORINE), "H2 + Cl2 explodes");
        h.assertFalse(GasType.explosive(GasType.OXYGEN, GasType.CHLORINE), "O2 + Cl2 does not explode");
        LOG.info("[LAB] chlor-alkali: water + 2 salt -> H2 + Cl2 + 2 NaOH in {} ticks; acid -> H2 x2 + O2", T);
        h.succeed();
    }

    /** 塩素：電解槽の陽極からガスホルダーへ吸われ、圧縮機で塩素ボンベになる。水素のホルダーに塩素を入れると爆発 */
    @GameTest(maxTicks = 80)
    public void chlorineHolderAndCylinder(GameTestHelper h) {
        BlockPos hp = p(1, 1, 1), ep = p(2, 1, 1), cp = p(0, 1, 1), h2p = p(1, 1, 3);
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        h.setBlock(ep, Gas.ELECTROLYZER.block);
        var ec = h.getBlockEntity(ep, ElectrolyzerBlockEntity.class).getContainer();
        ec.setItem(ElectrolyzerBlockEntity.SLOT_O2, new ItemStack(Gas.CHLORINE_GAS, 3));
        ec.setItem(ElectrolyzerBlockEntity.SLOT_NAOH, new ItemStack(Gas.SODIUM_HYDROXIDE, 2));
        h.setBlock(h2p, Gas.GAS_HOLDER.block);
        h.runAfterDelay(25, () -> {
            GasHolderBlockEntity g = h.getBlockEntity(hp, GasHolderBlockEntity.class);
            int total = g.getAmount() + g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT).getCount();
            h.assertValueEqual(g.getGasType() == GasType.NONE ? GasType.of(g.getContainer().getItem(GasHolderBlockEntity.SLOT_OUT)) : g.getGasType(), GasType.CHLORINE, "holder took chlorine");
            h.assertValueEqual(total, 3, "3 mol Cl2 pulled");
            h.assertValueEqual(ec.getItem(ElectrolyzerBlockEntity.SLOT_NAOH).getCount(), 2, "NaOH is not a gas: stays");
            // 圧縮機：塩素 60mol → 塩素ボンベ
            h.setBlock(cp, Gas.COMPRESSOR.block);
            CompressorBlockEntity comp = h.getBlockEntity(cp, CompressorBlockEntity.class);
            g.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(Gas.CHLORINE_GAS, 64));
            g.setContentsForTest(GasType.CHLORINE, 60);
            comp.getContainer().setItem(CompressorBlockEntity.SLOT_EMPTY, new ItemStack(Gas.EMPTY_GAS_CYLINDER, 1));
            tickConsumer(h, cp, comp, CompressorBlockEntity.TOTAL_TICKS, EnergyStorage.wattsToJoulesPerTick(CompressorBlockEntity.WATTS));
            h.assertTrue(comp.getContainer().getItem(CompressorBlockEntity.SLOT_OUT).is(Gas.CHLORINE_CYLINDER), "compressor -> chlorine cylinder");
            h.assertValueEqual(g.getAmount(), 10, "60 - 50 mol");
            // 塩素爆鳴気：水素の入ったホルダーに塩素の袋
            GasHolderBlockEntity g2 = h.getBlockEntity(h2p, GasHolderBlockEntity.class);
            g2.setContentsForTest(GasType.HYDROGEN, 30);
            g2.getContainer().setItem(GasHolderBlockEntity.SLOT_IN, new ItemStack(Gas.CHLORINE_GAS, 1));
        });
        h.runAfterDelay(30, () -> {
            GasHolderBlockEntity g2 = h.getBlockEntity(h2p, GasHolderBlockEntity.class);
            h.assertValueEqual(g2.getAmount(), 0, "H2 + Cl2 bang empties the holder");
            LOG.info("[LAB] chlorine: electrolyzer anode -> holder, compressor -> chlorine cylinder, H2 holder + Cl2 -> bang");
            h.succeed();
        });
    }

    /** [6-06] 塩酸合成炉：H2 1mol ＋ Cl2 1mol ＋ 水 1/4 バケツ（100 tick）→ 塩酸1。袋・ボンベ・隣のホルダーから。片方だけでは燃えない */
    @GameTest(maxTicks = 80)
    public void hclBurner(GameTestHelper h) {
        int T = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.BURN_TICKS;
        int H2 = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_H2, CL = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_CL2,
                W = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_WATER, OUT = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_OUT,
                EMPTY = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_EMPTY, BUCKET = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_BUCKET,
                BOTTLE = com.taku3.semiconductorlab.block.HclBurnerBlockEntity.SLOT_BOTTLE;
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Gas.HCL_BURNER.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.HclBurnerBlockEntity.class);
        var c = be.getContainer();
        // 水だけ・水素だけ：燃えない（水素の袋は開けない）
        c.setItem(W, new ItemStack(Items.WATER_BUCKET, 1));
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_GAS, 3));
        tickHcl(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "hydrogen without chlorine -> no flame");
        h.assertValueEqual(c.getItem(H2).getCount(), 3, "H2 bags not opened without chlorine");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, false);
        // 塩素の袋を足しても、空のガラス瓶がなければ塩酸は作らない（IMP-33：容器）
        c.setItem(CL, new ItemStack(Gas.CHLORINE_GAS, 3));
        tickHcl(h, pos, be, T + 10);
        h.assertTrue(c.getItem(OUT).isEmpty(), "no glass bottle -> no hydrochloric acid");
        h.assertValueEqual(c.getItem(CL).getCount(), 3, "no bottle -> chlorine bags not opened");
        // 瓶を入れる：100 tick で塩酸1、バケツは空になって出る（水は残り3本分）
        c.setItem(BOTTLE, new ItemStack(Chemistry.GLASS_BOTTLE, 4));
        tickHcl(h, pos, be, T - 1);
        h.assertTrue(c.getItem(OUT).isEmpty(), "not finished 1 tick early");
        h.assertBlockProperty(pos, BlockStateProperties.LIT, true);
        tickHcl(h, pos, be, 1);
        h.assertTrue(c.getItem(OUT).is(Gas.HYDROCHLORIC_ACID), "H2 + Cl2 -> 2HCl -> hydrochloric acid");
        h.assertValueEqual(c.getItem(H2).getCount(), 2, "1 mol H2 used");
        h.assertValueEqual(c.getItem(CL).getCount(), 2, "1 mol Cl2 used (1:1)");
        h.assertTrue(c.getItem(BUCKET).is(Items.BUCKET), "empty bucket comes out");
        h.assertValueEqual(be.getWater(), 3, "1 bucket = 4 bottles of acid");
        // ボンベ：塩素ボンベ1本＝50mol、空ボンベが出る
        c.setItem(CL, new ItemStack(Gas.CHLORINE_CYLINDER, 1));
        c.setItem(H2, new ItemStack(Gas.HYDROGEN_CYLINDER, 1));
        tickHcl(h, pos, be, T * 3);
        h.assertValueEqual(c.getItem(OUT).getCount(), 4, "4 acids from 1 bucket");
        h.assertValueEqual(c.getItem(EMPTY).getCount(), 2, "H2 and Cl2 cylinders opened -> 2 empties");
        h.assertTrue(c.getItem(BOTTLE).isEmpty(), "1 glass bottle per acid (4 used)");
        h.assertValueEqual(be.getCl2(), 47, "Cl2 50 - 3 mol");
        // 水がなくなると止まる
        tickHcl(h, pos, be, T * 2);
        h.assertValueEqual(c.getItem(OUT).getCount(), 4, "no water -> stops");
        // 出口を空けると、水なし＝乾いた塩化水素ガス 2mol（[1-16] 用、タスク22）
        c.setItem(OUT, ItemStack.EMPTY);
        tickHcl(h, pos, be, T);
        h.assertTrue(c.getItem(OUT).is(Metallurgy.HYDROGEN_CHLORIDE_GAS) && c.getItem(OUT).getCount() == 2, "no water -> dry HCl gas x2, got " + c.getItem(OUT));
        h.assertValueEqual(be.getWater(), 0, "dry mode uses no water");
        // 隣のガスホルダー（塩素）からももらえる
        BlockPos pos2 = p(3, 1, 1), hp = p(4, 1, 1);
        h.setBlock(pos2, Gas.HCL_BURNER.block);
        h.setBlock(hp, Gas.GAS_HOLDER.block);
        var be2 = h.getBlockEntity(pos2, com.taku3.semiconductorlab.block.HclBurnerBlockEntity.class);
        GasHolderBlockEntity holder = h.getBlockEntity(hp, GasHolderBlockEntity.class);
        holder.getContainer().setItem(GasHolderBlockEntity.SLOT_OUT, new ItemStack(Gas.CHLORINE_GAS, 64));
        holder.setContentsForTest(GasType.CHLORINE, 20);
        be2.getContainer().setItem(W, new ItemStack(Items.WATER_BUCKET, 1));
        be2.getContainer().setItem(H2, new ItemStack(Gas.HYDROGEN_GAS, 4));
        ServerLevel lv = h.getLevel();
        // 引き込みは 10 tick ごと（ゲーム時刻）。tick を手で回すとゲーム時刻は進まないので、引き込みを直接確かめる
        long t0 = lv.getGameTime();
        h.assertTrue(recipeUses(h, "hcl_burner", Optics.OXYHYDROGEN_BURNER.block.asItem()), "derived from the oxyhydrogen burner [7-02]");
        h.runAfterDelay(25, () -> {
            h.assertTrue(be2.getCl2() > 0 || be2.getContainer().getItem(OUT).getCount() > 0, "pulled chlorine from the adjacent holder");
            h.assertTrue(holder.getAmount() < 20, "holder chlorine decreased");
            LOG.info("[LAB] HCl burner: H2 + Cl2 + 1/4 bucket -> 1 hydrochloric acid in {} ticks; bags, cylinders and adjacent holder (from game time {})", T, t0);
            h.succeed();
        });
    }

    /** [6-16] 天然硝石（IMP2-02）：硝石土は鍾乳石ブロックだけを置き換える。土×4＋木灰＋水 → 浸出液（バケツは戻る）→かまど→ 硝石 */
    @GameTest public void oreNitreEarth(GameTestHelper h) { oreFeature(h, "nitre_earth_block", Blocks.DRIPSTONE_BLOCK, Blocks.STONE, Gas.NITRE_EARTH_BLOCK, s -> s.is(Blocks.DRIPSTONE_BLOCK)); }

    @GameTest
    public void saltpeterFromNitreEarth(GameTestHelper h) {
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Gas.NITRE_EARTH_BLOCK);
        var drops = net.minecraft.world.level.block.Block.getDrops(h.getBlockState(pos), h.getLevel(), h.absolutePos(pos), null, null, new ItemStack(Items.WOODEN_SHOVEL));
        h.assertTrue(drops.size() == 1 && drops.get(0).is(Gas.NITRE_EARTH) && drops.get(0).getCount() == 2, "shovel -> 2 nitre earth, got " + drops);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, SemiconductorLab.id("nitre_liquor"));
        var ings = h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow().value().placementInfo().ingredients();
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Gas.NITRE_EARTH))).count(), 4L, "4 nitre earth");
        h.assertValueEqual(ings.stream().filter(i -> i.test(new ItemStack(Chemistry.WOOD_ASH))).count(), 1L, "wood ash (K2CO3 turns Ca(NO3)2 into KNO3)");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Gas.NITRE_LIQUOR).is(Gas.SALTPETER), "liquor boiled down -> saltpeter");
        LOG.info("[LAB] saltpeter [6-16]: nitre earth (dripstone caves) x4 + wood ash + water -> liquor -> furnace -> KNO3");
        h.succeed();
    }

    /** [6-11] 硝酸・硝酸銀（IMP2-02）：硝石＋硫酸＋空き瓶＋石英の蒸留器 → 硝酸（瓶入り）。硫酸の空き瓶と蒸留器が戻る。銀＋硝酸 → 硝酸銀（空き瓶が戻る） */
    @GameTest
    public void nitricAcidAndSilverNitrate(GameTestHelper h) {
        java.util.function.Function<net.minecraft.world.item.Item[], ItemStack[]> craft = items -> {
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < 9; i++) list.add(i < items.length ? new ItemStack(items[i]) : ItemStack.EMPTY);
            var input = net.minecraft.world.item.crafting.CraftingInput.ofPositioned(3, 3, list).input();
            var r = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel());
            if (r.isEmpty()) return new ItemStack[0];
            List<ItemStack> out = new ArrayList<>(List.of(r.get().value().assemble(input)));
            r.get().value().getRemainingItems(input).stream().filter(x -> !x.isEmpty()).forEach(out::add);
            return out.toArray(new ItemStack[0]);
        };
        var res = craft.apply(new net.minecraft.world.item.Item[] {Gas.SALTPETER, Chemistry.SULFURIC_ACID, com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL, Chemistry.GLASS_BOTTLE});
        h.assertTrue(res.length == 3 && res[0].is(Gas.NITRIC_ACID), "KNO3 + H2SO4 distilled -> nitric acid");
        long bottles = java.util.Arrays.stream(res).filter(x -> x.is(Chemistry.GLASS_BOTTLE)).count();
        long stills = java.util.Arrays.stream(res).filter(x -> x.is(com.taku3.semiconductorlab.content.Metallurgy.QUARTZ_STILL)).count();
        h.assertTrue(bottles == 1 && stills == 1, "sulfuric acid's bottle and the still come back (bottles in 2 = out 2)");
        h.assertTrue(com.taku3.semiconductorlab.machine.Vessel.of(Gas.NITRIC_ACID) == com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE, "nitric acid lives in a glass bottle");
        h.assertValueEqual(craft.apply(new net.minecraft.world.item.Item[] {Gas.SALTPETER, Chemistry.SULFURIC_ACID, Chemistry.GLASS_BOTTLE}).length, 0, "no still -> no distillation");
        var ag = craft.apply(new net.minecraft.world.item.Item[] {com.taku3.semiconductorlab.content.Metallurgy.SILVER_NUGGET, Gas.NITRIC_ACID});
        h.assertTrue(ag.length == 2 && ag[0].is(Gas.SILVER_NITRATE) && ag[1].is(Chemistry.GLASS_BOTTLE), "Ag + HNO3 -> AgNO3, bottle back");
        LOG.info("[LAB] nitric acid [6-11]: KNO3 + H2SO4 (still) -> HNO3 in a bottle; Ag + HNO3 -> AgNO3");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Gas.ELECTROLYZER.block, "水の電気分解", L(io(Items.WATER_BUCKET, 1), kept(io(Chemistry.SULFURIC_ACID, 1))),
            L(io(Gas.HYDROGEN_GAS, 2), io(Gas.OXYGEN_GAS, 1), io(Items.BUCKET, 1)), ElectrolyzerBlockEntity.WATTS, ElectrolyzerBlockEntity.TOTAL_TICKS / 20.0, "2H2O → 2H2 + O2。硫酸は電解質");
        rec(a, Gas.ELECTROLYZER.block, "塩水の電気分解（クロルアルカリ）", L(io(Items.WATER_BUCKET, 1), io(Gas.SALT, ElectrolyzerBlockEntity.SALT_PER_RUN)),
            L(io(Gas.HYDROGEN_GAS, 1), io(Gas.CHLORINE_GAS, 1), io(Gas.SODIUM_HYDROXIDE, 2), io(Items.BUCKET, 1)), ElectrolyzerBlockEntity.WATTS, ElectrolyzerBlockEntity.BRINE_TICKS / 20.0, "2NaCl + 2H2O → Cl2 + H2 + 2NaOH");
        rec(a, Gas.COMPRESSOR.block, "ボンベに詰める", L(io(Gas.EMPTY_GAS_CYLINDER, 1), mol("隣のガスホルダーのガス", CompressorBlockEntity.MOL_PER_CYLINDER)),
            L(io("そのガスのボンベ", 1)), CompressorBlockEntity.WATTS, CompressorBlockEntity.TOTAL_TICKS / 20.0, "約150気圧。H2・O2・Cl2・N2・Ar");
        rec(a, Gas.HCL_BURNER.block, "塩酸を作る", L(mol(Gas.HYDROGEN_GAS, HclBurnerBlockEntity.H2_PER), mol(Gas.CHLORINE_GAS, HclBurnerBlockEntity.CL2_PER), io(Items.WATER_BUCKET, 1.0 / HclBurnerBlockEntity.ACID_PER_BUCKET), io(Chemistry.GLASS_BOTTLE, 1)),
            L(io(Gas.HYDROCHLORIC_ACID, 1)), 0, HclBurnerBlockEntity.BURN_TICKS / 20.0, "H2 + Cl2 → 2HCl を水に溶かす。電気なし");
        rec(a, Gas.AIR_SEPARATION_UNIT.block, "空気分離", L(mol("空気", AirSeparationBlockEntity.AIR_MOL)),
            L(mol(Gas.NITROGEN_GAS, AirSeparationBlockEntity.N2_PER_RUN / 1000.0), mol(Gas.OXYGEN_GAS, AirSeparationBlockEntity.O2_PER_RUN / 1000.0), mol(Gas.ARGON_GAS, AirSeparationBlockEntity.AR_PER_RUN / 1000.0)),
            AirSeparationBlockEntity.WATTS, AirSeparationBlockEntity.TICKS / 20.0, "端数は持ち越して袋（1 mol）にする");
        rec(a, Gas.HCL_BURNER.block, "乾いた塩化水素", L(mol(Gas.HYDROGEN_GAS, HclBurnerBlockEntity.H2_PER), mol(Gas.CHLORINE_GAS, HclBurnerBlockEntity.CL2_PER)),
            L(mol(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2)), 0, HclBurnerBlockEntity.BURN_TICKS / 20.0, "水のスロットが空のとき。H2 + Cl2 → 2HCl を水に溶かさず袋に。電気なし");
    }

    /** [6-09] ハーバー・ボッシュ（IMP3、S-005）：触媒なしでは動かない、N2＋3H2 → 2NH3（触媒は減らない）、硫酸で肥料、耕地 → 窒素の多い耕地 */
    @GameTest(maxTicks = 100)
    public void haberBosch(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "iron_catalyst", com.taku3.semiconductorlab.content.Metallurgy.MAGNETITE) && recipeUses(h, "iron_catalyst", Chemistry.POTASH), "Mittasch catalyst: magnetite + potash + alumina");
        h.assertTrue(recipeUses(h, "ammonium_sulfate", Gas.AMMONIA_GAS) && recipeUses(h, "ammonium_sulfate", Chemistry.SULFURIC_ACID), "NH3 + H2SO4 -> fertilizer");
        floor(h, 3);
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, Gas.HABER_BOSCH_REACTOR.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.class);
        var c = be.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.WATTS);
        int T = com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.TICKS;
        c.setItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_N2, new ItemStack(Gas.NITROGEN_GAS, 2));
        c.setItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, 6));
        tickConsumer(h, fp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_OUT).isEmpty(), "no catalyst -> no ammonia");
        c.setItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_CATALYST, new ItemStack(Gas.IRON_CATALYST));
        for (int i = 0; i < 2; i++) tickConsumer(h, fp, be, T, feed);
        ItemStack nh3 = c.getItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_OUT);
        h.assertTrue(nh3.is(Gas.AMMONIA_GAS) && nh3.getCount() == 4, "2 N2 + 6 H2 -> 4 NH3, got " + nh3);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.HaberBoschReactorBlockEntity.SLOT_CATALYST).is(Gas.IRON_CATALYST), "catalyst stays");
        BlockPos field = p(3, 1, 1);
        h.setBlock(field, net.minecraft.world.level.block.Blocks.FARMLAND);
        h.assertTrue(com.taku3.semiconductorlab.item.FertilizerItem.fertilize(h.getLevel(), h.absolutePos(field)), "fertilize farmland");
        h.assertTrue(h.getLevel().getBlockState(h.absolutePos(field)).is(com.taku3.semiconductorlab.content.Survival.NITROGEN_FARMLAND), "-> nitrogen-rich farmland");
        LOG.info("[LAB] Haber-Bosch [6-09]: no catalyst -> nothing; N2 + 3H2 -> 2NH3 on iron; NH3 + H2SO4 -> ammonium sulfate -> nitrogen-rich farmland");
        h.succeed();
    }
}
