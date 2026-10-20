package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.PeroxideCellBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 過酸化水素の電解槽 [2-19] の画面。左＝硫酸・白金の陽極・水・空の瓶、中央バー＝進み、出口＝過酸化水素・水素・空のバケツ、右＝電力・水・状態 */
public class PeroxideCellScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 41, BAR_W = 20, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.peroxide_cell.";

    public PeroxideCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF80C0FF);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は PeroxideCellBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int water = menu.get(PeroxideCellBlockEntity.DATA_WATER);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "water" -> Component.translatable(K + "water", String.valueOf(water));
            case "hint" -> {
                String k;
                if (menu.isReady()) k = menu.getKw() <= 0 ? "need_power" : "running";
                else if (menu.machineItem(PeroxideCellBlockEntity.SLOT_ANODE).isEmpty()) k = "need_anode";
                else k = "need_input";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
