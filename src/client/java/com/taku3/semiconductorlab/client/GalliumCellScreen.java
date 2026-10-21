package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.GalliumCellBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ガリウムの電解槽 [1-44] の画面（IMP4-01）。位置・幅・行数は GalliumCellBlockEntity.LAYOUT */
public class GalliumCellScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.gallium_cell.";

    public GalliumCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFB0C0D0);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> Component.translatable(K + (!GalliumCellBlockEntity.ready(menu.machineItem(GalliumCellBlockEntity.SLOT_LIQUOR), menu.machineItem(GalliumCellBlockEntity.SLOT_BOTTLE)) ? "need_input" : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
