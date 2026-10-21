package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.ChemicalCellBlockEntity.*;

/** 化学電池（ボルタ・ダニエル・乾電池の電池入れ）の画面。上＝亜鉛、下＝電解液、右＝いまの出力（W）・今の亜鉛の残り・状態（分極で下がったら知らせる） */
public class ChemicalCellScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.chemical_cell.";

    public ChemicalCellScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        double w = menu.get(DATA_DECI_W) / 10.0;
        int left = menu.get(DATA_LEFT);
        return switch (id) {
            case "output" -> Component.translatable(K + "output", String.format("%.1f W", w));
            case "left" -> Component.translatable(K + "left", left + "%");
            case "hint" -> {
                boolean dry = menu.machine().block == com.taku3.semiconductorlab.content.Power.DRY_CELL_HOLDER.block;
                String k = left <= 0 ? (dry ? "need_dry_cell" : "need_zinc") : (w > 0 && menu.machine().block == com.taku3.semiconductorlab.content.Power.VOLTAIC_PILE.block && w < 3.0 ? "polarized" : "running");
                yield Component.translatable(K + k);
            }
            default -> null;
        };
    }
}
