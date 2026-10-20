package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 生ゴム [2-29]（加硫していない天然ゴム）。暑い所（バイオームの気温 HOT 以上：ジャングル・砂漠など）では、べたべたに柔らかくなり、
 * 寒い所（COLD 以下：雪の地方）では硬くなる。名前が「べたついた生ゴム」「硬くなった生ゴム」に変わる（持ち物の中で CHECK_TICKS ごとに調べる）。
 * 加硫ゴム（硫黄で鎖を橋かけ）は、暑くても寒くても変わらない＝グッドイヤーの発見（1839）を比べる実験。
 */
public class RawRubberItem extends Item {
    public static final float HOT = 0.95f, COLD = 0.15f;
    public static final int CHECK_TICKS = 100;

    public enum State { NORMAL, STICKY, STIFF }

    public RawRubberItem(Properties properties) { super(properties); }

    public static State stateFor(float temperature) {
        if (temperature >= HOT) return State.STICKY;
        if (temperature <= COLD) return State.STIFF;
        return State.NORMAL;
    }

    public static State stateOf(ItemStack stack) {
        Component name = stack.get(DataComponents.ITEM_NAME);
        if (name == null || !(name.getContents() instanceof TranslatableContents t)) return State.NORMAL;
        return t.getKey().endsWith(".sticky") ? State.STICKY : t.getKey().endsWith(".stiff") ? State.STIFF : State.NORMAL;
    }

    /** 気温に合わせて名前（状態）を変える。変わったら true（テストからも呼ぶ） */
    public static boolean applyTemperature(ItemStack stack, float temperature) {
        State want = stateFor(temperature);
        if (stateOf(stack) == want) return false;
        switch (want) {
            case STICKY -> stack.set(DataComponents.ITEM_NAME, Component.translatable("item.semiconductor-lab.raw_rubber.sticky"));
            case STIFF -> stack.set(DataComponents.ITEM_NAME, Component.translatable("item.semiconductor-lab.raw_rubber.stiff"));
            default -> stack.remove(DataComponents.ITEM_NAME);
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % CHECK_TICKS != 0) return;
        float t = level.getBiome(entity.blockPosition()).value().getBaseTemperature();
        if (applyTemperature(stack, t) && entity instanceof Player p) {
            State s = stateOf(stack);
            p.sendOverlayMessage(Component.translatable("message.semiconductor-lab.raw_rubber." + s.name().toLowerCase()));
        }
    }
}
