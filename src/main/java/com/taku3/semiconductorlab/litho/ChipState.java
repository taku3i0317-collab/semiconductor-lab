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
 * overlayNm＝重ね合わせのずれ（[8-17] アライメント、C と決めた形）：2層目からの露光のずれの最大（nm）。これも失敗にはしない（ツールチップ、v0.5 の歩留まり）
 */
public record ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist,
                        BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination, int overlayNm) {

    /** レジストの状態 */
    public enum Resist {
        NONE, COATED, EXPOSED, DEVELOPED;
        public static final Codec<Resist> CODEC = Codec.STRING.xmap(s -> Resist.valueOf(s.toUpperCase()), r -> r.name().toLowerCase());
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
            Codec.INT.optionalFieldOf("overlay_nm", 0).forGetter(ChipState::overlayNm)
    ).apply(i, ChipState::new));

    public ChipState {
        done = List.copyOf(done);
    }

    /** ずれのない形（前からの呼び方） */
    public ChipState(int size, String substrate, List<Applied> done, BitGrid windows, Resist resist, BitGrid resistPattern, boolean metalFilm, int reclaims, int contamination) {
        this(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, 0);
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
        return new ChipState(s, substrate, done, BitGrid.empty(s), resist, BitGrid.empty(s), metalFilm, reclaims, contamination, overlayNm);
    }
    ChipState withResist(Resist r, BitGrid pattern) { return new ChipState(size, substrate, done, windows, r, pattern, metalFilm, reclaims, contamination, overlayNm); }
    ChipState withWindows(BitGrid w) { return new ChipState(size, substrate, done, w, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm); }
    ChipState withMetalFilm(boolean m) { return new ChipState(size, substrate, done, windows, resist, resistPattern, m, reclaims, contamination, overlayNm); }
    /** 汚れを n 増やす（工程のあと）・target まで落とす（洗浄） */
    ChipState dirtier(int n) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination + n, overlayNm); }
    ChipState cleanedTo(int target) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, Math.min(contamination, Math.max(0, target)), overlayNm); }
    /** 重ね合わせのずれを記録（いちばん大きいずれを残す） */
    ChipState overlay(int nm) { return new ChipState(size, substrate, done, windows, resist, resistPattern, metalFilm, reclaims, contamination, Math.max(overlayNm, Math.max(0, nm))); }

    ChipState plus(LayerKind k, BitGrid cells) {
        List<Applied> d = new ArrayList<>(done);
        d.add(new Applied(k, cells));
        return new ChipState(size, substrate, d, windows, resist, resistPattern, metalFilm, reclaims, contamination, overlayNm);
    }
}
