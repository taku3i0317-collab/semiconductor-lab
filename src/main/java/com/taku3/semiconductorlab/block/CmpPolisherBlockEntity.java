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
 * CMP 装置（化学機械研磨）[9-31]（1980年代 IBM ⚠️。IMP4-01、S-002 → v0.5 #2）：
 * 層を重ねると表面が凸凹になり、細い線を焼くときに焦点が合わなくなる（焦点深度 DOF = k2·λ/NA²）。
 * 回る定盤のパッドにアルカリ性のコロイダルシリカ（スラリー）を流し、ウェハーを押しつけて、層間膜の出っぱった所から削って平らにする（化学で柔らかくし、粒でこする）。
 * 層間膜を付けた加工中のウェハー 1〜8枚 ＋ コロイダルシリカ 1 → 平らにしたウェハー（ChipOps.planarize、A：段差 steps を 0 に、粒で汚れ +2＝後で洗う）。瓶は上に戻る。
 * ⚠️ 本物のパッドはポリウレタンの発泡体（IC1000 など）。ここは機械に組みこんだナイロンのフェルトで近似。電力・時間はゲームの値。焦点深度の判定は v0.5 #1（A）（cmp_polisher.placeholder）。
 */
public class CmpPolisherBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 2_000;
    public static final int TICKS = 20 * 30;
    public static final int WAFERS_PER_RUN = 8;
    public static final int SLOT_WAFER = 0, SLOT_SLURRY = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 26, ChipOps::isProcessWafer)
            .in(SLOT_SLURRY, 8, 50, s -> s.is(Chemistry.COLLOIDAL_SILICA))
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.cmp_polisher.power").samples("500.0 kW")
            .right("hint", 28, 4, "gui.semiconductor-lab.cmp_polisher.need_input", "gui.semiconductor-lab.cmp_polisher.no_interlayer",
                    "gui.semiconductor-lab.cmp_polisher.resist_on", "gui.semiconductor-lab.cmp_polisher.metal_on", "gui.semiconductor-lab.cmp_polisher.already_flat",
                    "gui.semiconductor-lab.cmp_polisher.need_power", "gui.semiconductor-lab.cmp_polisher.running")
            .bottom("recipe", "gui.semiconductor-lab.cmp_polisher.recipe")
            .build();

    public CmpPolisherBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.CMP_POLISHER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 平らにしたウェハー（1〜8枚）。できなければ EMPTY */
    public static ItemStack product(ItemStack w) {
        if (!ChipOps.isProcessWafer(w)) return ItemStack.EMPTY;
        ChipOps.Result r = ChipOps.planarize(w.copyWithCount(Math.min(w.getCount(), WAFERS_PER_RUN)));
        return r.ok() ? r.out() : ItemStack.EMPTY;
    }

    /** できない理由（ChipOps の reason：no_interlayer・resist_on・metal_on・already_flat。画面の文に使う） */
    public static String reason(ItemStack w) {
        return ChipOps.isProcessWafer(w) ? ChipOps.planarize(w.copyWithCount(1)).reason() : "no_interlayer";
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        return !out.isEmpty() && container.getItem(SLOT_SLURRY).is(Chemistry.COLLOIDAL_SILICA) && canMerge(container.getItem(SLOT_OUT), dust(out));
    }

    @Override protected void finishWork() {
        ItemStack out = product(container.getItem(SLOT_WAFER));
        container.getItem(SLOT_WAFER).shrink(out.getCount());
        ItemStack slurry = container.getItem(SLOT_SLURRY);
        Vessel.returnEmpty(level, worldPosition, slurry, 1);   // スラリーの瓶は上に戻る
        slurry.shrink(1);
        merge(SLOT_OUT, dust(out));
        container.setChanged();
    }

    /** クリーンルームの等級のほこりと欠陥の密度（O-017・歩留まり、C の CleanRoom.dustAt：外 +2・クラス1000 +1・クラス100 0）。失敗にはしない＝汚れは検査台の漏れ・歩留まりに効く */
    private ItemStack dust(ItemStack out) {
        return level == null ? out : ChipOps.inRoom(ChipOps.addDust(out, CleanRoom.dustAt(level, worldPosition)), CleanRoom.defectDensity(level, worldPosition));   // v0.5 #4 歩留まり：部屋の欠陥の密度も残す（A）
    }
}
