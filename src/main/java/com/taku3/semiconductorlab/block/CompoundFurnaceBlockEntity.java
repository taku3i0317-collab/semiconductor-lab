package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Branches;
import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 化合物半導体の炉 [B-07]（1962 の赤い GaAsP の LED。IMP4-01、S-007 案A）。入れた物で2つの工程を切りかえる（密閉した石英管の中で、毒の気体を外に出さない）。
 * ① ヒ素：白ヒ素（As2O3）＋コークスを熱すると、ヒ素が気体になって冷えた所に灰色の結晶でつく（As2O3 + 3C → 2As + 3CO）。
 * ② GaAsP：ガリウム＋ヒ素＋赤リンを熱すると、ガリウム・ヒ素・リンの結晶の層（ガリウムヒ素リン）ができる → GaAsP の板 {@link #WAFERS_PER_RUN} 枚。ガリウムのポリエチレン瓶は上に戻る。
 *   リンを混ぜるほどすき間（バンドギャップ）が広がり、赤い光が出る（GaAs だけだと目に見えない赤外）。
 * ⚠️ 本物の GaAsP は GaAs の板の上に、気体（アルシン・ホスフィン・塩化ガリウム）から育てる（気相エピタキシー）。ここは固体の材料で近似。量・電力・時間はゲームの値（compound_furnace.placeholder）。
 */
public class CompoundFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 20;
    public static final int WAFERS_PER_RUN = 4;
    public static final int SLOT_A = 0, SLOT_B = 1, SLOT_C = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int MODE_NONE = 0, MODE_ARSENIC = 1, MODE_GAASP = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_A, 8, 17, s -> s.is(Metallurgy.WHITE_ARSENIC) || s.is(Metallurgy.GALLIUM))
            .in(SLOT_B, 8, 35, s -> s.is(Metallurgy.COKE) || s.is(Branches.ARSENIC))
            .in(SLOT_C, 8, 53, s -> s.is(Chemistry.RED_PHOSPHORUS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.compound_furnace.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.compound_furnace.need_input", "gui.semiconductor-lab.compound_furnace.need_power",
                    "gui.semiconductor-lab.compound_furnace.arsenic", "gui.semiconductor-lab.compound_furnace.gaasp")
            .bottom("recipe", "gui.semiconductor-lab.compound_furnace.recipe")
            .build();

    public CompoundFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Branches.COMPOUND_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static int mode(ItemStack a, ItemStack b, ItemStack c) {
        if (a.is(Metallurgy.WHITE_ARSENIC) && b.is(Metallurgy.COKE) && c.isEmpty()) return MODE_ARSENIC;
        if (a.is(Metallurgy.GALLIUM) && b.is(Branches.ARSENIC) && c.is(Chemistry.RED_PHOSPHORUS)) return MODE_GAASP;
        return MODE_NONE;
    }

    private static ItemStack product(int m) {
        return m == MODE_ARSENIC ? new ItemStack(Branches.ARSENIC) : m == MODE_GAASP ? new ItemStack(Branches.GAASP_WAFER, WAFERS_PER_RUN) : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        int m = mode(container.getItem(SLOT_A), container.getItem(SLOT_B), container.getItem(SLOT_C));
        return m != MODE_NONE && canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        int m = mode(container.getItem(SLOT_A), container.getItem(SLOT_B), container.getItem(SLOT_C));
        if (m == MODE_NONE) return;
        ItemStack out = product(m);
        ItemStack a = container.getItem(SLOT_A);
        if (m == MODE_GAASP) Vessel.returnEmpty(level, worldPosition, a, 1);   // ガリウムのポリエチレン瓶は上に戻る
        a.shrink(1);
        container.getItem(SLOT_B).shrink(1);
        if (m == MODE_GAASP) container.getItem(SLOT_C).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
