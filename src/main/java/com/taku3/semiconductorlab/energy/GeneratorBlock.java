package com.taku3.semiconductorlab.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.function.BiFunction;

/** 発電機ブロックの共通クラス。右クリック：GUI を持つもの（蒸気機関）は画面を開き、それ以外（水車）は出力(W)と蓄積(J)をチャットに表示 */
public class GeneratorBlock extends Block implements EntityBlock {
    private final BiFunction<BlockPos, BlockState, ? extends GeneratorBlockEntity> factory;
    private final java.util.function.Supplier<BlockEntityType<?>> typeRef;
    public GeneratorBlock(Properties props, BiFunction<BlockPos, BlockState, ? extends GeneratorBlockEntity> factory, java.util.function.Supplier<BlockEntityType<?>> type) { super(props); this.factory = factory; this.typeRef = type; }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return factory.apply(pos, state); }
    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == typeRef.get() ? (BlockEntityTicker<T>) (BlockEntityTicker<GeneratorBlockEntity>) GeneratorBlockEntity::tick : null;
    }
    /** GUI を持つ発電機（今は蒸気機関だけ）は BE が MenuProvider。水車は null＝GUIなし */
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider mp ? mp : null;
    }
    /** [4-05] 自励式ダイナモを手に持って右クリック：永久磁石のダイナモと積み替える（出力2倍）。それ以外は素手と同じ */
    @Override protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(com.taku3.semiconductorlab.ModItems.SELF_EXCITED_DYNAMO) || !(level.getBlockEntity(pos) instanceof GeneratorBlockEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!be.supportsDynamoUpgrade()) player.sendSystemMessage(Component.translatable("message.semiconductor-lab.self_excited_dynamo.not_needed"));
        else if (be.isSelfExcited()) player.sendSystemMessage(Component.translatable("message.semiconductor-lab.self_excited_dynamo.already"));
        else if (be.installSelfExcitedDynamo()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.self_excited_dynamo.installed"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be0 = level.getBlockEntity(pos);
        if (be0 instanceof MenuProvider mp) player.openMenu(mp);      // 蒸気機関：GUI を開く
        else if (be0 instanceof GeneratorBlockEntity be)               // 水車：チャットに状態表示
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.generator.status", EnergyStorage.formatWatts(be.getLastWatts()), EnergyStorage.format(be.getStorage().getStoredJ())));
        return InteractionResult.SUCCESS;
    }
}