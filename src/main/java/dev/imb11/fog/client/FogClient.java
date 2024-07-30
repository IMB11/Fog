package dev.imb11.fog.client;

import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.FogResourceReloader;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class FogClient {
	public static final @NotNull String MOD_ID = "fog";
	public static final @NotNull String MOD_NAME = "Fog";
	public static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Path getConfigPath(String configFileName, String configExtension) {
		/*? if fabric {*/
		return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).resolve(configFileName + "." + configExtension);
		/*?} elif forge {*/
		/*return net.minecraftforge.fml.loading.FMLLoader.getGamePath().resolve("config").resolve(MOD_ID).resolve(configFileName + "." + configExtension);
		*//*?}*/
	}
	
	public static void initialize() {
		LOGGER.info("Loading {}.", MOD_NAME);

		FogConfig.load();
		FogClientCommands.register();

		ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new FogResourceReloader());
		ClientTickEvent.CLIENT_LEVEL_POST.register((world) -> FogManager.getInstance().onEndTick(world));
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((clientPlayerEntity) -> {
			FogManager.INSTANCE = new FogManager();
			FogRegistry.resetCaches();
		});
	}
}
