package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.energy.SteamEngineBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** 蒸気機関の画面。上＝水バケツ、下＝石炭、間の炎＝燃焼の残り時間。右に出力・蓄積・水の残り */
public class SteamEngineScreen extends MachineScreen {
    private static final int FLAME_X = 57, FLAME_Y = 37, FLAME_W = 14, FLAME_H = 14;
    private static final int TEXT_X = 82, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.steam_engine.";

    public SteamEngineScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        // 炎：燃えている間、残り時間に応じて下から縮む（かまどと同じ見せ方）
        int x = leftPos + FLAME_X, y = topPos + FLAME_Y;
        g.fill(x, y, x + FLAME_W, y + FLAME_H, 0xFF3A3A40);
        int h = menu.get(SteamEngineBlockEntity.DATA_BURN) * FLAME_H / Math.max(1, menu.get(SteamEngineBlockEntity.DATA_BURN_TOTAL));
        if (h > 0) g.fill(x, y + FLAME_H - h, x + FLAME_W, y + FLAME_H, 0xFFFF8C1E);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は SteamEngineBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int water = menu.get(SteamEngineBlockEntity.DATA_WATER);
        return switch (id) {
            case "output" -> Component.translatable(K + "output", String.format("%.1f kW", menu.get(SteamEngineBlockEntity.DATA_DECI_KW) / 10.0));
            case "stored" -> Component.translatable(K + "stored", menu.get(SteamEngineBlockEntity.DATA_KJ) + " kJ");
            case "water" -> water < 0 ? Component.translatable(K + "water_intake") : Component.translatable(K + "water", water);
            case "hint" -> {
                if (menu.get(SteamEngineBlockEntity.DATA_BURN) > 0) yield Component.translatable(K + "running");
                if (water == 0 && !menu.getSlot(1).getItem().is(Items.WATER_BUCKET)) yield Component.translatable(K + "need_water");
                if (menu.getSlot(0).getItem().isEmpty()) yield Component.translatable(K + "need_coal");
                yield null;
            }
            default -> null;
        };
    }
}
