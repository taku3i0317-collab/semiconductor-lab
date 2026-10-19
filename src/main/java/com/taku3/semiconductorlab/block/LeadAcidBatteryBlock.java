package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.energy.LeadAcidBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/** 鉛蓄電池 [4-07] のブロック。向き FACING（正面＝放電端子、置いた人の方を向く）＋ LIT（放電中）。右クリックで残量をチャットに表示 */
public class LeadAcidBatteryBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public LeadAcidBatteryBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.LIT, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, BlockStateProperties.LIT); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LeadAcidBatteryBlockEntity(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.LEAD_ACID_BATTERY ? (BlockEntityTicker<T>) (BlockEntityTicker<LeadAcidBatteryBlockEntity>) LeadAcidBatteryBlockEntity::tick : null;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LeadAcidBatteryBlockEntity be)
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.lead_acid_battery.status",
                    EnergyStorage.format(be.getStoredJ()), EnergyStorage.format(LeadAcidBatteryBlockEntity.CAPACITY),
                    String.format("%.1f kW", be.getLastChargeWatts() / 1000), String.format("%.1f kW", be.getLastDischargeWatts() / 1000)));
        return InteractionResult.SUCCESS;
    }
}
