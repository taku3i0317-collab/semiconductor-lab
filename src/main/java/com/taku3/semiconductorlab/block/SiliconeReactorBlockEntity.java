package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * シリコーンの反応器 [5-04] の③（1941〜45 ロチョウ（GE）とミュラー（ドイツ）の直接法。IMP4-01、[5-06] 油拡散ポンプの油のため）：
 * 3つの反応を、入れた物で切りかえる（約300℃ の鋼の反応管、電気ヒーター {@link #WATTS}）。
 * ① 塩化メチル：メタノール＋乾いた塩化水素 →（アルミナの触媒、減らない）→ 塩化メチル（CH3OH + HCl → CH3Cl + H2O。気体どうしで反応、水は捨てる）
 * ② 直接法：金属シリコン 1＋塩化メチル 2 →（銅の触媒、減らない）→ ジメチルジクロロシラン（Si + 2CH3Cl → (CH3)2SiCl2）。空のガラス瓶が要る
 * ③ 加水分解：ジメチルジクロロシラン＋蒸留水 → シリコーン油＋塩酸（n(CH3)2SiCl2 + nH2O → [(CH3)2SiO]n + 2nHCl）
 * ⚠️ 本物の直接法は副生成物（メチルトリクロロシランなど）を蒸留で分ける＝ここでは主生成物だけ（silicone_reactor.placeholder）。
 */
public class SiliconeReactorBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 15;
    public static final int MECL_PER_SI = 2;
    public static final int SLOT_A = 0, SLOT_B = 1, SLOT_CAT = 2, SLOT_BOTTLE = 3, SLOT_OUT = 4, SLOT_OUT2 = 5, SLOT_COUNT = 6;
    public static final int MODE_NONE = 0, MODE_MECL = 1, MODE_DIRECT = 2, MODE_HYDRO = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_A, 8, 17, s -> s.is(Chemistry.METHANOL) || s.is(Metallurgy.METALLURGICAL_SILICON) || s.is(Vacuum.DIMETHYLDICHLOROSILANE))
            .in(SLOT_B, 8, 35, s -> s.is(Metallurgy.HYDROGEN_CHLORIDE_GAS) || s.is(Vacuum.METHYL_CHLORIDE) || s.is(Chemistry.DISTILLED_WATER))
            .in(SLOT_CAT, 8, 53, s -> s.is(Metallurgy.ALUMINA) || s.is(Items.COPPER_INGOT))
            .in(SLOT_BOTTLE, 30, 53, s -> s.is(Chemistry.GLASS_BOTTLE))
            .out(SLOT_OUT, 78, 26)
            .out(SLOT_OUT2, 78, 46)
            .right("power", 17, 1, "gui.semiconductor-lab.silicone_reactor.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.silicone_reactor.need_input", "gui.semiconductor-lab.silicone_reactor.need_alumina",
                    "gui.semiconductor-lab.silicone_reactor.need_copper", "gui.semiconductor-lab.silicone_reactor.need_bottle",
                    "gui.semiconductor-lab.silicone_reactor.need_power", "gui.semiconductor-lab.silicone_reactor.mecl",
                    "gui.semiconductor-lab.silicone_reactor.direct", "gui.semiconductor-lab.silicone_reactor.hydro")
            .bottom("recipe", "gui.semiconductor-lab.silicone_reactor.recipe")
            .build();

    public SiliconeReactorBlockEntity(BlockPos pos, BlockState state) {
        super(Vacuum.SILICONE_REACTOR.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 入れた物から、どの反応か（触媒・瓶の有無は見ない） */
    public static int mode(ItemStack a, ItemStack b) {
        if (a.is(Chemistry.METHANOL) && b.is(Metallurgy.HYDROGEN_CHLORIDE_GAS)) return MODE_MECL;
        if (a.is(Metallurgy.METALLURGICAL_SILICON) && b.is(Vacuum.METHYL_CHLORIDE) && b.getCount() >= MECL_PER_SI) return MODE_DIRECT;
        if (a.is(Vacuum.DIMETHYLDICHLOROSILANE) && b.is(Chemistry.DISTILLED_WATER)) return MODE_HYDRO;
        return MODE_NONE;
    }

    /** 足りない物（画面の文の鍵の末尾）。そろっていれば null */
    public static String missing(int mode, ItemStack cat, ItemStack bottle) {
        if (mode == MODE_NONE) return "need_input";
        if (mode == MODE_MECL && !cat.is(Metallurgy.ALUMINA)) return "need_alumina";
        if (mode == MODE_DIRECT && !cat.is(Items.COPPER_INGOT)) return "need_copper";
        if (mode == MODE_DIRECT && !bottle.is(Chemistry.GLASS_BOTTLE)) return "need_bottle";
        return null;
    }

    private int mode() { return mode(container.getItem(SLOT_A), container.getItem(SLOT_B)); }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        int m = mode();
        if (missing(m, container.getItem(SLOT_CAT), container.getItem(SLOT_BOTTLE)) != null) return false;
        ItemStack o = container.getItem(SLOT_OUT), o2 = container.getItem(SLOT_OUT2);
        return switch (m) {
            case MODE_MECL -> canMerge(o, new ItemStack(Vacuum.METHYL_CHLORIDE));
            case MODE_DIRECT -> canMerge(o, new ItemStack(Vacuum.DIMETHYLDICHLOROSILANE));
            case MODE_HYDRO -> canMerge(o, new ItemStack(Vacuum.SILICONE_OIL)) && canMerge(o2, new ItemStack(Gas.HYDROCHLORIC_ACID));
            default -> false;
        };
    }

    @Override protected void finishWork() {
        ItemStack a = container.getItem(SLOT_A), b = container.getItem(SLOT_B);
        switch (mode()) {
            case MODE_MECL -> {
                a.shrink(1);
                b.shrink(1);
                merge(SLOT_OUT, new ItemStack(Vacuum.METHYL_CHLORIDE));
            }
            case MODE_DIRECT -> {
                a.shrink(1);
                b.shrink(MECL_PER_SI);
                container.getItem(SLOT_BOTTLE).shrink(1);
                merge(SLOT_OUT, new ItemStack(Vacuum.DIMETHYLDICHLOROSILANE));
            }
            case MODE_HYDRO -> {   // 瓶2本 → 瓶2本（シリコーン油と塩酸）
                a.shrink(1);
                b.shrink(1);
                merge(SLOT_OUT, new ItemStack(Vacuum.SILICONE_OIL));
                merge(SLOT_OUT2, new ItemStack(Gas.HYDROCHLORIC_ACID));
            }
            default -> { return; }
        }
        container.setChanged();
    }
}
