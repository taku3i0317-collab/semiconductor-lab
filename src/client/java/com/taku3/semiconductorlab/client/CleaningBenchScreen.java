package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.CleaningBenchBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 洗浄槽 [9-14] の画面。左＝加工中のウェハー、2列目＝硝酸・フッ酸・蒸留水、中央バー＝進み、出口＝洗ったウェハー、右＝電力・状態 */
public class CleaningBenchScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 41, BAR_W = 26, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.cleaning_bench.";

    public CleaningBenchScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFA0D8F0);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は CleaningBenchBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(CleaningBenchBlockEntity.SLOT_WAFER);
                String k;
                if (ChipOps.isProcessWafer(w) && CleaningBenchBlockEntity.product(w).isEmpty()) k = "resist_on".equals(ChipOps.clean(w, CleaningBenchBlockEntity.STAGE1_TARGET).reason()) ? "resist_on" : "no_dirt";
                else if (w.isEmpty() || menu.machineItem(CleaningBenchBlockEntity.SLOT_NITRIC).isEmpty() || menu.machineItem(CleaningBenchBlockEntity.SLOT_HF).isEmpty()
                        || menu.machineItem(CleaningBenchBlockEntity.SLOT_WATER).isEmpty()) k = "need_input";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
