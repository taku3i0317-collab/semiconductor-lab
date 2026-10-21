package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.CompoundFurnaceBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 化合物半導体の炉 [B-07] の画面（IMP4-01）。位置・幅・行数は CompoundFurnaceBlockEntity.LAYOUT */
public class CompoundFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.compound_furnace.";

    public CompoundFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFF6040);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                int m = CompoundFurnaceBlockEntity.mode(menu.machineItem(CompoundFurnaceBlockEntity.SLOT_A), menu.machineItem(CompoundFurnaceBlockEntity.SLOT_B), menu.machineItem(CompoundFurnaceBlockEntity.SLOT_C));
                yield Component.translatable(K + (m == CompoundFurnaceBlockEntity.MODE_NONE ? "need_input" : menu.getKw() <= 0 ? "need_power" : m == CompoundFurnaceBlockEntity.MODE_ARSENIC ? "arsenic" : m == CompoundFurnaceBlockEntity.MODE_GAASP ? "gaasp" : m == CompoundFurnaceBlockEntity.MODE_GAN ? "gan" : "phosphor"));
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
