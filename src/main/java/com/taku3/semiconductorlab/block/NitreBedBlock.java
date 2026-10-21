package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 硝石床 [6-10]（18世紀、IMP3 S-011）。土・腐った肉（窒素のもと）・骨粉・消石灰を積んだ山。土の細菌がアンモニアを硝酸に変え（硝化）、
 * 石灰のカルシウムと合わさって硝酸カルシウムがたまる。ランダムtick ごとに {@link #AGE_CHANCE} で熟し、{@link #MAX_AGE} をこえると
 * 硝石土のブロック（[6-16] の天然の硝石土と同じ）になる。そこから先は [6-16] と同じ（木灰で浸出 → 煮つめて硝石）。
 * ⚠️ 本物は何か月〜1年以上かかる。ゲームでは数十分。
 */
public class NitreBedBlock extends Block {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    public static final float AGE_CHANCE = 0.25f;

    public NitreBedBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AGE); }
    @Override protected boolean isRandomlyTicking(BlockState s) { return true; }

    @Override
    protected void randomTick(BlockState s, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= AGE_CHANCE) return;
        int a = s.getValue(AGE);
        if (a < MAX_AGE) level.setBlock(pos, s.setValue(AGE, a + 1), 2);
        else level.setBlock(pos, Gas.NITRE_EARTH_BLOCK.defaultBlockState(), 2);   // 熟した：硝石土になった
    }
}
