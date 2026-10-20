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
 * 枝🌿（EUV の前提にならない製品。docs/TECH_TREE.md の「枝（🌿）一覧」と [B-..] のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/branches/、src/gametest/.../BranchesTests.java）だけを書き換える。
 */
public final class Branches {
    private Branches() {}

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ。[B-12] 太陽電池（昼・空・雨なしで 60W）
    public static final Machine<SolarCellBlockEntity> SOLAR_CELL = Power.generator("solar_cell", SolarCellBlockEntity::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).requiresCorrectToolForDrops().strength(2.0f).sound(SoundType.GLASS)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
