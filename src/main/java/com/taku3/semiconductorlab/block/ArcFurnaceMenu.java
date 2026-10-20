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

import static com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity.*;

/** アーク炉の画面の中身。0＝珪石、1＝木炭、2＝黒鉛電極、3＝出口、4〜39＝プレイヤー */
public class ArcFurnaceMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ArcFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ArcFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ARC_FURNACE, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_QUARTZ, 8, 17) { @Override public boolean mayPlace(ItemStack s) { return isQuartz(s); } });
        addSlot(new Slot(container, SLOT_CARBON, 8, 35) { @Override public boolean mayPlace(ItemStack s) { return isCarbon(s); } });
        addSlot(new Slot(container, SLOT_ELECTRODE, 8, 53) { @Override public boolean mayPlace(ItemStack s) { return isElectrode(s); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.ARC_FURNACE); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isQuartz(stack)) ok = moveItemStackTo(stack, SLOT_QUARTZ, SLOT_QUARTZ + 1, false);
        else if (isCarbon(stack)) ok = moveItemStackTo(stack, SLOT_CARBON, SLOT_CARBON + 1, false);
        else if (isElectrode(stack)) ok = moveItemStackTo(stack, SLOT_ELECTRODE, SLOT_ELECTRODE + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
