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
    // --- シリコーン油（[5-04] の③、ロチョウの直接法 1941〜45。IMP4-01、[5-06] 油拡散ポンプの油）---
    public static final Item METHYL_CHLORIDE       = Reg.contained("methyl_chloride", com.taku3.semiconductorlab.machine.Vessel.GAS_BAG);   // CH3Cl（燃える・毒のある気体）
    public static final Item DIMETHYLDICHLOROSILANE = Reg.contained("dimethyldichlorosilane", com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE); // (CH3)2SiCl2（水で HCl を出す）
    public static final Item SILICONE_OIL          = Reg.contained("silicone_oil", com.taku3.semiconductorlab.machine.Vessel.GLASS_BOTTLE);   // ポリジメチルシロキサン。ほとんど蒸発しない油

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
    // 回転ポンプ [5-04] 段階①（IMP2-01、ゲーデの回転水銀ポンプ 1905）：モーター 250W、1Pa まで。拡散ポンプの前段にもなる
    public static final Machine<RotaryPumpBlockEntity> ROTARY_PUMP = Machine.electric("rotary_pump", RotaryPumpBlockEntity::new, RotaryPumpBlockEntity.LAYOUT, lit(metal(3.5f), 0));
    // 水銀拡散ポンプ [5-05]（IMP2-01、ゲーデ 1915、ガラスと鉄）：ヒーター 400W、前段の回転ポンプと 10Pa 以下が要る。10⁻⁴Pa 級
    // [5-07] ターボ分子ポンプ（IMP2 O-017、1958 ベッカー）：精密な玉軸受（段階2以上）で高速回転、10⁻⁶Pa、蒸気が戻らない
    public static final Machine<TurboPumpBlockEntity> TURBO_PUMP = Machine.electric("turbo_pump", TurboPumpBlockEntity::new, TurboPumpBlockEntity.LAYOUT, metal(3.0f));
    public static final Machine<DiffusionPumpBlockEntity> DIFFUSION_PUMP = Machine.electric("diffusion_pump", DiffusionPumpBlockEntity::new, DiffusionPumpBlockEntity.LAYOUT, lit(metal(2.0f).sound(SoundType.GLASS), 4));
    // [5-04] の③ シリコーンの反応器（IMP4-01）：塩化メチル → 直接法 → 加水分解でシリコーン油。2kW
    public static final Machine<SiliconeReactorBlockEntity> SILICONE_REACTOR = Machine.electric("silicone_reactor", SiliconeReactorBlockEntity::new, SiliconeReactorBlockEntity.LAYOUT, metal(3.5f));
    // [5-06] 油拡散ポンプ（IMP4-01、バーチ 1928・ヒックマン 1930年代）：鋼の胴＋シリコーン油、1kW。10⁻⁵Pa 級で水銀より速い。前段・画面は水銀拡散ポンプと共通
    public static final Machine<OilDiffusionPumpBlockEntity> OIL_DIFFUSION_PUMP = Machine.electric("oil_diffusion_pump", OilDiffusionPumpBlockEntity::new, DiffusionPumpBlockEntity.LAYOUT, lit(metal(3.0f), 4));
    // 真空蒸着装置 [5-14]（IMP2-01）：W 線のヒーター 300W で金属を蒸発。隣の容器が 0.01Pa 以下のときだけ。クラウンガラス＋銀 → 半透明鏡
    public static final Machine<EvaporatorBlockEntity> EVAPORATOR = Machine.electric("evaporator", EvaporatorBlockEntity::new, EvaporatorBlockEntity.LAYOUT, lit(metal(3.0f), 9));
    // 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器が十分低圧のときだけ組立品を封じる。動作中は光る
    public static final Machine<SealingStationBlockEntity> SEALING_STATION = Machine.electric("sealing_station", SealingStationBlockEntity::new, SealingStationBlockEntity.LAYOUT, lit(metal(3.0f), 7));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
