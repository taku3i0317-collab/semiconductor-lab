package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.MicroscopeItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 電子顕微鏡 [8-11]（IMP2 O-017、1931 ルスカとクノール、1933 光学顕微鏡の分解能を超えた）。
 * タングステン線の電子銃 [1-28] から出た電子を数万V（変圧器 [4-10]＋整流器 [4-11]）で速め、銅のコイルの電磁レンズで像を結ぶ。
 * 電子は空気にぶつかって散るので、隣の真空容器が {@value #MAX_PA}Pa 以下（拡散ポンプ [5-05] の高真空）でないと見えない。
 * 電子の波長 λ＝1.226÷√V nm（{@value #KV}kV で約 0.0055nm、光の 10 万分の1）。ただし磁気レンズの収差で、分解能は {@value #RESOLUTION_NM}nm ほど（⚠️ 1930年代末のめやす）。
 * 入口の見る物（マスク・設計図・加工中のウェハー）の「1マスの細さ」を、光学顕微鏡（複式 NA 0.9）と並べて画面に出す。2kW。
 */
public class ElectronMicroscopeBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final double MAX_PA = 0.01;
    public static final int KV = 50;
    public static final double RESOLUTION_NM = 10;
    public static final int SLOT_SAMPLE = 0, SLOT_COUNT = 1;
    public static final int DATA_ON = DATA_COUNT, EXTRA_DATA = 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA).height(214)
            .in(SLOT_SAMPLE, 8, 17, s -> MicroscopeItem.sizeOf(s) > 0, 1)
            .text("power", 30, 18, 140, 1, "gui.semiconductor-lab.electron_microscope.power").samples("2.0 kW")
            .text("limit", 8, 40, 160, 2, "gui.semiconductor-lab.electron_microscope.limit").samples("0.0055", "10")
            .text("optical", 8, 62, 160, 1, "gui.semiconductor-lab.electron_microscope.optical").samples("306")
            .text("cell", 8, 74, 160, 2, "gui.semiconductor-lab.electron_microscope.cell").samples("148", "148", "13 nm")
            .text("hint", 8, 96, 160, 2, MachineLayout.keys("gui.semiconductor-lab.electron_microscope.", "need_vacuum", "need_power", "need_sample",
                    "em_only", "both", "neither"))
            .build();

    public ElectronMicroscopeBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.ELECTRON_MICROSCOPE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 電子の波長（nm）：加速電圧 kV（非相対論の近似） */
    public static double electronWavelengthNm(double kV) { return 1.226 / Math.sqrt(kV * 1000); }

    /** その方眼の線が電子顕微鏡で見えるか */
    public static boolean canResolve(int size) { return size > 0 && MicroscopeItem.cellNm(size) >= RESOLUTION_NM; }

    private boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    /** 真空と電気がそろって、電子線が出ているか */
    public boolean on() { return getLastWatts() > 0; }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_ON ? (vacuumOk() ? (on() ? 2 : 1) : 0) : 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return 20; }
    @Override protected boolean canWork() { return vacuumOk(); }   // 高真空なら電子銃を点けておく
    @Override protected void finishWork() {}
}
