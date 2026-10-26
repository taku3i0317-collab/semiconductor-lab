package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * ろくろ [0-19]（陶工の回転台。重い円盤＝はずみ車が回り続けるので、粘土を回しながら薄く均一な壁に引き上げられる）。
 * 粘土玉を持って右クリック → 1つずつ円盤の上に粘土が積まれ、塊が大きくなる（{@link #CLAY} 1〜3、モデルの大きさが変わる）。
 * 3つそろったら素手で右クリック → 円盤が回る（{@link #SPIN} が {@link #SPIN_STEPS} まで進む。円盤のふちの印が回って見える、音つき）→ 生の土器が飛び出す。
 * 動きはブロックの状態とモデルだけ（BlockEntityRenderer は使わない＝軽い。S-012）。手で作ると粘土玉5つ要る＝ろくろの方が壁が薄い。
 */
public class PottersWheelBlock extends Block {
    /** ろくろで生の土器1つに使う粘土玉 */
    public static final int CLAY_ON_WHEEL = 3;
    public static final IntegerProperty CLAY = IntegerProperty.create("clay", 0, CLAY_ON_WHEEL);
    /** 回っている段（0＝止まっている）。1段ごとに印が 90 度まわる。SPIN_STEPS まで回ると土器ができる */
    public static final int SPIN_STEPS = 16, SPIN_INTERVAL = 2;
    public static final IntegerProperty SPIN = IntegerProperty.create("spin", 0, SPIN_STEPS);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 10, 16);

    public PottersWheelBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CLAY, 0).setValue(SPIN, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(CLAY, SPIN); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.CLAY_BALL)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) addClay(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) startSpin(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    /** 粘土玉を1つ円盤にのせる（テストからも呼ぶ）。のせたら true */
    public static boolean addClay(Level level, BlockPos pos, Player player, ItemStack clay) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(SPIN) > 0) return false;
        int c = s.getValue(CLAY);
        if (c >= CLAY_ON_WHEEL) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.potters_wheel.full"));
            return false;
        }
        clay.shrink(1);
        level.setBlock(pos, s.setValue(CLAY, c + 1), 3);
        level.playSound(null, pos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.8f, 0.9f + 0.1f * c);
        player.sendOverlayMessage(Component.translatable(c + 1 < CLAY_ON_WHEEL ? "message.semiconductor-lab.potters_wheel.added" : "message.semiconductor-lab.potters_wheel.ready", c + 1, CLAY_ON_WHEEL));
        return true;
    }

    /** 粘土がそろっていれば回し始める（テストからも呼ぶ）。始めたら true */
    public static boolean startSpin(Level level, BlockPos pos, Player player) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(SPIN) > 0) return false;
        if (s.getValue(CLAY) < CLAY_ON_WHEEL) {
            player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.potters_wheel.need_clay", CLAY_ON_WHEEL));
            return false;
        }
        level.setBlock(pos, s.setValue(SPIN, 1), 3);
        level.scheduleTick(pos, s.getBlock(), SPIN_INTERVAL);
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5f, 1.4f);
        return true;
    }

    /** 1段まわす。最後の段で土器を出して止まる（テストからも呼ぶ） */
    public static void step(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        int spin = s.getValue(SPIN);
        if (spin <= 0) return;
        if (spin < SPIN_STEPS) {
            level.setBlock(pos, s.setValue(SPIN, spin + 1), 3);
            if ((spin + 1) % 4 == 0) level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.35f, 1.6f);
            if (level instanceof ServerLevel sl) sl.scheduleTick(pos, s.getBlock(), SPIN_INTERVAL);
            return;
        }
        level.setBlock(pos, s.setValue(SPIN, 0).setValue(CLAY, 0), 3);
        Block.popResourceFromFace(level, pos, net.minecraft.core.Direction.UP, new ItemStack(Survival.RAW_CLAY_POT));
        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 0.8f, 1.0f);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { step(level, pos); }
}
