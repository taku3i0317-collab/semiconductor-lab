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

    // [1-34] GeCl4 蒸留経由のゲルマニウムか（true なら帯域精製で 10N まで、なければ硫酸経由として 9N まで）。還元→精製で受け継ぐ
    public static final DataComponentType<Boolean> CHLORIDE_ROUTE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("chloride_route"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // [9-11] ドーパント（"boron"＝p型 / "phosphorus"＝n型）。シリコン単結晶 [9-08] に付く。Dopant.byId で元素に戻す
    public static final DataComponentType<String> DOPANT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("dopant"),
            DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // [9-15] 熱酸化の膜の厚さ（nm）。酸化膜つきの板に付く。Deal–Grove の式で育つ（IMP4-01）
    public static final DataComponentType<Integer> OXIDE_NM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("oxide_nm"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 100_000))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    // 露光とマスク（O-002、docs/LITHO_DESIGN.md §4）：設計図・マスク・加工中のウェハーの中身
    public static final DataComponentType<com.taku3.semiconductorlab.litho.ChipDesign> CHIP_DESIGN = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("chip_design"),
            DataComponentType.<com.taku3.semiconductorlab.litho.ChipDesign>builder()
                    .persistent(com.taku3.semiconductorlab.litho.ChipDesign.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(com.taku3.semiconductorlab.litho.ChipDesign.CODEC))
                    .build());
    public static final DataComponentType<com.taku3.semiconductorlab.litho.MaskData> MASK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("mask"),
            DataComponentType.<com.taku3.semiconductorlab.litho.MaskData>builder()
                    .persistent(com.taku3.semiconductorlab.litho.MaskData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(com.taku3.semiconductorlab.litho.MaskData.CODEC))
                    .build());
    public static final DataComponentType<com.taku3.semiconductorlab.litho.ChipState> CHIP_STATE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("chip_state"),
            DataComponentType.<com.taku3.semiconductorlab.litho.ChipState>builder()
                    .persistent(com.taku3.semiconductorlab.litho.ChipState.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(com.taku3.semiconductorlab.litho.ChipState.CODEC))
                    .build());

    // [9-17] メサトランジスタの電流増幅率 β（むき出しの接合が湿気で劣化して下がる、IMP4-01）
    public static final DataComponentType<Integer> CURRENT_GAIN = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("current_gain"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 1000))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void initialize() {}
}