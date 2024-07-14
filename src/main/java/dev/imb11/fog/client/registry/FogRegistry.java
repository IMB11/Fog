package dev.imb11.fog.client.registry;

import dev.imb11.fog.client.resource.CustomFogDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FogRegistry {
	private static final CustomFogDefinition.FogColors DEFAULT_BIOME_FOG_COLORS = new CustomFogDefinition.FogColors("#add4ff", "#181f30");
	private static final CustomFogDefinition.FogColors DEFAULT_CAVE_FOG_COLORS = new CustomFogDefinition.FogColors("#212121", "#101010");
	private static final CustomFogDefinition DEFAULT_BIOME_FOG_DEFINITION = new CustomFogDefinition(1.0f, 1.0f, DEFAULT_BIOME_FOG_COLORS);
	private static final Map<Identifier, CustomFogDefinition> STRUCTURE_FOG_REGISTRY = new HashMap<>();
	private static final Map<Identifier, CustomFogDefinition> BIOME_FOG_REGISTRY = new HashMap<>();

	public static @NotNull Map<Identifier, CustomFogDefinition> getStructureFogRegistry() {
		return STRUCTURE_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getBiomeFogRegistry() {
		return BIOME_FOG_REGISTRY;
	}

	public static @NotNull CustomFogDefinition getBiomeFogDefinitionOrDefault(@NotNull Identifier biomeId) {
		var biomeFogDefinition = BIOME_FOG_REGISTRY.get(biomeId);
		if (biomeFogDefinition == null) {
			return DEFAULT_BIOME_FOG_DEFINITION;
		}

		return biomeFogDefinition;
	}

	public static @NotNull CustomFogDefinition.FogColors getDefaultBiomeColors() {
		return DEFAULT_BIOME_FOG_COLORS;
	}

	public static @NotNull CustomFogDefinition.FogColors getDefaultCaveColors() {
		return DEFAULT_CAVE_FOG_COLORS;
	}
}
