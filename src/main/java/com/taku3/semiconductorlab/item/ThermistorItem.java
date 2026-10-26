package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * サーミスタ [9-44]（1833 ファラデー：硫化銀は温まると電気を通しやすくなる。実用の NTC サーミスタは 1930年代〜。IMP4-01、S-009 案A 仮決定）：
 * マンガン・ニッケルの酸化物を焼き固めた半導体の焼き物。温度が上がると抵抗が下がる（NTC）。ブロックを右クリックすると、そこの温度と抵抗値。
 * 測れるのは {@link #MIN_C}〜{@link #MAX_C}℃（焼き物がこわれない範囲）。抵抗は B 定数の式 R = R25·exp(B(1/T − 1/298.15)) ⚠️（R25 10kΩ・B 3950K）。
 * 温度の値は熱電対と同じ、ブロックごとの代表値 ⚠️。
 */
public class ThermistorItem extends Item {
    public static final int MIN_C = -50, MAX_C = 300;
    public static final double R25_OHM = 10_000, B_K = 3950;
    private static final String K = "item.semiconductor-lab.thermistor.";

    public ThermistorItem(Properties p) { super(p); }

    /** 温度 c（℃）のときの抵抗（Ω） */
    public static double ohms(double c) { return R25_OHM * Math.exp(B_K * (1 / (c + 273.15) - 1 / 298.15)); }

    /** 抵抗を読みやすく（kΩ・Ω） */
    public static String format(double ohm) { return ohm >= 1000 ? String.format(java.util.Locale.ROOT, "%.1f kΩ", ohm / 1000) : String.format(java.util.Locale.ROOT, "%.0f Ω", ohm); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide() && ctx.getPlayer() != null) {
            int c = ThermocoupleItem.temperatureC(ctx.getLevel().getBlockState(ctx.getClickedPos()));
            ctx.getPlayer().sendOverlayMessage(c > MAX_C ? Component.translatable(K + "over", MAX_C) : Component.translatable(K + "reading", c, format(ohms(c))));
        }
        return InteractionResult.SUCCESS;
    }
}
