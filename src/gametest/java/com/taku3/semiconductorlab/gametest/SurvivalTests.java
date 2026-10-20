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
 * GameTest：幹0 生存・道具（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class SurvivalTests extends LabTestBase {
    // =====================================================================
    // [0-01] 打製石器（IMP3-01）：たたき石で打ち欠く
    // =====================================================================

    /**
     * 実験：たたき石で火打ち石（手）→ 剥片2、黒曜石ブロック → 黒曜石の剥片（ブロックは残る）、粒の粗い石 → 何も出ない。
     * 軽：作業台で剥片・ハンドアックス（たたき石は戻る）。剥片は小さな刃物、ハンドアックスは柄のない斧。砂利からたたき石が出る
     */
    @GameTest
    public void knapping(GameTestHelper h) {
        // 石の種類：ガラス質だけが刃になる
        h.assertTrue(com.taku3.semiconductorlab.item.HammerstoneItem.classify(Blocks.OBSIDIAN.defaultBlockState()) == com.taku3.semiconductorlab.item.HammerstoneItem.Knap.GLASSY, "obsidian is glassy");
        for (Block b : new Block[] {Blocks.STONE, Blocks.COBBLESTONE, Blocks.GRANITE, Blocks.ANDESITE, Blocks.SANDSTONE})
            h.assertTrue(com.taku3.semiconductorlab.item.HammerstoneItem.classify(b.defaultBlockState()) == com.taku3.semiconductorlab.item.HammerstoneItem.Knap.GRAINY, b + " is coarse-grained");
        h.assertTrue(com.taku3.semiconductorlab.item.HammerstoneItem.classify(Blocks.DIRT.defaultBlockState()) == com.taku3.semiconductorlab.item.HammerstoneItem.Knap.NONE, "dirt is not stone");

        // 手で打ち欠く：火打ち石1 → 剥片2
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        player.setItemInHand(hand, new ItemStack(Survival.HAMMERSTONE));
        h.assertFalse(com.taku3.semiconductorlab.item.HammerstoneItem.knapFlint(player, hand), "no flint in the other hand -> nothing");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.FLINT, 3));
        h.assertTrue(com.taku3.semiconductorlab.item.HammerstoneItem.knapFlint(player, hand), "knap flint");
        h.assertValueEqual(player.getOffhandItem().getCount(), 2, "1 flint used");
        h.assertValueEqual(player.getInventory().countItem(Survival.FLINT_FLAKE), com.taku3.semiconductorlab.item.HammerstoneItem.FLAKES_PER_FLINT, "2 flakes");
        h.assertTrue(player.getMainHandItem().is(Survival.HAMMERSTONE), "hammerstone stays");

        // ブロックを打つ：黒曜石 → 剥片（ブロックは残る）、丸石 → 何も出ない
        floor(h, 3);
        BlockPos obs = p(1, 1, 1), cob = p(2, 1, 1);
        h.setBlock(obs, Blocks.OBSIDIAN);
        h.setBlock(cob, Blocks.COBBLESTONE);
        var hs = player.getMainHandItem();
        java.util.function.Function<BlockPos, net.minecraft.world.InteractionResult> hit = rel -> {
            BlockPos a = h.absolutePos(rel);
            var ctx = new net.minecraft.world.item.context.UseOnContext(player, hand,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false));
            return hs.getItem().useOn(ctx);
        };
        hit.apply(cob);
        h.assertValueEqual(player.getInventory().countItem(Survival.OBSIDIAN_FLAKE), 0, "cobblestone gives no flake");
        h.assertTrue(h.getBlockState(cob).is(Blocks.COBBLESTONE), "cobblestone stays");
        hit.apply(obs);
        h.assertValueEqual(player.getInventory().countItem(Survival.OBSIDIAN_FLAKE), 1, "obsidian -> 1 flake");
        h.assertTrue(h.getBlockState(obs).is(Blocks.OBSIDIAN), "obsidian block stays");
        hit.apply(obs);
        h.assertValueEqual(player.getInventory().countItem(Survival.OBSIDIAN_FLAKE), 1, "cooldown: no second flake at once");

        // 作業台：たたき石は戻る
        h.assertTrue(recipeUses(h, "flint_flake", Items.FLINT) && recipeUses(h, "flint_flake", Survival.HAMMERSTONE), "flint + hammerstone -> flakes");
        h.assertTrue(recipeUses(h, "hand_axe", Items.FLINT) && recipeUses(h, "hand_axe", Survival.HAMMERSTONE), "3 flint + hammerstone -> hand axe");
        h.assertFalse(recipeUses(h, "hand_axe", Items.STICK), "a hand axe has no handle");
        h.assertTrue(Survival.HAMMERSTONE.getCraftingRemainder(new ItemStack(Survival.HAMMERSTONE)) != null, "hammerstone returns");

        // 道具として：剥片＝刃物、ハンドアックス＝柄のない斧（木の斧より少し速い）。火打ち石・黒曜石で直せる
        h.assertValueEqual(new ItemStack(Survival.FLINT_FLAKE).getMaxDamage(), 32, "flint flake durability");
        h.assertTrue(new ItemStack(Survival.OBSIDIAN_FLAKE).getMaxDamage() < new ItemStack(Survival.FLINT_FLAKE).getMaxDamage(), "obsidian is brittle");
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        float axe = new ItemStack(Survival.HAND_AXE).getDestroySpeed(log);
        h.assertTrue(axe > new ItemStack(Items.WOODEN_AXE).getDestroySpeed(log) && axe < new ItemStack(Items.STONE_AXE).getDestroySpeed(log), "hand axe between wooden and stone axe: " + axe);
        h.assertTrue(new ItemStack(Items.FLINT).is(Survival.KNAPPING_STONES) && new ItemStack(Items.OBSIDIAN).is(Survival.KNAPPING_STONES), "repair with flint / obsidian");

        // 砂利からたたき石（火打ち石が出なかったときの 10%）
        ServerLevel lv = h.getLevel();
        int stones = 0, N = 1000;
        for (int i = 0; i < N; i++)
            for (ItemStack st : Block.getDrops(Blocks.GRAVEL.defaultBlockState(), lv, h.absolutePos(obs), null, player, new ItemStack(Items.WOODEN_SHOVEL)))
                if (st.is(Survival.HAMMERSTONE)) stones++;
        h.assertTrue(stones > N * 0.03 && stones < N * 0.2, "hammerstones from gravel ~9%: " + stones + "/" + N);

        var ja = langJson("ja_jp");
        for (String id : new String[] {"hammerstone", "flint_flake", "obsidian_flake", "hand_axe"})
            h.assertTrue(ja.has("item.semiconductor-lab." + id + ".placeholder"), id + " should state what is omitted");
        LOG.info("[LAB] knapping [0-01]: flint -> 2 flakes, obsidian -> flake, cobble -> nothing; hand axe speed {}; gravel -> hammerstone {}/{}", axe, stones, N);
        h.succeed();
    }

    // =====================================================================
    // [0-03] 土器（IMP3-01）：焼く前は水で戻る、焼いたら戻らない
    // =====================================================================

    /** 粘土玉5 → 生の土器 → たき火（野焼き）/かまど → 土器。生の土器＋水 → 粘土玉5（バケツは戻る）、土器＋水 → 何もできない。塩田の釜にも使える */
    @GameTest
    public void pottery(GameTestHelper h) {
        var mgr = h.getLevel().getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        h.assertTrue(recipeUses(h, "raw_clay_pot", Items.CLAY_BALL), "clay balls -> unfired pot");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Survival.RAW_CLAY_POT).is(Survival.EARTHENWARE_POT), "open firing on a campfire");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Survival.RAW_CLAY_POT).is(Survival.EARTHENWARE_POT), "kiln (furnace)");
        // 焼く前：水で粘土に戻る
        var raw = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Survival.RAW_CLAY_POT), new ItemStack(Items.WATER_BUCKET)));
        var r = mgr.getRecipeFor(CRAFT, raw, h.getLevel()).orElseThrow().value();
        ItemStack back = r.assemble(raw);
        h.assertTrue(back.is(Items.CLAY_BALL) && back.getCount() == 5, "unfired pot + water -> 5 clay balls");
        h.assertTrue(r.getRemainingItems(raw).stream().anyMatch(s -> s.is(Items.BUCKET)), "the bucket comes back");
        // 焼いた後：もう戻らない（不可逆な脱水 Al2Si2O5(OH)4 → Al2Si2O7 + 2H2O）
        var fired = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Survival.EARTHENWARE_POT), new ItemStack(Items.WATER_BUCKET)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, fired, h.getLevel()).isEmpty(), "fired earthenware + water -> nothing");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Survival.EARTHENWARE_POT).isEmpty(), "firing again changes nothing");
        // 使い道：塩田の煮つめ釜
        h.assertTrue(recipeUses(h, "salt_pan_earthenware", Survival.EARTHENWARE_POT), "salt pan with an earthenware pot");
        h.assertTrue(recipeUses(h, "salt_pan", Items.CAULDRON), "salt pan with a cauldron still works");
        LOG.info("[LAB] pottery [0-03]: 5 clay -> unfired pot (+water -> 5 clay) -> campfire/furnace -> earthenware (+water -> nothing) -> salt pan");
        h.succeed();
    }

    // =====================================================================
    // [0-10] 生石灰・[0-11] 消石灰
    // =====================================================================

    /** 方解石をかまど/溶鉱炉で焼くと生石灰（CaCO3 → CaO + CO2）。生石灰＋水 → 消石灰。石や凝灰岩は焼いても石灰にならない */
    @GameTest
    public void limeFromCalcite(GameTestHelper h) {
        var SMELT = net.minecraft.world.item.crafting.RecipeType.SMELTING;
        var BLAST = net.minecraft.world.item.crafting.RecipeType.BLASTING;
        h.assertTrue(cook(h, SMELT, Items.CALCITE).is(Survival.QUICKLIME), "furnace: calcite -> quicklime");
        h.assertTrue(cook(h, BLAST, Items.CALCITE).is(Survival.QUICKLIME), "blast furnace: calcite -> quicklime");
        h.assertFalse(cook(h, SMELT, Items.TUFF).is(Survival.QUICKLIME), "tuff is not CaCO3");
        h.assertFalse(cook(h, SMELT, Items.STONE).is(Survival.QUICKLIME), "stone is not CaCO3");
        h.assertTrue(recipeUses(h, "slaked_lime", Survival.QUICKLIME) && recipeUses(h, "slaked_lime", Items.WATER_BUCKET), "CaO + H2O -> Ca(OH)2");
        LOG.info("[LAB] lime: calcite -> quicklime (furnace / blast), quicklime + water -> slaked lime -> brick binder");
        h.succeed();
    }

    // =====================================================================
    // [0-12] 木炭（タスク15）：バニラの木炭＝乾留した木の炭
    // =====================================================================

    /** 原木→かまど→木炭（バニラ）。MOD の還元（GeO2）とリンづくりが木炭を炭素として使う。木炭には へえ と省略の注記がある */
    @GameTest
    public void charcoal(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Items.OAK_LOG).is(Items.CHARCOAL), "log -> furnace -> charcoal (dry distillation)");
        h.assertTrue(recipeUses(h, "raw_germanium", Items.CHARCOAL), "GeO2 + C -> Ge uses charcoal as the reductant");
        h.assertTrue(recipeUses(h, "phosphorus", Items.CHARCOAL), "Wohler's phosphorus uses charcoal");
        var ja = langJson("ja_jp");
        for (String s : new String[] {"note1", "note2", "note3", "note4", "placeholder"})
            h.assertTrue(ja.has("item.minecraft.charcoal." + s), "charcoal should have " + s);
        LOG.info("[LAB] charcoal [0-12]: log -> furnace -> charcoal; used by GeO2 reduction and phosphorus; notes present");
        h.succeed();
    }

    /** [0-16] 塩田：海・浜バイオーム＋横に水＋空＋晴れで、1分に塩1、塩8ごとににがり1。右クリックで受け取る */
    @GameTest
    public void saltPan(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        floor(h, 4);
        BlockPos pos = p(1, 1, 1), water = p(2, 1, 1);
        h.setBlock(pos, Survival.SALT_PAN.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.SaltPanBlockEntity.class);
        BlockPos a = h.absolutePos(pos);
        int T = com.taku3.semiconductorlab.block.SaltPanBlockEntity.TICKS_PER_SALT;
        boolean sea = com.taku3.semiconductorlab.block.SaltPanBlockEntity.isSeaBiome(lv, a);
        LOG.info("[LAB] salt pan: test biome = {} (sea={})", lv.getBiome(a).unwrapKey().map(k -> k.identifier().toString()).orElse("?"), sea);
        h.setBlock(water, Blocks.WATER);
        if (!sea) {
            // 海でなければ（平原など）水があっても動かない
            be.step(lv, a, T * 2);
            h.assertValueEqual(be.getSalt(), 0, "not by the sea -> no salt");
            h.assertTrue(be.status(lv, a) == com.taku3.semiconductorlab.block.SaltPanBlockEntity.Status.NOT_SEA, "status NOT_SEA");
        }
        be.setForceSeaForTest(true);
        h.assertTrue(be.status(lv, a) != com.taku3.semiconductorlab.block.SaltPanBlockEntity.Status.NOT_SEA, "forced sea");
        if (lv.isRainingAt(a.above())) { LOG.info("[LAB] salt pan: raining in the test world, skip the sunny part"); h.succeed(); return; }
        // 水なし：止まる
        h.setBlock(water, Blocks.STONE);
        be.step(lv, a, T * 2);
        h.assertValueEqual(be.getSalt(), 0, "no seawater beside -> no salt");
        // 屋根：止まる
        h.setBlock(water, Blocks.WATER);
        h.setBlock(p(1, 2, 1), Blocks.STONE);
        be.step(lv, a, T * 2);
        h.assertValueEqual(be.getSalt(), 0, "no sky -> no salt");
        h.setBlock(p(1, 2, 1), Blocks.AIR);
        // 晴れ・空・水：1分で1個
        be.step(lv, a, T - 20);
        h.assertValueEqual(be.getSalt(), 0, "not yet 1 s before a minute");
        be.step(lv, a, 20);
        h.assertValueEqual(be.getSalt(), 1, "1 salt per minute");
        h.assertValueEqual(be.getBittern(), 0, "no bittern yet");
        be.step(lv, a, T * 7);
        h.assertValueEqual(be.getSalt(), 8, "8 salt");
        h.assertValueEqual(be.getBittern(), 1, "1 bittern per 8 salt (NaCl crystallises first, MgCl2 stays)");
        // 右クリック相当：プレイヤーに渡る
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int n = be.collect(player);
        h.assertValueEqual(n, 9, "collected 8 salt + 1 bittern");
        h.assertValueEqual(player.getInventory().countItem(Gas.SALT), 8, "salt in inventory");
        h.assertValueEqual(player.getInventory().countItem(Survival.BITTERN), 1, "bittern in inventory");
        h.assertValueEqual(be.getSalt(), 0, "pan emptied");
        // 満杯で止まる（塩64）
        be.step(lv, a, T * 80);
        h.assertValueEqual(be.getSalt(), com.taku3.semiconductorlab.block.SaltPanBlockEntity.MAX_SALT, "stops at 64 salt");
        h.assertTrue(recipeUses(h, "salt_pan", Items.CAULDRON), "salt pan needs an iron pot (cauldron) for boiling down");
        LOG.info("[LAB] salt pan: sea + water + sky + no rain -> 1 salt / {} ticks, 1 bittern / 8 salt, stops at 64", T);
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める。機械を足したらここに rec を1つ） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Survival.SALT_PAN.block, "天日で塩", L(kept(cond(io("海辺の水と日光", 1), "雨の日は止まる"))), L(io(Gas.SALT, 1), perRuns(io(Survival.BITTERN, 1), SaltPanBlockEntity.SALT_PER_BITTERN)),
            0, SaltPanBlockEntity.TICKS_PER_SALT / 20.0, "電気なし");
    }
}
