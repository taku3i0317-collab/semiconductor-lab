package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.ElectrolyzerBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** 電解槽の画面。左＝水バケツ/硫酸、中央バー＝分解の進み具合、出口＝水素/酸素、右＝電力・蓄積・状態 */
public class ElectrolyzerScreen extends MachineScreen {
    private static final int BAR_X = 28, BAR_Y = 38, BAR_W = 24, BAR_H = 6;
    private static final int TEXT_X = 100, TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.electrolyzer.";

    public ElectrolyzerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.getProgress() * BAR_W / menu.getTotal();
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF4FA3FF); // 青＝電気
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ElectrolyzerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        boolean brine = menu.get(ElectrolyzerBlockEntity.DATA_MODE) == ElectrolyzerBlockEntity.MODE_BRINE;
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "stored" -> Component.translatable(K + "stored", menu.getStoredKj() + " kJ");
            case "hint" -> {
                String hint;
                var second = menu.getSlot(ElectrolyzerBlockEntity.SLOT_ACID).getItem();
                if (!menu.getSlot(ElectrolyzerBlockEntity.SLOT_WATER).getItem().is(Items.WATER_BUCKET)) hint = "need_water";
                else if (!ElectrolyzerBlockEntity.isSecondInput(second)) hint = "need_acid";
                else if (ElectrolyzerBlockEntity.isSalt(second) && second.getCount() < ElectrolyzerBlockEntity.SALT_PER_RUN) hint = "need_salt";
                else if (!menu.isReady()) hint = "output_full";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            case "mode" -> Component.translatable(K + (brine ? "mode_brine" : "mode_water"));
            case "anode" -> Component.literal(brine ? "Cl2" : "O2");   // 陽極は酸素か塩素
            default -> null;
        };
    }
}
