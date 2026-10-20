package com.taku3.semiconductorlab.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 電卓 [B-08]（S-010）：自分で作ったチップ（加算器・レジスタ・カウンタのお題に合格したチップ）＋キー＋ニキシー管を組立台で組んだもの。
 * 右クリックで画面（キーで入力、ニキシー管で表示）。計算は {@link CalculatorLogic}（4ケタの足し算・引き算）。
 */
public class CalculatorItem extends Item {
    /** 画面を開く（クライアントの初期化で入れる。サーバーでは何もしない） */
    public static Runnable clientOpen = () -> {};

    public CalculatorItem(Properties p) { super(p); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) clientOpen.run();
        return InteractionResult.SUCCESS;
    }
}
