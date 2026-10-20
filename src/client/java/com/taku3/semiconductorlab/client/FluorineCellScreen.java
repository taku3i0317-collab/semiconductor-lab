package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.FluorineCellBlockEntity;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** フッ素の電解槽 [6-14] の画面（IMP2 O-018）。左＝KF・陽極・フッ酸・空のボンベ、右＝フッ素のボンベ・水素の袋。事故は赤で知らせる */
public class FluorineCellScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.fluorine_cell.";

    public FluorineCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "f2" -> Component.translatable(K + "f2", menu.get(FluorineCellBlockEntity.DATA_F2), FluorineCellBlockEntity.F2_PER_CYLINDER);
            case "hint" -> {
                String h;
                if (menu.get(FluorineCellBlockEntity.DATA_ACCIDENT) > 0) h = "accident";
                else if (!menu.getSlot(FluorineCellBlockEntity.SLOT_KF).getItem().is(Chemistry.POTASSIUM_FLUORIDE)) h = "need_kf";
                else if (!menu.getSlot(FluorineCellBlockEntity.SLOT_ANODE).getItem().is(Metallurgy.GRAPHITE)) h = "need_anode";
                else if (!menu.getSlot(FluorineCellBlockEntity.SLOT_HF).getItem().is(Chemistry.HYDROFLUORIC_ACID)) h = "need_hf";
                else if (!menu.getSlot(FluorineCellBlockEntity.SLOT_CYL).getItem().is(Gas.EMPTY_GAS_CYLINDER)) h = "need_cylinder";
                else if (menu.getKw() <= 0) h = "need_power";
                else h = "running";
                yield Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
