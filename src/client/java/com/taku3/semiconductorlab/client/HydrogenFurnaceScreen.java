package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.HydrogenFurnaceBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 水素炉の画面。左上＝WO3/W粉、左下＝水素、中央バー＝進み具合（橙＝還元、白＝焼結）、右＝電力・蓄積・状態 */
public class HydrogenFurnaceScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.hydrogen_furnace.";

    public HydrogenFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    private HydrogenFurnaceBlockEntity.Mode mode() { return HydrogenFurnaceBlockEntity.modeOf(menu.getSlot(HydrogenFurnaceBlockEntity.SLOT_IN).getItem()); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        int color = mode() == HydrogenFurnaceBlockEntity.Mode.SINTER ? 0xFFFFF4D6 : 0xFFFF8C1E;
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, color);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        text(g, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20);
        text(g, Component.translatable(K + "stored", menu.getStoredKj() + " kJ"), TEXT_X, 32);
        ItemStack h2 = menu.getSlot(HydrogenFurnaceBlockEntity.SLOT_H2).getItem();
        String hint;
        HydrogenFurnaceBlockEntity.Mode m = mode();
        if (m == HydrogenFurnaceBlockEntity.Mode.NONE) hint = "need_input";
        else if (!h2.is(Gas.HYDROGEN_GAS)) hint = "need_h2";
        else if (!menu.isReady()) hint = "not_enough";   // 量が足りない or 出口が満杯
        else if (menu.getKw() <= 0) hint = "need_power";
        else hint = switch (m) { case SINTER -> "sintering"; case REDUCE_GE -> "reducing_ge"; default -> "reducing"; };
        text(g, Component.translatable(K + hint), TEXT_X, 48);
        text(g, Component.literal("H2"), 28, 57);
    }
}
