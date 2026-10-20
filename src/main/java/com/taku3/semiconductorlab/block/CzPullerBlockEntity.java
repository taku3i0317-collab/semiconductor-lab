package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.Dopant;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.energy.EnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CZ 引き上げ機（シリコン）[9-08]。Ge の引き上げ機 [9-07] を大きくしたもの（チョクラルスキー 1916、シリコンでは1950年代〜）。
 * 石英るつぼ（シリコンは黒鉛と反応して SiC になるので石英）に多結晶シリコン [1-17] を入れ、黒鉛ヒーターで融点 1414℃ の少し上
 * （約1420℃）に保つ。アルゴン [6-07] を流しながら（酸化を防ぎ、るつぼから出る SiO を運び去る）種結晶を浸し、回しながら引き上げる。
 * ① 種結晶づくり：種のスロットに多結晶シリコン 1個 → シリコン種結晶（細く絞って引くと1つの粒だけが残る、ダッシュのネッキング 1959 ⚠️）
 * ② 単結晶：種のスロットにシリコン種結晶（減らない）＋多結晶シリコン 2個 → シリコン単結晶インゴット 1本（約2kg）。
 *    ドーパントのスロットに B2O3 か白リン 1個 [9-11] を入れると p型／n型（入れなければ不純物を入れない真性シリコン）。純度は多結晶シリコンから受け継ぐ。
 * どちらも石英るつぼ 1個（冷えると割れるので毎回使い捨て、本物も同じ）とアルゴン。電力はヒーター 150kW（引き上げ機の仕様例の最大値）。
 * 電力量 17.4kWh/kg（国立台湾大学の改良後の値。改良前は 59.1kWh/kg）＝ 2kg で 34.8kWh ＝ 150kW × 835秒。
 * ⚠️ 速さ：本物の引き上げは 1 分に約 1mm（2kg・直径10cm なら約2時間）。ゲームは約8倍速い（電力量は本物どおりにした）。
 * ⚠️ アルゴンの量は記号的（本物は 1kg あたり数十 mol 流す）。
 */
public class CzPullerBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 150_000;
    /** 34.8 kWh ÷ 150 kW = 835.2 秒 */
    public static final int GROW_TICKS = 16704, SEED_TICKS = 20 * 60;
    public static final double KWH_PER_KG = 17.4;
    public static final int POLY_PER_INGOT = 2, POLY_PER_SEED = 0, AR_PER_INGOT = 2, AR_PER_SEED = 1, CRUCIBLE_PER_RUN = 1;
    public static final int SLOT_POLY = 0, SLOT_SEED = 1, SLOT_DOPANT = 2, SLOT_CRUCIBLE = 3, SLOT_AR = 4, SLOT_OUT = 5, SLOT_COUNT = 6;
    private static final int PULL_INTERVAL = 10, AR_TARGET = 16;

    public enum Mode { NONE, SEED, GROW }

    /** 画面のスロットの並び（IMP-28：前の CzPullerMenu の代わり） */
    public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
            .in(SLOT_POLY, 8, 17, CzPullerBlockEntity::isPoly)
            .in(SLOT_SEED, 8, 35, s -> modeOf(s) != Mode.NONE)
            .in(SLOT_DOPANT, 8, 53, s -> com.taku3.semiconductorlab.Dopant.of(s) != null)
            .in(SLOT_CRUCIBLE, 30, 17, CzPullerBlockEntity::isCrucible)
            .in(SLOT_AR, 30, 53, CzPullerBlockEntity::isArgon)
            .out(SLOT_OUT, 78, 35)
            .build();

    public CzPullerBlockEntity(BlockPos pos, BlockState state) {
        super(Semiconductor.CZ_PULLER.type, pos, state, SLOT_COUNT, EnergyStorage.wattsToJoulesPerTick(WATTS) * 4, WATTS);
    }

    /** 種のスロットの中身で決まる：多結晶シリコン＝種結晶づくり、シリコン種結晶＝引き上げ */
    public static Mode modeOf(ItemStack seed) {
        if (seed.is(Metallurgy.POLYSILICON)) return Mode.SEED;
        if (seed.is(Semiconductor.SILICON_SEED_CRYSTAL)) return Mode.GROW;
        return Mode.NONE;
    }
    public Mode mode() { return modeOf(container.getItem(SLOT_SEED)); }

    public static boolean isPoly(ItemStack s) { return s.is(Metallurgy.POLYSILICON); }
    public static boolean isCrucible(ItemStack s) { return s.is(Semiconductor.QUARTZ_CRUCIBLE); }
    public static boolean isArgon(ItemStack s) { return s.is(Gas.ARGON_GAS); }

    private ItemStack product(Mode m) {
        if (m == Mode.SEED) return new ItemStack(Semiconductor.SILICON_SEED_CRYSTAL);
        ItemStack out = new ItemStack(Semiconductor.SILICON_SINGLE_CRYSTAL);
        Double p = container.getItem(SLOT_POLY).get(ModDataComponents.PURITY);
        if (p != null) out.set(ModDataComponents.PURITY, p);
        Dopant d = Dopant.of(container.getItem(SLOT_DOPANT));
        if (d != null) out.set(ModDataComponents.DOPANT, d.id);
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return mode() == Mode.SEED ? SEED_TICKS : GROW_TICKS; }

    @Override protected boolean canWork() {
        Mode m = mode();
        if (m == Mode.NONE) return false;
        ItemStack poly = container.getItem(SLOT_POLY), ar = container.getItem(SLOT_AR);
        int needPoly = m == Mode.SEED ? POLY_PER_SEED : POLY_PER_INGOT, needAr = m == Mode.SEED ? AR_PER_SEED : AR_PER_INGOT;
        ItemStack dp = container.getItem(SLOT_DOPANT);
        return (needPoly == 0 || (isPoly(poly) && poly.getCount() >= needPoly))
                && isCrucible(container.getItem(SLOT_CRUCIBLE))
                && isArgon(ar) && ar.getCount() >= needAr
                && (dp.isEmpty() || Dopant.of(dp) != null)
                && canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        Mode m = mode();
        ItemStack out = product(m);
        if (m == Mode.SEED) {
            container.getItem(SLOT_SEED).shrink(1);   // 多結晶のかけらは溶けて、細く絞った首の先に1つの粒が残る
            container.getItem(SLOT_AR).shrink(AR_PER_SEED);
        } else {
            container.getItem(SLOT_POLY).shrink(POLY_PER_INGOT);
            container.getItem(SLOT_AR).shrink(AR_PER_INGOT);
            if (!container.getItem(SLOT_DOPANT).isEmpty()) container.getItem(SLOT_DOPANT).shrink(1);
        }
        container.getItem(SLOT_CRUCIBLE).shrink(CRUCIBLE_PER_RUN);   // 冷えると割れる（使い捨て）
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダー（アルゴン）から、スロットが 16 袋になるまで補充する */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack ar = container.getItem(SLOT_AR);
        if (!ar.isEmpty() && !isArgon(ar)) return;
        for (Direction d : Direction.values()) {
            int want = AR_TARGET - container.getItem(SLOT_AR).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.ARGON, want);
            if (n > 0) merge(SLOT_AR, new ItemStack(Gas.ARGON_GAS, n));
        }
    }

}
