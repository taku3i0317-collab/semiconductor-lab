package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 銅ケーブル。隣のケーブルとは均し（拡散）、機械には押し込む。1ホップ1%損失（簡易。本物は I²R） */
public class CableBlockEntity extends BlockEntity {
    public static final long CAPACITY = 20_000;                 // 20 kJ
    public static final long MAX_TRANSFER = EnergyStorage.wattsToJoulesPerTick(1_000_000); // 1 MW/ホップ
    public static final int LOSS_PERCENT = 1;
    private final SimpleEnergyStorage storage = new SimpleEnergyStorage(CAPACITY, MAX_TRANSFER, MAX_TRANSFER, 0, this::setChanged);

    public CableBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.COPPER_CABLE, pos, state); }
    public EnergyStorage getStorage() { return storage; }

    public static void tick(Level level, BlockPos pos, BlockState state, CableBlockEntity be) {
        if (level.isClientSide()) return;
        for (Direction d : Direction.values()) {
            BlockPos np = pos.relative(d);
            if (!level.isLoaded(np)) continue;
            EnergyStorage n = EnergyApi.ENERGY.find(level, np, d.getOpposite());
            if (n == null || !n.canInsert()) continue;
            boolean cable = level.getBlockEntity(np) instanceof CableBlockEntity;
            long want = cable ? Math.max(0, (be.storage.getStoredJ() - n.getStoredJ()) / 2) : be.storage.getStoredJ();
            move(be.storage, n, want);
        }
    }

    /** from → to へ送る。受け側が受け取れる分だけ出し、損失分を差し引く */
    static void move(EnergyStorage from, EnergyStorage to, long amount) {
        amount = Math.min(amount, MAX_TRANSFER);
        if (amount <= 0) return;
        long accepted = to.insert(amount, true);
        long taken = from.extract(accepted, false);
        to.insert(taken - taken * LOSS_PERCENT / 100, false);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); storage.save(out); }
    @Override protected void loadAdditional(ValueInput in)   { super.loadAdditional(in); storage.load(in); }
}