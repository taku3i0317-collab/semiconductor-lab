package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ArcLampBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** アーク灯 [4-16] の画面（IMP4-01）。位置・幅・行数は ArcLampBlockEntity.LAYOUT */
public class ArcLampScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.arc_lamp.";

    public ArcLampScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> Component.translatable(K + (menu.machineItem(ArcLampBlockEntity.SLOT_ROD).isEmpty() ? "need_rod" : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
