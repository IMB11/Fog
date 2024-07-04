package dev.imb11.fog.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FogClient {
	public static final @NotNull String MOD_ID = "fog";
	public static final @NotNull String MOD_NAME = "Fog";
	public static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void initialize() {
		LOGGER.info("Loading {}.", MOD_NAME);

		ClientTickEvents.END_WORLD_TICK.register((world) -> FogManager.getInstance().onEndTick(world));
	}
}
