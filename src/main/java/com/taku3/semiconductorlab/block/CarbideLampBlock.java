package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * アセチレンランプ（カーバイドランプ）[B-14]（1892 ウィルソンのカーバイド）。下の入れ物にカーバイド、上の入れ物の水が少しずつたれて
 * アセチレンが出る：CaC2 + 2H2O → C2H2 + Ca(OH)2。その炎はとても明るい（炭鉱・自転車のランプ）。
 * カーバイドを持って右クリック → 燃料 FUEL＝8、明るさ15。ランダムティックで1ずつ減り、0で消える（SPENT＝消石灰のかすがたまっている）。
 * 燃え尽きたランプにまたカーバイドを入れると、かすの消石灰が出てくる。
 */
public class CarbideLampBlock extends Block {
    public static final int MAX_FUEL = 8;
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, MAX_FUEL);
    public static final BooleanProperty SPENT = BooleanProperty.create("spent");
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 10, 11);

    public CarbideLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FUEL, 0).setValue(SPENT, false));
    }

    /** 明るさ（Properties.lightLevel から呼ぶ） */
    public static int light(BlockState s) { return s.getValue(FUEL) > 0 ? 15 : 0; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FUEL, SPENT); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected boolean isRandomlyTicking(BlockState state) { return state.getValue(FUEL) > 0; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { burn(level, pos); }

    /** 燃料を1つ減らす（テストからも呼ぶ）。0 になったら消えて、かすがたまる */
    public static void burn(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        int f = s.getValue(FUEL);
        if (f <= 0) return;
        level.setBlock(pos, f == 1 ? s.setValue(FUEL, 0).setValue(SPENT, true) : s.setValue(FUEL, f - 1), 3);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Heat.CALCIUM_CARBIDE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) fuel(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** カーバイドを入れる（テストからも呼ぶ）。燃えている間は入らない */
    public static boolean fuel(Level level, BlockPos pos, Player player, ItemStack carbide) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(FUEL) > 0) { player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.carbide_lamp.burning")); return false; }
        carbide.shrink(1);
        if (s.getValue(SPENT)) {   // かす（消石灰）を出す
            ItemStack lime = new ItemStack(Survival.SLAKED_LIME);
            if (!player.getInventory().add(lime) && !lime.isEmpty())
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, lime));
        }
        level.setBlock(pos, s.setValue(FUEL, MAX_FUEL).setValue(SPENT, false), 3);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5f, 1.4f);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.carbide_lamp.lit"));
        return true;
    }
}
