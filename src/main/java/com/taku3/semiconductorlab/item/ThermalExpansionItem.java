package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 温度で伸びる長さを見せる金属の棒（IMP4-01、S-009）。空中で右クリックすると「1m の棒が 100℃ 上がると何µm 伸びるか」を鋼とくらべて出す。
 * [1-54] インバー（1896 ギヨーム：鉄＋ニッケル約36%、線膨張係数 約 1.2×10⁻⁶/K）に使う。鋼は約 12×10⁻⁶/K。
 */
public class ThermalExpansionItem extends Item {
    /** 鋼の線膨張係数（1/K） */
    public static final double STEEL_ALPHA = 12e-6;
    private final double alpha;
    private final String key;

    public ThermalExpansionItem(Properties p, double alpha, String id) {
        super(p);
        this.alpha = alpha;
        this.key = "item.semiconductor-lab." + id + ".expansion";
    }

    public double alpha() { return alpha; }

    /** 長さ m の棒が dT 上がったときの伸び（µm） */
    public static double stretchMicrometres(double alpha, double metres, double dT) { return alpha * metres * dT * 1e6; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide())
            p.sendOverlayMessage(Component.translatable(key, Math.round(stretchMicrometres(alpha, 1, 100)), Math.round(stretchMicrometres(STEEL_ALPHA, 1, 100))));
        return InteractionResult.SUCCESS;
    }
}
