package com.taku3.semiconductorlab;

import com.taku3.semiconductorlab.block.*;
import com.taku3.semiconductorlab.energy.*;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineBlock;
import com.taku3.semiconductorlab.machine.MachineEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.taku3.semiconductorlab.machine.Machine.lit;
import static com.taku3.semiconductorlab.machine.Machine.metal;
import static com.taku3.semiconductorlab.machine.Machine.stone;

/**
 * BE を持つブロック（機械・発電機・ケーブルなど）の登録（IMP-28）。1台＝1行。
 * ブロックは X.block、BE の種類は X.type。画面のある機械は BE の LAYOUT（スロットの並び）を渡す。
 */
public class ModMachines {
    // 帯域精製炉（Pfann, 1952）。電気で動く（組み込みの高周波発振器 [4-09]）。点灯中は光る
    public static final Machine<ZoneRefiningFurnaceBlockEntity> ZONE_REFINING_FURNACE = Machine.electric("zone_refining_furnace", ZoneRefiningFurnaceBlockEntity::new, ZoneRefiningFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 13));

    // 発電機（v0.2a）：水車＋ダイナモ／ボイラー＋蒸気機関＋ダイナモ。[B-12] 太陽電池（昼・空・雨なしで 60W）
    public static final Machine<SolarCellBlockEntity> SOLAR_CELL = generator("solar_cell", SolarCellBlockEntity::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).requiresCorrectToolForDrops().strength(2.0f).sound(SoundType.GLASS)).build();
    public static final Machine<WaterWheelBlockEntity> WATER_WHEEL = generator("water_wheel", WaterWheelBlockEntity::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD)).build();
    public static final Machine<SteamTurbineBlockEntity> STEAM_TURBINE = generator("steam_turbine", SteamTurbineBlockEntity::new, metal(4.0f)).layout(SteamEngineBlockEntity.LAYOUT).build();
    public static final Machine<LeadAcidBatteryBlockEntity> LEAD_ACID_BATTERY = Machine.builder("lead_acid_battery", LeadAcidBatteryBlockEntity::new)
            .block(LeadAcidBatteryBlock::new).ticker(LeadAcidBatteryBlockEntity::tick).energy((be, dir) -> be.storageFor(dir))   // 全面で充放電（タスク06）
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(2.5f).sound(SoundType.GLASS)).build();
    public static final Machine<SteamEngineBlockEntity> STEAM_ENGINE = generator("steam_engine", SteamEngineBlockEntity::new, metal(3.5f)).layout(SteamEngineBlockEntity.LAYOUT).build();

    // 電解槽 [6-01]（ガラス槽＋黒鉛電極）。電気を消費する機械の第1号
    public static final Machine<ElectrolyzerBlockEntity> ELECTROLYZER = Machine.electric("electrolyzer", ElectrolyzerBlockEntity::new, ElectrolyzerBlockEntity.LAYOUT, metal(3.0f).sound(SoundType.GLASS));

    // ガスホルダー [6-02]（水封式の低圧ガスタンク。鉄のベル＋水槽）。電気は使わない
    public static final Machine<GasHolderBlockEntity> GAS_HOLDER = Machine.builder("gas_holder", GasHolderBlockEntity::new)
            .block(MachineEntityBlock::new).ticker(GasHolderBlockEntity::tick).layout(GasHolderBlockEntity.LAYOUT).props(metal(3.0f)).build();

    // ベッセマー転炉 [1-26]（鉄の殻＋粘土の内張り＋底の羽口。送風は電動）。送風中は炉口が光る
    public static final Machine<BessemerConverterBlockEntity> BESSEMER_CONVERTER = Machine.electric("bessemer_converter", BessemerConverterBlockEntity::new, BessemerConverterBlockEntity.LAYOUT, lit(metal(3.5f), 13));

    // 水素炉 [1-28]（耐火れんがの管状炉＋水素の流れ。WO3 の水素還元と W 粉の通電焼結）。加熱中は光る
    public static final Machine<HydrogenFurnaceBlockEntity> HYDROGEN_FURNACE = Machine.electric("hydrogen_furnace", HydrogenFurnaceBlockEntity::new, HydrogenFurnaceBlockEntity.LAYOUT, lit(metal(3.5f), 10));

    // 電気炉 [4-12]（耐火れんがの箱＋黒鉛棒の発熱体。抵抗加熱で約1500℃）。加熱中は光る
    public static final Machine<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Machine.electric("electric_furnace", ElectricFurnaceBlockEntity::new, ElectricFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 12));

    // アチソン炉 [3-10]（1896。耐火れんがの箱に炭素＋砂、両端の黒鉛電極から中身に直接通電。約2500℃で人造黒鉛）。赤熱すると光る
    public static final Machine<AchesonFurnaceBlockEntity> ACHESON_FURNACE = Machine.electric("acheson_furnace", AchesonFurnaceBlockEntity::new, AchesonFurnaceBlockEntity.LAYOUT, lit(stone(3.5f), 15));

    // 反射炉 [3-06]（焙焼炉。燃焼室と炉床を分け、炎の熱を天井で反射させる。石炭で硫化鉱を焼き、SO2 を集める）。燃料式（電気の受け口なし）。燃焼中は光る
    public static final Machine<ReverberatoryFurnaceBlockEntity> REVERBERATORY_FURNACE = Machine.builder("reverberatory_furnace", ReverberatoryFurnaceBlockEntity::new)
            .ticker(ReverberatoryFurnaceBlockEntity::tick).layout(ReverberatoryFurnaceBlockEntity.LAYOUT)
            .props(lit(stone(3.5f).mapColor(MapColor.COLOR_RED), 13)).build();

    // 酸水素バーナー [7-02]（ヘアの吹管 1801、ゴーダン 1839。水素＋酸素ボンベの炎 約2800℃で石英を溶かす）。ガスボンベ式（電気の受け口なし）。炎が出ている間は光る
    public static final Machine<OxyhydrogenBurnerBlockEntity> OXYHYDROGEN_BURNER = Machine.builder("oxyhydrogen_burner", OxyhydrogenBurnerBlockEntity::new)
            .ticker(OxyhydrogenBurnerBlockEntity::tick).layout(OxyhydrogenBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f), 12)).build();

    // 塩酸合成炉 [6-06]（酸水素バーナーの派生。石英の火口で水素を塩素中で燃やし H2 + Cl2 → 2HCl、吸収塔で水に溶かす）。ガス式（電気の受け口なし）。燃焼中は光る
    public static final Machine<HclBurnerBlockEntity> HCL_BURNER = Machine.builder("hcl_burner", HclBurnerBlockEntity::new)
            .ticker(HclBurnerBlockEntity::tick).layout(HclBurnerBlockEntity.LAYOUT).props(lit(metal(3.5f).sound(SoundType.GLASS), 10)).build();

    // 第2章 シリコン（[1-16] [1-17] [9-08] [9-12] [9-16] [3-15]）
    public static final Machine<TrichlorosilaneReactorBlockEntity> TRICHLOROSILANE_REACTOR = Machine.electric("trichlorosilane_reactor", TrichlorosilaneReactorBlockEntity::new, TrichlorosilaneReactorBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<SiemensReactorBlockEntity> SIEMENS_REACTOR = Machine.electric("siemens_reactor", SiemensReactorBlockEntity::new, SiemensReactorBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<CzPullerBlockEntity> CZ_PULLER = Machine.electric("cz_puller", CzPullerBlockEntity::new, CzPullerBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<WaferSawBlockEntity> WAFER_SAW = Machine.electric("wafer_saw", WaferSawBlockEntity::new, WaferSawBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<DiffusionFurnaceBlockEntity> DIFFUSION_FURNACE = Machine.electric("diffusion_furnace", DiffusionFurnaceBlockEntity::new, DiffusionFurnaceBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<ArcFurnaceBlockEntity> ARC_FURNACE = Machine.electric("arc_furnace", ArcFurnaceBlockEntity::new, ArcFurnaceBlockEntity.LAYOUT, metal(4.0f));
    public static final Machine<CrystalPullerBlockEntity> CRYSTAL_PULLER = Machine.electric("crystal_puller", CrystalPullerBlockEntity::new, CrystalPullerBlockEntity.LAYOUT, metal(3.5f));
    public static final Machine<AirSeparationBlockEntity> AIR_SEPARATION_UNIT = Machine.electric("air_separation_unit", AirSeparationBlockEntity::new, AirSeparationBlockEntity.LAYOUT, metal(3.5f));
    // 圧縮機 [6-03]（電動モーターのピストン圧縮機。隣のガスホルダーのガスを鋼のボンベに 50mol ずつ詰める）
    public static final Machine<CompressorBlockEntity> COMPRESSOR = Machine.electric("compressor", CompressorBlockEntity::new, CompressorBlockEntity.LAYOUT, metal(3.5f));

    // 真空容器 [5-11]（ガラスのベルジャー＋鉄の台板）。中の圧力を持つ
    public static final Machine<VacuumChamberBlockEntity> VACUUM_CHAMBER = Machine.builder("vacuum_chamber", VacuumChamberBlockEntity::new)
            .block(VacuumChamberBlock::new).ticker(VacuumChamberBlockEntity::tick).props(metal(2.0f).sound(SoundType.GLASS)).build();
    // シュプレンゲル水銀ポンプ [5-15]
    public static final Machine<SprengelPumpBlockEntity> SPRENGEL_PUMP = Machine.builder("sprengel_pump", SprengelPumpBlockEntity::new)
            .block(SprengelPumpBlock::new).ticker(SprengelPumpBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(1.0f).sound(SoundType.GLASS).noOcclusion()).build();

    // 封止台 [5-02]（真空管の排気・封じ切り台）。隣の真空容器が十分低圧のときだけ組立品を封じる。動作中は光る
    public static final Machine<SealingStationBlockEntity> SEALING_STATION = Machine.electric("sealing_station", SealingStationBlockEntity::new, SealingStationBlockEntity.LAYOUT, lit(metal(3.0f), 7));

    // 銅ケーブル（v0.2a）。受け口を持たない（タスク06：導体。CableNetwork がまとめて配電する）
    public static final Machine<CableBlockEntity> COPPER_CABLE = Machine.builder("copper_cable", CableBlockEntity::new)
            .block(CableBlock::new).ticker(CableBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.0f).sound(SoundType.COPPER).noOcclusion()).build();

    // [0-16] 塩田（揚げ浜式。海水を砂にまいて天日で濃くし、釜で煮詰める）。海・浜バイオームで、横に水があり、空が見えて晴れているときだけ塩がたまる
    public static final Machine<SaltPanBlockEntity> SALT_PAN = Machine.builder("salt_pan", SaltPanBlockEntity::new)
            .block(SaltPanBlock::new).ticker(SaltPanBlockEntity::tick)
            .props(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.8f).sound(SoundType.SAND)).build();

    /** 発電機（GeneratorBlockEntity を継承）：ブロックは GeneratorBlock、全面から電気を取り出せる */
    private static <T extends GeneratorBlockEntity> Machine.Builder<T> generator(String name, net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory<T> f, BlockBehaviour.Properties props) {
        return Machine.builder(name, f).block(GeneratorBlock::new).ticker(GeneratorBlockEntity::tick).energy((be, dir) -> be.getStorage()).props(props);
    }

    public static void initialize() {}
}
