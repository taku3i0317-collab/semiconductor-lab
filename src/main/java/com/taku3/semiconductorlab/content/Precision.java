package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.item.*;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineBlock;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.taku3.semiconductorlab.machine.Machine.lit;
import static com.taku3.semiconductorlab.machine.Machine.metal;
import static com.taku3.semiconductorlab.machine.Machine.stone;

/**
 * 幹8 精密機械・計測（docs/TECH_TREE.md の「幹8」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/precision/、src/gametest/.../PrecisionTests.java）だけを書き換える。
 */
public final class Precision {
    private Precision() {}

    // ===== アイテム =====
    public static final Item LEAD_SCREW            = Reg.item("lead_screw");
    // [8-01] 旋盤で削った部品（IMP2-01）
    // [8-10] 光学顕微鏡（IMP2-04）：単レンズ（レーウェンフック 1670年代、NA 0.2⚠️）と複式（色消し対物、NA 0.9）。見分けられる線 d ＝ λ ÷ 2NA
    public static final Item SIMPLE_MICROSCOPE     = Reg.item("simple_microscope", p -> new MicroscopeItem(p, 0.2), new Item.Properties().stacksTo(1));
    public static final Item COMPOUND_MICROSCOPE   = Reg.item("compound_microscope", p -> new MicroscopeItem(p, 0.9), new Item.Properties().stacksTo(1));
    // [8-02] ねじ切りと互換性部品（IMP2-05）：旋盤の親ねじの入口に送りねじを入れて鋼を削る → 同じねじ山の規格ねじ（ホイットワース 1841：山の角度 55°）
    public static final Item STANDARD_SCREW        = Reg.item("standard_screw");
    /** 削った部品の寸法の誤差（µm、+ は大きすぎ）。旋盤のくせで決まる。ノギス・マイクロメータ [8-04] で測る（IMP2-05） */
    public static final net.minecraft.core.component.DataComponentType<Integer> PART_ERROR_UM = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("part_error_um"),
            net.minecraft.core.component.DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    // [8-04] ノギスとマイクロメータ（IMP2-05）：段階1 ノギス（副尺 1631、青銅、読み 0.05mm）→ 段階2 マイクロメータ（パルマー 1848、規格ねじ、読み 0.01mm）
    public static final Item VERNIER_CALIPER       = Reg.item("vernier_caliper", p -> new GaugeItem(p, 50), new Item.Properties().stacksTo(1));
    public static final Item MICROMETER            = Reg.item("micrometer", p -> new GaugeItem(p, 10), new Item.Properties().stacksTo(1));
    public static final Item PUMP_ROTOR            = Reg.item("pump_rotor");        // 回転ポンプ [5-04] の偏心ローター（鋼）

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 旋盤 [8-01]（IMP2-01、モーズリー 1797〜1800頃）：モーターで材料を回し、送りねじの刃物台で削る。750W
    public static final Machine<LatheBlockEntity> LATHE = Machine.electric("lathe", LatheBlockEntity::new, LatheBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
