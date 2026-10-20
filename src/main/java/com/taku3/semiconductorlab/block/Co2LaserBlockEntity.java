package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * CO2 レーザー [7-14]（IMP2 O-019、1964 パテル）。CO2 の気体に放電をかけると、分子の振動のエネルギーで 10.6µm の赤外線が出る。
 * N2 を混ぜると、放電で揺れた N2 が CO2 にエネルギーを渡して強くなる（パテル 1965）。決定どおり He は入れない低出力版。
 * <ul>
 * <li>入口：窓（{@link #isSaltWindow 岩塩の窓}。ガラス・石英は 10.6µm を吸って割れる）・CO2 の袋・N2 の袋（なくても弱く光る）・溶かす物（珪石）</li>
 * <li>CO2 1袋（＋N2 1袋）で {@value #RUNS_PER_FILL} 回。放電で CO2 が少しずつ壊れるので詰め替える</li>
 * <li>赤外線の出力 = 電気 {@value #WATTS} W × 効率（N2 あり {@value #EFF_N2}、CO2 だけ {@value #EFF_CO2}）。
 *     {@value #MELT_W} W 以上で珪石が溶けて溶融石英（石英は 10.6µm をよく吸う）。炎の水が入らない</li>
 * <li>岩塩の窓は空気の湿り気で少しずつ曇る（耐久）</li>
 * </ul>
 * のちの EUV 光源 [7-18] でスズの粒を撃つ駆動レーザー。
 */
public class Co2LaserBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 1_000;
    public static final int TICKS = 20 * 10;
    /** 効率。⚠️ He なしの近似（He ありの本物は 10〜20%） */
    public static final double EFF_N2 = 0.06, EFF_CO2 = 0.015;
    /** 珪石を溶かすのに要る赤外線の出力（W）。⚠️ ゲームの値 */
    public static final double MELT_W = 40;
    public static final int RUNS_PER_FILL = 8;
    public static final int SLOT_WINDOW = 0, SLOT_CO2 = 1, SLOT_N2 = 2, SLOT_IN = 3, SLOT_OUT = 4, SLOT_COUNT = 5;
    public static final int DATA_BEAM = DATA_COUNT, DATA_RUNS = DATA_COUNT + 1, DATA_CRACKED = DATA_COUNT + 2, EXTRA_DATA = 3;
    public static final int ACCIDENT_SHOW = 20 * 10;

    public static boolean isSaltWindow(ItemStack s) { return s.is(Optics.SALT_WINDOW); }
    /** 窓の入口に置ける物：岩塩の窓のほか、比べるためのガラス・石英（赤外線を吸って割れる） */
    public static boolean isWindow(ItemStack s) {
        return isSaltWindow(s) || s.is(Items.GLASS) || s.is(Optics.FUSED_QUARTZ) || s.is(Optics.CROWN_GLASS) || s.is(Optics.SYNTHETIC_QUARTZ);
    }

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_WINDOW, 8, 17, Co2LaserBlockEntity::isWindow, 1)
            .in(SLOT_CO2, 8, 39, s -> s.is(Gas.CARBON_DIOXIDE_GAS))
            .in(SLOT_N2, 30, 39, s -> s.is(Gas.NITROGEN_GAS))
            .in(SLOT_IN, 30, 17, s -> s.is(Metallurgy.VEIN_QUARTZ))
            .out(SLOT_OUT, 62, 17)
            .right("power", 17, 1, "gui.semiconductor-lab.co2_laser.power").samples("1.0 kW")
            .right("beam", 28, 1, "gui.semiconductor-lab.co2_laser.beam").samples("60 W", "8")
            .right("hint", 39, 3, MachineLayout.keys("gui.semiconductor-lab.co2_laser.", "need_window", "cracked", "need_co2", "weak",
                    "need_input", "output_full", "need_power", "running"))
            .build();

    private int runs;
    private boolean nitrogen;
    private int accidentTicks;

    public Co2LaserBlockEntity(BlockPos pos, BlockState state) {
        super(Optics.CO2_LASER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public int runs() { return runs; }
    public boolean hadAccident() { return accidentTicks > 0; }

    /** 今の詰め物で出る赤外線の出力（W）。詰めていなければ、入口の袋で詰めたときの値 */
    public double beamW() {
        boolean n2 = runs > 0 ? nitrogen : container.getItem(SLOT_N2).is(Gas.NITROGEN_GAS);
        boolean gas = runs > 0 || container.getItem(SLOT_CO2).is(Gas.CARBON_DIOXIDE_GAS);
        return gas ? WATTS * (n2 ? EFF_N2 : EFF_CO2) : 0;
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return i == DATA_BEAM ? (int) Math.round(beamW()) : i == DATA_RUNS ? runs : i == DATA_CRACKED ? (accidentTicks > 0 ? 1 : 0) : 0;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TICKS; }

    /** 管にガスを詰める（CO2 の袋1つ、あれば N2 の袋1つ） */
    private boolean fill() {
        if (runs > 0) return true;
        if (!container.getItem(SLOT_CO2).is(Gas.CARBON_DIOXIDE_GAS)) return false;
        container.getItem(SLOT_CO2).shrink(1);
        nitrogen = container.getItem(SLOT_N2).is(Gas.NITROGEN_GAS);
        if (nitrogen) container.getItem(SLOT_N2).shrink(1);
        runs = RUNS_PER_FILL;
        container.setChanged();
        return true;
    }

    @Override protected boolean canWork() {
        if (accidentTicks > 0) { accidentTicks--; return false; }
        ItemStack win = container.getItem(SLOT_WINDOW);
        if (!isWindow(win)) return false;
        boolean gas = runs > 0 || container.getItem(SLOT_CO2).is(Gas.CARBON_DIOXIDE_GAS);
        if (!gas || !container.getItem(SLOT_IN).is(Metallurgy.VEIN_QUARTZ) || getStorage().getStoredJ() <= 0) return false;
        if (!isSaltWindow(win)) {
            // 事故（小さな失敗）：ガラス・石英は 10.6µm を吸って熱くなり割れる。窓を失って止まる
            container.setItem(SLOT_WINDOW, ItemStack.EMPTY);
            accidentTicks = ACCIDENT_SHOW;
            container.setChanged();
            return false;
        }
        if (beamW() < MELT_W) return false;
        return canMerge(container.getItem(SLOT_OUT), new ItemStack(Optics.FUSED_QUARTZ)) && fill();
    }

    @Override protected void finishWork() {
        container.getItem(SLOT_IN).shrink(1);
        merge(SLOT_OUT, new ItemStack(Optics.FUSED_QUARTZ));
        runs--;
        ItemStack win = container.getItem(SLOT_WINDOW);
        win.setDamageValue(win.getDamageValue() + 1);   // 岩塩は湿気で曇る
        if (win.getDamageValue() >= win.getMaxDamage()) container.setItem(SLOT_WINDOW, ItemStack.EMPTY);
        container.setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("Runs", runs); out.putBoolean("N2", nitrogen); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); runs = in.getIntOr("Runs", 0); nitrogen = in.getBooleanOr("N2", false); }
}
