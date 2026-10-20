package com.taku3.semiconductorlab.energy;

import com.taku3.semiconductorlab.machine.MachineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 電気を「使う」機械の土台（GeneratorBlockEntity の逆）。
 * ケーブル・発電機から押し込まれた J を貯め、材料がそろっていれば毎 tick workWatts() 分を使って進む。
 * 電気が足りない tick は進捗が止まる（リセットはしない）。材料がなくなったら進捗は 0 に戻る。
 * サブクラスが書くのは「何W使うか」「何tickかかるか」「材料はそろっているか」「完成したら何を出すか」の4つだけ。
 */
public abstract class ConsumerBlockEntity extends BlockEntity implements MachineBE {
    // GUI へ送る数値（同期は short＝最大32767 なので、W は 0.1kW 単位、J は kJ 単位に縮める）
    public static final int DATA_PROGRESS = 0, DATA_TOTAL = 1, DATA_DECI_KW = 2, DATA_KJ = 3, DATA_READY = 4, DATA_COUNT = 5;

    protected final SimpleEnergyStorage storage;
    protected final SimpleContainer container;
    protected int progress;
    private double lastWatts;
    private boolean ready;

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int i) { return switch (i) {
            case DATA_PROGRESS -> progress; case DATA_TOTAL -> workTicks();
            case DATA_DECI_KW -> (int) Math.round(lastWatts / 100); case DATA_KJ -> (int) (storage.getStoredJ() / 1000);
            case DATA_READY -> ready ? 1 : 0; default -> extraData(i); }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT + extraDataCount(); }
    };

    /**
     * @param slots    アイテムスロット数
     * @param bufferJ  内部バッファ（J）。数秒分あれば電気の揺らぎを吸収できる
     * @param maxWatts 受け取れる最大電力。1tick にこの4倍まで受け取る（発電機の押し出しと同じ倍率）
     */
    protected ConsumerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, long bufferJ, double maxWatts) {
        super(type, pos, state);
        storage = new SimpleEnergyStorage(bufferJ, EnergyStorage.wattsToJoulesPerTick(maxWatts) * 4, 0, 0, this::setChanged).withCurrent(this::acceptsCurrent);
        container = new SimpleContainer(slots) { @Override public void setChanged() { super.setChanged(); ConsumerBlockEntity.this.setChanged(); } };
    }

    /** 動作中に使う電力（W） */
    protected abstract double workWatts();

    /** 受けられる電流（v0.4、S-007）：ふつうの機械（電熱・モーター）は ANY。電気分解など直流しか使えない機械は DC を返す（交流の網からはもらえない＝整流器 [4-11] を通す） */
    protected Current acceptsCurrent() { return Current.ANY; }

    // ===== [8-20] 玉軸受（IMP2 O-013、A と合意）：モーターで回す機械だけ。ここ1か所に機械の ID を並べる =====
    /** 玉軸受を入れられる機械（ブロックエンティティの ID）。炉・槽は入れない */
    public static final java.util.Set<String> BEARING_MACHINES = java.util.Set.of("lathe", "compressor", "wafer_saw", "wafer_polisher", "spin_coater", "cz_puller");
    /** 入っている玉軸受の段階（0＝なし＝すべり軸受。保存キー "Bearing"） */
    private int bearingGrade;

    /** 玉軸受を入れられるか（BEARING_MACHINES を見る） */
    public boolean acceptsBearing() {
        var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getType());
        return key != null && BEARING_MACHINES.contains(key.getPath());
    }
    public int bearingGrade() { return bearingGrade; }
    /** 玉軸受を入れる（前の物は返す。入れられなければ false） */
    public boolean installBearing(int grade) {
        if (!acceptsBearing() || grade <= 0) return false;
        bearingGrade = grade;
        setChanged();
        return true;
    }
    /** 実際に使う電力（W）：仕事の電力 × 玉軸受の倍率。仕事の時間は変えない */
    protected double drawWatts() { return workWatts() * com.taku3.semiconductorlab.item.BallBearingItem.powerFactor(bearingGrade); }
    /** 1回の処理にかかる tick 数（電気が足りている tick だけ数える） */
    protected abstract int workTicks();
    /** 材料がそろい、出口に空きがあるか */
    protected abstract boolean canWork();
    /** 1回分が完成したときに材料を減らし、生成物を出す */
    protected abstract void finishWork();

    public EnergyStorage getStorage() { return storage; }
    @Override public SimpleContainer getContainer() { return container; }
    /** GUI へ同期する数値（追加の数値がある機械は extraData で足す。列ごと差し替える機械は上書きする） */
    @Override public ContainerData getMenuData() { return data; }
    public double getLastWatts() { return lastWatts; }

    /** GUI へ追加で送る数値の個数（DATA_COUNT 番以降）。既定は 0 */
    protected int extraDataCount() { return 0; }
    /** 追加の数値（i は DATA_COUNT 以上） */
    protected int extraData(int i) { return 0; }
    protected void setLastWatts(double w) { lastWatts = w; }
    protected void setReady(boolean r) { ready = r; }

    /** 毎 tick、材料の確認より前に呼ばれる（隣のガスホルダーから吸うなど）。既定は何もしない */
    protected void beforeWork(Level level, BlockPos pos) {}

    public static void tick(Level level, BlockPos pos, BlockState state, ConsumerBlockEntity be) {
        if (level.isClientSide()) return;
        be.serverTick(level, pos, state);
    }

    /** サーバー側の1 tick。ふつうはこのままでよい（アチソン炉のように温度を持つ機械は上書きする） */
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        ConsumerBlockEntity be = this;
        be.beforeWork(level, pos);
        be.ready = be.canWork();
        double used = 0;
        if (be.ready) {
            long need = EnergyStorage.wattsToJoulesPerTick(be.drawWatts());
            if (be.storage.getStoredJ() >= need) {
                be.storage.extractRaw(need);
                used = be.drawWatts();
                if (++be.progress >= be.workTicks()) { be.progress = 0; be.finishWork(); }
                be.setChanged();
            }
        } else if (be.progress != 0) { be.progress = 0; be.setChanged(); }
        be.lastWatts = used;
        // ブロックに LIT（点灯）があれば「今電気を使って動いている」を表示
        setLit(level, pos, state, used > 0);
    }

    protected static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT) != lit)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    /** out に add を足せるか（同じアイテムで、スタック上限を超えない） */
    protected static boolean canMerge(ItemStack out, ItemStack add) {
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(out, add) && out.getCount() + add.getCount() <= out.getMaxStackSize();
    }
    /** スロットに add を足す（canMerge で確認済みの前提） */
    protected void merge(int slot, ItemStack add) {
        ItemStack out = container.getItem(slot);
        if (out.isEmpty()) container.setItem(slot, add.copy());
        else { out.grow(add.getCount()); container.setChanged(); }
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, container.getItems());
            if (bearingGrade > 0) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), com.taku3.semiconductorlab.item.BallBearingItem.of(bearingGrade));   // [8-20] 玉軸受も落とす
        }
        super.preRemoveSideEffects(pos, state);
    }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); storage.save(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Progress", progress); if (bearingGrade > 0) out.putInt("Bearing", bearingGrade); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); storage.load(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); progress = in.getIntOr("Progress", 0); bearingGrade = in.getIntOr("Bearing", 0); }
}
