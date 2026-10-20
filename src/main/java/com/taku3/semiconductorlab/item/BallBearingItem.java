package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 玉軸受 [8-20]（IMP2 O-013）。成分 BEARING_GRADE＝玉の精度の段階：
 * 1＝手で丸めた鋼球、2＝研削してマイクロメータでそろえた鋼球、3＝研削してゲージブロックでそろえた鋼球（1883 フィッシャーの核心）。
 * 回る機械に入れると、すべり軸受の摩擦が転がりになって消費電力が下がる（{@link #powerFactor(int)}、⚠️ ゲームの値）。
 */
public class BallBearingItem extends Item {
    private static final String K = "item.semiconductor-lab.ball_bearing.";
    /** 段階ごとの消費電力の倍率（0＝軸受なし＝すべり軸受） */
    private static final double[] FACTOR = {1.0, 0.95, 0.85, 0.70};

    public BallBearingItem(Properties properties) { super(properties); }

    public static double powerFactor(int grade) { return FACTOR[Math.max(0, Math.min(FACTOR.length - 1, grade))]; }

    public static ItemStack of(int grade) {
        ItemStack s = new ItemStack(Precision.BALL_BEARING);
        s.set(Precision.BEARING_GRADE, grade);
        return s;
    }

    /** 回る機械を右クリック → 玉軸受を入れる（前に入っていた物は手元に返す） */
    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof com.taku3.semiconductorlab.energy.ConsumerBlockEntity be)) return net.minecraft.world.InteractionResult.PASS;
        var p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide()) return net.minecraft.world.InteractionResult.SUCCESS;
        if (!be.acceptsBearing()) {
            if (p != null) p.sendSystemMessage(Component.translatable(K + "not_rotating"));
            return net.minecraft.world.InteractionResult.FAIL;
        }
        int old = be.bearingGrade(), g = grade(ctx.getItemInHand());
        be.installBearing(g);
        ctx.getItemInHand().shrink(1);
        if (p != null) {
            if (old > 0) p.getInventory().placeItemBackInInventory(of(old), net.minecraft.util.Prediction.SERVER_ONLY);
            p.sendSystemMessage(Component.translatable(K + "installed", Component.translatable(K + "grade" + g), Math.round((1 - powerFactor(g)) * 100)));
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    public static int grade(ItemStack s) { return s.is(Precision.BALL_BEARING) ? s.getOrDefault(Precision.BEARING_GRADE, 1) : 0; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        int g = grade(stack);
        out.accept(Component.translatable(K + "grade" + g));
        out.accept(Component.translatable(K + "factor", Math.round((1 - powerFactor(g)) * 100)));
    }
}
