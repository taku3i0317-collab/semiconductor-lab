package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * 数値演算チップ（[10-32] 浮動小数点の標準 IEEE 754、O-032 Q31 案A 仮決定：1980 インテル 8087 の形）。
 * 8ビット CPU・掛け算器・レジスタのお題に合格したチップを組立台で組む。右クリックで 0.1＋0.2 を半精度（16ビット）・単精度（32ビット）・倍精度（64ビット）で計算して、
 * どれも 0.3 から少しずれる（2進では 0.1 がちょうどに表せない＝丸めの誤差）のを見せる。AI チップ [B-11] の部品（学び直し＝学習は浮動小数点）
 */
public class MathCoprocessorItem extends Item {
    public MathCoprocessorItem(Properties p) { super(p); }

    /** 0.1＋0.2 の答え（ちょうどの10進の値）：{16ビット, 32ビット, 64ビット} */
    public static String[] sums() {
        float h = Float.float16ToFloat(Float.floatToFloat16(Float.float16ToFloat(Float.floatToFloat16(0.1f)) + Float.float16ToFloat(Float.floatToFloat16(0.2f))));
        float f = 0.1f + 0.2f;
        double d = 0.1 + 0.2;
        return new String[] {new BigDecimal(h).toPlainString(), new BigDecimal(f).toPlainString(), new BigDecimal(d).toPlainString()};
    }

    /** 0.1＋0.2 のビットの並び（16ビット・32ビット・64ビット、16進） */
    public static String[] bits() {
        short h = Float.floatToFloat16(Float.float16ToFloat(Float.floatToFloat16(0.1f)) + Float.float16ToFloat(Float.floatToFloat16(0.2f)));
        return new String[] {String.format(Locale.ROOT, "%04X", h & 0xFFFF), String.format(Locale.ROOT, "%08X", Float.floatToIntBits(0.1f + 0.2f)), String.format(Locale.ROOT, "%016X", Double.doubleToLongBits(0.1 + 0.2))};
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            String[] s = sums(), b = bits();
            p.sendSystemMessage(Component.translatable("message.semiconductor-lab.math_coprocessor.title"));
            for (int i = 0; i < 3; i++)
                p.sendSystemMessage(Component.translatable("message.semiconductor-lab.math_coprocessor.row" + i, s[i], b[i]));
        }
        return InteractionResult.SUCCESS;
    }
}
