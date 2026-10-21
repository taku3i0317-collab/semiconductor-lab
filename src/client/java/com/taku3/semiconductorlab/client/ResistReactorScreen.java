package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ResistReactorBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 化学増幅レジストの合成釜 [2-33] の画面（IMP4-01）。位置・幅・行数は ResistReactorBlockEntity.LAYOUT */
public class ResistReactorScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 38, BAR_W = 26, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.resist_reactor.";

    public ResistReactorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFD0A0E0);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack[] in = new ItemStack[4];
                for (int i = 0; i < 4; i++) in[i] = menu.machineItem(i);
                var r = ResistReactorBlockEntity.match(in, null);
                String k;
                if (r == null) k = "need_input";
                else if (r.catalyst() != null && !menu.machineItem(ResistReactorBlockEntity.SLOT_CAT).is(r.catalyst())) k = "need_catalyst";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = r.key();
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
