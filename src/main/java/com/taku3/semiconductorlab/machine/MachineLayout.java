package com.taku3.semiconductorlab.machine;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 機械の画面の「形」：どのスロットが画面のどこにあり、何を入れられるか（IMP-28）。
 * 機械ごとの Menu クラスの代わりに、BE に1つ書くだけでよい。
 * <pre>
 * public static final MachineLayout LAYOUT = MachineLayout.builder(ConsumerBlockEntity.DATA_COUNT)
 *         .in(SLOT_IN, 8, 35, MyBlockEntity::isInput)
 *         .out(SLOT_OUT, 78, 35)
 *         .build();
 * </pre>
 * 1つの BE に入っているアイテムの数（containerSize）は、いちばん大きいスロット番号＋1。
 */
public final class MachineLayout {
    /** スロット1つ。accepts が null なら出口（入れられない）。maxStack が 0 ならふつうの上限 */
    public record SlotDef(int index, int x, int y, Predicate<ItemStack> accepts, int maxStack) {
        public boolean isOutput() { return accepts == null; }
    }

    // ===== 画面の文字（IMP-30）：文字もスロットと同じく「どこに・どの幅で・何行まで」をデータで決める =====

    /** 1行の高さ（文字 9px ＋ すき間 1px）と、反応式を縮めてよい下限 */
    public static final int LINE = 10;
    public static final float MIN_SCALE = 0.6f;

    public enum Align { LEFT, CENTER, RIGHT }

    /** よく使う置き場所：右の列（出口のスロット x=78 の右）と、下の専用の行（「持ち物」の右、y=72） */
    public static final int RIGHT_X = 97, RIGHT_W = 75, BOTTOM_X = 62, BOTTOM_Y = 72, BOTTOM_W = 110;

    /**
     * 文字の置き場所1つ。箱＝(x, y) から 幅 width × lines 行。画面のコードは id ごとに「今どの文を出すか」だけを返す。
     * @param keys    出るかもしれない文の lang キー（全部の候補。自動チェックが ja・en の全部を測る）。「=」で始まるものは翻訳しない文字そのもの
     * @param samples 文の中の %s に入る値の見本（例 "500.0 kW"）。チェックで使う
     * @param shrink  入らないときは縮めてよい（反応式の行。MIN_SCALE まで）。false なら折り返して lines 行に入らないとだめ
     */
    public record TextDef(String id, int x, int y, int width, int lines, Align align, boolean shrink, List<String> keys, List<String> samples) {
        /** 箱の高さ（px） */
        public int height() { return (lines - 1) * LINE + 9; }
    }

    /** lang キーの頭をまとめて書く：keys("gui.semiconductor-lab.arc_furnace.", "need_input", "running") */
    public static String[] keys(String prefix, String... names) {
        String[] out = new String[names.length];
        for (int i = 0; i < names.length; i++) out[i] = prefix + names[i];
        return out;
    }

    private final List<SlotDef> slots;
    private final List<TextDef> texts;
    private final int dataCount;
    private final String texture;

    private MachineLayout(List<SlotDef> slots, List<TextDef> texts, int dataCount, String texture) {
        this.slots = List.copyOf(slots);
        this.texts = List.copyOf(texts);
        this.dataCount = dataCount;
        this.texture = texture;
    }

    public List<TextDef> texts() { return texts; }

    /** @param dataCount GUI へ送る数値の個数（ConsumerBlockEntity なら DATA_COUNT ＋ 追加の数） */
    public static Builder builder(int dataCount) { return new Builder(dataCount); }

    public List<SlotDef> slots() { return slots; }
    public int dataCount() { return dataCount; }
    /** 画面の背景 textures/gui/&lt;texture&gt;.png。null なら機械の名前と同じ */
    public String texture() { return texture; }
    public int containerSize() { return slots.stream().mapToInt(SlotDef::index).max().orElse(-1) + 1; }

    public static final class Builder {
        private final List<SlotDef> slots = new ArrayList<>();
        private final int dataCount;
        private String texture;

        private Builder(int dataCount) { this.dataCount = dataCount; }

        /** 入口のスロット（accepts が true の物だけ入る。シフトクリックもここへ） */
        public Builder in(int index, int x, int y, Predicate<ItemStack> accepts) { return in(index, x, y, accepts, 0); }
        /** 入口のスロット（1スロットに入る数の上限つき。例：帯域精製炉の Ge は 8 個まで） */
        public Builder in(int index, int x, int y, Predicate<ItemStack> accepts, int maxStack) {
            slots.add(new SlotDef(index, x, y, accepts, maxStack));
            return this;
        }
        /** 出口のスロット（取り出すだけ） */
        public Builder out(int index, int x, int y) {
            slots.add(new SlotDef(index, x, y, null, 0));
            return this;
        }
        /** 背景の画像を別の機械と共有するとき（蒸気タービンは蒸気機関の画面） */
        public Builder texture(String name) { texture = name; return this; }

        private final List<TextDef> texts = new ArrayList<>();

        /** 左寄せの文（幅 width で折り返し、lines 行まで） */
        public Builder text(String id, int x, int y, int width, int lines, String... keys) { return add(id, x, y, width, lines, Align.LEFT, false, keys); }
        /** 真ん中寄せの文（箱の中で真ん中） */
        public Builder center(String id, int x, int y, int width, int lines, String... keys) { return add(id, x, y, width, lines, Align.CENTER, false, keys); }
        /** 反応式の行（1行・右寄せ。入らなければ縮める） */
        public Builder formula(String id, int x, int y, int width, String... keys) { return add(id, x, y, width, 1, Align.RIGHT, true, keys); }
        /** 短い名札（翻訳しない文字。例「SO2」「H2」）。幅はその文字の幅 */
        public Builder label(String id, int x, int y, int width, String literal) { return add(id, x, y, width, 1, Align.LEFT, false, "=" + literal); }
        /** ふつうの画面（SimpleMachineScreen）の3つ：電力・蓄積・状態（右の列 x=100） */
        public Builder standardTexts() {
            String k = "gui.semiconductor-lab.machine.";
            return text("power", 100, 18, 72, 1, k + "power").samples("500.0 kW")
                    .text("stored", 100, 30, 72, 1, k + "stored").samples("50 kJ")
                    .text("hint", 100, 42, 72, 3, k + "need_input", k + "need_power", k + "running");
        }
        /** 直前の文を「折り返しても入らなければ縮める」にする（混んだ画面だけ。MIN_SCALE まで） */
        public Builder fit() {
            TextDef t = texts.remove(texts.size() - 1);
            texts.add(new TextDef(t.id(), t.x(), t.y(), t.width(), t.lines(), t.align(), true, t.keys(), t.samples()));
            return this;
        }
        /** 右の列（x=97・幅75）の行。y は 17・27・38… */
        public Builder right(String id, int y, int lines, String... keys) { return text(id, RIGHT_X, y, RIGHT_W, lines, keys); }
        /** 下の専用の行（「持ち物」と同じ高さの右、幅110。反応式・モードなど。入らなければ縮める） */
        public Builder bottom(String id, String... keys) { return formula(id, BOTTOM_X, BOTTOM_Y, BOTTOM_W, keys); }
        /** 直前の文の %s に入る値の見本 */
        public Builder samples(String... values) {
            TextDef t = texts.remove(texts.size() - 1);
            texts.add(new TextDef(t.id(), t.x(), t.y(), t.width(), t.lines(), t.align(), t.shrink(), t.keys(), List.of(values)));
            return this;
        }
        private Builder add(String id, int x, int y, int width, int lines, Align a, boolean shrink, String... keys) {
            texts.add(new TextDef(id, x, y, width, lines, a, shrink, List.of(keys), List.of()));
            return this;
        }

        public MachineLayout build() { return new MachineLayout(slots, texts, dataCount, texture); }
    }
}
