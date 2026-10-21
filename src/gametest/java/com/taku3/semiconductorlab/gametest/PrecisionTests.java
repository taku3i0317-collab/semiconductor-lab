package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.block.LatheBlockEntity;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GameTest：幹8 精密機械・計測（IMP2-01 で作成）。共通の道具は {@link LabTestBase}。
 */
public class PrecisionTests extends LabTestBase {
    /** [8-11] 電子顕微鏡（IMP2 O-017）：電子の波長は光の 10 万分の1。光学顕微鏡（306nm）で見えない KrF 以降の細かい方眼も見える。高真空がないと電子線が出ない */
    @GameTest
    public void electron_microscope(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "electron_microscope", com.taku3.semiconductorlab.content.Power.TRANSFORMER.block.asItem())
                && recipeUses(h, "electron_microscope", com.taku3.semiconductorlab.content.Power.RECTIFIER.block.asItem())
                && recipeUses(h, "electron_microscope", Metallurgy.TUNGSTEN_WIRE), "electron gun (W) + tens of kV (transformer + rectifier) + coil lenses");
        double lam = com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.electronWavelengthNm(com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.KV);
        h.assertTrue(lam > 0.005 && lam < 0.006, "50 kV electrons: 0.0055 nm (light 550 nm)");
        int krf = com.taku3.semiconductorlab.litho.ExposureTool.KRF.grid, iLine = com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I.grid;
        h.assertTrue(((com.taku3.semiconductorlab.item.MicroscopeItem) Precision.COMPOUND_MICROSCOPE).canResolve(iLine), "the optical microscope still sees i-line lines (350 nm)");
        h.assertFalse(((com.taku3.semiconductorlab.item.MicroscopeItem) Precision.COMPOUND_MICROSCOPE).canResolve(krf), "but not KrF lines (130 nm)");
        h.assertTrue(com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.canResolve(krf) && com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.EUV.grid), "the electron microscope sees KrF and even EUV lines");
        // 高真空なしでは電子線が出ない、あれば電気を使う
        BlockPos at = p(1, 1, 1), ch = p(2, 1, 1);
        h.setBlock(at, Precision.ELECTRON_MICROSCOPE.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.class);
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity.WATTS);
        tickConsumer(h, at, be, 3, feed);
        h.assertFalse(be.on(), "no vacuum -> no beam");
        h.setBlock(ch, com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block);
        h.getBlockEntity(ch, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.class).setPressure(0.001);
        tickConsumer(h, at, be, 3, feed);
        h.assertTrue(be.on(), "0.001 Pa next door -> beam on");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"power", "limit", "optical", "cell", "need_vacuum", "need_power", "need_sample", "em_only", "both", "neither"})
            h.assertTrue(ja.has("gui.semiconductor-lab.electron_microscope." + k), "lang " + k);
        LOG.info("[LAB] electron_microscope [8-11]: 50 kV -> 0.0055 nm, resolution 10 nm; optical 306 nm sees i-line but not KrF; vacuum needed");
        h.succeed();
    }

    /** [8-12] 刻線機（IMP2 O-016）：1mm に 600 本・30000 本の格子は D 線（R≈982）も水銀の黄2本も分けられる。針金の格子（246本）では1本。揺れ・温度でゴースト */
    @GameTest(maxTicks = 3000)
    public void ruling_engine(GameTestHelper h) {
        h.assertFalse(com.taku3.semiconductorlab.item.SpectroscopeItem.resolves(com.taku3.semiconductorlab.content.Optics.WIRE_GRATING_LINES, 589.0, 0.6), "wire grating (246 lines) cannot split the sodium D lines");
        h.assertTrue(com.taku3.semiconductorlab.item.SpectroscopeItem.resolves((int) Precision.RULED_GRATING_LINES, 589.0, 0.6), "ruled grating (30000 lines) splits them");
        h.assertFalse(com.taku3.semiconductorlab.item.SpectroscopeItem.resolves(com.taku3.semiconductorlab.content.Optics.WIRE_GRATING_LINES, 577.0, 2.1), "nor the mercury yellow pair with the wire grating");
        h.assertTrue(recipeUses(h, "ruling_engine", Precision.SURFACE_PLATE.asItem()) && recipeUses(h, "ruling_engine", Precision.LEAD_SCREW), "ruling engine = precise screw on a flat bed");
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.WATTS);
        boolean[] ghosts = new boolean[2];
        for (int k = 0; k < 2; k++) {
            BlockPos at = p(1 + 3 * k, 1, 1);
            if (k == 1) {   // 空調あり
                BlockPos ac = p(4, 1, 3);
                h.setBlock(ac, com.taku3.semiconductorlab.content.Construction.AIR_CONDITIONER.block);
                var acb = h.getBlockEntity(ac, com.taku3.semiconductorlab.block.AirConditionerBlockEntity.class);
                tickConsumer(h, ac, acb, 2, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.WATTS));
            }
            h.setBlock(at, Precision.RULING_ENGINE.block);
            var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.RulingEngineBlockEntity.class);
            be.getContainer().setItem(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.SLOT_BLANK, new ItemStack(com.taku3.semiconductorlab.content.Optics.SPECULUM_MIRROR));
            be.getContainer().setItem(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.SLOT_DIAMOND, new ItemStack(net.minecraft.world.item.Items.DIAMOND));
            be.getContainer().setItem(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.SLOT_SCREW, new ItemStack(Precision.LEAD_SCREW));
            if (k == 1) {
                var acb = h.getBlockEntity(p(4, 1, 3), com.taku3.semiconductorlab.block.AirConditionerBlockEntity.class);
                for (int t = 0; t < com.taku3.semiconductorlab.block.RulingEngineBlockEntity.TICKS + 5; t++) { tickConsumer(h, p(4, 1, 3), acb, 1, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.WATTS)); tickConsumer(h, at, be, 1, feed); }
            } else tickConsumer(h, at, be, com.taku3.semiconductorlab.block.RulingEngineBlockEntity.TICKS + 5, feed);
            ItemStack g = be.getContainer().getItem(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.SLOT_OUT);
            h.assertTrue(g.is(Precision.RULED_GRATING) && be.getContainer().getItem(com.taku3.semiconductorlab.block.RulingEngineBlockEntity.SLOT_DIAMOND).is(net.minecraft.world.item.Items.DIAMOND), "ruled grating, diamond kept");
            ghosts[k] = Boolean.TRUE.equals(g.get(Precision.GRATING_GHOSTS));
        }
        h.assertTrue(ghosts[0] && !ghosts[1], "without air conditioning (+-3 C) ghosts appear; with it, a clean grating");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"resolved", "merged", "ghosts"}) h.assertTrue(ja.has("item.semiconductor-lab.grating_spectroscope." + k), "lang " + k);
        LOG.info("[LAB] ruling_engine [8-12]: 600 lines/mm x 50 mm = 30000 -> D lines resolved (wire 246: merged); ghosts without air conditioning");
        h.succeed();
    }

    /** [8-18] 人工水晶（IMP2 O-016）：珪石×4＋NaOH＋種の水晶 → 水熱合成の仕込み → D の加圧加熱釜（280℃・15気圧以上⚠️）→ 人工水晶×3。圧力が足りないと仕込みのまま */
    @GameTest(maxTicks = 3000)
    public void cultured_quartz(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "hydrothermal_charge", Metallurgy.VEIN_QUARTZ) && recipeUses(h, "hydrothermal_charge", com.taku3.semiconductorlab.content.Gas.SODIUM_HYDROXIDE)
                && recipeUses(h, "hydrothermal_charge", Metallurgy.ROCK_CRYSTAL), "quartz scraps + NaOH (mineralizer) + seed crystal");
        h.assertTrue(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.find(new ItemStack(Precision.HYDROTHERMAL_CHARGE)) != null, "one row in D's autoclave table");
        BlockPos fp = p(1, 1, 1);
        h.setBlock(fp, com.taku3.semiconductorlab.content.Chemistry.AUTOCLAVE.block);
        var be = h.getBlockEntity(fp, com.taku3.semiconductorlab.block.AutoclaveBlockEntity.class);
        var c = be.getContainer();
        long feed = EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.WATTS);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, new ItemStack(Precision.HYDROTHERMAL_CHARGE));
        be.setTemperatureForTest(290);
        tickConsumer(h, fp, be, 20 * 40 + 5, feed);
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT).is(Precision.HYDROTHERMAL_CHARGE), "dry (1 atm) -> nothing grows, the charge comes back");
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_IN, new ItemStack(Precision.HYDROTHERMAL_CHARGE));
        c.setItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_WATER, new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
        be.setTemperatureForTest(290);
        tickConsumer(h, fp, be, 20 * 40 + 5, feed);
        ItemStack out = c.getItem(com.taku3.semiconductorlab.block.AutoclaveBlockEntity.SLOT_OUT);
        h.assertTrue(out.is(Precision.CULTURED_QUARTZ) && out.getCount() == 3, "hot water under pressure -> 3 cultured quartz, got " + out);
        h.assertTrue(recipeUses(h, "quartz_blank_cultured", Precision.CULTURED_QUARTZ), "cultured quartz cuts into blanks too");
        LOG.info("[LAB] cultured_quartz [8-18]: charge in D's autoclave at 290 C / " + Math.round(be.getPressureAtm()) + " atm -> 3 cultured quartz; dry -> charge back");
        h.succeed();
    }

    /** [8-15] 水晶振動子（IMP2 O-016）：水晶 → AT カットの水晶片（0.5mm＝3.32MHz）→ ルージュで磨くと振動数が上がる → 銀の電極で振動子。膜厚計（ザウアーブレイ） */
    @GameTest
    public void crystal_resonator(GameTestHelper h) {
        var lv = h.getLevel();
        var silk = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
        silk.enchant(lv.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
        var pick = new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
        int rc = 0, rcSilk = 0;
        for (int i = 0; i < 400; i++) {
            rc += net.minecraft.world.level.block.Block.getDrops(Metallurgy.QUARTZ_VEIN.defaultBlockState(), lv, h.absolutePos(p(1, 1, 1)), null, null, pick).stream().filter(s -> s.is(Metallurgy.ROCK_CRYSTAL)).count();
            rcSilk += net.minecraft.world.level.block.Block.getDrops(Metallurgy.QUARTZ_VEIN.defaultBlockState(), lv, h.absolutePos(p(1, 1, 1)), null, null, silk).stream().filter(s -> s.is(Metallurgy.ROCK_CRYSTAL)).count();
        }
        h.assertTrue(rc > 2 && rc < 40 && rcSilk == 0, "rock crystal is a rare drop from quartz veins (" + rc + "/400), never with silk touch");
        h.assertTrue(recipeUses(h, "quartz_blank", Precision.DIAMOND_SAW), "cut with a diamond saw");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.QuartzBlankItem.freqMHz(500) - 3.322) < 0.001, "AT-cut 0.5 mm -> 3.32 MHz");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack b = new ItemStack(Precision.QUARTZ_BLANK);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, b);
        h.assertFalse(Precision.QUARTZ_BLANK.use(lv, player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "no rouge -> cannot lap");
        player.getInventory().add(new ItemStack(com.taku3.semiconductorlab.content.Optics.ROUGE, 5));
        for (int i = 0; i < 5; i++) Precision.QUARTZ_BLANK.use(lv, player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertValueEqual(com.taku3.semiconductorlab.item.QuartzBlankItem.thicknessUm(b), 400, "5 laps of 20 um -> 0.4 mm");
        h.assertTrue(com.taku3.semiconductorlab.item.QuartzBlankItem.freqMHz(400) > com.taku3.semiconductorlab.item.QuartzBlankItem.freqMHz(500), "thinner -> higher frequency (4.15 MHz)");
        var in = net.minecraft.world.item.crafting.CraftingInput.of(2, 1, java.util.List.of(b, new ItemStack(Metallurgy.SILVER_NUGGET)));
        ItemStack res = lv.getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, lv).map(r -> r.value().assemble(in)).orElse(ItemStack.EMPTY);
        h.assertTrue(res.is(Precision.CRYSTAL_RESONATOR) && com.taku3.semiconductorlab.item.QuartzBlankItem.thicknessUm(res) == 400, "silver electrodes -> resonator keeps the thickness");
        double df = com.taku3.semiconductorlab.item.CrystalResonatorItem.sauerbreyHz(5e6, 100);
        h.assertTrue(df > 5000 && df < 7000, "Sauerbrey: 100 nm of silver on 5 MHz -> about 5.9 kHz down: " + df);
        var ja = langJson("ja_jp");
        for (String k : new String[] {"lapped", "too_thin", "spec", "beats", "monitor"}) h.assertTrue(ja.has("item.semiconductor-lab.quartz_blank." + k), "lang quartz_blank." + k);
        LOG.info("[LAB] crystal_resonator [8-15]: rock crystal " + rc + "/400 from veins, 0.5 mm 3.32 MHz -> lapped 0.4 mm 4.15 MHz, Sauerbrey 100 nm Ag on 5 MHz = " + Math.round(df) + " Hz");
        h.succeed();
    }

    /** [8-08] ボールねじ（IMP2 O-014）：送りねじ＋鋼球。旋盤の親ねじにすると、段階ごとに部品の誤差が小さく・電気が少なく。送りの細かさ 10 → 5・2・1µm */
    @GameTest
    public void ball_screw(GameTestHelper h) {
        var B = com.taku3.semiconductorlab.item.BallScrewItem.class;
        h.assertTrue(recipeUses(h, "ball_screw_3", Precision.LEAD_SCREW) && recipeUses(h, "ball_screw_3", Precision.STEEL_BALLS_PRECISE), "ball screw = lead screw + steel balls");
        h.assertValueEqual(com.taku3.semiconductorlab.item.BallScrewItem.feedUm(new ItemStack(Precision.LEAD_SCREW)), 10, "plain lead screw: 10 um (stick-slip)");
        int[] feed = new int[4]; int[] err = new int[4]; double[] watts = new double[4];
        for (int g = 0; g <= 3; g++) {
            ItemStack screw = g == 0 ? new ItemStack(Precision.LEAD_SCREW) : com.taku3.semiconductorlab.item.BallBearingItem.of(g).transmuteCopy(Precision.BALL_SCREW);
            feed[g] = com.taku3.semiconductorlab.item.BallScrewItem.feedUm(screw);
            BlockPos lp = p(1 + g, 1, 1);
            h.setBlock(lp, Precision.LATHE.block);
            var lb = h.getBlockEntity(lp, LatheBlockEntity.class);
            lb.calibrate(20);
            lb.getContainer().setItem(LatheBlockEntity.SLOT_IN, new ItemStack(Metallurgy.STEEL_INGOT));
            lb.getContainer().setItem(LatheBlockEntity.SLOT_TOOL, screw);
            tickConsumer(h, lp, lb, 1, EnergyStorage.wattsToJoulesPerTick(LatheBlockEntity.WATTS));
            watts[g] = lb.getLastWatts();
            tickConsumer(h, lp, lb, LatheBlockEntity.TICKS + 5, EnergyStorage.wattsToJoulesPerTick(LatheBlockEntity.WATTS));
            ItemStack out = lb.getContainer().getItem(LatheBlockEntity.SLOT_OUT);
            h.assertTrue(out.is(Precision.STANDARD_SCREW), "grade " + g + " makes standard screws");
            err[g] = out.getOrDefault(Precision.PART_ERROR_UM, 999);
        }
        h.assertTrue(feed[0] == 10 && feed[1] == 5 && feed[2] == 2 && feed[3] == 1, "feed " + java.util.Arrays.toString(feed));
        h.assertTrue(err[0] == 20 && err[1] == 10 && err[2] == 5 && err[3] == 2, "part error with a lathe 20 um off: " + java.util.Arrays.toString(err));
        h.assertTrue(watts[0] > watts[1] && watts[1] > watts[2] && watts[2] > watts[3], "power drops by grade: " + java.util.Arrays.toString(watts));
        var ja = langJson("ja_jp");
        h.assertTrue(ja.has("item.semiconductor-lab.ball_screw.feed"), "lang ball_screw.feed");
        LOG.info("[LAB] ball_screw [8-08]: feed 10/5/2/1 um, part error 20/10/5/2 um, lathe power " + java.util.Arrays.toString(watts));
        h.succeed();
    }

    /** [8-31] 研削盤・[8-20] 玉軸受（IMP2 O-013）：測る道具で玉の段階が決まり、回る機械に入れると段階ごとに消費電力が下がる（炉には入らない） */
    @GameTest
    public void ball_bearings(GameTestHelper h) {
        var G = com.taku3.semiconductorlab.block.GrinderBlockEntity.class;
        h.assertTrue(recipeUses(h, "steel_balls_rough", Metallurgy.STEEL_INGOT) && recipeUses(h, "raw_grinding_wheel", Metallurgy.ALUMINA), "rough balls from steel, wheel from alumina + clay");
        h.assertTrue(com.taku3.semiconductorlab.block.GrinderBlockEntity.product(ItemStack.EMPTY).isEmpty(), "no gauge -> nothing");
        h.assertTrue(com.taku3.semiconductorlab.block.GrinderBlockEntity.product(new ItemStack(Precision.VERNIER_CALIPER)).isEmpty(), "vernier caliper too coarse");
        h.assertTrue(com.taku3.semiconductorlab.block.GrinderBlockEntity.product(new ItemStack(Precision.MICROMETER)).is(Precision.STEEL_BALLS_GROUND), "micrometer -> ground balls");
        h.assertTrue(com.taku3.semiconductorlab.block.GrinderBlockEntity.product(new ItemStack(Precision.GAUGE_BLOCK_SET)).is(Precision.STEEL_BALLS_PRECISE), "gauge blocks -> precise balls");
        // 研削盤を回す
        BlockPos gp = p(1, 1, 1);
        h.setBlock(gp, Precision.GRINDER.block);
        var gb = h.getBlockEntity(gp, com.taku3.semiconductorlab.block.GrinderBlockEntity.class);
        gb.getContainer().setItem(com.taku3.semiconductorlab.block.GrinderBlockEntity.SLOT_IN, new ItemStack(Precision.STEEL_BALLS_ROUGH));
        gb.getContainer().setItem(com.taku3.semiconductorlab.block.GrinderBlockEntity.SLOT_WHEEL, new ItemStack(Precision.GRINDING_WHEEL));
        gb.getContainer().setItem(com.taku3.semiconductorlab.block.GrinderBlockEntity.SLOT_GAUGE, new ItemStack(Precision.GAUGE_BLOCK_SET));
        tickConsumer(h, gp, gb, com.taku3.semiconductorlab.block.GrinderBlockEntity.TICKS + 5, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.GrinderBlockEntity.WATTS));
        h.assertTrue(gb.getContainer().getItem(com.taku3.semiconductorlab.block.GrinderBlockEntity.SLOT_OUT).is(Precision.STEEL_BALLS_PRECISE), "grinder makes precise balls");
        // 玉軸受のレシピで段階が付く
        int[] got = new int[4];
        for (int g = 1; g <= 3; g++) {
            var in = net.minecraft.world.item.crafting.CraftingInput.of(3, 1, java.util.List.of(new ItemStack(Metallurgy.STEEL_INGOT), new ItemStack(Metallurgy.STEEL_INGOT),
                    new ItemStack(g == 1 ? Precision.STEEL_BALLS_ROUGH : g == 2 ? Precision.STEEL_BALLS_GROUND : Precision.STEEL_BALLS_PRECISE)));
            ItemStack b = h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING, in, h.getLevel())
                    .map(r -> r.value().assemble(in)).orElse(ItemStack.EMPTY);
            got[g] = com.taku3.semiconductorlab.item.BallBearingItem.grade(b);
        }
        h.assertTrue(got[1] == 1 && got[2] == 2 && got[3] == 3, "bearing grades from the recipes: " + java.util.Arrays.toString(got));
        // 旋盤：段階ごとに消費電力が下がる（仕事の時間は同じ）
        double prev = Double.MAX_VALUE;
        for (int g = 0; g <= 3; g++) {
            BlockPos lp = p(3 + g, 1, 3);
            h.setBlock(lp, Precision.LATHE.block);
            var lb = h.getBlockEntity(lp, LatheBlockEntity.class);
            if (g > 0) h.assertTrue(lb.installBearing(g), "lathe takes a bearing");
            lb.getContainer().setItem(LatheBlockEntity.SLOT_IN, new ItemStack(Metallurgy.STEEL_INGOT));
            tickConsumer(h, lp, lb, 1, EnergyStorage.wattsToJoulesPerTick(LatheBlockEntity.WATTS));
            double w = lb.getLastWatts();
            h.assertTrue(Math.abs(w - LatheBlockEntity.WATTS * com.taku3.semiconductorlab.item.BallBearingItem.powerFactor(g)) < 1e-6 && w < prev, "grade " + g + ": " + w + " W");
            prev = w;
        }
        // 炉には入らない
        h.setBlock(p(1, 1, 5), com.taku3.semiconductorlab.content.Power.ELECTRIC_FURNACE.block);
        var furnace = (com.taku3.semiconductorlab.energy.ConsumerBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(p(1, 1, 5)));
        h.assertFalse(furnace.acceptsBearing() || furnace.installBearing(3), "a furnace has no rotating shaft");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"grade1", "grade2", "grade3", "factor", "installed", "not_rotating"})
            h.assertTrue(ja.has("item.semiconductor-lab.ball_bearing." + k), "lang ball_bearing." + k);
        LOG.info("[LAB] ball_bearings [8-20][8-31]: grinder + gauge blocks -> precise balls; lathe 750 -> 712.5 / 637.5 / 525 W by grade; furnace refuses");
        h.succeed();
    }

    /** [8-06] 光波干渉測長（IMP2 O-012）：干渉計で縞を数え、ゲージブロック・メートル原器の長さを光の波長で測る。揺れていると数えられない */
    @GameTest
    public void light_wave_length(GameTestHelper h) {
        h.assertValueEqual(Math.round(com.taku3.semiconductorlab.item.InterferometerItem.wavelengths(1000, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM)), 1_831_166L, "1 m = 1,831,166 waves of mercury green");
        long fr = Math.round(com.taku3.semiconductorlab.item.InterferometerItem.fringes(37.415, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM));
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.InterferometerItem.lengthMm(fr, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM) - 37.415) < 0.0003, "37.415 mm gauge block -> " + fr + " fringes -> back within 0.3 um");
        h.assertTrue(com.taku3.semiconductorlab.item.InterferometerItem.lengthMm(1, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM) * 1e6 < com.taku3.semiconductorlab.item.InterferometerItem.GAUGE_ERR_NM * 2, "one fringe (273 nm) is finer than the gauge block error");
        // 点灯した水銀灯＋左手にメートル原器 / ゲージブロック：メッセージが出る（エラーなく動く）
        BlockPos lamp = p(1, 1, 1);
        h.setBlock(lamp, com.taku3.semiconductorlab.content.Optics.MERCURY_LAMP.block.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true));
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.taku3.semiconductorlab.content.Optics.INTERFEROMETER));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(lamp)), net.minecraft.core.Direction.UP, h.absolutePos(lamp), false);
        for (var other : new net.minecraft.world.item.Item[] {Precision.METRE_BAR, Precision.GAUGE_BLOCK_SET}) {
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(other));
            h.assertTrue(com.taku3.semiconductorlab.content.Optics.INTERFEROMETER.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit)).consumesAction(),
                    "interferometer measures " + other);
        }
        var ja = langJson("ja_jp");
        for (String k : new String[] {"measure_metre", "measure_gauge", "measure_shaky"})
            h.assertTrue(ja.has("item.semiconductor-lab.interferometer." + k), "lang interferometer." + k);
        LOG.info("[LAB] light_wave_length [8-06]: 1 m = 1,831,166 Hg-green waves; 37.415 mm gauge = " + fr + " fringes");
        h.succeed();
    }

    /** [8-05] ゲージブロック（IMP2 O-010）：焼入れしたるつぼ鋼。何個かを重ねて好きな長さ、マイクロメータと比べて旋盤のくせを ±1µm に */
    @GameTest
    public void gauge_blocks(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "gauge_block_set", Metallurgy.CRUCIBLE_STEEL) && recipeUses(h, "gauge_block_set", com.taku3.semiconductorlab.content.Optics.ROUGE),
                "gauge blocks = hardened crucible steel lapped with rouge");
        var G = com.taku3.semiconductorlab.item.GaugeBlockSetItem.class;
        for (int t : new int[] {3_000, 37_415, 41_009, 99_999, 150_000, 12_345, 3_499}) {
            var c = com.taku3.semiconductorlab.item.GaugeBlockSetItem.combine(t);
            h.assertTrue(!c.isEmpty() && c.stream().mapToInt(Integer::intValue).sum() == t && c.size() <= 6 && c.stream().distinct().count() == c.size(),
                    "combine " + t + " um -> " + c);
        }
        h.assertValueEqual(com.taku3.semiconductorlab.item.GaugeBlockSetItem.combine(37_415), java.util.List.of(1_005, 1_410, 25_000, 10_000), "37.415 = 1.005 + 1.41 + 25 + 10");
        // 旋盤を合わせる：マイクロメータを持っていないと断る、持っていれば ±1µm
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Precision.LATHE.block);
        var be = h.getBlockEntity(pos, LatheBlockEntity.class);
        be.calibrate(22);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Precision.GAUGE_BLOCK_SET));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(pos)), net.minecraft.core.Direction.UP, h.absolutePos(pos), false);
        Precision.GAUGE_BLOCK_SET.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        h.assertValueEqual(be.errorUm(), 22, "without a micrometer -> not calibrated");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Precision.VERNIER_CALIPER));
        Precision.GAUGE_BLOCK_SET.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        h.assertValueEqual(be.errorUm(), 22, "a vernier caliper (50 um) is too coarse");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Precision.MICROMETER));
        Precision.GAUGE_BLOCK_SET.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        h.assertTrue(Math.abs(be.errorUm()) <= com.taku3.semiconductorlab.item.GaugeBlockSetItem.CALIBRATED_UM, "micrometer + gauge blocks -> lathe within 1 um: " + be.errorUm());
        var ja = langJson("ja_jp");
        for (String k : new String[] {"combine", "need_micrometer", "calibrated"})
            h.assertTrue(ja.has("item.semiconductor-lab.gauge_block_set." + k), "lang gauge_block_set." + k);
        LOG.info("[LAB] gauge_blocks [8-05]: 37.415 mm = 1.005 + 1.41 + 25 + 10 (wrung), lathe +22 um -> " + be.errorUm() + " um with micrometer");
        h.succeed();
    }

    /** [8-14] 鏡筒の心出し（IMP2 O-010）：顕微鏡で像の揺れを見て、いちばんずれたレンズを寄せる。複式顕微鏡なら全部 1µm 以下、単レンズでは見えずに止まる */
    @GameTest
    public void lens_centering(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "lens_barrel", com.taku3.semiconductorlab.content.Optics.ACHROMATIC_LENS) && recipeUses(h, "lens_barrel", Precision.STANDARD_SCREW),
                "lens barrel = 4 achromatic lenses + steel tube + screw rings");
        var L = com.taku3.semiconductorlab.item.LensBarrelItem.class;
        int compound = com.taku3.semiconductorlab.item.LensBarrelItem.seeLimit(0.9, 0), simple = com.taku3.semiconductorlab.item.LensBarrelItem.seeLimit(0.2, 0);
        h.assertTrue(compound < 10 && simple > 10, "compound microscope sees below 1 um, simple one does not: " + compound + ", " + simple);
        var d = new java.util.ArrayList<>(java.util.List.of(800, 600, 300, 200));
        int n = 0;
        while (!com.taku3.semiconductorlab.item.LensBarrelItem.centered(d) && n < 30) { com.taku3.semiconductorlab.item.LensBarrelItem.tap(d, compound); n++; }
        h.assertTrue(com.taku3.semiconductorlab.item.LensBarrelItem.centered(d), "centered with the compound microscope after " + n + " taps");
        var e = new java.util.ArrayList<>(java.util.List.of(800, 600, 300, 200));
        for (int k = 0; k < 40; k++) com.taku3.semiconductorlab.item.LensBarrelItem.tap(e, simple);
        h.assertFalse(com.taku3.semiconductorlab.item.LensBarrelItem.centered(e), "simple microscope stops at about 1.4 um");
        h.assertTrue(com.taku3.semiconductorlab.item.LensBarrelItem.seeLimit(0.9, com.taku3.semiconductorlab.block.Vibration.PISTON_NM) > 10, "a moving piston next to you hides the wobble");
        // 手に持って：顕微鏡なしでは何もしない、あると1回寄せる
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Precision.LENS_BARREL));
        h.assertFalse(Precision.LENS_BARREL.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "no microscope -> nothing");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Precision.COMPOUND_MICROSCOPE));
        for (int k = 0; k < 30 && !player.getMainHandItem().is(Precision.CENTERED_LENS_BARREL); k++)
            Precision.LENS_BARREL.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().is(Precision.CENTERED_LENS_BARREL) && player.getMainHandItem().has(Precision.LENS_DECENTER), "tapping until centered -> centered lens barrel");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"hold", "wobble", "cannot_see", "tapped", "done", "unknown", "decenter"})
            h.assertTrue(ja.has("item.semiconductor-lab.lens_barrel." + k), "lang lens_barrel." + k);
        LOG.info("[LAB] lens_centering [8-14]: compound microscope sees " + compound / 10.0 + " um -> centered in " + n + " taps; simple " + simple / 10.0 + " um cannot finish");
        h.succeed();
    }

    /** [8-07] 除振（IMP2）：まわりで動く物・ピストンで床が揺れ、干渉縞がゆれる。除振台（定盤＋ゴム）の上なら 1/20 */
    @GameTest
    public void vibration_isolation(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "isolation_table", Precision.SURFACE_PLATE.asItem()) && recipeUses(h, "isolation_table", com.taku3.semiconductorlab.content.Chemistry.VULCANIZED_RUBBER),
                "isolation table = surface plate on rubber");
        var V = com.taku3.semiconductorlab.block.Vibration.class;
        BlockPos at = p(1, 2, 1);
        h.assertTrue(com.taku3.semiconductorlab.block.Vibration.nm(h.getLevel(), h.absolutePos(at), null) < 1, "quiet floor");
        var pig = h.spawn(net.minecraft.world.entity.EntityTypes.PIG, p(3, 1, 1));
        pig.setDeltaMovement(0.2, 0, 0);
        double floor = com.taku3.semiconductorlab.block.Vibration.nm(h.getLevel(), h.absolutePos(at), null);
        h.assertTrue(floor > 100, "a walking animal 2 m away shakes the floor: " + floor + " nm");
        double j = com.taku3.semiconductorlab.block.Vibration.fringeJitter(floor, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM);
        h.assertTrue(j >= 0.5, "fringes jitter by half a fringe or more -> cannot count: " + j);
        h.setBlock(at.below(), Precision.ISOLATION_TABLE);
        double table = com.taku3.semiconductorlab.block.Vibration.nm(h.getLevel(), h.absolutePos(at), null);
        h.assertTrue(Math.abs(table * com.taku3.semiconductorlab.block.Vibration.TABLE_FACTOR - floor) < 1e-6, "on the isolation table: 1/20");
        h.assertTrue(com.taku3.semiconductorlab.block.Vibration.fringeJitter(table, com.taku3.semiconductorlab.item.InterferometerItem.HG_GREEN_NM) < 0.5, "steady fringes on the table");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"vibration", "steady", "shaky", "on_table", "on_floor"})
            h.assertTrue(ja.has("item.semiconductor-lab.interferometer." + k), "lang interferometer." + k);
        pig.discard();
        LOG.info("[LAB] vibration_isolation [8-07]: walking pig " + Math.round(floor) + " nm -> jitter " + String.format("%.2f", j) + " fringes; isolation table 1/20 -> steady");
        h.succeed();
    }

    /** [8-03] 平面と定盤（IMP2 O-009）：2枚だけでは凸と凹で合ってしまう。3枚を順にすり合わせると、基準なしに3枚とも平面（定盤）になる */
    @GameTest
    public void surface_plates(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "cast_iron_plate", Metallurgy.PIG_IRON), "cast iron plate from pig iron [1-09]");
        // 2枚だけ：合い（a＋b）は 0 に近づくが、+20µm と -20µm のまま
        int a = 300, b = -100;
        for (int i = 0; i < 20; i++) { int[] r = com.taku3.semiconductorlab.item.CastPlateItem.lap(a, b); a = r[0]; b = r[1]; }
        h.assertTrue(Math.abs(a + b) <= 1, "two plates fit each other: gap " + (a + b));
        h.assertTrue(Math.abs(a) >= 150 && Math.abs(b) >= 150, "but one stays convex and one concave: " + a + ", " + b);
        // 3枚：A-B・B-C・C-A の順にすり合わせると、3枚とも 1µm 以下
        int[] p3 = {400, -250, 550};
        int n = 0;
        while (n < 40 && (Math.abs(p3[0]) > 10 || Math.abs(p3[1]) > 10 || Math.abs(p3[2]) > 10)) {
            int i = n % 3, j = (n + 1) % 3;
            int[] r = com.taku3.semiconductorlab.item.CastPlateItem.lap(p3[i], p3[j]);
            p3[i] = r[0]; p3[j] = r[1]; n++;
        }
        h.assertTrue(n < 40, "three plates all flat within 1 um after " + n + " laps");
        h.assertTrue(com.taku3.semiconductorlab.item.CastPlateItem.withCurve(5).is(Precision.SURFACE_PLATE.asItem()), "1 um or less -> surface plate");
        h.assertTrue(com.taku3.semiconductorlab.item.CastPlateItem.withCurve(-50).is(Precision.CAST_IRON_PLATE), "5 um concave -> still a cast plate");
        // 手に持ってすり合わせる：ルージュを1つ使う
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Precision.CAST_IRON_PLATE));
        h.assertFalse(Precision.CAST_IRON_PLATE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "one plate alone -> nothing");
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Precision.CAST_IRON_PLATE));
        h.assertFalse(Precision.CAST_IRON_PLATE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "no rouge -> nothing");
        player.getInventory().add(new ItemStack(com.taku3.semiconductorlab.content.Optics.ROUGE, 2));
        h.assertTrue(Precision.CAST_IRON_PLATE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "two plates + rouge -> lapped");
        h.assertTrue(player.getMainHandItem().has(Precision.PLATE_CURVE) && player.getOffhandItem().has(Precision.PLATE_CURVE), "both plates now have a measured curve");
        h.assertValueEqual(player.getInventory().countItem(com.taku3.semiconductorlab.content.Optics.ROUGE), 1, "one rouge used");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"hold", "no_rouge", "lapped", "unknown", "curve", "flat", "convex", "concave"})
            h.assertTrue(ja.has("item.semiconductor-lab.cast_iron_plate." + k), "lang cast_iron_plate." + k);
        LOG.info("[LAB] surface_plates [8-03]: two plates stay convex/concave, three plates flat within 1 um after " + n + " laps, rouge used");
        h.succeed();
    }

    /** [8-22] メートル原器（IMP2 O-007）：白金×3＋セオドライト（戻る）。2つのブロックを右クリックして長さを m・尺・トワーズで */
    @GameTest
    public void metre_bar(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "metre_bar", Metallurgy.PLATINUM_INGOT) && recipeUses(h, "metre_bar", com.taku3.semiconductorlab.content.Construction.THEODOLITE),
                "metre bar = platinum + the meridian survey (theodolite)");
        var rem = com.taku3.semiconductorlab.content.Construction.THEODOLITE.getCraftingRemainder(new ItemStack(com.taku3.semiconductorlab.content.Construction.THEODOLITE));
        h.assertTrue(rem != null && rem.item().value() == com.taku3.semiconductorlab.content.Construction.THEODOLITE, "theodolite comes back from the crafting grid");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.MetreBarItem.length(new BlockPos(0, 0, 0), new BlockPos(3, 0, 4)) - 5) < 1e-9, "3-4-5: 5.000 m");
        h.assertTrue(Math.abs(33 * com.taku3.semiconductorlab.item.MetreBarItem.SHAKU_M - 10) < 1e-9, "33 shaku = 10 m (1891)");
        // 使う：1つ目の印 → 2つ目で長さを出して印を消す
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack bar = new ItemStack(Precision.METRE_BAR);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bar);
        BlockPos a = h.absolutePos(p(0, 1, 0)), b = h.absolutePos(p(0, 1, 2));
        h.setBlock(p(0, 1, 0), net.minecraft.world.level.block.Blocks.STONE);
        h.setBlock(p(0, 1, 2), net.minecraft.world.level.block.Blocks.STONE);
        var hitA = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(a), net.minecraft.core.Direction.UP, a, false);
        var hitB = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(b), net.minecraft.core.Direction.UP, b, false);
        Precision.METRE_BAR.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hitA));
        h.assertTrue(a.equals(bar.get(Precision.METRE_MARK)), "first mark recorded");
        Precision.METRE_BAR.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hitB));
        h.assertFalse(bar.has(Precision.METRE_MARK), "second click measures and clears the mark");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"mark", "length", "placeholder"})
            h.assertTrue(ja.has("item.semiconductor-lab.metre_bar." + k), "lang metre_bar." + k);
        LOG.info("[LAB] metre_bar [8-22]: platinum + theodolite (returned), 3-4-5 = 5.000 m, 33 shaku = 10 m, mark -> measure");
        h.succeed();
    }

    /** [8-01] 旋盤（IMP2-01）：鋼 → ポンプのローター（750W×30秒）。電気が半分では進まない、鉄（錬鉄）は削らない */
    @GameTest
    public void lathe(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "lathe", Precision.LEAD_SCREW) && recipeUses(h, "lathe", Power.ELECTRIC_MOTOR) && recipeUses(h, "lathe", Metallurgy.STEEL_INGOT),
                "lathe = steel bed + lead screw (tool carriage) + motor");
        BlockPos pos = p(1, 1, 1);
        h.setBlock(pos, Precision.LATHE.block);
        var be = h.getBlockEntity(pos, LatheBlockEntity.class);
        var c = be.getContainer();
        int T = LatheBlockEntity.TICKS;
        long full = EnergyStorage.wattsToJoulesPerTick(LatheBlockEntity.WATTS);
        h.assertFalse(LatheBlockEntity.isInput(new ItemStack(Items.IRON_INGOT)), "iron is not turned into a rotor (needs steel)");
        int err = be.errorUm();
        h.assertTrue(Math.abs(err) <= LatheBlockEntity.ERROR_UM, "lathe error within +-25 um: " + err);
        c.setItem(LatheBlockEntity.SLOT_IN, new ItemStack(Metallurgy.STEEL_INGOT, 2));
        tickConsumer(h, pos, be, T, full / 2);
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_OUT).isEmpty(), "half power -> no progress");
        tickConsumer(h, pos, be, T, full);
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_OUT).is(Precision.PUMP_ROTOR), "steel -> pump rotor in 30 s at 750 W");
        h.assertValueEqual(c.getItem(LatheBlockEntity.SLOT_OUT).get(Precision.PART_ERROR_UM), err, "the part carries this lathe's error");
        // [8-02] 親ねじを入れると、鋼 → 規格ねじ×4（親ねじは減らない）
        c.setItem(LatheBlockEntity.SLOT_OUT, ItemStack.EMPTY);
        c.setItem(LatheBlockEntity.SLOT_TOOL, new ItemStack(Precision.LEAD_SCREW));
        tickConsumer(h, pos, be, T, full);
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_OUT).is(Precision.STANDARD_SCREW) && c.getItem(LatheBlockEntity.SLOT_OUT).getCount() == 4, "lead screw as master -> 4 standard screws");
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_TOOL).is(Precision.LEAD_SCREW), "the master screw stays");
        tickConsumer(h, pos, be, T, full);
        h.assertValueEqual(c.getItem(LatheBlockEntity.SLOT_OUT).getCount(), 4, "out of steel (2 used) -> stays at 4");
        LOG.info("[LAB] lathe [8-01][8-02]: steel -> pump rotor; with the lead screw as master -> 4 standard screws (error {} um)", err);
        h.succeed();
    }

    /** [8-10] 光学顕微鏡（IMP2-04）：d ＝ 550nm ÷ 2NA。単レンズ（NA 0.2 → 1.4µm）は密着（10µm）・等倍投影（3µm）のマスクは見え、g線ステッパー（1µm）は見えない。
     *  複式（NA 0.9 → 0.31µm）は i線（0.35µm）まで見え、KrF（0.13µm）は見えない。マスク・設計図・加工中のウェハーの方眼を読む */
    @GameTest
    public void microscope(GameTestHelper h) {
        var simple = (com.taku3.semiconductorlab.item.MicroscopeItem) Precision.SIMPLE_MICROSCOPE;
        var compound = (com.taku3.semiconductorlab.item.MicroscopeItem) Precision.COMPOUND_MICROSCOPE;
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.MicroscopeItem.resolutionNm(0.2) - 1375) < 1, "simple: 550/(2x0.2) = 1375 nm");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.MicroscopeItem.resolutionNm(0.9) - 305.6) < 1, "compound: 550/(2x0.9) = 306 nm");
        var T = com.taku3.semiconductorlab.litho.ExposureTool.class;
        h.assertTrue(simple.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.CONTACT_G.grid) && simple.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.PROJECTION_1X.grid), "simple sees 10 um and 3 um masks");
        h.assertFalse(simple.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_G.grid), "simple cannot see 1 um lines");
        h.assertTrue(compound.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.STEPPER_I.grid), "compound sees i-line 0.35 um");
        h.assertFalse(compound.canResolve(com.taku3.semiconductorlab.litho.ExposureTool.KRF.grid), "compound cannot see KrF 0.13 um (shorter light or electrons needed)");
        h.assertFalse(simple.canResolve(0), "nothing to look at");
        // 読む物：マスク成分・設計図・加工中のウェハー
        var design = com.taku3.semiconductorlab.litho.LithoExamples.notGate();
        ItemStack mask = new ItemStack(Items.PAPER);
        mask.set(com.taku3.semiconductorlab.ModDataComponents.MASK, design.mask(0));
        h.assertValueEqual(com.taku3.semiconductorlab.item.MicroscopeItem.sizeOf(mask), 8, "reads the mask grid size");
        h.assertValueEqual(com.taku3.semiconductorlab.item.MicroscopeItem.sizeOf(new ItemStack(Items.STONE)), 0, "stone is not a pattern");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.MicroscopeItem.cellNm(20) - 10_000 * Math.pow(16.0 / 20, 3)) < 1e-6, "grid not in the table -> inverse of the N formula (A S-008: factor 16)");
        // 使う：手に顕微鏡、もう片方にマスク → SUCCESS
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Precision.COMPOUND_MICROSCOPE));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, mask);
        h.assertTrue(Precision.COMPOUND_MICROSCOPE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "looking works");
        h.assertTrue(recipeUses(h, "compound_microscope", com.taku3.semiconductorlab.content.Optics.ACHROMATIC_LENS), "compound = achromatic objective + eyepiece");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"limit", "hold", "nothing", "cell", "sharp", "blurred", "scratches"}) h.assertTrue(ja.has("item.semiconductor-lab.microscope." + k), "lang " + k);
        LOG.info("[LAB] microscope [8-10]: simple NA 0.2 -> 1.4 um (sees 10/3 um masks), compound NA 0.9 -> 0.31 um (sees i-line, not KrF)");
        h.succeed();
    }

    /** [8-04] ノギスとマイクロメータ（IMP2-05）：ノギスは 0.05mm 読みで等級を決められない、マイクロメータは 0.01mm 読みで A/B/C。ねじ1回転 0.5mm ÷ 50 目盛り＝0.01mm */
    @GameTest
    public void calipersAndMicrometer(GameTestHelper h) {
        var cal = (com.taku3.semiconductorlab.item.GaugeItem) Precision.VERNIER_CALIPER;
        var mic = (com.taku3.semiconductorlab.item.GaugeItem) Precision.MICROMETER;
        h.assertValueEqual(mic.resolutionUm, (int) Math.round(com.taku3.semiconductorlab.item.InterferometerItem.SCREW_MM * 1000 / 50), "micrometer: 0.5 mm per turn / 50 divisions = 10 um (same screw pitch as the interferometer)");
        h.assertTrue(Math.abs(cal.readMm(40.0, 18) - 40.00) < 1e-9, "caliper reads 40.018 as 40.00 (0.05 mm steps)");
        h.assertTrue(Math.abs(mic.readMm(40.0, 18) - 40.02) < 1e-9, "micrometer reads 40.018 as 40.02");
        h.assertTrue(cal.grade(18) == null, "caliper cannot grade 10-20 um differences");
        h.assertValueEqual(mic.grade(-7), "A", "7 um -> A");
        h.assertValueEqual(mic.grade(18), "B", "18 um -> B");
        h.assertValueEqual(mic.grade(25), "C", "25 um -> C");
        h.assertTrue(recipeUses(h, "micrometer", Precision.STANDARD_SCREW), "micrometer needs a standard screw (1848 after screw-cutting)");
        h.assertTrue(recipeUses(h, "vernier_caliper", com.taku3.semiconductorlab.content.Metallurgy.BRONZE_INGOT), "caliper is brass/bronze (1631 vernier)");
        // 使う：もう片方の手に旋盤の部品 → SUCCESS
        ItemStack screw = new ItemStack(Precision.STANDARD_SCREW);
        screw.set(Precision.PART_ERROR_UM, 12);
        h.assertTrue(com.taku3.semiconductorlab.item.GaugeItem.nominalMm(screw) == 10.0, "standard screw nominal 10 mm");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Precision.MICROMETER));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, screw);
        h.assertTrue(Precision.MICROMETER.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "measuring works");
        var ja = langJson("ja_jp");
        for (String k : new String[] {"hold", "read", "no_grade", "grade_a", "grade_b", "grade_c"}) h.assertTrue(ja.has("item.semiconductor-lab.gauge." + k), "lang " + k);
        LOG.info("[LAB] gauges [8-04]: caliper 0.05 mm (no grade), micrometer 0.01 mm grades A<=10/B<=20/C um");
        h.succeed();
    }

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    /** [8-23] キログラム原器（IMP2 O-020）：白金＋ベンガラ → 原器。2つを比べると差が µg で出て、表面の汚れで年とともに広がる */
    @GameTest
    public void kilogram_prototype(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "kilogram_prototype", Metallurgy.PLATINUM_INGOT), "the kilogram is a platinum cylinder");
        ItemStack a = new ItemStack(Precision.KILOGRAM_PROTOTYPE), b = new ItemStack(Precision.KILOGRAM_PROTOTYPE);
        com.taku3.semiconductorlab.item.KilogramPrototypeItem.set(a, 10, 0, 0);    // 国際原器：汚れにくい
        com.taku3.semiconductorlab.item.KilogramPrototypeItem.set(b, -5, 2, 0);    // 各国の複製：1日 2µg ずつ重くなる
        h.assertValueEqual(com.taku3.semiconductorlab.item.KilogramPrototypeItem.massUg(a, 0) - com.taku3.semiconductorlab.item.KilogramPrototypeItem.massUg(b, 0), 15L, "freshly made: 15 ug apart");
        h.assertValueEqual(com.taku3.semiconductorlab.item.KilogramPrototypeItem.massUg(a, 30) - com.taku3.semiconductorlab.item.KilogramPrototypeItem.massUg(b, 30), -45L, "30 days later the copy has gained 60 ug");
        ItemStack fresh = new ItemStack(Precision.KILOGRAM_PROTOTYPE);
        com.taku3.semiconductorlab.item.KilogramPrototypeItem.ensure(fresh, h.getLevel());
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.KilogramPrototypeItem.massUg(fresh, h.getLevel().getGameTime() / com.taku3.semiconductorlab.item.KilogramPrototypeItem.DAY_TICKS)) <= com.taku3.semiconductorlab.item.KilogramPrototypeItem.FINISH_UG, "a new prototype is within the finishing tolerance");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, a);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, b);
        h.assertTrue(Precision.KILOGRAM_PROTOTYPE.use(h.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND) == net.minecraft.world.InteractionResult.SUCCESS, "compare on the balance");
        LOG.info("[LAB] kilogram prototype [8-23]: platinum + rouge; copies drift apart by surface contamination (15 ug -> -45 ug in 30 days)");
        h.succeed();
    }

    /** [8-24] 国際単位系（IMP2 S-005）：クリプトン86 ランプ＋干渉計で 2点の間を波の数で測る。1m ちょうどなら 1,650,763.73 波長（メートル原器なし） */
    @GameTest
    public void krypton_metre(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "krypton_lamp", com.taku3.semiconductorlab.content.Gas.KRYPTON_GAS), "the SI metre lamp is filled with krypton");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.InterferometerItem.KR86_NM * 1e-9 * com.taku3.semiconductorlab.item.InterferometerItem.KR86_PER_METRE - 1) < 1e-8, "1,650,763.73 wavelengths of 605.78 nm = 1 m");
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack meter = new ItemStack(com.taku3.semiconductorlab.content.Optics.INTERFEROMETER), lamp = new ItemStack(Precision.KRYPTON_LAMP);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, meter);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, lamp);
        BlockPos a = p(1, 1, 1), b = p(2, 1, 1);
        h.setBlock(a, net.minecraft.world.level.block.Blocks.STONE); h.setBlock(b, net.minecraft.world.level.block.Blocks.STONE);
        for (BlockPos rel : new BlockPos[]{a, b}) {
            BlockPos abs = h.absolutePos(rel);
            com.taku3.semiconductorlab.content.Optics.INTERFEROMETER.useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(abs), net.minecraft.core.Direction.UP, abs, false)));
            if (rel == a) h.assertTrue(meter.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null, "first click leaves a mark");
        }
        h.assertValueEqual(lamp.getDamageValue(), 1, "one measurement uses the lamp once");
        h.assertTrue(!meter.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().contains("KrMark"), "the mark is cleared after measuring");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.InterferometerItem.distanceM(h.absolutePos(a), h.absolutePos(b)) - 1) < 1e-12, "neighbouring blocks are 1 m apart");
        LOG.info("[LAB] SI metre [8-24]: krypton-86 lamp + interferometer, 1 m = 1,650,763.73 x 605.78 nm, no metre bar needed");
        h.succeed();
    }

    /** [8-25] ワット天秤（IMP2 S-005 古典版）：原器・ウェストン電池・標準抵抗・クリプトン86 ランプで原器の質量を測る。空調と真空であいまいさが減るが、原器の差 50µg までは見えない */
    @GameTest(maxTicks = 1000)
    public void kibble_balance(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "weston_cell", Metallurgy.CADMIUM) && recipeUses(h, "weston_cell", Metallurgy.MERCURY), "Weston cell = cadmium amalgam + mercury");
        h.assertTrue(recipeUses(h, "cadmium", com.taku3.semiconductorlab.content.Heat.ZINC_OXIDE), "cadmium comes from zinc ore");
        double air = com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.sigmaUg(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_OFF_C, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.ATM);
        double ac = com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.sigmaUg(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_ON_C, com.taku3.semiconductorlab.block.VacuumChamberBlockEntity.ATM);
        double best = com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.sigmaUg(com.taku3.semiconductorlab.block.AirConditionerBlockEntity.SWING_ON_C, 1e-3);
        h.assertTrue(air > ac && ac > best && best > 50, "uncertainty: open room " + air + " ug > air conditioned " + ac + " ug > + vacuum " + best + " ug, still above the 50 ug drift");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Precision.KIBBLE_BALANCE.block);
        var be = h.getBlockEntity(at, com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.class);
        var c = be.getContainer();
        ItemStack mass = new ItemStack(Precision.KILOGRAM_PROTOTYPE);
        com.taku3.semiconductorlab.item.KilogramPrototypeItem.set(mass, 20, 0, 0);
        c.setItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_MASS, mass);
        c.setItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_CELL, new ItemStack(Precision.WESTON_CELL));
        c.setItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_RES, new ItemStack(Precision.STANDARD_RESISTOR));
        c.setItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_LAMP, new ItemStack(Precision.KRYPTON_LAMP));
        tickConsumer(h, at, be, com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.TICKS + 5, EnergyStorage.wattsToJoulesPerTick(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.WATTS));
        h.assertTrue(be.hasResult() && Math.abs(be.sigma() - Math.round(air)) <= 1, "one measurement in the open room: +-" + be.sigma() + " ug");
        h.assertTrue(Math.abs(be.result() - 20) < 6 * be.sigma(), "the result scatters around the prototype's true offset");
        h.assertValueEqual(c.getItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_LAMP).getDamageValue(), 1, "the krypton lamp is used once");
        h.assertTrue(c.getItem(com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity.SLOT_CELL).is(Precision.WESTON_CELL), "a standard cell gives a reference voltage and is not used up");
        LOG.info("[LAB] Kibble balance [8-25]: classical standards, uncertainty " + Math.round(air) + " / " + Math.round(ac) + " / " + Math.round(best) + " ug (open / AC / AC+vacuum); 50 ug drift needs quantum standards");
        h.succeed();
    }

    /** [8-21] 六分儀（IMP2 O-023）：昼前から何回か太陽の高さを測り、下がり始めたら 南中高度 → 緯度。z＝−20000（北緯20°）・x＝0 で */
    @GameTest
    public void sextant_noon_sight(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "sextant", com.taku3.semiconductorlab.content.Optics.HALF_SILVERED_MIRROR) && recipeUses(h, "sextant", Precision.VERNIER_CALIPER), "two mirrors + vernier");
        BlockPos at = new BlockPos(0, 64, -20_000);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.Navigation.latitudeDeg(at) - 20) < 1e-9, "z = -20000 is 20 N");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.Navigation.sunAltitudeDeg(20, 12) - 70) < 1e-9 && com.taku3.semiconductorlab.item.Navigation.sunAltitudeDeg(20, 6) < 1e-9, "noon sun at 20 N stands 70 deg; at 6 o'clock on the horizon");
        ItemStack sx = new ItemStack(Precision.SEXTANT);
        int[] stages = new int[4];
        long[] ticks = {5_000, 5_600, 6_000, 6_400};   // 11時・11時36分・正午・12時24分（グリニッジ＝その場所）
        for (int i = 0; i < ticks.length; i++) stages[i] = com.taku3.semiconductorlab.item.SextantItem.sight(sx, h.getLevel(), at, ticks[i], ItemStack.EMPTY, null);
        h.assertTrue(stages[0] == 0 && stages[1] == 1 && stages[2] == 1 && stages[3] == 2, "first / rising / rising / past noon: " + java.util.Arrays.toString(stages));
        int max = sx.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getIntOr("MaxMin", 0);
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.SextantItem.latitudeFromNoon(max, true) - 20) < 0.02, "noon altitude " + max / 60.0 + " -> latitude 20 N");
        h.assertValueEqual(com.taku3.semiconductorlab.item.SextantItem.sight(sx, h.getLevel(), at, 6_800, ItemStack.EMPTY, null), 3, "after the noon result it just reports falling");
        LOG.info("[LAB] sextant [8-21]: noon sight at 20 N -> " + com.taku3.semiconductorlab.item.Navigation.dm(max / 60.0) + " -> latitude " + com.taku3.semiconductorlab.item.Navigation.dm(com.taku3.semiconductorlab.item.SextantItem.latitudeFromNoon(max, true)));
        h.succeed();
    }

    /** [8-35] クオーツ腕時計（IMP2 O-023）：水晶振動子＋IC＋ステッピングモーター＋乾電池。狂いの幅はクロノメーターより小さく、六分儀の時計にも使える */
    @GameTest
    public void quartz_watch(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "quartz_watch", Precision.CRYSTAL_RESONATOR) && recipeUses(h, "quartz_watch", com.taku3.semiconductorlab.content.Semiconductor.PLASTIC_PACKAGED_CHIP)
                && recipeUses(h, "quartz_watch", com.taku3.semiconductorlab.content.Power.STEPPING_MOTOR), "quartz watch = crystal + IC + stepping motor");
        h.assertTrue(recipeUses(h, "stepping_motor", com.taku3.semiconductorlab.content.Control.ELECTROMAGNET), "the watch motor is a tiny electromagnet + magnet");
        var q = (com.taku3.semiconductorlab.item.ChronometerItem) Precision.QUARTZ_WATCH; var m = (com.taku3.semiconductorlab.item.ChronometerItem) com.taku3.semiconductorlab.content.Transport.MARINE_CHRONOMETER;
        h.assertTrue(q.maxRateSecPerDay < m.maxRateSecPerDay, "a cheap quartz watch keeps better time than the marine chronometer");
        ItemStack w = new ItemStack(Precision.QUARTZ_WATCH);
        com.taku3.semiconductorlab.item.ChronometerItem.ensure(w, h.getLevel());
        long later = h.getLevel().getOverworldClockTime() + 30 * com.taku3.semiconductorlab.item.Navigation.DAY_TICKS;
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.ChronometerItem.errorSec(w, later)) <= 30 * q.maxRateSecPerDay + 1e-6, "30 days: within " + 30 * q.maxRateSecPerDay + " s");
        LOG.info("[LAB] quartz watch [8-35]: crystal + IC + stepping motor; +-" + q.maxRateSecPerDay + " s/day vs chronometer +-" + m.maxRateSecPerDay + " s/day");
        h.succeed();
    }

    /** [8-13] 液滴発生器（IMP2 S-007）：水晶（圧電）でノズルをふるわせる。レイリー：滴＝柱の約1.89倍、間隔＝約4.51倍 */
    @GameTest
    public void droplet_generator(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "droplet_generator", Precision.CRYSTAL_RESONATOR), "piezo crystal drives the nozzle");
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.DropletGeneratorItem.dropletUm(16) - 30.2) < 0.1 && Math.abs(com.taku3.semiconductorlab.item.DropletGeneratorItem.spacingUm(16) - 72.2) < 0.1, "16 um jet -> 30 um droplets every 72 um");
        h.assertTrue(com.taku3.semiconductorlab.item.DropletGeneratorItem.perSecond(16, 70) > 5e5, "hundreds of thousands of pinches a second");
        LOG.info("[LAB] droplet generator [8-13]: 16 um nozzle -> 30 um tin droplets, " + Math.round(com.taku3.semiconductorlab.item.DropletGeneratorItem.perSecond(16, 70)) + " /s natural breakup");
        h.succeed();
    }

    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Precision.LATHE.block, "ローターを削る", L(io(Metallurgy.STEEL_INGOT, 1)), L(io(Precision.PUMP_ROTOR, 1)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "送りねじの刃物台で削る（モーズリー型）");
        rec(a, Precision.LATHE.block, "規格ねじを切る", L(io(Metallurgy.STEEL_INGOT, 1), kept(io(Precision.LEAD_SCREW, 1))), L(io(Precision.STANDARD_SCREW, 4)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "親ねじと同じねじ山を何本でも（ホイットワースの規格 55°）");
    }
    /** [8-33] 質量分析器（IMP4-01、C の了承で追記）：B2O3 → ¹⁰B 19.9%・¹¹B 80.1%、リンは ³¹P だけ、割合の合計はほぼ100%、電磁石と拡散ポンプでできている */
    @GameTest
    public void massSpectrometer(GameTestHelper h) {
        var b = com.taku3.semiconductorlab.item.MassSpectrometerItem.of(new ItemStack(com.taku3.semiconductorlab.content.Metallurgy.BORON_TRIOXIDE));
        h.assertTrue(b != null && b.element().equals("B") && b.mass()[1] == 11 && b.percent()[1] > 80, "boron: 10B 19.9%, 11B 80.1%");
        var pp = com.taku3.semiconductorlab.item.MassSpectrometerItem.of(new ItemStack(com.taku3.semiconductorlab.content.Chemistry.PHOSPHORUS));
        h.assertTrue(pp != null && pp.mass().length == 1 && pp.mass()[0] == 31, "phosphorus has a single isotope 31P");
        for (var it : new net.minecraft.world.item.Item[] {com.taku3.semiconductorlab.content.Gas.CHLORINE_GAS, com.taku3.semiconductorlab.content.Metallurgy.POLYSILICON, com.taku3.semiconductorlab.content.Metallurgy.LEAD_INGOT, net.minecraft.world.item.Items.COPPER_INGOT}) {
            var iso = com.taku3.semiconductorlab.item.MassSpectrometerItem.of(new ItemStack(it));
            double sum = 0; for (double v : iso.percent()) sum += v;
            h.assertTrue(Math.abs(sum - 100) < 0.6, iso.element() + " shares add to 100%: " + sum);
        }
        h.assertTrue(recipeUses(h, "mass_spectrometer", com.taku3.semiconductorlab.content.Control.ELECTROMAGNET) && recipeUses(h, "mass_spectrometer", com.taku3.semiconductorlab.content.Vacuum.DIFFUSION_PUMP.block.asItem()), "electromagnet + high vacuum");
        LOG.info("[LAB] mass spectrometer [8-33]: B 10/11 = 19.9/80.1 %, P single, Cl/Si/Pb/Cu shares add to 100 %");
        h.succeed();
    }


}
