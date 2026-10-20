package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.DryEtcherBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** [9-27] の画面（IMP4-01）。位置・幅・行数は DryEtcherBlockEntity.LAYOUT */
public class DryEtcherScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.dry_etcher.";

    public DryEtcherScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF9070FF);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(DryEtcherBlockEntity.SLOT_WAFER), gas = menu.machineItem(DryEtcherBlockEntity.SLOT_GAS);
                String k;
                if (w.isEmpty() || gas.isEmpty()) k = "need_input";
                else if (DryEtcherBlockEntity.product(w).isEmpty()) k = "cannot";
                else if (menu.get(DryEtcherBlockEntity.DATA_VACUUM) == 0) k = "need_vacuum";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
