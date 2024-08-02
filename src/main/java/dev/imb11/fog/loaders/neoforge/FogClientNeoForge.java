/*? if neoforge {*/
/*package dev.imb11.fog.loaders.neoforge;

import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.config.FogConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

@Mod("fog")
public class FogClientNeoForge {
	public FogClientNeoForge() {
		FogClient.initialize();

		/^? if =1.20.4 {^//^
		ModLoadingContext.get().registerExtensionPoint(
				net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory(
						(client, parent) -> FogConfig.getInstance().getYetAnotherConfigLibInstance().generateScreen(parent)
				)
        );
        ^//^?} else {^/
		ModLoadingContext.get().getActiveContainer().registerExtensionPoint(
				net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, (client, parent) -> FogConfig.getInstance().getYetAnotherConfigLibInstance().generateScreen(parent)
		);
		/^?}^/
	}
}
*//*?}*/
