package com.taku3.semiconductorlab.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * アイテムの説明（ツールチップ）の行を作る（IMP-30）。ゲーム（client/NoteTooltip、本物のフォント）と
 * 自動チェック（GameTest tooltip_text、フォント画像の幅の表）が同じこの計算を使う。
 * 出す物：placeholder（近似・省略・未実装）・note1（へえ①、いつも）・note2（へえ②、Shift）・note3/note4 の冒頭（Shift）・「Shift で詳しく」。
 */
public final class TooltipText {
    private TooltipText() {}

    /** 1行の幅の上限（px） */
    public static final int WIDTH = 200;
    /** へえ③④ の冒頭として見せる文字数 */
    public static final int PREVIEW = 18;

    /** 行の色（ゲームでは ChatFormatting に直す） */
    public enum Tone { PLACEHOLDER, NOTE, DEEP, HINT }
    public record Line(String text, Tone tone) {}

    /**
     * @param base  "item.semiconductor-lab.<id>" か "block.semiconductor-lab.<id>"（lang にある方）
     * @param lang  lang のキー → 文（無ければ null）
     * @param width 文の幅（px）
     */
    public static List<Line> lines(String base, boolean shift, Function<String, String> lang, ToIntFunction<String> width) {
        List<Line> out = new ArrayList<>();
        // 「省略：」（en は Omitted / Simplified）は Shift のときだけ。「近似：」「未実装」はいつも出す（化学ルール1：本物と違うところは隠さない）
        String ph = lang.apply(base + ".placeholder");
        boolean omittedHidden = ph != null && isOmitted(ph) && !shift;
        if (ph != null && !omittedHidden) out.add(new Line(ph, Tone.PLACEHOLDER));
        String note1 = lang.apply(base + ".note1");
        if (note1 != null) out.add(new Line(note1, Tone.NOTE));
        String note = lang.apply(base + ".note2");
        if (note == null) note = lang.apply(base + ".note");
        if (note != null) {
            if (shift) {
                out.add(new Line(note, Tone.NOTE));
                for (String s : new String[] {"note3", "note4"}) {
                    String k = lang.apply(base + "." + s);
                    String locked = lang.apply("tooltip.semiconductor-lab." + s + "_locked");
                    if (k != null && locked != null) out.add(new Line(locked.replace("%s", preview(k)), Tone.DEEP));
                }
            }
        }
        if (!shift && (note != null || omittedHidden)) {
            String hint = lang.apply("tooltip.semiconductor-lab.shift");
            if (hint != null) out.add(new Line(hint, Tone.HINT));
        }
        // 長い行は WIDTH px で折り返す（色はそのまま、TextWrap は画面の文字と同じ計算）
        List<Line> wrapped = new ArrayList<>();
        for (Line l : out) for (String part : TextWrap.split(l.text(), WIDTH, width)) wrapped.add(new Line(part, l.tone()));
        return wrapped;
    }

    /** 冒頭 PREVIEW 文字（コードポイント単位）＋「…」 */
    public static String preview(String s) {
        if (s.codePointCount(0, s.length()) <= PREVIEW) return s;
        return s.substring(0, s.offsetByCodePoints(0, PREVIEW)) + "…";
    }

    /** 「省略：」の行か（ja・en） */
    public static boolean isOmitted(String s) { return s.startsWith("省略") || s.startsWith("Omitted") || s.startsWith("Simplified"); }
}
