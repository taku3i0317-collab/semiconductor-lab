package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 鉛蓄電池 [4-07]（プランテ 1859）。電気を化学変化として貯める（充電：2PbSO4 + 2H2O → Pb + PbO2 + 2H2SO4、放電はその逆）。
 * ゲーム：容量 1MJ。正面（端子）から放電して押し出し、ほかの5面から充電を受け取る。
 * 充電は 85% だけ貯まる（残りは熱と、水の電気分解で出るガス）。放電・充電とも最大 10kW。
 * 正面の受け口は「取り出し専用」なので、正面につないだケーブルから電気が逆流しない。
 */
public class LeadAcidBatteryBlockEntity extends BlockEntity {
    public static final long CAPACITY = 1_000_000;                                    // 1 MJ
    public static final double MAX_WATTS = 10_000;                                    // 充電・放電とも 10 kW
    public static final long MAX_PER_TICK = EnergyStorage.wattsToJoulesPerTick(MAX_WATTS);
    public static final int CHARGE_EFFICIENCY_PERCENT = 85;

    private final SimpleEnergyStorage cell = new SimpleEnergyStorage(CAPACITY, 0, 0, 0, this::setChanged);
    private long chargedThisTick, lastChargeJ, lastDischargeJ;

    /** 充電側（正面以外）：入れるだけ。入った量の 85% が貯まる */
    private final EnergyStorage input = new EnergyStorage() {
        @Override public long getStoredJ() { return cell.getStoredJ(); }
        @Override public long getCapacityJ() { return CAPACITY; }
        @Override public boolean canExtract() { return false; }
        @Override public long extract(long j, boolean simulate) { return 0; }
        @Override public long insert(long j, boolean simulate) {
            long room = (CAPACITY - cell.getStoredJ()) * 100 / CHARGE_EFFICIENCY_PERCENT;   // 貯まる余地を「入力側の J」に直す
            long n = Math.max(0, Math.min(Math.min(j, MAX_PER_TICK - chargedThisTick), room));
            if (!simulate && n > 0) { cell.insertRaw(n * CHARGE_EFFICIENCY_PERCENT / 100); chargedThisTick += n; }
            return n;
        }
    };
    /** 放電側（正面）：取り出すだけ。自分から隣へ押し出すのが基本だが、外から取られても同じ上限 */
    private final EnergyStorage output = new EnergyStorage() {
        @Override public long getStoredJ() { return cell.getStoredJ(); }
        @Override public long getCapacityJ() { return CAPACITY; }
        @Override public boolean canInsert() { return false; }
        @Override public long insert(long j, boolean simulate) { return 0; }
        @Override public long extract(long j, boolean simulate) {
            long n = Math.max(0, Math.min(Math.min(j, MAX_PER_TICK), cell.getStoredJ()));
            if (!simulate && n > 0) cell.extractRaw(n);
            return n;
        }
    };

    public LeadAcidBatteryBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.LEAD_ACID_BATTERY, pos, state); }

    public Direction front() { return getBlockState().getValue(HorizontalDirectionalBlock.FACING); }
    /** side＝このブロックのどの面か。正面＝放電端子、それ以外＝充電 */
    public EnergyStorage storageFor(Direction side) { return side == front() ? output : input; }
    public long getStoredJ() { return cell.getStoredJ(); }
    public double getLastChargeWatts() { return lastChargeJ * 20.0; }
    public double getLastDischargeWatts() { return lastDischargeJ * 20.0; }

    public static void tick(Level level, BlockPos pos, BlockState state, LeadAcidBatteryBlockEntity be) {
        if (level.isClientSide()) return;
        Direction f = be.front();
        long before = be.cell.getStoredJ();
        EnergyStorage n = EnergyApi.ENERGY.find(level, pos.relative(f), f.getOpposite());
        if (n != null && n.canInsert()) CableBlockEntity.move(be.output, n, MAX_PER_TICK);
        be.lastDischargeJ = before - be.cell.getStoredJ();
        be.lastChargeJ = be.chargedThisTick;
        be.chargedThisTick = 0;
        ConsumerBlockEntity.setLit(level, pos, state, be.lastDischargeJ > 0);   // 放電中は点灯
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); cell.save(out); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); cell.load(in); }
}
