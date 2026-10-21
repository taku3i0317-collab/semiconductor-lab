package com.taku3.semiconductorlab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 水銀柱の気圧計 [5-03]（1643 トリチェリ、1648 ペリエがピュイ・ド・ドーム山で高いほど低いことを確かめた。IMP4-01、S-009）：
 * 水銀を満たしたガラス管を逆さに立てると、水銀は約 760mm で止まり、上はトリチェリの真空になる。高い所ほど空気が薄く、水銀柱は低くなる。
 * 右クリックで、置いた高さ（Y）での水銀柱の高さ（mmHg）と気圧（hPa）を出す。海面＝Y 63 で 760mmHg、約 8.4km（8400 ブロック ⚠️）で 1/e になる近似。
 * ⚠️ 天気による変化・温度は省略。水銀の蒸気は毒（mercury_barometer.placeholder）。
 */
public class MercuryBarometerBlock extends Block {
    public static final int SEA_LEVEL_Y = 63;
    public static final double SCALE_HEIGHT = 8400;

    public MercuryBarometerBlock(Properties p) { super(p); }

    /** 高さ y での水銀柱の高さ（mmHg） */
    public static double mmHg(int y) { return 760.0 * Math.exp(-(y - SEA_LEVEL_Y) / SCALE_HEIGHT); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            double mm = mmHg(pos.getY());
            player.sendOverlayMessage(Component.translatable("block.semiconductor-lab.mercury_barometer.reading",
                    String.format(java.util.Locale.ROOT, "%.1f", mm), String.format(java.util.Locale.ROOT, "%.1f", mm * 101325.0 / 760.0 / 100.0), pos.getY()));
        }
        return InteractionResult.SUCCESS;
    }
}
