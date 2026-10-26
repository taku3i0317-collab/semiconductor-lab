package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Construction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** 蛇口 [12-06]：空のガラス瓶を持って右クリックすると、管でつながった水源の水が出る（水の質は {@link WaterSupply}） */
public class WaterTapBlock extends Block {
    public WaterTapBlock(Properties p) { super(p); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.GLASS_BOTTLE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) fill(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** 瓶に水を入れる（テストからも呼ぶ）。出た水のアイテム（出なければ null） */
    public static Item fill(Level level, BlockPos pos, Player player, ItemStack bottle) {
        WaterSupply.Result r = WaterSupply.trace(level, pos);
        Item out = switch (r.quality()) {
            case NONE -> null;
            case MURKY -> Construction.MURKY_WATER;
            case FILTERED -> Construction.FILTERED_WATER;
            case SAFE -> Construction.CHLORINATED_WATER;
        };
        if (out == null) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.water_tap.dry"));
            return null;
        }
        if (r.chlorinator() != null) ChlorinatorBlock.use(level, r.chlorinator());
        bottle.shrink(1);
        ItemStack filled = new ItemStack(out);
        if (!player.getInventory().add(filled)) player.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), filled));
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.water_tap." + r.quality().name().toLowerCase()));
        return out;
    }
}
