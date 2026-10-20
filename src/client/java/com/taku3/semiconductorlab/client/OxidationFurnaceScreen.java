package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.OxidationFurnaceBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 酸化炉 [9-15] の画面。左＝板・酸素か水、中央バー＝進み具合、出口＝酸化膜つきの板・空のバケツ、右＝電力・状態 */
public class OxidationFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.oxidation_furnace.";

    public OxidationFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFFF9040);   // 約1100℃の炉の色
    }

    private OxidationFurnaceBlockEntity.Mode mode() {
        return OxidationFurnaceBlockEntity.modeOf(menu.machineItem(OxidationFurnaceBlockEntity.SLOT_OXIDANT));
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は OxidationFurnaceBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        var m = mode();
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "need" -> Component.translatable(K + "need", String.format("%.0f kW", OxidationFurnaceBlockEntity.WATTS / 1000));
            case "hint" -> Component.translatable(K + (!menu.isReady() ? "need_input" : menu.getKw() <= 0 ? "need_power"
                    : m == OxidationFurnaceBlockEntity.Mode.WET ? "running_wet" : "running_dry"));
            case "recipe" -> Component.translatable(K + (m == OxidationFurnaceBlockEntity.Mode.DRY ? "recipe_dry" : m == OxidationFurnaceBlockEntity.Mode.WET ? "recipe_wet" : "recipe"));
            default -> null;
        };
    }
}
