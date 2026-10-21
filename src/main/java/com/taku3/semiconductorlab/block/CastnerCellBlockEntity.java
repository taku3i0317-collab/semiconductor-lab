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
 * カストナーの電解槽（1886 カストナー、溶けた水酸化ナトリウムの電解。IMP4-01、S-003：タンタル [1-66] の還元剤）：
 * 鉄の釜で NaOH を約320℃ に溶かし、ニッケルの陽極・鉄の陰極で電気を流すと、陰極に金属ナトリウムが浮かぶ（4NaOH → 4Na + O2 + 2H2O）。
 * 水酸化ナトリウム {@link #NAOH_PER_RUN} ＋ 空の鉄の容器 → 金属ナトリウム（鉄の容器入り）。ナトリウムは水にふれると燃えるので、鉄の容器に密封する。
 * ⚠️ 本物はできた水の一部がナトリウムと反応して効率が半分ほど。酸素は捨てる。電力・時間はゲームの値（castner_cell.placeholder）。
 */
public class CastnerCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 20;
    public static final int NAOH_PER_RUN = 4;
    public static final int SLOT_NAOH = 0, SLOT_FLASK = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_NAOH, 8, 26, s -> s.is(Gas.SODIUM_HYDROXIDE))
            .in(SLOT_FLASK, 8, 50, s -> s.is(Metallurgy.IRON_FLASK))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.castner_cell.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.castner_cell.need_input", "gui.semiconductor-lab.castner_cell.need_power", "gui.semiconductor-lab.castner_cell.running")
            .bottom("recipe", "gui.semiconductor-lab.castner_cell.recipe")
            .build();

    public CastnerCellBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.CASTNER_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack n = container.getItem(SLOT_NAOH);
        return n.is(Gas.SODIUM_HYDROXIDE) && n.getCount() >= NAOH_PER_RUN && container.getItem(SLOT_FLASK).is(Metallurgy.IRON_FLASK)
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Metallurgy.SODIUM));
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_NAOH).shrink(NAOH_PER_RUN);
        container.getItem(SLOT_FLASK).shrink(1);
        merge(SLOT_OUT, new ItemStack(Metallurgy.SODIUM));
        container.setChanged();
    }
}
