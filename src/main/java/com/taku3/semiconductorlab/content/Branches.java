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

    /** 組立台のレシピ（S-010）：電卓＝合格したチップ（加算器＝全加算器、レジスタ、カウンタ）＋キー14＋ニキシー管4＋枠＋配線 */
    /** お題に合格したチップの見本（組立台の表示・テスト用） */
    static net.minecraft.world.item.ItemStack passedChip(String id) {
        var st = new net.minecraft.world.item.ItemStack(Semiconductor.CHIP);
        st.set(com.taku3.semiconductorlab.ModDataComponents.CHALLENGES, java.util.List.of(id));
        return st;
    }

    static void assemblies() {
        com.taku3.semiconductorlab.machine.Assembly.recipe("calculator", CALCULATOR)
                .part("adder", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "full_adder"), 1, Semiconductor.CHIP, () -> passedChip("full_adder"))
                .part("register", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "register"), 2, Semiconductor.CHIP, () -> passedChip("register"))
                .part("counter", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "counter"), 1, Semiconductor.CHIP, () -> passedChip("counter"))
                .part("display", NIXIE_TUBE, 4)
                .part("keys", KEY_SWITCH, 14)
                .part("frame", net.minecraft.world.item.Items.IRON_INGOT, 4)
                .part("wiring", Power.COPPER_CABLE.block.asItem(), 2)
                .seconds(10).build();
        // [B-09] ゲーム機（S-017 Q27 案A ポン型）：玉と板の位置を数えるカウンタ2・速さを足す全加算器・点数を覚えるレジスタ（1972 のポンは TTL の IC 約66個、CPU なし）＋つまみ＋箱
        com.taku3.semiconductorlab.machine.Assembly.recipe("game_console", GAME_CONSOLE)
                .part("counter", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "counter"), 2, Semiconductor.CHIP, () -> passedChip("counter"))
                .part("adder", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "full_adder"), 1, Semiconductor.CHIP, () -> passedChip("full_adder"))
                .part("register", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "register"), 1, Semiconductor.CHIP, () -> passedChip("register"))
                .part("knobs", net.minecraft.world.item.Items.LEVER, 2)
                .part("wiring", Power.COPPER_CABLE.block.asItem(), 2)
                .part("case", net.minecraft.world.item.Items.IRON_INGOT, 4)
                .seconds(10).build();
        // [B-10] スマホ（O-029 Q28 案A 仮決定）：マイコン（mcu4 に合格）＋カメラ＋MEMS の加速度計＋タッチパネル＋TFT 液晶＋バックライト＋リチウムイオン電池＋ケース。
        // TFT 液晶・バックライトはまだない（ID で待つ）⚠️：その2つのアイテムが登録されたときだけレシピを作る（それまで組めない）
        if (smartphoneReady()) com.taku3.semiconductorlab.machine.Assembly.recipe("smartphone", SMARTPHONE)
                .part("soc", s -> s.is(Semiconductor.CHIP) && com.taku3.semiconductorlab.block.ProbeStationBlockEntity.passed(s, "mcu4"), 1, Semiconductor.CHIP, () -> passedChip("mcu4"))
                .part("camera", CAMERA_MODULE, 1)
                .part("motion", Semiconductor.MEMS_ACCELEROMETER, 1)
                .part("touch", Communication.TOUCH_PANEL, 1)
                .part("display", net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(com.taku3.semiconductorlab.SemiconductorLab.id(TFT_ID)), 1)
                .part("backlight", net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(com.taku3.semiconductorlab.SemiconductorLab.id(BACKLIGHT_ID)), 1)
                .part("battery", Power.LITHIUM_ION_BATTERY.block.asItem(), 1)
                .part("case", net.minecraft.world.item.Items.IRON_INGOT, 2)
                .seconds(20).build();
    }

    /** まだ登録されていない部品（ほかの担当が作る）の ID：TFT 液晶 [B-19]（E）・白色 LED バックライト [B-20]。この ID のアイテムができれば、スマホのレシピに入る */
    public static final String TFT_ID = "tft_lcd", BACKLIGHT_ID = "led_backlight";

    /** スマホの部品がそろったか（TFT 液晶とバックライトのアイテムが登録されているか） */
    public static boolean smartphoneReady() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(com.taku3.semiconductorlab.SemiconductorLab.id(TFT_ID))
                && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(com.taku3.semiconductorlab.SemiconductorLab.id(BACKLIGHT_ID));
    }

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ。[B-12] 太陽電池（昼・空・雨なしで 60W）
    // [B-07] 化合物半導体の炉（ヒ素の還元・GaAsP、3kW）と赤い LED の明かり（2W、明るさ7）（IMP4-01）
    public static final Machine<CompoundFurnaceBlockEntity> COMPOUND_FURNACE = Machine.electric("compound_furnace", CompoundFurnaceBlockEntity::new, CompoundFurnaceBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<LedLampBlockEntity> LED_LAMP = Machine.electric("led_lamp", LedLampBlockEntity::new, LedLampBlockEntity.LAYOUT, lit(metal(1.0f).sound(SoundType.GLASS), 7));
    public static final Machine<SolarCellBlockEntity> SOLAR_CELL = Power.generator("solar_cell", SolarCellBlockEntity::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).requiresCorrectToolForDrops().strength(2.0f).sound(SoundType.GLASS)).build();

    // ===== [B-24] ニキシー管・[B-08] 電卓（A、S-010） =====
    /** ニキシー管（1955 ⚠️ ハイドゥ兄弟／バローズ）：ネオンを入れたガラス管に、数字の形の陰極10枚。光る数字を重ねて見せる */
    // [B-07] 赤い LED（IMP4-01、S-007 案A：GaAsP 1962）：白ヒ素 → ヒ素、ガリウム＋ヒ素＋赤リン → GaAsP の板 → 素子 → 明かり
    public static final Item ARSENIC               = Reg.item("arsenic");          // ヒ素（灰色の結晶）。毒、工業の材料としてだけ
    public static final Item GAASP_WAFER           = Reg.item("gaasp_wafer");      // ガリウムヒ素リンの板
    public static final Item RED_LED               = Reg.item("red_led");          // 赤い LED の素子
    public static final Item NIXIE_TUBE            = Reg.item("nixie_tube");
    /** 電卓のキー（ばねで戻る押しボタンの接点） */
    public static final Item KEY_SWITCH            = Reg.item("key_switch");
    /** 電卓：自分で作ったチップ＋キー＋ニキシー管を組立台で。右クリックで計算 */
    public static final Item CALCULATOR            = Reg.item("calculator", com.taku3.semiconductorlab.item.CalculatorItem::new, new Item.Properties().stacksTo(1));
    /** [B-09] ゲーム機（A、S-017 Q27 案A ポン型）：合格したチップで組む、CPU なしの卓球。右クリックで画面 */
    public static final Item GAME_CONSOLE          = Reg.item("game_console", com.taku3.semiconductorlab.item.GameConsoleItem::new, new Item.Properties().stacksTo(1));
    /** [B-10] スマホ（A、O-029）：組立台で。アプリの画面はユーザーの答え待ち、TFT・バックライトがまだなので組めない ⚠️ */
    public static final Item SMARTPHONE            = Reg.item("smartphone", com.taku3.semiconductorlab.item.SmartphoneItem::new, new Item.Properties().stacksTo(1));

    // ===== [B-02a] ダゲレオタイプ（IMP2、S-002 案B：化学の工程を体験。写真の中身は場所・時刻・写りのデータ）=====
    /** 写真の中身（撮った場所・時刻・露光の量） */
    public static final net.minecraft.core.component.DataComponentType<PhotoData> PHOTO = net.minecraft.core.Registry.register(
            net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, com.taku3.semiconductorlab.SemiconductorLab.id("photo"),
            net.minecraft.core.component.DataComponentType.<PhotoData>builder().persistent(PhotoData.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(PhotoData.CODEC)).build());
    public static final Item SILVERED_PLATE        = Reg.item("silvered_plate");        // 銀めっきの銅板（磨いた鏡のような銀の面）
    public static final Item DAGUERREOTYPE_PLATE   = Reg.item("daguerreotype_plate");   // ヨウ素の蒸気で AgI の膜を付けた感光板
    public static final Item CAMERA                = Reg.item("camera", CameraItem::new, new Item.Properties().stacksTo(1)); // 暗箱カメラ
    public static final Item EXPOSED_PLATE         = Reg.item("exposed_plate", PhotoItem::new, new Item.Properties().stacksTo(1));   // 露光した板（まだ像は見えない）
    public static final Item DEVELOPED_PLATE       = Reg.item("developed_plate", PhotoItem::new, new Item.Properties().stacksTo(1)); // 水銀の蒸気で現像した板
    public static final Item DAGUERREOTYPE         = Reg.item("daguerreotype", PhotoItem::new, new Item.Properties().stacksTo(1));   // 定着した写真（1枚きり）

    // ===== [B-02b] ゼラチン乾板とネガ（IMP2 O-008：湿板は作らない＝コロジオンは危ない技術。[7-11] の感光乾板をカメラで露光）=====
    public static final Item EXPOSED_DRY_PLATE     = Reg.item("exposed_dry_plate", PhotoItem::new, new Item.Properties().stacksTo(1));   // 露光した乾板
    public static final Item DEVELOPED_DRY_PLATE   = Reg.item("developed_dry_plate", PhotoItem::new, new Item.Properties().stacksTo(1)); // 現像した乾板（まだ定着前）
    public static final Item NEGATIVE              = Reg.item("negative", NegativeItem::new, new Item.Properties().stacksTo(1));         // ガラスのネガ（焼いても戻る）
    public static final Item PRINTING_PAPER        = Reg.item("printing_paper");     // 印画紙（紙＋塩＋硝酸銀＝塩化銀の紙、タルボット 1840）
    // [P-17] スマホのカメラ（IMP2 O-030 仮決定の案A）：小さなレンズ×2＋赤外線カット膜＋色の分かる画像センサー＋IC＋乾電池。右クリックでその場でカラーの写真（化学の写真 → 電子の写真）
    public static final Item CAMERA_MODULE         = Reg.item("camera_module", com.taku3.semiconductorlab.item.CameraModuleItem::new, new Item.Properties().stacksTo(1));
    public static final Item PRINT                 = Reg.item("print", PhotoItem::new, new Item.Properties().stacksTo(16));            // 写真（紙焼き）

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    // [B-14] アセチレンランプ（IMP3、O-018）：カーバイドを入れると明るく光り、ランダムティックで燃え尽きる。かすは消石灰
    public static final Block CARBIDE_LAMP = Reg.functional("carbide_lamp", CarbideLampBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).sound(SoundType.LANTERN).noOcclusion().lightLevel(CarbideLampBlock::light));

    // [B-06] 偏光フィルム（IMP3、O-023）：ポリビニルアルコール＋水 → PVA の膜2。ヨウ素3＋鉄＋カリ＋水 → ヨウ化カリウム（昔の作り方）。
    //   PVA の膜＋ヨウ素＋ヨウ化カリウム＋ホウ酸 → 偏光フィルム2（引き伸ばしてヨウ素で染め、ホウ酸で止める＝1938 ランドの H シート）。両手に持って右クリックで回す（マリュスの法則）
    public static final Item PVA_FILM              = Reg.item("pva_film");
    public static final Item POTASSIUM_IODIDE      = Reg.item("potassium_iodide");
    public static final Item POLARIZING_FILM       = Reg.item("polarizing_film", PolarizerItem::new, new Item.Properties().stacksTo(16));

    static void init() {}
}
