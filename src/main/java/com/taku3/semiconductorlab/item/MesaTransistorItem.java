package com.taku3.semiconductorlab.item;

import com.taku3.semiconductorlab.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * メサトランジスタ [9-17]（IMP4-01）：接合が台地（メサ）の側面に**むき出し**なので、空気中の水分やイオンが付くと漏れ電流が増え、
 * 電流増幅率（β）が下がっていく。持ち歩いていると {@link #DECAY_INTERVAL} tick ごとに β が 1 下がり、雨や水の中では 5 下がる。
 * β が 0 になると漏れが大きすぎてトランジスタとして働かない。これを止めたのが酸化膜で接合を守るプレーナ技術 [9-18]（1959）。
 * ⚠️ 下がる速さはゲームの値（本物は湿度・温度で数日〜数か月。mesa_transistor.placeholder）。
 */
public class MesaTransistorItem extends Item {
    public static final int FRESH_GAIN = 40, DECAY_INTERVAL = 1200, RAIN_DECAY = 5;

    public MesaTransistorItem(Properties properties) { super(properties.component(ModDataComponents.CURRENT_GAIN, FRESH_GAIN)); }

    public static int gainOf(ItemStack s) { return s.getOrDefault(ModDataComponents.CURRENT_GAIN, 0); }

    /** 1回ぶん劣化させる（雨・水の中なら大きく）。0 より下にはならない */
    public static void decay(ItemStack s, boolean wet) {
        int g = gainOf(s);
        if (g > 0) s.set(ModDataComponents.CURRENT_GAIN, Math.max(0, g - (wet ? RAIN_DECAY : 1)));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (level.getGameTime() % DECAY_INTERVAL == 0) decay(stack, entity.isInWaterOrRain());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        int g = gainOf(stack);
        lines.accept(Component.translatable(g > 0 ? "tooltip.semiconductor-lab.mesa.gain" : "tooltip.semiconductor-lab.mesa.dead", g)
                .withStyle(g > FRESH_GAIN / 2 ? ChatFormatting.GREEN : g > 0 ? ChatFormatting.YELLOW : ChatFormatting.RED));
        lines.accept(Component.translatable("tooltip.semiconductor-lab.mesa.exposed").withStyle(ChatFormatting.GRAY));
    }
}
