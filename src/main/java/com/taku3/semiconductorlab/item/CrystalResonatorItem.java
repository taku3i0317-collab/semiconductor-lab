package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.EvaporatorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * 水晶振動子 [8-15]（IMP2 O-016、1921 ケイディ・1927 マリソンの水晶時計・1959 ザウアーブレイ）。水晶片に銀の電極を付けた物。厚さ（振動数）は水晶片から引き継ぐ。
 * <ul>
 * <li>空中で右クリック：1秒に何回ふるえるか（時計の心臓。のちの計算機 [10-] のクロックは {@link #frequencyHz(ItemStack)} を読む）</li>
 * <li>真空蒸着装置 [5-14] を右クリック：膜厚計。そこで付けた膜の厚さ（1回 {@value #FILM_NM_PER_RUN}nm の銀⚠️）で振動数が下がる（ザウアーブレイの式 Δf ＝ 2.26×10⁻⁶ f₀² Δm/A）</li>
 * </ul>
 * 磨く（薄くする）のは水晶片のうち。電極を付けたあとは磨けない。
 */
public class CrystalResonatorItem extends QuartzBlankItem {
    /** 蒸着1回で付く銀の膜（nm）。⚠️ ゲームの値 */
    public static final int FILM_NM_PER_RUN = 50;
    /** 銀の密度（g/cm³） */
    public static final double SILVER_G_CM3 = 10.49;

    public CrystalResonatorItem(Properties properties) { super(properties); }

    public static double frequencyHz(ItemStack s) { return freqMHz(thicknessUm(s)) * 1e6; }

    /** ザウアーブレイ：厚さ filmNm の銀で振動数がいくつ下がるか（Hz） */
    public static double sauerbreyHz(double f0Hz, double filmNm) {
        double massPerArea = SILVER_G_CM3 * filmNm * 1e-7;   // g/cm²
        return 2.26e-6 * f0Hz * f0Hz * massPerArea;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "beats", String.format(Locale.ROOT, "%,.0f", frequencyHz(p.getItemInHand(hand)))));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof EvaporatorBlockEntity ev)) return InteractionResult.PASS;
        Player p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        double f0 = frequencyHz(ctx.getItemInHand()), film = ev.runs() * (double) FILM_NM_PER_RUN;
        p.sendSystemMessage(Component.translatable(K + "monitor", ev.runs(), String.format(Locale.ROOT, "%.0f", film),
                String.format(Locale.ROOT, "%,.0f", sauerbreyHz(f0, film))));
        return InteractionResult.SUCCESS;
    }
}
