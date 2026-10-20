package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 発見の一覧（LITHO_DESIGN §6.3）。検査台でチップを調べ、歴史上の発明と同じつながりなら「見つけた」とする。
 * 正解は教えない：名前が出るのは見つけたあとだけ。v0.3 は4つ（抵抗・ダイオード・トランジスタ・NOT）。
 */
public final class Discoveries {
    private Discoveries() {}

    /** 発見1つ：id（lang・図鑑・進捗の名前）・年・ノード・判定 */
    public record Discovery(String id, int year, String node, Predicate<Netlist> test) {
        /** lang：litho.semiconductor-lab.discovery.&lt;id&gt;（名前）・.found（「あなたは…を自分で作った」） */
        public String langKey() { return "litho.semiconductor-lab.discovery." + id; }
    }

    private static final List<Discovery> ALL = new ArrayList<>();

    static {
        add("diffused_resistor", 1958, "9-22", nl -> !nl.resistors.isEmpty());
        add("junction_diode", 1940, "9-05", nl -> !nl.diodes.isEmpty() || !nl.transistors.isEmpty());
        add("planar_transistor", 1959, "9-18", nl -> !nl.transistors.isEmpty());
        add("rtl_not", 1961, "10-09", Circuit::worksAsNot);
    }

    private static void add(String id, int year, String node, Predicate<Netlist> test) { ALL.add(new Discovery(id, year, node, test)); }

    public static List<Discovery> all() { return List.copyOf(ALL); }

    public static Discovery byId(String id) { return ALL.stream().filter(d -> d.id().equals(id)).findFirst().orElse(null); }

    /** このチップで見つかるもの（一覧の順） */
    public static List<Discovery> match(Netlist nl) {
        List<Discovery> out = new ArrayList<>();
        for (Discovery d : ALL) if (d.test().test(nl)) out.add(d);
        return out;
    }
}
