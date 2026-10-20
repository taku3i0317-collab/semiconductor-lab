package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** 写真の中身（Branches.PHOTO）を持つ板（露光した板・現像した板・写真）。ツールチップに場所・時刻・写りを出す（IMP2、S-002） */
public class PhotoItem extends Item {
    private static final String K = "item.semiconductor-lab.photo.";

    public PhotoItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        PhotoData d = stack.get(Branches.PHOTO);
        if (d == null) return;
        long t = Math.floorMod(d.dayTime() + 6000, 24000);   // 0＝朝6時
        out.accept(Component.translatable(K + "place", d.place()));
        out.accept(Component.translatable(K + "time", String.format("%02d:%02d", t / 1000, (t % 1000) * 60 / 1000)));
        out.accept(Component.translatable(K + "exposure", d.exposure(), Component.translatable(K + d.quality())));
    }
}
