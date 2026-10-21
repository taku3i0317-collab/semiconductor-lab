package com.taku3.semiconductorlab.gametest;

import com.taku3.semiconductorlab.content.Control;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;

/**
 * GameTest：幹10 制御・計算（IMP4-01 O-011 で作った）。共通の道具は {@link LabTestBase}。
 */
public class ControlTests extends LabTestBase {

    /** [10-01] 電磁石：鉄の芯＋銅線（伸線 [1-58]）を巻く */
    @GameTest
    public void electromagnet(GameTestHelper h) {
        h.assertTrue(recipeUses(h, "electromagnet", Items.IRON_INGOT) && recipeUses(h, "electromagnet", com.taku3.semiconductorlab.content.Metallurgy.COPPER_WIRE), "electromagnet = iron core + copper wire coil");
        h.assertTrue(new net.minecraft.world.item.ItemStack(Control.ELECTROMAGNET).getCount() == 1, "item exists");
        LOG.info("[LAB] electromagnet [10-01]: iron core + copper wire coil");
        h.succeed();
    }

    /**
     * [10-13] 4ビットのマイコン（S-014 Q25-5 案A）：組立台のレシピは cpu4 と rom_program に合格したチップ・水晶・ニキシー管など6種。
     * 合格していないチップは CPU にも ROM にもなれない。見本のプログラムは「LOAD 3・ADD 5・OUT・JUMP 6」で、出力の口は 8
     */
    @GameTest
    public void microcomputer(GameTestHelper h) {
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Control.MICROCOMPUTER);
        h.assertTrue(a != null && a.parts.size() == 6, "microcomputer assembly with 6 roles");
        net.minecraft.world.item.ItemStack plain = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP);
        net.minecraft.world.item.ItemStack cpu = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP); cpu.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("cpu4"));
        net.minecraft.world.item.ItemStack rom = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP); rom.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("rom_program"));
        h.assertFalse(a.satisfied(0, plain), "a plain chip is not a CPU");
        h.assertTrue(a.satisfied(0, cpu) && !a.satisfied(0, rom), "the CPU slot takes a chip that passed cpu4");
        h.assertTrue(a.satisfied(1, rom), "the ROM slot takes a chip that passed rom_program");
        h.assertValueEqual(com.taku3.semiconductorlab.litho.Cpu4.listing(com.taku3.semiconductorlab.litho.Cpu4.ADD_3_5), "LOAD 3・ADD 5・OUT・JUMP 6", "program listing");
        h.succeed();
    }

    /**
     * [10-16] の準備（S-019 Q30 全部案A）：①掛け算器の見本（AND 16・全加算器 12 の箱）がお題 mul4（256通り）に合格
     * ②自作の CC0 データ（1200＋300）で学習した 64→16→10 のネットは正答率 90% 以上、4ビットにしても 85% 以上
     * ③4ビットの推論の掛け算を mul4 の箱でしても、Java の掛け算と同じ答え ④学び直し：まちがえた見本は、少し学ぶと正しくなる
     * ⑤同梱の重み digit_weights.json は、同梱のデータからいつでも作り直せる（ちがえば build/digit_weights.json に書き出して落ちる）
     */
    @GameTest(maxTicks = 400)
    public void aiDigitsAndMul4(GameTestHelper h) {
        var mul4 = com.taku3.semiconductorlab.litho.Challenge.byId("mul4");
        var roles = new java.util.HashMap<Integer, com.taku3.semiconductorlab.litho.Circuit.Role>();
        com.taku3.semiconductorlab.litho.LithoExamples.mul4Boxes(roles);
        var r = com.taku3.semiconductorlab.litho.Challenge.checkCombinational(mul4, () -> com.taku3.semiconductorlab.litho.LithoExamples.mul4Boxes(new java.util.HashMap<>()), roles);
        h.assertTrue(r.passed(), "the array multiplier of boxes passes mul4: " + r);
        var net = com.taku3.semiconductorlab.ai.DigitNet.trained();
        var test = com.taku3.semiconductorlab.ai.DigitNet.bundled("test");
        int okF = 0; for (var s : test) if (net.predictFloat(s.x()) == s.label()) okF++;
        double accF = okF / (double) test.size();
        h.assertTrue(accF >= 0.90, "float accuracy " + accF);
        var q = net.quantize(com.taku3.semiconductorlab.ai.DigitNet.bundled("train"));
        var count = new com.taku3.semiconductorlab.ai.DigitNet.Counter();
        int okQ = 0; for (var s : test) if (com.taku3.semiconductorlab.ai.DigitNet.predict(q, s.x(), (a, b) -> a * b, count) == s.label()) okQ++;
        double accQ = okQ / (double) test.size();
        h.assertTrue(accQ >= 0.85, "4-bit accuracy " + accQ);
        var sim = com.taku3.semiconductorlab.litho.LithoExamples.mul4Boxes(new java.util.HashMap<>());
        java.util.function.IntBinaryOperator chipMul = (a, b) -> {   // mul4 の箱で掛ける（入力A〜D＝a、E〜H＝b）
            var in = new java.util.EnumMap<com.taku3.semiconductorlab.litho.Circuit.Role, Boolean>(com.taku3.semiconductorlab.litho.Circuit.Role.class);
            for (int k = 0; k < 4; k++) { in.put(com.taku3.semiconductorlab.litho.Circuit.INS[k], (a >> k & 1) != 0); in.put(com.taku3.semiconductorlab.litho.Circuit.INS[4 + k], (b >> k & 1) != 0); }
            return com.taku3.semiconductorlab.litho.LithoExamples.mul4Boxes(new java.util.HashMap<>()).step(roles, in, 8);
        };
        for (int k = 0; k < 10; k++)
            h.assertValueEqual(com.taku3.semiconductorlab.ai.DigitNet.predict(q, test.get(k).x(), chipMul, null), com.taku3.semiconductorlab.ai.DigitNet.predict(q, test.get(k).x(), (a, b) -> a * b, null), "chip multiplier gives the same answer for sample " + k);
        int[] odd = new int[64]; odd[0] = odd[7] = odd[56] = odd[63] = 1;   // 四すみだけ
        int target = (net.predictFloat(odd) + 1) % 10;
        net.learn(odd, target, 40);
        h.assertValueEqual(net.predictFloat(odd), target, "after a little learning it answers as taught");
        String fresh = com.taku3.semiconductorlab.ai.DigitNet.trained().toJson();
        String shipped = null;
        try (var in = com.taku3.semiconductorlab.ai.DigitNet.class.getResourceAsStream("/data/semiconductor-lab/ai/digit_weights.json")) { if (in != null) shipped = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).replace("\r\n", "\n"); } catch (java.io.IOException e) { shipped = null; }
        if (!fresh.equals(shipped)) {
            java.nio.file.Path dir = java.nio.file.Paths.get("").toAbsolutePath();   // プロジェクトの根（docs/TECH_TREE.md のある所）の build/ へ
            while (dir != null && !java.nio.file.Files.exists(dir.resolve("docs").resolve("TECH_TREE.md"))) dir = dir.getParent();
            if (dir != null) try { java.nio.file.Files.writeString(dir.resolve("build").resolve("digit_weights.json"), fresh); } catch (java.io.IOException ignored) {}
        }
        h.assertTrue(fresh.equals(shipped), "bundled digit_weights.json matches a fresh training (copy build/digit_weights.json to src/main/resources/data/semiconductor-lab/ai/ if this fails)");
        LOG.info("[LAB] AI [10-16] prep: mul4 passes, float {} 4-bit {}, {} multiplies for {} digits", accF, accQ, count.multiplies, test.size());
        h.succeed();
    }

    /**
     * [10-32] 数値演算チップ（O-032 Q31 案A 仮決定）：0.1＋0.2 は 64ビットで 0.3000000000000000444…（0.3 ではない）、32ビット・16ビットでもずれる。
     * 組立台は cpu8・mul4・レジスタに合格したチップ＋配線＋板
     */
    @GameTest
    public void mathCoprocessor(GameTestHelper h) {
        String[] s = com.taku3.semiconductorlab.item.MathCoprocessorItem.sums();
        h.assertTrue(s[2].startsWith("0.3000000000000000444"), "double 0.1 + 0.2 = " + s[2]);
        for (String v : s) h.assertFalse(new java.math.BigDecimal(v).compareTo(new java.math.BigDecimal("0.3")) == 0, "never exactly 0.3: " + v);
        h.assertValueEqual(com.taku3.semiconductorlab.item.MathCoprocessorItem.bits()[2], "3FD3333333333334", "64-bit pattern of 0.1 + 0.2");
        var a = com.taku3.semiconductorlab.machine.Assembly.of(com.taku3.semiconductorlab.content.Control.MATH_COPROCESSOR);
        h.assertTrue(a != null && a.parts.size() == 5, "math coprocessor assembly with 5 roles");
        var chip = new net.minecraft.world.item.ItemStack(com.taku3.semiconductorlab.content.Semiconductor.CHIP);
        chip.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of("mul4"));
        h.assertTrue(a.satisfied(1, chip) && !a.satisfied(0, chip), "mul4 chip is the multiplier, not the CPU");
        h.succeed();
    }

    /**
     * [10-04] フィードバック制御（O-037、Q36-1 案A 仮決定）：遠心調速機を付けた蒸気機関は、電気を使う物が無いとき回りすぎ（蓄えがあふれそう）を見て弁をしぼり、
     * 石炭がゆっくり減る。付けていない機関は全開のまま（あふれた電気は捨てる）。調速機は2個目が付かない
     */
    @GameTest(maxTicks = 100)
    public void centrifugalGovernor(GameTestHelper h) {
        net.minecraft.core.BlockPos a = p(1, 1, 1), b = p(4, 1, 1);
        h.setBlock(a, com.taku3.semiconductorlab.content.Power.STEAM_ENGINE.block);
        h.setBlock(b, com.taku3.semiconductorlab.content.Power.STEAM_ENGINE.block);
        var plain = h.getBlockEntity(a, com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.class);
        var gov = h.getBlockEntity(b, com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.class);
        h.assertTrue(gov.installGovernor() && !gov.installGovernor(), "one governor only");
        for (var e : new com.taku3.semiconductorlab.energy.SteamEngineBlockEntity[] {plain, gov}) {
            e.getContainer().setItem(com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.SLOT_COAL, new net.minecraft.world.item.ItemStack(Items.COAL, 2));
            e.getContainer().setItem(com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.SLOT_WATER, new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET));
        }
        tickGenerator(h, a, plain, 400);
        tickGenerator(h, b, gov, 400);
        int speed = com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.speedPercent(gov.fill());
        h.assertTrue(plain.getBurnTicks() < 250, "without a governor the coal burns at full rate: " + plain.getBurnTicks());
        h.assertTrue(gov.getBurnTicks() > plain.getBurnTicks() + 150, "the governor saves coal: " + gov.getBurnTicks() + " vs " + plain.getBurnTicks());
        h.assertTrue(speed >= 95 && speed <= 112, "the governor holds the speed near the 100% target: " + speed);
        h.assertTrue(com.taku3.semiconductorlab.energy.SteamEngineBlockEntity.speedPercent(plain.fill()) >= 118, "the plain engine overspeeds");
        LOG.info("[LAB] governor [10-04]: burn left {} vs {} ticks, speed {}% (target 100)", gov.getBurnTicks(), plain.getBurnTicks(), speed);
        h.succeed();
    }
}
