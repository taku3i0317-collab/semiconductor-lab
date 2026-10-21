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
    // [15-18] 静電容量式タッチパネル（IMP4-01、O-021 仮決定の案A）：右クリックで体の静電容量の変化を読む（生き物だけ反応）。組立台で
    public static final Item TOUCH_PANEL           = Reg.item("touch_panel", com.taku3.semiconductorlab.item.TouchPanelItem::new, new Item.Properties().stacksTo(1));
    public static final Item TRANSISTOR_RADIO      = Reg.item("transistor_radio", new Item.Properties().stacksTo(1));
    // [15-04] 電話（IMP2 S-010、1876）：電磁石（受話器）＋木炭の粒（炭素マイク）＋鉄（振動板）＋銅線。2台を両手で結び、持ってチャットすると同じ回線の電話に届く
    // [15-05] 蓄音機（IMP2 S-010、1877 エジソン）：錫はくを巻いた筒に針で溝を刻んで録音・再生。錫はくは3回ほどで聞こえなくなる
    // [15-12] 磁気記録（IMP2 S-010、1898 ポールセンの鋼線・1928 プフロイマーの磁気テープ）：磁気なので何度聞いても消えず、消して記録し直せる
    // [15-02] 活版印刷（IMP2 S-010、1450年ごろ グーテンベルク）：鉛・アンチモン・錫の活字金（固まるときに少しふくらむ）→ 活字。印刷機で書いた本を組み、白い本に刷る
    public static final Item TYPE_METAL            = Reg.item("type_metal");
    public static final Item MOVABLE_TYPE          = Reg.item("movable_type", new Item.Properties().stacksTo(1));
    public static final Item TYPESET_FORM          = Reg.item("typeset_form", new Item.Properties().stacksTo(1));
    public static final Block PRINTING_PRESS       = Reg.block("printing_press", PrintingPressBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD));
    public static final Item STEEL_WIRE_SPOOL      = Reg.item("steel_wire_spool", new Item.Properties().stacksTo(1));
    public static final Item MAGNETIC_TAPE         = Reg.item("magnetic_tape", new Item.Properties().stacksTo(1));
    public static final Item TELEGRAPHONE          = Reg.item("telegraphone", p -> new com.taku3.semiconductorlab.item.TelegraphoneItem(p, () -> STEEL_WIRE_SPOOL, () -> MAGNETIC_TAPE), new Item.Properties().stacksTo(1));
    public static final Item TIN_FOIL_CYLINDER     = Reg.item("tin_foil_cylinder", new Item.Properties().stacksTo(1));
    public static final Item PHONOGRAPH            = Reg.item("phonograph", p -> new com.taku3.semiconductorlab.item.PhonographItem(p, () -> TIN_FOIL_CYLINDER), new Item.Properties().stacksTo(1));
    public static final Item TELEPHONE             = Reg.item("telephone", com.taku3.semiconductorlab.item.TelephoneItem::new, new Item.Properties().stacksTo(1));
    // [15-09] 真空管ラジオ（IMP2 S-010、1920）：三極真空管3（増幅）＋同調コイル・コンデンサ＋スピーカー＋木の箱。右クリックで聞くと鉛蓄電池から 40W×10秒（ヒーター）
    public static final Item TUBE_RADIO            = Reg.item("tube_radio", com.taku3.semiconductorlab.item.TubeRadioItem::new, new Item.Properties().stacksTo(1));
    // [15-01] 紙（パルプ）（IMP3、S-002）：SO2＋消石灰＋水 → 亜硫酸の蒸解液、原木2＋蒸解液 → 蒸解釜の仕込み →加圧加熱釜（約130℃以上・3気圧以上）→ 木材パルプ2（1867 ティルマン）。
    //   パルプ3 → 紙3。パルプのセルロースは [2-18] 酢酸セルロース（RO 膜）の材料
    public static final Item SULFITE_LIQUOR        = Reg.item("sulfite_liquor", new Item.Properties().stacksTo(16));
    public static final Item PULP_CHARGE           = Reg.item("pulp_charge", new Item.Properties().stacksTo(16));
    public static final Item WOOD_PULP             = Reg.item("wood_pulp");

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    // [15-13] 光ファイバー（IMP2 O-033、1966 カオ・1970 コーニング）：石英×4＋GeO2（コアの屈折率を上げる）→ 母材 →酸水素バーナーで引く → ファイバー×8。
    //   合成石英の母材は 20dB/km、天然の珪石を溶かした石英の母材は 1000dB/km（不純物の鉄・水が光を吸う）
    public static final Item FIBER_PREFORM         = Reg.item("fiber_preform");
    public static final Item FIBER_PREFORM_NATURAL = Reg.item("fiber_preform_natural");
    public static final Item OPTICAL_FIBER         = Reg.item("optical_fiber", com.taku3.semiconductorlab.item.OpticalFiberItem::new, new Item.Properties());

    static void init() {
        // [15-04] 電話を手に持って話すと、同じ回線の電話を持つ人に届く（IMP2 S-010）
        net.fabricmc.fabric.api.message.v1.ServerMessageEvents.CHAT_MESSAGE.register((msg, sender, params) ->
                {
                    com.taku3.semiconductorlab.item.TelephoneItem.relay(msg.signedContent(), sender);
                    com.taku3.semiconductorlab.item.PhonographItem.onChat(msg.signedContent(), sender);   // [15-05] 蓄音機の録音
                    com.taku3.semiconductorlab.item.TelegraphoneItem.onChat(msg.signedContent(), sender);   // [15-12] 磁気記録
                });
        // [15-01] 亜硫酸法の蒸解は圧力釜で煮る（加圧加熱釜の表に1行、IMP3）。圧力が足りないとリグニンが溶けず、仕込みがそのまま戻る
        AutoclaveBlockEntity.RECIPES.add(new AutoclaveBlockEntity.Recipe("wood_pulp", st -> st.is(PULP_CHARGE), 1,
                () -> new net.minecraft.world.item.ItemStack(WOOD_PULP, 2), () -> new net.minecraft.world.item.ItemStack(PULP_CHARGE), 130, 3, 20 * 30));
    }

    /** 組立台のレシピ（IMP-33。Content.initialize が全部の幹のあとで呼ぶ） */
    static void assemblies() {
        com.taku3.semiconductorlab.machine.Assembly.recipe("touch_panel", TOUCH_PANEL)   // [15-18] IMP4-01
                .part("electrode", Chemistry.ITO_GLASS, 1)
                .part("wiring", net.minecraft.world.item.Items.COPPER_INGOT, 1)
                .part("readout", st -> st.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(st, "not"), 1, Semiconductor.CHIP, Semiconductor::passedNot)
                .seconds(5).build();
        // [15-09] 真空管ラジオ（IMP2 S-010）：トランジスタラジオの前の形。増幅は三極真空管、箱は木
        com.taku3.semiconductorlab.machine.Assembly.recipe("tube_radio", TUBE_RADIO)
                .part("amplify", com.taku3.semiconductorlab.content.Vacuum.VACUUM_TUBE, 3)
                .part("tuning_coil", Power.COPPER_CABLE.block.asItem(), 1)
                .part("tuning_capacitor", Power.LEYDEN_JAR, 1)
                .part("speaker", Power.MAGNET, 1)
                .part("cabinet", net.minecraft.world.item.Items.OAK_PLANKS, 4)
                .seconds(5).build();
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
