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
public class EuvScannerBlockEntity extends ScannerBlockEntity implements com.taku3.semiconductorlab.machine.Multiblock.Host {
    public static final int MIRRORS = 10;
    public static final double DOSE_J = 0.2, MAX_PA = 1;
    /** 鏡1枚の反射率（多層膜 40対、⚠️ 組み立てで対の数を引き継がないので 40対とみなす） */
    public static final double MIRROR_R = MultilayerMirrorItem.reflectance(40);
    public static final int DATA_EUV_MW = DATA_COUNT + EXTRA_DATA, DATA_FORMED = DATA_EUV_MW + 1, EUV_EXTRA = EXTRA_DATA + 2;
    /** DATA_FORMED の値：形ができていない・形はできたがクラス100 の部屋の外・組み上がった */
    public static final int NOT_FORMED = 0, NEED_ROOM = 1, FORMED = 2;

    private static com.taku3.semiconductorlab.machine.MultiblockPattern shape;
    /**
     * マルチブロックの形（O-034 Q33-1 案A、5×3×3）。下の段から、行は手前（制御ブロックの正面）から奥。
     * 光の道：奥の上の角の光源 S → 照明の鏡 I → 上の段の真ん中のマスクの台 M（反射型マスクで下へはね返す）→ 投影の鏡 P → 下の段のウェハーの台 W。
     * 光源の真下（奥の左の角）に真空容器 V（外の2面からポンプをつなぐ。光源もこの真空を使う）。H は真空の囲い（電気・物・ガスの口も置ける）。
     * 光源は角にあるので、外の3面に CO2 レーザーを並べられる
     */
    public static com.taku3.semiconductorlab.machine.MultiblockPattern shape() {
        if (shape == null) shape = com.taku3.semiconductorlab.machine.MultiblockPattern.builder()
                .layer("HHCHH", "HHWHH", "HHHHH")
                .layer("HHHHH", "HHPHH", "VIHHH")
                .layer("HHHHH", "HHMHH", "SHHHH")
                .where('H', st -> st.is(Semiconductor.EUV_CASING) || Semiconductor.isPort(st), "euv_casing")
                .where('S', com.taku3.semiconductorlab.content.Optics.EUV_SOURCE.block, "euv_source")
                .where('I', Semiconductor.EUV_ILLUMINATOR, "euv_illuminator")
                .where('M', Semiconductor.EUV_RETICLE_STAGE, "euv_reticle_stage")
                .where('P', Semiconductor.EUV_PROJECTION_BOX, "euv_projection_box")
                .where('W', Semiconductor.EUV_WAFER_STAGE, "euv_wafer_stage")
                .where('V', com.taku3.semiconductorlab.content.Vacuum.VACUUM_CHAMBER.block, "vacuum_chamber")
                .controller('C').build();
        return shape;
    }

    private final com.taku3.semiconductorlab.machine.Multiblock mb = new com.taku3.semiconductorlab.machine.Multiblock(EuvScannerBlockEntity::shape);
    @Override public com.taku3.semiconductorlab.machine.Multiblock multiblock() { return mb; }

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
            .right("hint", 72, 3, MachineLayout.keys("gui.semiconductor-lab.", "euv_scanner.not_formed", "euv_scanner.need_room", "stepper.need_wafer", "euv_scanner.need_mask", "euv_scanner.need_optics", "euv_scanner.need_source",
                    "euv_scanner.need_vacuum", "euv_scanner.high_na_rough", "stepper.need_mask_stage", "stepper.defocus", "stepper.blurred", "stepper.cannot", "stepper.output_full", "stepper.press",
                    "stepper.need_power", "stepper.running"))
            .text("dof", 8, 62, 44, 2, "gui.semiconductor-lab.stepper.dof").samples("0.12")
            .text("k1", 8, 100, 88, 1, "gui.semiconductor-lab.stepper.k1").samples("0.60", "0.30", "0.30")
            .build();

    public EuvScannerBlockEntity(BlockPos pos, BlockState state) { super(Semiconductor.EUV_SCANNER.type, pos, state); }

    /** E の反射型 EUV マスク（ID の名前で見る：E の登録より先でも壊れない） */
    public static boolean isEuvMask(ItemStack s) { return s.has(ModDataComponents.MASK) && "euv_mask".equals(BuiltInRegistries.ITEM.getKey(s.getItem()).getPath()); }

    public static boolean isEuvOptics(ItemStack s) { return ProjectionLensItem.toolOf(s) == ExposureTool.EUV; }

    /** v1.0 #6 High-NA の投影光学（NA 0.55） */
    public static boolean isHighNa(ItemStack s) { return s.is(com.taku3.semiconductorlab.content.Optics.EUV_HIGH_NA_OPTICS); }

    /** High-NA は鏡筒の心出しのずれが合計 {@value #HIGH_NA_MAX_DECENTER}（0.1µm 単位）以下でないと使えない（⚠️ ゲームの値） */
    public static final int HIGH_NA_MAX_DECENTER = 16;

    public static boolean opticsUsable(ItemStack s) {
        if (!isEuvOptics(s)) return false;
        if (!isHighNa(s)) return true;
        var d = s.get(com.taku3.semiconductorlab.content.Precision.LENS_DECENTER);
        return d != null && d.stream().mapToInt(Integer::intValue).sum() <= HIGH_NA_MAX_DECENTER;
    }

    /** 鏡10枚＋マスクの反射で、光源からウェハーに届く割合 */
    public static double throughput() { return Math.pow(MIRROR_R, MIRRORS + 1); }

    /** 1チップ（1回の送り）にかかる tick：DOSE_J ÷ 届く光の W（短くてもステッパーと同じ） */
    public static int stepTicks(double deliveredW) { return deliveredW <= 0 ? Integer.MAX_VALUE : Math.max(STEP_TICKS, (int) Math.ceil(DOSE_J / deliveredW * 20)); }

    public Direction facing() {
        BlockState s = getBlockState();
        return s.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING) ? s.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    /** 形の中の文字 c の場所（最初の1つ） */
    private BlockPos cellPos(char c) {
        var p = shape();
        for (int y = 0; y < p.height(); y++) for (int z = 0; z < p.depth(); z++) {
            int x = p.layers().get(y)[z].indexOf(c);
            if (x >= 0) return p.worldPos(worldPosition, facing(), x, y, z);
        }
        return worldPosition;
    }

    /** 形を調べ直す（20 tick に1回と、テストから） */
    public com.taku3.semiconductorlab.machine.MultiblockPattern.Result recheck() { return mb.recheck(level, worldPosition, facing()); }

    /** 組み上がり：形ができていて、クラス100 の部屋の中（Q33-1） */
    public int formedState() {
        if (level == null || !mb.formed()) return NOT_FORMED;
        return CleanRoom.classAt(level, roomPos()) == CleanRoom.Grade.CLASS_100 ? FORMED : NEED_ROOM;
    }
    public boolean formed() { return formedState() == FORMED; }

    /** 上は囲いなので、部屋の等級は正面（制御ブロックの前の空気）で調べる */
    @Override protected BlockPos roomPos() { return worldPosition.relative(facing()); }

    /** 光源：形の角の S の場所の EUV 光源 */
    public EuvSourceBlockEntity source() {
        return level != null && level.getBlockEntity(cellPos('S')) instanceof EuvSourceBlockEntity e ? e : null;
    }

    /** 光の通り道の真空：形の V の場所の真空容器 */
    public boolean vacuumOk() {
        return level != null && level.getBlockEntity(cellPos('V')) instanceof VacuumChamberBlockEntity c && c.getPressure() <= MAX_PA;
    }

    @Override protected void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        mb.tick(level, pos, facing());
        super.serverTick(level, pos, state);
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out) { super.saveAdditional(out); mb.save(out); }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in) { super.loadAdditional(in); mb.load(in); }

    /** ウェハーに届く EUV（W） */
    public double deliveredW() {
        EuvSourceBlockEntity s = source();
        return s == null || !s.ready() ? 0 : s.euvW() * throughput();
    }

    @Override public ExposureTool tool() { return ExposureTool.EUV; }
    @Override protected boolean maskOk(ItemStack m) { return isEuvMask(m); }
    @Override protected boolean lightReady(ItemStack lens) { return formed() && opticsUsable(lens) && vacuumOk() && deliveredW() > 0; }

    /** High-NA はアナモルフィックで1ショットが半分＝送りが2倍 */
    @Override public int steps() { return isHighNa(container.getItem(SLOT_LENS)) ? 2 * STEPS : STEPS; }
    @Override protected int workTicks() { return stepTicks(deliveredW()); }

    @Override protected int extraDataCount() { return EUV_EXTRA; }
    @Override protected int extraData(int i) { return i == DATA_EUV_MW ? (int) Math.round(deliveredW() * 1000) : i == DATA_FORMED ? formedState() : super.extraData(i); }
}
