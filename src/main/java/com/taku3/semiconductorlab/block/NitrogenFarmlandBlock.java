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
 * <p>[11-06] 過リン酸石灰（IMP3、O-020）：リン {@link #PHOSPHORUS} も持てる（＝「肥えた耕地」）。リンは熟した作物の収穫を1つ増やし（{@link #harvestBonus}）、収穫ごとに1回分使う。
 * 窒素は育つ速さ、リンは実の多さ（本物も、窒素は葉や茎、リンは花や実や根に効く）。窒素もリンも 0 になるとふつうの耕地に戻る。
 */
public class NitrogenFarmlandBlock extends FarmlandBlock {
    /** 窒素の残り（作物が熟す回数ぶん） */
    public static final int MAX_NITROGEN = 3;
    public static final IntegerProperty NITROGEN = IntegerProperty.create("nitrogen", 0, MAX_NITROGEN);
    /** リンの残り（収穫の回数ぶん、[11-06]） */
    public static final int MAX_PHOSPHORUS = 3;
    public static final IntegerProperty PHOSPHORUS = IntegerProperty.create("phosphorus", 0, MAX_PHOSPHORUS);
    /** 上の作物の「熟した」をもう数えたか（同じ作物で2回減らさない） */
    public static final BooleanProperty COUNTED = BooleanProperty.create("counted");
    /** 耕地のランダムtick ごとに、上の作物をもう1段育てる確率 */
    public static final float BOOST_CHANCE = 0.5f;

    public NitrogenFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties);
        registerDefaultState(defaultBlockState().setValue(NITROGEN, MAX_NITROGEN).setValue(PHOSPHORUS, 0).setValue(COUNTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NITROGEN, PHOSPHORUS, COUNTED);
    }

    /** 豆が熟したとき：下の耕地（ふつう・窒素）を窒素いっぱいの耕地にする。湿り気はそのまま */
    public static void enrich(ServerLevel level, BlockPos farmland, Block self) {
        BlockState s = level.getBlockState(farmland);
        if (!s.is(Blocks.FARMLAND) && !s.is(self)) return;
        int phosphorus = s.is(self) ? s.getValue(PHOSPHORUS) : 0;   // リンはそのまま
        level.setBlock(farmland, self.defaultBlockState().setValue(MOISTURE, s.getValue(MOISTURE)).setValue(PHOSPHORUS, phosphorus), 2);
    }

    /** 窒素もリンも無くなったら、ふつうの耕地 */
    static BlockState settle(BlockState s) {
        if (s.getValue(NITROGEN) <= 0 && s.getValue(PHOSPHORUS) <= 0) return Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, s.getValue(MOISTURE));
        return s;
    }

    /**
     * [11-06] 熟した作物を収穫したとき（落とし物を決めるとき）：下の耕地にリンがあれば、いちばん目の収穫物を1つ増やし、リンを1回分使う。
     * 増やしたら true（テストからも呼ぶ。Agriculture の MODIFY_DROPS から呼ぶ）
     */
    public static boolean harvestBonus(ServerLevel level, BlockPos cropPos, BlockState crop, java.util.List<net.minecraft.world.item.ItemStack> drops) {
        if (!(crop.getBlock() instanceof CropBlock c) || !c.isMaxAge(crop) || drops.isEmpty()) return false;
        BlockPos below = cropPos.below();
        BlockState f = level.getBlockState(below);
        if (!(f.getBlock() instanceof NitrogenFarmlandBlock) || f.getValue(PHOSPHORUS) <= 0) return false;
        drops.add(drops.get(0).copyWithCount(1));
        level.setBlock(below, settle(f.setValue(PHOSPHORUS, f.getValue(PHOSPHORUS) - 1)), 2);
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);   // 湿り気（乾くと土に戻ることもある）
        BlockState now = level.getBlockState(pos);
        if (!now.is(this)) return;
        BlockPos up = pos.above();
        BlockState above = level.getBlockState(up);
        if (!(above.getBlock() instanceof CropBlock crop) || above.getBlock() instanceof PeaCropBlock) return;
        if (now.getValue(NITROGEN) <= 0) return;   // リンだけの耕地：速さは変わらない
        if (!crop.isMaxAge(above)) {
            if (now.getValue(COUNTED)) level.setBlock(pos, now = now.setValue(COUNTED, false), 2);   // 新しく植えた作物
            if (random.nextFloat() < BOOST_CHANCE) level.setBlock(up, crop.getStateForAge(crop.getAge(above) + 1), 2);
        } else if (!now.getValue(COUNTED)) {
            int n = now.getValue(NITROGEN) - 1;   // 作物が熟した：窒素を1回分使った
            level.setBlock(pos, settle(now.setValue(NITROGEN, n).setValue(COUNTED, true)), 2);
        }
    }
}
