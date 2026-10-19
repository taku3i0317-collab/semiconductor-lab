package com.taku3.semiconductorlab;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataComponents {

    // 純度：0.0〜1.0 の小数（0.999999 = 6N）。float では桁が足りないので double
    public static final DataComponentType<Double> PURITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("purity"),
            DataComponentType.<Double>builder()
                    .persistent(Codec.doubleRange(0.0, 1.0))
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    // 鉛蓄電池 [4-07] の残量（J）。壊してもアイテムに残り、置き直すと戻る（タスク06）
    public static final DataComponentType<Long> BATTERY_ENERGY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("battery_energy"),
            DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    public static void initialize() {}
}