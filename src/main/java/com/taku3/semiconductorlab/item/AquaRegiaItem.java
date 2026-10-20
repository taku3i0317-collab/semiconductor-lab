package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.content.Chemistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 王水 [2-22]：濃塩酸3＋濃硝酸1。混ぜるとすぐに塩化ニトロシル NOCl と塩素が出はじめ、それが金を溶かす力になる。
 * でも気体はどんどん逃げていくので、作り置きはできない：持ち歩いていると、1秒ごとに {@link #DECAY_CHANCE} の確率で
 * スタックごと「力を失った王水」に変わる（平均で約5分）。
 * ⚠️ チェストの中では変わらない（持っている間だけ時間を数える近似、aqua_regia.placeholder）。
 */
public class AquaRegiaItem extends Item {
    public static final float DECAY_CHANCE = 1f / 300f;

    public AquaRegiaItem(Properties properties) { super(properties); }

    /** 力を失わせる（テストからも呼ぶ）：同じ数の「力を失った王水」をプレイヤーに渡し、このスタックを消す */
    public static void decay(ItemStack stack, Player p) {
        int n = stack.getCount();
        stack.setCount(0);
        ItemStack spent = new ItemStack(Chemistry.SPENT_AQUA_REGIA, n);
        if (!p.getInventory().add(spent) && !spent.isEmpty()) p.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(p.level(), p.getX(), p.getY(), p.getZ(), spent));
        p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.aqua_regia.decayed"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % 20 != 0 || !(entity instanceof Player p)) return;
        RandomSource r = level.getRandom();
        if (r.nextFloat() < DECAY_CHANCE) decay(stack, p);
    }
}
