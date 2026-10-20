package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModMachines;
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
 * ベッセマー転炉 [1-26]（1856 ヘンリー・ベッセマー）。溶けた銑鉄（C約4%）の底から空気を吹き込み、
 * 炭素を燃やして抜く：C + O2 → CO2（炉の中では主に 2C + O2 → 2CO、炉口で CO が燃えて CO2）。
 * 燃料は使わない。ケイ素と炭素が燃える熱だけで、鉄が溶けたまま保たれる。
 * ゲーム：バニラの鉄インゴット＝銑鉄の近似（CHEMISTRY に注記）。電気は送風機（モーター）を回すのに使う。
 * 鉄インゴット1個 ＋ 20kW×20秒 → 鋼インゴット1個。
 */
public class BessemerConverterBlockEntity extends ConsumerBlockEntity {
    /** 送風機の電力（W）。本物の送風機関（蒸気駆動）は数百kW〜MW 級。ゲームでは小さくしている */
    public static final double WATTS = 20_000;
    /** 1回の吹錬（ブロー）の長さ。本物は数トンを10〜20分 */
    public static final int TOTAL_TICKS = 20 * 20;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    /** 吹錬の段階（炎の色で分かる）。進み具合の割合で決める */
    public enum Stage { SILICON, CARBON, DROP }
    /** ①ケイ素・マンガンが先に燃える（最初の約2割）→②炭素が燃える「ボイル」→③炎が落ちる（最後の約1割） */
    public static Stage stageOf(int progress, int total) {
        double f = (double) progress / Math.max(1, total);
        return f < 0.2 ? Stage.SILICON : f < 0.9 ? Stage.CARBON : Stage.DROP;
    }

    /** 画面のスロットの並び（IMP-28：前の BessemerConverterMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 26, 35, BessemerConverterBlockEntity::isPigIron)
            .out(SLOT_OUT, 134, 35)
            .build();

    public BessemerConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.BESSEMER_CONVERTER.type, pos, state, SLOT_COUNT, 100_000, WATTS);
    }

    /** 銑鉄として受け付けるもの（今はバニラの鉄インゴット＝銑鉄の近似。[1-09]高炉の銑鉄が来たら置き換え） */
    public static boolean isPigIron(ItemStack s) { return s.is(Items.IRON_INGOT); }
    private static ItemStack steel() { return new ItemStack(ModItems.STEEL_INGOT, 1); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TOTAL_TICKS; }

    @Override protected boolean canWork() {
        return isPigIron(container.getItem(SLOT_IN)) && canMerge(container.getItem(SLOT_OUT), steel());
    }

    @Override protected void finishWork() {
        container.removeItem(SLOT_IN, 1);
        merge(SLOT_OUT, steel());
    }

}
