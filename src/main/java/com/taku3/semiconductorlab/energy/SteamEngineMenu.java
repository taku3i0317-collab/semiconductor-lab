package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlocks;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 蒸気機関の画面の中身。スロット0＝石炭（下）、1＝水バケツ（上）、2〜37＝プレイヤー */
public class SteamEngineMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = 2;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final net.minecraft.world.level.block.Block block;   // 蒸気機関 or 蒸気タービン（画面は共通）

    /** クライアント側用（中身はサーバーから同期される） */
    public SteamEngineMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(2), new SimpleContainerData(SteamEngineBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL, ModBlocks.STEAM_ENGINE);
    }

    public SteamEngineMenu(int id, Inventory inv, Container container, ContainerData data, ContainerLevelAccess access, net.minecraft.world.level.block.Block block) {
        super(ModMenus.STEAM_ENGINE, id);
        this.data = data;
        this.access = access;
        this.block = block;
        // 石炭・木炭（下＝かまどの燃料と同じ位置）
        addSlot(new Slot(container, SteamEngineBlockEntity.SLOT_COAL, 56, 53) {
            @Override public boolean mayPlace(ItemStack s) { return SteamEngineBlockEntity.isFuel(s); }
        });
        // 水バケツ（上）。使い終わると空バケツが残るので取り出す
        addSlot(new Slot(container, SteamEngineBlockEntity.SLOT_WATER, 56, 17) {
            @Override public boolean mayPlace(ItemStack s) { return s.is(Items.WATER_BUCKET); }
        });
        addStandardInventorySlots(inv, 8, 84);
        addDataSlots(data);
    }

    public int getBurn()      { return data.get(SteamEngineBlockEntity.DATA_BURN); }
    public int getBurnTotal() { return Math.max(1, data.get(SteamEngineBlockEntity.DATA_BURN_TOTAL)); }
    public int getWater()     { return data.get(SteamEngineBlockEntity.DATA_WATER); }
    public double getKw()     { return data.get(SteamEngineBlockEntity.DATA_DECI_KW) / 10.0; }
    public int getStoredKj()  { return data.get(SteamEngineBlockEntity.DATA_KJ); }

    @Override
    public boolean stillValid(Player player) { return stillValid(access, player, block); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean ok;
        if (index < PLAYER_START) ok = moveItemStackTo(stack, PLAYER_START, slots.size(), true);
        else if (SteamEngineBlockEntity.isFuel(stack)) ok = moveItemStackTo(stack, 0, 1, false);
        else if (stack.is(Items.WATER_BUCKET)) ok = moveItemStackTo(stack, 1, 2, false);
        else ok = false;
        if (!ok) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
