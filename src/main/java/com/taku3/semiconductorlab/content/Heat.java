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
 * 幹3 熱（炉と温度）（docs/TECH_TREE.md の「幹3」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/heat/、src/gametest/.../HeatTests.java）だけを書き換える。
 */
public final class Heat {
    private Heat() {}

    // ===== アイテム =====
    public static final Item ZINC_OXIDE            = Reg.item("zinc_oxide");    // [3-06] 閃亜鉛鉱を反射炉で焙焼した ZnO（亜鉛華）
    // [3-25] カーバイド（炭化カルシウム、A、D のお願い）：アーク炉で CaO + 3C → CaC2 + CO（1892）。水をかけるとアセチレン（D の幹2）
    public static final Item CALCIUM_CARBIDE       = Reg.item("calcium_carbide");
    public static final Item SYNTHETIC_GRAPHITE    = Reg.item("synthetic_graphite"); // [3-10] 人造黒鉛（アチソン炉、炭素を約2500℃以上で黒鉛化）
    // --- 熱（[3-12] 耐火れんが＝珪石れんが・ディナス 1822）---
    public static final Item REFRACTORY_BRICK_BLANK = Reg.item("refractory_brick_blank"); // 成形しただけ（未焼成）
    public static final Item REFRACTORY_BRICK      = Reg.item("refractory_brick");       // 高温（約1450℃）で焼いたもの

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // アチソン炉 [3-10]（1896。耐火れんがの箱に炭素＋砂、両端の黒鉛電極から中身に直接通電。約2500℃で人造黒鉛）。赤熱すると光る
    public static final Machine<AchesonFurnaceBlockEntity> ACHESON_FURNACE = Machine.electric("acheson_furnace", AchesonFurnaceBlockEntity::new, AchesonFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 15));
    // 反射炉 [3-06]（焙焼炉。燃焼室と炉床を分け、炎の熱を天井で反射させる。石炭で硫化鉱を焼き、SO2 を集める）。燃料式（電気の受け口なし）。燃焼中は光る
    // [3-04] ふいご：炉の横に置いて踏む（素手で右クリック）と風を送る。横に回る水車があれば水車ふいご（ずっと送る）
    public static final Machine<BellowsBlockEntity> BELLOWS = Machine.builder("bellows", BellowsBlockEntity::new)
            .block(BellowsBlock::new).ticker(BellowsBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD)).build();
    public static final Machine<ReverberatoryFurnaceBlockEntity> REVERBERATORY_FURNACE = Machine.builder("reverberatory_furnace", ReverberatoryFurnaceBlockEntity::new)
            .ticker(ReverberatoryFurnaceBlockEntity::tick).layout(ReverberatoryFurnaceBlockEntity.LAYOUT)
            .props(lit(stone(3.5f).mapColor(MapColor.COLOR_RED), 13)).build();
    public static final Machine<ArcFurnaceBlockEntity> ARC_FURNACE = Machine.electric("arc_furnace", ArcFurnaceBlockEntity::new, ArcFurnaceBlockEntity.LAYOUT, metal(4.0f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
