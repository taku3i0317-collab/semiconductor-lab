package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ホール・エルー電解槽 [1-29]（1886 ホール（米）とエルー（仏）が独立に発明）。鋼の槽の内側を炭素で張り（陰極）、約960℃で溶けた氷晶石に
 * アルミナを溶かす。上から炭素の陽極を浸して直流の大電流を流すと、2Al2O3 + 3C → 4Al + 3CO2。アルミは槽の底にたまり、陽極は燃えて減る。
 * 電流の熱だけで浴が溶けたまま保たれる（外から燃料で熱しない）。
 * <pre>
 *   アルミナ 2 ＋ 黒鉛棒（陽極）{@link #RUNS_PER_ANODE} 回で 1 ＋ 氷晶石（浴）{@link #RUNS_PER_CRYOLITE} 回で 1 → アルミニウム 1（約1kg）
 *   電力量 15 kWh（現代の 13〜15 kWh/kg）＝ 100 kW × 540 秒
 * </pre>
 * ⚠️ CO2 などの気体（とフッ化物の排ガス）は表さない。電流・電圧（本物は約4〜5V・数万〜数十万A）は表さない（hall_heroult_cell.placeholder）。
 */
public class HallHeroultCellBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 100_000;
    /** 15 kWh ÷ 100 kW = 540 秒 */
    public static final int TICKS = 20 * 540;
    public static final double KWH_PER_INGOT = 15;
    public static final int ALUMINA_PER_RUN = 2, RUNS_PER_ANODE = 2, RUNS_PER_CRYOLITE = 16;
    public static final int SLOT_ALUMINA = 0, SLOT_CRYOLITE = 1, SLOT_ANODE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    public static final int DATA_BATH = DATA_COUNT, DATA_ANODE = DATA_COUNT + 1, EXTRA_DATA = 2;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_ALUMINA, 8, 17, s -> s.is(Metallurgy.ALUMINA))
            .in(SLOT_CRYOLITE, 8, 35, s -> s.is(Metallurgy.CRYOLITE))
            .in(SLOT_ANODE, 8, 53, s -> s.is(Power.GRAPHITE_ROD))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.hall_heroult_cell.power").samples("500.0 kW")
            .right("bath", 27, 1, "gui.semiconductor-lab.hall_heroult_cell.bath").samples("16")
            .right("hint", 38, 3, "gui.semiconductor-lab.hall_heroult_cell.need_alumina", "gui.semiconductor-lab.hall_heroult_cell.need_bath",
                    "gui.semiconductor-lab.hall_heroult_cell.need_anode", "gui.semiconductor-lab.hall_heroult_cell.need_power", "gui.semiconductor-lab.hall_heroult_cell.running")
            .bottom("recipe", "gui.semiconductor-lab.hall_heroult_cell.recipe")
            .build();

    /** 浴（溶けた氷晶石）があと何回もつか・いまの陽極があと何回もつか（保存する） */
    private int bath, anode;

    public HallHeroultCellBlockEntity(BlockPos pos, BlockState state) {
        super(Metallurgy.HALL_HEROULT_CELL.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int getBath() { return bath; }
    public int getAnode() { return anode; }
    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) { return i == DATA_BATH ? bath : i == DATA_ANODE ? anode : 0; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack al = container.getItem(SLOT_ALUMINA);
        return al.is(Metallurgy.ALUMINA) && al.getCount() >= ALUMINA_PER_RUN
                && (bath > 0 || container.getItem(SLOT_CRYOLITE).is(Metallurgy.CRYOLITE))
                && (anode > 0 || container.getItem(SLOT_ANODE).is(Power.GRAPHITE_ROD))
                && canMerge(container.getItem(SLOT_OUT), new ItemStack(Metallurgy.ALUMINIUM_INGOT));
    }

    @Override protected void finishWork() {
        if (bath <= 0) { container.getItem(SLOT_CRYOLITE).shrink(1); bath = RUNS_PER_CRYOLITE; }   // 浴を足す（少しずつ失われる）
        if (anode <= 0) { container.getItem(SLOT_ANODE).shrink(1); anode = RUNS_PER_ANODE; }       // 新しい陽極（炭素が燃えて減る）
        bath--; anode--;
        container.getItem(SLOT_ALUMINA).shrink(ALUMINA_PER_RUN);
        merge(SLOT_OUT, new ItemStack(Metallurgy.ALUMINIUM_INGOT));
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Bath", bath); out.putInt("Anode", anode); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); bath = in.getIntOr("Bath", 0); anode = in.getIntOr("Anode", 0); }
}
