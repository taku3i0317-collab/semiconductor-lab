package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

import java.util.Locale;

/**
 * ルビーレーザー [7-13]（IMP2 O-019、1960 メイマン）。クロムを少し入れたアルミナの単結晶（ルビー）の棒を、
 * らせん形のキセノン閃光ランプで強く照らすと、Cr³⁺ が励起され、誘導放出（アインシュタイン 1917）で 694.3nm の赤い光がそろって出る。
 * 棒の両端の銀の鏡で光が何度も往復して強まる。
 * <ul>
 * <li>1発ごとに、持ち物の鉛蓄電池から {@value #PULSE_J} J をコンデンサに貯めて閃光ランプで放つ（電池がないと撃てない）</li>
 * <li>もう片方の手に干渉計 [7-09] → 水銀灯の光とのコヒーレンス（そろい方）の比べっこ：縞が見えている鏡の動きの範囲</li>
 * <li>事故（DESIGN「危ない技術の扱い」⑥）：ガラス・氷・鉄や金のブロックなどの光るブロックに当てると、反射が自分の目に入って数秒見えなくなる
 *   （撃った本人だけ。地形は壊れない）</li>
 * </ul>
 * 耐久＝閃光ランプの残りの回数（ランプと合わせて作業台で交換）。
 */
public class RubyLaserItem extends Item {
    /** ルビーの R1 線 */
    public static final double RUBY_NM = 694.3;
    /** 1発の閃光ランプのエネルギー（J）。⚠️ メイマンの装置のしきい値は数百 J〜1kJ ほど */
    public static final long PULSE_J = 1_000;
    /** 光の幅（nm）。⚠️ ゲームの値：水銀灯の緑の線は圧力で広がる、ルビーレーザーは 0.01nm ほど */
    public static final double HG_WIDTH_NM = 0.5, RUBY_WIDTH_NM = 0.01;
    /** 反射が目に入ったとき見えなくなる時間（tick） */
    public static final int BLIND_TICKS = 20 * 4;
    private static final String K = "item.semiconductor-lab.ruby_laser.";

    public RubyLaserItem(Properties properties) { super(properties); }

    /** コヒーレンス長 Lc = λ²/Δλ（mm）：この道の長さの差までは縞が見える */
    public static double coherenceMm(double nm, double widthNm) { return nm * nm / widthNm / 1e6; }

    /** 縞が見えている間に動かせる鏡の距離（往復で2倍になるので Lc/2） */
    public static double mirrorRangeMm(double nm, double widthNm) { return coherenceMm(nm, widthNm) / 2; }

    /** 光るブロック（反射が目に入る）：ガラス・氷・金属のブロック */
    public static boolean reflective(BlockState s) {
        return s.is(BlockTags.ICE) || s.getBlock() instanceof net.minecraft.world.level.block.TransparentBlock
                || s.getBlock() instanceof net.minecraft.world.level.block.StainedGlassPaneBlock || s.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock
                || s.is(net.minecraft.world.level.block.Blocks.IRON_BLOCK) || s.is(net.minecraft.world.level.block.Blocks.GOLD_BLOCK);
    }

    /** 持ち物の鉛蓄電池から1発分を取る。足りなければ false */
    public static boolean drawPulse(Player p) {
        if (p.getAbilities().instabuild) return true;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            Long j = s.get(ModDataComponents.BATTERY_ENERGY);
            if (j == null || j < PULSE_J || s.getCount() != 1) continue;
            if (j - PULSE_J > 0) s.set(ModDataComponents.BATTERY_ENERGY, j - PULSE_J);
            else s.remove(ModDataComponents.BATTERY_ENERGY);
            return true;
        }
        return false;
    }

    /** 1発撃つ：電池とランプを使う。撃てなければ理由を出して false */
    private static boolean fire(Level level, Player p, ItemStack laser) {
        if (!drawPulse(p)) { p.sendSystemMessage(Component.translatable(K + "no_battery", PULSE_J)); return false; }
        if (!p.getAbilities().instabuild) {
            laser.setDamageValue(laser.getDamageValue() + 1);
            if (laser.getDamageValue() >= laser.getMaxDamage()) { laser.shrink(1); p.sendSystemMessage(Component.translatable(K + "lamp_spent")); }
        }
        return true;
    }

    /** 干渉計で比べる：水銀灯とルビーレーザーの、縞が見える鏡の範囲と縞の数 */
    public static void compare(Player p) {
        double hg = mirrorRangeMm(InterferometerItem.HG_GREEN_NM, HG_WIDTH_NM), ruby = mirrorRangeMm(RUBY_NM, RUBY_WIDTH_NM);
        p.sendSystemMessage(Component.translatable(K + "compare_lamp", String.format(Locale.ROOT, "%.2f", hg),
                Math.round(InterferometerItem.fringes(hg, InterferometerItem.HG_GREEN_NM))));
        p.sendSystemMessage(Component.translatable(K + "compare_laser", String.format(Locale.ROOT, "%.0f", ruby),
                Math.round(InterferometerItem.fringes(ruby, RUBY_NM))));
        p.sendSystemMessage(Component.translatable(K + "compare_why"));
    }

    private static boolean interferometerIn(Player p, InteractionHand hand) {
        return p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() instanceof InterferometerItem;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack laser = p.getItemInHand(hand);
            if (fire(level, p, laser)) {
                if (interferometerIn(p, hand)) compare(p);
                else p.sendSystemMessage(Component.translatable(K + "pulse", String.format(Locale.ROOT, "%.1f", RUBY_NM)));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player p = ctx.getPlayer();
        if (p == null) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (!fire(level, p, ctx.getItemInHand())) return InteractionResult.SUCCESS;
            if (interferometerIn(p, ctx.getHand())) compare(p);
            else if (reflective(level.getBlockState(ctx.getClickedPos()))) {
                // 事故：反射した光が自分の目に入る（撃った本人だけ・数秒）
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLIND_TICKS));
                p.sendSystemMessage(Component.translatable(K + "accident"));
            } else p.sendSystemMessage(Component.translatable(K + "spot", String.format(Locale.ROOT, "%.1f", RUBY_NM)));
        }
        return InteractionResult.SUCCESS;
    }
}
