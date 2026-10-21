package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ホットプレート（露光後の加熱 PEB）[2-33]（IMP4-01、O-016）：化学増幅レジストは、光で光酸発生剤から酸が出るだけでは変わらない。
 * 約100℃ に熱すると、酸1個がレジストの中を動きまわって何百もの保護基を外し（触媒）、そこだけアルカリの現像液に溶けるようになる。
 * 露光した化学増幅レジストのウェハー 1〜8枚 → 加熱したウェハー（ChipOps.bake、A）。加熱を忘れて現像すると、どこも溶けない（模様が出ない）。
 * ⚠️ 温度・時間はゲームの値。熱しすぎると酸が広がって線がぼける（酸の拡散）のは省略（bake_plate.placeholder）。
 */
public class BakePlateBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 300;
    public static final int TICKS = 20 * 10;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 35, ChipOps::isProcessWafer)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.bake_plate.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.bake_plate.need_input", "gui.semiconductor-lab.bake_plate.not_exposed",
                    "gui.semiconductor-lab.bake_plate.not_car", "gui.semiconductor-lab.bake_plate.already", "gui.semiconductor-lab.bake_plate.need_power", "gui.semiconductor-lab.bake_plate.running")
            .bottom("recipe", "gui.semiconductor-lab.bake_plate.recipe")
            .build();

    public BakePlateBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.BAKE_PLATE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.bake(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    /** できない理由（not_exposed・not_car・already。画面の文に使う） */
    public static String reason(ItemStack w) { return ChipOps.isProcessWafer(w) ? ChipOps.bake(w.copyWithCount(1)).reason() : "not_exposed"; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
