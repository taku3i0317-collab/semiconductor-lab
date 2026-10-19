package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModItems;
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
public class HydrogenFurnaceBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final double REDUCE_WATTS = 3_000, SINTER_WATTS = 10_000;
    public static final int REDUCE_TICKS = 20 * 30, SINTER_TICKS = 20 * 20;
    public static final int REDUCE_H2 = 3, SINTER_H2 = 1, SINTER_POWDER = 2;
    public static final int SLOT_IN = 0, SLOT_H2 = 1, SLOT_OUT = 2, SLOT_COUNT = 3;
    /** 隣のホルダーから水素を補充する間隔（tick）と、スロットに貯める上限 */
    private static final int PULL_INTERVAL = 10, H2_TARGET = 16;

    public enum Mode { NONE, REDUCE, SINTER }

    public HydrogenFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDROGEN_FURNACE, pos, state, SLOT_COUNT, 100_000, SINTER_WATTS);
    }

    public static boolean isInput(ItemStack s) { return s.is(ModItems.TUNGSTEN_TRIOXIDE) || s.is(ModItems.TUNGSTEN_POWDER); }

    /** 入っている材料から、どちらの工程か（材料がそろっていなくても種類で決める。GUI 表示用） */
    public static Mode modeOf(ItemStack in) {
        if (in.is(ModItems.TUNGSTEN_TRIOXIDE)) return Mode.REDUCE;
        if (in.is(ModItems.TUNGSTEN_POWDER)) return Mode.SINTER;
        return Mode.NONE;
    }

    private Mode mode() { return modeOf(container.getItem(SLOT_IN)); }
    private static ItemStack product(Mode m) {
        return m == Mode.REDUCE ? new ItemStack(ModItems.TUNGSTEN_POWDER) : new ItemStack(ModItems.TUNGSTEN_BAR);
    }

    @Override protected double workWatts() { return mode() == Mode.SINTER ? SINTER_WATTS : REDUCE_WATTS; }
    @Override protected int workTicks() { return mode() == Mode.SINTER ? SINTER_TICKS : REDUCE_TICKS; }

    @Override protected boolean canWork() {
        Mode m = mode();
        if (m == Mode.NONE) return false;
        ItemStack in = container.getItem(SLOT_IN), h2 = container.getItem(SLOT_H2);
        int needIn = m == Mode.SINTER ? SINTER_POWDER : 1, needH2 = m == Mode.SINTER ? SINTER_H2 : REDUCE_H2;
        return in.getCount() >= needIn && h2.is(ModItems.HYDROGEN_GAS) && h2.getCount() >= needH2
                && canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        Mode m = mode();
        container.getItem(SLOT_IN).shrink(m == Mode.SINTER ? SINTER_POWDER : 1);
        container.getItem(SLOT_H2).shrink(m == Mode.SINTER ? SINTER_H2 : REDUCE_H2);   // 水素は H2O になって出ていく（還元）／流しっぱなしで燃やす（焼結）
        merge(SLOT_OUT, product(m));
        container.setChanged();
    }

    /** 隣のガスホルダー（水素）から、スロットが 16 袋になるまで補充する */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack h2 = container.getItem(SLOT_H2);
        if (!h2.isEmpty() && !h2.is(ModItems.HYDROGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = H2_TARGET - container.getItem(SLOT_H2).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.HYDROGEN, want);
            if (n > 0) merge(SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, n));
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.hydrogen_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new HydrogenFurnaceMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
