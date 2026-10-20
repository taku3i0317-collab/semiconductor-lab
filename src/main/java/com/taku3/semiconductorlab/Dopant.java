package com.taku3.semiconductorlab;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * ドーパント源 [9-11]。シリコンにごく少し混ぜて、電気の流れ方（p型・n型）を決める元素。
 * ホウ素（III族、手が3本）→ 正孔ができて p型。原料は三酸化二ホウ素 B2O3 [1-33]（本物でも固体の拡散源）。
 * リン（V族、手が5本）→ 電子が余って n型。原料は白リン（v0.1 のリン）。
 * k＝偏析係数：結晶が固まるとき、融液の濃さに対して結晶に入る割合。B は約0.8（上から下までほぼ均一）、P は約0.35
 * （融液に残りやすく、インゴットの下ほど濃くなる）。値は米国特許 US 5,073,229 ほか（SOURCES.md）。
 * ヒ素・アンチモンは前提 [1-04]・[1-43] が未実装なのでまだない。
 */
public enum Dopant {
    BORON("boron", 'p', 0.80),
    PHOSPHORUS("phosphorus", 'n', 0.35);

    public final String id;
    public final char type;
    public final double k;

    Dopant(String id, char type, double k) { this.id = id; this.type = type; this.k = k; }

    /** そのドーパントの原料アイテム */
    public Item source() { return this == BORON ? ModItems.BORON_TRIOXIDE : ModItems.PHOSPHORUS; }

    /** アイテムがドーパント源なら、その元素。違えば null */
    public static Dopant of(ItemStack s) {
        for (Dopant d : values()) if (s.is(d.source())) return d;
        return null;
    }

    public static Dopant byId(String id) {
        for (Dopant d : values()) if (d.id.equals(id)) return d;
        return null;
    }
}
