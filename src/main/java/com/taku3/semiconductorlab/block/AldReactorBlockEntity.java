package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
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
 * ALD 装置（原子層堆積）[1-70] の使い道・FinFET [9-37] のゲート（IMP4-01、S-004：ユーザーの決定「[1-70] ができたら high-k に入れ替え」）：
 * 四塩化ハフニウムの蒸気と水の蒸気を、ガスを入れかえながら交互に流す。1回にちょうど原子1層ぶんだけ表面に付く（自分で止まる反応）ので、厚さを原子の単位で決められる。
 * HfCl4 + 2H2O → HfO2 + 4HCl（約300℃）。SiO2 より誘電率が約6倍高い（high-k）ので、厚くしても同じ効き目＝ゲートの漏れ（トンネル電流）が減る。
 * ゲート酸化を済ませた加工中のウェハー 1〜8枚 ＋ 四塩化ハフニウム 1 ＋ 蒸留水 1 → high-k のゲート（ChipOps.highK、A、記録だけ）。瓶は上に戻る。
 * ⚠️ 本物は 2007 インテル 45nm から high-k＋金属ゲート（金属ゲートは省略）。電力・時間・サイクル数はゲームの値（ald_reactor.placeholder）。
 */
public class AldReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 30;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_HFCL4 = 1, SLOT_WATER = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, ChipOps::isProcessWafer)
            .in(SLOT_HFCL4, 8, 35, s -> s.is(Semiconductor.HAFNIUM_TETRACHLORIDE))
            .in(SLOT_WATER, 8, 53, s -> s.is(Chemistry.DISTILLED_WATER))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.ald_reactor.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.ald_reactor.need_input", "gui.semiconductor-lab.ald_reactor.cannot",
                    "gui.semiconductor-lab.ald_reactor.need_power", "gui.semiconductor-lab.ald_reactor.running")
            .bottom("recipe", "gui.semiconductor-lab.ald_reactor.recipe")
            .build();

    public AldReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.ALD_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** high-k のゲートを付けたウェハー（1〜8枚）。できなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.highK(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_HFCL4).is(Semiconductor.HAFNIUM_TETRACHLORIDE) && container.getItem(SLOT_WATER).is(Chemistry.DISTILLED_WATER)
                && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        for (int slot : new int[] {SLOT_HFCL4, SLOT_WATER}) {
            ItemStack b = container.getItem(slot);
            Vessel.returnEmpty(level, worldPosition, b, 1);   // 瓶は上に戻る
            b.shrink(1);
        }
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
