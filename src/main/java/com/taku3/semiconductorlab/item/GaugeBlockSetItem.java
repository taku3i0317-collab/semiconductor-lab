package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.LatheBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ゲージブロックのセット [8-05]（IMP2 O-010、ヨハンソン 1896）。焼入れしたるつぼ鋼 [1-11] の直方体を、定盤 [8-03] の上で µm まで平らに仕上げた長さの基準。
 * 平らすぎて押し付けると吸い付く（リンギング）ので、何個か重ねて好きな長さを作れる。
 * <ul>
 * <li>空中で右クリック：ランダムな目標の長さを、セットの何個の組み合わせで作るかを出す（{@link #combine(int)}）</li>
 * <li>もう片方の手にマイクロメータ [8-04] を持って旋盤 [8-01] を右クリック：旋盤のくせ（±25µm）をゲージブロックと比べて ±{@value #CALIBRATED_UM}µm に合わせる</li>
 * </ul>
 * セット（µm）：1.001〜1.009（9個）、1.01〜1.49（49個）、0.5〜24.5（49個）、25・50・75・100（4個）＝111個。⚠️ ヨハンソンの最初のセットとは個数が違う。
 */
public class GaugeBlockSetItem extends Item {
    public static final int CALIBRATED_UM = 1;
    public static final int MIN_UM = 3_000, MAX_UM = 150_000;
    private static final int[] BIG = {100_000, 75_000, 50_000, 25_000};
    private static final String K = "item.semiconductor-lab.gauge_block_set.";

    public GaugeBlockSetItem(Properties properties) { super(properties); }

    /** 目標の長さ（µm）を、セットのブロック（µm）の組み合わせにする。同じ大きさは1個ずつ。作れなければ空 */
    public static List<Integer> combine(int target) {
        List<Integer> used = new ArrayList<>();
        int rem = target;
        int d = rem % 10;                       // 1µm の位：1.001〜1.009
        if (d != 0) { used.add(1_000 + d); rem -= 1_000 + d; }
        int h = rem % 500;                      // 0.01mm の位：1.01〜1.49 で 0.5mm の倍数にそろえる
        if (h != 0) { used.add(1_000 + h); rem -= 1_000 + h; }
        if (rem < 0) return List.of();
        for (int b : BIG) if (b <= rem && rem - b >= 0) { used.add(b); rem -= b; }
        if (rem > 24_500) return List.of();
        if (rem > 0) used.add(rem);             // 0.5〜24.5
        return used;
    }

    private static String mm(int um) {
        String s = String.format(Locale.ROOT, "%.3f", um / 1000.0);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            int target = level.getRandom().nextIntBetweenInclusive(MIN_UM, MAX_UM);
            List<Integer> c = combine(target);
            p.sendSystemMessage(Component.translatable(K + "combine", mm(target), String.join(" + ", c.stream().map(GaugeBlockSetItem::mm).toList()), c.size()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof LatheBlockEntity lathe)) return InteractionResult.PASS;
        Player p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        InteractionHand other = ctx.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!(p.getItemInHand(other).getItem() instanceof GaugeItem g) || g.resolutionUm > 10) {
            p.sendSystemMessage(Component.translatable(K + "need_micrometer"));
            return InteractionResult.FAIL;
        }
        int before = lathe.errorUm();
        int after = ctx.getLevel().getRandom().nextIntBetweenInclusive(-CALIBRATED_UM, CALIBRATED_UM);
        lathe.calibrate(after);
        p.sendSystemMessage(Component.translatable(K + "calibrated", String.format(Locale.ROOT, "%+d", before), String.format(Locale.ROOT, "%+d", after)));
        return InteractionResult.SUCCESS;
    }
}
