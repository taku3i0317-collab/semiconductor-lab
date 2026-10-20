package com.taku3.semiconductorlab.client;

import com.taku3.semiconductorlab.ModMachines;
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
		// 専用の画面を持つ機械（IMP-28：画面は MachineScreen を継承）
		screen(ModMachines.ZONE_REFINING_FURNACE, ZoneRefiningFurnaceScreen::new);
		screen(ModMachines.STEAM_ENGINE, SteamEngineScreen::new);
		screen(ModMachines.STEAM_TURBINE, SteamEngineScreen::new);   // 蒸気機関と同じ画面
		screen(ModMachines.ELECTROLYZER, ElectrolyzerScreen::new);
		screen(ModMachines.GAS_HOLDER, GasHolderScreen::new);
		screen(ModMachines.BESSEMER_CONVERTER, BessemerConverterScreen::new);
		screen(ModMachines.HYDROGEN_FURNACE, HydrogenFurnaceScreen::new);
		screen(ModMachines.COMPRESSOR, CompressorScreen::new);
		screen(ModMachines.AIR_SEPARATION_UNIT, AirSeparationScreen::new);
		screen(ModMachines.CRYSTAL_PULLER, CrystalPullerScreen::new);
		screen(ModMachines.ARC_FURNACE, ArcFurnaceScreen::new);
		screen(ModMachines.DIFFUSION_FURNACE, DiffusionFurnaceScreen::new);
		screen(ModMachines.WAFER_SAW, WaferSawScreen::new);
		screen(ModMachines.CZ_PULLER, CzPullerScreen::new);
		screen(ModMachines.SIEMENS_REACTOR, SiemensReactorScreen::new);
		screen(ModMachines.TRICHLOROSILANE_REACTOR, TrichlorosilaneReactorScreen::new);
		screen(ModMachines.SEALING_STATION, SealingStationScreen::new);
		screen(ModMachines.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
		screen(ModMachines.ACHESON_FURNACE, AchesonFurnaceScreen::new);
		screen(ModMachines.REVERBERATORY_FURNACE, ReverberatoryFurnaceScreen::new);
		screen(ModMachines.OXYHYDROGEN_BURNER, OxyhydrogenBurnerScreen::new);
		screen(ModMachines.HCL_BURNER, HclBurnerScreen::new);
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
