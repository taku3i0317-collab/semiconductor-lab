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
 * 幹5 真空（docs/TECH_TREE.md の「幹5」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/vacuum/、src/gametest/.../VacuumTests.java）だけを書き換える。
 */
public final class Vacuum {
    private Vacuum() {}

    // ===== アイテム =====
    // --- 真空（[5-01] ポンプ・[5-10] 真空計）---
    public static final Item MERCURY_VACUUM_GAUGE  = Reg.item("mercury_vacuum_gauge", MercuryVacuumGaugeItem::new, new Item.Properties().stacksTo(1));
    // --- 真空管（[5-02] ド・フォレストの三極管 1906）---
    public static final Item VACUUM_TUBE_ASSEMBLY  = Reg.item("vacuum_tube_assembly"); // ガラス球＋W フィラメント＋グリッド＋プレート（まだ空気入り）
    public static final Item VACUUM_TUBE           = Reg.item("vacuum_tube");          // 排気して封じた三極真空管

    // ===== ブロック =====
    // 真空容器 [5-11]（ガラスのベルジャー＋鉄の台板）。中の圧力を持つ
    // 手押しピストンポンプ [5-01]（ゲーリケ 1650頃）。隣の真空容器を右クリックで排気
    public static final Block HAND_VACUUM_PUMP = Reg.functional("hand_vacuum_pump", HandVacuumPumpBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(3.0f).sound(SoundType.METAL));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 真空容器 [5-11]（ガラスのベルジャー＋鉄の台板）。中の圧力を持つ
    public static final Machine<VacuumChamberBlockEntity> VACUUM_CHAMBER = Machine.builder("vacuum_chamber", VacuumChamberBlockEntity::new)
            .block(VacuumChamberBlock::new).ticker(VacuumChamberBlockEntity::tick).props(metal(2.0f).sound(SoundType.GLASS)).build();
    // シュプレンゲル水銀ポンプ [5-15]
    public static final Machine<SprengelPumpBlockEntity> SPRENGEL_PUMP = Machine.builder("sprengel_pump", SprengelPumpBlockEntity::new)
            .block(SprengelPumpBlock::new).ticker(SprengelPumpBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(1.0f).sound(SoundType.GLASS).noOcclusion()).build();
    // 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器が十分低圧のときだけ組立品を封じる。動作中は光る
    public static final Machine<SealingStationBlockEntity> SEALING_STATION = Machine.electric("sealing_station", SealingStationBlockEntity::new, SealingStationBlockEntity.LAYOUT, lit(metal(3.0f), 7));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
