/*? if fabric && >=1.20.6 {*/
package dev.imb11.fog.loaders.fabric.datagen;

import dev.imb11.fog.client.FogClient;
import dev.imb11.fog.client.resource.FogResourceReloader;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class FogDatagenFabric implements DataGeneratorEntrypoint {
	public static void postDatagen(@NotNull Path outputDirectory) {
		// Traverse and process JSON files
		try (Stream<Path> paths = Files.walk(outputDirectory.resolve("assets"))) {
			paths.filter(path -> path.toString().endsWith(".json") && path.toString().contains(FogResourceReloader.FOG_DEFINITIONS_FOLDER_NAME))
			     .forEach(path -> {
				     try {
					     Path relativePath = outputDirectory.relativize(path);
					     Path targetPath = outputDirectory.resolve("packed").resolve(relativePath);
					     Files.createDirectories(targetPath.getParent());
					     Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
					     Files.delete(path);
				     } catch (IOException e) {
					     throw new RuntimeException("Failed to copy file: " + path, e);
				     }
			     });
		} catch (IOException e) {
			throw new RuntimeException("Failed to traverse assets directory", e);
		}
	}

	@Override
	public void onInitializeDataGenerator(@NotNull FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(VanillaFogDefinitionProvider::new);
	}

	@Override
	public @Nullable String getEffectiveModId() {
		return FogClient.MOD_ID;
	}
}
/*?}*/
