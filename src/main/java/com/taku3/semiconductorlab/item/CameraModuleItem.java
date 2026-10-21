package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Branches;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * スマホのカメラ [P-17]（IMP2 O-030 仮決定の案A）：小さなレンズ＋赤外線カット膜＋ベイヤーのフィルターを載せた画像センサー（E の [9-42]）＋処理の IC。
 * 右クリックで、その場ですぐ見られるカラーの写真（PhotoData の種類 "digital"）＝乾板（現像・定着が要る）とくらべる「化学の写真 → 電子の写真」。
 * 露出は自動（暗い所でも、明るさ {@value #AUTO_MIN_LIGHT} 以上なら ちょうどよい）。画面に F値・画素・回折の円の大きさを出す。
 */
public class CameraModuleItem extends Item {
    /** レンズの F 値・画素の大きさ（µm）・緑の光（µm）⚠️ 代表値 */
    public static final double F_NUMBER = 2.0, PIXEL_UM = 1.0, GREEN_UM = 0.55;
    public static final int AUTO_MIN_LIGHT = 4;
    private static final String K = "item.semiconductor-lab.camera_module.";

    public CameraModuleItem(Properties properties) { super(properties); }

    /** 回折の円（エアリーの円）の直径 ≒ 2.44·λ·F（µm） */
    public static double airyUm(double lambdaUm, double f) { return 2.44 * lambdaUm * f; }

    /** 自動露出：明るさ 0〜15 → 露光の量（％）。暗すぎると足りない */
    public static int autoExposure(int light) { return light >= AUTO_MIN_LIGHT ? 100 : (int) Math.round(100.0 * light / AUTO_MIN_LIGHT); }

    /** 撮った写真（場所・時刻・露光の量・種類 digital） */
    public static ItemStack shoot(Level level, BlockPos pos) {
        String biome = level.getBiome(pos).unwrapKey().map(k -> k.identifier().getPath()).orElse("?");
        int exposure = autoExposure(level.getMaxLocalRawBrightness(pos));
        ItemStack photo = new ItemStack(Branches.PRINT);
        photo.set(Branches.PHOTO, new PhotoData(biome + " (" + pos.getX() + ", " + pos.getZ() + ")", level.getOverworldClockTime(), exposure, "digital"));
        return photo;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack photo = shoot(level, p.blockPosition());
        PhotoData d = photo.get(Branches.PHOTO);
        p.getInventory().placeItemBackInInventory(photo, net.minecraft.util.Prediction.SERVER_ONLY);
        p.sendSystemMessage(Component.translatable(K + "shot", d.exposure(), Component.translatable("item.semiconductor-lab.photo." + d.quality())));
        p.sendSystemMessage(Component.translatable(K + "optics", String.format(java.util.Locale.ROOT, "%.0f", F_NUMBER), String.format(java.util.Locale.ROOT, "%.1f", PIXEL_UM),
                String.format(java.util.Locale.ROOT, "%.1f", airyUm(GREEN_UM, F_NUMBER))));
        p.getCooldowns().addCooldown(p.getItemInHand(hand), 20);
        return InteractionResult.SUCCESS;
    }
}
