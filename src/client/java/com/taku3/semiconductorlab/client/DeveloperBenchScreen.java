package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.DeveloperBenchBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 現像槽 [9-21] の画面。左＝露光したウェハー・現像液、中央バー＝進み、出口＝現像したウェハー、右＝電力・状態 */
public class DeveloperBenchScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.developer_bench.";

    public DeveloperBenchScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE8C060);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は DeveloperBenchBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(DeveloperBenchBlockEntity.SLOT_WAFER), dev = menu.machineItem(DeveloperBenchBlockEntity.SLOT_DEV);
                String k;
                if (w.isEmpty() || dev.isEmpty()) k = "need_input";
                else if (ChipOps.isProcessWafer(w) && !DeveloperBenchBlockEntity.result(w, dev).ok())
                    k = "wrong_developer".equals(DeveloperBenchBlockEntity.result(w, dev).reason()) ? "wrong_developer" : "not_exposed";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
