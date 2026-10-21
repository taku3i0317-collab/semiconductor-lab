package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.MercuryLampBlockEntity;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.litho.ExposureTool;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 投影レンズ [7-19] 縮小光学系（IMP2 O-010）。心出し済みの鏡筒 [8-14] に反射防止膜のレンズ [7-22] を足した g線用の縮小レンズ（1/{@value #REDUCTION}）。
 * 鏡筒のレンズのずれ（成分 LENS_DECENTER を引き継ぐ）から波面収差 W（波長の何倍）を出し、解像度 CD ＝ k1·λ/NA（k1 ＝ 0.6 ＋ 2W）。
 * 点灯中の水銀灯 [7-10] を右クリック → テスト露光：CD と、g線ステッパー（A の ExposureTool.STEPPER_G）の CD に届くか。もう片方の手に干渉計 [7-09] があれば W も測る。
 * 露光装置そのもの（[9-39]）は別のノード。この部品を使う機械は {@link #cdNm(ItemStack)} を読めばよい。
 */
public class ProjectionLensItem extends Item {
    /** 縮小率（マスクの像を 1/5 に） */
    public static final int REDUCTION = 5;
    /** 開口数。⚠️ 1978 の GCA DSW は約 0.28、1980年代前半の g線ステッパーで 0.35 前後 */
    public static final double NA = 0.35;
    /** g線（水銀の 436nm） */
    public static final double LAMBDA_NM = ExposureTool.STEPPER_G.wavelengthNm;
    /** レンズのずれ 1µm あたりの波面収差（波長の何倍）。⚠️ ゲームの値 */
    public static final double WAVES_PER_UM = 0.01;
    private static final String K = "item.semiconductor-lab.projection_lens.";

    /** このレンズの露光の方法（光の波長と、届けばよい CD）と開口数。g線レンズ＝STEPPER_G・NA 0.35、i線レンズ＝STEPPER_I・NA 0.7（O-017） */
    public final ExposureTool tool;
    public final double na;
    /** k1 の土台（g線・i線 0.6、KrF は位相シフトマスクなどの解像度を上げる工夫で 0.35⚠️） */
    public final double k1Base;

    public ProjectionLensItem(Properties properties) { this(properties, ExposureTool.STEPPER_G, NA); }
    public ProjectionLensItem(Properties properties, ExposureTool tool, double na) { this(properties, tool, na, 0.6); }
    public ProjectionLensItem(Properties properties, ExposureTool tool, double na, double k1Base) { super(properties); this.tool = tool; this.na = na; this.k1Base = k1Base; }

    /** レンズの露光の方法（ProjectionLensItem でなければ null） */
    public static ExposureTool toolOf(ItemStack s) { return s.getItem() instanceof ProjectionLensItem l ? l.tool : null; }

    /** 波面収差 W（波長の何倍、rms のめやす） */
    public static double wavefrontWaves(ItemStack s) {
        List<Integer> d = s.get(Precision.LENS_DECENTER);
        if (d == null) return 0.5;   // 鏡筒の記録がない＝心出しされていない扱い
        return d.stream().mapToInt(Integer::intValue).sum() / 10.0 * WAVES_PER_UM;
    }

    /** プロセスの係数 k1（収差が大きいと上がる） */
    public static double k1(double w) { return 0.6 + 2 * w; }

    /** 解像度 CD（nm）＝ k1·λ/NA */
    public static int cdNm(ItemStack s) { return cdNm(s, 1.0); }

    /** スキャナー [9-39] 段階2：細いスリットの部分だけを使うと、収差が aberration 倍に減る（レンズ全体を使うステッパーは 1） */
    public static int cdNm(ItemStack s, double aberration) {
        ProjectionLensItem l = s.getItem() instanceof ProjectionLensItem p ? p : null;
        double lambda = l == null ? LAMBDA_NM : l.tool.wavelengthNm, na = l == null ? NA : l.na, base = l == null ? 0.6 : l.k1Base;
        return (int) Math.round((base + 2 * wavefrontWaves(s) * aberration) * lambda / na);
    }

    /** このレンズの露光の方法の CD（g線なら 1µm、i線なら 0.35µm）に届くか */
    public static boolean meetsTool(ItemStack s) { ExposureTool t = toolOf(s); return t != null && cdNm(s) <= t.cdNm; }

    /** g線ステッパーの CD に届くか（g線のレンズ） */
    public static boolean meetsStepperG(ItemStack s) { return toolOf(s) == ExposureTool.STEPPER_G && meetsTool(s); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!MercuryLampBlockEntity.isLit(ctx.getLevel().getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        Player p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        ItemStack s = ctx.getItemInHand();
        if (p.getItemInHand(ctx.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() instanceof InterferometerItem)
            p.sendSystemMessage(Component.translatable(K + "wavefront", String.format(Locale.ROOT, "%.3f", wavefrontWaves(s))));
        p.sendSystemMessage(Component.translatable(K + "test", String.format(Locale.ROOT, "%.2f", k1Base + 2 * wavefrontWaves(s)),
                String.format(Locale.ROOT, "%.0f", tool.wavelengthNm), String.format(Locale.ROOT, "%.2f", na), String.format(Locale.ROOT, "%.2f", cdNm(s) / 1000.0)));
        p.sendSystemMessage(Component.translatable(K + (meetsTool(s) ? "ok" : "blurred"), String.format(Locale.ROOT, "%.2f", tool.cdNm / 1000.0)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable(K + "spec", REDUCTION, String.format(Locale.ROOT, "%.2f", na), String.format(Locale.ROOT, "%.2f", cdNm(stack) / 1000.0)));
    }
}
