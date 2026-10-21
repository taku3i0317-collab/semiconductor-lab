package com.taku3.semiconductorlab.item;

import net.minecraft.core.BlockPos;
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

/**
 * 六分儀 [8-21]（IMP2 O-023、1731 ハドリーの八分儀 → 1757 六分儀 ⚠️）。2枚の鏡で太陽の像を水平線に重ね、太陽の高さを副尺で 1′ まで読む。
 * <b>正午の測り方（ヌーン・サイト）</b>：昼前から何回か測ると、太陽の高さは上がり、南中で一番高くなり、そのあと下がり始める。
 * 下がり始めたら、一番高かった角度から 緯度 ＝ 90° − 南中高度（季節なしの近似 ⚠️）。
 * そのとき、もう片方の手にクロノメーター [14-03]・クオーツ腕時計 [8-35] を持っていると、南中したときのグリニッジ時刻から 経度 ＝ (12時 − その時刻)×15°。
 * 空が見えない・雨・夜・オーバーワールドの外では測れない。
 */
public class SextantItem extends Item {
    /** 続けて測ったとみなす間（tick）。これより空くと最初からやり直し */
    public static final long SERIES_TICKS = 3_000;
    private static final String K = "item.semiconductor-lab.sextant.";

    public SextantItem(Properties properties) { super(properties); }

    /** 副尺で読んだ高さ（分、1′ 単位） */
    public static int readMinutes(double altDeg) { return (int) Math.round(altDeg * 60); }

    /** 南中の高さ（分）から緯度（度、北が正）。北半球なら太陽は南に見える */
    public static double latitudeFromNoon(int maxMinutes, boolean sunInSouth) { double lat = 90 - maxMinutes / 60.0; return sunInSouth ? lat : -lat; }

    /** 南中したときのグリニッジ時刻（時）から経度（度、東が正） */
    public static double longitudeFromNoon(double gmtAtNoon) {
        double d = (12 - gmtAtNoon) * 15;
        return ((d + 180) % 360 + 360) % 360 - 180;
    }

    /**
     * 1回測る。結果の段：0＝はじめ、1＝上がっている、2＝南中を過ぎた（緯度・経度が出る）、3＝下がっている。
     * clock は もう片方の手の時計（なければ EMPTY）。メッセージを p に送る（p が null なら送らない）
     */
    public static int sight(ItemStack sextant, Level level, BlockPos at, long now, ItemStack clock, Player p) {
        double alt = Navigation.sunAltitudeDeg(Navigation.latitudeDeg(at), Navigation.localHours(Navigation.gmtHours(now), Navigation.longitudeDeg(at)));
        int m = readMinutes(alt);
        if (p != null) p.sendSystemMessage(Component.translatable(K + "altitude", Navigation.dm(m / 60.0)));
        CustomData d = sextant.get(DataComponents.CUSTOM_DATA);
        CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
        boolean series = t.contains("PrevTick") && now - t.getLongOr("PrevTick", 0) <= SERIES_TICKS && now > t.getLongOr("PrevTick", 0);
        int prev = t.getIntOr("PrevMin", 0), stage;
        if (!series) {
            stage = 0;
            t.putBoolean("Rising", false); t.putInt("MaxMin", m); t.putLong("MaxTick", now);
        } else if (m > prev) {
            stage = 1;
            t.putBoolean("Rising", true); t.putInt("MaxMin", m); t.putLong("MaxTick", now);
        } else if (t.getBooleanOr("Rising", false)) {
            stage = 2;
            int max = t.getIntOr("MaxMin", m);
            long maxTick = t.getLongOr("MaxTick", now);
            double lat = latitudeFromNoon(max, Navigation.latitudeDeg(at) >= 0);
            if (p != null) p.sendSystemMessage(Component.translatable(K + "noon", Navigation.dm(max / 60.0), Navigation.dm(Math.abs(lat)),
                    Component.translatable(K + (lat >= 0 ? "north" : "south"))));
            if (!clock.isEmpty() && clock.getItem() instanceof ChronometerItem) {
                ChronometerItem.ensure(clock, level);
                double gmt = ChronometerItem.readingHours(clock, maxTick), lon = longitudeFromNoon(gmt);
                if (p != null) p.sendSystemMessage(Component.translatable(K + "longitude", Navigation.hms(gmt), Navigation.dm(Math.abs(lon)),
                        Component.translatable(K + (lon >= 0 ? "east" : "west"))));
            } else if (p != null) p.sendSystemMessage(Component.translatable(K + "need_clock"));
            t.putBoolean("Rising", false);
        } else {
            stage = 3;
            if (p != null) p.sendSystemMessage(Component.translatable(K + "falling"));
        }
        if (stage == 0 && p != null) p.sendSystemMessage(Component.translatable(K + "again"));
        if (stage == 1 && p != null) p.sendSystemMessage(Component.translatable(K + "rising"));
        t.putInt("PrevMin", m); t.putLong("PrevTick", now);
        sextant.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return stage;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos at = p.blockPosition();
        if (level.dimension() != Level.OVERWORLD || !level.canSeeSky(at.above()) || level.isRaining()) {
            p.sendSystemMessage(Component.translatable(K + "no_sky"));
            return InteractionResult.SUCCESS;
        }
        if (Navigation.sunAltitudeDeg(level, at) <= 0) {
            p.sendSystemMessage(Component.translatable(K + "night"));
            return InteractionResult.SUCCESS;
        }
        ItemStack clock = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        sight(p.getItemInHand(hand), level, at, level.getOverworldClockTime(), clock, p);
        return InteractionResult.SUCCESS;
    }
}
