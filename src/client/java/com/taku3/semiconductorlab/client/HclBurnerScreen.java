package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.HclBurnerBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 塩酸合成炉の画面。左上＝水バケツ、左下＝水素・塩素、中央バー＝燃焼の進み、右上＝空のガラス瓶、右＝塩酸、右下＝空ボンベ・空バケツ。中のガス（mol）と水を表示 */
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

    /** 置き場所ごとに今出す文（位置・幅・行数は HclBurnerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "h2" -> Component.literal("H2 " + menu.get(HclBurnerBlockEntity.DATA_H2));
            case "cl2" -> Component.literal("Cl2 " + menu.get(HclBurnerBlockEntity.DATA_CL2));
            case "water" -> Component.translatable(K + "water", menu.get(HclBurnerBlockEntity.DATA_WATER));
            case "hint" -> {
                String hint;
                if (menu.get(HclBurnerBlockEntity.DATA_PROGRESS) > 0) hint = "burning";
                else if (menu.get(HclBurnerBlockEntity.DATA_WATER) <= 0 && menu.getSlot(HclBurnerBlockEntity.SLOT_WATER).getItem().isEmpty()) hint = "need_water";
                else if (menu.get(HclBurnerBlockEntity.DATA_H2) < HclBurnerBlockEntity.H2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_H2).getItem().isEmpty()) hint = "need_h2";
                else if (menu.get(HclBurnerBlockEntity.DATA_CL2) < HclBurnerBlockEntity.CL2_PER && menu.getSlot(HclBurnerBlockEntity.SLOT_CL2).getItem().isEmpty()) hint = "need_cl2";
                else if (menu.get(HclBurnerBlockEntity.DATA_WATER) > 0 && menu.getSlot(HclBurnerBlockEntity.SLOT_BOTTLE).getItem().isEmpty()) hint = "need_bottle";
                else hint = "out_full";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
