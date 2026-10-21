package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * スマホのホーム画面（[B-10]、S-017 の3・S-019 の5：自作のドット絵のアイコンが並ぶ画面）の入れ物。スロットはない。
 * アイコンを押す＝ボタン（0＝電卓・1＝写真・2＝傾き）→ サーバーでアプリを選んで動かす（{@link SmartphoneItem#runApp}）
 */
public class PhoneMenu extends AbstractContainerMenu {
    public PhoneMenu(int id, Inventory inv) { super(Branches.PHONE_MENU, id); }

    /** 手に持っているスマホ（なければ EMPTY） */
    public static ItemStack phone(Player p) {
        for (InteractionHand h : InteractionHand.values()) if (p.getItemInHand(h).is(Branches.SMARTPHONE)) return p.getItemInHand(h);
        return ItemStack.EMPTY;
    }

    @Override public ItemStack quickMoveStack(Player p, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return !phone(p).isEmpty(); }

    @Override
    public boolean clickMenuButton(Player p, int id) {
        ItemStack s = phone(p);
        if (s.isEmpty() || id < 0 || id >= SmartphoneItem.APPS.length) return false;
        s.set(ModDataComponents.PHONE_APP, id);
        if (!p.level().isClientSide()) SmartphoneItem.runApp(p.level(), p, s);
        return true;
    }
}
