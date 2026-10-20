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

    /** この幹の機械のレシピ（docs/machine_recipes.json の元。CommonTests.exportMachineRecipes が集める） */
    static void machineRecipes(com.google.gson.JsonArray a) {
        rec(a, Precision.LATHE.block, "ローターを削る", L(io(Metallurgy.STEEL_INGOT, 1)), L(io(Precision.PUMP_ROTOR, 1)),
            LatheBlockEntity.WATTS, LatheBlockEntity.TICKS / 20.0, "送りねじの刃物台で削る（モーズリー型）");
    }
}
