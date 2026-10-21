package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.CopperPlaterBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** ウェハーの銅めっき装置 [9-36] の画面（IMP4-01）。位置・幅・行数は CopperPlaterBlockEntity.LAYOUT */
public class CopperPlaterScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.copper_plater.";

    public CopperPlaterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE08040);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(CopperPlaterBlockEntity.SLOT_WAFER);
                String k;
                if (w.isEmpty() || menu.machineItem(CopperPlaterBlockEntity.SLOT_SULFATE).isEmpty() || menu.machineItem(CopperPlaterBlockEntity.SLOT_ANODE).isEmpty()) k = "need_input";
                else if (CopperPlaterBlockEntity.product(w).isEmpty()) k = "cannot";
                else if (!CopperPlaterBlockEntity.hasBarrier(w)) k = "no_barrier";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
