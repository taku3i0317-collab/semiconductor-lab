package com.taku3.semiconductorlab.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 羅針盤 [14-02]（IMP2 S-010、1088 沈括『夢渓筆談』）。天然磁石でこすって磁化した鉄の針を、水を張った器に浮かべる（水に浮かべる羅針盤）。
 * 手に持つと、北がどちらかを画面の下に矢印で出す（マイクラの北＝−Z。バニラのコンパスは初期スポーンを指すが、これは北を指す）。
 * 近くに鉄・鋼のブロックがあると針が引かれてくるう（{@value #IRON_RANGE} ブロック以内、近いほど強い）。
 * 鉄の船でこれが問題になり、1801 フリンダースの鉄の棒、1876 ケルビンの鉄の玉で打ち消した。⚠️ 地磁気の偏角（真北とのずれ）は省略
 */
public class CompassItem extends Item {
    public static final int IRON_RANGE = 3;
    /** 鉄1ブロック（距離1）が針を引く強さ（地磁気の水平の力を 1 として、⚠️ ゲームの値） */
    public static final double IRON_PULL = 0.6;
    private static final String K = "item.semiconductor-lab.magnetic_compass.";
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    public CompassItem(Properties properties) { super(properties); }

    /** 鉄や鋼でできたブロックか（ID の名前で見る：バニラの鉄のブロック・鉄格子・金床・レールなど、MOD の鋼の梁など） */
    public static boolean isIron(net.minecraft.world.level.block.state.BlockState s) {
        String path = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
        return path.contains("iron") || path.contains("steel") || path.contains("anvil") || path.endsWith("rail");
    }

    /** 針の向き（北から時計回りの度、0＝北・90＝東）。近くの鉄の向きへ引かれる */
    public static double needleDeg(Level level, BlockPos at) {
        double x = 0, z = -1;   // 地磁気：北（−Z）
        for (BlockPos q : BlockPos.betweenClosed(at.offset(-IRON_RANGE, -IRON_RANGE, -IRON_RANGE), at.offset(IRON_RANGE, IRON_RANGE, IRON_RANGE))) {
            if (!isIron(level.getBlockState(q))) continue;
            double dx = q.getX() - at.getX(), dz = q.getZ() - at.getZ(), d2 = dx * dx + dz * dz + Math.pow(q.getY() - at.getY(), 2);
            double h = Math.sqrt(dx * dx + dz * dz);
            if (d2 == 0 || h == 0) continue;
            x += dx / h * IRON_PULL / d2;
            z += dz / h * IRON_PULL / d2;
        }
        return Math.toDegrees(Math.atan2(x, -z));
    }

    /** 真北からのくるい（度、−180〜180。atan2 がこの範囲で返す） */
    public static double errorDeg(Level level, BlockPos at) { return needleDeg(level, at); }

    /** 向いている方位（北から時計回りの度）。マイクラの yaw は 0＝南・90＝西 */
    public static double headingDeg(float yaw) { return ((yaw + 180) % 360 + 360) % 360; }

    /** 自分から見た北の矢印（↑＝前） */
    public static String arrow(double needleDeg, float yaw) {
        double rel = ((needleDeg - headingDeg(yaw)) % 360 + 360) % 360;
        return ARROWS[(int) Math.round(rel / 45) % 8];
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player p) || level.getGameTime() % 10 != 0) return;
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) return;
        double n = needleDeg(level, p.blockPosition()), err = n;
        String a = arrow(n, p.getYRot());
        p.sendOverlayMessage(Math.abs(err) >= 5
                ? Component.translatable(K + "disturbed", a, String.format(java.util.Locale.ROOT, "%.0f", Math.abs(err)))
                : Component.translatable(K + "north", a));
    }
}
