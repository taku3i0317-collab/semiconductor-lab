package com.taku3.semiconductorlab.energy;

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
public abstract class ConsumerBlockEntity extends BlockEntity {
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
            case DATA_READY -> ready ? 1 : 0; default -> 0; }; }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return DATA_COUNT; }
    };

    /**
     * @param slots    アイテムスロット数
     * @param bufferJ  内部バッファ（J）。数秒分あれば電気の揺らぎを吸収できる
     * @param maxWatts 受け取れる最大電力。1tick にこの4倍まで受け取る（発電機の押し出しと同じ倍率）
     */
    protected ConsumerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots, long bufferJ, double maxWatts) {
        super(type, pos, state);
        storage = new SimpleEnergyStorage(bufferJ, EnergyStorage.wattsToJoulesPerTick(maxWatts) * 4, 0, 0, this::setChanged);
        container = new SimpleContainer(slots) { @Override public void setChanged() { super.setChanged(); ConsumerBlockEntity.this.setChanged(); } };
    }

    /** 動作中に使う電力（W） */
    protected abstract double workWatts();
    /** 1回の処理にかかる tick 数（電気が足りている tick だけ数える） */
    protected abstract int workTicks();
    /** 材料がそろい、出口に空きがあるか */
    protected abstract boolean canWork();
    /** 1回分が完成したときに材料を減らし、生成物を出す */
    protected abstract void finishWork();

    public EnergyStorage getStorage() { return storage; }
    public SimpleContainer getContainer() { return container; }
    public double getLastWatts() { return lastWatts; }

    public static void tick(Level level, BlockPos pos, BlockState state, ConsumerBlockEntity be) {
        if (level.isClientSide()) return;
        be.ready = be.canWork();
        double used = 0;
        if (be.ready) {
            long need = EnergyStorage.wattsToJoulesPerTick(be.workWatts());
            if (be.storage.getStoredJ() >= need) {
                be.storage.extractRaw(need);
                used = be.workWatts();
                if (++be.progress >= be.workTicks()) { be.progress = 0; be.finishWork(); }
                be.setChanged();
            }
        } else if (be.progress != 0) { be.progress = 0; be.setChanged(); }
        be.lastWatts = used;
        // ブロックに LIT（点灯）があれば「今電気を使って動いている」を表示
        boolean lit = used > 0;
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

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) { if (level != null) Containers.dropContents(level, pos, container.getItems()); super.preRemoveSideEffects(pos, state); }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); storage.save(out); ContainerHelper.saveAllItems(out, container.getItems()); out.putInt("Progress", progress); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); storage.load(in); container.getItems().replaceAll(s -> ItemStack.EMPTY); ContainerHelper.loadAllItems(in, container.getItems()); progress = in.getIntOr("Progress", 0); }
}
