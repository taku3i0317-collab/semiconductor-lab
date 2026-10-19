package com.taku3.semiconductorlab.client;

import net.fabricmc.api.ClientModInitializer;

public class SemiconductorLabClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PurityTooltip.register();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}