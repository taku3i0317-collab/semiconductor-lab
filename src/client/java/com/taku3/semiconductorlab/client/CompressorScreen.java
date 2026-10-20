package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.CompressorBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 圧縮機の画面。左＝空ボンベ、中央バー＝充填の進み具合、出口＝充填ボンベ、右＝電力・蓄積・状態 */
public class CompressorScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.compressor.";

    public CompressorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF4FA3FF);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は CompressorBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "stored" -> Component.translatable(K + "stored", menu.getStoredKj() + " kJ");
            case "hint" -> {
                ItemStack empty = menu.getSlot(CompressorBlockEntity.SLOT_EMPTY).getItem();
                ItemStack out = menu.getSlot(CompressorBlockEntity.SLOT_OUT).getItem();
                String hint;
                if (!empty.is(Gas.EMPTY_GAS_CYLINDER)) hint = "need_cylinder";
                else if (!menu.isReady()) hint = !out.isEmpty() && out.getCount() >= out.getMaxStackSize() ? "output_full" : "need_gas";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            case "mol" -> Component.translatable(K + "mol", CompressorBlockEntity.MOL_PER_CYLINDER);
            default -> null;
        };
    }
}
