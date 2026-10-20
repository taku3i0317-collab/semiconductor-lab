package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * 寸法を測る計器 [8-04]（IMP2-05）：ノギス（副尺、ヴェルニエ 1631、読み 0.05mm）とマイクロメータ（パルマー 1848、
 * ねじ1回転 0.5mm を目盛り 50 で割って 0.01mm）。もう片方の手の部品（旋盤で削った物＝成分 Precision.PART_ERROR_UM）を測り、
 * 読みの細かさに丸めた長さと、等級（A：誤差 10µm 以内／B：20µm 以内／C：それより大きい）を出す。
 * 読みが 10µm より粗い計器では等級を決められない（マイクロメータが要る）。
 */
public class GaugeItem extends Item {
    /** 等級の境目（µm） */
    public static final int GRADE_A_UM = 10, GRADE_B_UM = 20;
    private static final String K = "item.semiconductor-lab.gauge.";

    /** 読みの細かさ（µm） */
    public final int resolutionUm;

    public GaugeItem(Properties properties, int resolutionUm) { super(properties); this.resolutionUm = resolutionUm; }

    /** 部品のもとの寸法（mm）。測れない物は 0 */
    public static double nominalMm(ItemStack s) {
        if (s.is(Precision.PUMP_ROTOR)) return 40.0;       // ローターの直径
        if (s.is(Precision.STANDARD_SCREW)) return 10.0;   // ねじの外径（M10 くらい）
        return 0;
    }

    /** 読みの細かさに丸めた測った値（mm） */
    public double readMm(double nominalMm, int errorUm) {
        double um = nominalMm * 1000 + errorUm;
        return Math.round(um / resolutionUm) * resolutionUm / 1000.0;
    }

    /** 等級（"A"・"B"・"C"）。この計器の読みでは決められないなら null */
    public String grade(int errorUm) {
        if (resolutionUm > GRADE_A_UM) return null;
        int e = Math.abs(errorUm);
        return e <= GRADE_A_UM ? "A" : e <= GRADE_B_UM ? "B" : "C";
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack part = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        double nom = nominalMm(part);
        Integer err = part.get(Precision.PART_ERROR_UM);
        if (nom <= 0 || err == null) {
            p.sendSystemMessage(Component.translatable(K + "hold"));
            return InteractionResult.SUCCESS;
        }
        String fmt = resolutionUm >= 50 ? "%.2f" : "%.3f";
        p.sendSystemMessage(Component.translatable(K + "read", part.getHoverName(), String.format(Locale.ROOT, fmt, readMm(nom, err)),
                String.format(Locale.ROOT, "%.3f", resolutionUm / 1000.0)));
        String g = grade(err);
        p.sendSystemMessage(g == null ? Component.translatable(K + "no_grade") : Component.translatable(K + "grade_" + g.toLowerCase(Locale.ROOT), err));
        return InteractionResult.SUCCESS;
    }
}
