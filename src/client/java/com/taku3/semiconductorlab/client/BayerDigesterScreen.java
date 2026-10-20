package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.BayerDigesterBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** バイヤー法の溶解釜 [1-14] の画面。左＝ボーキサイト・NaOH・水、中央バー＝進み、出口＝水酸化アルミニウム・赤泥・空のバケツ、右＝電力・液・状態 */
public class BayerDigesterScreen extends MachineScreen {
    private static final int BAR_X = 28, BAR_Y = 39, BAR_W = 24, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.bayer_digester.";

    public BayerDigesterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFC07040);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は BayerDigesterBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int liquor = menu.get(BayerDigesterBlockEntity.DATA_LIQUOR);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "liquor" -> Component.translatable(K + "liquor", String.valueOf(liquor));
            case "hint" -> {
                boolean bx = menu.machineItem(BayerDigesterBlockEntity.SLOT_BAUXITE).getCount() >= BayerDigesterBlockEntity.BAUXITE_PER_RUN;
                boolean lq = liquor > 0 || (!menu.machineItem(BayerDigesterBlockEntity.SLOT_NAOH).isEmpty() && !menu.machineItem(BayerDigesterBlockEntity.SLOT_WATER).isEmpty());
                yield Component.translatable(K + (!bx ? "need_input" : !lq ? "need_liquor" : menu.getKw() <= 0 ? "need_power" : "running"));
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
