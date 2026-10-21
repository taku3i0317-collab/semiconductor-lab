package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Agriculture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
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
 * 塩のたまった耕地 [11-01]（灌漑と塩害、IMP3 S-011）。乾いた土地（砂漠・サバンナ・荒野）で、水で湿らせた（灌漑した）ふつうの耕地で熟した作物を
 * 収穫するたびに、水の中の塩が土に残る（SALT +1）。{@link #SALTY_AT} 回で「塩害」：収穫が1つ少ない。
 * 戻し方：石こう（硫酸カルシウム）をまく（ナトリウムを入れかえて洗い流す、今も使う方法）。雨の多い土地では塩は雨で流れるのでたまらない。
 */
public class SaltyFarmlandBlock extends FarmlandBlock {
    public static final int SALTY_AT = 3;
    public static final IntegerProperty SALT = IntegerProperty.create("salt", 1, SALTY_AT);

    public SaltyFarmlandBlock(Properties p) {
        super(Blocks.DIRT, p);
        registerDefaultState(defaultBlockState().setValue(SALT, 1));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { super.createBlockStateDefinition(b); b.add(SALT); }

    /** 乾いた土地か（砂漠・サバンナ・荒野） */
    public static boolean arid(ServerLevel level, BlockPos pos) {
        var b = level.getBiome(pos);
        return b.is(net.minecraft.world.level.biome.Biomes.DESERT) || b.is(BiomeTags.IS_SAVANNA) || b.is(BiomeTags.IS_BADLANDS);
    }

    /** 熟した作物を収穫したとき（Agriculture の MODIFY_DROPS から）。arid は乾いた土地か（テストでは直接わたす）。収穫を減らしたら true */
    public static boolean onHarvest(ServerLevel level, BlockPos cropPos, BlockState crop, List<ItemStack> drops, boolean arid) {
        if (!arid || !(crop.getBlock() instanceof CropBlock c) || !c.isMaxAge(crop)) return false;
        BlockPos below = cropPos.below();
        BlockState f = level.getBlockState(below);
        if (f.is(Blocks.FARMLAND)) {
            if (f.getValue(MOISTURE) <= 0) return false;   // 灌漑していない（乾いた）畑には塩はたまらない
            level.setBlock(below, Agriculture.SALTY_FARMLAND.defaultBlockState().setValue(MOISTURE, f.getValue(MOISTURE)), 2);
            return false;
        }
        if (!(f.getBlock() instanceof SaltyFarmlandBlock)) return false;
        int salt = Math.min(SALTY_AT, f.getValue(SALT) + 1);
        level.setBlock(below, f.setValue(SALT, salt), 2);
        if (salt < SALTY_AT || drops.isEmpty()) return false;
        drops.get(0).shrink(1);
        if (drops.get(0).isEmpty()) drops.remove(0);
        return true;
    }

    /** 石こうをまく：ふつうの耕地にもどす（テストからも呼ぶ）。できたら true */
    public static boolean gypsum(ServerLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (!(s.getBlock() instanceof SaltyFarmlandBlock)) return false;
        level.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, s.getValue(MOISTURE)), 3);
        return true;
    }
}
