package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.MercuryLampBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * マイケルソン干渉計 [7-09]（IMP2-01）。半透明鏡で光を2つの道に分け、2枚の鏡で返して重ねると、道の長さの差で明暗の縞ができる。
 * 片方の鏡を送りねじで d 動かすと、往復で 2d 変わるので、縞が N = 2d/λ 本流れる → 光の波長を物差しにして長さが測れる。
 * 光源は点灯中の水銀灯 [7-10] の緑の線 546.1nm（[6-15] と整合する決定。He-Ne レーザー 633nm は解説だけ）。
 * 使い方：点灯中の水銀灯を右クリック → 送りねじを1回転（{@link #SCREW_MM} mm）回したときの縞の数と、そこから求めた長さ。
 * 炎（たき火・火）では色が混ざって縞がすぐ消える、と出る。
 * [8-07]：まわりの揺れ（block/Vibration）で縞がゆれる。水銀灯を除振台の上に置くと 1/20。
 * [8-06]：もう片方の手にゲージブロック・メートル原器を持つと、縞を数えて光の波長でその長さを測る（マイケルソン 1892〜93）。
 * [7-21]：もう片方の手に材料（ULE ガラス・石英・ガラス・鋼・鉄・銅）を持つと、長さ {@value #SAMPLE_MM} mm の棒が部屋の温度のゆれ
 * （空調 [12-12] がなければ ±3℃、あれば ±0.1℃）で伸び縮みして、縞が何本ゆれるかを見る。
 */
public class InterferometerItem extends Item {
    /** 光源の波長（水銀の緑の線） */
    public static final double HG_GREEN_NM = 546.1;
    /** 送りねじ1回転で鏡が動く距離（mm）。⚠️ ゲームの値 */
    public static final double SCREW_MM = 0.5;
    private static final String K = "item.semiconductor-lab.interferometer.";

    public InterferometerItem(Properties properties) { super(properties); }

    /** 鏡を mm 動かしたときに流れる縞の本数 N = 2d/λ */
    public static double fringes(double mm, double nm) { return 2 * mm * 1e6 / nm; }

    /** ゲージブロック1個の本当のずれの幅（nm）。⚠️ ゲームの値（ヨハンソンのブロックで 0.1〜0.3µm ほど） */
    public static final int GAUGE_ERR_NM = 200;

    /** [7-21] 熱膨張を見る棒の長さ（mm） */
    public static final double SAMPLE_MM = 100;

    /** 材料の線膨張係数（×10⁻⁶/K、室温）。熱膨張を見られない物は -1 */
    public static double expansionPpm(net.minecraft.world.item.ItemStack s) {
        if (s.is(com.taku3.semiconductorlab.content.Optics.ULE_GLASS)) return 0.03;      // コーニング ULE：±0.03
        if (s.is(com.taku3.semiconductorlab.content.Optics.FUSED_QUARTZ) || s.is(com.taku3.semiconductorlab.content.Optics.SYNTHETIC_QUARTZ)) return 0.55;
        if (s.is(com.taku3.semiconductorlab.content.Optics.CROWN_GLASS) || s.is(net.minecraft.world.item.Items.GLASS)) return 9;   // ソーダ石灰ガラス
        if (s.is(com.taku3.semiconductorlab.content.Metallurgy.STEEL_INGOT) || s.is(net.minecraft.world.item.Items.IRON_INGOT)) return 12;
        if (s.is(net.minecraft.world.item.Items.COPPER_INGOT)) return 17;
        return -1;
    }

    /** 温度が ±c ℃ ゆれたとき、{@value #SAMPLE_MM} mm の棒の長さのゆれ（nm）ΔL = α·L·ΔT */
    public static double thermalNm(double ppm, double c) { return ppm * 1e-6 * SAMPLE_MM * 1e6 * c; }

    /** 長さ（mm）の中に入る波の数 L/λ */
    public static double wavelengths(double mm, double nm) { return mm * 1e6 / nm; }

    /** 縞の本数から長さ（mm）に戻す d = N·λ/2 */
    public static double lengthMm(long n, double nm) { return n * nm / 2 / 1e6; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        var state = level.getBlockState(ctx.getClickedPos());
        Player p = ctx.getPlayer();
        if (MercuryLampBlockEntity.isLit(state)) {
            if (!level.isClientSide() && p != null) {
                long n = Math.round(fringes(SCREW_MM, HG_GREEN_NM));
                p.sendSystemMessage(Component.translatable(K + "lamp"));
                p.sendSystemMessage(Component.translatable(K + "count", String.format(Locale.ROOT, "%.1f", SCREW_MM), n));
                p.sendSystemMessage(Component.translatable(K + "length", n, String.format(Locale.ROOT, "%.1f", HG_GREEN_NM),
                        String.format(Locale.ROOT, "%.4f", lengthMm(n, HG_GREEN_NM))));
                // [8-07] 床の揺れ：縞が半分（±0.5本）以上ゆれると数え間違える
                double v = com.taku3.semiconductorlab.block.Vibration.nm(level, ctx.getClickedPos(), p);
                double j = com.taku3.semiconductorlab.block.Vibration.fringeJitter(v, HG_GREEN_NM);
                p.sendSystemMessage(Component.translatable(K + "vibration", String.format(Locale.ROOT, "%.0f", v), String.format(Locale.ROOT, "%.2f", j),
                        Component.translatable(K + (j < 0.5 ? "steady" : "shaky")),
                        Component.translatable(K + (com.taku3.semiconductorlab.block.Vibration.onTable(level, ctx.getClickedPos()) ? "on_table" : "on_floor"))));
                // [8-06] 光波干渉測長：もう片方の手のゲージブロック・メートル原器を、縞を数えて光の波長で測る（揺れていると数えられない）
                var other = p.getItemInHand(ctx.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                double ppm = expansionPpm(other);
                if (ppm >= 0) {   // [7-21] 熱で伸び縮みする長さを縞で見る
                    double c = com.taku3.semiconductorlab.block.AirConditionerBlockEntity.swingC(level, ctx.getClickedPos());
                    double nm = thermalNm(ppm, c), fr = 2 * nm / HG_GREEN_NM;
                    p.sendSystemMessage(Component.translatable(K + "thermal", other.getHoverName(), String.format(Locale.ROOT, "%.2f", ppm),
                            String.format(Locale.ROOT, "%.0f", SAMPLE_MM), String.format(Locale.ROOT, "%.1f", c), String.format(Locale.ROOT, "%.0f", nm),
                            String.format(Locale.ROOT, "%.2f", fr), Component.translatable(K + (fr < 0.1 ? "thermal_still" : fr < 1 ? "thermal_small" : "thermal_big"))));
                }
                if (other.getItem() instanceof GaugeBlockSetItem || other.getItem() instanceof MetreBarItem) {
                    if (j >= 0.5) p.sendSystemMessage(Component.translatable(K + "measure_shaky"));
                    else if (other.getItem() instanceof MetreBarItem) {
                        long w = Math.round(wavelengths(1000, HG_GREEN_NM));
                        p.sendSystemMessage(Component.translatable(K + "measure_metre", w, String.format(Locale.ROOT, "%.1f", HG_GREEN_NM)));
                    } else {
                        int target = level.getRandom().nextIntBetweenInclusive(GaugeBlockSetItem.MIN_UM, 25_000);
                        int errNm = level.getRandom().nextIntBetweenInclusive(-GAUGE_ERR_NM, GAUGE_ERR_NM);   // そのブロックの本当のずれ
                        long fr = Math.round(fringes((target + errNm / 1000.0) / 1000.0, HG_GREEN_NM));
                        double got = lengthMm(fr, HG_GREEN_NM);
                        p.sendSystemMessage(Component.translatable(K + "measure_gauge", String.format(Locale.ROOT, "%.3f", target / 1000.0), fr,
                                String.format(Locale.ROOT, "%.5f", got), String.format(Locale.ROOT, "%+.2f", (got * 1000 - target))));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (SpectroscopeItem.isFlame(state)) {
            if (!level.isClientSide() && p != null) p.sendSystemMessage(Component.translatable(K + "white"));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "hint"));
        return InteractionResult.SUCCESS;
    }
}
