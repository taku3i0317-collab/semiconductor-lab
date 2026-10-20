package com.taku3.semiconductorlab.litho;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * チップから回路を取り出す（LITHO_DESIGN §6.2）。形ではなく「つながり方」を見るので、どこに描いても・どの向きでも同じ結果になる。
 * <ul>
 *   <li>領域：同じ型でつながったマス（上下左右）</li>
 *   <li>線（net）：つながった金属。CONTACT のマスで下の領域とつながる。ふちのマスにかかる線はパッド</li>
 *   <li>部品：抵抗・ダイオード・トランジスタ（入れ子の n-p-n / p-n-p）</li>
 * </ul>
 */
public final class Netlist {
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public record Region(int id, char type, List<int[]> cells, boolean substrate) {}
    public record Net(int id, List<int[]> cells, Set<Integer> regions, int[] pad) {
        public boolean isPad() { return pad != null; }
    }
    public record Resistor(int region, int netA, int netB, double ohms) {}
    public record Diode(int anodeRegion, int cathodeRegion, int anodeNet, int cathodeNet) {}
    public record Transistor(boolean npn, int emitter, int base, int collector, int eNet, int bNet, int cNet) {}

    public final ChipModel model;
    public final List<Region> regions = new ArrayList<>();
    public final List<Net> nets = new ArrayList<>();
    public final List<Resistor> resistors = new ArrayList<>();
    public final List<Diode> diodes = new ArrayList<>();
    public final List<Transistor> transistors = new ArrayList<>();
    /** マス → 領域の番号、金属のマス → 線の番号（金属がなければ −1） */
    public final int[] regionOf, netOf;
    /** 領域 → それにつながっている線 */
    public final Map<Integer, Set<Integer>> netsOfRegion = new LinkedHashMap<>();

    private Netlist(ChipModel m) {
        model = m;
        int n = m.size;
        regionOf = new int[n * n];
        netOf = new int[n * n];
        java.util.Arrays.fill(regionOf, -1);
        java.util.Arrays.fill(netOf, -1);
    }

    public static Netlist extract(ChipModel m) {
        Netlist nl = new Netlist(m);
        nl.findRegions();
        nl.findNets();
        nl.findDevices();
        return nl;
    }

    private void findRegions() {
        int n = model.size;
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            if (regionOf[y * n + x] >= 0) continue;
            int id = regions.size();
            char t = model.type(x, y);
            List<int[]> cells = new ArrayList<>();
            boolean sub = false;
            ArrayDeque<int[]> q = new ArrayDeque<>();
            q.add(new int[] {x, y});
            regionOf[y * n + x] = id;
            while (!q.isEmpty()) {
                int[] c = q.poll();
                cells.add(c);
                if (model.level(c[0], c[1]) == 0) sub = true;
                for (int[] d : DIRS) {
                    int a = c[0] + d[0], b = c[1] + d[1];
                    if (model.inside(a, b) && regionOf[b * n + a] < 0 && model.type(a, b) == t) { regionOf[b * n + a] = id; q.add(new int[] {a, b}); }
                }
            }
            regions.add(new Region(id, t, cells, sub));
        }
    }

    private void findNets() {
        int n = model.size;
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            if (!model.metal(x, y) || netOf[y * n + x] >= 0) continue;
            int id = nets.size();
            List<int[]> cells = new ArrayList<>();
            Set<Integer> regs = new LinkedHashSet<>();
            int[] pad = null;
            ArrayDeque<int[]> q = new ArrayDeque<>();
            q.add(new int[] {x, y});
            netOf[y * n + x] = id;
            while (!q.isEmpty()) {
                int[] c = q.poll();
                cells.add(c);
                if (model.connected(c[0], c[1])) regs.add(regionOf[c[1] * n + c[0]]);
                if (pad == null && model.edge(c[0], c[1])) pad = c;
                for (int[] d : DIRS) {
                    int a = c[0] + d[0], b = c[1] + d[1];
                    if (model.inside(a, b) && netOf[b * n + a] < 0 && model.metal(a, b)) { netOf[b * n + a] = id; q.add(new int[] {a, b}); }
                }
            }
            nets.add(new Net(id, cells, regs, pad));
            for (int r : regs) netsOfRegion.computeIfAbsent(r, k -> new LinkedHashSet<>()).add(id);
        }
    }

    /** 領域どうしが接しているか */
    public boolean touches(int ra, int rb) {
        for (int[] c : regions.get(ra).cells())
            for (int[] d : DIRS) {
                int a = c[0] + d[0], b = c[1] + d[1];
                if (model.inside(a, b) && regionOf[b * model.size + a] == rb) return true;
            }
        return false;
    }

    /** 領域のまわりの領域 */
    public Set<Integer> neighbours(int r) {
        Set<Integer> s = new LinkedHashSet<>();
        for (int[] c : regions.get(r).cells())
            for (int[] d : DIRS) {
                int a = c[0] + d[0], b = c[1] + d[1];
                if (model.inside(a, b) && regionOf[b * model.size + a] != r) s.add(regionOf[b * model.size + a]);
            }
        return s;
    }

    /** 領域につながっている線（1本だけのときその番号、なければ −1、2本以上も最初の1本） */
    private int firstNet(int r) {
        Set<Integer> s = netsOfRegion.get(r);
        return s == null || s.isEmpty() ? -1 : s.iterator().next();
    }

    private void findDevices() {
        Set<Integer> inTransistor = new LinkedHashSet<>();
        // トランジスタ：E のまわりが全部 B（入れ子）、B は拡散した領域、B のまわりに E と同じ型の C。3つとも別々の線につながる
        for (Region e : regions) {
            if (e.substrate()) continue;
            Set<Integer> around = neighbours(e.id());
            if (around.size() != 1) continue;
            int b = around.iterator().next();
            if (regions.get(b).type() == e.type() || regions.get(b).substrate()) continue;   // ベースは拡散した領域（板そのものではない）
            for (int c : neighbours(b)) {
                if (c == e.id() || regions.get(c).type() != e.type()) continue;
                int en = firstNet(e.id()), bn = firstNet(b), cn = firstNet(c);
                if (en < 0 || bn < 0 || cn < 0 || en == bn || bn == cn || en == cn) continue;
                transistors.add(new Transistor(e.type() == 'n', e.id(), b, c, en, bn, cn));
                inTransistor.add(e.id()); inTransistor.add(b);
                break;
            }
        }
        // 抵抗：1つの領域に別々の線が2本以上（トランジスタのエミッタ・ベースは除く）
        for (Region r : regions) {
            Set<Integer> s = netsOfRegion.get(r.id());
            if (s == null || s.size() < 2 || inTransistor.contains(r.id()) || r.substrate()) continue;
            List<Integer> l = new ArrayList<>(s);
            resistors.add(new Resistor(r.id(), l.get(0), l.get(1), ohmsBetween(r, l.get(0), l.get(1))));
        }
        // ダイオード：p と n の領域が接し、別々の線につながる（トランジスタの中の接合は除く）
        for (Region p : regions) {
            if (p.type() != 'p') continue;
            for (int nIdx : neighbours(p.id())) {
                Region nr = regions.get(nIdx);
                if (nr.type() != 'n') continue;
                if (isTransistorJunction(p.id(), nIdx)) continue;
                int an = firstNet(p.id()), cn = firstNet(nIdx);
                if (an >= 0 && cn >= 0 && an != cn) diodes.add(new Diode(p.id(), nIdx, an, cn));
            }
        }
    }

    private boolean isTransistorJunction(int a, int b) {
        for (Transistor t : transistors) {
            if ((a == t.emitter() || a == t.base()) && (b == t.emitter() || b == t.base())) return true;
            if ((a == t.base() || a == t.collector()) && (b == t.base() || b == t.collector())) return true;
        }
        return false;
    }

    /** 領域の中で、線 a の窓から線 b の窓までのいちばん短い道のマス数 × 面抵抗（太さは1マスとみなす。めやす） */
    private double ohmsBetween(Region r, int a, int b) {
        int n = model.size;
        int[] dist = new int[n * n];
        java.util.Arrays.fill(dist, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>();
        for (int[] c : r.cells()) if (model.connected(c[0], c[1]) && netOf[c[1] * n + c[0]] == a) { dist[c[1] * n + c[0]] = 0; q.add(c); }
        double sum = 0; int steps = 0;
        while (!q.isEmpty()) {
            int[] c = q.poll();
            if (model.connected(c[0], c[1]) && netOf[c[1] * n + c[0]] == b) { steps = dist[c[1] * n + c[0]]; break; }
            for (int[] d : DIRS) {
                int x = c[0] + d[0], y = c[1] + d[1];
                if (model.inside(x, y) && dist[y * n + x] < 0 && regionOf[y * n + x] == r.id()) { dist[y * n + x] = dist[c[1] * n + c[0]] + 1; q.add(new int[] {x, y}); }
            }
        }
        int[] c0 = r.cells().get(0);
        return Math.max(1, steps) * model.sheetOhms(c0[0], c0[1]);
    }

    /** パッドの線の一覧 */
    public List<Net> pads() { return nets.stream().filter(Net::isPad).toList(); }
}
