package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.item.FoupItem;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineBE;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ロードポート（v1.0 #4 自動搬送、IMP4-01、O-026 仮決定の案A。本物：300mm の工場の装置の前の台＋ウェハー搬送ロボット＝EFEM、1990年代末〜 ⚠️）：
 * 工程の機械の隣に置き、「入れる FOUP」から機械のウェハーの入口へ1回分ずつ入れ、機械の出口のウェハーを「出す FOUP」へ戻す。
 * 機械の入口・出口は、その機械の画面のスロットの並び（MachineLayout）から読む（入口＝その物を受け付けるスロット、出口＝取り出すだけのスロット）。
 * 出口の物は出す FOUP にだけ戻すので、同じ機械にまた入ることはない。次の機械へ運ぶのは人（出す FOUP を次のロードポートの入れる FOUP に）。電気 {@link #WATTS} W、1秒ごと。レッドストーンは使わない。
 * ⚠️ 本物の FOUP は窒素で満たし、装置の中も密閉したまま受け渡す。ここでは中身の受け渡しだけ（load_port.placeholder）。
 */
public class LoadPortBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 50;
    public static final int SLOT_IN_FOUP = 0, SLOT_OUT_FOUP = 1, SLOT_COUNT = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN_FOUP, 8, 26, s -> s.getItem() instanceof FoupItem)
            .in(SLOT_OUT_FOUP, 8, 50, s -> s.getItem() instanceof FoupItem)
            .right("power", 17, 1, "gui.semiconductor-lab.load_port.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.load_port.need_foup", "gui.semiconductor-lab.load_port.no_machine",
                    "gui.semiconductor-lab.load_port.need_power", "gui.semiconductor-lab.load_port.running")
            .bottom("recipe", "gui.semiconductor-lab.load_port.recipe")
            .build();

    public LoadPortBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.LOAD_PORT.type, pos, state, SLOT_COUNT, 2_000, 200);
    }

    /**
     * 隣の工程の機械（スロットの並びを持つ MachineBE、ロードポートどうしは除く）。なければ null。
     * 隣がマルチブロックの物の口（Multiblock.ItemPort、A の O-034）なら、その本体（組み上がっていなければ使わない）
     */
    public BlockEntity machine() {
        if (level == null) return null;
        for (Direction d : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(d));
            if (be instanceof com.taku3.semiconductorlab.machine.Multiblock.ItemPort port) {
                MachineBE c = port.controller();
                be = c instanceof BlockEntity cb ? cb : null;
            }
            if (be instanceof MachineBE && !(be instanceof LoadPortBlockEntity)) {
                Machine<?> m = Machine.of(be.getType());
                if (m != null && m.layout != null) return be;
            }
        }
        return null;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }

    @Override protected boolean canWork() {
        return machine() != null && (container.getItem(SLOT_IN_FOUP).getItem() instanceof FoupItem || container.getItem(SLOT_OUT_FOUP).getItem() instanceof FoupItem);
    }

    @Override protected void finishWork() {
        BlockEntity be = machine();
        if (be == null) return;
        transfer(Machine.of(be.getType()).layout, ((MachineBE) be).getContainer(), container.getItem(SLOT_IN_FOUP), container.getItem(SLOT_OUT_FOUP));
        container.setChanged();
    }

    /**
     * 1回分の受け渡し（テストからも呼ぶ）：①機械の出口のウェハーを出す FOUP へ ②入れる FOUP のウェハーを、受け付ける入口（空か同じ物）へ1回分
     * @return 動かした枚数
     */
    public static int transfer(MachineLayout layout, Container mc, ItemStack inFoup, ItemStack outFoup) {
        int moved = 0;
        if (outFoup.getItem() instanceof FoupItem) {
            for (MachineLayout.SlotDef sd : layout.slots()) {
                if (!sd.isOutput()) continue;
                ItemStack st = mc.getItem(sd.index());
                if (st.isEmpty() || !FoupItem.isWafer(st)) continue;
                int n = FoupItem.add(outFoup, st);
                if (n > 0) { st.shrink(n); moved += n; }
            }
        }
        if (inFoup.getItem() instanceof FoupItem) {
            ItemStack w = FoupItem.contents(inFoup);
            if (!w.isEmpty()) {
                for (MachineLayout.SlotDef sd : layout.slots()) {
                    if (sd.isOutput() || !sd.accepts(i -> 0, w)) continue;
                    ItemStack cur = mc.getItem(sd.index());
                    if (!cur.isEmpty() && !ItemStack.isSameItemSameComponents(cur, w)) continue;
                    int room = Math.min(8, w.getMaxStackSize()) - cur.getCount();
                    int n = Math.min(room, w.getCount());
                    if (n <= 0) continue;
                    ItemStack put = FoupItem.take(inFoup, n);
                    if (cur.isEmpty()) mc.setItem(sd.index(), put); else cur.grow(put.getCount());
                    moved += put.getCount();
                    break;
                }
            }
        }
        if (moved > 0) mc.setChanged();
        return moved;
    }
}
