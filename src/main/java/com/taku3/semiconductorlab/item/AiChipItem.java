package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.ai.DigitNet;
import com.taku3.semiconductorlab.litho.ChipState;
import com.taku3.semiconductorlab.litho.Circuit;
import com.taku3.semiconductorlab.litho.LogicSim;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI チップ [B-11]（v1.0、MOD の最終目的 [10-16] の部品、S-019 Q30 全部案A）：EUV と GAA で作った掛け算器（お題 mul4 に合格）4つ＋数値演算チップ＋DRAM を組立台で。
 * 中では本物の小さなニューラルネット（{@link DigitNet}、64→16→10）が、8×8 に書いた数字を読む。掛け算はみな 4ビット×4ビットで、
 * お題 mul4 に合格した掛け算器の動き（論理の箱）で1回ずつ計算する。プレイヤーが教えた数字（最大 {@link #MAX_SAMPLES}）でネットを少し学び直す（学習は浮動小数点＝数値演算チップ）。
 * 書く画面の見た目はユーザーの答え待ち（それまで右クリックは見本の数字を読んでみせる）
 */
public class AiChipItem extends Item {
    /** 中の掛け算器の数（組立台のレシピと同じ）。1クロックにこの数だけ掛け算できる */
    public static final int UNITS = 4;
    public static final int MAX_SAMPLES = 32, LEARN_STEPS = 20;

    public AiChipItem(Properties p) { super(p); }

    /** AI チップの掛け算器になれるチップか：mul4 に合格し、EUV の方眼（GAA の細かさ）で GAA にして作った */
    public static boolean euvMultiplier(ItemStack s) {
        if (!com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "mul4")) return false;
        ChipState st = s.get(ModDataComponents.CHIP_STATE);
        return st != null && st.sized() && st.size() >= Circuit.GAA_GRID && st.extras().gaa();
    }

    /** 答え：数字・出力の点数（0〜9）・隠れ層の光り方（0〜15）・掛け算の回数 */
    public record Answer(int digit, long[] scores, int[] hidden, long multiplies) {
        public long clocks() { return (multiplies + UNITS - 1) / UNITS; }
    }

    private static DigitNet base;
    private static DigitNet.Quantized baseQ;
    private static List<DigitNet.Sample> calib;

    private static synchronized void load() {
        if (base != null) return;
        base = DigitNet.bundledWeights();
        if (base == null) base = DigitNet.trained();
        calib = DigitNet.bundled("train");
        baseQ = base.quantize(calib);
    }

    /** このチップのネット（同梱の重み＋このチップで教えた数字で学び直したもの）を4ビットに */
    public static DigitNet.Quantized network(ItemStack chip) {
        load();
        List<String> samples = chip.getOrDefault(ModDataComponents.AI_SAMPLES, List.of());
        if (samples.isEmpty()) return baseQ;
        DigitNet n = copy(base);
        for (String s : samples) { int[] x = decode(s); n.learn(x, label(s), LEARN_STEPS); }
        return n.quantize(calib);
    }

    private static DigitNet copy(DigitNet b) {
        DigitNet n = new DigitNet();
        for (int j = 0; j < DigitNet.HID; j++) { n.b1[j] = b.b1[j]; System.arraycopy(b.w1[j], 0, n.w1[j], 0, DigitNet.IN); }
        for (int o = 0; o < DigitNet.OUT; o++) { n.b2[o] = b.b2[o]; System.arraycopy(b.w2[o], 0, n.w2[o], 0, DigitNet.HID); }
        return n;
    }

    /** 掛け算器の箱（お題 mul4 の動き）：入力A〜D＝a、E〜H＝b → 出力1〜8 */
    private static final Map<Integer, Circuit.Role> MUL_ROLES = new HashMap<>();
    private static final LogicSim MUL;
    static {
        Map<Circuit.Role, Integer> pins = new EnumMap<>(Circuit.Role.class);
        pins.put(Circuit.Role.GROUND, 0); pins.put(Circuit.Role.VCC, 1);
        MUL_ROLES.put(0, Circuit.Role.GROUND); MUL_ROLES.put(1, Circuit.Role.VCC);
        for (int k = 0; k < 8; k++) { pins.put(Circuit.INS[k], 2 + k); MUL_ROLES.put(2 + k, Circuit.INS[k]); pins.put(Circuit.OUTS[k], 10 + k); MUL_ROLES.put(10 + k, Circuit.OUTS[k]); }
        MUL = LogicSim.wired(List.of(new LogicSim.Wire("mul4", pins)));
    }

    /** 掛け算器の箱で a×b（0〜15） */
    public static synchronized int chipMultiply(int a, int b) {
        Map<Circuit.Role, Boolean> in = new EnumMap<>(Circuit.Role.class);
        for (int k = 0; k < 4; k++) { in.put(Circuit.INS[k], (a >> k & 1) != 0); in.put(Circuit.INS[4 + k], (b >> k & 1) != 0); }
        return MUL.step(MUL_ROLES, in, 8);
    }

    /** 書いた 8×8（64個の 0/1）を読む */
    public static Answer recognize(ItemStack chip, int[] x) {
        DigitNet.Quantized q = network(chip);
        DigitNet.Counter c = new DigitNet.Counter();
        int d = DigitNet.predict(q, x, AiChipItem::chipMultiply, c);
        return new Answer(d, new long[0], new int[0], c.multiplies);
    }

    /** 教える（このチップに見本を足す。古いものから消える） */
    public static void teach(ItemStack chip, int[] x, int label) {
        List<String> s = new ArrayList<>(chip.getOrDefault(ModDataComponents.AI_SAMPLES, List.of()));
        s.add(encode(x, label));
        while (s.size() > MAX_SAMPLES) s.remove(0);
        chip.set(ModDataComponents.AI_SAMPLES, List.copyOf(s));
    }

    /** 見本の形："16けたの16進（64マス）:答え" */
    static String encode(int[] x, int label) { long b = 0; for (int i = 0; i < 64; i++) if (x[i] != 0) b |= 1L << i; return String.format(java.util.Locale.ROOT, "%016x:%d", b, label); }
    static int[] decode(String s) { long b = Long.parseUnsignedLong(s.substring(0, 16), 16); int[] x = new int[64]; for (int i = 0; i < 64; i++) x[i] = (int) (b >> i & 1); return x; }
    static int label(String s) { return Integer.parseInt(s.substring(17)); }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {   // 書く画面ができるまで：同梱の見本（テスト用の数字）を1つ読んでみせる
            var test = DigitNet.bundled("test");
            var s = test.get(level.getRandom().nextInt(test.size()));
            Answer a = recognize(p.getItemInHand(hand), s.x());
            p.sendSystemMessage(Component.translatable("message.semiconductor-lab.ai_chip.demo", s.label(), a.digit(), a.multiplies(), a.clocks()));
        }
        return InteractionResult.SUCCESS;
    }
}
