package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 質量分析器 [8-33]（1919 アストン、1918 デンプスター。IMP4-01、O-011）：試料の原子をイオンにして飛ばし、電磁石の中で曲げると、
 * 軽いイオンほど大きく曲がる。曲がり方から重さ（質量数）ごとに分けて数えられる。
 * 右クリックで、もう片方の手に持った試料の元素の同位体（重さのちがう仲間）と、その割合をチャットに出す（IUPAC の天然存在比）。
 * イオン注入装置 [9-28] は同じしくみで、入れたいイオン（ホウ素なら ¹¹B）だけを選んでウェハーに届ける。
 */
public class MassSpectrometerItem extends Item {
    private static final String K = "item.semiconductor-lab.mass_spectrometer.";

    public MassSpectrometerItem(Properties properties) { super(properties); }

    /** 試料 → 元素の記号と、同位体の質量数と割合（%）。出典：IUPAC の同位体の天然存在比（2位まで丸め） */
    public record Isotopes(String element, int[] mass, double[] percent) {}

    public static Isotopes of(ItemStack s) {
        if (s.is(Metallurgy.BORON_TRIOXIDE)) return new Isotopes("B", new int[] {10, 11}, new double[] {19.9, 80.1});
        if (s.is(Chemistry.PHOSPHORUS)) return new Isotopes("P", new int[] {31}, new double[] {100});
        if (s.is(Gas.CHLORINE_GAS) || s.is(Gas.HYDROCHLORIC_ACID)) return new Isotopes("Cl", new int[] {35, 37}, new double[] {75.8, 24.2});
        if (s.is(Gas.ARGON_GAS)) return new Isotopes("Ar", new int[] {36, 38, 40}, new double[] {0.33, 0.06, 99.6});
        if (s.is(Gas.HYDROGEN_GAS)) return new Isotopes("H", new int[] {1, 2}, new double[] {99.99, 0.01});
        if (s.is(Metallurgy.POLYSILICON) || s.is(Semiconductor.SILICON_WAFER) || s.is(Semiconductor.SILICON_SINGLE_CRYSTAL))
            return new Isotopes("Si", new int[] {28, 29, 30}, new double[] {92.2, 4.7, 3.1});
        if (s.is(Semiconductor.REFINED_GERMANIUM) || s.is(Metallurgy.RAW_GERMANIUM))
            return new Isotopes("Ge", new int[] {70, 72, 73, 74, 76}, new double[] {20.5, 27.4, 7.8, 36.5, 7.8});
        if (s.is(Items.COPPER_INGOT)) return new Isotopes("Cu", new int[] {63, 65}, new double[] {69.2, 30.8});
        if (s.is(Metallurgy.LEAD_INGOT)) return new Isotopes("Pb", new int[] {204, 206, 207, 208}, new double[] {1.4, 24.1, 22.1, 52.4});
        if (s.is(Items.GOLD_INGOT)) return new Isotopes("Au", new int[] {197}, new double[] {100});
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack other = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            Isotopes iso = of(other);
            if (iso == null) {
                p.sendSystemMessage(Component.translatable(K + (other.isEmpty() ? "no_sample" : "unknown")));
            } else {
                p.sendSystemMessage(Component.translatable(K + "result", iso.element(), iso.mass().length));
                for (int i = 0; i < iso.mass().length; i++)
                    p.sendSystemMessage(Component.translatable(K + "line", iso.mass()[i] + iso.element(), String.format(java.util.Locale.ROOT, "%.2f", iso.percent()[i])));
                if (iso.mass().length == 1) p.sendSystemMessage(Component.translatable(K + "single"));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
