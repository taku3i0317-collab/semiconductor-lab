package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ガリウムの電解槽 [1-44]（1875 ルコック・ド・ボアボードランが発見、今のガリウムの大半はバイヤー法の副産物。IMP4-01、O-020）：
 * バイヤー法の使い終わった液（アルミン酸ナトリウムの液、ガリウムはガリウム酸の形で溶けている）を、ステンレスの電極で電解すると、陰極に液体のガリウムが出る。
 * 使い終わった液 {@link #LIQUOR_PER_RUN} ＋ 空のポリエチレン瓶 → ガリウム（瓶入り）＋ 水酸化ナトリウム 1（液は釜に戻せる）。
 * ガリウムは約30℃ で溶け、アルミニウムを中から壊すので、金属やガラスでなくポリエチレンの瓶に入れる。
 * ⚠️ 本物の液のガリウムは 1L に 0.1〜0.2g ほどで、イオン交換で濃くしてから電解する（ここでは省略）。量・電力・時間はゲームの値（gallium_cell.placeholder）。
 */
public class GalliumCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 20;
    public static final int LIQUOR_PER_RUN = 4;
    public static final int SLOT_LIQUOR = 0, SLOT_BOTTLE = 1, SLOT_OUT = 2, SLOT_NAOH = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_LIQUOR, 8, 26, s -> s.is(Metallurgy.SPENT_BAYER_LIQUOR))
            .in(SLOT_BOTTLE, 8, 50, s -> s.is(Chemistry.POLYETHYLENE_BOTTLE))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_NAOH, 78, 50)
            .right("power", 17, 1, "gui.semiconductor-lab.gallium_cell.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.gallium_cell.need_input", "gui.semiconductor-lab.gallium_cell.need_power", "gui.semiconductor-lab.gallium_cell.running")
            .bottom("recipe", "gui.semiconductor-lab.gallium_cell.recipe")
            .build();

    public GalliumCellBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.GALLIUM_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean ready(ItemStack liquor, ItemStack bottle) {
        return liquor.is(Metallurgy.SPENT_BAYER_LIQUOR) && liquor.getCount() >= LIQUOR_PER_RUN && bottle.is(Chemistry.POLYETHYLENE_BOTTLE);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        return ready(container.getItem(SLOT_LIQUOR), container.getItem(SLOT_BOTTLE))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Metallurgy.GALLIUM))
                && canMerge(container.getItem(SLOT_NAOH), new ItemStack(Gas.SODIUM_HYDROXIDE));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_LIQUOR).shrink(LIQUOR_PER_RUN);
        container.getItem(SLOT_BOTTLE).shrink(1);
        merge(SLOT_OUT, new ItemStack(Metallurgy.GALLIUM));
        merge(SLOT_NAOH, new ItemStack(Gas.SODIUM_HYDROXIDE));
        container.setChanged();
    }
}
