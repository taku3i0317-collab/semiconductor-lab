package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** 銅ケーブル（v0.2a #1、タスク06 でネットワーク方式）。右クリックでネットワークの様子を表示（テスター代わり） */
public class CableBlock extends Block implements EntityBlock {
    public CableBlock(Properties props) { super(props); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CableBlockEntity(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.COPPER_CABLE ? (BlockEntityTicker<T>) (BlockEntityTicker<CableBlockEntity>) CableBlockEntity::tick : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            CableNetwork net = CableNetwork.get(level, pos);
            if (net != null) player.sendSystemMessage(Component.translatable("message.semiconductor-lab.cable.network",
                    net.cables.size(), net.longestPath, net.lossPercent(), net.sourceCount(), net.sinkCount(), net.batteryCount(),
                    String.format("%.1f kW", net.lastDeliveredW(level) / 1000)));
        }
        return InteractionResult.SUCCESS;
    }

    // 置いた・壊したときにネットワークを組み直す（同じ tick の中でも）
    // 隣に機械が置かれた・外されたときも組み直す
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation o, boolean moved) { super.neighborChanged(state, level, pos, block, o, moved); CableNetwork.invalidate(level); }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) { super.onPlace(state, level, pos, old, moved); CableNetwork.invalidate(level); }
}
