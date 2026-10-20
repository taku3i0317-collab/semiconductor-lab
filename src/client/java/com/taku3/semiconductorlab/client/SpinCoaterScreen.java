package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.SpinCoaterBlockEntity;
import com.taku3.semiconductorlab.item.PositiveResistItem;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** レジスト塗布機 [9-19] の画面。左＝ウェハー・レジスト、中央バー＝進み、出口＝塗ったウェハー、右＝電力・状態 */
public class SpinCoaterScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.spin_coater.";

    public SpinCoaterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE0A040);   // レジストの色（黄色い照明の部屋）
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は SpinCoaterBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(SpinCoaterBlockEntity.SLOT_WAFER), r = menu.machineItem(SpinCoaterBlockEntity.SLOT_RESIST);
                String k;
                if (w.isEmpty() || r.isEmpty()) k = "need_input";
                else if (PositiveResistItem.spoiled(r)) k = "spoiled";
                else if (SpinCoaterBlockEntity.product(w).isEmpty()) k = "cannot";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
