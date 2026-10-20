package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 作業台で使っても減らない触媒（O-010、[2-60] の硫酸水銀など）。反応を助けるだけで、自分は変わらない＝作業台に戻る。
 * QuartzStillItem と同じく、Item.Properties.craftRemainder(Item) は自分自身を渡せないので、ここで返す。
 * ⚠️ 本物の触媒は少しずつ弱る（汚れる・失われる）が、それは扱わない。
 */
public class CatalystItem extends Item {
    public CatalystItem(Properties properties) { super(properties); }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return new ItemStackTemplate(this);
    }
}
