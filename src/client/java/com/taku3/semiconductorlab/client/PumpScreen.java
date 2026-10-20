package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.RotaryPumpBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 回転ポンプ [5-04]・拡散ポンプ [5-05] の画面（IMP2-01）。右に 電力・容器の圧力・状態（容器がない／前段ポンプがない／圧力が高すぎる／電気が足りない／動作中）。
 * 2台とも同じデータの並び（RotaryPumpBlockEntity.DATA_PRESSURE・DATA_STATUS、STATUS_* は拡散ポンプも同じ値）。
 */
public class PumpScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.pump.";
    private static final String[] STATUS = {"running", "no_chamber", "no_backing", "too_high", "need_power"};

    public PumpScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable("gui.semiconductor-lab.machine.power", String.format("%.1f kW", menu.getKw()));
            case "pressure" -> {
                double pa = SealingStationBlockEntity.decodePa(menu.get(RotaryPumpBlockEntity.DATA_PRESSURE));
                yield Component.translatable(K + "pressure", pa < 0 ? "-" : VacuumChamberBlockEntity.formatPa(pa));
            }
            case "hint" -> {
                int s = menu.get(RotaryPumpBlockEntity.DATA_STATUS);
                yield s == 4 ? Component.translatable("gui.semiconductor-lab.machine.need_power") : Component.translatable(K + STATUS[Math.max(0, Math.min(3, s))]);
            }
            default -> null;
        };
    }
}
