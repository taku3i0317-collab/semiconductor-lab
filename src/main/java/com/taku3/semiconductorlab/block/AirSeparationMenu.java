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

import static com.taku3.semiconductorlab.block.AirSeparationBlockEntity.*;

/** 空気分離装置の画面の中身。0＝N2、1＝O2、2＝Ar（どれも出口）、3〜38＝プレイヤー */
public class AirSeparationMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AirSeparationMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public AirSeparationMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.AIR_SEPARATION, id);
        this.data = data;
        this.access = access;
        for (int s = 0; s < SLOT_COUNT; s++)
            addSlot(new Slot(container, s, 62, 17 + s * 18) { @Override public boolean mayPlace(ItemStack st) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()   { return data.get(ConsumerBlockEntity.DATA_KJ); }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.AIR_SEPARATION_UNIT); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || index >= PLAYER_START) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
