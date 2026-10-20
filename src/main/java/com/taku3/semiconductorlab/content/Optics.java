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

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。ガスボンベ式（電気の受け口なし）。炎が出ている間は光る
    public static final Machine<OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Machine.builder("oxyhydrogen_burner", OxyhydrogenBurnerBlockEntity::new)
            .ticker(OxyhydrogenBurnerBlockEntity::tick).layout(OxyhydrogenBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f), 12)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
