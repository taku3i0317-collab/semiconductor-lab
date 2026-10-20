package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.client.MachineScreen;
import com.taku3.semiconductorlab.machine.Machine;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraft.client.gui.screens.MenuScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 本物の画面の見た目の確認（IMP-30 ③、最後の見た目確認）。クライアント（画面つきのゲーム）を起動して、全部の機械の画面を英語・日本語で開き、
 * 画面の部分のスクリーンショットを見本（src/gametest/resources/templates/&lt;機械&gt;_&lt;言語&gt;.png）と比べる。
 * 見本がないときは、その回の画像を見本として書き出す（GitHub の自動ビルドの成果物 screen-checks から取ってきて commit する）。
 * 文字のはみ出し・重なりは、ゲームなしの GameTest gui_layout（GuiLayoutTests）が先に調べる。
 * ウィンドウが開くので手元では実行しない（runClientGameTest は GitHub の自動ビルドが画面なし＝xvfb で実行する）。
 */
public class ScreenChecks implements FabricClientGameTest {
    private static final Logger LOG = LoggerFactory.getLogger("semiconductor-lab-gametest");
    private static final String[] LANGS = {"en_us", "ja_jp"};
    /** 見本と比べるときの許し（平均二乗誤差。文字のにじみ程度は通す） */
    private static final float MSE = 0.005f;

    @Override
    public void runTest(ClientGameTestContext ctx) {
        List<String> problems = new ArrayList<>();
        int screens = 0, newTemplates = 0;
        try (TestSingleplayerContext world = ctx.worldBuilder().setUseConsistentSettings(true).create()) {
            ctx.waitFor(mc -> mc.player != null && mc.level != null);
            ctx.waitTicks(20);
            for (String lang : LANGS) {
                setLanguage(ctx, lang);
                for (Machine<?> m : Machine.all()) {
                    if (m.menu == null) continue;
                    ctx.runOnClient(mc -> MenuScreens.create(m.menu, mc, 100, m.block.getName()));
                    ctx.waitTicks(3);
                    String name = m.name + "_" + lang;
                    int[] region = ctx.computeOnClient(mc -> {
                        if (!(mc.gui.screen() instanceof MachineScreen s)) return null;
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
                    if (!hadTemplate) newTemplates++;
                    ctx.runOnClient(mc -> mc.player.closeContainer());
                    ctx.waitTicks(1);
                    screens++;
                }
            }
        }
        setLanguage(ctx, "en_us");
        LOG.info("[LAB] screen checks: {} screens (en+ja), {} differ from templates, {} new templates (IMP-30)", screens, problems.size(), newTemplates);
        problems.forEach(p -> LOG.warn("[LAB] screen checks: {}", p));
        if (!problems.isEmpty()) throw new AssertionError("screens differ from their templates: " + problems);
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
}
