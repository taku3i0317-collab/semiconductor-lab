package com.taku3.semiconductorlab.block;

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
 * タンタルの精製槽 [1-66]（IMP4-01、S-003 案A）。入れた物で2つの工程を切りかえる。
 * ① マリニャック法（1866）：タンタル石をフッ酸で溶かし、フッ化カリウムを入れると、K2TaF7 は水に溶けにくいので結晶になり、
 *   そっくりな仲間のニオブ（K2NbOF5）は溶けたまま残る＝ここで初めてタンタルとニオブを分けられた。
 *   タンタル石 1 ＋ フッ酸 {@link #HF_PER_RUN} ＋ フッ化カリウム 1 → K2TaF7 1（ニオブの液は捨てる）。フッ酸の容器（鉛・ポリエチレン）は上に戻る。
 * ② ナトリウム還元（1903 ジーメンス・ハルスケ社のフォン・ボルトン）：K2TaF7 ＋ 金属ナトリウムを鉄の釜で熱する（K2TaF7 + 5Na → Ta + 2KF + 5NaF）。
 *   K2TaF7 1 ＋ 金属ナトリウム 1 → タンタル 1（塩は水で洗って捨てる）。鉄の容器は上に戻る。
 * ⚠️ 本物はこのあと真空で溶かして延ばせる金属にする。ニオブは作らない。量・電力・時間はゲームの値（tantalum_refinery.placeholder）。
 */
public class TantalumRefineryBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 3_000;
    public static final int TICKS = 20 * 20;
    public static final int HF_PER_RUN = 2;
    public static final int SLOT_A = 0, SLOT_B = 1, SLOT_KF = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int MODE_NONE = 0, MODE_MARIGNAC = 1, MODE_REDUCE = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_A, 8, 17, s -> s.is(Metallurgy.TANTALITE) || s.is(Metallurgy.POTASSIUM_FLUOROTANTALATE))
            .in(SLOT_B, 8, 35, s -> s.is(Chemistry.HF_TAG) || s.is(Metallurgy.SODIUM))
            .in(SLOT_KF, 8, 53, s -> s.is(Chemistry.POTASSIUM_FLUORIDE))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.tantalum_refinery.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.tantalum_refinery.need_input", "gui.semiconductor-lab.tantalum_refinery.need_kf",
                    "gui.semiconductor-lab.tantalum_refinery.need_power", "gui.semiconductor-lab.tantalum_refinery.marignac", "gui.semiconductor-lab.tantalum_refinery.reduce")
            .bottom("recipe", "gui.semiconductor-lab.tantalum_refinery.recipe")
            .build();

    public TantalumRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.TANTALUM_REFINERY.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static int mode(ItemStack a, ItemStack b) {
        if (a.is(Metallurgy.TANTALITE) && b.is(Chemistry.HF_TAG) && b.getCount() >= HF_PER_RUN) return MODE_MARIGNAC;
        if (a.is(Metallurgy.POTASSIUM_FLUOROTANTALATE) && b.is(Metallurgy.SODIUM)) return MODE_REDUCE;
        return MODE_NONE;
    }

    private static ItemStack product(int m) {
        return m == MODE_MARIGNAC ? new ItemStack(Metallurgy.POTASSIUM_FLUOROTANTALATE) : m == MODE_REDUCE ? new ItemStack(Metallurgy.TANTALUM) : ItemStack.EMPTY;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        int m = mode(container.getItem(SLOT_A), container.getItem(SLOT_B));
        if (m == MODE_NONE) return false;
        if (m == MODE_MARIGNAC && !container.getItem(SLOT_KF).is(Chemistry.POTASSIUM_FLUORIDE)) return false;
        return canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        ItemStack a = container.getItem(SLOT_A), b = container.getItem(SLOT_B);
        int m = mode(a, b);
        ItemStack out = product(m);
        if (m == MODE_NONE) return;
        int nb = m == MODE_MARIGNAC ? HF_PER_RUN : 1;
        a.shrink(1);
        Vessel.returnEmpty(level, worldPosition, b, nb);   // フッ酸の容器・ナトリウムの鉄の容器は上に戻る
        b.shrink(nb);
        if (m == MODE_MARIGNAC) container.getItem(SLOT_KF).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
