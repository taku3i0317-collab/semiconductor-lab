package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.taku3.semiconductorlab.block.ReverberatoryFurnaceBlockEntity.*;

/** 反射炉の画面の中身。0＝鉱石（左上）、1＝燃料（左下）、2＝主生成物、3＝SO2、4＝Ge精鉱、5＝インジウム、6〜41＝プレイヤー */
public class ReverberatoryFurnaceMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    // 出口 2x2 の左上（主生成物）。右＝SO2、下段＝副産物
    public static final int OUT_X = 104, OUT_Y = 22;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ReverberatoryFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ReverberatoryFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.REVERBERATORY_FURNACE, id);
        this.data = data;
        this.access = access;
        addSlot(new Slot(container, SLOT_IN, 44, 17) { @Override public boolean mayPlace(ItemStack s) { return isInput(s); } });
        addSlot(new Slot(container, SLOT_FUEL, 44, 53) { @Override public boolean mayPlace(ItemStack s) { return isFuel(s); } });
        addSlot(out(container, SLOT_OUT, OUT_X, OUT_Y));
        addSlot(out(container, SLOT_SO2, OUT_X + 22, OUT_Y));
        addSlot(out(container, SLOT_BY_GE, OUT_X, OUT_Y + 22));
        addSlot(out(container, SLOT_BY_IN, OUT_X + 22, OUT_Y + 22));
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    private static Slot out(Container c, int slot, int x, int y) {
        return new Slot(c, slot, x, y) { @Override public boolean mayPlace(ItemStack s) { return false; } };
    }

    public int getBurn()      { return data.get(DATA_BURN); }
    public int getBurnTotal() { return Math.max(1, data.get(DATA_BURN_TOTAL)); }
    public int getProgress()  { return data.get(DATA_PROGRESS); }
    public int getTotal()     { return Math.max(1, data.get(DATA_TOTAL)); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.REVERBERATORY_FURNACE); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (isInput(stack)) ok = moveItemStackTo(stack, SLOT_IN, SLOT_IN + 1, false);
        else if (isFuel(stack)) ok = moveItemStackTo(stack, SLOT_FUEL, SLOT_FUEL + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
