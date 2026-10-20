package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.litho.ExposureTool;
import com.taku3.semiconductorlab.litho.MaskData;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * マスクアライナー [9-20] 密着・近接露光（IMP2 O-011、1955〜57 ベル研・陸軍のラスロップとナル）。
 * レジストを塗った加工中のウェハー（[9-19]）にフォトマスク（[7-11]）を重ね、中の水銀ランプ [7-10] の g線で焼く → {@code ChipOps.expose(w, mask, CONTACT_G)}（A の litho）。
 * 画面のボタンで「すき間」を切りかえる：
 * <ul>
 * <li>密着（0µm）：像はくっきり（ぼけ≒波長）。マスクがウェハーにこすれて、板1枚ごとに耐久が1減る（フォトマスクは耐久10）</li>
 * <li>近接 {@value #GAP_NEAR_UM}µm・{@value #GAP_FAR_UM}µm：マスクは傷まないが、回折で像がぼける（ぼけ ≒ √(λ×すき間)）。ぼけが線幅 CD の半分を超えると焼けない</li>
 * </ul>
 * [8-17] アライメント：台のねじの入口（送りねじ・ボールねじ）と中の顕微鏡で前の層に合わせる。ずれは {@link #overlayNm} で ChipOps.expose に渡す（失敗にはしない、A の ChipState.overlayNm）。
 */
public class MaskAlignerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500;
    public static final int TICKS = 20 * 10;
    public static final int SLOT_WAFER = 0, SLOT_MASK = 1, SLOT_OUT = 2, SLOT_STAGE = 3, SLOT_COUNT = 4;
    public static final int GAP_NEAR_UM = 20, GAP_FAR_UM = 80;
    public static final int[] GAPS_UM = {0, GAP_NEAR_UM, GAP_FAR_UM};
    public static final ExposureTool TOOL = ExposureTool.CONTACT_G;
    public static final int DATA_GAP = DATA_COUNT, DATA_MASK_LEFT = DATA_COUNT + 1, DATA_OVERLAY = DATA_COUNT + 2, EXTRA_DATA = 3;
    public static final int BTN_GAP = 0;

    public static boolean isMask(ItemStack s) { return (s.is(Optics.PHOTOMASK) || s.is(Optics.CHROME_PHOTOMASK)) && s.has(ModDataComponents.MASK); }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA).height(196)
            .in(SLOT_WAFER, 8, 17, s -> s.is(Semiconductor.PROCESS_WAFER))
            .in(SLOT_MASK, 8, 39, MaskAlignerBlockEntity::isMask, 1)
            .out(SLOT_OUT, 54, 28)
            .in(SLOT_STAGE, 30, 17, s -> com.taku3.semiconductorlab.block.LatheBlockEntity.isMasterScrew(s), 1)
            .area("gap_button", 8, 60, 66, 14)
            .right("power", 17, 1, "gui.semiconductor-lab.mask_aligner.power").samples("0.5 kW")
            .right("gap", 28, 1, "gui.semiconductor-lab.mask_aligner.gap_contact", "gui.semiconductor-lab.mask_aligner.gap").samples("80")
            .right("blur", 39, 1, "gui.semiconductor-lab.mask_aligner.blur").samples("5.9")
            .right("mask", 50, 1, "gui.semiconductor-lab.mask_aligner.mask_left").samples("10")
            .right("overlay", 61, 1, "gui.semiconductor-lab.mask_aligner.overlay").samples("20.0")
            .right("hint", 72, 3, MachineLayout.keys("gui.semiconductor-lab.mask_aligner.", "need_wafer", "need_mask", "too_blurry", "mask_worn", "cannot",
                    "output_full", "need_power", "running"))
            .build();

    /** 今のすき間の番号（0＝密着） */
    private int gapIndex;

    public MaskAlignerBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.MASK_ALIGNER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int gapUm() { return GAPS_UM[gapIndex]; }
    public void setGapIndex(int i) { gapIndex = Math.floorMod(i, GAPS_UM.length); setChanged(); }

    /** 像のぼけ（µm）：密着なら波長ほど、近接なら √(λ×すき間)（フレネル回折） */
    public static double blurUm(int gapUm) {
        double lambdaUm = TOOL.wavelengthNm / 1000.0;
        return gapUm <= 0 ? lambdaUm : Math.sqrt(lambdaUm * gapUm);
    }

    /** ぼけが線幅の半分以下なら焼ける */
    public static boolean sharpEnough(int gapUm) { return blurUm(gapUm) <= TOOL.cdNm / 1000.0 / 2; }

    /** 手の台（ねじなし）で合わせたときの送り（µm）。⚠️ ゲームの値 */
    public static final int HAND_FEED_UM = 20;

    /**
     * [8-17] アライメント（IMP2 O-014）：前の層のマークを、中の複式顕微鏡（NA 0.9）で見て台のねじで動かして合わせる。
     * 重ね合わせのずれ（nm）＝ 台の送りの細かさ・顕微鏡の分解能・床の揺れ [8-07] の一番大きいもの。台のねじ：なし＝手 20µm、送りねじ 10µm、ボールねじ 5・2・1µm [8-08]
     */
    public static int overlayNm(ItemStack stage, double vibrationNm) {
        double feed = stage.isEmpty() ? HAND_FEED_UM * 1000.0 : com.taku3.semiconductorlab.item.BallScrewItem.feedUm(stage) * 1000.0;
        return (int) Math.round(Math.max(Math.max(feed, com.taku3.semiconductorlab.item.MicroscopeItem.resolutionNm(0.9)), vibrationNm));
    }

    /** フォトマスクの残りの回数 */
    public static int usesLeft(ItemStack mask) { return mask.isEmpty() ? 0 : mask.getMaxDamage() - mask.getDamageValue(); }

    @Override public boolean onButton(Player player, int id) {
        if (id != BTN_GAP) return false;
        setGapIndex(gapIndex + 1);
        return true;
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return switch (i) {
            case DATA_GAP -> gapUm();
            case DATA_MASK_LEFT -> usesLeft(container.getItem(SLOT_MASK));
            case DATA_OVERLAY -> overlayNm(container.getItem(SLOT_STAGE), level == null ? 0 : Vibration.nm(level, worldPosition, null));
            default -> 0;
        };
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack w = container.getItem(SLOT_WAFER), m = container.getItem(SLOT_MASK);
        return isMask(m) && sharpEnough(gapUm()) && container.getItem(SLOT_OUT).isEmpty() && (gapUm() > 0 || usesLeft(m) >= w.getCount())
                && ChipOps.canExpose(w, m.get(ModDataComponents.MASK), TOOL);
    }

    @Override protected void finishWork() {
        ItemStack w = container.getItem(SLOT_WAFER), m = container.getItem(SLOT_MASK);
        MaskData mask = m.get(ModDataComponents.MASK);
        ChipOps.Result r = ChipOps.expose(w, mask, TOOL, overlayNm(container.getItem(SLOT_STAGE), level == null ? 0 : Vibration.nm(level, worldPosition, null)));
        if (!r.ok()) return;
        container.setItem(SLOT_OUT, r.out());
        container.setItem(SLOT_WAFER, ItemStack.EMPTY);
        if (gapUm() == 0) {   // 密着：マスクがこすれて傷む（板1枚につき1）
            m.setDamageValue(m.getDamageValue() + w.getCount());
            if (m.getDamageValue() >= m.getMaxDamage()) container.setItem(SLOT_MASK, ItemStack.EMPTY);
        }
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Gap", gapIndex); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); gapIndex = Math.floorMod(in.getIntOr("Gap", 0), GAPS_UM.length); }
}
