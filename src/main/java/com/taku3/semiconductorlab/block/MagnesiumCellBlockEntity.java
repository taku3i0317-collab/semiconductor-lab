package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * マグネシウムの電解槽 [1-39]（ダウ法、1916 かん水・1941 海水）：溶かした無水の塩化マグネシウムに電気を流す：MgCl2 → Mg + Cl2。
 * 陰極にマグネシウムが（軽いので液の上に浮く）、陽極（黒鉛）に塩素が出る。塩素は塩酸に戻して、また にがりの乾燥に使う。
 * <pre>
 *   無水の塩化マグネシウム 1 → マグネシウム 1 ＋ 塩素の袋 1   （20秒、10kW）
 * </pre>
 * ⚠️ 約700℃の溶けた塩・電圧は数字で扱わない（ふつうの画面）。
 */
public class MagnesiumCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 10_000;
    public static final int TICKS = 20 * 20;
    public static final int SLOT_IN = 0, SLOT_OUT = 1, SLOT_GAS = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 26, s -> s.is(Metallurgy.ANHYDROUS_MAGNESIUM_CHLORIDE))
            .out(SLOT_OUT, 78, 17)
            .out(SLOT_GAS, 78, 35)
            .standardTexts()
            .build();

    public MagnesiumCellBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.MAGNESIUM_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_IN).is(Metallurgy.ANHYDROUS_MAGNESIUM_CHLORIDE)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Metallurgy.MAGNESIUM))
                && canMerge(container.getItem(SLOT_GAS), new ItemStack(Gas.CHLORINE_GAS));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_OUT, new ItemStack(Metallurgy.MAGNESIUM));
        merge(SLOT_GAS, new ItemStack(Gas.CHLORINE_GAS));
        container.setChanged();
    }
}
