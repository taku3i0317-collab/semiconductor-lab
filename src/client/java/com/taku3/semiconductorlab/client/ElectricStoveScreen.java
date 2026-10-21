package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ElectricStoveBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 電気こんろ [3-24] の画面（IMP4-01）。左＝焼く物、右＝焼けた物。位置・幅・行数は ElectricStoveBlockEntity.LAYOUT */
public class ElectricStoveScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.electric_stove.";

    public ElectricStoveScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "heater" -> Component.translatable(K + "heater", menu.getKw() > 0 ? ElectricStoveBlockEntity.HEATER_C : 20);
            case "hint" -> Component.translatable(K + (menu.machineItem(ElectricStoveBlockEntity.SLOT_IN).isEmpty() ? "need_food"
                    : !menu.isReady() ? (menu.machineItem(ElectricStoveBlockEntity.SLOT_OUT).isEmpty() ? "need_food" : "output_full") : menu.getKw() <= 0 ? "need_power" : "cooking"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
