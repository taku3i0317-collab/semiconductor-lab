package com.taku3.semiconductorlab.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 銅ケーブルのネットワーク（タスク06）。つながったケーブル群を「1本の導体」とみなし、毎 tick
 * 「供給側（発電機・電池の放電）」から取り出して「消費側（機械・電池の充電）」に配る。ケーブル自身は電気を貯めない。
 *
 * 決まりごと（ゲームの簡単なモデル）:
 * <ul>
 *   <li>つながった相手は EnergyApi で調べる。取り出せるだけ＝供給、入れられるだけ＝消費、両方できる＝電池</li>
 *   <li>損失：ネットワークの「いちばん長い経路」のケーブル本数 × 1%（本物は I²R。経路ごとに計算せず最長で近似）</li>
 *   <li>通過上限：ネットワーク全体で 1tick に取り出せるのは 1本分（1MW）まで。要求が多いときは要求の大きさに比例して按分</li>
 *   <li>順番：①発電機 → 機械 ②足りなければ 電池 → 機械（放電） ③余れば 発電機 → 電池（充電）。電池どうしでは流さない（行ったり来たりを防ぐ）</li>
 * </ul>
 */
public final class CableNetwork {
    public static final long MAX_TRANSFER = CableBlockEntity.MAX_TRANSFER;   // 1 MW = 50 kJ/tick
    public static final int LOSS_PERCENT_PER_HOP = CableBlockEntity.LOSS_PERCENT;
    /** BFS の上限（巨大ネットワークでサーバーが止まらないように） */
    public static final int MAX_CABLES = 4096;

    public final List<BlockPos> cables;
    public final int longestPath;
    final List<EnergyStorage> sources = new ArrayList<>(), sinks = new ArrayList<>(), batteries = new ArrayList<>();
    /** 直近の tick に「取り出した」量と「届けた」量（J） */
    public long lastTakenJ, lastDeliveredJ;
    private final BlockPos leader;

    private CableNetwork(List<BlockPos> cables, int longestPath, BlockPos leader) {
        this.cables = cables; this.longestPath = longestPath; this.leader = leader;
    }

    public int lossPercent() { return Math.min(100, longestPath * LOSS_PERCENT_PER_HOP); }
    public boolean isLeader(BlockPos pos) { return leader.equals(pos); }
    public int sourceCount() { return sources.size(); }
    public int sinkCount() { return sinks.size(); }
    public int batteryCount() { return batteries.size(); }
    /** 直近に配電した電力（W）。配電はリーダーのケーブルが覚えている */
    public double lastDeliveredW(Level level) {
        return level.getBlockEntity(leader) instanceof CableBlockEntity c ? c.getLastDeliveredJ() * 20.0 : 0;
    }

    // ---------------------------------------------------------------- キャッシュ（同じ tick の中では1回だけ組み立てる）

    private static final Map<Level, Cache> CACHE = new WeakHashMap<>();
    private static final class Cache { long time = Long.MIN_VALUE; final Map<BlockPos, CableNetwork> byPos = new HashMap<>(); }

    /** ケーブルが置かれた・壊れたときに呼ぶ（同じ tick の中で形が変わってもすぐ組み直す） */
    public static void invalidate(Level level) { Cache c = CACHE.get(level); if (c != null) c.byPos.clear(); }

    /** pos のケーブルが属するネットワーク（この tick の分）。ケーブルでなければ null */
    public static CableNetwork get(Level level, BlockPos pos) {
        Cache c = CACHE.computeIfAbsent(level, l -> new Cache());
        long now = level.getGameTime();
        if (c.time != now) { c.time = now; c.byPos.clear(); }
        CableNetwork n = c.byPos.get(pos);
        if (n == null) {
            n = build(level, pos);
            if (n == null) return null;
            for (BlockPos p : n.cables) c.byPos.put(p, n);
        }
        return n;
    }

    private static boolean isCable(Level level, BlockPos p) { return level.isLoaded(p) && level.getBlockEntity(p) instanceof CableBlockEntity; }

    private static CableNetwork build(Level level, BlockPos start) {
        if (!isCable(level, start)) return null;
        List<BlockPos> cables = new ArrayList<>();
        Map<BlockPos, Integer> dist = bfs(level, start, cables);
        // いちばん長い経路の近似：start から最も遠い A → A から最も遠い B の距離（木の形なら正確）。本数＝辺の数＋1
        BlockPos far = farthest(dist);
        Map<BlockPos, Integer> fromFar = bfs(level, far, null);
        int longest = Collections.max(fromFar.values()) + 1;
        BlockPos leader = cables.get(0);
        for (BlockPos p : cables) if (p.asLong() < leader.asLong()) leader = p;
        CableNetwork n = new CableNetwork(cables, longest, leader);
        // つながった機械を集める（同じ機械が2本のケーブルに触れていても1回だけ数える）
        Set<EnergyStorage> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (BlockPos p : cables) for (Direction d : Direction.values()) {
            BlockPos np = p.relative(d);
            if (!level.isLoaded(np) || dist.containsKey(np) || level.getBlockEntity(np) instanceof CableBlockEntity) continue;
            EnergyStorage s = EnergyApi.ENERGY.find(level, np, d.getOpposite());
            if (s == null || !seen.add(s)) continue;
            boolean in = s.canInsert(), out = s.canExtract();
            if (in && out) n.batteries.add(s); else if (out) n.sources.add(s); else if (in) n.sinks.add(s);
        }
        return n;
    }

    private static Map<BlockPos, Integer> bfs(Level level, BlockPos start, List<BlockPos> order) {
        Map<BlockPos, Integer> dist = new HashMap<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        dist.put(start, 0); q.add(start);
        while (!q.isEmpty() && dist.size() <= MAX_CABLES) {
            BlockPos p = q.poll();
            if (order != null) order.add(p);
            for (Direction d : Direction.values()) {
                BlockPos np = p.relative(d);
                if (!dist.containsKey(np) && isCable(level, np)) { dist.put(np, dist.get(p) + 1); q.add(np); }
            }
        }
        if (order != null) while (!q.isEmpty()) order.add(q.poll());
        return dist;
    }

    private static BlockPos farthest(Map<BlockPos, Integer> dist) {
        BlockPos best = null; int bd = -1;
        for (Map.Entry<BlockPos, Integer> e : dist.entrySet()) if (e.getValue() > bd) { bd = e.getValue(); best = e.getKey(); }
        return best;
    }

    // ---------------------------------------------------------------- 1 tick の配電

    /** この tick の配電をする（リーダーのケーブルが1回だけ呼ぶ） */
    public void distribute() {
        int lossPct = lossPercent();
        lastTakenJ = 0; lastDeliveredJ = 0;
        if (lossPct >= 100) return;
        long budget = MAX_TRANSFER;   // 取り出し側で数える通過上限
        // ① 発電機 → 機械
        long genAvail = available(sources);
        long sinkDemand = demand(sinks);
        long t1 = transfer(sources, sinks, Math.min(genAvail, budget), sinkDemand, lossPct);
        budget -= t1; genAvail -= t1;
        long delivered1 = lastDeliveredJ;
        if (delivered1 < sinkDemand && budget > 0) {
            // ② 足りない分を電池から（放電）
            transfer(batteries, sinks, Math.min(available(batteries), budget), sinkDemand - delivered1, lossPct);
        } else if (genAvail > 0 && budget > 0) {
            // ③ 余った分で電池を充電
            transfer(sources, batteries, Math.min(genAvail, budget), demand(batteries), lossPct);
        }
    }

    private static long available(List<EnergyStorage> list) { long s = 0; for (EnergyStorage e : list) s += e.extract(Long.MAX_VALUE / 4, true); return s; }
    private static long demand(List<EnergyStorage> list) { long s = 0; for (EnergyStorage e : list) s += e.insert(Long.MAX_VALUE / 4, true); return s; }

    /**
     * from から最大 maxTake を取り出し、損失を引いて to に配る（to の要求の大きさに比例）。実際に取り出した量を返す。
     * 先に「届けられる量」を決めて入れ、入った量から逆算した分だけ取り出す（受け手がいっぱいなら取り出さない）。
     */
    private long transfer(List<EnergyStorage> from, List<EnergyStorage> to, long maxTake, long wanted, int lossPct) {
        if (maxTake <= 0 || wanted <= 0 || from.isEmpty() || to.isEmpty()) return 0;
        long deliverable = Math.min(wanted, maxTake * (100 - lossPct) / 100);
        if (deliverable <= 0) return 0;
        // 受け手へ按分
        long[] want = new long[to.size()]; long sumWant = 0;
        for (int i = 0; i < to.size(); i++) { want[i] = to.get(i).insert(Long.MAX_VALUE / 4, true); sumWant += want[i]; }
        if (sumWant <= 0) return 0;
        long delivered = 0;
        for (int i = 0; i < to.size(); i++) {
            long share = (long) ((double) deliverable * want[i] / sumWant);
            if (share > 0) delivered += to.get(i).insert(Math.min(share, want[i]), false);
        }
        if (delivered <= 0) return 0;
        // 届いた量 ÷ (1 − 損失) を送り手から按分して取り出す（切り上げ）
        long take = Math.min(maxTake, (delivered * 100 + (99 - lossPct)) / (100 - lossPct));
        long[] avail = new long[from.size()]; long sumAvail = 0;
        for (int i = 0; i < from.size(); i++) { avail[i] = from.get(i).extract(Long.MAX_VALUE / 4, true); sumAvail += avail[i]; }
        long taken = 0;
        for (int i = 0; i < from.size() && sumAvail > 0; i++) {
            long share = (long) Math.ceil((double) take * avail[i] / sumAvail);
            share = Math.min(share, Math.min(avail[i], take - taken));
            if (share > 0) taken += from.get(i).extract(share, false);
        }
        lastTakenJ += taken;
        lastDeliveredJ += delivered;
        return taken;
    }
}
