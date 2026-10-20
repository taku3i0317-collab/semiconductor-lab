package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.IonImplanterBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** イオン注入装置 [9-28] の画面。左＝加工中のウェハー・ドーパント源、中央バー＝進み、出口＝注入したウェハー、右＝電力・状態 */
public class IonImplanterScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.ion_implanter.";

    public IonImplanterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFB070FF);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は IonImplanterBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(IonImplanterBlockEntity.SLOT_WAFER), src = menu.machineItem(IonImplanterBlockEntity.SLOT_SOURCE);
                String k;
                if (w.isEmpty() || src.isEmpty()) k = "need_input";
                else if (IonImplanterBlockEntity.product(w, src).isEmpty()) k = "cannot";
                else if (menu.get(IonImplanterBlockEntity.DATA_VACUUM) == 0) k = "need_vacuum";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
