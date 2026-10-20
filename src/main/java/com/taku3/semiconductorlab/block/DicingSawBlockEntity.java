package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.BallScrewItem;
import com.taku3.semiconductorlab.item.DiamondScribeItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * [9-32] ダイシングの段階2（O-014、A）：ダイシングソー。ダイヤモンドの粒を金属で固めた薄い円盤刃を高速で回し、水をかけながら
 * ウェハーを格子に切る。台はボールねじ [8-08]（C）で送るので、切る線がまっすぐで、けがいて割る段階1より欠けが少ない。
 * 仕上がった加工中のウェハー1枚 ＋ 刃（耐久 {@link #BLADE_USES} 枚）＋ 台のボールねじ（使っても減らない）→ チップ {@link DiamondScribeItem#CHIPS_PER_WAFER} 個。
 * 欠ける確率（1枚につき1個）：ボールねじの段階 1・2・3 で {@link #BREAK_CHANCE}（段階1 のけがき針は 10%）。
 * ⚠️ 電力（1.5kW：スピンドル）・時間（10秒）・欠ける確率はゲームの値。冷やす水と、ウェハーを貼るテープは省略。
 */
public class DicingSawBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1500;
    public static final int TICKS = 20 * 10;
    public static final int BLADE_USES = 32;
    /** ボールねじの段階 0（なし＝動かない）・1・2・3 で、1枚につき1個欠ける確率 */
    public static final float[] BREAK_CHANCE = {1f, 0.04f, 0.02f, 0.01f};
    public static final int SLOT_WAFER = 0, SLOT_BLADE = 1, SLOT_STAGE = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_WAFER, 8, 17, DiamondScribeItem::finished)
            .in(SLOT_BLADE, 8, 35, s -> s.is(Semiconductor.DIAMOND_BLADE), 1)
            .in(SLOT_STAGE, 8, 53, s -> s.is(Precision.BALL_SCREW), 1)
            .out(SLOT_OUT, 78, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.machine.power").samples("1.5 kW")
            .right("hint", 28, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running").fit()
            .build();

    public DicingSawBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.DICING_SAW.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 台のボールねじの段階（なければ 0） */
    public int stageGrade() { return BallScrewItem.grade(container.getItem(SLOT_STAGE)); }

    /** 1枚を切ったチップ（欠けた数を引く）。chip_state をそのまま写す */
    public static ItemStack saw(ItemStack wafer, int grade, RandomSource random) {
        ItemStack chips = DiamondScribeItem.scribe(wafer, random);   // 形と chip_state はけがき針と同じ
        chips.setCount(Math.max(0, DiamondScribeItem.good(wafer) - (random.nextFloat() < BREAK_CHANCE[Math.min(3, grade)] ? 1 : 0)));   // プローバーの印のチップは捨てる
        return chips;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        ItemStack w = container.getItem(SLOT_WAFER);
        if (!DiamondScribeItem.finished(w) || !container.getItem(SLOT_BLADE).is(Semiconductor.DIAMOND_BLADE) || stageGrade() <= 0) return false;
        ItemStack out = container.getItem(SLOT_OUT);
        ItemStack sample = new ItemStack(Semiconductor.CHIP, DiamondScribeItem.CHIPS_PER_WAFER);
        sample.set(com.taku3.semiconductorlab.ModDataComponents.CHIP_STATE, w.get(com.taku3.semiconductorlab.ModDataComponents.CHIP_STATE));
        return canMerge(out, sample);
    }

    @Override protected void finishWork() {
        ItemStack w = container.getItem(SLOT_WAFER);
        ItemStack chips = saw(w, stageGrade(), level == null ? RandomSource.create() : level.getRandom());
        w.shrink(1);
        if (chips.isEmpty()) { container.setChanged(); return; }   // 全部に印が付いていた
        ItemStack blade = container.getItem(SLOT_BLADE);
        blade.setDamageValue(blade.getDamageValue() + 1);
        if (blade.getDamageValue() >= blade.getMaxDamage()) container.setItem(SLOT_BLADE, ItemStack.EMPTY);   // 刃がすり減った
        merge(SLOT_OUT, chips);
        container.setChanged();
    }
}
