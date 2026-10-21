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

/**
 * 時計（[14-03] マリン・クロノメーター 1761 ハリソン H4、[8-35] クオーツ腕時計 1969）。出港地（グリニッジ＝x=0）の時刻を持ち歩く。
 * 1個ずつ、1日あたりの進み・遅れ（秒）を持つ（はじめて使ったときに決まる）。表示＝本当のグリニッジ時刻＋（合わせてからの日数×進み）。
 * 六分儀 [8-21] で太陽の南中を測るとき、もう片方の手に持つと経度が出る（1時間の差＝15°、1秒の狂い＝15″）。
 */
public class ChronometerItem extends Item {
    /** 1日あたりの狂いの幅（±秒）。⚠️ ゲームの値（H4 は81日で約5秒＝1日 約0.06秒、クオーツは月に ±15秒ほど＝1日 約0.5秒） */
    public final double maxRateSecPerDay;
    private final String key;

    public ChronometerItem(Properties properties, double maxRateSecPerDay, String key) {
        super(properties);
        this.maxRateSecPerDay = maxRateSecPerDay;
        this.key = "item.semiconductor-lab." + key + ".";
    }

    /** まだ合わせていなければ、今のグリニッジ時刻に合わせて、狂いの速さを決める */
    public static void ensure(ItemStack s, Level level) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        if (d != null && d.copyTag().contains("SetTick")) return;
        double max = s.getItem() instanceof ChronometerItem c ? c.maxRateSecPerDay : 1;
        set(s, level.getOverworldClockTime(), (level.getRandom().nextDouble() * 2 - 1) * max);
    }

    public static void set(ItemStack s, long setTick, double rateSecPerDay) {
        CompoundTag t = new CompoundTag();
        t.putLong("SetTick", setTick);
        t.putLong("RateMs", Math.round(rateSecPerDay * 1000));
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    /** 合わせてからの狂い（秒） */
    public static double errorSec(ItemStack s, long now) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        if (d == null) return 0;
        CompoundTag t = d.copyTag();
        double days = (now - t.getLongOr("SetTick", now)) / (double) Navigation.DAY_TICKS;
        return days * t.getLongOr("RateMs", 0) / 1000.0;
    }

    public static double daysSinceSet(ItemStack s, long now) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? 0 : (now - d.copyTag().getLongOr("SetTick", now)) / (double) Navigation.DAY_TICKS;
    }

    /** この時計が tick の時点で示すグリニッジ時刻（時） */
    public static double readingHours(ItemStack s, long tick) { return Navigation.gmtHours(tick) + errorSec(s, tick) / 3600; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack s = p.getItemInHand(hand);
            ensure(s, level);
            long now = level.getOverworldClockTime();
            p.sendSystemMessage(Component.translatable(key + "reading", Navigation.hms(readingHours(s, now)),
                    String.format(java.util.Locale.ROOT, "%.1f", daysSinceSet(s, now))));
            // もう片方の手にも時計：2つの読みの差（どちらがくるっているかは、正しい時刻＝時報がないと分からない）
            ItemStack o = p.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (o.getItem() instanceof ChronometerItem) {
                ensure(o, level);
                p.sendSystemMessage(Component.translatable("item.semiconductor-lab.marine_chronometer.compare", o.getHoverName(),
                        String.format(java.util.Locale.ROOT, "%+.1f", (readingHours(s, now) - readingHours(o, now)) * 3600)));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
