package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 作業台で使っても手元に戻る道具（IMP2-01：レンズ研磨 [7-03] の研削皿・ピッチの研磨皿）。しくみは {@link QuartzStillItem} と同じ
 * （Item.Properties.craftRemainder(Item) は登録前の自分を渡せないので、Fabric の getCraftingRemainder を上書きして自分を返す）。
 */
public class ReusableToolItem extends Item {
    public ReusableToolItem(Properties properties) { super(properties); }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return new ItemStackTemplate(this);
    }
}
