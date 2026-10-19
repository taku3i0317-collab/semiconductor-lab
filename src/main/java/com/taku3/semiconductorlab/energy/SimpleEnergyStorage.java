package com.taku3.semiconductorlab.energy;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** ふつうの電池・バッファ。容量と最大出入り速度を持つ */
public class SimpleEnergyStorage implements EnergyStorage {
    private final long capacity, maxInsert, maxExtract;
    private final int voltage;
    private long stored;
    private final Runnable onChange;

    public SimpleEnergyStorage(long capacity, long maxInsert, long maxExtract, int voltage, Runnable onChange) {
        this.capacity = capacity; this.maxInsert = maxInsert; this.maxExtract = maxExtract; this.voltage = voltage; this.onChange = onChange;
    }
    @Override public long getStoredJ() { return stored; }
    @Override public long getCapacityJ() { return capacity; }
    @Override public int getVoltage() { return voltage; }
    @Override public boolean canInsert()  { return maxInsert > 0; }
    @Override public boolean canExtract() { return maxExtract > 0; }

    @Override public long insert(long j, boolean simulate) {
        long n = Math.max(0, Math.min(Math.min(j, maxInsert), capacity - stored));
        if (!simulate && n > 0) { stored += n; onChange.run(); }
        return n;
    }
    @Override public long extract(long j, boolean simulate) {
        long n = Math.max(0, Math.min(Math.min(j, maxExtract), stored));
        if (!simulate && n > 0) { stored -= n; onChange.run(); }
        return n;
    }
    /** 自分で作った分を入れる（maxInsert を無視） */
    public void insertRaw(long j) { long n = Math.max(0, Math.min(j, capacity - stored)); if (n > 0) { stored += n; onChange.run(); } }
    public void save(ValueOutput out) { out.store("EnergyJ", Codec.LONG, stored); }
    public void load(ValueInput in)   { stored = Math.min(capacity, in.read("EnergyJ", Codec.LONG).orElse(0L)); }
}