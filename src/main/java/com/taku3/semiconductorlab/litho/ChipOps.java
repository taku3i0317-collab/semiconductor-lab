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
    public static Result coat(ItemStack w, ChipState.Tone tone) {
        ChipState s = state(w);
        if (s == null) {
            if (!w.is(Semiconductor.OXIDIZED_WAFER)) return Result.fail("not_oxidized");
            String sub = substrateOf(w);
            if (sub == null) return Result.fail("undoped");
            s = ChipState.fresh(sub, 0);
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
    public static Result exposeAtCd(ItemStack w, MaskData mask, ExposureTool baseTool, int cdNm, int overlayNm) {
        if (cdNm <= 0 || mask.size() > ExposureTool.gridFor(cdNm)) return Result.fail("too_fine");
        return exposeGrid(w, mask, baseTool, Math.max(mask.size(), 1), overlayNm);
    }

    public static Result expose(ItemStack w, MaskData mask, ExposureTool tool, int overlayNm) { return exposeGrid(w, mask, tool, tool.grid, overlayNm); }

    private static Result exposeGrid(ItemStack w, MaskData mask, ExposureTool tool, int grid, int overlayNm) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.COATED) return Result.fail("no_resist");
        if (mask.size() > grid) return Result.fail("too_fine");
        if (s.sized() && s.size() != mask.size()) return Result.fail("size_mismatch");
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
        return Result.ok(write(w, s.withResist(ChipState.Resist.DEVELOPED, neg ? s.resistPattern() : s.resistPattern().invert())));
    }

    public static boolean canStrip(ItemStack w) { return strip(w).ok(); }

    /** レジストをはがす（どの段階でも。露光をまちがえたら、はがして塗り直せる＝本物のリワーク） */
    public static Result strip(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() == ChipState.Resist.NONE) return Result.fail("no_resist");
        return Result.ok(write(w, s.withResist(ChipState.Resist.NONE, s.sized() ? BitGrid.empty(s.size()) : s.resistPattern()).withTone(ChipState.Tone.POSITIVE).dirtier(1)));
    }

    // ===== 削る =====

    public static boolean canEtchOxide(ItemStack w) { return etchOxide(w).ok(); }

    /** [9-06] 酸化膜を削る（HF）。レジストのない所に窓が開く（前から開いていた窓も残る）。レジストは残る（あとで strip） */
    public static Result etchOxide(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        if (s.metalFilm()) return Result.fail("metal_on");
        BitGrid open = s.windows();
        BitGrid r = s.resistPattern();
        for (int y = 0; y < s.size(); y++) for (int x = 0; x < s.size(); x++) if (!r.get(x, y)) open = open.with(x, y, true);
        return Result.ok(write(w, s.withWindows(open).dirtier(1)));
    }

    public static boolean canEtchMetal(ItemStack w) { return etchMetal(w).ok(); }

    /** [9-25] Al を削る：レジストが残っている所の金属だけが残る → 金属の層ができる */
    public static Result etchMetal(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.metalFilm()) return Result.fail("no_metal");
        if (s.resist() != ChipState.Resist.DEVELOPED) return Result.fail("not_developed");
        if (s.has(LayerKind.METAL)) return Result.fail("too_many");
        return Result.ok(write(w, s.plus(LayerKind.METAL, s.resistPattern()).withMetalFilm(false).dirtier(1)));
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

    public static boolean canDepositMetal(ItemStack w) { return depositMetal(w).ok(); }

    /** [9-25] Al を一面に付ける（真空蒸着）。開いている窓は金属とシリコンがつながる所（CONTACT）になる */
    public static Result depositMetal(ItemStack w) {
        ChipState s = state(w);
        if (s == null || !s.sized()) return Result.fail("no_pattern");
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("resist_on");
        if (s.metalFilm() || s.has(LayerKind.METAL)) return Result.fail("too_many");
        ChipState t = s.hasWindows() ? s.plus(LayerKind.CONTACT, s.windows()) : s;
        return Result.ok(write(w, t.withWindows(BitGrid.empty(s.size())).withMetalFilm(true).dirtier(1)));
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
