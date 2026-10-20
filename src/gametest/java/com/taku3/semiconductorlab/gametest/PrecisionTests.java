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
