package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.LoadPortBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ロードポート（v1.0 #4）の画面（IMP4-01）。上＝入れる FOUP、下＝出す FOUP。位置・幅・行数は LoadPortBlockEntity.LAYOUT */
public class LoadPortScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.load_port.";

    public LoadPortScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> Component.translatable(K + (menu.machineItem(LoadPortBlockEntity.SLOT_IN_FOUP).isEmpty() && menu.machineItem(LoadPortBlockEntity.SLOT_OUT_FOUP).isEmpty() ? "need_foup"
                    : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
