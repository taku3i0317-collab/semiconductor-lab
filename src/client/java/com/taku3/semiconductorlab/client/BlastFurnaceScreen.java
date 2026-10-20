package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.BlastFurnaceBlockEntity.*;

/**
 * 高炉の画面。左＝鉄鉱石・石灰石・木炭（上に残りの火）、縦の温度計（0〜1600℃、赤い線＝銑鉄が流れる 1400℃）、右＝銑鉄・スラグ。
 * 右の列＝温度・送風・状態、下の行＝組み上がっていないときの足りない物（位置は BlastFurnaceBlockEntity.LAYOUT）。
 * 画面を開いたとき、組み上がっていなければ世界の足りない場所に粒子が出る（サーバー）。
 */
public class BlastFurnaceScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.blast_furnace.";
    private static final int T_SCALE = 1600;

    public BlastFurnaceScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        vbar(g, FUEL_X + 3, 36, 10, 14, menu.get(DATA_BURN), menu.get(DATA_BURN_TOTAL), 0xFFFF7A1E);
        int t = menu.get(DATA_TEMP);
        vbar(g, THERMO_X, THERMO_Y, 6, THERMO_H, t, T_SCALE, t >= T_MELT ? 0xFFFF5020 : 0xFFE0A040);
        int ly = topPos + THERMO_Y + THERMO_H - T_MELT * THERMO_H / T_SCALE;
        g.fill(leftPos + THERMO_X - 2, ly, leftPos + THERMO_X + 8, ly + 1, 0xFFC02020);
        bar(g, 58, 30, 16, 6, menu.get(DATA_PROGRESS), menu.get(DATA_TOTAL), 0xFFFF9030);
    }

    @Override
    protected Component text(String id) {
        boolean formed = menu.get(DATA_FORMED) != 0;
        return switch (id) {
            case "temp" -> Component.translatable(K + "temp", menu.get(DATA_TEMP));
            case "air" -> Component.translatable(K + "air", menu.get(DATA_AIR));
            case "missing" -> formed ? null : Component.translatable(K + "missing", menu.get(DATA_MISSING),
                    Component.translatable("multiblock.semiconductor-lab." + NAMES.get(Math.min(NAMES.size() - 1, menu.get(DATA_MISSING_KIND)))));
            case "hint" -> {
                String h;
                if (!formed) h = "not_formed";
                else if (menu.machineItem(SLOT_ORE).isEmpty()) h = "need_ore";
                else if (menu.get(DATA_BURN) <= 0 && menu.machineItem(SLOT_FUEL).isEmpty()) h = "need_fuel";
                else if (menu.get(DATA_TEMP) < T_MELT) h = "need_air";
                else if (menu.get(DATA_PROGRESS) > 0) h = "smelting";
                else if (menu.machineItem(SLOT_FLUX).isEmpty()) h = "need_flux";
                else h = "out_full";
                yield Component.translatable(K + h);
            }
            default -> null;
        };
    }
}
