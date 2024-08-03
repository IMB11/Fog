package dev.imb11.fog.client.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.imb11.fog.client.FogClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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
		int packFormat = 48;
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

		// Get all files that match glob `packed/assets/*/fog_definitions/**/*.json` within this jar.
		// If a file already exists, we will ignore it.
		String fileGlob = String.format("assets/*/%s/**/*.json", FogResourceReloader.FOG_DEFINITIONS_FOLDER_NAME);
		FogClient.LOGGER.info("Searching for files matching glob: {}", fileGlob);
		List<Path> files = getFilesFromResourceFolder("packed", fileGlob);
		FogClient.LOGGER.info("Found {} files to unpack:", files.size());
		FogClient.LOGGER.info(Arrays.toString(files.toArray()));
		for (Path file : files) {
			var splitFilePath = file.toString().split(String.format("assets\\%s", File.separator));
			if (splitFilePath.length == 0) {
				continue;
			}

			// Create a relative path for the file in the destination directory
			@NotNull String relativePath = String.format("assets%s%s", File.separator, splitFilePath[1]);
			@NotNull Path fullPath = UNPACKED_PATH.resolve(relativePath);

			// Create directories if they don't exist
			try {
				Files.createDirectories(fullPath.getParent());
			} catch (IOException e) {
				FogClient.LOGGER.error(
						"Exception thrown while creating folders for unpacked config resource pack assets (path: {}): {}", UNPACKED_PATH,
						e
				);
			}

			// Copy the file
			if (!Files.exists(fullPath)) {
				Files.copy(file, fullPath);
			}
		}

		walkNamespaces();
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

	public static @NotNull List<Path> getFilesFromResourceFolder(@NotNull String resourceFolder, @NotNull String pattern) throws IOException, URISyntaxException {
		@NotNull List<Path> result = new ArrayList<>();
		// Get the base directory from the resource folder
		@Nullable var resource = FogResourceUnpacker.class.getClassLoader().getResource(resourceFolder);
		if (resource == null) {
			FogClient.LOGGER.error("Resource folder {} not found.", resourceFolder);
			return result;
		}

		@NotNull Path basePath = Paths.get(resource.toURI());
		// Create a PathMatcher for the glob pattern
		@NotNull PathMatcher matcher = getPathMatcher(resource.toURI(), pattern);

		try (@NotNull var stream = Files.walk(basePath)) {
			result = stream
					.filter(Files::isRegularFile)
					.filter(path -> matcher.matches(basePath.relativize(path)))
					.collect(Collectors.toList());
		}

		return result;
	}

	@SuppressWarnings("resource")
	public static @NotNull PathMatcher getPathMatcher(@NotNull URI uri, @NotNull String pattern) {
		FileSystem fs;
		try {
			fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
		} catch (Exception e) {
			try {
				fs = FileSystems.getFileSystem(uri);
			} catch (Exception e2) {
				fs = FileSystems.getDefault();
			}
		}
		return fs.getPathMatcher("glob:" + pattern);
	}
}
