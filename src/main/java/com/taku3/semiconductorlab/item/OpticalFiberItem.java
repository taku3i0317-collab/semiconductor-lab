package com.taku3.semiconductorlab.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Locale;

/**
 * 光ファイバー [15-13]（IMP2 O-033、1966 カオの提案、1970 コーニングの低損失ファイバー）。中心（コア）にゲルマニウムを入れて屈折率を少し上げた石英の糸。
 * 光はまわり（クラッド）との境で全反射して、曲がった道でも進む。減り方（dB/km）は石英の純度で決まる：合成石英 {@value #SYNTHETIC_DB_KM}、天然の珪石を溶かした石英 {@value #NATURAL_DB_KM}。
 * 右クリック：1つ目のブロックで印、2つ目で間の長さ（1ブロック＝1m）だけ光を送ると、何％届くか（10^(−dB/10)）。
 */
public class OpticalFiberItem extends Item {
    public static final int SYNTHETIC_DB_KM = 20, NATURAL_DB_KM = 1000;
    /** コアとクラッドの屈折率（⚠️ 代表値） */
    public static final double N_CORE = 1.462, N_CLAD = 1.457;
    private static final String K = "item.semiconductor-lab.optical_fiber.";

    public OpticalFiberItem(Properties properties) { super(properties); }

    public static int lossDbKm(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? SYNTHETIC_DB_KM : d.copyTag().getIntOr("LossDbKm", SYNTHETIC_DB_KM);
    }

    public static ItemStack withLoss(ItemStack s, int dbKm) {
        CompoundTag t = new CompoundTag();
        t.putInt("LossDbKm", dbKm);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return s;
    }

    /** 長さ m 送ったとき届く割合（0〜1） */
    public static double transmitted(int dbKm, double meters) { return Math.pow(10, -dbKm * meters / 1000.0 / 10.0); }

    /** 全反射の臨界角（度、境の面に立てた線から） */
    public static double criticalAngleDeg() { return Math.toDegrees(Math.asin(N_CLAD / N_CORE)); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player p = ctx.getPlayer();
        if (ctx.getLevel().isClientSide() || p == null) return InteractionResult.SUCCESS;
        ItemStack s = ctx.getItemInHand();
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
        if (!t.contains("LossDbKm")) t.putInt("LossDbKm", lossDbKm(s));
        BlockPos at = ctx.getClickedPos();
        if (!t.contains("Mark")) {
            t.putLong("Mark", at.asLong());
            s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
            p.sendSystemMessage(Component.translatable(K + "mark", String.format(Locale.ROOT, "%.1f", criticalAngleDeg())));
            return InteractionResult.SUCCESS;
        }
        double m = Math.sqrt(BlockPos.of(t.getLongOr("Mark", 0)).distSqr(at));
        t.remove("Mark");
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        int db = t.getIntOr("LossDbKm", SYNTHETIC_DB_KM);
        double r = transmitted(db, m);
        p.sendSystemMessage(Component.translatable(K + "sent", String.format(Locale.ROOT, "%.0f", m), db, String.format(Locale.ROOT, "%.1f", db * m / 1000.0),
                r < 1e-4 ? "≈0" : String.format(Locale.ROOT, "%.2f", r * 100)));
        return InteractionResult.SUCCESS;
    }
}
