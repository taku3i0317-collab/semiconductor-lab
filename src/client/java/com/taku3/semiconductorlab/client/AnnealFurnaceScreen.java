package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.AnnealFurnaceBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** アニール炉 [9-29] の画面。左＝注入したウェハー・窒素かアルゴン、中央バー＝進み、出口＝アニールしたウェハー、右＝電力・状態 */
public class AnnealFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.anneal_furnace.";

    public AnnealFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFF8030);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は AnnealFurnaceBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(AnnealFurnaceBlockEntity.SLOT_WAFER), gas = menu.machineItem(AnnealFurnaceBlockEntity.SLOT_GAS);
                String k;
                if (w.isEmpty() || gas.isEmpty()) k = "need_input";
                else if (AnnealFurnaceBlockEntity.product(w).isEmpty()) k = "resist_on".equals(AnnealFurnaceBlockEntity.reason(w)) ? "resist_on" : "nothing";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
