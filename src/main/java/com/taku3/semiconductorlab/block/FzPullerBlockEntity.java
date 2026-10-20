package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.Dopant;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FZ 炉（浮遊帯域溶融）[9-09]（1952 テウラー（ベル研、特許出願 1952年12月）、1953 キックとゴレイ（米陸軍通信部隊）、1953〜54 エマイス（シーメンス）が別々に）。
 * 多結晶シリコンの棒を縦に立て、誘導加熱のコイルで一部だけを溶かす。溶けた帯は表面張力で宙に保たれ、るつぼに触れない。
 * 帯を種結晶の側から棒にそって動かすと、単結晶になっていく。石英るつぼを使わない＝るつぼが溶けて入る酸素がない（CZ は 10〜20 ppma、FZ は 0.1 ppma 以下）。
 * 多結晶シリコン 2 ＋ シリコン種結晶（減らない）＋ アルゴン 2 ＋（ドーパント 1、なくてもよい）→ シリコン単結晶 1（酸素 {@link #FZ_OXYGEN_PPMA} ppma）。
 * 電力 20kW × 300秒（誘導加熱）。⚠️ 電力・時間はゲームの値。本物の FZ は太い結晶が作りにくい（大口径は CZ が主）＝ここでは表さない（fz_puller.placeholder）。
 */
public class FzPullerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 20_000;
    public static final int TICKS = 20 * 300;
    public static final int POLY_PER_INGOT = 2, AR_PER_INGOT = 2;
    public static final double FZ_OXYGEN_PPMA = 0.05, CZ_OXYGEN_PPMA = 15;
    public static final int SLOT_POLY = 0, SLOT_SEED = 1, SLOT_DOPANT = 2, SLOT_AR = 3, SLOT_OUT = 4, SLOT_COUNT = 5;
    private static final int PULL_INTERVAL = 10, AR_TARGET = 16;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_POLY, 8, 17, s -> s.is(Metallurgy.POLYSILICON))
            .in(SLOT_SEED, 8, 35, s -> s.is(Semiconductor.SILICON_SEED_CRYSTAL))
            .in(SLOT_DOPANT, 8, 53, s -> Dopant.of(s) != null)
            .in(SLOT_AR, 30, 53, s -> s.is(Gas.ARGON_GAS))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.fz_puller.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.fz_puller.need_input", "gui.semiconductor-lab.fz_puller.need_ar",
                    "gui.semiconductor-lab.fz_puller.need_power", "gui.semiconductor-lab.fz_puller.running")
            .bottom("recipe", "gui.semiconductor-lab.fz_puller.recipe")
            .build();

    public FzPullerBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.FZ_PULLER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    private ItemStack product() {
        ItemStack out = new ItemStack(Semiconductor.SILICON_SINGLE_CRYSTAL);
        Double p = container.getItem(SLOT_POLY).get(ModDataComponents.PURITY);
        if (p != null) out.set(ModDataComponents.PURITY, p);
        Dopant d = Dopant.of(container.getItem(SLOT_DOPANT));
        if (d != null) out.set(ModDataComponents.DOPANT, d.id);
        out.set(ModDataComponents.OXYGEN_PPMA, FZ_OXYGEN_PPMA);
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack poly = container.getItem(SLOT_POLY), ar = container.getItem(SLOT_AR);
        return poly.is(Metallurgy.POLYSILICON) && poly.getCount() >= POLY_PER_INGOT
                && container.getItem(SLOT_SEED).is(Semiconductor.SILICON_SEED_CRYSTAL)
                && ar.is(Gas.ARGON_GAS) && ar.getCount() >= AR_PER_INGOT
                && canMerge(container.getItem(SLOT_OUT), product());
    }

    @Override protected void finishWork() {
        ItemStack out = product();
        container.getItem(SLOT_POLY).shrink(POLY_PER_INGOT);
        container.getItem(SLOT_AR).shrink(AR_PER_INGOT);
        if (!container.getItem(SLOT_DOPANT).isEmpty()) container.getItem(SLOT_DOPANT).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダー（アルゴン）から、スロットが 16 袋になるまで補充する（CZ 引き上げ機と同じ） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack ar = container.getItem(SLOT_AR);
        if (!ar.isEmpty() && !ar.is(Gas.ARGON_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = AR_TARGET - container.getItem(SLOT_AR).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.ARGON, want);
            if (n > 0) merge(SLOT_AR, new ItemStack(Gas.ARGON_GAS, n));
        }
    }
}
