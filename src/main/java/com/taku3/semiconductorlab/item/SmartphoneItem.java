package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * スマホ（[B-10]、v0.5 のゴール、O-029 Q28 案A 仮決定：アプリ3つ＝電卓・写真・傾き、使うと電池が減る）。
 * 今は入れ物だけ：画面（アプリ）の見た目はユーザーの答え待ち、TFT 液晶・バックライトがまだないので組立台で組めない（⚠️ 仮）
 */
public class SmartphoneItem extends Item {
    public SmartphoneItem(Properties p) { super(p); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) p.sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.placeholder"));
        return InteractionResult.SUCCESS;
    }
}
