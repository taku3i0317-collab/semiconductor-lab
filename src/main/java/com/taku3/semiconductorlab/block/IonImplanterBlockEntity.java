package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.Dopant;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.litho.ChipOps;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * イオン注入装置 [9-28]（1954 ショックレーの特許、1960〜70年代に量産へ。IMP4-01、O-011）：
 * ドーパントをイオンにし（イオン源）、質量分析の電磁石 [8-33] で入れたいイオン（¹¹B⁺・³¹P⁺）だけを選び、
 * コッククロフト・ウォルトン回路 [9-47] の高電圧で加速して、ウェハーに打ちこむ。現像したレジストの開いた所（なければ酸化膜の窓）にだけ入る。
 * 拡散とちがい、窓は閉じない・横に広がらない・量を決められる。ただし打ちこんだだけでは結晶がこわれていて効かない＝アニール [9-29] が要る。
 * 加工中のウェハー 1〜8枚 ＋ ドーパント源 1（B2O3＝p型・リン＝n型）→ 注入したウェハー（ChipOps.implant）。
 * 隣の真空容器が {@link #MAX_PA} 以下のときだけ動く（イオンが空気にぶつからないように）。
 * ⚠️ 電力（5kW）・時間（30秒）はゲームの値。本物のイオン源のガス（BF3・PH3）は危険なので固体の源で近似（ion_implanter.placeholder）。
 */
public class IonImplanterBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 30;
    public static final double MAX_PA = 0.01;
    public static final int WAFERS_PER_RUN = 8, DOSE_PER_RUN = 1;
    public static final int DATA_VACUUM = DATA_COUNT;
    public static final int SLOT_WAFER = 0, SLOT_SOURCE = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + 1)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_SOURCE, 8, 50, s -> Dopant.of(s) != null)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.ion_implanter.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.ion_implanter.need_input", "gui.semiconductor-lab.ion_implanter.cannot",
                    "gui.semiconductor-lab.ion_implanter.need_vacuum", "gui.semiconductor-lab.ion_implanter.need_power", "gui.semiconductor-lab.ion_implanter.running")
            .bottom("recipe", "gui.semiconductor-lab.ion_implanter.recipe")
            .build();

    public IonImplanterBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.ION_IMPLANTER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 注入したウェハー（1〜8枚）。注入できなければ EMPTY */
    public static ItemStack product(ItemStack w, ItemStack source) {
        Dopant d = Dopant.of(source);
        if (d == null || !ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.implant(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)), d.type, DOSE_PER_RUN);
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    public boolean vacuumOk() {
        VacuumChamberBlockEntity c = level == null ? null : VacuumChamberBlockEntity.findAdjacent(level, worldPosition);
        return c != null && c.getPressure() <= MAX_PA;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_SOURCE));
        return !out.isEmpty() && vacuumOk() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER), container.getItem(SLOT_SOURCE));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        ItemStack src = container.getItem(SLOT_SOURCE);
        com.taku3.semiconductorlab.machine.Vessel.returnEmpty(level, worldPosition, src, 1);   // 白リンの水の瓶は上に戻る
        src.shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 画面に出す：真空がよいか（1/0） */
    @Override protected int extraDataCount() { return 1; }
    @Override protected int extraData(int i) { return i == DATA_VACUUM && vacuumOk() ? 1 : 0; }
}
