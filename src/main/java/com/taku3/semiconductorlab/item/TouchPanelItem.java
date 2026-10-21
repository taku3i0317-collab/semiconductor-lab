package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 静電容量式タッチパネル [15-18]（1965 イギリス王立レーダー研究所〜2007 スマホ。IMP4-01、O-021 仮決定の案A）：
 * 透明な ITO の電極を縦横に並べ、交点ごとに少しの電気をためておく。指（体）は電気をためられる導体なので、近づくと交点の静電容量（ためられる電気の量）が増え、読み取りの IC がその場所を知る。
 * 右クリックで読む：プレイヤー・動物（体の水分で電気を通す）に向けると反応し、ブロック・何もない所では反応しない（手袋や棒で押しても反応しない、の理由）。
 * ⚠️ 変化の大きさ（約 0.5〜2 pF）と、体の大きさで変わる計算はゲームの値（touch_panel.placeholder）。
 */
public class TouchPanelItem extends Item {
    private static final String K = "item.semiconductor-lab.touch_panel.";

    public TouchPanelItem(Properties properties) { super(properties); }

    /** 体（生き物）が触れたときの静電容量の変化（pF）：体が大きいほど少し大きい（⚠️ ゲームの値） */
    public static double deltaPf(LivingEntity e) {
        double size = e.getBbWidth() * e.getBbHeight();
        return Math.round(Math.min(2.0, 0.5 + 0.6 * size) * 10) / 10.0;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide()) player.sendOverlayMessage(Component.translatable(K + "touch", String.valueOf(deltaPf(target)), target.getDisplayName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide() && ctx.getPlayer() != null) ctx.getPlayer().sendOverlayMessage(Component.translatable(K + "no_touch_block"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) p.sendOverlayMessage(Component.translatable(K + "no_touch"));
        return InteractionResult.SUCCESS;
    }
}
