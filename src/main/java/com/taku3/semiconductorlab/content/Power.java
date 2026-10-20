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
 * 幹4 動力・電気（docs/TECH_TREE.md の「幹4」のノード）。IMP-28 で ModItems・ModBlocks・ModMachines から分けた。
 * この幹のノードを足すときは、このファイル（と src/main/lang/power/、src/gametest/.../PowerTests.java）だけを書き換える。
 */
public final class Power {
    private Power() {}

    // ===== アイテム =====
    public static final Item INDUCTION_COIL        = Reg.item("induction_coil");
    public static final Item ELECTRIC_MOTOR        = Reg.item("electric_motor");
    // 高周波発振器 [4-09]（真空管2本＋コイル＋ライデン瓶の発振回路。誘導加熱の電源。旧・仮アイテム hf_power_supply を置換）
    public static final Item HF_OSCILLATOR         = Reg.item("hf_oscillator");
    public static final Item LEYDEN_JAR            = Reg.item("leyden_jar");       // コンデンサ（ライデン瓶 1745：ガラス瓶の内外に金属箔）
    // --- 電気の部品（v0.2a）---
    public static final Item MAGNET                = Reg.item("magnet");   // 天然磁石（磁鉄鉱のうち磁化したもの＝ロードストーン）
    public static final Item DYNAMO                = Reg.item("dynamo");   // 磁石＋銅コイル＋鉄：ファラデーの電磁誘導
    public static final Item SELF_EXCITED_DYNAMO   = Reg.item("self_excited_dynamo"); // [4-05] ジーメンス1866：鋼の鉄心＋コイル×4、磁石不要（残留磁気で自分を励磁）
    // --- 電気炉（[4-12] 抵抗加熱の発熱体）---
    public static final Item GRAPHITE_ROD          = Reg.item("graphite_rod");      // 黒鉛棒（電気炉の発熱体・アチソン炉の電極）

    // ===== 機械（BE を持つブロック、Machine 1行） =====
    // 発電機（v0.2a）：水車＋ダイナモ [4-01]／ボイラー＋蒸気機関＋ダイナモ [4-02][4-14]／蒸気タービン [4-06]、鉛蓄電池 [4-07]
    public static final Machine<WaterWheelBlockEntity> WATER_WHEEL = Power.generator("water_wheel", WaterWheelBlockEntity::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD)).build();
    public static final Machine<SteamTurbineBlockEntity> STEAM_TURBINE = Power.generator("steam_turbine", SteamTurbineBlockEntity::new, metal(4.0f)).layout(SteamEngineBlockEntity.LAYOUT).build();
    public static final Machine<LeadAcidBatteryBlockEntity> LEAD_ACID_BATTERY = Machine.builder("lead_acid_battery", LeadAcidBatteryBlockEntity::new)
            .block(LeadAcidBatteryBlock::new).ticker(LeadAcidBatteryBlockEntity::tick).energy((be, dir) -> be.storageFor(dir))   // 全面で充放電（タスク06）
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(2.5f).sound(SoundType.GLASS)).build();
    public static final Machine<SteamEngineBlockEntity> STEAM_ENGINE = Power.generator("steam_engine", SteamEngineBlockEntity::new, metal(3.5f)).layout(SteamEngineBlockEntity.LAYOUT).build();
    // 電気炉 [4-12]（耐火れんがの箱＋黒鉛棒の発熱体。抵抗加熱で約1500℃）。加熱中は光る
    public static final Machine<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Machine.electric("electric_furnace", ElectricFurnaceBlockEntity::new, ElectricFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 12));
    // 銅ケーブル（v0.2a）。受け口を持たない（タスク06：導体。CableNetwork がまとめて配電する）
    public static final Machine<CableBlockEntity> COPPER_CABLE = Machine.builder("copper_cable", CableBlockEntity::new)
            .block(CableBlock::new).ticker(CableBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0f).sound(SoundType.COPPER).noOcclusion()).build();

    /** 発電機（GeneratorBlockEntity を継承）：ブロックは GeneratorBlock、全面から電気を取り出せる */
    public static <T extends GeneratorBlockEntity> Machine.Builder<T> generator(String name, net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory<T> f, BlockBehaviour.Properties props) {
        return Machine.builder(name, f).block(GeneratorBlock::new).ticker(GeneratorBlockEntity::tick).energy((be, dir) -> be.getStorage()).props(props);
    }

    /** Content.initialize() から呼ぶ（このクラスを読み込んで、上の登録を走らせる） */
    static void init() {}

    /** 組立台のレシピ（IMP-33。Content.initialize が全部の幹のあとで呼ぶ） */
    static void assemblies() {
        // [4-09] 高周波発振器：真空管2本（発振）＋共振回路（コイル＋ライデン瓶）＋鉄の枠＋配線（前は作業台）
        com.taku3.semiconductorlab.machine.Assembly.recipe("hf_oscillator", HF_OSCILLATOR)
                .part("oscillate", Vacuum.VACUUM_TUBE, 2)
                .part("tank_coil", INDUCTION_COIL, 1)
                .part("tank_capacitor", LEYDEN_JAR, 1)
                .part("wiring", COPPER_CABLE.block.asItem(), 1)
                .part("frame", net.minecraft.world.item.Items.IRON_INGOT, 4)
                .seconds(5).build();
    }
}
