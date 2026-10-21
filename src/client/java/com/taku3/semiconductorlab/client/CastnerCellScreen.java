package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.CastnerCellBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** [1-66] の画面（IMP4-01）。位置・幅・行数は CastnerCellBlockEntity.LAYOUT */
public class CastnerCellScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.castner_cell.";

    public CastnerCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE0E0E8);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                String k = menu.machineItem(CastnerCellBlockEntity.SLOT_NAOH).getCount() < CastnerCellBlockEntity.NAOH_PER_RUN || menu.machineItem(CastnerCellBlockEntity.SLOT_FLASK).isEmpty() ? "need_input"
                        : menu.getKw() <= 0 ? "need_power" : "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
