package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.AutoclaveBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.AutoclaveBlockEntity.*;

/**
 * 加圧加熱釜 [2-28] の画面。左上＝材料、左下＝水バケツ、中央＝進み具合・温度計・圧力計（必要な所に白い印）、
 * 右＝電力・温度（いま／必要）・圧力（いま／必要）・状態。圧力が足りないまま進むと「泡が入った」と赤で出す。
 */
public class AutoclaveScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 30, BAR_W = 40, BAR_H = 6;
    private static final int TEMP_Y = 40, PRES_Y = 48, METER_H = 4;
    private static final String K = "gui.semiconductor-lab.autoclave.";

    public AutoclaveScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), menu.get(DATA_BUBBLES) == 1 ? 0xFFB06040 : 0xFF6A4A2A);
        meter(g, TEMP_Y, menu.get(DATA_TEMP), STEADY_C, menu.get(DATA_NEED_C), 0xFFFF5A1E);
        meter(g, PRES_Y, menu.get(DATA_ATM10) / 10.0, VALVE_ATM, menu.get(DATA_NEED_ATM10) / 10.0, 0xFF5AA0FF);
    }

    private void meter(GuiGraphicsExtractor g, int y0, double v, double max, double need, int color) {
        int x = leftPos + BAR_X, y = topPos + y0;
        g.fill(x, y, x + BAR_W, y + METER_H, 0xFF3A3A40);
        int w = (int) Math.min(BAR_W, v * BAR_W / max);
        if (w > 0) g.fill(x, y, x + w, y + METER_H, color);
        if (need > 0) { int m = x + (int) Math.min(BAR_W - 1, need * BAR_W / max); g.fill(m, y - 1, m + 1, y + METER_H + 1, 0xFFFFFFFF); }
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は AutoclaveBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int t = menu.get(DATA_TEMP), needC = menu.get(DATA_NEED_C);
        double atm = menu.get(DATA_ATM10) / 10.0, needAtm = menu.get(DATA_NEED_ATM10) / 10.0;
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "temp" -> Component.translatable(K + "temp", t + "℃");
            case "pressure" -> { Component c = Component.translatable(K + "pressure", String.format("%.1f atm", atm)); yield needAtm > 0 && atm < needAtm ? warn(c) : c; }
            case "need" -> needC > 0 ? Component.translatable(K + "need", needC + "℃ " + String.format("%.0fatm", needAtm)) : null;
            case "hint" -> {
                String k;
                if (menu.machineItem(SLOT_IN).isEmpty() || needC == 0) k = "need_input";
                else if (!menu.isReady()) k = "output_full";
                else if (menu.getKw() <= 0) k = "need_power";
                else if (menu.get(DATA_BUBBLES) == 1) k = "bubbled";
                else if (t < needC) k = "heating";
                else if (atm < needAtm) k = "low_pressure";
                else k = "curing";
                Component c = Component.translatable(K + k);
                yield k.equals("bubbled") || k.equals("low_pressure") ? warn(c) : c;
            }
            default -> null;
        };
    }
}
