package com.taku3.semiconductorlab.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 電気を「変える」ブロックの土台（v0.4、S-007）：後ろの面＝入力（受けるだけ）、前の面＝出力（出すだけ）。ほかの面は何もつながない。
 * 変圧器 [4-10]（交流 → 交流、電圧の段を変える）と整流器 [4-11]（交流 → 直流、同じ段）がこれを使う。
 * それぞれの面の電圧の段は、その面に付いたケーブルの段（変圧器：低圧の網と高圧の網をつなぐ）。
 */
public abstract class PowerConverterBlockEntity extends BlockEntity {
    /** 通す電力の上限：1MW（低圧の網の上限と同じ） */
    public static final long MAX_PER_TICK = EnergyStorage.wattsToJoulesPerTick(1_000_000);
    private final SimpleEnergyStorage buffer = new SimpleEnergyStorage(MAX_PER_TICK * 4, 0, 0, 0, this::setChanged);
    private long passedThisTick, lastPassedJ;

    protected PowerConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    /** 入力の電流（変圧器・整流器とも交流） */
    protected abstract Current inputCurrent();
    /** 出力の電流（変圧器＝交流、整流器＝直流） */
    protected abstract Current outputCurrent();
    /** 効率（%）：変圧器 98、整流器 95（⚠️ゲームの値） */
    protected abstract int efficiencyPercent();

    public Direction front() { return getBlockState().getValue(HorizontalDirectionalBlock.FACING); }
    /** その面に付いたケーブルの電圧の段（ケーブルがなければ低圧） */
    protected int classAt(Direction side) {
        return level != null && level.getBlockEntity(worldPosition.relative(side)) instanceof CableBlockEntity c ? c.voltageClass() : 0;
    }
    public int inputClass() { return classAt(front().getOpposite()); }
    public int outputClass() { return classAt(front()); }
    public double getLastWatts() { return lastPassedJ * 20.0; }

    private final EnergyStorage input = new EnergyStorage() {
        @Override public long getStoredJ() { return buffer.getStoredJ(); }
        @Override public long getCapacityJ() { return buffer.getCapacityJ(); }
        @Override public boolean canExtract() { return false; }
        @Override public Current current() { return inputCurrent(); }
        @Override public int voltageClass() { return inputClass(); }
        @Override public long insert(long j, boolean simulate) {
            long n = Math.max(0, Math.min(Math.min(j, MAX_PER_TICK - passedThisTick), (buffer.getCapacityJ() - buffer.getStoredJ()) * 100 / efficiencyPercent()));
            if (!simulate && n > 0) { buffer.insertRaw(n * efficiencyPercent() / 100); passedThisTick += n; }
            return n;
        }
        @Override public long extract(long j, boolean simulate) { return 0; }
    };
    private final EnergyStorage output = new EnergyStorage() {
        @Override public long getStoredJ() { return buffer.getStoredJ(); }
        @Override public long getCapacityJ() { return buffer.getCapacityJ(); }
        @Override public boolean canInsert() { return false; }
        @Override public Current current() { return outputCurrent(); }
        @Override public int voltageClass() { return outputClass(); }
        @Override public long insert(long j, boolean simulate) { return 0; }
        @Override public long extract(long j, boolean simulate) {
            long n = Math.max(0, Math.min(j, buffer.getStoredJ()));
            if (!simulate && n > 0) buffer.extractRaw(n);
            return n;
        }
    };

    /** 後ろの面＝入力、前の面＝出力、ほか＝なし */
    public EnergyStorage storageFor(Direction side) {
        if (side == front()) return output;
        if (side == front().getOpposite()) return input;
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PowerConverterBlockEntity be) {
        if (level.isClientSide()) return;
        be.lastPassedJ = be.passedThisTick;
        be.passedThisTick = 0;
        // 前の面にケーブルではなく直接機械が付いていれば、そこへ押し出す（段と電流が合うときだけ）
        Direction f = be.front();
        BlockPos np = pos.relative(f);
        if (!(level.getBlockEntity(np) instanceof CableBlockEntity)) {
            EnergyStorage n = EnergyApi.ENERGY.find(level, np, f.getOpposite());
            if (n != null && n.canInsert() && !n.canExtract()) CableBlockEntity.move(be.output, n, MAX_PER_TICK);
        }
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); buffer.save(out); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); buffer.load(in); }
}
