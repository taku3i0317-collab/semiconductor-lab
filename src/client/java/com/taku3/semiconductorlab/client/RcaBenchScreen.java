package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.RcaBenchBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** RCA 洗浄台 [9-14] の画面。左＝ウェハー、2列目＝アンモニア水・塩酸・過酸化水素、3列目＝脱イオン水、出口＝洗ったウェハー、右＝電力・状態 */
public class RcaBenchScreen extends MachineScreen {
    private static final int BAR_X = 46, BAR_Y = 41, BAR_W = 26, BAR_H = 4;
    private static final String K = "gui.semiconductor-lab.rca_bench.";

    public RcaBenchScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF90D0F8);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は RcaBenchBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack w = menu.machineItem(RcaBenchBlockEntity.SLOT_WAFER);
                String k;
                if (ChipOps.isProcessWafer(w) && !ChipOps.canClean(w)) k = "resist_on".equals(ChipOps.clean(w, 0).reason()) ? "resist_on" : "no_dirt";
                else if (w.isEmpty() || menu.machineItem(RcaBenchBlockEntity.SLOT_AMMONIA).isEmpty() || menu.machineItem(RcaBenchBlockEntity.SLOT_HCL).isEmpty()
                        || menu.machineItem(RcaBenchBlockEntity.SLOT_PEROXIDE).getCount() < RcaBenchBlockEntity.PEROXIDE_PER_RUN || menu.machineItem(RcaBenchBlockEntity.SLOT_WATER).isEmpty()) k = "need_input";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = "running";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + "recipe");
            default -> null;
        };
    }
}
