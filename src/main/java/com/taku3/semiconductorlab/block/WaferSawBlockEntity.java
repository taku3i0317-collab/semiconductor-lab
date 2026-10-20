package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.ModMachines;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ウェハー切断機 [9-12]（1954 の作り方：ダイヤモンドを付けた鋼の円盤刃をモーターで回して切る）。
 * シリコン単結晶インゴット 1本（約2kg）→ シリコンの板 {@link #SLICES_PER_INGOT} 枚。純度とドーパントは板に受け継ぐ。
 * 刃のすき間（切りしろ）の分は粉になって消える（本物でもインゴットのかなりの部分が粉になる）。
 * 刃はダイヤモンドが少しずつ取れて減る：{@link #RUNS_PER_BLADE} 本で1枚。
 * ⚠️ 電力（1kW）・時間（60秒）・枚数はゲームの値（出典なし、DECISIONS_PENDING Q25-5）。
 */
public class WaferSawBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 60;
    public static final int SLICES_PER_INGOT = 8, RUNS_PER_BLADE = 4;
    public static final int SLOT_CRYSTAL = 0, SLOT_BLADE = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    private int bladeRuns;

    /** 画面のスロットの並び（IMP-28：前の WaferSawMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_CRYSTAL, 8, 26, WaferSawBlockEntity::isCrystal)
            .in(SLOT_BLADE, 8, 50, WaferSawBlockEntity::isBlade)
            .out(SLOT_OUT, 78, 35)
            .build();

    public WaferSawBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.WAFER_SAW.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isCrystal(ItemStack s) { return s.is(ModItems.SILICON_SINGLE_CRYSTAL); }
    public static boolean isBlade(ItemStack s) { return s.is(ModItems.DIAMOND_SAW_BLADE); }
    public int getBladeRuns() { return bladeRuns; }

    /** 板：インゴットの純度とドーパントを受け継ぐ */
    public static ItemStack slices(ItemStack crystal) {
        ItemStack s = new ItemStack(ModItems.SILICON_WAFER, SLICES_PER_INGOT);
        Double p = crystal.get(ModDataComponents.PURITY);
        if (p != null) s.set(ModDataComponents.PURITY, p);
        String d = crystal.get(ModDataComponents.DOPANT);
        if (d != null) s.set(ModDataComponents.DOPANT, d);
        return s;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack c = container.getItem(SLOT_CRYSTAL);
        return isCrystal(c) && (bladeRuns > 0 || isBlade(container.getItem(SLOT_BLADE)))
                && canMerge(container.getItem(SLOT_OUT), slices(c));
    }

    @Override protected void finishWork() {
        if (bladeRuns <= 0) { container.getItem(SLOT_BLADE).shrink(1); bladeRuns = RUNS_PER_BLADE; }
        bladeRuns--;
        ItemStack out = slices(container.getItem(SLOT_CRYSTAL));
        container.getItem(SLOT_CRYSTAL).shrink(1);
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("BladeRuns", bladeRuns); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); bladeRuns = in.getIntOr("BladeRuns", 0); }

}
