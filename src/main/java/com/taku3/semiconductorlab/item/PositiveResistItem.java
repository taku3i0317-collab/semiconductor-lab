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
 * ポジ型フォトレジスト [2-32]（IMP4-01）：ノボラック樹脂に感光剤 DNQ（D の [2-57]）を溶かした液。DNQ と同じく、日なたで持ち歩くと
 * 光で感光剤が分解して使えなくなる（名前が「感光してしまったレジスト」に変わり、戻らない）。塗布機は感光したレジストを受け付けない。
 * 工場の照明が黄色いのは、この感光を防ぐため（イエロールーム）。判定のしくみは {@link DnqItem} と同じ（ITEM_NAME の翻訳キー）。
 */
public class PositiveResistItem extends Item {
    public static final int CHECK_TICKS = 40;
    public static final String SPOILED = "item.semiconductor-lab.positive_resist.spoiled";

    public PositiveResistItem(Properties properties) { super(properties); }

    public static boolean spoiled(ItemStack stack) {
        Component name = stack.get(DataComponents.ITEM_NAME);
        return name != null && name.getContents() instanceof TranslatableContents t && t.getKey().equals(SPOILED);
    }

    /** 光で感光させる（戻らない）。感光させたら true */
    public static boolean expose(ItemStack stack) {
        if (spoiled(stack)) return false;
        stack.set(DataComponents.ITEM_NAME, Component.translatable(SPOILED));
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % CHECK_TICKS != 0 || !(entity instanceof Player p)) return;
        if (level.isBrightOutside() && HeliographPlateBlock.openToSky(level, p.blockPosition().above()) && expose(stack))
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.positive_resist.spoiled"));
    }
}
