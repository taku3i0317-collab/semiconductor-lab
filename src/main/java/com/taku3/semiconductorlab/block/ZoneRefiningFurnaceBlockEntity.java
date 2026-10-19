package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.*;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * 帯域精製炉（Pfann 1952）。精製（未精製→6N→9N→10N）と、ゾーンレベリング（9N以上＋ドーパント→n型/p型）。
 * 電気で動く：組み込みの高周波発振器 [4-09] が電力（WATTS）を使い、誘導コイルで Ge の帯だけを溶かす。電気がないと止まる。
 * 黒鉛ボートを中央のスロットに入れる（消耗しない）。粘土黒鉛ボート（19世紀式）は粘土の不純物がしみ出すので 9N まで、
 * 人造黒鉛ボート（アチソン炉 [3-10]）なら 10N まで上がる。
 */
public class ZoneRefiningFurnaceBlockEntity extends ConsumerBlockEntity implements MenuProvider {
    public static final int TOTAL_TICKS = 20 * 30;
    public static final double WATTS = 3_000;
    public static final int GE_SLOT = 0, DOPANT_SLOT = 1, BOAT_SLOT = 2, SLOT_COUNT = 3;
    public static final int DATA_NINES = DATA_COUNT, DATA_MODE = DATA_COUNT + 1, DATA_BOAT_CAP = DATA_COUNT + 2, DATA_ROUTE_CAP = DATA_COUNT + 3, EXTRA_DATA = 4;
    public static final int MODE_IDLE = 0, MODE_REFINE = 1, MODE_DOPE = 2;

    /** 中身が変わったら進捗をやり直す（途中で Ge を足して「ただで精製」させないため） */
    private Integer lastSignature;

    public ZoneRefiningFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZONE_REFINING_FURNACE, pos, state, SLOT_COUNT, 30_000, WATTS);
    }

    public static boolean isGermanium(ItemStack s) { return s.is(ModItems.RAW_GERMANIUM) || s.is(ModItems.REFINED_GERMANIUM); }
    public static boolean isDopant(ItemStack s) { return s.is(ModItems.PHOSPHORUS) || s.is(ModItems.INDIUM); }
    public static boolean isBoat(ItemStack s) { return s.is(ModItems.GRAPHITE_BOAT) || s.is(ModItems.SYNTHETIC_GRAPHITE_BOAT); }
    public static boolean isTransistorGrade(ItemStack s) { Double p = s.get(ModDataComponents.PURITY); return s.is(ModItems.REFINED_GERMANIUM) && p != null && p >= Purity.NINE_N; }

    /** ボートで届く純度の上限。ボートがなければ 0 */
    public static double boatCap(ItemStack boat) {
        if (boat.is(ModItems.SYNTHETIC_GRAPHITE_BOAT)) return Purity.TEN_N;
        if (boat.is(ModItems.GRAPHITE_BOAT)) return Purity.NINE_N;
        return 0;
    }

    /** GeCl4 蒸留 [1-34] を経たゲルマニウムか（データコンポーネント chloride_route） */
    public static boolean isChlorideRoute(ItemStack s) { return Boolean.TRUE.equals(s.get(ModDataComponents.CHLORIDE_ROUTE)); }

    /**
     * 原料の経路で決まる純度の上限：GeCl4 蒸留経由なら 10N、硫酸浸出のまま（蒸留していない）なら 9N。
     * 本物：硫酸浸出の GeO2 には As・Si などが残り、帯域精製でも分配係数が1に近い不純物は取り切れない。GeCl4 は沸点83℃で蒸留でき、AsCl3（130℃）などと分かれる。
     */
    public static double routeCap(ItemStack ge) { return isChlorideRoute(ge) ? Purity.TEN_N : Purity.NINE_N; }

    /** ボートと原料の経路の、低いほうの上限 */
    public static double effectiveCap(ItemStack ge, ItemStack boat) { return Math.min(boatCap(boat), routeCap(ge)); }

    /** 次の純度段階。上がらないなら -1 */
    public static double nextPurity(ItemStack s) {
        if (s.is(ModItems.RAW_GERMANIUM)) return Purity.SIX_N;
        Double p = s.get(ModDataComponents.PURITY);
        if (p == null) return -1;
        if (p < Purity.NINE_N) return Purity.NINE_N;
        if (p < Purity.TEN_N) return Purity.TEN_N;
        return -1;
    }

    private int currentNines() { ItemStack s = container.getItem(GE_SLOT); if (s.isEmpty()) return -1; Double p = s.get(ModDataComponents.PURITY); return p == null ? 0 : Purity.nines(p); }

    private int mode() {
        ItemStack ge = container.getItem(GE_SLOT), dp = container.getItem(DOPANT_SLOT);
        double cap = effectiveCap(ge, container.getItem(BOAT_SLOT));
        if (ge.isEmpty() || cap <= 0) return MODE_IDLE;
        if (isTransistorGrade(ge) && isDopant(dp)) return MODE_DOPE;
        double next = nextPurity(ge);
        if (isGermanium(ge) && next > 0 && next <= cap) return MODE_REFINE;
        return MODE_IDLE;
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return TOTAL_TICKS; }
    @Override protected boolean canWork() { return mode() != MODE_IDLE; }

    @Override protected void beforeWork(Level level, BlockPos pos) {
        ItemStack ge = container.getItem(GE_SLOT);
        int sig = Objects.hash(ge.getItem(), ge.getCount(), ge.get(ModDataComponents.PURITY),
                container.getItem(DOPANT_SLOT).getItem(), container.getItem(BOAT_SLOT).getItem());
        if (lastSignature != null && lastSignature != sig && progress != 0) { progress = 0; setChanged(); }
        lastSignature = sig;
    }

    @Override protected void finishWork() {
        ItemStack ge = container.getItem(GE_SLOT);
        if (mode() == MODE_REFINE) {
            ItemStack out = new ItemStack(ModItems.REFINED_GERMANIUM, ge.getCount());
            out.set(ModDataComponents.PURITY, nextPurity(ge));
            if (isChlorideRoute(ge)) out.set(ModDataComponents.CHLORIDE_ROUTE, true);   // 経路は受け継ぐ
            container.setItem(GE_SLOT, out);
        } else {
            ItemStack dp = container.getItem(DOPANT_SLOT);
            ItemStack out = new ItemStack(dp.is(ModItems.PHOSPHORUS) ? ModItems.N_TYPE_GERMANIUM : ModItems.P_TYPE_GERMANIUM, ge.getCount());
            dp.shrink(1);
            container.setItem(DOPANT_SLOT, dp.isEmpty() ? ItemStack.EMPTY : dp);
            container.setItem(GE_SLOT, out);
        }
    }

    @Override protected int extraDataCount() { return EXTRA_DATA; }
    @Override protected int extraData(int i) {
        return switch (i) {
            case DATA_NINES -> currentNines();
            case DATA_MODE -> mode();
            case DATA_BOAT_CAP -> { double c = boatCap(container.getItem(BOAT_SLOT)); yield c <= 0 ? 0 : Purity.nines(c); }
            case DATA_ROUTE_CAP -> container.getItem(GE_SLOT).isEmpty() ? 0 : Purity.nines(routeCap(container.getItem(GE_SLOT)));
            default -> 0;
        };
    }

    @Override public Component getDisplayName() { return Component.translatable("block.semiconductor-lab.zone_refining_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ZoneRefiningFurnaceMenu(id, inv, container, data, ContainerLevelAccess.create(level, worldPosition)); }
}
