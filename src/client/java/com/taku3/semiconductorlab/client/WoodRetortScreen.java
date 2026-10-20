package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.WoodRetortBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 乾留炉 [2-23] の画面。左上＝原木、左下＝燃料、間の縦バー＝残りの火、中央バー＝乾留の進み、右＝木炭・木酢液・木タール（反射炉と同じ並び） */
public class WoodRetortScreen extends MachineScreen {
    private static final int FLAME_X = 47, FLAME_Y = 36, FLAME_W = 10, FLAME_H = 14;
    private static final int BAR_X = 66, BAR_Y = 38, BAR_W = 32, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.wood_retort.";

    public WoodRetortScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        vbar(g, FLAME_X, FLAME_Y, FLAME_W, FLAME_H, menu.get(WoodRetortBlockEntity.DATA_BURN), menu.get(WoodRetortBlockEntity.DATA_BURN_TOTAL), 0xFFFF7A1E);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.get(WoodRetortBlockEntity.DATA_PROGRESS), menu.get(WoodRetortBlockEntity.DATA_TOTAL), 0xFF8A5A2A);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        ItemStack in = menu.getSlot(WoodRetortBlockEntity.SLOT_IN).getItem();
        ItemStack fuel = menu.getSlot(WoodRetortBlockEntity.SLOT_FUEL).getItem();
        String hint;
        if (in.isEmpty()) hint = "need_input";
        else if (menu.get(WoodRetortBlockEntity.DATA_BURN) <= 0 && fuel.isEmpty()) hint = "need_fuel";
        else if (menu.get(WoodRetortBlockEntity.DATA_PROGRESS) > 0) hint = "running";
        else hint = "out_full";   // 原木も燃料もあるのに進まない＝出口のどれかが満杯
        text(g, Component.translatable(K + hint), 64, 63);
    }
}
