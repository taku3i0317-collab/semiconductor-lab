package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 発酵のかめ [2-39]（土器 [0-03] のかめ＋木のふた）。酵母を入れた砂糖水を入れると、時間がたつうちに酵母が糖をアルコールに変える：
 * C6H12O6 → 2C2H5OH + 2CO2（泡が出る）。STAGE 0＝空、1〜3＝発酵中（ランダムティックで進む）、4＝できあがり。
 * できあがりを素手で右クリック → 発酵した液2＋酵母1（酵母は発酵中に増えるので、次の仕込みに使える＝昔の「おり」の使い回し）。
 */
public class FermentationCrockBlock extends Block {
    public static final int DONE = 4;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, DONE);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public FermentationCrockBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(STAGE); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected boolean isRandomlyTicking(BlockState state) { int s = state.getValue(STAGE); return s > 0 && s < DONE; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { ferment(level, pos); }

    /** 発酵を1段すすめる（テストからも呼ぶ）。泡（CO2）が出る */
    public static void ferment(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        int st = s.getValue(STAGE);
        if (st == 0 || st >= DONE) return;
        level.setBlock(pos, s.setValue(STAGE, st + 1), 3);
        if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 6, 0.15, 0.02, 0.15, 0.01);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.SUGAR_WASH)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) fill(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) collect(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    /** 酵母入りの砂糖水を仕込む。空のかめにだけ入る */
    public static boolean fill(Level level, BlockPos pos, Player player, ItemStack wash) {
        BlockState s = level.getBlockState(pos);
        if (s.getValue(STAGE) != 0) { player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.fermentation_crock.busy")); return false; }
        wash.shrink(1);
        level.setBlock(pos, s.setValue(STAGE, 1), 3);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6f, 1.0f);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.fermentation_crock.filled"));
        return true;
    }

    /** できあがりを取り出す。まだなら何も出ない */
    public static boolean collect(Level level, BlockPos pos, Player player) {
        BlockState s = level.getBlockState(pos);
        int st = s.getValue(STAGE);
        if (st < DONE) {
            player.sendOverlayMessage(Component.translatable(st == 0 ? "message.semiconductor-lab.fermentation_crock.empty" : "message.semiconductor-lab.fermentation_crock.fermenting", st, DONE));
            return false;
        }
        level.setBlock(pos, s.setValue(STAGE, 0), 3);
        give(level, pos, player, new ItemStack(Chemistry.FERMENTED_WASH, 2));
        give(level, pos, player, new ItemStack(Chemistry.WILD_YEAST));
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6f, 0.9f);
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.fermentation_crock.done"));
        return true;
    }

    private static void give(Level level, BlockPos pos, Player player, ItemStack out) {
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, out));
    }
}
