package dev.imb11.fog.client.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.imb11.fog.client.registry.FogRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
public class FogResourceReloader implements ResourceReloader {
	private static final @NotNull Gson GSON = new Gson();
	private static final @NotNull String JSON_FILE_SUFFIX = ".json";
	private static final @NotNull String FOG_FOLDER_NAME = "fog_definitions";
	private static final @NotNull String TAG_FOLDER_NAME = "tag";
	private static final @NotNull String BIOME_FOLDER_NAME = "biome";
	private static final @NotNull String STRUCTURE_FOLDER_NAME = "structure";

	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> {
			loadFogs(resourceManager, STRUCTURE_FOLDER_NAME, FogRegistry.getStructureFogRegistry());
			loadFogs(resourceManager, BIOME_FOLDER_NAME, FogRegistry.getBiomeFogRegistry());

			synchronizer.whenPrepared(null);
			return null;
		}, applyExecutor);
	}

	private void loadFogs(ResourceManager resourceManager, String folderName, Map<Identifier, CustomFogDefinition> fogRegistry) {
		// TODO: Move into BiomeStructureFogRegistry#register using a client-side resource reload event
		fogRegistry.clear();

		final var jsonFogs = resourceManager.findResources(
				String.format("%s/%s", FOG_FOLDER_NAME, folderName),
				identifier -> identifier.toString().endsWith(JSON_FILE_SUFFIX)
		);
		for (final var jsonFog : jsonFogs.entrySet()) {
			final var jsonFogPath = jsonFog.getKey();
			final var jsonFogPathSplit = jsonFogPath.getPath().replace(JSON_FILE_SUFFIX, "").split("/");
			final var fogIdentifier = new Identifier(jsonFogPath.getNamespace(), jsonFogPathSplit[jsonFogPathSplit.length - 1]);
			if (fogRegistry.containsKey(fogIdentifier)) {
				continue;
			}

			try {
				String content = new String(jsonFog.getValue().getInputStream().readAllBytes());
				JsonElement element = GSON.fromJson(content, JsonElement.class);

				CustomFogDefinition definition = CustomFogDefinition.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow();

				fogRegistry.put(fogIdentifier, definition);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
