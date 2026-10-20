package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.EvaporatorBlockEntity;
import com.taku3.semiconductorlab.block.SealingStationBlockEntity;
import com.taku3.semiconductorlab.block.VacuumChamberBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 真空蒸着装置 [5-14] の画面（IMP2-01）。左上＝基板、左下＝蒸着する金属、右＝製品、中央バー＝進み、右側＝電力・容器の圧力・目標・状態 */
public class EvaporatorScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.evaporator.";

    public EvaporatorScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, 30, 38, 40, 6, menu.getProgress(), menu.getTotal(), 0xFFD8E0E8);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は EvaporatorBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        double pa = SealingStationBlockEntity.decodePa(menu.get(EvaporatorBlockEntity.DATA_PRESSURE));
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "pressure" -> Component.translatable(K + "pressure", pa < 0 ? "-" : VacuumChamberBlockEntity.formatPa(pa));
            case "target" -> Component.translatable(K + "target", VacuumChamberBlockEntity.formatPa(EvaporatorBlockEntity.MAX_PA));
            case "hint" -> {
                String hint;
                if (EvaporatorBlockEntity.recipe(menu.getSlot(EvaporatorBlockEntity.SLOT_SUBSTRATE).getItem(),
                        menu.getSlot(EvaporatorBlockEntity.SLOT_METAL).getItem()) == null) hint = "need_input";
                else if (pa < 0) hint = "need_chamber";
                else if (pa > EvaporatorBlockEntity.MAX_PA) hint = "need_vacuum";
                else if (!menu.isReady()) hint = "output_full";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
