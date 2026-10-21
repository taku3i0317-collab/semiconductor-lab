package com.taku3.semiconductorlab.litho;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * チップの直流の回路を解く（LITHO_DESIGN §6.2 の 6・7）。検査台の電流の表示と、発見の確かめ（NOT など）に使う。
 * <pre>
 * 1マス＝1つの点。同じ領域のとなりのマスは面抵抗でつながる。窓（金属がさわる所）は線とつながる。
 * 型のちがうとなりのマスは pn 接合（ショックレーの式 I = Is(e^(V/Vt) − 1)）。
 * トランジスタ（入れ子）は、ベース・エミッタの接合を流れる電流の α 倍をコレクタが受け持つ（エバース・モルの順方向だけ）。
 * </pre>
 * ニュートン法で解く。点が {@link #DENSE_MAX} 個までは密な行列（ガウスの消去法、v0.3 の 8×8）、それより多いと疎な行列と BiCGSTAB（O-006）。
 */
public final class Circuit {
    private Circuit() {}

    /** 熱電圧（25℃）・1つの接合（1マスの辺）の飽和電流・電流増幅率 β=100 の α・窓の抵抗・どこにもつながらない点の逃げ道 */
    static final double VT = 0.02585, IS = 1e-14, ALPHA = 0.99, CONTACT_OHMS = 20, GMIN = 1e-9;
    /** これより大きい順方向の電圧は直線でのばす（指数が大きくなりすぎないように） */
    static final double VMAX = 0.85;
    /**
     * 汚れ1つあたり、pn 接合の辺1つに並ぶ漏れのコンダクタンス（S、ゲームの値 ⚠️、S-003）。
     * 金属の汚れ（Na・Fe など）は接合のまわりに漏れ道を作る。8×8 の NOT の例は工程の汚れ約14で出力の高い方が約3.7 Vに下がり、
     * 汚れが200ほどになると動かない
     */
    public static final double LEAK_PER_DIRT = 3e-7;

    /** 解いた結果：点ごとの電圧、パッドから流れこむ電流（A）、マスごとの電流の大きさ（表示用） */
    public record Solution(double[] volts, Map<Integer, Double> padAmps, double[] cellAmps, boolean converged) {
        public double netVolts(Netlist nl, int net) { return volts[nl.model.size * nl.model.size + net]; }
    }

    private record Res(int a, int b, double g) {}
    private record Dio(int anode, int cathode) {}
    /** α × (接合 anode→cathode の電流) を from から to へ流す */
    private record Src(int from, int to, double k, int anode, int cathode) {}

    private static double id(double v) {
        if (v > VMAX) { double e = Math.exp(VMAX / VT); return IS * (e - 1) + IS / VT * e * (v - VMAX); }
        return IS * (Math.exp(v / VT) - 1);
    }
    private static double gd(double v) { return IS / VT * Math.exp(Math.min(v, VMAX) / VT); }

    /** drive：パッドの線の番号 → 電圧（つながないパッドは入れない） */
    public static Solution solve(Netlist nl, Map<Integer, Double> drive) { return solve(nl, drive, 1.0); }

    /** leakScale：漏れの強さの倍率（検査台で「調べる」たびに少し変わる＝汚れたチップは動いたり動かなかったりする） */
    public static Solution solve(Netlist nl, Map<Integer, Double> drive, double leakScale) { return solve(nl, drive, leakScale, null); }

    /** start＝計算を始める電圧（前の手の答え。順序回路の記憶、S-009）。長さがちがえば無視 */
    public static Solution solve(Netlist nl, Map<Integer, Double> drive, double leakScale, double[] start) { return solve(nl, drive, leakScale, start, 0); }

    /**
     * MOS のゲートと MOS のコンデンサの1マスの容量（F、ゲームの値 ⚠️：本物の 1103 のセルは 0.1 pF より小さい）。
     * ためた電荷は、どの点にもある逃げ道 GMIN と接合の漏れで少しずつ減る（数 ms で忘れる＝リフレッシュが要る、[10-12]）
     */
    public static final double CAP_PER_CELL = 3e-11;

    /**
     * dt＞0：前の手の電圧 start から dt 秒たった後を解く（[10-12]、S-012 Q22-2 案A）。コンデンサ（MOS のゲート・MOS のコンデンサ）は
     * 後退オイラー法（電流 C·(V−V前)/dt）。dt＝0 は直流（コンデンサは開いたまま、今までと同じ）
     */
    public static Solution solve(Netlist nl, Map<Integer, Double> drive, double leakScale, double[] start, double dt) {
        ChipModel m = nl.model;
        double leak = LEAK_PER_DIRT * m.contamination * leakScale;
        int n = m.size, cells = n * n, nodes = cells + nl.nets.size();
        List<Res> res = new ArrayList<>();
        List<Dio> dio = new ArrayList<>();
        List<Src> src = new ArrayList<>();
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            int i = y * n + x;
            if (m.connected(x, y) && nl.netOf[i] >= 0) res.add(new Res(i, cells + nl.netOf[i], 1 / CONTACT_OHMS));
            for (int[] d : new int[][] {{1, 0}, {0, 1}}) {
                int a = x + d[0], b = y + d[1];
                if (!m.inside(a, b)) continue;
                int j = b * n + a;
                if (m.type(x, y) == m.type(a, b)) res.add(new Res(i, j, 2 / (m.sheetOhms(x, y) + m.sheetOhms(a, b))));
                else {
                    if (m.type(x, y) == 'p') dio.add(new Dio(i, j)); else dio.add(new Dio(j, i));
                    if (leak > 0) res.add(new Res(i, j, leak));   // 汚れの漏れ道
                }
            }
        }
        for (Netlist.Transistor t : nl.transistors) {
            List<int[]> be = edges(nl, t.emitter(), t.base()), bc = edges(nl, t.base(), t.collector());
            if (bc.isEmpty()) continue;
            for (int[] e : be) {   // e = {エミッタ側のマス, ベース側のマス}
                int anode = t.npn() ? e[1] : e[0], cathode = t.npn() ? e[0] : e[1];
                for (int[] c : bc) {   // c = {ベース側のマス, コレクタ側のマス}
                    if (t.npn()) src.add(new Src(c[1], e[1], ALPHA / bc.size(), anode, cathode));   // コレクタ → ベースの点へ（ベースの電流が減る）
                    else src.add(new Src(e[1], c[1], ALPHA / bc.size(), anode, cathode));
                }
            }
        }
        // MOS（S-011 Q21-4 案A、二乗の式）：ゲートの線の電圧で、ソースとドレインの代表のマスの間に電流
        List<int[]> mos = new ArrayList<>();
        List<Double> mosK = new ArrayList<>();
        for (Netlist.Mos t : nl.mosfets) { mos.add(new int[] {cells + t.gateNet(), t.cellA(), t.cellB(), t.p() ? 1 : 0}); mosK.add((t.p() ? MOS_KP_P : MOS_KP) * t.wOverL()); }
        if (n >= SHORT_CHANNEL_GRID) for (Netlist.Mos t : nl.mosfets)   // 細い平らな MOS の漏れ（[9-37]）
            if (!t.channel().stream().allMatch(c -> m.fin(c[0], c[1]))) res.add(new Res(t.cellA(), t.cellB(), (t.p() ? MOS_KP_P : MOS_KP) * t.wOverL() * SHORT_CHANNEL_VOLTS));
        // コンデンサ {a, b}・容量（時間を進めるときだけ）：MOS のゲート（ゲートの線 ↔ チャネルの下のマス）と MOS のコンデンサ
        List<int[]> cap = new ArrayList<>();
        List<Double> capC = new ArrayList<>();
        boolean timed = dt > 0 && start != null && start.length == nodes;
        if (timed) {
            for (Netlist.Mos t : nl.mosfets) { int[] c0 = t.channel().get(0); cap.add(new int[] {cells + t.gateNet(), c0[1] * n + c0[0]}); capC.add(CAP_PER_CELL * t.channel().size()); }
            for (Netlist.Cap c : nl.caps) { cap.add(new int[] {cells + c.net(), c.cell()}); capC.add(CAP_PER_CELL * c.area()); }
        }
        // 固定の点（パッド）と、解く点
        double[] v = start != null && start.length == nodes ? start.clone() : new double[nodes];
        double[] before = timed ? start.clone() : null;
        boolean[] fixed = new boolean[nodes];
        for (var e : drive.entrySet()) { int k = cells + e.getKey(); fixed[k] = true; v[k] = e.getValue(); }
        int[] idx = new int[nodes];
        int u = 0;
        for (int k = 0; k < nodes; k++) idx[k] = fixed[k] ? -1 : u++;
        boolean ok = false;
        // 収束しなければ、電源を少しずつ上げて解き直す（電源の階段、SPICE と同じ工夫。大きな MOS の回路＝[10-13] の ROM など）
        for (int pass = 0; pass < 2 && !ok && u > 0; pass++) {
        double[] scales = pass == 0 ? new double[] {1.0} : new double[] {0.1, 0.25, 0.4, 0.55, 0.7, 0.85, 1.0};
        if (pass == 1) for (int k = 0; k < nodes; k++) if (!fixed[k]) v[k] = 0;
        for (double sc : scales) {
        for (var e : drive.entrySet()) v[cells + e.getKey()] = e.getValue() * sc;
        ok = false;
        for (int it = 0; it < 200 && u > 0; it++) {
            Mat J = u <= DENSE_MAX ? new Dense(u) : new Sparse(u);
            double[] F = new double[u];
            for (int k = 0; k < nodes; k++) if (idx[k] >= 0) { F[idx[k]] += GMIN * v[k]; J.add(idx[k], idx[k], GMIN); }
            for (Res r : res) stampG(J, F, idx, v, r.a, r.b, r.g);
            for (Dio d : dio) {
                double vd = v[d.anode] - v[d.cathode], i = id(vd), g = gd(vd);
                addI(F, idx, d.anode, i); addI(F, idx, d.cathode, -i);
                addJ(J, idx, d.anode, d.anode, g); addJ(J, idx, d.anode, d.cathode, -g);
                addJ(J, idx, d.cathode, d.anode, -g); addJ(J, idx, d.cathode, d.cathode, g);
            }
            for (int q = 0; q < mos.size(); q++) {   // MOS：ドレイン（電圧の高い方）→ ソース
                int g = mos.get(q)[0], a = mos.get(q)[1], b = mos.get(q)[2];
                if (mos.get(q)[3] == 1) {   // p チャネル（[9-24]）：ソース（電圧の高い方）→ ドレイン。Vsg・Vsd で同じ二乗の式
                    boolean fl = v[b] > v[a];
                    int sp = fl ? b : a, dp = fl ? a : b;
                    double[] ip = mosCurrent(mosK.get(q), v[sp] - v[g], v[sp] - v[dp]);
                    double i = ip[0], gm = ip[1], gds = ip[2];
                    addI(F, idx, sp, i); addI(F, idx, dp, -i);
                    addJ(J, idx, sp, sp, gm + gds); addJ(J, idx, sp, g, -gm); addJ(J, idx, sp, dp, -gds);
                    addJ(J, idx, dp, sp, -gm - gds); addJ(J, idx, dp, g, gm); addJ(J, idx, dp, dp, gds);
                    continue;
                }
                boolean flip = v[b] > v[a];
                int dn = flip ? b : a, sn = flip ? a : b;
                double[] ig = mosCurrent(mosK.get(q), v[g] - v[sn], v[dn] - v[sn]);   // {I, ∂I/∂Vgs, ∂I/∂Vds}
                double i = ig[0], gm = ig[1], gds = ig[2];
                addI(F, idx, dn, i); addI(F, idx, sn, -i);
                addJ(J, idx, dn, g, gm); addJ(J, idx, dn, dn, gds); addJ(J, idx, dn, sn, -gm - gds);
                addJ(J, idx, sn, g, -gm); addJ(J, idx, sn, dn, -gds); addJ(J, idx, sn, sn, gm + gds);
            }
            for (int q = 0; q < cap.size(); q++) {   // コンデンサ：i = C/dt·((Va−Vb) − (Va−Vb)前)
                int a = cap.get(q)[0], b = cap.get(q)[1];
                double g = capC.get(q) / dt, i = g * ((v[a] - v[b]) - (before[a] - before[b]));
                addI(F, idx, a, i); addI(F, idx, b, -i);
                addJ(J, idx, a, a, g); addJ(J, idx, a, b, -g); addJ(J, idx, b, a, -g); addJ(J, idx, b, b, g);
            }
            for (Src s : src) {
                double vd = v[s.anode] - v[s.cathode], i = s.k * id(vd), g = s.k * gd(vd);
                addI(F, idx, s.from, i); addI(F, idx, s.to, -i);
                addJ(J, idx, s.from, s.anode, g); addJ(J, idx, s.from, s.cathode, -g);
                addJ(J, idx, s.to, s.anode, -g); addJ(J, idx, s.to, s.cathode, g);
            }
            double[] dv = J.solve(F);
            double max = 0;
            for (int k = 0; k < nodes; k++) if (idx[k] >= 0) {
                double step = Math.max(-0.3, Math.min(0.3, -dv[idx[k]]));
                v[k] += step;
                max = Math.max(max, Math.abs(step));
            }
            if (max < 1e-7) { ok = true; break; }
        }
        if (!ok && pass == 1) break;
        }
        }
        if (u == 0) ok = true;
        // パッドの電流・マスの電流
        Map<Integer, Double> pad = new HashMap<>();
        double[] cellAmps = new double[cells];
        for (Res r : res) {
            double i = r.g * (v[r.a] - v[r.b]);
            if (r.b >= cells) pad.merge(r.b - cells, -i, Double::sum);   // 線 → マスへ流れる向きを＋
            if (r.a < cells) cellAmps[r.a] += Math.abs(i) / 2;
            if (r.b < cells) cellAmps[r.b] += Math.abs(i) / 2;
        }
        for (Dio d : dio) { double i = Math.abs(id(v[d.anode] - v[d.cathode])); cellAmps[d.anode] += i / 2; cellAmps[d.cathode] += i / 2; }
        Map<Integer, Double> padAmps = new HashMap<>();
        for (int net : drive.keySet()) padAmps.put(net, pad.getOrDefault(net, 0.0));
        return new Solution(v, padAmps, cellAmps, ok);
    }

    /** MOS のしきい値（V）・利得の係数（A/V²、W/L＝1 のとき）。⚠️ ゲームの値（1960年代の MOS は Vt 数 V） */
    static final double MOS_VT = 1.0, MOS_KP = 1e-4;
    /** p チャネルの係数：正孔は電子より動きにくい（移動度 約 1/2.5）ので同じ形でも電流が小さい ⚠️ */
    static final double MOS_KP_P = MOS_KP / 2.5;
    /**
     * 短チャネルの漏れ（[9-37]、E と決めた形）：方眼がこれ以上（CD 約 40 nm 以下）の平らな MOS は、ゲートを切ってもソースとドレインの間が半分開いている
     * （ドレインの電界がチャネルまで届く）。漏れ＝係数×W/L×{@link #SHORT_CHANNEL_VOLTS}（ゲームの値 ⚠️）。チャネルのマスがみなフィンの上（FinFET）なら漏れない
     */
    public static final int SHORT_CHANNEL_GRID = ExposureTool.gridFor(40);
    static final double SHORT_CHANNEL_VOLTS = 3.0;

    /** n チャネル MOS の二乗の式：{I, ∂I/∂Vgs, ∂I/∂Vds}。しきい値の下は 0（計算のためにごく小さな電流） */
    static double[] mosCurrent(double k, double vgs, double vds) {
        double ov = vgs - MOS_VT;
        if (ov <= 0) return new double[] {1e-12 * vds, 0, 1e-12};
        if (vds < ov) return new double[] {k * (ov * vds - vds * vds / 2), k * vds, k * (ov - vds)};   // 線形の領域
        return new double[] {k / 2 * ov * ov, k * ov, 1e-12};                                          // 飽和
    }

    /** 領域 a と領域 b が接している辺（{a 側のマス, b 側のマス}） */
    private static List<int[]> edges(Netlist nl, int a, int b) {
        int n = nl.model.size;
        List<int[]> out = new ArrayList<>();
        for (int[] c : nl.regions.get(a).cells())
            for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int x = c[0] + d[0], y = c[1] + d[1];
                if (nl.model.inside(x, y) && nl.regionOf[y * n + x] == b) out.add(new int[] {c[1] * n + c[0], y * n + x});
            }
        return out;
    }

    private static void stampG(Mat J, double[] F, int[] idx, double[] v, int a, int b, double g) {
        double i = g * (v[a] - v[b]);
        addI(F, idx, a, i); addI(F, idx, b, -i);
        addJ(J, idx, a, a, g); addJ(J, idx, a, b, -g); addJ(J, idx, b, a, -g); addJ(J, idx, b, b, g);
    }
    private static void addI(double[] F, int[] idx, int k, double i) { if (idx[k] >= 0) F[idx[k]] += i; }
    private static void addJ(Mat J, int[] idx, int r, int c, double g) { if (idx[r] >= 0 && idx[c] >= 0) J.add(idx[r], idx[c], g); }

    // ===== 連立一次方程式（O-006）：小さい回路は密な行列（v0.3 の 8×8 はこれ＝結果は前と同じ）、大きい回路は疎な行列 =====

    /** これより多い点の数は疎な行列（24×24＝576 マスより上で重くならないように） */
    public static int DENSE_MAX = 400;   // テストで疎な解き方を試すときだけ変える

    interface Mat {
        void add(int r, int c, double g);
        double[] solve(double[] b);
    }

    /** 密な行列：ガウスの消去法（部分ピボット） */
    static final class Dense implements Mat {
        final double[][] a;
        Dense(int n) { a = new double[n][n]; }
        @Override public void add(int r, int c, double g) { a[r][c] += g; }
        @Override public double[] solve(double[] b) { return gauss(a, b); }
    }

    /**
     * 疎な行列：行ごとに「列 → 値」。解き方は BiCGSTAB（非対称でもよい反復法）に、対角で割る前処理（ヤコビ）。
     * 1マスは上下左右と窓・接合にしかつながらないので、1行の中身は数個。点が数千でも1回の掛け算は速い
     */
    static final class Sparse implements Mat {
        final int n;
        final java.util.HashMap<Integer, Double>[] rows;
        @SuppressWarnings("unchecked")
        Sparse(int n) { this.n = n; rows = new java.util.HashMap[n]; for (int i = 0; i < n; i++) rows[i] = new java.util.HashMap<>(8); }
        @Override public void add(int r, int c, double g) { rows[r].merge(c, g, Double::sum); }

        private int[][] cols; private double[][] vals; private double[] diag;
        private void freeze() {
            cols = new int[n][]; vals = new double[n][]; diag = new double[n];
            for (int i = 0; i < n; i++) {
                cols[i] = new int[rows[i].size()]; vals[i] = new double[rows[i].size()];
                int k = 0;
                for (var e : rows[i].entrySet()) { cols[i][k] = e.getKey(); vals[i][k] = e.getValue(); k++; if (e.getKey() == i) diag[i] = e.getValue(); }
                if (diag[i] == 0) diag[i] = 1;
            }
        }
        private void mul(double[] x, double[] out) {
            for (int i = 0; i < n; i++) { double s = 0; int[] c = cols[i]; double[] v = vals[i]; for (int k = 0; k < c.length; k++) s += v[k] * x[c[k]]; out[i] = s; }
        }
        private void precond(double[] r, double[] out) { for (int i = 0; i < n; i++) out[i] = r[i] / diag[i]; }
        private static double dot(double[] a, double[] b) { double s = 0; for (int i = 0; i < a.length; i++) s += a[i] * b[i]; return s; }

        @Override public double[] solve(double[] b) {
            freeze();
            double[] x = new double[n], r = b.clone(), r0 = b.clone(), p = new double[n], v = new double[n], s = new double[n], t = new double[n], y = new double[n], z = new double[n];
            double bn = Math.sqrt(dot(b, b));
            if (bn == 0) return x;
            double rho = 1, alpha = 1, omega = 1;
            for (int it = 0; it < Math.min(20 * n + 100, 3000); it++) {   // 上限（収束しない行列で長く止まらないように）
                double rho1 = dot(r0, r);
                if (rho1 == 0) break;
                double beta = (rho1 / rho) * (alpha / omega);
                for (int i = 0; i < n; i++) p[i] = r[i] + beta * (p[i] - omega * v[i]);
                precond(p, y);
                mul(y, v);
                double r0v = dot(r0, v);
                if (r0v == 0) break;
                alpha = rho1 / r0v;
                for (int i = 0; i < n; i++) s[i] = r[i] - alpha * v[i];
                if (Math.sqrt(dot(s, s)) < 1e-13 * bn) { for (int i = 0; i < n; i++) x[i] += alpha * y[i]; break; }
                precond(s, z);
                mul(z, t);
                double tt = dot(t, t);
                omega = tt == 0 ? 0 : dot(t, s) / tt;
                for (int i = 0; i < n; i++) { x[i] += alpha * y[i] + omega * z[i]; r[i] = s[i] - omega * t[i]; }
                rho = rho1;
                if (Math.sqrt(dot(r, r)) < 1e-13 * bn || omega == 0) break;
            }
            return x;
        }
    }

    /** ガウスの消去法（部分ピボット）で J x = F を解く */
    private static double[] gauss(double[][] a, double[] b) {
        int n = b.length;
        double[][] m = new double[n][];
        for (int i = 0; i < n; i++) m[i] = a[i].clone();
        double[] x = b.clone();
        for (int c = 0; c < n; c++) {
            int p = c;
            for (int r = c + 1; r < n; r++) if (Math.abs(m[r][c]) > Math.abs(m[p][c])) p = r;
            double[] t = m[c]; m[c] = m[p]; m[p] = t;
            double tx = x[c]; x[c] = x[p]; x[p] = tx;
            if (Math.abs(m[c][c]) < 1e-300) continue;
            for (int r = c + 1; r < n; r++) {
                double f = m[r][c] / m[c][c];
                if (f == 0) continue;
                for (int k = c; k < n; k++) m[r][k] -= f * m[c][k];
                x[r] -= f * x[c];
            }
        }
        for (int r = n - 1; r >= 0; r--) {
            double s = x[r];
            for (int k = r + 1; k < n; k++) s -= m[r][k] * x[k];
            x[r] = Math.abs(m[r][r]) < 1e-300 ? 0 : s / m[r][r];
        }
        return x;
    }

    // ===== 論理（LITHO_DESIGN §6.2 の 7） =====

    /** 電源・入力の電圧 */
    public static final double VCC = 5.0, HIGH = 2.5;

    /** パッドの役目（v0.3b のお題、検査台で決める）。入力C は O-018、入力D・クロック・出力3/4 は S-009（Q18-2） */
    public enum Role { GROUND, VCC, IN_A, IN_B, OUT_1, OUT_2, IN_C, IN_D, CLK, OUT_3, OUT_4, OUT_5, OUT_6, OUT_7, OUT_8 }   // 出力5〜8 は CPU の番地（[10-13]）
    /** 出力の役目（ビット順：bit0＝出力1 …） */
    public static final Role[] OUTS = {Role.OUT_1, Role.OUT_2, Role.OUT_3, Role.OUT_4, Role.OUT_5, Role.OUT_6, Role.OUT_7, Role.OUT_8};

    /** 論理の計算の結果：出力のビットと、全部の点の電圧（次の手の計算の始まりに使う＝記憶、S-009） */
    public record Eval(int bits, double[] volts) {}

    /**
     * 論理の計算（§6.2 の 7、v0.3b・S-009）：0 V・電源・入力（役目 → 1/0、1＝5 V）をかけて回路を解き、出力の線の電圧が {@link #HIGH} より上なら 1。
     * start＝前の手の電圧（順序回路は前の状態から計算を始めると、その状態を覚えている）。null なら 0 V から。
     * 役目の決まっていない入力のパッドは 0 V。0 V・電源・要る入力・要る出力の役目がそろっていなければ null
     */
    public static Eval evaluate(Netlist nl, Map<Integer, Role> roles, Map<Role, Boolean> inputs, int outputs, double leakScale, double[] start) {
        Map<Role, Double> v = new java.util.EnumMap<>(Role.class);
        for (var e : inputs.entrySet()) v.put(e.getKey(), e.getValue() ? VCC : 0.0);
        return evaluateVolts(nl, roles, v, outputs, leakScale, start);
    }

    /** 入力を電圧で（手から手へ入力をなめらかに変えるとき、S-009） */
    public static Eval evaluateVolts(Netlist nl, Map<Integer, Role> roles, Map<Role, Double> inputs, int outputs, double leakScale, double[] start) { return evaluateVolts(nl, roles, inputs, outputs, leakScale, start, 0); }

    /** dt＞0：前の手の電圧から dt 秒後（コンデンサの電荷を持ちこす、[10-12]） */
    public static Eval evaluateVolts(Netlist nl, Map<Integer, Role> roles, Map<Role, Double> inputs, int outputs, double leakScale, double[] start, double dt) {
        Map<Integer, Double> d = new HashMap<>();
        int[] outNet = {-1, -1, -1, -1};
        java.util.EnumSet<Role> seen = java.util.EnumSet.noneOf(Role.class);
        for (var e : roles.entrySet()) {
            Role r = e.getValue();
            seen.add(r);
            switch (r) {
                case GROUND -> d.put(e.getKey(), 0.0);
                case VCC -> d.put(e.getKey(), VCC);
                case OUT_1 -> outNet[0] = e.getKey();
                case OUT_2 -> outNet[1] = e.getKey();
                case OUT_3 -> outNet[2] = e.getKey();
                case OUT_4 -> outNet[3] = e.getKey();
                case OUT_5, OUT_6, OUT_7, OUT_8 -> {}   // CPU の番地（アナログでは見ない）
                default -> d.put(e.getKey(), inputs.getOrDefault(r, 0.0));
            }
        }
        if (!seen.contains(Role.GROUND) || !seen.contains(Role.VCC)) return null;
        for (Role r : inputs.keySet()) if (!seen.contains(r)) return null;
        for (int i = 0; i < outputs; i++) if (outNet[i] < 0) return null;
        Solution sol = solve(nl, d, leakScale, start, dt);
        int bits = 0;
        for (int i = 0; i < outputs; i++) if (sol.netVolts(nl, outNet[i]) > HIGH) bits |= 1 << i;
        return new Eval(bits, sol.volts());
    }

    /** 2入力の形（前からの呼び方） */
    public static int logic(Netlist nl, Map<Integer, Role> roles, boolean a, boolean b, int inputs, int outputs, double leakScale) {
        return logic(nl, roles, (a ? 4 : 0) | (b ? 2 : 0), inputs, outputs, leakScale);
    }

    /** 入力をビットで（bit2＝A、bit1＝B、bit0＝C）。返り値＝出力のビット、役目がそろっていなければ −1 */
    public static int logic(Netlist nl, Map<Integer, Role> roles, int in, int inputs, int outputs, double leakScale) {
        Map<Role, Boolean> ins = new java.util.EnumMap<>(Role.class);
        Role[] order = {Role.IN_A, Role.IN_B, Role.IN_C};
        for (int i = 0; i < Math.min(3, inputs); i++) ins.put(order[i], (in & (4 >> i)) != 0);
        Eval e = evaluate(nl, roles, ins, outputs, leakScale, null);
        return e == null ? -1 : e.bits();
    }

    /**
     * NOT（RTL のインバータ）の形か：トランジスタ1つ、エミッタの線＝パッド（0 V）、コレクタの線＝パッド（出力）で、
     * コレクタの線から抵抗で別のパッド（電源）へ、ベースの線（か抵抗の先）がパッド（入力）。
     * そうなら {入力, 出力, 電源, 0V} のパッドの線の番号、ちがえば null
     */
    public static int[] notPins(Netlist nl) {
        for (Netlist.Transistor t : nl.transistors) {
            if (!t.npn()) continue;
            if (!isPad(nl, t.eNet()) || !isPad(nl, t.cNet())) continue;
            int vcc = -1, in = isPad(nl, t.bNet()) ? t.bNet() : -1;
            for (Netlist.Resistor r : nl.resistors) {
                int other = r.netA() == t.cNet() ? r.netB() : r.netB() == t.cNet() ? r.netA() : -1;
                if (other >= 0 && isPad(nl, other) && other != t.eNet()) vcc = other;
                int viaBase = r.netA() == t.bNet() ? r.netB() : r.netB() == t.bNet() ? r.netA() : -1;
                if (in < 0 && viaBase >= 0 && isPad(nl, viaBase) && viaBase != t.cNet() && viaBase != t.eNet()) in = viaBase;
            }
            if (vcc >= 0 && in >= 0 && vcc != in) return new int[] {in, t.cNet(), vcc, t.eNet()};
        }
        return null;
    }

    private static boolean isPad(Netlist nl, int net) { return net >= 0 && nl.nets.get(net).isPad(); }

    /** NOT のパッドに 入力を与えて出力の電圧を計算（出力はつながない） */
    public static double notOutput(Netlist nl, int[] pins, boolean input) { return notOutput(nl, pins, input, 1.0); }

    public static double notOutput(Netlist nl, int[] pins, boolean input, double leakScale) {
        Map<Integer, Double> d = new HashMap<>();
        d.put(pins[0], input ? VCC : 0.0);
        d.put(pins[2], VCC);
        d.put(pins[3], 0.0);
        return solve(nl, d, leakScale).netVolts(nl, pins[1]);
    }

    /** NOT として本当に動くか（入力 0 → 出力が高い、入力 1 → 出力が低い） */
    public static boolean worksAsNot(Netlist nl) { return worksAsNot(nl, 1.0); }

    public static boolean worksAsNot(Netlist nl, double leakScale) {
        int[] p = notPins(nl);
        return p != null && notOutput(nl, p, false, leakScale) > HIGH && notOutput(nl, p, true, leakScale) < HIGH;
    }
}
