package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 封止機 [9-34]（IMP4-01、S-001）：線をつないだチップを、湿気や汚れから守る殻に閉じこめる。
 * ① 金属缶（1960年代初め）：鋼の缶をかぶせ、乾いた窒素を満たしてヘッダーのふちを電気で溶接する（気密封止）。
 *    線をつないだチップ 1〜8個 ＋ 金属缶 1個ずつ ＋ 窒素 1袋 → 金属缶の IC。
 * ② エポキシ（1960年代後半〜）：温めた型にエポキシの成形材（D の [2-41]）を押しこんでチップを包む（トランスファー成形、約175℃）。安くたくさん作れる。
 *    線をつないだチップ 1〜8個 ＋ エポキシ成形材 1（チップ4個ぶん）→ プラスチックの IC。
 * どちらもチップの中身（CHIP_STATE）を写す。⚠️ 電力（500W）・時間（20秒）はゲームの値（package_sealer.placeholder）。
 */
public class PackageSealerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 20;
    public static final int CHIPS_PER_RUN = 8, CHIPS_PER_COMPOUND = 4;
    public static final int SLOT_CHIP = 0, SLOT_SHELL = 1, SLOT_GAS = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public enum Mode { NONE, CAN, EPOXY }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_CHIP, 8, 17, s -> s.is(Semiconductor.BONDED_CHIP))
            .in(SLOT_SHELL, 8, 35, s -> modeOf(s) != Mode.NONE)
            .in(SLOT_GAS, 8, 53, s -> s.is(Gas.NITROGEN_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.package_sealer.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.package_sealer.need_input", "gui.semiconductor-lab.package_sealer.need_n2",
                    "gui.semiconductor-lab.package_sealer.need_power", "gui.semiconductor-lab.package_sealer.running_can", "gui.semiconductor-lab.package_sealer.running_epoxy")
            .bottom("recipe", "gui.semiconductor-lab.package_sealer.recipe_can", "gui.semiconductor-lab.package_sealer.recipe_epoxy", "gui.semiconductor-lab.package_sealer.recipe")
            .build();

    public PackageSealerBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.PACKAGE_SEALER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static Mode modeOf(ItemStack shell) {
        if (shell.is(Semiconductor.CAN_CAP)) return Mode.CAN;
        if (shell.is(Chemistry.EPOXY_MOLDING_COMPOUND)) return Mode.EPOXY;
        return Mode.NONE;
    }

    /** 1回に封じるチップの数 */
    public static int batch(ItemStack chips, ItemStack shell, ItemStack gas) {
        if (!chips.is(Semiconductor.BONDED_CHIP)) return 0;
        int n = Math.min(chips.getCount(), CHIPS_PER_RUN);
        return switch (modeOf(shell)) {
            case CAN -> gas.is(Gas.NITROGEN_GAS) ? Math.min(n, shell.getCount()) : 0;
            case EPOXY -> Math.min(n, shell.getCount() * CHIPS_PER_COMPOUND);
            case NONE -> 0;
        };
    }

    public static ItemStack product(ItemStack chips, ItemStack shell, ItemStack gas) {
        int n = batch(chips, shell, gas);
        if (n == 0) return ItemStack.EMPTY;
        return chips.transmuteCopy(modeOf(shell) == Mode.CAN ? Semiconductor.CAN_PACKAGED_CHIP : Semiconductor.PLASTIC_PACKAGED_CHIP, n);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    private ItemStack product() { return product(container.getItem(SLOT_CHIP), container.getItem(SLOT_SHELL), container.getItem(SLOT_GAS)); }

    @Override protected boolean canWork() {
        ItemStack out = product();
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product();
        int n = out.getCount();
        Mode m = modeOf(container.getItem(SLOT_SHELL));
        container.getItem(SLOT_CHIP).shrink(n);
        if (m == Mode.CAN) { container.getItem(SLOT_SHELL).shrink(n); container.getItem(SLOT_GAS).shrink(1); }
        else container.getItem(SLOT_SHELL).shrink((n + CHIPS_PER_COMPOUND - 1) / CHIPS_PER_COMPOUND);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
