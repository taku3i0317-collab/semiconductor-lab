package com.taku3.semiconductorlab.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * マルチブロックの部品のブロック（IMP2、O-034 Q33-4＝b）。{@link Multiblock#FORMED} を持ち、組み上がると true になって
 * blockstates の JSON が「つながった模様」の絵に切りかえる（切りかえは Multiblock.recheck がサーバーでする）。置いたときは false。
 */
public class MultiblockPartBlock extends Block {
    public MultiblockPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(Multiblock.FORMED, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(Multiblock.FORMED); }
}
