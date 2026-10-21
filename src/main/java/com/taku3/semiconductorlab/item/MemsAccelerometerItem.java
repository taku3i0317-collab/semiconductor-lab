package com.taku3.semiconductorlab.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MEMS 加速度センサー [9-43]（1993 エアバッグ用の量産、2007 スマホの画面の向き。IMP4-01、O-019 仮決定の案A）：
 * シリコンを削って作った小さなおもりがばねでつられ、動くとおもりが遅れて、くし形の電極のすき間（静電容量）が変わる。
 * センサーが感じるのは「重力に逆らって支えられている分」の加速度：立ち止まると上向き 1g、自由に落ちている間は 0g（無重量）、着地の瞬間は大きい。
 * 手に持つと、毎 tick の位置の変化から今の加速度を g（この世界の重力＝1g）で出し、見上げ・見下ろしの角度を「傾き」で出す。
 * ⚠️ この世界の重力（1 tick に 0.08 ブロック/tick）を 1g とした。位置から計算するので、乗り物・はね返りでは大まかになる（mems_accelerometer.placeholder）。
 */
public class MemsAccelerometerItem extends Item {
    /** この世界の重力（ブロック/tick²）。これを 1g とする */
    public static final double GRAVITY = 0.08;
    private static final String K = "item.semiconductor-lab.mems_accelerometer.";
    private static final Map<UUID, Vec3[]> LAST = new ConcurrentHashMap<>();   // [前の位置, 前の速さ]

    public MemsAccelerometerItem(Properties properties) { super(properties); }

    /**
     * 感じる加速度の大きさ（g）：a＝（速さの変化）−（重力の加速度）。重力は下向き −GRAVITY なので、止まっていれば 1、落ちていれば 0
     * @param dv 1 tick の速さの変化（ブロック/tick²）
     */
    public static double gForce(Vec3 dv) {
        return dv.add(0, GRAVITY, 0).length() / GRAVITY;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player p) || (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND)) return;
        Vec3 pos = p.position();
        Vec3[] last = LAST.get(p.getUUID());
        Vec3 v = last == null ? Vec3.ZERO : pos.subtract(last[0]);
        Vec3 dv = last == null ? Vec3.ZERO : v.subtract(last[1]);
        if (p.onGround() && Math.abs(v.y) < 1e-3) dv = new Vec3(dv.x, 0, dv.z);   // 地面に立っている＝縦は支えられている
        LAST.put(p.getUUID(), new Vec3[] {pos, v});
        if (level.getGameTime() % 5 != 0) return;
        double g = gForce(dv);
        p.sendOverlayMessage(Component.translatable(K + "reading", String.format(java.util.Locale.ROOT, "%.2f", g), Math.round(-p.getXRot())));
    }
}
