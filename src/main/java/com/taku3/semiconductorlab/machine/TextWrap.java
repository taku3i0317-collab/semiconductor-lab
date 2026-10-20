package com.taku3.semiconductorlab.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * 文を決まった幅（px）で折り返す（IMP-30）。画面（MachineScreen、ゲームのフォント）と
 * 自動チェック（GameTest gui_layout、フォント画像から作った幅の表）が同じこの計算を使うので、
 * 「テストで入った文は、ゲームでも同じ行数で入る」。
 * 決まり：入らなくなったら、その行の最後の空白で切る（空白は消える）。空白がなければ（日本語など）文字の間で切る。改行 \n でも切る。
 */
public final class TextWrap {
    private TextWrap() {}

    /** 箱に入れた結果：行・倍率（1＝そのまま）・入ったか */
    public record Fit(List<String> lines, float scale, boolean ok) {}

    /**
     * 文を幅 width・maxLines 行の箱に入れる。まず折り返す。shrink なら、入るまで 0.05 ずつ縮めて（MachineLayout.MIN_SCALE まで）折り返し直す。
     * 画面（MachineScreen）と自動チェック（GuiLayoutTests）が同じこれを使う
     */
    public static Fit fit(String text, int width, int maxLines, boolean shrink, ToIntFunction<String> widthFn) {
        float scale = 1f;
        List<String> lines = split(text, width, widthFn);
        while (shrink && !fits(lines.size(), scale, maxLines) && scale > MachineLayout.MIN_SCALE + 1e-4f) {
            scale = Math.max(MachineLayout.MIN_SCALE, scale - 0.05f);
            lines = split(text, (int) (width / scale), widthFn);
        }
        return new Fit(lines, scale, fits(lines.size(), scale, maxLines));
    }

    /** 縮めた後の高さが、maxLines 行の箱（そのままの大きさ）に入るか。縮めると行が多くても入ることがある */
    static boolean fits(int n, float scale, int maxLines) {
        return ((n - 1) * MachineLayout.LINE + 9) * scale <= (maxLines - 1) * MachineLayout.LINE + 9 + 1e-3f;
    }

    /** @param width 文の幅を返す関数（ゲームは Font::width、テストは幅の表） */
    public static List<String> split(String text, int maxWidth, ToIntFunction<String> width) {
        List<String> out = new ArrayList<>();
        for (String para : text.split("\n", -1)) {
            String rest = para;
            while (!rest.isEmpty()) {
                if (width.applyAsInt(rest) <= maxWidth) { out.add(rest); break; }
                int cut = 0, lastSpace = -1;
                // 入る一番長い頭を探す
                for (int i = rest.offsetByCodePoints(0, 1); i <= rest.length(); i = i < rest.length() ? rest.offsetByCodePoints(i, 1) : rest.length() + 1) {
                    if (width.applyAsInt(rest.substring(0, i)) > maxWidth) break;
                    cut = i;
                    if (rest.charAt(i - 1) == ' ') lastSpace = i - 1;
                    if (i == rest.length()) break;
                }
                if (cut == 0) cut = rest.offsetByCodePoints(0, 1);   // 1文字も入らない幅：1文字ずつ
                int end = lastSpace > 0 && cut < rest.length() && rest.charAt(cut) != ' ' ? lastSpace : cut;
                out.add(rest.substring(0, end).stripTrailing());
                rest = rest.substring(end).stripLeading();
            }
            if (para.isEmpty()) out.add("");
        }
        return out;
    }
}
