package dev.imb11.fog.client.resource;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.json.Fogs;
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
	private static final @NotNull String JSON_FILE_SUFFIX = ".json";
	private static final @NotNull String FOG_FOLDER_NAME = "fog";
	private static final @NotNull String TAG_FOLDER_NAME = "tag";
	private static final @NotNull String BIOME_FOLDER_NAME = "biome";
	private static final @NotNull String STRUCTURE_FOLDER_NAME = "structure";

	/**
	 * Asynchronously process and load resource-based data.
	 * The code must be thread-safe and not modify game state!
	 *
	 * @param synchronizer    The {@link Synchronizer} which should be used for this stage.
	 * @param resourceManager The {@link ResourceManager} used during reloading.
	 * @param prepareProfiler The {@link Profiler} which may be used for this stage.
	 * @param applyProfiler   The {@link Profiler} which may be used for this stage.
	 * @param prepareExecutor The {@link Executor} which should be used for this stage.
	 * @param applyExecutor   The {@link Executor} which should be used for this stage.
	 * @return A {@link CompletableFuture} representing the completed result.
	 */
	@Override
	public @NotNull CompletableFuture<Void> reload(@NotNull Synchronizer synchronizer, @NotNull ResourceManager resourceManager, @NotNull Profiler prepareProfiler, @NotNull Profiler applyProfiler, @NotNull Executor prepareExecutor, @NotNull Executor applyExecutor) {
		return CompletableFuture.supplyAsync(() -> {
			loadFogs(resourceManager, STRUCTURE_FOLDER_NAME, FogRegistry.STRUCTURE_FOG_REGISTRY);
			loadFogs(resourceManager, BIOME_FOLDER_NAME, FogRegistry.BIOME_FOG_REGISTRY);

			synchronizer.whenPrepared(null);
			return null;
		}, applyExecutor);
	}

	private void loadFogs(@NotNull ResourceManager resourceManager, @NotNull String folderName, @NotNull Map<Identifier, Fogs> fogRegistry) {
		// TODO: Move into BiomeStructureFogRegistry#register using a client-side resource reload event
		fogRegistry.clear();

		@NotNull final var jsonFogs = resourceManager.findResources(
				String.format("%s/%s", FOG_FOLDER_NAME, folderName),
				identifier -> identifier.toString().endsWith(JSON_FILE_SUFFIX)
		);
		for (@NotNull final var jsonFog : jsonFogs.entrySet()) {
			@NotNull final var jsonFogPath = jsonFog.getKey();
			@NotNull final var jsonFogPathSplit = jsonFogPath.getPath().replace(JSON_FILE_SUFFIX, "").split("/");
			@NotNull final var fogIdentifier = new Identifier(jsonFogPath.getNamespace(), jsonFogPathSplit[jsonFogPathSplit.length - 1]);
			if (fogRegistry.containsKey(fogIdentifier)) {
				continue;
			}

			try {
				fogRegistry.put(fogIdentifier,
						new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(
								jsonFog.getValue().getReader(), Fogs.class)
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
