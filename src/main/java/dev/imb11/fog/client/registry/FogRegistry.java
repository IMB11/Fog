package dev.imb11.fog.client.registry;

import dev.imb11.fog.api.CustomFogDefinition;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FogRegistry {
	private static final Map<TagKey<Structure>, CustomFogDefinition> STRUCTURE_TAG_FOG_REGISTRY = new ConcurrentHashMap<>();
	private static final Map<Identifier, CustomFogDefinition> STRUCTURE_FOG_REGISTRY = new ConcurrentHashMap<>();
	private static final Map<TagKey<Biome>, CustomFogDefinition> BIOME_TAG_FOG_REGISTRY = new ConcurrentHashMap<>();
	private static final Map<Identifier, CustomFogDefinition> BIOME_FOG_REGISTRY = new ConcurrentHashMap<>();
	private static final Map<Identifier, Identifier> TAGGED_BIOME_TO_FOG_CACHE = new ConcurrentHashMap<>();
	private static final Map<Identifier, Set<Identifier>> TAGGED_BIOME_SKIP_LIST = new ConcurrentHashMap<>();

	public static @NotNull Map<TagKey<Structure>, CustomFogDefinition> getStructureTagFogRegistry() {
		return STRUCTURE_TAG_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getStructureFogRegistry() {
		return STRUCTURE_FOG_REGISTRY;
	}

	public static @NotNull Map<TagKey<Biome>, CustomFogDefinition> getBiomeTagFogRegistry() {
		return BIOME_TAG_FOG_REGISTRY;
	}

	public static @NotNull Map<Identifier, CustomFogDefinition> getBiomeFogRegistry() {
		return BIOME_FOG_REGISTRY;
	}

	public static @NotNull CustomFogDefinition getFogDefinitionOrDefault(@NotNull Identifier biomeId, @NotNull ClientWorld world) {
		CustomFogDefinition biomeFogDefinition = getBiomeFogRegistry().get(biomeId);
		if (biomeFogDefinition != null) {
			return biomeFogDefinition;
		}

		Identifier cachedFogId = TAGGED_BIOME_TO_FOG_CACHE.get(biomeId);
		if (cachedFogId != null) {
			return getBiomeTagFogRegistry().get(TagKey.of(RegistryKeys.BIOME, cachedFogId));
		}

		Set<Identifier> skippedTags = TAGGED_BIOME_SKIP_LIST.getOrDefault(biomeId, new HashSet<>());
		Registry<Biome> biomeRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
		for (var biomeTagFogEntry : getBiomeTagFogRegistry().entrySet()) {
			TagKey<Biome> tagKey = biomeTagFogEntry.getKey();
			Identifier tagId = tagKey.id();

			if (skippedTags.contains(tagId)) {
				continue;
			}

			//? if <1.21.2 {
			/*var entryListOptional = biomeRegistry.getEntryList(tagKey);
			*///?} else {
			var entryListOptional = biomeRegistry.getOptional(tagKey);
			//?}
			if (entryListOptional.isPresent()) {
				var entryList = entryListOptional.get();
				for (var entry : entryList) {
					//noinspection OptionalGetWithoutIsPresent
					if (entry.getKey().get().getValue().equals(biomeId)) {
						TAGGED_BIOME_TO_FOG_CACHE.put(biomeId, tagId);
						TAGGED_BIOME_SKIP_LIST.put(biomeId, skippedTags);
						return biomeTagFogEntry.getValue();
					}
				}
				skippedTags.add(tagId);
			}
		}

		TAGGED_BIOME_SKIP_LIST.put(biomeId, skippedTags);
		return CustomFogDefinition.getDefault(world);
	}

	public static void resetCaches() {
		TAGGED_BIOME_TO_FOG_CACHE.clear();
		TAGGED_BIOME_SKIP_LIST.clear();
	}
}
