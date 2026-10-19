package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity.*;

/** 酸水素バーナーの画面の中身。0＝石英（上）、1＝水素ボンベ、2＝酸素ボンベ（左下に並ぶ）、3＝溶融石英、4＝空ボンベ、5〜40＝プレイヤー */
public class OxyhydrogenBurnerMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    public static final int IN_X = 44, IN_Y = 17, H2_X = 26, O2_X = 62, GAS_Y = 53, OUT_X = 116, OUT_Y = 35, EMPTY_X = 148, EMPTY_Y = 53;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public OxyhydrogenBurnerMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public OxyhydrogenBurnerMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.OXYHYDROGEN_BURNER, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_IN, IN_X, IN_Y) { @Override public boolean mayPlace(ItemStack s) { return isInput(s); } });
        addSlot(new Slot(container, SLOT_H2, H2_X, GAS_Y) { @Override public boolean mayPlace(ItemStack s) { return s.is(ModItems.HYDROGEN_CYLINDER); } });
        addSlot(new Slot(container, SLOT_O2, O2_X, GAS_Y) { @Override public boolean mayPlace(ItemStack s) { return s.is(ModItems.OXYGEN_CYLINDER); } });
        addSlot(out(container, SLOT_OUT, OUT_X, OUT_Y));
        addSlot(out(container, SLOT_EMPTY, EMPTY_X, EMPTY_Y));
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    private static Slot out(Container c, int slot, int x, int y) {
        return new Slot(c, slot, x, y) { @Override public boolean mayPlace(ItemStack s) { return false; } };
    }

    public int getProgress() { return data.get(DATA_PROGRESS); }
    public int getTotal()    { return Math.max(1, data.get(DATA_TOTAL)); }
    public int getH2()       { return data.get(DATA_H2); }
    public int getO2()       { return data.get(DATA_O2); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.OXYHYDROGEN_BURNER); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isInput(stack)) ok = moveItemStackTo(stack, SLOT_IN, SLOT_IN + 1, false);
        else if (stack.is(ModItems.HYDROGEN_CYLINDER)) ok = moveItemStackTo(stack, SLOT_H2, SLOT_H2 + 1, false);
        else if (stack.is(ModItems.OXYGEN_CYLINDER)) ok = moveItemStackTo(stack, SLOT_O2, SLOT_O2 + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
