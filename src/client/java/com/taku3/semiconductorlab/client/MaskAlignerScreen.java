package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.block.MaskAlignerBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** マスクアライナー [9-20] の画面（IMP2 O-011）。左上＝ウェハー、左下＝フォトマスク、中＝できた板、下のボタン＝すき間の切りかえ。右に電力・すき間・ぼけ・マスクの残り・状態 */
public class MaskAlignerScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.mask_aligner.";

    public MaskAlignerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable(K + "switch_gap"), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MaskAlignerBlockEntity.BTN_GAP);
        }).bounds(leftPos + 8, topPos + 60, 66, 14).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, 28, 46, 22, 6, menu.getProgress(), menu.getTotal(), 0xFFB8A0FF);
    }

    @Override
    protected Component text(String id) {
        int gap = menu.get(MaskAlignerBlockEntity.DATA_GAP);
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "gap" -> gap == 0 ? Component.translatable(K + "gap_contact") : Component.translatable(K + "gap", gap);
            case "blur" -> Component.translatable(K + "blur", String.format("%.1f", MaskAlignerBlockEntity.blurUm(gap)));
            case "mask" -> Component.translatable(K + "mask_left", menu.get(MaskAlignerBlockEntity.DATA_MASK_LEFT));
            case "room" -> Component.translatable(K + switch (menu.get(MaskAlignerBlockEntity.DATA_ROOM)) { case 2 -> "room_100"; case 1 -> "room_1000"; default -> "room_none"; });
            case "overlay" -> Component.translatable(K + "overlay", String.format("%.1f", menu.get(MaskAlignerBlockEntity.DATA_OVERLAY) / 1000.0));
            case "hint" -> {
                ItemStack w = menu.getSlot(MaskAlignerBlockEntity.SLOT_WAFER).getItem(), m = menu.getSlot(MaskAlignerBlockEntity.SLOT_MASK).getItem();
                String hint;
                if (!w.is(com.taku3.semiconductorlab.content.Semiconductor.PROCESS_WAFER)) hint = "need_wafer";
                else if (!MaskAlignerBlockEntity.isMask(m)) hint = "need_mask";
                else if (!MaskAlignerBlockEntity.sharpEnough(gap)) hint = "too_blurry";
                else if (gap == 0 && MaskAlignerBlockEntity.usesLeft(m) < w.getCount()) hint = "mask_worn";
                else if (!ChipOps.canExpose(w, m.get(ModDataComponents.MASK), MaskAlignerBlockEntity.TOOL)) hint = "cannot";
                else if (!menu.getSlot(MaskAlignerBlockEntity.SLOT_OUT).getItem().isEmpty()) hint = "output_full";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
