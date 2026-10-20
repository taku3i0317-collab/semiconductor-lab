package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.CrystalPullerBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 引き上げ機の画面。左＝Ge・種・水素、中央バー＝引き上げの進み具合、出口、右＝電力・状態・モード */
public class CrystalPullerScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.crystal_puller.";

    public CrystalPullerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFC0C8D0);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は CrystalPullerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        var mode = CrystalPullerBlockEntity.modeOf(menu.getSlot(CrystalPullerBlockEntity.SLOT_SEED).getItem());
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "stored" -> Component.translatable(K + "stored", menu.getStoredKj() + " kJ");
            case "hint" -> {
                String hint;
                if (mode == CrystalPullerBlockEntity.Mode.NONE) hint = "need_seed";
                else if (!menu.getSlot(CrystalPullerBlockEntity.SLOT_H2).getItem().is(Gas.HYDROGEN_GAS)) hint = "need_h2";
                else if (!menu.isReady()) hint = "need_ge";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            case "mode" -> mode == CrystalPullerBlockEntity.Mode.NONE ? null : Component.translatable(K + "mode." + mode.name().toLowerCase());
            default -> null;
        };
    }
}
