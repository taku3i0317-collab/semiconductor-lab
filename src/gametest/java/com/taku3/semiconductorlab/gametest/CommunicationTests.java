package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.content.Communication;
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
 * GameTest：幹15 通信・情報（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class CommunicationTests extends LabTestBase {
    /** [15-05] 蓄音機（IMP2 S-010）：錫はくの筒に録音した文を再生できる。3回でつぶれて聞こえなくなる */
    @GameTest
    public void phonograph(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "tin_foil_cylinder", com.taku3.semiconductorlab.content.Metallurgy.TIN_INGOT) && recipeUses(h, "phonograph", com.taku3.semiconductorlab.content.Precision.STANDARD_SCREW), "tin foil + feed screw");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack foil = new ItemStack(com.taku3.semiconductorlab.content.Communication.TIN_FOIL_CYLINDER);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.taku3.semiconductorlab.content.Communication.PHONOGRAPH));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, foil);
        com.taku3.semiconductorlab.item.PhonographItem.record(foil, "Mary had a little lamb");
        h.assertValueEqual(com.taku3.semiconductorlab.item.PhonographItem.recording(foil), "Mary had a little lamb", "recorded on tin foil");
        for (int i = 0; i < com.taku3.semiconductorlab.item.PhonographItem.PLAYS + 1; i++) com.taku3.semiconductorlab.content.Communication.PHONOGRAPH.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertValueEqual(com.taku3.semiconductorlab.item.PhonographItem.played(foil), com.taku3.semiconductorlab.item.PhonographItem.PLAYS, "the foil wears out after " + com.taku3.semiconductorlab.item.PhonographItem.PLAYS + " plays");
        LOG.info("[LAB] phonograph [15-05]: tin foil records a line and plays " + com.taku3.semiconductorlab.item.PhonographItem.PLAYS + " times before the groove is flattened");
        h.succeed();
    }

    /** [15-04] 電話（IMP2 S-010）：両手の2台を結ぶと同じ回線。回線のない電話・別の回線には届かない */
    @GameTest
    public void telephone(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "telephone", com.taku3.semiconductorlab.content.Control.ELECTROMAGNET) && recipeUses(h, "telephone", net.minecraft.world.item.Items.CHARCOAL), "receiver electromagnet + carbon microphone");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack a = new ItemStack(com.taku3.semiconductorlab.content.Communication.TELEPHONE), b = new ItemStack(com.taku3.semiconductorlab.content.Communication.TELEPHONE), c = new ItemStack(com.taku3.semiconductorlab.content.Communication.TELEPHONE);
        h.assertFalse(com.taku3.semiconductorlab.item.TelephoneItem.sameLine(a, b), "new phones are not connected");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, a);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, b);
        com.taku3.semiconductorlab.content.Communication.TELEPHONE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(com.taku3.semiconductorlab.item.TelephoneItem.sameLine(a, b) && !com.taku3.semiconductorlab.item.TelephoneItem.sameLine(a, c), "linked pair shares a line, a third phone does not");
        h.assertValueEqual(com.taku3.semiconductorlab.item.TelephoneItem.lineOf(player), com.taku3.semiconductorlab.item.TelephoneItem.line(a), "holding the phone puts you on its line");
        LOG.info("[LAB] telephone [15-04]: two phones paired on one line; chat while holding relays to the other");
        h.succeed();
    }

    /** [15-09] 真空管ラジオ（IMP2 S-010）：聞くたびに鉛蓄電池から 400J（ヒーター 40W×10秒）。電池が足りないと鳴らない */
    @GameTest
    public void tube_radio(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack radio = new ItemStack(com.taku3.semiconductorlab.content.Communication.TUBE_RADIO);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, radio);
        ItemStack bat = new ItemStack(com.taku3.semiconductorlab.content.Power.LEAD_ACID_BATTERY.block.asItem());
        bat.set(com.taku3.semiconductorlab.ModDataComponents.BATTERY_ENERGY, com.taku3.semiconductorlab.item.TubeRadioItem.LISTEN_J + 100);
        player.getInventory().setItem(9, bat);
        h.assertTrue(com.taku3.semiconductorlab.content.Communication.TUBE_RADIO.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND) == net.minecraft.world.InteractionResult.SUCCESS, "plays");
        h.assertValueEqual(bat.get(com.taku3.semiconductorlab.ModDataComponents.BATTERY_ENERGY), 100L, "10 s of 40 W heaters = 400 J");
        h.assertTrue(com.taku3.semiconductorlab.content.Communication.TUBE_RADIO.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND) == net.minecraft.world.InteractionResult.FAIL, "battery too low -> silent");
        LOG.info("[LAB] tube radio [15-09]: 3 triodes, 40 W heaters -> 400 J per 10 s listen");
        h.succeed();
    }

    /** [15-13] 光ファイバー（IMP2 O-033）：合成石英の母材は 20dB/km、天然の石英は 1000dB/km。100m 送ると 63% と 0.0000000001% */
    @GameTest
    public void optical_fiber(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "fiber_preform", com.taku3.semiconductorlab.content.Optics.SYNTHETIC_QUARTZ) && recipeUses(h, "fiber_preform", com.taku3.semiconductorlab.content.Metallurgy.GERMANIUM_DIOXIDE), "preform = synthetic silica + GeO2 core");
        ItemStack good = com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity.productOf(new ItemStack(com.taku3.semiconductorlab.content.Communication.FIBER_PREFORM));
        ItemStack bad = com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity.productOf(new ItemStack(com.taku3.semiconductorlab.content.Communication.FIBER_PREFORM_NATURAL));
        h.assertTrue(good.is(com.taku3.semiconductorlab.content.Communication.OPTICAL_FIBER) && good.getCount() == 8 && com.taku3.semiconductorlab.item.OpticalFiberItem.lossDbKm(good) == 20 && com.taku3.semiconductorlab.item.OpticalFiberItem.lossDbKm(bad) == 1000, "drawn in the flame: 8 fibres, 20 vs 1000 dB/km");
        double g = com.taku3.semiconductorlab.item.OpticalFiberItem.transmitted(20, 100), b = com.taku3.semiconductorlab.item.OpticalFiberItem.transmitted(1000, 100);
        h.assertTrue(Math.abs(g - 0.631) < 0.001 && b < 1e-9, "100 m: pure " + g + " vs impure " + b);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.OpticalFiberItem.criticalAngleDeg() - 85.3) < 0.2, "total internal reflection beyond " + com.taku3.semiconductorlab.item.OpticalFiberItem.criticalAngleDeg() + " deg");
        LOG.info("[LAB] optical fibre [15-13]: 100 m through synthetic silica " + Math.round(g * 100) + "%, through natural fused quartz ~0%; critical angle " + String.format(java.util.Locale.ROOT, "%.1f", com.taku3.semiconductorlab.item.OpticalFiberItem.criticalAngleDeg()) + " deg");
        h.succeed();
    }

    /** トランジスタラジオ [15-10]：合金接合型3＋炭素抵抗（黒鉛）＋コイル（銅ケーブル）＋コンデンサ（ライデン瓶）＋スピーカー（磁石）＋乾電池（亜鉛・黒鉛棒） */
    @GameTest
    public void transistorRadioParts(GameTestHelper h) {
        for (var it : new net.minecraft.world.item.Item[] {Semiconductor.JUNCTION_TRANSISTOR, Metallurgy.GRAPHITE, Power.COPPER_CABLE.block.asItem(), Power.LEYDEN_JAR, Power.MAGNET, Metallurgy.ZINC_INGOT, Power.GRAPHITE_ROD})
            h.assertTrue(assemblyCount(Communication.TRANSISTOR_RADIO, it) > 0, "radio part (assembly bench): " + it);
        h.assertValueEqual(assemblyCount(Communication.TRANSISTOR_RADIO, Semiconductor.JUNCTION_TRANSISTOR), 3, "3 transistors amplify");
        h.assertValueEqual(assemblyCount(Communication.TRANSISTOR_RADIO, Items.REDSTONE), 0, "no redstone in the radio");
        h.assertValueEqual(assemblyCount(Communication.TRANSISTOR_RADIO, Items.IRON_INGOT), 0, "the plain iron chassis was removed");
        h.succeed();
    }

    /** [15-01] 紙（パルプ）（IMP3、S-002）：SO2＋石灰＋水 → 蒸解液、原木2＋蒸解液 → 仕込み、加圧加熱釜で水あり → パルプ2、水なし → 仕込みのまま。パルプ3 → 紙3 */
    @GameTest(maxTicks = 2000)
    public void woodPulp(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sulfite_liquor", Chemistry.SULFUR_DIOXIDE) && recipeUses(h, "sulfite_liquor", Survival.SLAKED_LIME), "SO2 + lime -> cooking liquor");
        h.assertTrue(recipeUses(h, "pulp_charge", Items.OAK_LOG) && recipeUses(h, "pulp_charge", Communication.SULFITE_LIQUOR), "logs + liquor -> charge");
        h.assertTrue(recipeUses(h, "paper_from_wood_pulp", Communication.WOOD_PULP), "pulp -> paper");
        floor(h, 3);
        BlockPos ap = p(1, 1, 1);
        h.setBlock(ap, Chemistry.AUTOCLAVE.block);
        var ac = h.getBlockEntity(ap, com.taku3.semiconductorlab.block.AutoclaveBlockEntity.class);
        var c = ac.getContainer();
        long feed = com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, new ItemStack(Communication.PULP_CHARGE, 1));
        ac.setTemperatureForTest(200);
        tickConsumer(h, ap, ac, 20 * 30 + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).is(Communication.PULP_CHARGE), "no water (1 atm) -> not cooked, charge comes back");
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).copy());
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        ac.setTemperatureForTest(200);
        tickConsumer(h, ap, ac, 20 * 30 + 5, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Communication.WOOD_PULP) && out.getCount() == 2, "under pressure -> 2 wood pulp, got " + out);
        h.succeed();
    }
}
