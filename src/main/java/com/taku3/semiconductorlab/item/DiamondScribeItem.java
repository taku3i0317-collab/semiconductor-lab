package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.litho.ChipState;
import com.taku3.semiconductorlab.litho.LayerKind;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * [9-32] ダイシングの段階1（1960年ごろ、O-013）：ダイヤモンドのけがき針。もう片方の手に仕上がった加工中のウェハーを持って右クリックすると、
 * 表面に格子の傷（スクライブ）を付けて割り、チップ {@link #CHIPS_PER_WAFER} 個にする。チップは同じ chip_state を引き継ぐ（検査台で同じ発見）。
 * 割るときにまれに欠ける（歩留まり）：1枚につき {@link #BREAK_CHANCE} の確率で1個だけ失う（DESIGN「ストレスを減らす」：小さく）。
 * 段階2（円盤刃のダイシングソー、[8-08] ボールねじの台）はのちに。
 */
public class DiamondScribeItem extends Item {
    /** 1枚から取れるチップの数（本物の1インチのウェハーは数十〜百個。ゲームは8個） */
    public static final int CHIPS_PER_WAFER = 8;
    /** 割るときに1個欠ける確率 */
    public static final float BREAK_CHANCE = 0.1f;
    private static final String K = "item.semiconductor-lab.diamond_scribe.";

    public DiamondScribeItem(Properties p) { super(p); }

    /** 割ってよいウェハー：レジストがなく、Al の配線まで終わっている */
    public static boolean finished(ItemStack w) {
        if (!w.is(Semiconductor.PROCESS_WAFER)) return false;
        ChipState s = w.get(ModDataComponents.CHIP_STATE);
        return s != null && s.resist() == ChipState.Resist.NONE && s.has(LayerKind.METAL);
    }

    /** ウェハー1枚を割ったチップ（欠けた数を引いたもの）。chip_state をそのまま写す */
    public static ItemStack scribe(ItemStack wafer, RandomSource random) {
        int n = CHIPS_PER_WAFER - (random.nextFloat() < BREAK_CHANCE ? 1 : 0);
        ItemStack chips = new ItemStack(Semiconductor.CHIP, n);
        chips.set(ModDataComponents.CHIP_STATE, wafer.get(ModDataComponents.CHIP_STATE));
        return chips;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack wafer = p.getItemInHand(other);
        if (!wafer.is(Semiconductor.PROCESS_WAFER)) { p.sendSystemMessage(Component.translatable(K + "hold")); return InteractionResult.SUCCESS; }
        if (!finished(wafer)) { p.sendSystemMessage(Component.translatable(K + "not_finished")); return InteractionResult.SUCCESS; }
        ItemStack chips = scribe(wafer, level.getRandom());
        int got = chips.getCount();
        wafer.shrink(1);
        if (!p.getInventory().add(chips)) net.minecraft.world.level.block.Block.popResource(level, p.blockPosition(), chips);
        p.getItemInHand(hand).hurtAndBreak(1, p, hand);
        p.sendSystemMessage(Component.translatable(K + "done", got, CHIPS_PER_WAFER - got));
        return InteractionResult.SUCCESS;
    }
}
