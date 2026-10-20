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

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。ガスボンベ式（電気の受け口なし）。炎が出ている間は光る
    public static final Machine<OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Machine.builder("oxyhydrogen_burner", OxyhydrogenBurnerBlockEntity::new)
            .ticker(OxyhydrogenBurnerBlockEntity::tick).layout(OxyhydrogenBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f), 12)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
