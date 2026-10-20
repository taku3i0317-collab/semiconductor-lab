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
 * 幹0 生存・道具（docs/TECH_TREE.md の「幹0」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/survival/、src/gametest/.../SurvivalTests.java）だけを書き換える。
 */
public final class Survival {
    private Survival() {}

    // ===== アイテム =====
    // --- 石灰（[0-10] 方解石 CaCO3 → 焼成 → 生石灰 CaO、[0-11] CaO + H2O → 消石灰 Ca(OH)2）---
    public static final Item QUICKLIME             = Reg.item("quicklime");
    public static final Item SLAKED_LIME           = Reg.item("slaked_lime");
    public static final Item BITTERN               = Reg.item("bittern");           // にがり（海水を煮詰めた母液、主成分 MgCl2）

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // [0-16] 塩田（揚げ浜式。海水を砂にまいて天日で濃くし、釜で煮詰める）。海・浜バイオームで、横に水があり、空が見えて晴れているときだけ塩がたまる
    public static final Machine<SaltPanBlockEntity> SALT_PAN = Machine.builder("salt_pan", SaltPanBlockEntity::new)
            .block(SaltPanBlock::new).ticker(SaltPanBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.8f).sound(SoundType.SAND)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
