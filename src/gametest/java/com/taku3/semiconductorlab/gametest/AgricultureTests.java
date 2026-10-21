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
}
