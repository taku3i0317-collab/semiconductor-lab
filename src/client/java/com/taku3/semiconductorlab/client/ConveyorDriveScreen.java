package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ConveyorDriveBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ベルトコンベアの駆動 [14-22] の画面（IMP4-01）。位置・幅・行数は ConveyorDriveBlockEntity.LAYOUT */
public class ConveyorDriveScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.conveyor_drive.";

    public ConveyorDriveScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        int belts = menu.get(ConveyorDriveBlockEntity.DATA_BELTS);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "belt" -> Component.translatable(K + "belt", belts, menu.get(ConveyorDriveBlockEntity.DATA_ITEMS));
            case "hint" -> Component.translatable(K + (belts == 0 ? "no_belt" : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
