package com.taku3.semiconductorlab.litho;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.content.Semiconductor;
import net.minecraft.world.item.ItemStack;

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

    // ===== レジスト =====

    public static boolean canCoat(ItemStack w) { return coat(w).ok(); }

    /** [9-19] レジストを塗る。酸化膜つきの板はここで加工中のウェハーになる */
    public static Result coat(ItemStack w) {
        ChipState s = state(w);
        if (s == null) {
            if (!w.is(Semiconductor.OXIDIZED_WAFER)) return Result.fail("not_oxidized");
            String sub = substrateOf(w);
            if (sub == null) return Result.fail("undoped");
            s = ChipState.fresh(sub, 0);
        }
        if (s.resist() != ChipState.Resist.NONE) return Result.fail("already_coated");
        return Result.ok(write(w, s.withResist(ChipState.Resist.COATED, s.resistPattern())));
    }

    public static boolean canExpose(ItemStack w, MaskData mask, ExposureTool tool) { return expose(w, mask, tool).ok(); }

    /**
     * [9-20]／[9-39] マスクを通して光を当てる。ポジ型レジスト：光の当たった所があとで溶ける。
     * 窓の層は描いた所に光、金属の層は描いていない所に光（描いた所の金属が残る）。
     */
    public static Result expose(ItemStack w, MaskData mask, ExposureTool tool) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.COATED) return Result.fail("no_resist");
        if (mask.size() > tool.grid) return Result.fail("too_fine");
        if (s.sized() && s.size() != mask.size()) return Result.fail("size_mismatch");
        if (!s.sized()) s = s.withSize(mask.size());
        BitGrid lit = mask.kind().window ? mask.cells() : mask.cells().invert();
        return Result.ok(write(w, s.withResist(ChipState.Resist.EXPOSED, lit)));
    }

    public static boolean canDevelop(ItemStack w) { return develop(w).ok(); }

    /** [9-21] 現像：光の当たった所のレジストが溶ける。resistPattern は「レジストが残っている所」になる */
    public static Result develop(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() != ChipState.Resist.EXPOSED) return Result.fail("not_exposed");
        return Result.ok(write(w, s.withResist(ChipState.Resist.DEVELOPED, s.resistPattern().invert())));
    }

    public static boolean canStrip(ItemStack w) { return strip(w).ok(); }

    /** レジストをはがす（どの段階でも。露光をまちがえたら、はがして塗り直せる＝本物のリワーク） */
    public static Result strip(ItemStack w) {
        ChipState s = state(w);
        if (s == null || s.resist() == ChipState.Resist.NONE) return Result.fail("no_resist");
        return Result.ok(write(w, s.withResist(ChipState.Resist.NONE, s.sized() ? BitGrid.empty(s.size()) : s.resistPattern()).dirtier(1)));
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
        if (s.diffusions() >= MAX_DIFFUSIONS) return Result.fail("too_many");
        LayerKind k = type == 'n' ? LayerKind.DIFF_N : LayerKind.DIFF_P;
        return Result.ok(write(w, s.plus(k, s.windows()).withWindows(BitGrid.empty(s.size())).dirtier(1)));
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
