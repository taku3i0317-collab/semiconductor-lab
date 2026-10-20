package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 加圧加熱釜（オートクレーブ）[2-28]（O-011、ベークランドの「ベークライザー」1909年ごろ ⚠️）。厚い鋼の釜に材料と水を入れて電気で熱する。
 * 水が沸くと、ふたが閉まっているので蒸気の圧力が上がる（飽和水蒸気圧：100℃で1気圧、約150℃で約5気圧、約190℃で約12気圧）。
 * 圧力をかけながら熱すると、材料から出る水やホルムアルデヒドの蒸気が泡にならない＝ベークランドの発明の核心。
 *
 * 温度のしくみ（アチソン炉と同じ簡単なモデル）：毎 tick「入った電力 − 逃げる熱」。電力 WATTS で STEADY_C に落ち着く。
 * 圧力：水があれば温度の飽和水蒸気圧（アントワンの式）、安全弁 VALVE_ATM で頭打ち。水がなければ 1気圧のまま。
 * 処理：材料の温度 minC 以上の間だけ進む。進んでいる間に一度でも圧力が minAtm より低いと「泡が入った」失敗作になる。
 *
 * <b>組み合わせの表 {@link #RECIPES}</b>：ほかの担当は自分の content/<幹>.java の static 初期化で
 * {@code AutoclaveBlockEntity.RECIPES.add(new AutoclaveBlockEntity.Recipe("id", s -> s.is(X), 1, () -> new ItemStack(Y), () -> new ItemStack(Z), 150, 5, 600));} の1行を足せばよい
 * （失敗作がないなら failed に () -> ItemStack.EMPTY ＝何も出ない）。GameTest は ChemistryTests.autoclave の形をまねる。
 */
public class AutoclaveBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 10_000;                    // 10kW（ゲームの値）
    public static final double AMBIENT_C = 20, STEADY_C = 200, BOIL_C = 100, VALVE_ATM = 12;
    public static final double TAU_SECONDS = 10;
    public static final double K_W_PER_C = WATTS / (STEADY_C - AMBIENT_C);
    public static final double HEAT_CAPACITY_J_PER_C = K_W_PER_C * TAU_SECONDS;
    public static final int RUNS_PER_WATER = 4;
    public static final int SLOT_IN = 0, SLOT_WATER = 1, SLOT_OUT = 2, SLOT_BUCKET = 3, SLOT_COUNT = 4;
    public static final int DATA_TEMP = DATA_COUNT, DATA_ATM10 = DATA_COUNT + 1, DATA_BUBBLES = DATA_COUNT + 2, DATA_NEED_C = DATA_COUNT + 3, DATA_NEED_ATM10 = DATA_COUNT + 4, DATA_WATER = DATA_COUNT + 5, EXTRA_DATA = 6;

    /** 組み合わせ：材料 count 個 → output（圧力が足りていれば）／ failed（足りなければ）。minC 以上で ticks 進めると終わる */
    public record Recipe(String id, Predicate<ItemStack> input, int count, Supplier<ItemStack> output, Supplier<ItemStack> failed, double minC, double minAtm, int ticks) {}

    /** 組み合わせの表。上から順に探す */
    public static final List<Recipe> RECIPES = new ArrayList<>(List.of(
            // [2-28] ベークライト：レゾールの反応液を約150℃以上・5気圧以上で固める。圧力が足りないと泡だらけ
            new Recipe("bakelite", s -> s.is(Chemistry.RESOL_MIX), 1, () -> new ItemStack(Chemistry.BAKELITE, 2), () -> new ItemStack(Chemistry.FOAMY_BAKELITE, 2), 150, 5, 20 * 30),
            // [2-38] PTFE：TFE を水の中で温めて重合（IMP3、S-002）。⚠️ 本物の圧力は TFE 自身（約10〜30気圧）と過硫酸の開始剤。この釜の圧力は水の蒸気だけなので、温度だけを条件にした（ptfe.placeholder）
            new Recipe("ptfe", s -> s.is(Chemistry.TETRAFLUOROETHYLENE_GAS), 4, () -> new ItemStack(Chemistry.PTFE), () -> ItemStack.EMPTY, 80, 1, 20 * 30)
    ));

    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT + EXTRA_DATA)
            .in(SLOT_IN, 8, 17, s -> find(s) != null)
            .in(SLOT_WATER, 8, 53, s -> s.is(Items.WATER_BUCKET))
            .out(SLOT_OUT, 78, 17)
            .out(SLOT_BUCKET, 78, 53)
            .right("power", 16, 1, "gui.semiconductor-lab.autoclave.power").samples("10.0 kW")
            .right("temp", 25, 1, "gui.semiconductor-lab.autoclave.temp").samples("200℃")
            .right("pressure", 34, 1, "gui.semiconductor-lab.autoclave.pressure").samples("12.0 atm")
            .right("need", 43, 1, "gui.semiconductor-lab.autoclave.need").samples("150℃ 5atm")
            .right("hint", 53, 2, "gui.semiconductor-lab.autoclave.need_input", "gui.semiconductor-lab.autoclave.output_full", "gui.semiconductor-lab.autoclave.need_power",
                    "gui.semiconductor-lab.autoclave.heating", "gui.semiconductor-lab.autoclave.low_pressure", "gui.semiconductor-lab.autoclave.curing", "gui.semiconductor-lab.autoclave.bubbled")
            .build();

    private double temperature = AMBIENT_C;
    /** 水があと何回ぶんか（保存する）。0 なら圧力は上がらない */
    private int water;
    /** 今の処理の途中で、圧力が足りない時があったか（保存する） */
    private boolean bubbles;

    public AutoclaveBlockEntity(BlockPos pos, BlockState state) {
        super(Chemistry.AUTOCLAVE.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    public static Recipe find(ItemStack s) {
        if (s.isEmpty()) return null;
        for (Recipe r : RECIPES) if (r.input().test(s)) return r;
        return null;
    }

    /** 飽和水蒸気圧（気圧）。アントワンの式（水、mmHg、100〜374℃）。100℃より下は 1気圧（大気） */
    public static double steamAtm(double c) {
        if (c <= BOIL_C) return 1.0;
        return Math.pow(10, 8.14019 - 1810.94 / (244.485 + c)) / 760.0;
    }

    public double getTemperature() { return temperature; }
    public double getPressureAtm() { return water > 0 ? Math.min(VALVE_ATM, steamAtm(temperature)) : 1.0; }
    public int getWater() { return water; }
    public boolean hasBubbles() { return bubbles; }
    /** テスト用：温度を決める（GameTest で温まるのを待たない） */
    public void setTemperatureForTest(double c) { temperature = c; }

    private Recipe current() {
        Recipe r = find(container.getItem(SLOT_IN));
        return r != null && container.getItem(SLOT_IN).getCount() >= r.count() ? r : null;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { Recipe r = current(); return r == null ? 20 * 30 : r.ticks(); }

    @Override protected boolean canWork() {
        Recipe r = current();
        return r != null && canMerge(container.getItem(SLOT_OUT), r.output().get()) && canMerge(container.getItem(SLOT_OUT), r.failed().get());
    }

    @Override protected void finishWork() {
        Recipe r = current();
        if (r == null) return;
        container.getItem(SLOT_IN).shrink(r.count());
        ItemStack out = bubbles ? r.failed().get() : r.output().get();
        if (!out.isEmpty()) merge(SLOT_OUT, out);
        if (water > 0) water--;
        bubbles = false;
        container.setChanged();
    }

    /** 処理を始めるとき：水が尽きていて水バケツがあれば、入れて空のバケツを出す */
    private void fillWater() {
        if (water > 0 || !container.getItem(SLOT_WATER).is(Items.WATER_BUCKET) || !canMerge(container.getItem(SLOT_BUCKET), new ItemStack(Items.BUCKET))) return;
        container.getItem(SLOT_WATER).shrink(1);
        merge(SLOT_BUCKET, new ItemStack(Items.BUCKET));
        water = RUNS_PER_WATER;
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        // 1) 電気で熱する（材料が入っているときだけ電気を使う）
        boolean ready = canWork();
        long drawn = ready ? storage.extractRaw(Math.min(storage.getStoredJ(), EnergyStorage.wattsToJoulesPerTick(WATTS))) : 0;
        double watts = drawn * 20.0;
        double before = temperature;
        temperature += (watts - K_W_PER_C * (temperature - AMBIENT_C)) / HEAT_CAPACITY_J_PER_C / 20.0;
        if (temperature < AMBIENT_C) temperature = AMBIENT_C;
        setLastWatts(watts);
        setReady(ready);
        // 2) 処理：材料の温度以上の間だけ進む。圧力が足りない tick があれば泡が入る
        Recipe r = ready ? current() : null;
        if (r == null) { if (progress != 0 || bubbles) { progress = 0; bubbles = false; } }
        else if (temperature >= r.minC()) {
            if (progress == 0) fillWater();
            if (getPressureAtm() < r.minAtm()) bubbles = true;
            if (++progress >= r.ticks()) { progress = 0; finishWork(); }
        }
        if (drawn > 0 || Math.abs(before - temperature) > 1e-6) setChanged();
        setLit(level, pos, state, temperature >= BOIL_C);
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        Recipe r = current();
        return switch (i) {
            case DATA_TEMP -> (int) Math.round(temperature);
            case DATA_ATM10 -> (int) Math.round(getPressureAtm() * 10);
            case DATA_BUBBLES -> bubbles ? 1 : 0;
            case DATA_NEED_C -> r == null ? 0 : (int) Math.round(r.minC());
            case DATA_NEED_ATM10 -> r == null ? 0 : (int) Math.round(r.minAtm() * 10);
            case DATA_WATER -> water;
            default -> 0;
        };
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("TempC", (int) Math.round(temperature)); out.putInt("Water", water); out.putBoolean("Bubbles", bubbles);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        temperature = in.getIntOr("TempC", (int) AMBIENT_C); water = in.getIntOr("Water", 0); bubbles = in.getBooleanOr("Bubbles", false);
    }
}
