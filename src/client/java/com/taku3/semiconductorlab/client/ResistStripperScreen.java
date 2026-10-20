package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ResistStripperBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** レジストはがし槽の画面。左＝加工中のウェハー、2列目＝硫酸・過酸化水素、中央バー＝進み、出口＝はがしたウェハー、右＝電力・状態 */
public class ResistStripperScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 41, BAR_W = 26, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.resist_stripper.";

    public ResistStripperScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFD0B060);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ResistStripperBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(ResistStripperBlockEntity.SLOT_WAFER);
                String k;
                if (ChipOps.isProcessWafer(w) && ResistStripperBlockEntity.product(w).isEmpty()) k = "no_resist";
                else if (w.isEmpty() || menu.machineItem(ResistStripperBlockEntity.SLOT_ACID).isEmpty() || menu.machineItem(ResistStripperBlockEntity.SLOT_PEROXIDE).isEmpty()) k = "need_input";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
