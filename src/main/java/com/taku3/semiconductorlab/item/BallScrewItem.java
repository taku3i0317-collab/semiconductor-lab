package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * ボールねじ [8-08] 精密ステージ（IMP2 O-014）。送りねじ [8-01] のねじ山とナットの間に鋼球を転がして、すべりの摩擦をなくしたねじ。
 * 段階（成分 BEARING_GRADE、玉軸受 [8-20] と同じ 1〜3）は中の鋼球の精度。
 * <ul>
 * <li>送りの細かさ {@link #feedUm(ItemStack)}：送りねじは すべりの引っかかり（スティックスリップ）で 10µm、ボールねじは 5・2・1µm</li>
 * <li>旋盤の親ねじの入口に入れると、作る部品の誤差が {@link #errorScale(ItemStack)} 倍、消費電力が {@link #powerFactor(ItemStack)} 倍</li>
 * </ul>
 * ⚠️ 数値はゲームの値。のちのアライメント [8-17] のステージの細かさにも使う。
 */
public class BallScrewItem extends Item {
    private static final String K = "item.semiconductor-lab.ball_screw.";
    private static final int[] FEED_UM = {10, 5, 2, 1};
    private static final double[] ERROR_SCALE = {1.0, 0.5, 0.25, 0.1};

    public BallScrewItem(Properties properties) { super(properties); }

    /** 段階（送りねじ＝0） */
    public static int grade(ItemStack s) { return s.is(Precision.BALL_SCREW) ? s.getOrDefault(Precision.BEARING_GRADE, 1) : 0; }

    /** ステージを動かせる一番小さい送り（µm） */
    public static int feedUm(ItemStack s) { return FEED_UM[Math.min(3, grade(s))]; }

    /** 旋盤で作る部品の誤差の倍率 */
    public static double errorScale(ItemStack s) { return ERROR_SCALE[Math.min(3, grade(s))]; }

    /** 送りの摩擦の分の消費電力の倍率（玉軸受の半分の効き目） */
    public static double powerFactor(ItemStack s) { return 1 - (1 - BallBearingItem.powerFactor(grade(s))) / 2; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable("item.semiconductor-lab.ball_bearing.grade" + grade(stack)));
        out.accept(Component.translatable(K + "feed", feedUm(stack), Math.round(errorScale(stack) * 100)));
    }
}
