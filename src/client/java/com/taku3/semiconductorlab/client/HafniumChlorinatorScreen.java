package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.HafniumChlorinatorBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ハフニウムの塩素化炉の画面（IMP4-01）。位置・幅・行数は HafniumChlorinatorBlockEntity.LAYOUT */
public class HafniumChlorinatorScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.hafnium_chlorinator.";

    public HafniumChlorinatorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFC0E080);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> Component.translatable(K + (!HafniumChlorinatorBlockEntity.ready(menu.machineItem(HafniumChlorinatorBlockEntity.SLOT_OXIDE), menu.machineItem(HafniumChlorinatorBlockEntity.SLOT_COKE),
                    menu.machineItem(HafniumChlorinatorBlockEntity.SLOT_CL2), menu.machineItem(HafniumChlorinatorBlockEntity.SLOT_BOTTLE)) ? "need_input" : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
