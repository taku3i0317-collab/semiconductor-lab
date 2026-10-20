package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
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

/** 塩田 [0-16]。GUI はなく、右クリックでたまった塩・にがりを受け取る（何もなければ今の状態をチャットに出す） */
public class SaltPanBlock extends MachineEntityBlock {
    public SaltPanBlock(Properties props, Machine<?> machine) { super(props, machine); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SaltPanBlockEntity be) {
            int salt = be.getSalt(), bittern = be.getBittern();
            if (be.collect(player) > 0)
                player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.salt_pan.collected", salt, bittern));
            else
                player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.salt_pan." + be.status(level, pos).name().toLowerCase(java.util.Locale.ROOT),
                        be.getProgress() * 100 / SaltPanBlockEntity.TICKS_PER_SALT));
        }
        return InteractionResult.SUCCESS;
    }
}
