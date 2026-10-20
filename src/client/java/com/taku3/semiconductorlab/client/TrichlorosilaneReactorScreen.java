package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.TrichlorosilaneReactorBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** トリクロロシラン反応器の画面。左＝金属シリコン・塩化水素・蒸留するトリクロロシラン、中央バー＝進み具合、出口＝トリクロロシラン・水素、右＝電力・状態 */
public class TrichlorosilaneReactorScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.trichlorosilane_reactor.";

    public TrichlorosilaneReactorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, (menu.get(TrichlorosilaneReactorBlockEntity.DATA_MODE) != 0) ? 0xFF9AD0FF : 0xFFE0C080);   // 蒸留＝水色、合成＝黄土色
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は TrichlorosilaneReactorBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        boolean distill = menu.get(TrichlorosilaneReactorBlockEntity.DATA_MODE) != 0;
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "need" -> Component.translatable(K + "need", String.format("%.0f kW", (distill ? TrichlorosilaneReactorBlockEntity.DISTILL_WATTS : TrichlorosilaneReactorBlockEntity.SYNTH_WATTS) / 1000));
            case "hint" -> Component.translatable(K + (!menu.isReady() ? "need_input" : menu.getKw() <= 0 ? "need_power" : distill ? "distilling" : "running"));
            case "recipe" -> Component.translatable(K + (distill ? "recipe_distill" : "recipe"));
            default -> null;
        };
    }
}
