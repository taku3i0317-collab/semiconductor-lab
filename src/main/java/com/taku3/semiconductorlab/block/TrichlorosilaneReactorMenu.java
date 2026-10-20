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

import static com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity.*;

/** トリクロロシラン反応器の画面の中身。0＝金属シリコン、1＝塩化水素、2＝蒸留するトリクロロシラン、3＝出口、4＝水素の出口、5〜40＝プレイヤー */
public class TrichlorosilaneReactorMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public TrichlorosilaneReactorMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT + 1), ContainerLevelAccess.NULL);
    }

    public TrichlorosilaneReactorMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.TRICHLOROSILANE_REACTOR, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_SI, 8, 17) { @Override public boolean mayPlace(ItemStack s) { return isSilicon(s); } });
        addSlot(new Slot(container, SLOT_HCL, 8, 35) { @Override public boolean mayPlace(ItemStack s) { return isHcl(s); } });
        addSlot(new Slot(container, SLOT_DISTILL, 8, 53) { @Override public boolean mayPlace(ItemStack s) { return isTcs(s); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 26) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addSlot(new Slot(container, SLOT_H2, 78, 50) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }
    public boolean isDistilling() { return data.get(DATA_MODE) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.TRICHLOROSILANE_REACTOR); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isSilicon(stack)) ok = moveItemStackTo(stack, SLOT_SI, SLOT_SI + 1, false);
        else if (isHcl(stack)) ok = moveItemStackTo(stack, SLOT_HCL, SLOT_HCL + 1, false);
        else if (isTcs(stack)) ok = moveItemStackTo(stack, SLOT_DISTILL, SLOT_DISTILL + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
