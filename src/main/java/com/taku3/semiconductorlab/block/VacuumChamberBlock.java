package com.taku3.semiconductorlab.block;

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

/** 真空容器 [5-11]（ガラスのベルジャー）。素手で右クリックしても圧力は分からない（真空計 [5-10] で測る） */
public class VacuumChamberBlock extends Block implements EntityBlock {
    public VacuumChamberBlock(Properties props) { super(props); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new VacuumChamberBlockEntity(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.VACUUM_CHAMBER
                ? (BlockEntityTicker<T>) (BlockEntityTicker<VacuumChamberBlockEntity>) VacuumChamberBlockEntity::tick : null;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.vacuum_chamber.need_gauge"));
        return InteractionResult.SUCCESS;
    }
}
