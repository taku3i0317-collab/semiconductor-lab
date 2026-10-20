package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 銅ケーブル。タスク06 から「導体」になった（電気を貯めない）。つながったケーブル群は {@link CableNetwork} が
 * まとめて1つのネットワークとして扱い、毎 tick 発電機・電池 → 機械・電池へ直接配る。
 * 損失はネットワークの最長経路 1本につき 1%、通過上限は 1MW（本物は I²R。太い銅線と高電圧で損失を減らす）。
 */
public class CableBlockEntity extends BlockEntity {
    public static final long MAX_TRANSFER = EnergyStorage.wattsToJoulesPerTick(1_000_000); // 1 MW/本
    public static final int LOSS_PERCENT = 1;

    /** 直近の配電でネットワークを流れた量（右クリック表示用。リーダー以外は0のことがある） */
    private long lastDeliveredJ;

    public CableBlockEntity(BlockPos pos, BlockState state) { super(Power.COPPER_CABLE.type, pos, state); }
    protected CableBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    /** 電圧の段（v0.4）：銅ケーブル＝0（低圧）。高圧の電線は 1 */
    public int voltageClass() { return 0; }

    public static void tick(Level level, BlockPos pos, BlockState state, CableBlockEntity be) {
        if (level.isClientSide()) return;
        CableNetwork net = CableNetwork.get(level, pos);
        if (net == null || !net.isLeader(pos)) return;   // 1つのネットワークにつき、リーダーの1本だけが配電する
        net.distribute();
        be.lastDeliveredJ = net.lastDeliveredJ;
    }

    public long getLastDeliveredJ() { return lastDeliveredJ; }

    /** from → to へ直接送る（ケーブルなしで隣り合った発電機・電池 → 機械）。接触 1% の損失 */
    static void move(EnergyStorage from, EnergyStorage to, long amount) {
        if (!Current.compatible(from.current(), to.current()) || from.voltageClass() != to.voltageClass()) return;   // v0.4：交流と直流・ちがう電圧はつながない
        amount = Math.min(amount, MAX_TRANSFER);
        if (amount <= 0) return;
        long accepted = to.insert(amount, true);
        long taken = from.extract(accepted, false);
        to.insert(taken - taken * LOSS_PERCENT / 100, false);
    }

    @Override public void setLevel(Level level) { super.setLevel(level); CableNetwork.invalidate(level); }
    @Override public void setRemoved() { if (level != null) CableNetwork.invalidate(level); super.setRemoved(); }
}
