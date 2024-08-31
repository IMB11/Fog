/*? if fabric && >=1.20.6 {*/
package dev.imb11.fog.loaders.fabric.datagen;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.api.providers.CustomFogDefinitionDataProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VanillaFogDefinitionProvider extends CustomFogDefinitionDataProvider {
	public VanillaFogDefinitionProvider(@NotNull FabricDataOutput dataOutput, @NotNull CompletableFuture<RegistryWrapper.WrapperLookup> lookup) {
		super(dataOutput, lookup);
	}

	@Override
	public void acceptBiomes(@NotNull BiConsumer<Identifier, CustomFogDefinition> provider) {
		provider.accept(Identifier.tryParse("sparse_jungle"), CustomFogDefinition.Builder.create()
                 .colors("#7EBAA2", "#1A2122").startMultiplier(0.75f).endMultiplier(1.0f).build());

		provider.accept(Identifier.tryParse("basalt_deltas"), CustomFogDefinition.Builder.create()
				.colors("#36292B", "#36292B").startMultiplier(0.8f).endMultiplier(0.01f).build());

		provider.accept(Identifier.tryParse("crimson_forest"), CustomFogDefinition.Builder.create()
				.colors("#700f18", "#700f18").startMultiplier(0.8f).endMultiplier(0.01f).build());

		provider.accept(Identifier.tryParse("warped_forest"), CustomFogDefinition.Builder.create()
		        .colors("#0f4c4c", "#0f4c4c").startMultiplier(0.8f).endMultiplier(0.01f).build());

		provider.accept(Identifier.tryParse("soul_sand_valley"), CustomFogDefinition.Builder.create()
		        .colors("#005157", "#005157").startMultiplier(1.0f).endMultiplier(0.01f).build());

		provider.accept(Identifier.tryParse("nether_wastes"), CustomFogDefinition.Builder.create()
				.colors("#330808", "#330808").startMultiplier(1.0f).endMultiplier(0.01f).build());
	}

	@Override
	public void acceptBiomeTags(@NotNull BiConsumer<TagKey<Biome>, CustomFogDefinition> provider) {
		provider.accept(ConventionalBiomeTags.IS_JUNGLE, CustomFogDefinition.Builder.create()
		        .colors("#5B9071", "#171E23").startMultiplier(0.15f).endMultiplier(0.35f).build());

		provider.accept(ConventionalBiomeTags.IS_SWAMP, CustomFogDefinition.Builder.create()
				.colors("#BDC8C2", "#5A6459").build());

		provider.accept(ConventionalBiomeTags.IS_BADLANDS, CustomFogDefinition.Builder.create()
				.colors("#BCA199", "#82512E").build());

		provider.accept(ConventionalBiomeTags.IS_DESERT, CustomFogDefinition.Builder.create()
				.colors("#D1C8AB", "#827D6A").build());

		provider.accept(ConventionalBiomeTags.IS_SNOWY, CustomFogDefinition.Builder.create()
				.colors("#DFEBF4", "#687782").build());

		provider.accept(ConventionalBiomeTags.IS_AQUATIC_ICY, CustomFogDefinition.Builder.create()
				.colors("#BDDBE1", "#526C72").build());

		provider.accept(ConventionalBiomeTags.IS_BEACH, CustomFogDefinition.Builder.create()
				.colors("#CBCABD", "#827D6A").build());

		provider.accept(ConventionalBiomeTags.IS_END, CustomFogDefinition.Builder.create()
				.colors("#291d26", "#291d26")
				.startMultiplier(0.5f).endMultiplier(0.5f).build());
	}
}
/*?}*/
