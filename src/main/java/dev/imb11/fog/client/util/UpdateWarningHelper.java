package dev.imb11.fog.client.util;

import dev.imb11.fog.client.FogClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.nio.file.Files;

public class UpdateWarningHelper {
	// TODO: Bump this every time you modify something within the datagen classes.
	public static int getDatagenVersion() {
		return 1;
	}

	public static void checkVersion() {
		var datagenVersionPath = FogClient.getConfigFolder().resolve("datagen.version");

		boolean shouldWarn = true;
		// Check if datagenVersionPath content is < datagenVersion
		if (datagenVersionPath.toFile().exists()) {
			try {
				var version = Integer.parseInt(Files.readString(datagenVersionPath));
				if (version >= getDatagenVersion()) {
					shouldWarn = false;
				}
			} catch (Exception e) {
				// Ignore
			}
		}

		if (shouldWarn) {
			MinecraftClient client = MinecraftClient.getInstance();

			client.setScreen(new ConfirmScreen(
					confirmed -> {
						try {
							Files.writeString(datagenVersionPath, Integer.toString(getDatagenVersion()));
						} catch (Exception ignored) {}
						client.setScreen(new TitleScreen());
					},
					Text.translatable("fog.datagen_warning.title"),
					Text.translatable("fog.datagen_warning.message"),
					ScreenTexts.OK,
					ScreenTexts.OK
			));
			FogClient.LOGGER.info("Datagen warning displayed - you may need to delete the `.minecraft/config/fog/fog_definitions` folder.");
		}
	}
}
