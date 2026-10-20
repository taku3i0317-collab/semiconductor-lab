package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.ElectroplatingBathBlockEntity.*;

/** めっき槽 [2-12] の画面。左＝銅の陽極・硫酸銅・型、中央バー＝くっついた銅、右＝電力・電流・電気の量・銅の重さ（ファラデーの法則）・状態 */
public class ElectroplatingBathScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.electroplating_bath.";

    public ElectroplatingBathScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFD07A3A);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "amps" -> Component.translatable(K + "amps", menu.get(DATA_AMPS) + " A");
            case "charge" -> Component.translatable(K + "charge", menu.get(DATA_KC) + " kC");
            case "mass" -> Component.translatable(K + "mass", String.format("%.1f g", menu.get(DATA_DECI_GRAMS) / 10.0));
            case "hint" -> {
                String k;
                if (menu.machineItem(SLOT_ANODE).isEmpty()) k = "need_anode";
                else if (menu.machineItem(SLOT_ELECTROLYTE).isEmpty()) k = "need_electrolyte";
                else if (menu.machineItem(SLOT_MOULD).isEmpty()) k = "need_mould";
                else if (!menu.isReady()) k = "output_full";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "plating";
                yield Component.translatable(K + k);
            }
            default -> null;
        };
    }
}
