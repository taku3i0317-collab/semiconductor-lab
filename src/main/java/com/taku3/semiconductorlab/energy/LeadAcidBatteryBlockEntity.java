package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.content.Power;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 鉛蓄電池 [4-07]（プランテ 1859）。電気を化学変化として貯める（充電：2PbSO4 + 2H2O → Pb + PbO2 + 2H2SO4、放電はその逆）。
 * ゲーム：容量 1MJ。充電は 85% だけ貯まる（残りは熱と、水の電気分解で出るガス）。放電・充電とも最大 10kW。
 * 充放電・残量の保存などのしくみは親の {@link RechargeableBatteryBlockEntity}（S-011 の3 で分けた）。
 */
public class LeadAcidBatteryBlockEntity extends RechargeableBatteryBlockEntity {
    public static final Spec SPEC = new Spec(1_000_000, 10_000, 85);
    public static final long CAPACITY = SPEC.capacityJ();                             // 1 MJ
    public static final double MAX_WATTS = SPEC.maxWatts();                           // 充電・放電とも 10 kW
    public static final long MAX_PER_TICK = SPEC.maxPerTick();
    public static final int CHARGE_EFFICIENCY_PERCENT = SPEC.chargeEfficiencyPercent();

    public LeadAcidBatteryBlockEntity(BlockPos pos, BlockState state) { super(Power.LEAD_ACID_BATTERY.type, pos, state, SPEC); }

    public static void tick(Level level, BlockPos pos, BlockState state, LeadAcidBatteryBlockEntity be) { tickBattery(level, pos, state, be); }
}
