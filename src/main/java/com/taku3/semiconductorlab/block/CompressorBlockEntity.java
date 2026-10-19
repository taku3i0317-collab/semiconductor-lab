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
 * 圧縮機 [6-03]（電動モーターで回すピストン圧縮機）。隣のガスホルダーの低圧ガスを、鋼の空ボンベに約150気圧で 50mol 詰める。
 * 空ボンベ ×1 ＋ 隣のホルダーのガス 50mol ＋ 5kW×124秒 → 充填ボンベ ×1（ガスの種類はホルダーの中身で決まる）。
 * 電力量 620kJ ≒ 50mol を 1→150 気圧へ等温圧縮する仕事 nRT·ln(150) ≈ 621kJ（25℃）。
 * 袋詰め（低圧・約1mol）は手軽な運搬用として残る。ボンベは「たくさん・小さく」運ぶためのもの。
 */
public class CompressorBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final double WATTS = 5_000;
    public static final int TOTAL_TICKS = 20 * 124;
    public static final int MOL_PER_CYLINDER = 50;
    public static final int SLOT_EMPTY = 0, SLOT_OUT = 1, SLOT_COUNT = 2;

    /** この tick に使うガスホルダーとガスの種類（beforeWork で毎 tick 探し直す。セーブしない） */
    private GasHolderBlockEntity source;
    private GasType sourceGas = GasType.NONE;

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR, pos, state, SLOT_COUNT, 50_000, WATTS);
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TOTAL_TICKS; }

    /** 隣（6方向）から「50mol 以上あり、出口にそのボンベを置ける」ホルダーを1つ選ぶ */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        source = null;
        sourceGas = GasType.NONE;
        ItemStack out = container.getItem(SLOT_OUT);
        for (Direction d : Direction.values()) {
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity h)) continue;
            GasType g = h.getGasType();
            if (g.cylinderItem() == null || h.getAmount() < MOL_PER_CYLINDER) continue;
            if (!canMerge(out, new ItemStack(g.cylinderItem()))) continue;
            source = h;
            sourceGas = g;
            return;
        }
    }

    @Override protected boolean canWork() {
        return container.getItem(SLOT_EMPTY).is(ModItems.EMPTY_GAS_CYLINDER) && source != null && !source.isRemoved();
    }

    @Override protected void finishWork() {
        int got = source.extract(sourceGas, MOL_PER_CYLINDER);
        if (got < MOL_PER_CYLINDER) return;   // 同じ tick で確認済みなので起きないはず（念のため：詰めずに終わる）
        container.getItem(SLOT_EMPTY).shrink(1);
        merge(SLOT_OUT, new ItemStack(sourceGas.cylinderItem()));
        container.setChanged();
    }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.compressor"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CompressorMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
