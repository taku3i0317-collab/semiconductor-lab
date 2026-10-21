package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ウェハーの銅めっき装置 [9-36]（1997 IBM の銅配線。IMP4-01、S-003）：硫酸銅の液にウェハーを陰極、銅の板を陽極にして電気を流すと、
 * 溝と穴の底から銅が積もって埋まり、一面に銅の膜ができる（Cu²⁺ + 2e⁻ → Cu）。銅はエッチングしにくいので、このあと CMP で上を削り、溝の中だけ残す（ダマシン）。
 * 層間膜に穴と溝を開けたウェハー 1〜8枚 ＋ 硫酸銅 1（液）＋ 銅 1（陽極が溶ける）→ 銅の膜のウェハー（ChipOps.plateCopper、A）。
 * 先にタンタルのバリア膜（スパッタの Ta の的）を付けないと、銅がシリコンへしみて汚れが大きく増える（＋30）。
 * ⚠️ 本物は先にスパッタで薄い銅の種膜を付け、液に添加剤を入れて底から埋める（ボトムアップ）。電力・時間はゲームの値（copper_plater.placeholder）。
 */
public class CopperPlaterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_SULFATE = 1, SLOT_ANODE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, ChipOps::isProcessWafer)
            .in(SLOT_SULFATE, 8, 35, s -> s.is(Chemistry.COPPER_SULFATE))
            .in(SLOT_ANODE, 8, 53, s -> s.is(Items.COPPER_INGOT))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.copper_plater.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.copper_plater.need_input", "gui.semiconductor-lab.copper_plater.cannot",
                    "gui.semiconductor-lab.copper_plater.no_barrier", "gui.semiconductor-lab.copper_plater.need_power", "gui.semiconductor-lab.copper_plater.running")
            .bottom("recipe", "gui.semiconductor-lab.copper_plater.recipe")
            .build();

    public CopperPlaterBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.COPPER_PLATER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 銅の膜を付けたウェハー（1〜8枚）。付けられなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.plateCopper(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    /** バリア膜があるか（画面で「バリアなし」を知らせる） */
    public static boolean hasBarrier(ItemStack w) {
        var s = ChipOps.state(w);
        return s != null && s.extras().barrier();
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_SULFATE).is(Chemistry.COPPER_SULFATE) && container.getItem(SLOT_ANODE).is(Items.COPPER_INGOT)
                && canMerge(container.getItem(SLOT_OUT), dust(out));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_SULFATE).shrink(1);
        container.getItem(SLOT_ANODE).shrink(1);
        merge(SLOT_OUT, dust(out));
        container.setChanged();
    }

    /** クリーンルームの等級のほこり（O-017、C の CleanRoom.dustAt：外 +2・クラス1000 +1・クラス100 0）。失敗にはしない＝汚れは検査台の漏れ・歩留まりに効く */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition));
    }
}
