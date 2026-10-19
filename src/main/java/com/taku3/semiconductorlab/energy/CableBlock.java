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

/** 銅ケーブル（v0.2a #1）。右クリックで蓄積量を表示（テスター代わり） */
public class CableBlock extends Block implements EntityBlock {
    public CableBlock(Properties props) { super(props); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CableBlockEntity(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.COPPER_CABLE ? (BlockEntityTicker<T>) (BlockEntityTicker<CableBlockEntity>) CableBlockEntity::tick : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CableBlockEntity be) {
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.cable.stored", EnergyStorage.format(be.getStorage().getStoredJ()), EnergyStorage.format(CableBlockEntity.CAPACITY)));
        }
        return InteractionResult.SUCCESS;
    }
}