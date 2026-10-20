package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * アーク炉 [3-15]（モアッサン 1892 の電気炉、エルー 1900 の製鋼アーク炉）。黒鉛の電極と原料のあいだに大電流のアーク（電気の火花）を飛ばし、
 * 約2000℃以上にする。ふつうの炉では離れない酸素とケイ素を、炭素で引きはがせる。
 * [1-15] 金属シリコン：珪石 ×2 ＋ 木炭か石炭 ×1 → 金属シリコン ×1（純度 98%＝冶金グレード）。SiO2 + 2C → Si + 2CO（1個 ≈ 1kg）。
 * 電力量 12kWh/kg（本物は 1kg あたり 10kWh 以上、現代の炉で約11〜13kWh）＝ 500kW × 86.4秒。電力が足りない tick は進まない（2000℃を保てない）。
 * 黒鉛の電極（黒鉛棒）は少しずつ燃えて減る：{@link #RUNS_PER_ELECTRODE} 回で1本。
 * [3-25] カーバイド（D のお願い、KPR の材料のアセチレンへ）：生石灰 ×1 ＋ 炭素 ×3 → 炭化カルシウム ×1。CaO + 3C → CaC2 + CO（ウィルソン・モアッサン 1892）。
 * 電力量 3kWh/kg（本物は 1t あたり約 3000kWh）＝ 500kW × 21.6秒。入口の1つ目に珪石か生石灰を入れると、どちらを作るかが決まる。
 * ⚠️ 近似：本物（現代）は交流のサブマージドアーク炉。ゲームでは交流 [4-10] が v0.4 で出るまで、直流（モアッサンの炉と同じ、⚠️要確認）で近似。
 */
public class ArcFurnaceBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 500_000;
    /** 12 kWh = 43.2 MJ ÷ 500 kW = 86.4 秒 */
    public static final int TICKS = 1728;
    public static final int QUARTZ_PER_RUN = 2, CARBON_PER_RUN = 1, RUNS_PER_ELECTRODE = 8;
    /** カーバイド：3 kWh = 10.8 MJ ÷ 500 kW = 21.6 秒。生石灰1・炭素3（CaO + 3C） */
    public static final int TICKS_CARBIDE = 432, LIME_PER_RUN = 1, CARBON_PER_CARBIDE = 3;
    public static final int SLOT_QUARTZ = 0, SLOT_CARBON = 1, SLOT_ELECTRODE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    /** いま差している電極で、あと何回動けるか（0 なら次の電極を1本使う） */
    private int electrodeRuns;

    /** 画面のスロットの並び（IMP-28：前の ArcFurnaceMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_QUARTZ, 8, 17, ArcFurnaceBlockEntity::isCharge)
            .in(SLOT_CARBON, 8, 35, ArcFurnaceBlockEntity::isCarbon)
            .in(SLOT_ELECTRODE, 8, 53, ArcFurnaceBlockEntity::isElectrode)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.arc_furnace.power").samples("500.0 kW")
            .right("need", 27, 1, "gui.semiconductor-lab.arc_furnace.need").samples("500 kW")
            .right("hint", 38, 3, "gui.semiconductor-lab.arc_furnace.need_input", "gui.semiconductor-lab.arc_furnace.need_power", "gui.semiconductor-lab.arc_furnace.running").fit()
            .bottom("recipe", "gui.semiconductor-lab.arc_furnace.recipe", "gui.semiconductor-lab.arc_furnace.recipe_carbide")
            .build();

    public ArcFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Heat.ARC_FURNACE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isQuartz(ItemStack s) { return s.is(Metallurgy.VEIN_QUARTZ); }
    public static boolean isLime(ItemStack s) { return s.is(com.taku3.semiconductorlab.content.Survival.QUICKLIME); }
    /** 入口の1つ目：珪石（→ 金属シリコン）か生石灰（→ カーバイド） */
    public static boolean isCharge(ItemStack s) { return isQuartz(s) || isLime(s); }
    private boolean carbideMode() { return isLime(container.getItem(SLOT_QUARTZ)); }
    /** 炭素は木炭か石炭（どちらでも出力は同じ。DECISIONS Q1515-2）。本物は石炭・コークス・木片も混ぜる */
    public static boolean isCarbon(ItemStack s) { return s.is(Items.CHARCOAL) || s.is(Items.COAL) || s.is(Metallurgy.COKE); }
    public static boolean isElectrode(ItemStack s) { return s.is(Power.GRAPHITE_ROD); }
    public int getElectrodeRuns() { return electrodeRuns; }

    private static ItemStack silicon() {
        ItemStack s = new ItemStack(Metallurgy.METALLURGICAL_SILICON);
        s.set(ModDataComponents.PURITY, Purity.METALLURGICAL);
        return s;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return carbideMode() ? TICKS_CARBIDE : TICKS; }

    @Override protected boolean canWork() {
        if (carbideMode())
            return container.getItem(SLOT_QUARTZ).getCount() >= LIME_PER_RUN
                    && container.getItem(SLOT_CARBON).getCount() >= CARBON_PER_CARBIDE && isCarbon(container.getItem(SLOT_CARBON))
                    && (electrodeRuns > 0 || isElectrode(container.getItem(SLOT_ELECTRODE)))
                    && canMerge(container.getItem(SLOT_OUT), new ItemStack(Heat.CALCIUM_CARBIDE));
        return container.getItem(SLOT_QUARTZ).getCount() >= QUARTZ_PER_RUN && isQuartz(container.getItem(SLOT_QUARTZ))
                && container.getItem(SLOT_CARBON).getCount() >= CARBON_PER_RUN && isCarbon(container.getItem(SLOT_CARBON))
                && (electrodeRuns > 0 || isElectrode(container.getItem(SLOT_ELECTRODE)))
                && canMerge(container.getItem(SLOT_OUT), silicon());
    }

    @Override protected void finishWork() {
        if (electrodeRuns <= 0) { container.getItem(SLOT_ELECTRODE).shrink(1); electrodeRuns = RUNS_PER_ELECTRODE; }
        electrodeRuns--;
        if (carbideMode()) {   // CaO + 3C → CaC2 + CO
            container.getItem(SLOT_QUARTZ).shrink(LIME_PER_RUN);
            container.getItem(SLOT_CARBON).shrink(CARBON_PER_CARBIDE);
            merge(SLOT_OUT, new ItemStack(Heat.CALCIUM_CARBIDE));
            container.setChanged();
            return;
        }
        container.getItem(SLOT_QUARTZ).shrink(QUARTZ_PER_RUN);
        container.getItem(SLOT_CARBON).shrink(CARBON_PER_RUN);   // 炭素は CO になって炉の上で燃える
        merge(SLOT_OUT, silicon());
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("ElectrodeRuns", electrodeRuns); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); electrodeRuns = in.getIntOr("ElectrodeRuns", 0); }

}
