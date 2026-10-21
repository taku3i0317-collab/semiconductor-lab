package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Construction;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.item.SurveyStation;
import com.taku3.semiconductorlab.item.TheodoliteItem;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * GameTest：幹12 建設・測量（IMP2 O-007 で作成）。共通の道具は {@link LabTestBase}。
 */
public class ConstructionTests extends LabTestBase {
    /** 石の箱（外 0〜6、中 1〜5 × 2〜4 × 1〜5）を作り、天井の段（y=5）の中を ceiling で埋める。hepa＝HEPA の天井にするマスの数 */
    private static void room(GameTestHelper h, int hepa) {
        for (int x = 0; x <= 6; x++) for (int y = 1; y <= 5; y++) for (int z = 0; z <= 6; z++) {
            boolean wall = x == 0 || x == 6 || z == 0 || z == 6 || y == 1 || y == 5;
            h.setBlock(p(x, y, z), wall ? net.minecraft.world.level.block.Blocks.STONE : net.minecraft.world.level.block.Blocks.AIR);
        }
        int k = 0;
        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) if (k++ < hepa) h.setBlock(p(x, 5, z), Construction.HEPA_CEILING);
    }

    /** [12-14] 層流クリーンルーム（IMP2 S-003、A と決めた形）：天井の HEPA の割合と、HEPA 入りの空調で等級。等級の汚れ（外 2・1000 は 1・100 は 0）が露光のあとに付く */
    @GameTest(maxTicks = 100)
    public void clean_room(GameTestHelper h) {
        net.minecraft.core.BlockPos at = p(3, 2, 3), ac = p(1, 2, 1);
        var lv = h.getLevel();
        room(h, 25);   // 天井 25 マス全部 HEPA
        h.setBlock(at, net.minecraft.world.level.block.Blocks.STONE);
        h.assertValueEqual(com.taku3.semiconductorlab.block.CleanRoom.classAt(lv, h.absolutePos(at)), com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE, "no air conditioning -> outside grade");
        h.setBlock(ac, Construction.AIR_CONDITIONER.block);
        var be = h.getBlockEntity(ac, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.class);
        be.getContainer().setItem(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SLOT_FILTER, new ItemStack(Construction.HEPA_FILTER));
        tickConsumer(h, ac, be, 2, com.taku3.semiconductorlab.energy.EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.WATTS));
        h.assertValueEqual(com.taku3.semiconductorlab.block.CleanRoom.classAt(lv, h.absolutePos(at)), com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_100, "full HEPA ceiling + filtered air -> class 100");
        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) if (x > 2) h.setBlock(p(x, 5, z), net.minecraft.world.level.block.Blocks.STONE);   // 10/25 = 40%
        h.assertValueEqual(com.taku3.semiconductorlab.block.CleanRoom.classAt(lv, h.absolutePos(at)), com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_1000, "40% HEPA ceiling -> class 1000");
        h.setBlock(p(3, 5, 3), net.minecraft.world.level.block.Blocks.AIR);   // 天井に穴
        h.assertValueEqual(com.taku3.semiconductorlab.block.CleanRoom.classAt(lv, h.absolutePos(at)), com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE, "a hole to the sky -> not a room");
        h.assertTrue(com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE.dust == 2 && com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_1000.dust == 1 && com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_100.dust == 0, "dust by grade (A's guide)");
        h.assertTrue(recipeUses(h, "hepa_ceiling", Construction.HEPA_FILTER), "HEPA ceiling from HEPA filters");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"room_none", "room_1000", "room_100"}) h.assertTrue(ja.has("gui.semiconductor-lab.mask_aligner." + k), "lang " + k);
        LOG.info("[LAB] clean_room [12-14]: no AC -> none; full HEPA ceiling + filtered AC -> class 100; 40% -> class 1000; hole -> none; dust 2/1/0");
        h.succeed();
    }

    /** [12-13] HEPA フィルター（IMP2 S-003）：ソーダ石灰ガラス →溶鉱炉→ ガラス繊維×4 → HEPA フィルター。空調機のフィルターの入口に入る */
    /** v0.5 #4・#5（IMP2 O-025）：部屋の等級 → 欠陥の密度 D（外 5・クラス1000 1・クラス100 0.2 個/cm²）と歩留まり e^(−D·A)。露光以外の工程の汚れの入口 dustAt */
    @GameTest
    public void clean_room_yield(GameTestHelper h) {
        var g = com.taku3.semiconductorlab.block.CleanRoom.Grade.values();
        h.assertTrue(com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE.defectsPerCm2 > com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_1000.defectsPerCm2 && com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_1000.defectsPerCm2 > com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_100.defectsPerCm2, "cleaner room -> fewer defects");
        double out = com.taku3.semiconductorlab.block.CleanRoom.poissonYield(com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE.defectsPerCm2, 1), c1000 = com.taku3.semiconductorlab.block.CleanRoom.poissonYield(com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_1000.defectsPerCm2, 1), c100 = com.taku3.semiconductorlab.block.CleanRoom.poissonYield(com.taku3.semiconductorlab.block.CleanRoom.Grade.CLASS_100.defectsPerCm2, 1);
        h.assertTrue(out < 0.01 && Math.abs(c1000 - 0.368) < 0.001 && Math.abs(c100 - 0.819) < 0.001, "1 cm2 chip yield: open " + out + ", class 1000 " + c1000 + ", class 100 " + c100);
        h.assertTrue(com.taku3.semiconductorlab.block.CleanRoom.poissonYield(1, 0.1) > com.taku3.semiconductorlab.block.CleanRoom.poissonYield(1, 1), "small chips yield better");
        BlockPos at = p(1, 1, 1);
        h.assertValueEqual(com.taku3.semiconductorlab.block.CleanRoom.dustAt(h.getLevel(), h.absolutePos(at)), com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE.dust, "no room -> open-air dust for any process");
        h.assertTrue(com.taku3.semiconductorlab.block.CleanRoom.defectDensity(h.getLevel(), h.absolutePos(at)) == com.taku3.semiconductorlab.block.CleanRoom.Grade.NONE.defectsPerCm2, "no room -> open-air defect density");
        LOG.info("[LAB] clean room yield [v0.5 #4/#5]: D = 5 / 1 / 0.2 per cm2 -> 1 cm2 yield " + String.format(java.util.Locale.ROOT, "%.1f%% / %.0f%% / %.0f%%", out * 100, c1000 * 100, c100 * 100) + " (open / class 1000 / class 100)");
        h.succeed();
    }

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

    /** [12-01] ローマンコンクリート（IMP3、S-011）：消石灰＋凝灰岩＋砂利 → 生コン、水にふれると固まる（水硬性） */
    @GameTest
    public void romanConcrete(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "roman_concrete_mix", com.taku3.semiconductorlab.content.Survival.SLAKED_LIME) && recipeUses(h, "roman_concrete_mix", net.minecraft.world.item.Items.TUFF), "lime + volcanic ash (tuff)");
        floor(h, 3);
        net.minecraft.core.BlockPos mix = p(1, 1, 1);
        h.setBlock(mix, com.taku3.semiconductorlab.content.Construction.ROMAN_CONCRETE_MIX);
        h.assertBlockPresent(com.taku3.semiconductorlab.content.Construction.ROMAN_CONCRETE_MIX, mix);
        h.setBlock(p(2, 1, 1), net.minecraft.world.level.block.Blocks.WATER);
        h.succeedWhen(() -> h.assertBlockPresent(com.taku3.semiconductorlab.content.Construction.ROMAN_CONCRETE, mix));
    }

    /** [12-02] ポルトランドセメント（IMP3、S-011）：石灰石＋粘土 → 溶鉱炉 → クリンカー → ＋石こう → セメント → ＋砂＋砂利 → 灰色のコンクリートパウダー */
    @GameTest
    public void portlandCement(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cement_raw_meal", net.minecraft.world.item.Items.CALCITE) && recipeUses(h, "cement_raw_meal", net.minecraft.world.item.Items.CLAY_BALL), "limestone + clay");
        h.assertTrue(cook(h, net.minecraft.world.item.crafting.RecipeType.BLASTING, Construction.CEMENT_RAW_MEAL).is(Construction.CEMENT_CLINKER), "kiln -> clinker");
        h.assertTrue(recipeUses(h, "portland_cement", com.taku3.semiconductorlab.content.Chemistry.CALCIUM_SULFATE), "clinker + gypsum");
        h.assertTrue(recipeUses(h, "gray_concrete_powder_from_cement", Construction.PORTLAND_CEMENT), "cement -> concrete powder");
        h.succeed();
    }

    /** [12-03] 鉄筋コンクリート（IMP3、S-011）：コンクリート8＋鋼 → 8、ふつうのコンクリートより爆発に強い */
    @GameTest
    public void reinforcedConcrete(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "reinforced_concrete", com.taku3.semiconductorlab.content.Metallurgy.STEEL_INGOT) && recipeUses(h, "reinforced_concrete", net.minecraft.world.item.Items.CONCRETE.gray()), "concrete + steel rebar");
        float rc = Construction.REINFORCED_CONCRETE.getExplosionResistance(), c = net.minecraft.world.level.block.Blocks.CONCRETE.gray().getExplosionResistance();
        h.assertTrue(rc > c * 10, "much stronger than plain concrete: " + rc + " vs " + c);
        h.succeed();
    }

    /** [12-05] 板ガラス（フロート法、IMP3、S-011）：スズの浴槽（戻る）＋ソーダ石灰ガラス6 → バニラのガラス板16 */
    @GameTest
    public void floatGlass(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "glass_pane_float", Construction.TIN_BATH) && recipeUses(h, "glass_pane_float", com.taku3.semiconductorlab.content.Chemistry.SODA_LIME_GLASS), "float glass on molten tin");
        h.assertTrue(recipeUses(h, "tin_bath", com.taku3.semiconductorlab.content.Metallurgy.TIN_INGOT), "tin bath");
        var rem = Construction.TIN_BATH.getCraftingRemainder(new ItemStack(Construction.TIN_BATH));
        h.assertTrue(rem != null && rem.create().is(Construction.TIN_BATH), "the tin bath comes back");
        h.succeed();
    }

    /** [12-15] 避雷針（IMP3、S-011）：バニラの避雷針は銅＝本物どおり（新しいアイテムなし、説明だけ） */
    @GameTest
    public void lightningRod(GameTestHelper h) {
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, net.minecraft.resources.Identifier.withDefaultNamespace("lightning_rod"));
        boolean copper = h.getLevel().getServer().getRecipeManager().byKey(key)
                .map(r -> r.value().placementInfo().ingredients().stream().anyMatch(i -> i.test(new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT)))).orElse(false);
        h.assertTrue(copper, "the vanilla lightning rod is made of copper");
        h.succeed();
    }
}
