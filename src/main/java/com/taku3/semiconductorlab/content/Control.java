package com.taku3.semiconductorlab.content;

import net.minecraft.world.item.Item;

/**
 * 幹10 制御・計算（docs/TECH_TREE.md の「幹10」のノード）。最初のノード [10-01] 電磁石のときに作った（IMP4-01、O-011）。
 * この幹のノードを足すときは、このファイル（と src/main/lang/control/、src/gametest/.../ControlTests.java）だけを書き換える。
 */
public final class Control {
    private Control() {}

    // ===== アイテム =====
    // [10-01] 電磁石（1825 スタージャン、1830年代 ヘンリー）：鉄の芯に絶縁した銅線を何重にも巻く。電流を流す間だけ磁石になる。イオン注入 [9-28] の質量分離・電磁レンズなどの部品
    public static final Item ELECTROMAGNET         = Reg.item("electromagnet");

    // [10-13] 4ビットのマイコン（1971 インテル MCS-4 の形、A、S-014）：CPU のチップ＋ROM のチップ＋水晶＋ニキシー管を組立台で
    public static final Item MICROCOMPUTER         = Reg.item("microcomputer", com.taku3.semiconductorlab.item.MicrocomputerItem::new, new Item.Properties().stacksTo(1));

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
    }

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
