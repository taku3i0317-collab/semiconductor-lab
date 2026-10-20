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
 * キログラム原器 [8-23]（IMP2 O-020）。白金の円柱（1799 アルシーヴのキログラム。1889 の国際原器は白金90%・イリジウム10%⚠️）。
 * 1個ずつ、磨き上げたときの小さなずれ（µg）と、表面の汚れでだんだん重くなる速さを持つ（作った日から数える）。
 * 右クリック：もう片方の手にも原器を持つと、天秤で比べた差（µg）。1個だけなら「定義では 1 kg ちょうど」。
 * 原器どうしの差が年とともに広がる（本物：100年で約 50µg）＝どれが正しいのか誰にも分からない → [8-25] ワット天秤・2019 の定義し直しへ。
 */
public class KilogramPrototypeItem extends Item {
    /** 磨き上げたときのずれの幅（±µg）。⚠️ ゲームの値 */
    public static final int FINISH_UG = 50;
    /** 表面の汚れで重くなる速さの上限（µg／ゲーム内の1日）。⚠️ 本物は 100年で数十µg、ゲームでは早回し */
    public static final int MAX_GAIN_UG_PER_DAY = 3;
    public static final long DAY_TICKS = 24_000;
    private static final String K = "item.semiconductor-lab.kilogram_prototype.";

    public KilogramPrototypeItem(Properties properties) { super(properties); }

    /** まだ値がなければ、ずれ・汚れの速さ・作った日を決める */
    public static void ensure(ItemStack s, Level level) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        if (d != null && d.copyTag().contains("OffsetUg")) return;
        CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
        t.putInt("OffsetUg", level.getRandom().nextIntBetweenInclusive(-FINISH_UG, FINISH_UG));
        t.putInt("GainUgPerDay", level.getRandom().nextIntBetweenInclusive(0, MAX_GAIN_UG_PER_DAY));
        t.putLong("MadeDay", level.getGameTime() / DAY_TICKS);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** 値を決めて入れる（テスト・作った直後用） */
    public static void set(ItemStack s, int offsetUg, int gainPerDay, long madeDay) {
        CompoundTag t = new CompoundTag();
        t.putInt("OffsetUg", offsetUg); t.putInt("GainUgPerDay", gainPerDay); t.putLong("MadeDay", madeDay);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** 1 kg からの重さのずれ（µg）＝磨きのずれ＋汚れ（作ってからの日数×速さ） */
    public static long massUg(ItemStack s, long today) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        if (d == null) return 0;
        CompoundTag t = d.copyTag();
        long days = Math.max(0, today - t.getLongOr("MadeDay", today));
        return t.getIntOr("OffsetUg", 0) + days * t.getIntOr("GainUgPerDay", 0);
    }

    public static long ageDays(ItemStack s, long today) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : Math.max(0, today - d.copyTag().getLongOr("MadeDay", today));
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack a = p.getItemInHand(hand), b = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        ensure(a, level);
        long today = level.getGameTime() / DAY_TICKS;
        if (b.getItem() instanceof KilogramPrototypeItem) {
            ensure(b, level);
            long diff = massUg(a, today) - massUg(b, today);
            // 1 kg の 1µg ＝ 10億分の1（ppb）
            p.sendSystemMessage(Component.translatable(K + "compare", String.format(Locale.ROOT, "%+d", diff), Math.abs(diff), ageDays(a, today), ageDays(b, today)));
            p.sendSystemMessage(Component.translatable(K + (Math.abs(diff) < 10 ? "same" : "drift")));
        } else {
            p.sendSystemMessage(Component.translatable(K + "alone", ageDays(a, today)));
        }
        return InteractionResult.SUCCESS;
    }
}
