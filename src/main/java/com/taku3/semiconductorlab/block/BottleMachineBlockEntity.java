package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 自動製びん機 [2-56]（1903 オーエンス）：溶けたガラスを吸い上げ、型の中で空気を吹きこんでびんにする。人が口で吹いていた仕事を機械が続ける。
 * <pre>
 *   ソーダ石灰ガラス 1 → ガラス瓶 1   （1秒、2kW。止めずに作り続ける＝1分に60本）
 * </pre>
 * 作業台の手作り（ガラス3 → 瓶3）は1回ずつ人が作る。機械は材料を入れておけば電気だけで作り続ける。
 * ⚠️ 1分あたりの本数はゲームの値。本物の速さは機械と年で大きく違う（オーエンスの機械は職人の組の何十倍とされる ⚠️、1926 リボンマシンは電球の球を1分に最大約2000個）。
 */
public class BottleMachineBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 26, s -> s.is(Chemistry.SODA_LIME_GLASS))
            .out(SLOT_OUT, 78, 26)
            .standardTexts()
            .build();

    public BottleMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.BOTTLE_MACHINE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_IN).is(Chemistry.SODA_LIME_GLASS) && canMerge(container.getItem(SLOT_OUT), new ItemStack(Chemistry.GLASS_BOTTLE));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_OUT, new ItemStack(Chemistry.GLASS_BOTTLE));
        container.setChanged();
    }
}
