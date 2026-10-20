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
    // [0-04] 農耕（IMP3-01）：野生のヒトツブコムギから「穂が落ちない」種を選ぶ
    // =====================================================================

    /**
     * 草 → 野生のヒトツブコムギの種（約5%）。野生種は熟したまま放っておくと穂が落ちて芽に戻る（収穫なし）。
     * 熟したうちに刈ると約10%で栽培種の種。栽培種は熟しても穂が落ちず、刈ると麦（バニラの小麦）と種。
     */
    @GameTest
    public void einkornDomestication(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        var W = (com.taku3.semiconductorlab.block.EinkornCropBlock) Survival.WILD_EINKORN;
        var D = (com.taku3.semiconductorlab.block.EinkornCropBlock) Survival.EINKORN;
        floor(h, 3);
        BlockPos wild = p(1, 1, 1), dom = p(2, 1, 1);
        h.setBlock(wild.below(), Blocks.FARMLAND);
        h.setBlock(dom.below(), Blocks.FARMLAND);
        h.setBlock(wild, W.getStateForAge(W.getMaxAge()));
        h.setBlock(dom, D.getStateForAge(D.getMaxAge()));
        RandomSource rnd = RandomSource.create(4);

        // 野生種：熟したまま待つと穂が落ちる（芽に戻る）
        int ticks = 0;
        while (W.isMaxAge(h.getBlockState(wild)) && ticks < 200) { h.getBlockState(wild).randomTick(lv, h.absolutePos(wild), rnd); ticks++; }
        h.assertTrue(h.getBlockState(wild).is(W) && W.getAge(h.getBlockState(wild)) == 0, "ripe wild einkorn shatters back to a sprout (after " + ticks + " random ticks)");
        // 栽培種：いくら待っても穂は落ちない
        for (int i = 0; i < 200; i++) h.getBlockState(dom).randomTick(lv, h.absolutePos(dom), rnd);
        h.assertTrue(D.isMaxAge(h.getBlockState(dom)), "domesticated einkorn keeps its ears");

        // 刈り取り：野生種は種と、まれに栽培種の種。麦（小麦）は出ない
        int N = 1000, wildSeeds = 0, mutants = 0, wheat = 0;
        for (int i = 0; i < N; i++)
            for (ItemStack st : Block.getDrops(W.getStateForAge(W.getMaxAge()), lv, h.absolutePos(wild), null)) {
                if (st.is(W.asItem())) wildSeeds += st.getCount();
                if (st.is(D.asItem())) mutants += st.getCount();
                if (st.is(Items.WHEAT)) wheat += st.getCount();
            }
        h.assertValueEqual(wheat, 0, "wild einkorn gives no wheat");
        h.assertTrue(wildSeeds >= N, "ripe wild einkorn gives seeds: " + wildSeeds);
        h.assertTrue(mutants > N * 0.05 && mutants < N * 0.16, "non-shattering mutants ~10%: " + mutants + "/" + N);
        h.assertTrue(Block.getDrops(W.getStateForAge(3), lv, h.absolutePos(wild), null).stream().noneMatch(s -> s.is(D.asItem())), "unripe -> no mutant");
        // 栽培種は麦と種
        var domDrops = Block.getDrops(D.getStateForAge(D.getMaxAge()), lv, h.absolutePos(dom), null);
        h.assertTrue(domDrops.stream().anyMatch(s -> s.is(Items.WHEAT)) && domDrops.stream().anyMatch(s -> s.is(D.asItem())), "domesticated einkorn -> wheat + seeds");
        h.assertTrue(W.asItem() instanceof net.minecraft.world.item.BlockItem && D.asItem() instanceof net.minecraft.world.item.BlockItem, "seeds plant the crop");

        // 草むらから野生の麦（シャベルなどの素手扱い）
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        int found = 0, G = 4000;
        for (int i = 0; i < G; i++)
            for (ItemStack st : Block.getDrops(Blocks.SHORT_GRASS.defaultBlockState(), lv, h.absolutePos(wild), null, player, new ItemStack(Items.WOODEN_SHOVEL)))
                if (st.is(W.asItem())) found++;
        h.assertTrue(found > G * 0.02 && found < G * 0.08, "wild einkorn from grass ~4%: " + found + "/" + G);
        LOG.info("[LAB] einkorn [0-04]: wild shatters after {} random ticks, domesticated keeps ears; mutants {}/{}; grass -> wild {}/{}", ticks, mutants, N, found, G);
        h.succeed();
    }

    // =====================================================================
    // [0-06] 織物（IMP3-01）：亜麻 → 水に浸ける → 繊維 → 糸 → リネン
    // =====================================================================

    /**
     * 熟した亜麻 → 茎2＋種。水の中の茎だけが浸水した茎になる（陸の上はそのまま）。浸水した茎＋たたき石 → 繊維（浸けていない茎は不可）。
     * 繊維2 → 糸、糸4 → リネン。浸水は本当の tick（mixin）で確かめるため、テストの間だけ時間を 20tick に縮める
     */
    @GameTest(maxTicks = 120)
    public void flaxToLinen(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        var F = (net.minecraft.world.level.block.CropBlock) Survival.FLAX;
        var ripe = Block.getDrops(F.getStateForAge(F.getMaxAge()), lv, h.absolutePos(p(1, 1, 1)), null);
        h.assertTrue(ripe.stream().filter(s -> s.is(Survival.FLAX_STALKS)).mapToInt(ItemStack::getCount).sum() == 2, "ripe flax -> 2 stalks");
        h.assertTrue(ripe.stream().anyMatch(s -> s.is(F.asItem())), "ripe flax -> seeds");
        h.assertTrue(Block.getDrops(F.getStateForAge(2), lv, h.absolutePos(p(1, 1, 1)), null).stream().noneMatch(s -> s.is(Survival.FLAX_STALKS)), "unripe -> no stalks");

        // 作業台：浸水した茎だけが繊維になる
        var mgr = lv.getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        var retted = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Survival.RETTED_FLAX), new ItemStack(Survival.HAMMERSTONE)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, retted, lv).orElseThrow().value().assemble(retted).is(Survival.FLAX_FIBER), "retted flax + hammerstone -> fibre");
        var dry = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, List.of(new ItemStack(Survival.FLAX_STALKS), new ItemStack(Survival.HAMMERSTONE)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, dry, lv).isEmpty(), "unretted stalks give no fibre (pectin still glues it)");
        h.assertTrue(recipeUses(h, "linen_thread", Survival.FLAX_FIBER), "fibre -> thread");
        h.assertTrue(recipeUses(h, "linen_cloth", Survival.LINEN_THREAD), "thread -> linen");

        // 水に浸ける（本当の tick、mixin 経由）
        floor(h, 4);
        BlockPos water = p(1, 1, 1);
        for (int y = 1; y <= 3; y++)   // 水が流れ出ず、浮いた茎も外へ出ないよう、3段の壁で囲む
            for (BlockPos wall : new BlockPos[] {p(0, y, 1), p(2, y, 1), p(1, y, 0), p(1, y, 2)}) h.setBlock(wall, Blocks.STONE);
        h.setBlock(water, Blocks.WATER);
        // ブロックの中央に、勢いなしで置く（spawnItem(BlockPos) は角に出て壁にめり込み、押し出されることがある）
        var wet = h.spawnItem(Survival.FLAX_STALKS, new net.minecraft.world.phys.Vec3(1.5, 1.3, 1.5));
        var onLand = h.spawnItem(Survival.FLAX_STALKS, new net.minecraft.world.phys.Vec3(3.5, 1.1, 3.5));
        wet.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        onLand.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        com.taku3.semiconductorlab.item.Retting.ticksForRetting = 20;
        h.runAfterDelay(80, () -> {
            com.taku3.semiconductorlab.item.Retting.ticksForRetting = com.taku3.semiconductorlab.item.Retting.RETTING_TICKS;
            h.assertTrue(wet.getItem().is(Survival.RETTED_FLAX), "stalks in water -> retted (got " + wet.getItem() + ", wet=" + com.taku3.semiconductorlab.item.Retting.wet(wet) + ")");
            h.assertTrue(onLand.getItem().is(Survival.FLAX_STALKS), "stalks on land stay unretted");
            LOG.info("[LAB] flax [0-06]: ripe -> 2 stalks; in water -> retted, on land -> unchanged; retted + hammerstone -> fibre -> thread -> linen");
            h.succeed();
        });
    }

    // =====================================================================
    // [0-07] 羊毛と紡錘車（IMP3-01）：はずみ車の実験
    // =====================================================================

    /**
     * 棒だけの紡錘は 1回で糸1本、紡錘車つきは 4本（長く回る）。材料の比（繊維2 → 糸1）は同じ。繊維が足りなければその分だけ。
     * 羊毛ブロック → ほぐした羊毛4 → 紡錘で毛糸。右クリックで回し始める（繊維がなければ回らない）
     */
    @GameTest
    public void spindleWhorl(GameTestHelper h) {
        var shaft = (com.taku3.semiconductorlab.item.SpindleItem) Survival.SPINDLE_SHAFT;
        var spindle = (com.taku3.semiconductorlab.item.SpindleItem) Survival.SPINDLE;
        h.assertTrue(spindle.spinTicks() > shaft.spinTicks(), "a whorl keeps the spindle spinning longer");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var MAIN = net.minecraft.world.InteractionHand.MAIN_HAND;
        var OFF = net.minecraft.world.InteractionHand.OFF_HAND;

        // 棒だけ：1本
        player.setItemInHand(MAIN, new ItemStack(Survival.SPINDLE_SHAFT));
        player.setItemInHand(OFF, new ItemStack(Survival.FLAX_FIBER, 10));
        int a = com.taku3.semiconductorlab.item.SpindleItem.spin(player, MAIN, shaft.spinTicks());
        h.assertValueEqual(a, 1, "bare shaft: 1 thread per spin");
        h.assertValueEqual(player.getOffhandItem().getCount(), 8, "2 fibre per thread");
        // 紡錘車つき：4本
        player.setItemInHand(MAIN, new ItemStack(Survival.SPINDLE));
        int b = com.taku3.semiconductorlab.item.SpindleItem.spin(player, MAIN, spindle.spinTicks());
        h.assertValueEqual(b, 4, "with a whorl: 4 threads per spin");
        h.assertValueEqual(player.getOffhandItem().getCount(), 0, "2 fibre per thread (same ratio)");
        h.assertValueEqual(player.getInventory().countItem(Survival.LINEN_THREAD), 5, "linen thread in inventory");
        // 繊維が足りない・途中で離した
        player.setItemInHand(OFF, new ItemStack(Survival.FLAX_FIBER, 3));
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpindleItem.spin(player, MAIN, spindle.spinTicks()), 1, "only 3 fibre -> 1 thread");
        player.setItemInHand(OFF, new ItemStack(Survival.FLAX_FIBER, 10));
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpindleItem.spin(player, MAIN, 45), 2, "let go after 2.25 s -> 2 threads");
        // 羊毛
        player.setItemInHand(OFF, new ItemStack(Survival.CARDED_WOOL, 8));
        h.assertValueEqual(com.taku3.semiconductorlab.item.SpindleItem.spin(player, MAIN, spindle.spinTicks()), 4, "wool -> 4 yarn");
        h.assertValueEqual(player.getInventory().countItem(Survival.WOOLEN_YARN), 4, "woolen yarn");
        // 右クリック：繊維がなければ回らない、あれば回り始める
        player.setItemInHand(OFF, ItemStack.EMPTY);
        h.assertTrue(spindle.use(h.getLevel(), player, MAIN) == net.minecraft.world.InteractionResult.FAIL, "no fibre -> no spin");
        player.setItemInHand(OFF, new ItemStack(Survival.FLAX_FIBER, 4));
        spindle.use(h.getLevel(), player, MAIN);
        h.assertTrue(player.isUsingItem(), "fibre in the other hand -> the spindle starts spinning");

        // レシピ
        for (var wool : Items.WOOL.asList()) h.assertTrue(recipeUses(h, "carded_wool", wool), "any wool block -> carded wool: " + wool);  // 26.3：色つきの物は Items.WOOL（ColorCollection）
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Survival.RAW_CLAY_WHORL).is(Survival.EARTHENWARE_WHORL), "clay whorl fired on a campfire");
        h.assertTrue(recipeUses(h, "spindle", Survival.EARTHENWARE_WHORL) && recipeUses(h, "spindle", Survival.STONE_WHORL) && recipeUses(h, "spindle", Survival.SPINDLE_SHAFT), "shaft + clay or stone whorl -> spindle");
        h.assertFalse(recipeUses(h, "spindle", Survival.RAW_CLAY_WHORL), "unfired whorl cannot be used");
        h.assertTrue(recipeUses(h, "stone_whorl", Survival.HAMMERSTONE) && recipeUses(h, "stone_whorl", Items.FLINT), "stone whorl: hammerstone + flint drill");
        LOG.info("[LAB] spindle [0-07]: bare shaft {} ticks -> {} thread, whorl {} ticks -> {} threads (2 fibre each); wool -> yarn", shaft.spinTicks(), a, spindle.spinTicks(), b);
        h.succeed();
    }

    // =====================================================================
    // [0-18] ロープ（IMP3-01）
    // =====================================================================

    /** リネンの糸×3 → ロープ（毛糸では作らない） */
    @GameTest
    public void rope(GameTestHelper h) {
        var lv = h.getLevel();
        var mgr = lv.getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        var in = net.minecraft.world.item.crafting.CraftingInput.of(3, 1, List.of(new ItemStack(Survival.LINEN_THREAD), new ItemStack(Survival.LINEN_THREAD), new ItemStack(Survival.LINEN_THREAD)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, in, lv).orElseThrow().value().assemble(in).is(Survival.ROPE), "3 linen thread -> rope");
        var wool = net.minecraft.world.item.crafting.CraftingInput.of(3, 1, List.of(new ItemStack(Survival.WOOLEN_YARN), new ItemStack(Survival.WOOLEN_YARN), new ItemStack(Survival.WOOLEN_YARN)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, wool, lv).isEmpty(), "woolen yarn is not made into rope (stretchy)");
        LOG.info("[LAB] rope [0-18]: 3 linen thread -> rope");
        h.succeed();
    }

    // =====================================================================
    // [0-05] 豆（IMP3-01、O-003）：輪作の実験
    // =====================================================================

    /**
     * エンドウが熟すと下の耕地が窒素の多い耕地（3回分）。同じ回数のランダムtick で、窒素の多い耕地の麦はふつうの耕地の麦より育つ。
     * 麦が熟すたびに1回分使い、3回でふつうの耕地に戻る。豆が熟しても減らない
     */
    @GameTest
    public void peaRotation(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        var pea = (com.taku3.semiconductorlab.block.PeaCropBlock) Survival.PEA;
        var NF = Survival.NITROGEN_FARMLAND;
        var N = com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.NITROGEN;
        var wheat = (net.minecraft.world.level.block.CropBlock) Blocks.WHEAT;
        floor(h, 4);
        BlockPos withPea = p(1, 1, 1), noPea = p(3, 1, 1);
        h.setBlock(withPea, Blocks.FARMLAND);
        h.setBlock(noPea, Blocks.FARMLAND);
        // エンドウを熟させる → 下が窒素の多い耕地
        h.setBlock(withPea.above(), pea.getStateForAge(pea.getMaxAge()));
        pea.afterGrow(lv, h.absolutePos(withPea.above()));
        h.assertTrue(h.getBlockState(withPea).is(NF) && h.getBlockState(withPea).getValue(N) == 3, "ripe peas leave nitrogen (3 shares)");
        // 豆が熟していても減らない
        RandomSource rnd = RandomSource.create(5);
        for (int i = 0; i < 20; i++) h.getBlockState(withPea).randomTick(lv, h.absolutePos(withPea), rnd);
        h.assertValueEqual(h.getBlockState(withPea).getValue(N), 3, "peas do not use the nitrogen");
        h.assertTrue(wheat.defaultBlockState().canSurvive(lv, h.absolutePos(withPea.above())), "wheat can be planted on nitrogen farmland");
        for (Block b : new Block[] {Survival.PEA, Survival.WILD_EINKORN, Survival.EINKORN, Survival.FLAX})
            h.assertTrue(b.defaultBlockState().is(net.minecraft.tags.BlockTags.MAINTAINS_FARMLAND), b + " keeps the farmland from drying back to dirt");

        // 比べる：同じ回数だけ耕地のランダムtick（豆なしの耕地は作物を育てない）
        h.setBlock(withPea.above(), wheat.getStateForAge(0));
        h.setBlock(noPea.above(), wheat.getStateForAge(0));
        for (int i = 0; i < 6; i++) {
            h.getBlockState(withPea).randomTick(lv, h.absolutePos(withPea), rnd);
            h.getBlockState(noPea).randomTick(lv, h.absolutePos(noPea), rnd);
        }
        int aPea = wheat.getAge(h.getBlockState(withPea.above())), aNo = wheat.getAge(h.getBlockState(noPea.above()));
        h.assertTrue(aPea > aNo, "wheat after peas grows faster: " + aPea + " vs " + aNo);

        // 3回熟すと使い切って、ふつうの耕地に戻る
        int crops = 0;
        for (int round = 0; round < 3 && h.getBlockState(withPea).is(NF); round++) {
            h.setBlock(withPea.above(), wheat.getStateForAge(0));
            for (int i = 0; i < 200 && h.getBlockState(withPea).is(NF); i++) {
                h.getBlockState(withPea).randomTick(lv, h.absolutePos(withPea), rnd);
                if (h.getBlockState(withPea).is(NF) && h.getBlockState(withPea).getValue(com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.COUNTED)) break;
            }
            crops++;
        }
        h.assertTrue(h.getBlockState(withPea).is(Blocks.FARMLAND), "after 3 ripe crops the nitrogen is used up -> ordinary farmland (crops=" + crops + ")");
        h.assertValueEqual(crops, 3, "3 crops boosted");
        LOG.info("[LAB] pea rotation [0-05]: peas -> nitrogen farmland x3; wheat age after 6 ticks {} vs {} without peas; back to farmland after 3 crops", aPea, aNo);
        h.succeed();
    }

    // =====================================================================
    // [0-19] ろくろ・[0-09] 車輪（IMP3-01、O-003）
    // =====================================================================

    /** ろくろ：粘土玉3つで生の土器（手は5つ）。木の車輪：板材3＋磨製石斧（斧は戻るが耐久 −10、壊れかけなら戻らない） */
    @GameTest
    public void pottersWheelAndWheel(GameTestHelper h) {
        var lv = h.getLevel();
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack clay = new ItemStack(Items.CLAY_BALL, 5);
        h.assertTrue(com.taku3.semiconductorlab.block.PottersWheelBlock.throwPot(player, clay), "throw a pot on the wheel");
        h.assertValueEqual(clay.getCount(), 2, "3 clay used (5 by hand)");
        h.assertValueEqual(player.getInventory().countItem(Survival.RAW_CLAY_POT), 1, "unfired pot");
        h.assertFalse(com.taku3.semiconductorlab.block.PottersWheelBlock.throwPot(player, clay), "2 clay is not enough");
        h.assertTrue(recipeUses(h, "potters_wheel", Survival.SPINDLE_SHAFT) && recipeUses(h, "potters_wheel", Items.STONE), "wheel = stone disc (flywheel) + spindle shaft (axle)");

        var mgr = lv.getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        ItemStack axe = new ItemStack(Survival.POLISHED_STONE_AXE);
        var in = net.minecraft.world.item.crafting.CraftingInput.of(2, 2, List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.SPRUCE_PLANKS), axe));
        var r = mgr.getRecipeFor(CRAFT, in, lv).orElseThrow().value();
        h.assertTrue(r.assemble(in).is(Survival.WOODEN_WHEEL), "3 planks + polished stone axe -> wooden wheel");
        ItemStack back = r.getRemainingItems(in).stream().filter(s -> s.is(Survival.POLISHED_STONE_AXE)).findFirst().orElse(ItemStack.EMPTY);
        h.assertValueEqual(back.getDamageValue(), com.taku3.semiconductorlab.item.PolishedStoneAxeItem.CRAFT_WEAR, "the axe comes back slightly worn");
        axe.setDamageValue(axe.getMaxDamage() - 1);
        h.assertTrue(r.getRemainingItems(in).stream().noneMatch(s -> s.is(Survival.POLISHED_STONE_AXE)), "a nearly broken axe breaks");
        var noAxe = net.minecraft.world.item.crafting.CraftingInput.of(2, 2, List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Survival.HAND_AXE)));
        h.assertTrue(mgr.getRecipeFor(CRAFT, noAxe, lv).isEmpty(), "a knapped hand axe cannot shape a wheel");
        LOG.info("[LAB] potter's wheel [0-19]: 3 clay -> pot (5 by hand); wheel [0-09]: 3 planks + polished axe (-{} durability)", com.taku3.semiconductorlab.item.PolishedStoneAxeItem.CRAFT_WEAR);
        h.succeed();
    }

    // =====================================================================
    // [0-14] 水運（IMP3-01、O-004）：丸木舟と石材の荷
    // =====================================================================

    /** 石材の荷を持って歩くと遅い（荷の数で1〜4段）。丸木舟に乗ると遅くならない。丸木舟には荷物入れ（27マス）があり荷を積める */
    @GameTest
    public void dugoutCanoe(GameTestHelper h) {
        var lv = h.getLevel();
        var SLOW = net.minecraft.world.effect.MobEffects.SLOWNESS;
        var SPEED = net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED;
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        double free = player.getAttributeValue(SPEED);
        h.assertValueEqual(com.taku3.semiconductorlab.item.HeavyLoadItem.burden(player), 0, "no load -> not slowed");
        player.getInventory().add(new ItemStack(Survival.STONE_LOAD, 2));
        h.assertValueEqual(com.taku3.semiconductorlab.item.HeavyLoadItem.burden(player), 2, "2 loads -> slowness level 2");
        h.assertTrue(player.hasEffect(SLOW) && player.getEffect(SLOW).getAmplifier() == 1, "slowness II");
        player.getInventory().add(new ItemStack(Survival.STONE_LOAD, 5));
        h.assertValueEqual(com.taku3.semiconductorlab.item.HeavyLoadItem.burden(player), com.taku3.semiconductorlab.item.HeavyLoadItem.MAX_LEVEL, "capped at level 4");
        double loaded = player.getAttributeValue(SPEED);
        h.assertTrue(loaded < free, "walking speed drops with the load: " + loaded + " < " + free);

        // 丸木舟：置ける、荷物入れ、乗ると遅くならない
        floor(h, 4);
        var canoe = Survival.DUGOUT_CANOE_ENTITY.create(lv, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        h.assertTrue(canoe != null, "canoe entity");
        BlockPos at = h.absolutePos(p(1, 1, 1));
        canoe.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        lv.addFreshEntity(canoe);
        h.assertValueEqual(canoe.getContainerSize(), 27, "the hold has 27 slots");
        canoe.setItem(0, new ItemStack(Survival.STONE_LOAD, 16));
        h.assertTrue(canoe.getItem(0).is(Survival.STONE_LOAD), "stone loads go into the hold");
        player.removeEffect(SLOW);
        h.assertTrue(player.startRiding(canoe, true, false), "ride the canoe");
        h.assertValueEqual(com.taku3.semiconductorlab.item.HeavyLoadItem.burden(player), 0, "in the canoe the water carries the load -> not slowed");
        h.assertTrue(Survival.DUGOUT_CANOE instanceof net.minecraft.world.item.BoatItem, "the canoe item places a boat");
        h.assertTrue(recipeUses(h, "dugout_canoe", Survival.POLISHED_STONE_AXE), "dugout = logs hollowed with a polished stone axe");
        h.assertTrue(recipeUses(h, "stone_load", Survival.ROPE), "stone load is tied with rope");
        player.stopRiding();
        canoe.discard();
        LOG.info("[LAB] dugout canoe [0-14]: walking speed {} -> {} with loads; riding -> no slowness; hold 27 slots", free, loaded);
        h.succeed();
    }

    // =====================================================================
    // [0-17] 家畜化（IMP3-01、O-004）：世代を重ねる
    // =====================================================================

    /**
     * 繁殖で生まれた牛・羊・山羊の子に世代（親の大きい方＋1）が付く。本当の繁殖（spawnChildFromBreeding → mixin）でも付く。
     * 3世代目が家畜化。豚など対象外には付けない。進捗4つ（はじまり・牛・羊・山羊）が読み込まれている
     */
    @GameTest
    public void domestication(GameTestHelper h) {
        var lv = h.getLevel();
        floor(h, 5);
        var cowA = h.spawn(net.minecraft.world.entity.EntityTypes.COW, p(1, 1, 1));
        var cowB = h.spawn(net.minecraft.world.entity.EntityTypes.COW, p(2, 1, 1));
        h.assertValueEqual(com.taku3.semiconductorlab.entity.Domestication.generation(cowA), 0, "wild cow = generation 0");
        // 本当の繁殖（mixin 経由）
        int before = h.getEntities(net.minecraft.world.entity.EntityTypes.COW).size();
        cowA.spawnChildFromBreeding(lv, cowB);
        var cows = h.getEntities(net.minecraft.world.entity.EntityTypes.COW);
        h.assertValueEqual(cows.size(), before + 1, "a calf is born");
        var calf = cows.stream().filter(c -> c.isBaby()).findFirst().orElseThrow();
        h.assertValueEqual(com.taku3.semiconductorlab.entity.Domestication.generation(calf), 1, "calf of wild parents = generation 1 (tagged by the mixin)");
        // 世代を重ねる：親の大きい方 + 1
        cowA.addTag("semilab_gen_2");
        var calf2 = h.spawn(net.minecraft.world.entity.EntityTypes.COW, p(3, 1, 1));
        int g = com.taku3.semiconductorlab.entity.Domestication.onBred(lv, cowA, cowB, calf2, null);
        h.assertValueEqual(g, 3, "parents gen 2 and 0 -> gen 3");
        h.assertTrue(g >= com.taku3.semiconductorlab.entity.Domestication.GENERATIONS, "generation 3 = domesticated");
        var pig = h.spawn(net.minecraft.world.entity.EntityTypes.PIG, p(3, 1, 3));
        h.assertValueEqual(com.taku3.semiconductorlab.entity.Domestication.onBred(lv, cowA, cowB, pig, null), 0, "pigs are not part of [0-17]");
        h.assertValueEqual(com.taku3.semiconductorlab.entity.Domestication.species(net.minecraft.world.entity.EntityTypes.GOAT), "goat", "goats count");
        for (String a : new String[] {"root", "cow", "sheep", "goat", "cart"})   // cart＝[0-13] 荷車（決定：進捗のみ、車輪を作ると達成）
            h.assertTrue(lv.getServer().getAdvancements().get(SemiconductorLab.id("domestication/" + a)) != null, "advancement domestication/" + a);
        var ja = langJson("ja_jp");
        for (String egg : new String[] {"cow", "sheep", "goat"})
            for (String n : new String[] {"note1", "note2", "note3", "note4"})
                h.assertTrue(ja.has("item.minecraft." + egg + "_spawn_egg." + n), egg + " spawn egg " + n);
        LOG.info("[LAB] domestication [0-17]: wild x wild -> gen 1 (mixin), gen 2 parent -> gen 3 = domesticated; 4 advancements");
        h.succeed();
    }

    // =====================================================================
    // 試験場（S-001、IMP3）：関数 semiconductor-lab:testworld（tools/gen_testworld.py が作る）
    // =====================================================================

    /**
     * 試験場の関数が読み込まれ、最後まで動き、testworld_layout.json のとおりに置かれるか（機械・試験用の電源・チェスト・看板）。
     * ほかのテストとぶつからないよう、テストの場所の 100 ブロック上に建て、そのあいだチャンクを読み込んだままにする
     */
    @GameTest(maxTicks = 40)
    public void testworld(GameTestHelper h) {
        ServerLevel lv = h.getLevel();
        var server = lv.getServer();
        h.assertTrue(server.getFunctions().get(SemiconductorLab.id("testworld")).isPresent(), "function semiconductor-lab:testworld is loaded (no parse error)");
        com.google.gson.JsonObject layout;
        try (var in = SurvivalTests.class.getResourceAsStream("/testworld_layout.json")) {
            layout = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) { throw new RuntimeException("testworld_layout.json", e); }
        int w = layout.get("width").getAsInt(), d = layout.get("depth").getAsInt();
        BlockPos base = h.absolutePos(p(0, 0, 0)).above(100);
        List<long[]> forced = new ArrayList<>();
        for (int cx = (base.getX() - 2) >> 4; cx <= (base.getX() + w + 2) >> 4; cx++)
            for (int cz = (base.getZ() - 3) >> 4; cz <= (base.getZ() + d + 2) >> 4; cz++)
                if (lv.setChunkForced(cx, cz, true)) forced.add(new long[] {cx, cz});
        try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withLevel(lv)
                    .withPosition(net.minecraft.world.phys.Vec3.atLowerCornerOf(base)).withSuppressedOutput(), "function semiconductor-lab:testworld");
            int n = 0, power = 0, chests = 0, signs = 0;
            List<String> wrong = new ArrayList<>();
            for (var el : layout.getAsJsonArray("blocks")) {
                var o = el.getAsJsonObject();
                var pa = o.getAsJsonArray("pos");
                BlockPos at = base.offset(pa.get(0).getAsInt(), pa.get(1).getAsInt(), pa.get(2).getAsInt());
                String want = o.get("block").getAsString();
                String got = BuiltInRegistries.BLOCK.getKey(lv.getBlockState(at).getBlock()).toString();
                if (!got.equals(want)) wrong.add(pa + " " + want + " != " + got);
                if (want.equals("semiconductor-lab:test_power_source")) power++;
                if (want.equals("minecraft:chest") && lv.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity c && !c.isEmpty()) chests++;
                if (want.equals("minecraft:oak_sign") && lv.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sg
                        && !sg.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).getMessages(false).get(0).getString().isEmpty()) signs++;
                n++;
            }
            h.assertTrue(wrong.isEmpty(), "testworld blocks wrong (" + wrong.size() + "): " + wrong.subList(0, Math.min(5, wrong.size())));
            h.assertTrue(power > 0 && chests > 0 && signs > 0, "power " + power + ", filled chests " + chests + ", signs with text " + signs);
            LOG.info("[LAB] testworld (S-001): {} blocks as planned ({} test power sources, {} filled chests, {} signs), {}x{}", n, power, chests, signs, w, d);
        } finally {
            for (long[] c : forced) lv.setChunkForced((int) c[0], (int) c[1], false);
        }
        h.succeed();
    }

    // =====================================================================
    // [0-08] 木工（IMP3-01）：磨製石斧
    // =====================================================================

    /** ハンドアックス＋砂＋水（バケツは戻る）＋棒 → 磨製石斧。打ち欠いた斧より長もちで速い。砂なし・水なしでは研げない */
    @GameTest
    public void polishedStoneAxe(GameTestHelper h) {
        var mgr = h.getLevel().getServer().getRecipeManager();
        var CRAFT = net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        java.util.function.Function<List<ItemStack>, java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>>> find =
                l -> mgr.getRecipeFor(CRAFT, net.minecraft.world.item.crafting.CraftingInput.of(2, 2, l), h.getLevel());
        var full = List.of(new ItemStack(Survival.HAND_AXE), new ItemStack(Items.SAND), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.STICK));
        var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 2, full);
        var r = find.apply(full).orElseThrow().value();
        h.assertTrue(r.assemble(input).is(Survival.POLISHED_STONE_AXE), "hand axe + sand + water + stick -> polished stone axe");
        h.assertTrue(r.getRemainingItems(input).stream().anyMatch(s -> s.is(Items.BUCKET)), "the bucket comes back");
        h.assertTrue(find.apply(List.of(new ItemStack(Survival.HAND_AXE), ItemStack.EMPTY, new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.STICK))).isEmpty(), "no sand (abrasive) -> no polishing");
        h.assertTrue(find.apply(List.of(new ItemStack(Survival.HAND_AXE), new ItemStack(Items.SAND), ItemStack.EMPTY, new ItemStack(Items.STICK))).isEmpty(), "no water -> no polishing");
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        h.assertTrue(new ItemStack(Survival.POLISHED_STONE_AXE).getDestroySpeed(log) > new ItemStack(Survival.HAND_AXE).getDestroySpeed(log), "polished axe cuts faster than a knapped one");
        h.assertTrue(new ItemStack(Survival.POLISHED_STONE_AXE).getMaxDamage() > new ItemStack(Survival.HAND_AXE).getMaxDamage(), "polished edge does not chip as fast");
        LOG.info("[LAB] polished stone axe [0-08]: hand axe + sand + water + stick; speed {} vs hand axe {}", new ItemStack(Survival.POLISHED_STONE_AXE).getDestroySpeed(log), new ItemStack(Survival.HAND_AXE).getDestroySpeed(log));
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

    // =====================================================================
    // 組立台（IMP-33、DESIGN「組立台」）
    // =====================================================================

    /**
     * 組立台：役目のスロットにはその部品しか入らない／足りないと押しても何も起きない／そろえて押すと部品が減って完成品が出る。
     * 作業台から移した製品（ラジオ・高周波発振器）は作業台のレシピに残っていない（二重にならない）。全部の部品が登録済み。
     */
    @GameTest(maxTicks = 300)
    public void assemblyBench(GameTestHelper h) {
        var all = com.taku3.semiconductorlab.machine.Assembly.all();
        h.assertTrue(all.size() >= 2, "radio and HF oscillator are assembly recipes: " + all.size());
        for (String moved : new String[] {"transistor_radio", "hf_oscillator"})
            h.assertTrue(h.getLevel().getServer().getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, com.taku3.semiconductorlab.SemiconductorLab.id(moved))).isEmpty(),
                    "crafting recipe removed (assembly bench only): " + moved);
        for (var r : all) for (var pt : r.parts)
            h.assertTrue(pt.ok().test(new net.minecraft.world.item.ItemStack(pt.display())), r.id + "." + pt.role() + ": display item is a valid part");
        int radio = all.indexOf(com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Communication.TRANSISTOR_RADIO));
        var pos = p(1, 1, 1);
        h.setBlock(pos, Survival.ASSEMBLY_BENCH.block);
        var be = h.getBlockEntity(pos, com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.class);
        var c = be.getContainer();
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        while (be.getMenuData().get(com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.DATA_RECIPE) != radio) be.onButton(player, com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.BUTTON_NEXT);
        var a = be.current();
        // スロットの条件（画面と同じ判定）：増幅のスロットに磁石は入らない、トランジスタは入る。使わない8番目のスロットは隠れる
        var slots = com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.LAYOUT.slots();
        java.util.function.IntUnaryOperator data = be.getMenuData()::get;
        h.assertFalse(slots.get(0).accepts(data, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Power.MAGNET)), "amplify slot rejects a magnet");
        h.assertTrue(slots.get(0).accepts(data, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.JUNCTION_TRANSISTOR)), "amplify slot takes a transistor");
        h.assertFalse(slots.get(7).active(data), "unused 8th slot is hidden for the radio (7 parts)");
        // 1つ足りない（トランジスタ2個）：押しても始まらない
        for (int i = 0; i < a.parts.size(); i++) c.setItem(i, new net.minecraft.world.item.ItemStack(a.parts.get(i).display(), a.parts.get(i).count()));
        c.getItem(0).setCount(2);
        h.assertFalse(be.onButton(player, com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.BUTTON_ASSEMBLE), "2 of 3 transistors -> cannot assemble");
        c.getItem(0).setCount(3);
        h.assertTrue(be.onButton(player, com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.BUTTON_ASSEMBLE), "all parts -> assemble starts");
        h.runAfterDelay(a.ticks + 5, () -> {
            h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AssemblyBenchBlockEntity.SLOT_OUT).is(com.taku3.semiconductorlab.content.Communication.TRANSISTOR_RADIO), "radio comes out");
            for (int i = 0; i < a.parts.size(); i++) h.assertTrue(c.getItem(i).isEmpty(), "part used: " + a.parts.get(i).role());
            LOG.info("[LAB] assembly bench: {} recipes; radio from {} parts in {} ticks", all.size(), a.parts.size(), a.ticks);
            h.succeed();
        });
    }

    static void machineRecipes(com.google.gson.JsonArray a) {
        // 組立台（IMP-33）：レシピごとに1行。部品の cond に役目
        for (var r : com.taku3.semiconductorlab.machine.Assembly.all()) {
            var in = new com.google.gson.JsonArray();
            for (var pt : r.parts) in.add(cond(io(pt.display(), pt.count()), pt.role()));
            rec(a, Survival.ASSEMBLY_BENCH.block, "組み立て（" + r.id + "）", in, L(io(r.result, 1)), 0, r.ticks / 20.0, "部品表で組み立てる。電気なし");
        }
        rec(a, Survival.SALT_PAN.block, "天日で塩", L(kept(cond(io("海辺の水と日光", 1), "雨の日は止まる"))), L(io(Gas.SALT, 1), perRuns(io(Survival.BITTERN, 1), SaltPanBlockEntity.SALT_PER_BITTERN)),
            0, SaltPanBlockEntity.TICKS_PER_SALT / 20.0, "電気なし");
    }
}
