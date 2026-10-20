package com.taku3.semiconductorlab.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 窒素の多い耕地 [0-05]（豆を育てた後の畑）。豆の根の根粒菌が空気の窒素 N2 をアンモニアに変え、根が土に残す。
 * 上の作物（豆以外）を速く育てる：耕地のランダムtick ごとに BOOST_CHANCE で作物を1段育てる（ふつうの耕地の作物はこれが無い）。
 * 上の作物が1回熟すごとに窒素を1回分使い（NITROGEN − 1）、0 になるとふつうの耕地（バニラ）に戻る。
 * 作物が植えられる・水で湿るのはバニラと同じ（タグ minecraft:supports_crops・grows_crops に入れてある）。踏むと土に戻る。
 */
public class NitrogenFarmlandBlock extends FarmlandBlock {
    /** 窒素の残り（作物が熟す回数ぶん） */
    public static final int MAX_NITROGEN = 3;
    public static final IntegerProperty NITROGEN = IntegerProperty.create("nitrogen", 1, MAX_NITROGEN);
    /** 上の作物の「熟した」をもう数えたか（同じ作物で2回減らさない） */
    public static final BooleanProperty COUNTED = BooleanProperty.create("counted");
    /** 耕地のランダムtick ごとに、上の作物をもう1段育てる確率 */
    public static final float BOOST_CHANCE = 0.5f;

    public NitrogenFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties);
        registerDefaultState(defaultBlockState().setValue(NITROGEN, MAX_NITROGEN).setValue(COUNTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NITROGEN, COUNTED);
    }

    /** 豆が熟したとき：下の耕地（ふつう・窒素）を窒素いっぱいの耕地にする。湿り気はそのまま */
    public static void enrich(ServerLevel level, BlockPos farmland, Block self) {
        BlockState s = level.getBlockState(farmland);
        if (!s.is(Blocks.FARMLAND) && !s.is(self)) return;
        level.setBlock(farmland, self.defaultBlockState().setValue(MOISTURE, s.getValue(MOISTURE)), 2);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);   // 湿り気（乾くと土に戻ることもある）
        BlockState now = level.getBlockState(pos);
        if (!now.is(this)) return;
        BlockPos up = pos.above();
        BlockState above = level.getBlockState(up);
        if (!(above.getBlock() instanceof CropBlock crop) || above.getBlock() instanceof PeaCropBlock) return;
        if (!crop.isMaxAge(above)) {
            if (now.getValue(COUNTED)) level.setBlock(pos, now = now.setValue(COUNTED, false), 2);   // 新しく植えた作物
            if (random.nextFloat() < BOOST_CHANCE) level.setBlock(up, crop.getStateForAge(crop.getAge(above) + 1), 2);
        } else if (!now.getValue(COUNTED)) {
            int n = now.getValue(NITROGEN) - 1;   // 作物が熟した：窒素を1回分使った
            if (n <= 0) level.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, now.getValue(MOISTURE)), 2);
            else level.setBlock(pos, now.setValue(NITROGEN, n).setValue(COUNTED, true), 2);
        }
    }
}
