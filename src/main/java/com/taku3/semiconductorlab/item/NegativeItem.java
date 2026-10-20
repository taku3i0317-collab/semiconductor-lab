package com.taku3.semiconductorlab.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * ガラスのネガ（[B-02b] ゼラチン乾板、IMP2 O-008）。印画紙と作業台で合わせると写真（紙焼き）ができ、ネガは写真の中身ごと手元に戻る
 * ＝ 1枚のネガから何枚でも焼ける（マスクから何枚も転写するフォトリソグラフィの考え方）。
 */
public class NegativeItem extends PhotoItem {
    public NegativeItem(Properties properties) { super(properties); }

    @Override
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return ItemStackTemplate.fromNonEmptyStack(stack, 1);
    }
}
