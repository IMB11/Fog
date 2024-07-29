/*? if forge {*/
/*package dev.imb11.fog.loaders;

import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import java.util.function.BiFunction;

@Mod("fog")
public class FogClientForge {
	public FogClientForge() {
		FogClient.initialize();

		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(
						(minecraftClient, parent) -> FogConfig.getInstance().getYetAnotherConfigLibInstance().generateScreen(parent)
				)
        );
	}
}
*//*?}*/
