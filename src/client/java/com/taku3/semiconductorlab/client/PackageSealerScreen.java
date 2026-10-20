package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.PackageSealerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 封止機 [9-34] の画面。左＝線をつないだチップ・金属缶かエポキシ・窒素、中央バー＝進み、出口＝封じた IC、右＝電力・状態 */
public class PackageSealerScreen extends MachineScreen {
    private static final int BAR_X = 30, BAR_Y = 41, BAR_W = 40, BAR_H = 6;
    private static final String K = "gui.semiconductor-lab.package_sealer.";

    public PackageSealerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, BAR_X, BAR_Y, BAR_W, BAR_H, menu.getProgress(), menu.getTotal(), 0xFF707880);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は PackageSealerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        var m = PackageSealerBlockEntity.modeOf(menu.machineItem(PackageSealerBlockEntity.SLOT_SHELL));
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                ItemStack c = menu.machineItem(PackageSealerBlockEntity.SLOT_CHIP);
                String k;
                if (c.isEmpty() || m == PackageSealerBlockEntity.Mode.NONE) k = "need_input";
                else if (m == PackageSealerBlockEntity.Mode.CAN && menu.machineItem(PackageSealerBlockEntity.SLOT_GAS).isEmpty()) k = "need_n2";
                else if (menu.getKw() <= 0) k = "need_power";
                else k = m == PackageSealerBlockEntity.Mode.CAN ? "running_can" : "running_epoxy";
                yield Component.translatable(K + k);
            }
            case "recipe" -> Component.translatable(K + (m == PackageSealerBlockEntity.Mode.CAN ? "recipe_can" : m == PackageSealerBlockEntity.Mode.EPOXY ? "recipe_epoxy" : "recipe"));
            default -> null;
        };
    }
}
