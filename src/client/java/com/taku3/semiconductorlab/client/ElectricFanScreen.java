package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 電気のファン [4-18] の画面（IMP4-01）。位置・幅・行数は ElectricFanBlockEntity.LAYOUT */
public class ElectricFanScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.electric_fan.";

    public ElectricFanScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "reach" -> Component.translatable(K + "reach", menu.get(com.taku3.semiconductorlab.block.ElectricFanBlockEntity.DATA_COUNT));
            case "hint" -> Component.translatable(K + (menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
