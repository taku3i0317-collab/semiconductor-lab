package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.item.ProjectionLensItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * スキャナー（ステップ・アンド・スキャン）[9-39] の続き（IMP2 O-021、1990年代 ⚠️）。ステッパーと同じく1チップずつ送るが、
 * 1チップの中は細いスリットの光で、マスクの台と板の台を逆向きに同じ比（4:1）で動かしながら焼く。
 * レンズはスリットの細い帯だけを使うので、収差（ずれ）の効きが {@value #SLIT_ABERRATION} 倍になり、同じレンズでも CD が細くなる
 * ＝ チップ全体を一度に写す大きなレンズが要らない。
 * 入口はステッパーと同じに、マスクの台のねじ（送りねじ／ボールねじ）を1つ足す。マスクの台のずれは縮小率で 1/{@value #REDUCTION} になって重ね合わせに入る。
 */
public class ScannerBlockEntity extends StepperBlockEntity {
    /** ⚠️ ゲームの値：スリットの帯だけを使うと、収差の効きが半分 */
    public static final double SLIT_ABERRATION = 0.5;
    public static final int REDUCTION = 4;
    public static final int SLOT_MASK_STAGE = 8, SCANNER_SLOT_COUNT = 9;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA).height(206)
            .in(SLOT_WAFER, 8, 17, s -> s.is(Semiconductor.PROCESS_WAFER), 1)
            .in(SLOT_MASK, 8, 39, MaskAlignerBlockEntity::isMask, 1)
            .in(SLOT_LENS, 30, 17, s -> s.getItem() instanceof ProjectionLensItem, 1)
            .in(SLOT_STAGE, 30, 39, LatheBlockEntity::isMasterScrew, 1)
            .out(SLOT_OUT, 54, 28)
            .in(SLOT_FILTER, 78, 17, s -> s.is(Optics.I_LINE_FILTER) || s.is(Optics.KRF_LASER) || s.is(Optics.ARF_LASER), 1)
            .in(SLOT_WATER, 78, 39, s -> s.is(com.taku3.semiconductorlab.content.Chemistry.ULTRAPURE_WATER))
            .in(SLOT_MASK_STAGE, 78, 61, LatheBlockEntity::isMasterScrew, 1)
            .in(SLOT_APERTURE, 54, 61, StepperBlockEntity::isAperture, 1)
            .area("step_button", 8, 84, 66, 14)
            .right("power", 17, 1, "gui.semiconductor-lab.stepper.power").samples("1.5 kW")
            .right("steps", 28, 1, "gui.semiconductor-lab.stepper.steps").samples("4", "4")
            .right("cd", 39, 1, "gui.semiconductor-lab.stepper.cd").samples("0.85")
            .right("overlay", 50, 1, "gui.semiconductor-lab.mask_aligner.overlay").samples("20.0")
            .right("room", 61, 1, "gui.semiconductor-lab.mask_aligner.room_none", "gui.semiconductor-lab.mask_aligner.room_1000", "gui.semiconductor-lab.mask_aligner.room_100")
            .text("dof", 8, 62, 44, 2, "gui.semiconductor-lab.stepper.dof").samples("0.12")
            .right("hint", 72, 3, MachineLayout.keys("gui.semiconductor-lab.stepper.", "need_wafer", "need_mask", "need_lens", "lens_wrong", "need_water", "defocus",
                    "need_mask_stage", "blurred", "cannot", "output_full", "press", "need_power", "running"))
            .text("k1", 8, 100, 88, 1, "gui.semiconductor-lab.stepper.k1").samples("0.60", "0.30", "0.30")
            .build();

    public ScannerBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.SCANNER.type, pos, state, SCANNER_SLOT_COUNT); }

    @Override public int lensCd() {
        ItemStack lens = container.getItem(SLOT_LENS);
        return lensOk(lens) ? ProjectionLensItem.cdNm(lens, SLIT_ABERRATION, tricks()) : 0;
    }

    @Override protected boolean stagesReady() { return LatheBlockEntity.isMasterScrew(container.getItem(SLOT_MASK_STAGE)); }

    /** 重ね合わせ：板の台（ステッパーと同じ）＋マスクの台のずれ÷縮小率 */
    @Override public int overlayNm() {
        double wafer = super.overlayNm(), mask = MaskAlignerBlockEntity.overlayNm(container.getItem(SLOT_MASK_STAGE), 0) / (double) REDUCTION;
        return (int) Math.round(Math.sqrt(wafer * wafer + mask * mask));
    }
}
