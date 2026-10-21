package com.taku3.semiconductorlab.content;

import net.minecraft.world.item.Item;

/**
 * 幹10 制御・計算（docs/TECH_TREE.md の「幹10」のノード）。最初のノード [10-01] 電磁石のときに作った（IMP4-01、O-011）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/control/、src/gametest/.../ControlTests.java）だけを書き換える。
 */
import com.taku3.semiconductorlab.block.RobotArmBlockEntity;
import com.taku3.semiconductorlab.machine.Machine;

public final class Control {
    private Control() {}

    // ===== アイテム =====
    // [10-01] 電磁石（1825 スタージャン、1830年代 ヘンリー）：鉄の芯に絶縁した銅線を何重にも巻く。電流を流す間だけ磁石になる。イオン注入 [9-28] の質量分離・電磁レンズなどの部品
    public static final Item ELECTROMAGNET         = Reg.item("electromagnet");
    // [10-04] フィードバック制御（O-037）：遠心調速機（1788 ワット）。蒸気機関・タービンに右クリックで付ける
    public static final Item CENTRIFUGAL_GOVERNOR  = Reg.item("centrifugal_governor");
    // [10-26] 産業用ロボット（O-037）：腕（1kW）と、動きを教えるティーチングペンダント
    public static final Item TEACH_PENDANT         = Reg.item("teach_pendant", com.taku3.semiconductorlab.item.TeachPendantItem::new, new Item.Properties().stacksTo(1));
    public static final Machine<RobotArmBlockEntity> ROBOT_ARM = Machine.electric("robot_arm", com.taku3.semiconductorlab.block.RobotArmBlockEntity::new, com.taku3.semiconductorlab.block.RobotArmBlockEntity.LAYOUT, com.taku3.semiconductorlab.machine.Machine.metal(3.0f));

    // [10-13] 4ビットのマイコン（1971 インテル MCS-4 の形、A、S-014）：CPU のチップ＋ROM のチップ＋水晶＋ニキシー管を組立台で
    public static final Item MICROCOMPUTER         = Reg.item("microcomputer", com.taku3.semiconductorlab.item.MicrocomputerItem::new, new Item.Properties().stacksTo(1));

    // [10-32] 数値演算チップ（IEEE 754、1980 インテル 8087 の形、A、O-032）：右クリックで 0.1＋0.2 を 16・32・64ビットで
    public static final Item MATH_COPROCESSOR      = Reg.item("math_coprocessor", com.taku3.semiconductorlab.item.MathCoprocessorItem::new, new Item.Properties().stacksTo(16));

    /** お題に合格したチップの見本（組立台の表示・テスト用） */
    static net.minecraft.world.item.ItemStack passedChip(String id) {
        var st = new net.minecraft.world.item.ItemStack(Semiconductor.CHIP);
        st.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of(id));
        return st;
    }

    /** 組立台のレシピ（S-014 Q25-5 案A）：マイコン＝cpu4 に合格したチップ＋rom_program に合格したチップ＋水晶の振動子＋ニキシー管＋板＋配線 */
    static void assemblies() {
        com.taku3.semiconductorlab.machine.Assembly.recipe("microcomputer", MICROCOMPUTER)
                .part("cpu", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "cpu4"), 1, Semiconductor.CHIP, () -> passedChip("cpu4"))
                .part("rom", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "rom_program"), 1, Semiconductor.CHIP, () -> passedChip("rom_program"))
                .part("clock", Precision.CRYSTAL_RESONATOR, 1)
                .part("display", Branches.NIXIE_TUBE, 1)
                .part("board", net.minecraft.world.item.Items.IRON_INGOT, 2)
                .part("wiring", Power.COPPER_CABLE.block.asItem(), 1)
                .seconds(10).build();
        // [10-32] 数値演算チップ（O-032 Q31 案A 仮決定）：8ビット CPU・掛け算器・レジスタのお題に合格したチップ＋配線＋板
        com.taku3.semiconductorlab.machine.Assembly.recipe("math_coprocessor", MATH_COPROCESSOR)
                .part("cpu", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "cpu8"), 1, Semiconductor.CHIP, () -> passedChip("cpu8"))
                .part("multiplier", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "mul4"), 1, Semiconductor.CHIP, () -> passedChip("mul4"))
                .part("register", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "register"), 1, Semiconductor.CHIP, () -> passedChip("register"))
                .part("wiring", Power.COPPER_CABLE.block.asItem(), 1)
                .part("board", net.minecraft.world.item.Items.IRON_INGOT, 1)
                .seconds(10).build();
    }

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
