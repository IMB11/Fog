/*? if fabric && >=1.20.6 {*/
package dev.imb11.fog.api.providers;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.client.resource.FogResourceReloader;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static dev.imb11.fog.client.resource.FogResourceReloader.FOG_DEFINITIONS_FOLDER_NAME;

public abstract class CustomFogDefinitionDataProvider extends FabricCodecDataProvider<CustomFogDefinition> {
	private final FabricDataOutput output;

	public CustomFogDefinitionDataProvider(@NotNull FabricDataOutput dataOutput, @NotNull CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
		super(dataOutput, lookup, DataOutput.OutputType.RESOURCE_PACK, FOG_DEFINITIONS_FOLDER_NAME, CustomFogDefinition.CODEC);
		output = dataOutput;
	}

	/**
	 * Accepts all custom fog definitions for biomes.
	 *
	 * @param provider A consumer that accepts the {@link Identifier} of the biome, and {@link CustomFogDefinition} to apply.
	 */
	public void acceptBiomes(@NotNull BiConsumer<Identifier, CustomFogDefinition> provider) {}

	/**
	 * Accepts all custom fog definitions for biome tags.
	 *
	 * @param provider A consumer that accepts the {@link Identifier} of the biome tag, and {@link CustomFogDefinition} to apply.
	 */
	public void acceptBiomeTags(@NotNull BiConsumer<TagKey<Biome>, CustomFogDefinition> provider) {}

	/**
	 * @param provider A consumer that accepts the {@link Identifier} of the structure, and the {@link CustomFogDefinition} to apply.
	 * @deprecated Not yet implemented, structure fog definitions are loaded, but are not applied to the game.
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public void acceptStructures(BiConsumer<Identifier, CustomFogDefinition> provider) {}

	/**
	 * @param provider A consumer that accepts the {@link Identifier} of the structure tag, and {@link CustomFogDefinition} to apply.
	 * @deprecated Not yet implemented, structure fog definitions are loaded, but are not applied to the game.
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public void acceptStructureTags(BiConsumer<TagKey<Structure>, CustomFogDefinition> provider) {}

	protected void configure(BiConsumer<Identifier, CustomFogDefinition> provider, RegistryWrapper.WrapperLookup lookup) {
		acceptBiomes((id, fog) -> provider.accept(id.withPrefixedPath(FogResourceReloader.BIOME_FOLDER_NAME + "/"), fog));
		acceptBiomeTags((tag, fog) -> provider.accept(tag.id().withPrefixedPath(FogResourceReloader.BIOME_TAGS_FOLDER_NAME + "/"), fog));
		acceptStructures((id, fog) -> provider.accept(id.withPrefixedPath(FogResourceReloader.STRUCTURE_FOLDER_NAME + "/"), fog));
		acceptStructureTags(
				(tag, fog) -> provider.accept(tag.id().withPrefixedPath(FogResourceReloader.STRUCTURE_TAGS_FOLDER_NAME + "/"), fog));
	}

	@Override
	public String getName() {
		return "Custom Fog Definition:" + this.output.getModId();
	}
}
/*?}*/
