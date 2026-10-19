package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.GasHolderBlockEntity.*;

/** ガスホルダーの画面の中身。0＝袋詰めガスの入口（左）、1＝出口（右、1袋ずつ）、2〜37＝プレイヤー */
public class GasHolderMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** クライアント側用（中身はサーバーから同期される） */
    public GasHolderMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public GasHolderMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.GAS_HOLDER, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_IN, 26, 35) {
            @Override public boolean mayPlace(ItemStack s) { return GasType.of(s) != GasType.NONE; }
        });
        addSlot(new Slot(container, SLOT_OUT, 134, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public GasType getGasType() { return GasType.byIndex(data.get(DATA_TYPE)); }
    public int getAmount()      { return data.get(DATA_AMOUNT); }
    public boolean justBanged() { return data.get(DATA_BANG) > 0; }

    @Override
    public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.GAS_HOLDER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (GasType.of(stack) != GasType.NONE) ok = moveItemStackTo(stack, SLOT_IN, SLOT_IN + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
