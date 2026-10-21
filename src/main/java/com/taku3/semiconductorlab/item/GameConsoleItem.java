package com.taku3.semiconductorlab.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * ゲーム機 [B-09]（S-017 Q27 案A ポン型）：カウンタ・全加算器・レジスタのお題に合格したチップを組立台で組んだ、CPU なしの卓球ゲーム（1972 のポンと同じく論理回路だけ）。
 * 右クリックで画面（左の板をマウスで動かす）。動きは {@link PongLogic}
 */
public class GameConsoleItem extends Item {
    /** 画面を開く（クライアントの初期化で入れる） */
    public static Runnable clientOpen = () -> {};

    public GameConsoleItem(Properties p) { super(p); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) clientOpen.run();
        return InteractionResult.SUCCESS;
    }
}
