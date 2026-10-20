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

    // [B-08] 電卓（S-010）：このチップ（ウェハー）が検査台で合格したお題の id。電卓の組み立てに「加算器・レジスタ・カウンタに合格したチップ」が要る
    public static final DataComponentType<java.util.List<String>> CHALLENGES = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("challenges"),
            DataComponentType.<java.util.List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.fromCodec(Codec.STRING.listOf()))
                    .build());

    // [9-35] プローバー（O-017）：ウェハーのチップごとの良否（ビット i＝チップ i が良品）。ダイシングで印の付いたチップは捨てる
    public static final DataComponentType<Integer> DIE_MAP = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("die_map"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
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

    // [9-09] シリコン結晶の酸素の量（ppma）。FZ は 0.05、付いていない単結晶は CZ（石英るつぼから溶けこむ、約15）として表示する。切った板に受け継ぐ（IMP4-01）
    public static final DataComponentType<Double> OXYGEN_PPMA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            SemiconductorLab.id("oxygen_ppma"),
            DataComponentType.<Double>builder()
                    .persistent(Codec.doubleRange(0.0, 1000.0))
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    public static void initialize() {}
}