package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * ベルトコンベアのベルト [14-22]（IMP4-01、O-029 仮決定の案A）：向き FACING＝物が進む向き（置いた人から遠ざかる向き）。1マスに物1スタック、上に乗って見える。
 * 自分では動かない。動かすのは駆動 {@link ConveyorDriveBlockEntity}。素手で右クリックすると乗っている物を取る
 */
public class ConveyorBeltBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    private final Machine<ConveyorBeltBlockEntity> machine;

    public ConveyorBeltBlock(Properties props, Machine<ConveyorBeltBlockEntity> machine) {
        super(props);
        this.machine = machine;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return machine.create(pos, state); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConveyorBeltBlockEntity be && !be.getItem().isEmpty()) {
            player.getInventory().placeItemBackInInventory(be.getItem().copy(), net.minecraft.util.Prediction.SERVER_ONLY);
            be.setItem(net.minecraft.world.item.ItemStack.EMPTY);
        }
        return InteractionResult.SUCCESS;
    }
}
