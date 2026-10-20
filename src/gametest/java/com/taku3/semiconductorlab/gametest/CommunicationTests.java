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
