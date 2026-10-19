package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 鉛蓄電池 [4-07]（プランテ 1859）。電気を化学変化として貯める（充電：2PbSO4 + 2H2O → Pb + PbO2 + 2H2SO4、放電はその逆）。
 * ゲーム：容量 1MJ。どの面からも充電・放電できる（タスク06：ケーブルのネットワークが「発電が余れば充電、足りなければ放電」を決める）。
 * 充電は 85% だけ貯まる（残りは熱と、水の電気分解で出るガス）。放電・充電とも最大 10kW。
 * ケーブルなしで直接となりの機械（受け取るだけのもの）に触れていれば、そこへも押し出す。
 * 壊すと残量はアイテムのデータコンポーネント battery_energy に残り、置き直すと戻る。
 */
public class LeadAcidBatteryBlockEntity extends BlockEntity {
    public static final long CAPACITY = 1_000_000;                                    // 1 MJ
    public static final double MAX_WATTS = 10_000;                                    // 充電・放電とも 10 kW
    public static final long MAX_PER_TICK = EnergyStorage.wattsToJoulesPerTick(MAX_WATTS);
    public static final int CHARGE_EFFICIENCY_PERCENT = 85;

    private final SimpleEnergyStorage cell = new SimpleEnergyStorage(CAPACITY, 0, 0, 0, this::setChanged);
    private long chargedThisTick, dischargedThisTick, lastChargeJ, lastDischargeJ;

    /** 全面共通の受け口：入れる（85% が貯まる）・取り出す の両方ができる。1 tick の上限はそれぞれ 10kW 分 */
    private final EnergyStorage terminal = new EnergyStorage() {
        @Override public long getStoredJ() { return cell.getStoredJ(); }
        @Override public long getCapacityJ() { return CAPACITY; }
        @Override public long insert(long j, boolean simulate) {
            long room = (CAPACITY - cell.getStoredJ()) * 100 / CHARGE_EFFICIENCY_PERCENT;   // 貯まる余地を「入力側の J」に直す
            long n = Math.max(0, Math.min(Math.min(j, MAX_PER_TICK - chargedThisTick), room));
            if (!simulate && n > 0) { cell.insertRaw(n * CHARGE_EFFICIENCY_PERCENT / 100); chargedThisTick += n; }
            return n;
        }
        @Override public long extract(long j, boolean simulate) {
            long n = Math.max(0, Math.min(Math.min(j, MAX_PER_TICK - dischargedThisTick), cell.getStoredJ()));
            if (!simulate && n > 0) { cell.extractRaw(n); dischargedThisTick += n; }
            return n;
        }
    };

    public LeadAcidBatteryBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.LEAD_ACID_BATTERY, pos, state); }

    public Direction front() { return getBlockState().getValue(HorizontalDirectionalBlock.FACING); }
    /** どの面も同じ受け口（充放電とも可） */
    public EnergyStorage storageFor(Direction side) { return terminal; }
    public long getStoredJ() { return cell.getStoredJ(); }
    public double getLastChargeWatts() { return lastChargeJ * 20.0; }
    public double getLastDischargeWatts() { return lastDischargeJ * 20.0; }

    public static void tick(Level level, BlockPos pos, BlockState state, LeadAcidBatteryBlockEntity be) {
        if (level.isClientSide()) return;
        // ケーブルなしで直接触れている「受け取るだけ」の機械へ押し出す（電池・発電機・ケーブルには押さない＝行ったり来たりしない）
        for (Direction d : Direction.values()) {
            BlockPos np = pos.relative(d);
            if (!level.isLoaded(np) || level.getBlockEntity(np) instanceof CableBlockEntity) continue;
            EnergyStorage n = EnergyApi.ENERGY.find(level, np, d.getOpposite());
            if (n != null && n.canInsert() && !n.canExtract()) CableBlockEntity.move(be.terminal, n, MAX_PER_TICK);
        }
        be.lastDischargeJ = be.dischargedThisTick;
        be.lastChargeJ = be.chargedThisTick;
        be.chargedThisTick = 0;
        be.dischargedThisTick = 0;
        ConsumerBlockEntity.setLit(level, pos, state, be.lastDischargeJ > 0);   // 放電中は点灯
    }

    /** テスト・コマンド用：残量を直接決める */
    public void setStoredForTest(long j) { cell.extractRaw(cell.getStoredJ()); cell.insertRaw(j); }

    // ---- 残量をアイテムに持たせる（壊す → アイテム → 置き直す）
    @Override protected void collectImplicitComponents(DataComponentMap.Builder b) {
        super.collectImplicitComponents(b);
        if (cell.getStoredJ() > 0) b.set(ModDataComponents.BATTERY_ENERGY, cell.getStoredJ());
    }
    @Override protected void applyImplicitComponents(DataComponentGetter g) {
        super.applyImplicitComponents(g);
        Long j = g.get(ModDataComponents.BATTERY_ENERGY);
        if (j != null) setStoredForTest(Math.min(CAPACITY, j));
    }
    @Override public void removeComponentsFromTag(ValueOutput out) { super.removeComponentsFromTag(out); out.discard("EnergyJ"); }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); cell.save(out); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); cell.load(in); }
}
