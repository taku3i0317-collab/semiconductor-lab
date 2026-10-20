package com.taku3.semiconductorlab.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 容器の種類（IMP-33、DESIGN「容器のしくみ」）。中身入りのアイテムは、入っている容器を1つ決めて登録する（content/Reg.contained）。
 * - 作業台で使うと空の容器が戻る（Item.Properties.craftRemainder）
 * - 機械で使ったら Vessel.emptyOf で空の容器を返す（出口がなければ機械の上に出す）
 * - 相性表（ALLOWED）にない組み合わせは GameTest vessels が見つける（例：フッ酸×ガラス瓶＝ガラスが溶ける）
 */
public enum Vessel {
    /** 袋（ゴム引き布）。レシピには出さない（CHEMISTRY #33） */
    GAS_BAG(() -> null, 64),
    /** 鋼のボンベ（[6-03]、約150気圧） */
    STEEL_CYLINDER(() -> com.taku3.semiconductorlab.content.Gas.EMPTY_GAS_CYLINDER, 16),
    /** ガラス瓶（[2-14] ソーダ石灰ガラス）。硫酸・塩酸 */
    GLASS_BOTTLE(() -> com.taku3.semiconductorlab.content.Chemistry.GLASS_BOTTLE, 16),
    /** 水を張ったガラス瓶。白リン（空気中で約30℃で自然発火するので水の中に保管） */
    WATER_JAR(() -> com.taku3.semiconductorlab.content.Chemistry.GLASS_BOTTLE, 16),
    /** 鉄のフラスコ。水銀（本物は 76 ポンド＝約 34.5kg 入りが取引の単位） */
    IRON_FLASK(() -> com.taku3.semiconductorlab.content.Metallurgy.IRON_FLASK, 16),
    /** 鉛の瓶。フッ酸（ガラスを溶かすので、昔は鉛・ろう・ガタパーチャの瓶。今はポリエチレン） */
    LEAD_BOTTLE(() -> com.taku3.semiconductorlab.content.Metallurgy.LEAD_BOTTLE, 16);

    private final Supplier<Item> empty;
    /** 1スロットに重なる数 */
    public final int stack;

    Vessel(Supplier<Item> empty, int stack) { this.empty = empty; this.stack = stack; }

    /** 空の容器のアイテム（袋は null） */
    public Item empty() { return empty.get(); }

    /** lang のキー（容器の名前。ツールチップの「容器：…」） */
    public String langKey() { return "vessel.semiconductor-lab." + name().toLowerCase(); }

    // ===== 中身入りのアイテム → 容器 =====

    private static final Map<Item, Vessel> BY_ITEM = new HashMap<>();

    /** Reg.contained から呼ぶ */
    public static void register(Item filled, Vessel v) { BY_ITEM.put(filled, v); }

    /** このアイテムの容器（容器に入っていない物は null） */
    public static Vessel of(Item item) { return BY_ITEM.get(item); }
    public static Map<Item, Vessel> all() { return Map.copyOf(BY_ITEM); }

    /** 中身を1つ使ったあとに残る空の容器（容器なし・袋なら EMPTY） */
    public static ItemStack emptyOf(ItemStack used) {
        Vessel v = of(used.getItem());
        return v == null || v.empty() == null ? ItemStack.EMPTY : new ItemStack(v.empty());
    }

    /** 機械が中身を n 個使ったとき：空の容器を機械の上に出す（容器なし・袋なら何もしない）。中身を減らす前に呼ぶ */
    public static void returnEmpty(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, ItemStack used, int n) {
        ItemStack e = emptyOf(used);
        if (e.isEmpty() || level == null || level.isClientSide()) return;
        e.setCount(n);
        net.minecraft.world.level.block.Block.popResource(level, pos.above(), e);
    }

    /** 手に持った中身を1つ使ったとき：空の容器をプレイヤーに返す（持ち物がいっぱいなら足もとに落とす） */
    public static void returnEmpty(net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos pos, ItemStack used) {
        ItemStack e = emptyOf(used);
        if (e.isEmpty() || player.getAbilities().instabuild) return;
        if (!player.getInventory().add(e)) net.minecraft.world.level.block.Block.popResource(player.level(), pos.above(), e);
    }

    // ===== 相性表：中身の名前（アイテムの ID）→ 入れてよい容器 =====

    /** ここに載っていない中身はどの容器でもよい。載っていれば、この中からだけ */
    public static final Map<String, Set<Vessel>> ALLOWED = Map.of(
            "hydrofluoric_acid", Set.of(LEAD_BOTTLE),          // HF は SiO2 を溶かす（4HF + SiO2 → SiF4 + 2H2O）。ガラス瓶は不可
            "phosphorus", Set.of(WATER_JAR),                   // 白リンは空気に触れさせない
            "mercury", Set.of(IRON_FLASK, GLASS_BOTTLE),       // 水銀は鉄を溶かさない（アマルガムを作らない）。銅・亜鉛・金の容器は不可
            "sulfuric_acid", Set.of(GLASS_BOTTLE),
            "hydrochloric_acid", Set.of(GLASS_BOTTLE));         // 塩酸は鉄を溶かす（Fe + 2HCl → FeCl2 + H2）
}
