package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
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
 * ヘリオグラフの板 [2-31]（ニエプス 1826ごろ：ピューター板に、ラベンダー油などに溶かした瀝青を塗り、日光に当てる）。
 * 瀝青は光を浴びると固まり（溶剤に溶けなくなる）＝レジストの一番古い祖先。
 * 空の光が強い昼（板の真上が空まで空気か光を通すブロックだけ。ガラス越しは届く、石の下は届かない）に、ランダムtick ごとに露光が1段すすむ（MAX_EXPOSURE で完成）。
 * テレビン油を持って右クリックで洗う：露光しきっていれば像が残る（ヘリオグラフ）、足りなければ全部溶けてピューター板だけが戻る。
 */
public class HeliographPlateBlock extends Block {
    public static final int MAX_EXPOSURE = 4;
    public static final IntegerProperty EXPOSURE = IntegerProperty.create("exposure", 0, MAX_EXPOSURE);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 1, 15);

    public HeliographPlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(EXPOSURE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(EXPOSURE); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected boolean isRandomlyTicking(BlockState state) { return state.getValue(EXPOSURE) < MAX_EXPOSURE; }

    /** 今この場所に、瀝青を固めるだけの日光が当たっているか */
    public static boolean sunlit(Level level, BlockPos pos) {
        return level.isBrightOutside() && openToSky(level, pos);
    }

    /** 板の真上が空まで、空気か光を通すブロック（ガラスなど）だけか（光の計算を待たずに分かる。ブロックの影なら false） */
    public static boolean openToSky(Level level, BlockPos pos) {
        for (BlockPos p = pos.above(); p.getY() <= level.getMaxY(); p = p.above())
            if (!level.getBlockState(p).propagatesSkylightDown()) return false;
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (sunlit(level, pos)) level.setBlock(pos, state.setValue(EXPOSURE, Math.min(MAX_EXPOSURE, state.getValue(EXPOSURE) + 1)), 2);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.TURPENTINE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) {
            boolean image = wash(level, pos, state);
            stack.shrink(1);
            player.sendOverlayMessage(Component.translatable(image ? "message.semiconductor-lab.heliograph_plate.image" : "message.semiconductor-lab.heliograph_plate.dissolved"));
        }
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5f, 1.4f);
        return InteractionResult.SUCCESS;
    }

    /** テレビン油で洗う。光で固まっていれば像が残る（true）、足りなければ瀝青は全部溶けてピューター板が戻る（false）。テストからも呼ぶ */
    public static boolean wash(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(EXPOSURE) >= MAX_EXPOSURE) {
            level.setBlock(pos, Chemistry.HELIOGRAPH.defaultBlockState(), 3);
            return true;
        }
        level.removeBlock(pos, false);
        Block.popResource(level, pos, new ItemStack(Chemistry.PEWTER_PLATE));
        return false;
    }
}
