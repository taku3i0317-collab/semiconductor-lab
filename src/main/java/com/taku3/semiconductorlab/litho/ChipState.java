package com.taku3.semiconductorlab.litho;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 加工中のウェハーの中身（データコンポーネント chip_state、アイテム process_wafer。LITHO_DESIGN §4.4）。
 * 中身は ChipOps だけが書きかえる（C・E の機械は ChipOps を呼ぶ）。
 * contamination＝汚れの度合い（[9-14] 洗浄、E と決めた形）：削る・はがす・しみ込ませる・Al のたびに +1、ChipOps.clean で落とす。
 * 失敗にはしない（O-002 方針2）。ツールチップに出し、v0.5 の歩留まりの欠陥密度 D に足す
 * interlayer＝1層目の金属の上に層間膜（CVD の SiO2）を付けたか（S-011、2層目の金属）。付けたあとの窓は VIA、金属は METAL2 になる。
 * conformalMetal＝金属を段差によく付く方法（[9-26] スパッタ）で付けたか（E と決めた形、記録だけ。v0.5 の歩留まりで、蒸着は段差のふちで切れやすい ⚠️）。
 * implanted＝イオン注入したが、まだアニールしていない層（[9-28]・[9-29]、E と決めた形）。アニールまで回路には効かない（ChipModel は done だけを見る）。
 * tone＝レジストの型（[2-30] ネガ型 KPR／[2-32] ポジ型、E と決めた形）：塗るときに決まり、露光・現像の向きが変わる。
 * overlayNm＝重ね合わせのずれ（[8-17] アライメント、C と決めた形）：2層目からの露光のずれの最大（nm）。これも失敗にはしない（ツールチップ、v0.5 の歩留まり）
 */
public record ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist,
                        BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm, Tone tone, List<Part> parts, List<Applied> implanted, boolean conformalMetal, boolean interlayer) {

    /** レジストの状態 */
    public enum Resist {
        NONE, COATED, EXPOSED, DEVELOPED;
        public static final Codec<Resist> CODEC = Codec.STRING.xmap(s -> Resist.valueOf(s.toUpperCase()), r -> r.name().toLowerCase());
    }

    /** レジストの型：ポジ型＝光の当たった所が溶ける（アルカリで現像）、ネガ型＝光の当たった所が固まって残る（有機溶剤で現像） */
    public enum Tone {
        POSITIVE, NEGATIVE;
        public static final Codec<Tone> CODEC = Codec.STRING.xmap(s -> Tone.valueOf(s.toUpperCase()), t -> t.name().toLowerCase());
    }

    /** 終わった層（拡散・窓・金属） */
    public record Applied(LayerKind kind, BitGrid cells) {
        public static final Codec<Applied> CODEC = RecordCodecBuilder.create(i -> i.group(
                LayerKind.CODEC.fieldOf("kind").forGetter(Applied::kind),
                BitGrid.CODEC.fieldOf("cells").forGetter(Applied::cells)
        ).apply(i, Applied::new));
    }

    /** まだ方眼の大きさが決まっていないときの仮の大きさ（窓・レジストの方眼の置き場所） */
    private static final int UNSET = BitGrid.MIN;

    public static final Codec<ChipState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, BitGrid.MAX).fieldOf("size").forGetter(ChipState::size),
            Codec.STRING.fieldOf("substrate").forGetter(ChipState::substrate),
            Applied.CODEC.listOf().fieldOf("done").forGetter(ChipState::done),
            BitGrid.CODEC.fieldOf("windows").forGetter(ChipState::windows),
            Resist.CODEC.fieldOf("resist").forGetter(ChipState::resist),
            BitGrid.CODEC.fieldOf("resist_pattern").forGetter(ChipState::resistPattern),
            Codec.BOOL.fieldOf("metal_film").forGetter(ChipState::metalFilm),
            Codec.INT.fieldOf("reclaims").forGetter(ChipState::reclaims),
            Codec.INT.optionalFieldOf("contamination", 0).forGetter(ChipState::contamination),
            Codec.INT.optionalFieldOf("overlay_nm", 0).forGetter(ChipState::overlayNm),
            Tone.CODEC.optionalFieldOf("resist_tone", Tone.POSITIVE).forGetter(ChipState::tone),
            Part.CODEC.listOf().optionalFieldOf("parts", List.of()).forGetter(ChipState::parts),
            Applied.CODEC.listOf().optionalFieldOf("implanted", List.of()).forGetter(ChipState::implanted),
            Codec.BOOL.optionalFieldOf("conformal_metal", false).forGetter(ChipState::conformalMetal),
            Codec.BOOL.optionalFieldOf("interlayer", false).forGetter(ChipState::interlayer)
    ).apply(i, ChipState::new));

    public ChipState {
        done = List.copyOf(done);
        parts = List.copyOf(parts);
        implanted = List.copyOf(implanted);
    }

    /** 注入なしの形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm, Tone tone, List<Part> parts) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, List.of());
    }

    /** 金属の付け方の記録なしの形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm, Tone tone, List<Part> parts, List<Applied> implanted) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, false);
    }

    /** 層間膜の記録なしの形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm, Tone tone, List<Part> parts, List<Applied> implanted, boolean conformalMetal) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, false);
    }

    /** ハンコの記録なしの形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm, Tone tone) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, List.of(), List.of());
    }

    /** ずれのない形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, 0, Tone.POSITIVE);
    }

    /** ポジ型の形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, Tone.POSITIVE);
    }

    /** 酸化膜つきの板から始めるとき（まだ何も描いていない） */
    public static ChipState fresh(String substrate, int reclaims) {
        return new ChipState(0, substrate, List.of(), BitGrid.empty(UNSET), Resist.NONE, BitGrid.empty(UNSET), false, reclaims, 0);
    }

    public boolean sized() { return size > 0; }
    public boolean hasWindows() { return sized() && !windows.isEmpty(); }
    public int diffusions() { return (int) done.stream().filter(a -> a.kind().isDiffusion()).count(); }
    public boolean has(LayerKind k) { return done.stream().anyMatch(a -> a.kind() == k); }

    // ===== 書きかえ（新しい ChipState を返す） =====
    ChipState withSize(int s) {
        return new ChipState(s, substrate, done, BitGrid.empty(s), resist, BitGrid.empty(s), metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, interlayer);
    }
    ChipState withResist(Resist r, BitGrid pattern) { return new ChipState(size, substrate, done, windows, r, pattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, interlayer); }
    ChipState withWindows(BitGrid w) { return new ChipState(size, substrate, done, w, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, interlayer); }
    ChipState withTone(Tone t) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, t, parts, implanted, conformalMetal, interlayer); }
    /** 押したハンコの記録（最初の露光でマスクから写す、v0.3b） */
    ChipState withParts(List<Part> p) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, p, implanted, conformalMetal, interlayer); }
    ChipState withImplanted(List<Applied> im) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, im, conformalMetal, interlayer); }
    ChipState withConformal(boolean c) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, c, interlayer); }
    ChipState withInterlayer(boolean il) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, il); }
    ChipState withMetalFilm(boolean m) { return new ChipState(size, substrate, done, windows, resist, resistPattern, m, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, interlayer); }
    /** 汚れを n 増やす（工程のあと）・target まで落とす（洗浄） */
    ChipState dirtier(int n) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination + n, overlayNm, tone, parts, implanted, conformalMetal, interlayer); }
    ChipState cleanedTo(int target) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, Math.min(contamination, Math.max(0, target)), overlayNm, tone, parts, implanted, conformalMetal, interlayer); }
    /** 重ね合わせのずれを記録（いちばん大きいずれを残す） */
    ChipState overlay(int nm) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, Math.max(overlayNm, Math.max(0, nm)), tone, parts, implanted, conformalMetal, interlayer); }

    ChipState plus(LayerKind k, BitGrid cells) {
        List<Applied> d = new ArrayList<>(done);
        d.add(new Applied(k, cells));
        return new ChipState(size, substrate, d, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm, tone, parts, implanted, conformalMetal, interlayer);
    }
}
