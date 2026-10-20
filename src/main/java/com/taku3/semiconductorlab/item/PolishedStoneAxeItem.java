package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 磨製石斧 [0-08]。作業台で木を削る道具として使う（[0-09] 木の車輪）と、手元に戻るが少し傷む（耐久 − CRAFT_WEAR）。
 * 壊れるほど傷んでいたら戻らない。
 */
public class PolishedStoneAxeItem extends Item {
    /** 作業台で1回使うごとの傷み */
    public static final int CRAFT_WEAR = 10;

    public PolishedStoneAxeItem(Properties properties) { super(properties); }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        int damage = stack.getDamageValue() + CRAFT_WEAR;
        if (damage >= stack.getMaxDamage()) return null;
        ItemStack worn = stack.copyWithCount(1);
        worn.setDamageValue(damage);
        return ItemStackTemplate.fromNonEmptyStack(worn);
    }
}
