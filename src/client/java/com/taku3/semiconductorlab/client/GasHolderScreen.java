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

    /** 置き場所ごとに今出す文（位置・幅・行数は GasHolderBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int amount = menu.get(GasHolderBlockEntity.DATA_AMOUNT);
        return switch (id) {
            case "gas" -> Component.translatable(K + "gas." + GasType.byIndex(menu.get(GasHolderBlockEntity.DATA_TYPE)).key);
            case "amount" -> Component.literal(amount + "/" + GasHolderBlockEntity.CAPACITY + " mol");
            case "volume" -> Component.literal(String.format("≈%.1f m³", amount * LITERS_PER_MOL / 1000));
            case "bang" -> menu.get(GasHolderBlockEntity.DATA_BANG) > 0 ? Component.translatable(K + "bang").withStyle(s -> s.withColor(0xC03030)) : null;
            case "in" -> Component.translatable(K + "in");
            case "out" -> Component.translatable(K + "out");
            default -> null;
        };
    }
}
