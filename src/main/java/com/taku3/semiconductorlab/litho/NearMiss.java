package com.taku3.semiconductorlab.litho;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 「あと少し」の手がかり（v0.3b、S-005、LITHO_DESIGN §11.2）：お題に合わなかったとき、どこまで近いかを段で返す。正解の形は言わない。
 * 段は上から順に調べ、最初に当てはまった物を返す（lang：litho.semiconductor-lab.near.&lt;key&gt;）。
 */
public final class NearMiss {
    private NearMiss() {}

    /** 電流が流れていると見なす電源の電流（A） */
    static final double FLOWING = 1e-6;

    public static String diagnose(Challenge.Def d, Netlist nl, Map<Integer, Circuit.Role> roles, Challenge.Result r, double leakScale) {
        if (r.passed()) return "solved";
        if (r.missingRoles()) return "roles";
        // ① 電源から電流が流れない（電源と 0 V の間に部品がない・つながっていない）
        int vcc = -1;
        for (var e : roles.entrySet()) if (e.getValue() == Circuit.Role.VCC) vcc = e.getKey();
        boolean flows = false;
        for (int row = 0; row < d.rows() && !flows; row++) {
            Map<Integer, Double> drive = new HashMap<>();
            int in = Challenge.bits(d, row);
            boolean a = (in & 4) != 0, b = (in & 2) != 0, c = (in & 1) != 0;
            for (var e : roles.entrySet()) switch (e.getValue()) {
                case GROUND -> drive.put(e.getKey(), 0.0);
                case VCC -> drive.put(e.getKey(), Circuit.VCC);
                case IN_A -> drive.put(e.getKey(), a ? Circuit.VCC : 0.0);
                case IN_B -> drive.put(e.getKey(), b ? Circuit.VCC : 0.0);
                case IN_C -> drive.put(e.getKey(), c ? Circuit.VCC : 0.0);
                default -> {}
            }
            Double amps = Circuit.solve(nl, drive, leakScale).padAmps().get(vcc);
            flows = amps != null && Math.abs(amps) > FLOWING;
        }
        if (!flows) return "no_current";
        // ② トランジスタがない。入れ子の形はあるが配線のない領域がある →「あと1か所つながればトランジスタ」
        if (nl.transistors.isEmpty()) return unwiredRegion(nl) ? "almost_transistor" : "no_switch";
        // ③ 入力のパッドが、どのトランジスタのベースにも（直接か抵抗1本で）つながっていない
        Set<Integer> inputs = new HashSet<>();
        for (var e : roles.entrySet()) if (e.getValue() == Circuit.Role.IN_A || e.getValue() == Circuit.Role.IN_B || e.getValue() == Circuit.Role.IN_C) inputs.add(e.getKey());
        for (int in : inputs) if (!reachesBase(nl, in)) return "input_not_base";
        // ④ 出力が入力で変わらない
        Set<Integer> outs = new HashSet<>();
        for (int row = 0; row < d.rows(); row++) {
            outs.add(Circuit.logic(nl, roles, Challenge.bits(d, row), d.inputs(), d.outputs(), leakScale));
        }
        if (outs.size() <= 1) return "output_stuck";
        // ⑤ いくつかの行は合っている
        return "some_rows";
    }

    /** 板でない領域のうち、どの線にもつながっていない物があるか（入れ子の形の一つに窓・金属がない） */
    static boolean unwiredRegion(Netlist nl) {
        Set<Integer> wired = new HashSet<>();
        for (Netlist.Net n : nl.nets) wired.addAll(n.regions());
        for (Netlist.Region r : nl.regions) if (!r.substrate() && !wired.contains(r.id())) return true;
        return false;
    }

    /** 線 net が、トランジスタのベースの線そのものか、抵抗1本の先がベースの線か */
    static boolean reachesBase(Netlist nl, int net) {
        Set<Integer> bases = new HashSet<>();
        for (Netlist.Transistor t : nl.transistors) bases.add(t.bNet());
        if (bases.contains(net)) return true;
        for (Netlist.Resistor r : nl.resistors) {
            if (r.netA() == net && bases.contains(r.netB())) return true;
            if (r.netB() == net && bases.contains(r.netA())) return true;
        }
        return false;
    }
}
