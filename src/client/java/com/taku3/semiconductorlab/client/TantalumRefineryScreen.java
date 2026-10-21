package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.TantalumRefineryBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** [1-66] の画面（IMP4-01）。位置・幅・行数は TantalumRefineryBlockEntity.LAYOUT */
public class TantalumRefineryScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.tantalum_refinery.";

    public TantalumRefineryScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF9098B0);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                int m = TantalumRefineryBlockEntity.mode(menu.machineItem(TantalumRefineryBlockEntity.SLOT_A), menu.machineItem(TantalumRefineryBlockEntity.SLOT_B));
                String k;
                if (m == TantalumRefineryBlockEntity.MODE_NONE) k = "need_input";
                else if (m == TantalumRefineryBlockEntity.MODE_MARIGNAC && menu.machineItem(TantalumRefineryBlockEntity.SLOT_KF).isEmpty()) k = "need_kf";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = m == TantalumRefineryBlockEntity.MODE_MARIGNAC ? "marignac" : "reduce";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
