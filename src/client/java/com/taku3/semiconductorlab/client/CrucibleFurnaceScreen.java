package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.CrucibleFurnaceBlockEntity.*;

/**
 * るつぼ炉 [3-07] の画面（IMP4-01、塊鉄炉の画面と同じ形）。左上＝仕込み、左下＝コークス、縦の温度計（0〜1700℃、赤い線＝溶ける 1500℃）、
 * 右上＝出来た物、右下＝戻ったるつぼ。位置は CrucibleFurnaceBlockEntity.LAYOUT
 */
public class CrucibleFurnaceScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.crucible_furnace.";
    private static final int T_SCALE = 1700;

    public CrucibleFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        vbar(g, FUEL_X + 3, 36, 10, 14, menu.get(DATA_BURN), menu.get(DATA_BURN_TOTAL), 0xFFFF7A1E);
        int t = menu.get(DATA_TEMP);
        vbar(g, THERMO_X, THERMO_Y, 6, THERMO_H, t, T_SCALE, t >= T_MELT ? 0xFFFF5020 : 0xFFE0A040);
        int ly = topPos + THERMO_Y + THERMO_H - T_MELT * THERMO_H / T_SCALE;
        g.fill(leftPos + THERMO_X - 2, ly, leftPos + THERMO_X + 8, ly + 1, 0xFFC02020);
        bar(g, 54, 38, 20, 6, menu.get(DATA_PROGRESS), menu.get(DATA_TOTAL), 0xFFB0B0B8);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "temp" -> Component.translatable(K + "temp", menu.get(DATA_TEMP));
            case "air" -> Component.translatable(K + "air", menu.get(DATA_AIR));
            case "hint" -> {
                String h;
                if (menu.machineItem(SLOT_CHARGE).isEmpty()) h = "need_charge";
                else if (menu.get(DATA_BURN) <= 0 && menu.machineItem(SLOT_FUEL).isEmpty()) h = "need_fuel";
                else if (menu.get(DATA_TEMP) < T_MELT) h = "need_air";
                else if (menu.get(DATA_PROGRESS) > 0) h = "melting";
                else h = "out_full";
                yield Component.translatable(K + h);
            }
            case "formula" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
