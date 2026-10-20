package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.HeliographPlateBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * DNQ（ジアゾナフトキノン）[2-57]：ポジ型レジストの感光剤。光（紫外線）を受けると窒素が抜けて分解し（ヴォルフ転位 → インデンカルボン酸）、
 * アルカリに溶けるようになる＝ポジ型のしくみ。持ったまま日なた（昼・真上が空まで開けている）にいると分解し、名前が「光で分解した DNQ」に変わる（戻らない）。
 * チェストや屋内（日かげ）なら分解しない。半導体工場の照明が黄色いのは、レジストが感光しないようにするため。
 */
public class DnqItem extends Item {
    public static final int CHECK_TICKS = 40;
    public static final String DECOMPOSED = "item.semiconductor-lab.dnq.decomposed";

    public DnqItem(Properties properties) { super(properties); }

    public static boolean decomposed(ItemStack stack) {
        Component name = stack.get(DataComponents.ITEM_NAME);
        return name != null && name.getContents() instanceof TranslatableContents t && t.getKey().equals(DECOMPOSED);
    }

    /** 光で分解させる（戻らない）。分解したら true */
    public static boolean expose(ItemStack stack) {
        if (decomposed(stack)) return false;
        stack.set(DataComponents.ITEM_NAME, Component.translatable(DECOMPOSED));
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % CHECK_TICKS != 0 || !(entity instanceof Player p)) return;
        if (level.isBrightOutside() && HeliographPlateBlock.openToSky(level, p.blockPosition().above()) && expose(stack))
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.dnq.decomposed"));
    }
}
