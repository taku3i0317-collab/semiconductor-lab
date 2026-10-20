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
 * 幹2 化学（docs/TECH_TREE.md の「幹2」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/chemistry/、src/gametest/.../ChemistryTests.java）だけを書き換える。
 */
public final class Chemistry {
    private Chemistry() {}

    // ===== アイテム =====
    // --- 試薬 ---
    public static final Item SULFUR_POWDER         = Reg.item("sulfur_powder");
    public static final Item SULFUR_DIOXIDE        = Reg.item("sulfur_dioxide"); // 硫黄を燃やした気体
    public static final Item SULFURIC_ACID         = Reg.item("sulfuric_acid", new Item.Properties().stacksTo(16));
    public static final Item PHOSPHORUS            = Reg.item("phosphorus");
    // --- ソーダ（[2-03] 天然ソーダ：トロナを焼く 2(Na2CO3·NaHCO3·2H2O) → 3Na2CO3 + CO2 + 5H2O）---
    public static final Item TRONA                 = Reg.item("trona");             // トロナ（天然ソーダの鉱物）
    public static final Item SODIUM_CARBONATE      = Reg.item("sodium_carbonate");  // 炭酸ナトリウム（ソーダ灰）
    // [2-14] ソーダ石灰ガラス（SiO2 約72%・Na2O 14%・CaO 10%）。実験器具用（窓・建材はバニラのガラス＝近似、CHEMISTRY #36）
    public static final Item GLASS_BATCH           = Reg.item("glass_batch");       // 調合原料（砂6＋ソーダ1＋生石灰1）
    public static final Item SODA_LIME_GLASS       = Reg.item("soda_lime_glass");   // 溶かして固めたガラス（器具の素材）
    // [2-23] 木材乾留（IMP2-01）：乾留炉の副産物。木タールをかまどで煮詰めるとピッチ（レンズ研磨 [7-03] の研磨皿）
    public static final Item WOOD_VINEGAR          = Reg.item("wood_vinegar", new Item.Properties().stacksTo(16)); // 木酢液（酢酸・メタノールを含む水）
    public static final Item WOOD_TAR              = Reg.item("wood_tar", new Item.Properties().stacksTo(16));     // 木タール
    public static final Item PITCH                 = Reg.item("pitch");             // ピッチ（タールを煮詰めた黒い固まり）

    // ===== ブロック =====
    public static final Block TRONA_DEPOSIT  = Reg.ore("trona_deposit", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).requiresCorrectToolForDrops().strength(1.0f).sound(SoundType.STONE));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 乾留炉 [2-23]（IMP2-01）：鉄のレトルト＋銅の冷却管。原木 → 木炭＋木酢液＋木タール。石炭・木炭を燃やす（電気なし）
    public static final Machine<WoodRetortBlockEntity> WOOD_RETORT = Machine.builder("wood_retort", WoodRetortBlockEntity::new)
            .ticker(WoodRetortBlockEntity::tick).layout(WoodRetortBlockEntity.LAYOUT).props(lit(metal(3.0f), 11)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
