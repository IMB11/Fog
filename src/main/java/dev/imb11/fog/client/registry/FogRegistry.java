package dev.imb11.fog.client.registry;

import dev.imb11.fog.client.resource.CustomFogDefinition;


import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class FogRegistry {
	private static final CustomFogDefinition.FogColors DEFAULT_BIOME_FOG_COLORS = new CustomFogDefinition.FogColors("#add4ff", "#181f30");
	private static final CustomFogDefinition.FogColors DEFAULT_CAVE_FOG_COLORS = new CustomFogDefinition.FogColors("#212121", "#101010");
	private static final CustomFogDefinition DEFAULT_BIOME_FOG_DEFINITION = new CustomFogDefinition(1.0f, 1.0f, DEFAULT_BIOME_FOG_COLORS);
	private static final Map<Identifier, CustomFogDefinition> STRUCTURE_TAG_FOG_REGISTRY = new HashMap<>();
	private static final Map<Identifier, CustomFogDefinition> STRUCTURE_FOG_REGISTRY = new HashMap<>();
	private static final Map<Identifier, CustomFogDefinition> BIOME_TAG_FOG_REGISTRY = new HashMap<>();
	private static final Map<Identifier, CustomFogDefinition> BIOME_FOG_REGISTRY = new HashMap<>();

	public static @NotNull Map<Identifier, CustomFogDefinition> getStructureTagFogRegistry() {
		return STRUCTURE_TAG_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getStructureFogRegistry() {
		return STRUCTURE_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getBiomeTagFogRegistry() {
		return BIOME_TAG_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getBiomeFogRegistry() {
		return BIOME_FOG_REGISTRY;
	}

	public static @NotNull CustomFogDefinition getFogDefinitionOrDefault(@NotNull Identifier biomeId) {
		@Nullable var biomeFogDefinition = getBiomeFogRegistry().get(biomeId);
		if (biomeFogDefinition != null) {
			return biomeFogDefinition;
		}

		for (var biomeTagFogEntry : getBiomeTagFogRegistry().entrySet()) {
			if (!biomeTagFogEntry.getKey().equals(biomeId)) {
				continue;
			}

			return biomeTagFogEntry.getValue();
		}

		return DEFAULT_BIOME_FOG_DEFINITION;

	}

	public static @NotNull CustomFogDefinition.FogColors getDefaultBiomeColors() {
		return DEFAULT_BIOME_FOG_COLORS;
	}

	public static @NotNull CustomFogDefinition.FogColors getDefaultCaveColors() {
		return DEFAULT_CAVE_FOG_COLORS;
	}
}
