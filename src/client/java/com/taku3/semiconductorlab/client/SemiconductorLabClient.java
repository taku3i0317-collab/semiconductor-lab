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
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}