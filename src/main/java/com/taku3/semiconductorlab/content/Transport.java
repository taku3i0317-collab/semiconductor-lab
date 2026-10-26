package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.block.ConveyorBeltBlock;
import com.taku3.semiconductorlab.block.ConveyorBeltBlockEntity;
import com.taku3.semiconductorlab.block.ConveyorDriveBlockEntity;
import com.taku3.semiconductorlab.block.OhtDriveBlockEntity;
import com.taku3.semiconductorlab.block.OhtRailBlock;
import com.taku3.semiconductorlab.item.ChronometerItem;
import com.taku3.semiconductorlab.machine.Machine;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * 幹14 交通・運輸（[14-..] のノード。IMP2 O-023 の [14-03] 経度とクロノメーターで作成。[8-21] 六分儀の続き）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/transport/、src/gametest/.../TransportTests.java）だけを書き換える。
 */
public final class Transport {
    private Transport() {}

    // [14-03] マリン・クロノメーター（IMP2 O-023、1761 ハリソン H4）：るつぼ鋼のぜんまい＋ルビーの軸受け＋規格ねじ＋青銅。グリニッジの時刻を船に持ち歩く。
    //   1日の狂い ±0.5秒⚠️（H4 は81日で約5秒）。六分儀で南中を測るとき、もう片方の手に持つと経度が出る
    // [14-02] 羅針盤（IMP2 S-010、1088 沈括）：天然磁石でこすった鉄の針を水の器に浮かべる。北を指し、近くの鉄でくるう
    public static final Item MAGNETIC_COMPASS      = Reg.item("magnetic_compass", com.taku3.semiconductorlab.item.CompassItem::new, new Item.Properties().stacksTo(1));
    // [14-07] 空気入りタイヤ（IMP2 S-012、1888 ダンロップ）：加硫ゴムの管を麻布で包んで空気を入れる
    public static final Item PNEUMATIC_TYRE        = Reg.item("pneumatic_tyre");
    // [14-08] 自転車（IMP2 S-012、1885 ローバー安全型）：後輪をチェーンで回す。利き手に持つと歩く速さが上がる（ゲームの近似：乗り物でなく持ち物）
    public static final double BICYCLE_SPEED = 0.5;
    public static final Item BICYCLE               = Reg.item("bicycle", new Item.Properties().stacksTo(1).attributes(
            net.minecraft.world.item.component.ItemAttributeModifiers.builder().add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(com.taku3.semiconductorlab.SemiconductorLab.id("bicycle_speed"), BICYCLE_SPEED,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build()));
    public static final Item MARINE_CHRONOMETER    = Reg.item("marine_chronometer", p -> new ChronometerItem(p, 0.5, "marine_chronometer"), new Item.Properties().stacksTo(1));

    // [14-13] ジュラルミン（IMP2 S-010、1906 ヴィルム）：Al×6＋銅＋Mg → 調合 →かまど（焼入れ）→ 焼入れしたジュラルミン（やわらかい）→ 持ち物で1日⚠️ → ジュラルミン（時効硬化）
    public static final Item DURALUMIN_CHARGE      = Reg.item("duralumin_charge");
    public static final Item DURALUMIN             = Reg.item("duralumin");
    public static final Item QUENCHED_DURALUMIN    = Reg.item("quenched_duralumin", p -> new com.taku3.semiconductorlab.item.QuenchedDuraluminItem(p, () -> DURALUMIN), new Item.Properties());

    // [14-22] 流れ作業とベルトコンベア（IMP4-01、O-029 仮決定の案A、1913 フォード ⚠️）：ベルト（向き＝物の進む向き、1マスに1スタック）＋電動モーターの駆動（ベルト1マス 5W）。
    //   駆動の後ろの機械の出口 → ベルト1秒1マス → 終わりの先の機械の入口（Transfer API）。E は運ぶ道具だけ（幹14 のほかのノードは C）
    public static final Machine<ConveyorBeltBlockEntity> CONVEYOR_BELT = Machine.builder("conveyor_belt", ConveyorBeltBlockEntity::new)
            .block(ConveyorBeltBlock::new).props(Machine.metal(1.0f).noOcclusion()).build();
    public static final Machine<ConveyorDriveBlockEntity> CONVEYOR_DRIVE = Machine.electric("conveyor_drive", ConveyorDriveBlockEntity::new, ConveyorDriveBlockEntity.LAYOUT, Machine.metal(2.0f));

    // [14-23] 天井の搬送 OHT（IMP4-01、O-029 仮決定の案A、1990年代の 300mm 工場 ⚠️）：天井のレール（光る＝台車のいる所）＋駆動（下のロードポートの出す FOUP がいっぱいなら、
    //   レールの行き止まりの下のロードポートの入れる口へ。時間はレールの長さに比例、200W）
    public static final Block OHT_RAIL = Reg.block("oht_rail", OhtRailBlock::new, Machine.lit(Machine.metal(1.5f).noOcclusion(), 7));
    public static final Machine<OhtDriveBlockEntity> OHT_DRIVE = Machine.electric("oht_drive", OhtDriveBlockEntity::new, OhtDriveBlockEntity.LAYOUT, Machine.metal(2.0f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
