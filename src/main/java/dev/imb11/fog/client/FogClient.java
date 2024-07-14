package dev.imb11.fog.client;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.imb11.fog.client.resource.FogResourceReloader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceType;
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

		ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new FogResourceReloader());
		ClientTickEvent.CLIENT_LEVEL_POST.register((world) -> FogManager.getInstance().onEndTick(world));
		// TODO: Add a client-side command for reloading the config
		ClientCommandRegistrationEvent.EVENT.register(
				(dispatcher, context) -> dispatcher.register(ClientCommandRegistrationEvent.literal("resetFog").executes((e) -> {
					FogManager.INSTANCE = new FogManager();
					return 1;
				})));
	}
}
