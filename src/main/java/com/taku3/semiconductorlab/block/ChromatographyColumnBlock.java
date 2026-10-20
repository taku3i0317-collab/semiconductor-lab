package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * クロマトグラフィーの柱 [2-54]（1906 ツヴェット）：ガラスの管に炭酸カルシウム（方解石の粉）をつめた物。
 * 1) 葉の色素の液を持って右クリック → 色素が柱の上にしみこむ（LOADED）。
 * 2) ベンゼン（流す液）を持って右クリック → 色素が流されて帯に分かれる。粉にくっつきにくいカロテン（だいだい）が先に下から出てきて、
 *    くっつきやすいクロロフィル（緑）は柱の中に帯として残る（掘り出す）。流す液がないと色素は上にとどまって分かれない。
 * ⚠️ キサントフィル（黄）とクロロフィル a・b の区別はメッセージだけ（アイテムはカロテンとクロロフィルの2つ）。
 */
public class ChromatographyColumnBlock extends Block {
    public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 16, 11);

    public ChromatographyColumnBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LOADED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LOADED); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Chemistry.LEAF_PIGMENT_EXTRACT) && !stack.is(Chemistry.BENZENE)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!level.isClientSide()) use(level, pos, player, stack);
        return InteractionResult.SUCCESS;
    }

    /** 手の物で柱を使う（テストからも呼ぶ）。結果のメッセージのキーの末尾を返す */
    public static String use(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockState s = level.getBlockState(pos);
        String key;
        if (stack.is(Chemistry.LEAF_PIGMENT_EXTRACT)) {
            if (s.getValue(LOADED)) key = "already_loaded";
            else {
                level.setBlock(pos, s.setValue(LOADED, true), 3);
                ItemStack rem = stack.getItem().getCraftingRemainder(stack) == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder(stack).create();
                stack.shrink(1);
                give(level, pos, player, rem);
                key = "loaded";
            }
        } else if (stack.is(Chemistry.BENZENE)) {
            if (!s.getValue(LOADED)) key = "empty";
            else {
                stack.shrink(1);
                level.setBlock(pos, s.setValue(LOADED, false), 3);
                give(level, pos, player, new ItemStack(Chemistry.CAROTENE));
                give(level, pos, player, new ItemStack(Chemistry.CHLOROPHYLL));
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.5f, 1.2f);
                key = "separated";
            }
        } else key = "empty";
        player.sendOverlayMessage(Component.translatable("message.semiconductor-lab.chromatography_column." + key));
        return key;
    }

    private static void give(Level level, BlockPos pos, Player player, ItemStack out) {
        if (out.isEmpty()) return;
        if (!player.getInventory().add(out) && !out.isEmpty())
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, out));
    }
}
