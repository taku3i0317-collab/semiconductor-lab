package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * シュプレンゲル水銀ポンプ [5-15]（ヘルマン・シュプレンゲル 1865）。
 * 細いガラス管に水銀を滴にして落とすと、滴と滴のあいだに気体が閉じ込められて下へ運び出される。
 * 水銀 {@link #MERCURY_NEEDED} 個を入れると、隣の真空容器を1秒ごとに p' = LIMIT + (p − LIMIT)·RATIO で引き続ける。
 * 水銀は下の受け皿から上の溜めに戻して使う（循環・減らない。本物は人が注ぎ戻した＝ここは省略）。電気は使わない。
 * 到達 {@link #LIMIT_PA}（約 4×10⁻⁴ Torr）：シュプレンゲル本人の見積もり 10⁻³ Torr と、マクラウド計（1874）で測られた
 * 直列・長時間の 10⁻⁴〜10⁻⁶ Torr のあいだ（1台・短時間の値）。速さは本物（0.5L を約20分）を短縮（SOURCES.md）。
 */
public class SprengelPumpBlockEntity extends BlockEntity {
    /** 到達圧力（Pa）。空気（永久気体）の分。水銀自身の蒸気（20℃で約0.2Pa）は省略 */
    public static final double LIMIT_PA = 0.05;
    /** 1秒で残る割合。1気圧 → 0.1Pa まで約2分、手押しポンプで 1kPa にしてからなら約1分半 */
    public static final double RATIO = 0.9;
    /** 動かすのに要る水銀（個） */
    public static final int MERCURY_NEEDED = 4;

    private int mercury;

    public SprengelPumpBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.SPRENGEL_PUMP, pos, state); }

    public int getMercury() { return mercury; }
    public boolean isReady() { return mercury >= MERCURY_NEEDED; }

    /** 水銀を1個入れる。いっぱいなら false */
    public boolean addMercury() {
        if (mercury >= MERCURY_NEEDED) return false;
        mercury++;
        setChanged();
        return true;
    }

    /** 1秒ぶん引く（テストからも呼ぶ）。水銀が足りない・容器がなければ null */
    public VacuumChamberBlockEntity pumpOnce() {
        if (level == null || !isReady()) return null;
        VacuumChamberBlockEntity c = VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        if (c != null) c.pumpStroke(LIMIT_PA, RATIO);
        return c;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SprengelPumpBlockEntity be) {
        if (level.isClientSide() || level.getGameTime() % 20 != 0) return;
        be.pumpOnce();
    }

    /** 壊したら入れた水銀を落とす */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && mercury > 0) Block.popResource(level, pos, new ItemStack(ModItems.MERCURY, mercury));
        super.preRemoveSideEffects(pos, state);
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Mercury", mercury); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); mercury = in.getIntOr("Mercury", 0); }
}
