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
 * GameTest：枝🌿（IMP-28 で LabTests から分けた）。共通の道具は {@link LabTestBase}。
 * この幹のノードを足したら、テストはここに足す（新しいクラスを作ったら src/gametest/resources/fabric.mod.json にも1行）。
 */
public class BranchesTests extends LabTestBase {
    /** [P-17] スマホのカメラ（IMP2 O-030・O-031）：色の分かる画像センサー（E の素子＋ベイヤー）＋小さなレンズ＋赤外線カット膜 → カメラモジュール。右クリックでその場でカラーの写真 */
    @GameTest
    public void camera_module(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "color_image_sensor", com.taku3.semiconductorlab.content.Semiconductor.IMAGE_SENSOR) && recipeUses(h, "color_image_sensor", com.taku3.semiconductorlab.content.Semiconductor.BAYER_FILTER), "colour sensor = sensor + Bayer filter");
        h.assertTrue(recipeUses(h, "camera_module", com.taku3.semiconductorlab.content.Optics.CAMERA_LENS) && recipeUses(h, "camera_module", com.taku3.semiconductorlab.content.Optics.IR_CUT_FILTER)
                && recipeUses(h, "camera_module", com.taku3.semiconductorlab.content.Semiconductor.COLOR_IMAGE_SENSOR), "camera module = lenses + IR-cut + colour sensor");
        h.assertTrue(com.taku3.semiconductorlab.item.CameraModuleItem.autoExposure(15) == 100 && com.taku3.semiconductorlab.item.CameraModuleItem.autoExposure(4) == 100 && com.taku3.semiconductorlab.item.CameraModuleItem.autoExposure(0) == 0, "auto exposure works indoors, not in the dark");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.CameraModuleItem.airyUm(0.55, 2) - 2.684) < 0.01, "diffraction blur 2.7 um > 1 um pixel");
        ItemStack photo = com.taku3.semiconductorlab.item.CameraModuleItem.shoot(h.getLevel(), h.absolutePos(p(1, 2, 1)));
        h.assertTrue(photo.is(Branches.PRINT) && "digital".equals(photo.get(Branches.PHOTO).kind()), "an instant digital photo (no developing)");
        LOG.info("[LAB] camera module [P-17]: instant colour photo, F2, Airy 2.7 um vs 1 um pixels");
        h.succeed();
    }

    // =====================================================================
    // [B-12] 太陽電池（IMP-25、v0.2b 第2章のゴール）
    // =====================================================================

    /** p-n 接合の板8＋銅ケーブル → 太陽電池。昼・空で 60W（1000W/m²×6%）、雨は約2割の 12W。夜・屋根の下は 0。テスト世界の昼夜・天気に合わせて確かめる */
    @GameTest
    public void solarCell(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "solar_cell", Semiconductor.PN_JUNCTION_WAFER) && recipeUses(h, "solar_cell", Power.COPPER_CABLE.block.asItem()), "solar cell = p-n slices + copper electrodes");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS - 60) < 1e-9, "1000 W/m2 x 6% x 1 m2 = 60 W");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS * 0.836 - 50) < 1.0, "matches 1954 'about 50 W per square yard'");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, true, false), com.taku3.semiconductorlab.energy.SolarCellBlockEntity.WATTS, "sunny day, open sky -> 60 W");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, false, false), 0.0, "night -> 0");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(false, true, false), 0.0, "under a roof -> 0");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, true, true) - 12) < 1e-9, "rain (overcast) -> diffuse light, about 20% = 12 W");
        h.assertValueEqual(com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(true, false, true), 0.0, "rainy night -> 0");
        BlockPos fp = p(1, 1, 1), roofed = p(3, 1, 1);
        h.setBlock(fp, Branches.SOLAR_CELL.block);
        h.setBlock(roofed, Branches.SOLAR_CELL.block);
        h.setBlock(roofed.above(2), Blocks.STONE);
        ServerLevel lv = h.getLevel();
        BlockPos a = h.absolutePos(fp), ar = h.absolutePos(roofed);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.energy.SolarCellBlockEntity.class);
        var ber = h.getBlockEntity(roofed, com.taku3.semiconductorlab.energy.SolarCellBlockEntity.class);
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, a, lv.getBlockState(a), be);
        com.taku3.semiconductorlab.energy.GeneratorBlockEntity.tick(lv, ar, lv.getBlockState(ar), ber);
        double expect = com.taku3.semiconductorlab.energy.SolarCellBlockEntity.watts(com.taku3.semiconductorlab.block.SaltPanBlockEntity.openSky(lv, a), lv.isBrightOutside(), lv.isRainingAt(a.above()));
        h.assertValueEqual(be.getLastWatts(), expect, "output follows the world's day/night and weather");
        h.assertValueEqual(ber.getLastWatts(), 0.0, "stone roof -> 0 W");
        LOG.info("[LAB] solar cell [B-12]: {} W (bright={}, rain={}); roofed 0 W", be.getLastWatts(), lv.isBrightOutside(), lv.isRainingAt(a.above()));
        h.succeed();
    }

    /** [B-02b] ゼラチン乾板（IMP2 O-008：湿板の代わり）：乾板 1秒 → 硫酸鉄(II)で現像 → ハイポで定着 → ネガ。ネガ＋印画紙 → 紙焼き（ネガは戻る＝何枚でも） */
    @GameTest
    public void dry_plate_negative(GameTestHelper h) {
        int D = com.taku3.semiconductorlab.item.CameraItem.DRY_TARGET_TICKS;
        h.assertTrue(com.taku3.semiconductorlab.item.CameraItem.targetTicks(new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTO_PLATE)) == D, "dry plate can be exposed");
        h.assertTrue(D * 60 == com.taku3.semiconductorlab.item.CameraItem.DAG_TARGET_TICKS, "dry plate 1 s vs silver plate 60 s");
        h.assertValueEqual(com.taku3.semiconductorlab.item.CameraItem.exposurePercent(D, 1.0, D), 100, "1 s in sun = 100%");
        h.assertTrue(recipeUses(h, "printing_paper", com.taku3.semiconductorlab.content.Gas.SILVER_NITRATE) && recipeUses(h, "printing_paper", com.taku3.semiconductorlab.content.Gas.SALT),
                "printing paper = salted paper + silver nitrate (silver chloride)");
        var data = new com.taku3.semiconductorlab.item.PhotoData("forest (5, 6)", 1000, 90, "dry_plate");
        ItemStack exposed = new ItemStack(Branches.EXPOSED_DRY_PLATE);
        exposed.set(Branches.PHOTO, data);
        ItemStack dev = craft2(h, exposed, new ItemStack(com.taku3.semiconductorlab.content.Optics.FERROUS_SULFATE));
        h.assertTrue(dev.is(Branches.DEVELOPED_DRY_PLATE) && data.equals(dev.get(Branches.PHOTO)), "iron(II) sulfate develops, data kept");
        h.assertTrue(craft2(h, exposed, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.MERCURY)).isEmpty(), "mercury does not develop a dry plate");
        ItemStack neg = craft2(h, dev, new ItemStack(com.taku3.semiconductorlab.content.Optics.SODIUM_THIOSULFATE));
        h.assertTrue(neg.is(Branches.NEGATIVE) && data.equals(neg.get(Branches.PHOTO)), "hypo fixes -> negative");
        // ネガから何枚でも焼ける：ネガは中身ごと戻る
        for (int n = 0; n < 3; n++) {
            ItemStack print = craft2(h, neg, new ItemStack(Branches.PRINTING_PAPER));
            h.assertTrue(print.is(Branches.PRINT) && data.equals(print.get(Branches.PHOTO)), "print " + (n + 1) + " carries the negative's data");
            var back = Branches.NEGATIVE.getCraftingRemainder(neg);
            h.assertTrue(back != null && back.item().value() == Branches.NEGATIVE, "negative comes back");
            neg = back.create();
            h.assertTrue(data.equals(neg.get(Branches.PHOTO)), "returned negative keeps its data");
        }
        // カメラに乾板を持つと開く
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Branches.CAMERA));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(com.taku3.semiconductorlab.content.Optics.PHOTO_PLATE));
        h.assertTrue(Branches.CAMERA.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "with a dry plate -> shutter opens");
        LOG.info("[LAB] dry_plate_negative [B-02b]: dry plate 1 s, iron(II) sulfate develop, hypo fix -> negative, 3 prints and the negative returns with its data");
        h.succeed();
    }

    /** [B-02a] ダゲレオタイプ（IMP2、S-002 案B）：銀めっき板＋ヨウ素 → 感光板、カメラで露光（日なた 60秒がちょうどよい）、水銀で現像、塩かハイポで定着。写真のデータは最後まで引き継ぐ */
    @GameTest
    public void daguerreotype(GameTestHelper h) {
        var cam = com.taku3.semiconductorlab.item.CameraItem.class;
        int T = com.taku3.semiconductorlab.item.CameraItem.DAG_TARGET_TICKS;
        h.assertValueEqual(com.taku3.semiconductorlab.item.CameraItem.exposurePercent(T, 1.0, T), 100, "60 s in sun = 100%");
        h.assertValueEqual(com.taku3.semiconductorlab.item.CameraItem.exposurePercent(T, 0.3, T), 30, "rain: 30% -> thin");
        h.assertValueEqual(new com.taku3.semiconductorlab.item.PhotoData("x", 0, 30, "daguerreotype").quality(), "thin", "under 50% -> thin");
        h.assertValueEqual(new com.taku3.semiconductorlab.item.PhotoData("x", 0, 120, "daguerreotype").quality(), "good", "50-200% -> good");
        h.assertValueEqual(new com.taku3.semiconductorlab.item.PhotoData("x", 0, 350, "daguerreotype").quality(), "dark", "over 200% -> dark");
        h.assertTrue(com.taku3.semiconductorlab.item.CameraItem.targetTicks(new ItemStack(Branches.DAGUERREOTYPE_PLATE)) == T, "sensitized plate can be exposed");
        h.assertTrue(com.taku3.semiconductorlab.item.CameraItem.targetTicks(new ItemStack(Branches.SILVERED_PLATE)) == 0, "unsensitized plate cannot");
        h.assertTrue(recipeUses(h, "daguerreotype_plate", com.taku3.semiconductorlab.content.Chemistry.IODINE), "iodine vapor sensitizes the plate");
        // 現像・定着：成分（写真の中身）を引き継ぐ（crafting_transmute）
        var data = new com.taku3.semiconductorlab.item.PhotoData("plains (10, 20)", 6000, 110, "daguerreotype");
        ItemStack exposed = new ItemStack(Branches.EXPOSED_PLATE);
        exposed.set(Branches.PHOTO, data);
        ItemStack dev = craft2(h, exposed, new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.MERCURY));
        h.assertTrue(dev.is(Branches.DEVELOPED_PLATE) && data.equals(dev.get(Branches.PHOTO)), "mercury vapor develops, data kept");
        ItemStack fixed = craft2(h, dev, new ItemStack(com.taku3.semiconductorlab.content.Gas.SALT));
        h.assertTrue(fixed.is(Branches.DAGUERREOTYPE) && data.equals(fixed.get(Branches.PHOTO)), "salt fixes, data kept");
        ItemStack fixed2 = craft2(h, dev, new ItemStack(com.taku3.semiconductorlab.content.Optics.SODIUM_THIOSULFATE));
        h.assertTrue(fixed2.is(Branches.DAGUERREOTYPE), "hypo fixes too");
        // カメラを使う：感光板を持たないと FAIL
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Branches.CAMERA));
        h.assertFalse(Branches.CAMERA.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "no plate -> nothing");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Branches.DAGUERREOTYPE_PLATE));
        h.assertTrue(Branches.CAMERA.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "with a plate -> shutter opens");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"photo.place", "photo.time", "photo.exposure", "photo.thin", "photo.good", "photo.dark", "camera.hold", "camera.exposed"})
            h.assertTrue(ja.has("item.semiconductor-lab." + k), "lang " + k);
        LOG.info("[LAB] daguerreotype [B-02a]: plate + iodine, camera 60 s in sun = 100%, mercury develop, salt/hypo fix, data kept");
        h.succeed();
    }

    /** 作業台に2つ並べたときの出力（成分を引き継ぐクラフトの確認用） */
    private static ItemStack craft2(GameTestHelper h, ItemStack a, ItemStack b) {
        var input = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, java.util.List.of(a, b));
        return h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, input, h.getLevel())
                .map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    /** [B-14] アセチレンランプ（IMP3、O-018）：カーバイドで光る（明るさ15）、燃えている間は入らない、燃え尽きると暗くなり、また入れると消石灰が出る */
    @GameTest
    public void carbideLamp(GameTestHelper h) {
        floor(h, 3);
        BlockPos rel = p(1, 1, 1); BlockPos pos = h.absolutePos(rel);
        h.setBlock(rel, Branches.CARBIDE_LAMP);
        var level = h.getLevel(); var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack carbide = new ItemStack(com.taku3.semiconductorlab.content.Heat.CALCIUM_CARBIDE, 3);
        h.assertTrue(com.taku3.semiconductorlab.block.CarbideLampBlock.light(level.getBlockState(pos)) == 0, "dark at first");
        h.assertTrue(com.taku3.semiconductorlab.block.CarbideLampBlock.fuel(level, pos, player, carbide) && carbide.getCount() == 2, "carbide in");
        h.assertTrue(com.taku3.semiconductorlab.block.CarbideLampBlock.light(level.getBlockState(pos)) == 15, "bright");
        h.assertFalse(com.taku3.semiconductorlab.block.CarbideLampBlock.fuel(level, pos, player, carbide), "not while burning");
        for (int i = 0; i < com.taku3.semiconductorlab.block.CarbideLampBlock.MAX_FUEL; i++) com.taku3.semiconductorlab.block.CarbideLampBlock.burn(level, pos);
        h.assertTrue(com.taku3.semiconductorlab.block.CarbideLampBlock.light(level.getBlockState(pos)) == 0, "burnt out");
        h.assertTrue(com.taku3.semiconductorlab.block.CarbideLampBlock.fuel(level, pos, player, carbide) && player.getInventory().countItem(Survival.SLAKED_LIME) == 1, "refill gives back the spent slaked lime");
        h.succeed();
    }

    /**
     * [B-10] スマホ（O-029 Q28 案A 仮決定）：組立台のレシピは8種（マイコン・カメラ・加速度計・タッチパネル・TFT 液晶・バックライト・電池・ケース）。
     * TFT 液晶・バックライトのアイテムがまだ登録されていなければ、レシピそのものがない＝組めない。そろったら8種のレシピ、マイコンは mcu4 に合格したチップだけ
     */
    @GameTest
    public void smartphoneRecipe(GameTestHelper h) {
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Branches.SMARTPHONE);
        if (!com.taku3.semiconductorlab.content.Branches.smartphoneReady()) {   // TFT 液晶・バックライトがまだ：レシピがない＝組めない
            h.assertTrue(a == null, "no smartphone recipe until the TFT LCD and the backlight exist");
            h.succeed();
            return;
        }
        h.assertTrue(a != null && a.parts.size() == 8, "smartphone assembly with 8 roles");
        var chip = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP);
        chip.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("mcu4"));
        h.assertTrue(a.satisfied(0, chip) && !a.satisfied(0, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP)), "the SoC role needs a chip that passed mcu4");
        h.assertTrue(a.satisfied(3, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Communication.TOUCH_PANEL)), "the touch panel fits");
        h.succeed();
    }

    /**
     * [B-09] ゲーム機（S-017 Q27 案A ポン型）：卓球の動き（PongLogic）。玉は上の壁で跳ね返る、プレイヤーが板を動かさないと自動の側に点が入る、
     * 板で打ち返せる、9点で終わる。組立台はカウンタ・全加算器・レジスタに合格したチップ（6種）
     */
    @GameTest
    public void gameConsolePong(GameTestHelper h) {
        var p = new com.taku3.semiconductorlab.item.PongLogic();
        p.by = 1; p.vy = -1; p.bx = 60; p.vx = 2;
        p.step(p.left);
        h.assertTrue(p.vy > 0, "the ball bounces off the top wall");
        var miss = new com.taku3.semiconductorlab.item.PongLogic();
        miss.vx = -2; miss.vy = 0; miss.by = 2;
        for (int i = 0; i < 80 && miss.scoreRight == 0; i++) miss.step(com.taku3.semiconductorlab.item.PongLogic.H);   // 板を下に置いたまま
        h.assertValueEqual(miss.scoreRight, 1, "a missed ball scores for the other side");
        var hit = new com.taku3.semiconductorlab.item.PongLogic();
        hit.vx = -2; hit.vy = 0; hit.by = hit.left + 4;
        for (int i = 0; i < 40; i++) hit.step(hit.left);
        h.assertTrue(hit.scoreRight == 0 && hit.vx > 0, "the paddle returns the ball");
        var end = new com.taku3.semiconductorlab.item.PongLogic();
        for (int i = 0; i < 20000 && !end.over(); i++) end.step(end.by < com.taku3.semiconductorlab.item.PongLogic.H / 2 ? com.taku3.semiconductorlab.item.PongLogic.H : 0);   // 玉から逃げる板
        h.assertTrue(end.over() && Math.max(end.scoreLeft, end.scoreRight) == com.taku3.semiconductorlab.item.PongLogic.WIN, "the game ends at 9");
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Branches.GAME_CONSOLE);
        h.assertTrue(a != null && a.parts.size() == 6, "game console assembly with 6 roles");
        var counter = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP, 2);
        counter.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("counter"));
        h.assertTrue(a.satisfied(0, counter) && !a.satisfied(0, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP, 2)), "counter chips only");
        h.succeed();
    }

    /** [B-06] 偏光フィルム（IMP3、O-023）：PVA の膜＋ヨウ素＋KI＋ホウ酸。1枚で 50%、2枚で 50 cos²θ（同じ向き 50、45度 25、直角 0）。両手で右クリックすると15度ずつ回る */
    @GameTest
    public void polarizingFilm(GameTestHelper h) {
        var B = com.taku3.semiconductorlab.content.Branches.class;
        h.assertTrue(recipeUses(h, "polarizing_film", com.taku3.semiconductorlab.content.Branches.PVA_FILM) && recipeUses(h, "polarizing_film", com.taku3.semiconductorlab.content.Chemistry.IODINE)
                && recipeUses(h, "polarizing_film", com.taku3.semiconductorlab.content.Metallurgy.BORIC_ACID), "PVA + iodine + boric acid (H-sheet)");
        h.assertTrue(recipeUses(h, "pva_film", com.taku3.semiconductorlab.content.Chemistry.POLYVINYL_ALCOHOL) && recipeUses(h, "potassium_iodide", com.taku3.semiconductorlab.content.Chemistry.POTASH), "PVA film, KI");
        var P = com.taku3.semiconductorlab.item.PolarizerItem.class;
        h.assertTrue(com.taku3.semiconductorlab.item.PolarizerItem.brightness(1, 0) == 50, "one film: half");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.PolarizerItem.brightness(2, 0) - 50) < 1e-9 && Math.abs(com.taku3.semiconductorlab.item.PolarizerItem.brightness(2, 45) - 25) < 1e-9
                && com.taku3.semiconductorlab.item.PolarizerItem.brightness(2, 90) < 1e-9, "Malus: 50, 25, 0");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Branches.POLARIZING_FILM));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Branches.POLARIZING_FILM));
        for (int i = 0; i < 6; i++) player.getMainHandItem().use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertValueEqual(com.taku3.semiconductorlab.item.PolarizerItem.angle(player.getMainHandItem()), 90, "six turns of 15 degrees = crossed");
        LOG.info("[LAB] polarizing film [B-06]: 1 film 50%, 2 films 50cos^2: 0deg 50, 45deg 25, 90deg 0 (" + B.getSimpleName() + "/" + P.getSimpleName() + ")");
        h.succeed();
    }
}
