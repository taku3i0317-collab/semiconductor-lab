package com.taku3.semiconductorlab.content;

import com.taku3.semiconductorlab.ModDataComponents;
import com.taku3.semiconductorlab.Purity;
import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.item.*;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineBlock;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
import com.taku3.semiconductorlab.SemiconductorLab;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.taku3.semiconductorlab.machine.Machine.lit;
import static com.taku3.semiconductorlab.machine.Machine.metal;
import static com.taku3.semiconductorlab.machine.Machine.stone;

/**
 * 幹0 生存・道具（docs/TECH_TREE.md の「幹0」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/survival/、src/gametest/.../SurvivalTests.java）だけを書き換える。
 */
public final class Survival {
    private Survival() {}

    // ===== アイテム =====
    // --- [0-01] 打製石器（オルドワン 約260万年前〜、ハンドアックス＝アシュール 約170万年前〜）。柄はない。バニラの石の道具（丸石＋棒）は「柄を付けた石器」として扱う ---
    /** 打製石器の修理に使える石（打ち欠き直す）：火打ち石・黒曜石 */
    public static final TagKey<Item> KNAPPING_STONES = TagKey.create(Registries.ITEM, SemiconductorLab.id("knapping_stones"));
    /** フリントの剥片：小さな刃物。耐久は小さめ */
    public static final ToolMaterial FLINT_FLAKE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 32, 2.0f, 0.5f, 5, KNAPPING_STONES);
    /** 黒曜石の剥片：鋭い（刃先は数 nm）が、もろい */
    public static final ToolMaterial OBSIDIAN_FLAKE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 16, 2.0f, 1.5f, 5, KNAPPING_STONES);
    /** ハンドアックス：両面を打ち欠いた握り斧。木の斧より少し速く切れるが、柄がないので振りは遅い */
    public static final ToolMaterial HAND_AXE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 80, 3.0f, 1.0f, 5, KNAPPING_STONES);

    public static final Item HAMMERSTONE           = Reg.item("hammerstone", HammerstoneItem::new, new Item.Properties().stacksTo(1));
    public static final Item FLINT_FLAKE           = Reg.item("flint_flake", new Item.Properties().sword(FLINT_FLAKE_MATERIAL, 1.0f, -1.8f));
    public static final Item OBSIDIAN_FLAKE        = Reg.item("obsidian_flake", new Item.Properties().sword(OBSIDIAN_FLAKE_MATERIAL, 1.0f, -1.8f));
    public static final Item HAND_AXE              = Reg.item("hand_axe", new Item.Properties().axe(HAND_AXE_MATERIAL, 5.0f, -3.4f));
    // --- [0-03] 土器（約2万年前〜）。粘土は約500〜600℃で結晶水が抜け、もう水で戻らない ---
    public static final Item RAW_CLAY_POT          = Reg.item("raw_clay_pot", new Item.Properties().stacksTo(16));
    public static final Item EARTHENWARE_POT       = Reg.item("earthenware_pot", new Item.Properties().stacksTo(16));
    // --- [0-08] 木工（新石器時代の磨製石斧）。打ち欠いた斧を砂と水で研ぐと、剥離の傷が消えて欠けにくい刃になる。柄を付ける ---
    public static final ToolMaterial POLISHED_STONE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 200, 4.5f, 1.0f, 5, KNAPPING_STONES);
    public static final Item POLISHED_STONE_AXE    = Reg.item("polished_stone_axe", new Item.Properties().axe(POLISHED_STONE_MATERIAL, 6.0f, -3.1f));
    // --- [0-06] 亜麻の茎・浸水した茎・繊維・リネンの糸・リネン ---
    public static final Item FLAX_STALKS           = Reg.item("flax_stalks");
    public static final Item RETTED_FLAX           = Reg.item("retted_flax");
    public static final Item FLAX_FIBER            = Reg.item("flax_fiber");
    public static final Item LINEN_THREAD          = Reg.item("linen_thread");
    public static final Item LINEN_CLOTH           = Reg.item("linen_cloth");
    // --- [0-07] 羊毛と紡錘車（はずみ車）。紡錘車＝人類最古のはずみ車。棒だけの紡錘はすぐ止まる ---
    /** 紡錘に付けられる紡錘車（土器・石の円盤） */
    public static final TagKey<Item> SPINDLE_WHORLS = TagKey.create(Registries.ITEM, SemiconductorLab.id("spindle_whorls"));
    public static final Item RAW_CLAY_WHORL        = Reg.item("raw_clay_whorl");
    public static final Item EARTHENWARE_WHORL     = Reg.item("earthenware_whorl");
    public static final Item STONE_WHORL           = Reg.item("stone_whorl");
    public static final Item SPINDLE_SHAFT         = Reg.item("spindle_shaft", p -> new SpindleItem(p, 20), new Item.Properties().stacksTo(1));
    public static final Item SPINDLE               = Reg.item("spindle", p -> new SpindleItem(p, 80), new Item.Properties().stacksTo(1));
    public static final Item CARDED_WOOL           = Reg.item("carded_wool");
    public static final Item WOOLEN_YARN           = Reg.item("woolen_yarn");
    // --- [0-18] ロープ（糸を撚り合わせ、さらに逆向きに撚り合わせる） ---
    public static final Item ROPE                  = Reg.item("rope");
    /** 砂利を壊したとき（火打ち石が出なかったとき）に、たたき石（河原の丸石）が混ざる確率 */
    public static final float HAMMERSTONE_FROM_GRAVEL = 0.1f;

    // --- 石灰（[0-10] 方解石 CaCO3 → 焼成 → 生石灰 CaO、[0-11] CaO + H2O → 消石灰 Ca(OH)2）---
    public static final Item QUICKLIME             = Reg.item("quicklime");
    public static final Item SLAKED_LIME           = Reg.item("slaked_lime");
    public static final Item BITTERN               = Reg.item("bittern");           // にがり（海水を煮詰めた母液、主成分 MgCl2）

    // ===== ブロック =====
    // --- [0-04] 農耕：ヒトツブコムギ（野生種は熟すと穂が落ちる。まれに出る「穂が落ちない」種を選んで植え続けると栽培種）。種＝ブロックのアイテム ---
    public static final Block WILD_EINKORN = Reg.block("wild_einkorn", p -> new EinkornCropBlock(p, true), BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
    public static final Block EINKORN      = Reg.block("einkorn", p -> new EinkornCropBlock(p, false), BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
    /** 草（短い草・背の高い草）を刈ったとき、種が出なかったら野生のヒトツブコムギの種が混ざる確率 */
    public static final float WILD_EINKORN_FROM_GRASS = 0.05f;
    // --- [0-06] 織物：亜麻（茎 → 水に浸ける＝レッティング → たたいて繊維 → 撚って糸 → 織ってリネン） ---
    public static final Block FLAX         = Reg.block("flax", FlaxCropBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
    /** 同じく、亜麻の種が混ざる確率（ヒトツブコムギが出なかったとき） */
    public static final float FLAX_FROM_GRASS = 0.04f;

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // [0-16] 塩田（揚げ浜式。海水を砂にまいて天日で濃くし、釜で煮詰める）。海・浜バイオームで、横に水があり、空が見えて晴れているときだけ塩がたまる
    public static final Machine<SaltPanBlockEntity> SALT_PAN = Machine.builder("salt_pan", SaltPanBlockEntity::new)
            .block(SaltPanBlock::new).ticker(SaltPanBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.8f).sound(SoundType.SAND)).build();

    // 組立台（IMP-33、作り方の3段の2段目）：木の作業机＋万力。部品表で組み立てる。電気なし
    public static final Machine<AssemblyBenchBlockEntity> ASSEMBLY_BENCH = Machine.builder("assembly_bench", AssemblyBenchBlockEntity::new)
            .ticker(AssemblyBenchBlockEntity::tick).layout(AssemblyBenchBlockEntity.LAYOUT)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD)).build();

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {
        // [0-04] 草むらの中に野生の麦がまじっている
        LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            if (!table.is(Blocks.SHORT_GRASS.getLootTable().orElseThrow()) && !table.is(Blocks.TALL_GRASS.getLootTable().orElseThrow())) return;
            if (!drops.isEmpty()) return;
            float r = ctx.getRandom().nextFloat();
            if (r < WILD_EINKORN_FROM_GRASS) drops.add(new ItemStack(WILD_EINKORN));
            else if (r < WILD_EINKORN_FROM_GRASS + FLAX_FROM_GRASS) drops.add(new ItemStack(FLAX));
        });
        // [0-01] 砂利の中にはときどき丸く硬い河原石がある → たたき石
        LootTableEvents.MODIFY_DROPS.register((table, ctx, drops) -> {
            if (!table.is(Blocks.GRAVEL.getLootTable().orElseThrow())) return;
            if (drops.stream().anyMatch(st -> st.is(Items.GRAVEL)) && ctx.getRandom().nextFloat() < HAMMERSTONE_FROM_GRAVEL)
                drops.add(new ItemStack(HAMMERSTONE));
        });
    }
}
