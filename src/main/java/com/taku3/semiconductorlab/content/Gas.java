package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.item.*;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.Vessel;
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
 * 幹6 ガス・電解（docs/TECH_TREE.md の「幹6」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/gas/、src/gametest/.../GasTests.java）だけを書き換える。
 */
public final class Gas {
    private Gas() {}

    // ===== アイテム =====
    public static final Item HYDROCHLORIC_ACID     = Reg.contained("hydrochloric_acid", Vessel.GLASS_BOTTLE); // [6-06] 塩酸（HCl 約25%、ガラス瓶入り）
    // --- ガス（v0.2a [6-01] 電解槽の生成物）---
    // 「袋詰め」のガス（約1mol）。貯蔵は [6-02] ガスホルダー、高圧の持ち運びは [6-03] ボンベ
    public static final Item HYDROGEN_GAS          = Reg.contained("hydrogen_gas", Vessel.GAS_BAG);
    public static final Item OXYGEN_GAS            = Reg.contained("oxygen_gas", Vessel.GAS_BAG);
    // 高圧ボンベ（[6-03] 圧縮機。鋼製・約150気圧・1本 50mol）
    public static final Item EMPTY_GAS_CYLINDER    = Reg.item("empty_gas_cylinder", new Item.Properties().stacksTo(16));
    public static final Item HYDROGEN_CYLINDER     = Reg.contained("hydrogen_cylinder", Vessel.STEEL_CYLINDER);
    public static final Item OXYGEN_CYLINDER       = Reg.contained("oxygen_cylinder", Vessel.STEEL_CYLINDER);
    // [6-05] クロルアルカリ電解：2NaCl + 2H2O → Cl2 + H2 + 2NaOH
    public static final Item CHLORINE_GAS          = Reg.contained("chlorine_gas", Vessel.GAS_BAG);      // 塩素（黄緑色の有毒な気体）。1袋＝約1mol
    public static final Item CHLORINE_CYLINDER     = Reg.contained("chlorine_cylinder", Vessel.STEEL_CYLINDER); // 塗色は黄（日本の規格）
    public static final Item SODIUM_HYDROXIDE      = Reg.item("sodium_hydroxide");  // 水酸化ナトリウム NaOH（苛性ソーダ）
    // [6-07] 空気分離（リンデ）：空気 → N2 78% ＋ O2 21% ＋ Ar 0.93%
    public static final Item NITROGEN_GAS          = Reg.contained("nitrogen_gas", Vessel.GAS_BAG);      // 窒素 N2。1袋＝約1mol
    public static final Item ARGON_GAS             = Reg.contained("argon_gas", Vessel.GAS_BAG);         // アルゴン Ar。1袋＝約1mol
    public static final Item NITROGEN_CYLINDER     = Reg.contained("nitrogen_cylinder", Vessel.STEEL_CYLINDER); // 塗色はねずみ色（日本の規格「その他のガス」）
    public static final Item ARGON_CYLINDER        = Reg.contained("argon_cylinder", Vessel.STEEL_CYLINDER);    // 同じくねずみ色
    // --- 塩（[6-04] 岩塩・[0-16] 塩田）。岩塩でも海水でも同じ NaCl ---
    public static final Item ROCK_SALT             = Reg.item("rock_salt");         // 岩塩の原石（蒸発岩、NaCl 95〜99%）
    public static final Item SALT                  = Reg.item("salt");              // 塩 NaCl（クロルアルカリ電解 [6-05] の原料）

    // ===== ブロック =====
    // [6-04] 岩塩鉱（古い海が干上がってできた蒸発岩の層）。砂漠・荒野の地下の石に生成。軟らかい（モース硬度 2.5）
    public static final Block ROCK_SALT_ORE  = Reg.ore("rock_salt_ore", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(1.5f, 3.0f).sound(SoundType.STONE));

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 電解槽 [6-01]（ガラス槽＋黒鉛電極）。電気を消費する機械の第1号
    public static final Machine<ElectrolyzerBlockEntity> ELECTROLYZER = Machine.electric("electrolyzer", ElectrolyzerBlockEntity::new, ElectrolyzerBlockEntity.LAYOUT, metal(3.0f).sound(SoundType.GLASS));
    // ガスホルダー [6-02]（水封式の低圧ガスタンク。鉄のベル＋水槽）。電気は使わない
    public static final Machine<GasHolderBlockEntity> GAS_HOLDER = Machine.builder("gas_holder", GasHolderBlockEntity::new)
            .block(MachineEntityBlock::new).ticker(GasHolderBlockEntity::tick).layout(GasHolderBlockEntity.LAYOUT).props(metal(3.0f)).build();
    // 塩酸合成炉 [6-06]（酸水素バーナーの派生。石英の火口で水素を塩素中で燃やし H2 + Cl2 → 2HCl、吸収塔で水に溶かす）。ガス式（電気の受け口なし）。燃焼中は光る
    public static final Machine<HclBurnerBlockEntity> HCL_BURNER = Machine.builder("hcl_burner", HclBurnerBlockEntity::new)
            .ticker(HclBurnerBlockEntity::tick).layout(HclBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f).sound(SoundType.GLASS), 10)).build();
    public static final Machine<AirSeparationBlockEntity> AIR_SEPARATION_UNIT = Machine.electric("air_separation_unit", AirSeparationBlockEntity::new, AirSeparationBlockEntity.LAYOUT, metal(3.5f));
    // 圧縮機 [6-03]（電動モーターのピストン圧縮機。隣のガスホルダーのガスを鋼のボンベに 50mol ずつ詰める）
    public static final Machine<CompressorBlockEntity> COMPRESSOR = Machine.electric("compressor", CompressorBlockEntity::new, CompressorBlockEntity.LAYOUT, metal(3.5f));

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}
}
