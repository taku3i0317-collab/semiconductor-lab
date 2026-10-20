package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.SputterBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** スパッタ装置 [9-26] の画面。左＝加工中のウェハー・Al の的・アルゴン、中央バー＝進み、出口＝Al を付けたウェハー、右＝電力・状態 */
public class SputterScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.sputter.";

    public SputterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFC080FF);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は SputterBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(SputterBlockEntity.SLOT_WAFER), t = menu.machineItem(SputterBlockEntity.SLOT_TARGET), ar = menu.machineItem(SputterBlockEntity.SLOT_AR);
                String k;
                if (w.isEmpty() || t.isEmpty() || ar.isEmpty()) k = "need_input";
                else if (SputterBlockEntity.product(w, t).isEmpty()) k = "cannot";
                else if (menu.get(SputterBlockEntity.DATA_VACUUM) == 0) k = "need_vacuum";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
