package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity;
import com.taku3.semiconductorlab.block.BessemerConverterBlockEntity.Stage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 転炉の画面。左＝銑鉄、中央バー＝吹錬の進み（色＝炉口の炎の色）、右＝鋼。下の行＝今の段階（炎の見え方） */
public class BessemerConverterScreen extends MachineScreen {
    private static final int BAR_X = 48, BAR_Y = 20, BAR_W = 80, BAR_H = 8;
    private static final int TEXT_X = 48, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.bessemer_converter.";

    public BessemerConverterScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    /** 炎の色：①ケイ素＝赤い火花まじりの短い炎 ②炭素＝白く明るい長い炎 ③炎が落ちる＝暗い橙 */
    private static int flameColor(Stage s) {
        return switch (s) { case SILICON -> 0xFFE0602A; case CARBON -> 0xFFFFF4D0; case DROP -> 0xFFB07040; };
    }

    private boolean running() { return menu.isReady() && menu.getKw() > 0; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, flameColor(BessemerConverterBlockEntity.stageOf(menu.getProgress(), menu.getTotal())));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 32, TEXT_COLOR, false);
        g.text(font, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 43, TEXT_COLOR, false);
        // 下の行：動いていれば炎の段階、止まっていれば足りないもの
        String line;
        if (!BessemerConverterBlockEntity.isPigIron(menu.getSlot(BessemerConverterBlockEntity.SLOT_IN).getItem())) line = "need_iron";
        else if (!menu.isReady()) line = "output_full";
        else if (menu.getKw() <= 0) line = "need_power";
        else line = "stage." + BessemerConverterBlockEntity.stageOf(menu.getProgress(), menu.getTotal()).name().toLowerCase();
        g.text(font, Component.translatable(K + line), 8, 60, running() ? 0xFF803010 : TEXT_COLOR, false);
    }
}
