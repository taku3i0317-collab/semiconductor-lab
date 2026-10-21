package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 水銀の体温計・温度計 [13-13]（1714 ファーレンハイトの水銀温度計、1866 オールバットの短い体温計。IMP3、S-011）。
 * 右クリック：その場の空気の温度（バイオームの温度から、{@link #airCelsius}）。しゃがんで右クリック：自分の体温（約36.5〜37.0℃）。
 * ⚠️ 空気の温度はゲームの値（バイオームの温度の数を ℃ に直した近似）。水銀は毒（GHS）なので、今は電子式に置きかわった。
 */
public class ThermometerItem extends Item {
    public ThermometerItem(Properties p) { super(p); }

    /** バイオームの温度（バニラの数、平原 0.8・砂漠 2.0・雪原 0.0）→ ℃ の近似 */
    public static double airCelsius(float biomeTemperature) { return 25.0 * biomeTemperature - 3.0; }

    /** ふつうの体温（わきの下）の近似：36.5〜37.0℃ */
    public static double bodyCelsius(double random01) { return 36.5 + 0.5 * random01; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (p.isShiftKeyDown()) {
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.mercury_thermometer.body", String.format("%.1f", bodyCelsius(level.getRandom().nextDouble()))));
        } else {
            float t = level.getBiome(p.blockPosition()).value().getBaseTemperature();
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.mercury_thermometer.air", String.format("%.0f", airCelsius(t))));
        }
        return InteractionResult.SUCCESS;
    }
}
