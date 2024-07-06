/*? if fabric {*/
package dev.imb11.fog.fabric.client;

import dev.imb11.fog.client.FogClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FogClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FogClient.initialize();
	}
}
/*?}*/
