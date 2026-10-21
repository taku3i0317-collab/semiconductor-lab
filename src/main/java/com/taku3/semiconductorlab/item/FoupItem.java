package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.litho.ChipOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * FOUP（ウェハーを入れて運ぶ密閉の箱、v1.0 #4 自動搬送。IMP4-01、O-026 仮決定の案A。本物は 300mm の工場で 1990年代末〜 ⚠️）：
 * 同じ種類のウェハーを {@link #CAPACITY} 枚まで入れる（中身はデータ部品 foup_contents）。ロードポートが中身を機械に入れ、出口の物を戻す。
 * 閉じた箱なので、クリーンルームの外を運んでもウェハーにほこりが付かない（今のゲームではほこりは工程の機械でだけ付くので、本物の理由の説明）。
 */
public class FoupItem extends Item {
    public static final int CAPACITY = 25;
    private static final String K = "item.semiconductor-lab.foup.";

    public FoupItem(Properties properties) { super(properties); }

    /** FOUP に入れられるウェハーか（素の板・酸化した板・接合の板・加工中のウェハーなど） */
    public static boolean isWafer(ItemStack s) {
        return ChipOps.isProcessWafer(s) || s.is(Semiconductor.SILICON_WAFER) || s.is(Semiconductor.OXIDIZED_WAFER) || s.is(Semiconductor.PN_JUNCTION_WAFER)
                || s.is(Semiconductor.THYRISTOR_WAFER) || s.is(Semiconductor.MEMS_WAFER) || s.is(Branches.GAASP_WAFER) || s.is(Branches.GAN_WAFER);
    }

    public static ItemStack contents(ItemStack foup) { return foup.getOrDefault(ModDataComponents.FOUP_CONTENTS, ItemStack.EMPTY); }

    /** w を入れられるだけ入れる（空か同じ物、合わせて 25 枚まで）。入れた枚数を返す（w は減らさない） */
    public static int add(ItemStack foup, ItemStack w) {
        if (!isWafer(w)) return 0;
        ItemStack c = contents(foup);
        if (!c.isEmpty() && !ItemStack.isSameItemSameComponents(c, w)) return 0;
        int n = Math.min(w.getCount(), CAPACITY - c.getCount());
        if (n <= 0) return 0;
        ItemStack nc = c.isEmpty() ? w.copyWithCount(n) : c.copyWithCount(c.getCount() + n);
        foup.set(ModDataComponents.FOUP_CONTENTS, nc);
        return n;
    }

    /** n 枚取り出す（足りなければあるだけ） */
    public static ItemStack take(ItemStack foup, int n) {
        ItemStack c = contents(foup);
        int k = Math.min(n, c.getCount());
        if (k <= 0) return ItemStack.EMPTY;
        ItemStack out = c.copyWithCount(k);
        if (c.getCount() - k <= 0) foup.remove(ModDataComponents.FOUP_CONTENTS);
        else foup.set(ModDataComponents.FOUP_CONTENTS, c.copyWithCount(c.getCount() - k));
        return out;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        ItemStack c = contents(stack);
        out.accept(c.isEmpty() ? Component.translatable(K + "empty") : Component.translatable(K + "holds", c.getCount(), CAPACITY, c.getHoverName()));
    }
}
