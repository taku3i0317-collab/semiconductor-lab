package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.SemiconductorLab;
import com.taku3.semiconductorlab.block.LeadAcidBatteryBlock;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * 充電池の共通の親（S-011 の3、鉛蓄電池 [4-07] を分けた。D の [4-15] リチウムイオンなどが継承する）。
 * 電気を化学変化として貯め、どの面からも充電・放電できる（ケーブルのネットワークが「発電が余れば充電、足りなければ放電」を決める）。直流だけ。
 * 電池ごとのちがいは {@link Spec}（容量・充放電の上限 W・充電の効率 %）。ケーブルなしで直接となりの「受け取るだけ」の機械にも押し出す。
 * 壊すと残量はアイテムのデータコンポーネント battery_energy に残り、置き直すと戻る（ツールチップは {@link #capacityOf} で容量を出す）。
 * <p>新しい電池の作り方：① このクラスを継承し、{@code public static final Spec SPEC = new Spec(...)} とコンストラクタ {@code super(<幹>.X.type, pos, state, SPEC)}。
 * ② content/<幹>.java に {@code RechargeableBatteryBlockEntity.machine("id", XBlockEntity::new, XBlockEntity.SPEC, props)} の1行。
 * ③ lang に message.semiconductor-lab.<id>.status（右クリックの表示「%s / %s（充電 %s、放電 %s）」）
 */
public abstract class RechargeableBatteryBlockEntity extends BlockEntity {
    /** 電池の性質：容量（J）・充電と放電の上限（W）・充電の効率（%、残りは熱など） */
    public record Spec(long capacityJ, double maxWatts, int chargeEfficiencyPercent) {
        public long maxPerTick() { return EnergyStorage.wattsToJoulesPerTick(maxWatts); }
    }

    private static final Map<String, Long> CAPACITY_BY_ID = new HashMap<>();

    /** アイテムの ID（"semiconductor-lab:lead_acid_battery" など）から容量。知らない ID は 0 */
    public static long capacityOf(String itemId) { return CAPACITY_BY_ID.getOrDefault(itemId, 0L); }

    /** 電池を中に持つアイテム（スマホ [B-10] など）の容量を登録（ツールチップの「残量 / 容量」） */
    public static void registerCapacity(String itemId, long capacityJ) { CAPACITY_BY_ID.put(itemId, capacityJ); }

    /** 電池の機械の1行（ブロックは右クリックで残量、全面で充放電、向きと LIT＝放電中） */
    public static <T extends RechargeableBatteryBlockEntity> Machine<T> machine(String id, net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory<T> factory, Spec spec, BlockBehaviour.Properties props) {
        CAPACITY_BY_ID.put(SemiconductorLab.MOD_ID + ":" + id, spec.capacityJ());
        return Machine.builder(id, factory).block(LeadAcidBatteryBlock::new)
                .ticker((l, p, s, be) -> tickBattery(l, p, s, be)).energy((be, dir) -> be.storageFor(dir))
                .props(props).build();
    }

    public final Spec spec;
    private final SimpleEnergyStorage cell;
    private long chargedThisTick, dischargedThisTick, lastChargeJ, lastDischargeJ;

    /** 全面共通の受け口：入れる（効率の分だけ貯まる）・取り出す の両方ができる。1 tick の上限はそれぞれ maxWatts 分 */
    private final EnergyStorage terminal = new EnergyStorage() {
        @Override public long getStoredJ() { return cell.getStoredJ(); }
        @Override public long getCapacityJ() { return spec.capacityJ(); }
        @Override public Current current() { return Current.DC; }   // 電池は直流だけ（v0.4：交流の網では充電・放電しない）
        @Override public long insert(long j, boolean simulate) {
            long room = (spec.capacityJ() - cell.getStoredJ()) * 100 / spec.chargeEfficiencyPercent();   // 貯まる余地を「入力側の J」に直す
            long n = Math.max(0, Math.min(Math.min(j, spec.maxPerTick() - chargedThisTick), room));
            if (!simulate && n > 0) { cell.insertRaw(n * spec.chargeEfficiencyPercent() / 100); chargedThisTick += n; }
            return n;
        }
        @Override public long extract(long j, boolean simulate) {
            long n = Math.max(0, Math.min(Math.min(j, spec.maxPerTick() - dischargedThisTick), cell.getStoredJ()));
            if (!simulate && n > 0) { cell.extractRaw(n); dischargedThisTick += n; }
            return n;
        }
    };

    protected RechargeableBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Spec spec) {
        super(type, pos, state);
        this.spec = spec;
        this.cell = new SimpleEnergyStorage(spec.capacityJ(), 0, 0, 0, this::setChanged);
    }

    public Direction front() { return getBlockState().getValue(HorizontalDirectionalBlock.FACING); }
    /** どの面も同じ受け口（充放電とも可） */
    public EnergyStorage storageFor(Direction side) { return terminal; }
    public long getStoredJ() { return cell.getStoredJ(); }
    public long getCapacityJ() { return spec.capacityJ(); }
    public double getLastChargeWatts() { return lastChargeJ * 20.0; }
    public double getLastDischargeWatts() { return lastDischargeJ * 20.0; }

    public static void tickBattery(Level level, BlockPos pos, BlockState state, RechargeableBatteryBlockEntity be) {
        if (level.isClientSide()) return;
        // ケーブルなしで直接触れている「受け取るだけ」の機械へ押し出す（電池・発電機・ケーブルには押さない＝行ったり来たりしない）
        for (Direction d : Direction.values()) {
            BlockPos np = pos.relative(d);
            if (!level.isLoaded(np) || level.getBlockEntity(np) instanceof CableBlockEntity) continue;
            EnergyStorage n = EnergyApi.ENERGY.find(level, np, d.getOpposite());
            if (n != null && n.canInsert() && !n.canExtract()) CableBlockEntity.move(be.terminal, n, be.spec.maxPerTick());
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
        if (j != null) setStoredForTest(Math.min(spec.capacityJ(), j));
    }
    @Override public void removeComponentsFromTag(ValueOutput out) { super.removeComponentsFromTag(out); out.discard("EnergyJ"); }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); cell.save(out); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); cell.load(in); }
}
