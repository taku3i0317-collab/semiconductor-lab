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
        ItemStack hg = cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Metallurgy.CINNABAR_DUST);
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

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Vacuum.SEALING_STATION.block, "真空管の封止", L(io(Vacuum.VACUUM_TUBE_ASSEMBLY, 1), kept(cond(io("隣の真空容器", 1), SealingStationBlockEntity.SEAL_MAX_PA + " Pa 以下"))),
            L(io(Vacuum.VACUUM_TUBE, 1)), SealingStationBlockEntity.WATTS, SealingStationBlockEntity.TICKS / 20.0, "封じるたびに容器へ空気がもどる");
        rec(a, Vacuum.SPRENGEL_PUMP.block, "真空にする", L(kept(io(Metallurgy.MERCURY, SprengelPumpBlockEntity.MERCURY_NEEDED)), kept(io("隣の真空容器", 1))),
            L(cond(io("真空容器の圧力", 1), "1秒ごとに残り " + (int) (SprengelPumpBlockEntity.RATIO * 100) + "%、" + SprengelPumpBlockEntity.LIMIT_PA + " Pa まで")), 0, 1, "電気なし");
    }
}
