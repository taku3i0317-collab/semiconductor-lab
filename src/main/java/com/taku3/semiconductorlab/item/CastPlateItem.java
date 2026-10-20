package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * 鋳鉄の板 [8-03] 平面と定盤（IMP2 O-009）。板の面のゆがみ（そり）を成分 PLATE_CURVE（0.1µm 単位、＋は凸・−は凹）で持つ。
 * 利き手にこの板、もう片方の手に別の板（鋳鉄の板か定盤）を持って右クリック → 研磨剤（ルージュ）1つで2枚をすり合わせる。
 * すり合わせると2枚は「ぴったり合う」形（a ≒ −b）に近づくが、2枚だけだと凸と凹のまま合ってしまう。
 * 3枚を A-B・B-C・C-A と順に合わせると、3枚とも平面に近づく（ホイットワース 1830〜40年代）。そりが 1µm 以下になると定盤になる。
 */
public class CastPlateItem extends Item {
    /** 1回のすり合わせで、2枚の合わない分（a＋b）のうち削れる割合 */
    public static final double LAP_RATE = 0.8;
    /** 定盤になるそり（0.1µm 単位）＝1µm */
    public static final int FLAT_TENTHS = 10;
    /** 鋳物のそりの範囲（0.1µm 単位）＝ ±20〜60µm */
    public static final int CAST_MIN = 200, CAST_MAX = 600;
    private static final String K = "item.semiconductor-lab.cast_iron_plate.";

    public CastPlateItem(Properties properties) { super(properties); }

    /** すり合わせ：2枚のそり → すり合わせた後のそり（0.1µm 単位） */
    public static int[] lap(int a, int b) {
        int d = (int) Math.round(LAP_RATE * (a + b) / 2.0);
        return new int[] {a - d, b - d};
    }

    public static boolean isPlate(ItemStack s) { return s.is(Precision.CAST_IRON_PLATE) || s.is(Precision.SURFACE_PLATE.asItem()); }

    /** 板のそり。まだ無ければ（鋳物のまま）ランダムに決める。定盤で成分が無い物は 0 */
    public static int curve(ItemStack s, net.minecraft.util.RandomSource rnd) {
        Integer c = s.get(Precision.PLATE_CURVE);
        if (c != null) return c;
        if (!s.is(Precision.CAST_IRON_PLATE)) return 0;
        int v = rnd.nextIntBetweenInclusive(CAST_MIN, CAST_MAX);
        return rnd.nextBoolean() ? v : -v;
    }

    /** そりに合わせて、鋳鉄の板か定盤（1µm 以下）の物を作る */
    public static ItemStack withCurve(int c) {
        ItemStack s = new ItemStack(Math.abs(c) <= FLAT_TENTHS ? Precision.SURFACE_PLATE.asItem() : Precision.CAST_IRON_PLATE);
        s.set(Precision.PLATE_CURVE, c);
        return s;
    }

    public static String fmt(int tenths) { return String.format("%+.1f", tenths / 10.0); }

    private static Component shape(int c) {
        return Component.translatable(K + (Math.abs(c) <= FLAT_TENTHS ? "flat" : c > 0 ? "convex" : "concave"));
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack other = p.getItemInHand(InteractionHand.OFF_HAND);
        if (!isPlate(other)) {
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "hold"));
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        int slot = p.getInventory().findSlotMatchingItem(new ItemStack(Optics.ROUGE));
        if (slot < 0 && !p.getAbilities().instabuild) {
            p.sendSystemMessage(Component.translatable(K + "no_rouge"));
            return InteractionResult.FAIL;
        }
        if (slot >= 0 && !p.getAbilities().instabuild) p.getInventory().removeItem(slot, 1);
        int[] r = lap(curve(p.getItemInHand(hand), level.getRandom()), curve(other, level.getRandom()));
        p.setItemInHand(InteractionHand.MAIN_HAND, withCurve(r[0]));
        p.setItemInHand(InteractionHand.OFF_HAND, withCurve(r[1]));
        p.sendSystemMessage(Component.translatable(K + "lapped", fmt(r[0]), shape(r[0]), fmt(r[1]), shape(r[1]),
                String.format("%.1f", Math.abs(r[0] + r[1]) / 10.0)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        Integer c = stack.get(Precision.PLATE_CURVE);
        out.accept(c == null ? Component.translatable(K + "unknown") : Component.translatable(K + "curve", fmt(c), shape(c)));
    }
}
