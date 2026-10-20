package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Precision;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 旋盤 [8-01]（IMP2-01）。モーズリーのねじ切り旋盤（1797〜1800頃）：材料を回し、刃物台を送りねじで動かして削る。
 * 手で刃物を持たないので、同じ寸法の部品を何個でも作れる（互換部品＝精密機械の母）。動力はモーター（この MOD では電気 750W）。
 * 削れる物は {@link #product} に1行ずつ：
 * <ul>
 *   <li>鋼 → 回転ポンプのローター [5-04]</li>
 *   <li>[8-02]（IMP2-05）：親ねじの入口に送りねじを入れておく（減らない）と、鋼 → 規格ねじ×4（親ねじと同じねじ山を何本でも切れる）</li>
 * </ul>
 * 削った部品には「寸法の誤差」（Precision.PART_ERROR_UM、µm）が付く。誤差はこの旋盤の「くせ」で、はじめて削るときに
 * ±{@link #ERROR_UM} µm の中から決まり、その旋盤の部品はみな同じ（だから重なる）。ノギス・マイクロメータ [8-04] で測れる。
 */
public class LatheBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 750;
    public static final int TICKS = 20 * 30;
    /** 旋盤のくせ（寸法の誤差）の幅 µm。⚠️ ゲームの値（1850年ごろの工作機械で 0.01〜0.03mm のめやす） */
    public static final int ERROR_UM = 25;
    public static final int SLOT_IN = 0, SLOT_TOOL = 1, SLOT_OUT = 2, SLOT_COUNT = 3;

    /** この旋盤のくせ（µm）。Integer.MIN_VALUE＝まだ決まっていない */
    private int errorUm = Integer.MIN_VALUE;

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_IN, 8, 26, LatheBlockEntity::isInput)
            .in(SLOT_TOOL, 8, 50, s -> s.is(Precision.LEAD_SCREW))
            .out(SLOT_OUT, 78, 35)
            .text("power", 100, 18, 72, 1, "gui.semiconductor-lab.machine.power").samples("0.8 kW")
            .text("stored", 100, 30, 72, 1, "gui.semiconductor-lab.machine.stored").samples("20 kJ")
            .text("hint", 100, 42, 72, 3, "gui.semiconductor-lab.machine.need_input", "gui.semiconductor-lab.machine.need_power", "gui.semiconductor-lab.machine.running")
            .build();

    public LatheBlockEntity(BlockPos pos, BlockState state) {
        super(Precision.LATHE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static boolean isInput(ItemStack s) { return s.is(Metallurgy.STEEL_INGOT); }

    /** 削るとできる物（削れない物は EMPTY）。tool＝親ねじの入口の中身 */
    public static ItemStack product(ItemStack in, ItemStack tool) {
        if (!in.is(Metallurgy.STEEL_INGOT)) return ItemStack.EMPTY;
        if (tool.is(Precision.LEAD_SCREW)) return new ItemStack(Precision.STANDARD_SCREW, 4);
        return new ItemStack(Precision.PUMP_ROTOR);
    }

    /** この旋盤のくせ（はじめて呼ばれたときに決める） */
    public int errorUm() {
        if (errorUm == Integer.MIN_VALUE) {
            errorUm = level == null ? 0 : level.getRandom().nextIntBetweenInclusive(-ERROR_UM, ERROR_UM);
            setChanged();
        }
        return errorUm;
    }

    private ItemStack made() {
        ItemStack out = product(container.getItem(SLOT_IN), container.getItem(SLOT_TOOL));
        if (!out.isEmpty()) out.set(Precision.PART_ERROR_UM, errorUm());
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }
    @Override protected boolean canWork() {
        ItemStack out = made();
        return !out.isEmpty() && canMerge(container.getItem(SLOT_OUT), out);
    }
    @Override protected void finishWork() {
        ItemStack out = made();
        container.getItem(SLOT_IN).shrink(1);   // 親ねじは減らない
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("ErrorUm", errorUm); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); errorUm = in.getIntOr("ErrorUm", Integer.MIN_VALUE); }
}
