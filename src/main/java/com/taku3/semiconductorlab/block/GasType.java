package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ガスホルダーに貯められるガスの種類。1単位 = 袋詰めガス1個 = 約1mol。
 * 将来ここに CHLORINE（[6-05]）、NITROGEN / ARGON（[6-07]空気分離）を足す。
 * 順番（ordinal）を GUI 同期とセーブに使わないよう、セーブは name() で行う。
 */
public enum GasType {
    NONE(null, "none"),
    HYDROGEN(() -> ModItems.HYDROGEN_GAS, "hydrogen"),
    OXYGEN(() -> ModItems.OXYGEN_GAS, "oxygen");

    private final java.util.function.Supplier<Item> bag;
    public final String key;

    GasType(java.util.function.Supplier<Item> bag, String key) { this.bag = bag; this.key = key; }

    /** 袋詰めガスのアイテム（NONE は null） */
    public Item bagItem() { return bag == null ? null : bag.get(); }

    /** アイテムが袋詰めガスならその種類、違えば NONE */
    public static GasType of(ItemStack s) {
        for (GasType t : values()) if (t.bag != null && s.is(t.bag.get())) return t;
        return NONE;
    }

    public static GasType byName(String name) {
        for (GasType t : values()) if (t.name().equals(name)) return t;
        return NONE;
    }

    public static GasType byIndex(int i) { return i >= 0 && i < values().length ? values()[i] : NONE; }

    /**
     * 混ぜると危ない組み合わせか。水素＋酸素＝爆鳴気（2H2+O2→2H2O、火花1つで一気に反応）。
     * 違う種類を同じホルダーに入れたときに呼ぶ。今は「燃える気体＋酸素」だけ。
     */
    public static boolean explosive(GasType a, GasType b) {
        return (a == HYDROGEN && b == OXYGEN) || (a == OXYGEN && b == HYDROGEN);
    }
}
