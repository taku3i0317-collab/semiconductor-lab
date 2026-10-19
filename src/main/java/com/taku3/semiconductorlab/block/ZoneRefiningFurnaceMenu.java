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

import static com.taku3.semiconductorlab.block.ZoneRefiningFurnaceBlockEntity.*;

/** スロット0＝ゲルマニウム（最大8、左）、1＝ドーパント（右）、2＝黒鉛ボート（中央）、3〜38＝プレイヤー */
public class ZoneRefiningFurnaceMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = SLOT_COUNT;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ZoneRefiningFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA), ContainerLevelAccess.NULL);
    }

    public ZoneRefiningFurnaceMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ZONE_REFINING_FURNACE, id);
        this.data = data;
        this.access = access;
        // ゲルマニウム（左）。8個まで＝ドーパント1個に対して「微量」を表現
        addSlot(new Slot(container, GE_SLOT, 56, 35) {
            @Override public boolean mayPlace(ItemStack s) { return isGermanium(s); }
            @Override public int getMaxStackSize() { return 8; }
        });
        // ドーパント（右）：リン＝n型、インジウム＝p型
        addSlot(new Slot(container, DOPANT_SLOT, 104, 35) {
            @Override public boolean mayPlace(ItemStack s) { return isDopant(s); }
        });
        // 黒鉛ボート（中央）。消耗しない。粘土黒鉛＝9Nまで、人造黒鉛＝10Nまで
        addSlot(new Slot(container, BOAT_SLOT, 80, 35) {
            @Override public boolean mayPlace(ItemStack s) { return isBoat(s); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getProgress() { return data.get(ConsumerBlockEntity.DATA_PROGRESS); }
    public int getTotal()    { return Math.max(1, data.get(ConsumerBlockEntity.DATA_TOTAL)); }
    public double getKw()    { return data.get(ConsumerBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getNines()    { return data.get(DATA_NINES); }
    public int getMode()     { return data.get(DATA_MODE); }
    /** ボートで届く上限の「9の数」。0＝ボートなし */
    public int getBoatCap()  { return data.get(DATA_BOAT_CAP); }
    public int getRouteCap() { return data.get(DATA_ROUTE_CAP); }
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
        else if (isGermanium(stack)) ok = moveItemStackTo(stack, GE_SLOT, GE_SLOT + 1, false);
        else if (isDopant(stack)) ok = moveItemStackTo(stack, DOPANT_SLOT, DOPANT_SLOT + 1, false);
        else if (isBoat(stack)) ok = moveItemStackTo(stack, BOAT_SLOT, BOAT_SLOT + 1, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
