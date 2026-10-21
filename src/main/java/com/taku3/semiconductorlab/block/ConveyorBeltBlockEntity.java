package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Transport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** ベルト1マス [14-22]：乗っている物1スタック。物が変わったらクライアントにも送る（上に乗って見えるように） */
public class ConveyorBeltBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;

    public ConveyorBeltBlockEntity(BlockPos pos, BlockState state) { super(Transport.CONVEYOR_BELT.type, pos, state); }

    public ItemStack getItem() { return item; }

    public void setItem(ItemStack s) {
        item = s;
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); if (!item.isEmpty()) out.store("Item", ItemStack.OPTIONAL_CODEC, item); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); item = in.read("Item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY); }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p) { return saveCustomOnly(p); }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !item.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, item);
        super.preRemoveSideEffects(pos, state);
    }
}
