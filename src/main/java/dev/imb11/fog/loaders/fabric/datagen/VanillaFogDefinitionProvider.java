/*? if fabric && >1.20.4 {*/
/*package dev.imb11.fog.loaders.fabric.datagen;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.api.providers.CustomFogDefinitionDataProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.function.BiConsumer;

public class VanillaFogDefinitionProvider extends CustomFogDefinitionDataProvider {
	public VanillaFogDefinitionProvider(FabricDataOutput dataOutput) {
		super(dataOutput);
	}

	@Override
	public void acceptBiomes(BiConsumer<Identifier, CustomFogDefinition> provider) {
		provider.accept(Identifier.of("minecraft", "nether_wastes"), CustomFogDefinition.Builder.create()
				.colors("#420000", "#420000").build());

		provider.accept(Identifier.of("minecraft", "basalt_deltas"), CustomFogDefinition.Builder.create()
				.colors("#36292B", "#36292B").build());
	}

	@Override
	public void acceptBiomeTags(BiConsumer<TagKey<Biome>, CustomFogDefinition> provider) {
		provider.accept(ConventionalBiomeTags.JUNGLE, CustomFogDefinition.Builder.create()
				.colors("#35422f", "#252924").build());

		provider.accept(ConventionalBiomeTags.SWAMP, CustomFogDefinition.Builder.create()
				.colors("#393d32", "#272922").build());

		provider.accept(ConventionalBiomeTags.BADLANDS, CustomFogDefinition.Builder.create()
				.colors("#BF6621", "#8e4a19").build());

		provider.accept(ConventionalBiomeTags.DESERT, CustomFogDefinition.Builder.create()
				.colors("#D6C699", "#BFAA7F").build());

		provider.accept(ConventionalBiomeTags.SNOWY, CustomFogDefinition.Builder.create()
				.colors("#F4F9EF", "#F4F9EF").build());

		provider.accept(ConventionalBiomeTags.AQUATIC_ICY, CustomFogDefinition.Builder.create()
				.colors("#bfded7", "#83a099").build());

		provider.accept(ConventionalBiomeTags.BEACH, CustomFogDefinition.Builder.create()
				.colors("#D6C699", "#BFAA7F").build());

		provider.accept(ConventionalBiomeTags.IN_THE_END, CustomFogDefinition.Builder.create()
				.colors("#291d26", "#291d26")
				.startMultiplier(0.5f).endMultiplier(0.5f).build());
	}
}
*//*?}*/
