package com.taku3.semiconductorlab.machine;

import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * すべての機械で共通の「画面の中身」（IMP-28。前は機械ごとに約60行の Menu クラスがあった）。
 * スロットの並びは {@link MachineLayout}、数値は BE の ContainerData（同期される整数の列）から読む。
 * 並び：0〜（機械のスロット）→ プレイヤーの持ち物 36 個。
 */
public class MachineMenu extends AbstractContainerMenu {
    private final Machine<?> machine;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Block block;
    private final int playerStart;

    /** クライアント側（中身はサーバーから同期される） */
    public MachineMenu(Machine<?> machine, int id, Inventory inv) {
        this(machine, id, inv, new SimpleContainer(machine.layout.containerSize()), new SimpleContainerData(machine.layout.dataCount()),
                ContainerLevelAccess.NULL, machine.block);
    }

    public MachineMenu(Machine<?> machine, int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access, Block block) {
        super(machine.menu, id);
        this.machine = machine;
        this.data = data;
        this.access = access;
        this.block = block;
        for (MachineLayout.SlotDef s : machine.layout.slots()) {
            addSlot(new Slot(container, s.index(), s.x(), s.y()) {
                @Override public boolean mayPlace(ItemStack st) { return s.accepts(data::get, st); }
                @Override public boolean isActive() { return s.active(data::get); }
                @Override public int getMaxStackSize() { return s.maxStack() > 0 ? s.maxStack() : super.getMaxStackSize(); }
            });
        }
        playerStart = slots.size();
        addStandardInventorySlots(inv, 8, machine.layout.inventoryY());
        addDataSlots(data);
    }

    public Machine<?> machine() { return machine; }
    /** 同期された数値 i 番（番号は BE の DATA_… 定数） */
    public int get(int i) { return data.get(i); }

    // ConsumerBlockEntity（電気を使う機械）の共通の数値
    public int getProgress()  { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()     { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()     { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()  { return data.get(ConsumerBlockEntity.DATA_KJ); }
    public boolean isReady()  { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    /** 機械のスロット i 番（レイアウトに書いた順）に入っている物 */
    public ItemStack machineItem(int containerIndex) {
        for (int i = 0; i < playerStart; i++) if (slots.get(i).getContainerSlot() == containerIndex) return slots.get(i).getItem();
        return ItemStack.EMPTY;
    }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, block); }

    /** 画面のボタン（サーバー側。クライアントは MultiPlayerGameMode.handleInventoryButtonClick で送る）。BE の onButton へ渡す */
    @Override public boolean clickMenuButton(Player player, int id) {
        return access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof MachineBE be && be.onButton(player, id), false);
    }

    /**
     * シフトクリック：機械 → 持ち物。持ち物 → 「入れられる最初の入口」だけ
     * （前の Menu と同じ。いっぱいでも次の入口へは回さない：CZ の多結晶 Si が種のスロットへ入らないように）
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok = false;
        if (index < playerStart) ok = moveItemStackTo(stack, playerStart, slots.size(), true);
        else {
            for (int i = 0; i < playerStart; i++) {
                MachineLayout.SlotDef s = machine.layout.slots().get(i);
                if (s.accepts(data::get, stack) && s.active(data::get)) { ok = moveItemStackTo(stack, i, i + 1, false); break; }
            }
        }
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
