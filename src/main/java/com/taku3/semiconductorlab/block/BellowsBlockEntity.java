package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.energy.WaterWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ふいご [3-04]（送風）。炉の横に置き、素手で右クリック＝踏むたびに風がたまり、少しずつ抜けていく。
 * 横に回っている水車 [4-01] があれば、水車ふいご（後漢の杜詩、31年）になってずっと風を送る。
 * 横の炉（塊鉄炉 [1-06]・高炉 [1-09]）は {@link #airflow(Level, BlockPos)} で「どれだけ風が来ているか」（0〜1）を読み、温度が上がる。
 * 本物：送風で酸素を多く送ると木炭がよく燃え、約1200〜1300℃に（TECH_TREE の決定：ふいご＋木炭 ≈1300℃）。
 */
public class BellowsBlockEntity extends BlockEntity {
    /** 1回踏むとたまる風・たまる上限・1 tick に抜ける風。風 {@link #FULL} 以上で送風 100% */
    public static final int PER_PUMP = 20, MAX_AIR = 40, FULL = 20;

    private int air;

    public BellowsBlockEntity(BlockPos pos, BlockState state) { super(Heat.BELLOWS.type, pos, state); }

    public int air() { return air; }

    /** 踏む（素手で右クリック） */
    public void pump() {
        air = Math.min(MAX_AIR, air + PER_PUMP);
        setChanged();
    }

    /** 横に回っている水車があるか（水車ふいご） */
    public static boolean waterDriven(Level level, BlockPos pos) {
        for (Direction d : Direction.values())
            if (level.getBlockEntity(pos.relative(d)) instanceof WaterWheelBlockEntity w && WaterWheelBlockEntity.turning(level, w.getBlockPos())) return true;
        return false;
    }

    /** このふいごの送風（0〜1） */
    public double blow() {
        if (level != null && waterDriven(level, worldPosition)) return 1.0;
        return Math.min(1.0, air / (double) FULL);
    }

    /** 炉のまわりのふいごから来る風（0〜1、いちばん強いもの） */
    public static double airflow(Level level, BlockPos furnace) {
        double best = 0;
        for (Direction d : Direction.values())
            if (level.getBlockEntity(furnace.relative(d)) instanceof BellowsBlockEntity b) best = Math.max(best, b.blow());
        return best;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity be) {
        if (level.isClientSide() || be.air <= 0) return;
        be.air--;   // 風は少しずつ抜ける
        be.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Air", air); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); air = in.getIntOr("Air", 0); }
}
