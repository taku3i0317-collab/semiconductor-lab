package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Transport;
import com.taku3.semiconductorlab.item.ChronometerItem;
import com.taku3.semiconductorlab.item.Navigation;
import com.taku3.semiconductorlab.item.SextantItem;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * GameTest：幹14 交通・運輸（IMP2 O-023 で作成）。共通の道具は {@link LabTestBase}。
 */
public class TransportTests extends LabTestBase {
    /** [14-03] クロノメーター（IMP2 O-023）：東経30°（x=30000）で南中はグリニッジ 10時。六分儀＋クロノメーターで経度 30°E。狂った時計は経度もずれる */
    @GameTest
    public void chronometer_longitude(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "marine_chronometer", com.taku3.semiconductorlab.content.Metallurgy.CRUCIBLE_STEEL)
                && recipeUses(h, "marine_chronometer", com.taku3.semiconductorlab.content.Optics.RUBY_BOULE), "chronometer = crucible-steel spring + ruby jewels");
        BlockPos at = new BlockPos(30_000, 64, -10_000);   // 東経30°・北緯10°
        ItemStack clock = new ItemStack(Transport.MARINE_CHRONOMETER), sx = new ItemStack(Precision.SEXTANT);
        ChronometerItem.set(clock, 0, 0);
        int stage = 0;
        for (long t : new long[] {3_000, 3_600, 4_000, 4_400}) stage = SextantItem.sight(sx, h.getLevel(), at, t, clock, null);
        h.assertValueEqual(stage, 2, "noon passed");
        long maxTick = sx.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getLongOr("MaxTick", 0);
        double lon = SextantItem.longitudeFromNoon(ChronometerItem.readingHours(clock, maxTick));
        h.assertTrue(Math.abs(lon - 30) < 0.01, "local noon at 10:00 GMT -> 30 E, got " + lon);
        ItemStack bad = new ItemStack(Transport.MARINE_CHRONOMETER);
        ChronometerItem.set(bad, 0, 4);   // 1日に4秒進む
        long tenDays = 10 * Navigation.DAY_TICKS + 4_000;
        double err = ChronometerItem.errorSec(bad, tenDays);
        double badLon = SextantItem.longitudeFromNoon(ChronometerItem.readingHours(bad, tenDays));
        h.assertTrue(Math.abs(err - 40.67) < 0.1 && Math.abs(badLon - 30) > 0.15, "40 s fast after 10 days -> longitude off by " + Math.abs(badLon - 30) * 60 + "'");
        LOG.info("[LAB] chronometer [14-03]: noon at 10:00 GMT -> 30 E; a clock 40 s fast misplaces the ship by " + String.format(java.util.Locale.ROOT, "%.1f", Math.abs(badLon - 30) * 60) + " arcmin");
        h.succeed();
    }
}
