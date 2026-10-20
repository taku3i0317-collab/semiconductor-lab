package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.List;

/**
 * 発見の一覧（LITHO_DESIGN §6.3）。検査台でチップを調べ、歴史上の発明と同じつながりなら「見つけた」とする。
 * 正解は教えない：名前が出るのは見つけたあとだけ。v0.3 は4つ（抵抗・ダイオード・トランジスタ・NOT）。
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
        add("rtl_not", 1961, "10-09", Circuit::worksAsNot);   // 汚れた（漏れる）チップは動かないことがある
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
