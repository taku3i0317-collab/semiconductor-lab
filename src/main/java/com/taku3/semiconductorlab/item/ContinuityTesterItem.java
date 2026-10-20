package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 導通テスター [2-55]（乾電池＋豆電球＋2本の線）。利き手に持ち、もう片方の手に調べたい物を持って右クリック →
 * 電気の通りやすさ（電気伝導率 S/cm、だいたいの値）と、豆電球がつくか（{@link #LAMP_S_PER_CM} 以上）をメッセージで出す。
 * 金属・黒鉛はつく、プラスチック・ガラスはつかない。ポリアセチレンはヨウ素を加える（ドーピング）とつくようになる。
 */
public class ContinuityTesterItem extends Item {
    /** 豆電球がつく目安（S/cm）。ゲームの値 */
    public static final double LAMP_S_PER_CM = 1.0;

    /** だいたいの電気伝導率（S/cm、室温）。表にない物は「はかれない」 */
    private static final Map<Supplier<Item>, Double> TABLE = new LinkedHashMap<>();
    static {
        TABLE.put(() -> Items.COPPER_INGOT, 6e5);
        TABLE.put(() -> Items.GOLD_INGOT, 4.5e5);
        TABLE.put(() -> Items.IRON_INGOT, 1e5);
        TABLE.put(() -> Metallurgy.GRAPHITE, 1e3);
        TABLE.put(() -> Chemistry.DOPED_POLYACETYLENE, 30.0);
        TABLE.put(() -> Chemistry.POLYACETYLENE_FILM, 1e-5);
        TABLE.put(() -> Chemistry.ULTRAPURE_WATER, 5.5e-8);
        TABLE.put(() -> Chemistry.SODA_LIME_GLASS, 1e-13);
        TABLE.put(() -> Chemistry.POLYETHYLENE, 1e-16);
        TABLE.put(() -> Chemistry.HDPE, 1e-16);
        TABLE.put(() -> Chemistry.PTFE, 1e-18);
    }

    public ContinuityTesterItem(Properties properties) { super(properties); }

    /** その物の電気伝導率（S/cm）。表にないときは -1 */
    public static double conductivity(ItemStack s) {
        for (var e : TABLE.entrySet()) if (s.is(e.getKey().get())) return e.getValue();
        return -1;
    }

    /** 豆電球がつくか */
    public static boolean lampOn(ItemStack s) { return conductivity(s) >= LAMP_S_PER_CM; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack sample = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        double c = conductivity(sample);
        if (sample.isEmpty()) p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.continuity_tester.empty"));
        else if (c < 0) p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.continuity_tester.unknown", sample.getHoverName()));
        else p.sendOverlayMessage(Component.translatable(lampOn(sample) ? "message.semiconductor-lab.continuity_tester.on" : "message.semiconductor-lab.continuity_tester.off",
                sample.getHoverName(), String.format("%.1e S/cm", c)));
        return InteractionResult.SUCCESS;
    }
}
