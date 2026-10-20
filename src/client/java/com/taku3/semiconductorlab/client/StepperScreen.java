package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.block.MaskAlignerBlockEntity;
import com.taku3.semiconductorlab.block.StepperBlockEntity;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** ステッパー [9-39] の画面（IMP2 S-004）。左＝ウェハー・マスク・投影レンズ・台のねじ、中＝焼けた板、下のボタン＝1チップ送って焼く（段階1＝手で送る）。右に電力・チップ数・CD・ずれ・部屋・状態 */
public class StepperScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.stepper.";
    private static final String A = "gui.semiconductor-lab.mask_aligner.";

    public StepperScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable(K + "step"), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, StepperBlockEntity.BTN_STEP);
        }).bounds(leftPos + 8, topPos + 60, 66, 14).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, 52, 48, 22, 6, menu.getProgress(), menu.getTotal(), 0xFFB8A0FF);
    }

    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "steps" -> Component.translatable(K + "steps", menu.get(StepperBlockEntity.DATA_STEPS), StepperBlockEntity.STEPS);
            case "cd" -> Component.translatable(K + "cd", String.format("%.2f", menu.get(StepperBlockEntity.DATA_CD) / 1000.0));
            case "overlay" -> Component.translatable(A + "overlay", String.format("%.1f", menu.get(StepperBlockEntity.DATA_OVERLAY) / 1000.0));
            case "room" -> Component.translatable(A + switch (menu.get(StepperBlockEntity.DATA_ROOM)) { case 2 -> "room_100"; case 1 -> "room_1000"; default -> "room_none"; });
            case "hint" -> {
                ItemStack w = menu.getSlot(StepperBlockEntity.SLOT_WAFER).getItem(), m = menu.getSlot(StepperBlockEntity.SLOT_MASK).getItem(),
                        lens = menu.getSlot(StepperBlockEntity.SLOT_LENS).getItem();
                String hint;
                if (!w.is(com.taku3.semiconductorlab.content.Semiconductor.PROCESS_WAFER)) hint = "need_wafer";
                else if (!MaskAlignerBlockEntity.isMask(m)) hint = "need_mask";
                else if (com.taku3.semiconductorlab.item.ProjectionLensItem.toolOf(lens) == null) hint = "need_lens";
                else if (!StepperBlockEntity.lensMatches(lens, menu.getSlot(StepperBlockEntity.SLOT_FILTER).getItem())) hint = "lens_wrong";
                else if (m.get(ModDataComponents.MASK).size() > com.taku3.semiconductorlab.litho.ExposureTool.gridFor(StepperBlockEntity.cdOf(lens))) hint = "blurred";   // レンズの CD より細かいマスク（S-010）
                else if (!ChipOps.exposeAtCd(w, m.get(ModDataComponents.MASK), StepperBlockEntity.toolFor(menu.getSlot(StepperBlockEntity.SLOT_FILTER).getItem()),
                        StepperBlockEntity.cdOf(lens), 0).ok()) hint = "cannot";
                else if (!menu.getSlot(StepperBlockEntity.SLOT_OUT).getItem().isEmpty()) hint = "output_full";
                else if (menu.getKw() <= 0 && menu.getProgress() == 0) hint = "press";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
