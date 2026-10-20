package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.HallHeroultCellBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ホール・エルー電解槽 [1-29] の画面。左＝アルミナ・氷晶石・黒鉛棒（陽極）、中央バー＝進み、出口＝アルミニウム、右＝電力・浴・状態 */
public class HallHeroultCellScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.hall_heroult_cell.";

    public HallHeroultCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFFFB050);   // 約960℃の浴
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は HallHeroultCellBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        int bath = menu.get(HallHeroultCellBlockEntity.DATA_BATH), anode = menu.get(HallHeroultCellBlockEntity.DATA_ANODE);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "bath" -> Component.translatable(K + "bath", String.valueOf(bath));
            case "hint" -> {
                String k;
                if (menu.machineItem(HallHeroultCellBlockEntity.SLOT_ALUMINA).getCount() < HallHeroultCellBlockEntity.ALUMINA_PER_RUN) k = "need_alumina";
                else if (bath <= 0 && menu.machineItem(HallHeroultCellBlockEntity.SLOT_CRYOLITE).isEmpty()) k = "need_bath";
                else if (anode <= 0 && menu.machineItem(HallHeroultCellBlockEntity.SLOT_ANODE).isEmpty()) k = "need_anode";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
