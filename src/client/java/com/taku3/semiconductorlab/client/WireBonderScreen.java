package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.WireBonderBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** ワイヤーボンダー [9-33] の画面。左＝チップ・金のボンディング線・ヘッダー、中央バー＝進み、出口＝線をつないだチップ、右＝電力・パッドの数・状態 */
public class WireBonderScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.wire_bonder.";

    public WireBonderScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE8C040);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は WireBonderBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int pads = menu.get(WireBonderBlockEntity.DATA_PADS);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "pads" -> Component.translatable(K + "pads", String.valueOf(pads));
            case "hint" -> {
                ItemStack c = menu.machineItem(WireBonderBlockEntity.SLOT_CHIP), wire = menu.machineItem(WireBonderBlockEntity.SLOT_WIRE), hd = menu.machineItem(WireBonderBlockEntity.SLOT_HEADER);
                String k;
                if (c.isEmpty() || hd.isEmpty()) k = "need_input";
                else if (pads == 0) k = "no_pads";
                else if (wire.getCount() < pads) k = "need_wire";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
