package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 水素炉 [1-28]（電気で熱する管状炉＋水素の流れ）。タングステンの粉末冶金（クーリッジ 1909〜1910）の2工程：
 * ① 還元：WO3 ×1 ＋ 水素 ×3 → タングステン粉 ×1（WO3 + 3H2 → W + 3H2O、約700〜1000℃）
 * ② 焼結：タングステン粉 ×2 ＋ 水素 ×1 → タングステン焼結棒 ×1（押し固めた棒に水素中で大電流を流し、約3000℃で焼き固める）
 * 水素は左下のスロット（袋）か、隣のガスホルダー（水素）から自動で受け取る。
 * ⚠️ 近似：本物の還元炉は抵抗加熱の電気炉 [4-12]（未実装）。ここでは電力を直接「熱」とみなしている。
 */
public class HydrogenFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double REDUCE_WATTS = 3_000, SINTER_WATTS = 10_000;
    public static final int REDUCE_TICKS = 20 * 30, SINTER_TICKS = 20 * 20;
    public static final int REDUCE_H2 = 3, SINTER_H2 = 1, SINTER_POWDER = 2;
    /** GeO2 + 2H2 → Ge + 2H2O（約650℃）。3kW×20秒 */
    public static final int GE_REDUCE_TICKS = 20 * 20, GE_REDUCE_H2 = 2;
    public static final int SLOT_IN = 0, SLOT_H2 = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    /** 隣のホルダーから水素を補充する間隔（tick）と、スロットに貯める上限 */
    private static final int PULL_INTERVAL = 10, H2_TARGET = 16;

    public enum Mode { NONE, REDUCE, SINTER, REDUCE_GE, REDUCE_MO }

    /** 画面のスロットの並び（IMP-28：前の HydrogenFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 17, HydrogenFurnaceBlockEntity::isInput)
            .in(SLOT_H2, 8, 53, s -> s.is(com.taku3.semiconductorlab.content.Gas.HYDROGEN_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.hydrogen_furnace.power").samples("10.0 kW")
            .right("stored", 27, 1, "gui.semiconductor-lab.hydrogen_furnace.stored").samples("50 kJ")
            .right("hint", 38, 3, "gui.semiconductor-lab.hydrogen_furnace.need_input", "gui.semiconductor-lab.hydrogen_furnace.need_h2", "gui.semiconductor-lab.hydrogen_furnace.not_enough", "gui.semiconductor-lab.hydrogen_furnace.need_power", "gui.semiconductor-lab.hydrogen_furnace.sintering", "gui.semiconductor-lab.hydrogen_furnace.reducing_ge", "gui.semiconductor-lab.hydrogen_furnace.reducing_mo", "gui.semiconductor-lab.hydrogen_furnace.reducing")
            .label("h2", 28, 57, 12, "H2")
            .build();

    public HydrogenFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.HYDROGEN_FURNACE.type, pos, state, SLOT_COUNT, 100_000, SINTER_WATTS);
    }

    public static boolean isInput(ItemStack s) { return modeOf(s) != Mode.NONE; }

    /** 入っている材料から、どちらの工程か（材料がそろっていなくても種類で決める。GUI 表示用） */
    public static Mode modeOf(ItemStack in) {
        if (in.is(Metallurgy.TUNGSTEN_TRIOXIDE)) return Mode.REDUCE;
        if (in.is(Metallurgy.TUNGSTEN_POWDER)) return Mode.SINTER;
        if (in.is(Metallurgy.MOLYBDENUM_TRIOXIDE)) return Mode.REDUCE_MO;   // [1-31] MoO3 + 3H2 → Mo + 3H2O（IMP2 S-007、WO3 と同じ3）
        if (in.is(Metallurgy.GERMANIUM_DIOXIDE) || in.is(Metallurgy.HIGH_PURITY_GERMANIUM_DIOXIDE)) return Mode.REDUCE_GE;
        return Mode.NONE;
    }

    private Mode mode() { return modeOf(container.getItem(SLOT_IN)); }
    private ItemStack product(Mode m) {
        if (m == Mode.REDUCE_GE) {
            ItemStack ge = new ItemStack(Metallurgy.RAW_GERMANIUM);
            // GeCl4 蒸留を経た高純度 GeO2 から還元した Ge は「経路」を持つ（帯域精製で 10N まで）
            if (container.getItem(SLOT_IN).is(Metallurgy.HIGH_PURITY_GERMANIUM_DIOXIDE)) ge.set(com.taku3.semiconductorlab.ModDataComponents.CHLORIDE_ROUTE, true);
            return ge;
        }
        if (m == Mode.REDUCE_MO) return new ItemStack(Metallurgy.MOLYBDENUM);
        return m == Mode.REDUCE ? new ItemStack(Metallurgy.TUNGSTEN_POWDER) : new ItemStack(Metallurgy.TUNGSTEN_BAR);
    }
    private static int h2For(Mode m) { return switch (m) { case SINTER -> SINTER_H2; case REDUCE_GE -> GE_REDUCE_H2; default -> REDUCE_H2; }; }

    @Override protected double workWatts() { return mode() == Mode.SINTER ? SINTER_WATTS : REDUCE_WATTS; }
    @Override protected int workTicks() { return switch (mode()) { case SINTER -> SINTER_TICKS; case REDUCE_GE -> GE_REDUCE_TICKS; default -> REDUCE_TICKS; }; }

    @Override protected boolean canWork() {
        Mode m = mode();
        if (m == Mode.NONE) return false;
        ItemStack in = container.getItem(SLOT_IN), h2 = container.getItem(SLOT_H2);
        int needIn = m == Mode.SINTER ? SINTER_POWDER : 1, needH2 = h2For(m);
        return in.getCount() >= needIn && h2.is(Gas.HYDROGEN_GAS) && h2.getCount() >= needH2
                && canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        Mode m = mode();
        ItemStack out = product(m);   // 入力が減る前に決める（GeO2 の種類で経路が変わる）
        container.getItem(SLOT_IN).shrink(m == Mode.SINTER ? SINTER_POWDER : 1);
        container.getItem(SLOT_H2).shrink(h2For(m));   // 水素は H2O になって出ていく（還元）／流しっぱなしで燃やす（焼結）
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダー（水素）から、スロットが 16 袋になるまで補充する */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack h2 = container.getItem(SLOT_H2);
        if (!h2.isEmpty() && !h2.is(Gas.HYDROGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = H2_TARGET - container.getItem(SLOT_H2).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.HYDROGEN, want);
            if (n > 0) merge(SLOT_H2, new ItemStack(Gas.HYDROGEN_GAS, n));
        }
    }

}
