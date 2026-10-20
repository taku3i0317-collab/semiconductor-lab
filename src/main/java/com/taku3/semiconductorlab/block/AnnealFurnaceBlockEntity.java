package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * アニール炉 [9-29]（IMP4-01、O-011）：イオン注入 [9-28] で打ちこんだだけのウェハーは、結晶がこわれていて、ドーパントも格子の位置にいないので効かない。
 * 窒素かアルゴンの中で 約1000℃ に熱すると、原子が並び直し（焼きなまし）、ドーパントが働きだす（活性化）。注入した層が拡散と同じ層になる（ChipOps.anneal）。
 * 注入したウェハー 1〜8枚 ＋ 窒素かアルゴン 1袋 → アニールしたウェハー。レジストが付いたままは入れられない（焼けてこげる＝先にはがす）。
 * 隣のガスホルダーから窒素・アルゴンを補充する（FZ 炉と同じ）。空気の中だと酸素で表面が酸化するので、守るガスが要る。
 * ⚠️ 電力（3kW）・時間（20秒）はゲームの値。本物の炉は 30分ほど。1980年代からはランプで数秒だけ熱する RTA（ドーパントが横に広がらない）＝ここでは炉だけ（anneal_furnace.placeholder）。
 */
public class AnnealFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8, GAS_PER_RUN = 1;
    public static final int SLOT_WAFER = 0, SLOT_GAS = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    private static final int PULL_INTERVAL = 10, GAS_TARGET = 16;
    private int pullTimer;   // 補充の間隔（ゲーム時刻ではなく自分の tick で数える）

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_GAS, 8, 50, AnnealFurnaceBlockEntity::isGas)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.anneal_furnace.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.anneal_furnace.need_input", "gui.semiconductor-lab.anneal_furnace.nothing",
                    "gui.semiconductor-lab.anneal_furnace.resist_on", "gui.semiconductor-lab.anneal_furnace.need_power", "gui.semiconductor-lab.anneal_furnace.running")
            .bottom("recipe", "gui.semiconductor-lab.anneal_furnace.recipe")
            .build();

    public AnnealFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.ANNEAL_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isGas(ItemStack s) { return s.is(Gas.NITROGEN_GAS) || s.is(Gas.ARGON_GAS); }

    /** アニールしたウェハー（1〜8枚）。できなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.anneal(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    /** できない理由（ChipOps の reason。画面の文に使う） */
    public static String reason(ItemStack w) {
        return ChipOps.isProcessWafer(w) ? ChipOps.anneal(w.copyWithCount(1)).reason() : "nothing_to_anneal";
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER)), g = container.getItem(SLOT_GAS);
        return !out.isEmpty() && isGas(g) && g.getCount() >= GAS_PER_RUN && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_GAS).shrink(GAS_PER_RUN);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダーから、スロットと同じガス（空なら窒素→アルゴンの順）を 16 袋になるまで補充する */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (++pullTimer < PULL_INTERVAL) return;
        pullTimer = 0;
        for (Direction d : Direction.values()) {
            ItemStack g = container.getItem(SLOT_GAS);
            if (!g.isEmpty() && !isGas(g)) return;
            int want = GAS_TARGET - g.getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            for (GasType t : new GasType[] {GasType.NITROGEN, GasType.ARGON}) {
                if (!g.isEmpty() && !g.is(t.bagItem())) continue;
                int n = holder.extract(t, want);
                if (n > 0) { merge(SLOT_GAS, new ItemStack(t.bagItem(), n)); break; }
            }
        }
    }
}
