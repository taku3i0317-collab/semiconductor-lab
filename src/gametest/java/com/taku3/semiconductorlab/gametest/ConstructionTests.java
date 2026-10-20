package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Construction;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.item.SurveyStation;
import com.taku3.semiconductorlab.item.TheodoliteItem;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * GameTest：幹12 建設・測量（IMP2 O-007 で作成）。共通の道具は {@link LabTestBase}。
 */
public class ConstructionTests extends LabTestBase {
    /** [12-13] HEPA フィルター（IMP2 S-003）：ソーダ石灰ガラス →溶鉱炉→ ガラス繊維×4 → HEPA フィルター。空調機のフィルターの入口に入る */
    @GameTest
    public void hepa_filter(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "glass_fiber", Construction.PLATINUM_BUSHING) && recipeUses(h, "glass_fiber", com.taku3.semiconductorlab.content.Chemistry.SODA_LIME_GLASS), "glass drawn through a platinum bushing");
        var rem = Construction.PLATINUM_BUSHING.getCraftingRemainder(new ItemStack(Construction.PLATINUM_BUSHING));
        h.assertTrue(rem != null && rem.item().value() == Construction.PLATINUM_BUSHING, "the bushing comes back");
        h.assertTrue(recipeUses(h, "hepa_filter", Construction.GLASS_FIBER), "HEPA = glass fibre mat in a frame");
        net.minecraft.core.BlockPos at = p(1, 1, 1);
        h.setBlock(at, Construction.AIR_CONDITIONER.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.class);
        h.assertFalse(be.filtered(), "no filter yet");
        be.getContainer().setItem(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SLOT_FILTER, new ItemStack(Construction.HEPA_FILTER));
        h.assertTrue(be.filtered(), "HEPA filter installed in the air conditioner");
        LOG.info("[LAB] hepa_filter [12-13]: soda-lime glass -> 4 glass fibre -> HEPA filter -> air conditioner filter slot");
        h.succeed();
    }

    /** [12-12] 空調（IMP2 S-003）：動いている空調機の近くは温度のゆれ ±3℃ → ±0.1℃。マスクアライナーの重ね合わせの「温度のずれ」が 3.3µm → 0.11µm */
    @GameTest
    public void air_conditioning(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "air_conditioner", com.taku3.semiconductorlab.content.Agriculture.REFRIGERATOR.block.asItem()), "air conditioner = refrigerator + fan motor");
        net.minecraft.core.BlockPos aligner = p(4, 1, 1), ac = p(1, 1, 1);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.swingC(h.getLevel(), h.absolutePos(aligner)) - 3.0) < 1e-9, "no air conditioning: +-3 C");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.thermalNm(3.0) - 3300) < 1e-6, "100 mm steel stage x 11 um/m/C x 3 C = 3.3 um");
        h.setBlock(ac, Construction.AIR_CONDITIONER.block);
        var be = h.getBlockEntity(ac, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.class);
        h.assertTrue(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.swingC(h.getLevel(), h.absolutePos(aligner)) == 3.0, "an unpowered air conditioner does nothing");
        tickConsumer(h, ac, be, 2, com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.WATTS));
        h.assertTrue(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.swingC(h.getLevel(), h.absolutePos(aligner)) == com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_ON_C, "running air conditioner: +-0.1 C");
        ItemStack ball3 = com.taku3.semiconductorlab.item.BallBearingItem.of(3).transmuteCopy(com.taku3.semiconductorlab.content.Precision.BALL_SCREW);
        int off = com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(ball3, 0, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.thermalNm(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_OFF_C));
        int on = com.taku3.semiconductorlab.block.MaskAlignerBlockEntity.overlayNm(ball3, 0, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.thermalNm(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_ON_C));
        h.assertTrue(off == 3_300 && on == 1_000, "overlay with the ball screw: " + off + " nm without, " + on + " nm with air conditioning");
        LOG.info("[LAB] air_conditioning [12-12]: +-3 C -> +-0.1 C near a running unit; aligner overlay 3.3 um -> 1.0 um");
        h.succeed();
    }

    /** yaw（Minecraft の向き）：A から (tx, tz) をのぞく向き */
    private static float yawTo(double ax, double az, double tx, double tz) { return (float) Math.toDegrees(Math.atan2(-(tx - ax), tz - az)); }

    /** [12-11] 測量（IMP2 O-007）：2つの測点から同じ目標をのぞいて、正弦定理で距離を出す */
    @GameTest
    public void triangulation(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "theodolite", Optics.REFRACTING_TELESCOPE) && recipeUses(h, "theodolite", Precision.VERNIER_CALIPER),
                "theodolite = telescope + graduated circle read with a vernier");
        // 底辺 20 m（A=(0,0)、B=(20,0)）、目標 (8, 30)
        double tx = 8, tz = 30;
        var f = TheodoliteItem.triangulate(0, 0, yawTo(0, 0, tx, tz), 20, 0, yawTo(20, 0, tx, tz));
        h.assertTrue(f != null, "two sight lines cross");
        h.assertTrue(Math.abs(f.base() - 20) < 1e-6, "base 20 m");
        h.assertTrue(Math.abs(f.distB() - Math.hypot(tx - 20, tz)) < 0.01, "distance from B by the sine rule: " + f.distB());
        h.assertTrue(Math.abs(f.distA() - Math.hypot(tx, tz)) < 0.01, "distance from A: " + f.distA());
        h.assertTrue(Math.abs(f.tx() - tx) < 0.01 && Math.abs(f.tz() - tz) < 0.01, "target position");
        h.assertTrue(Math.abs(f.alpha() + f.beta() + (180 - f.alpha() - f.beta()) - 180) < 1e-9 && f.alpha() > 70 && f.alpha() < 80, "angle at A ~75 deg: " + f.alpha());
        // 1分（1/60°）に丸めた読みでも、30 m 先なら誤差は数 cm
        var r = TheodoliteItem.triangulate(0, 0, TheodoliteItem.read(yawTo(0, 0, tx, tz)), 20, 0, TheodoliteItem.read(yawTo(20, 0, tx, tz)));
        h.assertTrue(Math.abs(r.distB() - f.distB()) < 0.05, "reading to 1 arcminute: error < 5 cm");
        // 1° ずれてねらうと、細長い三角形ほど誤差が大きい（底辺 5 m で 100 m 先）
        double far = 100;
        var thin = TheodoliteItem.triangulate(0, 0, yawTo(0, 0, 2, far) + 1f, 5, 0, yawTo(5, 0, 2, far));
        h.assertTrue(thin == null || Math.abs(thin.distB() - Math.hypot(3, far)) > 10, "1 deg off on a thin triangle -> big error (or no crossing)");
        // 視線が交わらない（反対の側を向く・平行）
        h.assertTrue(TheodoliteItem.triangulate(0, 0, yawTo(0, 0, 8, 30), 20, 0, yawTo(20, 0, 8, -30)) == null, "opposite sides -> no crossing");
        h.assertTrue(TheodoliteItem.triangulate(0, 0, 0f, 20, 0, 0f) == null, "parallel -> no crossing");
        // 方位：北＝0°、東＝90°
        h.assertTrue(Math.abs(TheodoliteItem.bearing(180f)) < 1e-9 && Math.abs(TheodoliteItem.bearing(-90f) - 90) < 1e-9, "bearing north 0, east 90");
        // アイテム：1回目で測点A を記録、短い底辺は測れない、2回目で計算して測点A を消す
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack th = new ItemStack(Construction.THEODOLITE);
        player.setItemInHand(InteractionHand.MAIN_HAND, th);
        player.setPos(0.5, 2, 0.5);
        player.setYRot(yawTo(0.5, 0.5, 8.5, 30.5));
        TheodoliteItem.sight(th, h.getLevel(), player);
        SurveyStation a = th.get(Construction.SURVEY_STATION);
        h.assertTrue(a != null && a.x() == 0.5, "station A recorded");
        player.setPos(1.5, 2, 0.5);
        TheodoliteItem.sight(th, h.getLevel(), player);
        h.assertTrue(th.has(Construction.SURVEY_STATION), "base 1 m is too short -> A kept");
        player.setPos(20.5, 2, 0.5);
        player.setYRot(yawTo(20.5, 0.5, 8.5, 30.5));
        TheodoliteItem.sight(th, h.getLevel(), player);
        h.assertFalse(th.has(Construction.SURVEY_STATION), "station B -> result, A cleared");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"station_a", "short_base", "no_cross", "result", "real", "cleared", "placeholder"})
            h.assertTrue(ja.has("item.semiconductor-lab.theodolite." + k), "lang theodolite." + k);
        LOG.info("[LAB] triangulation [12-11]: base 20 m, sine rule to 1 cm, 1-arcminute reading < 5 cm, thin triangle amplifies error, station A -> B");
        h.succeed();
    }
}
