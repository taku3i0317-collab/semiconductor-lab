package com.taku3.semiconductorlab.litho;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 工程の関数（LITHO_DESIGN §5）。C・E の機械はこれだけを呼ぶ。
 * どれも入れたウェハーは変えず、結果のウェハー（同じ数）を {@link Result#out()} で返す。失敗ならウェハーはそのまま（材料を失わない）。
 * 理由のキー：lang litho.semiconductor-lab.fail.&lt;reason&gt;
 */
public final class ChipOps {
    private ChipOps() {}

    /** 拡散は v0.3 では3回まで（LITHO_DESIGN §2） */
    public static final int MAX_DIFFUSIONS = 3;
    /** 研磨してやり直せる回数（本物の再生ウェハー） */
    public static final int MAX_RECLAIMS = 2;

    public record Result(boolean ok, String reason, ItemStack out) {
        static Result ok(ItemStack out) { return new Result(true, "", out); }
        static Result fail(String reason) { return new Result(false, reason, ItemStack.EMPTY); }
        /** 画面に出す理由の lang キー */
        public String langKey() { return "litho.semiconductor-lab.fail." + reason; }
    }

    /** ウェハーの中身（process_wafer なら chip_state、酸化膜つきの板ならまだ null） */
    public static ChipState state(ItemStack w) { return w.is(Semiconductor.PROCESS_WAFER) ? w.get(ModDataComponents.CHIP_STATE) : null; }

    /** 加工中のウェハーか */
    public static boolean isProcessWafer(ItemStack w) { return state(w) != null; }

    private static ItemStack write(ItemStack w, ChipState s) {
        ItemStack out = w.is(Semiconductor.PROCESS_WAFER) ? w.copy() : w.transmuteCopy(Semiconductor.PROCESS_WAFER);
        out.set(ModDataComponents.CHIP_STATE, s);
        return out;
    }

    /** もとの板のドーパント → 板の型（'p' / 'n'）。ドーパントがなければ null */
    static String substrateOf(ItemStack w) {
        String e = w.get(ModDataComponents.EPI);   // エピ層があれば、表面の型＝エピ層の型（[9-30]）
        if ("n".equals(e) || "p".equals(e)) return baseOf(w) == null ? null : e;
        return baseOf(w);
    }

    /** 板そのものの型（DOPANT から） */
    static String baseOf(ItemStack w) {
        String d = w.get(ModDataComponents.DOPANT);
        if ("boron".equals(d)) return "p";
        if ("phosphorus".equals(d)) return "n";
        return null;
    }

    /**
     * ほこり（クリーンルーム [12-14]、C と決めた形）：工程の機械が、部屋の等級に合わせて汚れを n 足す。失敗にはしない（ツールチップ・検査台の漏れ・v0.5 の歩留まり）。
     * 目安：クリーンルームの外 +2、クラス1000 相当 +1、クラス100 相当 0。加工中のウェハーでなければそのまま返す
     */
    public static ItemStack addDust(ItemStack w, int n) {
        ChipState s = state(w);
        return s == null || n <= 0 ? w : write(w, s.dirtier(n));
    }

    // ===== レジスト =====

    public static boolean canCoat(ItemStack w) { return coat(w).ok(); }

    /** [9-19] ポジ型レジストを塗る（前からの呼び方） */
    public static Result coat(ItemStack w) { return coat(w, ChipState.Tone.POSITIVE); }

    /** レジストの型（塗っていなければ null） */
    public static ChipState.Tone tone(ItemStack w) { ChipState s = state(w); return s == null || s.resist() == ChipState.Resist.NONE ? null : s.tone(); }

    /** 現像液の種類：ポジ型はアルカリの薄い水溶液（NaOH・KOH・のちに TMAH）、ネガ型は有機溶剤（キシレン、KPR・KTFR） */
    public enum Developer { ALKALI, ORGANIC }

    /** [9-19] レジストを塗る（型つき：ポジ型 [2-32]・ネガ型 [2-30] KPR）。酸化膜つきの板はここで加工中のウェハーになる */
    /** レジストの種類（[2-33]、E と決めた形）：DNQ ノボラック（ポジ型）・KPR（ネガ型）・化学増幅（1982 IBM） */
    public static final String RESIST_DNQ = "dnq", RESIST_KPR = "kpr", RESIST_CAR = "car";
    /** これより短い波長（KrF 248 nm から）では、ノボラックも KPR も光を吸いすぎて底まで届かない（化学増幅でないと焼けない） */
    public static final double OPAQUE_BELOW_NM = 250;

    /** レジストの種類つき（[2-33]）。種類を決めたレジストだけ、短い波長で光を通すかを見る（前からの塗り方は見ない＝今の例は壊れない） */
    public static Result coat(ItemStack w, ChipState.Tone tone, String kind) {
        Result r = coat(w, tone);
        if (!r.ok()) return r;
        ChipState s = state(r.out());
        return Result.ok(write(r.out(), s.withExtras(s.extras().withResist(kind == null ? "" : kind, false))));
    }

    public static Result coat(ItemStack w, ChipState.Tone tone) {
        ChipState s = state(w);
        if (s == null) {
            if (!w.is(Semiconductor.OXIDIZED_WAFER)) return Result.fail("not_oxidized");
            String sub = substrateOf(w);
            if (sub == null) return Result.fail("undoped");
            s = ChipState.fresh(sub, 0);
            if (w.has(ModDataComponents.EPI)) s = s.withExtras(new ChipState.Extras(baseOf(w), false));   // エピの下の板の型を記録
        }
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("already_coated");
        return Result.ok(write(w, s.withTone(tone).withResist(ChipState.Resist.COATED, s.resistPattern())));
    }

    public static boolean canExpose(ItemStack w, MaskData mask, ExposureTool tool) { return expose(w, mask, tool).ok(); }

    /**
     * [9-20]／[9-39] マスクを通して光を当てる。ポジ型レジスト：光の当たった所があとで溶ける。
     * 窓の層は描いた所に光、金属の層は描いていない所に光（描いた所の金属が残る）。
     */
    public static Result expose(ItemStack w, MaskData mask, ExposureTool tool) { return expose(w, mask, tool, 0); }

    /**
     * 露光（ずれつき、[8-17] C のマスクアライナー）：overlayNm＝この層の重ね合わせのずれ（nm）。1層目（まだ何も焼いていない板）は合わせる相手がないので記録しない。
     * ずれは失敗にしない（ChipState.overlayNm に最大を残す。ツールチップ、v0.5 の歩留まり）
     */
    /**
     * 露光（レイリーの式、S-010 Q20-1 案A）：レンズの解像度 CD（＝k1·λ/NA、C の投影レンズ）から方眼の大きさ N を出し（{@link ExposureTool#gridFor}、4刻み）、
     * それより細かいマスクは焼けない。光源を短い波長に・レンズを良く（NA を大きく、収差を小さく）すると細かく描ける。
     * baseTool＝光の当て方の種類（密着か投影か、波長）。表の方眼の数は「基準のレンズ」の値として残る
     */
    /** 1段の段差の高さ（nm、v0.5 #1 焦点深度、C と決めた形・司令塔の仮決定 O-030、ゲームの値 ⚠️）。表面の段差＝steps()×これ */
    public static final int STEP_NM = 80;

    /**
     * 焦点深度つき（v0.5 #1）：表面の段差（{@link ChipState#steps()}×{@link #STEP_NM}）がレンズの焦点深度 dofNm（k2·λ/NA²、C の ProjectionLensItem.dofNm）より大きいと、
     * ピントが合わず焼けない（defocus、材料は失わない）。CMP（{@link #planarize}）で平らにすると焼ける
     */
    public static Result exposeAtCd(ItemStack w, MaskData mask, ExposureTool baseTool, int cdNm, int overlayNm, int dofNm) {
        ChipState s = state(w);
        if (s != null && dofNm > 0 && (long) s.steps() * STEP_NM > dofNm) return Result.fail("defocus");
        return exposeAtCd(w, mask, baseTool, cdNm, overlayNm);
    }

    public static Result exposeAtCd(ItemStack w, MaskData mask, ExposureTool baseTool, int cdNm, int overlayNm) {
        if (cdNm <= 0) return Result.fail("too_fine");
        int g = ExposureTool.gridFor(cdNm);
        // マルチパターニング（LELE、v0.5 #6、C と決めた形・ユーザーの決定 S-013「2枚まで」）：縮小カメラで2つに分けたマスク（設計の名前の末尾 " [1/2]"・" [2/2]"）で、
        // となり合う列に描いた所がなければ、方眼の2倍まで焼ける
        boolean split = isSplitMask(mask) && mask.size() <= Math.min(BitGrid.MAX, 2 * g) && isSplit(mask.cells());
        if (mask.size() > g && !split) return Result.fail("too_fine");
        return exposeGrid(w, mask, baseTool, Math.max(mask.size(), 1), overlayNm);
    }

    /** 縮小カメラで2つに分けたマスクか（設計の名前の末尾が " [1/2]" か " [2/2]"） */
    public static boolean isSplitMask(MaskData m) { return m.design() != null && (m.design().endsWith(" [1/2]") || m.design().endsWith(" [2/2]")); }

    /** 分けたマスクか：描いた所の横どなりに描いた所がない（列の偶数・奇数で2枚に分けた形＝横のピッチが2倍） */
    public static boolean isSplit(BitGrid cells) {
        for (int y = 0; y < cells.size(); y++) for (int x = 0; x + 1 < cells.size(); x++) if (cells.get(x, y) && cells.get(x + 1, y)) return false;
        return true;
    }

    public static Result expose(ItemStack w, MaskData mask, ExposureTool tool, int overlayNm) { return exposeGrid(w, mask, tool, tool.grid, overlayNm); }

    private static Result exposeGrid(ItemStack w, MaskData mask, ExposureTool tool, int grid, int overlayNm) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.COATED) return Result.fail("no_resist");
        if (mask.size() > grid) return Result.fail("too_fine");
        if (s.sized() && s.size() != mask.size()) return Result.fail("size_mismatch");
        String kind = s.extras().resistKind();
        if (tool.wavelengthNm < OPAQUE_BELOW_NM && (RESIST_DNQ.equals(kind) || RESIST_KPR.equals(kind))) return Result.fail("opaque_resist");   // [2-33]：ノボラックは 248 nm で不透明
        if (!s.sized()) s = s.withSize(mask.size()).withParts(mask.parts());   // 1枚目：設計図に押したハンコの記録もチップへ（v0.3b）
        else if (overlayNm > 0) s = s.overlay(overlayNm);   // 2層目から
        BitGrid lit = mask.kind().window ? mask.cells() : mask.cells().invert();
        if (s.tone() == ChipState.Tone.NEGATIVE) lit = lit.invert();   // ネガ型：残したい所に光を当てる（マスクの白黒が逆＝ネガ型用のマスク。遊ぶ人は今までどおり「ある所」を描く）
        return Result.ok(write(w, s.withResist(ChipState.Resist.EXPOSED, lit)));
    }

    public static boolean canDevelop(ItemStack w) { return develop(w).ok(); }

    /** [9-21] 現像：光の当たった所のレジストが溶ける。resistPattern は「レジストが残っている所」になる */
    public static Result develop(ItemStack w) { return develop(w, Developer.ALKALI); }

    /** 現像（現像液つき）：ポジ型はアルカリで光の当たった所が溶け、ネガ型は有機溶剤で光の当たらなかった所が溶ける。型と現像液が合わないと "wrong_developer" */
    public static Result develop(ItemStack w, Developer dev) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.EXPOSED) return Result.fail("not_exposed");
        boolean neg = s.tone() == ChipState.Tone.NEGATIVE;
        if (dev != (neg ? Developer.ORGANIC : Developer.ALKALI)) return Result.fail("wrong_developer");
        if (RESIST_CAR.equals(s.extras().resistKind()) && !s.extras().baked())   // 化学増幅：露光後の加熱（PEB）がないと酸が保護基を外さない＝どこも溶けない
            return Result.ok(write(w, s.withResist(ChipState.Resist.DEVELOPED, BitGrid.empty(s.size()).invert())));
        return Result.ok(write(w, s.withResist(ChipState.Resist.DEVELOPED, neg ? s.resistPattern() : s.resistPattern().invert())));
    }

    public static boolean canBake(ItemStack w) { return bake(w).ok(); }

    /**
     * 露光後の加熱（PEB、[2-33]、E のホットプレート、E と決めた形）：化学増幅レジストで、光で生まれた酸1個が熱で何百もの保護基を外す（1982 IBM の伊藤・ウィルソン・フレシェ）。
     * 露光したあと・現像の前に1回。化学増幅でなければ not_car
     */
    public static Result bake(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.EXPOSED) return Result.fail("not_exposed");
        if (!RESIST_CAR.equals(s.extras().resistKind())) return Result.fail("not_car");
        if (s.extras().baked()) return Result.fail("already");
        return Result.ok(write(w, s.withExtras(s.extras().withResist(RESIST_CAR, true))));
    }

    public static boolean canStrip(ItemStack w) { return strip(w).ok(); }

    /** レジストをはがす（どの段階でも。露光をまちがえたら、はがして塗り直せる＝本物のリワーク） */
    public static Result strip(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() == ChipState.Resist.NONE) return Result.fail("no_resist");
        ChipState t = s.withResist(ChipState.Resist.NONE, s.sized() ? BitGrid.empty(s.size()) : s.resistPattern()).withTone(ChipState.Tone.POSITIVE).dirtier(1);
        return Result.ok(write(w, t.withExtras(t.extras().withResist("", false))));   // はがすとレジストの種類・加熱の記録も消える
    }

    // ===== 削る =====

    public static boolean canEtchOxide(ItemStack w) { return etchOxide(w).ok(); }

    /** [9-06] 酸化膜を削る（HF）。レジストのない所に窓が開く（前から開いていた窓も残る）。レジストは残る（あとで strip） */
    public static Result etchOxide(ItemStack w) { return etchOxide(w, false); }

    /** 液の削り方（HF）が細かい線で太る方眼：g線ステッパーの方眼（CD 約 1 µm）から。本物も液は約 2〜3 µm が限界 */
    public static final int WET_LIMIT_GRID = ExposureTool.STEPPER_G.grid;

    /**
     * [9-06]／[9-27] 酸化膜を削る（E と決めた形）。anisotropic＝ドライエッチング（RIE、真下にだけ削る）。
     * 液（HF）は横にも溶かす（アンダーカット）：方眼が {@link #WET_LIMIT_GRID} 以上の細かいウェハーでは、開ける所が上下左右に1マス太る（線がくっつく・回路が変わる）。
     * それより粗い方眼（密着 16・投影 24）は液でも今までどおり
     */
    public static Result etchOxide(ItemStack w, boolean anisotropic) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        if (s.metalFilm()) return Result.fail("metal_on");
        BitGrid r = s.resistPattern();
        BitGrid cut = BitGrid.empty(s.size());
        for (int y = 0; y < s.size(); y++) for (int x = 0; x < s.size(); x++) if (!r.get(x, y)) cut = cut.with(x, y, true);
        if (!anisotropic && s.size() >= WET_LIMIT_GRID) cut = grow(cut);
        BitGrid open = s.windows();
        for (int y = 0; y < s.size(); y++) for (int x = 0; x < s.size(); x++) if (cut.get(x, y)) open = open.with(x, y, true);
        ChipState t = s;
        // 銅のダマシン（[9-36]、ビアが先）：層間膜に2回目の窓を開けるとき、1回目の窓をビアの穴として覚える（2回目＝配線の溝）
        if (s.interlayer() && s.hasWindows() && !s.extras().hasViaHoles()) t = t.withExtras(s.extras().withViaHoles(s.windows()));
        return Result.ok(write(w, t.withWindows(open).dirtier(1)));
    }

    /** 上下左右に1マス太らせる（液の横への溶け） */
    static BitGrid grow(BitGrid g) {
        BitGrid o = g;
        for (int y = 0; y < g.size(); y++) for (int x = 0; x < g.size(); x++) {
            if (!g.get(x, y)) continue;
            for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) if (g.inside(x + d[0], y + d[1])) o = o.with(x + d[0], y + d[1], true);
        }
        return o;
    }

    public static boolean canEtchMetal(ItemStack w) { return etchMetal(w).ok(); }

    /** [9-25] Al を削る：レジストが残っている所の金属だけが残る → 金属の層ができる */
    public static Result etchMetal(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.metalFilm()) return Result.fail("no_metal");
        if (s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        LayerKind k = s.interlayer() ? LayerKind.METAL2 : LayerKind.METAL;   // 層間膜の上なら2層目（S-011）
        if (s.has(k)) return Result.fail("too_many");
        return Result.ok(write(w, s.plus(k, s.resistPattern()).withMetalFilm(false).dirtier(1)));
    }

    // ===== しみ込ませる・付ける =====

    public static boolean canDiffuse(ItemStack w, char type) { return diffuse(w, type).ok(); }

    /** [9-16] 窓からドーパントをしみ込ませる（type＝'n' か 'p'）。拡散のあいだに酸化膜が育って窓は閉じる */
    public static Result diffuse(ItemStack w, char type) {
        ChipState s = state(w);
        if (s == null || !s.hasWindows()) return Result.fail("no_window");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm() || s.has(LayerKind.METAL)) return Result.fail("metal_on");
        if (s.diffusions() + s.implanted().size() >= MAX_DIFFUSIONS) return Result.fail("too_many");
        LayerKind k = type == 'n' ? LayerKind.DIFF_N : LayerKind.DIFF_P;
        return Result.ok(write(w, s.plus(k, s.windows()).withWindows(BitGrid.empty(s.size())).dirtier(1)));
    }

    public static boolean canImplant(ItemStack w, char type) { return implant(w, type, 1).ok(); }

    /**
     * [9-28] イオン注入（E と決めた形）：加速したイオンを、レジストの開いた所（現像したレジストがマスク）か、レジストがなければ酸化膜の窓に打ちこむ。
     * 拡散とちがい、窓は閉じない・横に広がらない・量を選べる。打ちこんだだけでは結晶がこわれていて効かない＝{@link #anneal} まで回路に入らない。
     * dose＝量の段（今は記録だけ。のちに面抵抗へ ⚠️）
     */
    public static Result implant(ItemStack w, char type, int dose) {
        ChipState s = state(w);
        if (s == null || !s.sized()) return Result.fail("no_pattern");
        if (s.metalFilm() || s.has(LayerKind.METAL)) return Result.fail("metal_on");
        if (s.diffusions() + s.implanted().size() >= MAX_DIFFUSIONS) return Result.fail("too_many");
        BitGrid open;
        if (s.resist() == ChipState.Resist.DEVELOPED) open = s.resistPattern().invert();   // レジストのない所に入る
        else if (s.resist() == ChipState.Resist.NONE && s.hasWindows()) open = s.windows();
        else return Result.fail(s.resist() == ChipState.Resist.NONE ? "no_window" : "not_developed");
        if (open.isEmpty()) return Result.fail("no_window");
        List<ChipState.Applied> im = new java.util.ArrayList<>(s.implanted());
        im.add(new ChipState.Applied(type == 'n' ? LayerKind.DIFF_N : LayerKind.DIFF_P, open));
        return Result.ok(write(w, s.withImplanted(im).dirtier(1)));
    }

    public static boolean canAnneal(ItemStack w) { return anneal(w).ok(); }

    /** [9-29] アニール：熱で結晶を直し、注入したドーパントを働かせる（注入した層が拡散と同じ層になる） */
    public static Result anneal(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.implanted().isEmpty()) return Result.fail("nothing_to_anneal");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");   // レジストは焼けてしまう：先にはがす
        ChipState t = s;
        for (ChipState.Applied a : s.implanted()) t = t.plus(a.kind(), a.cells());
        return Result.ok(write(w, t.withImplanted(List.of())));
    }

    public static boolean canDepositInterlayer(ItemStack w) { return depositInterlayer(w).ok(); }

    /**
     * 層間膜（S-011、2層目の金属の前、E の CVD 炉 [9-30]）：1層目の金属の上一面に SiO2（約400℃、Al が溶けない温度）。
     * このあとの窓は VIA（1層目と2層目をつなぐ）、金属は METAL2 になる
     */
    public static Result depositInterlayer(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.has(LayerKind.METAL)) return Result.fail("no_metal");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm() || s.interlayer()) return Result.fail("too_many");
        return Result.ok(write(w, s.withInterlayer(true).withWindows(BitGrid.empty(s.size())).dirtier(1)));
    }

    public static boolean canPlanarize(ItemStack w) { return planarize(w).ok(); }

    /**
     * CMP＝化学機械研磨（[9-31]、E の CMP 装置、E と決めた形）：層間膜の表面をコロイダルシリカのスラリーで削って平らにする（IBM 1980年代）。
     * 段差の段数 steps() が 0 になる（記録だけ。v0.5 の焦点深度で、細いツールの露光に効く）。スラリーの粒が残るので汚れ +2（このあと洗浄）。
     * 層間膜があり、レジストも金属の膜もないときだけ。もう平らなら already_flat
     */
    public static Result planarize(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.interlayer()) return Result.fail("no_interlayer");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.extras().copperFilm()) {   // 銅のダマシン（[9-36]）：上の銅をけずり、溝の中だけ残す＝溝の形がそのまま2層目、ビアの穴が VIA（Al のエッチングは使わない）
            ChipState t = s.extras().hasViaHoles() ? s.plus(LayerKind.VIA, s.extras().viaHoles()) : s;
            t = t.plus(LayerKind.METAL2, s.windows()).withWindows(BitGrid.empty(s.size())).withMetalFilm(false);
            return Result.ok(write(w, t.withExtras(t.extras().withCopperFilm(false)).flattened().dirtier(2)));
        }
        if (s.metalFilm()) return Result.fail("metal_on");
        if (s.steps() == 0) return Result.fail("already_flat");
        return Result.ok(write(w, s.flattened().dirtier(2)));
    }

    public static boolean canDepositBarrier(ItemStack w) { return depositBarrier(w).ok(); }

    /**
     * 銅の配線のバリア膜（[9-36]、1997 IBM、E のスパッタ＋タンタルの的、E と決めた形）：窓（溝）のある層間膜に Ta（TaN）をうすく付ける。
     * 銅はシリコンにしみこむと接合をこわすので、先にふたをする（記録だけ）
     */
    public static Result depositBarrier(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.interlayer()) return Result.fail("no_interlayer");
        if (!s.hasWindows()) return Result.fail("no_window");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm()) return Result.fail("metal_on");
        if (s.extras().barrier()) return Result.fail("already");
        return Result.ok(write(w, s.withExtras(s.extras().withBarrier(true)).dirtier(1)));
    }

    public static boolean canPlateCopper(ItemStack w) { return plateCopper(w).ok(); }

    /**
     * 銅めっき（[9-36]、E の硫酸銅のめっき槽）：窓（溝とビアの穴）を銅で埋めて、上一面に銅の膜。
     * バリアなしだと銅がシリコンへしみて汚れ +30（漏れが増える）。このあと CMP（{@link #planarize}）で溝の中だけ残す
     */
    public static Result plateCopper(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.interlayer()) return Result.fail("no_interlayer");
        if (!s.hasWindows()) return Result.fail("no_window");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm() || s.has(LayerKind.METAL2)) return Result.fail("metal_on");
        return Result.ok(write(w, s.withMetalFilm(true).withExtras(s.extras().withCopperFilm(true)).dirtier(s.extras().barrier() ? 1 : 30)));
    }

    public static boolean canFormFins(ItemStack w, boolean anisotropic) { return formFins(w, anisotropic).ok(); }

    /**
     * FinFET のフィンを立てる（[9-37]、E のドライエッチング、E と決めた形）：現像したレジストが残った所のシリコンを残し、まわりを深く削る＝細い板（フィン）。
     * 液のエッチングでは横にも溶けて細い板が立たない（wet）。拡散や金属の前、1回だけ。レジストはそのまま（E がはがす）
     */
    public static Result formFins(ItemStack w, boolean anisotropic) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        if (!anisotropic) return Result.fail("wet");
        if (s.has(LayerKind.FIN)) return Result.fail("too_many");
        if (s.diffusions() > 0 || s.has(LayerKind.METAL) || s.metalFilm()) return Result.fail("too_late");
        return Result.ok(write(w, s.plus(LayerKind.FIN, s.resistPattern()).dirtier(1)));
    }

    public static boolean canHighK(ItemStack w) { return highK(w).ok(); }

    /**
     * high-k のゲート絶縁膜（[1-70] HfO2、E の ALD 装置：HfCl4 と水を1層ずつ交互に、約 300℃。2007 インテル 45 nm、E と決めた形）。
     * SiO2 を薄くしすぎると電子がトンネルで漏れるので、誘電率の高い HfO2 で厚いまま同じ効き目にする（記録だけ、判定は変えない）。
     * ゲート酸化膜（GATE_OX）があり、レジストも金属の膜もないとき1回
     */
    public static Result highK(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.has(LayerKind.GATE_OX)) return Result.fail("no_gate");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm()) return Result.fail("metal_on");
        if (s.extras().highK()) return Result.fail("already");
        return Result.ok(write(w, s.withExtras(s.extras().withHighK(true))));
    }

    public static boolean canPassivate(ItemStack w) { return passivate(w).ok(); }

    /**
     * 最後の保護膜（パッシベーション、[9-30] の窒化膜 Si3N4、E の CVD 炉、E と決めた形）：金属の配線を削ったあと、上一面を窒化膜でおおって湿気・Na イオンから守る（記録だけ）。
     * 金属の配線があり、レジストも金属の膜もないときだけ。2回目は already
     */
    public static Result passivate(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !(s.has(LayerKind.METAL) || s.has(LayerKind.METAL2))) return Result.fail("no_metal");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm()) return Result.fail("metal_on");
        if (s.extras().passivated()) return Result.fail("already");
        return Result.ok(write(w, s.withExtras(s.extras().withPassivated(true))));
    }

    public static boolean canGateOxide(ItemStack w) { return gateOxide(w).ok(); }

    /**
     * MOS のゲート酸化膜（[9-23]、S-011 Q21-1 案A）：現像したレジストの開いた所の厚い酸化膜を削り、薄い酸化膜（約 0.1 µm）を育て直す。
     * 上に金属（Al ゲート、Q21-2 案A）を残すとゲートになる（窓ではないので電流は流れない）
     */
    public static Result gateOxide(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        if (s.metalFilm() || s.has(LayerKind.METAL)) return Result.fail("metal_on");
        if (s.has(LayerKind.GATE_OX)) return Result.fail("too_many");
        BitGrid cut = s.resistPattern().invert();
        if (cut.isEmpty()) return Result.fail("no_window");
        return Result.ok(write(w, s.plus(LayerKind.GATE_OX, cut).dirtier(1)));
    }

    public static boolean canDepositMetal(ItemStack w) { return depositMetal(w).ok(); }

    /** [9-25] Al を一面に付ける（真空蒸着）。開いている窓は金属とシリコンがつながる所（CONTACT）になる */
    public static Result depositMetal(ItemStack w) { return depositMetal(w, false); }

    /** conformal＝段差によく付く方法（[9-26] スパッタ）。今は記録だけ（ChipState.conformalMetal、v0.5 の歩留まりで使う ⚠️） */
    public static Result depositMetal(ItemStack w, boolean conformal) {
        ChipState s = state(w);
        if (s == null || !s.sized()) return Result.fail("no_pattern");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm() || s.has(s.interlayer() ? LayerKind.METAL2 : LayerKind.METAL)) return Result.fail("too_many");
        LayerKind hole = s.interlayer() ? LayerKind.VIA : LayerKind.CONTACT;   // 層間膜の窓は1層目と2層目をつなぐ VIA（S-011）
        ChipState t = s.hasWindows() ? s.plus(hole, s.windows()) : s;
        return Result.ok(write(w, t.withWindows(BitGrid.empty(s.size())).withMetalFilm(true).withConformal(conformal).dirtier(1)));
    }

    // ===== 洗う（[9-14]、E の洗浄槽が呼ぶ） =====

    public static boolean canClean(ItemStack w) { return clean(w, 0).ok(); }

    /**
     * 汚れを target まで落とす（段階1＝硝酸・フッ酸・蒸留水なら 0、のちの RCA との差は歩留まりのときに）。
     * レジストが付いていたら洗えない（先にはがす）。もう target 以下なら何もしない（no_dirt）
     */
    public static Result clean(ItemStack w, int target) {
        ChipState s = state(w);
        if (s == null) return Result.fail("no_pattern");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.contamination() <= Math.max(0, target)) return Result.fail("no_dirt");
        return Result.ok(write(w, s.cleanedTo(target)));
    }

    // ===== やり直し =====

    public static boolean canReoxidize(ItemStack w) { return reoxidize(w).ok(); }

    /** [9-15] 窓を閉じる（窓の開けまちがいのやり直し） */
    public static Result reoxidize(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.hasWindows()) return Result.fail("no_window");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm()) return Result.fail("metal_on");
        return Result.ok(write(w, s.withWindows(BitGrid.empty(s.size()))));
    }

    public static boolean canReclaim(ItemStack w) { return reclaim(w).ok(); }

    /** [9-13] 研磨して最初からやり直す（本物の再生ウェハー。2回まで）。板の型・純度は残る */
    public static Result reclaim(ItemStack w) {
        ChipState s = state(w);
        if (s == null) return Result.fail("no_pattern");
        if (s.reclaims() >= MAX_RECLAIMS) return Result.fail("too_many_reclaims");
        return Result.ok(write(w, ChipState.fresh(s.substrate(), s.reclaims() + 1)));
    }
}
