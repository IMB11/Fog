package dev.imb11.fog.client.resource;

import dev.imb11.fog.client.FogClient;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FogResourceUnpacker {
	public static final Set<String> NAMESPACES = new HashSet<>();
	public static final Path UNPACKED_PATH = FogClient.getConfigFolder().resolve("fog_definitions");
	private static final Path README_PATH = UNPACKED_PATH.resolve("README.txt");
	private static final Path META_PATH = UNPACKED_PATH.resolve("pack.mcmeta");
	public static void tryUnpack() throws IOException, URISyntaxException {
		if(System.getProperty("fabric-api.datagen") != null) return;

		if(!UNPACKED_PATH.toFile().exists()) {
			UNPACKED_PATH.toFile().mkdirs();
		}

		if(!README_PATH.toFile().exists()) {
			Files.writeString(README_PATH, "This folder contains the unpacked fog definitions.\nYou can edit these files to customize the fog in your game.\nFor more information, visit https://docs.imb11.dev/fog/");
		}

		if(!META_PATH.toFile().exists()) {
			Files.writeString(META_PATH, "{\n" +
					"  \"pack\": {\n" +
					"    \"description\": \"An expansive and dynamic overhaul to Minecraft's fog rendering system.\",\n" +
					"    \"pack_format\": 14\n" +
					"  }\n" +
					"}\n");
		}

		// Get all files that match glob `packed/assets/*/fog_definitions/**/*.json` within this jar.
		// If a file already exists, we will ignore it.
		List<Path> files = getFilesFromResourceFolder("packed", "assets/*/fog_definitions/**/*.json");
		for (Path file : files) {
			// Create a relative path for the file in the destination directory
			String relativePath = "assets/" + file.toString().split("assets/")[1];
			Path fullPath = UNPACKED_PATH.resolve(relativePath);

			// Create directories if they don't exist
			Path parentDir = fullPath.getParent();
			if (!Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			// Copy the file
			if (!Files.exists(fullPath)) {
				Files.copy(file, fullPath);
			}
		}

		walkNamespaces();
	}

	public static void walkNamespaces()  {
		try {
			NAMESPACES.clear();
			try (var stream = Files.walk(UNPACKED_PATH)) {
				stream.filter(Files::isRegularFile)
				      .filter(path -> path.toString().endsWith(".json"))
				      .forEach(path -> {
					      String relativePath = path.toString().split(UNPACKED_PATH.toString())[1];
						  String[] split = relativePath.split("/");
					      NAMESPACES.add(split[2]);
				      });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Path> getFilesFromResourceFolder(String resourceFolder, String pattern) throws IOException, URISyntaxException {
		List<Path> result = new ArrayList<>();

		// Get the base directory from the resources folder
		Path basePath = Paths.get(FileUtils.class.getClassLoader().getResource(resourceFolder).toURI());

		// Create a PathMatcher for the glob pattern
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

		try (var stream = Files.walk(basePath)) {
			result = stream
					.filter(Files::isRegularFile)
					.filter(path -> matcher.matches(basePath.relativize(path)))
					.collect(Collectors.toList());
		}

		return result;
	}
}
