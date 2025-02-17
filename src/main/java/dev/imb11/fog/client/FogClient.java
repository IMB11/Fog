package dev.imb11.fog.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.imb11.fog.client.command.FogClientCommands;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.FogResourceReloader;
import dev.imb11.fog.client.util.FogKeybinds;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.resource.ResourceType;
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
		*//*?} else {*/
		/*return net.neoforged.fml.loading.FMLLoader.getGamePath().resolve("config").resolve(MOD_ID).resolve(configFileName + "." + configExtension);
		*//*?}*/
	}

	public static boolean isModInstalled(String modid) {
		/*? if fabric {*/
		return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modid);
		/*?} elif forge {*/
		/*return net.minecraftforge.fml.loading.FMLLoader.getLoadingModList().getModFileById(modid) != null;
		*//*?} else {*/
		/*return net.neoforged.fml.loading.FMLLoader.getLoadingModList().getModFileById(modid) != null;
		*//*?}*/
	}

	public static Path getConfigFolder() {
		/*? if fabric {*/
		return net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("config").resolve(MOD_ID);
		/*?} elif forge {*/
		/*return net.minecraftforge.fml.loading.FMLLoader.getGamePath().resolve("config").resolve(MOD_ID);
		*//*?} else {*/
		/*return net.neoforged.fml.loading.FMLLoader.getGamePath().resolve("config").resolve(MOD_ID);
		*//*?}*/
	}
	
	public static void initialize() {
		LOGGER.info("Loading {}.", MOD_NAME);

		FogConfig.load();
		FogClientCommands.register();

		FogKeybinds.init();

		/*? if <1.21.4 {*/
		/*ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new FogResourceReloader());
		*//*?} else {*/
		ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new FogResourceReloader(), FogResourceReloader.IDENTIFIER);
		/*?}*/
		ClientTickEvent.CLIENT_LEVEL_POST.register((world) -> FogManager.getInstance().onEndTick(world));
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((clientPlayerEntity) -> {
			FogManager.INSTANCE = new FogManager();
			FogRegistry.resetCaches();
		});
	}
}
