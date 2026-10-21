package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.item.MultilayerMirrorItem;
import com.taku3.semiconductorlab.item.ProjectionLensItem;
import com.taku3.semiconductorlab.litho.ExposureTool;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * EUV 露光装置 [9-40]（IMP2 S-009、2019 量産 ⚠️）。スキャナー [9-39] を広げた形：13.5nm の EUV はガラスに吸われるので、
 * 投影は全部が多層膜の鏡（NA 0.33、euv_projection_optics）、マスクも反射型（E の euv_mask）。光は隣の EUV 光源 [7-18]、光の通り道は真空（隣の真空容器 {@value #MAX_PA} Pa 以下）。
 * 鏡を {@value #MIRRORS} 枚通り、マスクでもう1回反射するので、ウェハーに届く光は光源の数％。1チップ焼くのに {@value #DOSE_J} J（⚠️ ゲームの値）を当てるので、
 * 届く光が少ないほど時間がかかる（光源の CO2 レーザーを増やすと速くなる）。
 */
public class EuvScannerBlockEntity extends ScannerBlockEntity {
    public static final int MIRRORS = 10;
    public static final double DOSE_J = 0.2, MAX_PA = 1;
    /** 鏡1枚の反射率（多層膜 40対、⚠️ 組み立てで対の数を引き継がないので 40対とみなす） */
    public static final double MIRROR_R = MultilayerMirrorItem.reflectance(40);
    public static final int DATA_EUV_MW = DATA_COUNT + EXTRA_DATA, EUV_EXTRA = EXTRA_DATA + 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EUV_EXTRA).height(206)
            .in(SLOT_WAFER, 8, 17, s -> s.is(Semiconductor.PROCESS_WAFER), 1)
            .in(SLOT_MASK, 8, 39, EuvScannerBlockEntity::isEuvMask, 1)
            .in(SLOT_LENS, 30, 17, EuvScannerBlockEntity::isEuvOptics, 1)
            .in(SLOT_STAGE, 30, 39, LatheBlockEntity::isMasterScrew, 1)
            .out(SLOT_OUT, 54, 28)
            .in(SLOT_MASK_STAGE, 78, 61, LatheBlockEntity::isMasterScrew, 1)
            .in(SLOT_APERTURE, 54, 61, StepperBlockEntity::isAperture, 1)
            .area("step_button", 8, 84, 66, 14)
            .right("power", 17, 1, "gui.semiconductor-lab.euv_scanner.power").samples("1234")
            .right("steps", 28, 1, "gui.semiconductor-lab.stepper.steps").samples("4", "4")
            .right("cd", 39, 1, "gui.semiconductor-lab.stepper.cd").samples("0.85")
            .right("overlay", 50, 1, "gui.semiconductor-lab.mask_aligner.overlay").samples("20.0")
            .right("room", 61, 1, "gui.semiconductor-lab.mask_aligner.room_none", "gui.semiconductor-lab.mask_aligner.room_1000", "gui.semiconductor-lab.mask_aligner.room_100")
            .right("hint", 72, 3, MachineLayout.keys("gui.semiconductor-lab.", "stepper.need_wafer", "euv_scanner.need_mask", "euv_scanner.need_optics", "euv_scanner.need_source",
                    "euv_scanner.need_vacuum", "stepper.need_mask_stage", "stepper.defocus", "stepper.blurred", "stepper.cannot", "stepper.output_full", "stepper.press",
                    "stepper.need_power", "stepper.running"))
            .text("dof", 8, 62, 44, 2, "gui.semiconductor-lab.stepper.dof").samples("0.12")
            .text("k1", 8, 100, 88, 1, "gui.semiconductor-lab.stepper.k1").samples("0.60", "0.30", "0.30")
            .build();

    public EuvScannerBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.EUV_SCANNER.type, pos, state); }

    /** E の反射型 EUV マスク（ID の名前で見る：E の登録より先でも壊れない） */
    public static boolean isEuvMask(ItemStack s) { return s.has(ModDataComponents.MASK) && "euv_mask".equals(BuiltInRegistries.ITEM.getKey(s.getItem()).getPath()); }

    public static boolean isEuvOptics(ItemStack s) { return ProjectionLensItem.toolOf(s) == ExposureTool.EUV; }

    /** 鏡10枚＋マスクの反射で、光源からウェハーに届く割合 */
    public static double throughput() { return Math.pow(MIRROR_R, MIRRORS + 1); }

    /** 1チップ（1回の送り）にかかる tick：DOSE_J ÷ 届く光の W（短くてもステッパーと同じ） */
    public static int stepTicks(double deliveredW) { return deliveredW <= 0 ? Integer.MAX_VALUE : Math.max(STEP_TICKS, (int) Math.ceil(DOSE_J / deliveredW * 20)); }

    public EuvSourceBlockEntity source() {
        if (level == null) return null;
        for (Direction d : Direction.values()) if (level.getBlockEntity(worldPosition.relative(d)) instanceof EuvSourceBlockEntity e) return e;
        return null;
    }

    public boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    /** ウェハーに届く EUV（W） */
    public double deliveredW() {
        EuvSourceBlockEntity s = source();
        return s == null || !s.ready() ? 0 : s.euvW() * throughput();
    }

    @Override public ExposureTool tool() { return ExposureTool.EUV; }
    @Override protected boolean maskOk(ItemStack m) { return isEuvMask(m); }
    @Override protected boolean lightReady(ItemStack lens) { return isEuvOptics(lens) && vacuumOk() && deliveredW() > 0; }
    @Override protected int workTicks() { return stepTicks(deliveredW()); }

    @Override protected int extraDataCount() { return EUV_EXTRA; }
    @Override protected int extraData(int i) { return i == DATA_EUV_MW ? (int) Math.round(deliveredW() * 1000) : super.extraData(i); }
}
