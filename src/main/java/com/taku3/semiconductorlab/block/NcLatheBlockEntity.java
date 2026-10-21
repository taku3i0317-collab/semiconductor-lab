package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.item.BallScrewItem;
import com.taku3.semiconductorlab.item.PunchedTapeItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NC 旋盤 [8-34]（IMP2 O-037・O-038 案A、1952 MIT のサーボ機構研究所、構想は 1949 パーソンズ）。
 * 刃物の動きを人の手ではなく穴あきテープの数字で決め、サーボモーターが刃物の位置を測りながら（フィードバック [10-04]）ボールねじ [8-08] を回して合わせる。
 * <ul>
 * <li>入口：鋼（ホッパー・ベルトコンベアからも入る）・記録したテープ（{@value PunchedTapeItem#RUNS} 回で傷む）・ボールねじ（減らない）</li>
 * <li>テープの部品（回転ポンプのローターか規格ねじ×4）を、鋼が来るたびにくり返し削る</li>
 * <li>位置を測って直すので、手の旋盤 [8-01] の「くせ」（±{@value LatheBlockEntity#ERROR_UM}µm）は無い。残る誤差はボールねじの送りの細かさの半分までで、この機械の部品はみな同じ（くり返しの精度、だから重なる）</li>
 * </ul>
 * {@value #WATTS}W・{@value #TICKS} tick（手の旋盤の半分の時間）。⚠️ 時間・電力・テープの持ちはゲームの値
 */
public class NcLatheBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1500;
    public static final int TICKS = 20 * 15;
    public static final int SLOT_IN = 0, SLOT_TAPE = 1, SLOT_SCREW = 2, SLOT_OUT = 3, SLOT_COUNT = 4;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 17, LatheBlockEntity::isInput)
            .in(SLOT_TAPE, 8, 39, PunchedTapeItem::isRecorded, 1)
            .in(SLOT_SCREW, 30, 39, s -> s.is(Precision.BALL_SCREW), 1)
            .out(SLOT_OUT, 78, 35)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("1.5 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    /** この機械に残る誤差（µm）。Integer.MIN_VALUE＝まだ決まっていない */
    private int errorUm = Integer.MIN_VALUE;

    public NcLatheBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.NC_LATHE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** テープの部品を鋼から削った物（できないなら EMPTY） */
    public static ItemStack product(ItemStack in, ItemStack tape) {
        if (!in.is(Metallurgy.STEEL_INGOT) || !PunchedTapeItem.usable(tape)) return ItemStack.EMPTY;
        String part = PunchedTapeItem.part(tape);
        if (part.equals("standard_screw")) return new ItemStack(Precision.STANDARD_SCREW, 4);
        if (part.equals("pump_rotor")) return new ItemStack(Precision.PUMP_ROTOR);
        return ItemStack.EMPTY;
    }

    /** 1個ごとの誤差の幅（µm）：ボールねじの送りの半分（切り上げ） */
    public static int errorRangeUm(ItemStack screw) { return (BallScrewItem.feedUm(screw) + 1) / 2; }

    /** 残る誤差：はじめて削るときにボールねじの送りの半分の中から決まり、ねじを細かい物に替えるとその幅に収まる */
    public int errorUm() {
        int r = errorRangeUm(container.getItem(SLOT_SCREW));
        if (errorUm == Integer.MIN_VALUE) { errorUm = level == null ? 0 : level.getRandom().nextIntBetweenInclusive(-r, r); setChanged(); }
        if (Math.abs(errorUm) > r) { errorUm = Integer.signum(errorUm) * r; setChanged(); }
        return errorUm;
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out) { super.saveAdditional(out); out.putInt("ErrorUm", errorUm); }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in) { super.loadAdditional(in); errorUm = in.getIntOr("ErrorUm", Integer.MIN_VALUE); }

    /** 削った部品（誤差つき。出口の物と重なるかを比べるのにも使う） */
    private ItemStack made() {
        ItemStack out = product(container.getItem(SLOT_IN), container.getItem(SLOT_TAPE));
        if (!out.isEmpty()) out.set(Precision.PART_ERROR_UM, errorUm());
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected double drawWatts() { return super.drawWatts() * BallScrewItem.powerFactor(container.getItem(SLOT_SCREW)); }
    @Override protected int workTicks() { return TICKS; }

    @Override protected boolean canWork() {
        if (!container.getItem(SLOT_SCREW).is(Precision.BALL_SCREW)) return false;
        ItemStack out = made();
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }

    @Override protected void finishWork() {
        ItemStack out = made();
        container.getItem(SLOT_IN).shrink(1);
        PunchedTapeItem.wear(container.getItem(SLOT_TAPE));
        merge(SLOT_OUT, out);
        container.setChanged();
    }
}
