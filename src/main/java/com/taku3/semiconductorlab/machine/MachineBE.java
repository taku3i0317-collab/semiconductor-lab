package com.taku3.semiconductorlab.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 画面を持つ機械の BE が実装する（IMP-28）。中身（Container）と数値（ContainerData）を渡すだけで、
 * 画面の名前（ブロックの名前）と {@link MachineMenu} は自動で作られる。
 * ConsumerBlockEntity（電気の機械）はもう実装しているので、電気の機械は何も書かなくてよい。
 */
public interface MachineBE extends MenuProvider {
    /** 機械のスロットの中身 */
    Container getContainer();
    /** GUI へ同期する数値の列 */
    ContainerData getMenuData();

    // BlockEntity がすでに持っているもの（ここで名前を出しておくと下の default から使える）
    Level getLevel();
    BlockPos getBlockPos();
    BlockState getBlockState();
    BlockEntityType<?> getType();

    /** 画面のボタンが押された（サーバー側、MachineMenu.clickMenuButton から）。使ったら true */
    default boolean onButton(Player player, int id) { return false; }

    @Override default Component getDisplayName() { return getBlockState().getBlock().getName(); }

    @Override default AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MachineMenu(Machine.of(getType()), id, inv, getContainer(), getMenuData(),
                ContainerLevelAccess.create(getLevel(), getBlockPos()), getBlockState().getBlock());
    }
}
