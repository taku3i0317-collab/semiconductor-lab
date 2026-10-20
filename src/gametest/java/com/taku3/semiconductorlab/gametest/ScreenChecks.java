package com.taku3.semiconductorlab.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taku3.semiconductorlab.client.MachineScreen;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineLayout;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 画面の自動チェック（IMP-30 ②③）。クライアント（画面つきのゲーム）を起動して、全部の機械の画面を英語・日本語で開く。
 * - ② 文字：描いた文字が画面の枠からはみ出さない／スロット・ほかの文字・題名・「持ち物」に重ならない。
 *       描かれていない文の候補（同じ画面の gui.semiconductor-lab.<名前>.* の全部のキー）も、その列に入る幅かを測る
 * - ③ 見た目：画面の部分のスクリーンショットを見本（src/gametest/resources/templates/&lt;機械&gt;_&lt;言語&gt;.png）と比べる。
 *       見本がないときは、その回の画像を見本として書き出す（GitHub の自動ビルドの成果物から取ってきて commit する）
 * ウィンドウが開くので手元では実行しない（runClientGameTest は GitHub の自動ビルドが画面なし＝xvfb で実行する）。
 * 結果は実行フォルダの screen_checks.txt と、ログの [LAB] 行。
 */
public class ScreenChecks implements FabricClientGameTest {
    private static final Logger LOG = LoggerFactory.getLogger("semiconductor-lab-gametest");
    private static final String NS = "semiconductor-lab";
    private static final String[] LANGS = {"en_us", "ja_jp"};
    /** 画面の内側の縁（px）と、持ち物の段の上の端 */
    private static final int EDGE = 4, INV_TOP = 83, LINE_H = 9;
    /** 描かれていない文の幅を測るときの、%s の代わりの文字（よくある長さ） */
    private static final String SAMPLE_ARG = "0.0 kW";
    /** 見本と比べるときの許し（平均二乗誤差。文字のにじみ程度は通す） */
    private static final float MSE = 0.005f;
    /**
     * わかっていて、まだ直していない文字の問題（lang を直したら消す）。
     * NET-06（ネットワーク担当が lang の部品を直している間）は lang を触らないので、見つかったものはここに書いて記録する
     */
    private static final Set<String> KNOWN = Set.of();

    @Override
    public void runTest(ClientGameTestContext ctx) {
        List<String> problems = new ArrayList<>();
        List<String> report = new ArrayList<>();
        int screens = 0, newTemplates = 0;
        try (TestSingleplayerContext world = ctx.worldBuilder().setUseConsistentSettings(true).create()) {
            ctx.waitFor(mc -> mc.player != null && mc.level != null);
            ctx.waitTicks(20);
            for (String lang : LANGS) {
                setLanguage(ctx, lang);
                Map<String, String> strings = langStrings(ctx, lang);
                for (Machine<?> m : Machine.all()) {
                    if (m.menu == null) continue;
                    List<MachineScreen.TextDraw> draws = new ArrayList<>();
                    ctx.runOnClient(mc -> {
                        MachineScreen.recordTextInto(draws);
                        MenuScreens.create(m.menu, mc, 100, m.block.getName());
                    });
                    ctx.waitTicks(3);
                    String name = m.name + "_" + lang;
                    int[] region = ctx.computeOnClient(mc -> {
                        MachineScreen.recordTextInto(null);
                        if (!(mc.gui.screen() instanceof MachineScreen s)) return null;
                        checkTexts(m, s, new ArrayList<>(draws), strings, lang, problems, report);
                        int sc = mc.getWindow().getGuiScale();
                        return new int[] {s.panelLeft() * sc, s.panelTop() * sc, s.panelWidth() * sc, s.panelHeight() * sc};
                    });
                    if (region == null) { problems.add(name + ": the screen did not open"); continue; }
                    boolean hadTemplate = getClass().getResource("/templates/" + name + ".png") != null;
                    try {
                        ctx.assertScreenshotEquals(TestScreenshotComparisonOptions.of(name).withRegion(region[0], region[1], region[2], region[3])
                                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(MSE)).save());
                    } catch (Throwable t) {
                        if (hadTemplate) problems.add(name + ": looks different from templates/" + name + ".png (" + t.getMessage() + ")");
                    }
                    if (!hadTemplate) { newTemplates++; report.add("new template (written to src/gametest/resources/templates): " + name); }
                    ctx.runOnClient(mc -> mc.player.closeContainer());
                    ctx.waitTicks(1);
                    screens++;
                }
            }
        }
        setLanguage(ctx, "en_us");
        report.add(0, "screens: " + screens + ", problems: " + problems.size() + ", new templates: " + newTemplates);
        problems.forEach(p -> report.add("PROBLEM " + p));
        Path out = ctx.computeOnClient(mc -> mc.gameDirectory.toPath().resolve("screen_checks.txt"));
        try { Files.write(out, report, StandardCharsets.UTF_8); } catch (java.io.IOException e) { throw new RuntimeException(e); }
        LOG.info("[LAB] screen checks: {} screens (en+ja), {} problems, {} new templates, report {} (IMP-30)", screens, problems.size(), newTemplates, out);
        problems.forEach(p -> LOG.warn("[LAB] screen checks: {}", p));
        List<String> unknown = problems.stream().filter(p -> KNOWN.stream().noneMatch(p::startsWith)).toList();
        if (!unknown.isEmpty()) throw new AssertionError("screen checks found " + unknown.size() + " problems: " + unknown);
    }

    /** 言語を切り替えて、読み込みが終わるまで待つ */
    private static void setLanguage(ClientGameTestContext ctx, String code) {
        var f = ctx.computeOnClient(mc -> {
            if (code.equals(mc.getLanguageManager().getSelected())) return null;
            mc.options.languageCode = code;
            mc.getLanguageManager().setSelected(code);
            return mc.reloadResourcePacks();
        });
        if (f != null) ctx.waitFor(mc -> f.isDone() && mc.gui.overlay() == null, 20 * 120);
        ctx.waitTicks(2);
    }

    /** MOD の lang（ゲームが読んだもの）をそのまま読む */
    private static Map<String, String> langStrings(ClientGameTestContext ctx, String code) {
        return ctx.computeOnClient(mc -> {
            Map<String, String> map = new HashMap<>();
            var res = mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath(NS, "lang/" + code + ".json"));
            if (res.isPresent()) try (var r = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                for (var e : o.entrySet()) map.put(e.getKey(), e.getValue().getAsString());
            } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
            return map;
        });
    }

    private static void checkTexts(Machine<?> m, MachineScreen s, List<MachineScreen.TextDraw> draws, Map<String, String> strings,
                                   String lang, List<String> problems, List<String> report) {
        String where = m.name + "_" + lang;
        int w = s.panelWidth();
        var font = s.font();
        // 題名（真ん中）と「持ち物」（左下）の場所
        int titleW = font.width(m.block.getName());
        int[] title = {(w - titleW) / 2, 6, titleW, LINE_H};
        int[] invLabel = {8, s.panelHeight() - 94, font.width(Component.translatable("container.inventory")), LINE_H};
        List<int[]> slots = new ArrayList<>();
        for (MachineLayout.SlotDef d : m.layout.slots()) slots.add(new int[] {d.x() - 1, d.y() - 1, 18, 18});
        Map<String, Integer> allowed = new HashMap<>();   // 文のキーの頭 → その列に入る幅
        for (int i = 0; i < draws.size(); i++) {
            var d = draws.get(i);
            int[] r = {d.x(), d.y(), d.width(), LINE_H};
            String what = where + " \"" + d.text().getString() + "\"";
            if (d.x() < EDGE || d.x() + d.width() > w - EDGE) problems.add(what + " sticks out (x " + d.x() + "〜" + (d.x() + d.width()) + ", panel 0〜" + w + ")");
            if (d.y() < EDGE || d.y() + LINE_H > INV_TOP) problems.add(what + " is outside the upper part (y " + d.y() + ")");
            if (overlap(r, title)) problems.add(what + " overlaps the title");
            if (overlap(r, invLabel)) problems.add(what + " overlaps the inventory label");
            for (int[] sl : slots) if (overlap(r, sl)) { problems.add(what + " overlaps a slot at (" + (sl[0] + 1) + "," + (sl[1] + 1) + ")"); break; }
            for (int j = i + 1; j < draws.size(); j++) {
                var e = draws.get(j);
                if (overlap(r, new int[] {e.x(), e.y(), e.width(), LINE_H})) problems.add(what + " overlaps \"" + e.text().getString() + "\"");
            }
            if (d.text().getContents() instanceof TranslatableContents tc && tc.getKey().startsWith("gui." + NS + ".")) {
                String prefix = tc.getKey().substring(0, tc.getKey().lastIndexOf('.') + 1);
                boolean centered = Math.abs(d.x() * 2 + d.width() - w) <= 2;
                int room = centered ? w - 2 * EDGE : w - EDGE - d.x();
                allowed.merge(prefix, room, Math::min);
            }
            report.add(where + " text \"" + d.text().getString() + "\" x=" + d.x() + " y=" + d.y() + " w=" + d.width());
        }
        // まだ描かれていない文（状態で切り替わる表示）も、同じ列に入るか
        for (var e : allowed.entrySet()) {
            for (String key : strings.keySet()) {
                if (!key.startsWith(e.getKey())) continue;
                Object[] args = {SAMPLE_ARG, SAMPLE_ARG, SAMPLE_ARG, SAMPLE_ARG};
                int tw = font.width(Component.translatable(key, args));
                if (tw > e.getValue()) problems.add(where + " lang " + key + " is " + tw + " px wide, room " + e.getValue() + " px (\"" + Component.translatable(key, args).getString() + "\")");
            }
        }
    }

    private static boolean overlap(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }
}
