package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.Co2LaserBlockEntity;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** CO2 レーザー [7-14] の画面（IMP2 O-019）。左＝窓・珪石・CO2・N2、右＝溶融石英。赤外線の出力と残りの回数を出す */
public class Co2LaserScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.co2_laser.";

    public Co2LaserScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "beam" -> Component.translatable(K + "beam", menu.get(Co2LaserBlockEntity.DATA_BEAM) + " W", menu.get(Co2LaserBlockEntity.DATA_RUNS));
            case "hint" -> {
                String h;
                boolean gas = menu.get(Co2LaserBlockEntity.DATA_RUNS) > 0 || menu.getSlot(Co2LaserBlockEntity.SLOT_CO2).getItem().is(Gas.CARBON_DIOXIDE_GAS);
                if (menu.get(Co2LaserBlockEntity.DATA_CRACKED) > 0) h = "cracked";
                else if (!Co2LaserBlockEntity.isWindow(menu.getSlot(Co2LaserBlockEntity.SLOT_WINDOW).getItem())) h = "need_window";
                else if (!gas) h = "need_co2";
                else if (menu.get(Co2LaserBlockEntity.DATA_BEAM) < Co2LaserBlockEntity.MELT_W) h = "weak";
                else if (!menu.getSlot(Co2LaserBlockEntity.SLOT_IN).getItem().is(Metallurgy.VEIN_QUARTZ)) h = "need_input";
                else if (!menu.getSlot(Co2LaserBlockEntity.SLOT_OUT).getItem().isEmpty()
                        && menu.getSlot(Co2LaserBlockEntity.SLOT_OUT).getItem().getCount() >= menu.getSlot(Co2LaserBlockEntity.SLOT_OUT).getItem().getMaxStackSize()) h = "output_full";
                else if (menu.getKw() <= 0) h = "need_power";
                else h = "running";
                yield Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
