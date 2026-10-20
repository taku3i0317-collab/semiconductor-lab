package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModBlockEntities;
import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ModItems;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
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
 * Ge 単結晶の引き上げ機 [9-07]（ティールとリトル、ベル研 1950。チョクラルスキー法 1916 を半導体に使った）。
 * 黒鉛のるつぼ（Ge は炭素と反応しない）に精製ゲルマニウムを入れ、高周波の誘導加熱 [3-14] で融点 約938℃ の少し上に保つ。
 * 水素の雰囲気（酸化を防ぐ）の中で、種結晶を融液に浸し、回しながらゆっくり引き上げると、種と同じ向きの1つの結晶が育つ。
 * ① 種結晶づくり：種のスロットにタングステン線 [1-28]＋精製 Ge 1個 → 種結晶 1個。線の先に多結晶が付くが、細く絞って引くと
 *    いちばん速く育つ粒だけが生き残る（粒の選択）。線は結晶に埋まって戻らない
 * ② 単結晶の引き上げ：種のスロットに種結晶＋精製 Ge 4個 → 単結晶インゴット 1個。種は上の端を切り取ってまた使う（減らない）
 * どちらも水素 1袋（流しっぱなしで燃やして捨てる）と 3kW（発振器 1台分）。純度と GeCl4 経路の印はインゴットに受け継ぐ。
 * 速さ：本物は 1時間に 1〜4cm（10〜40mm/h）、回転 3〜20rpm。ゲームでは1本 60秒に短縮（SOURCES.md）。
 */
public class CrystalPullerBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final double WATTS = 3_000;
    public static final int GROW_TICKS = 20 * 60, SEED_TICKS = 20 * 30;
    public static final int GE_PER_INGOT = 4, GE_PER_SEED = 1, H2_PER_RUN = 1;
    public static final int SLOT_GE = 0, SLOT_SEED = 1, SLOT_H2 = 2, SLOT_OUT = 3, SLOT_COUNT = 4;
    private static final int PULL_INTERVAL = 10, H2_TARGET = 16;

    public enum Mode { NONE, SEED, GROW }

    public CrystalPullerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_PULLER, pos, state, SLOT_COUNT, 100_000, WATTS);
    }

    /** 種のスロットの中身で決まる：タングステン線＝種結晶づくり、種結晶＝引き上げ */
    public static Mode modeOf(ItemStack seed) {
        if (seed.is(ModItems.TUNGSTEN_WIRE)) return Mode.SEED;
        if (seed.is(ModItems.GERMANIUM_SEED_CRYSTAL)) return Mode.GROW;
        return Mode.NONE;
    }
    public Mode mode() { return modeOf(container.getItem(SLOT_SEED)); }

    public static boolean isGermanium(ItemStack s) { return s.is(ModItems.REFINED_GERMANIUM); }

    private ItemStack product(Mode m) {
        ItemStack out = new ItemStack(m == Mode.SEED ? ModItems.GERMANIUM_SEED_CRYSTAL : ModItems.GERMANIUM_SINGLE_CRYSTAL);
        if (m == Mode.GROW) {
            ItemStack ge = container.getItem(SLOT_GE);
            Double p = ge.get(ModDataComponents.PURITY);
            if (p != null) out.set(ModDataComponents.PURITY, p);
            if (Boolean.TRUE.equals(ge.get(ModDataComponents.CHLORIDE_ROUTE))) out.set(ModDataComponents.CHLORIDE_ROUTE, true);
        }
        return out;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return mode() == Mode.SEED ? SEED_TICKS : GROW_TICKS; }

    @Override protected boolean canWork() {
        Mode m = mode();
        if (m == Mode.NONE) return false;
        ItemStack ge = container.getItem(SLOT_GE), h2 = container.getItem(SLOT_H2);
        int need = m == Mode.SEED ? GE_PER_SEED : GE_PER_INGOT;
        return isGermanium(ge) && ge.getCount() >= need && h2.is(ModItems.HYDROGEN_GAS) && h2.getCount() >= H2_PER_RUN
                && canMerge(container.getItem(SLOT_OUT), product(m));
    }

    @Override protected void finishWork() {
        Mode m = mode();
        ItemStack out = product(m);
        container.getItem(SLOT_GE).shrink(m == Mode.SEED ? GE_PER_SEED : GE_PER_INGOT);
        container.getItem(SLOT_H2).shrink(H2_PER_RUN);
        if (m == Mode.SEED) container.getItem(SLOT_SEED).shrink(1);   // タングステン線は結晶に埋まる
        merge(SLOT_OUT, out);
        container.setChanged();
    }

    /** 隣のガスホルダー（水素）から、スロットが 16 袋になるまで補充する（水素炉と同じ） */
    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (level.getGameTime() % PULL_INTERVAL != 0) return;
        ItemStack h2 = container.getItem(SLOT_H2);
        if (!h2.isEmpty() && !h2.is(ModItems.HYDROGEN_GAS)) return;
        for (Direction d : Direction.values()) {
            int want = H2_TARGET - container.getItem(SLOT_H2).getCount();
            if (want <= 0) return;
            if (!(level.getBlockEntity(pos.relative(d)) instanceof GasHolderBlockEntity holder)) continue;
            int n = holder.extract(GasType.HYDROGEN, want);
            if (n > 0) merge(SLOT_H2, new ItemStack(ModItems.HYDROGEN_GAS, n));
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.crystal_puller"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CrystalPullerMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition));
    }
}
