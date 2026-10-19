package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 電解槽 [6-01]。2H2O → 2H2 + O2（黒鉛電極・希硫酸電解質・直流）。
 * 水バケツ1杯 ＋ 硫酸（電解質、消耗しない）＋ 5kW → 水素×2、酸素×1。終わるとバケツは空になる。
 */
public class ElectrolyzerBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final double WATTS = 5_000;
    // 水 2 mol を分解するのに要るエネルギー ≒ 2×286 kJ = 572 kJ。5kW なら約 115 秒
    public static final int TOTAL_TICKS = 20 * 115;
    public static final int SLOT_WATER = 0, SLOT_ACID = 1, SLOT_H2 = 2, SLOT_O2 = 3, SLOT_COUNT = 4;

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER, pos, state, SLOT_COUNT, 50_000, WATTS);
    }

    public static boolean isElectrolyte(ItemStack s) { return s.is(ModItems.SULFURIC_ACID); }
    private static ItemStack h2() { return new ItemStack(ModItems.HYDROGEN_GAS, 2); }
    private static ItemStack o2() { return new ItemStack(ModItems.OXYGEN_GAS, 1); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TOTAL_TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_WATER).is(Items.WATER_BUCKET)
                && isElectrolyte(container.getItem(SLOT_ACID))          // 純水は電気を通さない
                && canMerge(container.getItem(SLOT_H2), h2())
                && canMerge(container.getItem(SLOT_O2), o2());
    }

    @Override protected void finishWork() {
        container.setItem(SLOT_WATER, new ItemStack(Items.BUCKET));    // 水を使い切った。硫酸は減らない
        merge(SLOT_H2, h2());   // 陰極（−）から水素、体積は酸素の2倍
        merge(SLOT_O2, o2());   // 陽極（＋）から酸素
    }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.electrolyzer"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ElectrolyzerMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
