package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.ArcFurnaceBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** アーク炉の画面。左＝珪石・木炭・電極、中央バー＝進み具合、出口＝金属シリコン、右＝電力・状態 */
public class ArcFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.arc_furnace.";

    public ArcFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFB8E0FF);   // アークの青白い光
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ArcFurnaceBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "need" -> Component.translatable(K + "need", String.format("%.0f kW", ArcFurnaceBlockEntity.WATTS / 1000));
            case "hint" -> Component.translatable(K + (!menu.isReady() ? "need_input" : menu.getKw() <= 0 ? "need_power" : "running"));
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
