package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 封止台の画面。左＝組立品、右＝真空管、中央バー＝進み具合、右側＝電力・容器の圧力・目標・状態 */
public class SealingStationScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 38, BAR_W = 40, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.sealing_station.";

    public SealingStationScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFFFFC870);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        double pa = SealingStationBlockEntity.decodePa(menu.get(SealingStationBlockEntity.DATA_PRESSURE));
        text(g, Component.translatable(K + "power", String.format("%.1f kW", menu.getKw())), TEXT_X, 20);
        text(g, Component.translatable(K + "pressure", pa < 0 ? "-" : VacuumChamberBlockEntity.formatPa(pa)), TEXT_X, 32);
        text(g, Component.translatable(K + "target", VacuumChamberBlockEntity.formatPa(SealingStationBlockEntity.SEAL_MAX_PA)), TEXT_X, 44);
        String hint;
        if (!menu.getSlot(SealingStationBlockEntity.SLOT_IN).getItem().is(Vacuum.VACUUM_TUBE_ASSEMBLY)) hint = "need_input";
        else if (pa < 0) hint = "need_chamber";
        else if (pa > SealingStationBlockEntity.SEAL_MAX_PA) hint = "need_vacuum";
        else if (!menu.isReady()) hint = "output_full";
        else if (menu.getKw() <= 0) hint = "need_power";
        else hint = "sealing";
        text(g, Component.translatable(K + hint), TEXT_X, 60);
    }
}
