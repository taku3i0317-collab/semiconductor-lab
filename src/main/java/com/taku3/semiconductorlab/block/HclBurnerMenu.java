package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.taku3.semiconductorlab.block.HclBurnerBlockEntity.*;

/** 塩酸合成炉の画面の中身。0＝水素、1＝塩素（左下）、2＝水バケツ（左上）、3＝塩酸、4＝空ボンベ、5＝空バケツ、6〜41＝プレイヤー */
public class HclBurnerMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    public static final int H2_X = 26, CL2_X = 62, GAS_Y = 53, WATER_X = 44, WATER_Y = 17, OUT_X = 116, OUT_Y = 35, EMPTY_X = 134, BUCKET_X = 152, EMPTIES_Y = 59;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public HclBurnerMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public HclBurnerMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.HCL_BURNER, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_H2, H2_X, GAS_Y) { @Override public boolean mayPlace(ItemStack s) { return isHydrogen(s); } });
        addSlot(new Slot(container, SLOT_CL2, CL2_X, GAS_Y) { @Override public boolean mayPlace(ItemStack s) { return isChlorine(s); } });
        addSlot(new Slot(container, SLOT_WATER, WATER_X, WATER_Y) { @Override public boolean mayPlace(ItemStack s) { return s.is(Items.WATER_BUCKET); } });
        addSlot(out(container, SLOT_OUT, OUT_X, OUT_Y));
        addSlot(out(container, SLOT_EMPTY, EMPTY_X, EMPTIES_Y));
        addSlot(out(container, SLOT_BUCKET, BUCKET_X, EMPTIES_Y));
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    private static Slot out(Container c, int slot, int x, int y) {
        return new Slot(c, slot, x, y) { @Override public boolean mayPlace(ItemStack s) { return false; } };
    }

    public int getProgress() { return data.get(DATA_PROGRESS); }
    public int getTotal()    { return Math.max(1, data.get(DATA_TOTAL)); }
    public int getH2()       { return data.get(DATA_H2); }
    public int getCl2()      { return data.get(DATA_CL2); }
    public int getWater()    { return data.get(DATA_WATER); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.HCL_BURNER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isHydrogen(stack)) ok = moveItemStackTo(stack, SLOT_H2, SLOT_H2 + 1, false);
        else if (isChlorine(stack)) ok = moveItemStackTo(stack, SLOT_CL2, SLOT_CL2 + 1, false);
        else if (stack.is(Items.WATER_BUCKET)) ok = moveItemStackTo(stack, SLOT_WATER, SLOT_WATER + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
