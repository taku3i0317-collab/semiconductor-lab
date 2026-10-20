package com.taku3.semiconductorlab.gametest;

import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * ゲームのフォントの文字の幅（IMP-30）。tools/gen_font_widths.py がフォント画像から作った font_widths.json を読む。
 * ゲームの Font.width と同じく、1文字ずつの送り幅を足して切り上げる（ゲームの実測 105 本と一致：GameTest font_widths_match_game）。
 */
public final class FontWidths {
    private static Map<Integer, Float> advance;
    /** 表にない文字（見つかったら、表を作り直すか文を直す） */
    public static final Set<String> UNKNOWN = new TreeSet<>();

    private FontWidths() {}

    private static synchronized Map<Integer, Float> table() {
        if (advance != null) return advance;
        Map<Integer, Float> m = new HashMap<>();
        try (var in = FontWidths.class.getResourceAsStream("/font_widths.json")) {
            if (in == null) throw new IllegalStateException("font_widths.json がない（python tools/gen_font_widths.py）");
            var runs = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("runs");
            for (var r : runs) {
                var a = r.getAsJsonArray();
                int start = a.get(0).getAsInt(), n = a.get(1).getAsInt();
                float adv = a.get(2).getAsFloat();
                for (int i = 0; i < n; i++) m.put(start + i, adv);
            }
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        return advance = m;
    }

    /** 文の幅（px） */
    public static int width(String s) {
        var t = table();
        float sum = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            Float a = t.get(cp);
            if (a == null) { UNKNOWN.add(new String(Character.toChars(cp)) + " U+" + Integer.toHexString(cp).toUpperCase()); a = 6f; }
            sum += a;
            i += Character.charCount(cp);
        }
        return (int) Math.ceil(sum);
    }
}
