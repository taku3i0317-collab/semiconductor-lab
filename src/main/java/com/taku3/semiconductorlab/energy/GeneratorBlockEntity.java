package com.taku3.semiconductorlab.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 発電機の土台（機械テンプレートの原型）。毎tick J を作り、隣へ押し出す。サブクラスは「今何W出せるか」だけ書く */
public abstract class GeneratorBlockEntity extends BlockEntity {
    protected final SimpleEnergyStorage storage;
    private double lastWatts;
    /** [4-05] 自励式ダイナモ（ジーメンス1866）に積み替えたか。true なら出力2倍（燃料も2倍の速さで使う） */
    private boolean selfExcited;
    /** 交流発電機（v0.4、S-007）を積んだか。true なら交流を出す（出力は自励式と同じ2倍：界磁は自分の直流の励磁機） */
    private boolean alternator;

    protected GeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long bufferJ, double maxWatts) {
        super(type, pos, state);
        long perTick = EnergyStorage.wattsToJoulesPerTick(maxWatts);
        storage = new SimpleEnergyStorage(bufferJ, 0, perTick * 4, 0, this::setChanged).withCurrent(() -> alternator ? Current.AC : baseCurrent());
    }

    /** 今この tick に作れる電力（W）。0 なら停止中 */
    protected abstract double produceWatts(Level level, BlockPos pos, BlockState state);
    public double getLastWatts() { return lastWatts; }
    public EnergyStorage getStorage() { return storage; }

    /** 自励式ダイナモに積み替えられる発電機か（水車・蒸気機関＝はじめは永久磁石のダイナモ）。タービンは最初から自励式なので false */
    public boolean supportsDynamoUpgrade() { return false; }
    public boolean isSelfExcited() { return selfExcited; }
    /** ダイナモの倍率。永久磁石＝1、自励式（電磁石）＝2 */
    public int dynamoFactor() { return selfExcited || alternator ? 2 : 1; }
    /** 交流発電機を積んでいないときの電流（ダイナモ・太陽電池＝直流。試験用の電源は ANY） */
    protected Current baseCurrent() { return Current.DC; }
    /** 交流発電機を積めるか（回る発電機だけ。太陽電池・試験用の電源は false） */
    public boolean supportsAlternator() { return true; }
    public boolean hasAlternator() { return alternator; }
    /** 交流発電機を積む。積めたら true。自励式ダイナモが積んであれば外れる（戻すのは呼ぶ側、wasSelfExcited で分かる） */
    public boolean installAlternator() {
        if (!supportsAlternator() || alternator) return false;
        alternator = true;
        selfExcited = false;
        setChanged();
        return true;
    }
    /** 自励式ダイナモを取り付ける。取り付けられたら true（アイテムを1個減らすのは呼ぶ側） */
    public boolean installSelfExcitedDynamo() {
        if (!supportsDynamoUpgrade() || selfExcited) return false;
        selfExcited = true;
        setChanged();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (level.isClientSide()) return;
        be.lastWatts = be.produceWatts(level, pos, state);
        if (be.lastWatts > 0) be.storage.insertRaw(EnergyStorage.wattsToJoulesPerTick(be.lastWatts));
        for (Direction d : Direction.values()) {
            EnergyStorage n = EnergyApi.ENERGY.find(level, pos.relative(d), d.getOpposite());
            if (n != null && n.canInsert()) CableBlockEntity.move(be.storage, n, be.storage.getStoredJ());
        }
    }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); storage.save(out); out.putBoolean("SelfExcited", selfExcited); out.putBoolean("Alternator", alternator); }
    @Override protected void loadAdditional(ValueInput in)   { super.loadAdditional(in); storage.load(in); selfExcited = in.getBooleanOr("SelfExcited", false); alternator = in.getBooleanOr("Alternator", false); }
    /** 壊したら、取り付けた自励式ダイナモを落とす（ブロック本体はふつうのルートテーブルで落ちる） */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (selfExcited && level != null) net.minecraft.world.level.block.Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Power.SELF_EXCITED_DYNAMO));
        if (alternator && level != null) net.minecraft.world.level.block.Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Power.ALTERNATOR));
        super.preRemoveSideEffects(pos, state);
    }
}