package com.taku3.semiconductorlab.machine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 入手の道（O-035、DESIGN「入手・操作・組み立てを分かりやすく」）：ツールチップの「作り方：〇〇」1行。
 * 元は assets/semiconductor-lab/howto_get.json（tools/check_howto.py がレシピ・機械のレシピ・鉱石・ルートから作る）。
 * 道の印：crafting（作業台）・smelting（かまど）・campfire（たき火）・ore（掘る）・loot（壊すと出る）・machine:&lt;機械の ID&gt;
 */
public final class HowToGet {
    private HowToGet() {}
    /** 1行に出す道の数（多い物は最初の数個＋「…」） */
    public static final int MAX = 3;
    private static Map<String, List<String>> ways;
    private static JsonObject first, icons;
    /** 最初の作り方（{via, in:[[ID, 数]…]}、無ければ null）。S-030：ツールチップのアイコンの並び */
    public static JsonObject first(String id) { ways(id); return first != null && first.has(id) ? first.getAsJsonObject(id) : null; }
    /** MOD の物の絵（[アトラス, 絵]、無ければ null） */
    public static String[] icon(String id) {
        ways(id);
        if (icons == null || !icons.has(id)) return null;
        var a = icons.getAsJsonArray(id);
        return new String[] {a.get(0).getAsString(), a.get(1).getAsString()};
    }

    public static List<String> ways(String id) {
        if (ways == null) {
            Map<String, List<String>> m = new HashMap<>();
            try (var in = HowToGet.class.getResourceAsStream("/assets/semiconductor-lab/howto_get.json")) {
                if (in != null) {
                    JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    JsonObject o = root.getAsJsonObject("ways");
                    first = root.has("first") ? root.getAsJsonObject("first") : new JsonObject();
                    icons = root.has("icons") ? root.getAsJsonObject("icons") : new JsonObject();
                    for (var e : o.entrySet()) {
                        List<String> l = new ArrayList<>();
                        e.getValue().getAsJsonArray().forEach(x -> l.add(x.getAsString()));
                        m.put(e.getKey(), l);
                    }
                }
            } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            ways = m;
        }
        return ways.getOrDefault(id, List.of());
    }

    private static Map<String, List<JsonObject>> recipes;

    /** 機械のレシピの一覧（assets/semiconductor-lab/machine_howto.json、check_howto.py が machine_recipes から作る。1機械 6 個まで） */
    public static List<JsonObject> recipes(String machine) {
        if (recipes == null) {
            Map<String, List<JsonObject>> m = new HashMap<>();
            try (var in = HowToGet.class.getResourceAsStream("/assets/semiconductor-lab/machine_howto.json")) {
                if (in != null) {
                    JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("recipes");
                    for (var e : o.entrySet()) {
                        List<JsonObject> l = new ArrayList<>();
                        e.getValue().getAsJsonArray().forEach(x -> l.add(x.getAsJsonObject()));
                        m.put(e.getKey(), l);
                    }
                }
            } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            recipes = m;
        }
        return recipes.getOrDefault(machine, List.of());
    }

    /**
     * 機械の画面の「?」の文（O-035）：手書きの使い方（lang block.semiconductor-lab.&lt;id&gt;.howto）＋ 自動のレシピの一覧「・A ×2 + B → C（500 kW・60 秒）」。
     * どちらも無ければ空
     */
    public static List<String> machineHelp(String machine, Function<String, String> lang) {
        List<String> out = new ArrayList<>();
        String hand = lang.apply("block.semiconductor-lab." + machine + ".howto");
        if (hand != null) out.addAll(List.of(hand.split("\n")));
        List<JsonObject> rs = recipes(machine);
        String head = lang.apply("gui.semiconductor-lab.howto.recipes");
        if (!rs.isEmpty() && head != null) {
            out.add(head);
            for (JsonObject r : rs) {
                String in = items(r.getAsJsonArray("in"), lang), res = items(r.getAsJsonArray("out"), lang);
                StringBuilder sb = new StringBuilder("・").append(in).append(" → ").append(res);
                List<String> extra = new ArrayList<>();
                if (r.has("watts") && !r.get("watts").isJsonNull() && r.get("watts").getAsDouble() > 0) extra.add(watts(r.get("watts").getAsDouble()));
                String sec = lang.apply("gui.semiconductor-lab.howto.seconds");
                if (r.has("seconds") && !r.get("seconds").isJsonNull() && sec != null) extra.add(sec.replace("%s", trim(r.get("seconds").getAsDouble())));
                String ex = lang.apply("gui.semiconductor-lab.howto.extra"), sep = lang.apply("tooltip.semiconductor-lab.howto_get.sep");
                if (!extra.isEmpty() && ex != null) sb.append(ex.replace("%s", String.join(sep == null ? ", " : sep, extra)));
                out.add(sb.toString());
            }
        }
        return out;
    }

    private static String items(com.google.gson.JsonArray a, Function<String, String> lang) {
        List<String> names = new ArrayList<>();
        for (var e : a) {
            var pair = e.getAsJsonArray();
            String id = pair.get(0).getAsString();
            int n = pair.get(1).getAsInt();
            String ns = id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft", path = id.substring(id.indexOf(':') + 1);
            String name = lang.apply("item." + ns + "." + path);
            if (name == null) name = lang.apply("block." + ns + "." + path);
            if (name == null) name = path;
            names.add(n > 1 ? name + " ×" + n : name);
        }
        return String.join(" + ", names);
    }

    private static String watts(double w) {
        return w >= 1e6 ? trim(w / 1e6) + " MW" : w >= 1e3 ? trim(w / 1e3) + " kW" : trim(w) + " W";
    }

    private static String trim(double v) { return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(Math.round(v * 10) / 10.0); }

    /** 「作り方：電解槽・作業台」の文（道が無い・lang が無いときは null）。base は item.semiconductor-lab.&lt;id&gt; か block.… */
    public static String line(String base, Function<String, String> lang) {
        String fmt = lang.apply("tooltip.semiconductor-lab.howto_get");
        int dot = base.lastIndexOf('.');
        if (fmt == null || dot < 0) return null;
        List<String> w = ways(base.substring(dot + 1));
        if (w.isEmpty()) return null;
        List<String> names = new ArrayList<>();
        for (String s : w) {
            String n = s.startsWith("machine:") ? lang.apply("block.semiconductor-lab." + s.substring(8)) : lang.apply("tooltip.semiconductor-lab.howto_get." + s);
            if (n != null && !names.contains(n)) names.add(n);
            if (names.size() == MAX) break;
        }
        if (names.isEmpty()) return null;
        String sep = lang.apply("tooltip.semiconductor-lab.howto_get.sep");
        String joined = String.join(sep == null ? ", " : sep, names) + (w.size() > MAX ? "…" : "");
        return fmt.replace("%s", joined);
    }
}
