package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.SilaneGeneratorBlockEntity;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** シランの発生器 [9-30] の画面（IMP4-01）。位置・幅・行数は SilaneGeneratorBlockEntity.LAYOUT */
public class SilaneGeneratorScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.silane_generator.";

    public SilaneGeneratorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFA0D0A0);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                int m = SilaneGeneratorBlockEntity.mode(menu.machineItem(SilaneGeneratorBlockEntity.SLOT_A), menu.machineItem(SilaneGeneratorBlockEntity.SLOT_B));
                String k;
                if (m == SilaneGeneratorBlockEntity.MODE_NONE) k = "need_input";
                else if (m == SilaneGeneratorBlockEntity.MODE_SILANE && !menu.machineItem(SilaneGeneratorBlockEntity.SLOT_CYL).is(Gas.EMPTY_GAS_CYLINDER)) k = "need_cylinder";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = m == SilaneGeneratorBlockEntity.MODE_SILICIDE ? "silicide" : "silane";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
