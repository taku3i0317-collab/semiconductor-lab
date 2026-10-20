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
        h.assertTrue(Math.abs(com.taku3.semiconductorlab.item.MicroscopeItem.cellNm(20) - 10_000 * Math.pow(0.4, 3)) < 1e-6, "grid not in the table -> inverse of the N formula");
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
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Precision.LATHE.block, "ローターを削る", L(io(Metallurgy.STEEL_INGOT, 1)), L(io(Precision.PUMP_ROTOR, 1)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "送りねじの刃物台で削る（モーズリー型）");
        rec(a, Precision.LATHE.block, "規格ねじを切る", L(io(Metallurgy.STEEL_INGOT, 1), kept(io(Precision.LEAD_SCREW, 1))), L(io(Precision.STANDARD_SCREW, 4)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "親ねじと同じねじ山を何本でも（ホイットワースの規格 55°）");
    }
}
