package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.SiliconeReactorBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** シリコーンの反応器 [5-04]③ の画面。左＝原料2つ・触媒・空き瓶、中央バー＝進み、出口2つ、右＝電力・どの反応か */
public class SiliconeReactorScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.silicone_reactor.";

    public SiliconeReactorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF80C0E0);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は SiliconeReactorBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                int m = SiliconeReactorBlockEntity.mode(menu.machineItem(SiliconeReactorBlockEntity.SLOT_A), menu.machineItem(SiliconeReactorBlockEntity.SLOT_B));
                String k = SiliconeReactorBlockEntity.missing(m, menu.machineItem(SiliconeReactorBlockEntity.SLOT_CAT), menu.machineItem(SiliconeReactorBlockEntity.SLOT_BOTTLE));
                if (k == null) k = menu.getKw() <= 0 ? "need_power" : m == SiliconeReactorBlockEntity.MODE_MECL ? "mecl" : m == SiliconeReactorBlockEntity.MODE_DIRECT ? "direct" : "hydro";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
