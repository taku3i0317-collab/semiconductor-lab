package com.taku3.semiconductorlab.client;

import net.fabricmc.api.ClientModInitializer;
import com.taku3.semiconductorlab.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;

public class SemiconductorLabClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PurityTooltip.register();
		NoteTooltip.register();
		MenuScreens.register(ModMenus.ZONE_REFINING_FURNACE, ZoneRefiningFurnaceScreen::new);
		MenuScreens.register(ModMenus.STEAM_ENGINE, SteamEngineScreen::new);
		MenuScreens.register(ModMenus.ELECTROLYZER, ElectrolyzerScreen::new);
		MenuScreens.register(ModMenus.GAS_HOLDER, GasHolderScreen::new);
		MenuScreens.register(ModMenus.BESSEMER_CONVERTER, BessemerConverterScreen::new);
		MenuScreens.register(ModMenus.HYDROGEN_FURNACE, HydrogenFurnaceScreen::new);
		MenuScreens.register(ModMenus.COMPRESSOR, CompressorScreen::new);
		MenuScreens.register(ModMenus.AIR_SEPARATION, AirSeparationScreen::new);
		MenuScreens.register(ModMenus.CRYSTAL_PULLER, CrystalPullerScreen::new);
		MenuScreens.register(ModMenus.SEALING_STATION, SealingStationScreen::new);
		MenuScreens.register(ModMenus.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
		MenuScreens.register(ModMenus.ACHESON_FURNACE, AchesonFurnaceScreen::new);
		MenuScreens.register(ModMenus.REVERBERATORY_FURNACE, ReverberatoryFurnaceScreen::new);
		MenuScreens.register(ModMenus.OXYHYDROGEN_BURNER, OxyhydrogenBurnerScreen::new);
		MenuScreens.register(ModMenus.HCL_BURNER, HclBurnerScreen::new);
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}