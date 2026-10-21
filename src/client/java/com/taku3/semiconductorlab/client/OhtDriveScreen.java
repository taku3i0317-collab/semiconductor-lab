package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.OhtDriveBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 天井の搬送 OHT の駆動 [14-23] の画面（IMP4-01）。左のスロット＝運んでいる FOUP。位置・幅・行数は OhtDriveBlockEntity.LAYOUT */
public class OhtDriveScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.oht_drive.";

    public OhtDriveScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        int st = menu.get(OhtDriveBlockEntity.DATA_STATE);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "rail" -> Component.translatable(K + "rail", menu.get(OhtDriveBlockEntity.DATA_RAILS));
            case "hint" -> Component.translatable(K + switch (st) {
                case 3 -> "no_port";
                case 2 -> "no_dest";
                case 1 -> menu.getKw() <= 0 ? "need_power" : "running";
                default -> "waiting";
            });
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
