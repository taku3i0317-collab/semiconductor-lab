package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** スロット0＝ゲルマニウム（最大8）、1＝ドーパント、2〜37＝プレイヤー */
public class ZoneRefiningFurnaceMenu extends AbstractContainerMenu {
    public static final int GE_SLOT = 0, DOPANT_SLOT = 1, PLAYER_START = 2;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ZoneRefiningFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(2), new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    public ZoneRefiningFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ZONE_REFINING_FURNACE, id);
        this.data = data;
        this.access = access;
        // ゲルマニウム（左）。8個まで＝ドーパント1個に対して「微量」を表現
        addSlot(new Slot(container, GE_SLOT, 56, 35) {
            @Override public boolean mayPlace(ItemStack s) { return ZoneRefiningFurnaceBlockEntity.isGermanium(s); }
            @Override public int getMaxStackSize() { return 8; }
        });
        // ドーパント（右）：リン＝n型、インジウム＝p型
        addSlot(new Slot(container, DOPANT_SLOT, 104, 35) {
            @Override public boolean mayPlace(ItemStack s) { return ZoneRefiningFurnaceBlockEntity.isDopant(s); }
        });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress() { return data.get(ZoneRefiningFurnaceBlockEntity.DATA_PROGRESS); }
    public int getTotal()    { return Math.max(1, data.get(ZoneRefiningFurnaceBlockEntity.DATA_TOTAL)); }
    public int getNines()    { return data.get(ZoneRefiningFurnaceBlockEntity.DATA_NINES); }
    public int getMode()     { return data.get(ZoneRefiningFurnaceBlockEntity.DATA_MODE); }
    public boolean isGeEmpty() { return getSlot(GE_SLOT).getItem().isEmpty(); }
    public boolean hasDopant() { return !getSlot(DOPANT_SLOT).getItem().isEmpty(); }

    @Override
    public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.ZONE_REFINING_FURNACE); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (ZoneRefiningFurnaceBlockEntity.isGermanium(stack)) ok = moveItemStackTo(stack, GE_SLOT, GE_SLOT + 1, false);
        else if (ZoneRefiningFurnaceBlockEntity.isDopant(stack)) ok = moveItemStackTo(stack, DOPANT_SLOT, DOPANT_SLOT + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}