package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Agriculture;
import com.taku3.semiconductorlab.content.Survival;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

/**
 * [11-03] 連作の耕地（やせた耕地）（IMP3、S-007、決定 Q3-15 ①）。ふつうの耕地で熟した作物を収穫すると、何の作物を何回続けたかを覚える。
 * 同じ作物を {@link #TIRED_AT} 回続けると、その収穫から1つ少ない（同じ養分ばかり使われ、その作物の病気や虫も増える＝連作障害）。
 * 戻し方：違う作物を育てる（数え直し）、豆（エンドウ）を育てる・堆肥や肥料をまく（肥えた耕地になる）、何も植えずに休ませる（休閑、三圃式。ランダムtick ごとに {@link #FALLOW_CHANCE}）。
 * 肥えた耕地（{@link NitrogenFarmlandBlock}）の上の収穫は数えない（養分を足しているので）。
 */
public class TiredFarmlandBlock extends FarmlandBlock {
    /** 覚える作物：小麦・ヒトツブコムギ・ニンジン・ジャガイモ・ビートルート（豆は窒素の耕地にするので数えない） */
    public static final int TIRED_AT = 3;
    public static final IntegerProperty CROP = IntegerProperty.create("crop", 0, 4);
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, TIRED_AT);
    /** 何も植えていないとき、ランダムtick ごとにふつうの耕地へ戻る確率（休閑） */
    public static final float FALLOW_CHANCE = 0.1f;

    public TiredFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties);
        registerDefaultState(defaultBlockState().setValue(CROP, 0).setValue(COUNT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CROP, COUNT);
    }

    /** 覚える作物の番号（覚えない作物は -1） */
    public static int cropIndex(Block b) {
        if (b == Blocks.WHEAT) return 0;
        if (b == Survival.EINKORN || b == Survival.WILD_EINKORN) return 1;
        if (b == Blocks.CARROTS) return 2;
        if (b == Blocks.POTATOES) return 3;
        if (b == Blocks.BEETROOTS) return 4;
        return -1;
    }

    /** ふつうの耕地か連作の耕地（肥料・堆肥・豆がそのまま使える耕地） */
    public static boolean isPlain(BlockState s) { return s.is(Blocks.FARMLAND) || s.getBlock() instanceof TiredFarmlandBlock; }

    /** 熟した作物を収穫したとき（Agriculture の MODIFY_DROPS から、テストからも）。収穫を減らしたら true */
    public static boolean onHarvest(ServerLevel level, BlockPos cropPos, BlockState crop, List<ItemStack> drops) {
        if (!(crop.getBlock() instanceof CropBlock c) || !c.isMaxAge(crop)) return false;
        int idx = cropIndex(crop.getBlock());
        if (idx < 0) return false;
        BlockPos below = cropPos.below();
        BlockState f = level.getBlockState(below);
        int moisture;
        int count;
        if (f.is(Blocks.FARMLAND)) { moisture = f.getValue(MOISTURE); count = 1; }
        else if (f.getBlock() instanceof TiredFarmlandBlock) {
            moisture = f.getValue(MOISTURE);
            count = f.getValue(CROP) == idx ? Math.min(TIRED_AT, f.getValue(COUNT) + 1) : 1;   // 違う作物なら数え直し
        } else return false;
        level.setBlock(below, Agriculture.TIRED_FARMLAND.defaultBlockState().setValue(MOISTURE, moisture).setValue(CROP, idx).setValue(COUNT, count), 2);
        if (count < TIRED_AT || drops.isEmpty()) return false;
        drops.get(0).shrink(1);   // やせた：いちばん目の収穫物が1つ少ない
        if (drops.get(0).isEmpty()) drops.remove(0);
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);   // 湿り気（乾くと土に戻ることもある）
        BlockState now = level.getBlockState(pos);
        if (!now.is(this) || !level.getBlockState(pos.above()).isAir()) return;
        if (random.nextFloat() < FALLOW_CHANCE)   // 休閑：何も植えずに休ませると地力が戻る
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, now.getValue(MOISTURE)), 2);
    }
}
