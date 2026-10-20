package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.content.Chemistry;
import com.taku3.semiconductorlab.content.Gas;
import com.taku3.semiconductorlab.content.Heat;
import com.taku3.semiconductorlab.content.Metallurgy;
import com.taku3.semiconductorlab.content.Optics;
import com.taku3.semiconductorlab.content.Power;
import com.taku3.semiconductorlab.content.Semiconductor;
import com.taku3.semiconductorlab.content.Survival;
import com.taku3.semiconductorlab.content.Vacuum;
import com.taku3.semiconductorlab.machine.Machine;
import com.taku3.semiconductorlab.machine.MachineMenu;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

import java.util.HashSet;
import java.util.Set;

public class SemiconductorLabClient implements ClientModInitializer {
	private static final Set<Machine<?>> CUSTOM = new HashSet<>();

	@Override
	public void onInitializeClient() {
		PurityTooltip.register();
		NoteTooltip.register();
		// 専用の画面を持つ機械（IMP-28：画面は MachineScreen を継承）。幹ごとにまとめる（自分の幹の段にだけ足す）
		// 幹0 生存・道具
		screen(Survival.ASSEMBLY_BENCH, AssemblyBenchScreen::new);   // IMP-33 組立台
		SurvivalClient.register();   // IMP3-01 丸木舟 [0-14] の描画
		// 幹1 冶金
		screen(Metallurgy.BESSEMER_CONVERTER, BessemerConverterScreen::new);
		screen(Metallurgy.BLOOMERY, BloomeryScreen::new);   // [1-06] 塊鉄炉
		screen(Metallurgy.BLAST_FURNACE, BlastFurnaceScreen::new);   // [1-09] 高炉
		screen(Metallurgy.HYDROGEN_FURNACE, HydrogenFurnaceScreen::new);
		screen(Metallurgy.SIEMENS_REACTOR, SiemensReactorScreen::new);
		screen(Metallurgy.TRICHLOROSILANE_REACTOR, TrichlorosilaneReactorScreen::new);
		// 幹2 化学
		screen(Chemistry.WOOD_RETORT, WoodRetortScreen::new);
		screen(Chemistry.GAS_RETORT, GasRetortScreen::new);
		screen(Chemistry.LEAD_RETORT, HfRetortScreen::new);
		screen(Chemistry.AMMONIA_STILL, AmmoniaStillScreen::new);
		screen(Chemistry.PEROXIDE_CELL, PeroxideCellScreen::new);   // [2-23] IMP2-01
		// 幹3 熱
		screen(Heat.ARC_FURNACE, ArcFurnaceScreen::new);
		screen(Heat.ACHESON_FURNACE, AchesonFurnaceScreen::new);
		screen(Heat.REVERBERATORY_FURNACE, ReverberatoryFurnaceScreen::new);
		// 幹4 動力・電気
		screen(Power.STEAM_ENGINE, SteamEngineScreen::new);
		screen(Power.STEAM_TURBINE, SteamEngineScreen::new);   // 蒸気機関と同じ画面
		screen(Power.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
		// 幹5 真空
		screen(Vacuum.ROTARY_PUMP, PumpScreen::new);      // [5-04] IMP2-01
		screen(Vacuum.DIFFUSION_PUMP, PumpScreen::new);   // [5-05] IMP2-01
		screen(Vacuum.EVAPORATOR, EvaporatorScreen::new); // [5-14] IMP2-01
		screen(Vacuum.SEALING_STATION, SealingStationScreen::new);
		// 幹6 ガス・電解
		screen(Gas.ELECTROLYZER, ElectrolyzerScreen::new);
		screen(Gas.GAS_HOLDER, GasHolderScreen::new);
		screen(Gas.COMPRESSOR, CompressorScreen::new);
		screen(Gas.AIR_SEPARATION_UNIT, AirSeparationScreen::new);
		screen(Gas.HCL_BURNER, HclBurnerScreen::new);
		// 幹7 光学
		screen(Optics.REDUCTION_CAMERA, ReductionCameraScreen::new);   // [7-11] IMP2-03
		screen(Optics.OXYHYDROGEN_BURNER, OxyhydrogenBurnerScreen::new);
		// 幹9 半導体プロセス
		screen(Semiconductor.DRAFTING_TABLE, DraftingTableScreen::new);   // O-002 製図台
		screen(Semiconductor.PROBE_STATION, ProbeStationScreen::new);   // O-002 検査台
		screen(Semiconductor.ZONE_REFINING_FURNACE, ZoneRefiningFurnaceScreen::new);
		screen(Semiconductor.CRYSTAL_PULLER, CrystalPullerScreen::new);
		screen(Semiconductor.DIFFUSION_FURNACE, DiffusionFurnaceScreen::new);
		screen(Semiconductor.OXIDATION_FURNACE, OxidationFurnaceScreen::new);
		screen(com.taku3.semiconductorlab.content.Metallurgy.BAYER_DIGESTER, BayerDigesterScreen::new);
		screen(com.taku3.semiconductorlab.content.Metallurgy.HALL_HEROULT_CELL, HallHeroultCellScreen::new);
		screen(Semiconductor.ETCH_TANK, EtchTankScreen::new);
		screen(Semiconductor.WAFER_POLISHER, WaferPolisherScreen::new);
		screen(Semiconductor.CLEANING_BENCH, CleaningBenchScreen::new);
		screen(Semiconductor.FZ_PULLER, FzPullerScreen::new);
		screen(Semiconductor.SPIN_COATER, SpinCoaterScreen::new);
		screen(Semiconductor.RESIST_STRIPPER, ResistStripperScreen::new);
		screen(Semiconductor.WAFER_SAW, WaferSawScreen::new);
		screen(Semiconductor.CZ_PULLER, CzPullerScreen::new);
		// 専用の画面を書いていない機械は、ふつうの画面（電力・進み具合・状態）
		for (Machine<?> m : Machine.all())
			if (m.menu != null && !CUSTOM.contains(m)) MenuScreens.register(m.menu, SimpleMachineScreen::new);
	}

	/** 機械に専用の画面をつなぐ */
	public static <U extends MachineScreen> void screen(Machine<?> m, MenuScreens.ScreenConstructor<MachineMenu, U> ctor) {
		MenuScreens.register(m.menu, ctor);
		CUSTOM.add(m);
	}
}
