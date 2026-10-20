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
    public static final Item PUMP_ROTOR            = Reg.item("pump_rotor");        // 回転ポンプ [5-04] の偏心ローター（鋼）

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 旋盤 [8-01]（IMP2-01、モーズリー 1797〜1800頃）：モーターで材料を回し、送りねじの刃物台で削る。750W
    public static final Machine<LatheBlockEntity> LATHE = Machine.electric("lathe", LatheBlockEntity::new, LatheBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
