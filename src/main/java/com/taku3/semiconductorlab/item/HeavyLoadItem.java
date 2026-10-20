package com.taku3.semiconductorlab.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 石材の荷 [0-14]（切り石をロープでしばった重い荷）。持ち物に入っていると、歩くのが遅くなる（荷の数が多いほど遅い、最大 MAX_LEVEL 段）。
 * 舟や乗り物に乗っている間は、重さを舟と水が支えるので遅くならない。丸木舟の荷物入れに積めば、持たずに運べる。
 */
public class HeavyLoadItem extends Item {
    /** 鈍化の段の上限（荷 1つ＝1段、4つ以上＝4段） */
    public static final int MAX_LEVEL = 4;
    /** 効果を付け直す長さ（tick） */
    public static final int REFRESH = 40;

    public HeavyLoadItem(Properties properties) { super(properties); }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player p) burden(p);
    }

    /** 持っている荷の数ぶん遅くする。乗り物に乗っていれば何もしない。付けた段（0＝なし）を返す（テストからも呼ぶ） */
    public static int burden(Player p) {
        if (p.isPassenger() || p.isCreative() || p.isSpectator()) return 0;
        int loads = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.getItem() instanceof HeavyLoadItem) loads += s.getCount();
        }
        if (loads == 0) return 0;
        int level = Math.min(loads, MAX_LEVEL);
        p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, REFRESH, level - 1, false, false, true));
        return level;
    }
}
