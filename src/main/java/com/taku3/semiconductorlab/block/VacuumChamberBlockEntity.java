package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 真空容器 [5-11]（19世紀のガラスのベルジャー＋鉄の台板）。中の気圧 pressure（Pa＝パスカル）だけを持つ。
 * - 隣の手押しポンプ [5-01] を1回引くと、容器の空気の一部がシリンダーへ移って外に捨てられる（ボイルの法則）
 * - 隣のシュプレンゲル水銀ポンプ [5-15] は1秒ごとに引き続ける（0.05Pa まで）
 * - すり合わせ面から少しずつ空気が漏れ込む（1秒に {@link #LEAK_PA_PER_SECOND} Pa）
 * - 圧力は見た目では分からない。真空計 [5-10] を当てると読める
 */
public class VacuumChamberBlockEntity extends BlockEntity {
    /** 1気圧（Pa）。760mmHg */
    public static final double ATM = 101_325;
    /** 1 mmHg（＝1 Torr）は何 Pa か */
    public static final double PA_PER_MMHG = 133.322;
    /** 漏れ込み（Pa/秒）。グリースを塗ったすり合わせ面の小さな漏れ（1分で約0.1Pa）。シュプレンゲル[5-15]が 0.1Pa を保てる大きさ */
    public static final double LEAK_PA_PER_SECOND = 0.002;

    private double pressure = ATM;

    public VacuumChamberBlockEntity(BlockPos pos, BlockState state) { super(ModMachines.VACUUM_CHAMBER.type, pos, state); }

    public double getPressure() { return pressure; }
    public void setPressure(double pa) { pressure = Math.max(0, Math.min(ATM, pa)); setChanged(); }

    /**
     * ポンプを1回引く。容器の体積 V、シリンダー V_c とすると、1回で p → p·V/(V+V_c)（ratio）。
     * ただしシリンダーの「すき間（死容積）」に残る空気が戻るので、limit より下には下がらない：
     * p' = limit + (p − limit)·ratio
     * @return 引いたあとの圧力
     */
    public double pumpStroke(double limit, double ratio) {
        if (pressure > limit) setPressure(limit + (pressure - limit) * ratio);
        return pressure;
    }

    /** 外の空気を少し入れる（fraction＝大気圧との差のうち何割を戻すか）。封止台で次の管を付け替えるときなど */
    public void vent(double fraction) { setPressure(pressure + (ATM - pressure) * fraction); }

    /** 隣（6方向）にある真空容器を1つ探す。なければ null */
    public static VacuumChamberBlockEntity findAdjacent(Level level, BlockPos pos) {
        for (Direction d : Direction.values())
            if (level.getBlockEntity(pos.relative(d)) instanceof VacuumChamberBlockEntity c) return c;
        return null;
    }

    /** 表示用：1000Pa 以上は kPa、未満は Pa */
    public static String formatPa(double pa) {
        if (pa >= 1000) return String.format("%.1f kPa", pa / 1000);
        if (pa >= 10) return String.format("%.0f Pa", pa);
        return String.format("%.2g Pa", pa);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VacuumChamberBlockEntity be) {
        if (level.isClientSide() || be.pressure >= ATM) return;
        if (level.getGameTime() % 20 == 0) be.setPressure(be.pressure + LEAK_PA_PER_SECOND);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putDouble("Pressure", pressure); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); pressure = in.getDoubleOr("Pressure", ATM); }
}
