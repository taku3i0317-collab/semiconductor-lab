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

import static com.taku3.semiconductorlab.block.CzPullerBlockEntity.*;

/** CZ 引き上げ機の画面の中身。0＝多結晶シリコン、1＝種（多結晶シリコン or 種結晶）、2＝ドーパント、3＝石英るつぼ、4＝アルゴン、5＝出口、6〜41＝プレイヤー */
public class CzPullerMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CzPullerMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public CzPullerMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.CZ_PULLER, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_POLY, 8, 17) { @Override public boolean mayPlace(ItemStack s) { return isPoly(s); } });
        addSlot(new Slot(container, SLOT_SEED, 8, 35) { @Override public boolean mayPlace(ItemStack s) { return modeOf(s) != Mode.NONE; } });
        addSlot(new Slot(container, SLOT_DOPANT, 8, 53) { @Override public boolean mayPlace(ItemStack s) { return com.taku3.semiconductorlab.Dopant.of(s) != null; } });
        addSlot(new Slot(container, SLOT_CRUCIBLE, 30, 17) { @Override public boolean mayPlace(ItemStack s) { return isCrucible(s); } });
        addSlot(new Slot(container, SLOT_AR, 30, 53) { @Override public boolean mayPlace(ItemStack s) { return isArgon(s); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()   { return data.get(ConsumerBlockEntity.DATA_KJ); }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.CZ_PULLER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isPoly(stack)) ok = moveItemStackTo(stack, SLOT_POLY, SLOT_POLY + 1, false);
        else if (modeOf(stack) != Mode.NONE) ok = moveItemStackTo(stack, SLOT_SEED, SLOT_SEED + 1, false);
        else if (com.taku3.semiconductorlab.Dopant.of(stack) != null) ok = moveItemStackTo(stack, SLOT_DOPANT, SLOT_DOPANT + 1, false);
        else if (isCrucible(stack)) ok = moveItemStackTo(stack, SLOT_CRUCIBLE, SLOT_CRUCIBLE + 1, false);
        else if (isArgon(stack)) ok = moveItemStackTo(stack, SLOT_AR, SLOT_AR + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
