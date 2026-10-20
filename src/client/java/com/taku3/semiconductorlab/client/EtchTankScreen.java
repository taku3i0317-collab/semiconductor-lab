package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.EtchTankBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** エッチング槽 [9-06] の画面。左＝酸化膜つきの板・フッ酸、中央バー＝膜が溶ける進み、出口＝シリコンの板、右＝状態 */
public class EtchTankScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.etch_tank.";

    public EtchTankScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.get(EtchTankBlockEntity.DATA_PROGRESS), menu.get(EtchTankBlockEntity.DATA_TOTAL), 0xFF7FB8D8);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は EtchTankBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "hint" -> {
                ItemStack w = menu.machineItem(EtchTankBlockEntity.SLOT_WAFER), hf = menu.machineItem(EtchTankBlockEntity.SLOT_HF);
                String k = EtchTankBlockEntity.batch(w) == 0 || hf.isEmpty() ? "need_input"
                        : menu.get(EtchTankBlockEntity.DATA_PROGRESS) > 0 ? "running" : "out_full";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + (menu.machineItem(EtchTankBlockEntity.SLOT_HF).is(com.taku3.semiconductorlab.content.Chemistry.PHOSPHORIC_ACID) ? "recipe_metal" : menu.machineItem(EtchTankBlockEntity.SLOT_HF).is(com.taku3.semiconductorlab.content.Chemistry.SILICON_ETCH) ? "recipe_silicon" : "recipe"));
            default -> null;
        };
    }
}
