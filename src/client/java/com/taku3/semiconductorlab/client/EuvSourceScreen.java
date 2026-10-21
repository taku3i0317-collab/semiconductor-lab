package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.EuvSourceBlockEntity;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** EUV 光源 [7-18] の画面（IMP2 S-007）。左＝スズ・液滴発生器・集光鏡・水素、右＝電力・EUV の出力・駆動している CO2 レーザー */
public class EuvSourceScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.euv_source.";

    public EuvSourceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "euv" -> Component.translatable(K + "euv", String.format("%.2f W", menu.get(EuvSourceBlockEntity.DATA_EUV_MW) / 1000.0));
            case "lasers" -> Component.translatable(K + "lasers", menu.get(EuvSourceBlockEntity.DATA_LASERS));
            case "hint" -> {
                String h;
                if (!menu.getSlot(EuvSourceBlockEntity.SLOT_TIN).getItem().is(Metallurgy.TIN_INGOT)) h = "need_tin";
                else if (!menu.getSlot(EuvSourceBlockEntity.SLOT_DROPLET).getItem().is(Precision.DROPLET_GENERATOR)) h = "need_droplet";
                else if (!menu.getSlot(EuvSourceBlockEntity.SLOT_COLLECTOR).getItem().is(Optics.MULTILAYER_MIRROR)) h = "need_collector";
                else if (menu.get(EuvSourceBlockEntity.DATA_DIRTY) > 0) h = "dirty";
                else if (menu.getKw() <= 0) h = "need_power";
                else h = "running";
                yield Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
