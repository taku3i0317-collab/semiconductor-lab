package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.AmmoniaStillBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** アンモニア蒸留器 [2-21] の画面。左＝安水・消石灰・空のガラス瓶・水、中央バー＝進み、出口＝アンモニア水・空のバケツ、右＝電力・水・状態 */
public class AmmoniaStillScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 41, BAR_W = 26, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.ammonia_still.";

    public AmmoniaStillScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF9AD0E0);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は AmmoniaStillBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int water = menu.get(AmmoniaStillBlockEntity.DATA_WATER);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "water" -> Component.translatable(K + "water", String.valueOf(water));
            case "hint" -> {
                String k;
                if (menu.machineItem(AmmoniaStillBlockEntity.SLOT_LIQUOR).getCount() < AmmoniaStillBlockEntity.LIQUOR_PER_RUN
                        || menu.machineItem(AmmoniaStillBlockEntity.SLOT_LIME).isEmpty() || menu.machineItem(AmmoniaStillBlockEntity.SLOT_BOTTLE).isEmpty()) k = "need_input";
                else if (water <= 0 && menu.machineItem(AmmoniaStillBlockEntity.SLOT_WATER).isEmpty()) k = "need_water";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
