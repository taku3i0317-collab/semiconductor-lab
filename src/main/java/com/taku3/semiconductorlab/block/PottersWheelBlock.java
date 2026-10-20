package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

/**
 * ろくろ [0-19]（陶工の回転台。重い円盤＝はずみ車が回り続けるので、粘土を回しながら薄く均一な壁に引き上げられる）。
 * 粘土玉を持って右クリック → 粘土玉 CLAY_ON_WHEEL 個で生の土器（手で作ると 5個要る＝壁が厚い）。
 */
public class PottersWheelBlock extends Block {
    /** ろくろで生の土器1つに使う粘土玉 */
    public static final int CLAY_ON_WHEEL = 3;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public PottersWheelBlock(Properties properties) { super(properties); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.CLAY_BALL)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) throwPot(player, stack);
        level.playSound(null, pos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.8f, 0.9f);
        return InteractionResult.SUCCESS;
    }

    /** 手の粘土玉で生の土器を1つ引き上げる。足りなければ知らせるだけ（テストからも呼ぶ） */
    public static boolean throwPot(Player player, ItemStack clay) {
        if (clay.getCount() < CLAY_ON_WHEEL) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.potters_wheel.need_clay", CLAY_ON_WHEEL));
            return false;
        }
        clay.shrink(CLAY_ON_WHEEL);
        ItemStack pot = new ItemStack(Survival.RAW_CLAY_POT);
        if (!player.getInventory().add(pot) && !pot.isEmpty())
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), pot));
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.potters_wheel.thrown", CLAY_ON_WHEEL));
        return true;
    }
}
