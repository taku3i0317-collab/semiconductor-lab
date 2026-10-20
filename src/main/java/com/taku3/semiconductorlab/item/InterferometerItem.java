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
