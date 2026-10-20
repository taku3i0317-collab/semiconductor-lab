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
 * 幹15 通信・情報（docs/TECH_TREE.md の「幹15」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/communication/、src/gametest/.../CommunicationTests.java）だけを書き換える。
 */
public final class Communication {
    private Communication() {}

    // ===== アイテム =====
    public static final Item TRANSISTOR_RADIO      = Reg.item("transistor_radio", new Item.Properties().stacksTo(1));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}

    /** 組立台のレシピ（IMP-33。Content.initialize が全部の幹のあとで呼ぶ） */
    static void assemblies() {
        // [15-10] トランジスタラジオ：合金接合型トランジスタ3（増幅）＋炭素抵抗＋同調コイル＋同調コンデンサ＋スピーカー＋乾電池（前は作業台）
        com.taku3.semiconductorlab.machine.Assembly.recipe("transistor_radio", TRANSISTOR_RADIO)
                .part("amplify", Semiconductor.JUNCTION_TRANSISTOR, 3)
                .part("resistor", Metallurgy.GRAPHITE, 1)
                .part("tuning_coil", Power.COPPER_CABLE.block.asItem(), 1)
                .part("tuning_capacitor", Power.LEYDEN_JAR, 1)
                .part("speaker", Power.MAGNET, 1)
                .part("battery_case", Metallurgy.ZINC_INGOT, 1)
                .part("battery_rod", Power.GRAPHITE_ROD, 1)
                .seconds(5).build();
    }
}
