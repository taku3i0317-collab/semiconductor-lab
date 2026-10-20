package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.List;

/**
 * 発見の一覧（LITHO_DESIGN §6.3）。検査台でチップを調べ、歴史上の発明と同じつながりなら「見つけた」とする。
 * 正解は教えない：名前が出るのは見つけたあとだけ。v0.3 は5つ（抵抗・ダイオード・トランジスタ・IC・NOT）。
 */
public final class Discoveries {
    private Discoveries() {}

    /** 発見1つ：id（lang・図鑑・進捗の名前）・年・ノード・判定 */
    public record Discovery(String id, int year, String node, java.util.function.BiPredicate<Netlist, Double> test) {
        /** lang：litho.semiconductor-lab.discovery.&lt;id&gt;（名前）・.found（「あなたは…を自分で作った」） */
        public String langKey() { return "litho.semiconductor-lab.discovery." + id; }
    }

    private static final List<Discovery> ALL = new ArrayList<>();

    static {
        add("diffused_resistor", 1958, "9-22", (nl, k) -> !nl.resistors.isEmpty());
        add("junction_diode", 1940, "9-05", (nl, k) -> !nl.diodes.isEmpty() || !nl.transistors.isEmpty());
        add("planar_transistor", 1959, "9-18", (nl, k) -> !nl.transistors.isEmpty());
        add("monolithic_ic", 1959, "9-22", (nl, k) -> integrated(nl));   // ノイスのプレーナ IC（O-010）
        add("rtl_not", 1961, "10-09", Circuit::worksAsNot);   // 汚れた（漏れる）チップは動かないことがある
    }

    /**
     * 集積回路（[9-22]、ノイス 1959）：1枚の板の中で、トランジスタと「別の領域の」抵抗が、板の上の金属の線でつながっている。
     * 別の領域＝接合で分けられた別の部品（ルホヴェックの接合分離）。金属の線＝酸化膜の上に蒸着した Al（ノイス）。
     * キルビーの IC（1958、ゲルマニウム、部品どうしを金の細い線で空中につなぐ）はこの形では作れない（TECH_TREE [9-22]）
     */
    static boolean integrated(Netlist nl) {
        for (Netlist.Transistor t : nl.transistors)
            for (Netlist.Resistor r : nl.resistors) {
                if (r.region() == t.emitter() || r.region() == t.base() || r.region() == t.collector()) continue;
                for (int n : new int[] {t.eNet(), t.bNet(), t.cNet()})
                    if (n == r.netA() || n == r.netB()) return true;
            }
        return false;
    }

    private static void add(String id, int year, String node, java.util.function.BiPredicate<Netlist, Double> test) { ALL.add(new Discovery(id, year, node, test)); }

    public static List<Discovery> all() { return List.copyOf(ALL); }

    public static Discovery byId(String id) { return ALL.stream().filter(d -> d.id().equals(id)).findFirst().orElse(null); }

    /** このチップで見つかるもの（一覧の順） */
    public static List<Discovery> match(Netlist nl) { return match(nl, 1.0); }

    /** leakScale：汚れの漏れの強さの倍率（検査台は「調べる」たびに 0.5〜1.5 で変える） */
    public static List<Discovery> match(Netlist nl, double leakScale) {
        List<Discovery> out = new ArrayList<>();
        for (Discovery d : ALL) if (d.test().test(nl, leakScale)) out.add(d);
        return out;
    }
}
