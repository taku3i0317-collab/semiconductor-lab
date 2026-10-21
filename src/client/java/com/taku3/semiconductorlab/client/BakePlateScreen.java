package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.BakePlateBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** ホットプレート（PEB）[2-33] の画面（IMP4-01）。位置・幅・行数は BakePlateBlockEntity.LAYOUT */
public class BakePlateScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.bake_plate.";

    public BakePlateScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFF9060);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(BakePlateBlockEntity.SLOT_WAFER);
                String k;
                if (w.isEmpty()) k = "need_input";
                else if (BakePlateBlockEntity.product(w).isEmpty()) {
                    String r = BakePlateBlockEntity.reason(w);
                    k = switch (r) { case "not_car", "already" -> r; default -> "not_exposed"; };
                } else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
