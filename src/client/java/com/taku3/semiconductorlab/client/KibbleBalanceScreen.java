package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.KibbleBalanceBlockEntity;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** ワット天秤 [8-25] の画面（IMP2 S-005）。左＝原器・標準電池・標準抵抗・クリプトン86 ランプ、右＝測った結果とあいまいさ */
public class KibbleBalanceScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.kibble_balance.";

    public KibbleBalanceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    /** µg を見やすく：1000µg 以上は mg */
    static String ug(long v, boolean sign) {
        if (Math.abs(v) >= 1000) return String.format(sign ? "%+.1f mg" : "%.1f mg", v / 1000.0);
        return String.format(sign ? "%+d µg" : "%d µg", v);
    }

    @Override
    protected Component text(String id) {
        boolean has = menu.get(KibbleBalanceBlockEntity.DATA_HAS) > 0;
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "result" -> Component.translatable(K + "result", has ? ug(menu.get(KibbleBalanceBlockEntity.DATA_RESULT), true) : "-");
            case "sigma" -> Component.translatable(K + "sigma", has ? ug(menu.get(KibbleBalanceBlockEntity.DATA_SIGMA), false) : "-");
            case "hint" -> {
                String h;
                if (!menu.getSlot(KibbleBalanceBlockEntity.SLOT_MASS).getItem().is(Precision.KILOGRAM_PROTOTYPE)) h = "need_mass";
                else if (!menu.getSlot(KibbleBalanceBlockEntity.SLOT_CELL).getItem().is(Precision.WESTON_CELL)) h = "need_cell";
                else if (!menu.getSlot(KibbleBalanceBlockEntity.SLOT_RES).getItem().is(Precision.STANDARD_RESISTOR)) h = "need_resistor";
                else if (!menu.getSlot(KibbleBalanceBlockEntity.SLOT_LAMP).getItem().is(Precision.KRYPTON_LAMP)) h = "need_lamp";
                else if (menu.getKw() <= 0 && !has) h = "need_power";
                else if (!has) h = "running";
                else h = menu.get(KibbleBalanceBlockEntity.DATA_SIGMA) > 50 ? "hidden" : "seen";
                yield Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
