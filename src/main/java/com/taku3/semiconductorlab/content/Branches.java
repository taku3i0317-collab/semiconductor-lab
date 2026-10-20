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
    public static final Item PRINT                 = Reg.item("print", PhotoItem::new, new Item.Properties().stacksTo(16));            // 写真（紙焼き）

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
