package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.GasHolderBlockEntity;
import com.taku3.semiconductorlab.block.GasType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ガスホルダーの画面。左＝袋詰めガスの入口、中央＝貯蔵量バーとガスの種類・量・体積、右＝出口 */
public class GasHolderScreen extends MachineScreen {
    private static final int BAR_X = 48, BAR_Y = 18, BAR_W = 80, BAR_H = 8;
    private static final int TEXT_X = 48, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.gas_holder.";
    /** 25℃・1気圧の 1mol の体積（L） */
    private static final double LITERS_PER_MOL = 24.5;

    public GasHolderScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    private static int color(GasType t) {
        return switch (t) { case HYDROGEN -> 0xFFE8E070; case OXYGEN -> 0xFF6FB7FF; case CHLORINE -> 0xFFB8D040; case NITROGEN -> 0xFFB0B8C8; case ARGON -> 0xFFC090E0; default -> 0xFF808080; };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.get(GasHolderBlockEntity.DATA_AMOUNT) * BAR_W / GasHolderBlockEntity.CAPACITY;
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, color(GasType.byIndex(menu.get(GasHolderBlockEntity.DATA_TYPE))));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        GasType t = GasType.byIndex(menu.get(GasHolderBlockEntity.DATA_TYPE));
        int amount = menu.get(GasHolderBlockEntity.DATA_AMOUNT);
        text(g, Component.translatable(K + "gas." + t.key), TEXT_X, 30);
        text(g, Component.literal(amount + "/" + GasHolderBlockEntity.CAPACITY + " mol"), TEXT_X, 41);
        text(g, Component.literal(String.format("≈%.1f m³", amount * LITERS_PER_MOL / 1000)), TEXT_X, 52);
        if ((menu.get(GasHolderBlockEntity.DATA_BANG) > 0)) text(g, Component.translatable(K + "bang"), TEXT_X, 63, 0xFFC03030);
        text(g, Component.translatable(K + "in"), 22, 54);
        text(g, Component.translatable(K + "out"), 130, 54);
    }
}
