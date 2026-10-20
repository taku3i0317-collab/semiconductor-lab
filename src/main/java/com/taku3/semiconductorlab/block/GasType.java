package com.taku3.semiconductorlab.block;

import com.taku3.semiconductorlab.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ガスホルダーに貯められるガスの種類。1単位 = 袋詰めガス1個 = 約1mol。
 * CHLORINE は [6-05] クロルアルカリ電解（タスク09）。NITROGEN / ARGON は [6-07] 空気分離（タスク12）。
 * 順番（ordinal）を GUI 同期とセーブに使わないよう、セーブは name() で行う。
 */
public enum GasType {
    NONE(null, null, "none"),
    HYDROGEN(() -> ModItems.HYDROGEN_GAS, () -> ModItems.HYDROGEN_CYLINDER, "hydrogen"),
    OXYGEN(() -> ModItems.OXYGEN_GAS, () -> ModItems.OXYGEN_CYLINDER, "oxygen"),
    CHLORINE(() -> ModItems.CHLORINE_GAS, () -> ModItems.CHLORINE_CYLINDER, "chlorine"),
    NITROGEN(() -> ModItems.NITROGEN_GAS, () -> ModItems.NITROGEN_CYLINDER, "nitrogen"),
    ARGON(() -> ModItems.ARGON_GAS, () -> ModItems.ARGON_CYLINDER, "argon");

    private final java.util.function.Supplier<Item> bag;
    private final java.util.function.Supplier<Item> cylinder;
    public final String key;

    GasType(java.util.function.Supplier<Item> bag, java.util.function.Supplier<Item> cylinder, String key) { this.bag = bag; this.cylinder = cylinder; this.key = key; }

    /** 袋詰めガスのアイテム（NONE は null） */
    public Item bagItem() { return bag == null ? null : bag.get(); }

    /** 高圧ボンベ（[6-03] 圧縮機で 50mol 詰めたもの）のアイテム（NONE は null） */
    public Item cylinderItem() { return cylinder == null ? null : cylinder.get(); }

    /** アイテムが充填済みボンベならその種類、違えば NONE */
    public static GasType ofCylinder(ItemStack s) {
        for (GasType t : values()) if (t.cylinder != null && s.is(t.cylinder.get())) return t;
        return NONE;
    }

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
     * 水素＋塩素＝塩素爆鳴気（H2+Cl2→2HCl、光や火花で連鎖反応して爆発。1809 ゲイ=リュサックとテナールが日光で爆発することを発見）。
     * 違う種類を同じホルダーに入れたときに呼ぶ。酸素＋塩素は反応しない（混ざるだけ＝入れられない）。窒素・アルゴンは何とも反応しない。
     */
    public static boolean explosive(GasType a, GasType b) {
        return pair(a, b, HYDROGEN, OXYGEN) || pair(a, b, HYDROGEN, CHLORINE);
    }

    private static boolean pair(GasType a, GasType b, GasType x, GasType y) { return (a == x && b == y) || (a == y && b == x); }
}
