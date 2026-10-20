package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.machine.MachineMenu;
import com.taku3.semiconductorlab.block.OxyhydrogenBurnerBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** 酸水素バーナーの画面。上＝石英、左下＝水素・酸素ボンベ、中央バー＝溶ける進み、右＝溶融石英・空ボンベ。つないだガスの残り（mol）を表示 */
public class OxyhydrogenBurnerScreen extends MachineScreen {
    private static final int BAR_X = 80, BAR_Y = 41, BAR_W = 30, BAR_H = 6;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final String K = "gui.semiconductor-lab.oxyhydrogen_burner.";

    public OxyhydrogenBurnerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3A3A40);
        int w = menu.get(OxyhydrogenBurnerBlockEntity.DATA_PROGRESS) * BAR_W / Math.max(1, menu.get(OxyhydrogenBurnerBlockEntity.DATA_TOTAL));
        if (w > 0) g.fill(x, y, x + w, y + BAR_H, 0xFF9AD8FF);   // 酸水素炎はほとんど見えない淡い青
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は OxyhydrogenBurnerBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "h2" -> Component.literal("H2 " + menu.get(OxyhydrogenBurnerBlockEntity.DATA_H2));
            case "o2" -> Component.literal("O2 " + menu.get(OxyhydrogenBurnerBlockEntity.DATA_O2));
            case "hint" -> {
                ItemStack in = menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_IN).getItem();
                String hint;
                if (in.isEmpty()) hint = "need_input";
                else if (menu.get(OxyhydrogenBurnerBlockEntity.DATA_PROGRESS) > 0) hint = "melting";
                else if (menu.get(OxyhydrogenBurnerBlockEntity.DATA_H2) < OxyhydrogenBurnerBlockEntity.H2_PER && menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_H2).getItem().isEmpty()) hint = "need_h2";
                else if (menu.get(OxyhydrogenBurnerBlockEntity.DATA_O2) < OxyhydrogenBurnerBlockEntity.O2_PER && menu.getSlot(OxyhydrogenBurnerBlockEntity.SLOT_O2).getItem().isEmpty()) hint = "need_o2";
                else hint = "out_full";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
