package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

/** 帯域精製炉（Pfann 1952）。#6：右クリックで入れる／取り出す（#7 で画面に置き換え） */
public class ZoneRefiningFurnaceBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ZoneRefiningFurnaceBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZoneRefiningFurnaceBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.ZONE_REFINING_FURNACE
                ? (BlockEntityTicker<T>) (BlockEntityTicker<ZoneRefiningFurnaceBlockEntity>) ZoneRefiningFurnaceBlockEntity::tick
                : null;
    }

    /** 手にアイテムを持って右クリック：ゲルマニウムなら入れる。違えば理由を表示 */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ZoneRefiningFurnaceBlockEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!be.getItem().isEmpty()) return takeOut(level, pos, be);
        if (!ZoneRefiningFurnaceBlockEntity.isGermanium(stack)) {
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.refiner.not_germanium"));
            return InteractionResult.FAIL;
        }
        if (ZoneRefiningFurnaceBlockEntity.nextPurity(stack) < 0) {
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.refiner.max_purity"));
            return InteractionResult.FAIL;
        }
        ItemStack in = stack.copy();
        if (!player.hasInfiniteMaterials()) stack.setCount(0);
        be.setItem(in);
        return InteractionResult.SUCCESS;
    }

    /** 素手で右クリック：中身を取り出す */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ZoneRefiningFurnaceBlockEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (be.getItem().isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.refiner.empty"));
            return InteractionResult.SUCCESS;
        }
        return takeOut(level, pos, be);
    }

    private static InteractionResult takeOut(Level level, BlockPos pos, ZoneRefiningFurnaceBlockEntity be) {
        Block.popResource(level, pos.above(), be.getItem());
        be.setItem(ItemStack.EMPTY);
        return InteractionResult.SUCCESS;
    }
}