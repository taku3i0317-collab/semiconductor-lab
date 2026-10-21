package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * 液滴発生器（ピエゾ）[8-13]（IMP2 S-007）。溶けたスズを細いノズルから押し出すと、液体の柱は自然に滴に分かれる（レイリー・プラトー不安定性）。
 * 水晶（圧電 [8-15]）でノズルを決まった速さでふるわせると、滴の大きさと間隔がそろう（インクジェットと同じ原理）。EUV 光源 [7-18] の部品。
 * 右クリック：ノズルの太さから、滴の直径（約1.89倍）・柱の分かれる間隔（約4.51倍）・1秒の滴の数。
 */
public class DropletGeneratorItem extends Item {
    /** ⚠️ 代表値：ノズルの太さ（µm）・スズの速さ（m/s）。本物は直径 約30µm の滴を毎秒 約5万個（ふるわせて滴をまとめる） */
    public static final double NOZZLE_UM = 16, SPEED_M_S = 70;
    private static final String K = "item.semiconductor-lab.droplet_generator.";

    public DropletGeneratorItem(Properties properties) { super(properties); }

    /** レイリー：一番育ちやすい波長＝柱の直径×約4.51、滴の直径＝柱の直径×約1.89 */
    public static double dropletUm(double nozzleUm) { return 1.89 * nozzleUm; }
    public static double spacingUm(double nozzleUm) { return 4.51 * nozzleUm; }
    /** 1秒の滴の数＝速さ÷間隔 */
    public static double perSecond(double nozzleUm, double speed) { return speed / (spacingUm(nozzleUm) * 1e-6); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide())
            p.sendSystemMessage(Component.translatable(K + "spec", String.format(Locale.ROOT, "%.0f", NOZZLE_UM), String.format(Locale.ROOT, "%.0f", dropletUm(NOZZLE_UM)),
                    String.format(Locale.ROOT, "%.0f", spacingUm(NOZZLE_UM)), String.format(Locale.ROOT, "%,.0f", perSecond(NOZZLE_UM, SPEED_M_S))));
        return InteractionResult.SUCCESS;
    }
}
