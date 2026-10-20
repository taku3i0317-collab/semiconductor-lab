package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.FzPullerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** FZ 炉 [9-09] の画面。左＝多結晶シリコン・種結晶・ドーパント、2列目＝アルゴン、中央バー＝進み、出口＝単結晶、右＝電力・状態 */
public class FzPullerScreen extends MachineScreen {
    private static final int BAR_X = 50, BAR_Y = 41, BAR_W = 22, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.fz_puller.";

    public FzPullerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFFA040);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は FzPullerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                String k;
                if (menu.machineItem(FzPullerBlockEntity.SLOT_POLY).getCount() < FzPullerBlockEntity.POLY_PER_INGOT || menu.machineItem(FzPullerBlockEntity.SLOT_SEED).isEmpty()) k = "need_input";
                else if (menu.machineItem(FzPullerBlockEntity.SLOT_AR).getCount() < FzPullerBlockEntity.AR_PER_INGOT) k = "need_ar";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
