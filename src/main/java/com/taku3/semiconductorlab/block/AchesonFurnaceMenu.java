package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.AchesonFurnaceBlockEntity.*;

/** アチソン炉の画面の中身。0＝炭素（石炭・木炭、左上）、1＝砂（左下）、2＝出口、3〜38＝プレイヤー */
public class AchesonFurnaceMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AchesonFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA), ContainerLevelAccess.NULL);
    }

    public AchesonFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ACHESON_FURNACE, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_CARBON, 8, 17) { @Override public boolean mayPlace(ItemStack s) { return isCarbon(s); } });
        addSlot(new Slot(container, SLOT_SAND, 8, 53) { @Override public boolean mayPlace(ItemStack s) { return isSand(s); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }
    public int getTemperature() { return data.get(DATA_TEMP); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.ACHESON_FURNACE); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isCarbon(stack)) ok = moveItemStackTo(stack, SLOT_CARBON, SLOT_CARBON + 1, false);
        else if (isSand(stack)) ok = moveItemStackTo(stack, SLOT_SAND, SLOT_SAND + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
