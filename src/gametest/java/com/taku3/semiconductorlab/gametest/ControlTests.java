package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Control;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;

/**
 * GameTest：幹10 制御・計算（IMP4-01 O-011 で作った）。共通の道具は {@link LabTestBase}。
 */
public class ControlTests extends LabTestBase {

    /** [10-01] 電磁石：鉄の芯＋銅線（伸線 [1-58]）を巻く */
    @GameTest
    public void electromagnet(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "electromagnet", Items.IRON_INGOT) && recipeUses(h, "electromagnet", com.taku3.semiconductorlab.content.Metallurgy.COPPER_WIRE), "electromagnet = iron core + copper wire coil");
        h.assertTrue(new net.minecraft.world.item.ItemStack(Control.ELECTROMAGNET).getCount() == 1, "item exists");
        LOG.info("[LAB] electromagnet [10-01]: iron core + copper wire coil");
        h.succeed();
    }
}
