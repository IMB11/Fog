package dev.imb11.fog.client.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.imb11.fog.client.FogClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class FogResourceUnpacker {
	public static final Path UNPACKED_PATH = FogClient.getConfigFolder().resolve(FogResourceReloader.FOG_DEFINITIONS_FOLDER_NAME);

	private static final Set<String> NAMESPACES = new HashSet<>();
	private static final Path README_PATH = UNPACKED_PATH.resolve("README.txt");
	private static final Path META_PATH = UNPACKED_PATH.resolve("pack.mcmeta");

	public static @NotNull Set<String> getNamespaces() {
		return NAMESPACES;
	}

	public static void tryUnpack() throws IOException, URISyntaxException {
		if (System.getProperty("fabric-api.datagen") != null) {
			FogClient.LOGGER.info("Skipping unpacking of resource pack as we are in a data generation environment.");
			return;
		}

		try {
			Files.createDirectories(UNPACKED_PATH);
		} catch (IOException e) {
			FogClient.LOGGER.error(
					"Exception thrown while creating folders for the unpacked config resource pack (path: {}): {}", UNPACKED_PATH, e);
		}

		if (!README_PATH.toFile().exists()) {
			FogClient.LOGGER.info("Creating README.txt in unpacked resource pack folder.");
			Files.writeString(
					README_PATH,
					"This folder contains the unpacked fog definitions.\nYou can edit these files to customize the fog in your game.\nFor more information, visit https://docs.imb11.dev/fog/"
			);
		}

		/*? if =1.20.1 {*/
		/*int packFormat = 15;
		 *//*?} elif =1.20.4 {*/
		/*int packFormat = 26;
		 *//*?} elif =1.20.6 {*/
		/*int packFormat = 41;
		 *//*?} else {*/
		int packFormat = 45;
		/*?}*/

		if (!META_PATH.toFile().exists()) {
			FogClient.LOGGER.info("Creating pack.mcmeta in unpacked resource pack folder.");
			Files.writeString(META_PATH, String.format("""
					{
					  "pack": {
					    "description": "An expansive and dynamic overhaul to Minecraft's fog rendering system.",
					    "pack_format": %d
					  }
					}
					""", packFormat)
			);
		} else {
			// Ensure pack format is set to packFormat
			String metaContent = Files.readString(META_PATH);
			Gson GSON = new GsonBuilder().setPrettyPrinting().create();
			JsonObject object = GSON.fromJson(metaContent, JsonObject.class);
			object.getAsJsonObject("pack").addProperty("pack_format", packFormat);
			Files.writeString(META_PATH, GSON.toJson(object));
			FogClient.LOGGER.info("Updated pack.mcmeta to pack format {}", packFormat);
		}

		URI jarUrl;

		/*? if fabric {*/
		jarUrl = FogResourceUnpacker.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		/*?}*/

		/*? if forge {*/
		jarUrl = net.minecraftforge.fml.ModList.get().getModFileById("fog").getFile().getFilePath().toUri();
		/*?}*/

		/*? if neoforge {*/
		jarUrl = net.neoforged.fml.ModList.get().getModFileById("fog").getFile().getFilePath().toUri();
		/*?}*/

		try {
			URL resourceUrl = FogResourceUnpacker.class.getClassLoader().getResource("packed");
			if (resourceUrl == null) {
				throw new IllegalArgumentException("Resource not found: packed");
			}

			URI uri = resourceUrl.toURI();

			FogClient.LOGGER.info("Unpacking resources from: {}", uri);

			// Check if running from a JAR file
			try {
				String jarPath = Paths.get(jarUrl).toString();
				FogClient.LOGGER.info("Running from JAR file: {}", jarPath);

				try (JarFile jarFile = new JarFile(jarPath)) {
					Stream<JarEntry> entries = jarFile.stream();
					entries.filter(e -> e.getName().startsWith("packed/"))
					       .forEach(entry -> {
						       Path targetPath = UNPACKED_PATH.resolve(entry.getName().substring("packed/".length()));
							   FogClient.LOGGER.info("Unpacking: {}", targetPath);
						       if (entry.isDirectory()) {
							       try {
								       if (!Files.exists(targetPath)) {
									       Files.createDirectories(targetPath);
								       }
							       } catch (IOException e) {
								       FogClient.LOGGER.error("Failed to create directory: {}", targetPath, e);
							       }
						       } else {
							       if (!Files.exists(targetPath)) {
								       try (InputStream inputStream = jarFile.getInputStream(entry)) {
									       Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
								       } catch (IOException e) {
									       FogClient.LOGGER.error("Failed to copy file: {}", targetPath, e);
								       }
							       }
						       }
					       });
				}
			} catch (Exception ignored) {
				// Running from file system
				Path path = Paths.get(uri);
				Files.walk(path).forEach(sourcePath -> {
					try {
						Path relativePath = path.relativize(sourcePath);
						Path targetPath = UNPACKED_PATH.resolve(relativePath);

						if (Files.isDirectory(sourcePath)) {
							if (!Files.exists(targetPath)) {
								Files.createDirectories(targetPath);
							}
						} else {
							if (!Files.exists(targetPath)) {
								Files.copy(sourcePath, targetPath);
							}
						}
					} catch (IOException e) {
						FogClient.LOGGER.error("Failed to copy file.", e);
					}
				});
			}
		} catch (Exception e) {
			FogClient.LOGGER.error("Failed to unpack resources", e);
		}
	}

	public static void walkNamespaces() {
		try {
			NAMESPACES.clear();
			@NotNull Path assetsFolder = UNPACKED_PATH.resolve("assets");
			if (!assetsFolder.toFile().exists() || !assetsFolder.toFile().isDirectory()) {
				return;
			}

			try (@NotNull var assets = Files.list(assetsFolder)) {
				assets.forEach(namespace -> {
					if (namespace.toFile().isDirectory()) {
						NAMESPACES.add(namespace.getFileName().toString());
						FogClient.LOGGER.info("Found namespace: {}", namespace.getFileName().toString());
					}
				});
			}
		} catch (IOException e) {
			FogClient.LOGGER.error("Error occurred while walking resource pack namespaces: {}", ExceptionUtils.getStackTrace(e));
		}
	}
}
