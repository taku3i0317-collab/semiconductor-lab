package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.ModMenus;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.SealingStationBlockEntity.*;

/** 封止台の画面の中身。0＝真空管の組立品（左）、1＝出口（右）、2〜37＝プレイヤー */
public class SealingStationMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public SealingStationMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(MENU_DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public SealingStationMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.SEALING_STATION, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_IN, 8, 35) { @Override public boolean mayPlace(ItemStack s) { return s.is(ModItems.VACUUM_TUBE_ASSEMBLY); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }
    /** 隣の真空容器の圧力（Pa）。容器がなければ -1 */
    public double getPressure() { return decodePa(data.get(DATA_PRESSURE)); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.SEALING_STATION); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (stack.is(ModItems.VACUUM_TUBE_ASSEMBLY)) ok = moveItemStackTo(stack, SLOT_IN, SLOT_IN + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
