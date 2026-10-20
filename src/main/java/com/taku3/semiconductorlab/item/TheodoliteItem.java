package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Construction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * セオドライト [12-11] 三角測量（IMP2 O-007）。望遠鏡（[7-04]）に、副尺（[8-04]）で 1分（1/60°）まで読める水平の目盛り円を付けた測量器。
 * 右クリックを押している間のぞき（TelescopeItem と同じ）、離したときの向きを読む。
 * <ol>
 * <li>測点A で目標をのぞいて離す → A の場所と向きを記録</li>
 * <li>横に歩いて（底辺）、測点B で同じ目標をのぞいて離す → 底辺の長さと、A・B の2つの角から、正弦定理で目標までの距離を計算する</li>
 * </ol>
 * のぞいた先にブロックがあれば「実際の距離」も出して、ねらいのずれ（誤差）が分かる。スニーク＋右クリックで測点A を消す。
 * ⚠️ 底辺の長さは本物では測鎖（くさり）や物差しで地面を測る。ゲームでは2つの測点の間の長さをそのまま使う（省略）。
 */
public class TheodoliteItem extends TelescopeItem {
    /** 目盛りの読み：1分＝1/60° */
    public static final double READ_DEG = 1.0 / 60.0;
    /** 底辺がこれより短いと測れない（m） */
    public static final double MIN_BASE = 3.0;
    private static final String K = "item.semiconductor-lab.theodolite.";

    public TheodoliteItem(Properties properties) { super(properties); }

    /** 三角測量の結果。base＝底辺、alpha・beta＝A・B の角（度）、distA・distB＝A・B から目標まで（m）、tx・tz＝目標の場所 */
    public record Fix(double base, double alpha, double beta, double distA, double distB, double tx, double tz) {}

    /** yaw（Minecraft の向き、度）を目盛りの 1分に丸める */
    public static float read(float yaw) { return (float) (Math.round(yaw / READ_DEG) * READ_DEG); }

    /** 方位（北＝0°、東＝90°、時計回り） */
    public static double bearing(float yaw) { return Math.floorMod(Math.round((yaw + 180.0) / READ_DEG), Math.round(360 / READ_DEG)) * READ_DEG; }

    /** 2つの測点の場所と向きから目標を決める（正弦定理）。2本の視線が前で交わらないときは null */
    public static Fix triangulate(double ax, double az, float yawA, double bx, double bz, float yawB) {
        double ux = -Math.sin(Math.toRadians(yawA)), uz = Math.cos(Math.toRadians(yawA));
        double vx = -Math.sin(Math.toRadians(yawB)), vz = Math.cos(Math.toRadians(yawB));
        double abx = bx - ax, abz = bz - az, base = Math.hypot(abx, abz);
        if (base < 1e-6) return null;
        // 目標が底辺の同じ側にあること（2本の視線が底辺の同じ側を向く）
        double sideA = abx * uz - abz * ux, sideB = abx * vz - abz * vx;
        if (sideA * sideB <= 0) return null;
        double alpha = Math.toDegrees(Math.acos(clamp((ux * abx + uz * abz) / base)));
        double beta = Math.toDegrees(Math.acos(clamp((-vx * abx - vz * abz) / base)));
        double gamma = 180 - alpha - beta;   // 目標の角
        if (alpha < 0.5 || beta < 0.5 || gamma < 0.5) return null;
        double s = Math.sin(Math.toRadians(gamma));
        double distA = base * Math.sin(Math.toRadians(beta)) / s;
        double distB = base * Math.sin(Math.toRadians(alpha)) / s;
        return new Fix(base, alpha, beta, distA, distB, ax + distA * ux, az + distA * uz);
    }

    private static double clamp(double c) { return Math.max(-1, Math.min(1, c)); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (p.isShiftKeyDown() && stack.has(Construction.SURVEY_STATION)) {
            stack.remove(Construction.SURVEY_STATION);
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable(K + "cleared"));
            return InteractionResult.SUCCESS;
        }
        return super.use(level, p, hand);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
        if (!level.isClientSide() && entity instanceof Player p) sight(stack, level, p);
        return true;
    }

    /** のぞいた向きを読んで、測点A を記録するか、測点B として距離を出す */
    public static void sight(ItemStack stack, Level level, Player p) {
        float yaw = read(p.getYRot());
        SurveyStation a = stack.get(Construction.SURVEY_STATION);
        if (a == null) {
            stack.set(Construction.SURVEY_STATION, new SurveyStation(p.getX(), p.getZ(), yaw));
            p.sendSystemMessage(Component.translatable(K + "station_a", String.format("%.2f", bearing(yaw))));
            return;
        }
        double base = Math.hypot(p.getX() - a.x(), p.getZ() - a.z());
        if (base < MIN_BASE) {
            p.sendSystemMessage(Component.translatable(K + "short_base", String.format("%.1f", base), String.format("%.0f", MIN_BASE)));
            return;
        }
        Fix f = triangulate(a.x(), a.z(), a.yaw(), p.getX(), p.getZ(), yaw);
        if (f == null) {
            p.sendSystemMessage(Component.translatable(K + "no_cross"));
            return;
        }
        stack.remove(Construction.SURVEY_STATION);
        p.sendSystemMessage(Component.translatable(K + "result", String.format("%.1f", f.base()), String.format("%.2f", f.alpha()),
                String.format("%.2f", f.beta()), String.format("%.1f", f.distB()), Math.round(f.tx()), Math.round(f.tz())));
        // のぞいた先のブロック（あれば）までの実際の水平距離
        Vec3 eye = p.getEyePosition();
        var hit = level.clip(new ClipContext(eye, eye.add(p.getViewVector(1f).scale(256)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        if (hit.getType() == HitResult.Type.BLOCK) {
            double real = Math.hypot(hit.getLocation().x - p.getX(), hit.getLocation().z - p.getZ());
            p.sendSystemMessage(Component.translatable(K + "real", String.format("%.1f", real), String.format("%.1f", f.distB() - real)));
        }
    }
}
