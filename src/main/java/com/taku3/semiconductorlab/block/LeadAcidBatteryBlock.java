package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineBlock;
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
public class LeadAcidBatteryBlock extends MachineBlock {
    public LeadAcidBatteryBlock(Properties props, Machine<?> machine) { super(props, machine); }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LeadAcidBatteryBlockEntity be)
            player.sendSystemMessage(Component.translatable("message.semiconductor-lab.lead_acid_battery.status",
                    EnergyStorage.format(be.getStoredJ()), EnergyStorage.format(LeadAcidBatteryBlockEntity.CAPACITY),
                    String.format("%.1f kW", be.getLastChargeWatts() / 1000), String.format("%.1f kW", be.getLastDischargeWatts() / 1000)));
        return InteractionResult.SUCCESS;
    }
}
