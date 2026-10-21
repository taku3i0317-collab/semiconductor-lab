package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 機械式腕時計（[P-38]、IMP2 S-011）：ぜんまい [8-44] の力を輪列 [8-43] で伝え、レバー脱進機 [8-46] がてんぷ [8-45] の往復で少しずつ放す。
 * 軸受は人工ルビー [8-47]。ChronometerItem と同じく1個ずつ1日の狂いを持つ（±{@value #RATE} 秒⚠️）。
 * <ul>
 * <li>右クリック：ぜんまいを巻く（巻いたときに時刻も合わせる）。巻いてから {@value #RESERVE_TICKS} tick（⚠️ ゲームの2日、本物は約40時間）で止まる</li>
 * <li>手に持つと、画面の下に時刻とチクタク、ぜんまいの残り。止まっていると「止まっている」</li>
 * <li>自動巻き [8-48]（automatic=true）：持ち物に入れて歩くと、ローターが回ってぜんまいが巻かれる</li>
 * </ul>
 */
public class MechanicalWatchItem extends ChronometerItem {
    public static final double RATE = 10;
    public static final long RESERVE_TICKS = 48_000;
    private final boolean automatic;
    private static final String K = "item.semiconductor-lab.mechanical_watch.";

    public MechanicalWatchItem(Properties properties, boolean automatic) { super(properties, RATE, "mechanical_watch"); this.automatic = automatic; }

    public boolean automatic() { return automatic; }

    public static long woundAt(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? Long.MIN_VALUE / 4 : d.copyTag().getLongOr("WoundAt", Long.MIN_VALUE / 4);
    }

    /** 巻く：巻いた時刻と、時刻合わせ（狂いの速さはそのまま） */
    public static void wind(ItemStack s, long now, double rateSecPerDay) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
        if (!t.contains("RateMs")) t.putLong("RateMs", Math.round(rateSecPerDay * 1000));
        if (now - woundAtTag(t) > RESERVE_TICKS || !t.contains("SetTick")) t.putLong("SetTick", now);   // 止まっていたら時刻を合わせ直す
        t.putLong("WoundAt", now);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    private static long woundAtTag(CompoundTag t) { return t.getLongOr("WoundAt", Long.MIN_VALUE / 4); }

    public static boolean running(ItemStack s, long now) { return now - woundAt(s) <= RESERVE_TICKS; }

    /** ぜんまいの残り（時間、ゲームの1日＝24時間とした表示） */
    public static double reserveHours(ItemStack s, long now) { return Math.max(0, RESERVE_TICKS - (now - woundAt(s))) / 1000.0; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack s = p.getItemInHand(hand);
        double rate = (level.getRandom().nextDouble() * 2 - 1) * RATE;
        wind(s, level.getOverworldClockTime(), rate);
        p.sendSystemMessage(Component.translatable(K + "wound", String.format(java.util.Locale.ROOT, "%.0f", RESERVE_TICKS / 1000.0)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player p) || level.getGameTime() % 20 != 0) return;
        long now = level.getOverworldClockTime();
        // [8-48] 自動巻き：歩いているとローターが回って巻かれる（ゲームの値：1秒歩くと少し巻ける⚠️）
        if (automatic && p.getDeltaMovement().horizontalDistanceSqr() > 1e-4 && woundAt(stack) > Long.MIN_VALUE / 8)
            wind(stack, Math.min(now, woundAt(stack) + 600), 0);
        boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        if (!held) return;
        if (running(stack, now))
            p.sendOverlayMessage(Component.translatable(K + "tick", Navigation.hms(readingHours(stack, now)),
                    String.format(java.util.Locale.ROOT, "%.0f", reserveHours(stack, now))));
        else p.sendOverlayMessage(Component.translatable(K + "stopped"));
    }
}
