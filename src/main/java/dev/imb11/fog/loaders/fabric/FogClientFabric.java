/*? if fabric {*/
package dev.imb11.fog.loaders.fabric;

import dev.imb11.fog.client.FogClient;
import net.fabricmc.api.ClientModInitializer;

public class FogClientFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FogClient.initialize();
	}
}
/*?}*/
