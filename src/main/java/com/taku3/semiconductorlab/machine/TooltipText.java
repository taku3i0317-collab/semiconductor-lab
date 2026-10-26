package com.taku3.semiconductorlab.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * アイテムの説明（ツールチップ）の行を作る（IMP-30）。ゲーム（client/NoteTooltip、本物のフォント）と
 * 自動チェック（GameTest tooltip_text、フォント画像の幅の表）が同じこの計算を使う。
 * 出す物：容器（IMP-33、中身入りのアイテムだけ）・placeholder（近似・省略・未実装）・note1（へえ①、いつも）・note2（へえ②、Shift）・note3/note4 の冒頭（Shift）・「Shift で詳しく」。
 */
public final class TooltipText {
    private TooltipText() {}

    /** 1行の幅の上限（px） */
    public static final int WIDTH = 200;
    /** へえ③④ の冒頭として見せる文字数 */
    public static final int PREVIEW = 18;

    /** 行の色（ゲームでは ChatFormatting に直す） */
    public enum Tone { VESSEL, PLACEHOLDER, NOTE, DEEP, HINT, HOWTO }   // HOWTO＝「作り方」の行（クライアントはアイコンの並びに替える、S-030）
    public record Line(String text, Tone tone) {}
    /** 組立台の部品表の1行分（O-005、DESIGN「組立台」）：部品の名前の lang キー・数・役目（lang キーの頭、＋".short" が短い名前） */
    public record BomPart(String itemKey, int count, String roleKey) {}

    /**
     * @param base  "item.semiconductor-lab.<id>" か "block.semiconductor-lab.<id>"（lang にある方）
     * @param lang  lang のキー → 文（無ければ null）
     * @param width 文の幅（px）
     */
    public static List<Line> lines(String base, boolean shift, Function<String, String> lang, ToIntFunction<String> width) {
        return lines(base, null, shift, lang, width);
    }

    /** @param vesselKey 入っている容器の lang キー（Vessel.langKey()、容器に入っていない物は null） */
    public static List<Line> lines(String base, String vesselKey, boolean shift, Function<String, String> lang, ToIntFunction<String> width) {
        return lines(base, vesselKey, null, shift, lang, width);
    }

    /** @param bom 組立台で作る物の部品表（なければ null）。Shift のときに「部品：」と1行ずつ出す */
    public static List<Line> lines(String base, String vesselKey, List<BomPart> bom, boolean shift, Function<String, String> lang, ToIntFunction<String> width) {
        List<Line> out = new ArrayList<>();
        // 「容器：ガラス瓶」はいつも出す（作業台で使うと空の容器が戻る、IMP-33）
        String vfmt = lang.apply("tooltip.semiconductor-lab.vessel"), vname = vesselKey == null ? null : lang.apply(vesselKey);
        if (vfmt != null && vname != null) out.add(new Line(vfmt.replace("%s", vname), Tone.VESSEL));
        // 「作り方：〇〇」はいつも出す（O-035、DESIGN「入手・操作・組み立てを分かりやすく」）
        String how = HowToGet.line(base, lang);
        if (how != null) out.add(new Line(how, Tone.HOWTO));
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
                bomLines(out, bom, lang);
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

    /** 部品表の行（見出し＋部品ごと）。名前が lang に無いとき（バニラの物をテストで測るとき）は ID の末尾 */
    private static void bomLines(List<Line> out, List<BomPart> bom, Function<String, String> lang) {
        if (bom == null || bom.isEmpty()) return;
        String head = lang.apply("tooltip.semiconductor-lab.bom"), fmt = lang.apply("tooltip.semiconductor-lab.bom_part");
        if (head == null || fmt == null) return;
        out.add(new Line(head, Tone.DEEP));
        for (BomPart p : bom) {
            String name = lang.apply(p.itemKey());
            if (name == null) name = p.itemKey().substring(p.itemKey().lastIndexOf('.') + 1);
            String role = lang.apply(p.roleKey() + ".short");
            out.add(new Line(fmt.replace("%1$s", name).replace("%2$s", String.valueOf(p.count())).replace("%3$s", role == null ? "" : role), Tone.DEEP));
        }
    }

    /** 冒頭 PREVIEW 文字（コードポイント単位）＋「…」 */
    public static String preview(String s) {
        if (s.codePointCount(0, s.length()) <= PREVIEW) return s;
        return s.substring(0, s.offsetByCodePoints(0, PREVIEW)) + "…";
    }

    /** 「省略：」の行か（ja・en） */
    public static boolean isOmitted(String s) { return s.startsWith("省略") || s.startsWith("Omitted") || s.startsWith("Simplified"); }
}
