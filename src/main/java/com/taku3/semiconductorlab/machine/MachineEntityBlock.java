package com.taku3.semiconductorlab.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 向きを持たない機械ブロックの共通クラス（ガスホルダー・真空容器・塩田・ケーブル・発電機など、IMP-28）。
 * BE を作る・tick は {@link Machine} から。画面がある BE なら右クリックで開く。右クリックで別のことをするブロックは useWithoutItem を上書きする。
 */
public class MachineEntityBlock extends Block implements EntityBlock {
    protected final Machine<?> machine;

    public MachineEntityBlock(Properties props, Machine<?> machine) {
        super(props);
        this.machine = machine;
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return machine.create(pos, state); }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return machine.tickerFor(type);
    }

    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider mp ? mp : null;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider mp) player.openMenu(mp);
        return InteractionResult.SUCCESS;
    }
}
