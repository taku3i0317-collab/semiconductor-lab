package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.block.EuvScannerBlockEntity;
import com.taku3.semiconductorlab.block.LatheBlockEntity;
import com.taku3.semiconductorlab.block.StepperBlockEntity;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.litho.ExposureTool;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** EUV 露光装置 [9-40] の画面（IMP2 S-009）。スキャナーの画面に、ウェハーに届く EUV（mW）と EUV のヒントを足した */
public class EuvScannerScreen extends ScannerScreen {
    private static final String E = "gui.semiconductor-lab.euv_scanner.", S = "gui.semiconductor-lab.stepper.";

    public EuvScannerScreen(MachineMenu menu, Inventory inv, Component title) { super(menu, inv, title); }

    @Override
    protected Component text(String id) {
        if (id.equals("power")) return Component.translatable(E + "power", String.valueOf(menu.get(EuvScannerBlockEntity.DATA_EUV_MW)));
        if (id.equals("steps")) return Component.translatable(S + "steps", menu.get(StepperBlockEntity.DATA_STEPS),
                EuvScannerBlockEntity.isHighNa(menu.getSlot(StepperBlockEntity.SLOT_LENS).getItem()) ? 2 * StepperBlockEntity.STEPS : StepperBlockEntity.STEPS);
        if (!id.equals("hint")) return super.text(id);
        ItemStack w = menu.getSlot(StepperBlockEntity.SLOT_WAFER).getItem(), m = menu.getSlot(StepperBlockEntity.SLOT_MASK).getItem(),
                lens = menu.getSlot(StepperBlockEntity.SLOT_LENS).getItem();
        String h;
        int formed = menu.get(EuvScannerBlockEntity.DATA_FORMED);
        if (formed == EuvScannerBlockEntity.NOT_FORMED) h = E + "not_formed";   // O-034 マルチブロック：しゃがんで右クリックで見取り図
        else if (formed == EuvScannerBlockEntity.NEED_ROOM) h = E + "need_room";
        else if (!w.is(com.taku3.semiconductorlab.content.Semiconductor.PROCESS_WAFER)) h = S + "need_wafer";
        else if (!EuvScannerBlockEntity.isEuvMask(m)) h = E + "need_mask";
        else if (!EuvScannerBlockEntity.isEuvOptics(lens)) h = E + "need_optics";
        else if (!EuvScannerBlockEntity.opticsUsable(lens)) h = E + "high_na_rough";
        else if (menu.get(EuvScannerBlockEntity.DATA_EUV_MW) <= 0) h = E + "need_source";
        else if (!LatheBlockEntity.isMasterScrew(menu.getSlot(EuvScannerBlockEntity.SLOT_MASK_STAGE).getItem())) h = S + "need_mask_stage";
        else {
            var r = ChipOps.exposeAtCd(w, m.get(ModDataComponents.MASK), ExposureTool.EUV, menu.get(StepperBlockEntity.DATA_CD), 0, menu.get(StepperBlockEntity.DATA_DOF));
            if (!r.ok()) h = S + ("too_fine".equals(r.reason()) ? "blurred" : "defocus".equals(r.reason()) ? "defocus" : "cannot");
            else if (!menu.getSlot(StepperBlockEntity.SLOT_OUT).getItem().isEmpty()) h = S + "output_full";
            else if (menu.getKw() <= 0 && menu.getProgress() == 0) h = S + "press";
            else if (menu.getKw() <= 0) h = S + "need_power";
            else h = S + "running";
        }
        return Component.translatable(h);
    }
}
