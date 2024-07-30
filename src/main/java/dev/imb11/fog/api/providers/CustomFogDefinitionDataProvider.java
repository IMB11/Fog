/*? if fabric {*/
package dev.imb11.fog.api.providers;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.FogResourceReloader;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;

import java.util.function.BiConsumer;

public abstract class CustomFogDefinitionDataProvider extends FabricCodecDataProvider<CustomFogDefinition> {
	private final FabricDataOutput output;
	public CustomFogDefinitionDataProvider(FabricDataOutput dataOutput) {
		super(dataOutput, DataOutput.OutputType.RESOURCE_PACK, "fog_definitions", CustomFogDefinition.CODEC);
		output = dataOutput;
	}

	/**
	 * Accepts all custom fog definitions for biomes.
	 * @param provider A consumer that accepts the {@link Identifier} of the biome, and {@link CustomFogDefinition} to apply.
	 */
	public void acceptBiomes(BiConsumer<Identifier, CustomFogDefinition> provider) {};
	
	/**
	 * Accepts all custom fog definitions for biome tags.
	 * @param provider A consumer that accepts the {@link Identifier} of the biome tag, and {@link CustomFogDefinition} to apply.
	 */
	public void acceptBiomeTags(BiConsumer<TagKey<Biome>, CustomFogDefinition> provider) {};

	/**
	 * @deprecated Not yet implemented, structure fog definitions are loaded, but are not applied to the game.
	 * @param provider A consumer that accepts the {@link Identifier} of the structure, and the {@link CustomFogDefinition} to apply.
	 */
	@Deprecated
	public void acceptStructures(BiConsumer<Identifier, CustomFogDefinition> provider) {};

	/**
	 * @deprecated Not yet implemented, structure fog definitions are loaded, but are not applied to the game.
	 * @param provider A consumer that accepts the {@link Identifier} of the structure tag, and {@link CustomFogDefinition} to apply.
	 */
	@Deprecated
	public void acceptStructureTags(BiConsumer<TagKey<Structure>, CustomFogDefinition> provider) {};

	@Override
	protected void configure(BiConsumer<Identifier, CustomFogDefinition> provider) {
		acceptBiomes((id, fog) -> provider.accept(id.withPrefixedPath(FogResourceReloader.BIOME_FOLDER_NAME + "/"), fog));
		acceptBiomeTags((tag, fog) -> provider.accept(tag.id().withPrefixedPath(FogResourceReloader.BIOME_TAGS_FOLDER_NAME + "/"), fog));
		acceptStructures((id, fog) -> provider.accept(id.withPrefixedPath(FogResourceReloader.STRUCTURE_FOLDER_NAME + "/"), fog));
		acceptStructureTags((tag, fog) -> provider.accept(tag.id().withPrefixedPath(FogResourceReloader.STRUCTURE_TAGS_FOLDER_NAME + "/"), fog));
	}

	@Override
	public String getName() {
		return "Custom Fog Definition:" + this.output.getModId();
	}
}
/*? } */
