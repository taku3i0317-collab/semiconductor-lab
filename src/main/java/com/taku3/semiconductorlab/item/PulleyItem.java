package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * 滑車 [8-19]（IMP2 S-010、古代：ウィトルウィウス・ヘロンの記述）。綱を何本もの車にかけると、小さな力で重い物が持ち上がる（そのぶん綱を長く引く）。
 * 右クリックしたブロック1個（1m³）を持ち上げるのに要る力 F ＝ m·g ÷ 綱の本数 N と、引く綱の長さ N m。人の力 {@value #HUMAN_N} N と比べる。
 */
public class PulleyItem extends Item {
    /** 人が綱を引ける力（N）。⚠️ 代表値（体重 40kg ぶんくらい） */
    public static final double HUMAN_N = 400, G = 9.8;
    private static final String K = "item.semiconductor-lab.pulley.";
    /** 重さを支える綱の本数（定滑車 1、複滑車 4） */
    public final int ropes;

    public PulleyItem(Properties properties, int ropes) { super(properties); this.ropes = ropes; }

    /** ブロック1個（1m³）の重さ（kg）。⚠️ 代表的な密度 */
    public static double massKg(BlockState s) {
        if (s.is(Blocks.GOLD_BLOCK)) return 19_300;
        if (s.is(Blocks.IRON_BLOCK)) return 7_870;
        if (s.is(BlockTags.LOGS) || s.is(BlockTags.PLANKS) || s.is(BlockTags.WOOL)) return s.is(BlockTags.WOOL) ? 100 : 600;
        if (s.is(BlockTags.DIRT) || s.is(BlockTags.SAND)) return 1_500;
        if (s.is(Blocks.WATER) || s.is(Blocks.ICE)) return 1_000;
        return 2_600;   // 石など
    }

    /** 引く力（N） */
    public static double forceN(double kg, int ropes) { return kg * G / ropes; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        BlockState s = ctx.getLevel().getBlockState(ctx.getClickedPos());
        double kg = massKg(s), f = forceN(kg, ropes);
        p.sendSystemMessage(Component.translatable(K + "lift", s.getBlock().getName(), String.format(Locale.ROOT, "%,.0f", kg), ropes,
                String.format(Locale.ROOT, "%,.0f", f), ropes));
        p.sendSystemMessage(Component.translatable(K + (f <= HUMAN_N ? "can" : "cannot"), String.format(Locale.ROOT, "%.0f", HUMAN_N),
                (int) Math.ceil(kg * G / HUMAN_N)));
        return InteractionResult.SUCCESS;
    }
}
