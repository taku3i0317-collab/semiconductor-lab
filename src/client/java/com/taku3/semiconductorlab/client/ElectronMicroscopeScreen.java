package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ElectronMicroscopeBlockEntity;
import com.taku3.semiconductorlab.item.MicroscopeItem;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** 電子顕微鏡 [8-11] の画面（IMP2 O-017）。左上＝見る物。下に、電子の波長と分解能・光学顕微鏡の限界・見る物の1マスの細さ・どちらで見えるか */
public class ElectronMicroscopeScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.electron_microscope.";

    public ElectronMicroscopeScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    private static String len(double nm) { return nm >= 1000 ? String.format(Locale.ROOT, "%.1f µm", nm / 1000) : String.format(Locale.ROOT, "%.0f nm", nm); }

    @Override
    protected Component text(String id) {
        ItemStack s = menu.getSlot(ElectronMicroscopeBlockEntity.SLOT_SAMPLE).getItem();
        int size = MicroscopeItem.sizeOf(s);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "limit" -> Component.translatable(K + "limit", String.format(Locale.ROOT, "%.4f", ElectronMicroscopeBlockEntity.electronWavelengthNm(ElectronMicroscopeBlockEntity.KV)),
                    String.format(Locale.ROOT, "%.0f", ElectronMicroscopeBlockEntity.RESOLUTION_NM));
            case "optical" -> Component.translatable(K + "optical", String.format(Locale.ROOT, "%.0f", MicroscopeItem.resolutionNm(0.9)));
            case "cell" -> size > 0 ? Component.translatable(K + "cell", size, size, len(MicroscopeItem.cellNm(size))) : Component.empty();
            case "hint" -> {
                int on = menu.get(ElectronMicroscopeBlockEntity.DATA_ON);
                String hint;
                if (on == 0) hint = "need_vacuum";
                else if (on == 1) hint = "need_power";
                else if (size <= 0) hint = "need_sample";
                else if (!ElectronMicroscopeBlockEntity.canResolve(size)) hint = "neither";
                else if (MicroscopeItem.cellNm(size) >= MicroscopeItem.resolutionNm(0.9)) hint = "both";
                else hint = "em_only";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
