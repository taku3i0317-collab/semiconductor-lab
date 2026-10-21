package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 論理の箱で速く計算する（[10-13]、S-014 Q25-4 案A）。お題に合格したハンコは一度アナログ（{@link Circuit}）で確かめてあるので、
 * 「その動き（真理値表・手順）をする箱」として扱い、ハンコの間の線だけをマスクの形（{@link Netlist} の線）から読む。
 * <p>使えるのは、チップの回路がみなハンコで説明できるときだけ：①どのハンコにも名前（お題の id）とピン（合格したときのパッドの役目と場所）がある
 * ②ハンコを押した四角の中が、ハンコと同じ形（描き変えていない）③ハンコの外には拡散がない（線＝金属だけ）。そうでなければ null（今までどおりアナログ）。
 * ⚠️ 箱の中の電気の強さ（つなぎすぎ・電圧の落ち）と、DRAM の電荷の漏れは見ない
 */
public final class LogicSim {
    /** 箱：押したハンコ・お題・ピンの役目 → 線の番号 */
    record Box(Part part, Challenge.Def def, Map<Circuit.Role, Integer> pin) {}

    final Netlist nl;
    final List<Box> boxes;
    /** 箱ごとの記憶（出力のビット）・前のクロック・カウンタの数 */
    private final int[] state;
    private final boolean[] lastClk;

    private LogicSim(Netlist nl, List<Box> boxes) {
        this.nl = nl;
        this.boxes = boxes;
        this.state = new int[boxes.size()];
        this.lastClk = new boolean[boxes.size()];
    }

    /** 線の番号で直接つないだ箱（テスト・見本用。形は見ない）：お題の id と、ピンの役目 → 線の番号 */
    public record Wire(String id, Map<Circuit.Role, Integer> pins) {}

    public static LogicSim wired(List<Wire> wires) {
        List<Box> boxes = new ArrayList<>();
        for (Wire w : wires) {
            Challenge.Def def = Challenge.byId(w.id());
            if (def == null || !Behaviour.known(def)) throw new IllegalArgumentException("not a logic box: " + w.id());
            boxes.add(new Box(null, def, new EnumMap<>(w.pins())));
        }
        return new LogicSim(null, boxes);
    }

    /** チップ（parts＝押したハンコ）が論理の箱だけでできていれば、その計算機。でなければ null */
    public static LogicSim of(Netlist nl, List<Part> parts) {
        if (nl == null || parts == null || parts.isEmpty()) return null;
        ChipModel m = nl.model;
        int n = m.size;
        boolean[] covered = new boolean[n * n];
        List<Box> boxes = new ArrayList<>();
        for (Part p : parts) {
            ChipDesign st = p.stamp();
            Challenge.Def def = Challenge.byId(st.name());
            if (def == null || st.pins().isEmpty() || !Behaviour.known(def)) return null;
            ChipModel sm = ChipModel.of(st, String.valueOf(m.substrate));
            for (int y = 0; y < st.size(); y++) for (int x = 0; x < st.size(); x++) {
                int cx = p.x() + x, cy = p.y() + y;
                if (!m.inside(cx, cy)) continue;
                if (m.type(cx, cy) != sm.type(x, y) || m.contact(cx, cy) != sm.contact(x, y) || m.metal(cx, cy) != sm.metal(x, y) || m.gate(cx, cy) != sm.gate(x, y)) return null;   // 描き変えてある
                covered[cy * n + cx] = true;
            }
            Map<Circuit.Role, Integer> pin = new EnumMap<>(Circuit.Role.class);
            for (ChipDesign.Pin pn : st.pins()) {
                Circuit.Role r = Circuit.Role.valueOf(pn.role());
                int cx = p.x() + pn.x(), cy = p.y() + pn.y();
                pin.put(r, m.inside(cx, cy) ? nl.netOf[cy * n + cx] : -1);
            }
            boxes.add(new Box(p, def, pin));
        }
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++)
            if (m.type(x, y) != m.substrate && !covered[y * n + x]) return null;   // ハンコの外にトランジスタや抵抗がある
        return new LogicSim(nl, boxes);
    }

    /**
     * 1手：チップのパッドの役目（線 → 役目）と入力（役目 → 1/0）をかけて、出力1〜outputs のビットを返す（役目が足りなければ −1）。
     * 箱の記憶は手から手へ残る。クロックの上がり・下がりで動く箱は、落ち着いたあとに1回だけ動く
     */
    public int step(Map<Integer, Circuit.Role> roles, Map<Circuit.Role, Boolean> inputs, int outputs) {
        Map<Integer, Boolean> fixed = new HashMap<>();
        int[] outNet = {-1, -1, -1, -1, -1, -1, -1, -1};
        boolean gnd = false, vcc = false;
        for (var e : roles.entrySet()) {
            switch (e.getValue()) {
                case GROUND -> { fixed.put(e.getKey(), false); gnd = true; }
                case VCC -> { fixed.put(e.getKey(), true); vcc = true; }
                case OUT_1 -> outNet[0] = e.getKey();
                case OUT_2 -> outNet[1] = e.getKey();
                case OUT_3 -> outNet[2] = e.getKey();
                case OUT_4 -> outNet[3] = e.getKey();
                case OUT_5 -> outNet[4] = e.getKey();
                case OUT_6 -> outNet[5] = e.getKey();
                case OUT_7 -> outNet[6] = e.getKey();
                case OUT_8 -> outNet[7] = e.getKey();
                default -> fixed.put(e.getKey(), inputs.getOrDefault(e.getValue(), false));
            }
        }
        if (!gnd || !vcc) return -1;
        for (Circuit.Role r : inputs.keySet()) if (!roles.containsValue(r)) return -1;
        for (int i = 0; i < outputs; i++) if (outNet[i] < 0) return -1;
        Map<Integer, Boolean> driven = new HashMap<>();
        settle(roles, fixed, driven);
        for (int b = 0; b < boxes.size(); b++) {   // クロックで動く箱：落ち着いた入力で1回
            Box box = boxes.get(b);
            if (!Behaviour.edge(box.def())) continue;
            boolean clk = value(box, Circuit.Role.CLK, fixed, driven);
            state[b] = Behaviour.onEdge(box.def(), state[b], lastClk[b], clk, ins(box, fixed, driven));
            lastClk[b] = clk;
        }
        settle(roles, fixed, driven);
        int bits = 0;
        for (int i = 0; i < outputs; i++) if (Boolean.TRUE.equals(fixed.getOrDefault(outNet[i], driven.get(outNet[i])))) bits |= 1 << i;
        return bits;
    }

    /** 組み合わせの箱と、レベルで動く記憶（ラッチ）を、変わらなくなるまで回す */
    private void settle(Map<Integer, Circuit.Role> roles, Map<Integer, Boolean> fixed, Map<Integer, Boolean> driven) {
        for (int it = 0; it < 4 * boxes.size() + 8; it++) {
            boolean changed = false;
            for (int b = 0; b < boxes.size(); b++) {
                Box box = boxes.get(b);
                boolean powered = isRole(box.pin().get(Circuit.Role.GROUND), Circuit.Role.GROUND, roles) && isRole(box.pin().get(Circuit.Role.VCC), Circuit.Role.VCC, roles);
                int out;
                if (!powered) out = 0;
                else if (Behaviour.edge(box.def())) out = state[b];
                else { state[b] = Behaviour.level(box.def(), state[b], ins(box, fixed, driven)); out = state[b]; }
                for (int k = 0; k < box.def().outputs(); k++) {
                    Integer net = box.pin().get(Circuit.OUTS[k]);
                    if (net == null || net < 0 || fixed.containsKey(net)) continue;
                    boolean v = (out >> k & 1) != 0;
                    if (!Boolean.valueOf(v).equals(driven.put(net, v))) changed = true;
                }
            }
            if (!changed) break;
        }
    }

    private static boolean isRole(Integer net, Circuit.Role want, Map<Integer, Circuit.Role> roles) { return net != null && net >= 0 && roles.get(net) == want; }

    private static boolean value(Box box, Circuit.Role r, Map<Integer, Boolean> fixed, Map<Integer, Boolean> driven) {
        Integer net = box.pin().get(r);
        if (net == null || net < 0) return false;
        Boolean v = fixed.get(net);
        return v != null ? v : Boolean.TRUE.equals(driven.get(net));
    }

    /** 箱の入力の行番号（お題と同じ並び：A が上の位） */
    private static int ins(Box box, Map<Integer, Boolean> fixed, Map<Integer, Boolean> driven) {
        Challenge.Def d = box.def();
        int row = 0;
        for (int i = 0; i < d.inputs(); i++) row = row << 1 | (value(box, d.inputRole(i), fixed, driven) ? 1 : 0);
        return row;
    }

    /** お題ごとの箱の動き（ハンコの名前＝お題の id）。row＝入力の行番号（A が上の位、クロックつきなら最後がクロック） */
    static final class Behaviour {
        static boolean known(Challenge.Def d) {
            return !d.sequential() || switch (d.id()) { case "sr_latch", "d_latch", "counter", "register", "dram_cell" -> true; default -> false; };
        }
        /** クロックの上がり・下がりで動くか */
        static boolean edge(Challenge.Def d) { return d.id().equals("counter") || d.id().equals("register"); }
        /** 組み合わせ・ラッチ：今の入力（と記憶）から出力 */
        static int level(Challenge.Def d, int q, int row) {
            if (!d.sequential()) return d.table()[row];
            return switch (d.id()) {
                case "sr_latch" -> { boolean s = (row & 2) != 0, r = (row & 1) != 0; yield s && !r ? 1 : r && !s ? 0 : q; }
                case "d_latch" -> (row & 1) != 0 ? row >> 1 & 1 : q;
                case "dram_cell" -> {   // W×4＋D×2＋R。記憶は bit1（⚠️漏れは見ない）、出力1＝読むと逆の値（読まないときは引き上げで1）
                    int mem = (row & 4) != 0 ? (row >> 1 & 1) : (q >> 1 & 1);
                    boolean read = (row & 1) != 0;
                    yield mem << 1 | (read ? 1 - mem : 1);
                }
                default -> q;
            };
        }
        /** クロックで動く箱：前のクロック → 今のクロックで記憶を変える */
        static int onEdge(Challenge.Def d, int q, boolean before, boolean now, int row) {
            return switch (d.id()) {
                case "counter" -> (row & 2) != 0 ? 0 : before && !now ? (q + 1) & 3 : q;   // R×2＋CLK。下がるときに数える、R で 0
                case "register" -> !before && now ? ((row >> 2 & 1) | (row >> 1 & 1) << 1) : q;   // D0×4＋D1×2＋CLK。上がるときに覚える
                default -> q;
            };
        }
    }
}
