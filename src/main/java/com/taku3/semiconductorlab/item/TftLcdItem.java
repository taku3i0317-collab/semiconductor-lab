package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * TFT 液晶 [B-19]（1979 水素を含むアモルファスシリコンの TFT、1988 14型のカラー TFT 液晶。IMP4-01、O-024 仮決定の案A）：
 * 画素1つずつに薄い膜のトランジスタ（TFT）が付いていて、その画素に電気をためて、次に書きかえるまで明るさを保つ（アクティブマトリクス）。
 * 手に持つと、そのしくみを一言で画面の下に出す（表示の絵はなし）。スマホの部品（A の組立台、ID tft_lcd）。
 * ⚠️ 画素の並びの露光・エッチングは省略（tft_lcd.placeholder）。
 */
public class TftLcdItem extends Item {
    public TftLcdItem(Properties properties) { super(properties); }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player p) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)) return;
        if (level.getGameTime() % 40 != 0) return;
        p.sendOverlayMessage(Component.translatable("item.semiconductor-lab.tft_lcd.holding"));
    }
}
