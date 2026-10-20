package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** ふいご [3-04] のブロック：素手で右クリック＝踏む。送風の強さを表示する（画面なし） */
public class BellowsBlock extends MachineEntityBlock {
    public BellowsBlock(Properties props, Machine<?> machine) { super(props, machine); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BellowsBlockEntity be)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        boolean water = BellowsBlockEntity.waterDriven(level, pos);
        if (!water) {
            be.pump();
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8f, 0.6f);
        }
        player.sendOverlayMessage(Component.translatable(water ? "message.semiconductor-lab.bellows.water" : "message.semiconductor-lab.bellows.air",
                Math.round(be.blow() * 100)));
        return InteractionResult.SUCCESS;
    }
}
