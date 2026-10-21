package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.content.Agriculture;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * GameTest：幹11 農業・食品（IMP2 S-003 で作成）。共通の道具は {@link LabTestBase}。
 */
public class AgricultureTests extends LabTestBase {
    /** [11-13] 冷凍機（IMP2 S-003）：水バケツ → 氷（バケツは戻る）。冷媒のアンモニアは減らない。冷媒がないと動かない */
    @GameTest
    public void refrigerator(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "refrigerator", Gas.COMPRESSOR.block.asItem()), "refrigerator = compressor + copper condenser + iron");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Agriculture.REFRIGERATOR.block);
        var be = h.getBlockEntity(at, RefrigeratorBlockEntity.class);
        var c = be.getContainer();
        long full = EnergyStorage.wattsToJoulesPerTick(RefrigeratorBlockEntity.WATTS);
        c.setItem(RefrigeratorBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickConsumer(h, at, be, RefrigeratorBlockEntity.TICKS + 5, full);
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_ICE).isEmpty(), "no ammonia refrigerant -> nothing");
        c.setItem(RefrigeratorBlockEntity.SLOT_REFRIGERANT, new ItemStack(Chemistry.AMMONIA_WATER));
        tickConsumer(h, at, be, RefrigeratorBlockEntity.TICKS + 5, full);
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_ICE).is(Items.ICE) && c.getItem(RefrigeratorBlockEntity.SLOT_BUCKET).is(Items.BUCKET), "water bucket -> ice + bucket");
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_REFRIGERANT).is(Chemistry.AMMONIA_WATER), "the refrigerant circulates and is not used up");
        LOG.info("[LAB] refrigerator [11-13]: ammonia compression, water bucket -> ice in 20 s at 1 kW, refrigerant kept");
        h.succeed();
    }

    /** [11-10] 酢・[11-15] 低温殺菌（IMP3、S-005）：酒をかめに入れて3段 → 酢2。たき火で温めた酒はかめに入らない（酢にならない） */
    @GameTest
    public void vinegarPasteurization(GameTestHelper h) {
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Chemistry.FERMENTED_WASH).is(Agriculture.PASTEURIZED_WINE), "gently heated -> pasteurized");
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, Chemistry.FERMENTATION_CROCK);
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        h.assertFalse(com.taku3.semiconductorlab.block.FermentationCrockBlock.fill(level, pos, player, new ItemStack(Agriculture.PASTEURIZED_WINE)), "pasteurized wine does not sour");
        h.assertTrue(com.taku3.semiconductorlab.block.FermentationCrockBlock.fill(level, pos, player, new ItemStack(Chemistry.FERMENTED_WASH)), "wine in the open crock");
        for (int i = 0; i < 3; i++) com.taku3.semiconductorlab.block.FermentationCrockBlock.ferment(level, pos);
        h.assertTrue(com.taku3.semiconductorlab.block.FermentationCrockBlock.collect(level, pos, player), "done");
        h.assertTrue(player.getInventory().countItem(Agriculture.VINEGAR) == 2 && player.getInventory().countItem(Chemistry.WILD_YEAST) == 0, "acetic acid bacteria -> 2 vinegar");
        h.assertTrue(recipeUses(h, "sodium_acetate_from_vinegar", Agriculture.VINEGAR), "vinegar -> sodium acetate");
        h.succeed();
    }

    /** [11-14] 砂糖（IMP3、S-005）：サトウキビ・テンサイ＋石灰＋水 → 汁 → たき火 → 黒糖2、骨 → かまど → 骨炭、黒糖2＋骨炭＋水 → 白い砂糖2 */
    @GameTest
    public void sugarRefining(GameTestHelper h) {
        var FIRE = net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING;
        h.assertTrue(recipeUses(h, "cane_juice", net.minecraft.world.item.Items.SUGAR_CANE) && recipeUses(h, "beet_juice", net.minecraft.world.item.Items.BEETROOT), "cane and beet");
        ItemStack raw = cook(h, FIRE, Agriculture.CANE_JUICE);
        h.assertTrue(raw.is(Agriculture.RAW_SUGAR) && raw.getCount() == 2 && cook(h, FIRE, Agriculture.BEET_JUICE).is(Agriculture.RAW_SUGAR), "boiled down -> raw sugar");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, net.minecraft.world.item.Items.BONE).is(Agriculture.BONE_CHAR), "bone -> bone char");
        h.assertTrue(recipeUses(h, "white_sugar", Agriculture.RAW_SUGAR) && recipeUses(h, "white_sugar", Agriculture.BONE_CHAR), "bone char decolourizes -> white sugar");
        h.succeed();
    }

    /** [11-12] 缶詰・[11-16] ボルドー液（IMP3、S-006）：鉄＋スズ → ブリキ缶、缶＋肉 → 封をした缶 → 加熱 → 缶詰（焼いた牛肉より少し満腹）。硫酸銅＋消石灰 → ボルドー液 */
    @GameTest
    public void cannedFoodBordeaux(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "tin_can", com.taku3.semiconductorlab.content.Metallurgy.TIN_INGOT) && recipeUses(h, "tin_can", net.minecraft.world.item.Items.IRON_INGOT), "iron + tin -> tinplate can");
        h.assertTrue(recipeUses(h, "sealed_can", Agriculture.TIN_CAN) && recipeUses(h, "sealed_can", net.minecraft.world.item.Items.COOKED_BEEF), "can + cooked meat -> sealed can");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Agriculture.SEALED_CAN).is(Agriculture.CANNED_MEAT), "heat the sealed can -> canned food");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Agriculture.SEALED_CAN).is(Agriculture.CANNED_MEAT), "campfire works too");
        var food = new ItemStack(Agriculture.CANNED_MEAT).get(net.minecraft.core.component.DataComponents.FOOD);
        var beef = new ItemStack(net.minecraft.world.item.Items.COOKED_BEEF).get(net.minecraft.core.component.DataComponents.FOOD);
        h.assertTrue(food != null && food.nutrition() > beef.nutrition() && food.saturation() > beef.saturation(), "a little more filling than cooked beef");
        h.assertTrue(recipeUses(h, "bordeaux_mixture", com.taku3.semiconductorlab.content.Chemistry.COPPER_SULFATE) && recipeUses(h, "bordeaux_mixture", com.taku3.semiconductorlab.content.Survival.SLAKED_LIME), "CuSO4 + Ca(OH)2 -> Bordeaux");
        h.succeed();
    }

    /**
     * [11-06] 過リン酸石灰・[11-07] 硝安（IMP3、O-020）：骨粉＋硫酸 → リン肥料、NH3＋HNO3 → 窒素肥料。
     * 豆の輪作 [0-05] とくらべる：窒素（豆・硝安）は速さ、リンは収穫が1つ多い（3回分）。リンだけの耕地は速くならない。両方なくなればふつうの耕地
     */
    @GameTest
    public void fertilizers(GameTestHelper h) {
        var lv = h.getLevel();
        var NF = com.taku3.semiconductorlab.content.Survival.NITROGEN_FARMLAND;
        var Nk = com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.NITROGEN;
        var Pk = com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.PHOSPHORUS;
        var P = com.taku3.semiconductorlab.item.FertilizerItem.Nutrient.PHOSPHORUS;
        h.assertTrue(recipeUses(h, "superphosphate", Items.BONE_MEAL) && recipeUses(h, "superphosphate", Chemistry.SULFURIC_ACID), "bone + H2SO4 -> superphosphate");
        h.assertTrue(recipeUses(h, "ammonium_nitrate", Gas.AMMONIA_GAS) && recipeUses(h, "ammonium_nitrate", Gas.NITRIC_ACID), "NH3 + HNO3 -> ammonium nitrate");
        floor(h, 5);
        BlockPos nOnly = p(1, 1, 1), pOnly = p(3, 1, 1), both = p(5, 1, 1);
        for (BlockPos b : new BlockPos[] {nOnly, pOnly, both}) h.setBlock(b, Blocks.FARMLAND);
        h.assertTrue(com.taku3.semiconductorlab.item.FertilizerItem.fertilize(lv, h.absolutePos(nOnly)), "nitrogen on farmland");
        h.assertTrue(com.taku3.semiconductorlab.item.FertilizerItem.fertilize(lv, h.absolutePos(pOnly), P), "phosphate on farmland");
        com.taku3.semiconductorlab.item.FertilizerItem.fertilize(lv, h.absolutePos(both));
        h.assertTrue(com.taku3.semiconductorlab.item.FertilizerItem.fertilize(lv, h.absolutePos(both), P), "phosphate on top of nitrogen");
        h.assertTrue(h.getBlockState(nOnly).getValue(Nk) == 3 && h.getBlockState(nOnly).getValue(Pk) == 0, "N 3, P 0");
        h.assertTrue(h.getBlockState(pOnly).is(NF) && h.getBlockState(pOnly).getValue(Nk) == 0 && h.getBlockState(pOnly).getValue(Pk) == 3, "N 0, P 3");
        h.assertTrue(h.getBlockState(both).getValue(Nk) == 3 && h.getBlockState(both).getValue(Pk) == 3, "N 3, P 3");
        // 速さ：リンだけの耕地は麦を速くしない
        var wheat = (net.minecraft.world.level.block.CropBlock) Blocks.WHEAT;
        h.setBlock(pOnly.above(), wheat.getStateForAge(0));
        net.minecraft.util.RandomSource rnd = net.minecraft.util.RandomSource.create(3);
        for (int i = 0; i < 10; i++) h.getBlockState(pOnly).randomTick(lv, h.absolutePos(pOnly), rnd);
        h.assertValueEqual(wheat.getAge(h.getBlockState(pOnly.above())), 0, "phosphorus alone does not speed growth");
        // 収穫：熟した麦の落とし物が1つ多い、3回で使い切ってふつうの耕地
        int extra = 0;
        for (int i = 0; i < 4; i++) {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>(java.util.List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT_SEEDS)));
            if (com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.harvestBonus(lv, h.absolutePos(pOnly.above()), wheat.getStateForAge(wheat.getMaxAge()), drops)) {
                extra++;
                h.assertTrue(drops.size() == 3 && drops.get(2).is(Items.WHEAT), "one more wheat");
            }
        }
        h.assertValueEqual(extra, 3, "3 bigger harvests");
        h.assertTrue(h.getBlockState(pOnly).is(Blocks.FARMLAND), "phosphorus used up -> ordinary farmland");
        java.util.List<ItemStack> young = new java.util.ArrayList<>(java.util.List.of(new ItemStack(Items.WHEAT_SEEDS)));
        h.assertFalse(com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.harvestBonus(lv, h.absolutePos(both.above()), wheat.getStateForAge(3), young), "unripe crop: no bonus");
        LOG.info("[LAB] fertilizers [11-06][11-07]: N (peas/AS/AN) -> faster, P (superphosphate) -> +1 per harvest x3, P alone not faster");
        h.succeed();
    }

    /** [11-11] 塩蔵・燻製、[11-08] 製粉、[11-09] 発酵パン（IMP3、O-020）：火なしで塩漬け → いぶして燻製（焼いた肉より満腹）、石臼で粉 → 酵母で生地 → 焼いてバニラのパンより満腹 */
    @GameTest
    public void preservedFoodAndBread(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "salted_meat", Items.BEEF) && recipeUses(h, "salted_meat", Items.COD) && recipeUses(h, "salted_meat", Gas.SALT), "raw meat/fish + salt");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMOKING, Agriculture.SALTED_MEAT).is(Agriculture.SMOKED_MEAT), "smoke the salted meat");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING, Agriculture.SALTED_MEAT).is(Agriculture.SMOKED_MEAT), "campfire smoking");
        var F = net.minecraft.core.component.DataComponents.FOOD;
        var smoked = new ItemStack(Agriculture.SMOKED_MEAT).get(F);
        var salted = new ItemStack(Agriculture.SALTED_MEAT).get(F);
        h.assertTrue(salted != null && smoked.nutrition() > new ItemStack(Items.COOKED_BEEF).get(F).nutrition() && smoked.nutrition() > salted.nutrition(), "smoked > cooked beef > ...; salted edible");
        h.assertTrue(recipeUses(h, "flour", Agriculture.QUERN) && recipeUses(h, "flour", Items.WHEAT), "quern + wheat -> flour");
        h.assertTrue(new ItemStack(Agriculture.QUERN).getItem().getCraftingRemainder(new ItemStack(Agriculture.QUERN)) != null, "the quern comes back");
        h.assertTrue(recipeUses(h, "bread_dough", Agriculture.FLOUR) && recipeUses(h, "bread_dough", Chemistry.WILD_YEAST), "flour + yeast -> dough");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.SMELTING, Agriculture.BREAD_DOUGH).is(Agriculture.LEAVENED_BREAD), "bake -> leavened bread");
        var bread = new ItemStack(Agriculture.LEAVENED_BREAD).get(F);
        h.assertTrue(bread.nutrition() > new ItemStack(Items.BREAD).get(F).nutrition(), "more filling than vanilla bread");
        h.succeed();
    }

    /** [11-05] 堆肥（IMP3、O-021）：コンポスターの骨粉を耕地に → 窒素・リン 1回分ずつ（化学肥料は3回分）。足していけて、両方いっぱいなら使えない */
    @GameTest
    public void compost(GameTestHelper h) {
        var lv = h.getLevel();
        var Nk = com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.NITROGEN;
        var Pk = com.taku3.semiconductorlab.block.NitrogenFarmlandBlock.PHOSPHORUS;
        floor(h, 3);
        BlockPos f = p(1, 1, 1), chem = p(3, 1, 1);
        h.setBlock(f, Blocks.FARMLAND);
        h.setBlock(chem, Blocks.FARMLAND);
        h.assertTrue(com.taku3.semiconductorlab.item.FertilizerItem.compost(lv, h.absolutePos(f)), "compost on farmland");
        h.assertTrue(h.getBlockState(f).getValue(Nk) == 1 && h.getBlockState(f).getValue(Pk) == 1, "a little N and P");
        com.taku3.semiconductorlab.item.FertilizerItem.fertilize(lv, h.absolutePos(chem));
        h.assertTrue(h.getBlockState(chem).getValue(Nk) == 3, "chemical fertilizer: 3 shares at once");
        com.taku3.semiconductorlab.item.FertilizerItem.compost(lv, h.absolutePos(f));
        com.taku3.semiconductorlab.item.FertilizerItem.compost(lv, h.absolutePos(f));
        h.assertTrue(h.getBlockState(f).getValue(Nk) == 3 && h.getBlockState(f).getValue(Pk) == 3, "3 composts = full");
        h.assertFalse(com.taku3.semiconductorlab.item.FertilizerItem.compost(lv, h.absolutePos(f)), "already full");
        h.succeed();
    }
}
