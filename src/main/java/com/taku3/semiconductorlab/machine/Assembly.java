package com.taku3.semiconductorlab.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * 組立台のレシピ（IMP-33、DESIGN「組立台」）。部品表（BOM＝製品を作る部品の一覧）で組み立てる。
 * レシピは製品の幹のファイルに Java で書く：
 * <pre>
 * public static final Assembly RADIO = Assembly.recipe("transistor_radio", TRANSISTOR_RADIO)
 *         .part("amplify", Semiconductor.JUNCTION_TRANSISTOR, 3)   // 役目 → lang assembly.semiconductor-lab.transistor_radio.amplify
 *         .seconds(5).build();
 * </pre>
 * 役目1つ＝組立台のスロット1つ（多くて {@link #MAX_PARTS} 個）。数がそろって初めて「組み立てる」が押せる。
 */
public final class Assembly {
    /** 組立台の部品のスロットの数 */
    public static final int MAX_PARTS = 8;

    /** 役目1つ：名前（lang の頭）・入れてよい物・要る数・表示用のアイテム */
    public record Part(String role, Predicate<ItemStack> ok, int count, Item display) {}

    private static final List<Assembly> ALL = new ArrayList<>();

    public final String id;
    public final Item result;
    public final List<Part> parts;
    public final int ticks;

    private Assembly(String id, Item result, List<Part> parts, int ticks) {
        this.id = id; this.result = result; this.parts = List.copyOf(parts); this.ticks = ticks;
    }

    public static Builder recipe(String id, Item result) { return new Builder(id, result); }

    /** 登録順（＝content の幹の順）の一覧 */
    public static List<Assembly> all() { return Collections.unmodifiableList(ALL); }
    public static Assembly get(int i) { return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }
    public static Assembly of(Item result) { return ALL.stream().filter(a -> a.result == result).findFirst().orElse(null); }

    /** 役目の説明の lang キー（例 assembly.semiconductor-lab.transistor_radio.amplify） */
    public String roleKey(Part p) { return "assembly.semiconductor-lab." + id + "." + p.role(); }
    /** 製品の名前の lang キー */
    public String resultKey() {
        var k = BuiltInRegistries.ITEM.getKey(result);
        return "item." + k.getNamespace() + "." + k.getPath();
    }

    /** スロット i の役目（なければ null） */
    public Part part(int slot) { return slot >= 0 && slot < parts.size() ? parts.get(slot) : null; }

    /** スロットの中身がその役目の条件と数を満たすか */
    public boolean satisfied(int slot, ItemStack s) {
        Part p = part(slot);
        return p != null && !s.isEmpty() && p.ok().test(s) && s.getCount() >= p.count();
    }

    /** 自動チェック用：全部のレシピの製品と役目の lang キー */
    public static List<String> resultKeys() { return ALL.stream().map(Assembly::resultKey).toList(); }
    public static List<String> roleKeys() { return ALL.stream().flatMap(a -> a.parts.stream().map(a::roleKey)).distinct().toList(); }

    public static final class Builder {
        private final String id;
        private final Item result;
        private final List<Part> parts = new ArrayList<>();
        private int ticks = 100;

        private Builder(String id, Item result) { this.id = id; this.result = result; }

        /** 役目1つ（その1種類のアイテムを count 個） */
        public Builder part(String role, Item item, int count) { return part(role, s -> s.is(item), count, item); }
        /** 役目1つ（条件つき：純度・n型など） */
        public Builder part(String role, Predicate<ItemStack> ok, int count, Item display) {
            parts.add(new Part(role, ok, count, display));
            return this;
        }
        public Builder seconds(int s) { ticks = s * 20; return this; }

        public Assembly build() {
            if (parts.isEmpty() || parts.size() > MAX_PARTS) throw new IllegalStateException(id + ": 1.." + MAX_PARTS + " parts");
            Assembly a = new Assembly(id, result, parts, ticks);
            ALL.add(a);
            return a;
        }
    }
}
