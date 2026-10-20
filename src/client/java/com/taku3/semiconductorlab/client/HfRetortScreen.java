package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.HfRetortBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 鉛のレトルト [2-10] の画面。上の段＝蛍石・硫酸・空の鉛の瓶、下＝水・燃料、縦バー＝残りの火、中央バー＝進み、右＝フッ酸・石こう・空のバケツ */
public class HfRetortScreen extends MachineScreen {
    private static final int FLAME_X = 47, FLAME_Y = 36, FLAME_W = 10, FLAME_H = 14;
    private static final int BAR_X = 66, BAR_Y = 38, BAR_W = 32, BAR_H = 6;
    private static final int WATER_X = 29, WATER_Y = 53, WATER_W = 6, WATER_H = 16;
    private static final String K = "gui.semiconductor-lab.lead_retort.";

    public HfRetortScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        vbar(g, FLAME_X, FLAME_Y, FLAME_W, FLAME_H, menu.get(HfRetortBlockEntity.DATA_BURN), menu.get(HfRetortBlockEntity.DATA_BURN_TOTAL), 0xFFFF7A1E);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.get(HfRetortBlockEntity.DATA_PROGRESS), menu.get(HfRetortBlockEntity.DATA_TOTAL), 0xFF9AA0A8);
        vbar(g, WATER_X, WATER_Y, WATER_W, WATER_H, menu.get(HfRetortBlockEntity.DATA_WATER), HfRetortBlockEntity.RUNS_PER_WATER, 0xFF3A6EDC);   // 受け器の水
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は HfRetortBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        if (!id.equals("hint")) return null;
        ItemStack fl = menu.getSlot(HfRetortBlockEntity.SLOT_FLUORITE).getItem(), acid = menu.getSlot(HfRetortBlockEntity.SLOT_ACID).getItem();
        ItemStack bottle = menu.getSlot(HfRetortBlockEntity.SLOT_BOTTLE).getItem(), water = menu.getSlot(HfRetortBlockEntity.SLOT_WATER).getItem();
        ItemStack fuel = menu.getSlot(HfRetortBlockEntity.SLOT_FUEL).getItem();
        String hint;
        if (fl.isEmpty() || acid.isEmpty()) hint = "need_input";
        else if (bottle.isEmpty()) hint = "need_bottle";
        else if (menu.get(HfRetortBlockEntity.DATA_WATER) <= 0 && water.isEmpty()) hint = "need_water";
        else if (menu.get(HfRetortBlockEntity.DATA_BURN) <= 0 && fuel.isEmpty()) hint = "need_fuel";
        else if (menu.get(HfRetortBlockEntity.DATA_PROGRESS) > 0) hint = "running";
        else hint = "out_full";
        return Component.translatable(K + hint);
    }
}
