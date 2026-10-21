package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Transport;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ベルトコンベアの駆動 [14-22]（1913 フォードのハイランドパーク工場の流れ作業 ⚠️。IMP4-01、O-029 仮決定の案A）：電動モーター [4-08] の付いた頭。
 * 横の1つに、自分から遠ざかる向きのベルトをつなぐ。駆動の反対側（後ろ）の機械の出口から物を取り（Fabric Transfer API の ItemStorage、machine/MachineItems）、
 * ベルトを1秒に1マスずつ進めて、ベルトの終わりの先の機械の入口へ入れる。ベルトは向きに沿って曲がれる（最長 {@link #MAX_BELT} マス）。
 * 電気はベルト1マスあたり {@link #WATTS_PER_BELT} W ⚠️。電気がある間だけ動く。レッドストーンは使わない。
 */
public class ConveyorDriveBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS_PER_BELT = 5;
    public static final int MAX_BELT = 64;
    public static final int DATA_BELTS = DATA_COUNT, DATA_ITEMS = DATA_COUNT + 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT + 2)
            .right("power", 17, 1, "gui.semiconductor-lab.conveyor_drive.power").samples("500.0 kW")
            .right("belt", 28, 2, "gui.semiconductor-lab.conveyor_drive.belt").samples("64", "64")
            .right("hint", 50, 2, "gui.semiconductor-lab.conveyor_drive.no_belt", "gui.semiconductor-lab.conveyor_drive.need_power",
                    "gui.semiconductor-lab.conveyor_drive.running")
            .bottom("recipe", "gui.semiconductor-lab.conveyor_drive.recipe")
            .build();

    private int belts, items;

    public ConveyorDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Transport.CONVEYOR_DRIVE.type, pos, state, 0, 2_000, WATTS_PER_BELT * MAX_BELT);
    }

    /** 駆動につながったベルトの並び（始め→終わり）。最初のベルトは、駆動の横で駆動から遠ざかる向きのもの */
    public static List<BlockPos> belts(Level level, BlockPos drive) {
        List<BlockPos> list = new ArrayList<>();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockState s = level.getBlockState(drive.relative(d));
            if (s.getBlock() instanceof ConveyorBeltBlock && s.getValue(BlockStateProperties.HORIZONTAL_FACING) == d) {
                BlockPos p = drive.relative(d);
                while (list.size() < MAX_BELT && !list.contains(p)) {
                    BlockState b = level.getBlockState(p);
                    if (!(b.getBlock() instanceof ConveyorBeltBlock)) break;
                    list.add(p);
                    p = p.relative(b.getValue(BlockStateProperties.HORIZONTAL_FACING));
                }
                break;
            }
        }
        return list;
    }

    private static ConveyorBeltBlockEntity belt(Level level, BlockPos p) { return level.getBlockEntity(p) instanceof ConveyorBeltBlockEntity b ? b : null; }

    /**
     * 1秒ぶん進める（テストからも呼ぶ）：①終わりのベルトの物を先の機械の入口へ ②ベルトの物を1マスずつ前へ ③後ろの機械の出口から最初のベルトへ1スタック
     * @return 動いた物の数（スタックの数）
     */
    public static int step(Level level, BlockPos drive) {
        List<BlockPos> list = belts(level, drive);
        if (list.isEmpty()) return 0;
        int moved = 0;
        BlockPos last = list.get(list.size() - 1);
        Direction out = level.getBlockState(last).getValue(BlockStateProperties.HORIZONTAL_FACING);
        ConveyorBeltBlockEntity end = belt(level, last);
        if (end != null && !end.getItem().isEmpty()) {
            Storage<ItemVariant> dest = ItemStorage.SIDED.find(level, last.relative(out), out.getOpposite());
            if (dest != null) {
                ItemStack st = end.getItem();
                try (Transaction tx = Transaction.openOuter()) {
                    long n = dest.insert(ItemVariant.of(st), st.getCount(), tx);
                    tx.commit();
                    if (n > 0) { ItemStack rest = st.copy(); rest.shrink((int) n); end.setItem(rest); moved++; }
                }
            }
        }
        for (int i = list.size() - 2; i >= 0; i--) {
            ConveyorBeltBlockEntity a = belt(level, list.get(i)), b = belt(level, list.get(i + 1));
            if (a != null && b != null && !a.getItem().isEmpty() && b.getItem().isEmpty()) { b.setItem(a.getItem()); a.setItem(ItemStack.EMPTY); moved++; }
        }
        ConveyorBeltBlockEntity first = belt(level, list.get(0));
        Direction d = Direction.getApproximateNearest(list.get(0).getX() - drive.getX(), 0, list.get(0).getZ() - drive.getZ());
        if (first != null && first.getItem().isEmpty()) {
            Storage<ItemVariant> src = ItemStorage.SIDED.find(level, drive.relative(d.getOpposite()), d);
            if (src != null) {
                for (StorageView<ItemVariant> v : src.nonEmptyViews()) {
                    ItemVariant r = v.getResource();
                    try (Transaction tx = Transaction.openOuter()) {
                        long n = src.extract(r, r.toStack().getMaxStackSize(), tx);
                        if (n > 0) { tx.commit(); first.setItem(r.toStack((int) n)); moved++; break; }
                    }
                }
            }
        }
        return moved;
    }

    @Override protected void beforeWork(Level level, BlockPos pos) {
        List<BlockPos> list = belts(level, pos);
        belts = list.size();
        items = 0;
        for (BlockPos p : list) { ConveyorBeltBlockEntity b = belt(level, p); if (b != null && !b.getItem().isEmpty()) items++; }
    }

    @Override protected double workWatts() { return WATTS_PER_BELT * Math.max(1, belts); }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return belts > 0; }
    @Override protected void finishWork() { if (level != null) step(level, worldPosition); }

    @Override protected int extraDataCount() { return 2; }
    @Override protected int extraData(int i) { return i == 0 ? belts : items; }
}
