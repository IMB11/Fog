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
	private static final @NotNull String STRUCTURE_FOLDER_NAME = "structure";
	private static final @NotNull String STRUCTURE_TAGS_FOLDER_NAME = String.format("%s/%s", TAG_FOLDER_NAME, STRUCTURE_FOLDER_NAME);
	private static final @NotNull String BIOME_FOLDER_NAME = "biome";
	private static final @NotNull String BIOME_TAGS_FOLDER_NAME = String.format("%s/%s", TAG_FOLDER_NAME, BIOME_FOLDER_NAME);

	@Override
	public CompletableFuture<Void> reload(
			@NotNull Synchronizer synchronizer, @NotNull ResourceManager resourceManager, @NotNull Profiler prepareProfiler,
			@NotNull Profiler applyProfiler, @NotNull Executor prepareExecutor, @NotNull Executor applyExecutor
	) {
		return CompletableFuture.supplyAsync(() -> {
			loadFogs(resourceManager, STRUCTURE_TAGS_FOLDER_NAME, FogRegistry.getStructureTagFogRegistry());
			loadFogs(resourceManager, BIOME_TAGS_FOLDER_NAME, FogRegistry.getBiomeTagFogRegistry());
			loadFogs(resourceManager, STRUCTURE_FOLDER_NAME, FogRegistry.getStructureFogRegistry());
			loadFogs(resourceManager, BIOME_FOLDER_NAME, FogRegistry.getBiomeFogRegistry());

			synchronizer.whenPrepared(null);
			return null;
		}, applyExecutor);
	}

	private void loadFogs(@NotNull ResourceManager resourceManager, @NotNull String folderName, @NotNull Map<Identifier, CustomFogDefinition> fogRegistry) {
		// TODO: Move into BiomeStructureFogRegistry#register using a client-side resource reload event
		fogRegistry.clear();

		@NotNull var jsonFogs = resourceManager.findResources(
				String.format("%s/%s", FOG_FOLDER_NAME, folderName),
				identifier -> identifier.toString().endsWith(JSON_FILE_SUFFIX)
		);
		for (@NotNull var jsonFog : jsonFogs.entrySet()) {
			@NotNull var jsonFogPath = jsonFog.getKey();
			@NotNull var jsonFogPathSplit = jsonFogPath.getPath().replace(JSON_FILE_SUFFIX, "").split("/");
			@NotNull var fogIdentifier = new Identifier(jsonFogPath.getNamespace(), jsonFogPathSplit[jsonFogPathSplit.length - 1]);
			if (fogRegistry.containsKey(fogIdentifier)) {
				continue;
			}

			try {
				fogRegistry.put(fogIdentifier, CustomFogDefinition.CODEC.parse(
						JsonOps.INSTANCE,
						GSON.fromJson(new String(jsonFog.getValue().getInputStream().readAllBytes()), JsonElement.class)
				).result().orElseThrow());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
