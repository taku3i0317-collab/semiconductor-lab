package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.block.LatheBlockEntity;
import com.taku3.semiconductorlab.block.ScannerBlockEntity;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** スキャナー [9-39]（IMP2 O-021）の画面。ステッパーの画面に、マスクの台のねじ（右下）がないときのヒントを足しただけ */
public class ScannerScreen extends StepperScreen {
    public ScannerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        if (id.equals("hint") && !LatheBlockEntity.isMasterScrew(menu.getSlot(ScannerBlockEntity.SLOT_MASK_STAGE).getItem()))
            return Component.translatable("gui.semiconductor-lab.stepper.need_mask_stage");
        return super.text(id);
    }
}
