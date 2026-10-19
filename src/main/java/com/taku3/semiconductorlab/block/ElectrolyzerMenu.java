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
import net.minecraft.world.item.Items;

import static com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity.*;

/** 電解槽の画面の中身。0＝水バケツ（左上）、1＝硫酸（左下）、2＝水素（出口）、3＝酸素（出口）、4〜39＝プレイヤー */
public class ElectrolyzerMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** クライアント側用（中身はサーバーから同期される） */
    public ElectrolyzerMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ElectrolyzerMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ELECTROLYZER, id);
        this.data = data;
        this.access = access;
        // 水バケツ（左上）。使い終わると空バケツが残るので取り出す
        addSlot(new Slot(container, SLOT_WATER, 8, 17) {
            @Override public boolean mayPlace(ItemStack s) { return s.is(Items.WATER_BUCKET); }
        });
        // 電解質の硫酸（左下）。消耗しない
        addSlot(new Slot(container, SLOT_ACID, 8, 53) {
            @Override public boolean mayPlace(ItemStack s) { return ElectrolyzerBlockEntity.isElectrolyte(s); }
        });
        // 出口：水素（上）・酸素（下）。取り出し専用
        addSlot(new Slot(container, SLOT_H2, 78, 26) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addSlot(new Slot(container, SLOT_O2, 78, 48) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress()   { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()      { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()      { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()   { return data.get(ConsumerBlockEntity.DATA_KJ); }
    public boolean isReady()   { return data.get(ConsumerBlockEntity.DATA_READY) != 0; }

    @Override
    public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.ELECTROLYZER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (stack.is(Items.WATER_BUCKET)) ok = moveItemStackTo(stack, SLOT_WATER, SLOT_WATER + 1, false);
        else if (ElectrolyzerBlockEntity.isElectrolyte(stack)) ok = moveItemStackTo(stack, SLOT_ACID, SLOT_ACID + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
