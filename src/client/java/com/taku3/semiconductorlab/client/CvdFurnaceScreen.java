package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.CvdFurnaceBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** CVD 炉 [9-30] の画面（IMP4-01）。位置・幅・行数は CvdFurnaceBlockEntity.LAYOUT */
public class CvdFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.cvd_furnace.";

    public CvdFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE0A060);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(CvdFurnaceBlockEntity.SLOT_WAFER), si = menu.machineItem(CvdFurnaceBlockEntity.SLOT_SILANE), o2 = menu.machineItem(CvdFurnaceBlockEntity.SLOT_O2);
                String k;
                if (w.isEmpty() || si.isEmpty() || o2.isEmpty()) k = "need_input";
                else if (CvdFurnaceBlockEntity.product(w, o2).isEmpty()) k = o2.is(com.taku3.semiconductorlab.content.Gas.AMMONIA_GAS) ? "cannot_nitride" : "cannot";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = o2.is(com.taku3.semiconductorlab.content.Gas.AMMONIA_GAS) ? "running_nitride" : "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
