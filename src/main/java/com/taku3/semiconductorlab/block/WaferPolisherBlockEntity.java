package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 研磨機 [9-13]（IMP4-01）：回る鋼の定盤にウェハーを押し当て、まず砥粒（アルミナかベンガラ）でラッピングして平らにし、
 * 最後にコロイダルシリカ（水に分散したシリカの微粒子、1963 ウォルシュとハーゾグ）で化学＋機械の鏡面研磨をする。
 * 加工中のウェハー（process_wafer）1〜8枚 ＋ 砥粒 1 ＋ コロイダルシリカ 1（空のガラス瓶は上に戻る）
 * → 作った層を全部けずり落とし、最初からやり直せるウェハー（ChipOps.reclaim、本物の再生ウェハー。2回まで）。
 * ⚠️ 電力（1kW）・時間（60秒）はゲームの値。平らさ（平坦度）はまだ表さない（wafer_polisher.placeholder）。
 */
public class WaferPolisherBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 60;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_ABRASIVE = 1, SLOT_SILICA = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, ChipOps::isProcessWafer)
            .in(SLOT_ABRASIVE, 8, 35, WaferPolisherBlockEntity::isAbrasive)
            .in(SLOT_SILICA, 8, 53, s -> s.is(Chemistry.COLLOIDAL_SILICA))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.wafer_polisher.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.wafer_polisher.need_input", "gui.semiconductor-lab.wafer_polisher.need_power",
                    "gui.semiconductor-lab.wafer_polisher.running", "gui.semiconductor-lab.wafer_polisher.too_many")
            .bottom("recipe", "gui.semiconductor-lab.wafer_polisher.recipe")
            .build();

    public WaferPolisherBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.WAFER_POLISHER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** ラッピングの砥粒：アルミナ [1-14] かベンガラ [7-03] */
    public static boolean isAbrasive(ItemStack s) { return s.is(Metallurgy.ALUMINA) || s.is(Optics.ROUGE); }

    /** 1回に磨く枚数（1〜8） */
    public static int batch(ItemStack w) { return ChipOps.isProcessWafer(w) ? Math.min(w.getCount(), WAFERS_PER_RUN) : 0; }

    public static ItemStack product(ItemStack w) {
        int n = batch(w);
        if (n == 0) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.reclaim(w.copyWithCount(n));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && isAbrasive(container.getItem(SLOT_ABRASIVE)) && container.getItem(SLOT_SILICA).is(Chemistry.COLLOIDAL_SILICA)
                && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        container.getItem(SLOT_ABRASIVE).shrink(1);
        ItemStack sil = container.getItem(SLOT_SILICA);
        Vessel.returnEmpty(level, worldPosition, sil, 1);   // 空のガラス瓶は上に戻る
        sil.shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
