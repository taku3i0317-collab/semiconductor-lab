package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.Vessel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * シランの発生器 [9-30] の前段（IMP4-01、S-002：層間膜の CVD の原料）。入れた物で2つの反応を切りかえる。
 * ① ケイ化マグネシウム：マグネシウム 2 ＋ 金属シリコン 1 を熱する → Mg2Si（2Mg + Si → Mg2Si）
 * ② シラン：Mg2Si 1 ＋ 塩酸 4 ＋ 空のボンベ → シランのボンベ（Mg2Si + 4HCl → SiH4 + 2MgCl2。1857 ヴェーラーとブフ）。塩酸の瓶は上に戻る、塩化マグネシウムの水は捨てる。
 * シランは空気にふれると自然に燃える気体＝ボンベに詰めて、漏れを見張る。
 * ⚠️ 本物のこの方法はシランが Si の約25% しかできず、ジシランなども混ざる（工業では SiHCl3 の不均化）。量・電力・時間はゲームの値（silane_generator.placeholder）。
 */
public class SilaneGeneratorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 10;
    public static final int MG_PER_RUN = 2, HCL_PER_RUN = 4;
    public static final int SLOT_A = 0, SLOT_B = 1, SLOT_CYL = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int MODE_NONE = 0, MODE_SILICIDE = 1, MODE_SILANE = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_A, 8, 17, s -> s.is(Metallurgy.MAGNESIUM) || s.is(Semiconductor.MAGNESIUM_SILICIDE))
            .in(SLOT_B, 8, 35, s -> s.is(Metallurgy.METALLURGICAL_SILICON) || s.is(Gas.HYDROCHLORIC_ACID))
            .in(SLOT_CYL, 8, 53, s -> s.is(Gas.EMPTY_GAS_CYLINDER))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.silane_generator.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.silane_generator.need_input", "gui.semiconductor-lab.silane_generator.need_cylinder",
                    "gui.semiconductor-lab.silane_generator.need_power", "gui.semiconductor-lab.silane_generator.silicide", "gui.semiconductor-lab.silane_generator.silane")
            .bottom("recipe", "gui.semiconductor-lab.silane_generator.recipe")
            .build();

    public SilaneGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.SILANE_GENERATOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static int mode(ItemStack a, ItemStack b) {
        if (a.is(Metallurgy.MAGNESIUM) && a.getCount() >= MG_PER_RUN && b.is(Metallurgy.METALLURGICAL_SILICON)) return MODE_SILICIDE;
        if (a.is(Semiconductor.MAGNESIUM_SILICIDE) && b.is(Gas.HYDROCHLORIC_ACID) && b.getCount() >= HCL_PER_RUN) return MODE_SILANE;
        return MODE_NONE;
    }

    private static ItemStack product(int mode) {
        return mode == MODE_SILICIDE ? new ItemStack(Semiconductor.MAGNESIUM_SILICIDE) : mode == MODE_SILANE ? new ItemStack(Semiconductor.SILANE_CYLINDER) : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        int m = mode(container.getItem(SLOT_A), container.getItem(SLOT_B));
        if (m == MODE_NONE) return false;
        if (m == MODE_SILANE && !container.getItem(SLOT_CYL).is(Gas.EMPTY_GAS_CYLINDER)) return false;
        return canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        ItemStack a = container.getItem(SLOT_A), b = container.getItem(SLOT_B);
        int m = mode(a, b);
        ItemStack out = product(m);
        if (m == MODE_SILICIDE) {
            a.shrink(MG_PER_RUN);
            b.shrink(1);
        } else if (m == MODE_SILANE) {
            a.shrink(1);
            Vessel.returnEmpty(level, worldPosition, b, HCL_PER_RUN);   // 塩酸の瓶は上に戻る
            b.shrink(HCL_PER_RUN);
            container.getItem(SLOT_CYL).shrink(1);
        } else return;
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
