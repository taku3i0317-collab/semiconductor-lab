package com.taku3.semiconductorlab.machine;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * 機械をホッパーとほかの MOD につなぐ（S-027 ①、Fabric Transfer API の ItemStorage）。画面のある機械は {@link Machine} が登録のとき自動で呼ぶ。
 * どの面からも：入口のスロット（LAYOUT の in）には入れられるだけ（その物を受け付けるときだけ、スロットの上限も守る）、
 * 出口のスロット（LAYOUT の out）からは取り出せるだけ。だから上のホッパーで材料を入れ、下のホッパーで出来た物を取れる。
 * 画面の数値で変わるスロット（組立台の製品ごとの部品）は、今の数値で使うスロットだけ。物の口（{@link PortBlockEntity}）は制御ブロックの分を渡す
 */
public final class MachineItems {
    private MachineItems() {}

    static void register(BlockEntityType<?> type, MachineLayout layout) {
        ItemStorage.SIDED.registerForBlockEntity((be, dir) -> be instanceof MachineBE m ? storage(m, layout) : null, type);
    }

    /** 物の口：組み上がっていれば制御ブロックの ItemStorage */
    public static void registerPort(BlockEntityType<PortBlockEntity> type) {
        ItemStorage.SIDED.registerForBlockEntity((be, dir) -> {
            MachineBE c = be.controller();
            return c == null ? null : ItemStorage.SIDED.find(c.getLevel(), c.getBlockPos(), dir);
        }, type);
    }

    public static Storage<ItemVariant> storage(MachineBE m, MachineLayout layout) {
        List<Storage<ItemVariant>> parts = new ArrayList<>();
        for (MachineLayout.SlotDef s : layout.slots()) parts.add(new SlotStorage(m, s));
        return new CombinedStorage<>(parts);
    }

    private static final class SlotStorage extends SingleStackStorage {
        private final MachineBE m;
        private final MachineLayout.SlotDef def;
        SlotStorage(MachineBE m, MachineLayout.SlotDef def) { this.m = m; this.def = def; }

        private boolean active() { return def.active(i -> m.getMenuData().get(i)); }
        @Override protected ItemStack getStack() { return m.getContainer().getItem(def.index()); }
        @Override protected void setStack(ItemStack s) { m.getContainer().setItem(def.index(), s); }
        @Override protected boolean canInsert(ItemVariant v) { return active() && def.accepts(i -> m.getMenuData().get(i), v.toStack()); }
        @Override protected boolean canExtract(ItemVariant v) { return def.isOutput(); }
        @Override protected int getCapacity(ItemVariant v) { return def.maxStack() > 0 ? Math.min(def.maxStack(), super.getCapacity(v)) : super.getCapacity(v); }
        @Override protected void onFinalCommit() { m.getContainer().setChanged(); }
    }
}
