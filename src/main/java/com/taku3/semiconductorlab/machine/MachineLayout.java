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

    private final List<SlotDef> slots;
    private final int dataCount;
    private final String texture;

    private MachineLayout(List<SlotDef> slots, int dataCount, String texture) {
        this.slots = List.copyOf(slots);
        this.dataCount = dataCount;
        this.texture = texture;
    }

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
        public MachineLayout build() { return new MachineLayout(slots, dataCount, texture); }
    }
}
