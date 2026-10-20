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
        c.setItem(LatheBlockEntity.SLOT_IN, new ItemStack(Metallurgy.STEEL_INGOT, 2));
        tickConsumer(h, pos, be, T, full / 2);
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_OUT).isEmpty(), "half power -> no progress");
        tickConsumer(h, pos, be, T, full);
        h.assertTrue(c.getItem(LatheBlockEntity.SLOT_OUT).is(Precision.PUMP_ROTOR), "steel -> pump rotor in 30 s at 750 W");
        LOG.info("[LAB] lathe [8-01]: steel -> pump rotor (750 W x 30 s)");
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

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Precision.LATHE.block, "ローターを削る", L(io(Metallurgy.STEEL_INGOT, 1)), L(io(Precision.PUMP_ROTOR, 1)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "送りねじの刃物台で削る（モーズリー型）");
    }
}
