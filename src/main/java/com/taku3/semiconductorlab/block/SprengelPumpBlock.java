package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** シュプレンゲル水銀ポンプ [5-15]。水銀を持って右クリックで入れる。素手で右クリックすると状態を表示 */
public class SprengelPumpBlock extends Block implements EntityBlock {
    public SprengelPumpBlock(Properties props) { super(props); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SprengelPumpBlockEntity(pos, state); }

    @Override @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.SPRENGEL_PUMP
                ? (BlockEntityTicker<T>) (BlockEntityTicker<SprengelPumpBlockEntity>) SprengelPumpBlockEntity::tick : null;
    }

    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.MERCURY) || !(level.getBlockEntity(pos) instanceof SprengelPumpBlockEntity be)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (be.addMercury()) { if (!player.getAbilities().instabuild) stack.shrink(1); }
        player.sendOverlayMessage(status(be));
        return InteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SprengelPumpBlockEntity be) player.sendOverlayMessage(status(be));
        return InteractionResult.SUCCESS;
    }

    private static Component status(SprengelPumpBlockEntity be) {
        String k = "message.semiconductor-lab.sprengel_pump.";
        if (!be.isReady()) return Component.translatable(k + "need_mercury", be.getMercury(), SprengelPumpBlockEntity.MERCURY_NEEDED);
        if (be.getLevel() == null || VacuumChamberBlockEntity.findAdjacent(be.getLevel(), be.getBlockPos()) == null) return Component.translatable(k + "no_chamber");
        return Component.translatable(k + "running");
    }
}
