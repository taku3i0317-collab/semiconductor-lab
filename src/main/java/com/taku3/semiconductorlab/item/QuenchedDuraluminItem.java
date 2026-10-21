package com.taku3.semiconductorlab.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * 焼入れしたジュラルミン [14-13]（IMP2 S-010、1906 ヴィルム）。Al に Cu 約4%・Mg を入れて焼入れした直後はやわらかいが、
 * 部屋の温度で放っておくと、細かい析出物ができて数日で硬くなる（時効硬化）。持ち物に入れておくと {@value #AGE_TICKS} tick（1日⚠️、本物は約4日）で
 * 硬いジュラルミン（agedItem）に変わる。右クリックで今の硬さ（ビッカース硬さ HV）。
 */
public class QuenchedDuraluminItem extends Item {
    public static final long AGE_TICKS = 24_000;
    /** 焼入れ直後と時効のあとの硬さ（HV、⚠️ 代表値） */
    public static final int HV_SOFT = 60, HV_AGED = 120;
    private final java.util.function.Supplier<Item> aged;

    public QuenchedDuraluminItem(Properties properties, java.util.function.Supplier<Item> aged) { super(properties); this.aged = aged; }

    /** 時効の進み（0〜1）から硬さ */
    public static int hardness(long ageTicks) {
        double f = 1 - Math.exp(-3.0 * Math.max(0, ageTicks) / AGE_TICKS);
        return (int) Math.round(HV_SOFT + (HV_AGED - HV_SOFT) * f);
    }

    public static long quenchedAt(ItemStack s, long now) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? now : d.copyTag().getLongOr("QuenchedAt", now);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % 20 != 0 || !(entity instanceof Player p)) return;
        tryAge(p, stack, level.getGameTime());
    }

    /** 焼入れの時刻を記録し、{@value #AGE_TICKS} tick たっていれば持ち物の中で硬いジュラルミンに替える。替えたら true */
    public boolean tryAge(Player p, ItemStack stack, long now) {
        CustomData d = stack.get(DataComponents.CUSTOM_DATA);
        if (d == null || !d.copyTag().contains("QuenchedAt")) {
            CompoundTag t = new CompoundTag();
            t.putLong("QuenchedAt", now);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
            return false;
        }
        if (now - quenchedAt(stack, now) < AGE_TICKS) return false;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++)
            if (inv.getItem(i) == stack) { inv.setItem(i, new ItemStack(aged.get(), stack.getCount())); return true; }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            long now = level.getGameTime(), age = now - quenchedAt(p.getItemInHand(hand), now);
            p.sendSystemMessage(Component.translatable("item.semiconductor-lab.quenched_duralumin.hardness", hardness(age), HV_AGED,
                    String.format(java.util.Locale.ROOT, "%.1f", age / 24000.0)));
        }
        return InteractionResult.SUCCESS;
    }
}
