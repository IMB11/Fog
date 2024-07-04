/*? if fabric { ?*/
package dev.imb11.fog.fabric.client;

import dev.imb11.fog.FogManager;
import dev.imb11.fog.client.FogClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

@Environment(EnvType.CLIENT)
public class FogClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// TODO: Change to a client-side command
		// TODO: Add a client-side command for reloading the config
		// TODO: Move into FogClient#initialize
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("resetFog").executes(context -> {
				FogManager.INSTANCE = new FogManager();
				return 1;
			}));
		});

		FogClient.initialize();
	}
}
/*? } ?*/
