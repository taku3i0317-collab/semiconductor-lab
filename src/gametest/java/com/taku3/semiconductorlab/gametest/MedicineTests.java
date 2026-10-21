package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Medicine;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTest：幹13 医療・公衆衛生（IMP3 S-011 で作成）。共通の道具は {@link LabTestBase}。
 */
public class MedicineTests extends LabTestBase {
    /** [13-13] 体温計（IMP3、S-011）：ガラス＋水銀、空気の温度（平原 約17℃・砂漠 約47℃・雪原 約-3℃）、体温 36.5〜37.0℃ */
    @GameTest
    public void thermometer(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "mercury_thermometer", com.taku3.semiconductorlab.content.Metallurgy.MERCURY) && recipeUses(h, "mercury_thermometer", com.taku3.semiconductorlab.content.Chemistry.SODA_LIME_GLASS), "mercury in a glass tube");
        double plains = com.taku3.semiconductorlab.item.ThermometerItem.airCelsius(0.8f), desert = com.taku3.semiconductorlab.item.ThermometerItem.airCelsius(2.0f), snow = com.taku3.semiconductorlab.item.ThermometerItem.airCelsius(0.0f);
        h.assertTrue(plains > 10 && plains < 25 && desert > 35 && snow < 0, "air: plains " + plains + ", desert " + desert + ", snow " + snow);
        h.assertTrue(com.taku3.semiconductorlab.item.ThermometerItem.bodyCelsius(0) >= 36.5 && com.taku3.semiconductorlab.item.ThermometerItem.bodyCelsius(1) <= 37.0, "body 36.5-37.0");
        h.assertTrue(Medicine.MERCURY_THERMOMETER.getDefaultMaxStackSize() == 1, "one per slot");
        h.succeed();
    }
}
