package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.MercuryLampBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 反射鏡 [7-05] の3段階（IMP2 O-020）：スペキュラム合金（1668 ニュートン）→ 銀鏡（1857 フーコー）→ アルミ蒸着（1930年代 ストロング）。
 * 点灯中の水銀灯 [7-10] を右クリックすると、水銀の3つの線（緑 546nm・i線 365nm・紫外 254nm）をどれだけ反射するかを比べられる。
 * 空中で右クリックすると、空気の中でどう曇るか（スペキュラムは数週間・銀は硫黄で数か月・アルミは薄い酸化膜で何年も）。
 * 反射率は ⚠️ 代表値（新しい鏡、垂直に当てた光）。
 */
public class MirrorItem extends Item {
    /** 水銀の3つの線（nm） */
    public static final double[] LINES_NM = {546.1, 365.0, 253.7};
    private static final String K = "item.semiconductor-lab.mirror.";

    /** 反射率（%）：緑・i線・紫外の順 */
    public final int[] reflectance;
    /** 曇り方の lang キーの末尾（speculum / silver / aluminium） */
    public final String tarnish;

    public MirrorItem(Properties properties, String tarnish, int green, int iLine, int uv) {
        super(properties);
        this.tarnish = tarnish;
        this.reflectance = new int[]{green, iLine, uv};
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player p = ctx.getPlayer();
        if (!MercuryLampBlockEntity.isLit(level.getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        if (!level.isClientSide() && p != null) {
            p.sendSystemMessage(Component.translatable(K + "lamp", ctx.getItemInHand().getHoverName()));
            for (int i = 0; i < LINES_NM.length; i++)
                p.sendSystemMessage(Component.translatable(K + "line" + i, String.format(java.util.Locale.ROOT, "%.1f", LINES_NM[i]), reflectance[i]));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "tarnish." + tarnish));
        return InteractionResult.SUCCESS;
    }
}
