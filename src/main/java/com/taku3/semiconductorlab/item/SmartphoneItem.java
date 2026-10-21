package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * スマホ（[B-10]、v0.5 のゴール、O-029 Q28 案A 仮決定：アプリ3つ＝電卓・写真・傾き、使うと電池が減る）。
 * 右クリックでホーム画面（自作のドット絵のアイコン、S-019 の5）→ アイコンを押すとそのアプリ。しゃがんで右クリックで今のアプリをすぐ動かす。
 * 電卓＝今の電卓の画面、写真＝カメラモジュールで撮ってすぐ見られる写真（CameraModuleItem.shoot）、傾き＝MEMS の加速度計：重力の向きから前後の傾き（チャット）。
 * 電池：中のリチウムイオン電池（{@link #CAPACITY_J}、データコンポーネント battery_energy、ないときは満タン）。アプリごとに使う電気が決まり、足りないと「電池切れ」。
 * リチウムイオン電池のブロックに使うと充電（1回にそのブロックが出せる分だけ）
 */
public class SmartphoneItem extends Item {
    /** 電池の量（J）：3.7 V×3 Ah ≒ 40 kJ（本物のスマホくらい） */
    public static final long CAPACITY_J = 40_000;
    /** アプリ：0＝電卓・1＝写真・2＝傾き。1回に使う電気（J、ゲームの値 ⚠️：写真はセンサーと処理で多め） */
    public static final String[] APPS = {"calculator", "photo", "tilt"};
    public static final long[] COST_J = {200, 2_000, 100};
    /** 電卓のアプリの画面を開く（クライアントの初期化で入れる） */
    public static Runnable openCalculator = () -> {};

    static { com.taku3.semiconductorlab.energy.RechargeableBatteryBlockEntity.registerCapacity("semiconductor-lab:smartphone", CAPACITY_J); }

    public SmartphoneItem(Properties p) { super(p); }

    public static long energy(ItemStack s) { Long e = s.get(ModDataComponents.BATTERY_ENERGY); return e == null ? CAPACITY_J : e; }
    public static int app(ItemStack s) { Integer a = s.get(ModDataComponents.PHONE_APP); return a == null ? 0 : Math.floorMod(a, APPS.length); }
    /** 次のアプリへ */
    public static int nextApp(ItemStack s) { int a = (app(s) + 1) % APPS.length; s.set(ModDataComponents.PHONE_APP, a); return a; }
    /** 今のアプリの分の電気を使う。足りなければ false（減らさない） */
    public static boolean drain(ItemStack s) {
        long e = energy(s), c = COST_J[app(s)];
        if (e < c) return false;
        s.set(ModDataComponents.BATTERY_ENERGY, e - c);
        return true;
    }
    /** 電池のブロックから充電：足りない分を、そのブロックが1回に出せるだけ受け取る。受け取った量（J） */
    public static long charge(ItemStack s, com.taku3.semiconductorlab.energy.RechargeableBatteryBlockEntity be) {
        long need = CAPACITY_J - energy(s);
        if (need <= 0) return 0;
        long got = be.storageFor(net.minecraft.core.Direction.UP).extract(need, false);
        if (got > 0) s.set(ModDataComponents.BATTERY_ENERGY, energy(s) + got);
        return got;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        ItemStack s = p.getItemInHand(hand);
        if (!p.isShiftKeyDown()) {   // ホーム画面（アイコンが並ぶ）
            if (!level.isClientSide()) p.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, pl) -> new PhoneMenu(id, inv), Component.translatable("item.semiconductor-lab.smartphone")));
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            if (app(s) == 0 && energy(s) >= COST_J[0]) openCalculator.run();
            return InteractionResult.SUCCESS;
        }
        runApp(level, p, s);
        return InteractionResult.SUCCESS;
    }

    /** サーバーで今のアプリを1回動かす（電気を使い、写真・傾きはチャットへ。電卓の画面はクライアントが開く） */
    public static void runApp(Level level, Player p, ItemStack s) {
        int a = app(s);
        if (!drain(s)) { p.sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.empty")); return; }
        switch (a) {
            case 1 -> {   // 写真：カメラモジュールと同じ（その場ですぐ見られる電子の写真）
                ItemStack photo = CameraModuleItem.shoot(level, p.blockPosition());
                p.getInventory().placeItemBackInInventory(photo, net.minecraft.util.Prediction.SERVER_ONLY);
                p.sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.photo"));
            }
            case 2 -> p.sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.tilt", Math.round(-p.getXRot())));   // 上を向くと＋
            default -> {}
        }
        p.sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.battery", Math.round(100.0 * energy(s) / CAPACITY_J)));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof com.taku3.semiconductorlab.energy.RechargeableBatteryBlockEntity be)) return InteractionResult.PASS;
        if (!ctx.getLevel().isClientSide() && ctx.getPlayer() != null) {
            long got = charge(ctx.getItemInHand(), be);
            ctx.getPlayer().sendSystemMessage(Component.translatable("message.semiconductor-lab.smartphone.charged", got, Math.round(100.0 * energy(ctx.getItemInHand()) / CAPACITY_J)));
        }
        return InteractionResult.SUCCESS;
    }
}
