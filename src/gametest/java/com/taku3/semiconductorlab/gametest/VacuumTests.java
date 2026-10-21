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
 * GameTest：幹5 真空（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class VacuumTests extends LabTestBase {
    /** [5-07] ターボ分子ポンプ（IMP2 O-017）：精密な玉軸受（段階2以上）がないと回せない。回転ポンプを前段に 10⁻⁵Pa 以下まで。拡散ポンプは水銀の蒸気が容器に戻る */
    @GameTest(maxTicks = 2000)
    public void turbo_pump(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "turbo_pump", Metallurgy.ALUMINIUM_INGOT), "aluminium rotor blades");
        BlockPos c = p(2, 1, 1), t = p(1, 1, 1), r2 = p(0, 1, 1), d = p(3, 1, 1), r = p(4, 1, 1);
        h.setBlock(c, Vacuum.VACUUM_CHAMBER.block);
        var ch = h.getBlockEntity(c, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class);
        ch.setPressure(1.0);
        h.setBlock(t, Vacuum.TURBO_PUMP.block);
        h.setBlock(r2, Vacuum.ROTARY_PUMP.block);
        var tp = h.getBlockEntity(t, com.taku3.semiconductorlab.block.TurboPumpBlockEntity.class);
        var rp2 = h.getBlockEntity(r2, com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.class);
        long ft = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.TurboPumpBlockEntity.WATTS), fr = EnergyStorage.wattsToJoulesPerTick(1000);
        for (int k = 0; k < 40; k++) { tickConsumer(h, r2, rp2, 1, fr); tickConsumer(h, t, tp, 1, ft); }
        h.assertValueEqual(tp.status(), com.taku3.semiconductorlab.block.TurboPumpBlockEntity.STATUS_NO_BEARING, "no precise bearing -> cannot spin at tens of thousands rpm");
        h.assertFalse(tp.installBearing(0), "nothing to install");
        tp.installBearing(1);
        h.assertValueEqual(tp.status(), com.taku3.semiconductorlab.block.TurboPumpBlockEntity.STATUS_NO_BEARING, "hand-rounded balls are too rough");
        tp.installBearing(2);
        for (int k = 0; k < 1200; k++) { tickConsumer(h, r2, rp2, 1, fr); tickConsumer(h, t, tp, 1, ft); }
        h.assertTrue(ch.getPressure() < 1e-5, "turbo pump with a ground-ball bearing -> high vacuum: " + ch.getPressure() + " Pa");
        h.assertFalse(com.taku3.semiconductorlab.block.TurboPumpBlockEntity.backstreams(h.getLevel(), h.absolutePos(c)), "no vapour comes back from a turbo pump");
        // 拡散ポンプを動かすと水銀の蒸気が戻る
        h.setBlock(d, Vacuum.DIFFUSION_PUMP.block);
        h.setBlock(r, Vacuum.ROTARY_PUMP.block);
        var dp = h.getBlockEntity(d, com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.class);
        var rp = h.getBlockEntity(r, com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.class);
        for (int k = 0; k < 40; k++) { tickConsumer(h, r, rp, 1, fr); tickConsumer(h, d, dp, 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.WATTS)); }
        h.assertTrue(com.taku3.semiconductorlab.block.TurboPumpBlockEntity.backstreams(h.getLevel(), h.absolutePos(c)), "a running mercury diffusion pump backstreams vapour into the chamber");
        h.assertTrue(langJson("ja_jp").has("gui.semiconductor-lab.pump.no_bearing"), "lang no_bearing");
        LOG.info("[LAB] turbo_pump [5-07]: needs bearing grade 2+, rotary backing -> " + ch.getPressure() + " Pa; diffusion pump backstreams mercury");
        h.succeed();
    }

    // =====================================================================
    // [5-01] 手押しピストンポンプ ＋ [5-11] 真空容器
    // =====================================================================

    /** 1回で p' = 1000 + (p-1000)*0.8。何回引いても約 1kPa より下がらない。容器がなければ何もしない。放っておくと漏れて上がる */
    @GameTest(maxTicks = 80)
    public void handPumpEvacuates(GameTestHelper h) {
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), pp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(pp, Vacuum.HAND_VACUUM_PUMP);
        h.setBlock(lonely, Vacuum.HAND_VACUUM_PUMP);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        h.assertTrue(Math.abs(c.getPressure() - VacuumChamberBlockEntity.ATM) < 1e-6, "a new chamber starts at 1 atm");
        HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp));
        double one = 1000 + (VacuumChamberBlockEntity.ATM - 1000) * 0.8;
        h.assertTrue(Math.abs(c.getPressure() - one) < 1, "one stroke should give " + one + " Pa, got " + c.getPressure());
        int n = 1;
        while (c.getPressure() > 1500 && n < 200) { HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp)); n++; }
        h.assertTrue(n >= 20 && n <= 30, "about 24 strokes to get below 1.5 kPa, took " + n);
        for (int i = 0; i < 100; i++) HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(pp));
        double floorPa = c.getPressure();
        h.assertTrue(floorPa >= HandVacuumPumpBlock.LIMIT_PA && floorPa < HandVacuumPumpBlock.LIMIT_PA + 1, "hand pump bottoms out at ~1 kPa, got " + floorPa);
        h.assertTrue(HandVacuumPumpBlock.stroke(h.getLevel(), h.absolutePos(lonely)) == null, "a pump with no chamber next to it does nothing");
        final int strokes = n;
        h.runAfterDelay(45, () -> {
            h.assertTrue(c.getPressure() > floorPa, "air should leak back in (~0.002 Pa/s): " + floorPa + " -> " + c.getPressure());
            LOG.info("[LAB] hand pump: 1 stroke -> {}, {} strokes to <1.5 kPa, floor {}, leak to {}", VacuumChamberBlockEntity.formatPa(one), strokes,
                    VacuumChamberBlockEntity.formatPa(floorPa), VacuumChamberBlockEntity.formatPa(c.getPressure()));
            h.succeed();
        });
    }

    // =====================================================================
    // [5-15] シュプレンゲル水銀ポンプ（タスク12）
    // =====================================================================

    /** 水銀4個で動く。1秒ごとに p' = 0.05 + (p-0.05)*0.9。1気圧から約2分で 0.1Pa 以下、漏れがあっても 0.1Pa 未満を保つ。水銀なし・容器なしでは何もしない */
    @GameTest
    public void sprengelPumpReachesSoftVacuum(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sprengel_pump", Chemistry.SODA_LIME_GLASS), "the pump is glass tubing (soda-lime glass)");
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(sp, Vacuum.SPRENGEL_PUMP.block);
        h.setBlock(lonely, Vacuum.SPRENGEL_PUMP.block);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var pump = h.getBlockEntity(sp, com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.class);
        h.assertTrue(pump.pumpOnce() == null && c.getPressure() == VacuumChamberBlockEntity.ATM, "no mercury -> no pumping");
        for (int i = 0; i < com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.MERCURY_NEEDED; i++) h.assertTrue(pump.addMercury(), "mercury " + i + " should go in");
        h.assertTrue(!pump.addMercury(), "the reservoir holds only " + com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.MERCURY_NEEDED);
        int s = 0;
        while (c.getPressure() > SealingStationBlockEntity.SEAL_MAX_PA && s < 600) { pump.pumpOnce(); c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND); s++; }
        h.assertTrue(s >= 90 && s <= 180, "about 2 minutes from 1 atm to 0.1 Pa, took " + s + " s");
        for (int i = 0; i < 300; i++) { pump.pumpOnce(); c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND); }
        double eq = c.getPressure();
        h.assertTrue(eq >= com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.LIMIT_PA && eq < SealingStationBlockEntity.SEAL_MAX_PA, "steady state with the leak stays below 0.1 Pa, got " + eq);
        c.setPressure(HandVacuumPumpBlock.LIMIT_PA);
        int r = 0;
        while (c.getPressure() > SealingStationBlockEntity.SEAL_MAX_PA && r < 600) { pump.pumpOnce(); r++; }
        h.assertTrue(r < s, "roughing to 1 kPa with the hand pump first is faster: " + r + " < " + s);
        var alone = h.getBlockEntity(lonely, com.taku3.semiconductorlab.block.SprengelPumpBlockEntity.class);
        for (int i = 0; i < 4; i++) alone.addMercury();
        h.assertTrue(alone.pumpOnce() == null, "no chamber next to it -> nothing");
        h.destroyBlock(sp);
        final int fromAtm = s, fromRough = r;
        h.runAfterDelay(2, () -> {
            h.assertItemEntityPresent(Metallurgy.MERCURY, sp, 2.0);
            LOG.info("[LAB] Sprengel pump: 4 Hg, 1 atm -> 0.1 Pa in {} s, from 1 kPa in {} s, steady {} with leak, breaking drops the mercury", fromAtm, fromRough, VacuumChamberBlockEntity.formatPa(eq));
            h.succeed();
        });
    }

    /** 辰砂→粉→（かまど）→水銀。真空計は 1気圧で 760mm、1mm 未満は「読めない」。容器に当てると SUCCESS、石には PASS */
    @GameTest
    public void mercuryGauge(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cinnabar_dust", Items.CINNABAR), "cinnabar dust is crushed from the vanilla cinnabar block");
        ItemStack hg = cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.CINNABAR_RETORT_CHARGE);
        h.assertTrue(hg.is(Metallurgy.MERCURY), "roasting cinnabar dust should give mercury, got " + hg);
        h.assertTrue(recipeUses(h, "mercury_vacuum_gauge", Metallurgy.MERCURY), "the gauge needs mercury");
        double atm = MercuryVacuumGaugeItem.mmHg(VacuumChamberBlockEntity.ATM);
        h.assertTrue(Math.abs(atm - 760) < 0.5, "1 atm should read 760 mmHg, got " + atm);
        h.assertTrue(keyOf(MercuryVacuumGaugeItem.reading(1000)).endsWith(".reading"), "1 kPa (7.5 mm) is readable");
        h.assertTrue(keyOf(MercuryVacuumGaugeItem.reading(100)).endsWith(".below"), "100 Pa (0.75 mm) is below what the eye can read");
        floor(h, 5);
        BlockPos cp = p(1, 1, 1);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        ItemStack gauge = new ItemStack(Vacuum.MERCURY_VACUUM_GAUGE);
        java.util.function.Function<BlockPos, net.minecraft.world.InteractionResult> use = rel -> {
            BlockPos a = h.absolutePos(rel);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false);
            return gauge.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(h.getLevel(), null, net.minecraft.world.InteractionHand.MAIN_HAND, gauge, hit));
        };
        h.assertTrue(use.apply(cp) instanceof net.minecraft.world.InteractionResult.Success, "gauge on a chamber should succeed");
        h.assertTrue(use.apply(p(3, 0, 3)) == net.minecraft.world.InteractionResult.PASS, "gauge on stone should pass");
        LOG.info("[LAB] mercury: cinnabar -> dust -> furnace -> mercury; gauge 1 atm = {} mmHg, 1 kPa readable, 100 Pa below 1 mm", String.format("%.1f", atm));
        h.succeed();
    }

    // =====================================================================
    // [5-02] 真空管（封止台）
    // =====================================================================

    /** 容器が 0.1Pa 以下のときだけ封じる（手押しポンプの 1kPa では不可）。1本封じると容器に空気がもどり、続けては封じられない。容器なしでは動かない */
    @GameTest
    public void sealingStationNeedsVacuum(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "vacuum_tube_assembly", Metallurgy.TUNGSTEN_WIRE), "the filament is tungsten wire [1-28]");
        double back = SealingStationBlockEntity.decodePa(SealingStationBlockEntity.encodePa(1234));
        h.assertTrue(Math.abs(back - 1234) < 5, "pressure sync encoding round-trip, got " + back);
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), sp = p(2, 1, 1), lonely = p(5, 1, 5);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(sp, Vacuum.SEALING_STATION.block);
        h.setBlock(lonely, Vacuum.SEALING_STATION.block);
        VacuumChamberBlockEntity c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        SealingStationBlockEntity be = h.getBlockEntity(sp, SealingStationBlockEntity.class);
        var box = be.getContainer();
        int IN = SealingStationBlockEntity.SLOT_IN, OUT = SealingStationBlockEntity.SLOT_OUT, T = SealingStationBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(SealingStationBlockEntity.WATTS);
        box.setItem(IN, new ItemStack(Vacuum.VACUUM_TUBE_ASSEMBLY, 2));
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertTrue(box.getItem(OUT).isEmpty(), "at 1 atm nothing may be sealed");
        c.setPressure(HandVacuumPumpBlock.LIMIT_PA);
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertTrue(box.getItem(OUT).isEmpty(), "the hand pump's 1 kPa is not enough to seal a tube");
        c.setPressure(0.08);
        tickConsumer(h, sp, be, T, feed);
        h.assertTrue(box.getItem(OUT).is(Vacuum.VACUUM_TUBE) && box.getItem(OUT).getCount() == 1, "at 0.08 Pa one tube should be sealed in " + T + " ticks, got " + box.getItem(OUT));
        h.assertValueEqual(box.getItem(IN).getCount(), 1, "one assembly used");
        double after = c.getPressure();
        h.assertTrue(after > SealingStationBlockEntity.SEAL_MAX_PA, "sealing lets air back into the chamber: " + after);
        tickConsumer(h, sp, be, T + 20, feed);
        h.assertValueEqual(box.getItem(OUT).getCount(), 1, "no second tube until pumped down again");
        SealingStationBlockEntity alone = h.getBlockEntity(lonely, SealingStationBlockEntity.class);
        alone.getContainer().setItem(IN, new ItemStack(Vacuum.VACUUM_TUBE_ASSEMBLY));
        tickConsumer(h, lonely, alone, T + 20, feed);
        h.assertTrue(alone.getContainer().getItem(OUT).isEmpty(), "no chamber next to it -> no sealing");
        LOG.info("[LAB] sealing station: 1 atm / 1 kPa -> wait, 0.08 Pa -> tube in {} ticks, chamber back to {}, no chamber -> wait", T, VacuumChamberBlockEntity.formatPa(after));
        h.succeed();
    }

    /** [5-04] 回転ポンプ・[5-05] 水銀拡散ポンプ（IMP2-01）：回転ポンプだけ → 1Pa どまり。拡散ポンプは前段なし・10Pa より上では動かない。両方で 約0.004Pa（漏れとつり合う） */
    @GameTest
    public void rotaryAndDiffusionPumps(GameTestHelper h) {
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), dp = p(2, 1, 1), rp = p(3, 1, 1);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(dp, Vacuum.DIFFUSION_PUMP.block);
        var c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var d = h.getBlockEntity(dp, com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.class);
        long dFeed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.WATTS);
        long rFeed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.WATTS);
        // 拡散ポンプだけ（前段なし）：大気圧から引けない
        tickConsumer(h, dp, d, 400, dFeed);
        h.assertTrue(c.getPressure() >= VacuumChamberBlockEntity.ATM - 1, "diffusion pump alone cannot pump from 1 atm");
        h.assertValueEqual(d.status(), com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.STATUS_NO_BACKING, "status: needs backing");
        // 回転ポンプを拡散ポンプの向こうに：拡散ポンプ越しに容器を粗引き → 1Pa 近く
        h.setBlock(rp, Vacuum.ROTARY_PUMP.block);
        var r = h.getBlockEntity(rp, com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.class);
        h.assertTrue(r.target() == c, "rotary pump reaches the chamber through the diffusion pump (foreline)");
        tickConsumer(h, rp, r, 20 * 30, rFeed);
        h.assertTrue(c.getPressure() < 1.1, "rotary pump: 1 atm -> ~1 Pa in 30 s, got " + c.getPressure());
        double rough = c.getPressure();
        // 回転ポンプを回しながら拡散ポンプ：高真空へ
        for (int i = 0; i < 20 * 30; i++) {
            tickConsumer(h, rp, r, 1, rFeed);
            tickConsumer(h, dp, d, 1, dFeed);
            if (i % 20 == 0) c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND);   // 漏れ込み（容器の tick の代わり）
        }
        h.assertTrue(c.getPressure() < 0.01, "diffusion pump: below 0.01 Pa (evaporation range), got " + c.getPressure());
        h.assertTrue(c.getPressure() > com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.LIMIT_PA, "leak keeps it above the pump's own limit");
        double high = c.getPressure();
        // 圧力が高すぎると止まる
        c.setPressure(50);
        tickConsumer(h, rp, r, 1, rFeed);
        h.assertValueEqual(d.status(), com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.STATUS_TOO_HIGH, "above 10 Pa the jet breaks down");
        h.assertTrue(recipeUses(h, "rotary_pump", com.taku3.semiconductorlab.content.Precision.PUMP_ROTOR) && recipeUses(h, "rotary_pump", com.taku3.semiconductorlab.content.Metallurgy.MERCURY), "rotary pump = lathe-turned rotor + mercury seal");
        h.assertTrue(recipeUses(h, "diffusion_pump", com.taku3.semiconductorlab.content.Chemistry.SODA_LIME_GLASS) && recipeUses(h, "diffusion_pump", com.taku3.semiconductorlab.content.Metallurgy.MERCURY), "diffusion pump = glass + iron + mercury");
        LOG.info("[LAB] pumps [5-04][5-05]: rotary 1 atm -> {} Pa; + diffusion -> {} Pa", String.format("%.3f", rough), String.format("%.4f", high));
        h.succeed();
    }

    /** [5-14] 真空蒸着（IMP2-01）：クラウンガラス＋銀 → 半透明鏡。シュプレンゲル（0.05Pa）では足りず 0.01Pa 以下だけ。1回ごとに容器が少し大気にもどる */
    @GameTest
    public void vacuumEvaporation(GameTestHelper h) {
        var E = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.class;
        floor(h, 7);
        BlockPos cp = p(1, 1, 1), ep = p(2, 1, 1);
        h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
        h.setBlock(ep, Vacuum.EVAPORATOR.block);
        var c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
        var e = h.getBlockEntity(ep, E);
        var box = e.getContainer();
        int S = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_SUBSTRATE, M = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_METAL, O = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.SLOT_OUT;
        int T = com.taku3.semiconductorlab.block.EvaporatorBlockEntity.TICKS;
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.WATTS);
        box.setItem(S, new ItemStack(com.taku3.semiconductorlab.content.Optics.CROWN_GLASS, 2));
        box.setItem(M, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.SILVER_NUGGET, 2));
        c.setPressure(0.06);   // シュプレンゲルの到達あたり
        tickConsumer(h, ep, e, T + 20, feed);
        h.assertTrue(box.getItem(O).isEmpty(), "0.06 Pa (Sprengel) is not enough: atoms scatter");
        c.setPressure(0.004);  // 拡散ポンプ
        tickConsumer(h, ep, e, T, feed);
        h.assertTrue(box.getItem(O).is(com.taku3.semiconductorlab.content.Optics.HALF_SILVERED_MIRROR), "0.004 Pa -> half-silvered mirror");
        h.assertValueEqual(box.getItem(M).getCount(), 1, "1 silver nugget used");
        h.assertTrue(c.getPressure() > com.taku3.semiconductorlab.block.EvaporatorBlockEntity.MAX_PA, "opening the bell jar lets air back in");
        h.assertTrue(com.taku3.semiconductorlab.block.EvaporatorBlockEntity.recipe(new ItemStack(Items.GLASS), new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.SILVER_NUGGET)) == null, "vanilla glass is not an optical substrate");
        LOG.info("[LAB] evaporation [5-14]: crown glass + Ag at <= 0.01 Pa -> half-silvered mirror; 0.06 Pa rejected");
        h.succeed();
    }

    /** [5-04]③ シリコーン油（IMP4-01）：塩化メチル → 直接法 → 加水分解。触媒は減らない、触媒がちがうと動かない */
    @GameTest(maxTicks = 100)
    public void siliconeOil(GameTestHelper h) {
        floor(h, 5);
        BlockPos rp = p(2, 1, 2);
        h.setBlock(rp, Vacuum.SILICONE_REACTOR.block);
        var be = h.getBlockEntity(rp, com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.WATTS);
        int T = com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.TICKS;
        // ① メタノール＋乾いた HCl。触媒が銅だと動かない → アルミナで塩化メチル
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_A, new ItemStack(Chemistry.METHANOL, 2));
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_B, new ItemStack(Metallurgy.HYDROGEN_CHLORIDE_GAS, 2));
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_CAT, new ItemStack(Items.COPPER_INGOT));
        tickConsumer(h, rp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).isEmpty(), "methyl chloride needs alumina, not copper");
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_CAT, new ItemStack(Metallurgy.ALUMINA));
        tickConsumer(h, rp, be, 2 * T + 2, feed);
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).getCount(), 2, "2 methyl chloride");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_CAT).getCount(), 1, "catalyst kept");
        // ② Si + 2CH3Cl（銅、空き瓶）
        ItemStack mecl = c.removeItemNoUpdate(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT);
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_A, new ItemStack(Metallurgy.METALLURGICAL_SILICON));
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_B, mecl);
        tickConsumer(h, rp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).isEmpty(), "direct process needs copper");
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_CAT, new ItemStack(Items.COPPER_INGOT));
        tickConsumer(h, rp, be, T + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).isEmpty(), "no empty bottle -> nowhere to put the liquid");
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_BOTTLE, new ItemStack(Chemistry.GLASS_BOTTLE));
        tickConsumer(h, rp, be, T + 1, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).is(Vacuum.DIMETHYLDICHLOROSILANE) && c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_B).isEmpty() && c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_A).isEmpty(), "Si + 2 CH3Cl -> (CH3)2SiCl2");
        // ③ 蒸留水で加水分解 → シリコーン油＋塩酸
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_A, c.removeItemNoUpdate(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT));
        c.setItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_B, new ItemStack(Chemistry.DISTILLED_WATER));
        tickConsumer(h, rp, be, T + 1, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT).is(Vacuum.SILICONE_OIL) && c.getItem(com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.SLOT_OUT2).is(Gas.HYDROCHLORIC_ACID), "hydrolysis -> silicone oil + hydrochloric acid");
        h.assertTrue(recipeUses(h, "oil_diffusion_pump", Vacuum.SILICONE_OIL), "oil diffusion pump is filled with silicone oil");
        LOG.info("[LAB] silicone oil [5-04]-3: MeOH + HCl (alumina) -> CH3Cl; Si + 2 CH3Cl (Cu) -> Me2SiCl2; + H2O -> silicone oil + HCl");
        h.succeed();
    }

    /** [5-06] 油拡散ポンプ（IMP4-01）：水銀拡散ポンプと同じ条件で動き、同じ漏れでもっと低い圧力まで。回転ポンプの前段になれる・蒸気は戻る */
    @GameTest(maxTicks = 100)
    public void oilDiffusionPump(GameTestHelper h) {
        floor(h, 9);
        double[] reached = new double[2];
        for (int k = 0; k < 2; k++) {
            int z = 1 + k * 4;
            BlockPos cp = p(1, 1, z), dp = p(2, 1, z), rp = p(3, 1, z);
            h.setBlock(cp, Vacuum.VACUUM_CHAMBER.block);
            h.setBlock(dp, k == 0 ? Vacuum.DIFFUSION_PUMP.block : Vacuum.OIL_DIFFUSION_PUMP.block);
            h.setBlock(rp, Vacuum.ROTARY_PUMP.block);
            var c = h.getBlockEntity(cp, VacuumChamberBlockEntity.class);
            var d = h.getBlockEntity(dp, com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.class);
            var r = h.getBlockEntity(rp, com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.class);
            h.assertTrue(r.target() == c, "rotary pump backs the chamber through either diffusion pump");
            long dFeed = EnergyStorage.wattsToJoulesPerTick(k == 0 ? com.taku3.semiconductorlab.block.DiffusionPumpBlockEntity.WATTS : com.taku3.semiconductorlab.block.OilDiffusionPumpBlockEntity.WATTS), rFeed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.RotaryPumpBlockEntity.WATTS);
            c.setPressure(50);
            tickConsumer(h, rp, r, 1, rFeed);
            tickConsumer(h, dp, d, 40, dFeed);
            h.assertTrue(c.getPressure() > 10, "above 10 Pa the jet cannot pump (k=" + k + ")");
            c.setPressure(1);
            for (int i = 0; i < 20 * 30; i++) {
                tickConsumer(h, rp, r, 1, rFeed);
                tickConsumer(h, dp, d, 1, dFeed);
                if (i % 20 == 0) c.setPressure(c.getPressure() + VacuumChamberBlockEntity.LEAK_PA_PER_SECOND);
            }
            reached[k] = c.getPressure();
            if (k == 1) h.assertTrue(com.taku3.semiconductorlab.block.TurboPumpBlockEntity.backstreams(h.getLevel(), h.absolutePos(cp)), "oil vapour also backstreams");
        }
        h.assertTrue(reached[1] < reached[0] && reached[1] < 0.004, "oil pump goes lower with the same leak: Hg " + reached[0] + " Pa vs oil " + reached[1] + " Pa");
        LOG.info("[LAB] oil diffusion pump [5-06]: same backing rules; with the same leak Hg -> " + reached[0] + " Pa, oil -> " + reached[1] + " Pa; oil also backstreams");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    /** [5-03] 水銀柱（IMP4-01、S-009）：海面 Y63 で 760mmHg、高いほど低い、レシピ */
    @GameTest
    public void mercuryBarometer(GameTestHelper h) {
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.MercuryBarometerBlock.mmHg(63) - 760) < 1e-9, "sea level 760 mmHg");
        h.assertTrue(com.taku3.semiconductorlab.block.MercuryBarometerBlock.mmHg(200) < com.taku3.semiconductorlab.block.MercuryBarometerBlock.mmHg(63), "higher -> lower column");
        h.assertTrue(recipeUses(h, "mercury_barometer", Metallurgy.MERCURY), "made with mercury");
        h.succeed();
    }

    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Vacuum.SEALING_STATION.block, "真空管の封止", L(io(Vacuum.VACUUM_TUBE_ASSEMBLY, 1), kept(cond(io("隣の真空容器", 1), SealingStationBlockEntity.SEAL_MAX_PA + " Pa 以下"))),
            L(io(Vacuum.VACUUM_TUBE, 1)), SealingStationBlockEntity.WATTS, SealingStationBlockEntity.TICKS / 20.0, "封じるたびに容器へ空気がもどる");
        rec(a, Vacuum.SILICONE_REACTOR.block, "塩化メチル", L(io(Chemistry.METHANOL, 1), io(Metallurgy.HYDROGEN_CHLORIDE_GAS, 1), kept(io(Metallurgy.ALUMINA, 1))),
            L(io(Vacuum.METHYL_CHLORIDE, 1)), com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.TICKS / 20.0, "CH3OH + HCl → CH3Cl + H2O（水は捨てる）");
        rec(a, Vacuum.SILICONE_REACTOR.block, "直接法", L(io(Metallurgy.METALLURGICAL_SILICON, 1), io(Vacuum.METHYL_CHLORIDE, com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.MECL_PER_SI), kept(io(Items.COPPER_INGOT, 1)), io(Chemistry.GLASS_BOTTLE, 1)),
            L(io(Vacuum.DIMETHYLDICHLOROSILANE, 1)), com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.TICKS / 20.0, "Si + 2CH3Cl → (CH3)2SiCl2（ロチョウ 1941〜45）");
        rec(a, Vacuum.SILICONE_REACTOR.block, "加水分解", L(io(Vacuum.DIMETHYLDICHLOROSILANE, 1), io(Chemistry.DISTILLED_WATER, 1)),
            L(io(Vacuum.SILICONE_OIL, 1), io(Gas.HYDROCHLORIC_ACID, 1)), com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.WATTS, com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity.TICKS / 20.0, "(CH3)2SiCl2 + H2O → シリコーン油 + 2HCl");
        rec(a, Vacuum.OIL_DIFFUSION_PUMP.block, "高真空にする", L(kept(io("隣の真空容器（10Pa 以下）", 1)), kept(io(Vacuum.ROTARY_PUMP.block, 1))),
            L(cond(io("真空容器の圧力", 1), "1秒ごとに残り " + (int) (com.taku3.semiconductorlab.block.OilDiffusionPumpBlockEntity.RATIO * 100) + "%、" + com.taku3.semiconductorlab.block.OilDiffusionPumpBlockEntity.LIMIT_PA + " Pa まで")), com.taku3.semiconductorlab.block.OilDiffusionPumpBlockEntity.WATTS, 1, "前段に回転ポンプ。油の蒸気も少し戻る");
        rec(a, Vacuum.SPRENGEL_PUMP.block, "真空にする", L(kept(io(Metallurgy.MERCURY, SprengelPumpBlockEntity.MERCURY_NEEDED)), kept(io("隣の真空容器", 1))),
            L(cond(io("真空容器の圧力", 1), "1秒ごとに残り " + (int) (SprengelPumpBlockEntity.RATIO * 100) + "%、" + SprengelPumpBlockEntity.LIMIT_PA + " Pa まで")), 0, 1, "電気なし");
    }
}
