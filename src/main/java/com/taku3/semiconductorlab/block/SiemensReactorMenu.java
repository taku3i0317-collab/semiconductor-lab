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

import static com.taku3.semiconductorlab.block.SiemensReactorBlockEntity.*;

/** シーメンス炉の画面の中身。0＝トリクロロシラン、1＝水素、2＝多結晶シリコンの出口、3＝塩化水素の出口、4〜39＝プレイヤー */
public class SiemensReactorMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public SiemensReactorMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public SiemensReactorMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.SIEMENS_REACTOR, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_TCS, 8, 26) { @Override public boolean mayPlace(ItemStack s) { return isTcs(s); } });
        addSlot(new Slot(container, SLOT_H2, 8, 50) { @Override public boolean mayPlace(ItemStack s) { return isHydrogen(s); } });
        addSlot(new Slot(container, SLOT_OUT, 78, 26) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addSlot(new Slot(container, SLOT_HCL, 78, 50) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.SIEMENS_REACTOR); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isTcs(stack)) ok = moveItemStackTo(stack, SLOT_TCS, SLOT_TCS + 1, false);
        else if (isHydrogen(stack)) ok = moveItemStackTo(stack, SLOT_H2, SLOT_H2 + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
