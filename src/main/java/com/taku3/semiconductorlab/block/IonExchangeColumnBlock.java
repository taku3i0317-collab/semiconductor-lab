package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * イオン交換塔 [2-17]（1935 アダムスとホームズ：フェノール樹脂のイオン交換体）。陽イオン交換樹脂（R-SO3H）と陰イオン交換樹脂（R-OH）を詰めた塔。
 * 蒸留水を持って右クリック → 脱イオン水（残ったイオンを H+ と OH- に入れ替え、それが水になる）。1回ごとに両方の樹脂を1回分使う。
 * 陽イオン側が尽きたら硫酸で、陰イオン側が尽きたら水酸化ナトリウムで再生する（右クリック）。どちらかが尽きると水はきれいにならない。
 */
public class IonExchangeColumnBlock extends Block {
    public static final int CAPACITY = 8;
    public static final IntegerProperty CATION = IntegerProperty.create("cation", 0, CAPACITY);
    public static final IntegerProperty ANION = IntegerProperty.create("anion", 0, CAPACITY);

    public IonExchangeColumnBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CATION, CAPACITY).setValue(ANION, CAPACITY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(CATION, ANION); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.DISTILLED_WATER) && !stack.is(Chemistry.SULFURIC_ACID) && !stack.is(Gas.SODIUM_HYDROXIDE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) use(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** 手の物で塔を使う（テストからも呼ぶ）。結果のメッセージのキーの末尾を返す */
    public static String use(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockState s = level.getBlockState(pos);
        String key;
        if (stack.is(Chemistry.SULFURIC_ACID)) {
            level.setBlock(pos, s.setValue(CATION, CAPACITY), 3);
            give(level, pos, player, stack.getItem().getCraftingRemainder(stack) == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder(stack).create());
            stack.shrink(1);
            key = "regen_cation";
        } else if (stack.is(Gas.SODIUM_HYDROXIDE)) {
            level.setBlock(pos, s.setValue(ANION, CAPACITY), 3);
            stack.shrink(1);
            key = "regen_anion";
        } else if (s.getValue(CATION) == 0 || s.getValue(ANION) == 0) {
            key = s.getValue(CATION) == 0 ? "spent_cation" : "spent_anion";
        } else {
            level.setBlock(pos, s.setValue(CATION, s.getValue(CATION) - 1).setValue(ANION, s.getValue(ANION) - 1), 3);
            stack.shrink(1);
            give(level, pos, player, new ItemStack(Chemistry.DEIONIZED_WATER));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6f, 1.2f);
            key = "deionized";
        }
        BlockState now = level.getBlockState(pos);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.ion_exchange_column." + key, now.getValue(CATION), now.getValue(ANION), CAPACITY));
        return key;
    }

    private static void give(Level level, BlockPos pos, Player player, ItemStack out) {
        if (out.isEmpty()) return;
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, out));
    }
}
