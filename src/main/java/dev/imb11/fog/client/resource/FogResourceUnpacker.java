package dev.imb11.fog.client.resource;

import dev.imb11.fog.client.FogClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FogResourceUnpacker {
	private static final Path UNPACKED_PATH = FogClient.getConfigFolder().resolve("fog_definitions");
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
		List<Path> files = getFilesFromResourceFolder("assets", "packed/assets/*/fog_definitions/**/*.json");
		for(Path file : files) {
			Path unpackedFile = UNPACKED_PATH.resolve(file.getFileName());
			if(!unpackedFile.toFile().exists()) {
				Files.copy(file, unpackedFile);
			}
		}
	}

	private static List<Path> getFilesFromResourceFolder(String folder, String globPattern) throws IOException, URISyntaxException {
		List<Path> result = new ArrayList<>();
		URL resource = FogResourceUnpacker.class.getClassLoader().getResource(folder);
		if (resource == null) {
			throw new IllegalArgumentException("Resource folder not found: " + folder);
		}

		// Determine if the resource is in a JAR file or filesystem
		if (resource.toURI().getScheme().equals("jar")) {
			// Resource is in a JAR file
			try (FileSystem fileSystem = getOrCreateFileSystem(resource.toURI())) {
				Path resourcePath = fileSystem.getPath(folder);
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourcePath, globPattern)) {
					directoryStream.forEach(result::add);
				}
			}
		} else {
			// Resource is in the filesystem
			Path resourcePath = Paths.get(resource.toURI());
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourcePath, globPattern)) {
				directoryStream.forEach(result::add);
			}
		}

		return result;
	}

	private static FileSystem getOrCreateFileSystem(URI uri) throws IOException {
		try {
			return FileSystems.newFileSystem(uri, java.util.Collections.emptyMap());
		} catch (FileSystemAlreadyExistsException e) {
			return FileSystems.getFileSystem(uri);
		}
	}
}
