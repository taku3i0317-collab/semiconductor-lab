package com.taku3.semiconductorlab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 手押しピストンポンプ [5-01]（オットー・フォン・ゲーリケ 1650頃）。
 * 隣に真空容器を置き、素手で右クリックするたびにピストンを1往復：容器の空気の 2割をシリンダーに吸い出して捨てる。
 * シリンダーのすき間（死容積）と革パッキンの漏れのせいで、約 1kPa（大気の 1/100）より下には下がらない。
 * 電気は使わない（人力）。
 */
public class HandVacuumPumpBlock extends Block {
    /** 到達圧力（Pa）。手押しポンプの限界 */
    public static final double LIMIT_PA = 1_000;
    /** 1回で残る割合 V/(V+V_c)。容器:シリンダー ＝ 4:1 */
    public static final double STROKE_RATIO = 0.8;

    public HandVacuumPumpBlock(Properties props) { super(props); }

    /**
     * pos のポンプを1回引く（テストからも呼ぶ）。隣に真空容器がなければ null。
     * @return 引いた真空容器
     */
    public static VacuumChamberBlockEntity stroke(Level level, BlockPos pos) {
        VacuumChamberBlockEntity c = VacuumChamberBlockEntity.findAdjacent(level, pos);
        if (c != null) c.pumpStroke(LIMIT_PA, STROKE_RATIO);
        return c;
    }

    /** 手応え（気圧の差が大きいほどピストンを引くのが重い）。圧力の数値は真空計で */
    public static String feelKey(double pa) {
        if (pa > 50_000) return "light";
        if (pa > 5_000) return "heavy";
        if (pa > LIMIT_PA * 1.1) return "very_heavy";
        return "limit";
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        VacuumChamberBlockEntity c = stroke(level, pos);
        if (c == null) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hand_vacuum_pump.no_chamber"));
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.4f, 1.6f);
        player.causeFoodExhaustion(0.05f);   // 人力なのでお腹が減る
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.hand_vacuum_pump." + feelKey(c.getPressure())));
        return InteractionResult.SUCCESS;
    }
}
