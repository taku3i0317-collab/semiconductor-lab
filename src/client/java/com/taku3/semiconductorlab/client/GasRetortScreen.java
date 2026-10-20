package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.GasRetortBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** ガス工場のレトルト [2-20] の画面。左上＝石炭、左下＝燃料、間の縦バー＝残りの火、中央バー＝乾留の進み、右＝コークス・コールタール・安水（反射炉と同じ並び） */
public class GasRetortScreen extends MachineScreen {
    private static final int FLAME_X = 47, FLAME_Y = 36, FLAME_W = 10, FLAME_H = 14;
    private static final int BAR_X = 66, BAR_Y = 38, BAR_W = 32, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.gas_retort.";

    public GasRetortScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        vbar(g, FLAME_X, FLAME_Y, FLAME_W, FLAME_H, menu.get(GasRetortBlockEntity.DATA_BURN), menu.get(GasRetortBlockEntity.DATA_BURN_TOTAL), 0xFFFF7A1E);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.get(GasRetortBlockEntity.DATA_PROGRESS), menu.get(GasRetortBlockEntity.DATA_TOTAL), 0xFF303030);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は GasRetortBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        if (!id.equals("hint")) return null;
        ItemStack in = menu.getSlot(GasRetortBlockEntity.SLOT_IN).getItem();
        ItemStack fuel = menu.getSlot(GasRetortBlockEntity.SLOT_FUEL).getItem();
        String hint;
        if (in.isEmpty()) hint = "need_input";
        else if (menu.get(GasRetortBlockEntity.DATA_BURN) <= 0 && fuel.isEmpty()) hint = "need_fuel";
        else if (menu.get(GasRetortBlockEntity.DATA_PROGRESS) > 0) hint = "running";
        else hint = "out_full";   // 石炭も燃料もあるのに進まない＝出口のどれかが満杯
        return Component.translatable(K + hint);
    }
}
