package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 人を感じるセンサー（焦電型赤外線センサー）[9-46]（IMP4-01、O-030 案A 仮決定）：近くで動く体（体温の赤外線の変化）を感じると LIT になる。
 * 隣に置いた LED の明かりは、このセンサーが LIT の間だけ点く（{@link #holdsOff}）。レッドストーンは使わない。中身は {@link PirSensorBlockEntity}
 */
public class PirSensorBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 6, 12);
    private final Machine<PirSensorBlockEntity> machine;

    public PirSensorBlock(Properties props, Machine<PirSensorBlockEntity> machine) {
        super(props);
        this.machine = machine;
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return machine.create(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return machine.tickerFor(type); }

    /** 隣に、人を感じていないセンサーがあるか（あれば明かりは消えたまま） */
    public static boolean holdsOff(Level level, BlockPos pos) {
        if (level == null) return false;
        for (Direction d : Direction.values()) {
            BlockState s = level.getBlockState(pos.relative(d));
            if (s.getBlock() instanceof PirSensorBlock && !s.getValue(LIT)) return true;
        }
        return false;
    }
}
