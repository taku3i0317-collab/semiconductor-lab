package com.taku3.semiconductorlab.gametest;

import com.google.gson.JsonParser;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineLayout;
import com.taku3.semiconductorlab.machine.TextWrap;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 画面の検査をゲームを起動せずに（IMP-30）。画面の文字・スロットの「レイアウトのデータ」（MachineLayout）と、
 * フォント画像から作った文字の幅（FontWidths）と、lang（ja・en）だけで、全部の機械について調べる：
 * スロットどうし・文字の箱どうしの重なり、枠・題名の帯・持ち物の段からのはみ出し、出るかもしれない全部の文が箱に入るか（折り返して決めた行数まで。反応式は縮めて）。
 * 同じデータから、設計図の画像 docs/gui_preview/&lt;機械&gt;_&lt;言語&gt;.png（枠・スロット・文字の箱）と report.txt を書き出す。
 */
public class GuiLayoutTests extends LabTestBase {
    static final String NS = "semiconductor-lab";
    static final String[] LANGS = {"en_us", "ja_jp"};
    /** 画面の大きさ（バニラの入れ物の画面と同じ）・内側の縁・持ち物の段の上の端・「持ち物」の行の高さ */
    static final int W = 176, EDGE = 4;
    /** 画面の高さ・持ち物の段の上の端・「持ち物」の行（画面の高さはレイアウトのデータ。ふつうは 166） */
    static int H(Machine<?> m) { return m.layout.height(); }
    static int invTop(Machine<?> m) { return H(m) - 83; }
    static int invLabelY(Machine<?> m) { return H(m) - 94; }
    /** バニラの「持ち物」の文字（lang は MOD の外にあるので、ここに書く） */
    static final Map<String, String> INVENTORY = Map.of("en_us", "Inventory", "ja_jp", "持ち物");

    /** ゲームの中で測った幅（font_samples.json）と、フォント画像から計算した幅が全部同じか */
    @GameTest
    public void fontWidthsMatchGame(GameTestHelper h) {
        var samples = readJson("/font_samples.json").getAsJsonObject("samples");
        List<String> bad = new ArrayList<>();
        for (var e : samples.entrySet()) {
            int calc = FontWidths.width(e.getKey()), game = e.getValue().getAsInt();
            if (calc != game) bad.add("\"" + e.getKey() + "\" game " + game + " / calc " + calc);
        }
        LOG.info("[LAB] font widths: {} strings measured in the game, {} differ (IMP-30)", samples.size(), bad.size());
        h.assertTrue(bad.isEmpty(), "font widths differ from the game: " + bad);
        h.succeed();
    }

    @GameTest
    public void guiLayout(GameTestHelper h) {
        List<String> problems = new ArrayList<>(), report = new ArrayList<>();
        Path out = projectRoot().resolve("docs").resolve("gui_preview");
        try { Files.createDirectories(out); } catch (java.io.IOException e) { throw new RuntimeException(e); }
        int screens = 0, texts = 0;
        for (String lang : LANGS) {
            Map<String, String> tr = lang(lang);
            for (Machine<?> m : Machine.all()) {
                if (m.layout == null) continue;
                List<Box> boxes = check(m, lang, tr, problems, report);
                texts += m.layout.texts().size();
                drawBlueprint(m, lang, boxes, out.resolve(m.name + "_" + lang + ".png"));
                screens++;
            }
        }
        if (!FontWidths.UNKNOWN.isEmpty()) problems.add("characters missing from font_widths.json: " + FontWidths.UNKNOWN);
        report.add(0, "# 自動生成：GameTest gui_layout（IMP-30）。画面 " + screens + "（ja・en）、文字の置き場所 " + texts + "、問題 " + problems.size());
        problems.forEach(p -> report.add("PROBLEM " + p));
        try { Files.write(out.resolve("report.txt"), report, StandardCharsets.UTF_8); } catch (java.io.IOException e) { throw new RuntimeException(e); }
        LOG.info("[LAB] gui layout: {} screens (en+ja), {} problems, blueprints in docs/gui_preview (IMP-30)", screens, problems.size());
        problems.forEach(p -> LOG.warn("[LAB] gui layout: {}", p));
        h.assertTrue(problems.isEmpty(), problems.size() + " gui layout problems (see docs/gui_preview/report.txt): " + problems);
        h.succeed();
    }

    /**
     * ツールチップ（IMP-30）：MOD の全部のアイテムで、ja・en、Shift なし・あり。ゲームと同じ TooltipText で行を作り、
     * どの行も TooltipText.WIDTH px 以内か、「省略：」の行が Shift なしで出ていないかを調べる
     */
    @GameTest
    public void tooltipText(GameTestHelper h) {
        List<String> problems = new ArrayList<>(), report = new ArrayList<>();
        int items = 0;
        for (String lang : LANGS) {
            Map<String, String> tr = lang(lang);
            for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
                var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
                if (!id.getNamespace().equals(NS)) continue;
                String key = NS + "." + id.getPath();
                String base = tr.containsKey("item." + key) || !tr.containsKey("block." + key) ? "item." + key : "block." + key;
                for (boolean shift : new boolean[] {false, true}) {
                    var asm = com.taku3.semiconductorlab.machine.Assembly.of(item);
                    var lines = com.taku3.semiconductorlab.machine.TooltipText.lines(base,
                            com.taku3.semiconductorlab.machine.Vessel.of(item) == null ? null : com.taku3.semiconductorlab.machine.Vessel.of(item).langKey(),
                            asm == null ? null : asm.bom(), shift, tr::get, FontWidths::width);
                    for (var l : lines) {
                        int w = FontWidths.width(l.text());
                        if (w > com.taku3.semiconductorlab.machine.TooltipText.WIDTH)
                            problems.add(lang + " " + id.getPath() + (shift ? " (Shift)" : "") + ": a line is " + w + " px (max " + com.taku3.semiconductorlab.machine.TooltipText.WIDTH + "): \"" + l.text() + "\"");
                        if (!shift && com.taku3.semiconductorlab.machine.TooltipText.isOmitted(l.text()))
                            problems.add(lang + " " + id.getPath() + ": the 'omitted' note shows without Shift: \"" + l.text() + "\"");
                    }
                    report.add(lang + " " + id.getPath() + (shift ? " shift" : "") + " lines=" + lines.size());
                }
                if (lang.equals(LANGS[0])) items++;
            }
        }
        report.add(0, "# 自動生成：GameTest tooltip_text（IMP-30）。アイテム " + items + "（ja・en、Shift なし・あり）、問題 " + problems.size());
        problems.forEach(p -> report.add("PROBLEM " + p));
        try { Files.write(projectRoot().resolve("docs").resolve("gui_preview").resolve("tooltip_report.txt"), report, StandardCharsets.UTF_8); } catch (java.io.IOException e) { throw new RuntimeException(e); }
        LOG.info("[LAB] tooltip text: {} items (en+ja, with/without Shift), {} problems (IMP-30)", items, problems.size());
        h.assertTrue(problems.isEmpty(), problems.size() + " tooltip problems (see docs/gui_preview/tooltip_report.txt)");
        h.succeed();
    }

    /** 画面の上の箱1つ（設計図用）：種類・位置・中に描く文字（いちばん長い候補） */
    record Box(String kind, int x, int y, int w, int h, List<String> lines, boolean bad, float scale) {
        Box(String kind, int x, int y, int w, int h, List<String> lines, boolean bad) { this(kind, x, y, w, h, lines, bad, 1f); }
    }

    private static List<Box> check(Machine<?> m, String lang, Map<String, String> tr, List<String> problems, List<String> report) {
        String where = m.name + "_" + lang;
        List<Box> boxes = new ArrayList<>();
        String titleText = tr.getOrDefault("block." + NS + "." + m.name, m.name);
        int tw = FontWidths.width(titleText);
        // 長い題名は画面が縮めて描く（MIN_SCALE まで）。それでも入らなければ問題
        float ts = Math.min(1f, (W - 8) / (float) tw);
        Box title = new Box("title", ts < 1 ? 4 : (W - tw) / 2, 6, Math.round(tw * ts), 9, List.of(titleText), ts < MachineLayout.MIN_SCALE);
        if (title.bad()) problems.add(where + " title \"" + titleText + "\" is " + tw + " px, does not fit " + (W - 8) + " px even shrunk");
        if (ts < 1) report.add(where + " title shrunk to " + ts);
        int iw = FontWidths.width(INVENTORY.get(lang));
        Box inv = new Box("label", 8, invLabelY(m), iw, 9, List.of(INVENTORY.get(lang)), false);
        boxes.add(title);
        boxes.add(inv);
        List<Box> slots = new ArrayList<>();
        for (var s : m.layout.slots()) slots.add(new Box(s.isOutput() ? "out" : "in", s.x() - 1, s.y() - 1, 18, 18, List.of(), false));
        boxes.addAll(slots);
        // 画面のコードが自分で描く場所（方眼など）：枠の中・持ち物の段より上・スロットと重ならない
        List<Box> areas = new ArrayList<>();
        for (MachineLayout.AreaDef a : m.layout.areas()) {
            Box b = new Box("area", a.x(), a.y(), a.width(), a.height(), List.of(), false);
            List<String> place = new ArrayList<>();
            if (b.x() < EDGE || b.x() + b.w() > W - EDGE || b.y() < EDGE || b.y() + b.h() > invTop(m)) place.add("sticks out of the upper part");
            if (overlap(b, title)) place.add("overlaps the title");
            if (overlap(b, inv)) place.add("overlaps the inventory label");
            for (Box sl : slots) if (overlap(b, sl)) place.add("overlaps a slot at (" + (sl.x() + 1) + "," + (sl.y() + 1) + ")");
            for (Box o : areas) if (overlap(b, o)) place.add("overlaps another area");
            for (String pl : place) problems.add(where + " area '" + a.id() + "' " + pl);
            areas.add(place.isEmpty() ? b : new Box("area", b.x(), b.y(), b.w(), b.h(), List.of(), true));
        }
        boxes.addAll(areas);
        List<Box> textBoxes = new ArrayList<>();
        for (MachineLayout.TextDef d : m.layout.texts()) {
            String what = where + " text '" + d.id() + "'";
            List<String> fail = new ArrayList<>();
            List<String> longest = List.of();
            int longestW = -1;
            float longestScale = 1f;
            for (String key : d.keys()) {
                String s = key.startsWith("=") ? key.substring(1) : tr.get(key);
                if (s == null) { fail.add("lang " + key + " is missing in " + lang); continue; }
                s = format(s, d.samples());
                int sw = FontWidths.width(s);
                var fit = TextWrap.fit(s, d.width(), d.lines(), d.shrink(), FontWidths::width);   // 画面と同じ計算
                List<String> lines = fit.lines();
                if (!fit.ok()) fail.add("\"" + s + "\" needs " + lines.size() + " lines at " + d.width() + " px (box has " + d.lines() + (d.shrink() ? ", even at " + MachineLayout.MIN_SCALE + "x" : "") + ")");
                if (sw > longestW) { longestW = sw; longest = lines; longestScale = fit.scale(); }
                report.add(where + " " + d.id() + " " + key + " w=" + sw + " lines=" + lines.size() + "/" + d.lines() + (fit.scale() < 1 ? " scale=" + fit.scale() : ""));
            }
            for (String f : fail) problems.add(what + ": " + f);
            Box b = new Box("text", d.x(), d.y(), d.width(), d.height(), longest, !fail.isEmpty(), longestScale);
            // 枠・題名の帯・持ち物の段
            List<String> place = new ArrayList<>();
            if (b.x() < EDGE || b.x() + b.w() > W - EDGE) place.add("sticks out of the panel (x " + b.x() + "〜" + (b.x() + b.w()) + ", inside " + EDGE + "〜" + (W - EDGE) + ")");
            if (b.y() < EDGE || b.y() + b.h() > invTop(m)) place.add("is outside the upper part (y " + b.y() + "〜" + (b.y() + b.h()) + ", inside " + EDGE + "〜" + invTop(m) + ")");
            if (overlap(b, title)) place.add("overlaps the title \"" + titleText + "\"");
            if (overlap(b, inv)) place.add("overlaps the inventory label");
            for (Box s : slots) if (overlap(b, s)) place.add("overlaps a slot at (" + (s.x() + 1) + "," + (s.y() + 1) + ")");
            for (int i = 0; i < areas.size(); i++) if (overlap(b, areas.get(i))) place.add("overlaps area '" + m.layout.areas().get(i).id() + "'");
            for (int i = 0; i < textBoxes.size(); i++) if (overlap(b, textBoxes.get(i))) place.add("overlaps text '" + m.layout.texts().get(i).id() + "'");
            for (String p : place) problems.add(what + " " + p);
            if (!place.isEmpty()) b = new Box(b.kind(), b.x(), b.y(), b.w(), b.h(), b.lines(), true, b.scale());
            textBoxes.add(b);
        }
        boxes.addAll(textBoxes);
        return boxes;
    }

    private static boolean overlap(Box a, Box b) {
        return a.x() < b.x() + b.w() && b.x() < a.x() + a.w() && a.y() < b.y() + b.h() && b.y() < a.y() + a.h();
    }

    /** lang の %s・%1$s を見本の値で埋める（見本が足りなければ "0"） */
    static String format(String pattern, List<String> samples) {
        Matcher mt = Pattern.compile("%(?:(\\d+)\\$)?([sd%])").matcher(pattern);
        StringBuilder b = new StringBuilder();
        int next = 0;
        while (mt.find()) {
            String rep;
            if (mt.group(2).equals("%")) rep = "%";
            else {
                int i = mt.group(1) != null ? Integer.parseInt(mt.group(1)) - 1 : next++;
                rep = i < samples.size() ? samples.get(i) : "0";
            }
            mt.appendReplacement(b, Matcher.quoteReplacement(rep));
        }
        mt.appendTail(b);
        return b.toString();
    }

    static Map<String, String> lang(String code) {
        Map<String, String> m = new LinkedHashMap<>();
        for (var e : readJson("/assets/" + NS + "/lang/" + code + ".json").entrySet()) m.put(e.getKey(), e.getValue().getAsString());
        return m;
    }

    static com.google.gson.JsonObject readJson(String path) {
        try (var in = GuiLayoutTests.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException(path + " not found");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }

    static Path projectRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("docs").resolve("TECH_TREE.md"))) dir = dir.getParent();
        if (dir == null) throw new IllegalStateException("project root not found");
        return dir;
    }

    // ===== 設計図の画像（3倍の大きさ）：灰色＝画面、白い枠＝スロット（入口は緑・出口は橙）、青＝文字の箱、赤＝問題 =====
    // 文字そのものは描かない（Mojang のフォントも、OS のフォントも画像に入れない）。いちばん長い候補の各行を、計算した幅の濃い棒で描く。文は report.txt

    static final int SC = 3;

    private static void drawBlueprint(Machine<?> m, String lang, List<Box> boxes, Path file) {
        System.setProperty("java.awt.headless", "true");
        int H = H(m), INV_TOP = invTop(m);
        BufferedImage img = new BufferedImage(W * SC, H * SC, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xC6C6C6));
        g.fillRect(0, 0, W * SC, H * SC);
        g.setColor(new Color(0x555555));
        g.setStroke(new BasicStroke(1));
        g.drawRect(EDGE * SC, EDGE * SC, (W - 2 * EDGE) * SC, (INV_TOP - EDGE) * SC);
        // プレイヤーの持ち物（3段＋ホットバー）
        g.setColor(new Color(0x8B8B8B));
        for (int r = 0; r < 4; r++) for (int c = 0; c < 9; c++)
            g.fillRect((7 + c * 18) * SC, (r < 3 ? INV_TOP + r * 18 : H - 25) * SC, 18 * SC - 2, 18 * SC - 2);
        for (Box b : boxes) {
            switch (b.kind()) {
                case "in", "out" -> {
                    g.setColor(b.kind().equals("in") ? new Color(0x4C8C4C) : new Color(0xC07A30));
                    g.fillRect(b.x() * SC, b.y() * SC, b.w() * SC, b.h() * SC);
                    g.setColor(Color.WHITE);
                    g.drawRect(b.x() * SC, b.y() * SC, b.w() * SC - 1, b.h() * SC - 1);
                }
                case "area" -> {   // 画面のコードが描く場所（方眼など）：薄い紫
                    g.setColor(b.bad() ? new Color(255, 80, 80, 110) : new Color(150, 110, 200, 80));
                    g.fillRect(b.x() * SC, b.y() * SC, b.w() * SC, b.h() * SC);
                    g.setColor(b.bad() ? Color.RED : new Color(0x7050A0));
                    g.drawRect(b.x() * SC, b.y() * SC, b.w() * SC - 1, b.h() * SC - 1);
                }
                case "text" -> {
                    g.setColor(b.bad() ? new Color(255, 80, 80, 110) : new Color(80, 120, 255, 70));
                    g.fillRect(b.x() * SC, b.y() * SC, b.w() * SC, b.h() * SC);
                    g.setColor(b.bad() ? Color.RED : new Color(0x3050C0));
                    g.drawRect(b.x() * SC, b.y() * SC, b.w() * SC - 1, b.h() * SC - 1);
                    drawBars(g, b);
                }
                default -> {   // 題名・「持ち物」：文字の範囲だけ
                    g.setColor(b.bad() ? Color.RED : new Color(0x707070));
                    g.drawRect(b.x() * SC, b.y() * SC, b.w() * SC - 1, b.h() * SC - 1);
                    drawBars(g, b);
                }
            }
        }
        g.dispose();
        try { javax.imageio.ImageIO.write(img, "png", file.toFile()); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }

    /** 各行の幅の棒（文字の代わり）。縮めた文は縮めた大きさで */
    private static void drawBars(Graphics2D g, Box b) {
        g.setColor(new Color(0x303030));
        for (int i = 0; i < b.lines().size(); i++) {
            float lw = FontWidths.width(b.lines().get(i)) * b.scale(), top = b.y() + (i * MachineLayout.LINE + 2) * b.scale();
            g.fillRect(Math.round(b.x() * SC), Math.round(top * SC), Math.round(lw * SC), Math.max(1, Math.round(5 * b.scale() * SC)));
        }
    }}
