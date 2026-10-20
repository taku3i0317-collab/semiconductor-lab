package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.HclBurnerBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 塩酸合成炉の画面。左上＝水バケツ、左下＝水素・塩素、中央バー＝燃焼の進み、右＝塩酸、右下＝空ボンベ・空バケツ。中のガス（mol）と水を表示 */
public class HclBurnerScreen extends MachineScreen {
    private static final int BAR_X = 80, BAR_Y = 41, BAR_W = 30, BAR_H = 6;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.hcl_burner.";

    public HclBurnerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.get(HclBurnerBlockEntity.DATA_PROGRESS) * BAR_W / Math.max(1, menu.get(HclBurnerBlockEntity.DATA_TOTAL));
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFE0E8A0);   // 塩素中の水素の炎は白っぽい黄緑
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.literal("H2 " + menu.get(HclBurnerBlockEntity.DATA_H2)), HclBurnerBlockEntity.H2_X - 2, HclBurnerBlockEntity.GAS_Y - 10, TEXT_COLOR, false);
        g.text(font, Component.literal("Cl2 " + menu.get(HclBurnerBlockEntity.DATA_CL2)), HclBurnerBlockEntity.CL2_X - 2, HclBurnerBlockEntity.GAS_Y - 10, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "water", menu.get(HclBurnerBlockEntity.DATA_WATER)), HclBurnerBlockEntity.WATER_X + 20, HclBurnerBlockEntity.WATER_Y + 4, TEXT_COLOR, false);
        String hint;
        if (menu.get(HclBurnerBlockEntity.DATA_PROGRESS) > 0) hint = "burning";
        else if (menu.get(HclBurnerBlockEntity.DATA_WATER) <= 0 && menu.getSlot(HclBurnerBlockEntity.SLOT_WATER).getItem().isEmpty()) hint = "need_water";
        else if (menu.get(HclBurnerBlockEntity.DATA_H2) < HclBurnerBlockEntity.H2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_H2).getItem().isEmpty()) hint = "need_h2";
        else if (menu.get(HclBurnerBlockEntity.DATA_CL2) < HclBurnerBlockEntity.CL2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_CL2).getItem().isEmpty()) hint = "need_cl2";
        else hint = "out_full";
        g.text(font, Component.translatable(K + hint), 80, 26, TEXT_COLOR, false);
    }
}
