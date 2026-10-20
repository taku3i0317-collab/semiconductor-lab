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
 * 幹7 光学（docs/TECH_TREE.md の「幹7」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/optics/、src/gametest/.../OpticsTests.java）だけを書き換える。
 */
public final class Optics {
    private Optics() {}

    // ===== アイテム =====
    public static final Item FUSED_QUARTZ          = Reg.item("fused_quartz");
    public static final Item QUARTZ_TUBE           = Reg.item("quartz_tube");
    // [7-01] 光学ガラス（IMP2-01）。クラウン＝ソーダ石灰ガラスを溶鉱炉で長く溶かしてかき混ぜたもの（分散が小さい）、
    // フリント＝一酸化鉛 PbO を約4割入れた鉛ガラス（レイヴンズクロフト 1674。屈折率が高く、分散が大きい）
    public static final Item FLINT_GLASS_BATCH     = Reg.item("flint_glass_batch"); // 調合原料（砂5＋一酸化鉛1＋カリ1。レイヴンズクロフトと同じカリ [2-02]）
    public static final Item FLINT_GLASS           = Reg.item("flint_glass");
    public static final Item CROWN_GLASS           = Reg.item("crown_glass");
    // [7-03] レンズ研磨（IMP2-01）：① 鋳鉄の研削皿＋砂（水といっしょにこする）→ すりガラスのレンズ素材、
    // ② ピッチの研磨皿＋ベンガラ Fe2O3 → 透明なレンズ。研削皿と研磨皿は作業台で使っても戻る
    public static final Item ROUGE                 = Reg.item("rouge");             // ベンガラ（酸化鉄(III) Fe2O3 の細かい粉）
    public static final Item GRINDING_TOOL         = Reg.item("grinding_tool", ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item PITCH_LAP             = Reg.item("pitch_lap", ReusableToolItem::new, new Item.Properties().stacksTo(1));
    public static final Item CROWN_LENS_BLANK      = Reg.item("crown_lens_blank");  // 削っただけ（すりガラス）
    public static final Item FLINT_LENS_BLANK      = Reg.item("flint_lens_blank");
    public static final Item CROWN_LENS            = Reg.item("crown_lens");        // 磨いた凸レンズ
    public static final Item FLINT_LENS            = Reg.item("flint_lens");        // 磨いた凹レンズ（色消しの相方）
    // [7-04] 色消しレンズと屈折望遠鏡（IMP2-01）：クラウン凸＋フリント凹 → 色消しレンズ（ドロンド 1758）。対物＝色消し、接眼＝凸のケプラー式
    public static final Item ACHROMATIC_LENS       = Reg.item("achromatic_lens");
    public static final Item REFRACTING_TELESCOPE  = Reg.item("refracting_telescope", TelescopeItem::new, new Item.Properties().stacksTo(1));
    // [7-05] 反射鏡 段階1 スペキュラム（IMP2-01）：銅2＋スズ1 の白い合金を削って磨いた鏡（反射率 約6割、すぐ曇る）→ ニュートン式反射望遠鏡
    public static final Item SPECULUM_CHARGE       = Reg.item("speculum_charge");   // 地金の調合（銅2＋スズ1）
    public static final Item SPECULUM_INGOT        = Reg.item("speculum_ingot");    // スペキュラム合金（Cu 約67%・Sn 約33%）
    public static final Item SPECULUM_MIRROR       = Reg.item("speculum_mirror");   // 磨いた凹面鏡
    public static final Item REFLECTING_TELESCOPE  = Reg.item("reflecting_telescope", TelescopeItem::new, new Item.Properties().stacksTo(1));
    // [7-10] 水銀ランプ（IMP2-01）：石英管＋水銀＋W電極2 → 組立品 →封止台（0.1Pa 以下、シュプレンゲル [5-15]）→ 水銀灯の管 → 直流で光る水銀灯（385W、クーパー・ヒューイット 1901）
    public static final Item MERCURY_LAMP_ASSEMBLY = Reg.item("mercury_lamp_assembly");
    public static final Item MERCURY_LAMP_TUBE     = Reg.item("mercury_lamp_tube");
    // [5-14] 真空蒸着の製品（IMP2-01）：クラウンガラスに銀を薄く蒸着した半透明鏡（半分反射・半分透過）。干渉計 [7-09] の光を2つに分ける板
    public static final Item HALF_SILVERED_MIRROR  = Reg.item("half_silvered_mirror");
    // [7-09] マイケルソン干渉計（IMP2-01）：半透明鏡＋スペキュラム鏡×2＋送りねじ（動く鏡）＋鉄の台。水銀の緑線 546.1nm で長さを測る
    public static final Item INTERFEROMETER        = Reg.item("interferometer", InterferometerItem::new, new Item.Properties().stacksTo(1));
    // [7-22] 反射防止膜（IMP2-01）：真空蒸着装置でレンズに CaF2 を 1/4 波長の厚さ付けたもの（表面の反射 約4% → 約2%）。投影レンズ [7-19] の材料
    public static final Item COATED_CROWN_LENS     = Reg.item("coated_crown_lens");
    public static final Item COATED_FLINT_LENS     = Reg.item("coated_flint_lens");
    // [7-15] 紫外光学材料・CaF2（IMP2-02）：ブリッジマン炉で蛍石を溶かして単結晶に → 削って磨いて CaF2 レンズ（紫外を通す。色消しのアポクロマートにも）
    public static final Item CAF2_CRYSTAL          = Reg.item("caf2_crystal");
    public static final Item CAF2_LENS             = Reg.item("caf2_lens");
    // [7-11] 銀塩フォトマスクの材料（IMP2-03。乾板の化学：1871 マドックスのゼラチン乾板の流れ）
    public static final Item HIDE_BROTH            = Reg.item("hide_broth", new Item.Properties().stacksTo(16)); // 皮の煮汁（革×2＋水）
    public static final Item GELATIN               = Reg.item("gelatin");           // ゼラチン（煮汁を煮つめて乾かす）
    public static final Item PHOTO_EMULSION        = Reg.item("photo_emulsion", new Item.Properties().stacksTo(16)); // 塩化銀ゼラチン乳剤
    public static final Item PHOTO_PLATE           = Reg.item("photo_plate");       // 感光乾板（ガラス板＋乳剤）
    public static final Item FERROUS_SULFATE       = Reg.item("ferrous_sulfate");   // 硫酸鉄(II)＝緑ばん（現像液）
    public static final Item HYPO_LIQUOR           = Reg.item("hypo_liquor", new Item.Properties().stacksTo(16)); // チオ硫酸の煮汁
    public static final Item SODIUM_THIOSULFATE    = Reg.item("sodium_thiosulfate"); // チオ硫酸ナトリウム＝ハイポ（定着液）
    // [7-06] プリズム分光（IMP2-01）：フリントガラスを削って磨いた三角柱（分散が大きい）→ 分光器（キルヒホッフとブンゼン 1859：隙間＋レンズ＋プリズム＋望遠鏡）
    public static final Item PRISM                 = Reg.item("prism");
    public static final Item SPECTROSCOPE          = Reg.item("spectroscope", SpectroscopeItem::new, new Item.Properties().stacksTo(1));
    // [7-07] 回折格子（IMP2-01）：フラウンホーファー 1821 の針金格子（2本の細いねじに銅線を巻く。間隔 0.004 インチ＝101.6μm）→ 波長を測れる分光器
    public static final double WIRE_GRATING_MICRONS = 101.6;
    public static final Item WIRE_GRATING          = Reg.item("wire_grating");
    public static final Item GRATING_SPECTROSCOPE  = Reg.item("grating_spectroscope", p -> new SpectroscopeItem(p, WIRE_GRATING_MICRONS), new Item.Properties().stacksTo(1));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // ブリッジマン炉 [7-15]（IMP2-02）：誘導加熱 3kW、アルゴンの中で蛍石を溶かし、ゆっくり下げて CaF2 単結晶に
    public static final Machine<BridgmanFurnaceBlockEntity> BRIDGMAN_FURNACE = Machine.electric("bridgman_furnace", BridgmanFurnaceBlockEntity::new, BridgmanFurnaceBlockEntity.LAYOUT, lit(metal(3.5f), 10));
    // 水銀灯 [7-10]（IMP2-01）：直流 385W（110V・3.5A）でつく。画面なし。点灯中は明るさ15、分光器で水銀の線（g線・h線・i線）が見える
    public static final Machine<MercuryLampBlockEntity> MERCURY_LAMP = Machine.electric("mercury_lamp", MercuryLampBlockEntity::new, MercuryLampBlockEntity.LAYOUT,
            lit(metal(1.5f).sound(SoundType.GLASS), 15));
    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。ガスボンベ式（電気の受け口なし）。炎が出ている間は光る
    public static final Machine<OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Machine.builder("oxyhydrogen_burner", OxyhydrogenBurnerBlockEntity::new)
            .ticker(OxyhydrogenBurnerBlockEntity::tick).layout(OxyhydrogenBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f), 12)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
