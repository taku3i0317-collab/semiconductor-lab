package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.taku3.semiconductorlab.block.SolvayTowerBlockEntity.*;

/** ソルベー塔 [2-06] の画面。左＝塩・方解石（石灰石）・アンモニア水、右＝電力・回っているアンモニア・補給までの回数・状態 */
public class SolvayTowerScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.solvay_tower.";

    public SolvayTowerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFFE8E8E0);
    }

    @Override
    protected Component text(String id) {
        int nh3 = menu.get(DATA_NH3);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "nh3" -> Component.translatable(K + "nh3", String.valueOf(nh3));
            case "loss" -> Component.translatable(K + "loss", String.valueOf(menu.get(DATA_UNTIL_LOSS)));
            case "hint" -> {
                String k;
                if (menu.machineItem(SLOT_SALT).getCount() < SALT_PER_RUN || menu.machineItem(SLOT_LIMESTONE).isEmpty()) k = "need_input";
                else if (nh3 < NH3_PER_RUN && menu.machineItem(SLOT_AMMONIA).isEmpty()) k = "need_ammonia";
                else if (!menu.isReady()) k = "output_full";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            default -> null;
        };
    }
}
