package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Locale;

/**
 * 多層膜ミラー [7-17]（IMP2 S-007、1985 Mo/Si）。EUV（13.5nm）はどの物質にも吸われ、1枚の面ではほとんど反射しない。
 * モリブデンとケイ素を周期 約6.9nm で何十対も重ねると、各境目の弱い反射が強め合う（ブラッグ反射）。
 * スパッタ装置で ULE ガラスに Mo/Si の的を当てるたびに {@value #PAIRS_PER_RUN} 対ずつ増える（成分 custom_data の Pairs）。
 * 反射率 R(N) ＝ {@value #R_MAX}·tanh²(N/{@value #N0})（⚠️ 近似：本物は約40対で約70%、それ以上は吸収で頭打ち）。
 */
public class MultilayerMirrorItem extends Item {
    public static final int PAIRS_PER_RUN = 10, MAX_PAIRS = 60;
    public static final double R_MAX = 0.70, N0 = 15;
    private static final String K = "item.semiconductor-lab.multilayer_mirror.";

    public MultilayerMirrorItem(Properties properties) { super(properties); }

    public static int pairs(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : d.copyTag().getIntOr("Pairs", 0);
    }

    public static ItemStack withPairs(ItemStack s, int n) {
        CompoundTag t = new CompoundTag();
        t.putInt("Pairs", n);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return s;
    }

    /** 13.5nm での反射率（0〜1） */
    public static double reflectance(int pairs) { double t = Math.tanh(pairs / N0); return R_MAX * t * t; }

    public static double reflectance(ItemStack s) { return reflectance(pairs(s)); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack s = p.getItemInHand(hand);
            double r = reflectance(s);
            p.sendSystemMessage(Component.translatable(K + "spec", pairs(s), String.format(Locale.ROOT, "%.0f", r * 100),
                    String.format(Locale.ROOT, "%.1f", Math.pow(r, 10) * 100)));
        }
        return InteractionResult.SUCCESS;
    }
}
