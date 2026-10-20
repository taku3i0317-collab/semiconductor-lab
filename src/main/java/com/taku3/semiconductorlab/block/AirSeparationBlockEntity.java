package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModMachines;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 空気分離装置 [6-07]（リンデ 1895 の空気の液化 ＋ 1902〜 の精留塔）。
 * 空気を圧縮して熱交換器で冷やし、膨張させて（ジュール・トムソン効果）液体にする。液体空気を精留塔で何段も蒸留すると、
 * 沸点の低い窒素（−196℃）が上へ、アルゴン（−186℃）が中ほど、酸素（−183℃）が下にたまる。
 * 1回で空気 {@link #AIR_MOL} mol を分ける：体積比 N2 78.08% ・ O2 20.95% ・ Ar 0.93%（CO2 などの残り 0.04% は捨てる）。
 * 端数は mmol で持ち越し、1000 mmol たまるごとに袋1つ（1mol）を出す。アルゴンは空気 約108mol で1袋。
 * 電力：現代の装置の実測 0.46 kWh/m³(O2) ×（1回の O2 2.1mol ＝ 0.047 m³）≈ 78 kJ → 5kW×16秒＝80kJ。
 * ⚠️ 省略：断熱箱（本物は羊毛などを詰めた木箱）・始動時の冷却に何時間もかかること・アルゴン専用の塔・1900年代の装置の電力（もっと多い）。
 */
public class AirSeparationBlockEntity extends ConsumerBlockEntity implements GasSource {
    public static final double WATTS = 5_000;
    public static final int TICKS = 20 * 16;
    public static final int AIR_MOL = 10;
    /** 1回（空気 10mol）で取れる量（mmol）。体積比＝mol 比 */
    public static final int N2_PER_RUN = 7808, O2_PER_RUN = 2095, AR_PER_RUN = 93;
    public static final int SLOT_N2 = 0, SLOT_O2 = 1, SLOT_AR = 2, SLOT_COUNT = 3;

    /** 次の袋になるまでの端数（mmol） */
    private int n2, o2, ar;

    /** 画面のスロットの並び（IMP-28）：出口 N2・O2・Ar を縦に */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .out(SLOT_N2, 62, 17)
            .out(SLOT_O2, 62, 35)
            .out(SLOT_AR, 62, 53)
            .build();

    public AirSeparationBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.AIR_SEPARATION_UNIT.type, pos, state, SLOT_COUNT, 50_000, WATTS);
    }

    @Override public int[] gasOutputSlots() { return new int[] {SLOT_N2, SLOT_O2, SLOT_AR}; }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    /** 空気はどこにでもあるので、出口に次の1回分の袋が入る空きがあれば動く */
    @Override protected boolean canWork() {
        return fits(SLOT_N2, ModItems.NITROGEN_GAS, (n2 + N2_PER_RUN) / 1000)
                && fits(SLOT_O2, ModItems.OXYGEN_GAS, (o2 + O2_PER_RUN) / 1000)
                && fits(SLOT_AR, ModItems.ARGON_GAS, (ar + AR_PER_RUN) / 1000);
    }

    private boolean fits(int slot, Item item, int n) { return n == 0 || canMerge(container.getItem(slot), new ItemStack(item, n)); }

    @Override protected void finishWork() {
        n2 = emit(SLOT_N2, ModItems.NITROGEN_GAS, n2 + N2_PER_RUN);
        o2 = emit(SLOT_O2, ModItems.OXYGEN_GAS, o2 + O2_PER_RUN);
        ar = emit(SLOT_AR, ModItems.ARGON_GAS, ar + AR_PER_RUN);
        setChanged();
    }

    /** mmol のうち 1000 ごとに袋を出し、残りの端数を返す */
    private int emit(int slot, Item item, int mmol) {
        if (mmol >= 1000) merge(slot, new ItemStack(item, mmol / 1000));
        return mmol % 1000;
    }

    /** テスト用：アルゴンの端数（mmol） */
    public int argonRemainderMmol() { return ar; }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("N2", n2); out.putInt("O2", o2); out.putInt("Ar", ar);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        n2 = in.getIntOr("N2", 0); o2 = in.getIntOr("O2", 0); ar = in.getIntOr("Ar", 0);
    }

}
