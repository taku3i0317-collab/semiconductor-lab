package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.ProjectionLensItem;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.litho.ExposureTool;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 縮小投影露光（ステッパー）[9-39]（IMP2 S-003・S-004、1978 GCA DSW〜）。マスクの像を投影レンズ [7-19] で 1/5 に縮めて、
 * ウェハーの上に1チップずつ「送っては焼く」（ステップ・アンド・リピート）。マスクに触れないので傷まない（密着 [9-20] との違い）。
 * <p><b>段階1（今）</b>：送りは人の手。画面の「1チップ送って焼く」ボタンを {@value #STEPS} 回押すと1枚が焼き上がる（1回 {@value #STEP_TICKS} tick）。
 * 1960年代の「ステップ・アンド・リピートの写真機」（マスクの原版を何度も並べて焼く機械）を手で送る近似。⚠️
 * <b>段階2</b>：[10-15] 露光装置の制御ができたら自動送りを足す（速さ）。</p>
 * 焼ける細かさ＝ g線ステッパー（A の ExposureTool.STEPPER_G、方眼 16）。投影レンズの CD が届かないと焼けない。
 * 重ね合わせはマスクアライナーと同じ式（台のねじ・顕微鏡・床の揺れ・温度）、汚れはクリーンルームの等級 [12-14]。
 */
public class StepperBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_500;
    public static final int STEP_TICKS = 20 * 2, STEPS = 4;
    public static final ExposureTool TOOL = ExposureTool.STEPPER_G;
    /** O-017：i線フィルターを入れると i線ステッパー（方眼が細かくなる） */
    public static final ExposureTool TOOL_I = ExposureTool.STEPPER_I;
    /** [7-20]：KrF エキシマレーザーを入れると KrF（248nm、方眼がさらに細かい）。1チップごとにレーザーのガスを1使う */
    public static final ExposureTool TOOL_KRF = ExposureTool.KRF;
    public static final int SLOT_WAFER = 0, SLOT_MASK = 1, SLOT_LENS = 2, SLOT_STAGE = 3, SLOT_OUT = 4, SLOT_FILTER = 5, SLOT_COUNT = 6;
    public static final int DATA_STEPS = DATA_COUNT, DATA_CD = DATA_COUNT + 1, DATA_OVERLAY = DATA_COUNT + 2, DATA_ROOM = DATA_COUNT + 3, EXTRA_DATA = 4;
    public static final int BTN_STEP = 0;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA).height(206)
            .in(SLOT_WAFER, 8, 17, s -> s.is(Semiconductor.PROCESS_WAFER), 1)
            .in(SLOT_MASK, 8, 39, MaskAlignerBlockEntity::isMask, 1)
            .in(SLOT_LENS, 30, 17, s -> s.is(Optics.PROJECTION_LENS), 1)
            .in(SLOT_STAGE, 30, 39, LatheBlockEntity::isMasterScrew, 1)
            .out(SLOT_OUT, 54, 28)
            .in(SLOT_FILTER, 78, 17, s -> s.is(Optics.I_LINE_FILTER) || s.is(Optics.KRF_LASER), 1)
            .area("step_button", 8, 60, 66, 14)
            .right("power", 17, 1, "gui.semiconductor-lab.stepper.power").samples("1.5 kW")
            .right("steps", 28, 1, "gui.semiconductor-lab.stepper.steps").samples("4", "4")
            .right("cd", 39, 1, "gui.semiconductor-lab.stepper.cd").samples("0.85")
            .right("overlay", 50, 1, "gui.semiconductor-lab.mask_aligner.overlay").samples("20.0")
            .right("room", 61, 1, "gui.semiconductor-lab.mask_aligner.room_none", "gui.semiconductor-lab.mask_aligner.room_1000", "gui.semiconductor-lab.mask_aligner.room_100")
            .right("hint", 72, 3, MachineLayout.keys("gui.semiconductor-lab.stepper.", "need_wafer", "need_mask", "need_lens", "lens_wrong", "blurred", "cannot",
                    "output_full", "press", "need_power", "running"))
            .build();

    /** 焼き終わったチップの数と、今焼いている途中か */
    private int stepsDone;
    private boolean stepping;

    public StepperBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.STEPPER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int stepsDone() { return stepsDone; }

    /** レンズが g線ステッパーの細かさに届くか */
    public static boolean lensOk(ItemStack lens) { return ProjectionLensItem.toolOf(lens) != null && ProjectionLensItem.meetsTool(lens); }

    /** 光源：i線フィルターがあれば i線、なければ g線 */
    public static ExposureTool toolFor(ItemStack filter) { return filter.is(Optics.KRF_LASER) ? TOOL_KRF : filter.is(Optics.I_LINE_FILTER) ? TOOL_I : TOOL; }

    /** レンズが光の波長に合っているか（g線のガラスのレンズは i線を吸う） */
    public static boolean lensMatches(ItemStack lens, ItemStack filter) { return ProjectionLensItem.toolOf(lens) == toolFor(filter); }

    public ExposureTool tool() { return toolFor(container.getItem(SLOT_FILTER)); }

    /** 材料がそろっていて、1チップ焼ける状態か（送りの前の確かめ） */
    public boolean canStep() {
        ItemStack w = container.getItem(SLOT_WAFER), m = container.getItem(SLOT_MASK);
        ItemStack lens = container.getItem(SLOT_LENS);
        return MaskAlignerBlockEntity.isMask(m) && lensOk(lens) && lensMatches(lens, container.getItem(SLOT_FILTER)) && container.getItem(SLOT_OUT).isEmpty()
                && ChipOps.canExpose(w, m.get(ModDataComponents.MASK), tool());
    }

    /** 画面のボタン：1チップ送って焼く（段階1＝人の手で送る） */
    @Override public boolean onButton(Player player, int id) {
        if (id != BTN_STEP || stepping || !canStep()) return false;
        stepping = true;
        setChanged();
        return true;
    }

    public int overlayNm() {
        return level == null ? 0 : MaskAlignerBlockEntity.overlayNm(container.getItem(SLOT_STAGE), Vibration.nm(level, worldPosition, null),
                AirConditionerBlockEntity.thermalNm(AirConditionerBlockEntity.swingC(level, worldPosition)));
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return switch (i) {
            case DATA_STEPS -> stepsDone;
            case DATA_CD -> ProjectionLensItem.toolOf(container.getItem(SLOT_LENS)) != null ? ProjectionLensItem.cdNm(container.getItem(SLOT_LENS)) : 0;
            case DATA_OVERLAY -> overlayNm();
            case DATA_ROOM -> level == null ? 0 : CleanRoom.classAt(level, worldPosition).ordinal();
            default -> 0;
        };
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return STEP_TICKS; }
    @Override protected boolean canWork() { return stepping && canStep(); }

    @Override protected void finishWork() {
        stepping = false;
        ItemStack light = container.getItem(SLOT_FILTER);
        if (light.is(Optics.KRF_LASER)) {   // [7-20] 1チップでガスを1使う。使い切ると空になる
            light.setDamageValue(light.getDamageValue() + 1);
            if (light.getDamageValue() >= light.getMaxDamage()) container.setItem(SLOT_FILTER, ItemStack.EMPTY);
        }
        if (++stepsDone < STEPS) { setChanged(); return; }
        stepsDone = 0;
        ItemStack w = container.getItem(SLOT_WAFER), m = container.getItem(SLOT_MASK);
        ChipOps.Result r = ChipOps.expose(w, m.get(ModDataComponents.MASK), tool(), overlayNm());
        if (!r.ok()) return;
        container.setItem(SLOT_OUT, level == null ? r.out() : ChipOps.addDust(r.out(), CleanRoom.classAt(level, worldPosition).dust));
        container.setItem(SLOT_WAFER, ItemStack.EMPTY);   // マスクは触れないので傷まない
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Steps", stepsDone); out.putBoolean("Stepping", stepping); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); stepsDone = in.getIntOr("Steps", 0); stepping = in.getBooleanOr("Stepping", false); }
}
