package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.block.RefrigeratorBlockEntity;
import com.taku3.semiconductorlab.content.Agriculture;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * GameTest：幹11 農業・食品（IMP2 S-003 で作成）。共通の道具は {@link LabTestBase}。
 */
public class AgricultureTests extends LabTestBase {
    /** [11-13] 冷凍機（IMP2 S-003）：水バケツ → 氷（バケツは戻る）。冷媒のアンモニアは減らない。冷媒がないと動かない */
    @GameTest
    public void refrigerator(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "refrigerator", Gas.COMPRESSOR.block.asItem()), "refrigerator = compressor + copper condenser + iron");
        BlockPos at = p(1, 1, 1);
        h.setBlock(at, Agriculture.REFRIGERATOR.block);
        var be = h.getBlockEntity(at, RefrigeratorBlockEntity.class);
        var c = be.getContainer();
        long full = EnergyStorage.wattsToJoulesPerTick(RefrigeratorBlockEntity.WATTS);
        c.setItem(RefrigeratorBlockEntity.SLOT_WATER, new ItemStack(Items.WATER_BUCKET));
        tickConsumer(h, at, be, RefrigeratorBlockEntity.TICKS + 5, full);
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_ICE).isEmpty(), "no ammonia refrigerant -> nothing");
        c.setItem(RefrigeratorBlockEntity.SLOT_REFRIGERANT, new ItemStack(Chemistry.AMMONIA_WATER));
        tickConsumer(h, at, be, RefrigeratorBlockEntity.TICKS + 5, full);
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_ICE).is(Items.ICE) && c.getItem(RefrigeratorBlockEntity.SLOT_BUCKET).is(Items.BUCKET), "water bucket -> ice + bucket");
        h.assertTrue(c.getItem(RefrigeratorBlockEntity.SLOT_REFRIGERANT).is(Chemistry.AMMONIA_WATER), "the refrigerant circulates and is not used up");
        LOG.info("[LAB] refrigerator [11-13]: ammonia compression, water bucket -> ice in 20 s at 1 kW, refrigerant kept");
        h.succeed();
    }
}
