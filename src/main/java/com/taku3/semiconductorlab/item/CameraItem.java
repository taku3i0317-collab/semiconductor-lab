package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.block.SaltPanBlockEntity;
import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * 暗箱カメラ [B-02a]（IMP2、S-002 案B）。もう片方の手に感光した板を持ち、右クリックを押している間だけシャッターが開いている。
 * 離したときの「押していた時間 × 光の強さ」が露光の量。ちょうどよい量（ダゲレオタイプ {@link #DAG_TARGET_TICKS}）の 50〜200％で「ちょうどよい」、
 * 少ないと「うすい」、多すぎると「黒い」。板は「露光した板」になり、場所・時刻・露光の量（PhotoData）が記録される。
 * 光の強さ：昼で空が見える＝1、雨＝0.3、屋根の下・夜＝0（ダゲレオタイプは日光が要った）。
 * [B-02b]（O-008）：ゼラチン乾板（[7-11] の感光乾板）も使える。ちょうどよい露光は 1秒（{@link #DRY_TARGET_TICKS}）で、ネガになる。
 */
public class CameraItem extends Item {
    /** ダゲレオタイプのちょうどよい露光（日なたで 60秒）。⚠️ 本物の 1839年は数分〜15分ほど（ゲームでは短縮） */
    public static final int DAG_TARGET_TICKS = 20 * 60;
    /** ゼラチン乾板 [B-02b] のちょうどよい露光（日なたで 1秒）。⚠️ 本物の 1878年ごろの乾板は 1/25秒ほど（ゲームでは押せる長さに） */
    public static final int DRY_TARGET_TICKS = 20;

    public CameraItem(Properties properties) { super(properties); }

    /** 板の種類ごとのちょうどよい露光（tick）。感光した板でなければ 0 */
    public static int targetTicks(ItemStack plate) {
        if (plate.is(Branches.DAGUERREOTYPE_PLATE)) return DAG_TARGET_TICKS;
        if (plate.is(com.taku3.semiconductorlab.content.Optics.PHOTO_PLATE)) return DRY_TARGET_TICKS;
        return 0;
    }

    /** 光の強さ（0〜1） */
    public static double light(Level level, BlockPos pos) {
        if (!level.isBrightOutside() || !SaltPanBlockEntity.openSky(level, pos)) return 0;
        return level.isRainingAt(pos.above()) ? 0.3 : 1.0;
    }

    /** 露光の量（％）：押していた tick × 光 ÷ ちょうどよい tick */
    public static int exposurePercent(int ticks, double light, int target) { return target <= 0 ? 0 : (int) Math.round(100.0 * ticks * light / target); }

    private static InteractionHand other(InteractionHand h) { return h == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND; }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72_000; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.SPYGLASS; }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (targetTicks(p.getItemInHand(other(hand))) <= 0) {
            if (!level.isClientSide()) p.sendSystemMessage(Component.translatable("item.semiconductor-lab.camera.hold"));
            return InteractionResult.FAIL;
        }
        p.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof Player p)) return false;
        InteractionHand hand = p.getUsedItemHand();
        ItemStack plate = p.getItemInHand(other(hand));
        int target = targetTicks(plate);
        if (target <= 0) return false;
        int ticks = getUseDuration(stack, entity) - timeLeft;
        int exposure = exposurePercent(ticks, light(level, p.blockPosition()), target);
        BlockPos pos = p.blockPosition();
        String biome = level.getBiome(pos).unwrapKey().map(k -> k.identifier().getPath()).orElse("?");
        boolean dry = target == DRY_TARGET_TICKS;
        PhotoData data = new PhotoData(biome + " (" + pos.getX() + ", " + pos.getZ() + ")", level.getOverworldClockTime(), exposure, dry ? "dry_plate" : "daguerreotype");
        ItemStack exposed = new ItemStack(dry ? Branches.EXPOSED_DRY_PLATE : Branches.EXPOSED_PLATE);
        exposed.set(Branches.PHOTO, data);
        plate.shrink(1);
        if (plate.isEmpty()) p.setItemInHand(other(hand), exposed);
        else p.getInventory().placeItemBackInInventory(exposed, net.minecraft.util.Prediction.SERVER_ONLY);   // 入らなければ足元に落ちる
        p.sendSystemMessage(Component.translatable("item.semiconductor-lab.camera.exposed", String.format("%.1f", ticks / 20.0), exposure,
                Component.translatable("item.semiconductor-lab.photo." + data.quality())));
        return true;
    }
}
