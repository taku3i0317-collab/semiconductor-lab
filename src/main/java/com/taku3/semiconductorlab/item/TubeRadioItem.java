package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 真空管ラジオ [15-09]（IMP2 S-010、1920 KDKA の定時放送）。三極真空管で電波の小さな信号を増幅して、スピーカーを鳴らす。
 * 真空管はヒーターで陰極を赤く熱して電子を出すので、電気をたくさん使う（{@value #WATTS} W）。右クリックで {@value #LISTEN_SECONDS} 秒聞くたびに、
 * 持ち物の鉛蓄電池から WATTS×秒 の電気を使う（トランジスタラジオは乾電池1つで何十時間）。
 */
public class TubeRadioItem extends Item {
    public static final double WATTS = 40;
    public static final int LISTEN_SECONDS = 10;
    public static final long LISTEN_J = (long) (WATTS * LISTEN_SECONDS);
    private static final String K = "item.semiconductor-lab.tube_radio.";

    public TubeRadioItem(Properties properties) { super(properties); }

    /** 持ち物の鉛蓄電池から j ジュール取る。足りなければ false */
    public static boolean drawBattery(Player p, long j) {
        if (p.getAbilities().instabuild) return true;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            Long e = s.get(ModDataComponents.BATTERY_ENERGY);
            if (e == null || e < j || s.getCount() != 1) continue;
            if (e - j > 0) s.set(ModDataComponents.BATTERY_ENERGY, e - j); else s.remove(ModDataComponents.BATTERY_ENERGY);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!drawBattery(p, LISTEN_J)) {
            p.sendSystemMessage(Component.translatable(K + "no_power", LISTEN_J));
            return InteractionResult.FAIL;
        }
        p.sendSystemMessage(Component.translatable(K + "listen", String.format(java.util.Locale.ROOT, "%.0f", WATTS), LISTEN_SECONDS, LISTEN_J));
        p.getCooldowns().addCooldown(p.getItemInHand(hand), 20 * LISTEN_SECONDS);
        return InteractionResult.SUCCESS;
    }
}
