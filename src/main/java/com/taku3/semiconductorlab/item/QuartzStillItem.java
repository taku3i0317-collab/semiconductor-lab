package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * 石英の蒸留器 [1-34]（石英のフラスコ＋ガラスの冷却管）。作業台で粗四塩化ゲルマニウムと一緒に使うと、
 * 蒸留した四塩化ゲルマニウムができ、蒸留器そのものは手元に戻る（Fabric の FabricItem.getCraftingRemainder）。
 * Item.Properties.craftRemainder(Item) は「自分自身」を渡せない（登録前に自分の Item が要る）ため、こちらで返す。
 */
public class QuartzStillItem extends Item {
    public QuartzStillItem(Properties properties) { super(properties); }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return new ItemStackTemplate(this);
    }
}
