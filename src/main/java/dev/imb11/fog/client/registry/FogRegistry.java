package dev.imb11.fog.client.registry;

import dev.imb11.fog.client.resource.CustomFogDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class FogRegistry {
	private static final CustomFogDefinition.FogColors DEFAULT_BIOME_FOG_COLORS;
	private static final CustomFogDefinition DEFAULT_BIOME_FOG_DEFINITION;
	private static final Map<Identifier, CustomFogDefinition> STRUCTURE_FOG_REGISTRY = new HashMap<>();
	private static final Map<Identifier, CustomFogDefinition> BIOME_FOG_REGISTRY = new HashMap<>();

	static {
		DEFAULT_BIOME_FOG_COLORS = new CustomFogDefinition.FogColors("#add4ff", "#add4ff");
		DEFAULT_BIOME_FOG_DEFINITION = new CustomFogDefinition(1.0f, 1.0f, Optional.of(DEFAULT_BIOME_FOG_COLORS));
	}

	public static Map<Identifier, CustomFogDefinition> getStructureFogRegistry() {
		return STRUCTURE_FOG_REGISTRY;
	}

	public static Map<Identifier, CustomFogDefinition> getBiomeFogRegistry() {
		return BIOME_FOG_REGISTRY;
	}

	public static CustomFogDefinition getBiomeOrDefault(Identifier biomeID) {
		var val = BIOME_FOG_REGISTRY.get(biomeID);

		if(val == null) {
			return DEFAULT_BIOME_FOG_DEFINITION;
		}

		return val;
	}

	public static CustomFogDefinition.FogColors getDefaultBiomeColors() {
		return DEFAULT_BIOME_FOG_COLORS;
	}
}
