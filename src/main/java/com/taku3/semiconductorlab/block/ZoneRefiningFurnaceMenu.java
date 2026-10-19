package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
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

/** 画面の中身（サーバー・クライアント両方で作られる）。スロット0＝ゲルマニウム、1〜36＝プレイヤー */
public class ZoneRefiningFurnaceMenu extends AbstractContainerMenu {
    public static final int GE_SLOT = 0;
    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** クライアント側（画面を開いたとき自動で呼ばれる） */
    public ZoneRefiningFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(1), new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    /** サーバー側（BlockEntity から呼ばれる） */
    public ZoneRefiningFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ZONE_REFINING_FURNACE, id);
        this.container = container;
        this.data = data;
        this.access = access;

        // ゲルマニウムだけ置けるスロット（画面の真ん中）
        addSlot(new Slot(container, GE_SLOT, 80, 35) {
            @Override public boolean mayPlace(ItemStack s) { return ZoneRefiningFurnaceBlockEntity.isGermanium(s); }
        });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress() { return data.get(ZoneRefiningFurnaceBlockEntity.DATA_PROGRESS); }
    public int getTotal()    { return Math.max(1, data.get(ZoneRefiningFurnaceBlockEntity.DATA_TOTAL)); }
    public int getNines()    { return data.get(ZoneRefiningFurnaceBlockEntity.DATA_NINES); }
    public boolean isEmptySlot() { return getSlot(GE_SLOT).getItem().isEmpty(); }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ZONE_REFINING_FURNACE);
    }

    /** Shift+クリックの移動 */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int playerStart = 1, playerEnd = slots.size();
        if (index == GE_SLOT) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else {
            if (!ZoneRefiningFurnaceBlockEntity.isGermanium(stack) || !moveItemStackTo(stack, GE_SLOT, GE_SLOT + 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}