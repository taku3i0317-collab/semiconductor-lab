package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * AI チップの書く画面（[B-11]・[10-16]、S-020 Q32 案A）の入れ物。スロットはない。ボタン：0〜63＝方眼のマスを塗る／消す、
 * 100＝全部消す、110〜119＝教える答え（0〜9）を選ぶ、200＝教える（今の方眼を選んだ答えとしてチップに覚えさせる）
 */
public class AiChipMenu extends AbstractContainerMenu {
    public static final int CLEAR = 100, LABEL = 110, TEACH = 200;
    public final int[] grid = new int[64];
    public int label = -1;

    public AiChipMenu(int id, Inventory inv) { super(Branches.AI_MENU, id); }

    public static ItemStack chip(Player p) {
        for (InteractionHand h : InteractionHand.values()) if (p.getItemInHand(h).is(Branches.AI_CHIP)) return p.getItemInHand(h);
        return ItemStack.EMPTY;
    }

    @Override public ItemStack quickMoveStack(Player p, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return !chip(p).isEmpty(); }

    /** 画面とサーバーの両方で同じように動かす */
    public boolean apply(int id) {
        if (id >= 0 && id < 64) { grid[id] = 1 - grid[id]; return true; }
        if (id == CLEAR) { java.util.Arrays.fill(grid, 0); return true; }
        if (id >= LABEL && id < LABEL + 10) { label = id - LABEL; return true; }
        return id == TEACH && label >= 0;
    }

    @Override
    public boolean clickMenuButton(Player p, int id) {
        if (!apply(id)) return false;
        if (id == TEACH && !p.level().isClientSide()) AiChipItem.teach(chip(p), grid.clone(), label);
        return true;
    }
}
