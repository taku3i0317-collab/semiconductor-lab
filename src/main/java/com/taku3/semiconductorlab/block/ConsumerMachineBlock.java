package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 電気を使う機械ブロックの共通クラス（電解槽と同じ形：向き FACING ＋ 点灯 LIT、右クリックで画面）。
 * 水素炉 [1-28]・圧縮機 [6-03] から使う。BE は ConsumerBlockEntity を継承し MenuProvider を実装すること。
 */
public class ConsumerMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final BiFunction<BlockPos, BlockState, ? extends ConsumerBlockEntity> factory;
    private final Supplier<BlockEntityType<?>> typeRef;

    public ConsumerMachineBlock(Properties props, BiFunction<BlockPos, BlockState, ? extends ConsumerBlockEntity> factory, Supplier<BlockEntityType<?>> type) {
        super(props);
        this.factory = factory;
        this.typeRef = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return factory.apply(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == typeRef.get() ? (BlockEntityTicker<T>) (BlockEntityTicker<ConsumerBlockEntity>) ConsumerBlockEntity::tick : null;
    }

    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider mp ? mp : null;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider mp) player.openMenu(mp);
        return InteractionResult.SUCCESS;
    }
}
