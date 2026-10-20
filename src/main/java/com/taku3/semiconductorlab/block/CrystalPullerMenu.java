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

import static com.taku3.semiconductorlab.block.CrystalPullerBlockEntity.*;

/** 引き上げ機の画面の中身。0＝精製 Ge、1＝種（W線 or 種結晶）、2＝水素、3＝出口、4〜39＝プレイヤー */
public class CrystalPullerMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CrystalPullerMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public CrystalPullerMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.CRYSTAL_PULLER, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_GE, 8, 17) { @Override public boolean mayPlace(ItemStack s) { return isGermanium(s); } });
        addSlot(new Slot(container, SLOT_SEED, 8, 35) { @Override public boolean mayPlace(ItemStack s) { return modeOf(s) != Mode.NONE; } });
        addSlot(new Slot(container, SLOT_H2, 8, 53) { @Override public boolean mayPlace(ItemStack s) { return s.is(ModItems.HYDROGEN_GAS); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()   { return data.get(ConsumerBlockEntity.DATA_KJ); }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.CRYSTAL_PULLER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isGermanium(stack)) ok = moveItemStackTo(stack, SLOT_GE, SLOT_GE + 1, false);
        else if (modeOf(stack) != Mode.NONE) ok = moveItemStackTo(stack, SLOT_SEED, SLOT_SEED + 1, false);
        else if (stack.is(ModItems.HYDROGEN_GAS)) ok = moveItemStackTo(stack, SLOT_H2, SLOT_H2 + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
