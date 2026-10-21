package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 画像センサーの素子 [9-42]（1969 CCD〜1993 CMOS センサー。IMP4-01、O-019 仮決定の案A）：
 * フォトダイオード（p-n 接合）に光が当たると電子が生まれ、画素の中にたまる。露光の時間のあと、画素ごとのトランジスタ（CMOS）で数を読み出す。
 * 手に持つと、その場の明るさ（0〜15）から「1画素にたまった電子の数」を画面の下に出す。明るいほど多く、満杯（{@link #FULL_WELL} 個）で白飛び。
 * 光の粒は不規則に来るので、数は ±√N ほどばらつく（ショットノイズ）。真っ暗でも熱で少し電子が出る（暗電流 {@link #DARK}）。
 * ⚠️ 明るさ→電子の数の換算・満杯の数・暗電流はゲームの値（image_sensor.placeholder）。レッドストーンは使わない（DESIGN）。
 */
public class ImageSensorItem extends Item {
    /** 1画素にためられる電子の数（スマホの小さい画素で数千個 ⚠️） */
    public static final int FULL_WELL = 6_000;
    /** 真っ暗でも熱で出る電子（露光1回ぶん ⚠️） */
    public static final int DARK = 5;
    private static final String K = "item.semiconductor-lab.image_sensor.";

    public ImageSensorItem(Properties properties) { super(properties); }

    /** 明るさ 0〜15 → 平均の電子の数（明るさの段は対数に近いので2乗で近似 ⚠️） */
    public static int meanElectrons(int light) {
        int l = Math.max(0, Math.min(15, light));
        return DARK + (int) Math.round((FULL_WELL - DARK) * (l / 15.0) * (l / 15.0));
    }

    /** ショットノイズ（±√N のばらつき）を入れた1回の読み出し。満杯をこえない */
    public static int read(int light, RandomSource r) {
        int n = meanElectrons(light);
        return Math.max(0, Math.min(FULL_WELL, (int) Math.round(n + Math.sqrt(n) * r.nextGaussian())));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player p) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)) return;
        if (level.getGameTime() % 10 != 0) return;
        int light = level.getMaxLocalRawBrightness(p.blockPosition());
        int n = read(light, level.getRandom());
        String key = n >= FULL_WELL ? "full" : light == 0 ? "dark" : "reading";
        p.sendOverlayMessage(Component.translatable(K + key, n, (int) Math.round(Math.sqrt(meanElectrons(light))), light));
    }
}
