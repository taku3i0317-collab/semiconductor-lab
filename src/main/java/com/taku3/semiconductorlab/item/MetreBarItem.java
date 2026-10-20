package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Precision;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * メートル原器 [8-22]（IMP2 O-007）。1799年の「アルシーヴのメートル」：子午線の三角測量で決めた長さの白金の棒（両はしの面の間が 1m＝端度器）。
 * 遊び方：ブロックを右クリック → 1つ目の印、別のブロックを右クリック → 2つの間の長さを m と、昔の単位（尺・トワーズ）で出す。
 * Minecraft の 1ブロック＝1m なので、原器で測ると「ブロックの数」がそのままメートルになる。
 */
public class MetreBarItem extends Item {
    /** 1 尺（曲尺）＝ 10/33 m（1891年の度量衡法で決めた） */
    public static final double SHAKU_M = 10.0 / 33.0;
    /** 1 トワーズ（フランスの古い単位、ペルーのトワーズ）≒ 1.949 m */
    public static final double TOISE_M = 1.949;
    private static final String K = "item.semiconductor-lab.metre_bar.";

    public MetreBarItem(Properties properties) { super(properties); }

    /** 2つのブロックの中心の間の長さ（m） */
    public static double length(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distSqr(b));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player p = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockPos pos = ctx.getClickedPos();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        BlockPos a = stack.get(Precision.METRE_MARK);
        if (a == null || a.equals(pos)) {
            stack.set(Precision.METRE_MARK, pos.immutable());
            p.sendSystemMessage(Component.translatable(K + "mark", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            double m = length(a, pos);
            stack.remove(Precision.METRE_MARK);
            p.sendSystemMessage(Component.translatable(K + "length", String.format("%.3f", m),
                    String.format("%.1f", m / SHAKU_M), String.format("%.2f", m / TOISE_M)));
        }
        return InteractionResult.SUCCESS;
    }
}
