package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.ReductionCameraBlockEntity;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 縮小カメラ [7-11] の画面（IMP2-03）。左上＝原図、左中＝感光乾板、左下＝現像液・定着液、右＝フォトマスク、中央バー＝露光と現像の進み */
public class ReductionCameraScreen extends MachineScreen {
    private static final String K = "gui.semiconductor-lab.reduction_camera.";

    public ReductionCameraScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        bar(g, 30, 38, 40, 6, menu.getProgress(), menu.getTotal(), 0xFFC8D0FF);
    }

    /** 置き場所ごとに今出す文（位置・幅・行数は ReductionCameraBlockEntity.LAYOUT） */
    @Override
    protected Component text(String id) {
        return switch (id) {
            case "power" -> Component.translatable(K + "power", String.format("%.1f kW", menu.getKw()));
            case "hint" -> {
                String hint;
                if (!ReductionCameraBlockEntity.isOriginal(menu.getSlot(ReductionCameraBlockEntity.SLOT_ORIGINAL).getItem())) hint = "need_original";
                else if (!ReductionCameraBlockEntity.isPlate(menu.getSlot(ReductionCameraBlockEntity.SLOT_PLATE).getItem())) hint = "need_plate";
                else if (!ReductionCameraBlockEntity.chemicalsMatch(menu.getSlot(ReductionCameraBlockEntity.SLOT_PLATE).getItem(),
                        menu.getSlot(ReductionCameraBlockEntity.SLOT_DEVELOPER).getItem(), menu.getSlot(ReductionCameraBlockEntity.SLOT_FIXER).getItem())) hint = "need_chemicals";
                else if (!ReductionCameraBlockEntity.fineEnough(menu.getSlot(ReductionCameraBlockEntity.SLOT_PLATE).getItem(),
                        menu.getSlot(ReductionCameraBlockEntity.SLOT_ORIGINAL).getItem())) hint = "too_fine";
                else if (!menu.getSlot(ReductionCameraBlockEntity.SLOT_OUT).getItem().isEmpty()) hint = "output_full";
                else if (menu.getKw() <= 0) hint = "need_power";
                else hint = "running";
                yield Component.translatable(K + hint);
            }
            default -> null;
        };
    }
}
