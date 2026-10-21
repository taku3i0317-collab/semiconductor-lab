package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.content.Transport;
import com.taku3.semiconductorlab.energy.ConsumerBlockEntity;
import com.taku3.semiconductorlab.item.FoupItem;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天井の搬送 OHT の駆動 [14-23]（1990年代の 300mm の半導体工場の自動搬送 AMHS ⚠️。IMP4-01、O-029 仮決定の案A）：
 * 下（{@link #DROP} マスまで）のロードポートの「出す FOUP」がいっぱい（{@link FoupItem#CAPACITY} 枚）か、入れる FOUP が空で機械が止まったとき、
 * その FOUP を持ち上げ、つながったレール {@link OhtRailBlock} の行き止まりの下にある別のロードポートの「入れる FOUP」の口（空のとき）へ運んで降ろす。
 * 運ぶ時間はレールの長さに比例（1マス {@link #TICKS_PER_RAIL} tick ⚠️）、運んでいる間は台車のいるレールが光る。電気 {@link #WATTS} W。レッドストーンは使わない。
 * ⚠️ 本物の台車はレールの上を走り、ベルトで FOUP を吊り下ろす（oht_drive.placeholder）。
 */
public class OhtDriveBlockEntity extends ConsumerBlockEntity {
    public static final double WATTS = 200;
    public static final int TICKS_PER_RAIL = 5, DROP = 4, MAX_RAIL = 256;
    public static final int SLOT_CARRY = 0;
    /** 状態 0 待つ・1 運ぶ・2 降ろす先がない・3 下にロードポートがない */
    public static final int DATA_RAILS = DATA_COUNT, DATA_STATE = DATA_COUNT + 1;

    public static final MachineLayout LAYOUT = MachineLayout.builder(DATA_COUNT + 2)
            .out(SLOT_CARRY, 8, 35)
            .right("power", 17, 1, "gui.semiconductor-lab.oht_drive.power").samples("500.0 kW")
            .right("rail", 28, 1, "gui.semiconductor-lab.oht_drive.rail").samples("256")
            .right("hint", 40, 3, "gui.semiconductor-lab.oht_drive.no_port", "gui.semiconductor-lab.oht_drive.no_dest",
                    "gui.semiconductor-lab.oht_drive.waiting", "gui.semiconductor-lab.oht_drive.need_power", "gui.semiconductor-lab.oht_drive.running")
            .bottom("recipe", "gui.semiconductor-lab.oht_drive.recipe")
            .build();

    /** 行き先：通るレール（駆動の隣から行き止まりまで）と、降ろすロードポート */
    public record Route(List<BlockPos> rails, BlockPos port) {}

    private int rails, state, scan;
    private Route route;
    private BlockPos litRail;

    public OhtDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Transport.OHT_DRIVE.type, pos, state, 1, 4_000, WATTS);
    }

    /** 下 DROP マスまでにあるロードポート */
    public static LoadPortBlockEntity portBelow(Level level, BlockPos p) {
        for (int i = 1; i <= DROP; i++) if (level.getBlockEntity(p.below(i)) instanceof LoadPortBlockEntity lp) return lp;
        return null;
    }

    /** レールの数（駆動からつながっている分） */
    public static int railCount(Level level, BlockPos drive) { return parents(level, drive).size(); }

    /** 駆動からレールをたどる（幅優先）。値は1つ手前（最初のレールは駆動） */
    private static Map<BlockPos, BlockPos> parents(Level level, BlockPos drive) {
        Map<BlockPos, BlockPos> parent = new HashMap<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        q.add(drive);
        while (!q.isEmpty() && parent.size() < MAX_RAIL) {
            BlockPos c = q.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = c.relative(d);
                if (n.equals(drive) || parent.containsKey(n) || !(level.getBlockState(n).getBlock() instanceof OhtRailBlock)) continue;
                parent.put(n, c);
                q.add(n);
            }
        }
        return parent;
    }

    /** 下のロードポートの「入れる FOUP」が空の行き止まりのうち、いちばん近い所への道。なければ null（テストからも呼ぶ） */
    public static Route route(Level level, BlockPos drive) {
        Map<BlockPos, BlockPos> parent = parents(level, drive);
        Route best = null;
        for (BlockPos r : parent.keySet()) {
            int links = 0;
            for (Direction d : Direction.values()) if (parent.containsKey(r.relative(d)) || r.relative(d).equals(drive)) links++;
            if (links != 1) continue;   // 行き止まりだけ
            LoadPortBlockEntity lp = portBelow(level, r);
            if (lp == null || !lp.getContainer().getItem(LoadPortBlockEntity.SLOT_IN_FOUP).isEmpty()) continue;
            List<BlockPos> path = new ArrayList<>();
            for (BlockPos c = r; !c.equals(drive); c = parent.get(c)) path.add(0, c);
            if (best == null || path.size() < best.rails().size()) best = new Route(path, lp.getBlockPos());
        }
        return best;
    }

    /** 出す FOUP を送る時か：いっぱい、または入れる FOUP にウェハーが無く（機械が止まった）出す FOUP にウェハーがある */
    public static boolean shouldSend(LoadPortBlockEntity lp) {
        ItemStack out = lp.getContainer().getItem(LoadPortBlockEntity.SLOT_OUT_FOUP);
        if (!(out.getItem() instanceof FoupItem)) return false;
        int n = FoupItem.contents(out).getCount();
        if (n >= FoupItem.CAPACITY) return true;
        ItemStack in = lp.getContainer().getItem(LoadPortBlockEntity.SLOT_IN_FOUP);
        return n > 0 && (!(in.getItem() instanceof FoupItem) || FoupItem.contents(in).isEmpty());
    }

    @Override protected void beforeWork(Level level, BlockPos pos) {
        if (scan-- > 0) return;
        scan = 20;
        rails = railCount(level, pos);
        if (container.getItem(SLOT_CARRY).isEmpty()) {
            LoadPortBlockEntity src = portBelow(level, pos);
            route = src != null && shouldSend(src) ? route(level, pos) : null;
            if (route != null) {   // 持ち上げる
                container.setItem(SLOT_CARRY, src.getContainer().getItem(LoadPortBlockEntity.SLOT_OUT_FOUP).copy());
                src.getContainer().setItem(LoadPortBlockEntity.SLOT_OUT_FOUP, ItemStack.EMPTY);
                progress = 0;
            }
            state = src == null ? 3 : route != null ? 1 : 0;
        } else {
            Route r = route(level, pos);
            if (route == null || r == null || !r.port().equals(route.port())) { route = r; progress = 0; }
            state = route == null ? 2 : 1;
        }
    }

    @Override protected double workWatts() { return WATTS; }
    @Override protected int workTicks() { return route == null ? 20 : Math.max(20, route.rails().size() * TICKS_PER_RAIL); }
    @Override protected boolean canWork() { return route != null && container.getItem(SLOT_CARRY).getItem() instanceof FoupItem; }

    @Override protected void finishWork() {
        if (level == null || route == null) return;
        if (level.getBlockEntity(route.port()) instanceof LoadPortBlockEntity lp && lp.getContainer().getItem(LoadPortBlockEntity.SLOT_IN_FOUP).isEmpty()) {
            lp.getContainer().setItem(LoadPortBlockEntity.SLOT_IN_FOUP, container.getItem(SLOT_CARRY).copy());
            container.setItem(SLOT_CARRY, ItemStack.EMPTY);
            route = null;
            scan = 0;
        }
    }

    @Override protected void serverTick(Level level, BlockPos pos, BlockState st) {
        super.serverTick(level, pos, st);
        BlockPos want = null;   // 台車のいるレールを光らせる
        if (route != null && !route.rails().isEmpty() && getLastWatts() > 0)
            want = route.rails().get(Math.min(route.rails().size() - 1, progress * route.rails().size() / Math.max(1, workTicks())));
        if (want == null ? litRail != null : !want.equals(litRail)) {
            setRail(level, litRail, false);
            setRail(level, want, true);
            litRail = want;
        }
    }

    private static void setRail(Level level, BlockPos p, boolean lit) {
        if (p == null) return;
        BlockState s = level.getBlockState(p);
        if (s.getBlock() instanceof OhtRailBlock && s.getValue(OhtRailBlock.LIT) != lit) level.setBlock(p, s.setValue(OhtRailBlock.LIT, lit), 3);
    }

    @Override protected int extraDataCount() { return 2; }
    @Override protected int extraData(int i) { return i == 0 ? rails : state; }
}
